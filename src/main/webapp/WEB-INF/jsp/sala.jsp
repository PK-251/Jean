<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Cine Amazonas | Comprar entradas</title>
    <link href="https://fonts.googleapis.com/css2?family=Archivo+Black&family=Manrope:wght@400;500;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/cartelera.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/sala.css">
</head>
<body>
<c:set var="seccion" value="cartelera"/>
<c:set var="destinoLogin" value="sala"/>
<%@ include file="cabecera.jspf" %>
<main class="seats-page">
    <a class="back-to-movies" href="${pageContext.request.contextPath}/cartelera">← Volver a cartelera</a>
    <p class="eyebrow">Compra tu entrada</p>
    <h1>¿Cuántas entradas necesitas?</h1>
    <c:if test="${not empty mensaje}"><p class="seat-alert" role="alert"><c:out value="${mensaje}"/></p></c:if>
    <c:if test="${not empty funcion}">
        <div class="function-summary">
            <div><h2><c:out value="${funcion.pelicula}"/></h2><p><c:out value="${funcion.sala}"/> · <c:out value="${funcion.fechaTexto}"/> · <c:out value="${funcion.horaTexto}"/></p></div>
            <div class="seat-price">S/ <c:out value="${funcion.precio}"/><small>por entrada</small></div>
        </div>
        <c:choose>
            <c:when test="${maximo lt 1}">
                <div class="seat-room"><p><strong>Entradas agotadas.</strong> Esta función ya no tiene entradas disponibles. Elige otro horario en la cartelera.</p></div>
            </c:when>
            <c:otherwise>
                <form action="${pageContext.request.contextPath}/seleccionar" method="post" class="seat-form">
                    <input type="hidden" name="csrf" value="${csrf}">
                    <input type="hidden" name="funcion" value="${funcion.id}">
                    <input type="hidden" name="compra" value="<c:out value='${compra}'/>">
                    <div class="seat-room">
                        <div class="ticket-field">
                            <label for="cantidad">Cantidad de entradas</label>
                            <select id="cantidad" name="cantidad">
                                <c:forEach var="n" begin="1" end="${maximo}">
                                    <option value="${n}" ${n eq cantidad ? 'selected' : ''}>${n}</option>
                                </c:forEach>
                            </select>
                        </div>
                        <p class="ticket-note">No eliges asiento. Cada entrada recibe un <strong>número de turno</strong> según el orden de compra: el día de la función ingresas a la sala en ese orden y te sientas donde prefieras.</p>
                    </div>
                    <div class="purchase-bar">
                        <p><strong><c:out value="${libres}"/> entradas disponibles</strong><span>Elige la cantidad y continúa. Todavía no se registrará la compra.</span><c:if test="${sessionScope.rol ne 'USER'}"><span>En el siguiente paso eliges iniciar sesión o seguir como invitado.</span></c:if></p>
                        <button class="confirm-purchase" type="submit">Continuar</button>
                    </div>
                </form>
            </c:otherwise>
        </c:choose>
    </c:if>
</main>
<footer><strong>Cine Amazonas</strong><span>Iquitos</span><span>© 2026</span></footer>
</body>
</html>
