<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="es">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Cine Amazonas - Publicidad</title>

    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/cartelera.css">
</head>

<body>

<c:set var="seccion" value="publicidad"/>
<%@ include file="cabecera.jspf" %>


<main class="legacy-page">

    <h2>Promociones y Novedades</h2>

    <p>
        Conoce las promociones disponibles y las novedades de Cine Amazonas.
    </p>


    <section>

        <h3>🎟 Promoción de la semana</h3>

        <p>
            Disfruta precios especiales en funciones seleccionadas.
        </p>

        <p>
            <strong>Disponible:</strong>
            De lunes a jueves.
        </p>

    </section>


    <section>

        <h3>🍿 Combos especiales</h3>

        <p>
            Encuentra promociones en combos de canchita y bebidas para acompañar tu película.
        </p>

        <p>
            <strong>Consulta disponibilidad en el cine.</strong>
        </p>

    </section>


    <section>

        <h3>🎬 Próximamente</h3>

        <p>
            Nuevas películas y funciones serán agregadas próximamente a nuestra cartelera.
        </p>

        <form method="get" action="${pageContext.request.contextPath}/cartelera" style="display:contents"><button type="submit">
            Ver cartelera
        </button></form>

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
