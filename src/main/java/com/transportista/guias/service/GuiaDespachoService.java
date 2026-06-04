package com.transportista.guias.service;

import com.transportista.guias.dto.GuiaDespachoRequest;
import com.transportista.guias.entity.GuiaDespacho;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

public interface GuiaDespachoService {

    GuiaDespacho crear(GuiaDespachoRequest request);

    GuiaDespacho buscarPorId(Long id);

    List<GuiaDespacho> consultar(String transportista, LocalDate fecha);

    GuiaDespacho actualizar(Long id, GuiaDespachoRequest request);

    void eliminar(Long id);

    GuiaDespacho subirAS3(Long id);

    Path descargar(Long id, String permiso);
}
