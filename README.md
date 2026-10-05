# Cine Amazonas

Aplicación Java 21 con Spring Boot, JSP y H2. No utiliza JavaScript, Thymeleaf, JPA ni Hibernate. Fondo blanco en todas las pantallas; las pantallas nuevas son la pantalla de compra de entradas y el acceso administrativo independiente.

## Ejecutar desde el código

Instala un JDK 21 o superior. No necesitas instalar Maven, Tomcat ni un servidor de base de datos.

En macOS o Linux, desde la carpeta del proyecto:

```sh
./mvnw clean package
java -Xms64m -Xmx256m -jar target/cine-amazonas.war
```

En Windows:

```bat
mvnw.cmd clean package
java -Xms64m -Xmx256m -jar target\cine-amazonas.war
```

La primera compilación necesita internet para descargar las dependencias. Abre http://localhost:8080. Para usar otro puerto añade `--server.port=8081` al comando de Java. Detén la aplicación antes de volver a compilar el WAR que esté usando.

## Llevarlo a la máquina del docente

Compila primero y copia únicamente `target/cine-amazonas.war` a una carpeta del docente. Allí, con Java 21 o superior instalado, ejecuta:

```sh
java -Xms64m -Xmx256m -jar cine-amazonas.war
```

El WAR incluye Spring, Tomcat, JSP y H2; no requiere Maven ni descargar dependencias en esa máquina. Las fuentes de Google necesitan internet para verse exactamente igual; sin conexión se usan las fuentes alternativas del CSS original.

Si debes entregar el código, incluye `src`, `pom.xml`, `mvnw`, `mvnw.cmd`, `.mvn` y este README. No incluyas el resto de `target`, `.idea` ni los datos de tus pruebas. El WAR es ejecutable, no un JAR, conforme al soporte de JSP documentado por [Spring Boot](https://docs.spring.io/spring-boot/3.5/reference/web/servlet.html#web.servlet.embedded-container.jsp-limitations).

## Cuentas de demostración

| Rol | Correo | Contraseña |
| --- | --- | --- |
| ADMIN | admin@cineamazonas.pe | Admin123! |
| USER | cliente@cineamazonas.pe | Cliente123! |

Estas cuentas son solo para la demostración local. Los clientes ingresan en `/login`; los administradores escriben `/admin`, que los lleva al acceso administrativo independiente si falta su sesión. No hay enlaces administrativos en las pantallas públicas. Cada acceso rechaza las cuentas del otro rol. El registro público siempre crea un USER. Las contraseñas se guardan como resúmenes PBKDF2 con sal, no como texto legible. El panel exige una sesión ADMIN; “Mis tickets” solo aparece para clientes con sesión USER y consulta únicamente sus entradas. Escribir su URL sin una sesión de cliente también requiere login.

## Comprar entradas

El cliente compra solo la entrada; **no elige asiento**. Cada entrada recibe un **número de turno de ingreso** correlativo dentro de su función, y el día de la función los clientes entran a la sala en el orden de su turno y se sientan donde prefieran.

1. En la cartelera pulsa **Comprar entrada** en el horario elegido. Se abre la pantalla de compra, sin realizar ninguna compra todavía.
2. Elige la **cantidad** de entradas (hasta 10 por compra y nunca más que las disponibles) y pulsa **Continuar**. Java valida la cantidad y la conserva temporalmente en la sesión; todavía no guarda tickets en H2.
3. Si no has iniciado sesión como cliente, elige **Ingresar** o **Seguir como invitado**. Ambas opciones llevan al resumen con la misma cantidad. Un cliente que ya inició sesión pasa directamente al resumen.
4. Revisa la función, la cantidad y el total y pulsa **Confirmar compra**. Solo entonces Java vuelve a comprobar el aforo y guarda una entrada por cada unidad, con su turno. Con una cuenta, las entradas aparecen en **Mis tickets**. Como invitado, se muestra el comprobante de esa compra, sin dar acceso al historial.

Los pasos se resuelven con formularios y redirecciones: `GET /sala` muestra la pantalla de cantidad, `POST /seleccionar` prepara la compra, el acceso elegido devuelve a `GET /confirmar-compra` y `POST /comprar` guarda la compra. Abrir otra función reemplaza la compra pendiente. Las entradas no se bloquean durante el login: si otra persona agota la función antes de confirmar, se informa el conflicto.

La compra de invitado no crea usuarios: sus entradas tienen `usuario_id` vacío y la compra se identifica con un código aleatorio de su sesión. Solo ese navegador y sesión pueden consultar el comprobante. Guarda una captura o imprímelo con el navegador antes de cerrar la sesión. Aunque el comprobante deje de estar accesible, las entradas compradas permanecen guardadas en H2. Ingresar después no agrega automáticamente esas compras al historial de una cuenta.

El control es por **aforo**: las entradas vendidas de una función nunca superan el aforo de su sala. El total lo calcula Java (precio de la función × cantidad). La compra completa se guarda en una transacción (`@Transactional` en el servicio): o se guardan todas las entradas, o ninguna; la función se bloquea mientras se cuenta la ocupación y un índice único `(función, turno)` impide repetir un turno. Reenviar el mismo formulario no duplica entradas. El comportamiento del bloqueo corresponde a la [documentación de H2](https://h2database.com/html/commands.html#select).

Es una compra de demostración registrada localmente: no procesa tarjetas ni pagos bancarios.

## Base de datos

H2 crea `data/cine-amazonas.mv.db` dentro de la carpeta desde la que ejecutas el programa. Las cuentas nuevas y los cambios SQL persisten al cerrar y volver a abrir. No hay que instalar MySQL ni PostgreSQL.

`src/main/resources/schema.sql` define las tablas y añade las columnas nuevas a las bases existentes; `data.sql` carga los datos de ejemplo sin borrar los registros. Las funciones iniciales se programan para el día del primer arranque; si reutilizas la base otro día debes ajustar sus fechas para que aparezcan como funciones de hoy. La migración conserva los tickets anteriores y elimina las columnas de asiento de versiones previas, porque ya no se eligen asientos.

Se conservó una copia previa a los cambios en `data/respaldo-anterior.mv.db`. No es necesaria para ejecutar ni entregar la aplicación; mantenla como respaldo local. La base en uso es `data/cine-amazonas.mv.db`.

Para llevar también tus registros al docente, detén la aplicación y copia la carpeta `data` junto al WAR. No compartas datos personales. Para consultar el archivo con una herramienta JDBC, cierra primero la aplicación y usa la URL `jdbc:h2:file:/ruta/absoluta/data/cine-amazonas`, usuario `sa` y contraseña vacía. Utiliza la misma versión de H2 incluida en el WAR. No se habilita una consola web adicional.

## Organización para explicarlo

El código sigue la arquitectura en capas vista en clase: **Controller → Service → Repository**. Cada capa solo habla con la siguiente.

| Capa | Anotación | Qué hace | Archivos |
| --- | --- | --- | --- |
| Controller | `@Controller` | Recibe la petición (`@GetMapping`, `@PostMapping`, `@RequestParam`), llama al servicio y devuelve la vista con `Model.addAttribute` o un `redirect:`. No contiene SQL. | `AuthController`, `CarteleraController`, `NavegacionController`, `AdminController` |
| Service | `@Service` | Reglas de negocio: validar login y registro, validar la cantidad de entradas, calcular el total y guardar la compra completa en una transacción. | `AuthService`, `CarteleraService`, `CompraService`, `TicketService`, `AdminService` |
| Repository | `@Repository` | Único lugar con SQL; lee y escribe en H2. | `UsuarioRepository`, `CarteleraRepository`, `TicketRepository` |

- `CineAmazonasApplication.java`: arranque de Spring.
- Flujo de una compra: `POST /comprar` en `CarteleraController` → `CompraService.registrarCompra` → `TicketRepository`. El controlador nunca llama a un repositorio directamente.
- `src/main/webapp/WEB-INF/jsp`: pantallas renderizadas en el servidor. JSTL repite elementos y evalúa condiciones; no es JavaScript.
- `cabecera.jspf`: encabezado compartido por las pantallas públicas; una condición muestra “Mis tickets” únicamente al cliente autenticado.
- `src/main/resources/static/css`: estilos con la misma paleta del login.

La lógica utiliza tipos primitivos (`int`, `long`, `boolean`), variables, las interfaces de colecciones `List` y `Map`, bucles `for` y condicionales. No hay lambdas, streams, JPA ni Hibernate: los repositorios usan `JdbcTemplate` con SQL escrito a mano. La compra usa `@Transactional` en `CompraService.registrarCompra` para que se guarden todos los asientos o ninguno.

## Alcance conservado

El login, el registro, los catálogos y las compras trabajan desde Spring y H2. La marca, el logo con iniciales CA, el nombre de la aplicación, los paquetes Java y el WAR se corresponden con Cine Amazonas.

Los botones de crear/editar/desactivar catálogos, recuperar contraseña y enviar contacto siguen sin implementación. Los indicadores y gráficos del dashboard continúan siendo ejemplos visuales, no estadísticas reales; la tabla de funciones sí muestra la ocupación leída desde H2. No hay una pasarela de pago ni un flujo de cancelación de compras.

## Verificar

```sh
./mvnw test
```

Las pruebas usan bases H2 en memoria independientes; no alteran la base de la aplicación. Comprueban registro, validaciones, credenciales, separación de roles, sesiones, formularios, cantidad y turnos de las entradas, aforo, compras con cuenta y como invitado, reenvíos y privacidad de tickets y comprobantes.
