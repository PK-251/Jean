<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Cine Amazonas | Administración</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link href="https://fonts.googleapis.com/css2?family=Bebas+Neue&family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin.css">
</head>
<body>
<aside class="sidebar">
    <a class="admin-brand" href="${pageContext.request.contextPath}/admin">
        <span class="brand-mark">CA</span>
        <span><strong>Cine Amazonas</strong><small>Administración</small></span>
    </a>
    <nav aria-label="Navegación administrativa">
        <a class="active" href="#dashboard">Dashboard</a>
        <a href="#peliculas">Películas</a>
        <a href="#generos">Géneros</a>
        <a href="#salas">Salas</a>
        <a href="#funciones">Funciones</a>
        <a href="#usuarios">Usuarios</a>
    </nav>
    <a class="back-link" href="${pageContext.request.contextPath}/cartelera">Volver a cartelera</a>
</aside>

<main class="admin-main">
    <header class="admin-header">
        <div>
            <p>Panel administrativo</p>
            <h1>Resumen del cine</h1>
        </div>
        <div class="admin-user"><span>A</span><div><strong><c:out value="${sessionScope.nombre}"/></strong><small>Rol ADMIN</small></div></div>
        <form class="admin-logout" action="${pageContext.request.contextPath}/salir" method="post"><input type="hidden" name="csrf" value="${sessionScope.csrf}"><button class="table-action" type="submit">Cerrar sesión</button></form>
    </header>

    <section id="dashboard" class="admin-section">
        <div class="section-title">
            <div><p>Hoy</p><h2>Indicadores principales</h2></div>
            <span>Actualización por transacción</span>
        </div>
        <div class="metrics-grid">
            <article><small>Ventas del día</small><strong>S/ <c:out value="${metricas.ventasHoy}"/></strong><span><c:out value="${metricas.entradasHoy}"/> entradas</span></article>
            <article><small>Cupos próxima función</small><strong><c:out value="${metricas.cuposProxima}"/></strong><span><c:out value="${metricas.proximaFuncion}"/></span></article>
            <article><small>Funciones activas hoy</small><strong><c:out value="${metricas.funcionesHoy}"/></strong><span><c:out value="${metricas.salasHoy}"/> salas operativas</span></article>
            <article><small>Usuarios registrados</small><strong><c:out value="${metricas.usuarios}"/></strong><span>Cuentas activas</span></article>
            <article><small>Más vendida del mes</small><strong><c:out value="${metricas.masVendida}"/></strong><span><c:out value="${metricas.masVendidaEntradas}"/> entradas</span></article>
        </div>

        <div class="charts-grid">
            <article class="chart-card">
                <h3>Ventas diarias de la semana</h3>
                <div class="bar-chart">
                    <c:forEach var="dia" items="${ventasSemana}"><span style="--value:${dia.porcentaje}%" title="S/ ${dia.total}"><b><c:out value="${dia.nombre}"/></b></span></c:forEach>
                </div>
            </article>
            <article class="chart-card">
                <h3>Tickets vendidos por semana <small>(referencial)</small></h3>
                <div class="progress-list"><p><span>Semana 1</span><b style="--value:72%"></b><strong>148</strong></p><p><span>Semana 2</span><b style="--value:88%"></b><strong>181</strong></p><p><span>Semana 3</span><b style="--value:64%"></b><strong>132</strong></p><p><span>Semana 4</span><b style="--value:93%"></b><strong>192</strong></p></div>
            </article>
            <article class="chart-card compact-chart">
                <h3>Ocupación promedio mensual <small>(referencial)</small></h3><strong>74%</strong><p>Promedio de las salas</p>
            </article>
            <article class="chart-card compact-chart">
                <h3>Funciones por semana <small>(referencial)</small></h3><strong>38</strong><p>Programadas este mes</p>
            </article>
            <article class="chart-card compact-chart">
                <h3>Ingresos por género <small>(referencial)</small></h3><strong>S/ 3,420</strong><p>Acción lidera este mes</p>
            </article>
        </div>
    </section>

    <section id="peliculas" class="admin-section catalog-section">
        <div class="section-title"><div><p>Catálogo</p><h2>Películas</h2></div><button type="button">Nueva película</button></div>
        <div class="table-wrap"><table><thead><tr><th>Película</th><th>Género</th><th>Duración</th><th>Estado</th><th>Acciones</th></tr></thead><tbody><c:forEach var="pelicula" items="${peliculas}"><tr><td><c:out value="${pelicula.nombre}"/></td><td><c:out value="${pelicula.genero}"/></td><td><c:out value="${pelicula.minutos}"/> min</td><td><span class="status${pelicula.activo ? ' active-status' : ''}">${pelicula.activo ? 'Activa' : 'Inactiva'}</span></td><td><button type="button" class="table-action">Editar</button><button type="button" class="table-action danger">Desactivar</button></td></tr></c:forEach></tbody></table></div>
    </section>

    <section id="generos" class="admin-section catalog-section">
        <div class="section-title"><div><p>Catálogo</p><h2>Géneros</h2></div><button type="button">Nuevo género</button></div>
        <div class="simple-list"><c:forEach var="genero" items="${generos}"><article><div><strong><c:out value="${genero.nombre}"/></strong><span><c:out value="${genero.cantidad}"/> película${genero.cantidad eq 1 ? '' : 's'}</span></div><button type="button">Editar</button></article></c:forEach></div>
    </section>

    <section id="salas" class="admin-section catalog-section">
        <div class="section-title"><div><p>Operación</p><h2>Salas</h2></div><button type="button">Nueva sala</button></div>
        <div class="room-grid"><c:forEach var="sala" items="${salas}"><article><span><c:out value="${sala.nombre}"/></span><strong><c:out value="${sala.aforo}"/></strong><small>personas de aforo</small><button type="button">Editar sala</button></article></c:forEach></div>
    </section>

    <section id="funciones" class="admin-section catalog-section">
        <div class="section-title"><div><p>Programación</p><h2>Funciones</h2></div><button type="button">Nueva función</button></div>
        <div class="table-wrap"><table><thead><tr><th>Película</th><th>Sala</th><th>Hora</th><th>Ocupación</th><th>Estado</th></tr></thead><tbody><c:forEach var="funcion" items="${funciones}"><tr><td><c:out value="${funcion.pelicula}"/></td><td><c:out value="${funcion.sala}"/></td><td><c:out value="${funcion.horaTexto}"/></td><td><c:out value="${funcion.ocupacion}"/> / <c:out value="${funcion.aforo}"/></td><td><span class="status${funcion.activo ? ' active-status' : ''}">${funcion.activo ? 'Activa' : 'Inactiva'}</span></td></tr></c:forEach></tbody></table></div>
    </section>

    <section id="usuarios" class="admin-section catalog-section">
        <div class="section-title"><div><p>Accesos</p><h2>Usuarios</h2></div></div>
        <div class="table-wrap"><table><thead><tr><th>Nombre</th><th>Correo</th><th>Rol</th><th>Estado</th></tr></thead><tbody><c:forEach var="usuario" items="${usuarios}"><tr><td><c:out value="${usuario.nombre}"/></td><td><c:out value="${usuario.correo}"/></td><td><c:out value="${usuario.rol}"/></td><td><span class="status${usuario.activo ? ' active-status' : ''}">${usuario.activo ? 'Activo' : 'Inactivo'}</span></td></tr></c:forEach></tbody></table></div>
    </section>
</main>
</body>
</html>
