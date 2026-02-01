<%-- WEB-INF/views/dashboard/layout/footer.jsp --%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<footer class="footer mt-auto py-3 bg-white border-top">
    <div class="container-fluid px-4">
        <div class="d-flex align-items-center justify-content-between small">
            <div class="text-muted fw-medium">Copyright &copy; Sysignin 2026</div>
            <div class="footer-links">
                <a href="${pageContext.request.contextPath}/public/privacy" class="text-decoration-none text-muted hover-primary">Privacidad</a>
                <span class="mx-2 text-muted opacity-50">&middot;</span>
                <a href="${pageContext.request.contextPath}/public/terms" class="text-decoration-none text-muted hover-primary">Términos y Condiciones</a>
            </div>
        </div>
    </div>
</footer>

<style>
    .hover-primary:hover {
        color: #0d6efd !important;
        text-decoration: underline !important;
    }
</style>