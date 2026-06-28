// URL de MockAPI para Usuarios
const MOCKAPI_USUARIOS_URL = "https://6a405da01ff1d27becc0c332.mockapi.io/usuarios";

document.addEventListener("DOMContentLoaded", () => {
    const registerForm = document.getElementById('registerForm');

    registerForm.addEventListener('submit', async (e) => {
        e.preventDefault(); // Evita que la página se recargue

        const btnSubmit = document.getElementById('btnSubmit');
        const alertExito = document.getElementById('registroExitoso');

        // Obtenemos los valores de los inputs
        const nombre = document.getElementById('registerInputName1').value;
        const email = document.getElementById('registerInputEmail1').value;
        const password = document.getElementById('registerInputPassword1').value;

        // Armamos el objeto usuario
        const nuevoUsuario = {
            nombre: nombre,
            email: email,
            password: password,
            rol: "USUARIO, VENDEDOR" // Por defecto adquieren ambos roles
        };

        // Cambiamos el botón para mostrar que está cargando
        btnSubmit.disabled = true;
        btnSubmit.innerText = "Registrando...";

        try {
            // Enviamos los datos a MockAPI
            const respuesta = await fetch(MOCKAPI_USUARIOS_URL, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(nuevoUsuario)
            });

            if (respuesta.ok) {
                // Mostrar alerta de éxito
                alertExito.classList.remove('d-none');
                registerForm.reset();
            } else {
                alert("Error al registrar. Asegurate de haber creado el recurso 'usuarios' en MockAPI.");
            }
        } catch (error) {
            console.error(error);
            alert("No se pudo conectar con el servidor.");
        } finally {
            // Restaurar botón
            btnSubmit.disabled = false;
            btnSubmit.innerText = "Registrarse";
        }
    });
});
