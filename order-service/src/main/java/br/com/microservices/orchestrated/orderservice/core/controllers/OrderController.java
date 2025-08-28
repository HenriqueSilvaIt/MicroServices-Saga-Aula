package br.com.microservices.orchestrated.orderservice.core.controllers;


import br.com.microservices.orchestrated.orderservice.core.document.Order;
import br.com.microservices.orchestrated.orderservice.core.dto.OrderDTO;
import br.com.microservices.orchestrated.orderservice.core.services.OrderService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/api/order")
public class OrderController {


    private final OrderService service;


    @PostMapping
    public Order createOrder(@RequestBody OrderDTO dto) {
      return service.createOrder(dto);/*recebe o dto
      que o usuário digitar no front end*/
    }
}
