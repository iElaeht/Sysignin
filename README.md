# 🔐 Sistema de Autenticación Segura - Java Web (Módulo de Registro)

Este proyecto representa el núcleo de un sistema de autenticación empresarial desarrollado en Java, enfocado en la **seguridad proactiva** y una experiencia de usuario fluida.

---

## 🛡️ Pilares de Seguridad (Fase de Registro)

Se ha implementado un sistema de defensa en profundidad para asegurar que los datos de los usuarios estén protegidos desde el primer segundo:

### 1. Blindaje contra Inyecciones (Anti-XSS)
- **Detección de Scripts:** El sistema cuenta con una utilidad de validación que bloquea cualquier intento de inyección de código HTML o JavaScript (`<script>`, `onclick`, etc.).
- **Validación Multicapa:** Los datos se limpian en el navegador (Frontend) para mayor rapidez, pero se validan obligatoriamente en el servidor (Backend) para máxima seguridad.

### 2. Normalización de Datos
- **Estandarización de Correos:** Todos los emails se procesan automáticamente en minúsculas y sin espacios, evitando duplicados y errores de inicio de sesión.
- **Tratamiento de Tokens:** Los códigos de verificación son normalizados para que el sistema no sea sensible a mayúsculas/minúsculas, mejorando la usabilidad en dispositivos móviles.

### 3. Control de Abuso (Anti-Spam)
- **Cooldown de Registro:** Se ha establecido un tiempo de espera de **30 segundos** entre solicitudes de registro desde la misma cuenta para evitar el spam.
- **Penalización Temporal:** Implementación de bloqueos de **15 minutos** para usuarios que exceden los intentos fallidos, mitigando ataques de fuerza bruta.

---

## 🏗️ Arquitectura del Software

El proyecto sigue una estructura limpia y categorizada para facilitar su escalabilidad:

* **Controllers (`AuthServlet`):** Centraliza las peticiones HTTP, organizadas por categorías (Auth, Registro, Passwords).
* **Service Layer (`AuthService`):** Contiene la lógica de negocio pura, aislada de la tecnología de presentación.
* **Utility Layer (`ValidationUtils`, `NetUtils`):** Herramientas modulares para validación de seguridad y manejo de redes.
* **Data Access (`DAOs`):** Gestión limpia de la persistencia de datos.

---

## 🛠️ Tecnologías Implementadas

| Tecnología | Propósito |
| :--- | :--- |
| **Java Jakarta EE** | Core del servidor y manejo de Servlets. |
| **BCrypt** | Encriptación de contraseñas con sal (salting) aleatoria. |
| **JavaScript (ES6+)** | Validaciones dinámicas y gestión de interfaz (Toasts). |
| **UUID** | Identificadores únicos universales para sesiones seguras. |
| **JSP / CSS3** | Interfaz responsiva y amigable. |

---

## 📌 Estado del Proyecto: Fase 1 Completada
- [x] Diseño de base de datos y DAOs.
- [x] Flujo de registro seguro con envío de tokens por correo.
- [x] Sistema de validación y blindaje anti-scripts.
- [ ] Implementación de Login y Gestión de Sesiones (En progreso...).
- [ ] Autenticación con Google OAuth2.0.

---
*Desarrollado con enfoque en ciberseguridad y buenas prácticas de ingeniería de software con IA.*