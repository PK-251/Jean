<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Cine Amazonas | Confirmar compra</title>
    <link href="https://fonts.googleapis.com/css2?family=Archivo+Black&family=Manrope:wght@400;500;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/cartelera.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/sala.css">
</head>
<body>
<c:set var="seccion" value="cartelera"/>
<c:set var="destinoLogin" value="compra"/>
<%@ include file="cabecera.jspf" %>
<main class="seats-page">
    <a class="back-to-movies" href="${pageContext.request.contextPath}/sala?funcion=${funcion.id}">← Cambiar cantidad</a>
    <p class="eyebrow">Revisa tu selección</p>
    <h1>Confirma tu compra</h1>
    <div class="function-summary">
        <div><h2><c:out value="${funcion.pelicula}"/></h2><p><c:out value="${funcion.sala}"/> · <c:out value="${funcion.fechaTexto}"/> · <c:out value="${funcion.horaTexto}"/></p></div>
        <div class="seat-price">S/ <c:out value="${total}"/><small>Total de la compra</small></div>
    </div>
    <div class="seat-room">
        <p><strong>Entradas:</strong> <c:out value="${cantidad}"/> · S/ <c:out value="${funcion.precio}"/> por entrada</p>
        <c:choose>
            <c:when test="${invitado}"><p>Continuarás como invitado. Guarda tu comprobante al terminar; esta compra no se añade a “Mis tickets”.</p></c:when>
            <c:otherwise><p>La compra se guardará en tu cuenta y podrás consultarla en “Mis tickets”.</p></c:otherwise>
        </c:choose>
        <p>Al confirmar, cada entrada recibe un número de turno de ingreso. No se elige asiento: el día de la función entras en el orden de tu turno y te sientas donde prefieras.</p>
    </div>
    <form action="${pageContext.request.contextPath}/comprar" method="post" class="purchase-bar">
        <input type="hidden" name="csrf" value="${csrf}">
        <input type="hidden" name="funcion" value="${funcion.id}">
        <input type="hidden" name="compra" value="<c:out value='${compra}'/>">
        <p>Verifica la función y la cantidad antes de guardar.</p>
        <button class="confirm-purchase" type="submit">Confirmar compra</button>
    </form>
</main>
<footer><strong>Cine Amazonas</strong><span>Iquitos</span><span>© 2026</span></footer>
</body>
</html>
