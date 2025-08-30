package br.com.microservices.orchestrated.orchestratorservice.core.dto;


import br.com.microservices.orchestrated.orchestratorservice.core.enums.EEventSource;
import br.com.microservices.orchestrated.orchestratorservice.core.enums.ESagaStatus;
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
public class Event {

    private String id;
    private String transactionId; /*id da transação atual*/
    private String orderId; /*id do pedido, n tem necessidade mais é mais facil
    deixar o id do pedido aqui no evento*/
    private Order payload; /*os dados do pedido que será alterado e utilizado par amanipular as regras de negócio*/
    private EEventSource source; /*qual origem do evento */
    private ESagaStatus status; /*status do evento aqui é enum, só usamos em enum em dados que queremos manipular
    para utilizar em regra de negócio e etc*/
    private List<History> eventHistory; /*estamos vinculando um array do historico do evento */
    private LocalDateTime createdAt; /*data de criação do evento*/
}
