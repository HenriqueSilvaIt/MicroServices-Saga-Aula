package br.com.microservices.orchestrated.orchestratorservice.core.services;

import br.com.microservices.orchestrated.orchestratorservice.core.dto.Event;
import br.com.microservices.orchestrated.orchestratorservice.core.dto.History;
import br.com.microservices.orchestrated.orchestratorservice.core.enums.EEventSource;
import br.com.microservices.orchestrated.orchestratorservice.core.enums.ESagaStatus;
import br.com.microservices.orchestrated.orchestratorservice.core.enums.ETopics;
import br.com.microservices.orchestrated.orchestratorservice.core.producer.SagaOrchestratorProducer;
import br.com.microservices.orchestrated.orchestratorservice.core.saga.SagaExecutionController;
import br.com.microservices.orchestrated.orchestratorservice.utils.JsonUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static br.com.microservices.orchestrated.orchestratorservice.core.enums.ETopics.NOTIFY_ENDING;

@Slf4j
@Service
@AllArgsConstructor
public class OrchestratorService {
    /*essa classe vai enviar os eventos para os producers , ele vai chamar o SagaExecutionController
     e vai ser chamado pelo SagaOrchestratorConsumer*/

    private final JsonUtil jsonUtil;
    private final SagaOrchestratorProducer producer;
    private final SagaExecutionController sagaExecutionController;


    public void startSaga(Event event) {
        event.setSource(EEventSource.ORCHESTRATOR);/*SOURCE do orchestrator
        porque o evento começa no nosso orchestrador*/
        event.setStatus(ESagaStatus.SUCCESS); /*Como o evento está
        iniciando ainda ele começa com sucesos*/
        ETopics topic = getTopic(event); /*ele vai pegar tópico e mandar
        para o  PRODUCT-SERVICE VALIDATION que é o próximo da SAGA*/
        log.info("SAGA STARTED!");
        addHistory(event, "Saga started!");
        sendToProducerWithTopic(event, topic);/*Aqui
        é o único producer que tem que pegar o tópico
        que é o producer do Orchestrador porque ele manda para vários
        tópico, estamos pegando o tóico do Saga Execution Controler conforme
        os tópicos apontados no SAGA HANDLER e estamos colocando .getTopic porque
        o tópico é um enum e temos que pegar o tipo String que é o parâmetro
        que está no enum*/
    }

    public void finishSagaSuccess(Event event) {
        event.setSource(EEventSource.ORCHESTRATOR);/*SOURCE do orchestrator
        porque o evento começa no nosso orchestrador*/
        event.setStatus(ESagaStatus.SUCCESS); /*Como o evento está
        iniciando ainda ele começa com sucesos*/
        log.info("SAGA FINISH SUCCESSFULLY FOR EVENT {}", event.getId());
        addHistory(event, "Saga finish successfully!");

       notifyFinishedSaga(event); /*saga finalizada mandando de volta para
       o order service com producer*/
    }

    public void finishSagaFail(Event event) {
        event.setSource(EEventSource.ORCHESTRATOR);/*SOURCE do orchestrator
        porque o evento começa no nosso orchestrador*/
        event.setStatus(ESagaStatus.FAIL); /*Como o evento está
        iniciando ainda ele começa com sucesos*/
        log.info("SAGA FINISH WITH ERRORS FOR EVENT {}", event.getId());
        addHistory(event, "Saga finished with errors!");

        notifyFinishedSaga(event); /*saga finalizada mandando de volta para
       o order service com producer*/
    }


    /*continuar nossa saga*/
    public void continueSaga(Event event) {
        ETopics topic = getTopic(event);
        sendToProducerWithTopic(event, topic);

    }

    private ETopics getTopic(Event event) {
        return sagaExecutionController.getNextTopic(event); /*pega
        o tópico do evento de acordo com o SOURCE e status que o
        ORDER SERVICE vai passar para o orchestrador*/
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


    private void sendToProducerWithTopic(Event event, ETopics topic) {
        producer.sendEvent(jsonUtil.toJson(event), topic.getTopic());

    }

    /*Finalizou a Saga com sucesso ou com falha
    * o único lugar que vai mandar agora é para o notifyEnding para voltar
    * para o service com saga finalizada*/
    private void notifyFinishedSaga(Event event) {
        producer.sendEvent(jsonUtil.toJson(event), NOTIFY_ENDING.getTopic());
        /*Aqui
        é o único producer que tem que pegar o tópico
        que é o producer do Orchestrador porque ele manda para vários
        tópico, estamos pegando o tóico do Saga Execution Controler conforme
        os tópicos apontados no SAGA HANDLER e estamos colocando .getTopic porque
        o tópico é um enum e temos que pegar o tipo String que é o parâmetro
        que está no enum*/;
    }
}
