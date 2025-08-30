package br.com.microservices.orchestrated.orchestratorservice.core.saga;

import static br.com.microservices.orchestrated.orchestratorservice.core.enums.EEventSource.*;
import static br.com.microservices.orchestrated.orchestratorservice.core.enums.ESagaStatus.*;
import static br.com.microservices.orchestrated.orchestratorservice.core.enums.ETopics.*;

public final /*quando colocamos final quer dizer
que essa classe é unica*/  class SagaHandler {

    private SagaHandler() {
        /*quando eu tenho um construtor privado quer
        * dizer que essa classe nunca vai ser instanciada
        * ou seja ela só vai pode ter métodos ou atributos estáticos*/
    }

    /*Matriz conforme a planilha para Orchestratação dos tópicos
    * vamos ter um método para percorrer essa matriz, porque o nosso evento
    * ele vai vir com um STATUS e um SOURCE, e o orchestrador precisa saber
    * qual tópico ele precisa mandar na sequência para saber isso
    * ele precisa saber qual é o SERVIÇO atual
    *  (primeira coluna do matriz), qual status atual (Segunda coluna da matriz)
    *  e tendo esses valores ele vai saber qual o tópico que ele vai ter que enviar
    * para Orchestrador conseguir mandar para outro serviço */
    public static final Object[] [] SAGA_HANDLER = {

            //ORCHESTRADOR
            {ORCHESTRATOR, SUCCESS, PRODUCT_VALIDATION_SERVICE},/*Orchestrador começo agora está
            com status SUCCESS, ele vai para o PRODUCT_VALIDATION_SERVICE*/
            {ORCHESTRATOR, FAIL, FINISH_FAIL}, /*Orchestrador  está
            com status FAIL, ele vai para o FINISH_FAIL*/

            //PRODUCT VALIDATION SERVICE
            {PRODUCT_VALIDATION_SERVICE, ROLLBACK_PENDING, PRODUCT_VALIDATION_FAIL},/*PRODUCT_VALIDATION_SERVICE
            está  com status  ROLLBACK_PENDING, ele vai mandar o  PRODUCT_VALIDATION_FAIL
            para relizar o rollback no productvalidation Service*/
            {PRODUCT_VALIDATION_SERVICE, FAIL, FINISH_FAIL}, /*PRODUCT_VALIDATION_SERVICE
            já retorno para ORCHESTRADOR com FAIL ou seja já fez o rollback,
             ai ele vai finaliza a SAGA porque então n tem mais oque fazer porque n tem
             outro microsserviço antes dele*/
            {PRODUCT_VALIDATION_SUCCESS, SUCCESS, PAYMENT_SUCCESS}, /*PRODUCT_VALIDATION_SUCCESS
            deu status sucesso, orchestrador manda um tópico para o PAYMENT_SUCCESS*/

            //PAYMENT SERVICE
            {PAYMENT_SERVICE, ROLLBACK_PENDING, PAYMENT_FAIL}, /* Se  PAYMENT_SERVICE
             deu falha ele vai retornar o ROLLBACK_PENDING e o orchestrador
             vai mandar o status de PAYMENT_FAIL para fazer o rollback*/
            {PAYMENT_SUCCESS, FAIL, FINISH_FAIL, PRODUCT_VALIDATION_FAIL}, /*Se o PAYMENT_SERVICE
            deu FAIL e já foi feito o rollback é enviado PRODUCT_VALIDATION_FAIL, fazer o rollback */
            {PAYMENT_SUCCESS, SUCCESS,  INVENTORY_SUCCESS},  /*PAYMENT_SUCCESS
            deu status sucesso, orchestrador manda um tópico para o INVENTORY_SUCCESS*/


            //INVENTORY SERVICE
            {INVENTORY_SERVICE, ROLLBACK_PENDING, INVENTORY_FAIL}, /* Se INVENTORY_SERVICE
             deu falha ele vai retornar o ROLLBACK_PENDING e o orchestrador
             vai mandar o status de INVENTORY_SERVICE para fazer o rollback*/
            {INVENTORY_SERVICE, FAIL, FINISH_FAIL, PAYMENT_FAIL}, /*Se o INVENTORY_SERVICE
            deu FAIL e já foi feito o rollback é enviado PAYMENT_FAIL, fazer o rollback */
            {INVENTORY_SERVICE, SUCCESS,  FINISH_SUCCESS}  /*INVENTORY_SERVICE
            deu status sucesso, retorna FINISH_SUCCESS para o Orchestrador
            finalizar a saga*/
    };


    public static final Integer EVENT_SOURCE_INDEX = 0; /*A origem
     SOURCE que é o nome do serviço fica na coluna 0 da matriz que é a primeira coluna, porque matriz em java
     começa com a coluna zer o*/
    public static final Integer EVENT_STATU_INDEX = 1; /*O STATUS
     do evento fica na coluna 1 da matriz que é a segunda coluna, porque matriz em java
     começa com a coluna 0*/
    public static final Integer TOPIC_INDEX = 2; /*O TÓPICO
     do evento fica na coluna 2 da matriz que é a terceira coluna, porque matriz em java
     começa com a coluna 0*/

    /*Exemplo se o evento for PAYMENT_SERVICE estiver com o status ROLLBACK_PENDING
    * vou devolver(publicar evento no tópico)
    *  para o payment service que o próximo evento dele é o PAYMENT_FAIL
    * payment service vai consumir e devolver para o ORCHESTRADOR com outro status
    * {PAYMENT_SERVICE, ROLLBACK_PENDING, PAYMENT_FAIL}*/

}