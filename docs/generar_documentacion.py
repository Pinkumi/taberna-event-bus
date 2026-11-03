"""Genera los PNG con PlantUML y el PDF con ReportLab y Pillow."""

import argparse
import os
from pathlib import Path
import subprocess

from PIL import Image
from reportlab.lib.styles import ParagraphStyle
from reportlab.pdfgen import canvas
from reportlab.platypus import Paragraph


DOCUMENTOS = Path(__file__).resolve().parent
DIAGRAMAS = [
    ("clases", "Diagrama de clases",
     "Las clases se organizan en aplicación, bus, componentes y eventos. Launcher crea y conecta "
     "los sistemas mediante EventBus. Barra y Cocina utilizan su propia PreparacionProgramada y "
     "comparten el catálogo. Los cuatro records conservan los datos; productos se copia de forma "
     "inmutable. AutoCloseable identifica los recursos que deben cerrarse."),
    ("secuencia", "Diagrama de secuencia",
     "SistemaDePedidos publica el pedido de mesa_5. El bus notifica al panel, la barra y la cocina. "
     "Las preparaciones se programan sin bloquear al publicador y generan eventos tras unos 2 y 5 "
     "segundos. En paralelo, la banda publica música para sonido, panel y humo. Solo el título "
     "Through the Fire and Flames activa humo. El cierre espera las preparaciones y detiene los hilos."),
    ("componentes", "Diagrama de componentes",
     "Los publicadores y suscriptores se comunican mediante el bus dentro de un único proceso Java. "
     "Las flechas continuas representan eventos y las punteadas representan uso o creación. "
     "El panel escucha los cuatro tipos de eventos. Agregar SistemaDeHumo requiere suscribirlo, "
     "sin modificar Banda ni EventBus, lo que demuestra el desacoplamiento del patrón."),
]


def parrafo(pdf, texto, x, y, ancho, tamano=12):
    estilo = ParagraphStyle("texto", fontName="Helvetica", fontSize=tamano, leading=tamano * 1.4)
    bloque = Paragraph(texto, estilo)
    _, alto = bloque.wrap(ancho, 1000)
    bloque.drawOn(pdf, x, y - alto)
    return y - alto


def generar_pdf():
    pdf = canvas.Canvas(str(DOCUMENTOS / "Diagramas_UML.pdf"))
    pdf.setTitle("Meta 3.1 - El Gólem Alquímico - Diagramas UML")
    pdf.setAuthor("Proyecto taberna-event-bus")
    for numero, (archivo, titulo, explicacion) in enumerate(DIAGRAMAS, start=1):
        imagen = DOCUMENTOS / "uml" / f"{archivo}.png"
        with Image.open(imagen) as png:
            ancho_imagen, alto_imagen = (valor * 0.75 for valor in png.size)
        # Una página por diagrama, a 96 dpi y sin reducir su texto para ajustarlo a A4.
        ancho = max(595, ancho_imagen + 72)
        alto = alto_imagen + 290
        pdf.setPageSize((ancho, alto))
        pdf.setFont("Helvetica-Bold", 20)
        pdf.drawString(36, alto - 40, "Meta 3.1 · El Gólem Alquímico")
        pdf.setFont("Helvetica-Bold", 15)
        pdf.drawString(36, alto - 66, titulo)
        parrafo(pdf, "Publisher/Subscriber permite que los sistemas publiquen eventos sin conocer "
                "a quienes los reciben. EventBus registra suscriptores por tipo y los notifica en "
                "el hilo publicador; las tareas de preparación y música usan ejecutores independientes.",
                36, alto - 82, ancho - 72)
        base_imagen = 150
        pdf.drawImage(str(imagen), (ancho - ancho_imagen) / 2, base_imagen,
                      width=ancho_imagen, height=alto_imagen)
        parrafo(pdf, explicacion, 36, base_imagen - 16, ancho - 72)
        pdf.setFont("Helvetica", 10)
        pdf.drawString(36, 25, f"Fuente: docs/uml/{archivo}.puml · PlantUML · Página {numero} de 3")
        pdf.showPage()
    pdf.save()


if __name__ == "__main__":
    argumentos = argparse.ArgumentParser(description=__doc__)
    argumentos.add_argument("--plantuml", required=True, type=Path, help="Ruta al JAR de PlantUML")
    argumentos.add_argument("--java", default="java", help="Ejecutable Java")
    opciones = argumentos.parse_args()
    entorno = dict(os.environ, PLANTUML_LIMIT_SIZE="16000")
    subprocess.run([opciones.java, "-jar", str(opciones.plantuml.resolve()), "-charset", "UTF-8",
                    "-tpng", *[str(DOCUMENTOS / "uml" / f"{nombre}.puml")
                               for nombre, _, _ in DIAGRAMAS]], check=True, env=entorno)
    generar_pdf()
    print("Generados tres PNG y docs/Diagramas_UML.pdf")
