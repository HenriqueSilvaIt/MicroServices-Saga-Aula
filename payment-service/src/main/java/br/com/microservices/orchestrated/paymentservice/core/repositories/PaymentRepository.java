package br.com.microservices.orchestrated.paymentservice.core.repositories;

import br.com.microservices.orchestrated.paymentservice.core.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Integer>{


    /*Validações para garantir que n tenha já uma validação existente para
    * o orderId ou transactionalId*/

    //Verifica se existe já uma validação para esse pedido e transação
    Boolean existsByOrderIdAndTransactionId(String orderId, String transactionId); /*
    esse método vai ser usado na primeira veze que tivermos validando e precisa retornar
    verdeadeiro ou falso*/

    /*Nesse serviço é muito importante a query acima
    * para validar se já existe um evento de pagamento
    * com o mesmo id de transação, para n gerar pagamento duplicado*/


    //Se a query acima der erro (false), na query abaixo ele vai verificar se já existe
    // esse objeto no banco, vamos atualizar o sucesso para falso, porque deu erro de validação no produto
    Optional<Payment> findByOrderIdAndTransactionId(String orderId, String transactionId);


}
