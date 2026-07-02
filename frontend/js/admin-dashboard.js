// SEGURIDAD FRONTEND
const user = usuarioLogueado();
const esAdmin = user && user.roles && user.roles.includes('ROLE_ADMIN');
if (!esAdmin) {
    Swal.fire('Acceso Denegado', 'Solo los administradores pueden ver esta página.', 'error').then(() => {
        window.location.href = '../index.html';
    });
}

document.addEventListener('DOMContentLoaded', () => {
    cargarSubastasAdmin();
    cargarUsuariosAdmin();
    cargarDisputasAdmin();
});


// GESTIÓN DE SUBASTAS

let subastasGlobalesAdmin = [];

async function cargarSubastasAdmin() {
    const tbody = document.getElementById('tablaAdminSubastas');
    try {
        const response = await fetch(API_BASE_URL + '/api/subastas', {
            headers: getAuthHeaders()
        });
        if (!response.ok) throw new Error('Error al obtener subastas');

        subastasGlobalesAdmin = await response.json();
        subastasGlobalesAdmin.reverse(); // Más nuevas primero

        filtrarSubastasAdmin();

    } catch (error) {
        tbody.innerHTML = `<tr><td colspan="5" class="text-center text-danger">Error cargando datos.</td></tr>`;
    }
}

function filtrarSubastasAdmin() {
    const estadoFiltro = document.getElementById('adminFiltroEstado').value;
    if (estadoFiltro === 'TODAS') {
        renderizarSubastasAdmin(subastasGlobalesAdmin);
    } else {
        const filtradas = subastasGlobalesAdmin.filter(s => s.estado === estadoFiltro);
        renderizarSubastasAdmin(filtradas);
    }
}

function renderizarSubastasAdmin(subastas) {
    const tbody = document.getElementById('tablaAdminSubastas');
    tbody.innerHTML = '';
    if (subastas.length === 0) {
        tbody.innerHTML = `<tr><td colspan="5" class="text-center text-muted">No hay subastas para mostrar.</td></tr>`;
        return;
    }

    subastas.forEach(subasta => {
        const tr = document.createElement('tr');

        let badgeClass = 'bg-secondary';
        if (subasta.estado === 'ACTIVA') badgeClass = 'bg-success';
        if (subasta.estado === 'PUBLICADA') badgeClass = 'bg-primary';
        if (subasta.estado === 'CANCELADA') badgeClass = 'bg-danger';

        tr.innerHTML = `
                <td>
                    <div class="fw-bold">${subasta.producto?.nombre || 'Sin nombre'}</div>
                    <small class="text-muted">ID: ${subasta.id} | ${subasta.producto?.categoria?.nombre || ''}</small>
                </td>
                <td>${subasta.vendedor?.usernameEmail || 'N/A'}</td>
                <td>
                    <small class="d-block">Inicia: ${new Date(subasta.fechaInicio).toLocaleDateString()}</small>
                    <small class="d-block">Cierra: ${new Date(subasta.fechaCierre).toLocaleDateString()}</small>
                </td>
                <td><span class="badge ${badgeClass}">${subasta.estado}</span></td>
                <td class="text-end">
                    <button class="btn btn-sm btn-outline-info me-1" title="Ver Historial" onclick="verHistorial('${subasta.id}')">
                        <i class="bi bi-clock-history"></i>
                    </button>
                    ${subasta.estado !== 'CANCELADA' ? `
                    <button class="btn btn-sm btn-outline-danger" title="Cancelar Subasta" onclick="cancelarSubastaAdmin('${subasta.id}')">
                        <i class="bi bi-x-circle"></i>
                    </button>` : ''}
                </td>
            `;
        tbody.appendChild(tr);
    });
}

async function cancelarSubastaAdmin(id) {
    const { isConfirmed, value: motivo } = await Swal.fire({
        title: '¿Cancelar Subasta?',
        text: "Como administrador, podés cancelar una subasta por inclumplimiento de normas.",
        icon: 'warning',
        input: 'text',
        inputPlaceholder: 'Ingrese motivo de la cancelación',
        showCancelButton: true,
        confirmButtonColor: '#dc3545',
        confirmButtonText: 'Sí, cancelar'
    });

    if (isConfirmed) {
        try {
            Swal.fire({ title: 'Cancelando...', didOpen: () => Swal.showLoading() });

            // Petición real al backend usando PATCH
            const response = await fetch(`${API_BASE_URL}/api/subastas/${id}/cancelar`, {
                method: 'PATCH',
                headers: getAuthHeaders()
            });

            if (!response.ok) throw new Error("No se pudo cancelar");

            Swal.fire('Cancelada', 'La subasta fue cancelada exitosamente.', 'success');
            cargarSubastasAdmin(); // Recarga la tabla
        } catch (error) {
            Swal.fire('Error', 'No se pudo cancelar la subasta.', 'error');
        }
    }
}


function verHistorial(id) {
    const lista = document.getElementById('listaHistorial');
    lista.innerHTML = `
        <li class="list-group-item d-flex justify-content-between align-items-center">
            <div><div class="fw-bold">BORRADOR</div><small class="text-muted">Creada por el vendedor</small></div>
            <span class="badge bg-light text-dark border">Hace 5 días</span>
        </li>
        <li class="list-group-item d-flex justify-content-between align-items-center">
            <div><div class="fw-bold">PUBLICADA</div><small class="text-muted">Aprobada automáticamente</small></div>
            <span class="badge bg-light text-dark border">Hace 4 días</span>
        </li>
        <li class="list-group-item d-flex justify-content-between align-items-center bg-light">
            <div><div class="fw-bold text-success">ACTIVA</div><small class="text-muted">Inicio de pujas</small></div>
            <span class="badge bg-light text-dark border">Actualmente</span>
        </li>
    `;
    const modal = new bootstrap.Modal(document.getElementById('modalHistorial'));
    modal.show();
}


// GESTIÓN DE USUARIOS

let usuariosGlobalesAdmin = [];

async function cargarUsuariosAdmin() {
    const tbody = document.getElementById('tablaAdminUsuarios');
    try {
        const response = await fetch(API_BASE_URL + '/api/usuarios', {
            headers: getAuthHeaders() // El backend exige ser ADMIN
        });
        if (!response.ok) throw new Error('Error al obtener usuarios');

        usuariosGlobalesAdmin = await response.json();
        renderizarUsuarios();
    } catch (error) {
        tbody.innerHTML = `<tr><td colspan="5" class="text-center text-danger">Error cargando usuarios. Asegurate de ser ADMIN.</td></tr>`;
    }
}

function renderizarUsuarios() {
    const tbody = document.getElementById('tablaAdminUsuarios');
    tbody.innerHTML = '';

    if (usuariosGlobalesAdmin.length === 0) {
        tbody.innerHTML = `<tr><td colspan="5" class="text-center text-muted">No hay usuarios para mostrar.</td></tr>`;
        return;
    }

    usuariosGlobalesAdmin.forEach(user => {
        const tr = document.createElement('tr');
        const activo = !user.bloqueado;
        const roles = user.roles ? user.roles.join(', ') : 'N/A';

        tr.innerHTML = `
            <td>${user.id}</td>
            <td class="fw-bold">${user.usernameEmail}</td>
            <td><span class="badge bg-dark">${roles}</span></td>
            <td>${activo ? '<span class="badge bg-success">Activo</span>' : '<span class="badge bg-danger">Inhabilitado</span>'}</td>
            <td class="text-end">
                <button class="btn btn-sm ${activo ? 'btn-outline-danger' : 'btn-outline-success'}" 
                        onclick="toggleEstadoUsuario(${user.id})">
                    ${activo ? '<i class="bi bi-person-x"></i> Inhabilitar' : '<i class="bi bi-person-check"></i> Habilitar'}
                </button>
            </td>
        `;
        tbody.appendChild(tr);
    });
}

async function toggleEstadoUsuario(id) {
    try {
        const response = await fetch(`${API_BASE_URL}/api/usuarios/${id}/bloquear`, {
            method: 'PATCH',
            headers: getAuthHeaders()
        });
        if (!response.ok) throw new Error('Error al cambiar estado');

        // Recargar la tabla completa desde el backend
        cargarUsuariosAdmin();

        Swal.fire({
            toast: true,
            position: 'top-end',
            icon: 'success',
            title: `Usuario actualizado`,
            showConfirmButton: false,
            timer: 1500
        });
    } catch (error) {
        Swal.fire('Error', 'No se pudo cambiar el estado del usuario.', 'error');
    }
}


// GESTIÓN DE DISPUTAS

let disputasGlobalesAdmin = [];

async function cargarDisputasAdmin() {
    const tbody = document.getElementById('tablaAdminDisputas');
    try {
        const response = await fetch(API_BASE_URL + '/api/disputas', {
            headers: getAuthHeaders()
        });
        if (!response.ok) throw new Error('Error al obtener disputas');

        disputasGlobalesAdmin = await response.json();
        renderizarDisputas();
    } catch (error) {
        tbody.innerHTML = `<tr><td colspan="6" class="text-center text-danger">Error cargando disputas.</td></tr>`;
    }
}

function renderizarDisputas() {
    const tbody = document.getElementById('tablaAdminDisputas');
    tbody.innerHTML = '';
    let pendientes = 0;

    if (disputasGlobalesAdmin.length === 0) {
        tbody.innerHTML = `<tr><td colspan="6" class="text-center text-muted">No hay disputas para mostrar.</td></tr>`;
        document.getElementById('badge-disputas').textContent = 0;
        return;
    }

    disputasGlobalesAdmin.forEach(disputa => {
        if (!disputa.resolucionAdministrativa) pendientes++;

        const tr = document.createElement('tr');
        const isResuelta = disputa.resolucionAdministrativa !== null;
        const fechaFormat = new Date(disputa.fechaCreacion).toLocaleDateString();

        tr.innerHTML = `
            <td>${fechaFormat}</td>
            <td><a href="subasta.html?id=${disputa.subastaId}" target="_blank">ID: ${disputa.subastaId}</a></td>
            <td>${disputa.usuarioInicioNombre || disputa.usuarioInicioEmail || 'N/A'}</td>
            <td>${disputa.motivo}</td>
            <td>${isResuelta ? '<span class="badge bg-success">Resuelta</span>' : '<span class="badge bg-warning text-dark">Pendiente</span>'}</td>
            <td class="text-end">
                ${!isResuelta ? `
                    <button class="btn btn-sm text-white" style="background-color: var(--color-cancelado);" onclick="abrirResolucion(${disputa.id})">Resolver</button>
                ` : `
                    <button class="btn btn-sm btn-outline-secondary" onclick="verResolucion(${disputa.id})">Ver Fallo</button>
                `}
            </td>
        `;
        tbody.appendChild(tr);
    });

    document.getElementById('badge-disputas').textContent = pendientes;
}

let modalResolucionBootstrap = null;

function abrirResolucion(id) {
    const disputa = disputasGlobalesAdmin.find(d => d.id === id);
    if (disputa) {
        document.getElementById('descDisputaModal').textContent = disputa.descripcion;
        document.getElementById('textoResolucion').value = '';
        document.getElementById('textoResolucion').disabled = false;
        document.getElementById('disputaIdModal').value = id;

        document.querySelector('#modalResolucion .btn-danger').style.display = 'inline-block';
        modalResolucionBootstrap = new bootstrap.Modal(document.getElementById('modalResolucion'));
        modalResolucionBootstrap.show();
    }
}

async function guardarResolucion() {
    const id = parseInt(document.getElementById('disputaIdModal').value);
    const texto = document.getElementById('textoResolucion').value;

    if (texto.trim() === '') {
        Swal.fire('Error', 'Debe escribir una resolución administrativa.', 'error');
        return;
    }

    try {
        const response = await fetch(`${API_BASE_URL}/api/disputas/${id}/resolver`, {
            method: 'PATCH',
            headers: getAuthHeaders(),
            body: JSON.stringify({ resolucionAdministrativa: texto })
        });

        if (!response.ok) throw new Error('Error al resolver la disputa');

        modalResolucionBootstrap.hide();
        Swal.fire('Resuelta', 'La disputa ha sido cerrada exitosamente.', 'success');
        cargarDisputasAdmin();
    } catch (error) {
        Swal.fire('Error', 'No se pudo guardar la resolución.', 'error');
    }
}

function verResolucion(id) {
    const disputa = disputasGlobalesAdmin.find(d => d.id === id);
    if (disputa) {
        document.getElementById('descDisputaModal').textContent = disputa.descripcion;
        document.getElementById('textoResolucion').value = disputa.resolucionAdministrativa;
        document.getElementById('textoResolucion').disabled = true;

        document.querySelector('#modalResolucion .btn-danger').style.display = 'none';
        modalResolucionBootstrap = new bootstrap.Modal(document.getElementById('modalResolucion'));
        modalResolucionBootstrap.show();
    }
}
