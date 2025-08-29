package br.com.microservices.orchestrated.orderservice.core.controllers;


import br.com.microservices.orchestrated.orderservice.core.document.Event;
import br.com.microservices.orchestrated.orderservice.core.dto.EventFiltersDTO;
import br.com.microservices.orchestrated.orderservice.core.services.EventService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/event ")
public class EventController {

    private final EventService eventService;

    /*Método para buscar evento pelo filtro que ser passado no Json dto*/
    @GetMapping
    public Event findByFilters(EventFiltersDTO filtersDTO) {

       return eventService.findByFilters(filtersDTO);
    }


    /*Método que busca todos os eventos sem filtrar nada*/
    @GetMapping("/all") /*para diferencia do get acima*/
    public List<Event> findAll() {
        return eventService.findAll();
    }

}
