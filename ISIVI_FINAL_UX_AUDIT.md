# AUDITORÍA FINAL DE EXPERIENCIA DE USUARIO (UX/UI) — ISIVI

**Fecha:** 21 de Agosto de 2026  
**Enfoque:** Usabilidad, Claridad Comercial, Feedback al Usuario y Experiencia en Roles Cliente y Administrador  

---

## 1. Experiencia del Cliente Primerizo

### ¿El usuario entiende qué ofrece el negocio al entrar?
- **Claridad Inmediata:** La cabecera superior y el Hero presentan claramente la propuesta de valor: *"Salón de Belleza y Cuidado Capilar Natural en Cartagena"*.
- **Separación de Servicios vs Productos:**
  - La sección **Servicios** comunica con claridad que se trata de citas presenciales en el salón de belleza (alisados, cortes, tratamientos, peinados) con agendamiento de fecha y hora.
  - La sección **Productos y Kits** expone claramente los artículos físicos para cuidado en el hogar con opciones de entrega a domicilio o recogida en el local.
- **Transparencia Financiera:** Se explica de forma explícita que las citas de servicios se reservan con un **anticipo del 25%** a través de Wompi, abonando el **75% restante** directamente en el salón el día de la cita.

---

## 2. Experiencia de Carrito y Checkout

1. **Indicador de Tipo de Orden:**
   - **Solo Productos:** El carrito muestra subtotales exactos, selector de entrega (*Envío a Domicilio* o *Recoger en Salón*), campo de dirección condicional y CTA *"Pagar con Wompi"*.
   - **Cita o Compra Mixta:** El carrito muestra el selector de fecha y hora, el cálculo del anticipo (25%) y el saldo a pagar en el salón.
2. **Prevención de Errores en Domicilio:**
   - Si el cliente elige *"Envío a Domicilio"*, el campo de dirección se resalta con borde dorado y aviso obligatorio. Si intenta continuar sin llenarlo, el formulario lo detiene con un mensaje claro antes de llamar a la pasarela.
3. **Feedback de Pago:**
   - Al pulsar *"Pagar con Wompi"*, los mensajes son 100% contextuales:
     - Cita: *"Horario reservado temporalmente mientras completas tu pago."*
     - Domicilio: *"Completa el pago para confirmar tu pedido y continuar con el envío."*
     - Pickup: *"Completa el pago para confirmar tu pedido y continuar con la preparación."*

---

## 3. Experiencia en la Consulta de Reservas y Pedidos (Autoservicio)

- **Unificación sin Confusión:** Un único buscador (*"Consultar reserva o pedido"*) atiende tanto a clientes de citas como a compradores de productos.
- **Presentación Adaptativa:**
  - **Para Pedidos:** Despliega el Stepper visual de 4 o 5 fases con iconos claros (`✓ Pagado`, `● Prep.`, `📦 Listo`, `🚚 En camino`, `✨ Entregado` o `✨ Recogido`) junto con la dirección de entrega o las instrucciones para retirar en el salón.
  - **Para Citas:** Muestra la fecha, hora, duración, estilista asignado, monto de anticipo pagado, saldo pendiente en el salón, y botones para agregar al calendario (Google / iCal) o gestionar reprogramaciones/cancelaciones según las políticas de 24 horas.

---

## 4. Experiencia del Administrador (Panel de Control)

- **Jerarquía Visual:**
  - **Dashboard:** Tarjetas superiores con KPIs diarios/mensuales, desglose de citas de hoy y caja estimada.
  - **Solicitudes que requieren acción:** Indicadores numéricos destacados para comprobantes por revisar o solicitudes de cancelación tardía, con enlaces directos para resolverlas con un solo clic.
  - **Separación de Módulos:** Módulo exclusivo de *Citas* y módulo exclusivo de *Pedidos*, evitando que compras de shampoo o kits aparezcan en la agenda diaria de los profesionales.
- **Control de Estados con Protección:** Botones de acción logística que solo permiten transiciones válidas, evitando confusiones operativas.

---

## 5. Microanimaciones, Feedback Visual y Accesibilidad

- **Contraste y Legibilidad:** Paleta de colores balanceada basada en tonos oscuros (`#0f0e0d`), dorado (`#d4af37`) y blanco cálido (`#f7f0e6`), cumpliendo con ratios WCAG AA.
- **Tipografía:** Combinación elegante de fuentes de Google Fonts (*Cinzel* para títulos de marca y *Plus Jakarta Sans* para lectura óptima de textos y formularios).
- **Notificaciones Toast:** Notificaciones flotantes en esquina superior derecha con autocierre temporizado y colores semánticos (verde para éxito, rojo para errores, amarillo para alertas y azul/dorado para información).
