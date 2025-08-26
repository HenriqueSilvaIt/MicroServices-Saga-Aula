package br.com.microservices.orchestrated.paymentservice.config.exceptions;

import br.com.microservices.orchestrated.orderservice.config.exception.ExceptionDetails;
import br.com.microservices.orchestrated.orderservice.config.exception.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice /*tem um handler global para todos os nossos controller*/
public class ExceptionGlobalHandler /*sempre que lançar uma exceção
ele vai converter para ExceptionDetails*/{

    @ExceptionHandler(ValidationException.class) /*captura o tipo de exception que passamos*/
    public ResponseEntity<?>  handleValidationException (ValidationException validationException) {
        /*a exception vem por parâmetro desse método e aqui dentro
        *                                        vamos mostrar oque fazer com ela*/

        /*ele pega só os dados abaixo status e mensagem, e o status http 400*/
        ExceptionDetails details = new ExceptionDetails(HttpStatus.BAD_REQUEST.value(), validationException.getMessage());

        return new ResponseEntity<>(details, HttpStatus.BAD_REQUEST);

    }

}
