document.addEventListener('DOMContentLoaded', () => {
    
    // ==========================================
    // 1. CONFIGURACIÓN Y SELECTORES
    // ==========================================
    const API_PATH = ''; // Ruta relativa para evitar error 404 /auth/auth/

    const elements = {
        // Formularios y Contenedores
        registerForm:      document.getElementById('registerForm'),
        tokenForm:         document.getElementById('tokenForm'),
        registerContainer: document.getElementById('registerContainer'),
        modalValidation:   document.getElementById('modalValidation'),
        
        // Inputs y Datos
        emailInput:        document.getElementById('email'),
        userEmailHidden:   document.getElementById('userEmail'),
        tokenInput:        document.getElementById('tokenInput'),
        password:          document.getElementById('password'),
        confirmPassword:   document.getElementById('confirmPassword'),
        
        // Botones
        btnRegister:       document.getElementById('btnRegister'),
        btnVerify:         document.getElementById('btnVerify'),
        btnCancelModal:    document.getElementById('btnCancelModal'),
        resendBtn:         document.getElementById('resendBtn'),
        
        // Indicadores de Requisitos
        reqLength:         document.getElementById('req-length'),
        reqMayus:          document.getElementById('req-mayus'),
        reqNumber:         document.getElementById('req-number'),
        reqMatch:          document.getElementById('req-match')
    };

    let penaltyInterval;
    let resendInterval;

    // ==========================================
    // 2. UTILIDADES VISUALES (TOASTS)
    // ==========================================
    const showToast = (icon, title) => {
        Swal.fire({
            icon: icon,
            title: title,
            toast: true,
            position: 'top-end',
            showConfirmButton: false,
            timer: 3500,
            timerProgressBar: true
        });
    };

    // ==========================================
    // 3. VALIDACIÓN DE CONTRASEÑA
    // ==========================================
    const updateRequirement = (el, isValid) => {
        if (!el) return;
        const icon = el.querySelector('i');
        el.classList.toggle('text-success', isValid);
        el.classList.toggle('text-muted', !isValid);
        icon.className = isValid ? 'fa-solid fa-circle-check me-1' : 'fa-solid fa-circle-xmark me-1';
    };

    const validatePasswords = () => {
        const pass = elements.password.value;
        const confirm = elements.confirmPassword.value;

        const isLongEnough = pass.length >= 8;
        const hasMayus = /[A-Z]/.test(pass);
        const hasNumber = /\d/.test(pass);
        const matches = pass === confirm && pass !== "";

        updateRequirement(elements.reqLength, isLongEnough);
        updateRequirement(elements.reqMayus, hasMayus);
        updateRequirement(elements.reqNumber, hasNumber);
        updateRequirement(elements.reqMatch, matches);

        return isLongEnough && hasMayus && hasNumber && matches;
    };

    // Listeners para validación en tiempo real
    if (elements.password && elements.confirmPassword) {
        ['input', 'blur'].forEach(evt => {
            elements.password.addEventListener(evt, validatePasswords);
            elements.confirmPassword.addEventListener(evt, validatePasswords);
        });
    }

    // ==========================================
    // 4. LÓGICA DEL MODAL (VERIFICACIÓN)
    // ==========================================
    
    // Botón Cancelar: Cierra el modal y limpia el efecto blur
    if (elements.btnCancelModal) {
        elements.btnCancelModal.addEventListener('click', () => {
            if (penaltyInterval) clearInterval(penaltyInterval);
            if (resendInterval) clearInterval(resendInterval);
            
            elements.modalValidation.style.display = 'none';
            elements.registerContainer.classList.remove('pointer-events-none');
            elements.btnVerify.classList.remove('btn-secondary');
            elements.btnVerify.classList.add('btn-success');
            elements.btnVerify.innerHTML = "Verificar Código";
            
            elements.tokenInput.disabled = false;
            elements.tokenInput.value = "";
            elements.tokenInput.placeholder = "XXXXXX";
        });
    }

    // Timer para el botón de Reenviar
    const startResendCooldown = (seconds) => {
        let timeLeft = seconds;
        elements.resendBtn.style.pointerEvents = 'none';
        elements.resendBtn.style.opacity = '0.5';

        if (resendInterval) clearInterval(resendInterval);
        resendInterval = setInterval(() => {
            elements.resendBtn.innerText = `Reenviar en ${timeLeft}s`;
            if (timeLeft-- <= 0) {
                clearInterval(resendInterval);
                elements.resendBtn.innerText = "Generar nuevo código";
                elements.resendBtn.style.pointerEvents = 'auto';
                elements.resendBtn.style.opacity = '1';
            }
        }, 1000);
    };

    // Bloqueo por demasiados intentos
    const applyPenalty = (minutes) => {
        let secondsLeft = minutes * 60;
        if (penaltyInterval) clearInterval(penaltyInterval);

        const updateUI = () => {
            const m = Math.floor(secondsLeft / 60);
            const s = secondsLeft % 60;
            const timeStr = `${m}:${s < 10 ? '0' : ''}${s}`;

            elements.tokenInput.disabled = true;
            elements.tokenInput.placeholder = `BLOQUEADO (${timeStr})`;
            elements.btnVerify.disabled = true;
            elements.btnVerify.innerHTML = `<i class="fa-solid fa-clock me-2"></i> ${timeStr}`;

            if (secondsLeft-- <= 0) {
                clearInterval(penaltyInterval);
                resetServerAttempts(); 
                elements.tokenInput.disabled = false;
                elements.tokenInput.placeholder = "XXXXXX";
                elements.btnVerify.disabled = false;
                elements.btnVerify.classList.replace('btn-secondary', 'btn-success');
                elements.btnVerify.innerHTML = "Verificar Código";
                
                showToast('info', 'Ya puedes intentar de nuevo');
            }
        };
        updateUI();
        penaltyInterval = setInterval(updateUI, 1000);
    };
    const resetServerAttempts = async () => {
        try {
            await fetch(`${API_PATH}reset-attempts`, { 
                method: 'POST', 
                body: new URLSearchParams({ email: elements.userEmailHidden.value }) 
            });
        } catch (err) {
            console.error("Error al resetear intentos en servidor:", err);
        }
    };

    // ==========================================
    // 5. PETICIONES AL SERVIDOR (FETCH)
    // ==========================================

    // Registro de Usuario
    elements.registerForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        if (!validatePasswords()) {
            showToast('warning', 'Revisa los requisitos de seguridad');
            return;
        }

        const formData = new URLSearchParams(new FormData(elements.registerForm));
        try {
            elements.btnRegister.disabled = true;
            elements.btnRegister.innerHTML = '<span class="spinner-border spinner-border-sm"></span>';

            const resp = await fetch(`${API_PATH}register`, { method: 'POST', body: formData });
            const result = await resp.text();

            if (result.includes("SUCCESS")) {
                // Preparar modal de verificación
                elements.userEmailHidden.value = elements.emailInput.value;
                elements.modalValidation.style.display = 'flex';
                elements.registerContainer.classList.add('pointer-events-none');
                
                showToast('success', '¡Registro exitoso! Revisa tu correo');
                startResendCooldown(30);
            } else {
                showToast('error', result.replace("ERROR: ", ""));
            }
        } catch (err) {
            showToast('error', 'Error de conexión con el servidor');
        } finally {
            elements.btnRegister.disabled = false;
            elements.btnRegister.innerText = 'Registrar Cuenta';
        }
    });

    // Verificación de Token
    elements.tokenForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        const params = new URLSearchParams({
            email: elements.userEmailHidden.value,
            token: elements.tokenInput.value.trim()
        });

        try {
            const resp = await fetch(`${API_PATH}verify`, { method: 'POST', body: params });
            const result = await resp.text();

            if (result.includes("SUCCESS")) {
                showToast('success', '¡Cuenta activada! Redirigiendo...');
                setTimeout(() => window.location.href = "../dashboard", 2000);
            } else if (result.includes("PENALIZADA")) {
                applyPenalty(1);
                showToast('error', 'Cuenta bloqueada temporalmente');
            } else {
                showToast('error', 'Código incorrecto');
                elements.tokenInput.value = "";
            }
        } catch (err) {
            showToast('error', 'Error al verificar código');
        }
    });

    // Reenvío de Token
    elements.resendBtn.addEventListener('click', async () => {
        const params = new URLSearchParams({ email: elements.userEmailHidden.value });
        try {
            const resp = await fetch(`${API_PATH}resend-token`, { method: 'POST', body: params });
            const result = await resp.text();
            
            if (result.includes("SUCCESS")) {
                showToast('info', 'Nuevo código enviado');
                startResendCooldown(40);
            } else {
                showToast('warning', result.replace("ERROR: ", ""));
            }
        } catch (err) {
            showToast('error', 'No se pudo reenviar el código');
        }
    });
});