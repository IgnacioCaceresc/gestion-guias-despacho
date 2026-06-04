package com.transportista.guias.service;

import com.transportista.guias.dto.GuiaDespachoRequest;
import com.transportista.guias.entity.GuiaDespacho;
import com.transportista.guias.repository.GuiaDespachoRepository;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GuiaDespachoServiceImpl implements GuiaDespachoService {

    private final GuiaDespachoRepository repository;
    private final AlmacenamientoTemporalService almacenamientoTemporalService;
    private final S3StorageService s3StorageService;

    public GuiaDespachoServiceImpl(
            GuiaDespachoRepository repository,
            AlmacenamientoTemporalService almacenamientoTemporalService,
            S3StorageService s3StorageService
    ) {
        this.repository = repository;
        this.almacenamientoTemporalService = almacenamientoTemporalService;
        this.s3StorageService = s3StorageService;
    }

    @Override
    @Transactional
    public GuiaDespacho crear(GuiaDespachoRequest request) {
        GuiaDespacho guia = new GuiaDespacho();
        aplicarDatos(guia, request);
        GuiaDespacho guardada = repository.save(guia);
        Path archivoTemporal = almacenamientoTemporalService.generarGuiaTemporal(guardada);
        guardada.setNombreArchivo(archivoTemporal.getFileName().toString());
        guardada.setRutaEfs(archivoTemporal.toString());
        return repository.save(guardada);
    }

    @Override
    public GuiaDespacho buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Guia no encontrada: " + id));
    }

    @Override
    public List<GuiaDespacho> consultar(String transportista, LocalDate fecha) {
        if (transportista != null && fecha != null) {
            return repository.findByTransportistaIgnoreCaseAndFecha(transportista, fecha);
        }
        return repository.findAll();
    }

    @Override
    @Transactional
    public GuiaDespacho actualizar(Long id, GuiaDespachoRequest request) {
        GuiaDespacho guia = buscarPorId(id);
        aplicarDatos(guia, request);
        Path archivoTemporal = almacenamientoTemporalService.generarGuiaTemporal(guia);
        guia.setNombreArchivo(archivoTemporal.getFileName().toString());
        guia.setRutaEfs(archivoTemporal.toString());
        return repository.save(guia);
    }

    @Override
    public void eliminar(Long id) {
        GuiaDespacho guia = buscarPorId(id);
        repository.delete(guia);
    }

    @Override
    @Transactional
    public GuiaDespacho subirAS3(Long id) {
        GuiaDespacho guia = buscarPorId(id);
        Path archivo = almacenamientoTemporalService.obtenerArchivo(guia.getRutaEfs());
        String key = construirKeyS3(guia);
        String rutaS3 = s3StorageService.subir(archivo, key);
        guia.setRutaS3(rutaS3);
        guia.setEstado("SUBIDA_S3");
        return repository.save(guia);
    }

    @Override
    public Path descargar(Long id, String permiso) {
        validarPermisoDescarga(permiso);
        GuiaDespacho guia = buscarPorId(id);
        return almacenamientoTemporalService.obtenerArchivo(guia.getRutaEfs());
    }

    private void aplicarDatos(GuiaDespacho guia, GuiaDespachoRequest request) {
        guia.setTransportista(request.getTransportista());
        guia.setFecha(request.getFecha());
        guia.setDestinatario(request.getDestinatario());
        guia.setDireccionDestino(request.getDireccionDestino());
        if (request.getEstado() != null && !request.getEstado().isBlank()) {
            guia.setEstado(request.getEstado());
        }
    }

    private String construirKeyS3(GuiaDespacho guia) {
        String fecha = guia.getFecha().format(DateTimeFormatter.BASIC_ISO_DATE);
        String transportista = guia.getTransportista().replaceAll("[^a-zA-Z0-9_-]", "-");
        return fecha + "/" + transportista + "/" + guia.getNombreArchivo();
    }

    private void validarPermisoDescarga(String permiso) {
        if (!"DESCARGAR_GUIA".equalsIgnoreCase(permiso) && !"ADMIN".equalsIgnoreCase(permiso)) {
            throw new IllegalArgumentException("No tiene permisos para descargar la guia");
        }
    }
}
