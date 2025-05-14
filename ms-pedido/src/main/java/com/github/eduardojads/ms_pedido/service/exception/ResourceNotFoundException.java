package com.github.eduardojads.ms_pedido.service.exception;

public class ResourceNotFoundException extends  RuntimeException{

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
