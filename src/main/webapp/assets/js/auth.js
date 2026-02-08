document.addEventListener('DOMContentLoaded', () => {
    
    // ==========================================
    // 1. CONFIGURACIÓN Y SELECTORES
    // ==========================================
    const contextPath = window.location.pathname.substring(0, window.location.pathname.indexOf("/", 1)) || "";
    const API_PATH = `${contextPath}/auth/`;

    const elements = {
        // --- LOGIN ---
        loginForm:         document.getElementById('loginForm'),
        identifierInput:   document.getElementById('identifier'),
        loginPassword:     document.getElementById('password'),
        loginContainer:    document.getElementById('loginContainer'),

        // Formularios y Contenedores
        registerForm:      document.getElementById('registerForm'),
        tokenForm:         document.getElementById('tokenForm'),
        registerContainer: document.getElementById('registerContainer'),
        modalValidation:   document.getElementById('modalValidation'),
        
        // --- COMPONENTE VERIFY --- (NUEVOS SELECTORES)
        authAction:        document.getElementById('authAction'),
        rememberContainer: document.getElementById('rememberDeviceContainer'), 
        rememberDevice:    document.getElementById('rememberDevice'),
        cancelBtnText:     document.getElementById('cancelBtnText'),
        verifyIconContainer: document.getElementById('verifyIconContainer'),
        verifyIcon:        document.getElementById('verifyIcon'),

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

    let penaltyInterval,resendInterval;
    const ajaxHeaders = { 'X-Requested-With': 'XMLHttpRequest' };

    // ==========================================
    // NUEVO: FUNCIÓN DE PREPARACIÓN DE UI
    // ==========================================
    const prepareVerifyUI = (mode, email) => {
        elements.userEmailHidden.value = email;
        elements.authAction.value = mode;
        
        // Aplicar desenfoque al contenedor que corresponda
        const activeContainer = elements.registerContainer || elements.loginContainer;
        if (activeContainer) activeContainer.classList.add('pointer-events-none');

        if (mode === 'login') {
            elements.rememberContainer.classList.remove('d-none');
            elements.cancelBtnText.innerText = "Regresar al login";
            elements.btnVerify.className = "btn btn-primary w-100 fw-bold py-2 shadow-sm mb-3";
            elements.verifyIconContainer.className = "bg-primary bg-opacity-10 p-3 rounded-circle d-inline-block mb-2 text-primary";
        } else {
            elements.rememberContainer.classList.add('d-none');
            elements.cancelBtnText.innerText = "Cancelar registro";
            elements.btnVerify.className = "btn btn-success w-100 fw-bold py-2 shadow-sm mb-3";
            elements.verifyIconContainer.className = "bg-success bg-opacity-10 p-3 rounded-circle d-inline-block mb-2 text-success";
        }
        
        elements.modalValidation.style.display = 'flex';
    };

    // ==========================================
    // 2. UTILIDADES VISUALES (TOASTS) - Sin cambios
    // ==========================================
    const showToast = (icon, title) => {
        Swal.fire({
            icon: icon, title: title, toast: true, position: 'top-end',
            showConfirmButton: false, timer: 3500, timerProgressBar: true
        });
    };

    // ==========================================
    // 3. VALIDACIÓN DE CONTRASEÑA - Sin cambios
    // ==========================================
    const updateRequirement = (el, isValid) => {
        if (!el) return;
        const icon = el.querySelector('i');
        el.classList.toggle('text-success', isValid);
        el.classList.toggle('text-muted', !isValid);
        icon.className = isValid ? 'fa-solid fa-circle-check me-1' : 'fa-solid fa-circle-xmark me-1';
    };

    const validatePasswords = () => {
        if(!elements.password) return true;
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

    if (elements.password && elements.confirmPassword) {
        ['input', 'blur'].forEach(evt => {
            elements.password.addEventListener(evt, validatePasswords);
            elements.confirmPassword.addEventListener(evt, validatePasswords);
        });
    }

    // ==========================================
    // 4. LÓGICA DEL MODAL (VERIFICACIÓN) - MODIFICADO
    // ==========================================
    if (elements.btnCancelModal) {
        elements.btnCancelModal.addEventListener('click', () => {
            if (penaltyInterval) clearInterval(penaltyInterval);
            if (resendInterval) clearInterval(resendInterval);
            
            elements.modalValidation.style.display = 'none';
            const activeContainer = elements.registerContainer || elements.loginContainer;
            if (activeContainer) activeContainer.classList.remove('pointer-events-none');
            
            elements.tokenInput.disabled = false;
            elements.tokenInput.value = "";
        });
    }

    // startResendCooldown, applyPenalty y resetServerAttempts se mantienen igual...
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
        } catch (err) { console.error(err); }
    };

    // ==========================================
    // 5. PETICIONES AL SERVIDOR (MODIFICADO)
    // ==========================================

    // Registro de Usuario
    if (elements.registerForm) {
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
                const resp = await fetch(`${API_PATH}register`, { 
                    method: 'POST', 
                    body: formData,
                    headers: ajaxHeaders
                });
                const result = await resp.json();
                if (result.status === "success") {
                    prepareVerifyUI('register', elements.emailInput.value); // NUEVO
                    showToast('success', '¡Registro exitoso! Revisa tu correo');
                    startResendCooldown(30);
                } else {
                    showToast('error', result.message || "Error al registrar");
                }
            } catch (err) { showToast('error', 'Error de conexión'); }
            finally { 
                elements.btnRegister.disabled = false; 
                elements.btnRegister.innerText = 'Registrar Cuenta'; 
            }
        });
    }

    // Verificación de Token (MODIFICADO para soportar Login/Register)
        elements.tokenForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const action = elements.authAction.value;
            const params = new URLSearchParams();
            params.append('email', elements.userEmailHidden.value);
            params.append('token', elements.tokenInput.value.trim());
            params.append('remember', elements.rememberDevice.checked);
            try {
                const resp = await fetch(`${API_PATH}verify`, { 
                    method: 'POST', 
                    body: params,
                    headers: ajaxHeaders
                });
                
                const result = await resp.json();

                if (result.status === "success") {
                    showToast('success', '¡Confirmado! Redirigiendo...');
                    
                    // Priorizamos el redirect que viene del servidor, si no, usamos el path manual
                    const destination = result.redirect || `${contextPath}/dashboard`;
                    
                    console.log("Acción:", action);
                    console.log("Destino final:", destination);

                    setTimeout(() => {
                        window.location.href = destination;
                    }, 2000);
                } else if (result.status === "penalty") {
                    applyPenalty(10);
                    showToast('error', result.message);
                } else {
                    showToast('error', result.message);
                }
            } catch (err) { 
                console.error("Error en fetch:", err);
                showToast('error', 'Error de comunicación con el servidor'); 
            }
        });

    elements.resendBtn.addEventListener('click', async () => {
        const params = new URLSearchParams({ email: elements.userEmailHidden.value });
        try {
            const resp = await fetch(`${API_PATH}resend-token`, { 
                method: 'POST', 
                body: params,
                headers: ajaxHeaders
            });
            const result = await resp.json();
            if (result.status === "success") {
                showToast('info', 'Nuevo código enviado');
                startResendCooldown(40);
            }
        } catch (err) { showToast('error', 'Error al reenviar'); }
    });

    // ==========================================
    // 6. LÓGICA DE LOGIN (MODIFICADO)
    // ==========================================
    if (elements.loginForm) {
        elements.loginForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const identifier = elements.identifierInput.value.trim();
            const password = elements.loginPassword.value;
            
            try {
                Swal.fire({
                    title: 'Verificando...', 
                    didOpen: () => { Swal.showLoading(); }, 
                    allowOutsideClick: false
                });

                const params = new URLSearchParams();
                params.append("identifier", identifier);
                params.append("password", password);

                const resp = await fetch(`${API_PATH}login`, { 
                    method: 'POST', 
                    body: params,
                    headers: ajaxHeaders
                });
                
                const result = await resp.json(); 
                Swal.close();

                if (result.status === "success") {
                    // CASO 1: Login directo (IP conocida)
                    showToast('success', result.message);
                    setTimeout(() => { window.location.href = result.redirect; }, 1500);

                } else if (result.status === "needs_verification") { 
                    // CASO 2: IP Nueva / Primer Login (Activación de 2FA)
                    // Usamos el email que nos devuelve el servidor para el modal
                    prepareVerifyUI('login', result.email); 
                    showToast('info', 'Nuevo dispositivo detectado. Verifica tu correo.');
                    startResendCooldown(30);

                } else {
                    // CASO 3: Error de credenciales o bloqueo
                    Swal.fire("Atención", result.message, "warning");
                }
            } catch (err) { 
                showToast('error', 'Error de conexión'); 
            }
        });
    }
});