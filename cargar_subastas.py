import requests

BASE_URL = "http://localhost:8080"
FALLBACK_INICIO = "2026-07-04T10:00:00Z"  # >= 48h desde ahora (regla de negocio)
FALLBACK_CIERRE = "2026-09-15T10:00:00Z"  # fecha futura para reemplazar fechas pasadas

def login(email, password):
    r = requests.post(f"{BASE_URL}/auth/login", json={"usernameEmail": email, "password": password})
    return r.json()["accessToken"]

def crear_subasta(token, producto_id, precio_base, incremento_fijo, fecha_inicio, fecha_cierre, descripcion):
    headers = {"Authorization": f"Bearer {token}"}
    body = {
        "productoId": producto_id,
        "precioBase": precio_base,
        "incrementoFijo": incremento_fijo,
        "fechaInicio": fecha_inicio,
        "fechaCierre": fecha_cierre,
        "descripcion": descripcion
    }
    r = requests.post(f"{BASE_URL}/api/subastas", json=body, headers=headers)
    if r.status_code == 201:
        return r.json()["id"]
    else:
        print(f"  ✗ Error creando subasta para producto {producto_id}: {r.status_code}: {r.text}")
        return None

def publicar(token, subasta_id):
    r = requests.patch(f"{BASE_URL}/api/subastas/{subasta_id}/publicar",
                       headers={"Authorization": f"Bearer {token}"})
    if r.status_code != 200:
        print(f"  ✗ Error publicando subasta {subasta_id}: {r.status_code}")

def cancelar(token, subasta_id):
    r = requests.patch(f"{BASE_URL}/api/subastas/{subasta_id}/cancelar",
                       headers={"Authorization": f"Bearer {token}"})
    if r.status_code != 200:
        print(f"  ✗ Error cancelando subasta {subasta_id}: {r.status_code}")

# ─── LOGIN ────────────────────────────────────────────────────────────────────
print("Iniciando sesión...")
token1 = login("rematador_oficial@subastas.com", "password123")   # productos 1-30
token2 = login("subastas_centro@subastas.com", "password123")     # productos 31-65
print("Tokens obtenidos.\n")

# ─── DATOS ───────────────────────────────────────────────────────────────────
# Formato: (producto_id_nuestro, precioBase, incrementoFijo, fechaInicio, fechaCierre, descripcion, cancelar)
# Si producto_id <= 30 → token1, si >= 31 → token2
# Fechas de cierre pasadas reemplazadas por FALLBACK_CIERRE

subastas = [
    # producto_id, precioBase, incremento, fechaInicio, fechaCierre, descripcion, cancelar
    # FALLBACK_INICIO = 2026-07-04 (>= 48h futuro, requerido por publicar)
    (1,  2800000.0,   50000.0,  FALLBACK_INICIO,        "2026-08-04T10:00:00Z", "Subasta de Bajaj Rouser NS 200 2023",                    False),
    (31, 95000.0,     5000.0,   FALLBACK_INICIO,        FALLBACK_CIERRE,        "Subasta de Horno Eléctrico Atma 40L",                    False),
    (2,  60000000.0,  500000.0, FALLBACK_INICIO,        "2026-08-04T14:00:00Z", "Subasta de Terreno Industrial 1500m2 sobre Ruta",        False),
    (32, 11500000.0,  100000.0, FALLBACK_INICIO,        FALLBACK_CIERRE,        "Subasta de Terreno con Nivelación y Cerco Perimetral",   True),
    (33, 350000.0,    15000.0,  "2026-07-05T09:00:00Z", "2026-08-05T09:00:00Z", "Subasta de Juego de Comedor Madera 6 Sillas",            False),
    (34, 34000000.0,  300000.0, FALLBACK_INICIO,        FALLBACK_CIERRE,        "Subasta de Nissan Frontier PRO-4X 2022",                 False),
    (3,  80000000.0,  500000.0, FALLBACK_INICIO,        "2026-08-04T09:00:00Z", "Subasta de Local Comercial Esquina Doble Altura",        False),
    (4,  180000.0,    5000.0,   FALLBACK_INICIO,        "2026-08-04T10:00:00Z", "Subasta de Batidora Planetaria Peabody",                 False),
    (5,  75000.0,     5000.0,   "2026-08-01T10:00:00Z", "2026-09-01T10:00:00Z", "Subasta de Mesa Ratona de Vidrio y Metal",               False),
    (6,  32000000.0,  300000.0, FALLBACK_INICIO,        "2026-08-04T10:00:00Z", "Subasta de Casa Duplex 2 Dormitorios con Cochera",       False),
    (35, 26000000.0,  250000.0, FALLBACK_INICIO,        FALLBACK_CIERRE,        "Subasta de Casa Sencilla con Amplio Terreno Libre",      False),
    (7,  21000000.0,  150000.0, FALLBACK_INICIO,        "2026-08-04T08:00:00Z", "Subasta de Departamento Tipo PH de 2 Ambientes",        False),
    (8,  6200000.0,   100000.0, "2026-08-01T10:00:00Z", "2026-09-01T10:00:00Z", "Subasta de Yamaha MT-03 2021",                           False),
    (9,  22000000.0,  200000.0, FALLBACK_INICIO,        "2026-08-04T00:00:00Z", "Subasta de Chevrolet Tracker Premier 2022",              False),
    (10, 24000000.0,  300000.0, FALLBACK_INICIO,        FALLBACK_CIERRE,        "Subasta de Oficina o Local Comercial Planta Alta",       False),
    (11, 23000000.0,  150000.0, FALLBACK_INICIO,        FALLBACK_CIERRE,        "Subasta de Monoambiente Amoblado Ideal Estudiantes",     False),
    (36, 85000.0,     5000.0,   FALLBACK_INICIO,        "2026-08-04T10:00:00Z", "Subasta de Microondas BGH 20 Litros",                    False),
    (37, 7800000.0,   150000.0, FALLBACK_INICIO,        FALLBACK_CIERRE,        "Subasta de Royal Enfield Interceptor 650 2023",          False),
    (12, 12000000.0,  100000.0, FALLBACK_INICIO,        "2026-08-04T10:00:00Z", "Subasta de Ford F-100 XLT 1994",                        False),
    (38, 9500000.0,   100000.0, FALLBACK_INICIO,        FALLBACK_CIERRE,        "Subasta de Lote de Terreno en Zona de Crecimiento",      False),
    (39, 25000000.0,  250000.0, "2026-07-25T08:00:00Z", "2026-08-25T08:00:00Z", "Subasta de Ford Transit 350L 2019",                      False),
    (40, 14000000.0,  100000.0, FALLBACK_INICIO,        "2026-08-04T08:00:00Z", "Subasta de Lote Terreno Perímetro Semicerrado",          False),
    (41, 85000000.0,  1000000.0,"2026-07-10T10:00:00Z", "2026-08-10T10:00:00Z", "Subasta de Casa de 4 Dormitorios, Quincho y Piscina",    False),
    (42, 4500000.0,   50000.0,  FALLBACK_INICIO,        "2026-08-04T10:00:00Z", "Subasta de Honda XR 250 Tornado 2022",                   False),
    (43, 480000.0,    10000.0,  FALLBACK_INICIO,        "2026-08-04T12:00:00Z", "Subasta de Smart TV LG 50 Pulgadas 4K",                  False),
    (44, 85000.0,     5000.0,   FALLBACK_INICIO,        FALLBACK_CIERRE,        "Subasta de Licuadora con Jarra de Vidrio Philips",       False),
    (45, 14000000.0,  100000.0, FALLBACK_INICIO,        "2026-08-04T12:00:00Z", "Subasta de Renault Sandero 2020",                        False),
    (13, 90000000.0,  1000000.0,FALLBACK_INICIO,        "2026-08-04T12:00:00Z", "Subasta de Predio Comercial con Oficinas y Galpón",      False),
    (14, 120000.0,    10000.0,  FALLBACK_INICIO,        FALLBACK_CIERRE,        "Subasta de Escritorio para PC en L",                     True),
    (15, 55000000.0,  500000.0, FALLBACK_INICIO,        "2026-08-04T12:00:00Z", "Subasta de Lote en Barrio Cerrado con Seguridad",        False),
    (16, 380000.0,    15000.0,  "2026-07-15T09:00:00Z", "2026-08-15T09:00:00Z", "Subasta de Aspiradora Robot Xiaomi",                     False),
    (46, 48000000.0,  400000.0, "2026-07-10T10:00:00Z", "2026-08-10T10:00:00Z", "Subasta de Casa Céntrica con Local Comercial Al Frente", False),
    (47, 320000.0,    15000.0,  "2026-07-10T08:00:00Z", "2026-08-10T08:00:00Z", "Subasta de Conjunto Sommier y Colchón 2 Plazas",         False),
    (48, 52000000.0,  500000.0, FALLBACK_INICIO,        FALLBACK_CIERRE,        "Subasta de Departamento de 2 Dormitorios con Amenities", False),
    (17, 45000000.0,  500000.0, FALLBACK_INICIO,        "2026-08-04T10:00:00Z", "Subasta de Casa Familiar de 3 Dormitorios con Patio",    False),
    (49, 110000.0,    5000.0,   "2026-07-10T08:00:00Z", "2026-08-10T08:00:00Z", "Subasta de Rack para TV de Melamina",                    False),
    (18, 42000000.0,  500000.0, "2026-07-06T10:00:00Z", "2026-08-06T10:00:00Z", "Subasta de Local para Gastronomía con Salida de Humos",  False),
    (19, 650000.0,    10000.0,  FALLBACK_INICIO,        "2026-08-04T10:00:00Z", "Subasta de Heladera con Freezer Samsung 328L",           False),
    (20, 46000000.0,  400000.0, FALLBACK_INICIO,        "2026-08-04T08:00:00Z", "Subasta de Departamento Duplex con Terraza Propia",      False),
    (50, 550000.0,    20000.0,  FALLBACK_INICIO,        "2026-08-04T10:00:00Z", "Subasta de Aire Acondicionado Split 3000F",              False),
    (21, 40000000.0,  400000.0, FALLBACK_INICIO,        FALLBACK_CIERRE,        "Subasta de Depósito o Local Comercial Amplio",           True),
    (51, 16000000.0,  100000.0, FALLBACK_INICIO,        "2026-08-04T10:00:00Z", "Subasta de Fiat Cronos Precision 1.8 2021",              False),
    (52, 280000.0,    20000.0,  FALLBACK_INICIO,        FALLBACK_CIERRE,        "Subasta de Sillón 3 Cuerpos Chenille",                   True),
    (22, 32000000.0,  500000.0, "2026-07-05T10:00:00Z", "2026-08-05T10:00:00Z", "Subasta de Toyota Corolla Cross XRX 2023",               True),
    (23, 35000000.0,  500000.0, FALLBACK_INICIO,        "2026-08-04T09:00:00Z", "Subasta de Volkswagen Amarok V6 Extreme 2022",           False),
    (53, 140000.0,    5000.0,   FALLBACK_INICIO,        FALLBACK_CIERRE,        "Subasta de Cómoda Cajonera 6 Cajones",                   False),
    (54, 75000000.0,  1000000.0,FALLBACK_INICIO,        "2026-08-04T10:00:00Z", "Subasta de Casa Minimalista de 2 Plantas en Estreno",    False),
    (24, 190000.0,    10000.0,  FALLBACK_INICIO,        FALLBACK_CIERRE,        "Subasta de Placard 4 Puertas con Cajones",               False),
    (55, 28000000.0,  300000.0, "2026-07-05T09:00:00Z", "2026-08-05T09:00:00Z", "Subasta de Departamento Moderno de 1 Dormitorio Centro", False),
    (25, 12000000.0,  100000.0, FALLBACK_INICIO,        FALLBACK_CIERRE,        "Subasta de Terreno Residencial 360m2 Barrio Abierto",    False),
    (56, 16000000.0,  200000.0, FALLBACK_INICIO,        FALLBACK_CIERRE,        "Subasta de Lote Esquina en Barrio Residencial",          False),
    (57, 19000000.0,  250000.0, FALLBACK_INICIO,        FALLBACK_CIERRE,        "Subasta de Honda HR-V EX-L 2018",                        False),
    (58, 22000000.0,  200000.0, "2026-07-10T08:00:00Z", "2026-08-10T08:00:00Z", "Subasta de Casa de Estilo Colonial a Restaurar",         False),
    (26, 18000000.0,  200000.0, "2026-07-28T10:00:00Z", "2026-08-28T10:00:00Z", "Subasta de Peugeot 208 Feline 1.6 2023",                 False),
    (59, 150000.0,    5000.0,   FALLBACK_INICIO,        "2026-08-04T10:00:00Z", "Subasta de Silla de Oficina Ergonómica",                 False),
    (60, 8500000.0,   100000.0, FALLBACK_INICIO,        FALLBACK_CIERRE,        "Subasta de Kawasaki Ninja 400 2020",                     True),
    (61, 19000000.0,  200000.0, FALLBACK_INICIO,        FALLBACK_CIERRE,        "Subasta de Monoambiente Luminoso con Balcón Al Frente",  False),
    (27, 160000.0,    5000.0,   FALLBACK_INICIO,        FALLBACK_CIERRE,        "Subasta de Estufa a Gas Tiro Balanceado",                False),
    (62, 15000000.0,  150000.0, "2026-07-10T09:00:00Z", "2026-08-10T09:00:00Z", "Subasta de Local Comercial en Galería Planta Baja",      False),
    (28, 65000.0,     5000.0,   "2026-07-20T10:00:00Z", "2026-08-20T10:00:00Z", "Subasta de Biblioteca de Madera Maciza 5 Estantes",      False),
    (63, 110000000.0, 2000000.0,FALLBACK_INICIO,        "2026-08-04T10:00:00Z", "Subasta de Terreno Apto Desarrollos Edificios",          False),
    (64, 38000000.0,  400000.0, "2026-07-20T10:00:00Z", "2026-08-20T10:00:00Z", "Subasta de Casa de Campo con Arboleda Centenaria",       False),
    (29, 420000.0,    10000.0,  FALLBACK_INICIO,        FALLBACK_CIERRE,        "Subasta de Lavarropas Automático Drean 8kg",             False),
    (65, 35000000.0,  400000.0, FALLBACK_INICIO,        FALLBACK_CIERRE,        "Subasta de Local Comercial con Vidriera en Zona Céntrica",True),
    (30, 95000000.0,  1000000.0,"2026-07-15T11:00:00Z", "2026-08-15T11:00:00Z", "Subasta de Piso Exclusivo 3 Dormitorios Frente al Parque",False),
]

# ─── CARGA ────────────────────────────────────────────────────────────────────
print(f"Cargando {len(subastas)} subastas...\n")

for i, (prod_id, precio, incremento, f_inicio, f_cierre, desc, debe_cancelar) in enumerate(subastas, 1):
    token = token1 if prod_id <= 30 else token2

    subasta_id = crear_subasta(token, prod_id, precio, incremento, f_inicio, f_cierre, desc)
    if subasta_id is None:
        continue

    publicar(token, subasta_id)

    if debe_cancelar:
        cancelar(token, subasta_id)
        estado = "CANCELADA"
    else:
        estado = "PUBLICADA/ACTIVA (scheduler)"

    print(f"  ✓ [{subasta_id}] {desc[:50]} → {estado}")

print("\nListo.")
