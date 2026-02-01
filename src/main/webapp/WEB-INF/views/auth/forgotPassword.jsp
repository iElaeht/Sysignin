<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="es">
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Recuperar Contraseña - SySignin</title>
    <script src="https://cdn.tailwindcss.com"></script>
  </head>
  <body class="bg-slate-900 flex items-center justify-center min-h-screen">
    <div
      class="bg-slate-800 p-8 rounded-2xl shadow-2xl w-full max-w-md border border-slate-700 text-center"
    >
      <div class="mb-6">
        <div
          class="mx-auto w-16 h-16 bg-blue-900/30 flex items-center justify-center rounded-full mb-4"
        >
          <svg
            class="w-8 h-8 text-blue-500"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M15 7a2 2 0 012 2m4 0a6 6 0 01-7.743 5.743L11 17H9v2H7v2H4a1 1 0 01-1-1v-2.586a1 1 0 01.293-.707l5.964-5.964A6 6 0 1121 9z"
            />
          </svg>
        </div>
        <h1 class="text-2xl font-bold text-white mb-2">
          ¿Olvidaste tu contraseña?
        </h1>
        <p class="text-slate-400 text-sm">
          No te preocupes, introduce tu correo y te enviaremos instrucciones.
        </p>
      </div>

      <form
        action="${pageContext.request.contextPath}/auth/request-password-change"
        method="POST"
        class="space-y-6"
      >
        <div class="text-left">
          <label class="block text-sm font-medium text-slate-300 mb-2 text-left"
            >Correo electrónico</label
          >
          <input
            type="email"
            name="email"
            required
            class="w-full px-4 py-3 bg-slate-900 border border-slate-700 rounded-lg text-white focus:ring-2 focus:ring-blue-500 outline-none transition"
            placeholder="tu@correo.com"
          />
        </div>

        <button
          type="submit"
          class="w-full bg-blue-600 hover:bg-blue-500 text-white font-bold py-3 rounded-lg shadow-lg transition transform active:scale-95"
        >
          Enviar enlace
        </button>
      </form>

      <div class="mt-8">
        <a
          href="${pageContext.request.contextPath}/auth/loginView"
          class="text-sm text-slate-400 hover:text-white transition flex items-center justify-center gap-2"
        >
          <svg
            class="w-4 h-4"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              d="M10 19l-7-7m0 0l7-7m-7 7h18"
              stroke-width="2"
              stroke-linecap="round"
              stroke-linejoin="round"
            />
          </svg>
          Volver al login
        </a>
      </div>
    </div>
  </body>
</html>
