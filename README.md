# ISIVI - Peluquería & Cuidado Capilar Natural (Spring Boot)

Proyecto convertido desde el prototipo HTML/JS (con `localStorage`) a una
aplicación **Spring Boot** completa: backend con API REST + base de datos,
y el mismo frontend conectado por `fetch()`.

## Estructura del proyecto

```
isivi-app/
├── pom.xml
├── src/main/java/com/isivi/app/
│   ├── IsiviApplication.java        <- clase principal (main)
│   ├── model/                       <- entidades JPA
│   │   ├── Producto.java
│   │   ├── Kit.java
│   │   ├── Servicio.java
│   │   └── Reserva.java
│   ├── repository/                  <- interfaces JpaRepository
│   ├── controller/                  <- controladores REST (@RestController)
│   └── config/
│       └── DataInitializer.java     <- carga el catálogo inicial al arrancar
└── src/main/resources/
    ├── application.properties       <- config de BD (H2 en memoria)
    └── static/
        ├── index.html               <- frontend (cliente + panel admin)
        ├── css/isivi.css
        └── js/isivi.js              <- llama a la API REST con fetch()
```

## Cómo abrirlo en Visual Studio Code

1. Instala la extensión **"Extension Pack for Java"** y **"Spring Boot Extension Pack"** en VS Code.
2. Abre la carpeta `isivi-app` con `File > Open Folder...`.
3. Espera a que VS Code descargue las dependencias de Maven (icono de progreso abajo).
4. Ejecuta la app de alguna de estas formas:
   - Presiona `F5` (Run/Debug) sobre `IsiviApplication.java`.
   - O desde la terminal integrada:
     ```
     ./mvnw spring-boot:run
     ```
     (en Windows: `mvnw.cmd spring-boot:run`)
   - O empaqueta y ejecuta el jar:
     ```
     ./mvnw clean package
     java -jar target/isivi-app-1.0.0.jar
     ```
5. Abre el navegador en: **http://localhost:8080**

## Requisitos

- Java 17 o superior
- Maven (o usa el wrapper `mvnw` incluido — si no aparece, ejecuta `mvn -N io.takari:maven:wrapper` una vez dentro del proyecto)

## Base de datos

Por defecto usa **H2 en memoria** (no necesitas instalar nada). Los datos
se reinician cada vez que reinicias la app, y `DataInitializer.java`
recarga el catálogo por defecto (productos, kits y servicios) si la base
está vacía.

- Consola web de H2: http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:isivi_db`
  - Usuario: `sa` / Password: *(vacío)*

Para pasar a **MySQL** en producción, edita
`src/main/resources/application.properties`: comenta el bloque de H2 y
descomenta el bloque de MySQL (ya incluido con comentarios), ajustando
usuario/clave y agregando la dependencia de MySQL en `pom.xml`:

```xml
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
</dependency>
```

## Endpoints REST disponibles

| Recurso    | Método | Ruta                          | Descripción                      |
|------------|--------|-------------------------------|-----------------------------------|
| Productos  | GET    | `/api/productos`              | Listar todos                      |
|            | POST   | `/api/productos`               | Crear                              |
|            | PUT    | `/api/productos/{id}`          | Editar                             |
|            | PATCH  | `/api/productos/{id}/stock`    | Alternar stock                     |
|            | DELETE | `/api/productos/{id}`          | Eliminar                           |
| Kits       | (igual que productos, en `/api/kits`) |
| Servicios  | GET    | `/api/servicios?categoria=`   | Listar (filtro opcional)          |
|            | POST/PUT/DELETE | `/api/servicios/{id}` | CRUD completo                      |
| Reservas   | GET    | `/api/reservas?fecha=`        | Listar (filtro opcional)          |
|            | POST   | `/api/reservas`                | Crear reserva (genera código ISV-####) |
|            | PATCH  | `/api/reservas/{id}/aprobar`   | Marcar como Confirmado             |
|            | DELETE | `/api/reservas` / `/api/reservas/{id}` | Borrar todas / una       |

## Panel de Administración

Accede desde el link discreto al final de la página web ("Acceso Panel
Administración") o directamente en `http://localhost:8080/#admin`.

**PIN por defecto:** `1234` (definido en `js/isivi.js`, constante `ADMIN_PIN`
— cámbialo antes de producción, y considera mover la validación al backend
con un login real en vez de un PIN en el cliente).

## Wompi Sandbox

ISIVI integra Wompi como alternativa al comprobante manual; WhatsApp se conserva. Configura estas variables de entorno con credenciales **Sandbox** y nunca las guardes en Git:

```text
WOMPI_PUBLIC_KEY=pub_test_...
WOMPI_PRIVATE_KEY=prv_test_...
WOMPI_INTEGRITY_SECRET=test_integrity_...
WOMPI_EVENTS_SECRET=test_events_...
WOMPI_BASE_URL=https://sandbox.wompi.co/v1
WOMPI_REDIRECT_URL=https://TU-DOMINIO/#pago
WOMPI_SANDBOX=true
```

Registra esta URL pública HTTPS en el dashboard Sandbox de Wompi:

```text
https://TU-DOMINIO/api/pagos/wompi/webhook
```

El botón **Pagar con Wompi Sandbox** prepara una referencia única y abre el Widget. La redirección solo informa al cliente: el webhook firmado valida referencia, monto en centavos, COP y estado antes de confirmar. Los eventos repetidos no generan una segunda confirmación. Para Wompi, servicios y productos se pagan como operaciones separadas; el flujo combinado de WhatsApp se mantiene.

## Almacenamiento de Imágenes con Cloudinary

ISIVI utiliza Cloudinary para almacenar de forma segura las imágenes de productos y kits. Configura estas variables de entorno en tu servidor:

```text
CLOUDINARY_CLOUD_NAME=tu_cloud_name
CLOUDINARY_API_KEY=tu_api_key
CLOUDINARY_API_SECRET=tu_api_secret
```

Si estas variables no están configuradas, la aplicación operará en **modo degradado**, guardando las imágenes en la base de datos como Base64 (fallback automático) y registrando una advertencia a nivel `WARN` en el backend.

## Notas de seguridad para producción

Este proyecto es un punto de partida funcional. Antes de publicarlo:
- Mueve la validación del PIN de administrador al backend (Spring Security).
- Cambia H2 por una base de datos persistente (MySQL/PostgreSQL).
- Restringe `@CrossOrigin(origins = "*")` en los controladores al dominio real.
- Agrega HTTPS y variables de entorno para credenciales.
