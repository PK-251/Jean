<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="es">

<head>
    <meta charset="UTF-8">
    <title>Cine Amazonas - Mis Tickets</title>

    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/cartelera.css">
</head>

<body>

<c:set var="seccion" value="tickets"/>
<%@ include file="cabecera.jspf" %>


<main class="legacy-page">

    <h2>Mis Tickets</h2>

    <p>
        Consulta tu historial de entradas adquiridas en Cine Amazonas.
    </p>


    <c:if test="${not empty compraConfirmada}"><p class="purchase-notice" role="status">Compra registrada: <c:out value="${compraConfirmada.cantidad}"/> entrada(s) · Total S/ <c:out value="${compraConfirmada.total}"/>. Tus entradas quedaron guardadas.</p></c:if>
    <c:if test="${empty tickets}"><p>Todavía no tienes tickets registrados.</p></c:if>
    <c:forEach var="ticket" items="${tickets}">
    <section>
        <h3>🎟 Ticket #<c:out value="${ticket.numero}"/></h3>
        <p><strong>Película:</strong> <c:out value="${ticket.pelicula}"/></p>
        <p><strong>Sala:</strong> <c:out value="${ticket.sala}"/></p>
        <p><strong>Fecha:</strong> <c:out value="${ticket.fechaTexto}"/></p>
        <p><strong>Hora:</strong> <c:out value="${ticket.horaTexto}"/></p>
        <p><strong>Turno de ingreso:</strong> <c:out value="${ticket.turno}"/></p>
        <p><strong>Precio:</strong> S/ <c:out value="${ticket.precio}"/></p>
        <p><strong>Estado:</strong> <c:out value="${ticket.estado}"/></p>
    </section>
    </c:forEach>

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
