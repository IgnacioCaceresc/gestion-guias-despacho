package com.transportista.guias.repository;

import com.transportista.guias.entity.GuiaDespacho;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GuiaDespachoRepository extends JpaRepository<GuiaDespacho, Long> {

    List<GuiaDespacho> findByTransportistaIgnoreCaseAndFecha(String transportista, LocalDate fecha);
}
