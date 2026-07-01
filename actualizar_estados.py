import subprocess, sys

# Instalar psycopg2 si no está
subprocess.check_call([sys.executable, "-m", "pip", "install", "psycopg2-binary", "--break-system-packages", "-q"])

import psycopg2

conn = psycopg2.connect(
    host="dpg-d8vjpaho3t8c73bbi8fg-a.oregon-postgres.render.com",
    port=5432,
    dbname="db_subastas",
    user="db_subastas_user",
    password="Q1G25bODdSM6xZOfUw2tm8SjKd0S2m2s"
)

cur = conn.cursor()

# ~20 subastas → ACTIVA (fechaInicio al pasado, fechaCierre al futuro)
cur.execute("""
    UPDATE subastas
    SET fecha_inicio = '2026-06-01 10:00:00+00'
    WHERE estado = 'PUBLICADA' AND id <= 25
""")
print(f"ACTIVA pendientes: {cur.rowcount} filas")

# ~12 subastas → FINALIZADA (ambas fechas al pasado, sin pujas = FINALIZADA)
cur.execute("""
    UPDATE subastas
    SET fecha_inicio = '2026-06-01 10:00:00+00',
        fecha_cierre = '2026-06-15 10:00:00+00'
    WHERE estado = 'PUBLICADA' AND id BETWEEN 26 AND 40
""")
print(f"FINALIZADA pendientes: {cur.rowcount} filas")

conn.commit()
cur.close()
conn.close()

print("\nListo. Esperá 30 segundos y el scheduler actualiza los estados.")
