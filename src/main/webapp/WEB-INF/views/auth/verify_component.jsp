<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<div id="modalValidation" class="hidden fixed inset-0 z-50 flex items-center justify-center bg-dark bg-opacity-50 backdrop-blur-sm">
    <div class="container d-flex justify-content-center">
        <div class="card shadow-lg p-4 border-0" style="max-width: 400px; width: 100%; border-radius: 15px; background: white;">
            
            <div class="text-center mb-4">
                <div class="display-6 text-primary mb-2">
                    <i class="fa-solid fa-envelope-shield"></i>
                </div>
                <h3 class="fw-bold">Verifica tu Identidad</h3>
                <p class="text-muted small">Hemos enviado un código de 10 caracteres a tu correo. Por favor, ingrésalo debajo.</p>
            </div>

            <form id="tokenForm">
                <input type="hidden" id="userEmail">

                <div class="mb-3">
                    <label for="tokenInput" class="form-label fw-semibold">Código de Verificación</label>
                    <input type="text" id="tokenInput" maxlength="10" 
                           class="form-control form-control-lg text-center fw-bold tracking-widest border-2" 
                           placeholder="0A1B2C3D4E" 
                           style="letter-spacing: 0.2rem; border-color: #e0e0e0; text-transform: uppercase;"
                           required>
                </div>

                <div id="rememberDeviceContainer" class="form-check mb-4 hidden">
                    <input class="form-check-input" type="checkbox" id="rememberDevice">
                    <label class="form-check-label small text-muted" for="rememberDevice">
                        Confío en este dispositivo (No pedir código por 30 días)
                    </label>
                </div>

                <div class="d-grid gap-2">
                    <button type="submit" class="btn btn-primary py-2 fw-bold shadow-sm">
                        Confirmar y Acceder
                    </button>
                    <button type="button" id="resendBtn" class="btn btn-outline-secondary btn-sm border-0">
                        <i class="fa-solid fa-rotate-right me-1"></i> Reenviar código
                    </button>
                </div>
            </form>

            <div class="text-center mt-3">
                <button type="button" onclick="location.reload()" class="btn btn-link text-decoration-none text-muted small">
                    Cancelar proceso
                </button>
            </div>
        </div>
    </div>
</div>

<style>
    /* Clases de utilidad rápidas si no usas Tailwind completo */
    .hidden { display: none !important; }
    .fixed { position: fixed; }
    .inset-0 { top: 0; right: 0; bottom: 0; left: 0; }
    .bg-opacity-50 { background-color: rgba(0, 0, 0, 0.5); }
    .backdrop-blur-sm { backdrop-filter: blur(4px); }
    .tracking-widest { letter-spacing: 0.25em; }
    
    #modalValidation {
        animation: fadeInScale 0.3s ease-out;
    }

    @keyframes fadeInScale {
        from { opacity: 0; transform: scale(0.95); }
        to { opacity: 1; transform: scale(1); }
    }

    #tokenInput:focus {
        border-color: #4e73df;
        box-shadow: 0 0 0 0.25 source rgba(78, 115, 223, 0.25);
    }
</style>