package br.com.microservices.orchestrated.productvalidationservice.core.services;

import br.com.microservices.orchestrated.productvalidationservice.config.exception.ValidationException;
import br.com.microservices.orchestrated.productvalidationservice.core.dto.Event;
import br.com.microservices.orchestrated.productvalidationservice.core.dto.History;
import br.com.microservices.orchestrated.productvalidationservice.core.dto.OrderProducts;
import br.com.microservices.orchestrated.productvalidationservice.core.dto.Product;
import br.com.microservices.orchestrated.productvalidationservice.core.enums.ESagaStatus;
import br.com.microservices.orchestrated.productvalidationservice.core.model.Validation;
import br.com.microservices.orchestrated.productvalidationservice.core.producer.KafkaProducer;
import br.com.microservices.orchestrated.productvalidationservice.core.repositories.ProductRepository;
import br.com.microservices.orchestrated.productvalidationservice.core.repositories.ValidationRepository;
import br.com.microservices.orchestrated.productvalidationservice.core.utils.JsonUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import static org.springframework.util.ObjectUtils.isEmpty;

@Slf4j /*visualiza log*/
@Service
@AllArgsConstructor /*Construtor com todos os atributos*/
public class ProductValidationService {

    private static final String CURRENT_SOURCE = "PRODUCT_VALIDATION_SERVICE";
    /*é um estático porque só vamos usar aqui*/

    private final JsonUtil jsonUtil;


    private final KafkaProducer producer;

    private final ProductRepository productRepository; /*banco de dados do produto*/
    private final ValidationRepository validationRepository; /*para verificar
    se a validação do produto já na foi feita anteriormente*/



    /*Quem vai chamar esse método vai ser o nosso consumer
    *  */
    public void validateExistingProducts(Event event) {

        try{
            //validações

            checkCurrentValidation(event); /*método para validar o evento*/
            createValidation(event, true); /*criar validação no banco de dados para salvar
            o objeto do tipo validation,  o true é só para informar
            que foi com sucesso e colocar essa informação no histórico*/
            handleSuccess(event); /*ação caso a validação de sucesso*/

        } catch(Exception e) {
            log.error("Error trying to validate products: ", e);
            /*caso deu falha aqui n vamos fazer o rollback agora,
            * só vamos fazer o rollback quando retornarmos o evento para orchestrador
            * e o orchestrador dispara o evento rollback*/
            handleFailCurrentNotExecuted(event, e.getMessage()); /*método para lidar com a falha
            do evento atual, recebe o evento e a mensagem de falha
           ele vai mandar um status de falha ROLLBACK_pendente
           para o ORCHESTRADOR e ai Orchestrador vai mandar um
           pedido de rollback que vai ser realizado em outro método rollBack */
        }

        producer.sendEvent(jsonUtil.toJson(event)); /*criar um tópico
        pegando o evento já atualizado
         após a validação e o producer vai mandar esse tópico para orchestrador*/

    }

    /*Verificar os produtos informados*/
    private void validateProductsInformed(Event event) {
        /*Verifica se o payload está com o produto vazio*/
        if( isEmpty(event.getPayload()) || isEmpty(event.getPayload().getProducts())) {
            throw new ValidationException("Product list is empty"); /*informa que está vazio*/
        }
        if( isEmpty(event.getPayload().getId()) || isEmpty(event.getPayload().getTransactionId())) {
            throw new ValidationException("OrderId and TransactionId must be informed!");
        }
    }



    /*verifica se todos os produtos , o id do pedido e da transação foram informados*/
    private void checkCurrentValidation(Event event){
        validateProductsInformed(event); /*valida se as informações
        do produto foram preenchidas*/


        /*Valida se já tem algum evento existente com esses mesmo
         * orderId transactionId(id do evento)*/
        if (validationRepository.existsByOrderIdAndTransactionId(event.getOrderId(),
                event.getTransactionId())) {

        throw new ValidationException("There's another transactionId for this validation.");

        }

        event.getPayload().getProducts().forEach(product -> {
            validateProductInformed(product); /*verifica se o produto
            dentro do oder product for informado*/
            validateExistingProduct(product.getProduct().getCode());/*verifica se o produto
            dentro do oder product  se o nome code esse campo
            do produto foi informado for informado*/


        });

       /* for(Product e : event.getPayload().getProducts()) {
        }*/
     }


    /*valida se o nome do produto ou outros campos for informado*/
    public void validateProductInformed(OrderProducts products) {

        if (isEmpty(products.getProduct()) || isEmpty(products.getProduct().getCode())) {
            throw new ValidationException("Product must be informed!");
        }

     }

        /*validata se o produto existe*/
    public void validateExistingProduct(String code) {
        if (productRepository.existsByCode(code)){
            throw new ValidationException("Product does not exists in database");
        }

    }

    /*cria objeto de  validação*/
    private void createValidation(Event event, boolean success) {
        Validation entity = Validation
                .builder()
                .orderId(event.getPayload().getId())
                .transactionId(event.getTransactionId())
                .success(success) /*é o que estaos passand por parametro*/
                .build();

                validationRepository.save(entity); /*Salva validação*/
    }

    /*colocando informatações de sucesso no evento, após validação do produto*/
    private void handleSuccess(Event event ) {
        event.setStatus(ESagaStatus.SUCCESS); /*coloca a informação de sucesso*/
        event.setSource(CURRENT_SOURCE); /*nome da origem do evento setado
        com o nome do topico do product servic*/
        addHistory(event, "Products was validated successfully"); /*
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
        addHistory(event, "Fail to validate products: ".concat(message)); /*
        adicionando histórico ao evento e mensagem de validação de sucesso*/
    }

    public void rollbackEvent(Event event) {

        changeValidationToFail(event); /*coloca a validação de produtos
        como falso*/
        event.setStatus(ESagaStatus.FAIL);/*coloca a informação de rollback no evento*/ event.setSource(CURRENT_SOURCE); /*nome da origem do evento setado
        com o nome do tópico do product servic*/
        event.setSource(CURRENT_SOURCE); /*nome da origem do evento setado
        com o nome do tópico do product servic*/
        addHistory(event, "Rollback executed on product validation"); /*
        adicionando histórico ao evento e mensagem de validação de sucesso*/
        producer.sendEvent(jsonUtil.toJson(event)); /*envia o evento
        de volta  atualizado para o producer*/

    }

    private void changeValidationToFail(Event event) {
        validationRepository
                .findByOrderIdAndTransactionId(
                        event.getPayload().getId(), event.getTransactionId())
                /*como a query acima retorna um optinal
                se retorna um optinal vazio nós conseguimos lançar uma função com
                o  ifPresentOrElse do optional*/
                .ifPresentOrElse(validationEntity -> {/*valida se já existe um validation
                 dentro do bancoo se existir, ele vai atualizar o status para false apontando que falho
                 e salvar no banco:*/
                    validationEntity.setSuccess(false); /*isso quer dizer
                    que ele encontrou uma entidade validação no banco
                    e estamos setando essa validação no banco como falso
                    porque deu erro*/
                    validationRepository.save(validationEntity);/*depois salvams no
                    banco sucesso*/
                },
                        () ->{ /*se n existir esse validation no banco ele vai criar um
                        já setando como status de erro e salvar no banco lá dentro
                        do método createValidation */

                        createValidation(event, false); /*cria objeto de
                        validação e seta ele como falso*/
                        });
    }

















}
