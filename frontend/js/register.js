const API_BASE_URL = "http://localhost:8080";

document.addEventListener("DOMContentLoaded", () => {
    const registerForm = document.getElementById('registerForm');

    registerForm.addEventListener('submit', async (e) => {
        e.preventDefault();

        const btnSubmit = document.getElementById('btnSubmit');
        const alertExito = document.getElementById('registroExitoso');
        const alertError = document.createElement('div');

        const email = document.getElementById('registerInputEmail1').value;
        const password = document.getElementById('registerInputPassword1').value;

        btnSubmit.disabled = true;
        btnSubmit.innerText = "Registrando...";

        try {
            const respuesta = await fetch(API_BASE_URL + '/auth/register', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ usernameEmail: email, password: password })
            });

            if (respuesta.ok) {
                const data = await respuesta.json();
                localStorage.setItem("accessToken", data.accessToken);
                localStorage.setItem("user", JSON.stringify(data.user));
                alertExito.classList.remove('d-none');
                registerForm.reset();

                setTimeout(() => {
                    window.location.href = '../index.html';
                }, 1500);
            } else {
                const error = await respuesta.json();
                alert("Error: " + (error.message || "No se pudo completar el registro."));
            }
        } catch (error) {
            console.error(error);
            alert("No se pudo conectar con el servidor.");
        } finally {
            btnSubmit.disabled = false;
            btnSubmit.innerText = "Registrarse";
        }
    });
});
