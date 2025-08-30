package br.com.microservices.orchestrated.paymentservice.core.model;

import br.com.microservices.orchestrated.paymentservice.core.enums.EPaymentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.security.Identity;
import java.time.LocalDateTime;

@Data /* ANotation do lombok cria get, set equals e hash code*/
@Builder /*ajuda na construção do objeto order que tem vários parâmetro*/
@NoArgsConstructor /* ANotation do lombok cria construtor sem argumentos*/
@AllArgsConstructor /* ANotation do lombokcria construtor com argumento*/
@Entity /*Para persistir os dados no banco*/
@Table(name = "payment")
public class Payment {

    /*
    Identity faz uma geração automatica do Id gerenciado pela própria aplicação
    Sequence faz uma geração automatica do Id gerenciado pelo banco
    UIDD faz uma geração automatica do Id tendo identificado universal no formato UIID
    Table é manual
    AUTO ele faz de maneira automatica*/
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;


    /*Esses dois campos abaixo é para garantir a idempotência do evento
     * ou seja esse evento ele é unico, é impossível esses 2 id ser identifico
     * até porque o transactional Id é formado pelo timestamp
     * isso só vai ocorrer se gerar duplicidade de evento no kafka*/
    @Column(nullable = false) /*n pode ser nulo*/
    private String orderId; /*Código do produto*/
    @Column(nullable = false) /*n pode ser nulo*/
    private String transactionId;

    @Column(nullable = false) /*n pode ser nulo*/
    private Integer totalItems; /*representa a quantidade total
    de itens dos pedido quantos produtos ao todo*/

    @Column(nullable = false) /*n pode ser nulo*/
    private Double totalAmount; /*aqui vai ser multiplicação da quantidade
     de itens pelos valores*/


    @Column(nullable = false) /*n pode ser nulo*/
    @Enumerated(EnumType.STRING) /*o banco de dados n conhece um enum então tem
    que colocar o @Enumerated e o tipo de dados que você vai reprentar esse enum*/
    private EPaymentStatus status; /*status do pagamento*/

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
        status = EPaymentStatus.PENDING; /*status do pagamento
        sempre começa pendente*/
    }

    @PreUpdate/*Sempre que for update algo no banco de dados
    antes de update ele faz as execuções abaixo*/
    public void preUpdated() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }


}
