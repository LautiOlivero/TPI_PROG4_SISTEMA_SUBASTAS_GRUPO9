import requests
import json

BASE_URL = "http://localhost:8080"

def login(email, password):
    r = requests.post(f"{BASE_URL}/auth/login", json={"usernameEmail": email, "password": password})
    return r.json()["accessToken"]

def crear_producto(token, nombre, descripcion, imagen_url, categoria_id):
    headers = {"Authorization": f"Bearer {token}"}
    body = {
        "nombre": nombre,
        "descripcion": descripcion,
        "imagenUrl": imagen_url,
        "categoriaId": categoria_id
    }
    r = requests.post(f"{BASE_URL}/api/productos", json=body, headers=headers)
    if r.status_code == 201:
        print(f"  ✓ [{r.json()['id']}] {nombre}")
    else:
        print(f"  ✗ {nombre} → {r.status_code}: {r.text}")

# ─── LOGIN ───────────────────────────────────────────────────────────────────
print("Iniciando sesión...")
token1 = login("rematador_oficial@subastas.com", "password123")
token2 = login("subastas_centro@subastas.com", "password123")
print("Tokens obtenidos.\n")

# ─── VENDEDOR 1 (rematador_oficial) ──────────────────────────────────────────
# Nota: producto 1 (Bajaj Rouser) ya fue creado manualmente, se omite.
print("=== Vendedor 1 ===")
productos_v1 = [
    ("Terreno Industrial 1500m2 sobre Ruta", "Fracción de tierra apta para radicación de industrias, galpones o depósitos logísticos. Acceso directo asfaltado.", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTbCnUlKddyybiWAMonPxFtIlfVyrBrBAgTxw&s", 1),
    ("Local Comercial Esquina Doble Altura", "Propiedad de gran exposición visual en esquina de avenida principal. Apto gastronomía, bancos o comercios de escala.", "https://thumbs.dreamstime.com/b/fachada-de-locales-comerciales-en-zona-comercial-el-%C3%A1rea-un-d%C3%ADa-soleado-226704557.jpg", 1),
    ("Batidora Planetaria Peabody", "Batidora de pie con bowl de acero inoxidable de 4.5L, incluye gancho amasador y batidor de alambre.", "https://electrooutlet.com.ar/Image/0/750_750-badca30d-678a-45b5-a546-747ba1c0f3a6.jpg", 3),
    ("Mesa Ratona de Vidrio y Metal", "Mesa de centro moderna con estructura metálica cromada y superficie de vidrio templado de 8mm.", "https://valeriocaballeromuebles.com/wp-content/uploads/2026/05/D_NQ_NP_831206-MLA32046283627_092019-V.jpg", 3),
    ("Casa Duplex 2 Dormitorios con Cochera", "Complejo residencial cerrado. Planta baja con estar, cocina y patio chico. Planta alta con 2 dormitorios y baño principal.", "https://thumbs.dreamstime.com/b/nuevo-exterior-americano-del-hogar-33045473.jpg", 1),
    ("Departamento Tipo PH de 2 Ambientes", "PH en planta baja por pasillo, sin expensas. Un dormitorio, comedor diario, cocina pequeña y patio interno de uso exclusivo.", "https://baroneinmobiliaria.com.ar/wp-content/uploads/2023/03/ph-villaadelina-08pg.jpg", 1),
    ("Yamaha MT-03 2021", "Moto naked deportiva, motor bicilíndrico de 321cc, frenos ABS. Excelente estado.", "https://pixnio.com/free-images/2021/04/14/2021-04-14-01-34-55-1200x1800.jpg", 2),
    ("Chevrolet Tracker Premier 2022", "SUV compacto con motor turbo, alerta de colisión frontal y frenado autónomo de emergencia.", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQGzZm0ZVjnFpsN38M8xgx_QK4wwPU6frEoyg&s", 2),
    ("Oficina o Local Comercial Planta Alta", "Espacio para oficinas o consultorios, recepción compartida, 3 despachos privados y toilette. Excelente ubicación institucional.", "https://thumbs.dreamstime.com/b/montigny-france-march-commercial-building-offering-office-retail-units-sale-vacant-business-front-view-modern-450622497.jpg", 1),
    ("Monoambiente Amoblado Ideal Estudiantes", "Departamento a cuadras de la zona universitaria. Incluye cocina armada, placard empotrado y artefactos de iluminación.", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQ6pg9eca7Fj8_VH1wHiFJKwZTEN83rute9_Q&s", 1),
    ("Ford F-100 XLT 1994", "Pick-up clásica en excelente estado de conservación, motor V8, ideal para proyectos de restauración o camperización.", "https://images.unsplash.com/photo-1551830820-330a71b99659?w=500&auto=format&fit=crop&q=60", 2),
    ("Predio Comercial con Oficinas y Galpón", "Inmueble comercial compuesto por oficinas de atención al público, baños privados, playa de maniobras y galpón cerrado.", "https://www.jpstella.com.ar/wp-content/uploads/2026/01/Copia-de-Guoli_20_09195.jpg", 1),
    ("Escritorio para PC en L", "Mesa de trabajo esquinera, ideal para home office. Melamina blanca con detalles en madera oscura.", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSjfPrn4xUtklGua95SrONEWVXeTLRWVbLdsg&s", 3),
    ("Lote en Barrio Cerrado con Seguridad", "Terreno de 800m2 ubicado en sector central de barrio privado. Seguridad 24hs, todos los servicios subterráneos y expensas al día.", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTimEAiTTZJVmF5HwaI-UGTUYacoT4uANEp8Q&s", 1),
    ("Aspiradora Robot Xiaomi", "Robot aspirador y trapeador, mapeo láser inteligente, control mediante aplicación móvil.", "https://i.blogs.es/3d42f4/img_0393/1366_2000.jpeg", 3),
    ("Casa Familiar de 3 Dormitorios con Patio", "Propiedad de dos plantas, 3 dormitorios, 2 baños, cocina comedor espaciosa, garage cubierto y patio amplio con asador.", "https://www.shutterstock.com/shutterstock/videos/3818972321/thumb/1.jpg?ip=x480", 1),
    ("Local para Gastronomía con Salida de Humos", "Local comercial preparado para el rubro gastronómico, instalación de gas industrial y campana de extracción reglamentaria.", "https://www.visitfinland.com/dam/jcr:9dbc410e-a562-44e4-b539-aafeb5a93f96/SEAHORSE_ELOKUU2022-5-4_onlyforVF.com.jpg", 1),
    ("Heladera con Freezer Samsung 328L", "Heladera no frost color plata, excelente estado de conservación, estantes de vidrio templado.", "https://d2pr1pn9ywx3vo.cloudfront.net/spree/products/5592/product/sam35x.jpg?1617838428", 3),
    ("Departamento Duplex con Terraza Propia", "Estructura moderna en dos niveles. Dos habitaciones en planta alta, planta baja social con salida a terraza privada con asador.", "https://thumbs.dreamstime.com/b/moderna-terraza-doble-de-lujo-casas-en-construcci%C3%B3n-dise%C3%B1o-arquitect%C3%B3nico-con-estilo-contempor%C3%A1neo-exterior-ladrillo-fachada-385725793.jpg", 1),
    ("Depósito o Local Comercial Amplio", "Inmueble de 200m2 cubiertos con techo parabólico, oficina administrativa al frente y entrada apta para camiones.", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSmq8uj6ZX7AnZKaAzYyVMHq9My2uxbp6adjw&s", 1),
    ("Toyota Corolla Cross XRX 2023", "SUV híbrido, excelente rendimiento de combustible, paquete de seguridad Toyota Safety Sense.", "https://static.foxdealer.com/657/2024/02/COC_MY_per_4.jpg", 2),
    ("Volkswagen Amarok V6 Extreme 2022", "Pick-up doble cabina, tracción 4x4, asientos de cuero y todos los servicios al día en concesionario oficial.", "https://images.unsplash.com/photo-1687952527525-b79297bd7ef2?w=500&auto=format&fit=crop&q=60", 2),
    ("Placard 4 Puertas con Cajones", "Ropero de melamina color roble, 4 puertas batientes, 2 cajones con guías metálicas y estantes internos.", "https://mall.icbc.com.ar/36064680-large_default/placard-4-puertas-2-cajones-pack-mendra-27478.jpg", 3),
    ("Terreno Residencial 360m2 Barrio Abierto", "Lote plano listo para escriturar y construir. Cuenta con servicios de agua corriente, electricidad y alumbrado público.", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRw3C6CnSYWQ1igRK4HVxNhMi_VBEIDwQzFhQ&s", 1),
    ("Peugeot 208 Feline 1.6 2023", "Hatchback tope de gama, techo panorámico, tablero digital 3D y cámara de retroceso. Como nuevo.", "https://images.unsplash.com/photo-1708237495413-1f3a9d82e4c0?w=500&auto=format&fit=crop&q=60", 2),
    ("Estufa a Gas Tiro Balanceado", "Calefactor de 3000 kcal/h, válvula de seguridad, encendido piezoeléctrico. Salida al exterior.", "https://sanitariosypintureriabianco.com/wp-content/uploads/2023/05/pro-1.webp", 3),
    ("Biblioteca de Madera Maciza 5 Estantes", "Estantería de pino macizo, ideal para organizar libros o decoración. Lista para pintar o barnizar.", "https://http2.mlstatic.com/D_NQ_NP_903413-MLA110432761557_042026-O.webp", 3),
    ("Lavarropas Automático Drean 8kg", "Lavarropas de carga frontal, 1200 RPM, display digital y lavado rápido. Funcionando perfectamente.", "https://thumbs.dreamstime.com/b/frente-que-se-lava-22406254.jpg", 3),
    ("Piso Exclusivo 3 Dormitorios Frente al Parque", "Departamento de categoría con palier privado, suite principal con vestidor, dependencia de servicio y balcón terraza con vista panorámica.", "https://zar2010.com/wp-content/uploads/2026/02/VZ1874-Calle-Subida-Pescadores-Miranda-Delfin-Garrucha-9.jpg", 1),
]

for p in productos_v1:
    crear_producto(token1, *p)

# ─── VENDEDOR 2 (subastas_centro) ─────────────────────────────────────────────
print("\n=== Vendedor 2 ===")
productos_v2 = [
    ("Horno Eléctrico Atma 40L", "Horno de mesa con convección, timer, termostato regulable y bandeja asadora. Uso mínimo.", "https://previsoraarg.vtexassets.com/arquivos/ids/156311-800-auto?v=637866587609670000&width=800&height=auto&aspect=true", 3),
    ("Terreno con Nivelación y Cerco Perimetral", "Lote regular totalmente limpio y nivelado. Cuenta con pilar de luz reglamentario instalado y conexión de agua.", "https://thumbs.dreamstime.com/b/dark-metal-fence-private-property-spring-detail-modern-brown-surrounding-residential-estate-tall-green-spruce-trees-450750206.jpg", 1),
    ("Juego de Comedor Madera 6 Sillas", "Mesa rectangular de madera maciza de pino lustrado con 6 sillas tapizadas en ecocuero.", "https://thumbs.dreamstime.com/b/mesa-de-comedor-moderna-con-seis-sillas-altas-madera-respaldo-y-ambientes-sitio-241441111.jpg", 3),
    ("Nissan Frontier PRO-4X 2022", "Camioneta off-road con bloqueo de diferencial trasero, cámaras 360 y suspensión reforzada.", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSovOU3Mk0SbEEB2YKIoZ8vvs8WVGyRMpJzng&s", 2),
    ("Casa Sencilla con Amplio Terreno Libre", "Construcción básica de ladrillos, 2 habitaciones, baño y cocina funcional. Gran superficie de terreno libre hacia el fondo.", "https://www.shutterstock.com/shutterstock/videos/3770780985/thumb/1.jpg?ip=x480", 1),
    ("Microondas BGH 20 Litros", "Horno microondas digital, color blanco, con múltiples niveles de potencia y función descongelar.", "https://grupomarquez.com.ar/Image/0/700_700-D_NQ_NP_919533-MLA49772421100_042022-O.jpg", 3),
    ("Royal Enfield Interceptor 650 2023", "Estilo clásico con motor de 648cc. Impecable, con accesorios originales incluidos.", "https://upload.wikimedia.org/wikipedia/commons/b/ba/Royal_Enfield_Interceptor_%281%29.jpg", 2),
    ("Lote de Terreno en Zona de Crecimiento", "Lote de 12x30 metros en nuevo loteo residencial. Apertura de calles consolidada y tendido eléctrico en ejecución.", "https://thumbs.dreamstime.com/b/zona-residencial-con-un-gran-lote-vac%C3%ADo-en-el-medio-terreno-est%C3%A1-rodeado-de-casas-y-una-valla-385832768.jpg", 1),
    ("Ford Transit 350L 2019", "Furgón largo de techo alto, excelente mecánica. Lista para trabajar o adaptar como motorhome.", "https://images.unsplash.com/photo-1776982298134-d24cc9a3de2b?w=500&auto=format&fit=crop&q=60", 2),
    ("Lote Terreno Perímetro Semicerrado", "Lote de 400m2 delimitado con cerco perimetral de alambrado olímpico y portón de acceso colocado. Entorno natural.", "https://http2.mlstatic.com/D_NQ_NP_2X_733800-MLA109944313140_042026-N.webp", 1),
    ("Casa de 4 Dormitorios, Quincho y Piscina", "Vivienda residencial de categoría en barrio residencial consolidado. Amplio quincho cerrado, asador y gran piscina de material.", "https://baroneinmobiliaria.com.ar/wp-content/uploads/2022/10/chalet-san-isidro-5.jpg", 1),
    ("Honda XR 250 Tornado 2022", "Motocicleta tipo enduro, ideal para ciudad y terrenos mixtos. Todos los papeles al día.", "https://images.unomotos.com.ar/unomotos/motos/honda_tornado_250_2020_1.png", 2),
    ("Smart TV LG 50 Pulgadas 4K", "Televisor inteligente resolución 4K UHD, conectividad Wi-Fi, Bluetooth y sistema operativo webOS.", "https://thumbs.dreamstime.com/b/samut-prakan-tailandia-de-agosto-la-nueva-tv-en-lg-es-el-modelo-uhd-ai-thinq-pulgadas-inch-muestra-tienda-electr%C3%B3nica-192335474.jpg", 3),
    ("Licuadora con Jarra de Vidrio Philips", "Licuadora de 800W, jarra de vidrio de 2 litros resistente al calor, cuchillas de acero inoxidable extraíbles.", "https://thumbs.dreamstime.com/b/licuadora-elegante-para-suavidades-y-preparaci%C3%B3n-de-alimentos-esta-imagen-muestra-una-moderna-batidora-roja-dise%C3%B1ada-hacer-342771638.jpg", 3),
    ("Renault Sandero 2020", "Hatchback compacto de 5 puertas, ideal para uso urbano. Único dueño.", "https://images.unsplash.com/photo-1600210733081-c3b8a58a2749?q=80&w=387&auto=format&fit=crop", 2),
    ("Casa Céntrica con Local Comercial Al Frente", "Vivienda familiar de 2 habitaciones unida a local comercial independiente al frente. Ideal para inversión mixta.", "https://www.revigorate.com/images/ludwigstrabe-garmisc-partenkirchen-germany.jpg", 1),
    ("Conjunto Sommier y Colchón 2 Plazas", "Colchón de resortes ensacados y base sommier de madera forrada. Medidas 140x190 cm.", "https://jeancartierhogar.com.ar/wp-content/uploads/2026/03/001-B-scaled.jpg", 3),
    ("Departamento de 2 Dormitorios con Amenities", "Semipiso de categoría. Cuenta con cochera subterránea, terraza con piscina de uso común, SUM y quincho con asadores.", "https://www.ahstatic.com/photos/b9l6_roq2aps_00_p_1024x768.jpg", 1),
    ("Rack para TV de Melamina", "Mueble para TV hasta 65 pulgadas, diseño nórdico con patas de madera paraíso y puertas corredizas.", "https://thumbs.dreamstime.com/b/centro-de-entretenimiento-moderno-para-sal%C3%B3n-426424998.jpg", 3),
    ("Aire Acondicionado Split 3000F", "Equipo split frío/calor, eficiencia energética A, gas ecológico. Control remoto incluido.", "https://nearefrigeracion.com.ar/cdn/shop/files/YK-3000FC.jpg?v=1762369751", 3),
    ("Fiat Cronos Precision 1.8 2021", "Sedán familiar con amplio baúl, llantas de aleación y climatizador automático. Patentes al día.", "https://images.pexels.com/photos/28836720/pexels-photo-28836720.jpeg", 2),
    ("Sillón 3 Cuerpos Chenille", "Sofá de tres cuerpos tapizado en tela chenille gris oscuro. Estructura reforzada y almohadones desmontables.", "https://santamadera.com.ar/assets/img/products/sillones-living/sillon-esquinero-venecia-3-cuerpos/1-sillon-esquinero-venecia-3-cuerpos-chenille.webp", 3),
    ("Cómoda Cajonera 6 Cajones", "Mueble cajonero en MDF laqueado blanco, tiradores metálicos. Dimensiones: 120x80x40 cm.", "https://media.istockphoto.com/id/1130450554/es/foto/maqueta-interior-render-3d.jpg?s=612x612&w=0&k=20&c=GRzAPWoJ6UY7nWOPVmVwsQ-2bPUDgJ3cPc5fe1NI0CQ=", 3),
    ("Casa Minimalista de 2 Plantas en Estreno", "Diseño moderno con aberturas de aluminio DVH, pisos de porcelanato, calefacción central e instalaciones de primera calidad.", "https://thumbs.dreamstime.com/b/casa-blanca-moderna-con-acentos-negros-exterior-dise%C3%B1o-de-edificios-residenciales-dos-plantas-lujoso-hogar-al-atardecer-lujo-408830046.jpg", 1),
    ("Departamento Moderno de 1 Dormitorio Centro", "Departamento céntrico, tercer piso por ascensor. Living-comedor luminoso, cocina integrada, baño completo y balcón al frente.", "https://thumbs.dreamstime.com/b/sala-de-estar-en-loft-adaptada-como-un-apartamento-moderno-y-espacioso-car%C3%A1cter-industrial-dado-por-los-ladrillos-paredes-parqu%C3%A9-318166585.jpg", 1),
    ("Lote Esquina en Barrio Residencial", "Ubicación destacada en esquina, ideal para proyecto constructivo de viviendas agrupadas o locales. Todos los servicios en puerta.", "https://thumbs.dreamstime.com/b/lote-vacante-en-una-nueva-subdivisi%C3%B3n-de-construcci%C3%B3n-spokane-wash-%C2%BA-estad%C3%ADa-wa-usa-agosto-un-terreno-nuevo-barrio-residencial-184647486.jpg", 1),
    ("Honda HR-V EX-L 2018", "Crossover muy cuidado, tapizado de cuero, caja automática CVT. Revisión técnica aprobada.", "https://upload.wikimedia.org/wikipedia/commons/0/09/2017_Honda_HR-V_EX_i-VTEC_1.5_Front.jpg", 2),
    ("Casa de Estilo Colonial a Restaurar", "Propiedad antigua de gran valor arquitectónico. 4 habitaciones, galería interna, patio de tierra y estructura sólida para reciclar.", "https://us.123rf.com/450wm/mulderphoto/mulderphoto2508/mulderphoto250800104/258929907-old-shabby-houses-in-the-slum-district.jpg?ver=6", 1),
    ("Silla de Oficina Ergonómica", "Silla giratoria con respaldo de malla transpirable, soporte lumbar, apoyabrazos ajustables y pistón a gas.", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSrL0B-A1GB62Xuxn2MsvgFJo69l0BuKXOgjA&s", 3),
    ("Kawasaki Ninja 400 2020", "Motocicleta deportiva ligera, carenado completo. Mantenimiento realizado en concesionario oficial.", "https://cdpcdn.dx1app.com/products-private/prod/736e7eeb-1566-4a79-a68b-349951b5ebfa/84d8f04b-04bf-44e4-b284-a619014687b8/00000000-0000-0000-0000-000000000000/8ef16ebe-547d-4fce-82bc-b1860050278a/0868981a-7580-4aaf-8802-b38601041777/6000000002.jpg", 2),
    ("Monoambiente Luminoso con Balcón Al Frente", "Unidad funcional a estrenar, ideal para inversión o estudiantes. Cocina con amoblamiento completo y baño con bañera.", "https://us.123rf.com/450wm/wavebreakmediamicro/wavebreakmediamicro2210/wavebreakmediamicro221000573/192835299-vista-general-de-cocina-moderna-con-encimera-equipamiento-de-cocina-y-ventana-concepto-de-casa.jpg?ver=6", 1),
    ("Local Comercial en Galería Planta Baja", "Local de 25m2 en zona peatonal de alto tránsito. Expensas bajas, ideal para rubro indumentaria o estética.", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTCwC_HYIt17vZHhCx08SSdKHURu5K3yBZj9g&s", 1),
    ("Terreno Apto Desarrollos Edificios", "Lote céntrico estratégico de 500m2 con indicadores urbanísticos óptimos para la edificación en altura.", "https://d1acdg20u0pmxj.cloudfront.net/listings/d0768764-600a-4e04-aebd-7e44b9fa0bd4/860x440/c0d8eec8-2829-4231-832a-f0d3cb74270f.jpg", 1),
    ("Casa de Campo con Arboleda Centenaria", "Vivienda tipo casa de campo en zona periurbana. Galería perimetral, cocina a leña, 2 habitaciones y gran parque forestado.", "https://thumbs.dreamstime.com/b/siglo-xix-del-hogar-52090199.jpg", 1),
    ("Local Comercial con Vidriera en Zona Céntrica", "Excelente local de 50m2 cubiertos, planta baja con baño privado y persiana metálica de seguridad automatizada.", "https://thumbs.dreamstime.com/b/tienda-de-comestibles-contempor%C3%A1nea-exterior-con-una-gran-vidriera-que-muestra-productos-frescos-interior-moderno-fruta-fresca-la-385469085.jpg", 1),
]

for p in productos_v2:
    crear_producto(token2, *p)

print("\nListo.")
