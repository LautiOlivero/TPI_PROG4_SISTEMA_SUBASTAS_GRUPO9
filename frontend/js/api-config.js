const API_BASE_URL = "http://localhost:8080";

function getAuthHeaders() {
    const token = localStorage.getItem("accessToken");
    if (!token) return { "Content-Type": "application/json" };
    return {
        "Content-Type": "application/json",
        "Authorization": "Bearer " + token
    };
}

function guardarSesion(loginResponse) {
    localStorage.setItem("accessToken", loginResponse.accessToken);
    localStorage.setItem("user", JSON.stringify(loginResponse.user));
}

function cerrarSesion() {
    localStorage.removeItem("accessToken");
    localStorage.removeItem("user");
    window.location.href = window.location.pathname.includes('/pages/') ? 'login.html' : 'pages/login.html';
}

function usuarioLogueado() {
    const user = localStorage.getItem("user");
    return user ? JSON.parse(user) : null;
}

// Script global para actualizar la barra de navegación en todas las páginas
document.addEventListener('DOMContentLoaded', () => {
    const user = usuarioLogueado();
    const spanSaludo = document.querySelector('.navbar-text.me-3');

    if (spanSaludo && user && user.usernameEmail) {
        const nombre = user.usernameEmail.split('@')[0];
        const esAdmin = user.roles && user.roles.includes('ROLE_ADMIN');
        spanSaludo.textContent = `Hola, ${nombre}`;
    } else if (spanSaludo && !user) {
        spanSaludo.textContent = 'Hola, Invitado';
    }
});
