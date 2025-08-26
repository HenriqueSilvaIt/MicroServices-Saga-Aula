package br.com.microservices.orchestrated.paymentservice.core.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data /* ANotation do lombok cria get, set equals e hash code*/
@NoArgsConstructor /* ANotation do lombok cria construtor sem argumentos*/
@AllArgsConstructor /* ANotation do lombokcria construtor com argumento*/
public class Product {

        private String code;
        private Double unitValue;
}
