<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="es">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Cine Amazonas — Ingresar</title>
<link rel="preconnect" href="https://fonts.googleapis.com">
<link href="https://fonts.googleapis.com/css2?family=Bebas+Neue&family=Inter:wght@400;500;600&display=swap" rel="stylesheet">
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/login.css">
</head>
<body>
  <div class="login-menu">
    <a class="logo-menu-button" href="${pageContext.request.contextPath}/cartelera" aria-label="Volver a la cartelera principal">
      <span class="logo-ticket">CA</span>
      <span>Cine Amazonas<small>Iquitos</small></span>
    </a>
  </div>
<div class="stage">

  <section class="marquee">
    <div class="filmstrip left"></div>
    <div class="filmstrip right"></div>

    <div class="brand">
      <span class="mark"></span>
      <span>CINE AMAZONAS · IQUITOS</span>
    </div>

    <div class="hero-copy">
      <h1>Tu entrada<br>te espera<em>.</em></h1>
      <p>Elige tu función, indica cuántas entradas necesitas y guárdalas. Recibes un turno de ingreso al confirmar la compra.</p>
    </div>

    <div class="showtimes">
      <div class="row"><span>Función más próxima</span><b>Hoy · 8:15 pm</b></div>
      <div class="row"><span>Sala</span><b>Sala 2</b></div>
      <div class="ticket-stub">Turno de ingreso asignado al confirmar tu compra · N.º <b>#014</b></div>
    </div>
  </section>

  <section class="auth-side">
    <div class="card">
      <div class="tabs" role="tablist" aria-label="Acceso a tu cuenta">
        <form method="get" action="${pageContext.request.contextPath}/login" style="display:contents">
          <input type="hidden" name="destino" value="${destino}">
          <button type="submit" name="panel" value="login" class="tab${panel eq 'login' ? ' active' : ''}" id="tab-login" role="tab" aria-selected="${panel eq 'login'}" aria-controls="panel-login">Ingresar</button>
          <button type="submit" name="panel" value="register" class="tab${panel eq 'register' ? ' active' : ''}" id="tab-register" role="tab" aria-selected="${panel eq 'register'}" aria-controls="panel-register">Crear cuenta</button>
        </form>
      </div>

      <form class="panel${panel eq 'login' ? ' active' : ''}" id="panel-login" role="tabpanel" aria-labelledby="tab-login"
            action="${pageContext.request.contextPath}/login" method="post" novalidate>
        <input type="hidden" name="csrf" value="${csrf}">
        <input type="hidden" name="destino" value="${destino}">
        <h2>${destino eq 'compra' ? 'Continúa tu compra' : 'Bienvenido de vuelta'}</h2>
        <c:if test="${destino eq 'compra' && not empty sessionScope.cantidadPendiente}"><p class="sub">Entradas elegidas: <c:out value="${sessionScope.cantidadPendiente}"/>. Inicia sesión o sigue como invitado; todavía no se ha registrado la compra.</p></c:if>
        <p class="sub">Ingresa para consultar tus tickets o compra como invitado sin crear una cuenta.</p>
        <c:if test="${not empty mensaje}"><p class="sub" role="alert"><c:out value="${mensaje}"/></p></c:if>

        <div class="field${errores['login-email'] ? ' has-error' : ''}" id="login-email-field">
          <label for="login-email">Correo electrónico</label>
          <input type="email" id="login-email" name="email" value="<c:out value='${email}'/>" placeholder="tu@correo.com" autocomplete="email">
          <div class="field-error">Ingresa un correo válido para continuar.</div>
        </div>

        <div class="field${errores['login-password'] ? ' has-error' : ''}" id="login-password-field">
          <label for="login-password">Contraseña</label>
          <input type="password" id="login-password" name="password" placeholder="••••••••" autocomplete="current-password">
          <div class="field-error">Ingresa tu contraseña.</div>
        </div>

        <div class="row-between">
          <label class="remember"><input type="checkbox" name="remember"> Mantener sesión iniciada</label>
          <a href="#" class="link">¿Olvidaste tu contraseña?</a>
        </div>

        <button type="submit" class="btn-primary">Ingresar</button>

        <button class="guest-access" type="submit" formaction="${pageContext.request.contextPath}/invitado" formnovalidate>Seguir como invitado</button>

        <p class="switch-line">¿Primera vez en Cine Amazonas? <a href="${pageContext.request.contextPath}/login?panel=register&amp;destino=${destino}" class="link">Crea tu cuenta</a></p>
      </form>

      <form class="panel${panel eq 'register' ? ' active' : ''}" id="panel-register" role="tabpanel" aria-labelledby="tab-register"
            action="${pageContext.request.contextPath}/registro" method="post" novalidate>
        <input type="hidden" name="csrf" value="${csrf}">
        <input type="hidden" name="destino" value="${destino}">
        <h2>Crea tu cuenta</h2>
        <p class="sub">Regístrate para guardar tus próximas compras en tu historial.</p>
        <c:if test="${not empty mensaje}"><p class="sub" role="alert"><c:out value="${mensaje}"/></p></c:if>

        <div class="field${errores['reg-name'] ? ' has-error' : ''}" id="reg-name-field">
          <label for="reg-name">Nombre completo</label>
          <input type="text" id="reg-name" name="nombre" value="<c:out value='${nombre}'/>" placeholder="Nombre y apellido" autocomplete="name">
          <div class="field-error">Cuéntanos cómo te llamas.</div>
        </div>

        <div class="field${errores['reg-email'] ? ' has-error' : ''}" id="reg-email-field">
          <label for="reg-email">Correo electrónico</label>
          <input type="email" id="reg-email" name="email" value="<c:out value='${email}'/>" placeholder="tu@correo.com" autocomplete="email">
          <div class="field-error">Ingresa un correo válido.</div>
        </div>

        <div class="field${errores['reg-password'] ? ' has-error' : ''}" id="reg-password-field">
          <label for="reg-password">Contraseña</label>
          <input type="password" id="reg-password" name="password" placeholder="Mínimo 8 caracteres" autocomplete="new-password">
          <div class="field-error">La contraseña debe tener al menos 8 caracteres.</div>
        </div>

        <div class="field${errores['reg-confirm'] ? ' has-error' : ''}" id="reg-confirm-field">
          <label for="reg-confirm">Confirmar contraseña</label>
          <input type="password" id="reg-confirm" name="confirmPassword" placeholder="Repite tu contraseña" autocomplete="new-password">
          <div class="field-error">Las contraseñas no coinciden.</div>
        </div>

        <button type="submit" class="btn-primary">Crear cuenta</button>

        <p class="switch-line">¿Ya tienes cuenta? <a href="${pageContext.request.contextPath}/login?panel=login&amp;destino=${destino}" class="link">Ingresa aquí</a></p>
      </form>
    </div>
  </section>

</div>

</body>
</html>
