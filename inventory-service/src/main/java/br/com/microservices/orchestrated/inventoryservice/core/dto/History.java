package br.com.microservices.orchestrated.inventoryservice.core.dto;


import br.com.microservices.orchestrated.inventoryservice.core.enums.ESagaStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data /* ANotation do lombok cria get, set equals e hash code*/
@Builder /*ajuda na construção do objeto order que tem vários parâmetro*/
@NoArgsConstructor /* ANotation do lombok cria construtor sem argumentos*/
@AllArgsConstructor /* ANotation do lombokcria construtor com argumento*/
public class History {

    private String source; /*origem, em outros serviços pode ser um enum ou string, mas
    aqui vai ser só string*/
    private ESagaStatus status; /* status, pode ser enum ou string, mas vamos
    usar como string aqui, só usamos em enum em dados que queremos manipular
    para utilizar em regra de negócio e etc*/
    private String message; /*mensagem*/
    private LocalDateTime createdAt; /*data que foi criado o evento*/

}
