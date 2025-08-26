package br.com.microservices.orchestrated.orchestratorservice.core.producer;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j /*anotação do lombok que instancia a classe Logger do java*/
@Component /*para especificar que essa classe é um componente do spring
e vamos conseguir injetar depêndencia dessa classe*/
@AllArgsConstructor /*constroi construtor padrão e com argumento*/
public class SagaOrquestratorProducer {

    private final KafkaTemplate<String, String> kafkaTemplate; /*esse método
    é um bean da classe KafkaConfig e vamos injetar ele aqui atraves
    do @RequiredArgsConstructor toda classe final precisa
    instanciar um valor para e o @RequiredArgsConstructor  instancia um construtor
      apenas com os valores final que definimos na nossa classe*/


    public void sendEvent(String payload, String topic) /*no orchestrador
    diferente dos outros serviços recebemos o tópico por parâmetro, por que
     quem chamar o producer a lógica que vamos criar vai apontar qual o próximo
     tópico que tem que produzir*/{
        try {
            log.info("Sending event to topic {} with data {}", topic, payload);
            /*envio do evento*/
            kafkaTemplate.send(topic, payload); /*send recebe o nome do tópico e o payload*/
                /*é possível vocÊ trabalhar com partição também (msa o nosso n tem replica nem partições)
                * pssando partição e a chave e valor dela no send
               * send(String topic, Integer partition, Long timestamp, K key, @Nullable V data)*/
        } catch (Exception e ) {
            /*vai mostrar porque deu erro ao enviar o evento*/
            log.error("Error trying to send data to topic {} with data {}", topic, payload,e.getMessage());
        }
    }


}
