package br.com.microservices.orchestrated.orchestratorservice.config.kafka;

import br.com.microservices.orchestrated.orchestratorservice.core.enums.ETopics;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.*;

import java.util.HashMap;
import java.util.Map;


@EnableKafka /*Anotation do Spring Kafka que habilita todos os recursos
do Kafka na aplicação*/
@Configuration /* anottation indica ao spring que essa classe vai consumir configurações
durante a subida do contexto da aplicação

nessa classe de configuração apontamos o
consumer group-id e auto-offeset-reset que definimos no applicatio.yl do projeto*/
@RequiredArgsConstructor()
public class KafkaConfig {


    private static final Integer PARTITION_COUNT = 1; /*Qtd de partições estátic*/
    private static final Integer REPLICA_COUNT = 1; /*Qtd de replica*/


    @Value("${spring.kafka.bootstrap-servers}") /*aqui passamos o caminho da propriedade dentro do application.yl
     se tiver variavel de ambiente ele vai dar prioridade ao valor dela*/
    private String boostrapServers; /*é o mesmo que passamos no applicatiom.yl*/

    @Value("${spring.kafka.consumer.group-id}") /*aqui passamos o caminho da propriedade dentro do application.yl
    se tiver variavel de ambiente ele vai dar prioridade ao valor dela*/
    private String grouId;

    @Value("${spring.kafka.consumer.auto-offset-reset}") /*aqui passamos o caminho da propriedade dentro do application.yl
     se tiver variavel de ambiente ele vai dar prioridade ao valor dela*/
    private String autoOffsetReset;

    @Bean /*Componente do Kafka para o Consumer*/
    public ConsumerFactory<String, String> consumerFactory() {
        /* Cosumer Facotory recebe chave valor é uma classe do Spring kafka*/

        return new DefaultKafkaConsumerFactory<>(consumerProps()); /*é um tipo uma implementação do ConsumerFactory
        vamos ter que definir propridades para ele no método consumerProps abaixo*/

    }

    private Map<String, Object> consumerProps() {
        /* Map<String, Object> também daria*/
        var props = new HashMap<String, Object>(); /*variavel recebendo hashMap*/

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, boostrapServers); /*chave
            estamos pegando o nome do servidor da próprio classe ConsumerConfig, e informando o
            servidor do kafka como valor*/
        props.put(ConsumerConfig.GROUP_ID_CONFIG, grouId);
             /*chave
            estamos pegando o nome do servidor da próprio classe ConsumerConfig, e informando o
            servidor do kafka como valor*/
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class); /*
            para enviar ou consumir do Kafka temos que informar o tipo
             de dados que estamos serializando no caso de em envio(producer) para o kafka
             ou deserializando no caso de um Consumer, então se estamos consumindo
             string, tenho que especificar que é string se for outro tipo tem que
             especificar

             KEY_DESERIALIZER_CLASS_CONFIG aqui é chave que vai ser automtica ou nula
             n vamos definir

             StringDeserializer.class é uma classe do Kafka*/
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class); /*
            para enviar ou consumir do Kafka temos que informar o tipo
             de dados que estamos serializando no caso de em envio(producer) para o kafka
             ou deserializando no caso de um Consumer, então se estamos consumindo
             string, tenho que especificar que é string se for outro tipo tem que
             especificar

             VALUE_DESERIALIZER_CLASS_CONFIG aqui é a chave do valor do payload json
             que vamos estar consumindo

             StringDeserializer.class é uma classe do Kafka*/
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);/*chave
            estamos pegando o nome do servidor da próprio classe ConsumerConfig, e informando o
            servidor do kafka como valor no application.ymlr*/


        return props;
    }


    @Bean /*Componente do Kafka para o producer*/
    public ProducerFactory<String, String> producerFactory() {
        return new DefaultKafkaProducerFactory<>(producerProps()); /*é um tipo uma implementação do ConsumerFactory
        vamos ter que definir propridades para ele no método productProps abaixo*/
    }

    private Map<String, Object> producerProps() {
        /* Map<String, Object> também daria*/
        var props = new HashMap<String, Object>(); /*variavel recebendo hashMap*/

        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, boostrapServers); /*chave
            estamos pegando o nome do servidor da próprio classe ProducerConfig, e informando o
            servidor do kafka como valor*/
             /*chave
            estamos pegando o nome do servidor da próprio classe ProducerConfig, e informando o
            servidor do kafka como valor*/
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringDeserializer.class); /*
            para enviar ou consumir do Kafka temos que informar o tipo
             de dados que estamos serializando no caso de em envio(producer) para o kafka
             ou deserializando no caso de um Consumer, então se estamos consumindo
             string, tenho que especificar que é string se for outro tipo tem que
             especificar

             KEY_DESERIALIZER_CLASS_CONFIG aqui é chave que vai ser automtica ou nula
             n vamos definir
produzindo, é serializar, porque vamos estar serializando a informação
             StringDeserializer.class é uma classe do Kafka*/
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringDeserializer.class); /*
            para enviar ou consumir do Kafka temos que informar o tipo
             de dados que estamos serializando no caso de em envio(producer) para o kafka
             ou deserializando no caso de um Consumer, então se estamos consumindo
             string, tenho que especificar que é string se for outro tipo tem que
             especificar

             VALUE_SERIALIZER_CLASS_CONFIG  aqui é a chave do valor do payload json
             que vamos estar produzindo, é serializar, porque vamos estar serializando a informação

             StringDeserializer.class é uma classe do Kafka*/


        /*auto-offset-reset e group id é configurações só de consumo
         * quando vamos publicar o tópico n precisamos especificar o grop id
         * porque publicamos no tópico e o tópico faz a divisão para os grups*/

        return props;
    }


    /*Classe KafkaTemplate que permite instanciar no código e de fato publicar um evento em um tópico*/
    @Bean
    public KafkaTemplate<String, String> kafkaTemplate(ProducerFactory<String, String> producerFactory) {

        return new KafkaTemplate<>(producerFactory); /*como criamos o bean producerFactory
         aqui na classe de configuração, aqui estamos injetando essas configurações no KafkaTemplate
         que ai quando formos dar um  producerFactory.send("") ele já vai vir com toda essa configuração*/
    }

    /*Método de builde(construtor) de tópico */
    private NewTopic buildTopic(String name) {
        return TopicBuilder
                .name(name) /*define o nome do tópico*/
                .replicas(REPLICA_COUNT) /*define o replica do tópico, criamos
                um atributo estático para passar esse valor nesse
                caso estamos passando só 1 replica para o tópico*/
                .partitions(PARTITION_COUNT) /*define o replica do tópico, criamos
                um atributo estático para passar esse valor nesse
                caso estamos passando só 1 replica para o tópico*/
                .build();/*define o nome do tópico*/
    }

    @Bean
    public NewTopic startSagaTopic() {
        return buildTopic(ETopics.START_SAGA.getTopic()); /*pegando
        tópico do enum e o enum tem o valor com o nome do tópico
        no application.yl ele puxa de la, tem como importar o tópico de maneira estática
         sem passar o nome da classe ETopics do Enu*/

    }

    @Bean
    public NewTopic orchestratorTopic() {
        return buildTopic(ETopics.BASE_ORCHESTRATOR.getTopic()); /*pegando
        tópico do enum e o enum tem o valor com o nome do tópico
        no application.yl ele puxa de la, tem como importar o tópico de maneira estática
         sem passar o nome da classe ETopics do Enu*/

    }

    @Bean
    public NewTopic finishSuccessTopic() {
        return buildTopic(ETopics.FINISH_SUCCESS.getTopic()); /*pegando
        tópico do enum e o enum tem o valor com o nome do tópico
        no application.yl ele puxa de la, tem como importar o tópico de maneira estática
         sem passar o nome da classe ETopics do Enu*/

    }

    @Bean
    public NewTopic finishFailTopic() {
        return buildTopic(ETopics.FINISH_FAIL.getTopic()); /*pegando
        tópico do enum e o enum tem o valor com o nome do tópico
        no application.yl ele puxa de la, tem como importar o tópico de maneira estática
         sem passar o nome da classe ETopics do Enu*/

    }

    @Bean
    public NewTopic productValidationSuccessTopic() {
        return buildTopic(ETopics.PRODUCT_VALIDATION_SUCCESS.getTopic()); /*pegando
        tópico do enum e o enum tem o valor com o nome do tópico
        no application.yl ele puxa de la, tem como importar o tópico de maneira estática
         sem passar o nome da classe ETopics do Enu*/

    }

    @Bean
    public NewTopic productValidationFailTopic() {
        return buildTopic(ETopics.PRODUCT_VALIDATION_FAIL.getTopic()); /*pegando
        tópico do enum e o enum tem o valor com o nome do tópico
        no application.yl ele puxa de la, tem como importar o tópico de maneira estática
         sem passar o nome da classe ETopics do Enu*/

    }


    @Bean
    public NewTopic paymentSuccessTopic() {
        return buildTopic(ETopics.PAYMENT_SUCCESS.getTopic()); /*pegando
        tópico do enum e o enum tem o valor com o nome do tópico
        no application.yl ele puxa de la, tem como importar o tópico de maneira estática
         sem passar o nome da classe ETopics do Enu*/

    }
    @Bean
    public NewTopic paymentFailTopic() {
        return buildTopic(ETopics.PAYMENT_FAIL.getTopic()); /*pegando
        tópico do enum e o enum tem o valor com o nome do tópico
        no application.yl ele puxa de la, tem como importar o tópico de maneira estática
         sem passar o nome da classe ETopics do Enu*/

    }

    @Bean
    public NewTopic inventorySuccessTopic() {
        return buildTopic(ETopics.INVENTORY_SUCCESS.getTopic()); /*pegando
        tópico do enum e o enum tem o valor com o nome do tópico
        no application.yl ele puxa de la, tem como importar o tópico de maneira estática
         sem passar o nome da classe ETopics do Enu*/

    }
    @Bean
    public NewTopic inventoryFailTopic() {
        return buildTopic(ETopics.INVENTORY_FAIL.getTopic()); /*pegando
        tópico do enum e o enum tem o valor com o nome do tópico
        no application.yl ele puxa de la, tem como importar o tópico de maneira estática
         sem passar o nome da classe ETopics do Enu*/

    }

    @Bean
    public NewTopic notifyEndingTopic() {

        return buildTopic(ETopics.NOTIFY_ENDING.getTopic());
    }
}
