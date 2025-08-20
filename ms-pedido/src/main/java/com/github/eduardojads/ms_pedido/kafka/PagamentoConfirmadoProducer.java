package com.github.eduardojads.ms_pedido.kafka;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class PagamentoConfirmadoProducer {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    //enviar tópico para pagamento-confirmado
    public void enviarConfirmacao(String pagamentoId){

        kafkaTemplate.send("pagamento-confirmado", pagamentoId);
    }
}
