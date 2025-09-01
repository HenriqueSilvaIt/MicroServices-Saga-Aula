package br.com.microservices.orchestrated.orchestratorservice.core.saga;

import br.com.microservices.orchestrated.orchestratorservice.config.exception.ValidationException;
import br.com.microservices.orchestrated.orchestratorservice.core.dto.Event;
import br.com.microservices.orchestrated.orchestratorservice.core.enums.EEventSource;
import br.com.microservices.orchestrated.orchestratorservice.core.enums.ESagaStatus;
import br.com.microservices.orchestrated.orchestratorservice.core.enums.ETopics;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;

import static br.com.microservices.orchestrated.orchestratorservice.core.enums.ESagaStatus.*;
import static br.com.microservices.orchestrated.orchestratorservice.core.saga.SagaHandler.*;
import static java.lang.String.format;
import static org.springframework.util.ObjectUtils.isEmpty;

@Slf4j
@Component /*Coloca como componente do Spring sem especifica*/
@AllArgsConstructor
public class SagaExecutionController /*Controller
 n quer dizer que essa classe é um endpoint ok, essa aqui é a SEC(Saga
 execution Controler) ele existe tanto no Saga orchestrado como no coreografado

 Essa classe vai percorrer a matriz do SagaHandler para fazer toda lógica de orchestração
 */{

     private final static String SAGA_LOG_ID = "ORDER ID: %s | TRANSACTION ID %s | EVENT ID %s";

     public ETopics getNextTopic(Event event) {

         /*Valida se o SOURCE(ORIGEM) ou STATUS do evento está vazio*/
         if(isEmpty(event.getSource()) || isEmpty(event.getStatus()))  {
            throw new ValidationException(("Source and Status must be informed!"));
         }

        ETopics topic = findTopicBySourceAndStatus(event); /**/
         logCurrentSaga(event, topic); /*mostrando no log informações do topico
         de acordo status encontrado, se for sucesso é uma coisa, rollback é outra, e falha é outra*/
         return topic;
     }


     private ETopics findTopicBySourceAndStatus(Event event) {
     /*casting para ETopics por que estamos
        trabalhando com Object e precisa converter para ETopics que é o que
        o método espera retorna*/    return (ETopics) (Arrays.stream(SAGA_HANDLER) /*converte o arra em uma colections,
        ai vamos percorrer o array matriz como se fosse um string de dados*/
                .filter(row -> isEventSourceAndStatusValid(event, row)) /*filte
                r do JavaCollection sempre retorna um boolena, aqui ele está filtrando
                só os SOURCE e STATUS existente*/
                .map(i -> i[TOPIC_INDEX]) //converte o objeto para o TOPIC INDEX que é o valor do topic
                        .findFirst() // traz o primeiro da lista
                        .orElseThrow(() -> new ValidationException("Topic not found")));

     }

     /*Valida se SOURCE (serviço) STatus (status do evento) existe na matriz Saga
     * handler*/
     private boolean isEventSourceAndStatusValid(Event event,  Object[] row)/*linha
     para percorrer cada linha, como Object [] [] é uma matriz o Object [] é só as linhas*/ {

         /*Recuperando o valor da coluna do SOURCE e STATUS*/

         var source = row[EVENT_SOURCE_INDEX]; /*RECUPERAmos o item da primeira coluna*/
         var status = row[EVENT_STATUS_INDEX]; /*RECUPERAmos o item da segunda coluna*/

         return source.equals(event.getSource()) && status.equals(event.getStatus());/*
         Valida se o  SOURCE (SERVIÇO) e STATUS do evento, é o mesmo status das variaveis
         que passamos acima, que representa a coluna 0 e 1 da matriz*/
     }


     /*Log do oque está acontecendo na saga*/

    private void logCurrentSaga(Event event /*evento atual*/ , ETopics topic /*topico encontrado*/) {

        String sagaId = createSagaId(event); /*pega do evento o Id do pedido, id da transação e o id do evento*/
        var  source = event.getSource();
        switch (event.getStatus())  /*estrutura caso*/{
            case SUCCESS -> log.info("### CURRENT SAGA: {} | SUCCESS | NEXT TOPIC {} | {}",
                    source /*serviços atual*/, topic /*tópico passado*/, sagaId /*composto porId do pedido, id da transação e o id do evento */  );
            case ROLLBACK_PENDING -> log.info("### CURRENT SAGA: {} | SENDING TO ROLLBACK CURRENT SERVICE | NEXT TOPIC {} | {}",
                    source /*serviços atual*/, topic /*tópico passado*/, sagaId /*composto porId do pedido, id da transação e o id do evento */);
            case FAIL -> log.info("### CURRENT SAGA: {} | SENDING TO ROLLBACK PREVIOUS SERVICE | NEXT TOPIC {} | {}",
                    source /*serviços atual*/, topic /*tópico passado*/, sagaId /*composto porId do pedido, id da transação e o id do evento */);
        }
    }

    private String createSagaId(Event event) {

        return format(SAGA_LOG_ID, event.getPayload().getId(), event.getTransactionId(), event.getId());
        /*Tras o Id do pedido, id da transação e o id do evento*/

    }
}
