# cn1_ms_products

Microservicio de catálogo y precios de productos para PrintWorks.

## Propósito

`ms-products` es propietario de `products_db` y de la lógica asociada a productos: catálogo, tags, imágenes S3, datos de fabricación, snapshots de costos, cálculo de precios, estado comercial y vigencia del precio.

El microservicio obtiene desde `ms-config` la configuración necesaria para calcular precios y recibe notificaciones de dicho servicio cuando un cambio de costos requiere invalidar precios previamente calculados.

## Arquitectura

**Database per Service:** Cada microservicio tiene su propia base de datos. `ms-products` nunca consulta directamente `config_db`.

La comunicación entre `ms-products` y `ms-config` se realiza mediante API REST.

```text
                  ┌───────────────────┐
                  │     ms-config     │
                  │     config_db     │
                  └─────────┬─────────┘
                            │
                  configuración de costos
                            │
                            ▼
                  ┌───────────────────┐
                  │    ms-products    │
                  │    products_db    │
                  └───────────────────┘
```

La comunicación actualmente ocurre en ambos sentidos:

```text
ms-products → ms-config
Obtención de filamentos y configuración energética

ms-config → ms-products
Invalidación de precios cuando cambian costos
```

Cada microservicio continúa siendo el único responsable de modificar su propia base de datos.

## Requisitos

- Java 21
- Maven 3.9+
- MySQL 8.0+
- Docker (opcional, para despliegue)

## Configuración

### Variables de Entorno

El microservicio se configura mediante variables de entorno. Ver `.env.example` para referencia:

```env
# Configuración del servidor
SERVER_PORT=8081

# Configuración de base de datos MySQL (products_db)
DB_HOST=localhost
DB_PORT=3306
DB_USER=products_user
DB_PASSWORD=products_password

# Configuración OAuth2 Resource Server (Access Token)
JWT_ISSUER_URI=https://login.microsoftonline.com/TENANT_ID/v2.0

# Cliente HTTP hacia ms-config
MS_CONFIG_BASE_URL=http://localhost:8080

# CORS (lista separada por comas)
CORS_ALLOWED_ORIGINS=http://localhost:5173,https://raulfuenzalida.github.io

# Integración S3 (opcional)
AWS_REGION=us-east-1
PRODUCT_IMAGES_BUCKET=
```

`MS_CONFIG_BASE_URL` permite configurar la dirección de `ms-config` sin hardcodear una dirección específica en el código.

Para desarrollo local:

```text
ms-config    → http://localhost:8080
ms-products  → http://localhost:8081
```

En ECS Fargate, `MS_CONFIG_BASE_URL` debe configurarse como variable de entorno con la dirección utilizada para alcanzar `ms-config` dentro de la infraestructura desplegada.

### application.yml

La configuración Spring Boot está exclusivamente en:

```text
src/main/resources/application.yml
```

No se utiliza `application.properties`.

## MySQL

### Estructura de la Base de Datos

El esquema se inicializa desde `database/init.sql`.

- **products**: Tabla principal de productos con datos de fabricación, snapshots de costos y precios
- **tags**: Tags para categorización de productos
- **product_tags**: Relación muchos-a-muchos entre productos y tags
- **product_images**: Imágenes de productos con historial y tipos (FRONT, SIDE, TOP)

`ms-products` es el único microservicio autorizado para modificar `products_db`.

## Estados de Productos

Los productos poseen dos estados independientes.

### Estado comercial

```text
ACTIVE
INACTIVE
```

Determina si el producto está habilitado comercialmente.

### Vigencia del precio

```text
CURRENT
OUTDATED
```

Determina si el precio fue calculado utilizando la configuración de costos vigente.

### Reglas de Negocio

- Un producto correctamente calculado queda con `priceStatus = CURRENT`
- Si cambia una configuración que afecta su precio, queda `priceStatus = OUTDATED`
- Un producto `OUTDATED` también queda `INACTIVE`
- Al recalcular un producto queda `CURRENT + INACTIVE`
- El recálculo no reactiva automáticamente el producto
- La activación posterior es una decisión manual del administrador
- Solo productos `ACTIVE + CURRENT` aparecen en el catálogo público

El flujo esperado después de un cambio de costos es:

```text
ACTIVE + CURRENT
        ↓
cambio de costo
        ↓
INACTIVE + OUTDATED
        ↓
administrador recalcula
        ↓
INACTIVE + CURRENT
        ↓
administrador revisa y activa
        ↓
ACTIVE + CURRENT
```

## Integración con ms-config

`ms-products` obtiene mediante API REST la información necesaria para calcular los precios.

### Información utilizada

- Filamento por ID
- Precio actual del filamento (`pricePerKg`)
- Estado del filamento (`ACTIVE/INACTIVE`)
- Configuración energética vigente
- Precio de electricidad por kWh
- Consumo energético de la impresora

### Endpoints utilizados

```text
GET /api/v1/config/filaments/{id}
GET /api/v1/config/printing
```

`ms-products` no accede directamente a `config_db`.

### Autenticación entre servicios

Cuando una operación administrativa requiere consultar `ms-config`, el Access Token JWT validado por `ms-products` se obtiene desde el contexto de seguridad y se propaga en la llamada HTTP.

```text
front-admin
    ↓ Access Token
ms-products
    ↓ mismo Access Token
ms-config
```

La comunicación utiliza:

```http
Authorization: Bearer <access_token>
```

Esto permite que `ms-config` valide la solicitud utilizando OAuth2 Resource Server.

## Invalidación Automática de Precios

`ms-products` dispone de endpoints internos utilizados por `ms-config` para indicar que una configuración de costos cambió.

### Cambio del precio de un filamento

Cuando cambia `pricePerKg` de un filamento, `ms-config` llama:

```http
POST /api/v1/products/internal/invalidate/filament/{idFilament}
```

`ms-products` obtiene los productos asociados a ese filamento y aplica:

```text
priceStatus = OUTDATED
status = INACTIVE
```

Los productos que ya se encuentran `OUTDATED` no requieren una nueva invalidación.

### Cambio de configuración de impresión

Cuando cambia:

- Precio de electricidad por kWh
- Consumo energético de la impresora

`ms-config` llama:

```http
POST /api/v1/products/internal/invalidate/printing
```

Debido a que estos costos participan en el cálculo de todos los productos, `ms-products` invalida todos los productos cuyo precio se encuentre `CURRENT`.

El resultado es:

```text
CURRENT
   ↓
INACTIVE + OUTDATED
```

## Gestión y Edición de Productos

Los productos pueden ser creados y modificados desde la API administrativa.

Los campos de fabricación que participan en el cálculo incluyen:

- Filamento
- Gramos de filamento
- Horas de impresión
- Porcentaje de ganancia

### Edición sin impacto en precio

Cambiar solamente:

- Nombre
- Descripción

no requiere recalcular el precio.

### Edición con impacto en precio

Cambiar alguno de los siguientes campos:

- `idFilament`
- `filamentGrams`
- `printingHours`
- `profitPercentage`

provoca un nuevo cálculo del precio.

```text
PUT /api/v1/products/{id}
        ↓
se detecta cambio relevante
        ↓
se obtiene configuración vigente
        ↓
se recalcula precio
        ↓
CURRENT + INACTIVE
```

El producto permanece `INACTIVE` después del cálculo para permitir que el administrador revise el nuevo precio antes de volver a publicarlo.

Si cambia el filamento, el nuevo filamento se valida mediante `ms-config` antes de completar la actualización.

## Snapshots de Costos

Durante el cálculo de precios, `ms-products` conserva los valores utilizados para calcular el precio.

Esto permite identificar con qué configuración fue calculado un producto y mantener separada la información de configuración actual de los valores utilizados en el cálculo correspondiente.

Los snapshots forman parte de `products_db` y son responsabilidad exclusiva de `ms-products`.

## Reglas de Cálculo de Precio

```text
materialCost =
(filamentGrams / 1000) * filamentPricePerKg

electricityCost =
printingHours * printerConsumptionKwh * electricityPriceKwh

baseCost =
materialCost + electricityCost

profitAmount =
baseCost * (profitPercentage / 100)

finalPrice =
baseCost + profitAmount
```

El precio final CLP se redondea a `0` decimales utilizando:

```text
RoundingMode.HALF_UP
```

## Endpoints Principales

### Públicos

- `GET /api/v1/products/obtener` - Catálogo público (solo productos ACTIVE + CURRENT)
- `GET /api/v1/products/obtener/{id}` - Obtener producto público por ID

```http
GET /api/v1/products
```

Obtiene el catálogo público.

Solo devuelve productos:

```text
ACTIVE + CURRENT
```

```http
GET /api/v1/products/{id}
```

Obtiene un producto disponible públicamente por ID.

### Administrativos

Requieren Access Token válido.

```http
GET /api/v1/products/admin
```

Lista todos los productos, independientemente de su estado.

```http
GET /api/v1/products/admin/{id}
```

Obtiene un producto por ID para administración.

```http
POST /api/v1/products
```

Crea un producto y calcula su precio utilizando la configuración vigente.

```http
PUT /api/v1/products/{id}
```

Actualiza un producto.

Los cambios en datos de fabricación o porcentaje de ganancia provocan un nuevo cálculo del precio.

```http
PATCH /api/v1/products/{id}/status
```

Actualiza el estado comercial del producto.

```http
POST /api/v1/products/{id}/recalculate
```

Recalcula individualmente el precio de un producto utilizando la configuración vigente.

Después del recálculo:

```text
priceStatus = CURRENT
status = INACTIVE
```

```http
POST /api/v1/products/recalculate-outdated
```

Recalcula los productos cuyo precio se encuentra desactualizado.

### Endpoints Internos de Invalidación

```http
POST /api/v1/products/internal/invalidate/filament/{idFilament}
```

Marca como `OUTDATED + INACTIVE` los productos asociados al filamento indicado.

```http
POST /api/v1/products/internal/invalidate/printing
```

Marca como `OUTDATED + INACTIVE` todos los productos afectados por cambios en la configuración energética.

Estos endpoints permiten que `ms-config` notifique cambios sin acceder directamente a `products_db`.

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

## Ejecución Local

Para ejecutar correctamente las operaciones de cálculo se recomienda tener ambos microservicios disponibles:

```text
ms-config    → localhost:8080
ms-products  → localhost:8081
```

### Con Maven

Compilar y ejecutar tests:

```bash
./mvnw clean test
```

Ejecutar:

```bash
./mvnw spring-boot:run
```

### Con Docker

Construir imagen:

```bash
docker build -t ms-products:latest .
```

Ejecutar con docker-compose:

```bash
docker-compose up -d
```

## Swagger

La documentación API está disponible en:

```text
http://localhost:8081/swagger-ui.html
```

OpenAPI JSON:

```text
http://localhost:8081/api-docs
```

## Seguridad

### OAuth2 Resource Server

El microservicio utiliza Spring Security como OAuth2 Resource Server.

Las operaciones protegidas requieren:

```http
Authorization: Bearer <ACCESS_TOKEN>
```

El ID Token no se acepta como sustituto del Access Token.

### Endpoints Públicos

- `GET /api/v1/products/obtener`
- `GET /api/v1/products/obtener/{id}`
- `/api-docs/**`
- `/swagger-ui/**`

Las operaciones administrativas requieren un Access Token válido.

Esto incluye:

- Creación
- Edición
- Cambios de estado
- Recálculo
- Gestión administrativa de productos

### CORS

CORS se configura mediante:

```text
CORS_ALLOWED_ORIGINS
```

Ejemplos:

```text
Desarrollo local:
http://localhost:5173

GitHub Pages:
https://raulfuenzalida.github.io
```

## S3

### Integración AWS S3

Las imágenes de productos se almacenan en AWS S3.

La base de datos guarda el:

```text
s3_key
```

y no URLs temporales o presigned.

### Configuración

- `AWS_REGION` - Región de AWS
- `PRODUCT_IMAGES_BUCKET` - Nombre del bucket S3

### Funcionalidades

- Subida de archivos a S3
- Generación de URLs presignadas temporales
- Eliminación de archivos
- Verificación de existencia de objetos

### Tipos de imagen

- `FRONT` - Vista frontal
- `SIDE` - Vista lateral
- `TOP` - Vista superior

### Reglas

- Máximo una imagen `ACTIVE` por producto y tipo
- Se conserva historial de imágenes
- Es posible restaurar imágenes históricas

## Tests

El proyecto incluye pruebas para componentes como:

### PriceCalculator

Validación del cálculo de:

- `materialCost`
- `electricityCost`
- `baseCost`
- `profitAmount`
- `finalPrice`
- Redondeo `HALF_UP`

### ProductService

Lógica relacionada con:

- Creación de productos
- Cálculo de precios
- Recálculo
- Snapshots
- Estados de productos

### TagService

- Normalización de nombres
- Detección de duplicados

Ejecutar tests:

```bash
./mvnw test
```

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

Incluye MySQL y configuración de variables de entorno.

```bash
docker-compose up -d
```

## GitHub Actions

El workflow CI/CD ejecuta:

1. Compilación
2. Tests
3. Construcción de imagen Docker
4. Publicación de la imagen en Docker Hub cuando corresponde

La imagen posteriormente puede utilizarse para actualizar manualmente el servicio desplegado en ECS Fargate.

Las variables específicas del entorno AWS se configuran en la definición de tarea correspondiente y no se incorporan directamente a la imagen Docker.

## Flujo General de Productos

```text
                      ┌───────────────┐
                      │   ms-config   │
                      │   config_db   │
                      └───────┬───────┘
                              │
                      configuración REST
                              │
                              ▼
                      ┌───────────────┐
                      │  ms-products  │
                      │  products_db  │
                      └───────┬───────┘
                              │
                         precio CURRENT
                              │
                              ▼
                       ACTIVE + CURRENT
                              │
                cambio de configuración
                              │
              ms-config → invalidación REST
                              │
                              ▼
                      INACTIVE + OUTDATED
                              │
                       administrador
                         recalcula
                              │
                              ▼
                      INACTIVE + CURRENT
                              │
                       administrador
                          activa
                              │
                              ▼
                       ACTIVE + CURRENT
```

## Responsabilidades del Microservicio

`ms-products` es responsable de:

- Mantener `products_db`
- Crear y editar productos
- Mantener datos de fabricación
- Consultar la configuración necesaria desde `ms-config`
- Calcular precios
- Mantener snapshots de costos
- Controlar `CURRENT/OUTDATED`
- Controlar `ACTIVE/INACTIVE`
- Invalidar productos cuando `ms-config` informa cambios relevantes
- Recalcular productos
- Gestionar catálogo público
- Gestionar tags
- Gestionar imágenes de productos

`ms-products` no es responsable de:

- Modificar `config_db`
- Administrar precios de filamentos
- Administrar costos energéticos
- Gestionar pedidos

## Decisiones que Requieren Configuración Oficial Futura

- **Scopes/Roles de OAuth2:** La estructura se encuentra preparada para incorporar reglas de autorización basadas en scopes o roles cuando sean definidas oficialmente por la asignatura.
- **Configuración definitiva de identidad:** Los valores definitivos asociados a la configuración OAuth2 deben mantenerse de acuerdo con la infraestructura utilizada para el despliegue.

## Notas Importantes

- `ms-products` es propietario exclusivamente de `products_db`.
- Nunca consulta directamente `config_db`.
- La comunicación con `ms-config` se realiza mediante HTTP REST.
- Las llamadas protegidas entre servicios propagan el Access Token JWT de la solicitud administrativa.
- La invalidación automática actualmente se implementa mediante comunicación REST entre `ms-config` y `ms-products`.
- Un producto `OUTDATED` se mantiene `INACTIVE` hasta que su precio sea recalculado.
- Recalcular un producto no lo activa automáticamente.
- Después del recálculo, el administrador debe revisar y activar manualmente el producto.
- El catálogo público contiene únicamente productos `ACTIVE + CURRENT`.