<%-- login.jsp --%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="es">
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Iniciar Sesión | TuApp</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet" />
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css" />
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/auth.css" />

    <style>
      .transition-all { transition: all 0.3s ease-in-out; }
      .card { border-radius: 15px; border: none; }
      
      /* Ajuste para botones sociales (Evita el error visual de Microsoft) */
      .btn-social {
        display: flex;
        align-items: center;
        justify-content: center;
        padding: 0.6rem;
        font-weight: 500;
        border: 1px solid #dee2e6;
        background-color: #fff;
        color: #212529;
        text-decoration: none;
        border-radius: 8px;
        transition: all 0.2s ease;
      }
      .btn-social:hover { background-color: #f8f9fa; border-color: #ced4da; color: #000; }
      .btn-social img { width: 20px; height: 20px; object-fit: contain; flex-shrink: 0; }
      
      #modalValidation {
        display: none; position: fixed; top: 0; left: 0; width: 100%; height: 100%;
        background: rgba(0, 0, 0, 0.4); z-index: 9999; align-items: center;
        justify-content: center; backdrop-filter: blur(8px);
      }
    </style>
    
    <script>
      window.addEventListener('pageshow', function(event) {
          if (event.persisted) { window.location.reload(); }
      });
    </script>
  </head>
  <body class="bg-light">
    <div id="loginContainer" class="container d-flex justify-content-center align-items-center vh-100 transition-all">
      <div class="card shadow-lg p-4" style="max-width: 420px; width: 100%;">
        <div class="text-center mb-4">
          <h2 class="fw-bold">Bienvenido</h2>
          <p class="text-muted">Ingresa para gestionar tu cuenta</p>
        </div>

        <form id="loginForm">
          <div class="mb-3">
            <label class="form-label small fw-bold">Usuario o Correo</label>
            <div class="input-group">
              <span class="input-group-text bg-white"><i class="fa-solid fa-user text-muted"></i></span>
              <input type="text" class="form-control" id="identifier" name="identifier" placeholder="usuario@example.com" required />
            </div>
          </div>

          <div class="mb-2">
            <label class="form-label small fw-bold">Contraseña</label>
            <div class="input-group">
              <span class="input-group-text bg-white"><i class="fa-solid fa-key text-muted"></i></span>
              <input type="password" class="form-control" id="password" name="password" placeholder="********" required />
            </div>
          </div>

          <div class="text-end mb-4">
            <a href="${pageContext.request.contextPath}/auth/forgot-password" class="small fw-bold text-decoration-none text-primary">
              ¿Olvidaste tu contraseña?
            </a>
          </div>

          <button type="submit" id="btnLogin" class="btn btn-primary w-100 py-2 fw-bold shadow-sm mb-3">
            Iniciar Sesión
          </button>
        </form>

        <div class="d-flex align-items-center my-3">
          <hr class="flex-grow-1 border-secondary-subtle">
          <span class="mx-2 text-muted small">O continúa con</span>
          <hr class="flex-grow-1 border-secondary-subtle">
        </div>

        <div class="row g-2 mb-4">
          <div class="col-6">
            <a href="${pageContext.request.contextPath}/auth/google" class="btn-social shadow-sm">
              <img src="https://authjs.dev/img/providers/google.svg" alt="G">
              <span class="ms-2">Google</span>
            </a>
          </div>
          <div class="col-6">
            <a href="${pageContext.request.contextPath}/auth/microsoft" class="btn-social shadow-sm">
              <img src="https://upload.wikimedia.org/wikipedia/commons/4/44/Microsoft_logo.svg" alt="M">
              <span class="ms-2">Microsoft</span>
            </a>
          </div>
        </div>

        <div class="text-center small">
          <span class="text-muted">¿No tienes una cuenta?</span>
          <a href="${pageContext.request.contextPath}/auth/registerView" class="fw-bold text-decoration-none text-primary ms-1">Regístrate</a>
        </div>
      </div>
    </div>

    <div id="modalValidation">
        <%@ include file="verify_component.jsp" %>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
    <script src="${pageContext.request.contextPath}/assets/js/auth.js"></script>
  </body>
</html>