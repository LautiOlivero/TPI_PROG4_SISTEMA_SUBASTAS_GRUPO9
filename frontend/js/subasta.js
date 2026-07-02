let subastaActual = null;

document.addEventListener("DOMContentLoaded", () => {
    const params = new URLSearchParams(window.location.search);
    const subastaId = params.get('id');

    if (!subastaId) {
        Swal.fire({ icon: 'error', title: 'Oops...', text: 'ID de subasta no válido.' }).then(() => {
            window.location.href = "../index.html";
        });
        return;
    }

    cargarDetalleSubasta(subastaId);

    const btnOfertar = document.getElementById('btn-ofertar');
    if (btnOfertar) {
        btnOfertar.addEventListener('click', () => realizarOferta(subastaId));
    }
});

async function cargarDetalleSubasta(id) {
    const loader = document.getElementById('loader');
    const containerDetalle = document.getElementById('subasta-detalle');

    try {
        const respuesta = await fetch(API_BASE_URL + '/api/subastas/' + id);

        if (!respuesta.ok) {
            throw new Error("No se pudo encontrar la subasta.");
        }

        subastaActual = await respuesta.json();

        loader.style.display = 'none';
        containerDetalle.style.display = 'flex';

        renderizarDetalles(subastaActual);

    } catch (error) {
        loader.innerHTML = `<div class="alert alert-danger">Error: ${error.message}</div>`;
        setTimeout(() => {
            window.location.href = "../index.html";
        }, 3000);
    }
}

async function realizarOferta(id) {
    if (!subastaActual) return;
    if (subastaActual.estado !== 'ACTIVA') {
        Swal.fire({ icon: 'warning', title: 'Subasta Cerrada', text: 'La subasta no está activa en este momento.' });
        return;
    }

    const result = await Swal.fire({
        title: 'Confirmar Oferta',
        text: `¿Estás seguro de realizar una oferta por esta subasta?`,
        icon: 'question',
        showCancelButton: true,
        confirmButtonColor: '#0d6efd',
        cancelButtonColor: '#6c757d',
        confirmButtonText: 'Sí, ofertar',
        cancelButtonText: 'Cancelar'
    });

    if (result.isConfirmed) {
        try {
            Swal.fire({ title: 'Procesando...', allowOutsideClick: false, didOpen: () => Swal.showLoading() });

            const response = await fetch(API_BASE_URL + '/api/pujas', {
                method: 'POST',
                headers: {
                    ...getAuthHeaders(),
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ subastaId: parseInt(id) })
            });

            if (!response.ok) {
                const err = await response.json();
                throw new Error(err.message || 'Error al procesar la oferta');
            }

            await Swal.fire('¡Éxito!', 'Tu oferta ha sido registrada correctamente.', 'success');

            // Recargar detalles de la subasta para actualizar precios y ganador
            cargarDetalleSubasta(id);

        } catch (error) {
            console.error('Error:', error);
            Swal.fire('Error', error.message || 'Hubo un problema al ofertar. Verifica que hayas iniciado sesión.', 'error');
        }
    }
}

function renderizarDetalles(subasta) {
    const formater = new Intl.NumberFormat('es-AR', { style: 'currency', currency: 'ARS', minimumFractionDigits: 0 });

    document.getElementById('imagen-principal').src = subasta.producto?.imagenUrl || 'https://via.placeholder.com/800x450?text=Sin+Imagen';
    document.getElementById('titulo-subasta').textContent = subasta.producto?.nombre || 'Sin nombre';
    document.getElementById('descripcion-texto').textContent = subasta.producto?.descripcion || 'Sin descripción';
    document.getElementById('vendedor-nombre').textContent = subasta.vendedor?.usernameEmail || 'Usuario Anónimo';

    const badgeEstado = document.getElementById('badge-estado');
    badgeEstado.textContent = subasta.estado;
    badgeEstado.className = 'badge mb-2 px-3 py-2 rounded-pill fw-medium';
    if (subasta.estado === 'ACTIVA') badgeEstado.classList.add('bg-success');
    else if (subasta.estado === 'PUBLICADA') badgeEstado.classList.add('bg-primary');
    else if (subasta.estado === 'FINALIZADA') badgeEstado.classList.add('bg-secondary');
    else badgeEstado.classList.add('bg-danger');

    const badgeCategoria = document.getElementById('badge-categoria');
    badgeCategoria.textContent = subasta.producto?.categoria?.nombre || 'General';

    document.getElementById('precio-base').textContent = formater.format(subasta.precioBase);

    const montoActualEl = document.getElementById('monto-actual');
    const nuevoMontoFormateado = formater.format(subasta.montoActual);
    montoActualEl.textContent = nuevoMontoFormateado;

    const fechaCierre = new Date(subasta.fechaCierre);
    document.getElementById('fecha-cierre').textContent = fechaCierre.toLocaleString('es-AR', { dateStyle: 'short', timeStyle: 'short' });

    iniciarRelojDescuento(subasta.fechaCierre);

    const incremento = subasta.incrementoFijo || 10000;
    const montoBaseCalculo = subasta.montoActual > 0 ? subasta.montoActual : subasta.precioBase;
    const proximaOferta = montoBaseCalculo + incremento;
    const impuestos = proximaOferta * 0.10;

    document.getElementById('incremento-fijo').textContent = formater.format(incremento);
    document.getElementById('impuestos').textContent = formater.format(impuestos);
    document.getElementById('proxima-oferta').textContent = formater.format(proximaOferta + impuestos);

    const btnOfertar = document.getElementById('btn-ofertar');
    if (btnOfertar && !btnOfertar.disabled) {
        btnOfertar.innerHTML = `<i class="bi bi-hammer me-2"></i> OFERTAR ${formater.format(proximaOferta)}`;
    }
}

let intervaloReloj = null;

function iniciarRelojDescuento(fechaCierreStr) {
    if (intervaloReloj) clearInterval(intervaloReloj);

    const fechaCierre = new Date(fechaCierreStr).getTime();
    const relojEl = document.getElementById('reloj-descuento');
    const btnOfertar = document.getElementById('btn-ofertar');

    actualizarReloj();
    intervaloReloj = setInterval(actualizarReloj, 1000);

    function actualizarReloj() {
        const ahora = new Date().getTime();
        const distancia = fechaCierre - ahora;

        if (distancia < 0) {
            clearInterval(intervaloReloj);
            if (relojEl) {
                relojEl.innerHTML = "FINALIZADA";
                relojEl.classList.replace('text-danger', 'text-muted');
            }
            if (btnOfertar) btnOfertar.disabled = true;
            return;
        }

        const dias = Math.floor(distancia / (1000 * 60 * 60 * 24));
        const horas = Math.floor((distancia % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
        const minutos = Math.floor((distancia % (1000 * 60 * 60)) / (1000 * 60));
        const segundos = Math.floor((distancia % (1000 * 60)) / 1000);

        let texto = "";
        if (dias > 0) texto += `${dias}d `;
        texto += `${horas.toString().padStart(2, '0')}:${minutos.toString().padStart(2, '0')}:${segundos.toString().padStart(2, '0')}`;

        if (relojEl) relojEl.innerHTML = texto;
    }
}
