package br.com.microservices.orchestrated.inventoryservice.core.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data /* ANotation do lombok cria get, set equals e hash code*/
@NoArgsConstructor /* ANotation do lombok cria construtor sem argumentos*/
@Builder /*ajuda na construção do objeto order que tem vários parâmetro*/
@AllArgsConstructor /* ANotation do lombokcria construtor com argumento*/
@Entity /*Para persistir os dados no banco*/
@Table(name = "order_inventory")
public class OrderInventory {

    /*
    Identity faz uma geração automatica do Id gerenciado pela própria aplicação
    Sequence faz uma geração automatica do Id gerenciado pelo banco
    UIDD faz uma geração automatica do Id tendo identificado universal no formato UIID
    Table é manual
    AUTO ele faz de maneira automatica*/
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne /*muitos OrderInventory para un inventoru*/
    @JoinColumn(name = "inventory_id", nullable = false) /*coluna no banco vai ser inventory_id que vai ser a chave estrangeira
    do inventory nessa entidade, é preciso ter essa informação de qual inventário estamos atualizando
    no nosso pedido*/
    private Inventory inventory; /*Vamos injetar uma chave estrangeira
    desse invetory, para falar que esse pedido está registrando
    uma alteração de inventário de pedido*/

    /*Esses dois campos abaixo é para garantir a idempotência do evento
     * ou seja esse evento ele é unico, é impossível esses 2 id ser identifico
     * até porque o transactional Id é formado pelo timestamp
     * isso só vai ocorrer se gerar duplicidade de evento no kafka*/
    @Column(nullable = false) /*n pode ser nulo*/
    private String orderId; /*Código do produto*/
    @Column(nullable = false) /*n pode ser nulo*/
    private String transactionId;

    @Column(nullable = false) /*n pode ser nulo*/
    private Integer orderQuantity; /*quantidade de itens de pedido no estoque atual
    , vamos criar uma regra para o order nunca ser maior que o old
    porque se o order for maior que o old n vai ter como subtrai */

    @Column(nullable = false) /*n pode ser nulo*/
    private Integer oldQuantity; /*quantidade de itens que tinha no estoque antes
    da nova atualização, se der um rollback
    vamos ter a quantidade antiga no estoque por isso que esse campo é importante també*/

    @Column(nullable = false) /*n pode ser nulo*/
    private Integer newQuantity; /*quantidade de itens que tem  no estoque após
     a nova atualização, o new quantity vai ser
     o orderQuantity - oldQuantity*/


    @Column(nullable = false, updatable = false) /*n pode ser nulo
    updatable false, porque isso registra a data de criação da tabela
    então ela n pode ser atualizada*/
    private LocalDateTime createdAt;

    @Column(nullable = false) /*n pode ser nulo*/
    private LocalDateTime updatedAt;

    @PrePersist/*Sempre que for salvar algo no banco de dados
    antes de salvar ele faz as execuções abaixo*/
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;

    }

    @PreUpdate/*Sempre que for update algo no banco de dados
    antes de update ele faz as execuções abaixo*/
    public void preUpdated() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

}
