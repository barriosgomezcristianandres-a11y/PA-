# ISIVI - MATRIZ DE ISSUES Y FALSOS POSITIVOS DESCARTADOS (SYSTEM-WIDE SMART ISSUES)

**Fecha**: 22 de Agosto, 2026

---

## 1. COMPORTAMIENTOS REVISADOS Y CONFIRMADOS COMO CORRECTOS

### 1.1 Exclusiones Financieras entre Servicios y Productos en Wompi
- **Premisa**: ¿Por qué un carrito mixto con servicio + producto exige procesar separadamente el pago Wompi?
- **Dictamen**: **DISEÑO CORRECTO**.
- **Fundamento**: Evita bloqueos contables donde el pago del 25% de anticipo expiraría junto al horario reservado (15 min) comprometiendo el pago del 100% de productos físicos.

### 1.2 Modal de Detalle Contextualizado en el Admin (`openAdminBookingDetail`)
- **Premisa**: ¿Por qué el botón "Detalle" de la tabla de Pedidos abre un modal distinto al de Citas?
- **Dictamen**: **DISEÑO CORRECTO**.
- **Fundamento**: La función evalúa `if (b.isPureOrder) { openAdminOrderDetail(b.id); return; }`, redirigiendo automáticamente al modal logístico de pedidos sin contaminar la vista con campos de citas (horarios/barberos).

---

*Documento de evaluación de issues ISIVI.*
