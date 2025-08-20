package com.github.eduardojads.ms_pagamento.service;

import com.github.eduardojads.ms_pagamento.dto.PagamentoDTO;
import com.github.eduardojads.ms_pagamento.entity.Pagamento;
import com.github.eduardojads.ms_pagamento.entity.Status;
import com.github.eduardojads.ms_pagamento.http.PedidoClient;
import com.github.eduardojads.ms_pagamento.repository.PagamentoRepository;
import com.github.eduardojads.ms_pagamento.service.exceptions.DatabaseException;
import com.github.eduardojads.ms_pagamento.service.exceptions.ResourceNotFoundException;
import jakarta.persistence.EntityNotFoundException;
import org.hibernate.dialect.Database;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PagamentoService {

    @Autowired
    private PagamentoRepository repository;

    @Autowired
    private PedidoClient pedidoClient;

    @Transactional(readOnly = true)
    public List<PagamentoDTO> getAll(){
        List<Pagamento> pagamentos = repository.findAll();
        return pagamentos.stream().map(PagamentoDTO::new).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PagamentoDTO getById(Long id){
        Pagamento entity = repository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Recurso não encontrado. ID: " + id)
        );
        return new PagamentoDTO(entity);
    }

    @Transactional
    public PagamentoDTO createPagamento(PagamentoDTO dto){
        Pagamento entity = new Pagamento();
        copyDtoToEntity(dto, entity);
        entity.setStatus(Status.CRIADO);
        entity = repository.save(entity);
        return new PagamentoDTO(entity);
    }

    @Transactional
    public PagamentoDTO updatePagamento(Long id, PagamentoDTO dto){

        try{
            Pagamento entity = repository.getReferenceById(id);
            copyDtoToEntity(dto, entity);
            entity.setStatus(dto.getStatus());
            entity = repository.save(entity);
            return new PagamentoDTO(entity);
        } catch (EntityNotFoundException e){
            throw  new ResourceNotFoundException("Recurso não encontrado. ID: " + id);
        }
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    public void deletePagamento(Long id){
        if(!repository.existsById(id)){
            throw new ResourceNotFoundException("Recurso não encontrado. ID: " + id);
        }
        repository.deleteById(id);

        try{
            repository.deleteById(id);
        }catch (DataIntegrityViolationException e ){
            throw new DatabaseException("Falha na integridade referencial");
        }
    }

    @Transactional
    public void confirmarPagamentoDoPedido(Long id){
        Optional<Pagamento> pagamento = repository.findById(id);
        if (pagamento.isEmpty()){
            throw new ResourceNotFoundException("Recurso não encontrado. ID: " + id);
        }

        pagamento.get().setStatus(Status.CONFIRMADO);
        repository.save(pagamento.get());
        pedidoClient.atualizarPagamentoDoPedido(pagamento.get().getPedidoId());
    }

    @Transactional
    public void alterarStatusDoPagamento (Long id) {
        Optional<Pagamento> pagamento = repository.findById(id);
        if (pagamento.isEmpty()) {
            throw new ResourceNotFoundException("Recurso não encontrado. ID: " + id);
        }
        pagamento.get().setStatus(Status.CONFIRMACAO_PENDENTE);
        repository.save(pagamento.get());
    }

    @Transactional
    public void confirmarPagamentoKafka(Long id){

        Optional<Pagamento> pagamento = repository.findById(id);
        if (pagamento.isEmpty()){
            throw new ResourceNotFoundException("Recurso não encontrado. ID: " + id);
        }

        pagamento.get().setStatus(Status.CONFIRMADO);
        repository.save(pagamento.get());
    }

    private void copyDtoToEntity(PagamentoDTO dto, Pagamento entity) {
        entity.setValor(dto.getValor());
        entity.setNome(dto.getNome());
        entity.setNumeroDoCartao(dto.getNumeroDoCartao());
        entity.setValidade(dto.getValidade());
        entity.setCodigoDeSeguranca(dto.getCodigoDeSeguranca());
        entity.setPedidoId(dto.getPedidoId());
        entity.setFormaDePagamentoId(dto.getFormaDePagamentoId());

    }
}
