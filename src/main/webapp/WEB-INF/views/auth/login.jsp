<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Iniciar Sesión | TuApp</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/auth.css">
    <script src="https://accounts.google.com/gsi/client" async defer></script>
</head>
<body class="bg-light">

<div class="container d-flex justify-content-center align-items-center vh-100">
    <div class="card shadow-lg p-4" style="max-width: 400px; width: 100%; border-radius: 15px;">
        <div class="text-center mb-4">
            <h2 class="fw-bold">Bienvenido</h2>
            <p class="text-muted">Ingresa a tu cuenta para continuar</p>
        </div>

        <form id="loginForm">
            <div class="mb-3">
                <label for="identifier" class="form-label">Usuario o Correo</label>
                <div class="input-group">
                    <span class="input-group-text"><i class="fa-solid fa-user"></i></span>
                    <input type="text" class="form-control" id="identifier" name="identifier" placeholder="Ej: usuario o correo@example.com" required>
                </div>
            </div>

            <div class="mb-2">
                <label for="password" class="form-label">Contraseña</label>
                <div class="input-group">
                    <span class="input-group-text"><i class="fa-solid fa-lock"></i></span>
                    <input type="password" class="form-control" id="password" name="password" placeholder="********" required>
                </div>
            </div>

            <div class="text-end mb-4">
                <a href="${pageContext.request.contextPath}/auth/forgotPasswordView" class="text-decoration-none small fw-semibold">¿Olvidaste tu contraseña?</a>
            </div>

            <button type="submit" class="btn btn-primary w-100 py-2 mb-3 fw-bold shadow-sm">Iniciar Sesión</button>
            
            <div class="text-center mb-3 text-muted small">
                ¿No tienes cuenta? <a href="${pageContext.request.contextPath}/auth/registerView" class="fw-bold text-decoration-none text-primary">Regístrate aquí</a>
            </div>
        </form>

        <div class="position-relative my-4">
            <hr>
            <span class="position-absolute top-50 start-50 translate-middle bg-white px-2 text-muted small">O continúa con</span>
        </div>

        <div class="d-grid gap-2">
            <button id="googleBtn" type="button" class="btn btn-outline-danger d-flex align-items-center justify-content-center gap-2 py-2 shadow-sm">
                <i class="fa-brands fa-google"></i> Google
            </button>
            <button class="btn btn-outline-dark d-flex align-items-center justify-content-center gap-2 py-2 shadow-sm">
                <i class="fa-brands fa-microsoft"></i> Microsoft
            </button>
        </div>
    </div>
</div>
<%@ include file="verify_component.jsp" %>
<script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
<script src="${pageContext.request.contextPath}/assets/js/auth.js"></script>
</body>
</html>