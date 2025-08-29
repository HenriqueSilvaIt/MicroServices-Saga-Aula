package br.com.microservices.orchestrated.orderservice.core.document;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data /* ANotation do lombok cria get, set equals e hash code*/
@Builder /*ajuda na construção do objeto order que tem vários parâmetro
podemos usar ele para passar os atributos no service*/
@NoArgsConstructor /* ANotation do lombok cria construtor sem argumentos*/
@AllArgsConstructor /* ANotation do lombokcria construtor com argumento*/

/*Anotações do mongodb*/
@Document(collection = "order") /*aqui passamos o nome da coleção do mondo no mongo
db*/
public class Order {

    @Id /*aponta qual é o campo identificador, que id*/
    private String id; /*id do pedido*/
    private List<OrderProducts>  products; /*lista de produto*/
    private LocalDateTime createAt; /*Data de criação do pedido*/
    private String transactionId; /*id da transação atual*/
    private Double totalAmount; /*total do pedido ou venda*/
    private Integer totalItems; /*total de item de pedidos*/


}
