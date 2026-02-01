<%-- verify_component.jsp --%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<div class="card p-4 shadow-lg border-0" style="max-width: 400px; width: 90%; border-radius: 15px">
    <div class="text-center mb-3">
        <div id="verifyIconContainer" class="bg-success bg-opacity-10 p-3 rounded-circle d-inline-block mb-2">
            <i id="verifyIcon" class="fa-solid fa-shield-halved text-success fs-3"></i>
        </div>
        <h4 id="verifyTitle" class="fw-bold">Verificar Cuenta</h4>
        <p id="verifyText" class="small text-muted">Ingresa el código enviado a tu correo.</p>
    </div>

    <form id="tokenForm">
        <input type="hidden" id="authAction" name="authAction" value="register" />
        <input type="hidden" id="userEmail" name="email" value="" />

        <div class="mb-3">
            <input
                type="text"
                id="tokenInput"
                class="form-control text-center fw-bold fs-4 py-2"
                placeholder="XXXXXXXXX"
                maxlength="9"
                required
                autocomplete="off"
                style="letter-spacing: 5px; border: 2px dashed #dee2e6"
            />
        </div>
        
        <div id="rememberDeviceContainer" class="mb-3 form-check form-switch text-start d-none">
            <input class="form-check-input" type="checkbox" id="rememberDevice" name="rememberDevice">
            <label class="form-check-label small text-muted" for="rememberDevice">
                Confiar en este dispositivo por 30 días
            </label>
        </div>

        <button
            type="submit"
            id="btnVerify"
            class="btn btn-success w-100 fw-bold py-2 shadow-sm mb-3"
        >
            Verificar Código
        </button>

        <div class="text-center mb-2">
            <button
                type="button"
                id="btnCancelModal"
                class="btn btn-link btn-cancel"
            >
                <i class="fa-solid fa-xmark me-1"></i> <span id="cancelBtnText">Cancelar</span>
            </button>
        </div>

        <hr class="text-muted opacity-25" />

        <div class="mt-3 text-center">
            <p class="mb-0 small text-muted">¿No recibiste el código?</p>
            <button
                type="button"
                id="resendBtn"
                class="btn btn-link btn-sm text-decoration-none fw-bold text-primary"
            >
                Reenviar Código
            </button>
        </div>
    </form>
</div>