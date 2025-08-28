package br.com.microservices.orchestrated.orderservice.core.services;

import br.com.microservices.orchestrated.orderservice.config.exception.ValidationException;
import br.com.microservices.orchestrated.orderservice.core.document.Event;
import br.com.microservices.orchestrated.orderservice.core.dto.EventFiltersDTO;
import br.com.microservices.orchestrated.orderservice.core.repositories.EventRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;


@Slf4j /*anotação do lombok que instancia a classe Logger do java*/
@Service
@AllArgsConstructor /*Construtor com todos argumentp*/
public class EventService /*classe para salvar e manipular os dados do evento*/{

    private final EventRepository repository;


    /* Método que vai salvar  no banco um evento de notificação assim que ele
          for consumido pelo nosso tópico de notify ending
          Como é um consumer vai ser void*/
    public void notifyEnding(Event event) {
        event.setOrderId(event.getOrderId());/*só para garantir
        que ele vai ter a referÊncia do id do pedido caso o id
        esteja nulo por algum motivo*/
        event.setCreatedAt(LocalDateTime.now()); /*data de criação do
        evento de notifyEndidng*/
        save(event); /*salva o evento no banco com o método que criamo*/

        log.info("Order {} with saga notified! TransactionaId: {}", event.getOrderId(), event.getTransactionId());
    }

    /*Salva evento no banco de dados*/
    public Event save(Event event) {

        return repository.save(event); /*spring data mondodb
        vai salvar na base event(collenction) do mondo db*/
    }


    /*FindAll retorna lista de event*/
    public List<Event> findAll() {

        return repository.findAllByOrderByCreatedAtDesc();
    }

    public Event findByFilters(EventFiltersDTO filtersDTO) {

        /*Valida se o id do pedido ou da transação é nulo*/
        validateEmptyFilters(filtersDTO); /*
        aqui ele validou e viu que não são os 2 que estão vazio
        apenas um deles está vazio*/


        /*Se o id do pedido não for nulo*/
        if (!ObjectUtils.isEmpty(filtersDTO.getOrderId())) {
                return findByOrderId(filtersDTO.getOrderId());/*retorna
                o evento apenas pelo id do pedido*/
        } else {/*no caso se é o id que está vai vazio
        ele vai conseguir retornar  o pedido pelo id da transação*/
            return findByTransactionalId(filtersDTO.getTransactionId());
        }

    }


    private Event findByOrderId(String orderId) {

        return repository.findTop1ByOrderIdOrderByCreatedAtDesc(orderId)
                .orElseThrow(() -> new ValidationException("Event not found by orderId."));
    }

    private Event findByTransactionalId(String transactionalId) {

        return repository.findTop1ByTransactionIdOrderByCreatedAtDesc(transactionalId)
                .orElseThrow(() -> new  ValidationException("Event not found by transactionalId."));
    }



    /*Método para verificar se os campos dos filtros que é
    *    orderId e transactional id foram prenchidos*/
    public void validateEmptyFilters(EventFiltersDTO filtersDTO) {

        if (ObjectUtils.isEmpty(filtersDTO.getOrderId())
                && ObjectUtils.isEmpty(filtersDTO.getTransactionId())) {
            throw new ValidationException("OrderID or TransactionalId mus be informed");
        }

    }

}
