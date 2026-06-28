const MOCKAPI_URL = "https://6a405da01ff1d27becc0c332.mockapi.io/subastas";

document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById('crearSubastaForm');

    form.addEventListener('submit', async (e) => {
        e.preventDefault();

        try {
            // 1. Recopilar datos
            const nombreInput = document.getElementById('titulo').value;
            const categoria = document.getElementById('categoria').value;
            const descripcion = document.getElementById('descripcion').value;
            const imagenUrl = document.getElementById('imagenUrl').value;
            const precioBase = parseFloat(document.getElementById('precioBase').value);
            const incrementoFijo = parseFloat(document.getElementById('incrementoFijo').value);
            const fechaInicio = document.getElementById('fechaInicio').value;
            const fechaCierre = document.getElementById('fechaCierre').value;

            // 2. Validaciones
            const now = new Date();
            const start = new Date(fechaInicio);
            const end = new Date(fechaCierre);

            if (isNaN(start.getTime()) || isNaN(end.getTime())) {
                Swal.fire({
                    icon: 'error',
                    title: 'Error de Validación',
                    text: 'Las fechas ingresadas no son válidas.'
                });
                return;
            }

            if (start <= now) {
                Swal.fire({
                    icon: 'error',
                    title: 'Error de Validación',
                    text: 'La fecha de inicio debe ser posterior a la fecha y hora actual.'
                });
                return;
            }

            if (end <= start) {
                Swal.fire({
                    icon: 'error',
                    title: 'Error de Validación',
                    text: 'La fecha de cierre debe ser posterior a la fecha de inicio.'
                });
                return;
            }

            if (isNaN(precioBase) || isNaN(incrementoFijo) || precioBase <= 0 || incrementoFijo <= 0) {
                Swal.fire({
                    icon: 'error',
                    title: 'Error de Validación',
                    text: 'Los precios y montos deben ser números mayores a cero.'
                });
                return;
            }

            // 3. Preparar Payload
            const nuevaSubasta = {
                precioBase: precioBase,
                montoActual: precioBase, // Al crear, el monto actual es el base
                incrementoFijo: incrementoFijo,
                fechaInicio: start.toISOString(),
                fechaCierre: end.toISOString(),
                estado: 'BORRADOR',
                producto: {
                    nombre: nombreInput,
                    descripcion: descripcion,
                    imagenUrl: imagenUrl,
                    categoria: {
                        nombre: categoria
                    }
                },
                vendedor: {
                    usernameEmail: "Rematador Oficial" // Simulado
                }
            };

            // 4. Enviar a MockAPI
            Swal.fire({
                title: 'Guardando...',
                text: 'Por favor espere',
                allowOutsideClick: false,
                didOpen: () => {
                    Swal.showLoading();
                }
            });

            const response = await fetch(MOCKAPI_URL, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(nuevaSubasta)
            });

            if (!response.ok) {
                throw new Error('Error al guardar la subasta en el servidor');
            }

            // Exito
            Swal.fire({
                icon: 'success',
                title: 'Borrador Guardado',
                text: 'Tu subasta ha sido creada como borrador exitosamente.',
                confirmButtonText: 'Ir a Mis Subastas'
            }).then(() => {
                window.location.href = 'mis-subastas.html';
            });

        } catch (error) {
            console.error('Error no capturado:', error);
            Swal.fire({
                icon: 'error',
                title: 'Error del Sistema',
                text: 'Ocurrió un error inesperado: ' + error.message
            });
        }
    });
});
