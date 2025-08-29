package br.com.microservices.orchestrated.productvalidationservice.core.repositories;

import br.com.microservices.orchestrated.productvalidationservice.core.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Integer>{


    /*Query methods para validar se o produto existe no banco
    * code é o nome do produto*/
    Boolean existsByCode(String code);
}
