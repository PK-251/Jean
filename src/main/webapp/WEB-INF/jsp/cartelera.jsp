<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Cine Amazonas | Cartelera</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link href="https://fonts.googleapis.com/css2?family=Archivo+Black&family=Manrope:wght@400;500;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/cartelera.css">
</head>
<body>
<c:set var="seccion" value="cartelera"/>
<%@ include file="cabecera.jspf" %>

<main>
    <section class="billboard" aria-labelledby="page-title">
        <p class="eyebrow">Cine Amazonas · Iquitos</p>
        <h1 id="page-title">Cartelera</h1>
        <p>Películas y horarios para disfrutar hoy en el cine.</p>
    </section>

    <section class="movie-grid" aria-label="Películas disponibles">
        <c:forEach var="pelicula" items="${peliculas}">
        <article class="movie-card <c:out value='${pelicula.estilo}'/>">
            <div class="movie-poster" aria-hidden="true"><span><c:out value="${pelicula.poster}"/></span><small><c:out value="${pelicula.subtitulo}"/></small></div>
            <div class="movie-content">
                <div class="movie-heading"><h2><c:out value="${pelicula.nombre}"/></h2><span class="rating${pelicula.clasificacion eq 'ATP' ? ' all' : ''}"><c:out value="${pelicula.clasificacion}"/></span></div>
                <p class="movie-meta"><c:out value="${pelicula.genero}"/> · <c:out value="${pelicula.minutos}"/> min</p>
                <p class="synopsis"><c:out value="${pelicula.sinopsis}"/></p>
                <h3>Funciones disponibles</h3>
                <c:forEach var="funcion" items="${funciones}">
                    <c:if test="${funcion.pelicula_id eq pelicula.id}">
                        <div class="showtime"><span><c:out value="${funcion.sala}"/></span><strong><c:out value="${funcion.horaTexto}"/></strong><span>S/ <c:out value="${funcion.precio}"/></span><form method="get" action="${pageContext.request.contextPath}/sala" style="display:contents"><button type="submit" name="funcion" value="${funcion.id}">Comprar entrada</button></form></div>
                    </c:if>
                </c:forEach>
            </div>
        </article>
        </c:forEach>
    </section>
</main>

<footer>
    <strong>Cine Amazonas</strong><span>Cine Amazonas · Iquitos</span><span>© 2026</span>
</footer>
</body>
</html>
