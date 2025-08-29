package br.com.microservices.orchestrated.productvalidationservice.core.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data /* ANotation do lombok cria get, set equals e hash code*/
@Builder /*ajuda na construção do objeto order que tem vários parâmetro*/
@NoArgsConstructor /* ANotation do lombok cria construtor sem argumentos*/
@AllArgsConstructor /* ANotation do lombokcria construtor com argumento*/
@Entity /*Para persistir os dados no banco*/
@Table(name = "product")
public class Product  /*Essa classe representa um produto ela só vai guardar
o id e código do produto, vamos ter outra tabela, para validar ser o pedido está valiado
 de acordo com o os dados do produto para proceder na nossa saga*/{


   /*
     Identity faz uma geração automatica do Id gerenciado pela própria aplicação
     Sequence faz uma geração automatica do Id gerenciado pelo banco
     UIDD faz uma geração automatica do Id tendo identificado universal no formato UIID
     Table é manual
     AUTO ele faz de maneira automatica*/
   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Integer id; /*chave primária por padrão já não pode ser nulo*/

    @Column(nullable = false) /*n pode ser nulo*/
     private String code; /*Código/nom do produto*/





}
