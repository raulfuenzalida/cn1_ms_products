# cn1_ms_products

Microservicio de catálogo y precios de productos para PrintWorks.

## Propósito

`ms-products` es propietario de `products_db` y de la lógica de productos: catálogo, tags, imágenes S3, datos de fabricación, snapshots de costos, cálculo de precio, estado comercial y vigencia del precio.

## Arquitectura

**Database per Service:** Cada microservicio tiene su propia base de datos. `ms-products` nunca consulta directamente `config_db`.

**Dependencia permitida:**
```
ms-products -> ms-config
```

La configuración de filamentos y costos energéticos se obtiene mediante API REST desde `ms-config`, no mediante acceso directo a su base de datos.

## Requisitos

- Java 21
- Maven 3.9+
- MySQL 8.0+
- Docker (opcional, para despliegue)

## Configuración

### Variables de Entorno

El microservicio se configura mediante variables de entorno. Ver `.env.example` para referencia:

```bash
# Configuración del servidor
SERVER_PORT=8081

# Configuración de base de datos MySQL (products_db)
DB_HOST=localhost
DB_PORT=3306
DB_USER=products_user
DB_PASSWORD=products_password

# Configuración OAuth2 Resource Server (Access Token)
JWT_ISSUER_URI=https://login.microsoftonline.com/TENANT_ID/v2.0

# Cliente HTTP hacia ms-config (no hardcodear IP/Gateway en código)
MS_CONFIG_BASE_URL=http://localhost:8080

# CORS (lista separada por comas). Incluye origin de GitHub Pages en despliegue.
CORS_ALLOWED_ORIGINS=http://localhost:5173,https://raulfuenzalida.github.io

# Integración S3 (opcional). Vacío desactiva la subida real a AWS.
AWS_REGION=us-east-1
PRODUCT_IMAGES_BUCKET=
```

### application.yml

La configuración Spring Boot está exclusivamente en `src/main/resources/application.yml`. No se utiliza `application.properties`.

## MySQL

### Estructura de la Base de Datos

El esquema se inicializa automáticamente desde `database/init.sql`:

- **products**: Tabla principal de productos con datos de fabricación, snapshots de costos y precios
- **tags**: Tags para categorización de productos
- **product_tags**: Tabla de relación muchos-a-muchos entre productos y tags
- **product_images**: Imágenes de productos con historial y tipos (FRONT, SIDE, TOP)

### Reglas de Negocio

- Producto nuevo correctamente calculado: `priceStatus = CURRENT`
- Si la configuración relevante cambia: `priceStatus = OUTDATED`, `status = INACTIVE`
- Al recalcular: `priceStatus = CURRENT`, `status = INACTIVE` (no reactiva automáticamente)
- Solo productos `status = ACTIVE` y `priceStatus = CURRENT` aparecen en el catálogo público

## ms-config

`ms-products` obtiene la siguiente información desde `ms-config` mediante API REST:

- Filamento por ID
- Precio actual del filamento (pricePerKg)
- Estado del filamento (ACTIVE/INACTIVE)
- Configuración energética vigente
- Precio por kWh
- Consumo energético requerido por el cálculo

**Endpoints utilizados:**
- `GET /api/v1/config/filaments/{id}` - Obtener filamento por ID
- `GET /api/v1/config/printing` - Obtener configuración de impresión

## Ejecución Local

### Con Maven

```bash
# Compilar y ejecutar tests
./mvnw clean test

# Ejecutar la aplicación
./mvnw spring-boot:run
```

### Con Docker

```bash
# Construir imagen
docker build -t ms-products:latest .

# Ejecutar con docker-compose
docker-compose up -d
```

## Endpoints Principales

### Públicos (sin autenticación)

- `GET /api/v1/products/obtener` - Catálogo público (solo productos ACTIVE + CURRENT)
- `GET /api/v1/products/obtener/{id}` - Obtener producto público por ID

### Administrativos (requieren Access Token)

- `GET /api/v1/products/admin` - Listar todos los productos
- `GET /api/v1/products/admin/{id}` - Obtener producto por ID (admin)
- `POST /api/v1/products` - Crear producto
- `PUT /api/v1/products/{id}` - Actualizar producto
- `PATCH /api/v1/products/{id}/status` - Actualizar estado del producto
- `POST /api/v1/products/{id}/recalculate` - Recalcular precio del producto
- `POST /api/v1/products/recalculate-outdated` - Recalcular productos desactualizados

### Tags

- `GET /api/v1/tags` - Obtener todos los tags
- `GET /api/v1/tags/{id}` - Obtener tag por ID
- `POST /api/v1/tags` - Crear tag
- `DELETE /api/v1/tags/{id}` - Eliminar tag
- `POST /api/v1/products/{productId}/tags/{tagId}` - Agregar tag a producto
- `DELETE /api/v1/products/{productId}/tags/{tagId}` - Remover tag de producto

### Imágenes

- `POST /api/v1/products/{productId}/images/{type}` - Subir imagen (FRONT/SIDE/TOP)
- `GET /api/v1/products/{productId}/images` - Obtener imágenes activas
- `GET /api/v1/products/{productId}/images/history` - Obtener historial de imágenes
- `POST /api/v1/products/{productId}/images/{imageId}/restore` - Restaurar imagen histórica
- `DELETE /api/v1/products/{productId}/images/{imageId}` - Eliminar imagen

### Interno

- `POST /api/v1/products/internal/invalidate` - Invalidar productos por cambio en filamento

## Swagger

La documentación API está disponible en:

- Swagger UI: `http://localhost:8081/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8081/api-docs`

## Tests

El proyecto incluye pruebas unitarias para:

- **PriceCalculator**: Cálculo de materialCost, electricityCost, baseCost, profitAmount, finalPrice con HALF_UP
- **ProductService**: Lógica de creación, recálculo, snapshots, estados
- **TagService**: Normalización de nombres, detección de duplicados

Ejecutar tests:
```bash
./mvnw test
```

## Seguridad

### OAuth2 Resource Server

El microservicio utiliza Spring Security como OAuth2 Resource Server.

**Regla importante:** Las llamadas protegidas utilizan `Authorization: Bearer <ACCESS_TOKEN>`. No se acepta ID Token como sustituto del Access Token.

### Endpoints Públicos

- `GET /api/v1/products/obtener`
- `GET /api/v1/products/obtener/{id}`
- `/api-docs/**`
- `/swagger-ui/**`

### Endpoints Protegidos

Todos los endpoints administrativos requieren Access Token válido.

### CORS

CORS se configura mediante la variable `CORS_ALLOWED_ORIGINS`. Soporta:

- Desarrollo local: `http://localhost:5173`
- GitHub Pages: `https://raulfuenzalida.github.io`

## S3

### Integración AWS S3

Las imágenes de productos se almacenan en AWS S3. La base de datos guarda únicamente el `s3_key`, no URLs temporales/presigned.

**Configuración:**
- `AWS_REGION`: Región de AWS
- `PRODUCT_IMAGES_BUCKET`: Nombre del bucket S3

**Funcionalidades:**
- Subida de archivos a S3
- Generación de URLs presignadas temporales
- Eliminación de archivos
- Verificación de existencia de objetos

**Tipos de imagen:**
- FRONT: Vista frontal
- SIDE: Vista lateral
- TOP: Vista superior

**Reglas:**
- Máximo una imagen ACTIVA por producto y tipo
- Se conserva historial de imágenes
- Es posible restaurar imágenes históricas

## Docker

### Dockerfile

Multi-stage build con Java 21:

```dockerfile
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/ms-products-*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### docker-compose.yml

Incluye MySQL y configuración de variables de entorno:

```bash
docker-compose up -d
```

## GitHub Actions

El workflow CI ejecuta automáticamente:

1. Compilación
2. Ejecución de tests
3. Construcción de imagen Docker
4. Publicación en Docker Hub (si corresponde)

## Reglas de Cálculo de Precio

```
materialCost = (filamentGrams / 1000) * filamentPricePerKg
electricityCost = printingHours * printerConsumptionKwh * electricityPriceKwh
baseCost = materialCost + electricityCost
profitAmount = baseCost * (profitPercentage / 100)
finalPrice = baseCost + profitAmount
```

El precio final CLP se redondea a 0 decimales con `RoundingMode.HALF_UP`.

## Decisiones que Requieren Configuración Oficial Futura

- **Scopes/Roles de OAuth2:** La estructura está preparada para incorporar reglas de autorización basadas en scopes/roles cuando se definan oficialmente
- **Audience del JWT:** Actualmente configurado con placeholder, requiere valor oficial
- **Mecanismo de invalidación automática:** Cuando se requiera Kafka/RabbitMQ, implementar eventos para invalidación de precios
