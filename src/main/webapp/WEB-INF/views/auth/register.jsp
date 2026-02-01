<%-- register.jsp --%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="es">
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Crear Cuenta | TuApp</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet" />
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css" />
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/auth.css" />

    <style>
      /* Tus estilos originales se mantienen intactos */
      @media (max-width: 400px) {
        #tokenInput { font-size: 1.2rem !important; letter-spacing: 2px !important; }
        .card { margin: 10px; }
      }
      .transition-all { transition: all 0.3s ease-in-out; }
      .pointer-events-none { pointer-events: none; }
      #modalValidation {
        display: none; position: fixed; top: 0; left: 0; width: 100%; height: 100%;
        background: rgba(0, 0, 0, 0.4); z-index: 9999; align-items: center;
        justify-content: center; backdrop-filter: blur(8px); -webkit-backdrop-filter: blur(8px);
      }
      .btn-cancel { color: #6c757d; text-decoration: none; font-size: 0.9rem; font-weight: 600; transition: color 0.2s; }
      .btn-cancel:hover { color: #dc3545; }
      .swal2-container { z-index: 10000 !important; }
    </style>

    <script>
      // ADICIÓN: Seguridad para recargar si se vuelve atrás
      window.addEventListener('pageshow', function(event) {
          if (event.persisted) { window.location.reload(); }
      });
    </script>
  </head>
  <body class="bg-light">
    <div id="registerContainer" class="container d-flex justify-content-center align-items-center vh-100 transition-all">
      <div class="card shadow-lg p-4" style="max-width: 450px; width: 100%; border-radius: 15px">
        <div class="text-center mb-4">
          <h2 class="fw-bold">Únete a nosotros</h2>
          <p class="text-muted">Crea tu cuenta y comienza la experiencia</p>
        </div>

        <form id="registerForm" autocomplete="on">
          <div class="mb-3">
            <label for="username" class="form-label">Nombre de usuario</label>
            <div class="input-group">
              <span class="input-group-text"><i class="fa-solid fa-at"></i></span>
              <input type="text" class="form-control" id="username" name="username" placeholder="TuUsuario123" required />
            </div>
          </div>

          <div class="mb-3">
            <label for="email" class="form-label">Correo electrónico</label>
            <div class="input-group">
              <span class="input-group-text"><i class="fa-solid fa-envelope"></i></span>
              <input type="email" class="form-control" id="email" name="email" placeholder="ejemplo@correo.com" required />
            </div>
          </div>

          <div class="mb-3">
            <label for="password" class="form-label">Contraseña</label>
            <div class="input-group">
              <span class="input-group-text"><i class="fa-solid fa-key"></i></span>
              <input type="password" class="form-control" id="password" name="password" placeholder="********" required />
            </div>
            <div id="passwordRequirements" class="mt-2 small p-2 bg-light rounded shadow-sm border">
              <div id="req-length" class="text-muted"><i class="fa-solid fa-circle-xmark me-1"></i> Mínimo 8 caracteres</div>
              <div id="req-mayus" class="text-muted"><i class="fa-solid fa-circle-xmark me-1"></i> Una Mayúscula</div>
              <div id="req-number" class="text-muted"><i class="fa-solid fa-circle-xmark me-1"></i> Al menos un número</div>
              <div id="req-match" class="text-muted"><i class="fa-solid fa-circle-xmark me-1"></i> Las contraseñas deben coincidir</div>
            </div>
          </div>

          <div class="mb-4">
            <label for="confirmPassword" class="form-label">Confirmar Contraseña</label>
            <div class="input-group">
              <span class="input-group-text"><i class="fa-solid fa-check-double"></i></span>
              <input type="password" class="form-control" id="confirmPassword" placeholder="********" required />
            </div>
          </div>

          <button type="submit" id="btnRegister" class="btn btn-primary w-100 py-2 mb-3 fw-bold shadow-sm">
            Registrar Cuenta
          </button>

          <div class="text-center text-muted small">
            ¿Ya tienes una cuenta?
            <a href="${pageContext.request.contextPath}/auth/loginView" class="fw-bold text-decoration-none text-primary">Inicia sesión</a>
          </div>
        </form>
      </div>
    </div>

    <div id="modalValidation">
        <%@ include file="verify_component.jsp" %>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
    <script src="${pageContext.request.contextPath}/assets/js/auth.js"></script>
  </body>
</html>