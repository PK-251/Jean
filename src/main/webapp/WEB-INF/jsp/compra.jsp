<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Cine Amazonas | Comprobante de compra</title>
    <link href="https://fonts.googleapis.com/css2?family=Archivo+Black&family=Manrope:wght@400;500;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/cartelera.css">
</head>
<body>
<c:set var="seccion" value=""/>
<%@ include file="cabecera.jspf" %>
<main class="legacy-page">
    <h2>Comprobante de compra</h2>
    <c:if test="${not empty mensaje}"><p role="alert"><c:out value="${mensaje}"/></p></c:if>
    <c:if test="${not empty compraConfirmada}">
        <p class="purchase-notice" role="status">Compra registrada: <c:out value="${compraConfirmada.cantidad}"/> entrada(s) · Total S/ <c:out value="${compraConfirmada.total}"/>. Tus entradas quedaron guardadas.</p>
        <section>
            <h3><c:out value="${compraConfirmada.pelicula}"/></h3>
            <p><strong>Sala:</strong> <c:out value="${compraConfirmada.sala}"/></p>
            <p><strong>Función:</strong> <c:out value="${compraConfirmada.fechaTexto}"/> · <c:out value="${compraConfirmada.horaTexto}"/></p>
            <p><strong>Código de compra:</strong> <c:out value="${compraConfirmada.id}"/></p>
            <c:forEach var="ticket" items="${tickets}">
                <p><strong>Entrada #<c:out value="${ticket.id}"/> · Turno de ingreso <c:out value="${ticket.turno}"/></strong> · S/ <c:out value="${ticket.precio}"/></p>
            </c:forEach>
        </section>
        <p>Compraste como invitado. Guarda una captura o imprime este comprobante desde tu navegador: solo está disponible en esta sesión, no en “Mis tickets”. Tus entradas siguen válidas aunque cierres la sesión. Ingresa a la sala en el orden de tu turno y elige dónde sentarte.</p>
    </c:if>
    <p><a class="login-link" href="${pageContext.request.contextPath}/cartelera">Volver a cartelera</a></p>
</main>
<footer><strong>Cine Amazonas</strong><span>Iquitos</span><span>© 2026</span></footer>
</body>
</html>
