package br.com.microservices.orchestrated.orchestratorservice.core.enums;

public enum EEventSource /*esa classe só tem no orchestrador
porque ela recebe eventos de diversos serviçoes estão ela vai receber o nome
de todos os micros serviços e para quem ele tem que mandar algum evento*/{

    ORCHESTRATOR,
    PRODUCT_VALIDATION_SERVICE,
    PAYMENT_SERVICE,
    INVENTORY_SERVICE

}
