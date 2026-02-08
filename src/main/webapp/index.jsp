<%-- webapp/index.jsp --%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Bienvenido | Sysignin</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <style>
        body {
            background: linear-gradient(135deg, #0f172a 0%, #1e293b 100%);
            color: white;
            min-height: 100vh;
            display: flex;
            align-items: center;
        }
        .hero-card {
            background: rgba(255, 255, 255, 0.05);
            backdrop-filter: blur(10px);
            border: 1px solid rgba(255, 255, 255, 0.1);
            border-radius: 20px;
            padding: 3rem;
        }
        .btn-primary-custom {
            background-color: #3b82f6;
            border: none;
            padding: 12px 30px;
            font-weight: 600;
            transition: 0.3s;
        }
        .btn-primary-custom:hover {
            background-color: #2563eb;
            transform: translateY(-2px);
        }
    </style>
</head>
<body>
    <div class="container text-center">
        <div class="row justify-content-center">
            <div class="col-lg-7 hero-card shadow-lg">
                <i class="fa-solid fa-shield-halved fa-4x text-primary mb-4"></i>
                <h1 class="display-4 fw-bold mb-3">Sysignin</h1>
                <p class="lead mb-5 opacity-75">
                    Sistema de gestión de identidad seguro con autenticación inteligente y protección de datos avanzada.
                </p>
                
                <div class="d-grid gap-3 d-sm-flex justify-content-sm-center">
                    <a href="${pageContext.request.contextPath}/auth/loginView" class="btn btn-primary-custom btn-lg">
                        <i class="fa-solid fa-right-to-bracket me-2"></i> Iniciar Sesión
                    </a>
                    <a href="${pageContext.request.contextPath}/auth/registerView" class="btn btn-outline-light btn-lg px-4">
                        Crear Cuenta
                    </a>
                </div>
            </div>
        </div>
    </div>
</body>
</html>