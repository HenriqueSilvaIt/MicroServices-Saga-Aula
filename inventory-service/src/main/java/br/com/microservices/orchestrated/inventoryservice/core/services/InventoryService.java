package br.com.microservices.orchestrated.inventoryservice.core.services;

import br.com.microservices.orchestrated.inventoryservice.config.exception.ValidationException;
import br.com.microservices.orchestrated.inventoryservice.core.dto.Event;
import br.com.microservices.orchestrated.inventoryservice.core.dto.History;
import br.com.microservices.orchestrated.inventoryservice.core.dto.Order;
import br.com.microservices.orchestrated.inventoryservice.core.dto.OrderProducts;
import br.com.microservices.orchestrated.inventoryservice.core.enums.ESagaStatus;
import br.com.microservices.orchestrated.inventoryservice.core.model.Inventory;
import br.com.microservices.orchestrated.inventoryservice.core.model.OrderInventory;
import br.com.microservices.orchestrated.inventoryservice.core.producer.KafkaProducer;
import br.com.microservices.orchestrated.inventoryservice.core.repositories.InventoryRepository;
import br.com.microservices.orchestrated.inventoryservice.core.repositories.OrderInventoryRepository;
import br.com.microservices.orchestrated.inventoryservice.core.utils.JsonUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j /*visualiza log*/
@Service
@AllArgsConstructor /*Construtor com todos os atributos*/
public class InventoryService {

    private static final String CURRENT_SOURCE = "INVENTORY_SERVICE";

    private final JsonUtil jsonUtil;

    private final KafkaProducer producer;

    private final InventoryRepository inventoryRepository;

    private final OrderInventoryRepository orderInventoryRepository;

    public void updateInventory(Event event) {
        try {
            checkCurrentValidation(event); /*para evitar eventos duplicado
           e resolver o problema da idempotência*/
            createOrderInventory(event);
            updateInventory(event.getPayload()); /*atualiza o estoque*/
            handleSuccess(event); /*setando evento de sucesso*/

        } catch(Exception e) {
            log.error("Error trying to update inventory: ", e);
            handleFailCurrentNotExecuted(event, e.getMessage()); /*
            passa o status de pendente de rollback por que deu erro
            no inventory e atualiza o histórico do evento
            com informações que deu erro*/
        }
        producer.sendEvent(jsonUtil.toJson(event));

    }


    private void createOrderInventory(Event event) {

        event
                .getPayload()
                .getProducts()
                .forEach(product -> {
                    /*Primeiro vamos encontrar o inventory responsável
                     * por esse produto*/
                    Inventory inventory = findInventoryByProductCode(
                            product.getProduct().getCode()); /*pega o inventário pelo nome do produto produto*/
                    OrderInventory orderInventory = createOrderInventory(event, product, inventory);/*cria
                    as informações do evento e do produto no inventário*/
                    orderInventoryRepository.save(orderInventory); /*salva no banco*/
                });
    }

    private OrderInventory createOrderInventory(Event event, OrderProducts product, Inventory inventory) {

        return OrderInventory
                .builder()
                .inventory(inventory)
                .oldQuantity(inventory.getAvailable()) /*pega a quantidade disponível no inventário, enquanto ainda n atualizamos*/
                .orderQuantity(product.getQuantity()) /*quantidade do produto que tenho no pedido*/
                .newQuantity(inventory.getAvailable() -  product.getQuantity()) /*o novo valor é quantidade disponível subtraindo a quantidade de produtos do pedido atual*/
                .orderId(event.getPayload().getId()) /*id do pedido*/
                .transactionId(event.getTransactionId()) /*id da transação*/
                .build();
    }



    /*colocando informatações de sucesso no evento, após validação do produto*/
    private void handleSuccess(Event event ) {
        event.setStatus(ESagaStatus.SUCCESS); /*coloca a informação de sucesso*/
        event.setSource(CURRENT_SOURCE); /*nome da origem do evento setado
        com o nome do topico do product servic*/
        addHistory(event, "Inventory updated successfully!"); /*
        adicionando histórico ao evento e mensagem de validação de sucesso*/
    }

    /*cria histórico do evento*/
    private void addHistory(Event event, String message) {

        History history = History
                .builder()
                .source(event.getSource())
                .status(event.getStatus())
                .message(message) /*mensagem do parâmetro*/
                .build();

        event.addToHistory(history);
    }

    private void handleFailCurrentNotExecuted(Event event, String message) {

        event.setStatus(ESagaStatus.ROLLBACK_PENDING); /*coloca a informação de rollback no evento*/
        event.setSource(CURRENT_SOURCE); /*nome da origem do evento setado
        com o nome do tópico do product servic*/
        addHistory(event, "Fail to update inventory: ".concat(message)); /*
        adicionando histórico ao evento e mensagem de validação de sucesso*/
    }

    public void rollbackInventory(Event event) {

        event.setStatus(ESagaStatus.FAIL); /*status de erro no evento*/
        event.setSource(CURRENT_SOURCE); /*origem  nome do tópico*/
        try {
            returnInventoryToPreviousValue(event);

            addHistory(event, "Rollback executed for inventory"); /*informando
        que o rollback foi realizado no banco e se for em prod
        informa que também foi realizado na API de integração*/
        } catch(Exception e ) {
            addHistory(event, "Rollback not executed for inventory".concat(e.getMessage())); /*informando
        que o rollback não foi realizado provavelmente
        porque deu uma falha para encontrar o id do evento no banco ou do produto
         ou outra coisa*/
        }

        producer.sendEvent(jsonUtil.toJson(event));

    }

    private void returnInventoryToPreviousValue(Event event) {

        orderInventoryRepository
                .findByOrderIdAndTransactionId(event.getPayload().getId(),
                        event.getTransactionId()) /*busca todos OrderInventory que existe para esse orderId e transactionalId e abaixo
                        para cada evento encontrado  ele restaura a quantidade de produto no estoque por conta do rollback*/
                .forEach(orderInventory -> {
                    Inventory inventory = orderInventory.getInventory();
                    inventory.setAvailable(orderInventory.getOldQuantity());
                    /*aplica o valor anterior do estoque para fazer o rollback*/
                    inventoryRepository.save(inventory);
                    log.info("Restores inventory for oder {} from {} to {} ",
                    event.getPayload().getId(), /*id do pedido*/
                    orderInventory.getNewQuantity(), /*quantidade que usuário
                    inseriu*/
                    inventory.getAvailable());
                });
    }

    private void updateInventory(Order order) {
        /*pegar a lista de produtos no pedido*/
        order
                .getProducts()
                .forEach(product -> {
                    Inventory inventory = findInventoryByProductCode(
                            product.getProduct().getCode()); /*pega o inventário pelo nome do produto produto*/
                        checkInventory(inventory.getAvailable(), product.getQuantity()); /*validar se a quantidade produto solicita existe no estoque*/
                        inventory.setAvailable(inventory.getAvailable() - product.getQuantity());
                        /*atualizando o inventário com a nova quantidad produtos*/
                    inventoryRepository.save(inventory); /* salva no banco
                    a atualização do próprio estoque*/
                });
    }

    /*Método para validar se a quantidade produto solicita existe no estoque*/
    private void checkInventory(Integer available, Integer orderQuantity) {
        if (orderQuantity > available) {
            throw  new ValidationException("Product id out of stock!");

        }
    }

    /*verifica se todos os produtos , o id do pedido e da transação foram informados
     * resolve o problema da idempotencia, para evitar eventos duplicados*/
    private void checkCurrentValidation(Event event) {

        /*Valida se já tem algum evento existente com esses mesmo
         * orderId transactionId(id do evento)*/
        if (orderInventoryRepository.existsByOrderIdAndTransactionId(event.getOrderId(),
                event.getTransactionId())) {
            throw new ValidationException("There's another transactionId for this validation.");

        }

    }

    private Inventory findInventoryByProductCode(String productCode) {
        return inventoryRepository
                .findByProductCode(productCode)
                .orElseThrow(() -> new ValidationException("Inventory" +
                        " not found by informed product."));
    }
}


