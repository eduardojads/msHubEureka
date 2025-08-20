package com.github.eduardojads.ms_pagamento.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class PagamentoPendenteProducer {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;


    //enviar mensagens para o tópico pagamneto-pendente
    //quando o fallback for acionado
    public void enviarPAgamentoPendente(String pagamentoId){
        kafkaTemplate.send("pagamento-pendente", pagamentoId);
    }
}
