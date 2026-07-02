let subastasGlobales = [];
let estadoActual = 'ACTIVA';

document.addEventListener("DOMContentLoaded", () => {
    cargarSubastas();

    const user = usuarioLogueado();
    const esAdmin = user && user.roles && user.roles.includes('ROLE_ADMIN');
    if (!esAdmin) {
        const btnAdmin = document.getElementById('nav-admin');
        if (btnAdmin) btnAdmin.style.display = 'none';
    }
});

async function cargarSubastas() {
    const loader = document.getElementById('loader');

    try {
        const respuesta = await fetch(API_BASE_URL + '/api/subastas');
        if (!respuesta.ok) throw new Error("No se pudo conectar al backend.");

        subastasGlobales = await respuesta.json();

        loader.style.display = 'none';

        filtrarCombinado();

    } catch (error) {
        loader.innerHTML = `<div class="alert alert-danger">Error: ${error.message}</div>`;
    }
}

function filtrarPorEstado(estado, elementoBoton) {
    estadoActual = estado;

    document.querySelectorAll('#estadoTabs .nav-link').forEach(btn => btn.classList.remove('active'));
    if (elementoBoton) elementoBoton.classList.add('active');

    filtrarCombinado();
}

function filtrarCombinado() {
    const categoriaSeleccionada = document.getElementById('filtroCategoria').value;

    const subastasFiltradas = subastasGlobales.filter(subasta => {
        const coincideEstado = subasta.estado === estadoActual;
        const coincideCategoria = (categoriaSeleccionada === 'TODAS') || (subasta.producto?.categoria?.nombre === categoriaSeleccionada);
        return coincideEstado && coincideCategoria;
    });

    renderizarSubastas(subastasFiltradas);
}

function renderizarSubastas(listaSubastas) {
    const container = document.getElementById('subastas-container');
    container.innerHTML = '';

    if (listaSubastas.length === 0) {
        container.innerHTML = `<div class="col-12"><div class="alert alert-info">No hay subastas para mostrar con estos filtros.</div></div>`;
        return;
    }

    listaSubastas.forEach(subasta => {
        let colorBadge = 'bg-success';
        if (subasta.estado === 'PUBLICADA') colorBadge = 'bg-primary';
        if (subasta.estado === 'FINALIZADA') colorBadge = 'bg-secondary';
        if (subasta.estado === 'CANCELADA') colorBadge = 'bg-danger';

        let badgeFechaColor = '';
        let textoFechaBadge = '';

        if (subasta.estado === 'ACTIVA') {
            const fechaCierre = new Date(subasta.fechaCierre).toLocaleDateString('es-AR', { month: 'short', day: 'numeric' }).toUpperCase();
            badgeFechaColor = 'bg-success';
            textoFechaBadge = `CIERRA ${fechaCierre}`;
        } else if (subasta.estado === 'PUBLICADA') {
            const fechaInicio = new Date(subasta.fechaInicio).toLocaleDateString('es-AR', { month: 'short', day: 'numeric' }).toUpperCase();
            badgeFechaColor = 'bg-primary';
            textoFechaBadge = `INICIA ${fechaInicio}`;
        } else if (subasta.estado === 'FINALIZADA') {
            const fechaCierre = new Date(subasta.fechaCierre).toLocaleDateString('es-AR', { month: 'short', day: 'numeric' }).toUpperCase();
            badgeFechaColor = 'bg-secondary';
            textoFechaBadge = `CERRÓ ${fechaCierre}`;
        } else {
            badgeFechaColor = 'bg-danger';
            textoFechaBadge = `CANCELADA`;
        }

        const montoFormateado = new Intl.NumberFormat('es-AR', { style: 'currency', currency: 'ARS', minimumFractionDigits: 0 }).format(subasta.montoActual);

        const cardHTML = `
            <div class="col-md-4 mb-4">
                <div class="card h-100 shadow-sm border-0 rounded-4 overflow-hidden hover-shadow" style="cursor: pointer; transition: transform 0.2s;" onclick="verDetalle(${subasta.id})" onmouseover="this.style.transform='scale(1.02)'" onmouseout="this.style.transform='scale(1)'">
                    <div class="position-relative">
                        <img src="${subasta.producto?.imagenUrl || 'https://via.placeholder.com/400x300'}" class="card-img-top" alt="Imagen del producto" style="height: 220px; object-fit: cover;">
                        <span class="badge ${colorBadge} position-absolute top-0 end-0 m-3 px-3 py-2 fs-6 rounded-pill shadow-sm">
                            ${subasta.estado}
                        </span>
                        <span class="badge ${badgeFechaColor} position-absolute bottom-0 end-0 m-3 px-3 py-1 fs-6 shadow-sm" style="border-radius: 6px;">
                            <i class="bi bi-clock me-1"></i> ${textoFechaBadge}
                        </span>
                    </div>
                    <div class="card-body p-4 text-start d-flex flex-column justify-content-center">
                        <h5 class="card-title fw-bold mb-4 text-dark text-truncate w-100" title="${subasta.producto?.nombre || 'Sin nombre'}">${subasta.producto?.nombre || 'Sin nombre'}</h5>
                        <div class="d-flex justify-content-between align-items-end mt-2">
                            <span class="text-muted small">Monto Actual</span>
                            <span class="fs-4 fw-bold text-primary lh-1">${montoFormateado}</span>
                        </div>
                    </div>
                </div>
            </div>
        `;
        container.innerHTML += cardHTML;
    });
}

function verDetalle(id) {
    window.location.href = `pages/subasta.html?id=${id}`;
}
