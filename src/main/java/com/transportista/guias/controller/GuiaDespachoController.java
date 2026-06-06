package com.transportista.guias.controller;

import com.transportista.guias.dto.GuiaDespachoRequest;
import com.transportista.guias.entity.GuiaDespacho;
import com.transportista.guias.service.GuiaDespachoService;
import jakarta.validation.Valid;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/guias")
public class GuiaDespachoController {

    private final GuiaDespachoService guiaDespachoService;

    public GuiaDespachoController(GuiaDespachoService guiaDespachoService) {
        this.guiaDespachoService = guiaDespachoService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GuiaDespacho crear(@Valid @RequestBody GuiaDespachoRequest request) {
        // 1. Crea la guía en Oracle y genera el archivo en el EFS local
        GuiaDespacho nuevaGuia = guiaDespachoService.crear(request);
        
        // 2. Automáticamente toma esa misma guía recién creada y la sube a S3
        return guiaDespachoService.subirAS3(nuevaGuia.getId());
    }

    @GetMapping
    public List<GuiaDespacho> consultar(
            @RequestParam(required = false) String transportista,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha
    ) {
        return guiaDespachoService.consultar(transportista, fecha);
    }

    @GetMapping("/{id}")
    public GuiaDespacho buscarPorId(@PathVariable Long id) {
        return guiaDespachoService.buscarPorId(id);
    }

    @GetMapping("/{id}/descargar")
    public ResponseEntity<Resource> descargar(@PathVariable Long id, @RequestParam String permiso) {
        Path archivo = guiaDespachoService.descargar(id, permiso);
        Resource resource = new FileSystemResource(archivo);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + archivo.getFileName() + "\"")
                .body(resource);
    }

    @PutMapping("/{id}")
    public GuiaDespacho actualizar(@PathVariable Long id, @Valid @RequestBody GuiaDespachoRequest request) {
        return guiaDespachoService.actualizar(id, request);
    }

    @PostMapping("/{id}/subir-s3")
    public GuiaDespacho subirAS3(@PathVariable Long id) {
        return guiaDespachoService.subirAS3(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        guiaDespachoService.eliminar(id);
    }
}
