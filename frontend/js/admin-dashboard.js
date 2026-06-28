const MOCKAPI_URL = "https://6a405da01ff1d27becc0c332.mockapi.io/subastas";

// Datos predefinidos (a reemplazar por endpoints reales)
let usuariosSimulados = [
    { id: 1, email: "vendedor1@test.com", rol: "USUARIO, VENDEDOR", activo: true },
    { id: 2, email: "comprador1@test.com", rol: "USUARIO, VENDEDOR", activo: true },
    { id: 3, email: "tramposo@test.com", rol: "USUARIO, VENDEDOR", activo: false }
];

let disputasSimuladas = [
    {
        id: 1,
        subastaId: 45,
        subastaNombre: "Ford F-100 XLT 1994",
        usuarioInicio: "comprador1@test.com",
        motivo: "El vendedor no responde los mensajes",
        descripcion: "Gané la subasta hace 3 días y el vendedor no me ha contactado para coordinar el pago.",
        fechaCreacion: "2026-06-25T14:30:00Z",
        resolucionAdministrativa: null // null = Pendiente
    },
    {
        id: 2,
        subastaId: 12,
        subastaNombre: "Smart TV LG 50 Pulgadas 4K",
        usuarioInicio: "vendedor1@test.com",
        motivo: "El comprador no quiso pagar",
        descripcion: "El comprador se arrepintió después de ganar y me dijo que no tiene el dinero.",
        fechaCreacion: "2026-06-26T09:15:00Z",
        resolucionAdministrativa: "Se inhabilitó al comprador. La subasta se cancela sin cobro de comisión." // Resuelta
    }
];

document.addEventListener('DOMContentLoaded', () => {
    cargarSubastasAdmin();
    renderizarUsuarios();
    renderizarDisputas();
});

// ==========================================
// 1. GESTIÓN DE SUBASTAS
// ==========================================
let subastasGlobalesAdmin = [];

async function cargarSubastasAdmin() {
    const tbody = document.getElementById('tablaAdminSubastas');
    try {
        const response = await fetch(MOCKAPI_URL);
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

            // En la DB real, vamos a giardar el motivo en la base de datos
            await fetch(`${MOCKAPI_URL}/${id}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ estado: 'CANCELADA' })
            });

            Swal.fire('Cancelada', 'La subasta fue cancelada exitosamente.', 'success');
            cargarSubastasAdmin();
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

// ==========================================
// 2. GESTIÓN DE USUARIOS
// ==========================================
function renderizarUsuarios() {
    const tbody = document.getElementById('tablaAdminUsuarios');
    tbody.innerHTML = '';

    usuariosSimulados.forEach(user => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>${user.id}</td>
            <td class="fw-bold">${user.email}</td>
            <td><span class="badge bg-dark">${user.rol}</span></td>
            <td>${user.activo ? '<span class="badge bg-success">Activo</span>' : '<span class="badge bg-danger">Inhabilitado</span>'}</td>
            <td class="text-end">
                <button class="btn btn-sm ${user.activo ? 'btn-outline-danger' : 'btn-outline-success'}" 
                        onclick="toggleEstadoUsuario(${user.id})">
                    ${user.activo ? '<i class="bi bi-person-x"></i> Inhabilitar' : '<i class="bi bi-person-check"></i> Habilitar'}
                </button>
            </td>
        `;
        tbody.appendChild(tr);
    });
}

function toggleEstadoUsuario(id) {
    const user = usuariosSimulados.find(u => u.id === id);
    if (user) {
        user.activo = !user.activo;
        renderizarUsuarios();
        Swal.fire({
            toast: true,
            position: 'top-end',
            icon: 'success',
            title: `Usuario ${user.activo ? 'habilitado' : 'inhabilitado'}`,
            showConfirmButton: false,
            timer: 1500
        });
    }
}

// ==========================================
// 3. GESTIÓN DE DISPUTAS
// ==========================================
function renderizarDisputas() {
    const tbody = document.getElementById('tablaAdminDisputas');
    tbody.innerHTML = '';

    let pendientes = 0;

    disputasSimuladas.forEach(disputa => {
        if (!disputa.resolucionAdministrativa) pendientes++;

        const tr = document.createElement('tr');
        const isResuelta = disputa.resolucionAdministrativa !== null;

        tr.innerHTML = `
            <td>${new Date(disputa.fechaCreacion).toLocaleDateString()}</td>
            <td><a href="subasta.html?id=${disputa.subastaId}" target="_blank">ID: ${disputa.subastaId}</a></td>
            <td>${disputa.usuarioInicio}</td>
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
    const disputa = disputasSimuladas.find(d => d.id === id);
    if (disputa) {
        document.getElementById('descDisputaModal').textContent = disputa.descripcion;
        document.getElementById('textoResolucion').value = '';
        document.getElementById('textoResolucion').disabled = false;
        document.getElementById('disputaIdModal').value = id;

        // Mostrar botón de guardar
        document.querySelector('#modalResolucion .btn-danger').style.display = 'inline-block';

        modalResolucionBootstrap = new bootstrap.Modal(document.getElementById('modalResolucion'));
        modalResolucionBootstrap.show();
    }
}

function guardarResolucion() {
    const id = parseInt(document.getElementById('disputaIdModal').value);
    const texto = document.getElementById('textoResolucion').value;

    if (texto.trim() === '') {
        Swal.fire('Error', 'Debe escribir una resolución administrativa.', 'error');
        return;
    }

    const disputa = disputasSimuladas.find(d => d.id === id);
    if (disputa) {
        disputa.resolucionAdministrativa = texto;
        renderizarDisputas();
        modalResolucionBootstrap.hide();
        Swal.fire('Resuelta', 'La disputa ha sido cerrada.', 'success');
    }
}

function verResolucion(id) {
    const disputa = disputasSimuladas.find(d => d.id === id);
    if (disputa) {
        document.getElementById('descDisputaModal').textContent = disputa.descripcion;
        document.getElementById('textoResolucion').value = disputa.resolucionAdministrativa;
        document.getElementById('textoResolucion').disabled = true;

        // Ocultar botón de guardar
        document.querySelector('#modalResolucion .btn-danger').style.display = 'none';

        modalResolucionBootstrap = new bootstrap.Modal(document.getElementById('modalResolucion'));
        modalResolucionBootstrap.show();
    }
}
