# Genera docs/Ingenieria_Web_TicketCine.docx siguiendo la estructura oficial del curso
# (Proyecto_Estructura_v2: secciones 5.1 a 5.15 y formato del punto 6).
# Requiere: pip install python-docx pillow
import copy
import re
from pathlib import Path

from docx import Document
from docx.enum.section import WD_ORIENT
from docx.enum.table import WD_TABLE_ALIGNMENT
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.opc.constants import RELATIONSHIP_TYPE as RT
from docx.opc.packuri import PackURI
from docx.opc.part import Part
from docx.oxml import OxmlElement, parse_xml
from docx.oxml.ns import qn
from docx.shared import Cm, Pt, RGBColor
from PIL import Image

D = Path(__file__).resolve().parent
IMG = D / 'img'
d = Document()

# ---------- Formato (punto 6): A4, márgenes 3/3/2.5/2.5, Arial 11, interlineado simple ----------
for s in d.sections:
    s.page_width, s.page_height = Cm(21), Cm(29.7)
    s.top_margin = s.bottom_margin = Cm(3)
    s.left_margin = s.right_margin = Cm(2.5)
ANCHO = 16  # ancho útil en cm

st = d.styles['Normal']
st.font.name = 'Arial'
st.font.size = Pt(11)
st.element.rPr.rFonts.set(qn('w:eastAsia'), 'Arial')
st.paragraph_format.line_spacing = 1.0
st.paragraph_format.space_after = Pt(6)
for n, size in [('Heading 1', 12), ('Heading 2', 11), ('Heading 3', 11)]:
    h = d.styles[n]
    h.font.name = 'Arial'
    h.font.size = Pt(size)
    h.font.bold = True
    h.font.italic = False
    h.font.color.rgb = RGBColor(0, 0, 0)
    rf = h.element.rPr.rFonts
    for a in ['w:asciiTheme', 'w:hAnsiTheme', 'w:eastAsiaTheme', 'w:cstheme']:
        if rf.get(qn(a)) is not None:
            del rf.attrib[qn(a)]
    rf.set(qn('w:ascii'), 'Arial')
    rf.set(qn('w:hAnsi'), 'Arial')
    h.paragraph_format.space_before = Pt(12)
    h.paragraph_format.space_after = Pt(6)
    h.paragraph_format.line_spacing = 1.0
for n in ['List Bullet', 'List Number']:
    d.styles[n].paragraph_format.line_spacing = 1.0
    d.styles[n].paragraph_format.space_after = Pt(3)

# ---------- Notas al pie ----------
NOTAS = []


def nota(p, texto):
    """Agrega una llamada de nota al pie al final del párrafo p."""
    NOTAS.append(texto)
    r = OxmlElement('w:r')
    rpr = OxmlElement('w:rPr')
    va = OxmlElement('w:vertAlign')
    va.set(qn('w:val'), 'superscript')
    rpr.append(va)
    r.append(rpr)
    ref = OxmlElement('w:footnoteReference')
    ref.set(qn('w:id'), str(len(NOTAS)))
    r.append(ref)
    p._p.append(r)


def escribir_notas():
    W = 'http://schemas.openxmlformats.org/wordprocessingml/2006/main'
    xml = ('<w:footnotes xmlns:w="%s">'
           '<w:footnote w:type="separator" w:id="-1"><w:p><w:r><w:separator/></w:r></w:p></w:footnote>'
           '<w:footnote w:type="continuationSeparator" w:id="0"><w:p><w:r><w:continuationSeparator/></w:r></w:p></w:footnote>') % W
    for i, t in enumerate(NOTAS, 1):
        t = t.replace('&', '&amp;').replace('<', '&lt;').replace('>', '&gt;')
        xml += ('<w:footnote w:id="%d"><w:p><w:pPr><w:spacing w:after="0"/></w:pPr>'
                '<w:r><w:rPr><w:rFonts w:ascii="Arial" w:hAnsi="Arial"/><w:vertAlign w:val="superscript"/><w:sz w:val="16"/></w:rPr><w:footnoteRef/></w:r>'
                '<w:r><w:rPr><w:rFonts w:ascii="Arial" w:hAnsi="Arial"/><w:sz w:val="16"/></w:rPr><w:t xml:space="preserve"> %s</w:t></w:r></w:p></w:footnote>') % (i, t)
    xml += '</w:footnotes>'
    parte = Part(PackURI('/word/footnotes.xml'),
                 'application/vnd.openxmlformats-officedocument.wordprocessingml.footnotes+xml',
                 xml.encode('utf-8'), d.part.package)
    d.part.relate_to(parte, RT.FOOTNOTES)


# ---------- Utilidades ----------
def runs(p, text, size=None):
    for part in re.split(r'(\*\*[^*]+\*\*|\*[^*]+\*|`[^`]+`)', text):
        if not part:
            continue
        if part.startswith('**'):
            r = p.add_run(part[2:-2]); r.bold = True
        elif part.startswith('`'):
            r = p.add_run(part[1:-1]); r.font.name = 'Consolas'
            r.font.size = Pt(size - 0.5 if size else 10)
            continue
        elif part.startswith('*') and len(part) > 2:
            r = p.add_run(part[1:-1]); r.italic = True
        else:
            r = p.add_run(part)
        if size:
            r.font.size = Pt(size)
    return p


def par(text, just=True, size=None):
    p = d.add_paragraph()
    runs(p, text, size)
    if just:
        p.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
    return p


def sub(text):
    CONTADOR[0] = 0
    p = d.add_paragraph()
    p.paragraph_format.space_before = Pt(6)
    r = p.add_run(text); r.bold = True
    return p


CONTADOR = [0]


def vineta(text, num=False):
    if num:
        CONTADOR[0] += 1
        p = d.add_paragraph()
        p.paragraph_format.left_indent = Cm(0.75)
        p.paragraph_format.first_line_indent = Cm(-0.75)
        p.paragraph_format.space_after = Pt(3)
        p.add_run('%d.\t' % CONTADOR[0])
        p.paragraph_format.tab_stops.add_tab_stop(Cm(0.75))
    else:
        CONTADOR[0] = 0
        p = d.add_paragraph(style='List Bullet')
    runs(p, text)
    p.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
    return p


def shade(cell, color):
    tcPr = cell._tc.get_or_add_tcPr()
    sh = OxmlElement('w:shd'); sh.set(qn('w:val'), 'clear'); sh.set(qn('w:fill'), color)
    tcPr.append(sh)


def repetir_encabezado(fila):
    trPr = fila._tr.get_or_add_trPr()
    e = OxmlElement('w:tblHeader'); e.set(qn('w:val'), 'true'); trPr.append(e)


def tabla(rows, widths=None, size=9, center=(), header=True):
    t = d.add_table(rows=len(rows), cols=len(rows[0]))
    t.style = 'Table Grid'
    t.alignment = WD_TABLE_ALIGNMENT.CENTER
    for ri, row in enumerate(rows):
        for ci, c in enumerate(row):
            cell = t.cell(ri, ci)
            p = cell.paragraphs[0]
            p.paragraph_format.space_after = Pt(0)
            runs(p, c if (ri or not header) else c.replace('**', ''), size)
            if ri == 0 and header:
                for r in p.runs:
                    r.bold = True
                shade(cell, 'D9D9D9')
            if ci in center:
                p.alignment = WD_ALIGN_PARAGRAPH.CENTER
            if widths:
                cell.width = Cm(widths[ci])
    if header:
        repetir_encabezado(t.rows[0])
    fijar_anchos(t, widths or [ANCHO / len(rows[0])] * len(rows[0]))
    d.add_paragraph().paragraph_format.space_after = Pt(0)
    return t


def fijar_anchos(t, widths):
    t.autofit = False
    tbl = t._tbl
    tblPr = tbl.tblPr
    for tag in ('w:tblW', 'w:tblLayout'):
        e = tblPr.find(qn(tag))
        if e is not None:
            tblPr.remove(e)
    tw = OxmlElement('w:tblW'); tw.set(qn('w:w'), str(int(sum(widths) * 567))); tw.set(qn('w:type'), 'dxa')
    tblPr.append(tw)
    lay = OxmlElement('w:tblLayout'); lay.set(qn('w:type'), 'fixed'); tblPr.append(lay)
    grid = tbl.tblGrid
    for gc, w in zip(grid.findall(qn('w:gridCol')), widths):
        gc.set(qn('w:w'), str(int(w * 567)))
    for fila in t.rows:
        for c, w in zip(fila.cells, widths):
            c.width = Cm(w)


FIG = [0]


def figura(nombre, titulo, ancho=ANCHO, alto_max=21):
    w, h = Image.open(IMG / nombre).size
    ancho = min(ancho, alto_max * w / h)
    d.add_picture(str(IMG / nombre), width=Cm(ancho))
    d.paragraphs[-1].alignment = WD_ALIGN_PARAGRAPH.CENTER
    d.paragraphs[-1].paragraph_format.keep_with_next = True
    FIG[0] += 1
    p = d.add_paragraph()
    r = p.add_run('Figura %d. ' % FIG[0]); r.bold = True; r.font.size = Pt(9)
    runs(p, titulo, 9)
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER


def h1(t):
    CONTADOR[0] = 0
    return d.add_heading(t, 1)


def h2(t):
    CONTADOR[0] = 0
    return d.add_heading(t, 2)


# ---------- Bloques reutilizados del Markdown de anexos ----------
MD = (D / 'secciones-faltantes.md').read_text(encoding='utf-8').splitlines()


def bloque_md(desde, hasta=None, saltar_titulo=True):
    """Renderiza las líneas del Markdown entre dos encabezados (sin incluir 'hasta')."""
    i = next(k for k, l in enumerate(MD) if l.startswith(desde))
    fin = len(MD) if hasta is None else next(k for k, l in enumerate(MD) if k > i and l.startswith(hasta))
    lineas = MD[i + (1 if saltar_titulo else 0):fin]
    k = 0
    while k < len(lineas):
        l = lineas[k]
        if not l.strip():
            k += 1; continue
        m = re.match(r'!\[(.*)\]\((.*)\)', l)
        if m:
            titulo = lineas[k + 2].strip('*') if k + 2 < len(lineas) and lineas[k + 2].startswith('*Figura') else m.group(1)
            titulo = re.sub(r'^Figura \d+\. ', '', titulo).replace('`', '')
            figura(m.group(2).replace('img/', ''), titulo, ANCHO if 'movil' not in m.group(2) else 7)
            k += 3 if titulo != m.group(1) else 1
            continue
        if l.startswith('|'):
            rows = []
            while k < len(lineas) and lineas[k].startswith('|'):
                if not re.match(r'^\|[\s\-|]+\|$', lineas[k]):
                    rows.append([c.strip() for c in lineas[k].strip('|').split('|')])
                k += 1
            tabla(rows)
            continue
        m = re.match(r'^(\d+)\. (.*)', l)
        if m:
            vineta(m.group(2), num=True); k += 1; continue
        if l.startswith('- '):
            vineta(l[2:]); k += 1; continue
        if l.startswith('**') and l.endswith('**') and l.count('**') == 2:
            sub(l[2:-2]); k += 1; continue
        if l.startswith('### '):
            sub(l[4:]); k += 1; continue
        par(l); k += 1


# =====================================================================
# CARÁTULA (punto 6.3)
# =====================================================================
def cen(text, bold=True, size=11, after=6):
    p = d.add_paragraph(); p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    r = p.add_run(text); r.bold = bold; r.font.size = Pt(size)
    p.paragraph_format.space_after = Pt(after)
    return p


cen('“Año de la Esperanza y el Fortalecimiento de la Democracia”', bold=False, size=10, after=24)
d.add_picture(str(IMG / 'logo-uc.png'), width=Cm(8))
d.paragraphs[-1].alignment = WD_ALIGN_PARAGRAPH.CENTER
cen('FACULTAD DE INGENIERÍA', after=2)
cen('Programa: Ingeniería de Sistemas', bold=False, after=30)
cen('PLAN DE PROYECTO DE INVESTIGACIÓN APLICADA', size=10, after=4)
cen('SISTEMA DE VENTA DE TICKETS PARA PELÍCULAS EN UN CINE', size=14, after=2)
cen('TicketCine – Cine Amazonas', bold=False, size=12, after=30)
cen('CURSO', after=0); cen('Ingeniería Web', bold=False, after=18)
cen('DOCENTE', after=0); cen('Zarate Mendoza Roberto', bold=False, after=18)
cen('CICLO, AULA Y SEMESTRE', after=0)
cen('Ciclo: ______     Aula: ______     Semestre: 2026-20', bold=False, after=36)
p = d.add_paragraph(); r = p.add_run('COORDINADOR DEL GRUPO: '); r.bold = True
p.add_run('Pacheco Gaspar Jean Brandon')
p = d.add_paragraph(); r = p.add_run('INTEGRANTES:'); r.bold = True
p.paragraph_format.space_after = Pt(2)
t = d.add_table(rows=5, cols=2)
for i, (n, pc) in enumerate([('Pacheco Gaspar Jean Brandon', '100%'), ('Arroyo Tello Geraldo Gerson', '100%'),
                             ('Jara Nuñuvero Ani', '0%'), ('Quispe Meza Renzo', '100%'),
                             ('Zamudio Benito Dayaneira Judith', '100%')]):
    a = t.cell(i, 0).paragraphs[0]; a.add_run('•  ' + n); a.paragraph_format.space_after = Pt(1)
    b = t.cell(i, 1).paragraphs[0]; b.add_run(pc); b.alignment = WD_ALIGN_PARAGRAPH.RIGHT
    b.paragraph_format.space_after = Pt(1)
    t.cell(i, 0).width = Cm(12); t.cell(i, 1).width = Cm(4)
fijar_anchos(t, [12, 4])
d.add_page_break()

# =====================================================================
# 1. RESUMEN
# =====================================================================
h1('1. Resumen')
par('TicketCine es una aplicación web desarrollada con Spring Boot, JSP y H2 orientada a digitalizar la gestión y venta de entradas en Cine Amazonas. El sistema permite a los clientes consultar la cartelera vigente, seleccionar una función, indicar la cantidad de entradas y registrar una compra, generando los tickets electrónicos correspondientes con un número correlativo único. Asimismo, cuenta con un módulo administrativo para gestionar géneros, películas, salas, funciones y usuarios, además de un dashboard con indicadores relacionados con las ventas y el funcionamiento del cine.')
par('El alcance del proyecto comprende el diagnóstico de la oportunidad de mejora, los objetivos, el modelo de datos, las interfaces de cliente y administrador, 25 funcionalidades y 14 reglas de negocio. Como sustento del desarrollo se entregan el diagrama entidad-relación, el diccionario de datos, los diagramas de casos de uso y de secuencia del proceso core (compra de entradas) y de soporte (acceso al sistema), los patrones de desarrollo aplicados y el cronograma; en los anexos se incluyen la landing page, el storyboard y el MVP publicado en la nube.')

# =====================================================================
# 2. INTRODUCCIÓN
# =====================================================================
h1('2. Introducción')
par('La gestión de venta de entradas en cines que trabajan principalmente mediante boletería presencial puede generar colas, dificultades para controlar la cantidad de entradas vendidas respecto al aforo disponible de cada función y limitada disponibilidad de información sobre las ventas realizadas. Ante esta situación, el proyecto TicketCine propone el desarrollo de una aplicación web que permita digitalizar parte de este proceso mediante la publicación de la cartelera, programación de funciones, registro de ventas y generación de tickets electrónicos.')
par('El sistema permitirá que los clientes consulten las películas disponibles y sus respectivas funciones, mientras que los administradores podrán mantener actualizada la información de películas, géneros, salas, funciones y usuarios. Además, se incorporará un dashboard para consultar indicadores relacionados con las ventas.')
par('A partir del diagnóstico social, económico, tecnológico y ecológico se identifica la oportunidad de mejora existente y se establecen los objetivos y el alcance que orientan el desarrollo del proyecto. El impacto esperado en el entorno es triple: los clientes de Cine Amazonas compran sus entradas sin hacer cola, la administración controla el aforo y conoce sus ventas en tiempo real, y se reduce el uso de boletos impresos.')

# =====================================================================
# 3. DIAGNÓSTICO (fuentes en notas al pie)
# =====================================================================
h1('3. Diagnóstico')
sub('Variable Social.')
p = par('La asistencia a salas de cine en el Perú es todavía minoritaria y desigual: tras la caída producida por la pandemia, en 2022 solo el 16.6% de la población mayor de 14 años había asistido a una función de cine en los últimos doce meses, y el público que más asiste se concentra en personas de 14 a 29 años, de estratos socioeconómicos altos y con educación superior.')
nota(p, 'Ministerio de Cultura del Perú. (2023). Indicadores de asistencia a funciones de cine. Infoartes, con datos de la Encuesta Nacional de Programas Presupuestales (ENAPRES) 2022 del INEI. https://infoartes.pe/posts/indicadores-de-asistencia-funciones-de-cine')
par('Esto evidencia que buena parte del público que sí asiste al cine pertenece a un segmento joven y familiarizado con canales digitales, lo que favorece la adopción de un sistema de compra de entradas por internet frente al modelo exclusivamente presencial.')
sub('Variable Económica.')
p = par('El sector cinematográfico peruano atraviesa un repunte: en 2025 operaron alrededor de 700 pantallas a nivel nacional y la Asociación Nacional de Salas Cinematográficas proyectó que ese año sería el de mayor crecimiento del último lustro, superando los 42 millones de espectadores de 2024.')
nota(p, 'Gestión. (2025). Cines en Perú: ¿qué películas impulsarán un 2025 con el mayor crecimiento en los últimos 5 años? https://gestion.pe/economia/empresas/cines-en-peru-por-que-el-2025-sera-el-de-mayor-crecimiento-en-los-ultimos-5-anos-industria-cinematografica-peliculas-indecopi-noticia/')
p = par('Estimaciones de PwC citadas por la prensa especializada calculan que el mercado cinematográfico peruano generará cerca de US$ 199 millones en 2025, con una tasa de crecimiento anual compuesta de 42.23% entre 2020 y 2025.')
nota(p, 'Gestión. (2021, 20 de diciembre). Industria peruana de cine crecerá 42% hasta el 2025, según PwC (Global Entertainment & Media Outlook 2021-2025). https://gestion.pe/economia/empresas/industria-peruana-de-cine-crecera-42-hasta-el-2025-segun-pwc-noticia/')
par('El crecimiento proyectado del sector sustenta la viabilidad económica de invertir en una plataforma propia de venta de entradas, en lugar de depender únicamente de la boletería física.')
sub('Variable Tecnológica.')
p = par('El comercio electrónico en el Perú mantiene un crecimiento sostenido: en 2024 movió alrededor de US$ 15,600 millones, 21.2% más que el año anterior; el 55% de los peruanos (18.7 millones de personas) realizó compras por internet y las billeteras móviles ya representan el 26% de los pagos en línea.')
nota(p, 'Forbes Perú. (2025, 30 de mayo). Comercio electrónico creció 21,2% durante 2024, según Capece. https://forbes.pe/economia-y-finanzas/2025-05-30/comercio-electronico-crecio-212-durante-2024-segun-capece')
par('Esta tendencia se refuerza con la expectativa de los consumidores de completar sus transacciones de forma inmediata y sin fricciones, desde el celular y en cualquier momento.')
par('Estas condiciones tecnológicas hacen viable ofrecer a los clientes de Cine Amazonas un canal de compra en línea integrado con medios de pago digitales, en lugar de limitarse a la venta presencial en taquilla.')
sub('Variable Ecológica.')
p = par('La migración de comprobantes físicos a comprobantes electrónicos —impulsada por la normativa de la SUNAT, que desde junio de 2022 obliga a todas las empresas a emitirlos— reduce el consumo de papel y los costos de impresión, distribución y almacenamiento físico de documentos, además de mejorar la experiencia del cliente al recibir su comprobante de forma digital.')
nota(p, 'La República. (2022, 31 de mayo). Sunat: desde el 1 de junio todas las empresas deberán emitir comprobantes electrónicos. https://larepublica.pe/economia/2022/05/31/sunat-desde-el-1-junio-todas-las-empresas-deberan-emitir-comprobantes-electronicos-factura-electronica-boleta-electronica-mypes ; SUNAT. (s.f.). Comprobantes de pago electrónicos: conceptos generales y beneficios del sistema. https://cpe.sunat.gob.pe/facturacion-mype')
par('Trasladar la venta y el comprobante de la entrada de cine a un formato electrónico dentro de TicketCine se alinea con esta tendencia de reducción de papel impreso (boletos y tickets físicos) y de la huella asociada a su impresión y desecho.')

# =====================================================================
# 4. OBJETIVOS (medibles, alcanzables, con plazo)
# =====================================================================
h1('4. Objetivos')
sub('Objetivo general')
par('Mejorar la gestión comercial y administrativa de Cine Amazonas mediante la automatización de sus procesos de venta de entradas, programación de funciones y control de ventas, contribuyendo a una mejor atención al cliente y a una toma de decisiones más informada.')
sub('Objetivos específicos')
par('**OBJ 1.** Reducir las colas en la boletería de Cine Amazonas logrando que, durante los tres primeros meses de funcionamiento, al menos el 30% de las entradas se venda por el canal web y que una compra en línea se complete en menos de 2 minutos.')
par('**OBJ 2.** Mejorar la organización y el control de las operaciones registrando en el sistema, antes del cierre del semestre 2026-20, el 100% de las películas, géneros, salas, funciones y usuarios de Cine Amazonas, sin ninguna venta que supere el aforo de su función.')
par('**OBJ 3.** Facilitar la toma de decisiones comerciales del propietario poniendo a su disposición, desde el primer mes de operación, un dashboard con 5 métricas de series de tiempo y 5 métricas puntuales actualizadas con cada venta, revisado al menos una vez por semana.')
par('**OBJ 4.** Mejorar la experiencia de los clientes emitiendo como ticket electrónico con número de turno el 100% de las entradas vendidas en línea y publicando la cartelera y las promociones de cada semana en el sitio web durante los tres primeros meses de operación.')
tabla([['Objetivo', 'Indicador (medible)', 'Meta (alcanzable)', 'Plazo (tiempo)'],
       ['OBJ 1', 'Entradas vendidas por web / total de entradas vendidas; tiempo promedio de compra', '≥ 30%; < 2 minutos', 'Primeros 3 meses de operación'],
       ['OBJ 2', 'Registros del catálogo gestionados en el sistema; ventas por encima del aforo', '100%; 0 casos', 'Cierre del semestre 2026-20'],
       ['OBJ 3', 'Métricas disponibles en el dashboard; revisiones del propietario', '10 métricas; 1 revisión por semana', 'Desde el primer mes de operación'],
       ['OBJ 4', 'Entradas en línea con ticket electrónico; carteleras y promociones publicadas', '100%; 1 publicación por semana', 'Primeros 3 meses de operación']],
      widths=[1.8, 6.2, 4, 4])

# =====================================================================
# 5. JUSTIFICACIÓN
# =====================================================================
h1('5. Justificación del Proyecto')
par('TicketCine contribuye a reducir las colas y los errores de venta por encima del aforo propios del proceso manual, y entrega a la administración de Cine Amazonas información oportuna sobre ventas y ocupación que hoy no está disponible de forma centralizada. Esto mejora la experiencia de compra del cliente y la capacidad de gestión del negocio.')
par('**Beneficiarios directos:** los clientes que compran entradas (ahorran tiempo y evitan filas), el personal administrativo del cine (gestiona el catálogo y las ventas desde un solo sistema) y el equipo desarrollador del proyecto (aplica lo aprendido en el curso).')
par('**Beneficiarios indirectos:** los distribuidores de películas que exhibe Cine Amazonas (mayor visibilidad de la cartelera), los proveedores de servicios de pago digital involucrados en el checkout y la comunidad de Iquitos, que gana una alternativa de entretenimiento con un canal de compra más accesible.')

# =====================================================================
# 6. DEFINICIÓN Y ALCANCE
# =====================================================================
h1('6. Definición y alcance')
par('TicketCine es una aplicación web construida sobre Spring Boot 3.5 y Java 21, con vistas JSP + JSTL renderizadas en el servidor para el cliente y un panel administrativo independiente. Los datos se guardan en una base H2 a la que se accede con JdbcTemplate. El modelo entidad-relación contempla las entidades Género, Película, Sala, Función, Usuario y Compra (con su detalle de tickets). El sistema no maneja numeración ni mapa de asientos: cada sala se gestiona por aforo (capacidad total), y cada ticket generado en una venta recibe un número correlativo único que representa el turno de ingreso del cliente a la sala. Al llegar, los clientes ingresan en el orden marcado por ese número y eligen libremente cualquier asiento disponible dentro de la sala, de forma similar a como un grupo de estudiantes elige su ubicación al entrar a un salón. El acceso al sistema se controla mediante login con dos roles: USER (cliente) y ADMIN (administrador), conforme a la matriz de permisos definida para el proyecto.')
par('**Funcionamiento.** El cliente consulta la cartelera, elige una función y la cantidad de entradas; el sistema comprueba el aforo, le pide iniciar sesión o seguir como invitado y muestra el resumen con el total. Al confirmar, el servidor vuelve a comprobar el aforo, registra la compra en una sola transacción y asigna a cada entrada su turno correlativo. El administrador ingresa por un acceso separado para mantener el catálogo y consultar el dashboard.')
par('El alcance incluye: el módulo público de cartelera y compra de entradas (con generación del ticket y su número de turno), las páginas estáticas de Publicidad y Contacto, el panel administrativo con CRUD sobre las tablas del modelo, y un dashboard con 5 métricas de series de tiempo (ventas diarias de la semana, tickets vendidos por semana en el mes, ocupación promedio de salas por mes, funciones programadas por semana e ingresos por género al mes) y 5 métricas puntuales (ventas del día, cupos disponibles en la función más próxima, funciones activas hoy, total de usuarios registrados y película más vendida del mes).')
par('La documentación entregada que sustenta el correcto desarrollo del proyecto es la siguiente:')
tabla([['Documento', 'Sección', 'Qué sustenta'],
       ['Matriz de funcionalidades e interfaces', '7 y 8', 'Las 25 funcionalidades y la interfaz donde se ejecuta cada una'],
       ['Reglas de negocio (14)', '9', 'Políticas que el sistema hace cumplir, con su validación y casos de prueba'],
       ['Diagrama entidad-relación', '10.1', 'Tablas, claves y relaciones de la base de datos'],
       ['Diccionario de datos', '10.2', 'Significado de cada entidad y atributo'],
       ['Diagramas de secuencia', '10.3', 'Proceso core (compra) y de soporte (acceso al sistema)'],
       ['Diagramas de casos de uso', '10.4', 'Actores y casos de uso del proceso core y de soporte'],
       ['Patrones de desarrollo', '10.5', 'Arquitectura en capas y patrones aplicados, con capturas del código'],
       ['Cronograma', '10.6', 'Actividades y sprints del proyecto'],
       ['Landing, storyboard y MVP', 'Anexos A, B y C', 'Prototipos de la interfaz y despliegue en la nube'],
       ['Código fuente y pruebas', 'Anexo E', 'Proyecto Spring Boot con 32 pruebas automatizadas']],
      widths=[5, 2.5, 8.5])

# =====================================================================
# 7. INTERFACES
# =====================================================================
h1('7. Interfaces')
par('El sistema cuenta con dos grandes bloques de interfaces: las orientadas al Cliente (autenticación, cartelera, checkout/compra con generación de ticket y número de turno, mis tickets, y las páginas estáticas de Publicidad y Contacto) y las orientadas al Administrador (panel administrativo de gestión de catálogo y dashboard de métricas). No existe una interfaz de selección de asientos, ya que la elección del asiento ocurre físicamente al ingresar a la sala, según el turno indicado en el ticket.')
sub('Especificación de interfaces')
tabla([['Interfaz', 'Ruta', 'Menú / acceso', 'Funciones y detalles'],
       ['Login / Registro', '/login', 'Botón “Iniciar sesión” del encabezado', 'Pestañas Ingresar y Crear cuenta; validación de correo y contraseña; opción “Recordarme”; “Seguir como invitado” durante una compra.'],
       ['Acceso administrativo', '/admin/login', 'Se escribe /admin (no hay enlace público)', 'Login exclusivo para cuentas ADMIN; rechaza cuentas USER.'],
       ['Cartelera', '/cartelera', 'Menú principal: Cartelera, Promociones, Mis tickets (solo USER), Contacto', 'Películas activas con género, duración, clasificación y sinopsis; funciones de hoy con sala, hora y precio; botón “Comprar entrada”.'],
       ['Checkout: cantidad', '/sala?funcion=N', 'Botón “Comprar entrada” de la cartelera', 'Resumen de la función; lista de cantidad de 1 a 10 sin superar los cupos; botón Continuar.'],
       ['Checkout: confirmar', '/confirmar-compra', 'Después de elegir la cantidad (y de ingresar o seguir como invitado)', 'Película, sala, fecha, hora, cantidad y total; botón “Confirmar compra”.'],
       ['Comprobante de invitado', '/compra?codigo=…', 'Al confirmar como invitado', 'Tickets de la compra con su turno; solo visible en la sesión que compró.'],
       ['Mis tickets', '/mis-tickets', 'Menú “Mis tickets” (USER)', 'Historial de tickets del cliente: película, sala, fecha, hora, turno, precio y estado.'],
       ['Publicidad', '/publicidad', 'Menú “Promociones”', 'Promoción de la semana, combos y próximos estrenos (estática).'],
       ['Contacto', '/contacto', 'Menú “Contacto”', 'Datos del cine y formulario de contacto (estática).'],
       ['Panel administrativo (CRUD)', '/admin', 'Menú lateral: Películas, Géneros, Salas, Funciones, Usuarios', 'Tablas de cada entidad con estado; acciones Nuevo, Editar y Desactivar; ocupación de las funciones del día.'],
       ['Dashboard', '/admin#dashboard', 'Menú lateral: Dashboard', '5 métricas puntuales y 5 gráficos de series de tiempo.']],
      widths=[3.2, 2.8, 4, 6], size=8.5)
figura('app-cartelera.png', 'Interfaz Cartelera (aplicación Spring Boot).', alto_max=14)
figura('app-login.png', 'Interfaz Login / Registro.', alto_max=11)
figura('app-sala.png', 'Interfaz Checkout: selección de la cantidad de entradas.', alto_max=11)
figura('app-confirmar.png', 'Interfaz Checkout: confirmación de la compra.', alto_max=11)
figura('app-mis-tickets.png', 'Interfaz Mis tickets con el turno de ingreso.', alto_max=14)
figura('app-admin.png', 'Panel administrativo y dashboard de métricas.', alto_max=11)
sub('Matriz de funcionalidades')
par('La matriz relaciona cada funcionalidad del sistema (filas) con la o las interfaces donde se ejecuta (columnas).')
cols = ['N°', 'Funcionalidad', 'Login/ Registro', 'Cartelera', 'Checkout', 'Mis Tickets', 'Publicidad', 'Contacto', 'Panel Admin (CRUD)', 'Dashboard']
L, C, K, M, PA, DB = 2, 3, 4, 5, 8, 9
mat = [('Registrar película', [PA]), ('Listar / consultar catálogo de películas', [C, PA]), ('Actualizar datos de película', [PA]),
       ('Desactivar película (baja lógica)', [PA]), ('Registrar género', [PA]), ('Listar géneros', [PA]), ('Actualizar género', [PA]),
       ('Desactivar género (baja lógica)', [PA]), ('Registrar sala', [PA]), ('Listar salas', [PA]), ('Actualizar sala (aforo, ubicación)', [PA]),
       ('Desactivar sala (baja lógica)', [PA]), ('Registrar función (programar horario)', [PA]), ('Listar funciones programadas', [C, PA]),
       ('Actualizar función', [PA]), ('Cancelar función', [PA]), ('Registrar usuario (alta de cuenta)', [L]), ('Listar usuarios', [PA]),
       ('Actualizar datos / rol de usuario', [PA]), ('Desactivar usuario', [PA]),
       ('Autenticar usuario (login con validación de credenciales y rol)', [L]),
       ('Procesar compra de entradas (validar aforo disponible, calcular monto, registrar la venta y generar tickets correlativos)', [K]),
       ('Consultar historial de compras del cliente', [M]), ('Generar dashboard de métricas de series de tiempo y puntuales', [DB]),
       ('Emitir ticket electrónico con número único de turno de ingreso', [K, M])]
tabla([cols] + [[str(i + 1), f] + ['X' if c in xs else '' for c in range(2, 10)] for i, (f, xs) in enumerate(mat)],
      widths=[0.8, 5.6] + [1.2] * 8, size=8, center=(0, 2, 3, 4, 5, 6, 7, 8, 9))

# =====================================================================
# 8. FUNCIONALIDADES
# =====================================================================
h1('8. Funcionalidades')
par('El sistema define 25 funcionalidades, clasificadas según el criterio de graduación de complejidad del curso: 18 corresponden a operaciones CRUD sobre las entidades del catálogo (Película, Género, Sala, Función, Usuario) y 7 corresponden a flujos de negocio orientados a procesos (autenticación, compra de entradas con generación de ticket y turno, historial, dashboard y aplicación de reglas de negocio), que integran validación, lógica de servicio y persistencia en una transacción completa. Ninguna funcionalidad gestiona asientos individuales, ya que el sistema controla la venta por aforo de sala y turno de ingreso, no por butaca.')
fun = [('Registrar película', 'Administrador', 'CRUD'), ('Listar / consultar catálogo de películas', 'Cliente / Administrador', 'CRUD'),
       ('Actualizar datos de película', 'Administrador', 'CRUD'), ('Desactivar película (baja lógica)', 'Administrador', 'CRUD'),
       ('Registrar género', 'Administrador', 'CRUD'), ('Listar géneros', 'Administrador', 'CRUD'), ('Actualizar género', 'Administrador', 'CRUD'),
       ('Desactivar género (baja lógica)', 'Administrador', 'CRUD'), ('Registrar sala', 'Administrador', 'CRUD'), ('Listar salas', 'Administrador', 'CRUD'),
       ('Actualizar sala (aforo, ubicación)', 'Administrador', 'CRUD'), ('Desactivar sala (baja lógica)', 'Administrador', 'CRUD'),
       ('Registrar función (programar horario)', 'Administrador', 'CRUD'), ('Listar funciones programadas', 'Cliente / Administrador', 'CRUD'),
       ('Actualizar función', 'Administrador', 'CRUD'), ('Cancelar función', 'Administrador', 'CRUD'),
       ('Gestionar el registro y las cuentas de los clientes, incluyendo el registro, actualización y desactivación de usuarios.', 'Cliente / Administrador', 'CRUD'),
       ('Consultar y administrar los usuarios registrados para mantener actualizada la información de las cuentas.', 'Administrador', 'CRUD'),
       ('Validar las credenciales de acceso y los roles de los usuarios para permitir el ingreso según sus permisos.', 'Cliente / Administrador', 'Proceso'),
       ('Gestionar la venta de entradas, validando la disponibilidad de cupos, calculando el importe y registrando la compra.', 'Cliente / Sistema', 'Proceso'),
       ('Generar tickets electrónicos con numeración única para identificar las entradas adquiridas por los clientes.', 'Sistema', 'Proceso'),
       ('Consultar el historial de compras y los tickets adquiridos por el cliente.', 'Cliente', 'Proceso'),
       ('Controlar la disponibilidad de entradas por función, evitando que las ventas superen el aforo de la sala.', 'Sistema', 'Proceso'),
       ('Generar dashboard de métricas de series de tiempo y puntuales', 'Administrador', 'Proceso'),
       ('Emitir ticket electrónico con número único de turno de ingreso', 'Sistema', 'Proceso')]
tabla([['N°', 'Funcionalidad', 'Actor', 'Nivel']] + [[str(i + 1), a, b, c] for i, (a, b, c) in enumerate(fun)],
      widths=[1, 9.5, 3.8, 1.7], center=(0, 3))

# =====================================================================
# 9. REGLAS DE NEGOCIO (6 elementos por regla)
# =====================================================================
h1('9. Reglas de Negocio')
par('Las reglas de negocio definen las condiciones que el sistema debe cumplir al registrar, validar y procesar la información de Cine Amazonas. Son atómicas, declarativas, estables y obligatorias. Se resumen en la tabla siguiente y luego se detalla cada una con su nombre, enunciado formal, justificación de negocio, impacto en el sistema, validación técnica y caso de prueba.')
RN = [
    ('Acceso solo con cuenta activa', 'Acceso',
     'Solo pueden acceder al sistema las cuentas activas con correo y contraseña válidos; las cuentas desactivadas no pueden iniciar sesión.',
     'Impide que clientes o empleados dados de baja sigan operando o consultando información del cine.',
     'Login de clientes y acceso administrativo (funcionalidades 19 y 21).',
     'AuthService.autenticar consulta solo usuarios con activo = TRUE (UsuarioRepository.buscarActivoPorCorreo).',
     'Cumple: cliente@cineamazonas.pe, activo, con contraseña correcta, ingresa a la cartelera. Viola: la misma cuenta con activo = FALSE recibe “Correo o contraseña incorrectos”.'),
    ('Separación de roles', 'Acceso',
     'El sistema maneja dos roles, USER (cliente) y ADMIN (administrador). Cada acceso rechaza las cuentas del otro rol y el panel administrativo exige una sesión ADMIN.',
     'Evita que un cliente modifique el catálogo o vea información comercial reservada.',
     'Login, acceso administrativo, panel y dashboard (funcionalidades 1 a 18 y 24).',
     'AuthController compara el rol de la cuenta con el acceso usado (/login USER, /admin/login ADMIN) y responde 403; AdminController verifica el rol ADMIN en la sesión.',
     'Cumple: admin@cineamazonas.pe en /admin/login entra al panel. Viola: la misma cuenta en /login recibe “Esta cuenta no corresponde a este acceso”.'),
    ('Registro público de clientes', 'Acceso',
     'El registro público de usuarios siempre crea una cuenta con rol USER. El correo debe tener formato válido y ser único; la contraseña debe tener entre 8 y 128 caracteres.',
     'Nadie puede autoasignarse permisos de administrador y cada cliente se identifica por un único correo.',
     'Registro de usuarios (funcionalidad 17).',
     'AuthService.validarRegistro valida formato y longitud; el INSERT fija rol = \'USER\'; la columna correo es UNIQUE.',
     'Cumple: nuevo@correo.com con clave de 10 caracteres crea una cuenta USER. Viola: un correo ya registrado o una clave de 5 caracteres muestran el error del campo.'),
    ('Protección de contraseñas', 'Seguridad',
     'Las contraseñas de los usuarios nunca se almacenan ni se muestran en texto legible.',
     'Protege a los clientes si la base de datos se expone y cumple buenas prácticas de protección de datos personales.',
     'Registro y login (funcionalidades 17, 19 y 21).',
     'AuthService guarda “sal:resumen” calculado con PBKDF2WithHmacSHA256 y una sal aleatoria por usuario; al ingresar recalcula y compara.',
     'Cumple: la columna clave de cliente@cineamazonas.pe contiene un resumen hexadecimal. Viola: buscar el texto “Cliente123!” en la tabla usuarios no devuelve resultados.'),
    ('Bajas lógicas del catálogo', 'Catálogo',
     'Solo se muestran en la cartelera las películas, géneros y salas activos. Las bajas son lógicas (campo activo): no se borra el historial.',
     'Conserva el historial de ventas y tickets aunque una película o sala deje de ofrecerse.',
     'Cartelera, panel administrativo y desactivaciones (funcionalidades 2, 4, 8, 12 y 20).',
     'Todas las tablas tienen el campo activo; las consultas de cartelera filtran activo = TRUE; no se ejecutan DELETE sobre el catálogo.',
     'Cumple: al desactivar una película deja de verse en la cartelera y sus tickets siguen en Mis tickets. Viola: borrar físicamente una película con funciones falla por la clave foránea.'),
    ('Película correctamente clasificada', 'Catálogo',
     'Cada película pertenece a un género, tiene duración mayor a cero y una clasificación (por ejemplo ATP, +14).',
     'El cliente necesita saber el tipo de película, su duración y si es apta para su edad.',
     'Registro y actualización de películas (funcionalidades 1 y 3); cartelera.',
     'peliculas.genero_id NOT NULL con clave foránea a generos; CHECK (minutos > 0); clasificacion NOT NULL.',
     'Cumple: “Spider-Man”, Acción, 148 min, +14 se registra. Viola: una película con 0 minutos es rechazada por la restricción CHECK.'),
    ('Función completa y con precio válido', 'Funciones',
     'Cada función se programa para una película, una sala, una fecha y una hora, con un precio mayor o igual a cero.',
     'Sin estos datos no se puede vender ni controlar el aforo de una función.',
     'Programación de funciones (funcionalidades 13 a 16); cartelera y compra.',
     'funciones.pelicula_id y sala_id NOT NULL con claves foráneas; fecha y hora NOT NULL; CHECK (precio >= 0).',
     'Cumple: Spider-Man, Sala 3, hoy 8:00 PM, S/ 15.00. Viola: una función con precio −5 es rechazada.'),
    ('Venta limitada al aforo', 'Ventas',
     'La venta de entradas de una función no puede superar el aforo de su sala. El sistema vuelve a comprobar la disponibilidad al confirmar la compra.',
     'Evita la sobreventa: ningún cliente llega a una sala llena con una entrada pagada.',
     'Compra de entradas y control de disponibilidad (funcionalidades 20 y 23).',
     'CompraService.validarCantidad compara con aforo − ocupación; al confirmar, la función se bloquea con SELECT … FOR UPDATE dentro de la transacción.',
     'Cumple: Sala 2 (aforo 65) con 60 entradas vendidas acepta una compra de 5. Viola: una compra de 6 muestra “No hay suficientes entradas disponibles para esta función”.'),
    ('Importe calculado por el sistema', 'Ventas',
     'El importe de la compra se calcula como el precio de la función multiplicado por la cantidad de entradas.',
     'Garantiza cobros correctos y evita que el cliente altere el precio.',
     'Confirmación y registro de la compra (funcionalidad 20).',
     'CompraService.calcularTotal usa el precio leído de la base de datos, no un valor enviado por el formulario.',
     'Cumple: 3 entradas de S/ 12.00 suman S/ 36.00. Viola: modificar el formulario para enviar otro precio no cambia el total registrado.'),
    ('Turno de ingreso único', 'Tickets',
     'Cada ticket recibe un número correlativo único dentro de su función (turno de ingreso). No pueden existir dos tickets con el mismo número en una misma función.',
     'Ordena el ingreso a la sala, ya que no se venden butacas numeradas.',
     'Generación y emisión de tickets (funcionalidades 21 y 25).',
     'El turno se calcula como último turno + 1 con la función bloqueada; índice único (funcion_id, turno) en la tabla tickets.',
     'Cumple: una compra de 2 y luego una de 3 reciben los turnos 1-2 y 3-5. Viola: insertar otro ticket con el turno 3 en la misma función lanza error de clave duplicada.'),
    ('Compra atómica y sin duplicados', 'Ventas',
     'La compra se registra en una sola transacción: se guardan todas las entradas o ninguna. Reenviar el mismo formulario no duplica entradas.',
     'Evita cobros o tickets parciales y compras repetidas por un doble clic o una recarga de página.',
     'Registro de la compra y emisión de tickets (funcionalidades 20, 21 y 25).',
     '@Transactional en CompraService.registrarCompra; cada compra tiene un código único (clave primaria de compras) y se verifica con compraExiste antes de registrar.',
     'Cumple: reenviar el formulario de una compra ya registrada muestra las mismas entradas. Viola: si falla la inserción de la tercera entrada, no queda guardada ninguna.'),
    ('Privacidad de los tickets', 'Ventas',
     'Un cliente con sesión consulta únicamente sus propias entradas en Mis tickets. Un cliente sin sesión debe ingresar o continuar como invitado para confirmar la compra.',
     'Protege la información de compra de cada cliente.',
     'Mis tickets y confirmación de compra (funcionalidades 20 y 22).',
     'NavegacionController exige rol USER y filtra por el usuarioId de la sesión; CarteleraController redirige a /login si no hay sesión ni modo invitado.',
     'Cumple: el cliente demo ve solo sus tickets. Viola: abrir /mis-tickets sin sesión redirige al login.'),
    ('Compra de invitado sin cuenta', 'Ventas',
     'La compra de un invitado no crea usuario: el comprobante solo es accesible desde la sesión del navegador donde se hizo la compra.',
     'Permite comprar rápido sin registrarse, sin exponer el comprobante a terceros.',
     'Compra como invitado y comprobante (funcionalidades 20 y 25).',
     'La compra se guarda con usuario_id nulo y un identificador aleatorio de invitado; TicketService.comprobanteInvitado compara ese identificador con el de la sesión.',
     'Cumple: el mismo navegador ve su comprobante. Viola: otro navegador que abre el mismo código no accede al comprobante.'),
    ('Administración exclusiva', 'Administración',
     'El dashboard y la gestión del catálogo son exclusivos del administrador.',
     'La información comercial y la configuración del cine solo deben estar en manos del responsable del negocio.',
     'Panel administrativo y dashboard (funcionalidades 1 a 18 y 24).',
     'AdminController redirige a /admin/login si la sesión no tiene rol ADMIN; no hay enlaces administrativos en las pantallas públicas.',
     'Cumple: el administrador ve el dashboard. Viola: un cliente USER que escribe /admin es enviado al acceso administrativo.'),
]
tabla([['Código', 'Nombre de la regla', 'Área']] + [['RN-%02d' % (i + 1), r[0], r[1]] for i, r in enumerate(RN)],
      widths=[2, 10.5, 3.5], center=(0,))
for i, (nombre, area, enun, just, imp, val, caso) in enumerate(RN):
    t = tabla([['RN-%02d · %s' % (i + 1, nombre), ''],
               ['Enunciado formal', enun], ['Justificación de negocio', just], ['Impacto en el sistema', imp],
               ['Validación técnica', val], ['Caso de prueba', caso]], widths=[4, 12])
    t.cell(0, 0).merge(t.cell(0, 1))
    for fila in t.rows[1:]:
        for r in fila.cells[0].paragraphs[0].runs:
            r.bold = True

# =====================================================================
# 10. PRODUCTOS Y ENTREGABLES
# =====================================================================
h1('10. Productos y entregables')
par('Los diagramas se elaboraron con PlantUML, una herramienta basada en código. Sus fuentes están en la carpeta `docs/diagramas` del repositorio del proyecto.')

h2('10.1 Diagrama entidad-relación')
figura('er.png', 'Diagrama entidad-relación de TicketCine (fuente: docs/diagramas/er.puml).', alto_max=19)
par('Un género clasifica muchas películas; una película se proyecta en muchas funciones y una sala alberga muchas funciones. Cada función registra muchas compras y emite muchos tickets; cada compra contiene uno o más tickets. Un usuario puede realizar compras y poseer tickets, pero esa relación es opcional porque las compras de invitado no tienen usuario.')

h2('10.2 Diccionario de datos')
DIC = [
    ('usuarios', 'Personas que acceden al sistema: clientes (USER) y administradores (ADMIN).', [
        ('id', 'BIGINT', 'PK, autoincremental', 'Identificador del usuario.'),
        ('nombre', 'VARCHAR(100)', 'NOT NULL', 'Nombre completo.'),
        ('correo', 'VARCHAR(254)', 'NOT NULL, UNIQUE', 'Correo con el que inicia sesión.'),
        ('clave', 'VARCHAR(200)', 'NOT NULL', 'Sal y resumen PBKDF2 de la contraseña.'),
        ('rol', 'VARCHAR(10)', 'NOT NULL, ADMIN o USER', 'Rol que define los permisos.'),
        ('activo', 'BOOLEAN', 'NOT NULL, por defecto TRUE', 'Indica si la cuenta puede usarse (baja lógica).')]),
    ('generos', 'Categorías del catálogo de películas.', [
        ('id', 'INT', 'PK', 'Identificador del género.'),
        ('nombre', 'VARCHAR(80)', 'NOT NULL, UNIQUE', 'Nombre del género (Acción, Animación…).'),
        ('activo', 'BOOLEAN', 'NOT NULL, por defecto TRUE', 'Baja lógica del género.')]),
    ('peliculas', 'Productos del catálogo: películas que el cine exhibe.', [
        ('id', 'INT', 'PK', 'Identificador de la película.'),
        ('nombre', 'VARCHAR(150)', 'NOT NULL', 'Título de la película.'),
        ('genero_id', 'INT', 'NOT NULL, FK → generos', 'Género al que pertenece.'),
        ('minutos', 'INT', 'NOT NULL, > 0', 'Duración en minutos.'),
        ('clasificacion', 'VARCHAR(10)', 'NOT NULL', 'Clasificación por edad (ATP, +14…).'),
        ('sinopsis', 'VARCHAR(500)', 'NOT NULL', 'Resumen que se muestra en la cartelera.'),
        ('estilo', 'VARCHAR(30)', 'NOT NULL', 'Estilo visual del afiche en la cartelera.'),
        ('poster', 'VARCHAR(80)', 'NOT NULL', 'Texto principal del afiche.'),
        ('subtitulo', 'VARCHAR(80)', 'NOT NULL', 'Texto secundario del afiche.'),
        ('activo', 'BOOLEAN', 'NOT NULL, por defecto TRUE', 'Baja lógica de la película.')]),
    ('salas', 'Salas de proyección del cine.', [
        ('id', 'INT', 'PK', 'Identificador de la sala.'),
        ('nombre', 'VARCHAR(40)', 'NOT NULL', 'Nombre de la sala.'),
        ('aforo', 'INT', 'NOT NULL, > 0', 'Capacidad máxima de personas.'),
        ('activo', 'BOOLEAN', 'NOT NULL, por defecto TRUE', 'Baja lógica de la sala.')]),
    ('funciones', 'Proyección de una película en una sala, fecha y hora.', [
        ('id', 'INT', 'PK', 'Identificador de la función.'),
        ('pelicula_id', 'INT', 'NOT NULL, FK → peliculas', 'Película que se proyecta.'),
        ('sala_id', 'INT', 'NOT NULL, FK → salas', 'Sala donde se proyecta.'),
        ('fecha', 'DATE', 'NOT NULL', 'Fecha de la función.'),
        ('hora', 'TIME', 'NOT NULL', 'Hora de inicio.'),
        ('precio', 'DECIMAL(10,2)', 'NOT NULL, >= 0', 'Precio por entrada en soles.'),
        ('activo', 'BOOLEAN', 'NOT NULL, por defecto TRUE', 'Indica si la función está vigente o cancelada.')]),
    ('compras', 'Cabecera de una venta de entradas.', [
        ('id', 'VARCHAR(36)', 'PK', 'Código único de la compra (UUID).'),
        ('usuario_id', 'BIGINT', 'FK → usuarios, nulo para invitado', 'Cliente que compró.'),
        ('funcion_id', 'INT', 'NOT NULL, FK → funciones', 'Función comprada.'),
        ('cantidad', 'INT', 'NOT NULL, > 0', 'Número de entradas.'),
        ('total', 'DECIMAL(10,2)', 'NOT NULL, >= 0', 'Importe: precio × cantidad.'),
        ('fecha', 'TIMESTAMP', 'NOT NULL, por defecto ahora', 'Fecha y hora de la compra.'),
        ('invitado', 'VARCHAR(36)', 'Opcional', 'Identificador de la sesión de invitado que compró.')]),
    ('tickets', 'Detalle de la venta: una entrada electrónica por persona.', [
        ('id', 'BIGINT', 'PK, autoincremental', 'Número del ticket.'),
        ('usuario_id', 'BIGINT', 'FK → usuarios, opcional', 'Dueño del ticket (nulo si es de invitado).'),
        ('funcion_id', 'INT', 'NOT NULL, FK → funciones', 'Función a la que da acceso.'),
        ('compra_id', 'VARCHAR(36)', 'FK → compras', 'Compra a la que pertenece.'),
        ('turno', 'INT', 'NOT NULL, > 0, UNIQUE con funcion_id', 'Turno de ingreso a la sala.'),
        ('precio', 'DECIMAL(10,2)', 'NOT NULL, >= 0', 'Precio pagado por la entrada.'),
        ('fecha_compra', 'DATE', 'NOT NULL', 'Fecha de la venta.'),
        ('activo', 'BOOLEAN', 'NOT NULL, por defecto TRUE', 'Indica si el ticket es válido.')]),
]
for nombre, desc, campos in DIC:
    sub('Tabla %s' % nombre)
    par(desc)
    tabla([['Atributo', 'Tipo', 'Restricciones', 'Descripción']] + [list(c) for c in campos],
          widths=[3, 2.8, 4.4, 5.8], size=8.5)

h2('10.3 Diagramas de secuencia')
sub('Proceso core: confirmar la compra de entradas')
figura('sec-core.png', 'Diagrama de secuencia del proceso core (fuente: docs/diagramas/sec-core.puml).')
par('El controlador valida el formulario y la sesión, y delega en CompraService. Dentro de una transacción, el servicio bloquea la función, cuenta las entradas vendidas, valida el aforo, calcula el total y registra la compra con sus tickets y turnos. Si no hay cupos, la transacción se deshace y el cliente vuelve a la pantalla de cantidad con un mensaje.')
sub('Proceso de soporte: iniciar sesión con validación de credenciales y rol')
figura('sec-soporte.png', 'Diagrama de secuencia del proceso de soporte (fuente: docs/diagramas/sec-soporte.puml).')
par('AuthController valida el token del formulario y el formato de los datos; AuthService busca la cuenta activa y compara el resumen PBKDF2. Si el rol coincide con el acceso usado, se renueva la sesión y se redirige al destino; si no, se rechaza el ingreso.')

h2('10.4 Diagramas de casos de uso')
sub('Proceso core: compra de entradas')
figura('cu-core.png', 'Casos de uso del proceso core (fuente: docs/diagramas/cu-core.puml).', ANCHO, 15)
par('El Cliente consulta la cartelera, selecciona la función y la cantidad, confirma la compra y consulta sus tickets. Confirmar la compra incluye validar el aforo, calcular el importe y generar los tickets; el inicio de sesión o el modo invitado la extienden. El Invitado es un cliente sin cuenta que solo ve el comprobante de su compra.')
sub('Proceso de soporte: acceso y gestión del catálogo')
figura('cu-soporte.png', 'Casos de uso de los procesos de soporte (fuente: docs/diagramas/cu-soporte.puml).', ANCHO, 15)
par('El Administrador gestiona películas, géneros, salas, funciones y usuarios y consulta el dashboard, siempre después de iniciar sesión. La desactivación (baja lógica) extiende la gestión de cada entidad. El Cliente puede registrarse, iniciar y cerrar sesión.')

h2('10.5 Patrones de desarrollo')
par('El sistema sigue una arquitectura en capas sobre el patrón MVC de Spring: cada capa solo se comunica con la siguiente (Controller → Service → Repository → H2).')
figura('diagrama-arquitectura.png', 'Arquitectura en capas de TicketCine.', alto_max=13)
PAT = [
    ('Modelo-Vista-Controlador (MVC) y Front Controller', 'patron-mvc.png',
     'El DispatcherServlet de Spring recibe todas las peticiones (Front Controller) y las envía al controlador anotado con @Controller. El controlador obtiene los datos del servicio, los coloca en el Model y devuelve el nombre de la vista; la JSP solo muestra los datos con JSTL.'),
    ('Inyección de dependencias por constructor', 'patron-di.png',
     'Las clases no crean sus dependencias: Spring las entrega en el constructor. CompraService recibe los repositorios que necesita, lo que reduce el acoplamiento y permite probar cada clase por separado.'),
    ('Capa de servicio (Service Layer) y transacción', 'patron-service.png',
     'Las reglas de negocio (aforo, total, turnos) están en el servicio, no en el controlador ni en la vista. @Transactional garantiza que la compra y todos sus tickets se guarden juntos o no se guarde nada (RN-11).'),
    ('Repositorio (Repository)', 'patron-repository.png',
     'Los repositorios anotados con @Repository son el único lugar con SQL. Usan JdbcTemplate con parámetros (?) para evitar la inyección SQL; la consulta FOR UPDATE bloquea la función mientras se cuenta la ocupación (RN-08).'),
    ('Post/Redirect/Get (PRG)', 'patron-prg.png',
     'Después de un POST exitoso el controlador responde con un redirect a una página GET. Así, recargar la página no reenvía la compra, y la verificación compraExiste evita duplicados si el formulario se envía dos veces (RN-11).'),
    ('Token sincronizador (protección CSRF)', 'patron-csrf.png',
     'Cada formulario incluye un token oculto guardado en la sesión. El controlador rechaza con 403 cualquier envío cuyo token no coincida, lo que impide que otro sitio envíe formularios en nombre del usuario.'),
]
for i, (nombre, img, texto) in enumerate(PAT, 1):
    sub('%d) %s' % (i, nombre))
    par(texto)
    figura(img, 'Patrón %s: captura del código.' % nombre, alto_max=12)

d.add_page_break()
h2('10.6 Cronograma del proyecto')
par('El proyecto se organizó con Scrum y prototipado incremental, en sprints de dos semanas a lo largo de las 16 semanas del semestre 2026-20. Al final de cada sprint se presentó un incremento al docente.')
ACT = [('Sprint 0', 'Diagnóstico, objetivos, requisitos y reglas de negocio', 1, 2),
       ('Sprint 1', 'Storyboard, landing page y MVP en HTML/CSS; despliegue en Vercel', 3, 4),
       ('Sprint 2', 'Modelo ER, diccionario de datos y arquitectura; proyecto Spring Boot base', 5, 6),
       ('Sprint 3', 'Login, registro, roles y protección de contraseñas', 7, 8),
       ('Sprint 4', 'Cartelera y compra de entradas con aforo, turnos e invitado', 9, 10),
       ('Sprint 5', 'Panel administrativo (CRUD) y dashboard de métricas', 11, 12),
       ('Sprint 6', 'Pruebas automatizadas, diagramas de secuencia y casos de uso', 13, 14),
       ('Cierre', 'Informe final, correcciones y exposición del portal', 15, 16)]
filas = [['Sprint', 'Actividades'] + [str(s) for s in range(1, 17)]]
for sp, act, a, b in ACT:
    filas.append([sp, act] + ['' for _ in range(16)])
t = tabla(filas, widths=[1.5, 5.3] + [0.575] * 16, size=7, center=tuple(range(2, 18)))
mar = OxmlElement('w:tblCellMar')
for lado in ('left', 'right'):
    e = OxmlElement('w:' + lado); e.set(qn('w:w'), '28'); e.set(qn('w:type'), 'dxa'); mar.append(e)
t._tbl.tblPr.append(mar)
for fila in t.rows:
    trPr = fila._tr.get_or_add_trPr()
    e = OxmlElement('w:cantSplit'); trPr.append(e)
for ri, (sp, act, a, b) in enumerate(ACT, 1):
    for s in range(a, b + 1):
        shade(t.cell(ri, s + 1), 'E8A33D')
par('*Las columnas 1 a 16 corresponden a las semanas del semestre.*', size=9)
tabla([['Rol Scrum', 'Responsable'],
       ['Product Owner (coordinador)', 'Pacheco Gaspar Jean Brandon'],
       ['Scrum Master', 'Arroyo Tello Geraldo Gerson'],
       ['Equipo de desarrollo', 'Quispe Meza Renzo, Zamudio Benito Dayaneira Judith y Pacheco Gaspar Jean Brandon']],
      widths=[5, 11])
par('**Eventos y artefactos.** Sprint Planning al inicio de cada sprint, reunión diaria breve, Sprint Review con demostración al docente y retrospectiva. El Product Backlog contiene las 25 funcionalidades como historias de usuario y el avance se controla en un tablero Kanban (Por hacer / En curso / Hecho). Una historia se considera terminada cuando funciona en la aplicación, cumple sus reglas de negocio, tiene pruebas que pasan y está en el repositorio.')

# =====================================================================
# 11. CONCLUSIONES (máximo 3)
# =====================================================================
h1('11. Conclusiones')
vineta('El diagnóstico confirmó una oportunidad de mejora real para Cine Amazonas: un público joven y digital, un sector cinematográfico en crecimiento y un comercio electrónico que ya usa más de la mitad de los peruanos hacen pertinente reemplazar la venta exclusivamente presencial por un canal web que reduzca colas y papel.', num=True)
vineta('Vender por aforo y turno de ingreso, en lugar de butacas numeradas, simplificó la compra a tres pasos sin perder el control de la capacidad: las reglas de negocio de aforo, importe, turno único y compra atómica se cumplen en el servidor y se comprueban con 32 pruebas automatizadas.', num=True)
vineta('La arquitectura en capas de Spring Boot (Controller → Service → Repository) y los patrones aplicados permiten mantener y ampliar el sistema; junto con el dashboard, TicketCine da al propietario información sobre ventas y ocupación que antes no tenía de forma centralizada, lo que apoya la toma de decisiones del negocio.', num=True)

# =====================================================================
# 12. RECOMENDACIONES (máximo 3)
# =====================================================================
h1('12. Recomendaciones')
vineta('Validar el flujo de compra con usuarios reales mediante storyboard y prototipos (landing y MVP) antes de programar el backend, y definir desde el inicio las reglas de negocio con sus casos de prueba.', num=True)
vineta('Controlar la capacidad en el servidor dentro de una transacción, con bloqueo de la función y restricciones únicas en la base de datos; nunca confiar en el precio o la cantidad enviados por el navegador.', num=True)
vineta('Antes de operar en producción, integrar una pasarela de pago autorizada, desplegar la aplicación en un servidor en la nube con una base de datos administrada y conectar los indicadores del dashboard con los datos reales de ventas.', num=True)

# =====================================================================
# 13. GLOSARIO
# =====================================================================
h1('13. Glosario')
GLO = [('Aforo', 'Capacidad máxima de personas de una sala.'),
       ('Baja lógica', 'Desactivar un registro con un campo “activo” en lugar de borrarlo, para conservar el historial.'),
       ('Checkout', 'Proceso de compra: elegir la cantidad, revisar el resumen y confirmar.'),
       ('Controller', 'Clase que recibe las peticiones HTTP y decide qué vista mostrar.'),
       ('CRUD', 'Operaciones básicas sobre datos: crear, leer, actualizar y eliminar (o desactivar).'),
       ('CSRF', 'Ataque en el que otro sitio envía formularios en nombre del usuario; se evita con un token por sesión.'),
       ('Dashboard', 'Panel con indicadores para el seguimiento del negocio.'),
       ('H2', 'Base de datos relacional escrita en Java que se ejecuta dentro de la aplicación.'),
       ('JdbcTemplate', 'Clase de Spring que ejecuta consultas SQL de forma segura y sencilla.'),
       ('JSP / JSTL', 'Tecnología de Java para generar páginas HTML en el servidor; JSTL añade etiquetas para bucles y condiciones.'),
       ('Landing page', 'Página de entrada que presenta el servicio y lleva al usuario a la acción principal.'),
       ('MVP', 'Producto mínimo viable: versión reducida que permite validar una idea con usuarios.'),
       ('PBKDF2', 'Algoritmo que transforma una contraseña en un resumen difícil de revertir, usando una sal.'),
       ('PlantUML', 'Herramienta que genera diagramas UML a partir de texto (código).'),
       ('Repository', 'Clase que concentra el acceso a la base de datos.'),
       ('Scrum', 'Marco ágil que organiza el trabajo en iteraciones cortas llamadas sprints.'),
       ('Service', 'Clase que contiene las reglas de negocio de la aplicación.'),
       ('Spring Boot', 'Framework de Java para crear aplicaciones web con configuración mínima.'),
       ('Storyboard', 'Secuencia de viñetas que muestra cómo un usuario usa el sistema en un escenario.'),
       ('Transacción', 'Conjunto de operaciones en la base de datos que se ejecutan todas o ninguna.'),
       ('Turno de ingreso', 'Número correlativo del ticket que indica el orden de entrada a la sala.'),
       ('Vercel', 'Servicio en la nube para publicar sitios web estáticos.')]
tabla([['Término', 'Definición']] + [list(g) for g in GLO], widths=[3.5, 12.5])

# =====================================================================
# 14. BIBLIOGRAFÍA (APA 7)
# =====================================================================
h1('14. Bibliografía')
BIB = [
    'Cervantes Maceda, H., Velasco-Elizondo, P. y Castro Careaga, L. (2016). *Arquitectura de software: conceptos y ciclo de desarrollo*. Cengage Learning.',
    'Coronel, C., Morris, S. y Rob, P. (2011). *Bases de datos: diseño, implementación y administración* (9.ª ed.). Cengage Learning.',
    'Forbes Perú. (2025, 30 de mayo). Comercio electrónico creció 21,2% durante 2024, según Capece. https://forbes.pe/economia-y-finanzas/2025-05-30/comercio-electronico-crecio-212-durante-2024-segun-capece',
    'Fowler, M. (2002). *Patterns of enterprise application architecture*. Addison-Wesley.',
    'Gestión. (2021, 20 de diciembre). Industria peruana de cine crecerá 42% hasta el 2025, según PwC. https://gestion.pe/economia/empresas/industria-peruana-de-cine-crecera-42-hasta-el-2025-segun-pwc-noticia/',
    'Gestión. (2025). Cines en Perú: ¿qué películas impulsarán un 2025 con el mayor crecimiento en los últimos 5 años? https://gestion.pe/economia/empresas/cines-en-peru-por-que-el-2025-sera-el-de-mayor-crecimiento-en-los-ultimos-5-anos-industria-cinematografica-peliculas-indecopi-noticia/',
    'H2 Database Engine. (s.f.). *Documentation*. https://h2database.com/html/main.html',
    'La República. (2022, 31 de mayo). Sunat: desde el 1 de junio todas las empresas deberán emitir comprobantes electrónicos. https://larepublica.pe/economia/2022/05/31/sunat-desde-el-1-junio-todas-las-empresas-deberan-emitir-comprobantes-electronicos-factura-electronica-boleta-electronica-mypes',
    'Ministerio de Cultura del Perú. (2023). *Indicadores de asistencia a funciones de cine*. Infoartes. https://infoartes.pe/posts/indicadores-de-asistencia-funciones-de-cine',
    'OWASP Foundation. (s.f.). *Cross-Site Request Forgery prevention cheat sheet*. https://cheatsheetseries.owasp.org/cheatsheets/Cross-Site_Request_Forgery_Prevention_Cheat_Sheet.html',
    'PlantUML. (s.f.). *PlantUML: diagramas UML a partir de texto*. https://plantuml.com/es/',
    'Schwaber, K. y Sutherland, J. (2020). *La Guía Scrum*. https://scrumguides.org',
    'Spring. (s.f.). *Spring Boot reference documentation 3.5*. https://docs.spring.io/spring-boot/3.5/',
    'SUNAT. (s.f.). *Comprobantes de pago electrónicos: conceptos generales y beneficios del sistema*. https://cpe.sunat.gob.pe/facturacion-mype',
]
for b in BIB:
    p = par(b, just=False)
    p.paragraph_format.left_indent = Cm(1.25)
    p.paragraph_format.first_line_indent = Cm(-1.25)

# =====================================================================
# 15. ANEXOS
# =====================================================================
h1('15. Anexos')
h2('Anexo A. Landing page (HTML/CSS) y su utilidad')
bloque_md('## 10.', '## 11.')
h2('Anexo B. Storyboard de la funcionalidad clave')
bloque_md('## 11.', '## 12.')
h2('Anexo C. MVP (HTML + CSS) y despliegue en la nube')
bloque_md('## 12.', '**Capturas de la aplicación completa')
h2('Anexo D. Técnicas de ingeniería web aplicadas')
bloque_md('### 14.1', '### 14.2')
h2('Anexo E. Ejecución del sistema y cuentas de demostración')
par('Requisito: JDK 21 o superior. Desde la carpeta del proyecto se compila con `./mvnw clean package` (Windows: `mvnw.cmd clean package`) y se ejecuta con `java -jar target/cine-amazonas.war`; luego se abre http://localhost:8080. Las pruebas automatizadas se ejecutan con `./mvnw test` (32 pruebas).')
tabla([['Rol', 'Correo', 'Contraseña', 'Acceso'],
       ['ADMIN', 'admin@cineamazonas.pe', 'Admin123!', '/admin'],
       ['USER', 'cliente@cineamazonas.pe', 'Cliente123!', '/login']], widths=[2, 6, 3.5, 4.5])

# ---------- Número de página en el pie ----------
for s in d.sections:
    p = s.footer.paragraphs[0]
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    r = p.add_run()
    r.font.size = Pt(9)
    for tipo, valor in [('begin', None), ('instr', ' PAGE '), ('end', None)]:
        if tipo == 'instr':
            e = OxmlElement('w:instrText'); e.set(qn('xml:space'), 'preserve'); e.text = valor
        else:
            e = OxmlElement('w:fldChar'); e.set(qn('w:fldCharType'), tipo)
        r._r.append(e)
    s.different_first_page_header_footer = True

escribir_notas()
d.save(str(D / 'Ingenieria_Web_TicketCine.docx'))
print('Figuras:', FIG[0], '| Notas al pie:', len(NOTAS))
