package br.com.microservices.orchestrated.inventoryservice.core.services;

import br.com.microservices.orchestrated.inventoryservice.config.exception.ValidationException;
import br.com.microservices.orchestrated.inventoryservice.core.dto.Event;
import br.com.microservices.orchestrated.inventoryservice.core.dto.Order;
import br.com.microservices.orchestrated.inventoryservice.core.dto.OrderProducts;
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
            createOrderInventory();

        } catch(Exception e) {
            log.error("Error trying to update inventory: ", e);
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

    private Inventory findInventoryByProductCode(String productCode) {
        return inventoryRepository
                .findByProductCode(productCode)
                .orElseThrow(() -> new ValidationException("Inventory" +
                        " not found by informed product."));
    }
}


