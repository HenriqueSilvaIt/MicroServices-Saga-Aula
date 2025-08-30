package br.com.microservices.orchestrated.orchestratorservice.core.consumer;


import br.com.microservices.orchestrated.orchestratorservice.core.dto.Event;
import br.com.microservices.orchestrated.orchestratorservice.core.services.OrchestratorService;
import br.com.microservices.orchestrated.orchestratorservice.utils.JsonUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j /*anotação do lombok que instancia a classe Logger do java*/
@Component /*para especificar que essa classe é um componente do spring
e vamos conseguir injetar depêndencia dessa classe*/
@AllArgsConstructor /*constroi construtor padrão e com argumento*/
public class SagaOrchestratorConsumer {

    private final OrchestratorService orchestratorService;

    private final JsonUtil jsonUtil; /* PRECISa dele
    para fazer a conversão de Json que vamos recuperar do kafk para objeto*/


    /*Método para consumir um evento*/
    @KafkaListener(
            groupId = "${spring.kafka.consumer.group-id}", /*colocamos o group id que está application.yl*/
            topics = "${spring.kafka.topic.start-saga}"
    )
    public void consumeStartSagaEvent(String payload) {
        log.info("Receiving ending notification event {} from start-saga topic", payload); /*mostrar
        informação que recebemos*/
        Event event = jsonUtil.toEvent(payload); /*converte json recebido para objeto*/
        log.info(event.toString()); /*só para mostrar  o objeto que foi criado*/
        orchestratorService.startSaga(event);
    }

    @KafkaListener(
            groupId = "${spring.kafka.consumer.group-id}", /*colocamos o group id que está application.yl*/
            topics = "${spring.kafka.topic.orchestrator}"
    )
    public void consumeOrchestratorEvent(String payload) {
        log.info("Receiving ending notification event {} from orchestrator topic", payload); /*mostrar
        informação que recebemos*/
        Event event = jsonUtil.toEvent(payload); /*converte json recebido para objeto*/
        log.info(event.toString()); /*só para mostrar  o objeto que foi criado*/
        orchestratorService.continueSaga(event);
    }

    @KafkaListener(
            groupId = "${spring.kafka.consumer.group-id}", /*colocamos o group id que está application.yl*/
            topics = "${spring.kafka.topic.finish-success}"
    )
    public void consumeFinishSuccessEvent(String payload) {
        log.info("Receiving ending notification event {} from finish-success topic", payload); /*mostrar
        informação que recebemos*/
        Event event = jsonUtil.toEvent(payload); /*converte json recebido para objeto*/
        log.info(event.toString()); /*só para mostrar  o objeto que foi criado*/

        orchestratorService.finishSagaSuccess(event);

    }

    @KafkaListener(
            groupId = "${spring.kafka.consumer.group-id}", /*colocamos o group id que está application.yl*/
            topics = "${spring.kafka.topic.finish-fail}"
    )
    public void consumeFinishFailEvent(String payload) {
        log.info("Receiving ending notification event {} from finish-fail topic", payload); /*mostrar
        informação que recebemos*/
        Event event = jsonUtil.toEvent(payload); /*converte json recebido para objeto*/
        log.info(event.toString()); /*só para mostrar  o objeto que foi criado*/

        orchestratorService.finishSagaFail(event);

    }
}
