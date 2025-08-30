package br.com.microservices.orchestrated.paymentservice.core.services;


import br.com.microservices.orchestrated.paymentservice.config.exceptions.ValidationException;
import br.com.microservices.orchestrated.paymentservice.core.dto.Event;
import br.com.microservices.orchestrated.paymentservice.core.dto.History;
import br.com.microservices.orchestrated.paymentservice.core.enums.EPaymentStatus;
import br.com.microservices.orchestrated.paymentservice.core.enums.ESagaStatus;
import br.com.microservices.orchestrated.paymentservice.core.model.Payment;
import br.com.microservices.orchestrated.paymentservice.core.producer.KafkaProducer;
import br.com.microservices.orchestrated.paymentservice.core.repositories.PaymentRepository;
import br.com.microservices.orchestrated.paymentservice.core.utils.JsonUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j /*visualiza log*/
@Service
@AllArgsConstructor /*Construtor com todos os atributos*/
public class PaymentService {

    private static final String CURRENT_SOURCE = "PAYMENT_SERVICE";

    private static final Double REDUCE_SUM_VALUE = 0.0;

    private static final Double MIN_AMOUNT_VALUE = 0.1;

    private final JsonUtil jsonUtil;

    private final KafkaProducer producer;

    private final PaymentRepository paymentRepository;

    /**/
    public void realizePayment(Event event) {

        try {
            checkCurrentValidation(event); /*método para validar o evento*/
            createPendingPayment(event); /*cria pagamento pendente*/
            Payment payment = findByOrderIdAndTransactionId(event); /*retornando  pagamento do banco de dados, pela
             * informações do evento, se der sucesso
             * quer dizer que já foi criado o pagamento no banco de dados*/
            validateAmount(payment.getTotalAmount()); /*validando de o pagamento
           tem o valor maior que 0.1 centavo*/
            changePaymentToSuccess(payment); /*coloca o status de ucesso
            no pagamento*/
            handleSuccess(event); /*colocando informação de sucesso
            no evento atual, coloca o source do evento com o tópico do payment service
            para orchestardor saber de quem veio esse evento, e  coloca no histórico do
            evento o status de sucesso*/

        } catch (Exception e) {

            log.error("Error trying to make payment", e);
            handleFailCurrentNotExecuted(event, e.getMessage()); /*
            passa o status de pendente de rollback por que deu erro
            no pagamento e atualiza o histórico do evento
            com informações que deu erro*/
        }

        producer.sendEvent(jsonUtil.toJson(event)); /*criar um tópico
        pegando o evento já atualizado
         do pagamentp  mandando para o producer que vai voltar esse tópico para orchestrador*/
    }



    /*verifica se todos os produtos , o id do pedido e da transação foram informados
    * resolve o problema da idempotencia, para evitar eventos duplicado*/
    private void checkCurrentValidation(Event event) {

        /*Valida se já tem algum evento existente com esses mesmo
         * orderId transactionId(id do evento)*/
        if (paymentRepository.existsByOrderIdAndTransactionId(event.getOrderId(),
                event.getTransactionId())) {
            throw new ValidationException("There's another transactionId for this validation.");

        }

    }

    private void createPendingPayment(Event event) {
                Double totalAmount = calculateTotalAmount(event);
                Integer  totalItems = calculateTotalItems(event);

            Payment payment = Payment
                    .builder()
                    .orderId(event.getPayload().getId())
                    .transactionId(event.getTransactionId())
                    .totalAmount(totalAmount) /*valor total do pedido*/
                    .totalItems(totalItems) /*quantidade de produtos no pedido*/
                    .build();
                save(payment); /*salvar pagamento no banco*/

        /*Agora vamos atualizar o totalAmount e totalItems no evento
        * ele só atualizado na etapa do paymentService, no productService ele fica nulo
        * quando ele chegar no orchestrador e de volta para order service esses valores
        * vão estar prenchido*/

        setEventAmountItens(event, payment); /*depois que salvo o pagamento
        faz essa ação de atualizar os campos totalAmount e totalItens
        com informações que salvamos no banco payment*/

        }

    /*Agora vamos atualizar o totalAmount e totalItems no evento
     * ele só atualizado na etapa do paymentService, no productService ele fica nulo*/
        private void setEventAmountItens(Event event, Payment payment) {
            event.getPayload().setTotalAmount(payment.getTotalAmount()); /*passando
            valor total do pedido que salvamos no banco payment*/
            event.getPayload().setTotalItems(payment.getTotalItems()); /*passando
            quantidade total do pedido que salvamos no banco payment*/
        }


        private void changePaymentToSuccess(Payment payment) {
            payment.setStatus(EPaymentStatus.SUCCESS); /*coloca
            o status sucesso no pagamento*/
            save(payment); /*salva no banco*/
        }



    /*colocando informatações de sucesso no evento, após validação do produto*/
    private void handleSuccess(Event event ) {
        event.setStatus(ESagaStatus.SUCCESS); /*coloca a informação de sucesso*/
        event.setSource(CURRENT_SOURCE); /*nome da origem do evento setado
        com o nome do topico do product servic*/
        addHistory(event, "Payment realized successfully! "); /*
        adicionando histórico ao evento e mensagem de validação de sucesso*/
    }

    /*cria histórico do evento*/
    private void addHistory(Event event, String message) {

        History history = History
                .builder()
                .source(event.getSource())
                .status(event.getStatus())
                .message(message) /*mensagem do parâmetro*/
                .build();

        event.addToHistory(history);
    }

    /*Validação, aqui vamos passar uma regra
        * que o pagamento não pode ser 0, tem que ser pelo menos um 1 centavo*/

        private void validateAmount(Double amount) {
            if (amount < MIN_AMOUNT_VALUE) {
                throw new ValidationException("The minimum amount available is  ".concat((MIN_AMOUNT_VALUE.toString() /*passando
                o valor 0.1 e convertendo de double para string*/)));
            }
        }


    private void handleFailCurrentNotExecuted(Event event, String message) {

        event.setStatus(ESagaStatus.ROLLBACK_PENDING); /*coloca a informação de rollback no evento*/
        event.setSource(CURRENT_SOURCE); /*nome da origem do evento setado
        com o nome do tópico do product servic*/
        addHistory(event, "Fail to realized payment: ".concat(message)); /*
        adicionando histórico ao evento e mensagem de validação de sucesso*/
    }

    public void realizeRefund(Event event) {
        event.setStatus(ESagaStatus.FAIL); /*status de erro no evento*/
        event.setSource(CURRENT_SOURCE);
            try {
                changePaymentStatusToRefund(event); /*muda o status do pagamento
        para recusado no banco em caso de falha no pagamento*/

                addHistory(event, "Rollback executed for payment"); /*informando
        que o rollback foi realizado no banco e se for em prod
        informa que também foi realizado na API de integração*/
            } catch(Exception e ) {
                addHistory(event, "Rollback not executed for payment".concat(e.getMessage())); /*informando
        que o rollback não foi realizado provavelmente
        porque deu uma falha para encontrar o id do evento no banco ou do produto
         ou outra coisa*/
            }

        producer.sendEvent(jsonUtil.toJson(event));
    }

    private  void changePaymentStatusToRefund(Event event) {
            Payment payment = findByOrderIdAndTransactionId(event); /*vai pegar
            o pagamento pelo id da transação(do evento)*/
            payment.setStatus(EPaymentStatus.REFUND); /*colocar o status para recusado*/
            setEventAmountItens(event, payment); /*passando o valor da quantidade
            de produtos e valor total do pedido para no evento*/
            save(payment); /*salva no banco de dados o status de pagamento recusado
            se tivesse uma integração com banco teria que ter um serviço para
            fazer uma chamada para api do banco solicitando o estorno para o cliente
            que fez o pedido, isso se o pedido foi realizado e preciso de rollback*/
    }

        /*retornando  pagamento do banco de dados, pela
        * informações do evento, se der sucesso
        * quer dizer que já foi criado o pagamento no banco de dados*/
        private  Payment findByOrderIdAndTransactionId(Event event) {
            return paymentRepository
                    .findByOrderIdAndTransactionId(event.getPayload().getId(),
                            event.getTransactionId())
                    .orElseThrow(() -> new ValidationException("Payment not found by" +
                            " OrderId and TransactionId")); /*o
                            pagamento é sempre único
                            não podemos gerar outro
                            como fazemos no product-validation,
                            aqui se n encontrou lança erro*/
    }


    /*Método para salvar pagamento no banco*/
    private void save(Payment payment ) {

        paymentRepository.save(payment);
    }


    /*Método para calcular o totalAmount*/
    private Double calculateTotalAmount(Event event) {

        return event.getPayload().getProducts()
                .stream()
                .map(p ->
                        p.getQuantity() * p.getProduct().getUnitValue())/*aqui
                        ele está multiplicando  a quantidade pelo preço*/
                .reduce(REDUCE_SUM_VALUE, Double::sum) /*reduce para pegar essa coleção
                e fazer a soma do preço
                total  de todos os produtos da lista de produtos no evento
                Double::sum é um método sum da classe double
                ele recebe dois valores double e faz a soma deles
                REDUCE_SUM_VALUE é a identidade como é double é 0.0*/;
    }

    /*Método para calcular o totalAmount*/
    private Integer calculateTotalItems(Event event) {

        return event.getPayload().getProducts()
                .stream()
                .map(p ->
                        p.getQuantity() )/* aqui ele pega quantidade
                        de cada produto na lista de itens de pedido
                        você pode fazer assim OrderProducts::*/
                .reduce(REDUCE_SUM_VALUE.intValue(), Integer::sum) /*reduce para pegar essa coleção
                e fazer a soma das quantidade
                total  de todos os produtos da lista de produtos no evento
                Double::sum é um método sum da classe double
                ele recebe dois valores double e faz a soma deles

                identidade vai 0 porque é integer
                 REDUCE_SUM_VALUE.intValue() converte 0.0 para inteiro 0*/;
    }

}