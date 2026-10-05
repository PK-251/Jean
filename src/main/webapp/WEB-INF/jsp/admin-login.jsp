<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Cine Amazonas | Acceso administrativo</title>
    <link href="https://fonts.googleapis.com/css2?family=Bebas+Neue&family=Inter:wght@400;500;600&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/login.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin-login.css">
</head>
<body class="admin-login-page">
<main class="admin-login-card">
    <a class="logo-menu-button" href="${pageContext.request.contextPath}/cartelera">
        <span class="logo-ticket">CA</span><span>Cine Amazonas<small>Iquitos</small></span>
    </a>
    <p class="admin-eyebrow">Acceso administrativo</p>
    <h1>Administración</h1>
    <p class="admin-description">Ingresa con tu cuenta de administrador del cine.</p>
    <c:if test="${not empty mensaje}"><p class="admin-message" role="alert"><c:out value="${mensaje}"/></p></c:if>
    <form action="${pageContext.request.contextPath}/admin/login" method="post" novalidate>
        <input type="hidden" name="csrf" value="${csrf}">
        <div class="field${errores['login-email'] ? ' has-error' : ''}">
            <label for="admin-email">Correo del administrador</label>
            <input id="admin-email" type="email" name="email" value="<c:out value='${email}'/>" placeholder="admin@cineamazonas.pe" autocomplete="username">
            <div class="field-error">Ingresa un correo válido.</div>
        </div>
        <div class="field${errores['login-password'] ? ' has-error' : ''}">
            <label for="admin-password">Contraseña</label>
            <input id="admin-password" type="password" name="password" placeholder="Tu contraseña" autocomplete="current-password">
            <div class="field-error">Ingresa tu contraseña.</div>
        </div>
        <button class="btn-primary" type="submit">Entrar al panel</button>
    </form>
    <p class="admin-return"><a class="link" href="${pageContext.request.contextPath}/login">Ir al acceso de clientes</a></p>
</main>
</body>
</html>
