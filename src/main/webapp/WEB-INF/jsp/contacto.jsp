<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="es">

<head>
    <meta charset="UTF-8">
    <title>Cine Amazonas - Contacto</title>

    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/cartelera.css">
</head>

<body>

<c:set var="seccion" value="contacto"/>
<%@ include file="cabecera.jspf" %>


<main class="legacy-page">

    <h2>Contacto</h2>

    <p>
        ¿Tienes alguna consulta? Envíanos un mensaje.
    </p>


    <section>

        <h3>📩 Formulario de contacto</h3>

        <p>
            <strong>Nombre:</strong>
        </p>

        <p>
            <input type="text" placeholder="Ingresa tu nombre">
        </p>

        <p>
            <strong>Correo electrónico:</strong>
        </p>

        <p>
            <input type="email" placeholder="Ingresa tu correo">
        </p>

        <p>
            <strong>Asunto:</strong>
        </p>

        <p>
            <input type="text" placeholder="Motivo de la consulta">
        </p>

        <p>
            <strong>Mensaje:</strong>
        </p>

        <p>
            <textarea rows="6" cols="50"
                      placeholder="Escribe tu mensaje"></textarea>
        </p>

        <button type="button">
            Enviar mensaje
        </button>

    </section>


    <section>

        <h3>🎬 Cine Amazonas</h3>

        <p>
            Puedes utilizar esta sección para realizar consultas relacionadas con películas,
            funciones, entradas y el uso de Cine Amazonas.
        </p>

    </section>

</main>


<footer>

    <p>
        <strong>Cine Amazonas</strong>
    </p>

    <p>
        Sistema de venta de entradas de cine
    </p>

    <p>
        © 2026 Cine Amazonas
    </p>

</footer>

</body>

</html>
