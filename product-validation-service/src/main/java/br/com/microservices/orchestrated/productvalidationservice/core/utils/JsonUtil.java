package br.com.microservices.orchestrated.productvalidationservice.core.utils;

import br.com.microservices.orchestrated.productvalidationservice.core.dto.Event;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component /*Vai se tornar um componente do spring*/
@AllArgsConstructor /*Para criarmos um construtor com todos os atributos da classe*/
public class JsonUtil {

    private final ObjectMapper objectMapper;
 /* @AllArgsConstructor  faz isso para nós
    public JsonUtil(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }*/

    /*Converte qualquer objeto para String(JSON)*/
    private String toJson(Object object) {

        try{

            return objectMapper.writeValueAsString(object);

        } catch (Exception e) {

            return "";
        }
    }


    /*Recebe um string Json e converte para um objeto do tipo Evento*/
    public Event toEvent(String json) {

        try{

            return objectMapper.readValue(json, Event.class); /*espera um valor string
             e converte para um objeto, nesse caso colocamos para classe
             Event.class, ele pode gerar exceção se esse Json vem em um formato
             diferente dessa classe*/

        }catch (Exception e) {
            return null;
        }
    }
}
