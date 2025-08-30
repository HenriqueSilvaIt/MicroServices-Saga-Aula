package br.com.microservices.orchestrated.inventoryservice.core.repositories;

import br.com.microservices.orchestrated.inventoryservice.core.model.OrderInventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderInventoryRepository extends JpaRepository<OrderInventory, Integer>{


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
    List<OrderInventory> findByOrderIdAndTransactionId(String orderId, String transactionId);
    /*Nesse caso vamos ter uma lista com todos os produtos daquele pedido no inventario*/

}
