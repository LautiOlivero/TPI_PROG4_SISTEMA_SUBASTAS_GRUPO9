const MOCKAPI_URL = "https://6a405da01ff1d27becc0c332.mockapi.io/subastas";
let subastaActual = null;
let pollingInterval = null;

document.addEventListener("DOMContentLoaded", () => {
    // Obtener el ID de la subasta desde la URL
    const params = new URLSearchParams(window.location.search);
    const subastaId = params.get('id');

    if (!subastaId) {
        Swal.fire({
            icon: 'error',
            title: 'Oops...',
            text: 'ID de subasta no válido.'
        }).then(() => {
            window.location.href = "../index.html";
        });
        return;
    }

    cargarDetalleSubasta(subastaId);

    // Próximamente: Conectar con Spring Boot (WebSockets / SSE)
    // El backend empujará las actualizaciones al frontend cuando haya una nueva puja.
    // connectWebSocket(subastaId);

    // Event listener para el botón de ofertar
    const btnOfertar = document.getElementById('btn-ofertar');
    if (btnOfertar) {
        btnOfertar.addEventListener('click', () => realizarOferta(subastaId));
    }
});

async function cargarDetalleSubasta(id) {
    const loader = document.getElementById('loader');
    const containerDetalle = document.getElementById('subasta-detalle');

    try {
        const respuesta = await fetch(`${MOCKAPI_URL}/${id}`);
        
        if (!respuesta.ok) {
            throw new Error("No se pudo encontrar la subasta.");
        }

        subastaActual = await respuesta.json();
        
        // Ocultar loader y mostrar detalle
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

// Función silenciosa para actualizar los datos en tiempo real (polling)
async function actualizarDatosSubasta(id) {
    try {
        const respuesta = await fetch(`${MOCKAPI_URL}/${id}`);
        if (respuesta.ok) {
            const subastaActualizada = await respuesta.json();
            
            // Si hubo un cambio en el monto o estado, re-renderizamos para actualizar la UI
            if (subastaActualizada.montoActual !== subastaActual.montoActual || subastaActualizada.estado !== subastaActual.estado) {
                subastaActual = subastaActualizada;
                renderizarDetalles(subastaActual);
                console.log("La subasta se actualizó automáticamente (nueva puja de otro usuario).");
            }
        }
    } catch (error) {
        console.error("Error al actualizar la subasta:", error);
    }
}

async function realizarOferta(id) {
    if (!subastaActual) return;
    if (subastaActual.estado !== 'ACTIVA') {
        Swal.fire({
            icon: 'warning',
            title: 'Subasta Cerrada',
            text: 'La subasta no está activa en este momento.'
        });
        return;
    }

    const btnOfertar = document.getElementById('btn-ofertar');
    
    // Estado de carga en el botón
    btnOfertar.disabled = true;
    btnOfertar.innerHTML = `<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Procesando...`;

    try {
        // Calcular nuevo monto
        const incremento = subastaActual.incrementoFijo || 10000;
        const montoBaseCalculo = subastaActual.montoActual > 0 ? subastaActual.montoActual : subastaActual.precioBase;
        const nuevoMonto = montoBaseCalculo + incremento;

        const payload = {
            montoActual: nuevoMonto
        };

        // Enviar puja a MockAPI mediante PUT
        const respuesta = await fetch(`${MOCKAPI_URL}/${id}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(payload)
        });

        if (!respuesta.ok) throw new Error("No se pudo procesar la oferta");

        // Actualizamos estado local con la respuesta
        const subastaActualizada = await respuesta.json();
        subastaActual = subastaActualizada;
        
        Swal.fire({
            icon: 'success',
            title: '¡Oferta Confirmada!',
            text: `Has ofertado exitosamente.`,
            confirmButtonText: 'Aceptar'
        });

    } catch (error) {
        console.error(error);
        Swal.fire({
            icon: 'error',
            title: 'Error',
            text: 'Error al procesar la oferta. Intente de nuevo.'
        });
    } finally {
        // Restaurar estado del botón con los datos correctos
        btnOfertar.disabled = false;
        renderizarDetalles(subastaActual);
    }
}

function renderizarDetalles(subasta) {
    // Formatear precios
    const formater = new Intl.NumberFormat('es-AR', {
        style: 'currency',
        currency: 'ARS',
        minimumFractionDigits: 0
    });

    // Imágenes y Textos
    document.getElementById('imagen-principal').src = subasta.producto?.imagenUrl || 'https://via.placeholder.com/800x450?text=Sin+Imagen';
    document.getElementById('titulo-subasta').textContent = subasta.producto?.nombre || 'Sin nombre';
    document.getElementById('descripcion-texto').textContent = subasta.producto?.descripcion || 'Sin descripción';
    document.getElementById('vendedor-nombre').textContent = subasta.vendedor?.usernameEmail || 'Usuario Anónimo';
    
    // Badges de Estado y Categoría
    const badgeEstado = document.getElementById('badge-estado');
    badgeEstado.textContent = subasta.estado;
    badgeEstado.className = 'badge mb-2 px-3 py-2 rounded-pill fw-medium'; 
    if (subasta.estado === 'ACTIVA') badgeEstado.classList.add('bg-success');
    else if (subasta.estado === 'PUBLICADA') badgeEstado.classList.add('bg-primary');
    else if (subasta.estado === 'FINALIZADA') badgeEstado.classList.add('bg-secondary');
    else badgeEstado.classList.add('bg-danger');

    const badgeCategoria = document.getElementById('badge-categoria');
    badgeCategoria.textContent = subasta.producto?.categoria?.nombre || 'General';

    // Precios y Fechas
    document.getElementById('precio-base').textContent = formater.format(subasta.precioBase);
    
    // Animar la actualización del precio actual (para que sea evidente cuando cambia)
    const montoActualEl = document.getElementById('monto-actual');
    const nuevoMontoFormateado = formater.format(subasta.montoActual);
    if (montoActualEl.textContent !== nuevoMontoFormateado && montoActualEl.textContent !== '') {
        // Si el precio cambió, le damos un color verde temporario
        montoActualEl.classList.remove('text-primary');
        montoActualEl.classList.add('text-success');
        setTimeout(() => {
            montoActualEl.classList.remove('text-success');
            montoActualEl.classList.add('text-primary');
        }, 1500);
    }
    montoActualEl.textContent = nuevoMontoFormateado;
    
    const fechaCierre = new Date(subasta.fechaCierre);
    document.getElementById('fecha-cierre').textContent = fechaCierre.toLocaleString('es-AR', { 
        dateStyle: 'short', 
        timeStyle: 'short' 
    });

    iniciarRelojDescuento(subasta.fechaCierre);

    // Composición de Oferta
    const incremento = subasta.incrementoFijo || 10000;
    const montoBaseCalculo = subasta.montoActual > 0 ? subasta.montoActual : subasta.precioBase;
    const proximaOferta = montoBaseCalculo + incremento;
    const impuestos = proximaOferta * 0.10; // Ejemplo 10%

    document.getElementById('incremento-fijo').textContent = formater.format(incremento);
    document.getElementById('impuestos').textContent = formater.format(impuestos);
    document.getElementById('proxima-oferta').textContent = formater.format(proximaOferta + impuestos);

    // Actualizar el texto del botón con el monto de la próxima oferta
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
