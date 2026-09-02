# ISIVI - EVALUACIÓN DE INCOHERENCIAS Y COMPORTAMIENTOS VERIFICADOS (SMART INCONSISTENCIES)

**Fecha**: 22 de Agosto, 2026

---

## 1. COMPORTAMIENTOS REVISADOS Y CONFIRMADOS COMO DISEÑO CORRECTO

Durante la Auditoría Inteligente, se analizaron diversos comportamientos del sistema para determinar si constituían incoherencias o si respondían a reglas de negocio intencionales:

### 1.1 Pago del 25% de Anticipo para Citas de Peluquería
- **Incoherencia sospechada**: ¿Por qué no se cobra el 100% de la cita al agendar?
- **Resultado del Análisis**: **NO ES ERROR**.
- **Justificación de Negocio**: ISIVI implementa un modelo de salón de belleza real donde el cliente reserva un turno pagando el **25% de anticipo** para garantizar el espacio del profesional, y el **75% restante** se paga presencialmente en el salón tras recibir el tratamiento.

### 1.2 Ausencia de Fecha/Hora para Pedidos Puros de Productos/Kits
- **Incoherencia sospechada**: ¿Por qué el checkout de un kit o producto no pide seleccionar una fecha en el calendario?
- **Resultado del Análisis**: **NO ES ERROR**.
- **Justificación de Negocio**: Los pedidos de productos no ocupan la agenda física de peluquería. Requieren método de entrega (`delivery` a domicilio o `pickup` en salón), pero no consumen turnos del profesional.

### 1.3 Bloqueo de Pagos Combinados (Servicio + Producto en un solo pago Wompi)
- **Incoherencia sospechada**: ¿Por qué `ReservaService.prepararPagoWompi` lanza una excepción si se intenta pagar una reserva que mezcla servicios y productos?
- **Resultado del Análisis**: **NO ES ERROR**.
- **Justificación de Negocio**: Las citas manejan retención temporal de horario (15 min) y anticipo parcial (25%), mientras que los productos manejan reserva de inventario físico y cobro al 100%. Para evitar contingencias contables y de expiración de agenda, la regla de negocio exige separar el pago Wompi de servicios y de productos.

---

## 2. COMPORTAMIENTO DE OTRAS TABLAS ADMINISTRATIVAS

- **Servicios de Peluquería**: 5 columnas `<th>` equivalentes a 5 celdas `<td>`. (Alineación 100% correcta).
- **Agenda de Citas**: 6 columnas `<th>` equivalentes a 6 celdas `<td>`. (Alineación 100% correcta).
- **Gestión de Pedidos**: 6 columnas `<th>` equivalentes a 6 celdas `<td>`. (Alineación 100% correcta).
- **Histórico de Solicitudes**: 7 columnas `<th>` equivalentes a 7 celdas `<td>`. (Alineación 100% correcta).

---

*Documento producido automáticamente durante la Auditoría Inteligente ISIVI.*
