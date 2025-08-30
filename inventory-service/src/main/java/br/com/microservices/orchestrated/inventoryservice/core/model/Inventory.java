package br.com.microservices.orchestrated.inventoryservice.core.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data /* ANotation do lombok cria get, set equals e hash code*/
@NoArgsConstructor /* ANotation do lombok cria construtor sem argumentos*/
@AllArgsConstructor /* ANotation do lombokcria construtor com argumento*/
@Entity /*Para persistir os dados no banco*/
@Table(name = "inventory")
public class Inventory {

    /*
    Identity faz uma geração automatica do Id gerenciado pela própria aplicação
    Sequence faz uma geração automatica do Id gerenciado pelo banco
    UIDD faz uma geração automatica do Id tendo identificado universal no formato UIID
    Table é manual
    AUTO ele faz de maneira automatica*/
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable=false) /*não pode ser nulo*/
    private String productCode;

    @Column(nullable=false) /*não pode ser nulo*/
    private Integer available;



}
