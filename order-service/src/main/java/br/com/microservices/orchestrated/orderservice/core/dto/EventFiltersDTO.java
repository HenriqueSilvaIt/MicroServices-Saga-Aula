package br.com.microservices.orchestrated.orderservice.core.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data /*cria get,set, equals hash code e tostring*/
@NoArgsConstructor /*Construtor  sem argumentos*/
@AllArgsConstructor /*Construtor com argumentos*/
public class EventFiltersDTO { /*Quando fazermos a criação do nosso end point de busca
 vamos precisar de alguns filtros, esse DTO é para filtrar os eventos, para buscarmos
  e visualizar em tela*/

    private String orderId;/*id do pedido*/
    private String transactionId; /*id da transação do pedido*/


}
