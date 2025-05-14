package com.github.eduardojads.ms_pedido.repositories;

import com.github.eduardojads.ms_pedido.entities.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
}
