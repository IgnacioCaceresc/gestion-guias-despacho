package com.transportista.guias.service;

import com.transportista.guias.entity.GuiaDespacho;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AlmacenamientoTemporalService {

    private final Path rutaBase;

    public AlmacenamientoTemporalService(@Value("${app.efs.path}") String efsPath) {
        this.rutaBase = Path.of(efsPath);
    }

    public Path generarGuiaTemporal(GuiaDespacho guia) {
        try {
            Files.createDirectories(rutaBase);
            String nombreArchivo = "guia-" + guia.getId() + ".pdf";
            Path archivo = rutaBase.resolve(nombreArchivo);
            Files.writeString(archivo, contenidoGuia(guia), StandardCharsets.UTF_8);
            return archivo;
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo generar la guia temporal en EFS", ex);
        }
    }

    public Path obtenerArchivo(String ruta) {
        Path archivo = Path.of(ruta);
        if (!Files.exists(archivo)) {
            throw new IllegalArgumentException("La guia no existe en almacenamiento temporal");
        }
        return archivo;
    }

    private String contenidoGuia(GuiaDespacho guia) {
        return """
                GUIA DE DESPACHO

                Numero: %s
                Transportista: %s
                Fecha: %s
                Destinatario: %s
                Direccion destino: %s
                Estado: %s
                """.formatted(
                guia.getId(),
                guia.getTransportista(),
                guia.getFecha().format(DateTimeFormatter.ISO_DATE),
                guia.getDestinatario(),
                guia.getDireccionDestino(),
                guia.getEstado()
        );
    }
}
