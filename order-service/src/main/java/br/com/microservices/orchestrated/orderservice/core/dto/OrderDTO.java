package br.com.microservices.orchestrated.orderservice.core.dto;


import br.com.microservices.orchestrated.orderservice.core.document.OrderProducts;
import br.com.microservices.orchestrated.orderservice.core.document.Product;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data /*cria get,set, equals hash code e tostring*/
@NoArgsConstructor /*Construtor  sem argumentos*/
@AllArgsConstructor /*Construtor com argumentos*/
public class OrderDTO {


    private List<Product> products; /*Recebe a lista de produtos*/


}
