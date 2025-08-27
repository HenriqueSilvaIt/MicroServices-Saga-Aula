package br.com.microservices.orchestrated.orderservice.core.consumer;

import br.com.microservices.orchestrated.orderservice.core.document.Event;
import br.com.microservices.orchestrated.orderservice.core.utils.JsonUtil;
import io.swagger.v3.core.util.Json;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j /*anotação do lombok que instancia a classe Logger do java*/
@Component /*para especificar que essa classe é um componente do spring
e vamos conseguir injetar depêndencia dessa classe*/
@AllArgsConstructor /*constroi construtor padrão e com argumento*/
public class EventConsumer {

    private final JsonUtil jsonUtil; /* PRECISa dele
    para fazer a conversão de Json que vamos recuperar do kafk para objeto*/


    /*Método para consumir um evento*/
    @KafkaListener(
            groupId = "${spring.kafka.consumer.group-id}", /*colocamos o group id que está application.yl*/
            topics = "${spring.kafka.topic.notify-ending}"
    )
    public void consumeNotifyEndingEvent(String payload) {
        log.info("Receiving ending notification event {} from notify-ending topic", payload); /*mostrar
        informação que recebemos*/
        Event event = jsonUtil.toEvent(payload); /*converte json recebido para objeto*/
        log.info(event.toString()); /*só para mostrar  o objeto que foi criado*/
    }
}
