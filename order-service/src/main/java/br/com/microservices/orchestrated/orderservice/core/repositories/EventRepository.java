package br.com.microservices.orchestrated.orderservice.core.repositories;

import br.com.microservices.orchestrated.orderservice.core.document.Event;
import br.com.microservices.orchestrated.orderservice.core.document.Order;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface EventRepository extends MongoRepository<Event, String> {
    /*passamos <classe, tipo do id> só com isso ele já implementa
    * finb by id, find all e todo crud*/


    /*Vamos fazer uma consulta no mongodb para pegar todos
     pedidos que estão filtrando pela data de criação em order decrescente*/


    List<Event> findAllByOrderByCreatedAtDesc(); /*
    isso é um QueryMethod do spring Data
    que busca os pedidos e ordena por da de criação em ordem decrescente*/

    /*Busca pelo order id*/
    Optional<Event> findTop1ByOrderIdOrderByCreatedAtDesc(String orderId);
    /*Tem que ter um optinal porque é fin findBy OrderId
    * e o optional é que se for um Id nulo ele retorna um Optional vazio
    * Top1 é para trazer só primeiro que encontrar, ele vai buscar
    * todos os eventos que estiver desse orderid passado caso exista mais de um
    * evento para esse pedido
    * e ordernando pelo último evento criado , por isso que ele tem CreatedAtDesc
    * ele pega primeiro produto do resultado Top1 que é o último evento criado
    * Desc*/

    /*Busca pelo transactional Id*/

    Optional<Event> findTop1ByTransactionIdOrderByCreatedAtDesc(String transactionalId);
    /*Tem que ter um optinal porque é fin findBy TransactionId
     * e o optional é que se for um Id nulo ele retorna um Optional vazio
     * Top1 é para trazer só primeiro que encontrar, ele vai buscar
     * todos os eventos que estiver desse orderid passado caso exista mais de um
     * evento para esse pedido
     * e ordernando pelo último evento criado , por isso que ele tem CreatedAtDesc
     * ele pega primeiro produto do resultado Top1 que é o último evento criado
     * Desc
     *
     * iss é bom caso exista um método de atualizaçã*/

}
