<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="es">
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Bienvenido | SySignin</title>
    <link
      href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/dist/css/bootstrap.min.css"
      rel="stylesheet"
    />
    <link
      rel="stylesheet"
      href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css"
    />
    <style>
      body {
        background-color: #0f172a;
        color: white;
        height: 100vh;
        margin: 0;
        display: flex;
        align-items: center;
        justify-content: center;
        font-family:
          "Inter",
          system-ui,
          -apple-system,
          sans-serif;
      }
      .hero-container {
        max-width: 600px;
        width: 90%;
        text-align: center;
        background: rgba(30, 41, 59, 0.7);
        padding: 3rem;
        border-radius: 2rem;
        border: 1px solid rgba(255, 255, 255, 0.1);
        backdrop-filter: blur(10px);
        box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.5);
      }
      .icon-box {
        background: rgba(37, 99, 235, 0.1);
        width: 100px;
        height: 100px;
        border-radius: 1.5rem;
        display: flex;
        align-items: center;
        justify-content: center;
        margin: 0 auto 2rem;
        border: 1px solid rgba(37, 99, 235, 0.2);
      }
      .btn-login {
        background-color: #2563eb;
        border: none;
        padding: 1rem 3rem;
        font-weight: 600;
        border-radius: 1rem;
        font-size: 1.1rem;
        transition: all 0.3s ease;
        text-decoration: none;
        color: white;
        display: inline-flex;
        align-items: center;
        gap: 0.75rem;
      }
      .btn-login:hover {
        background-color: #1d4ed8;
        transform: translateY(-2px);
        box-shadow: 0 10px 15px -3px rgba(37, 99, 235, 0.4);
        color: white;
      }
      .brand-name {
        font-size: 2.5rem;
        font-weight: 800;
        letter-spacing: -1px;
        margin-bottom: 1rem;
      }
      .brand-year {
        color: #3b82f6;
      }
    </style>
  </head>
  <body>
    <div class="hero-container">
      <div class="icon-box">
        <i class="fas fa-shield-halved fa-3x text-primary"></i>
      </div>

      <h1 class="brand-name">SySignin <span class="brand-year">2026</span></h1>

      <p
        class="lead mb-5 text-secondary"
        style="font-size: 1.1rem; line-height: 1.6"
      >
        Gestión de identidad segura con auditoría en tiempo real y protección
        avanzada. Accede a tu panel de control para gestionar tus credenciales.
      </p>

      <div class="d-grid gap-3">
        <a
          href="${pageContext.request.contextPath}/auth/loginView"
          class="btn btn-login mx-auto"
        >
          <i class="fas fa-sign-in-alt"></i>
          Iniciar Sesión
        </a>
      </div>

      <div class="mt-5 pt-4 border-top border-secondary" style="opacity: 0.5">
        <small>© 2026 SySignin Security Systems</small>
      </div>
    </div>
  </body>
</html>
