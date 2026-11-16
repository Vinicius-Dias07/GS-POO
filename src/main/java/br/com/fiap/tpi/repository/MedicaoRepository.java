package br.com.fiap.tpi.repository;

import br.com.fiap.tpi.domain.Medicao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Acesso ao banco para medições. O Spring Data gera o SQL a partir do
 * nome dos métodos (consultas derivadas), sempre com parâmetros —
 * nunca há SQL montado com concatenação de String.
 */
@Repository
public interface MedicaoRepository extends JpaRepository<Medicao, Long> {

    /** Histórico completo do sensor, da medição mais antiga para a mais recente. */
    List<Medicao> findBySensorIdOrderByDataHoraAsc(String sensorId);

    /** As 20 medições mais recentes do sensor (da mais nova para a mais antiga). */
    List<Medicao> findTop20BySensorIdOrderByDataHoraDesc(String sensorId);
}
