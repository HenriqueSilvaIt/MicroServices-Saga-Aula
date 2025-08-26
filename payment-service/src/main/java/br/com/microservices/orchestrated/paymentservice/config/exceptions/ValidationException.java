package br.com.microservices.orchestrated.paymentservice.config.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST) /*Como é uma exceção de validação
 qualquer erro que cair aqui vai ser erro de usuário 400*/
/*Classe para caso o usuário informar uma coisa errado no Json*/
public class ValidationException extends RuntimeException {

    public ValidationException(String message)  {

        super(message);
    }

}
