<%-- WEB-INF/views/dashboard/main.jsp --%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Panel Principal | Sysignin</title>
    
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    
    <style>
        body { background-color: #f4f7f6; overflow-x: hidden; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; }
        
        /* Ajuste para el Sidebar Fijo */
        .content-wrapper { 
            margin-left: 280px; 
            min-height: 100vh; 
            display: flex; 
            flex-direction: column; 
            transition: all 0.3s;
        }
        
        .card { border-radius: 12px; transition: transform 0.2s; }
        .card:hover { transform: translateY(-2px); }
        
        .badge-session {
            background-color: #e0f7fa;
            color: #006064;
            border: 1px solid #b2ebf2;
            font-weight: 600;
        }

        @media (max-width: 992px) {
            .content-wrapper { margin-left: 0; }
        }
    </style>
</head>
<body>

    <div class="d-flex">
        <%@ include file="layout/sidebar.jsp" %>
        
        <div class="content-wrapper w-100">
            <main class="container-fluid p-4 flex-grow-1">
                <div class="d-flex justify-content-between flex-wrap flex-md-nowrap align-items-center pt-3 pb-2 mb-4 border-bottom">
                    <h1 class="h2 text-dark fw-bold">Bienvenido, <span class="text-primary">${sessionScope.user.username}</span></h1>
                    <div class="btn-toolbar mb-2 mb-md-0">
                        <span class="badge badge-session p-2 shadow-sm rounded-pill">
                            <i class="fa-solid fa-clock-rotate-left me-1"></i> Sesión activa: 30 días
                        </span>
                    </div>
                </div>

                <div class="row g-4">
                    <div class="col-md-12">
                        <div class="card border-0 shadow-sm">
                            <div class="card-header bg-white border-0 pt-4 px-4">
                                <h5 class="fw-bold mb-0"><i class="fa-solid fa-chart-line me-2 text-primary"></i>Estado del Sistema</h5>
                            </div>
                            <div class="card-body px-4 pb-4">
                                <p class="text-muted">Aquí aparecerán tus notificaciones y registros de auditoría en tiempo real.</p>
                                
                                <div class="p-5 border border-dashed rounded text-center bg-light">
                                    <i class="fa-solid fa-inbox fa-3x text-muted mb-3 opacity-25"></i>
                                    <p class="mb-0 text-secondary">No hay actividades recientes para mostrar.</p>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </main>

            <%@ include file="layout/footer.jsp" %>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>