package br.com.microservices.orchestrated.paymentservice.core.consumer;


import br.com.microservices.orchestrated.paymentservice.core.dto.Event;
import br.com.microservices.orchestrated.paymentservice.core.utils.JsonUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j /*anotação do lombok que instancia a classe Logger do java*/
@Component /*para especificar que essa classe é um componente do spring
e vamos conseguir injetar depêndencia dessa classe*/
@AllArgsConstructor /*constroi construtor padrão e com argumento*/
public class PaymentConsumer {

    private final JsonUtil jsonUtil; /* PRECISa dele
    para fazer a conversão de Json que vamos recuperar do kafk para objeto*/


    /*Método para consumir um evento*/
    @KafkaListener(
            groupId = "${spring.kafka.consumer.group-id}", /*colocamos o group id que está application.yl*/
            topics = "${spring.kafka.topic.payment-success}"
    )
    public void consumeSuccessEvent(String payload) {
        log.info("Receiving success notification event {} from payment-success topic", payload); /*mostrar
        informação que recebemos*/
        Event event = jsonUtil.toEvent(payload); /*converte json recebido para objeto*/
        log.info(event.toString()); /*só para mostrar  o objeto que foi criado*/
    }

    @KafkaListener(
            groupId = "${spring.kafka.consumer.group-id}", /*colocamos o group id que está application.yl*/
            topics = "${spring.kafka.topic.payment-fail}"
    )
    public void consumeFailEvent(String payload) {
        log.info("Receiving rollback event {} from payment-fail topic", payload); /*mostrar
        informação que recebemos*/
        Event event = jsonUtil.toEvent(payload); /*converte json recebido para objeto*/
        log.info(event.toString()); /*só para mostrar  o objeto que foi criado*/
    }


}
