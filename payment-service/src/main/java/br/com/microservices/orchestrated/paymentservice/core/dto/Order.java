package br.com.microservices.orchestrated.paymentservice.core.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data /* ANotation do lombok cria get, set equals e hash code*/
@Builder /*ajuda na construção do objeto order que tem vários parâmetro*/
@NoArgsConstructor /* ANotation do lombok cria construtor sem argumentos*/
@AllArgsConstructor /* ANotation do lombokcria construtor com argumento*/
public class Order {

    private String id; /*id do pedido*/
    private List<OrderProducts>  products; /*lista de produto*/
    private LocalDateTime createAt; /*Data de criação do pedido*/
    private String transactionId; /*id da transação atual*/
    private Double totalAmount; /*total do pedido ou venda*/
    private Integer totalItems; /*total de item de pedidos*/


}
