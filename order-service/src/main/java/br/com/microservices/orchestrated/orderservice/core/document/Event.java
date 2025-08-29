    package br.com.microservices.orchestrated.orderservice.core.document;


    import lombok.AllArgsConstructor;
    import lombok.Builder;
    import lombok.Data;
    import lombok.NoArgsConstructor;
    import org.springframework.data.annotation.Id;
    import org.springframework.data.mongodb.core.mapping.Document;

    import java.time.LocalDateTime;
    import java.util.List;

    @Data /* ANotation do lombok cria get, set equals e hash code*/
    @Builder /*ajuda na construção do objeto order que tem vários parâmetro*/
    @NoArgsConstructor /* ANotation do lombok cria construtor sem argumentos*/
    @AllArgsConstructor /* ANotation do lombokcria construtor com argumento*/
    /*Anotações do mongodb*/
    @Document(collection = "event") /*aqui passamos o nome da coleção do mondo no mongo
    db*/
    public class Event {

        @Id /*aponta qual é o campo identificador, que id*/
        private String id; // id no mongo db é string
        private String transactionId; /*id da transação atual*/
        private String orderId; /*id do pedido, n tem necessidade mais é mais facil
        deixar o id do pedido aqui no evento*/
        private Order payload; /*os dados do pedido que será alterado e utilizado par amanipular as regras de negócio*/
        private String source; /*qual origem do evento */
        private String status; /*status do evento*/
        private List<History> eventHistory; /*estamos vinculando um array do historico do evento */
        private LocalDateTime createdAt; /*data de criação do evento*/
    }
