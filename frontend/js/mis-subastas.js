const MOCKAPI_URL = "https://6a405da01ff1d27becc0c332.mockapi.io/subastas";

document.addEventListener('DOMContentLoaded', () => {
    cargarMisSubastas();
});

async function cargarMisSubastas() {
    const tbody = document.getElementById('tablaMisSubastas');

    try {
        const response = await fetch(MOCKAPI_URL);
        if (!response.ok) throw new Error('Error al obtener los datos');

        let subastas = await response.json();

        subastas = subastas.filter(s => s.vendedorId === 1 || !s.vendedorId); 
        subastas.reverse();

        tbody.innerHTML = ''; // Limpiar loader

        if (subastas.length === 0) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="5" class="text-center py-5 text-muted">
                        No tienes ninguna subasta cargada aún.
                    </td>
                </tr>
            `;
            return;
        }

        subastas.forEach(subasta => {
            const fila = crearFilaSubasta(subasta);
            tbody.appendChild(fila);
        });

    } catch (error) {
        console.error('Error:', error);
        tbody.innerHTML = `
            <tr>
                <td colspan="5" class="text-center py-5 text-danger">
                    Error al cargar las subastas. Por favor, intente nuevamente.
                </td>
            </tr>
        `;
    }
}

function crearFilaSubasta(subasta) {
    const tr = document.createElement('tr');

    // Parsear fechas
    const fechaInicioStr = subasta.fechaInicio ? new Date(subasta.fechaInicio).toLocaleString() : 'N/A';
    const fechaCierreStr = subasta.fechaCierre ? new Date(subasta.fechaCierre).toLocaleString() : 'N/A';

    // Determinar Badge de Estado
    let badgeClass = 'bg-secondary';
    if (subasta.estado === 'BORRADOR') badgeClass = 'bg-warning text-dark';
    if (subasta.estado === 'ACTIVA') badgeClass = 'bg-success';
    if (subasta.estado === 'PUBLICADA') badgeClass = 'bg-primary';
    if (subasta.estado === 'FINALIZADA') badgeClass = 'bg-secondary';
    if (subasta.estado === 'CANCELADA') badgeClass = 'bg-danger';

    // Botones de acción
    let botonesHtml = '';

    if (subasta.estado === 'BORRADOR') {
        botonesHtml += `<button onclick="cambiarEstado('${subasta.id}', 'PUBLICADA')" class="btn btn-sm btn-outline-primary me-2" title="Publicar"><i class="bi bi-cloud-arrow-up"></i> Publicar</button>`;
        botonesHtml += `<button onclick="cambiarEstado('${subasta.id}', 'CANCELADA')" class="btn btn-sm btn-outline-danger" title="Cancelar"><i class="bi bi-x-circle"></i></button>`;
    } else if (subasta.estado === 'PUBLICADA') {
        botonesHtml += `<button onclick="cambiarEstado('${subasta.id}', 'CANCELADA')" class="btn btn-sm btn-outline-danger" title="Cancelar"><i class="bi bi-x-circle"></i> Cancelar</button>`;
    } else if (subasta.estado === 'ACTIVA' || subasta.estado === 'FINALIZADA') {
        botonesHtml += `<a href="subasta.html?id=${subasta.id}" class="btn btn-sm btn-outline-info" title="Ver Subasta"><i class="bi bi-eye"></i> Ver</a>`;
    }


    tr.innerHTML = `
        <td class="ps-4">
            <div class="d-flex align-items-center">
                <img src="${subasta.producto?.imagenUrl || 'https://via.placeholder.com/50'}" alt="${subasta.producto?.nombre || 'Sin nombre'}" class="rounded me-3" style="width: 50px; height: 50px; object-fit: cover;">
                <div>
                    <h6 class="mb-0 fw-bold">${subasta.producto?.nombre || 'Sin nombre'}</h6>
                    <small class="text-muted">${subasta.producto?.categoria?.nombre || 'Sin categoría'}</small>
                </div>
            </div>
        </td>
        <td>
            <small class="d-block"><strong>Inicio:</strong> ${fechaInicioStr}</small>
            <small class="d-block"><strong>Cierre:</strong> ${fechaCierreStr}</small>
        </td>
        <td>
            <div class="fw-bold text-success">$${parseFloat(subasta.precioBase).toLocaleString('es-AR')}</div>
            <small class="text-muted text-decoration-line-through">$${parseFloat(subasta.montoActual || subasta.precioBase).toLocaleString('es-AR')}</small>
        </td>
        <td>
            <span class="badge ${badgeClass} rounded-pill px-3">${subasta.estado}</span>
        </td>
        <td class="text-end pe-4">
            ${botonesHtml}
        </td>
    `;
    return tr;
}

async function cambiarEstado(id, nuevoEstado) {
    const textoAccion = nuevoEstado === 'PUBLICADA' ? 'publicar' : 'cancelar';

    const result = await Swal.fire({
        title: `¿Estás seguro?`,
        text: `Vas a ${textoAccion} esta subasta.`,
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: nuevoEstado === 'PUBLICADA' ? '#0d6efd' : '#dc3545',
        cancelButtonColor: '#6c757d',
        confirmButtonText: `Sí, ${textoAccion}`,
        cancelButtonText: 'No'
    });

    if (result.isConfirmed) {
        try {
            Swal.fire({
                title: 'Procesando...',
                allowOutsideClick: false,
                didOpen: () => Swal.showLoading()
            });

            const response = await fetch(`${MOCKAPI_URL}/${id}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ estado: nuevoEstado })
            });

            if (!response.ok) throw new Error('Error en la petición');

            await Swal.fire(
                '¡Listo!',
                `La subasta ha sido ${nuevoEstado === 'PUBLICADA' ? 'publicada' : 'cancelada'}.`,
                'success'
            );

            cargarMisSubastas(); // Recargar la tabla

        } catch (error) {
            console.error('Error:', error);
            Swal.fire('Error', 'Hubo un problema al procesar la solicitud.', 'error');
        }
    }
}
