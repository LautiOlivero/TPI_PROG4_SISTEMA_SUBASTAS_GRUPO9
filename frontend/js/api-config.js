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
    window.location.href = "index.html";
}

function usuarioLogueado() {
    const user = localStorage.getItem("user");
    return user ? JSON.parse(user) : null;
}
