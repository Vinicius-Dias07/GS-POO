package br.com.fiap.tpi.repository;

import br.com.fiap.tpi.domain.Alerta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Acesso ao banco para alertas (consultas derivadas, sem SQL concatenado).
 */
@Repository
public interface AlertaRepository extends JpaRepository<Alerta, Long> {

    /** Todos os alertas, do mais recente para o mais antigo. */
    List<Alerta> findAllByOrderByDataHoraDesc();
}
