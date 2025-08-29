package br.com.microservices.orchestrated.productvalidationservice.core.repositories;

import br.com.microservices.orchestrated.productvalidationservice.core.model.Product;
import br.com.microservices.orchestrated.productvalidationservice.core.model.Validation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ValidationRepository extends JpaRepository<Validation, Integer>{


    /*Validações para garantir que n tenha já uma validação existente para
    * o orderId ou transactionalId*/

    //Verifica se existe já uma validação para esse pedido e transação
    Boolean existsByOrderIdAndTransactionId(String orderId, String transactionId); /*
    esse método vai ser usado na primeira veze que tivermos validando e precisa retornar
    verdeadeiro ou falso*/

    //Se a query acima der erro (false), na query abaixo ele vai verificar se já existe
    // esse objeto no banco, vamos atualizar o sucesso para falso, porque deu erro de validação no produto
    Optional<Validation> findByOrderIdAndTransactionId(String orderId, String transactionId);


}
