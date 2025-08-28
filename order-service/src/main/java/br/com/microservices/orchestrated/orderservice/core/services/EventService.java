package br.com.microservices.orchestrated.orderservice.core.services;

import br.com.microservices.orchestrated.orderservice.core.document.Event;
import br.com.microservices.orchestrated.orderservice.core.repositories.EventRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor /*Construtor com todos argumentp*/
public class EventService /*classe para salvar e manipular os dados do evento*/{

    private final EventRepository repository;

    /*Salva evento no banco de dados*/
    public Event save(Event event) {

        return repository.save(event); /*spring data mondodb
        vai salvar na base event(collenction) do mondo db*/
    }

    /* Como é um consumer*/
    public void notifyEnding(Event event) {

    }
}
