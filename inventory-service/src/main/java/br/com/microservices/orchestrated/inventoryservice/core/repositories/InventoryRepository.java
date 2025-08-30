package br.com.microservices.orchestrated.inventoryservice.core.repositories;

import br.com.microservices.orchestrated.inventoryservice.core.model.Inventory;
import br.com.microservices.orchestrated.inventoryservice.core.model.OrderInventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Integer>{

    /*Query methos para encontrar um inventário pelo código do produto(nome do produto)
    * para quando tivermos trafegando o evento
    * conseguirms descobrir a quantidade atual que tem disponível dese produto*/

    Optional<Inventory> findByProductCode(String productCode); /*conseguimos
    encontrar um inventário pelo código(nome do produto*/
}
