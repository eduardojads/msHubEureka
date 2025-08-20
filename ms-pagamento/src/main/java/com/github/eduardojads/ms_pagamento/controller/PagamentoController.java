package com.github.eduardojads.ms_pagamento.controller;

import com.github.eduardojads.ms_pagamento.dto.PagamentoDTO;
import com.github.eduardojads.ms_pagamento.kafka.PagamentoPendenteProducer;
import com.github.eduardojads.ms_pagamento.service.PagamentoService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/pagamentos")
public class PagamentoController {

    @Autowired
    private PagamentoService service;

    @Autowired
    private PagamentoPendenteProducer pagamentoPendenteProducer;


    @GetMapping
    public ResponseEntity<List<PagamentoDTO>> getAll(){
        List<PagamentoDTO> dto = service.getAll();
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PagamentoDTO> getById( @PathVariable Long id){
        PagamentoDTO dto = service.getById(id);
        return ResponseEntity.ok(dto);
    }

    @PostMapping
    public ResponseEntity<PagamentoDTO> create( @Valid @RequestBody PagamentoDTO dto){
        dto = service.createPagamento(dto);

        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequestUri()
                .path("/{id}")
                .buildAndExpand(dto.getId())
                .toUri();

        return ResponseEntity.created(uri).body(dto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PagamentoDTO> update(@PathVariable Long id,
                                               @Valid @RequestBody PagamentoDTO dto){
        dto = service.updatePagamento(id, dto);
        return ResponseEntity.ok(dto);
    }

    @PatchMapping("/{id}/confirmar")
    @CircuitBreaker(name = "atualizarPedido", fallbackMethod = "confirmacaoPagamentoPendente")
    public void confirmarPagamentoDePedido(@PathVariable @NotNull Long id){
        service.confirmarPagamentoDoPedido(id);
    }

    public void confirmacaoPagamentoPendente (Long id, Exception e){
        service.alterarStatusDoPagamento(id);

        pagamentoPendenteProducer.enviarPAgamentoPendente((id.toString()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete (@PathVariable Long id){
        service.deletePagamento(id);
        return ResponseEntity.noContent().build();
    }
}
