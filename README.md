# Gestion de Guias de Despacho

Proyecto Spring Boot para la actividad sumativa de Desarrollo Cloud Native.

## Requisitos cubiertos

- Crear guias de despacho.
- Guardar guias temporalmente en una ruta configurable que representa EFS.
- Subir guias a S3 con estructura por fecha y transportista.
- Consultar guias por transportista y fecha.
- Actualizar y eliminar guias.
- Dockerfile para generar imagen.
- Workflow de GitHub Actions para publicar en Docker Hub y desplegar en EC2.

## Ejecutar localmente

```bash
mvn spring-boot:run
```

La API queda disponible en:

```text
http://localhost:8080
```

La consola H2 queda disponible en:

```text
http://localhost:8080/h2-console
```

Datos de H2:

```text
JDBC URL: jdbc:h2:mem:guiasdb
Usuario: sa
Password:
```

## Crear una guia

```bash
curl -X POST http://localhost:8080/api/guias \
  -H "Content-Type: application/json" \
  -d '{
    "transportista": "transportistaX",
    "fecha": "2026-06-03",
    "destinatario": "Cliente Demo",
    "direccionDestino": "Av. Siempre Viva 123"
  }'
```

## Consultar guias

```bash
curl "http://localhost:8080/api/guias?transportista=transportistaX&fecha=2026-06-03"
```

## Subir guia a S3

```bash
curl -X POST http://localhost:8080/api/guias/1/subir-s3
```

## Descargar guia con validacion de permisos

```bash
curl -O -J "http://localhost:8080/api/guias/1/descargar?permiso=DESCARGAR_GUIA"
```

Tambien puedes usar `permiso=ADMIN`.

Por defecto `S3_ENABLED=false`, por eso la subida se simula localmente. Para usar AWS real debes definir:

```bash
export AWS_REGION=us-east-1
export S3_BUCKET=nombre-del-bucket
export S3_ENABLED=true
```

## Docker

```bash
docker build -t gestion-guias-despacho .
docker run -p 8080:8080 gestion-guias-despacho
```

## Secrets requeridos en GitHub Actions

- `DOCKERHUB_USERNAME`
- `DOCKERHUB_TOKEN`
- `EC2_HOST`
- `EC2_USER`
- `EC2_SSH_KEY`
- `AWS_REGION`
- `S3_BUCKET`

## Nota sobre EFS

En local se usa `/tmp/guias-despacho`. En EC2, el workflow usa `/mnt/efs/guias-despacho`, que debe estar montado previamente con EFS.
