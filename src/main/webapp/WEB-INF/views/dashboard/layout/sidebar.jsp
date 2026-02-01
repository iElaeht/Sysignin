<%-- WEB-INF/views/dashboard/layout/sidebar.jsp --%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<style>
    .hover-bg-custom:hover {
        background-color: rgba(255, 255, 255, 0.1);
        transition: background 0.2s ease-in-out;
    }
    .sidebar-icon { width: 20px; text-align: center; }
</style>

<div class="d-flex flex-column flex-shrink-0 p-3 text-white bg-dark shadow" style="width: 280px; height: 100vh; position: fixed; z-index: 1000;">
    <a href="${pageContext.request.contextPath}/dashboard" class="d-flex align-items-center mb-3 mb-md-0 me-md-auto text-white text-decoration-none">
        <i class="fa-solid fa-shield-halved fs-3 me-2 text-primary"></i>
        <span class="fs-4 fw-bold tracking-tight">Sysignin</span>
    </a>
    <hr class="bg-secondary">

    <ul class="nav nav-pills flex-column mb-auto">
        <li class="nav-item">
            <a href="${pageContext.request.contextPath}/dashboard" class="nav-link active py-3 mb-2 shadow-sm">
                <i class="fa-solid fa-house sidebar-icon me-2"></i> Inicio
            </a>
        </li>
        
        <c:if test="${sessionScope.user.roles eq 'Admin'}">
            <li class="mt-3">
                <h6 class="ps-3 text-uppercase small opacity-50 fw-bold" style="font-size: 0.65rem; letter-spacing: 1px;">Administración</h6>
                <a href="${pageContext.request.contextPath}/admin/panel" class="nav-link text-warning py-3 hover-bg-custom">
                    <i class="fa-solid fa-unlock-keyhole sidebar-icon me-2"></i> Panel de Control
                </a>
            </li>
        </c:if>
    </ul>
    
    <hr class="bg-secondary">

    <div class="dropdown">
        <a href="#" class="d-flex align-items-center text-white text-decoration-none dropdown-toggle p-2 rounded hover-bg-custom" id="dropdownUser1" data-bs-toggle="dropdown" aria-expanded="false">
            <img src="${pageContext.request.contextPath}/assets/img/avatars/default.png" alt="Avatar" width="42" height="42" class="rounded-circle me-3 border border-2 border-primary shadow-sm">
            <div class="d-flex flex-column">
                <strong class="small">${sessionScope.user.username}</strong>
                <span style="font-size: 0.70rem;" class="text-success fw-bold"><i class="fa-solid fa-circle fs-6 me-1" style="font-size: 0.5rem !important;"></i> En línea</span>
            </div>
        </a>
        <ul class="dropdown-menu dropdown-menu-dark text-small shadow-lg border-0 animate slideIn" aria-labelledby="dropdownUser1">
            <li>
                <a class="dropdown-item py-2" href="${pageContext.request.contextPath}/dashboard/profile">
                    <i class="fa-solid fa-user-gear fa-fw me-2 text-muted"></i> Mi Perfil
                </a>
            </li>
            <li>
                <a class="dropdown-item py-2" href="${pageContext.request.contextPath}/dashboard/settings">
                    <i class="fa-solid fa-sliders fa-fw me-2 text-muted"></i> Configuración
                </a>
            </li>
            <li><hr class="dropdown-divider opacity-25"></li>
            <li>
                <a class="dropdown-item text-danger fw-bold py-2" href="${pageContext.request.contextPath}/auth/logout">
                    <i class="fa-solid fa-power-off fa-fw me-2"></i> Cerrar Sesión
                </a>
            </li>
        </ul>
    </div>
</div>