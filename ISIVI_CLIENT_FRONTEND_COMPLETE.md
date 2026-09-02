# ISIVI — CLIENT FRONTEND COMPLETE SOURCE

## 1. Estructura del frontend
La vista del cliente de ISIVI está compuesta por los siguientes archivos estáticos principales:
- `index.html`: Estructura HTML pública y panel administrativo embebido.
- `css/isivi.css`: Estilos visuales del sistema de diseño premium, adaptaciones de nitidez y fondos ambientados.
- `js/isivi.js`: Toda la lógica interactiva, renderizado dinámico de tarjetas, gestión del carrito, agendamiento de turnos y pasarela de Wompi.

## 2. index.html
```html
<!DOCTYPE html>
<html lang="es" class="scroll-smooth">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">

<!-- SEO Primario -->
<title>ISIVI | Salón de Belleza y Peluquería en Cartagena - Reserva Online &amp; Cuidado Capilar</title>
<meta name="description" content="Descubre servicios de belleza, peluquería profesional, tratamientos y productos capilares artesanales ISIVI en Cartagena. Agenda tu cita en línea o compra kits con pagos seguros.">
<meta name="keywords" content="ISIVI, salon de belleza cartagena, peluqueria cartagena, cuidado capilar natural, tratamientos capilares, reserva online peluqueria, productos capilares artesanales">
<meta name="author" content="ISIVI Belleza Natural">
<meta name="robots" content="index, follow">
<link rel="canonical" href="https://isivi-app.onrender.com/">

<!-- Google Search Console & Google Analytics 4 (Preparado para inserción de token) -->
<!-- <meta name="google-site-verification" content="CODIGO_VERIFICACION_SEARCH_CONSOLE"> -->
<!-- <meta name="ga-measurement-id" content="G-XXXXXXXXXX"> -->

<!-- Favicon & Iconos -->
<link rel="icon" type="image/png" href="/images/isivi-logo-transparent.png">
<link rel="apple-touch-icon" href="/images/isivi-logo-transparent.png">

<!-- Open Graph / Redes Sociales (WhatsApp, Facebook, LinkedIn) -->
<meta property="og:type" content="website">
<meta property="og:site_name" content="ISIVI">
<meta property="og:title" content="ISIVI | Salón de Belleza y Peluquería en Cartagena">
<meta property="og:description" content="Agenda tu cita de peluquería en línea con el 25% de anticipo o adquiere productos y kits artesanales de cuidado capilar en Cartagena.">
<meta property="og:url" content="https://isivi-app.onrender.com/">
<meta property="og:image" content="https://isivi-app.onrender.com/images/isivi-card.jpg">
<meta property="og:locale" content="es_CO">

<!-- Twitter / X Cards -->
<meta name="twitter:card" content="summary_large_image">
<meta name="twitter:title" content="ISIVI | Salón de Belleza y Peluquería en Cartagena">
<meta name="twitter:description" content="Servicios de peluquería profesional, tratamientos capilares naturales y productos en Cartagena. Agenda tu cita en línea.">
<meta name="twitter:image" content="https://isivi-app.onrender.com/images/isivi-card.jpg">

<!-- Datos Estructurados JSON-LD (Schema.org BeautySalon) -->
<script id="isivi-schema-jsonld" type="application/ld+json">
{
  "@context": "https://schema.org",
  "@type": "BeautySalon",
  "name": "ISIVI - Peluquería & Cuidado Capilar Natural",
  "image": "https://isivi-app.onrender.com/images/isivi-card.jpg",
  "logo": "https://isivi-app.onrender.com/images/isivi-logo-transparent.png",
  "@id": "https://isivi-app.onrender.com/#beautysalon",
  "url": "https://isivi-app.onrender.com/",
  "telephone": "+573008949050",
  "priceRange": "$$",
  "address": {
    "@type": "PostalAddress",
    "addressLocality": "Cartagena",
    "addressRegion": "Bolívar",
    "addressCountry": "CO"
  },
  "geo": {
    "@type": "GeoCoordinates",
    "latitude": 10.3997,
    "longitude": -75.5144
  },
  "openingHoursSpecification": [
    {
      "@type": "OpeningHoursSpecification",
      "dayOfWeek": [
        "Sunday",
        "Tuesday",
        "Wednesday",
        "Thursday",
        "Friday",
        "Saturday"
      ],
      "opens": "08:00",
      "closes": "19:00"
    }
  ],
  "paymentAccepted": "Wompi, Transferencia Bancolombia, Transferencia Nequi, Transferencia Daviplata",
  "currenciesAccepted": "COP",
  "description": "Salón de belleza y peluquería en Cartagena especializado en cuidado capilar natural, tratamientos sin sal ni sulfatos agresivos, agendamiento de citas online y venta de productos artesanales."
}
</script>

<!-- Local Production CSS (100% Self-Contained) -->
<link rel="stylesheet" href="/css/isivi.css?v=1.3.0">
<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/tailwindcss/2.2.19/tailwind.min.css">
<script defer src="https://cdn.tailwindcss.com"></script>
<script defer src="https://checkout.wompi.co/widget.js"></script>
<link rel="preconnect" href="https://fonts.googleapis.com">
<link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
<link href="https://fonts.googleapis.com/css2?family=Cinzel:wght@500;600;700;800&family=Plus+Jakarta+Sans:wght@300;400;500;600;700&display=swap" rel="stylesheet">
<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
<script>
    tailwind = window.tailwind || {};
    tailwind.config = {
        theme: {
            extend: {
                colors: {
                    isivi: {
                        50: '#fdfbf7', 100: '#f7f0e6', 200: '#eee0cc', 300: '#e0c8a5',
                        400: '#cca877', 500: '#b88a4c', 600: '#9b6e3b', 700: '#7a522e',
                        800: '#5c3d25', 900: '#1a1613', gold: '#d4af37', black: '#0f0e0d'
                    }
                },
                fontFamily: { serif: ['Cinzel', 'serif'], sans: ['Plus Jakarta Sans', 'sans-serif'] }
            }
        }
    }
</script>
</head>
<body class="bg-isivi-black text-isivi-100 antialiased min-h-screen flex flex-col justify-between">

<div id="toast-container" class="fixed top-5 right-5 z-50 flex flex-col gap-3 max-w-sm w-full pointer-events-none"></div>

<div id="admin-login" class="hidden min-h-screen bg-stone-950 px-4 flex items-center justify-center">
    <form onsubmit="handleAdminLogin(event)" class="w-full max-w-md rounded-2xl border border-isivi-500/30 bg-stone-900 p-7 shadow-2xl space-y-5">
        <div class="text-center"><div class="mx-auto mb-3 flex h-12 w-12 items-center justify-center rounded-xl bg-isivi-gold text-isivi-black"><i class="fa-solid fa-lock"></i></div><h2 class="font-serif-title text-xl font-bold text-white">Acceso Administrador</h2><p class="mt-1 text-xs text-isivi-300">Ingresa tus credenciales para gestionar ISIVI.</p></div>
        <div><label class="mb-1 block text-xs text-isivi-300">Usuario</label><input id="admin-username" type="text" required autocomplete="username" class="w-full rounded-xl border border-stone-700 bg-stone-950 px-3.5 py-2.5 text-sm text-white focus:border-isivi-gold focus:outline-none"></div>
        <div><label class="mb-1 block text-xs text-isivi-300">Contraseña</label><input id="admin-password" type="password" required autocomplete="current-password" class="w-full rounded-xl border border-stone-700 bg-stone-950 px-3.5 py-2.5 text-sm text-white focus:border-isivi-gold focus:outline-none"></div>
        <p id="admin-login-error" class="hidden text-center text-xs text-red-400"></p>
        <div class="flex gap-3"><button type="button" onclick="cancelAdminLogin()" class="flex-1 rounded-xl bg-stone-800 px-4 py-2.5 text-xs font-bold text-stone-200">Cancelar</button><button type="submit" class="flex-1 rounded-xl bg-isivi-gold px-4 py-2.5 text-xs font-bold text-isivi-black hover:bg-yellow-500">Ingresar</button></div>
    </form>
</div>

<!-- ============ PAGE 1: CLIENTE ============ -->
<div id="page-client" class="w-full flex-1 flex flex-col justify-between">
    <!-- Desktop-Only Editorial Margin Branding (Sidebars) -->
    <div class="hidden xl:flex fixed left-4 top-1/2 -translate-y-1/2 z-30 flex-col items-center gap-4 text-[9px] uppercase tracking-[0.3em] text-isivi-gold/25 select-none pointer-events-none [writing-mode:vertical-lr]">
        <span>Estilo Artesanal</span>
        <div class="w-[1px] h-12 bg-isivi-gold/15"></div>
        <span>Belleza Natural</span>
    </div>
    <div class="hidden xl:flex fixed right-4 top-1/2 -translate-y-1/2 z-30 flex-col items-center gap-4 text-[9px] uppercase tracking-[0.3em] text-isivi-gold/25 select-none pointer-events-none [writing-mode:vertical-lr]">
        <span>Cartagena de Indias</span>
        <div class="w-[1px] h-12 bg-isivi-gold/15"></div>
        <span>Reserva Online</span>
    </div>

    <div class="bg-gradient-to-r from-isivi-900 via-stone-900 to-isivi-900 border-b border-isivi-500/30 text-isivi-200 text-xs py-2 px-4 text-center font-medium flex flex-col items-center justify-between gap-1 sm:flex-row sm:gap-4 sm:px-8">
        <div class="flex flex-wrap items-center justify-center gap-x-3 gap-y-1 text-isivi-300 sm:justify-start sm:gap-4">
            <span id="header-location"><i class="fa-solid fa-location-dot text-isivi-gold mr-1.5"></i> <span id="header-location-text">Cartagena, Colombia</span></span>
            <span id="header-hours"><i class="fa-solid fa-clock text-isivi-gold mr-1.5"></i> <span id="header-hours-text">Mar - Sáb: 8:00 AM - 7:00 PM</span></span>
        </div>
        <div class="w-full sm:w-auto text-center">
            ✨ <span class="font-semibold text-isivi-gold">Agenda tu cita con el 25% de anticipo o solicita tus productos ISIVI</span>
        </div>
        <div class="hidden md:flex items-center gap-4">
            <a href="https://instagram.com/isivi_oficial21" target="_blank" class="hover:text-isivi-gold transition flex items-center gap-1">
                <i class="fa-brands fa-instagram text-isivi-gold"></i> @isivi_oficial21
            </a>
        </div>
    </div>

    <header class="sticky top-0 z-40 glass-nav border-b border-isivi-500/20 transition-all">
        <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-20 flex items-center justify-between">
            <a href="#" onclick="navigateToSection('inicio', event)" class="group flex items-center">
                <img src="/images/isivi-logo-transparent.png" alt="ISIVI" class="h-14 w-auto max-w-[180px] object-contain shadow-lg shadow-isivi-gold/10 transition group-hover:scale-105">
            </a>
            <nav class="hidden md:flex items-center gap-7 font-medium text-isivi-200 text-sm">
                <a id="nav-inicio" href="#" onclick="navigateToSection('inicio', event)" class="nav-link-premium pb-1 font-bold text-isivi-gold transition">Inicio</a>
                <a id="nav-servicios" href="#servicios" onclick="navigateToSection('servicios', event)" class="nav-link-premium pb-1 hover:text-isivi-gold transition">Servicios</a>
                <a id="nav-productos" href="#productos" onclick="navigateToSection('productos', event)" class="nav-link-premium pb-1 hover:text-isivi-gold transition">Productos y Kits</a>
                <a id="nav-nosotros" href="#nosotros" onclick="navigateToSection('nosotros', event)" class="nav-link-premium pb-1 hover:text-isivi-gold transition">Nosotros</a>
                <button type="button" onclick="openLookupModal()" class="text-xs text-isivi-300 hover:text-isivi-gold flex items-center gap-1.5 border border-stone-800 hover:border-isivi-gold bg-stone-900/80 px-3 py-1.5 rounded-full transition"><i class="fa-solid fa-magnifying-glass text-isivi-gold text-[10px]"></i><span>Consultar reserva o pedido</span></button>
            </nav>
            <div class="flex items-center gap-2 sm:gap-3">
                <button type="button" onclick="openCartDrawer()" aria-label="Abrir carrito" class="relative bg-gradient-to-r from-isivi-500 to-isivi-600 hover:from-isivi-600 hover:to-isivi-700 text-white text-xs sm:text-sm font-semibold px-3 py-2.5 sm:px-5 rounded-full shadow-lg shadow-isivi-500/20 transition flex items-center gap-2">
                    <i class="fa-solid fa-bag-shopping"></i>
                    <span class="hidden sm:inline">Mi carrito</span>
                    <span id="nav-cart-badge" class="hidden bg-isivi-gold text-isivi-black w-5 h-5 rounded-full text-xs font-bold items-center justify-center">0</span>
                </button>
                <button id="desktop-menu-toggle" type="button" onclick="toggleDesktopMenu()" class="isivi-desktop-menu-btn" aria-label="Abrir menú de navegación" aria-controls="desktop-nav-drawer" aria-expanded="false"><i class="fa-solid fa-bars text-lg"></i></button>
                <button id="mobile-menu-toggle" type="button" onclick="toggleMobileMenu()" class="flex h-11 w-11 items-center justify-center rounded-full border border-isivi-500/50 text-isivi-gold transition hover:bg-isivi-500/15 md:hidden" aria-label="Abrir menú de navegación" aria-controls="mobile-nav" aria-expanded="false"><i class="fa-solid fa-bars text-lg"></i></button>
            </div>
        </div>
        <nav id="mobile-nav" class="hidden absolute inset-x-0 top-full border-b border-isivi-500/30 bg-stone-950/95 px-4 py-3 shadow-xl backdrop-blur-md md:hidden" aria-label="Navegación móvil">
            <div class="mx-auto flex max-w-7xl flex-col gap-1">
                <a href="#" onclick="handleMobileNavigation('inicio', event)" class="rounded-xl px-4 py-3 text-sm font-medium text-isivi-200 transition hover:bg-isivi-500/15 hover:text-isivi-gold">Inicio</a>
                <a href="#servicios" onclick="handleMobileNavigation('servicios', event)" class="rounded-xl px-4 py-3 text-sm font-medium text-isivi-200 transition hover:bg-isivi-500/15 hover:text-isivi-gold">Servicios</a>
                <a href="#productos" onclick="handleMobileNavigation('productos', event)" class="rounded-xl px-4 py-3 text-sm font-medium text-isivi-200 transition hover:bg-isivi-500/15 hover:text-isivi-gold">Productos y Kits Especiales</a>
                <a href="#nosotros" onclick="handleMobileNavigation('nosotros', event)" class="rounded-xl px-4 py-3 text-sm font-medium text-isivi-200 transition hover:bg-isivi-500/15 hover:text-isivi-gold">Nosotros</a>
                <button type="button" onclick="openLookupModal(); closeMobileMenu();" class="text-left rounded-xl px-4 py-3 text-sm font-medium text-isivi-gold transition hover:bg-isivi-500/15"><i class="fa-solid fa-magnifying-glass mr-2"></i>Consultar reserva o pedido</button>
            </div>
        </nav>
    </header>

    <!-- ============ MENÚ HAMBURGUESA DESKTOP / PC (100% CSS LOCAL) ============ -->
    <div id="desktop-nav-overlay" onclick="closeDesktopMenu()" class="isivi-desktop-overlay"></div>
    <aside id="desktop-nav-drawer" class="isivi-desktop-drawer" aria-label="Menú principal desktop">
        <div>
            <div class="isivi-drawer-header">
                <div class="flex items-center gap-3">
                    <img src="/images/isivi-logo-transparent.png" alt="ISIVI" class="h-10 w-auto object-contain">
                    <span class="font-serif-title font-bold text-isivi-gold text-lg tracking-wider">Menú ISIVI</span>
                </div>
                <button type="button" onclick="closeDesktopMenu()" class="isivi-drawer-close-btn" aria-label="Cerrar menú"><i class="fa-solid fa-xmark"></i></button>
            </div>
            <nav class="isivi-drawer-nav">
                <a href="#" onclick="navigateToSection('inicio', event)" class="isivi-drawer-link">
                    <i class="fa-solid fa-house"></i>
                    <span>Inicio</span>
                </a>
                <a href="#servicios" onclick="navigateToSection('servicios', event)" class="isivi-drawer-link">
                    <i class="fa-solid fa-scissors"></i>
                    <span>Servicios de Peluquería</span>
                </a>
                <a href="#servicios" onclick="navigateToSection('servicios', event)" class="isivi-drawer-link">
                    <i class="fa-solid fa-calendar-check"></i>
                    <span>Agendar Cita</span>
                </a>
                <a href="#productos" onclick="navigateToSection('productos', event)" class="isivi-drawer-link">
                    <i class="fa-solid fa-pump-soap"></i>
                    <span>Productos Individuales</span>
                </a>
                <a href="#kits" onclick="navigateToSection('kits', event)" class="isivi-drawer-link">
                    <i class="fa-solid fa-box-open"></i>
                    <span>Kits Especiales</span>
                </a>
                <button type="button" onclick="closeDesktopMenu(); openCartDrawer();" class="isivi-drawer-link">
                    <i class="fa-solid fa-bag-shopping"></i>
                    <span>Mi Carrito</span>
                </button>
                <button type="button" onclick="closeDesktopMenu(); openLookupModal();" class="isivi-drawer-link" style="color: var(--isivi-gold);">
                    <i class="fa-solid fa-magnifying-glass"></i>
                    <span>Consultar Reserva o Pedido</span>
                </button>
                <a href="#nosotros" onclick="navigateToSection('nosotros', event)" class="isivi-drawer-link">
                    <i class="fa-solid fa-circle-info"></i>
                    <span>Nosotros &amp; Contacto</span>
                </a>
            </nav>
        </div>
        <div class="isivi-drawer-footer space-y-1">
            <p class="font-serif-title font-bold" style="color: var(--isivi-gold);">ISIVI Cartagena</p>
            <p style="font-size: 11px;">Cuidado capilar artesanal 100% natural.</p>
        </div>
    </aside>

    <div id="cart-overlay" onclick="closeCartDrawer()" class="fixed inset-0 z-50 hidden bg-black/70 backdrop-blur-sm"></div>
    <aside id="cart-drawer" class="fixed inset-0 sm:inset-auto sm:left-1/2 sm:top-1/2 z-[60] hidden flex flex-col w-full h-[100dvh] max-h-[100dvh] sm:h-[90vh] sm:max-h-[850px] sm:w-[calc(100%-2rem)] sm:max-w-3xl lg:max-w-4xl xl:max-w-5xl sm:-translate-x-1/2 sm:-translate-y-1/2 rounded-none sm:rounded-3xl border-0 sm:border border-isivi-500/40 bg-isivi-900 shadow-2xl overflow-hidden" aria-label="Carrito de compras">
        <div class="flex h-full flex-col">
            <!-- Header Fijo del Carrito -->
            <div class="flex items-center justify-between border-b border-stone-800 bg-stone-950 px-5 sm:px-7 py-3.5 sm:py-4 flex-shrink-0">
                <div>
                    <h2 class="font-serif-title text-xl sm:text-2xl font-bold text-isivi-gold flex items-center gap-2">
                        <i class="fa-solid fa-bag-shopping text-isivi-gold text-lg"></i>
                        <span>Mi carrito</span>
                    </h2>
                    <p id="cart-item-count" class="mt-0.5 text-xs text-isivi-300">0 artículos</p>
                </div>
                <button type="button" onclick="closeCartDrawer()" class="flex h-9 w-9 items-center justify-center rounded-full bg-stone-900 text-isivi-300 transition hover:bg-stone-800 hover:text-white" aria-label="Cerrar carrito">
                    <i class="fa-solid fa-xmark text-base"></i>
                </button>
            </div>

            <!-- Indicador Compacto de Progreso de Reserva -->
            <div id="booking-progress-stepper" class="border-b border-stone-800 bg-stone-950/60 px-4 sm:px-7 py-2 flex-shrink-0" aria-label="Progreso de reserva">
                <div class="isivi-progress-stepper flex items-center justify-between">
                    <div class="isivi-progress-line">
                        <div id="booking-progress-fill" class="isivi-progress-fill" style="width: 25%;"></div>
                    </div>
                    
                    <div id="step-node-service" class="isivi-step-node is-completed" data-step="1">
                        <div class="isivi-step-circle">
                            <span class="step-num">1</span>
                            <i class="fa-solid fa-check step-check hidden"></i>
                        </div>
                        <span class="isivi-step-label">Servicio</span>
                    </div>

                    <div id="step-node-date" class="isivi-step-node is-active" data-step="2">
                        <div class="isivi-step-circle">
                            <span class="step-num">2</span>
                            <i class="fa-solid fa-check step-check hidden"></i>
                        </div>
                        <span class="isivi-step-label">Fecha</span>
                    </div>

                    <div id="step-node-time" class="isivi-step-node" data-step="3">
                        <div class="isivi-step-circle">
                            <span class="step-num">3</span>
                            <i class="fa-solid fa-check step-check hidden"></i>
                        </div>
                        <span class="isivi-step-label">Hora</span>
                    </div>

                    <div id="step-node-data" class="isivi-step-node" data-step="4">
                        <div class="isivi-step-circle">
                            <span class="step-num">4</span>
                            <i class="fa-solid fa-check step-check hidden"></i>
                        </div>
                        <span class="isivi-step-label">Datos</span>
                    </div>

                    <div id="step-node-confirm" class="isivi-step-node" data-step="5">
                        <div class="isivi-step-circle">
                            <span class="step-num">5</span>
                            <i class="fa-solid fa-check step-check hidden"></i>
                        </div>
                        <span class="isivi-step-label">Pagar</span>
                    </div>
                </div>
            </div>

            <!-- CUERPO PRINCIPAL DESPLAZABLE (Scroll Único sin Anidamientos con Touch Momentum) -->
            <div id="cart-drawer-scroll-body" class="flex-1 overflow-y-auto overscroll-contain touch-pan-y p-4 sm:p-6 lg:p-8 space-y-6 pb-28 sm:pb-8 custom-scrollbar">
                <!-- Resumen de errores dentro del carrito -->
                <div id="cart-error-summary" class="hidden mx-auto bg-red-950/80 border border-red-500/60 px-4 py-3 rounded-2xl text-red-200 text-xs flex items-center justify-between gap-3 animate-fade-scale shadow-lg" role="alert" aria-live="assertive">
                    <div class="flex items-center gap-2 font-medium">
                        <i class="fa-solid fa-circle-exclamation text-red-400 text-base flex-shrink-0"></i>
                        <span>Por favor completa los campos destacados en rojo para continuar.</span>
                    </div>
                </div>

                <!-- Lista de items cuando no está en checkout o para estado vacío -->
                <div id="cart-items-list" class="space-y-3"></div>

                <!-- PROCESO DE CHECKOUT Y DISTRIBUCIÓN DE ZONAS -->
                <div id="cart-checkout" class="hidden border-t border-stone-800/80 pt-6">
                    <div class="grid grid-cols-1 lg:grid-cols-12 gap-6 lg:gap-8 items-start">
                        
                        <!-- ZONA 1: CONTENIDO / PROCESO PRINCIPAL (Columna Izquierda en Desktop) -->
                        <div class="lg:col-span-7 xl:col-span-7 space-y-6">
                            
                            <!-- Paso 1: Agenda tu cita (Fecha y Turno) -->
                            <div id="cart-schedule-section" class="hidden space-y-4 rounded-2xl border border-isivi-500/30 bg-stone-950/90 p-4 sm:p-5 shadow-lg">
                                <div class="flex items-center gap-2 text-xs font-bold uppercase tracking-wider text-isivi-gold">
                                    <span class="flex h-5 w-5 items-center justify-center rounded-full bg-isivi-500 text-[10px] text-white">1</span>
                                    <span>Selecciona Fecha &amp; Horario</span>
                                </div>
                                <div class="rounded-xl border border-stone-800 bg-isivi-900/60 p-3 sm:p-4">
                                    <div class="mb-3 flex items-center justify-between">
                                        <button type="button" onclick="changeCartCalendarMonth(-1)" aria-label="Mes anterior" class="flex h-8 w-8 items-center justify-center rounded-lg border border-stone-700 bg-stone-950 text-isivi-gold transition hover:border-isivi-gold">
                                            <i class="fa-solid fa-chevron-left text-[10px]"></i>
                                        </button>
                                        <h4 id="cart-calendar-month-year" class="text-sm font-bold text-white"></h4>
                                        <button type="button" onclick="changeCartCalendarMonth(1)" aria-label="Mes siguiente" class="flex h-8 w-8 items-center justify-center rounded-lg border border-stone-700 bg-stone-950 text-isivi-gold transition hover:border-isivi-gold">
                                            <i class="fa-solid fa-chevron-right text-[10px]"></i>
                                        </button>
                                    </div>
                                    <div class="mb-1.5 grid grid-cols-7 gap-1 text-center text-[9px] font-bold uppercase text-isivi-300">
                                        <span>Dom</span><span>Lun</span><span>Mar</span><span>Mié</span><span>Jue</span><span>Vie</span><span>Sáb</span>
                                    </div>
                                    <div id="cart-calendar-days-grid" class="grid grid-cols-7 gap-1"></div>
                                    <p class="mt-3 text-center text-[10px] text-isivi-300">Selecciona un día disponible para ver sus horarios.</p>
                                    <p id="cart-date-error" class="hidden text-[11px] text-red-400 mt-1.5 text-center flex items-center justify-center gap-1">
                                        <i class="fa-solid fa-circle-exclamation text-[10px]"></i>
                                        <span>Selecciona una fecha válida en el calendario.</span>
                                    </p>
                                </div>
                                <div class="border-t border-stone-800 pt-3">
                                    <p class="mb-2.5 text-xs font-bold text-white">
                                        <i class="fa-regular fa-clock mr-1 text-isivi-gold"></i> Horarios para <span id="cart-selected-date-label" class="text-isivi-gold"></span>
                                    </p>
                                    <div id="cart-time-slots" class="grid grid-cols-2 sm:grid-cols-3 gap-2"></div>
                                    <p id="cart-time-error" class="hidden text-[11px] text-red-400 mt-1.5 text-center flex items-center justify-center gap-1">
                                        <i class="fa-solid fa-circle-exclamation text-[10px]"></i>
                                        <span>Debes seleccionar un horario disponible.</span>
                                    </p>
                                </div>
                            </div>

                            <!-- Paso 2: Tus datos de contacto -->
                            <div id="cart-customer-section" class="space-y-4 rounded-2xl border border-stone-800/80 bg-stone-950/90 p-4 sm:p-5 shadow-lg">
                                <div class="flex items-center gap-2 text-xs font-bold uppercase tracking-wider text-isivi-gold">
                                    <span id="cart-step-cust-num" class="flex h-5 w-5 items-center justify-center rounded-full bg-isivi-500 text-[10px] text-white">2</span>
                                    <span>Tus Datos de Contacto</span>
                                </div>
                                <div class="space-y-3">
                                    <div>
                                        <label class="mb-1 block text-[11px] font-medium text-isivi-300">Nombre completo *</label>
                                        <input id="cart-cust-name" type="text" placeholder="Tu nombre y apellido" aria-describedby="cart-cust-name-error" class="w-full rounded-xl border border-stone-800 bg-stone-950 px-3.5 py-2.5 text-xs text-white placeholder-stone-600 focus:border-isivi-gold focus:outline-none transition">
                                        <p id="cart-cust-name-error" class="hidden text-[11px] text-red-400 mt-1 flex items-center gap-1">
                                            <i class="fa-solid fa-circle-exclamation text-[10px]"></i>
                                            <span>Ingresa tu nombre completo.</span>
                                        </p>
                                    </div>
                                    <div class="grid grid-cols-1 sm:grid-cols-2 gap-3">
                                        <div>
                                            <label class="mb-1 block text-[11px] font-medium text-isivi-300">WhatsApp / Teléfono *</label>
                                            <input id="cart-cust-phone" type="tel" placeholder="Ej: 3001234567" aria-describedby="cart-cust-phone-error" class="w-full rounded-xl border border-stone-800 bg-stone-950 px-3.5 py-2.5 text-xs text-white placeholder-stone-600 focus:border-isivi-gold focus:outline-none transition">
                                            <p id="cart-cust-phone-error" class="hidden text-[11px] text-red-400 mt-1 flex items-center gap-1">
                                                <i class="fa-solid fa-circle-exclamation text-[10px]"></i>
                                                <span>Ingresa tu número de WhatsApp.</span>
                                            </p>
                                        </div>
                                        <div>
                                            <label class="mb-1 block text-[11px] font-medium text-isivi-300">Ciudad / Barrio</label>
                                            <input id="cart-cust-city" type="text" placeholder="Ej: Cartagena - Bocagrande" class="w-full rounded-xl border border-stone-800 bg-stone-950 px-3.5 py-2.5 text-xs text-white placeholder-stone-600 focus:border-isivi-gold focus:outline-none transition">
                                        </div>
                                    </div>
                                    <div>
                                        <label class="mb-1 block text-[11px] font-medium text-isivi-300">Correo electrónico *</label>
                                        <input id="cart-cust-email" type="email" placeholder="tu.correo@ejemplo.com" aria-describedby="cart-cust-email-error" class="w-full rounded-xl border border-stone-800 bg-stone-950 px-3.5 py-2.5 text-xs text-white placeholder-stone-600 focus:border-isivi-gold focus:outline-none transition">
                                        <p id="cart-cust-email-error" class="hidden text-[11px] text-red-400 mt-1 flex items-center gap-1">
                                            <i class="fa-solid fa-circle-exclamation text-[10px]"></i>
                                            <span>Ingresa un correo electrónico válido.</span>
                                        </p>
                                        <p class="text-[11px] text-isivi-300 mt-1.5 leading-tight">Usaremos tu correo para enviarte la confirmación y los detalles de tu reserva o pedido.</p>
                                    </div>
                                </div>
                            </div>

                            <!-- Opciones de Entrega (Productos) -->
                            <div id="cart-delivery-options" class="hidden space-y-3 rounded-2xl border border-isivi-500/30 bg-isivi-900/60 p-4 sm:p-5 shadow-lg">
                                <label class="block text-xs font-bold text-isivi-gold">¿Cómo deseas recibir tus productos? *</label>
                                <select id="cart-delivery-method" onchange="toggleCartDeliveryAddress()" class="w-full rounded-xl border border-stone-800 bg-stone-950 px-3.5 py-2.5 text-xs text-white focus:border-isivi-gold focus:outline-none transition">
                                    <option value="pickup">Recoger en el local (ISIVI Salón Cartagena)</option>
                                    <option value="delivery">Envío a domicilio</option>
                                </select>
                                <div id="cart-delivery-address-wrap" class="hidden pt-1">
                                    <label class="mb-1 block text-[11px] font-medium text-isivi-300">Dirección completa de entrega *</label>
                                    <input id="cart-delivery-address" type="text" placeholder="Calle, número, apartamento / referencia *" aria-describedby="cart-delivery-address-error" class="w-full rounded-xl border border-stone-800 bg-stone-950 px-3.5 py-2.5 text-xs text-white placeholder-stone-600 focus:border-isivi-gold focus:outline-none transition">
                                    <p id="cart-delivery-address-error" class="hidden text-[11px] text-red-400 mt-1 flex items-center gap-1">
                                        <i class="fa-solid fa-circle-exclamation text-[10px]"></i>
                                        <span>Ingresa la dirección para el envío a domicilio.</span>
                                    </p>
                                </div>
                            </div>

                            <!-- Paso 3: Medio de Pago (Opciones Transferencia) -->
                            <div id="cart-payment-section" class="hidden space-y-3 rounded-2xl border border-emerald-500/30 bg-emerald-950/20 p-4 sm:p-5 shadow-lg">
                                <div class="flex items-center gap-2 text-xs font-bold uppercase tracking-wider text-emerald-300">
                                    <span id="cart-step-pay-num" class="flex h-5 w-5 items-center justify-center rounded-full bg-emerald-600 text-[10px] text-white">3</span>
                                    <span>Medio de Pago para Transferencia</span>
                                </div>
                                <select id="cart-payment-method" class="hidden">
                                    <option value="bancolombia">Bancolombia</option>
                                    <option value="nequi">Nequi</option>
                                    <option value="daviplata">Daviplata</option>
                                </select>
                                <div class="grid grid-cols-3 gap-2">
                                    <button type="button" data-method="bancolombia" onclick="setCartPaymentMethod('bancolombia')" class="cart-payment-method-btn rounded-xl border border-isivi-gold bg-stone-950 p-2 text-center text-[10px] font-bold text-white transition">
                                        <i class="fa-solid fa-building-columns mb-1 block text-isivi-gold"></i>Bancolombia
                                    </button>
                                    <button type="button" data-method="nequi" onclick="setCartPaymentMethod('nequi')" class="cart-payment-method-btn rounded-xl border border-stone-700 bg-stone-950 p-2 text-center text-[10px] font-bold text-white transition">
                                        <i class="fa-solid fa-mobile-screen-button mb-1 block text-purple-400"></i>Nequi
                                    </button>
                                    <button type="button" data-method="daviplata" onclick="setCartPaymentMethod('daviplata')" class="cart-payment-method-btn rounded-xl border border-stone-700 bg-stone-950 p-2 text-center text-[10px] font-bold text-white transition">
                                        <i class="fa-solid fa-wallet mb-1 block text-red-400"></i>Daviplata
                                    </button>
                                </div>
                                <div id="cart-payment-details" class="text-xs text-isivi-200 pt-1"></div>
                            </div>
                        </div>

                        <!-- ZONAS 2 Y 3: RESUMEN Y ACCIONES DE PAGO (Columna Derecha / Panel Lateral en Desktop) -->
                        <div class="lg:col-span-5 xl:col-span-5 space-y-5 lg:sticky lg:top-0">
                            
                            <!-- ZONA 2: RESUMEN FINANCIERO -->
                            <div class="rounded-2xl border border-isivi-500/30 bg-stone-950 p-5 space-y-4 shadow-xl">
                                <h3 class="text-xs font-bold uppercase tracking-wider text-isivi-gold flex items-center gap-2 pb-2 border-b border-stone-800">
                                    <i class="fa-solid fa-receipt text-isivi-gold"></i>
                                    <span>Resumen de tu Orden</span>
                                </h3>

                                <!-- Tarjeta de Resumen Financiero del Servicio (Si aplica Cita) -->
                                <div id="cart-service-financial-card" class="hidden rounded-xl border border-isivi-500/30 bg-isivi-900/50 p-3.5 space-y-2 text-xs">
                                    <div class="flex items-center justify-between pb-1.5 border-b border-stone-800">
                                        <span class="font-bold text-isivi-gold flex items-center gap-1.5"><i class="fa-solid fa-scissors"></i> Servicio Cita</span>
                                        <span class="text-[10px] bg-emerald-950 text-emerald-300 border border-emerald-500/40 px-2 py-0.5 rounded-full font-bold">25% Anticipo</span>
                                    </div>
                                    <div class="flex justify-between text-isivi-300">
                                        <span>Total servicios:</span>
                                        <strong id="cart-service-total-val" class="text-white">$0</strong>
                                    </div>
                                    <div class="flex justify-between text-isivi-300">
                                        <span>Saldo en salón (75%):</span>
                                        <strong id="cart-service-remaining-val" class="text-white">$0</strong>
                                    </div>
                                    <div class="pt-2 border-t border-stone-800 flex justify-between items-center text-xs">
                                        <div>
                                            <span class="block font-bold text-isivi-gold">Anticipo requerido hoy:</span>
                                        </div>
                                        <strong id="cart-service-deposit-val" class="text-base font-bold text-emerald-400">$0</strong>
                                    </div>
                                </div>

                                <!-- Total General a Pagar Hoy -->
                                <div class="pt-2 flex items-center justify-between text-sm">
                                    <span id="cart-total-label" class="font-semibold text-isivi-300">Total a Pagar Hoy</span>
                                    <span id="cart-total" class="text-2xl font-extrabold text-isivi-gold">$0</span>
                                </div>
                            </div>

                            <!-- ZONA 3: ACCIONES DE PAGO -->
                            <div class="space-y-3">
                                <!-- Método 1 (Principal): Wompi -->
                                <button id="cart-wompi-button" type="button" onclick="startCartWompiPayment()" class="flex w-full items-center justify-center gap-2.5 rounded-2xl bg-gradient-to-r from-isivi-500 via-yellow-500 to-isivi-600 py-3.5 px-4 text-xs sm:text-sm font-extrabold text-isivi-black shadow-xl transition hover:brightness-110 active:scale-98">
                                    <i class="fa-solid fa-credit-card text-base"></i>
                                    <span>PAGAR CON WOMPI (PAGO AUTOMÁTICO)</span>
                                </button>

                                <!-- Método 2 (Segunda Opción): Transferencia / WhatsApp -->
                                <button id="cart-checkout-button" type="button" onclick="completeCartCheckout()" class="flex w-full items-center justify-center gap-2 rounded-2xl border border-stone-700 bg-stone-900 hover:bg-stone-800 hover:border-isivi-500/60 py-3 px-4 text-xs font-semibold text-stone-300 transition">
                                    <i class="fa-solid fa-building-columns text-isivi-gold"></i>
                                    <span>Segunda opción: Transferencia Bancaria</span>
                                </button>
                                
                                <p id="cart-help" class="text-center text-[11px] text-isivi-300 pt-1 leading-normal">
                                    🔒 Pagos 100% seguros · Citas con el 25% de anticipo y saldo en el salón
                                </p>
                            </div>

                        </div>
                    </div>
                </div>
            </div>
        </div>
    </aside>

    <a id="whatsapp-float" href="https://wa.me/573008949050?text=Hola%20ISIVI%2C%20quiero%20más%20información." target="_blank" rel="noopener noreferrer" aria-label="Escribir a ISIVI por WhatsApp" onclick="trackEvent('whatsapp_click', { context: 'floating_chat' })" class="isivi-whatsapp-float fixed bottom-4 right-4 z-40 flex h-12 w-12 items-center justify-center rounded-full bg-emerald-500 text-xl text-white shadow-xl shadow-emerald-950/70 transition hover:scale-110 hover:bg-emerald-600 sm:bottom-5 sm:right-5 sm:h-14 sm:w-14 sm:text-2xl"><i class="fa-brands fa-whatsapp"></i></a>

    <section class="hero-isivi reveal-on-scroll relative overflow-hidden py-12 md:py-20 border-b border-isivi-500/20">
        <!-- Decorative Watermark (Aesthetic, non-semantic, non-interactive) -->
        <div class="absolute right-12 top-1/2 -translate-y-1/2 select-none pointer-events-none font-serif text-[18rem] font-bold text-isivi-gold/[0.03] leading-none z-0 hidden lg:block">ISIVI</div>
        <img src="/images/isivi-hero.jpg" alt="Modelo ISIVI con productos de cuidado capilar natural" class="hero-background-image">
        <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <div class="grid grid-cols-1 lg:grid-cols-12 gap-12 items-center">
                <div class="lg:col-span-7 text-center lg:text-left space-y-6">
                    <div class="inline-flex items-center gap-2 bg-isivi-900 border border-isivi-500/40 px-4 py-1.5 rounded-full text-isivi-300 text-xs font-semibold tracking-wide uppercase">
                        <span class="w-2 h-2 rounded-full bg-emerald-400 animate-pulse"></span>
                        Fórmulas Artesanales sin Químicos Agresivos
                    </div>
                    <h1 class="font-serif-title text-4xl sm:text-5xl lg:text-6xl font-bold text-white leading-[1.15]">
                        Transforma tu cabello con <span class="gold-gradient-text">ISIVI</span> · Peluquería &amp; Belleza en Cartagena
                    </h1>
                    <p class="text-isivi-200 text-base sm:text-lg max-w-2xl mx-auto lg:mx-0 font-light leading-relaxed">
                        Nuestra línea capilar combina ingredientes naturales para devolverle la fuerza, el brillo y el crecimiento a tu cabello. Agenda tus servicios de peluquería con el <strong class="text-isivi-gold font-semibold">25% de anticipo</strong> o adquiere nuestros productos en detal y kits.
                    </p>
                    <div class="grid grid-cols-3 gap-2 pt-2 max-w-lg mx-auto lg:mx-0 border-y border-isivi-500/30 py-4 text-left sm:gap-4">
                        <div class="min-w-0 hero-indicator-module"><span class="block text-isivi-gold font-bold text-base sm:text-lg"><i class="fa-solid fa-leaf text-isivi-gold mr-1"></i> 100%</span><span class="block break-words text-[11px] leading-tight text-isivi-300 sm:text-xs font-medium">Natural &amp; Artesanal</span></div>
                        <div class="min-w-0 hero-indicator-module"><span class="block text-isivi-gold font-bold text-base sm:text-lg"><i class="fa-solid fa-ban text-isivi-gold mr-1"></i> Sin Sal</span><span class="block break-words text-[11px] leading-tight text-isivi-300 sm:text-xs font-medium">Ni Sulfatos Agresivos</span></div>
                        <div class="min-w-0 hero-indicator-module"><span class="block text-isivi-gold font-bold text-base sm:text-lg"><i class="fa-solid fa-shield-heart text-emerald-400 mr-1"></i> 25%</span><span class="block break-words text-[11px] leading-tight text-isivi-300 sm:text-xs font-medium">Anticipo de Reserva</span></div>
                    </div>
                    <div class="flex flex-col sm:flex-row items-center justify-center lg:justify-start gap-4 pt-2">
                        <a href="#servicios" onclick="navigateToSection('servicios', event)" class="isivi-btn-primary w-full sm:w-auto text-sm tracking-wide shadow-xl"><i class="fa-solid fa-calendar-check mr-1 text-xs"></i> Ver Servicios &amp; Agendar</a>
                        <a href="#productos" onclick="navigateToSection('productos', event)" class="isivi-btn-secondary w-full sm:w-auto text-sm"><i class="fa-solid fa-bag-shopping mr-1 text-xs text-isivi-gold"></i> Comprar Productos</a>
                    </div>
                </div>
                <div class="lg:col-span-5 relative editorial-card-wrapper">
                    <!-- Offset decorative gold outline frame (aesthetic only, doesn't affect flow) -->
                    <div class="absolute inset-0 border border-isivi-gold/20 rounded-[2rem] translate-x-5 translate-y-5 pointer-events-none -z-10 transition-transform duration-500 hidden sm:block"></div>
                    <div class="relative mx-auto max-w-md lg:max-w-none editorial-card-inner">
                        <div class="aspect-[4/5] rounded-3xl overflow-hidden shadow-2xl relative border-2 border-isivi-500/40 gold-border-glow">
                            <img src="/images/isivi-card.jpg" alt="Tratamientos y productos de cuidado capilar natural ISIVI en Cartagena" class="w-full h-full object-cover">
                            <div class="absolute inset-0 bg-gradient-to-t from-isivi-black via-transparent to-transparent"></div>
                            <div class="absolute bottom-6 left-6 right-6 editorial-card-overlay p-4 rounded-2xl shadow-xl flex items-center justify-between">
                                <div class="flex items-center gap-3">
                                    <div class="w-10 h-10 rounded-full bg-isivi-500/20 border border-isivi-gold text-isivi-gold flex items-center justify-center font-bold"><i class="fa-solid fa-sparkles"></i></div>
                                    <div><h4 class="text-xs font-bold text-white">Tratamiento ISIVI</h4><p class="text-[11px] text-isivi-300">Cuidado capilar artesanal</p></div>
                                </div>
                                <span class="bg-isivi-gold text-isivi-black text-xs font-bold px-3 py-1 rounded-full">Cartagena</span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </section>

    <div class="editorial-divider"></div>

    <section id="client-banners" class="hidden bg-isivi-black px-4 py-8 sm:px-6 lg:px-8"></section>

    <section id="servicios" class="py-16 bg-isivi-black reveal-on-scroll relative overflow-hidden">
        <div class="absolute left-6 top-12 font-serif text-[15rem] font-bold text-isivi-gold/[0.02] leading-none z-0 select-none pointer-events-none hidden lg:block">01</div>
        <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 relative z-10">
            <div class="text-center max-w-3xl mx-auto mb-12">
                <div class="flex items-center justify-center gap-3 text-xs uppercase font-bold tracking-widest text-isivi-gold/50 mb-2.5 select-none pointer-events-none">
                    <span class="text-isivi-gold font-extrabold">01 Servicios</span>
                    <span class="text-isivi-gold/20">/</span>
                    <span>02 Productos</span>
                    <span class="text-isivi-gold/20">/</span>
                    <span>03 Confirmación</span>
                </div>
                <h2 class="font-serif-title text-3xl sm:text-4xl font-bold text-white mt-1">Servicios de Peluquería &amp; Estilo</h2>
                <p class="text-isivi-300 text-sm mt-2">Selecciona los servicios que deseas para tu visita.</p>
                <div class="flex flex-wrap justify-center gap-2 mt-6" id="service-category-tabs"></div>
            </div>
            <div class="relative px-2 sm:px-12"><button onclick="scrollCatalog('services-grid', -1)" class="absolute left-0 top-1/2 z-10 hidden -translate-y-1/2 rounded-full border border-isivi-500/60 bg-stone-950/95 px-4 py-3 text-isivi-gold shadow-lg hover:bg-stone-800 sm:flex sm:items-center sm:justify-center" aria-label="Servicios anteriores"><i class="fa-solid fa-chevron-left"></i></button><div id="services-grid" class="flex gap-4 overflow-x-auto scroll-smooth snap-x snap-mandatory pb-4 sm:gap-6"></div><button onclick="scrollCatalog('services-grid', 1)" class="absolute right-0 top-1/2 z-10 hidden -translate-y-1/2 rounded-full border border-isivi-500/60 bg-stone-950/95 px-4 py-3 text-isivi-gold shadow-lg hover:bg-stone-800 sm:flex sm:items-center sm:justify-center" aria-label="Más servicios"><i class="fa-solid fa-chevron-right"></i></button></div>
        </div>
    </section>

    <div class="editorial-divider"></div>

    <section id="productos" class="py-16 bg-isivi-900 border-t border-isivi-500/20 reveal-on-scroll relative overflow-hidden">
        <div class="absolute right-12 top-12 font-serif text-[15rem] font-bold text-isivi-gold/[0.02] leading-none z-0 select-none pointer-events-none hidden lg:block">02</div>
        <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 relative z-10">
            <div class="flex flex-col md:flex-row md:items-end justify-between mb-10 gap-4">
                <div>
                    <div class="flex items-center gap-3 text-xs uppercase font-bold tracking-widest text-isivi-gold/50 mb-2.5 select-none pointer-events-none">
                        <span class="text-isivi-gold/50">01 Servicios</span>
                        <span class="text-isivi-gold/20">/</span>
                        <span class="text-isivi-gold font-extrabold">02 Productos</span>
                        <span class="text-isivi-gold/20">/</span>
                        <span>03 Confirmación</span>
                    </div>
                    <h2 class="font-serif-title text-3xl sm:text-4xl font-bold text-white mt-1">Productos Individuales (Detal)</h2>
                    <p class="text-isivi-300 text-sm mt-1">Nutrición natural para el cuidado diario de tu hebra capilar.</p>
                </div>
                <div class="bg-stone-900 border border-isivi-500/40 p-3 rounded-2xl flex items-center gap-3 text-xs">
                    <i class="fa-solid fa-boxes-packing text-isivi-gold text-lg"></i>
                    <div><p class="font-bold text-white">¿Ventas al Por Mayor?</p><p class="text-[11px] text-isivi-300">Mayorista desde $200.000 · Distribuidor desde $500.000</p></div>
                    <a href="https://wa.me/573009623174?text=Hola,%20quisiera%20información%20para%20compras%20al%20mayor%20de%20ISIVI" target="_blank" onclick="trackEvent('whatsapp_click', { context: 'wholesale_inquiry' })" class="ml-2 bg-emerald-600 hover:bg-emerald-700 text-white font-bold px-3 py-1.5 rounded-xl transition text-[11px]">Consultar</a>
                </div>
            </div>
            <!-- Filtros de Categorías de Productos -->
            <div class="flex flex-wrap justify-center sm:justify-start gap-2 mb-6" id="product-categories-pills"></div>
            <div class="relative px-2 sm:px-12"><button onclick="scrollCatalog('products-grid', -1)" class="absolute left-0 top-1/2 z-10 hidden -translate-y-1/2 rounded-full border border-isivi-500/60 bg-stone-950/95 px-4 py-3 text-isivi-gold shadow-lg hover:bg-stone-800 sm:flex sm:items-center sm:justify-center" aria-label="Productos anteriores"><i class="fa-solid fa-chevron-left"></i></button><div id="products-grid" class="flex gap-4 overflow-x-auto scroll-smooth snap-x snap-mandatory pb-4 sm:gap-6"></div><button onclick="scrollCatalog('products-grid', 1)" class="absolute right-0 top-1/2 z-10 hidden -translate-y-1/2 rounded-full border border-isivi-500/60 bg-stone-950/95 px-4 py-3 text-isivi-gold shadow-lg hover:bg-stone-800 sm:flex sm:items-center sm:justify-center" aria-label="Más productos"><i class="fa-solid fa-chevron-right"></i></button></div>
        </div>
    </section>

    <div class="editorial-divider"></div>

    <section id="kits" class="py-16 bg-isivi-black border-t border-isivi-500/20 reveal-on-scroll relative overflow-hidden">
        <div class="absolute left-12 top-12 font-serif text-[15rem] font-bold text-isivi-gold/[0.02] leading-none z-0 select-none pointer-events-none hidden lg:block">03</div>
        <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 relative z-10">
            <div id="kits-intro" class="text-center max-w-3xl mx-auto mb-10">
                <div class="flex items-center justify-center gap-3 text-xs uppercase font-bold tracking-widest text-isivi-gold/50 mb-2.5 select-none pointer-events-none">
                    <span class="text-isivi-gold/50">01 Servicios</span>
                    <span class="text-isivi-gold/20">/</span>
                    <span class="text-isivi-gold font-extrabold">02 Productos</span>
                    <span class="text-isivi-gold/20">/</span>
                    <span>03 Confirmación</span>
                </div>
                <h2 class="font-serif-title text-3xl sm:text-4xl font-bold text-white mt-1">Kits Capilares Especiales</h2>
                <p class="text-isivi-300 text-sm mt-2">Combinaciones formuladas para una rutina capilar efectiva.</p>
            </div>
            <!-- Filtros de Categorías de Kits -->
            <div class="flex flex-wrap justify-center sm:justify-start gap-2 mb-6" id="kit-categories-pills"></div>
            <div class="relative px-2 sm:px-12"><button onclick="scrollCatalog('kits-grid', -1)" class="absolute left-0 top-1/2 z-10 hidden -translate-y-1/2 rounded-full border border-isivi-500/60 bg-stone-950/95 px-4 py-3 text-isivi-gold shadow-lg hover:bg-stone-800 sm:flex sm:items-center sm:justify-center" aria-label="Kits anteriores"><i class="fa-solid fa-chevron-left"></i></button><div id="kits-grid" class="flex gap-4 overflow-x-auto scroll-smooth snap-x snap-mandatory pb-4 sm:gap-6"></div><button onclick="scrollCatalog('kits-grid', 1)" class="absolute right-0 top-1/2 z-10 hidden -translate-y-1/2 rounded-full border border-isivi-500/60 bg-stone-950/95 px-4 py-3 text-isivi-gold shadow-lg hover:bg-stone-800 sm:flex sm:items-center sm:justify-center" aria-label="Más kits"><i class="fa-solid fa-chevron-right"></i></button></div>
        </div>
    </section>

    <div id="payment-modal" class="fixed inset-0 z-50 bg-black/85 backdrop-blur-md hidden flex items-center justify-center p-4">
        <div class="bg-isivi-900 rounded-3xl max-w-lg w-full overflow-hidden shadow-2xl transform transition-all border border-isivi-500/40">
            <div class="bg-stone-950 text-white p-6 flex justify-between items-center border-b border-stone-800">
                <div>
                    <div class="flex items-center gap-2">
                        <h3 class="font-serif-title text-lg font-bold text-isivi-gold">Transferencia Bancaria</h3>
                        <span class="px-2 py-0.5 rounded-full text-[10px] font-bold bg-stone-800 text-stone-300 border border-stone-700">Segunda opción</span>
                    </div>
                    <p class="text-xs text-isivi-300 mt-0.5">Monto total a transferir: <strong id="modal-deposit-amount" class="text-emerald-400 font-bold">$0</strong></p>
                </div>
                <button onclick="closePaymentModal()" class="w-8 h-8 rounded-full bg-stone-900 text-isivi-300 hover:bg-stone-800 flex items-center justify-center"><i class="fa-solid fa-xmark"></i></button>
            </div>
            <div class="p-6 space-y-5 max-h-[80vh] overflow-y-auto">
                <!-- Resumen de errores dentro del modal (Compacto) -->
                <div id="payment-modal-error-summary" class="hidden bg-red-950/80 border border-red-500/60 px-3.5 py-2.5 rounded-xl text-red-200 text-xs flex items-center justify-between gap-2 animate-fade-scale" role="alert" aria-live="assertive">
                    <div class="flex items-center gap-2 font-medium">
                        <i class="fa-solid fa-circle-exclamation text-red-400 text-sm flex-shrink-0"></i>
                        <span>Por favor completa los campos requeridos antes de continuar.</span>
                    </div>
                </div>

                <div>
                    <label class="block text-xs font-bold text-isivi-gold uppercase tracking-wider mb-2">Selecciona Medio de Pago</label>
                    <div class="grid grid-cols-3 gap-2.5">
                        <button onclick="selectPaymentMethod('bancolombia')" id="pay-btn-bancolombia" class="payment-method-btn p-3 rounded-2xl border-2 border-isivi-gold bg-stone-950 flex flex-col items-center justify-center text-center gap-1 transition"><i class="fa-solid fa-building-columns text-isivi-gold text-lg"></i><span class="text-[11px] font-bold text-white">Bancolombia</span></button>
                        <button onclick="selectPaymentMethod('nequi')" id="pay-btn-nequi" class="payment-method-btn p-3 rounded-2xl border border-stone-800 hover:border-isivi-500 flex flex-col items-center justify-center text-center gap-1 transition"><i class="fa-solid fa-mobile-screen-button text-purple-400 text-lg"></i><span class="text-[11px] font-bold text-white">Nequi</span></button>
                        <button onclick="selectPaymentMethod('daviplata')" id="pay-btn-daviplata" class="payment-method-btn p-3 rounded-2xl border border-stone-800 hover:border-isivi-500 flex flex-col items-center justify-center text-center gap-1 transition"><i class="fa-solid fa-wallet text-red-400 text-lg"></i><span class="text-[11px] font-bold text-white">Daviplata</span></button>
                    </div>
                </div>
                <div id="transfer-details-box" class="bg-stone-950 p-4 rounded-2xl border border-isivi-500/30 space-y-3"></div>
                <div class="bg-amber-950/40 border border-amber-500/30 p-3.5 rounded-2xl text-amber-200 text-xs space-y-1">
                    <p class="font-bold flex items-center gap-1.5"><i class="fa-solid fa-triangle-exclamation text-amber-400"></i> Pasos para confirmar tu cita:</p>
                    <ol class="list-decimal list-inside space-y-0.5 text-[11px] text-amber-300/90">
                        <li>Realiza la transferencia del 25% exacto ($<span id="instruction-deposit-val">0</span>).</li>
                        <li>Toma captura de pantalla del comprobante.</li>
                        <li>Haz clic en el botón inferior para abrir WhatsApp con tus datos listos.</li>
                    </ol>
                </div>
                <button id="btn-submit-whatsapp-payment" onclick="processWhatsAppReservation()" class="w-full bg-emerald-600 hover:bg-emerald-700 text-white font-bold py-3.5 rounded-2xl shadow-lg transition flex items-center justify-center gap-2 text-sm">
                    <i class="fa-brands fa-whatsapp text-lg"></i>
                    <span id="btn-submit-whatsapp-text">Enviar Comprobante por WhatsApp</span>
                </button>
            </div>
        </div>
    </div>

    <!-- ============ MODAL: PANTALLA DE ÉXITO POST-RESERVA / POST-PAGO ============ -->
    <div id="modal-success-confirmation" class="fixed inset-0 z-50 bg-black/85 backdrop-blur-md hidden flex items-center justify-center p-4 overflow-y-auto">
        <div class="bg-stone-950 rounded-3xl max-w-lg w-full overflow-hidden shadow-2xl border border-isivi-500/40 animate-fade-scale my-8 text-white">
            <div class="p-6 sm:p-8 space-y-6">
                <div class="text-center space-y-2">
                    <div id="success-icon-container" class="w-16 h-16 rounded-full bg-emerald-500/20 border-2 border-emerald-500 flex items-center justify-center text-emerald-400 text-3xl mx-auto shadow-lg shadow-emerald-500/20">
                        <i id="success-icon" class="fa-solid fa-check"></i>
                    </div>
                    <h2 id="success-title" class="font-serif-title text-2xl sm:text-3xl font-bold text-white">¡Tu cita está confirmada!</h2>
                    <p id="success-subtitle" class="text-xs text-isivi-300">Tu reserva ha sido confirmada y registrada en el sistema.</p>
                    <div class="pt-2">
                        <span id="success-booking-code" class="inline-flex items-center gap-2 bg-isivi-900 border border-isivi-gold text-isivi-gold font-mono font-bold text-base px-4 py-1.5 rounded-full shadow cursor-pointer hover:bg-isivi-800 transition" onclick="copyBookingCode()" title="Haz clic para copiar">
                            <span id="success-booking-code-text">ISV-4821</span>
                            <i class="fa-regular fa-copy text-xs"></i>
                        </span>
                    </div>
                </div>

                <div class="bg-stone-900/90 rounded-2xl border border-stone-800 p-4 space-y-3 text-xs">
                    <div id="success-appointment-datetime-wrap" class="space-y-3">
                        <div class="flex items-center gap-3 text-isivi-200">
                            <i class="fa-solid fa-calendar-day text-isivi-gold text-base w-5 text-center"></i>
                            <div>
                                <span class="text-[10px] text-isivi-300 block uppercase tracking-wider">Fecha de la cita</span>
                                <span id="success-date" class="font-semibold text-white text-sm">Sábado 22 de agosto</span>
                            </div>
                        </div>
                        <div class="flex items-center gap-3 text-isivi-200">
                            <i class="fa-solid fa-clock text-isivi-gold text-base w-5 text-center"></i>
                            <div>
                                <span class="text-[10px] text-isivi-300 block uppercase tracking-wider">Hora programada</span>
                                <span id="success-time" class="font-semibold text-white text-sm">3:00 PM</span>
                            </div>
                        </div>
                    </div>
                    <div class="flex items-start gap-3 text-isivi-200 border-t border-stone-800/80 pt-2.5">
                        <i class="fa-solid fa-scissors text-isivi-gold text-base w-5 text-center mt-0.5"></i>
                        <div class="flex-1">
                            <span id="success-items-label" class="text-[10px] text-isivi-300 block uppercase tracking-wider">Servicios y Productos</span>
                            <div id="success-items-list" class="font-medium text-white space-y-1 mt-0.5">
                                <div>Balayage Iluminador</div>
                            </div>
                        </div>
                    </div>
                    <div id="success-location-wrap" class="hidden flex items-start gap-3 text-isivi-200 border-t border-stone-800/80 pt-2.5">
                        <i class="fa-solid fa-location-dot text-isivi-gold text-base w-5 text-center mt-0.5"></i>
                        <div>
                            <span class="text-[10px] text-isivi-300 block uppercase tracking-wider">Lugar de la cita</span>
                            <span id="success-location-info" class="text-white">ISIVI Salón Cartagena</span>
                        </div>
                    </div>
                    <div id="success-delivery-wrap" class="hidden flex items-start gap-3 text-isivi-200 border-t border-stone-800/80 pt-2.5">
                        <i id="success-delivery-icon" class="fa-solid fa-truck text-isivi-gold text-base w-5 text-center mt-0.5"></i>
                        <div>
                            <span id="success-delivery-label" class="text-[10px] text-isivi-300 block uppercase tracking-wider">Entrega</span>
                            <span id="success-delivery-info" class="text-white">Recoger en el local</span>
                        </div>
                    </div>
                </div>

                <div id="success-payment-card" class="bg-stone-900/90 rounded-2xl border border-isivi-500/30 p-4 space-y-2 text-xs">
                    <div class="flex justify-between items-center text-isivi-300">
                        <span id="success-deposit-label">Anticipo pagado:</span>
                        <span id="success-deposit-val" class="font-bold text-sm text-emerald-400">$55.000 COP</span>
                    </div>
                    <div id="success-remaining-row" class="flex justify-between items-center text-isivi-300">
                        <span>Saldo en salón:</span>
                        <span id="success-remaining-val" class="font-medium text-white">$165.000 COP</span>
                    </div>
                    <div class="flex justify-between items-center text-isivi-300 border-t border-stone-800 pt-2">
                        <span>Valor total:</span>
                        <span id="success-total-val" class="font-bold text-white">$220.000 COP</span>
                    </div>
                </div>

                <div class="space-y-2.5 pt-1">
                    <button id="success-btn-calendar" type="button" onclick="downloadCalendarEvent()" class="w-full bg-stone-900 border border-isivi-500/50 hover:border-isivi-gold text-white font-bold py-3 px-4 rounded-xl transition flex items-center justify-center gap-2 text-xs shadow hover:bg-stone-800">
                        <i class="fa-solid fa-calendar-plus text-isivi-gold"></i>
                        <span>Agregar al calendario</span>
                    </button>
                    <button id="success-btn-whatsapp" type="button" onclick="sendWhatsAppConfirmation()" class="w-full bg-emerald-600 hover:bg-emerald-700 text-white font-bold py-3 px-4 rounded-xl transition flex items-center justify-center gap-2 text-xs shadow">
                        <i class="fa-brands fa-whatsapp text-sm"></i>
                        <span id="success-btn-whatsapp-text">Escribir por WhatsApp</span>
                    </button>
                    <button id="success-btn-retry-payment" type="button" onclick="retryPaymentFromSuccess()" class="hidden w-full bg-isivi-gold hover:bg-yellow-500 text-isivi-black font-bold py-3 px-4 rounded-xl transition flex items-center justify-center gap-2 text-xs shadow">
                        <i class="fa-solid fa-rotate-right"></i>
                        <span>Intentar nuevamente</span>
                    </button>
                    <div class="flex gap-2 pt-1">
                        <button type="button" onclick="lookupCurrentBookingFromSuccess()" class="flex-1 bg-stone-900 border border-stone-800 hover:border-stone-700 text-isivi-200 font-semibold py-2.5 px-3 rounded-xl transition text-[11px] text-center">
                            <i class="fa-solid fa-magnifying-glass mr-1 text-isivi-gold"></i> Consultar reserva o pedido
                        </button>
                        <button type="button" onclick="closeSuccessConfirmation()" class="bg-stone-900 border border-stone-800 hover:border-stone-700 text-stone-400 hover:text-white py-2.5 px-4 rounded-xl transition text-[11px]">
                            Cerrar
                        </button>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- ============ MODAL: CONSULTA DE RESERVA ============ -->
    <div id="modal-consultar-reserva" class="fixed inset-0 z-50 bg-black/80 backdrop-blur-md hidden flex items-center justify-center p-4 overflow-y-auto">
        <div class="bg-stone-950 rounded-3xl max-w-lg w-full overflow-hidden shadow-2xl border border-isivi-500/40 animate-fade-scale my-8 text-white">
            <div class="bg-stone-900 px-6 py-5 flex justify-between items-center border-b border-stone-800">
                <div>
                    <h3 class="font-serif-title text-lg font-bold text-isivi-gold flex items-center gap-2">
                        <i class="fa-solid fa-magnifying-glass"></i> Consultar reserva o pedido
                    </h3>
                    <p class="text-xs text-isivi-300 mt-0.5">Ingresa tu código (ISV-XXXX) o WhatsApp para consultar el estado de tu reserva o pedido.</p>
                </div>
                <button type="button" onclick="closeLookupModal()" class="w-8 h-8 rounded-full bg-stone-950 text-stone-400 hover:text-white flex items-center justify-center"><i class="fa-solid fa-xmark"></i></button>
            </div>
            <div class="p-6 space-y-5">
                <div id="lookup-error-message" class="hidden bg-red-950/90 border border-red-500/80 p-3 rounded-2xl text-red-200 text-xs flex items-center gap-2" role="alert">
                    <i class="fa-solid fa-circle-exclamation text-red-400"></i>
                    <span></span>
                </div>
                <form id="lookup-form" onsubmit="handleLookupSubmit(event)" class="space-y-3">
                    <div>
                        <label for="lookup-query" class="block text-[11px] text-isivi-300 mb-1.5 font-medium">Código de reserva/pedido o WhatsApp *</label>
                        <div class="relative">
                            <input id="lookup-query" type="text" placeholder="Ej: ISV-7518 o 3054449715" required aria-label="Código de reserva o WhatsApp" class="w-full bg-stone-900 border border-stone-800 rounded-xl px-4 py-3 text-xs text-white placeholder-stone-600 focus:outline-none focus:border-isivi-gold font-medium transition">
                            <i class="fa-solid fa-magnifying-glass absolute right-3.5 top-1/2 -translate-y-1/2 text-stone-500 text-xs pointer-events-none"></i>
                        </div>
                    </div>
                    <button type="submit" id="lookup-btn" class="w-full bg-isivi-gold hover:bg-yellow-500 text-isivi-black font-bold py-3 rounded-xl transition flex items-center justify-center gap-2 text-xs shadow-lg active:scale-98">
                        <i class="fa-solid fa-magnifying-glass"></i>
                        <span>Buscar reserva o pedido</span>
                    </button>
                </form>

                <div id="lookup-result-card" class="hidden space-y-4 pt-2 border-t border-stone-800">
                    <div class="bg-stone-900/90 rounded-2xl border border-stone-800 p-4 space-y-3 text-xs">
                        <div class="flex items-center justify-between">
                            <span id="lookup-res-code" class="font-mono font-bold text-isivi-gold text-sm">ISV-4821</span>
                            <span id="lookup-res-badge" class="px-2.5 py-1 rounded-full text-[10px] font-bold bg-amber-950 text-amber-300 border border-amber-500/30">Pendiente Comprobante</span>
                        </div>
                        <div class="grid grid-cols-2 gap-2 text-isivi-200 pt-1">
                            <div><span class="text-[10px] text-isivi-300 block">Cliente</span><span id="lookup-res-name" class="font-medium text-white">María Camila</span></div>
                            <div id="lookup-res-datetime-wrap"><span id="lookup-res-datetime-label" class="text-[10px] text-isivi-300 block">Fecha y Hora</span><span id="lookup-res-datetime" class="font-medium text-white">2026-08-22 · 3:00 PM</span></div>
                        </div>
                        <div class="pt-2 border-t border-stone-800/80">
                            <span id="lookup-res-items-label" class="text-[10px] text-isivi-300 block">Servicios / Productos</span>
                            <span id="lookup-res-items" class="font-medium text-white">Balayage Iluminador</span>
                        </div>
                        <div id="lookup-res-delivery-wrap" class="hidden pt-2 border-t border-stone-800/80">
                            <span id="lookup-res-delivery-label" class="text-[10px] text-isivi-300 block">Entrega</span>
                            <span id="lookup-res-delivery" class="font-medium text-white">Recoger en el local</span>
                        </div>
                        <div class="flex justify-between items-center pt-2 border-t border-stone-800/80 text-isivi-300">
                            <span id="lookup-res-deposit-wrap">Anticipo: <strong id="lookup-res-deposit" class="text-emerald-400 font-bold">$55.000</strong></span>
                            <span id="lookup-res-balance-wrap" class="hidden">Saldo en salón: <strong id="lookup-res-balance" class="text-isivi-gold font-bold">$0</strong></span>
                            <span>Total: <strong id="lookup-res-total" class="text-white font-bold">$220.000</strong></span>
                        </div>
                    </div>

                    <div id="lookup-hold-banner" class="hidden p-3 rounded-2xl bg-amber-950/60 border border-amber-500/50 text-amber-200 text-xs space-y-1.5">
                        <div class="flex items-center justify-between">
                            <span class="font-bold flex items-center gap-1.5 text-amber-300"><i class="fa-solid fa-clock text-amber-400"></i> Reserva pendiente de pago</span>
                            <span id="lookup-hold-countdown" class="px-2 py-0.5 rounded-full text-xs font-mono font-bold bg-amber-900 text-amber-100 border border-amber-500/50">--:--</span>
                        </div>
                        <p class="text-[11px] text-amber-200/90 leading-tight">Este horario sigue reservado para ti mientras completas el pago en Wompi.</p>
                        <button type="button" id="lookup-btn-resume" onclick="resumePaymentFromLookup()" class="w-full mt-1 bg-isivi-gold hover:bg-yellow-500 text-isivi-black font-bold py-2.5 px-3 rounded-xl text-xs transition flex items-center justify-center gap-1.5 shadow">
                            <i class="fa-solid fa-credit-card"></i> Retomar pago con Wompi
                        </button>
                    </div>

                    <!-- Aviso de Política de Cancelación en Consulta -->
                    <div id="lookup-cancel-policy-notice" class="hidden p-3 rounded-2xl text-xs space-y-1"></div>

                    <div class="flex flex-wrap gap-2 pt-1">
                        <button type="button" id="lookup-btn-reschedule" onclick="openClientRescheduleFromLookup()" class="flex-1 bg-stone-900 border border-isivi-500/40 hover:border-isivi-gold text-isivi-200 hover:text-white py-2.5 px-3 rounded-xl text-xs font-semibold transition flex items-center justify-center gap-1.5">
                            <i class="fa-solid fa-calendar-days text-isivi-gold"></i> Reprogramar
                        </button>
                        <button type="button" id="lookup-btn-cancel" onclick="openClientCancelModalFromLookup()" class="bg-red-950/60 border border-red-800/50 hover:bg-red-900 text-red-300 py-2.5 px-3 rounded-xl text-xs font-semibold transition flex items-center justify-center gap-1.5">
                            <i class="fa-solid fa-ban"></i> Cancelar cita
                        </button>
                        <button type="button" onclick="whatsappClientBookingFromLookup()" class="w-full bg-emerald-600 hover:bg-emerald-700 text-white font-bold py-2.5 px-3 rounded-xl text-xs transition flex items-center justify-center gap-1.5">
                            <i class="fa-brands fa-whatsapp"></i> Escribir a ISIVI
                        </button>
                    </div>

                </div>
            </div>
        </div>
    </div>

    <!-- ============ MODAL: REPROGRAMACIÓN CLIENTE ============ -->
    <div id="modal-client-reschedule" class="fixed inset-0 z-50 bg-black/80 backdrop-blur-md hidden flex items-center justify-center p-4">
        <div class="bg-stone-950 rounded-3xl max-w-md w-full overflow-hidden shadow-2xl border border-isivi-500/40 animate-fade-scale text-white p-6 space-y-4">
            <div class="flex justify-between items-center border-b border-stone-800 pb-3">
                <h3 class="font-serif-title font-bold text-isivi-gold text-base"><i class="fa-solid fa-calendar-days mr-2"></i>Reprogramar cita</h3>
                <button type="button" onclick="closeClientRescheduleModal()" class="text-stone-400 hover:text-white"><i class="fa-solid fa-xmark"></i></button>
            </div>
            <div class="space-y-3">
                <div id="client-reschedule-error" class="hidden bg-red-950/90 border border-red-500/80 p-3 rounded-2xl text-red-200 text-xs flex items-center gap-2" role="alert">
                    <i class="fa-solid fa-circle-exclamation text-red-400"></i>
                    <span></span>
                </div>
                <div>
                    <label class="block text-xs text-isivi-300 mb-1">Nueva Fecha</label>
                    <input id="client-reschedule-date" type="date" onchange="handleClientRescheduleDateChange()" class="w-full rounded-xl border border-stone-700 bg-stone-900 px-3.5 py-2.5 text-xs text-white focus:border-isivi-gold focus:outline-none">
                </div>
                <div>
                    <label class="block text-xs text-isivi-300 mb-1">Nuevo Horario</label>
                    <select id="client-reschedule-time" class="w-full rounded-xl border border-stone-700 bg-stone-900 px-3.5 py-2.5 text-xs text-white focus:border-isivi-gold focus:outline-none"></select>
                </div>
            </div>
            <div class="flex justify-end gap-2 pt-2">
                <button type="button" onclick="closeClientRescheduleModal()" class="px-4 py-2 bg-stone-800 hover:bg-stone-700 text-white rounded-xl text-xs font-bold">Cancelar</button>
                <button type="button" onclick="submitClientReschedule()" class="px-5 py-2 bg-isivi-gold hover:bg-yellow-500 text-isivi-black rounded-xl text-xs font-bold shadow">Confirmar reprogramación</button>
            </div>
        </div>
    </div>

    <!-- ============ MODAL: CANCELACIÓN DE CITA CLIENTE (MODAL PROPIO ISIVI) ============ -->
    <div id="modal-client-cancel" class="fixed inset-0 z-50 bg-black/80 backdrop-blur-md hidden flex items-center justify-center p-4 overflow-y-auto" role="dialog" aria-modal="true" aria-labelledby="client-cancel-modal-title">
        <div class="bg-stone-950 rounded-3xl max-w-md w-full overflow-hidden shadow-2xl border border-red-500/40 animate-fade-scale text-white p-6 space-y-4 my-6">
            <div class="flex justify-between items-center border-b border-stone-800 pb-3">
                <h3 id="client-cancel-modal-title" class="font-serif-title font-bold text-red-400 text-base flex items-center gap-2">
                    <i class="fa-solid fa-ban text-red-400"></i> ¿Cancelar tu cita?
                </h3>
                <button type="button" onclick="closeClientCancelModal()" class="text-stone-400 hover:text-white" aria-label="Cerrar modal"><i class="fa-solid fa-xmark"></i></button>
            </div>
            <div class="space-y-3 text-xs">
                <div id="client-cancel-error" class="hidden bg-red-950/90 border border-red-500/80 p-3 rounded-2xl text-red-200 text-xs flex items-center gap-2" role="alert">
                    <i class="fa-solid fa-circle-exclamation text-red-400"></i>
                    <span></span>
                </div>
                <p class="text-stone-300 leading-relaxed">
                    Estás a más de 24 horas de tu cita y puedes cancelarla. Al confirmar, tu horario quedará disponible para otros clientes.
                </p>
                <div class="p-3 bg-stone-900 rounded-xl border border-stone-800 text-[11px] text-stone-400 space-y-1">
                    <p><strong>Código:</strong> <span id="cancel-modal-code" class="text-isivi-gold font-mono"></span></p>
                    <p><strong>Cita:</strong> <span id="cancel-modal-datetime" class="text-white"></span></p>
                </div>
                <div>
                    <label class="block text-xs text-isivi-300 mb-1.5 font-medium">Motivo de cancelación (opcional)</label>
                    <div class="grid grid-cols-2 gap-1.5 mb-2">
                        <button type="button" onclick="selectCancelReason('Cambio de planes')" class="cancel-reason-chip px-2.5 py-2 bg-stone-900 hover:bg-stone-800 border border-stone-700 hover:border-isivi-gold text-stone-300 rounded-xl text-[11px] text-center transition">Cambio de planes</button>
                        <button type="button" onclick="selectCancelReason('Problema de horario')" class="cancel-reason-chip px-2.5 py-2 bg-stone-900 hover:bg-stone-800 border border-stone-700 hover:border-isivi-gold text-stone-300 rounded-xl text-[11px] text-center transition">Problema de horario</button>
                        <button type="button" onclick="selectCancelReason('Ya no necesito el servicio')" class="cancel-reason-chip px-2.5 py-2 bg-stone-900 hover:bg-stone-800 border border-stone-700 hover:border-isivi-gold text-stone-300 rounded-xl text-[11px] text-center transition">Ya no lo necesito</button>
                        <button type="button" onclick="selectCancelReason('Otro')" class="cancel-reason-chip px-2.5 py-2 bg-stone-900 hover:bg-stone-800 border border-stone-700 hover:border-isivi-gold text-stone-300 rounded-xl text-[11px] text-center transition">Otro</button>
                    </div>
                    <input id="client-cancel-custom-reason" type="text" placeholder="Cuéntanos brevemente el motivo..." class="w-full rounded-xl border border-stone-700 bg-stone-900 px-3.5 py-2.5 text-xs text-white focus:border-isivi-gold focus:outline-none">
                </div>
            </div>
            <div class="flex justify-end gap-2 pt-3 border-t border-stone-800">
                <button type="button" onclick="closeClientCancelModal()" class="px-4 py-2 bg-stone-800 hover:bg-stone-700 text-white rounded-xl text-xs font-bold">Volver</button>
                <button type="button" id="btn-submit-client-cancel" onclick="submitClientCancellationModal()" class="px-5 py-2 bg-red-800 hover:bg-red-700 text-white rounded-xl text-xs font-bold shadow flex items-center gap-1.5">
                    <i class="fa-solid fa-ban"></i> <span>Confirmar cancelación</span>
                </button>
            </div>
        </div>
    </div>

    <footer id="nosotros" class="bg-stone-950 text-isivi-300 py-12 border-t border-isivi-500/20 reveal-on-scroll">
        <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <div class="grid grid-cols-1 md:grid-cols-4 gap-8 mb-8">
                <div class="space-y-3">
                    <span class="font-serif-title text-2xl font-bold text-white tracking-widest">ISIVI</span>
                    <p class="text-xs text-isivi-300 leading-relaxed">Cuidado capilar artesanal sin sal ni sulfatos. Devolvemos la fuerza y brillo natural a tu hebra capilar.</p>
                    <p class="text-xs italic text-isivi-gold font-serif-title">¡Es tu turno de brillar!</p>
                </div>
                <div>
                    <h5 class="text-white font-bold text-sm mb-3">Contacto &amp; Envíos</h5>
                    <ul class="text-xs space-y-2 text-isivi-300">
                        <li><a id="footer-location-link" href="https://www.google.com/maps/search/?api=1&amp;query=Cartagena%2C%20Colombia" target="_blank" rel="noopener noreferrer" class="hover:text-isivi-gold transition"><i class="fa-solid fa-location-dot text-isivi-gold mr-2"></i> <span id="footer-location-text">Cartagena, Colombia</span></a></li>
                        <li><a href="https://wa.me/573008949050?text=Hola%20ISIVI%2C%20quiero%20informaci%C3%B3n%20sobre%20sus%20productos." target="_blank" rel="noopener noreferrer" class="hover:text-emerald-400 transition"><i class="fa-brands fa-whatsapp text-emerald-400 mr-2"></i> (+57) 300 894 9050 (Detal)</a></li>
                        <li><a id="footer-mayorista-link" href="https://wa.me/573009623174?text=Hola%20ISIVI%2C%20quiero%20informaci%C3%B3n%20para%20compras%20al%20por%20mayor." target="_blank" rel="noopener noreferrer" class="hover:text-emerald-400 transition"><i class="fa-brands fa-whatsapp text-emerald-400 mr-2"></i> <span id="footer-mayorista-text">(+57) 300 962 3174 (Mayorista)</span></a></li>
                        <li><a href="https://instagram.com/isivi_oficial21" target="_blank" rel="noopener noreferrer" class="hover:text-isivi-gold transition"><i class="fa-brands fa-instagram text-isivi-gold mr-2"></i> @isivi_oficial21</a></li>
                    </ul>
                </div>
                <div>
                    <h5 class="text-white font-bold text-sm mb-3">Ventas al Por Mayor</h5>
                    <ul class="text-xs space-y-1.5 text-isivi-300">
                        <li><strong class="text-white">Mayorista:</strong> Compra min. $200.000</li>
                        <li><strong class="text-white">Distribuidor:</strong> Compra min. $500.000</li>
                        <li class="text-isivi-gold pt-1"><i class="fa-solid fa-truck text-xs mr-1"></i> Envíos a todo el país</li>
                    </ul>
                </div>
                <div>
                    <h5 class="text-white font-bold text-sm mb-3">Políticas de Reserva</h5>
                    <p class="text-xs text-isivi-300 leading-relaxed">Toda reserva requiere el 25% de anticipo. Reagendamiento sin costo avisando con 24 horas de anticipación.</p>
                </div>
            </div>
            <div class="pt-6 border-t border-stone-900 flex flex-col sm:flex-row items-center justify-between text-xs text-stone-500 gap-4">
                <div>&copy; 2026 ISIVI - Cuidado Capilar Natural &amp; Peluquería. Todos los derechos reservados.</div>
                <button onclick="requestAdminAccess()" class="text-stone-600 hover:text-isivi-gold transition flex items-center gap-1.5 text-[11px] font-medium"><i class="fa-solid fa-lock text-[10px]"></i><span>Acceso Panel Administración</span></button>
            </div>
        </div>
    </footer>

    <!-- Barra Contextual Inteligente en Móvil (Mobile-Only Floating Action Bar) -->
    <aside id="mobile-context-bar" class="isivi-mobile-context-bar md:hidden is-hidden" aria-label="Barra de acción contextual">
        <div class="flex items-center justify-between gap-3 max-w-lg mx-auto">
            <div class="flex-1 min-w-0 pr-1">
                <div id="mobile-context-label" class="text-[10px] text-isivi-gold font-bold uppercase tracking-wider truncate flex items-center gap-1">
                    <i class="fa-solid fa-sparkles text-[9px]"></i> <span>ISIVI Peluquería</span>
                </div>
                <div id="mobile-context-subtext" class="text-xs text-white font-medium truncate">Agenda tu cita o pide productos</div>
            </div>
            <button id="mobile-context-btn" type="button" onclick="handleMobileContextAction()" class="px-4 py-2.5 rounded-xl bg-gradient-to-r from-isivi-500 to-isivi-600 hover:from-isivi-600 hover:to-isivi-700 text-white text-xs font-bold shadow-lg shadow-isivi-500/30 flex items-center gap-1.5 whitespace-nowrap active:scale-95 transition">
                <i id="mobile-context-icon" class="fa-solid fa-calendar-plus text-xs"></i>
                <span id="mobile-context-btn-text">Agendar</span>
            </button>
        </div>
    </aside>
</div>

<!-- ============ PAGE 2: ADMIN ============ -->
<div id="page-admin" class="hidden min-h-screen bg-stone-950 text-white flex flex-col justify-between">
    <div id="admin-user-modal" class="hidden fixed inset-0 z-50 bg-black/70 px-4 items-center justify-center">
        <form onsubmit="handleAdminUserSubmit(event)" class="w-full max-w-md rounded-2xl border border-isivi-500/30 bg-stone-900 p-6 shadow-2xl space-y-4">
            <div class="flex items-start justify-between gap-4"><div><h2 class="font-serif-title text-lg font-bold text-isivi-gold">Nuevo administrador</h2><p class="mt-1 text-xs text-isivi-300">Las credenciales se guardan de forma cifrada.</p></div><button type="button" onclick="hideAdminUserForm()" class="text-stone-400 hover:text-white"><i class="fa-solid fa-xmark"></i></button></div>
            <div><label class="mb-1 block text-xs text-isivi-300">Usuario *</label><input id="new-admin-username" type="text" required minlength="3" autocomplete="username" class="w-full rounded-xl border border-stone-700 bg-stone-950 px-3.5 py-2.5 text-sm text-white focus:border-isivi-gold focus:outline-none"></div>
            <div><label class="mb-1 block text-xs text-isivi-300">Contraseña *</label><input id="new-admin-password" type="password" required minlength="4" autocomplete="new-password" class="w-full rounded-xl border border-stone-700 bg-stone-950 px-3.5 py-2.5 text-sm text-white focus:border-isivi-gold focus:outline-none"></div>
            <p id="new-admin-error" class="hidden text-xs text-red-400"></p>
            <div class="flex justify-end gap-3 pt-1"><button type="button" onclick="hideAdminUserForm()" class="rounded-xl bg-stone-800 px-4 py-2.5 text-xs font-bold text-stone-200">Cancelar</button><button type="submit" class="rounded-xl bg-emerald-600 px-4 py-2.5 text-xs font-bold text-white hover:bg-emerald-700"><i class="fa-solid fa-user-plus mr-1"></i> Crear usuario</button></div>
        </form>
    </div>
    <header class="bg-isivi-900 border-b border-isivi-500/30 sticky top-0 z-40">
        <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-20 flex items-center justify-between">
            <div class="flex items-center gap-3">
                <div class="w-10 h-10 rounded-xl bg-isivi-gold text-isivi-black font-bold flex items-center justify-center text-lg shadow"><i class="fa-solid fa-sliders"></i></div>
                <div>
                    <h1 class="font-serif-title text-lg font-bold text-white flex items-center gap-2">ISIVI <span class="text-xs bg-amber-500/20 text-amber-300 border border-amber-500/40 px-2 py-0.5 rounded-full font-sans">Panel Admin</span></h1>
                    <p class="text-[11px] text-isivi-300">Gestor de Productos, Servicios, Fotos, Stock y Citas</p>
                </div>
            </div>
            <div class="flex items-center gap-2 sm:gap-3">
                <button onclick="showAdminUserForm()" class="bg-emerald-700 hover:bg-emerald-600 text-white px-3.5 py-2 rounded-xl text-xs font-semibold transition flex items-center gap-1.5 shadow"><i class="fa-solid fa-user-plus"></i><span class="hidden sm:inline">Nuevo administrador</span></button>
                <button onclick="navigateTo('client')" class="bg-stone-900 border border-isivi-500/40 hover:border-isivi-gold text-isivi-200 px-3.5 py-2 rounded-xl text-xs font-semibold transition flex items-center gap-1.5 shadow"><i class="fa-solid fa-globe text-isivi-gold"></i><span class="hidden sm:inline">Ver Sitio Web</span></button>
                <button onclick="logoutAdmin()" class="bg-red-950/80 border border-red-800/60 hover:bg-red-900 text-red-200 px-3.5 py-2 rounded-xl text-xs font-semibold transition flex items-center gap-1.5 shadow" title="Cerrar sesión"><i class="fa-solid fa-right-from-bracket"></i><span>Salir</span></button>
            </div>
        </div>
    </header>

    <div class="bg-stone-900 border-b border-stone-800 py-3 px-4">
        <div class="max-w-7xl mx-auto flex flex-wrap gap-2">
            <button id="adm-nav-dashboard" onclick="switchAdminTab('dashboard')" class="px-4 py-2 rounded-xl text-xs font-bold transition flex items-center gap-2 bg-isivi-gold text-isivi-black shadow"><i class="fa-solid fa-chart-pie"></i> Dashboard</button>
            <button id="adm-nav-bookings" onclick="switchAdminTab('bookings')" class="px-4 py-2 rounded-xl text-xs font-bold transition flex items-center gap-2 text-isivi-300 hover:text-white bg-stone-950 border border-stone-800"><i class="fa-solid fa-calendar-check"></i> Citas &amp; Agenda</button>
            <button id="adm-nav-orders" onclick="switchAdminTab('orders')" class="px-4 py-2 rounded-xl text-xs font-bold transition flex items-center gap-2 text-isivi-300 hover:text-white bg-stone-950 border border-stone-800"><i class="fa-solid fa-bag-shopping"></i> Pedidos <span id="adm-nav-orders-badge" class="hidden px-1.5 py-0.5 rounded-full text-[9px] font-bold bg-amber-500 text-stone-950">0</span></button>
            <button id="adm-nav-products" onclick="switchAdminTab('products')" class="px-4 py-2 rounded-xl text-xs font-bold transition flex items-center gap-2 text-isivi-300 hover:text-white bg-stone-950 border border-stone-800"><i class="fa-solid fa-boxes-stacked"></i> Productos &amp; Kits</button>
            <button id="adm-nav-services" onclick="switchAdminTab('services')" class="px-4 py-2 rounded-xl text-xs font-bold transition flex items-center gap-2 text-isivi-300 hover:text-white bg-stone-950 border border-stone-800"><i class="fa-solid fa-scissors"></i> Servicios Peluquería</button>
            <button id="adm-nav-history" onclick="switchAdminTab('history')" class="px-4 py-2 rounded-xl text-xs font-bold transition flex items-center gap-2 text-isivi-300 hover:text-white bg-stone-950 border border-stone-800"><i class="fa-solid fa-clock-rotate-left"></i> Historial</button>
            <button id="adm-nav-admins" onclick="switchAdminTab('admins')" class="px-4 py-2 rounded-xl text-xs font-bold transition flex items-center gap-2 text-isivi-300 hover:text-white bg-stone-950 border border-stone-800"><i class="fa-solid fa-users-gear"></i> Administradores</button>
            <button id="adm-nav-banners" onclick="switchAdminTab('banners')" class="px-4 py-2 rounded-xl text-xs font-bold transition flex items-center gap-2 text-isivi-300 hover:text-white bg-stone-950 border border-stone-800"><i class="fa-solid fa-panorama"></i> Banners</button>
            <button id="adm-nav-settings" onclick="switchAdminTab('settings')" class="px-4 py-2 rounded-xl text-xs font-bold transition flex items-center gap-2 text-isivi-300 hover:text-white bg-stone-950 border border-stone-800"><i class="fa-solid fa-store"></i> Configuración Negocio</button>
        </div>
    </div>

    <main class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 flex-1 w-full space-y-8">
        <!-- ============ TAB 0: DASHBOARD EJECUTIVO ============ -->
        <div id="adm-tab-dashboard" class="space-y-6">
            <!-- Header Resumen -->
            <div class="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between bg-isivi-900 border border-isivi-500/30 rounded-3xl p-6 shadow-xl">
                <div>
                    <h2 id="dash-greeting-title" class="font-serif-title text-2xl font-bold text-white flex items-center gap-2">
                        <i class="fa-solid fa-sparkles text-isivi-gold text-xl"></i>
                        <span>Bienvenido a ISIVI Admin</span>
                    </h2>
                    <button id="dash-refresh-btn" type="button" onclick="loadDashboardData()" class="rounded-xl border border-stone-700 bg-stone-950 px-4 py-2 text-xs font-bold text-isivi-gold hover:border-isivi-gold transition flex items-center gap-2 shadow hover:bg-stone-900 active:scale-95">
                        <i id="dash-refresh-icon" class="fa-solid fa-rotate text-[11px]"></i> <span>Actualizar</span>
                    </button>
                </div>
            </div>

            <!-- Banner de Error API (oculto por defecto) -->
            <div id="adm-dash-error-banner" class="hidden p-4 rounded-2xl bg-red-950/70 border border-red-500/40 text-red-200 flex items-center justify-between gap-3 shadow-lg">
                <div class="flex items-center gap-3">
                    <i class="fa-solid fa-triangle-exclamation text-red-400 text-lg"></i>
                    <p class="text-xs">No pudimos actualizar el resumen operativo. Verifica tu conexión e intenta nuevamente.</p>
                </div>
                <button type="button" onclick="loadDashboardData()" class="px-3 py-1 bg-red-900 hover:bg-red-800 text-white rounded-xl text-xs font-bold transition">Reintentar</button>
            </div>

            <!-- SECCIÓN COMPACTA: ATENCIÓN AHORA (solo visible cuando hay alertas activas) -->
            <div id="adm-dash-alerts-container" class="hidden bg-stone-950/90 border border-amber-500/40 rounded-3xl p-4 sm:p-5 shadow-xl space-y-3">
                <div class="flex items-center justify-between border-b border-stone-800 pb-2.5">
                    <div class="flex items-center gap-2">
                        <span class="w-2.5 h-2.5 rounded-full bg-amber-400 animate-ping"></span>
                        <h3 class="font-serif-title text-sm font-bold text-amber-300 uppercase tracking-wider flex items-center gap-2">
                            <i class="fa-solid fa-bell text-amber-400 isivi-bell-intermittent"></i> Atención Ahora
                        </h3>
                    </div>
                    <span class="text-[11px] text-isivi-300">Elementos que requieren tu intervención</span>
                </div>
                <div id="adm-dash-alerts-list" class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3">
                    <!-- Inyectado dinámicamente por JS -->
                </div>
            </div>

            <!-- TARJETAS KPI -->
            <!-- TARJETAS KPIS PRINCIPALES (Grid Responsive 5 KPIs) -->
            <div class="grid grid-cols-2 lg:grid-cols-5 gap-3 sm:gap-4">
                <!-- KPI 1: CITAS HOY -->
                <div class="bg-isivi-900 p-4 sm:p-5 rounded-2xl border border-isivi-500/30 shadow dashboard-card-hover flex flex-col justify-between">
                    <div class="flex items-center justify-between text-isivi-300 text-xs font-medium">
                        <span>Citas hoy</span>
                        <div class="w-8 h-8 rounded-xl bg-isivi-500/20 text-isivi-gold flex items-center justify-center text-sm"><i class="fa-solid fa-calendar-check"></i></div>
                    </div>
                    <p id="adm-dash-kpi-citas" class="text-2xl sm:text-3xl font-bold text-white mt-2">--</p>
                    <div id="adm-dash-kpi-citas-breakdown" class="text-[10px] text-isivi-300 mt-1 min-h-[16px]">
                        <span class="text-stone-400">Cargando turnos...</span>
                    </div>
                </div>

                <!-- KPI 2: PEDIDOS ACTIVOS -->
                <div class="bg-amber-950/30 p-4 sm:p-5 rounded-2xl border border-amber-500/40 shadow dashboard-card-hover flex flex-col justify-between cursor-pointer" onclick="switchAdminTab('orders')" title="Ver control de pedidos">
                    <div class="flex items-center justify-between text-amber-300 text-xs font-medium">
                        <span>Pedidos activos</span>
                        <div class="w-8 h-8 rounded-xl bg-amber-500/20 text-amber-300 flex items-center justify-center text-sm"><i class="fa-solid fa-bag-shopping"></i></div>
                    </div>
                    <p id="adm-dash-kpi-pedidos" class="text-2xl sm:text-3xl font-bold text-amber-300 mt-2">0</p>
                    <div id="adm-dash-kpi-pedidos-breakdown" class="text-[10px] text-amber-400/90 mt-1 min-h-[16px]">
                        <span class="text-stone-400">0 por preparar · 0 listos</span>
                    </div>
                </div>

                <!-- KPI 3: VENTAS HOY -->
                <div class="bg-emerald-950/40 p-4 sm:p-5 rounded-2xl border border-emerald-500/30 shadow dashboard-card-hover flex flex-col justify-between">
                    <div class="flex items-center justify-between text-emerald-400 text-xs font-medium">
                        <span>Ventas de hoy</span>
                        <div class="w-8 h-8 rounded-xl bg-emerald-500/20 text-emerald-300 flex items-center justify-center text-sm"><i class="fa-solid fa-sack-dollar"></i></div>
                    </div>
                    <p id="adm-dash-kpi-ventas" class="text-2xl sm:text-3xl font-bold text-emerald-300 mt-2">--</p>
                    <div id="adm-dash-kpi-ventas-breakdown" class="text-[10px] text-emerald-400/90 mt-1 min-h-[16px]">
                        <span class="text-stone-400">Cargando ingresos...</span>
                    </div>
                </div>

                <!-- KPI 4: PENDIENTES -->
                <div class="bg-amber-950/40 p-4 sm:p-5 rounded-2xl border border-amber-500/30 shadow dashboard-card-hover flex flex-col justify-between cursor-pointer" onclick="scrollToAdminAlerts()" title="Ver solicitudes pendientes">
                    <div class="flex items-center justify-between text-amber-400 text-xs font-medium">
                        <span>Pendientes</span>
                        <div class="w-8 h-8 rounded-xl bg-amber-500/20 text-amber-300 flex items-center justify-center text-sm"><i id="adm-dash-kpi-bell-icon" class="fa-solid fa-bell"></i></div>
                    </div>
                    <p id="adm-dash-kpi-pendientes" class="text-2xl sm:text-3xl font-bold text-amber-300 mt-2">--</p>
                    <div id="adm-dash-kpi-pendientes-breakdown" class="text-[10px] text-amber-400/90 mt-1 min-h-[16px]">
                        <span class="text-stone-400">Cargando solicitudes...</span>
                    </div>
                </div>

                <!-- KPI 5: STOCK BAJO -->
                <div class="bg-red-950/40 p-4 sm:p-5 rounded-2xl border border-red-500/30 shadow dashboard-card-hover flex flex-col justify-between col-span-2 lg:col-span-1">
                    <div class="flex items-center justify-between text-red-400 text-xs font-medium">
                        <span>Por reponer</span>
                        <div class="w-8 h-8 rounded-xl bg-red-500/20 text-red-300 flex items-center justify-center text-sm"><i class="fa-solid fa-boxes-packing"></i></div>
                    </div>
                    <p id="adm-dash-kpi-stock-bajo" class="text-2xl sm:text-3xl font-bold text-red-300 mt-2">--</p>
                    <div id="adm-dash-kpi-stock-breakdown" class="text-[10px] text-red-400/90 mt-1 min-h-[16px]">
                        <span class="text-stone-400">Cargando inventario...</span>
                    </div>
                </div>
            </div>


            <!-- CUADRICULA OPERATIVA EJECUTIVA (2 Columnas) -->
            <div class="grid grid-cols-1 lg:grid-cols-12 gap-6 items-start">
                <!-- Columna Izquierda: Próximas Citas + Agenda de Hoy + Pendientes -->
                <div class="lg:col-span-7 space-y-6">
                    <!-- PRÓXIMAS CITAS (Top 3 a 5 activas con countdown legible) -->
                    <div id="adm-dash-upcoming-card" class="bg-isivi-900 rounded-3xl border border-isivi-500/40 p-6 shadow-xl space-y-4">
                        <div class="flex items-center justify-between pb-3 border-b border-stone-800">
                            <div>
                                <h3 class="font-serif-title text-base font-bold text-isivi-gold flex items-center gap-2">
                                    <i class="fa-solid fa-clock-rotate-left"></i> Próximas Citas
                                </h3>
                                <p class="text-[11px] text-isivi-300">Anticipación operativa con tiempo restante en vivo</p>
                            </div>
                            <span id="adm-dash-upcoming-count-badge" class="px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-isivi-500/20 text-isivi-gold border border-isivi-500/30">0 próximas</span>
                        </div>
                        <div id="adm-dash-upcoming-list" class="space-y-3">
                            <div class="text-center py-5 text-xs text-isivi-300">No hay citas activas programadas próximamente.</div>
                        </div>
                    </div>

                    <!-- AGENDA DE HOY -->
                    <div class="bg-isivi-900 rounded-3xl border border-isivi-500/30 p-6 shadow-xl space-y-4">
                        <div class="flex items-center justify-between pb-3 border-b border-stone-800">
                            <div>
                                <h3 class="font-serif-title text-base font-bold text-white flex items-center gap-2">
                                    <i class="fa-solid fa-calendar-day text-isivi-gold"></i> Agenda de Hoy
                                </h3>
                                <p class="text-[11px] text-isivi-300">Turnos agendados en orden cronológico</p>
                            </div>
                            <button type="button" onclick="switchAdminTab('bookings')" class="text-xs text-isivi-gold hover:underline font-bold flex items-center gap-1">
                                <span>Ver agenda completa</span> <i class="fa-solid fa-arrow-right text-[10px]"></i>
                            </button>
                        </div>

                        <div id="adm-dash-today-list" class="space-y-2.5">
                            <div class="text-center py-6 text-xs text-isivi-300 animate-pulse">Cargando agenda de hoy...</div>
                        </div>
                    </div>

                    <!-- SOLICITUDES QUE REQUIEREN ACCIÓN -->
                    <div class="bg-isivi-900 rounded-3xl border border-amber-500/30 p-6 shadow-xl space-y-4">
                        <div class="flex items-center justify-between pb-3 border-b border-stone-800">
                            <div>
                                <h3 class="font-serif-title text-base font-bold text-amber-300 flex items-center gap-2">
                                    <i class="fa-solid fa-triangle-exclamation text-amber-400"></i> Solicitudes que Requieren Acción
                                </h3>
                                <p class="text-[11px] text-isivi-300">Prioridad: Reprogramaciones &gt; Transferencias por validar</p>
                            </div>
                            <span id="adm-dash-pending-count-badge" class="px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-amber-950 text-amber-300 border border-amber-500/30">0</span>
                        </div>
                        <div id="adm-dash-pending-list" class="space-y-2.5">
                            <div class="text-center py-6 text-xs text-isivi-300">No hay solicitudes pendientes por revisar.</div>
                        </div>
                    </div>
                </div>

                <!-- Columna Derecha: Stock Bajo + Resumen de Ventas -->
                <div class="lg:col-span-5 space-y-6">
                    <!-- STOCK BAJO -->
                    <div class="bg-isivi-900 rounded-3xl border border-red-500/30 p-6 shadow-xl space-y-4">
                        <div class="flex items-center justify-between pb-3 border-b border-stone-800">
                            <div>
                                <h3 class="font-serif-title text-base font-bold text-red-300 flex items-center gap-2">
                                    <i class="fa-solid fa-boxes-packing text-red-400"></i> Stock Bajo / Reponer
                                </h3>
                                <p class="text-[11px] text-isivi-300">Productos con 3 o menos existencias o agotados</p>
                            </div>
                            <button type="button" onclick="switchAdminTab('products')" class="text-xs text-isivi-gold hover:underline font-bold flex items-center gap-1">
                                <span>Ver inventario</span> <i class="fa-solid fa-arrow-right text-[10px]"></i>
                            </button>
                        </div>
                        <div id="adm-dash-low-stock-list" class="space-y-2 max-h-72 overflow-y-auto pr-1">
                            <div class="text-center py-6 text-xs text-isivi-300">Inventario en niveles óptimos.</div>
                        </div>
                    </div>

                    <!-- RESUMEN DE VENTAS -->
                    <div class="bg-isivi-900 rounded-3xl border border-isivi-500/30 p-6 shadow-xl space-y-4">
                        <div class="pb-3 border-b border-stone-800 flex items-center justify-between">
                            <div>
                                <h3 class="font-serif-title text-base font-bold text-white flex items-center gap-2">
                                    <i class="fa-solid fa-chart-simple text-isivi-gold"></i> Resumen de Ventas
                                </h3>
                                <p class="text-[11px] text-isivi-300">Ingresos confirmados del negocio</p>
                            </div>
                            <span id="adm-dash-ticket-prom-badge" class="text-[10px] font-mono text-isivi-gold bg-stone-950 px-2.5 py-1 rounded-xl border border-stone-800">Ticket prom: $0</span>
                        </div>
                        <div class="grid grid-cols-3 gap-2 text-center">
                            <div class="bg-stone-950 p-3 rounded-2xl border border-stone-800 flex flex-col justify-between">
                                <span class="text-[10px] uppercase tracking-wide text-isivi-300 block">Hoy</span>
                                <span id="adm-dash-sales-today" class="font-bold text-white text-xs block mt-1">$0</span>
                            </div>
                            <div class="bg-stone-950 p-3 rounded-2xl border border-stone-800 flex flex-col justify-between">
                                <span class="text-[10px] uppercase tracking-wide text-isivi-300 block">Esta semana</span>
                                <span id="adm-dash-sales-week" class="font-bold text-emerald-400 text-xs block mt-1">$0</span>
                            </div>
                            <div class="bg-stone-950 p-3 rounded-2xl border border-stone-800 flex flex-col justify-between">
                                <span class="text-[10px] uppercase tracking-wide text-isivi-300 block">Este mes</span>
                                <span id="adm-dash-sales-month" class="font-bold text-isivi-gold text-xs block mt-1">$0</span>
                            </div>
                        </div>

                        <!-- Desglose Servicios vs Productos -->
                        <div id="adm-dash-sales-breakdown-row" class="grid grid-cols-2 gap-2 pt-1 text-[11px]">
                            <div class="bg-stone-950/80 rounded-xl p-2.5 border border-stone-800/80 flex items-center justify-between">
                                <span class="text-stone-400 flex items-center gap-1.5"><i class="fa-solid fa-scissors text-isivi-gold text-[10px]"></i> Servicios</span>
                                <span id="adm-dash-sales-servicios" class="font-bold text-white text-[11px]">$0</span>
                            </div>
                            <div class="bg-stone-950/80 rounded-xl p-2.5 border border-stone-800/80 flex items-center justify-between">
                                <span class="text-stone-400 flex items-center gap-1.5"><i class="fa-solid fa-boxes-stacked text-emerald-400 text-[10px]"></i> Productos</span>
                                <span id="adm-dash-sales-productos" class="font-bold text-white text-[11px]">$0</span>
                            </div>
                        </div>

                        <!-- Mini gráfico de últimos 7 días -->
                        <div class="space-y-2 pt-2 border-t border-stone-800">
                            <div class="flex items-center justify-between">
                                <span class="text-[10px] font-bold text-isivi-300 uppercase tracking-wider block">Últimos 7 días</span>
                                <span class="text-[10px] text-stone-500">Historial diario</span>
                            </div>
                            <div id="adm-dash-chart-container" class="bg-stone-950 rounded-2xl p-4 border border-stone-800 min-h-[140px] flex items-center justify-center"></div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div id="adm-tab-admins" class="hidden space-y-6">
            <div class="bg-isivi-900 rounded-3xl border border-isivi-500/30 p-6 shadow-xl">
                <div class="mb-5 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between"><div><h3 class="font-serif-title text-lg font-bold text-isivi-gold"><i class="fa-solid fa-users-gear mr-2"></i>Administradores</h3><p class="mt-1 text-xs text-isivi-300">Gestiona los usuarios que pueden acceder al panel.</p></div><button type="button" onclick="showAdminUserForm()" class="rounded-xl bg-emerald-700 px-4 py-2 text-xs font-bold text-white hover:bg-emerald-600"><i class="fa-solid fa-user-plus mr-1"></i>Nuevo administrador</button></div>
                <div class="overflow-x-auto rounded-2xl border border-stone-800"><table class="w-full text-left text-xs"><thead class="bg-stone-950 text-isivi-gold"><tr><th class="p-3">Usuario</th><th class="p-3 text-right">Acción</th></tr></thead><tbody id="adm-admins-table-body" class="divide-y divide-stone-800"></tbody></table></div>
                <p class="mt-3 text-[10px] text-isivi-300">Por seguridad, el último administrador no se puede eliminar.</p>
            </div>
        </div>

        <div id="adm-tab-banners" class="hidden space-y-6">
            <div class="rounded-3xl border border-isivi-500/30 bg-isivi-900 p-6 shadow-xl"><div class="mb-5"><h3 class="font-serif-title text-lg font-bold text-isivi-gold"><i class="fa-solid fa-panorama mr-2"></i>Nuevo banner</h3><p class="mt-1 text-xs text-isivi-300">Aparecerá entre la portada y los servicios del sitio.</p></div><form onsubmit="handleBannerSubmit(event)" class="space-y-4"><div class="grid grid-cols-1 gap-4 sm:grid-cols-2"><input id="banner-title" required placeholder="Título del banner" class="rounded-xl border border-stone-700 bg-stone-950 px-3.5 py-2.5 text-xs text-white focus:border-isivi-gold focus:outline-none"><div><input id="banner-image" required type="file" accept="image/png,image/jpeg,image/webp" class="w-full rounded-xl border border-stone-700 bg-stone-950 px-3.5 py-2.5 text-xs text-white file:mr-3 file:rounded-lg file:border-0 file:bg-isivi-gold file:px-3 file:py-1.5 file:text-xs file:font-bold file:text-isivi-black hover:file:bg-yellow-500 focus:border-isivi-gold focus:outline-none"><p class="mt-1 text-[10px] text-isivi-300">JPG, PNG o WebP; máximo 5 MB.</p></div><input id="banner-button-text" placeholder="Texto del botón (ej. Ver promoción)" class="rounded-xl border border-stone-700 bg-stone-950 px-3.5 py-2.5 text-xs text-white focus:border-isivi-gold focus:outline-none"><input id="banner-button-link" placeholder="Enlace del botón (ej. #servicios)" class="rounded-xl border border-stone-700 bg-stone-950 px-3.5 py-2.5 text-xs text-white focus:border-isivi-gold focus:outline-none"></div><textarea id="banner-description" required rows="2" placeholder="Descripción breve de la promoción" class="w-full rounded-xl border border-stone-700 bg-stone-950 px-3.5 py-2.5 text-xs text-white focus:border-isivi-gold focus:outline-none"></textarea><div class="flex justify-end"><button class="rounded-xl bg-isivi-gold px-5 py-2.5 text-xs font-bold text-isivi-black hover:bg-yellow-500"><i class="fa-solid fa-plus mr-1"></i>Crear banner</button></div></form></div>
            <div class="rounded-3xl border border-stone-800 bg-isivi-900 p-6 shadow-xl"><h3 class="mb-4 font-serif-title text-lg font-bold text-white">Banners publicados</h3><div id="adm-banners-list" class="grid gap-4 md:grid-cols-2"></div></div>
        </div>

        <div id="adm-tab-products" class="hidden space-y-6">
            <!-- Sección: Categorías de Productos -->
            <div class="bg-isivi-900 rounded-3xl p-6 border border-stone-800 shadow-xl space-y-4">
                <div>
                    <h4 class="font-serif-title font-bold text-white text-base flex items-center gap-2"><i class="fa-solid fa-tags text-isivi-gold"></i> Categorías de Productos</h4>
                    <p class="mt-1 text-xs text-isivi-300">Organiza tu catálogo. Las categorías activas se muestran como filtros a los clientes.</p>
                </div>
                <div id="product-category-form-error" class="hidden bg-red-950/90 border border-red-500/80 p-3 rounded-2xl text-red-200 text-xs flex items-center gap-2" role="alert">
                    <i class="fa-solid fa-circle-exclamation text-red-400"></i>
                    <span></span>
                </div>
                <form onsubmit="handleProductCategorySubmit(event)" class="flex flex-col gap-3 sm:flex-row">
                    <input id="product-category-name" required maxlength="50" placeholder="Ej.: Champús y Tratamientos" class="flex-1 rounded-xl border border-stone-800 bg-stone-950 px-3.5 py-2.5 text-xs text-white focus:border-isivi-gold focus:outline-none">
                    <button type="submit" class="rounded-xl bg-isivi-gold px-5 py-2.5 text-xs font-bold text-isivi-black hover:bg-yellow-500 transition flex items-center justify-center gap-1.5 shadow">
                        <i class="fa-solid fa-plus mr-1"></i>Añadir categoría
                    </button>
                </form>
                <div id="admin-product-categories" class="flex flex-wrap gap-2"></div>
            </div>

            <!-- Formulario: Producto / Kit -->
            <div class="bg-isivi-900 p-6 rounded-3xl border border-isivi-500/40 shadow-xl space-y-4">
                <div class="flex justify-between items-center border-b border-stone-800 pb-4">
                    <h3 id="prod-form-title" class="font-serif-title text-lg font-bold text-isivi-gold flex items-center gap-2"><i class="fa-solid fa-circle-plus"></i> Crear Nuevo Producto o Kit Capilar</h3>
                    <button id="cancel-prod-edit-btn" onclick="resetProductForm()" class="hidden text-xs bg-stone-800 text-stone-300 px-3 py-1.5 rounded-lg hover:text-white">Cancelar Edición</button>
                </div>
                <form id="product-editor-form" onsubmit="handleProductFormSubmit(event)" class="space-y-4">
                    <input type="hidden" id="prod-edit-id" value="">
                    <div id="prod-form-error" class="hidden bg-red-950/90 border border-red-500/80 p-3 rounded-2xl text-red-200 text-xs flex items-center gap-2" role="alert">
                        <i class="fa-solid fa-circle-exclamation text-red-400"></i>
                        <span></span>
                    </div>
                    <div class="grid grid-cols-1 sm:grid-cols-4 gap-4">
                        <div class="sm:col-span-2"><label class="block text-xs text-isivi-300 mb-1">Nombre del Producto / Kit *</label><input type="text" id="prod-name" required placeholder="Ej: Shampoo de Romero &amp; Ortiga" class="w-full bg-stone-950 border border-stone-800 rounded-xl px-3.5 py-2.5 text-xs text-white focus:outline-none focus:border-isivi-gold"></div>
                        <div><label class="block text-xs text-isivi-300 mb-1">Tipo de Item</label><select id="prod-type" onchange="onProductTypeChange()" class="w-full bg-stone-950 border border-stone-800 rounded-xl px-3.5 py-2.5 text-xs text-white focus:outline-none focus:border-isivi-gold"><option value="product">Producto Individual (Detal)</option><option value="kit">Kit Capilar Especial</option></select></div>
                        <div id="prod-category-wrap"><label class="block text-xs text-isivi-300 mb-1">Categoría</label><select id="prod-category" class="w-full bg-stone-950 border border-stone-800 rounded-xl px-3.5 py-2.5 text-xs text-white focus:outline-none focus:border-isivi-gold"></select></div>
                    </div>
                    <div class="grid grid-cols-1 sm:grid-cols-3 gap-4">
                        <div id="prod-price-type-wrap"><label class="block text-xs text-isivi-300 mb-1">Modelo de Precios</label><select id="prod-price-type" onchange="toggleProdPriceTypeFields()" class="w-full bg-stone-950 border border-stone-800 rounded-xl px-3.5 py-2.5 text-xs text-white focus:outline-none focus:border-isivi-gold"><option value="UNICO">Precio Único</option><option value="VARIANTES">Precio por Modelo / Variante</option></select></div>
                        <div id="prod-single-price-wrap"><label class="block text-xs text-isivi-300 mb-1">Precio ($ COP) *</label><input type="text" inputmode="numeric" id="prod-price" placeholder="Ej: 30.000" class="w-full bg-stone-950 border border-stone-800 rounded-xl px-3.5 py-2.5 text-xs text-white focus:outline-none focus:border-isivi-gold"></div>
                        <div id="prod-single-qty-wrap"><label class="block text-xs text-isivi-300 mb-1">Cantidad en inventario *</label><input type="number" id="prod-quantity" min="0" step="1" value="1" placeholder="Ej: 10" class="w-full bg-stone-950 border border-stone-800 rounded-xl px-3.5 py-2.5 text-xs text-white focus:outline-none focus:border-isivi-gold"><p class="mt-1 text-[10px] text-isivi-300">Stock total.</p></div>
                    </div>

                    <!-- Editor de Variantes Dinámicas -->
                    <div id="prod-variants-section" class="hidden p-4 rounded-2xl border border-isivi-500/30 bg-stone-950 space-y-3">
                        <div class="flex items-center justify-between">
                            <label class="text-xs font-bold text-isivi-gold uppercase tracking-wider flex items-center gap-1.5"><i class="fa-solid fa-layer-group"></i> Variantes / Modelos y Precios</label>
                            <span class="text-[10px] text-isivi-300">Cada variante define su precio y stock</span>
                        </div>
                        <div id="prod-variants-list" class="space-y-2 max-h-48 overflow-y-auto"></div>
                        <div class="grid grid-cols-1 sm:grid-cols-4 gap-2 pt-2 border-t border-stone-800">
                            <input type="text" id="new-var-name" placeholder="Nombre (ej. 250 ml o Modelo A)" class="sm:col-span-2 rounded-xl border border-stone-800 bg-stone-900 px-3 py-2 text-xs text-white focus:border-isivi-gold focus:outline-none">
                            <input type="text" inputmode="numeric" id="new-var-price" placeholder="Precio ($ COP, ej: 30.000)" class="rounded-xl border border-stone-800 bg-stone-900 px-3 py-2 text-xs text-white focus:border-isivi-gold focus:outline-none">
                            <div class="flex gap-2">
                                <input type="number" id="new-var-qty" min="0" value="5" placeholder="Stock" class="w-20 rounded-xl border border-stone-800 bg-stone-900 px-2.5 py-2 text-xs text-white focus:border-isivi-gold focus:outline-none">
                                <button type="button" onclick="addVariantToForm()" class="flex-1 rounded-xl bg-isivi-gold text-isivi-black font-bold text-xs hover:bg-yellow-500 transition shadow flex items-center justify-center gap-1">
                                    <i class="fa-solid fa-plus"></i> Añadir
                                </button>
                            </div>
                        </div>
                    </div>

                    <div class="grid grid-cols-1 sm:grid-cols-3 gap-4">
                        <div class="sm:col-span-2"><label class="block text-xs text-isivi-300 mb-1">Foto del Producto / Kit *</label><input type="file" id="prod-img" accept="image/png,image/jpeg,image/webp" class="w-full bg-stone-950 border border-stone-800 rounded-xl px-3.5 py-2.5 text-xs text-white file:mr-3 file:rounded-lg file:border-0 file:bg-isivi-gold file:px-3 file:py-1.5 file:text-xs file:font-bold file:text-isivi-black hover:file:bg-yellow-500 focus:outline-none focus:border-isivi-gold"><p id="prod-img-help" class="mt-1 text-[10px] text-isivi-300">Selecciona una imagen JPG, PNG o WebP (máximo 5 MB).</p></div>
                        <div><label class="block text-xs text-isivi-300 mb-1">Estado Inicial de Stock</label><select id="prod-stock" class="w-full bg-stone-950 border border-stone-800 rounded-xl px-3.5 py-2.5 text-xs text-white focus:outline-none focus:border-isivi-gold"><option value="true">En Stock (Disponible)</option><option value="false">Agotado (Sin Stock)</option></select></div>
                    </div>
                    <div><label class="block text-xs text-isivi-300 mb-1">Descripción Breve o Beneficios</label><textarea id="prod-desc" rows="2" placeholder="Describe los beneficios o ingredientes principales..." class="w-full bg-stone-950 border border-stone-800 rounded-xl px-3.5 py-2.5 text-xs text-white focus:outline-none focus:border-isivi-gold"></textarea></div>
                    <div class="flex justify-end gap-3 pt-2"><button type="submit" id="prod-form-submit-btn" class="bg-emerald-600 hover:bg-emerald-700 text-white text-xs font-bold px-6 py-3 rounded-xl transition flex items-center gap-2 shadow"><i class="fa-solid fa-floppy-disk"></i> Guardar Producto / Kit</button></div>
                </form>
            </div>
            <div class="bg-isivi-900 rounded-3xl p-6 border border-stone-800 shadow-xl space-y-4">
                <div class="flex flex-col sm:flex-row justify-between sm:items-center gap-3">
                    <div>
                        <h4 class="font-serif-title font-bold text-white text-base flex items-center gap-2">
                            <i class="fa-solid fa-boxes-stacked text-isivi-gold"></i>
                            Inventario de Productos &amp; Kits
                            <span id="adm-products-count-badge" class="px-2 py-0.5 rounded-full text-[10px] font-bold bg-stone-950 text-isivi-gold border border-stone-800"></span>
                        </h4>
                        <span class="text-xs text-isivi-300">Haz clic en <strong>Stock</strong> para alternar disponibilidad en vivo o en <strong>Editar</strong> para reponer inventario.</span>
                    </div>
                </div>

                <!-- Filtros y Búsqueda de Productos -->
                <div class="flex flex-col sm:flex-row items-stretch sm:items-center justify-between gap-3 pt-2 border-t border-stone-800">
                    <div class="flex flex-wrap items-center gap-1.5" id="adm-products-filter-pills">
                        <button type="button" id="adm-prod-pill-TODOS" onclick="filterProductsByStatus('TODOS')" class="adm-prod-filter-pill px-3 py-1.5 rounded-xl text-xs font-bold transition bg-isivi-gold text-isivi-black shadow-sm">Todos</button>
                        <button type="button" id="adm-prod-pill-EN_STOCK" onclick="filterProductsByStatus('EN_STOCK')" class="adm-prod-filter-pill px-3 py-1.5 rounded-xl text-xs font-bold transition bg-stone-950 text-stone-300 border border-stone-800 hover:border-isivi-gold">En Stock</button>
                        <button type="button" id="adm-prod-pill-AGOTADOS" onclick="filterProductsByStatus('AGOTADOS')" class="adm-prod-filter-pill px-3 py-1.5 rounded-xl text-xs font-bold transition bg-stone-950 text-stone-300 border border-stone-800 hover:border-isivi-gold flex items-center gap-1"><span class="w-2 h-2 rounded-full bg-red-500"></span> Agotados</button>
                        <button type="button" id="adm-prod-pill-STOCK_BAJO" onclick="filterProductsByStatus('STOCK_BAJO')" class="adm-prod-filter-pill px-3 py-1.5 rounded-xl text-xs font-bold transition bg-stone-950 text-stone-300 border border-stone-800 hover:border-isivi-gold flex items-center gap-1"><span class="w-2 h-2 rounded-full bg-amber-500"></span> Stock Bajo (≤ 3)</button>
                    </div>
                    <div class="relative min-w-[200px] sm:w-64">
                        <input id="adm-products-search" type="search" oninput="applyAdminProductFilters()" placeholder="Buscar producto o kit..." class="w-full rounded-xl border border-stone-800 bg-stone-950 px-3.5 py-2 text-xs text-white placeholder-stone-500 focus:border-isivi-gold focus:outline-none">
                    </div>
                </div>

                <div class="overflow-x-auto rounded-2xl border border-stone-800 bg-stone-950">
                    <table class="w-full text-left text-xs">
                        <thead class="bg-stone-900 text-isivi-gold uppercase font-bold text-[10px] tracking-wider border-b border-stone-800">
                            <tr><th class="p-3">Foto</th><th class="p-3">Nombre &amp; Tipo</th><th class="p-3">Categoría</th><th class="p-3">Precio</th><th class="p-3 text-center">Cantidad</th><th class="p-3 text-center">Estado de Stock</th><th class="p-3 text-right">Acciones</th></tr>
                        </thead>
                        <tbody id="adm-products-table-body" class="divide-y divide-stone-800"></tbody>
                    </table>
                </div>
            </div>
        </div>

        <div id="adm-tab-services" class="hidden space-y-6">
            <div class="bg-isivi-900 p-6 rounded-3xl border border-isivi-500/40 shadow-xl space-y-4">
                <div class="flex justify-between items-center border-b border-stone-800 pb-4">
                    <h3 id="serv-form-title" class="font-serif-title text-lg font-bold text-isivi-gold flex items-center gap-2"><i class="fa-solid fa-scissors"></i> Crear Nuevo Servicio de Peluquería</h3>
                    <button id="cancel-serv-edit-btn" onclick="resetServiceForm()" class="hidden text-xs bg-stone-800 text-stone-300 px-3 py-1.5 rounded-lg hover:text-white">Cancelar Edición</button>
                </div>
                <form id="service-editor-form" onsubmit="handleServiceFormSubmit(event)" class="space-y-4">
                    <input type="hidden" id="serv-edit-id" value="">
                    <div id="serv-form-error" class="hidden bg-red-950/90 border border-red-500/80 p-3 rounded-2xl text-red-200 text-xs flex items-center gap-2" role="alert">
                        <i class="fa-solid fa-circle-exclamation text-red-400"></i>
                        <span></span>
                    </div>
                    <div class="grid grid-cols-1 sm:grid-cols-3 gap-4">
                        <div><label class="block text-xs text-isivi-300 mb-1">Nombre del Servicio *</label><input type="text" id="serv-name" required placeholder="Ej: Repolarización Térmica" class="w-full bg-stone-950 border border-stone-800 rounded-xl px-3.5 py-2.5 text-xs text-white focus:outline-none focus:border-isivi-gold"></div>
                        <div><label class="block text-xs text-isivi-300 mb-1">Categoría</label><select id="serv-category" required class="w-full bg-stone-950 border border-stone-800 rounded-xl px-3.5 py-2.5 text-xs text-white focus:outline-none focus:border-isivi-gold"></select></div>
                        <div><label class="block text-xs text-isivi-300 mb-1">Precio ($ COP) *</label><input type="text" inputmode="numeric" id="serv-price" required placeholder="Ej: 75.000" class="w-full bg-stone-950 border border-stone-800 rounded-xl px-3.5 py-2.5 text-xs text-white focus:outline-none focus:border-isivi-gold"></div>
                    </div>
                    <div class="grid grid-cols-1 sm:grid-cols-3 gap-4">
                        <div><label class="block text-xs text-isivi-300 mb-1">Duración Estimada</label><input type="text" id="serv-duration" placeholder="Ej: 60 min" class="w-full bg-stone-950 border border-stone-800 rounded-xl px-3.5 py-2.5 text-xs text-white focus:outline-none focus:border-isivi-gold"></div>
                        <div class="sm:col-span-2"><label class="block text-xs text-isivi-300 mb-1">Foto del Servicio *</label><input type="file" id="serv-img" accept="image/png,image/jpeg,image/webp" class="w-full bg-stone-950 border border-stone-800 rounded-xl px-3.5 py-2.5 text-xs text-white file:mr-3 file:rounded-lg file:border-0 file:bg-isivi-gold file:px-3 file:py-1.5 file:text-xs file:font-bold file:text-isivi-black hover:file:bg-yellow-500 focus:outline-none focus:border-isivi-gold"><p id="serv-img-help" class="mt-1 text-[10px] text-isivi-300">Selecciona una imagen JPG, PNG o WebP (máximo 5 MB).</p></div>
                    </div>
                    <div><label class="block text-xs text-isivi-300 mb-1">Descripción del Servicio</label><textarea id="serv-desc" rows="2" placeholder="Detalla los procedimientos incluidos..." class="w-full bg-stone-950 border border-stone-800 rounded-xl px-3.5 py-2.5 text-xs text-white focus:outline-none focus:border-isivi-gold"></textarea></div>
                    <div class="flex justify-end gap-3 pt-2"><button type="submit" id="serv-form-submit-btn" class="bg-emerald-600 hover:bg-emerald-700 text-white text-xs font-bold px-6 py-3 rounded-xl transition flex items-center gap-2 shadow"><i class="fa-solid fa-floppy-disk"></i> Guardar Servicio</button></div>
                </form>
            </div>
            <div class="bg-isivi-900 rounded-3xl p-6 border border-stone-800 shadow-xl space-y-4">
                <div><h4 class="font-serif-title font-bold text-white text-base">Categorías de servicios</h4><p class="mt-1 text-xs text-isivi-300">Cada categoría creada aparecerá como una pestaña en Servicios.</p></div>
                <form onsubmit="handleCategorySubmit(event)" class="flex flex-col gap-3 sm:flex-row"><input id="service-category-name" required maxlength="50" placeholder="Ej.: Manicure y pedicure" class="flex-1 rounded-xl border border-stone-800 bg-stone-950 px-3.5 py-2.5 text-xs text-white focus:border-isivi-gold focus:outline-none"><button class="rounded-xl bg-isivi-gold px-5 py-2.5 text-xs font-bold text-isivi-black hover:bg-yellow-500"><i class="fa-solid fa-plus mr-1"></i>Añadir categoría</button></form>
                <div id="admin-service-categories" class="flex flex-wrap gap-2"></div>
            </div>
            <div class="bg-isivi-900 rounded-3xl p-6 border border-stone-800 shadow-xl space-y-4">
                <h4 class="font-serif-title font-bold text-white text-base">Servicios de Peluquería Registrados</h4>
                <div class="overflow-x-auto rounded-2xl border border-stone-800 bg-stone-950">
                    <table class="w-full text-left text-xs">
                        <thead class="bg-stone-900 text-isivi-gold uppercase font-bold text-[10px] tracking-wider border-b border-stone-800">
                            <tr><th class="p-3">Foto</th><th class="p-3">Servicio</th><th class="p-3">Categoría &amp; Duración</th><th class="p-3">Precio</th><th class="p-3 text-right">Acciones</th></tr>
                        </thead>
                        <tbody id="adm-services-table-body" class="divide-y divide-stone-800"></tbody>
                    </table>
                </div>
            </div>
        </div>

        <div id="adm-tab-bookings" class="hidden space-y-6">
            <!-- ============ SECCIÓN 1: HORARIO SEMANAL DEL SALÓN ============ -->
            <div class="bg-isivi-900 rounded-3xl p-6 border border-isivi-500/30 shadow-xl space-y-4">
                <div class="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between pb-3 border-b border-stone-800">
                    <div>
                        <h3 class="font-serif-title text-base font-bold text-white flex items-center gap-2">
                            <i class="fa-solid fa-clock text-isivi-gold"></i> Horario Semanal del Salón
                        </h3>
                        <p class="text-xs text-isivi-300">Días habituales de atención y turnos configurados para reservas.</p>
                    </div>
                    <button type="button" onclick="openAdminWeeklyScheduleModal()" class="rounded-xl bg-stone-950 border border-isivi-500/40 px-4 py-2 text-xs font-bold text-isivi-gold hover:border-isivi-gold transition flex items-center gap-1.5 shadow">
                        <i class="fa-solid fa-pen-to-square text-xs"></i> <span>Editar horario semanal</span>
                    </button>
                </div>
                <!-- Tarjetas de Lunes a Domingo -->
                <div id="admin-weekly-schedule-cards" class="grid grid-cols-2 sm:grid-cols-4 lg:grid-cols-7 gap-2.5">
                    <!-- Inyectado dinámicamente por JS -->
                </div>
            </div>

            <!-- ============ SECCIÓN 2: EXCEPCIONES Y BLOQUEOS PRÓXIMOS ============ -->
            <div class="bg-isivi-900 rounded-3xl p-6 border border-isivi-500/30 shadow-xl space-y-4">
                <div class="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between pb-3 border-b border-stone-800">
                    <div>
                        <h3 class="font-serif-title text-base font-bold text-white flex items-center gap-2">
                            <i class="fa-solid fa-calendar-xmark text-isivi-gold"></i> Excepciones y Horarios Especiales
                        </h3>
                        <p class="text-xs text-isivi-300">Días cerrados por festivos/mantenimiento, horarios especiales y bloqueos de rango.</p>
                    </div>
                    <div class="flex flex-wrap items-center gap-2">
                        <button type="button" onclick="openAdminRecurringBlockModal()" class="rounded-xl bg-purple-950/80 border border-purple-500/40 px-3.5 py-2 text-xs font-bold text-purple-300 hover:bg-purple-900 transition flex items-center gap-1.5 shadow">
                            <i class="fa-solid fa-arrows-rotate text-xs"></i> <span>+ Bloqueo recurrente</span>
                        </button>
                        <button type="button" onclick="openAdminBlockRangeModal()" class="rounded-xl bg-amber-950/80 border border-amber-500/40 px-3.5 py-2 text-xs font-bold text-amber-300 hover:bg-amber-900 transition flex items-center gap-1.5 shadow">
                            <i class="fa-solid fa-lock text-xs"></i> <span>+ Bloquear horario</span>
                        </button>
                        <button type="button" onclick="openAdminExceptionModal()" class="rounded-xl bg-stone-950 border border-stone-700 px-3.5 py-2 text-xs font-bold text-white hover:border-isivi-gold transition flex items-center gap-1.5 shadow">
                            <i class="fa-solid fa-calendar-plus text-isivi-gold text-xs"></i> <span>+ Nueva excepción</span>
                        </button>
                    </div>
                </div>
                <div class="space-y-3">
                    <div id="admin-recurring-blocks-list" class="space-y-2">
                        <!-- Inyectado dinámicamente por JS -->
                    </div>
                    <div id="admin-exceptions-list" class="space-y-2">
                        <!-- Inyectado dinámicamente por JS -->
                    </div>
                </div>
            </div>


            <!-- ============ SECCIÓN 3: AGENDA SEMANAL INTERACTIVA ============ -->
            <div class="bg-isivi-900 rounded-3xl p-6 border border-isivi-500/30 shadow-xl space-y-4">
                <div class="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between"><div><h3 class="font-serif-title text-lg font-bold text-white"><i class="fa-solid fa-calendar-week mr-2 text-isivi-gold"></i>Agenda semanal</h3><p class="mt-1 text-xs text-isivi-300">Visualiza la disponibilidad y las citas de toda la semana. Haz clic en un horario para gestionarlo.</p></div><div class="flex items-center gap-2"><button type="button" onclick="changeAdminWeek(-1)" aria-label="Semana anterior" class="flex h-9 w-9 items-center justify-center rounded-xl border border-stone-700 bg-stone-950 text-isivi-gold hover:border-isivi-gold"><i class="fa-solid fa-chevron-left text-xs"></i></button><span id="admin-week-label" class="min-w-40 text-center text-xs font-bold text-isivi-gold"></span><button type="button" onclick="changeAdminWeek(1)" aria-label="Semana siguiente" class="flex h-9 w-9 items-center justify-center rounded-xl border border-stone-700 bg-stone-950 text-isivi-gold hover:border-isivi-gold"><i class="fa-solid fa-chevron-right text-xs"></i></button></div></div>
                <div class="flex flex-wrap gap-x-4 gap-y-1 text-[10px] text-isivi-300"><span><i class="fa-solid fa-circle mr-1 text-stone-500"></i>Disponible</span><span><i class="fa-solid fa-circle mr-1 text-amber-400"></i>Pendiente</span><span><i class="fa-solid fa-circle mr-1 text-emerald-400"></i>Confirmada</span><span><i class="fa-solid fa-circle mr-1 text-red-400"></i>Bloqueado</span></div>
                <div class="overflow-x-auto rounded-2xl border border-stone-800 bg-stone-950"><div id="admin-week-calendar" class="min-w-[780px]"></div></div>
            </div>

            <!-- ============ SECCIÓN 4: GESTIÓN DE DISPONIBILIDAD POR FECHA ============ -->
            <div class="bg-isivi-900 rounded-3xl p-6 border border-isivi-500/30 shadow-xl space-y-4">
                <div class="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between"><div><h4 class="font-serif-title font-bold text-white text-base"><i class="fa-solid fa-calendar-days text-isivi-gold mr-2"></i>Disponibilidad y Bloqueos por Fecha</h4><p class="text-xs text-isivi-300">Gestionando la fecha: <span id="admin-schedule-date-label" class="font-bold text-isivi-gold">—</span></p></div><input id="admin-schedule-date" type="date" onchange="selectAdminScheduleDate()" class="rounded-xl border border-stone-700 bg-stone-950 px-3 py-2 text-xs text-white focus:border-isivi-gold focus:outline-none"></div>
                <div id="admin-schedule-slots" class="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-7 gap-2.5"></div>
                <p class="text-[10px] text-isivi-300"><span class="text-emerald-400">Disponible</span> · <span class="text-red-400">Bloqueado / Ocupado manualmente</span> · <span class="text-amber-400">Reservado por cliente</span></p>
            </div>
            <div class="bg-isivi-900 rounded-3xl p-6 border border-stone-800 shadow-xl space-y-4">
                <div class="flex justify-between items-center">
                    <div><h4 class="font-serif-title font-bold text-white text-base">Citas Activas</h4><p class="text-xs text-isivi-300">Consulta y gestiona las citas programadas. Las citas pasan al historial al día siguiente de su fecha.</p></div>
                </div>
                <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-5 gap-3 rounded-2xl border border-stone-800 bg-stone-950 p-3">
                    <div class="lg:col-span-2"><label class="mb-1 block text-[10px] font-bold uppercase tracking-wide text-isivi-300">Cliente o servicio</label><input id="booking-filter-text" type="search" oninput="applyBookingFilters()" placeholder="Buscar por nombre o servicio..." class="w-full rounded-xl border border-stone-800 bg-stone-900 px-3 py-2 text-xs text-white placeholder-stone-500 focus:border-isivi-gold focus:outline-none"></div>
                    <div><label class="mb-1 block text-[10px] font-bold uppercase tracking-wide text-isivi-300">Hora</label><select id="booking-filter-time" onchange="applyBookingFilters()" class="w-full rounded-xl border border-stone-800 bg-stone-900 px-3 py-2 text-xs text-white focus:border-isivi-gold focus:outline-none"><option value="">Todas las horas</option></select></div>
                    <div><label class="mb-1 block text-[10px] font-bold uppercase tracking-wide text-isivi-300">Día</label><select id="booking-filter-day" onchange="applyBookingFilters()" class="w-full rounded-xl border border-stone-800 bg-stone-900 px-3 py-2 text-xs text-white focus:border-isivi-gold focus:outline-none"><option value="">Todos los días</option></select></div>
                    <div><label class="mb-1 block text-[10px] font-bold uppercase tracking-wide text-isivi-300">Mes</label><select id="booking-filter-month" onchange="applyBookingFilters()" class="w-full rounded-xl border border-stone-800 bg-stone-900 px-3 py-2 text-xs text-white focus:border-isivi-gold focus:outline-none"><option value="">Todos los meses</option></select></div>
                    <div class="sm:col-span-2 lg:col-span-5 flex justify-end"><button type="button" onclick="clearBookingFilters()" class="rounded-lg px-3 py-1.5 text-xs font-bold text-isivi-300 hover:bg-stone-800 hover:text-white"><i class="fa-solid fa-filter-circle-xmark mr-1"></i> Limpiar filtros</button></div>
                </div>
                <div class="overflow-x-auto rounded-2xl border border-stone-800 bg-stone-950">
                    <table class="w-full text-left text-xs">
                        <thead class="bg-stone-900 text-isivi-gold uppercase font-bold text-[10px] tracking-wider border-b border-stone-800">
                            <tr><th class="p-3">ID / Cliente</th><th class="p-3">Servicio</th><th class="p-3">Fecha &amp; Turno</th><th class="p-3">Anticipo / Total</th><th class="p-3">Método &amp; Estado</th><th class="p-3 text-right">Acciones</th></tr>
                        </thead>
                        <tbody id="adm-bookings-table-body" class="divide-y divide-stone-800"></tbody>
                    </table>
                </div>
            </div>
        </div>

        <!-- ============ TAB: GESTIÓN DE PEDIDOS (PRODUCTOS & KITS) ============ -->
        <div id="adm-tab-orders" class="hidden space-y-6">
            <div class="bg-isivi-900 rounded-3xl p-6 border border-isivi-500/30 shadow-xl space-y-4">
                <div class="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between pb-3 border-b border-stone-800">
                    <div>
                        <h3 class="font-serif-title text-lg font-bold text-white flex items-center gap-2">
                            <i class="fa-solid fa-bag-shopping text-isivi-gold"></i> Control de Pedidos de Productos &amp; Kits
                        </h3>
                        <p class="text-xs text-isivi-300">Monitorea compras, gestiona preparación, estado de entrega en local y notificaciones Brevo.</p>
                    </div>
                    <div class="flex items-center gap-2">
                        <button type="button" onclick="loadAdminOrders()" class="rounded-xl bg-stone-950 border border-stone-700 hover:border-isivi-gold px-3.5 py-2 text-xs font-bold text-white transition flex items-center gap-1.5 shadow">
                            <i id="adm-orders-refresh-icon" class="fa-solid fa-rotate text-isivi-gold text-xs"></i> <span>Actualizar</span>
                        </button>
                    </div>
                </div>

                <!-- Mini Métricas de Pedidos -->
                <div class="grid grid-cols-2 sm:grid-cols-4 gap-3">
                    <div class="p-3.5 bg-stone-950 rounded-2xl border border-stone-800">
                        <span class="text-[10px] text-stone-400 font-medium block uppercase tracking-wider">Activos en Proceso</span>
                        <p id="adm-orders-stat-active" class="text-xl font-bold text-white mt-1">0</p>
                    </div>
                    <div class="p-3.5 bg-amber-950/40 rounded-2xl border border-amber-500/30">
                        <span class="text-[10px] text-amber-300/80 font-medium block uppercase tracking-wider">En Preparación</span>
                        <p id="adm-orders-stat-prep" class="text-xl font-bold text-amber-300 mt-1">0</p>
                    </div>
                    <div class="p-3.5 bg-emerald-950/40 rounded-2xl border border-emerald-500/30">
                        <span class="text-[10px] text-emerald-300/80 font-medium block uppercase tracking-wider">Listos para Recoger</span>
                        <p id="adm-orders-stat-ready" class="text-xl font-bold text-emerald-300 mt-1">0</p>
                    </div>
                    <div class="p-3.5 bg-stone-950 rounded-2xl border border-stone-800">
                        <span class="text-[10px] text-stone-400 font-medium block uppercase tracking-wider">Entregados</span>
                        <p id="adm-orders-stat-delivered" class="text-xl font-bold text-stone-300 mt-1">0</p>
                    </div>
                </div>

                <!-- Filtros y Búsqueda de Pedidos -->
                <div class="space-y-3 pt-2">
                    <div class="flex flex-wrap items-center gap-1.5" id="adm-orders-status-pills">
                        <button type="button" onclick="filterOrdersByStatus('TODOS')" id="order-pill-TODOS" class="order-filter-pill px-3 py-1.5 rounded-xl text-xs font-bold transition bg-isivi-gold text-isivi-black shadow-sm">Todos</button>
                        <button type="button" onclick="filterOrdersByStatus('PAGO CONFIRMADO')" id="order-pill-PAGO CONFIRMADO" class="order-filter-pill px-3 py-1.5 rounded-xl text-xs font-bold transition bg-stone-950 text-stone-300 border border-stone-800 hover:border-isivi-gold">Pago Confirmado</button>
                        <button type="button" onclick="filterOrdersByStatus('EN PREPARACIÓN')" id="order-pill-EN PREPARACIÓN" class="order-filter-pill px-3 py-1.5 rounded-xl text-xs font-bold transition bg-stone-950 text-stone-300 border border-stone-800 hover:border-isivi-gold">En Preparación</button>
                        <button type="button" onclick="filterOrdersByStatus('LISTO PARA RECOGER')" id="order-pill-LISTO PARA RECOGER" class="order-filter-pill px-3 py-1.5 rounded-xl text-xs font-bold transition bg-stone-950 text-stone-300 border border-stone-800 hover:border-isivi-gold">Listo / Despachado</button>
                        <button type="button" onclick="filterOrdersByStatus('ENTREGADO')" id="order-pill-ENTREGADO" class="order-filter-pill px-3 py-1.5 rounded-xl text-xs font-bold transition bg-stone-950 text-stone-300 border border-stone-800 hover:border-isivi-gold">Entregados / Recogidos</button>
                        <button type="button" onclick="filterOrdersByStatus('CANCELADO')" id="order-pill-CANCELADO" class="order-filter-pill px-3 py-1.5 rounded-xl text-xs font-bold transition bg-stone-950 text-stone-300 border border-stone-800 hover:border-isivi-gold">Cancelados</button>
                    </div>

                    <div class="grid grid-cols-1 sm:grid-cols-3 gap-3">
                        <div class="sm:col-span-2 relative">
                            <input id="adm-orders-search" type="search" oninput="applyOrdersFilters()" placeholder="Buscar por código (ISV-...), cliente, teléfono, producto o variante..." class="w-full rounded-xl border border-stone-800 bg-stone-950 px-3.5 py-2.5 text-xs text-white placeholder-stone-500 focus:border-isivi-gold focus:outline-none">
                        </div>
                        <div>
                            <input id="adm-orders-filter-date" type="date" onchange="applyOrdersFilters()" class="w-full rounded-xl border border-stone-800 bg-stone-950 px-3.5 py-2.5 text-xs text-white focus:border-isivi-gold focus:outline-none">
                        </div>
                    </div>
                </div>

                <!-- Tabla de Pedidos -->
                <div class="overflow-x-auto rounded-2xl border border-stone-800 bg-stone-950">
                    <table class="w-full text-left text-xs">
                        <thead class="bg-stone-900 text-isivi-gold uppercase font-bold text-[10px] tracking-wider border-b border-stone-800">
                            <tr>
                                <th class="p-3">Código / Registro</th>
                                <th class="p-3">Cliente &amp; Contacto</th>
                                <th class="p-3">Productos &amp; Variantes</th>
                                <th class="p-3">Total &amp; Pago</th>
                                <th class="p-3">Estado del Pedido</th>
                                <th class="p-3 text-right">Acciones Operativas</th>
                            </tr>
                        </thead>
                        <tbody id="adm-orders-table-body" class="divide-y divide-stone-800"></tbody>
                    </table>
                </div>
            </div>
        </div>

        <div id="adm-tab-history" class="hidden space-y-6">
            <div class="bg-isivi-900 rounded-3xl p-6 border border-stone-800 shadow-xl space-y-4">
                <div class="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between pb-3 border-b border-stone-800">
                    <div>
                        <h3 class="font-serif-title text-lg font-bold text-white flex items-center gap-2">
                            <i class="fa-solid fa-clock-rotate-left text-isivi-gold"></i> Historial Unificado
                        </h3>
                        <p class="text-xs text-isivi-300">Registro histórico completo de citas atendidas, pedidos entregados y cancelaciones.</p>
                    </div>
                    <div class="flex items-center gap-2">
                        <button type="button" onclick="loadHistoryBookings()" class="rounded-xl bg-stone-950 border border-stone-700 px-3.5 py-2 text-xs font-bold text-white hover:border-isivi-gold transition flex items-center gap-1.5 shadow">
                            <i class="fa-solid fa-rotate text-isivi-gold text-xs"></i> <span>Actualizar</span>
                        </button>
                    </div>
                </div>

                <!-- Filtro por Tipo de Registro (Pills) -->
                <div class="flex flex-wrap items-center gap-1.5">
                    <span class="text-[11px] font-bold text-stone-400 mr-1"><i class="fa-solid fa-filter mr-1 text-isivi-gold"></i>Tipo:</span>
                    <button type="button" onclick="setHistoryTypeFilter('TODOS')" id="hist-type-pill-TODOS" class="hist-type-pill px-3 py-1.5 rounded-xl text-xs font-bold transition bg-isivi-gold text-isivi-black shadow-sm">Todos</button>
                    <button type="button" onclick="setHistoryTypeFilter('CITAS')" id="hist-type-pill-CITAS" class="hist-type-pill px-3 py-1.5 rounded-xl text-xs font-bold transition bg-stone-950 text-stone-300 border border-stone-800 hover:border-isivi-gold"><i class="fa-solid fa-scissors mr-1"></i> Citas</button>
                    <button type="button" onclick="setHistoryTypeFilter('PEDIDOS')" id="hist-type-pill-PEDIDOS" class="hist-type-pill px-3 py-1.5 rounded-xl text-xs font-bold transition bg-stone-950 text-stone-300 border border-stone-800 hover:border-isivi-gold"><i class="fa-solid fa-bag-shopping mr-1"></i> Pedidos</button>
                </div>

                <div class="grid grid-cols-1 gap-3 rounded-2xl border border-stone-800 bg-stone-950 p-3 sm:grid-cols-3">
                    <div class="relative">
                        <input id="history-filter-text" type="search" oninput="onHistorySearchInput(this.value)" onfocus="onHistorySearchFocus()" autocomplete="off" placeholder="Buscar cliente, código (ISV-...), teléfono, ítem..." class="w-full rounded-xl border border-stone-800 bg-stone-900 px-3 py-2 text-xs text-white placeholder-stone-500 focus:border-isivi-gold focus:outline-none">
                        <div id="history-search-autocomplete" class="hidden absolute top-full left-0 right-0 z-30 mt-1 max-h-60 overflow-y-auto rounded-xl border border-stone-700 bg-stone-950 p-1.5 shadow-2xl space-y-1 text-xs"></div>
                    </div>
                    <select id="history-filter-status" onchange="renderAdminHistory()" class="rounded-xl border border-stone-800 bg-stone-900 px-3 py-2 text-xs text-white focus:border-isivi-gold focus:outline-none">
                        <option value="">Todos los estados</option>
                        <option>Realizada</option>
                        <option>Entregado</option>
                        <option>Confirmado</option>
                        <option>Pago Confirmado</option>
                        <option>En preparación</option>
                        <option>Listo para recoger</option>
                        <option>Pendiente Comprobante</option>
                        <option>Denegada</option>
                        <option>Cancelada</option>
                        <option>Pendiente Reprogramación</option>
                    </select>
                    <input id="history-filter-date" type="month" onchange="onManualHistoryMonthChange()" class="rounded-xl border border-stone-800 bg-stone-900 px-3 py-2 text-xs text-white focus:border-isivi-gold focus:outline-none">
                </div>

                <div class="flex flex-wrap items-center gap-1.5" id="history-date-presets">
                    <span class="text-[11px] font-bold text-stone-400 mr-1"><i class="fa-solid fa-calendar-week mr-1 text-isivi-gold"></i>Periodo:</span>
                    <button type="button" onclick="setHistoryDatePreset('all')" id="preset-btn-all" class="preset-pill px-2.5 py-1 rounded-lg text-[11px] font-bold transition bg-isivi-gold text-isivi-black shadow-sm">Todos</button>
                    <button type="button" onclick="setHistoryDatePreset('today')" id="preset-btn-today" class="preset-pill px-2.5 py-1 rounded-lg text-[11px] font-bold transition bg-stone-900 text-stone-300 hover:text-white border border-stone-800">Hoy</button>
                    <button type="button" onclick="setHistoryDatePreset('yesterday')" id="preset-btn-yesterday" class="preset-pill px-2.5 py-1 rounded-lg text-[11px] font-bold transition bg-stone-900 text-stone-300 hover:text-white border border-stone-800">Ayer</button>
                    <button type="button" onclick="setHistoryDatePreset('this_week')" id="preset-btn-this_week" class="preset-pill px-2.5 py-1 rounded-lg text-[11px] font-bold transition bg-stone-900 text-stone-300 hover:text-white border border-stone-800">Esta semana</button>
                    <button type="button" onclick="setHistoryDatePreset('last_week')" id="preset-btn-last_week" class="preset-pill px-2.5 py-1 rounded-lg text-[11px] font-bold transition bg-stone-900 text-stone-300 hover:text-white border border-stone-800">Semana pasada</button>

                    <button type="button" onclick="setHistoryDatePreset('this_month')" id="preset-btn-this_month" class="preset-pill px-2.5 py-1 rounded-lg text-[11px] font-bold transition bg-stone-900 text-stone-300 hover:text-white border border-stone-800">Este mes</button>
                    <button type="button" onclick="setHistoryDatePreset('last_month')" id="preset-btn-last_month" class="preset-pill px-2.5 py-1 rounded-lg text-[11px] font-bold transition bg-stone-900 text-stone-300 hover:text-white border border-stone-800">Mes anterior</button>
                    <button type="button" onclick="setHistoryDatePreset('last_7_days')" id="preset-btn-last_7_days" class="preset-pill px-2.5 py-1 rounded-lg text-[11px] font-bold transition bg-stone-900 text-stone-300 hover:text-white border border-stone-800">Últimos 7 días</button>
                    <button type="button" onclick="setHistoryDatePreset('last_30_days')" id="preset-btn-last_30_days" class="preset-pill px-2.5 py-1 rounded-lg text-[11px] font-bold transition bg-stone-900 text-stone-300 hover:text-white border border-stone-800">Últimos 30 días</button>
                </div>
                <div id="history-date-range-label" class="mt-2 text-[11px] font-medium text-amber-300 hidden"></div>

                <div id="history-summary" class="mt-4 grid grid-cols-2 gap-3 sm:grid-cols-4"></div>
                <div class="mt-4 overflow-x-auto rounded-2xl border border-stone-800 bg-stone-950"><table class="w-full text-left text-xs"><thead class="bg-stone-900 text-isivi-gold uppercase font-bold text-[10px] tracking-wider border-b border-stone-800"><tr><th class="p-3">Código / cliente</th><th class="p-3">Solicitud</th><th class="p-3">Registro / cita</th><th class="p-3">Anticipo</th><th class="p-3">Método &amp; Estado</th><th class="p-3">Archivado</th><th class="p-3 text-right">Acción</th></tr></thead><tbody id="adm-history-table-body" class="divide-y divide-stone-800"></tbody></table></div>
            </div>
        </div>

        <!-- ============ TAB: CONFIGURACIÓN DEL NEGOCIO ============ -->
        <div id="adm-tab-settings" class="hidden space-y-6">
            <div class="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between bg-isivi-900 border border-isivi-500/30 rounded-3xl p-6 shadow-xl">
                <div>
                    <h2 class="font-serif-title text-xl font-bold text-white flex items-center gap-2">
                        <i class="fa-solid fa-store text-isivi-gold"></i>
                        <span>Configuración del Negocio</span>
                    </h2>
                    <p class="text-xs text-isivi-300 mt-0.5">Personaliza la información pública del salón: ubicación, horario de atención y línea de ventas mayorista.</p>
                </div>
            </div>

            <div class="bg-isivi-900 border border-isivi-500/30 rounded-3xl p-6 sm:p-8 shadow-xl max-w-3xl">
                <form id="admin-business-config-form" onsubmit="handleAdminBusinessConfigSubmit(event)" class="space-y-5">
                    <div id="admin-business-config-error" class="hidden bg-red-950/90 border border-red-500/80 p-3 rounded-2xl text-red-200 text-xs flex items-center gap-2" role="alert">
                        <i class="fa-solid fa-circle-exclamation text-red-400"></i>
                        <span></span>
                    </div>
                    <div id="admin-business-config-success" class="hidden bg-emerald-950/90 border border-emerald-500/80 p-3 rounded-2xl text-emerald-200 text-xs flex items-center gap-2" role="alert">
                        <i class="fa-solid fa-circle-check text-emerald-400"></i>
                        <span>Configuración del negocio guardada y publicada exitosamente.</span>
                    </div>

                    <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
                        <div>
                            <label class="block text-xs font-bold text-isivi-300 uppercase tracking-wider mb-1">Ciudad *</label>
                            <input id="admin-cfg-ciudad" type="text" required placeholder="Ej. Cartagena" class="w-full rounded-xl border border-stone-700 bg-stone-950 px-3.5 py-2.5 text-xs text-white focus:border-isivi-gold focus:outline-none">
                        </div>
                        <div>
                            <label class="block text-xs font-bold text-isivi-300 uppercase tracking-wider mb-1">País *</label>
                            <input id="admin-cfg-pais" type="text" required placeholder="Ej. Colombia" class="w-full rounded-xl border border-stone-700 bg-stone-950 px-3.5 py-2.5 text-xs text-white focus:border-isivi-gold focus:outline-none">
                        </div>
                    </div>

                    <div>
                        <label class="block text-xs font-bold text-isivi-300 uppercase tracking-wider mb-1">Días de Atención al Público *</label>
                        <input id="admin-cfg-dias" type="text" required placeholder="Ej. Mar - Sáb, Lun - Sáb" class="w-full rounded-xl border border-stone-700 bg-stone-950 px-3.5 py-2.5 text-xs text-white focus:border-isivi-gold focus:outline-none">
                        <p class="text-[11px] text-stone-500 mt-1">Texto descriptivo mostrado en la barra superior pública.</p>
                    </div>

                    <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
                        <div>
                            <label class="block text-xs font-bold text-isivi-300 uppercase tracking-wider mb-1">Hora de Apertura *</label>
                            <input id="admin-cfg-apertura" type="text" required placeholder="Ej. 08:00" class="w-full rounded-xl border border-stone-700 bg-stone-950 px-3.5 py-2.5 text-xs text-white focus:border-isivi-gold focus:outline-none">
                        </div>
                        <div>
                            <label class="block text-xs font-bold text-isivi-300 uppercase tracking-wider mb-1">Hora de Cierre *</label>
                            <input id="admin-cfg-cierre" type="text" required placeholder="Ej. 19:00" class="w-full rounded-xl border border-stone-700 bg-stone-950 px-3.5 py-2.5 text-xs text-white focus:border-isivi-gold focus:outline-none">
                        </div>
                    </div>

                    <div>
                        <label class="block text-xs font-bold text-isivi-300 uppercase tracking-wider mb-1">Teléfono / WhatsApp Mayorista *</label>
                        <input id="admin-cfg-mayorista" type="text" required placeholder="Ej. +57 300 962 3174" class="w-full rounded-xl border border-stone-700 bg-stone-950 px-3.5 py-2.5 text-xs text-white focus:border-isivi-gold focus:outline-none">
                        <p class="text-[11px] text-stone-500 mt-1">Número utilizado para el enlace directo de compras al por mayor.</p>
                    </div>

                    <div class="pt-4 border-t border-stone-800 flex justify-end">
                        <button type="submit" id="btn-save-business-config" class="px-6 py-2.5 bg-isivi-gold hover:bg-yellow-500 text-isivi-black rounded-xl text-xs font-bold shadow flex items-center gap-2 transition">
                            <i class="fa-solid fa-floppy-disk"></i> <span>Guardar Configuración del Negocio</span>
                        </button>
                    </div>
                </form>
            </div>
        </div>
    </main>

    <!-- ============ MODAL: REPROGRAMACIÓN ADMIN ============ -->
    <div id="admin-reschedule-modal" class="fixed inset-0 z-50 bg-black/80 backdrop-blur-md hidden flex items-center justify-center p-4">
        <div class="bg-stone-950 rounded-3xl max-w-md w-full overflow-hidden shadow-2xl border border-isivi-500/40 animate-fade-scale text-white p-6 space-y-4">
            <div class="flex justify-between items-center border-b border-stone-800 pb-3">
                <div>
                    <h3 class="font-serif-title font-bold text-isivi-gold text-base"><i class="fa-solid fa-calendar-days mr-2"></i>Reprogramar cita (Admin)</h3>
                    <p id="admin-reschedule-target-label" class="text-xs text-isivi-300 mt-0.5">Moviendo turno</p>
                </div>
                <button type="button" onclick="closeAdminRescheduleModal()" class="text-stone-400 hover:text-white"><i class="fa-solid fa-xmark"></i></button>
            </div>
            <div class="space-y-3">
                <div id="admin-reschedule-error" class="hidden bg-red-950/90 border border-red-500/80 p-3 rounded-2xl text-red-200 text-xs flex items-center gap-2" role="alert">
                    <i class="fa-solid fa-circle-exclamation text-red-400"></i>
                    <span></span>
                </div>
                <input type="hidden" id="admin-reschedule-booking-id" value="">
                <div>
                    <label class="block text-xs text-isivi-300 mb-1">Nueva Fecha</label>
                    <input id="admin-reschedule-date" type="date" onchange="handleAdminRescheduleDateChange()" class="w-full rounded-xl border border-stone-700 bg-stone-900 px-3.5 py-2.5 text-xs text-white focus:border-isivi-gold focus:outline-none">
                </div>
                <div>
                    <label class="block text-xs text-isivi-300 mb-1">Nuevo Horario</label>
                    <select id="admin-reschedule-time" class="w-full rounded-xl border border-stone-700 bg-stone-900 px-3.5 py-2.5 text-xs text-white focus:border-isivi-gold focus:outline-none"></select>
                </div>
            </div>
            <div class="flex justify-end gap-2 pt-2">
                <button type="button" onclick="closeAdminRescheduleModal()" class="px-4 py-2 bg-stone-800 hover:bg-stone-700 text-white rounded-xl text-xs font-bold">Cancelar</button>
                <button type="button" onclick="submitAdminReschedule()" class="px-5 py-2 bg-isivi-gold hover:bg-yellow-500 text-isivi-black rounded-xl text-xs font-bold shadow">Guardar cambio</button>
            </div>
        </div>
    </div>

    <!-- ============ MODAL: DETALLE DE RESERVA ADMIN ============ -->
    <div id="admin-booking-detail-modal" class="fixed inset-0 z-50 bg-black/80 backdrop-blur-md hidden flex items-center justify-center p-4 overflow-y-auto">
        <div class="bg-stone-950 rounded-3xl max-w-lg w-full overflow-hidden shadow-2xl border border-isivi-500/40 animate-fade-scale text-white p-6 space-y-4 my-6 max-h-[90vh] overflow-y-auto">
            <div class="flex justify-between items-center border-b border-stone-800 pb-3">
                <div>
                    <div class="flex items-center gap-2">
                        <span id="adm-detail-code" class="font-mono font-bold text-isivi-gold text-base">ISV-0000</span>
                        <span id="adm-detail-status-pill" class="px-2.5 py-0.5 rounded-full text-[10px] font-bold">Estado</span>
                    </div>
                    <p id="adm-detail-customer" class="text-xs text-isivi-300 mt-0.5">Cliente</p>
                </div>
                <button type="button" onclick="closeAdminBookingDetailModal()" class="w-8 h-8 rounded-full bg-stone-900 text-stone-400 hover:text-white flex items-center justify-center"><i class="fa-solid fa-xmark"></i></button>
            </div>

            <!-- Sección de Método de Pago y Auditoría -->
            <div id="adm-detail-payment-container">
                <!-- Se inyecta dinámicamente -->
            </div>

            <!-- Datos de la Cita / Entrega / Contacto -->
            <div class="grid grid-cols-2 gap-3 bg-stone-900/60 p-3.5 rounded-2xl border border-stone-800 text-xs">
                <div>
                    <span class="text-[10px] text-isivi-300 block">Fecha y Hora</span>
                    <span id="adm-detail-datetime" class="font-semibold text-white">2026-08-20 · 11:00 AM</span>
                </div>
                <div>
                    <span class="text-[10px] text-isivi-300 block">Teléfono / WhatsApp</span>
                    <span id="adm-detail-phone" class="font-semibold text-white">3001234567</span>
                </div>
                <div class="col-span-2">
                    <span class="text-[10px] text-isivi-300 block">Correo Electrónico</span>
                    <span id="adm-detail-email" class="font-medium text-white">cliente@ejemplo.com</span>
                </div>
                <div id="adm-detail-email-status-row" class="col-span-2 flex items-center justify-between pt-2 border-t border-stone-800">
                    <div>
                        <span class="text-[10px] text-isivi-300 block">Estado del Comprobante</span>
                        <span id="adm-detail-email-status" class="text-xs font-semibold">✓ Confirmación enviada</span>
                    </div>
                    <button type="button" id="adm-detail-btn-resend-email" onclick="resendEmailFromDetail()" class="hidden px-2.5 py-1 bg-stone-900 hover:bg-stone-800 text-isivi-gold border border-stone-700 rounded-lg text-[10px] font-bold transition">
                        <i class="fa-solid fa-envelope mr-1"></i> Reenviar confirmación
                    </button>
                </div>
                <div class="col-span-2">
                    <span id="adm-detail-items-label" class="text-[10px] text-isivi-300 block">Servicios / Productos Solicitados</span>
                    <span id="adm-detail-items" class="font-medium text-isivi-200">Balayage</span>
                </div>
                <div id="adm-detail-delivery-row" class="col-span-2 hidden pt-2 border-t border-stone-800">
                    <span id="adm-detail-delivery-label" class="text-[10px] text-isivi-300 block">Entrega</span>
                    <span id="adm-detail-delivery" class="text-isivi-gold font-medium">Domicilio</span>
                </div>
            </div>

            <!-- Desglose Financiero -->
            <div class="bg-stone-900/60 p-3.5 rounded-2xl border border-stone-800 text-xs space-y-1.5">
                <div class="flex justify-between text-isivi-300">
                    <span id="adm-detail-subtotal-label">Subtotal / Total:</span>
                    <span id="adm-detail-subtotal" class="font-bold text-white">$0</span>
                </div>
                <div class="flex justify-between text-isivi-300">
                    <span id="adm-detail-deposit-label">Anticipo (25% / Pago Wompi):</span>
                    <span id="adm-detail-deposit" class="font-bold text-emerald-400">$0</span>
                </div>
                <div class="flex justify-between text-isivi-300 pt-1 border-t border-stone-800">
                    <span>Saldo en salón:</span>
                    <span id="adm-detail-balance" class="font-bold text-isivi-gold">$0</span>
                </div>
            </div>

            <!-- Botones de Acción Contextuales -->
            <div id="adm-detail-actions" class="flex flex-wrap items-center justify-end gap-2 pt-2 border-t border-stone-800">
                <!-- Se inyecta según método y estado -->
            </div>
        </div>
    </div>

    <!-- ============ MODAL: DETALLE DE PEDIDO ADMIN ============ -->
    <div id="admin-order-detail-modal" class="fixed inset-0 z-50 bg-black/80 backdrop-blur-md hidden flex items-center justify-center p-4 overflow-y-auto">
        <div class="bg-stone-950 rounded-3xl max-w-lg w-full overflow-hidden shadow-2xl border border-isivi-500/40 animate-fade-scale text-white p-6 space-y-4 my-6 max-h-[90vh] overflow-y-auto">
            <div class="flex justify-between items-center border-b border-stone-800 pb-3">
                <div>
                    <div class="flex items-center gap-2">
                        <span id="adm-order-detail-code" class="font-mono font-bold text-isivi-gold text-base">ISV-0000</span>
                        <span id="adm-order-detail-status-pill" class="px-2.5 py-0.5 rounded-full text-[10px] font-bold">Estado</span>
                    </div>
                    <p id="adm-order-detail-customer" class="text-xs text-isivi-300 mt-0.5">Cliente</p>
                </div>
                <button type="button" onclick="closeAdminOrderDetailModal()" class="w-8 h-8 rounded-full bg-stone-900 text-stone-400 hover:text-white flex items-center justify-center"><i class="fa-solid fa-xmark"></i></button>
            </div>

            <!-- Paso a Paso del Ciclo de Vida del Pedido -->
            <div id="adm-order-detail-tracker" class="bg-stone-900/90 p-4 rounded-2xl border border-stone-800 space-y-2">
                <span class="text-[10px] text-isivi-300 uppercase tracking-wider font-bold block">Progreso del Pedido</span>
                <div id="adm-order-tracker-steps" class="grid grid-cols-4 gap-1 text-center text-[10px]">
                    <!-- Inyectado dinámicamente -->
                </div>
            </div>

            <!-- Datos de Contacto y Entrega -->
            <div class="grid grid-cols-2 gap-3 bg-stone-900/60 p-3.5 rounded-2xl border border-stone-800 text-xs">
                <div>
                    <span class="text-[10px] text-isivi-300 block">Fecha de Registro</span>
                    <span id="adm-order-detail-date" class="font-semibold text-white">--</span>
                </div>
                <div>
                    <span class="text-[10px] text-isivi-300 block">Teléfono / WhatsApp</span>
                    <span id="adm-order-detail-phone" class="font-semibold text-white">--</span>
                </div>
                <div class="col-span-2">
                    <span class="text-[10px] text-isivi-300 block">Correo Electrónico</span>
                    <span id="adm-order-detail-email" class="font-medium text-white">--</span>
                </div>
                <div class="col-span-2">
                    <span class="text-[10px] text-isivi-300 block">Entrega</span>
                    <span id="adm-order-detail-delivery" class="text-isivi-gold font-medium">📍 Recoger en el local (ISIVI Salón Cartagena)</span>
                </div>
            </div>

            <!-- Desglose de Productos y Variantes -->
            <div class="bg-stone-900/60 p-3.5 rounded-2xl border border-stone-800 space-y-2 text-xs">
                <span class="text-[10px] text-isivi-300 uppercase tracking-wider font-bold block">Productos Solicitados</span>
                <div id="adm-order-detail-items-list" class="space-y-2 divide-y divide-stone-800/60">
                    <!-- Inyectado dinámicamente -->
                </div>
            </div>

            <!-- Resumen Financiero -->
            <div class="bg-stone-900/80 p-3.5 rounded-2xl border border-stone-800 text-xs space-y-1.5">
                <div class="flex justify-between text-isivi-300">
                    <span>Método de Pago:</span>
                    <span id="adm-order-detail-payment-method" class="font-semibold text-white">--</span>
                </div>
                <div class="flex justify-between text-isivi-300">
                    <span>Estado del Pago:</span>
                    <span id="adm-order-detail-payment-status" class="font-bold text-emerald-400">APROBADO</span>
                </div>
                <div class="flex justify-between text-isivi-300 pt-1 border-t border-stone-800">
                    <span>Total Pagado:</span>
                    <span id="adm-order-detail-total" class="font-extrabold text-white text-sm">$0</span>
                </div>
            </div>

            <!-- Botones de Acción Contextuales para Pedidos -->
            <div id="adm-order-detail-actions" class="flex flex-wrap items-center justify-end gap-2 pt-2 border-t border-stone-800">
                <!-- Se inyecta dinámicamente según estado -->
            </div>
        </div>
    </div>


    <!-- ============ MODAL: EDITAR HORARIO SEMANAL ============ -->
    <div id="modal-admin-weekly-schedule" class="fixed inset-0 z-50 bg-black/80 backdrop-blur-md hidden flex items-center justify-center p-4 overflow-y-auto">
        <div class="bg-stone-950 rounded-3xl max-w-lg w-full overflow-hidden shadow-2xl border border-isivi-500/40 animate-fade-scale text-white p-6 space-y-4 my-6 max-h-[90vh] overflow-y-auto">
            <div class="flex justify-between items-center border-b border-stone-800 pb-3">
                <div>
                    <h3 class="font-serif-title font-bold text-isivi-gold text-base"><i class="fa-solid fa-clock mr-2"></i>Editar Horario Semanal</h3>
                    <p class="text-xs text-isivi-300 mt-0.5">Configura los días laborales habituales y los turnos de atención.</p>
                </div>
                <button type="button" onclick="closeAdminWeeklyScheduleModal()" class="text-stone-400 hover:text-white"><i class="fa-solid fa-xmark"></i></button>
            </div>
            <div class="space-y-4">
                <div id="admin-weekly-error" class="hidden bg-red-950/90 border border-red-500/80 p-3 rounded-2xl text-red-200 text-xs flex items-center gap-2" role="alert">
                    <i class="fa-solid fa-circle-exclamation text-red-400"></i>
                    <span></span>
                </div>
                <div>
                    <label class="block text-xs font-bold text-isivi-300 uppercase tracking-wider mb-2">Días Laborales Habituales</label>
                    <div id="admin-modal-working-days" class="grid grid-cols-2 sm:grid-cols-4 gap-2"></div>
                </div>
                <div>
                    <label class="block text-xs font-bold text-isivi-300 uppercase tracking-wider mb-2">Turnos de Atención por Defecto</label>
                    <div id="admin-modal-agenda-hours" class="flex flex-wrap gap-1.5 p-3 rounded-2xl border border-stone-800 bg-stone-900/60 max-h-36 overflow-y-auto"></div>
                    <div class="mt-2.5 flex max-w-md gap-2">
                        <select id="admin-modal-agenda-hour" class="min-w-0 rounded-xl border border-stone-700 bg-stone-900 px-3 py-2 text-xs text-white focus:border-isivi-gold focus:outline-none">
                            <option value="01">01</option><option value="02">02</option><option value="03">03</option><option value="04">04</option><option value="05">05</option><option value="06">06</option><option value="07">07</option><option value="08">08</option><option value="09">09</option><option value="10">10</option><option value="11">11</option><option value="12">12</option>
                        </select>
                        <select id="admin-modal-agenda-minutes" class="min-w-0 rounded-xl border border-stone-700 bg-stone-900 px-3 py-2 text-xs text-white focus:border-isivi-gold focus:outline-none">
                            <option value="00">00</option><option value="01">01</option><option value="02">02</option><option value="03">03</option><option value="04">04</option><option value="05">05</option><option value="06">06</option><option value="07">07</option><option value="08">08</option><option value="09">09</option><option value="10">10</option><option value="11">11</option><option value="12">12</option><option value="13">13</option><option value="14">14</option><option value="15">15</option><option value="16">16</option><option value="17">17</option><option value="18">18</option><option value="19">19</option><option value="20">20</option><option value="21">21</option><option value="22">22</option><option value="23">23</option><option value="24">24</option><option value="25">25</option><option value="26">26</option><option value="27">27</option><option value="28">28</option><option value="29">29</option><option value="30">30</option><option value="31">31</option><option value="32">32</option><option value="33">33</option><option value="34">34</option><option value="35">35</option><option value="36">36</option><option value="37">37</option><option value="38">38</option><option value="39">39</option><option value="40">40</option><option value="41">41</option><option value="42">42</option><option value="43">43</option><option value="44">44</option><option value="45">45</option><option value="46">46</option><option value="47">47</option><option value="48">48</option><option value="49">49</option><option value="50">50</option><option value="51">51</option><option value="52">52</option><option value="53">53</option><option value="54">54</option><option value="55">55</option><option value="56">56</option><option value="57">57</option><option value="58">58</option><option value="59">59</option>
                        </select>
                        <select id="admin-modal-agenda-period" class="min-w-0 rounded-xl border border-stone-700 bg-stone-900 px-3 py-2 text-xs text-white focus:border-isivi-gold focus:outline-none">
                            <option value="AM">AM</option><option value="PM">PM</option>
                        </select>
                        <button type="button" onclick="addModalAgendaTime()" class="rounded-xl bg-stone-800 px-3.5 py-2 text-xs font-bold text-isivi-gold hover:bg-stone-700 transition">
                            <i class="fa-solid fa-plus mr-1"></i>Añadir turno
                        </button>
                    </div>
                </div>
            </div>
            <div class="flex justify-end gap-2 pt-3 border-t border-stone-800">
                <button type="button" onclick="closeAdminWeeklyScheduleModal()" class="px-4 py-2 bg-stone-800 hover:bg-stone-700 text-white rounded-xl text-xs font-bold">Cancelar</button>
                <button type="button" id="btn-save-weekly-schedule" onclick="submitWeeklyScheduleModal()" class="px-5 py-2 bg-isivi-gold hover:bg-yellow-500 text-isivi-black rounded-xl text-xs font-bold shadow flex items-center gap-1.5">
                    <i class="fa-solid fa-floppy-disk"></i> <span>Guardar horario semanal</span>
                </button>
            </div>
        </div>
    </div>

    <!-- ============ MODAL: BLOQUEAR RANGO HORARIO ============ -->
    <div id="modal-admin-block-range" class="fixed inset-0 z-50 bg-black/80 backdrop-blur-md hidden flex items-center justify-center p-4 overflow-y-auto">
        <div class="bg-stone-950 rounded-3xl max-w-lg w-full overflow-hidden shadow-2xl border border-amber-500/40 animate-fade-scale text-white p-6 space-y-4 my-6 max-h-[90vh] overflow-y-auto">
            <div class="flex justify-between items-center border-b border-stone-800 pb-3">
                <div>
                    <h3 class="font-serif-title font-bold text-amber-300 text-base"><i class="fa-solid fa-lock mr-2"></i>Bloquear Horario o Rango</h3>
                    <p class="text-xs text-isivi-300 mt-0.5">Impide que clientes reserven los turnos seleccionados.</p>
                </div>
                <button type="button" onclick="closeAdminBlockRangeModal()" class="text-stone-400 hover:text-white"><i class="fa-solid fa-xmark"></i></button>
            </div>
            <div class="space-y-3">
                <div id="admin-block-range-error" class="hidden bg-red-950/90 border border-red-500/80 p-3 rounded-2xl text-red-200 text-xs flex items-center gap-2" role="alert">
                    <i class="fa-solid fa-circle-exclamation text-red-400"></i>
                    <span></span>
                </div>
                <!-- Alerta de Conflicto de Reservas Existentes -->
                <div id="admin-block-range-conflict-box" class="hidden bg-amber-950/90 border border-amber-500/80 p-4 rounded-2xl text-amber-200 text-xs space-y-2">
                    <div class="flex items-center gap-2 font-bold text-amber-300">
                        <i class="fa-solid fa-triangle-exclamation text-base"></i>
                        <span id="admin-block-range-conflict-title">Conflicto con reservas existentes</span>
                    </div>
                    <p id="admin-block-range-conflict-msg" class="text-[11px] leading-relaxed text-amber-200/90"></p>
                    <div id="admin-block-range-conflict-list" class="max-h-28 overflow-y-auto space-y-1 p-2 rounded-xl bg-black/40 border border-amber-500/30 text-[11px]"></div>
                    <p class="text-[10px] text-stone-400 italic">Nota: Las reservas existentes NO se cancelarán automáticamente.</p>
                </div>
                <div>
                    <label class="block text-xs text-isivi-300 mb-1">Fecha del Bloqueo *</label>
                    <input id="admin-block-range-date" type="date" onchange="handleBlockRangeDateChange()" class="w-full rounded-xl border border-stone-700 bg-stone-900 px-3.5 py-2.5 text-xs text-white focus:border-isivi-gold focus:outline-none">
                </div>
                <div>
                    <div class="flex items-center justify-between mb-1">
                        <label class="text-xs text-isivi-300">Selecciona los turnos a bloquear *</label>
                        <button type="button" onclick="selectAllBlockRangeSlots(true)" class="text-[10px] text-isivi-gold hover:underline">Todos</button>
                    </div>
                    <div id="admin-block-range-slots-container" class="grid grid-cols-2 sm:grid-cols-3 gap-2 max-h-36 overflow-y-auto p-2 rounded-xl border border-stone-800 bg-stone-900/60">
                        <span class="col-span-full text-center text-xs text-stone-500 py-2">Selecciona una fecha</span>
                    </div>
                </div>
                <div>
                    <label class="block text-xs text-isivi-300 mb-1">Motivo (Opcional)</label>
                    <input id="admin-block-range-reason" type="text" placeholder="Ej. Asunto personal, Reunión de equipo, Mantenimiento..." class="w-full rounded-xl border border-stone-700 bg-stone-900 px-3.5 py-2.5 text-xs text-white focus:border-isivi-gold focus:outline-none">
                </div>
            </div>
            <div class="flex justify-end gap-2 pt-3 border-t border-stone-800">
                <button type="button" onclick="closeAdminBlockRangeModal()" class="px-4 py-2 bg-stone-800 hover:bg-stone-700 text-white rounded-xl text-xs font-bold">Cancelar</button>
                <button type="button" id="btn-submit-block-range" onclick="submitBlockRange(false)" class="px-5 py-2 bg-amber-600 hover:bg-amber-500 text-white rounded-xl text-xs font-bold shadow flex items-center gap-1.5">
                    <i class="fa-solid fa-lock"></i> <span>Guardar Bloqueo</span>
                </button>
                <button type="button" id="btn-confirm-block-range-force" onclick="submitBlockRange(true)" class="hidden px-5 py-2 bg-amber-500 hover:bg-yellow-400 text-black rounded-xl text-xs font-bold shadow">
                    Confirmar de todos modos
                </button>
            </div>
        </div>
    </div>

    <!-- ============ MODAL: BLOQUEO RECURRENTE ============ -->
    <div id="modal-admin-recurring-block" class="fixed inset-0 z-50 bg-black/80 backdrop-blur-md hidden flex items-center justify-center p-4 overflow-y-auto">
        <div class="bg-stone-950 rounded-3xl max-w-lg w-full overflow-hidden shadow-2xl border border-purple-500/40 animate-fade-scale text-white p-6 space-y-4 my-6 max-h-[90vh] overflow-y-auto">
            <div class="flex justify-between items-center border-b border-stone-800 pb-3">
                <div>
                    <h3 class="font-serif-title font-bold text-purple-300 text-base"><i class="fa-solid fa-arrows-rotate mr-2"></i>Bloqueo Recurrente de Agenda</h3>
                    <p class="text-xs text-isivi-300 mt-0.5">Bloquea turnos repetitivos en un día de la semana (ej. todos los viernes 6:00 PM).</p>
                </div>
                <button type="button" onclick="closeAdminRecurringBlockModal()" class="text-stone-400 hover:text-white"><i class="fa-solid fa-xmark"></i></button>
            </div>
            <div class="space-y-3">
                <div id="admin-recurring-error" class="hidden bg-red-950/90 border border-red-500/80 p-3 rounded-2xl text-red-200 text-xs flex items-center gap-2" role="alert">
                    <i class="fa-solid fa-circle-exclamation text-red-400"></i>
                    <span></span>
                </div>
                <!-- Alerta de Conflicto -->
                <div id="admin-recurring-conflict-box" class="hidden bg-amber-950/90 border border-amber-500/80 p-4 rounded-2xl text-amber-200 text-xs space-y-2">
                    <div class="flex items-center gap-2 font-bold text-amber-300">
                        <i class="fa-solid fa-triangle-exclamation text-base"></i>
                        <span>Conflicto con reservas existentes</span>
                    </div>
                    <p id="admin-recurring-conflict-msg" class="text-[11px] leading-relaxed text-amber-200/90"></p>
                    <div id="admin-recurring-conflict-list" class="max-h-28 overflow-y-auto space-y-1 p-2 rounded-xl bg-black/40 border border-amber-500/30 text-[11px]"></div>
                    <p class="text-[10px] text-stone-400 italic">Nota: Las reservas existentes NO se cancelarán automáticamente.</p>
                </div>
                <div>
                    <label class="block text-xs text-isivi-300 mb-1">Día de la Semana a Bloquear *</label>
                    <select id="admin-recurring-day" class="w-full rounded-xl border border-stone-700 bg-stone-900 px-3.5 py-2.5 text-xs text-white focus:border-isivi-gold focus:outline-none">
                        <option value="1">Lunes</option>
                        <option value="2">Martes</option>
                        <option value="3">Miércoles</option>
                        <option value="4">Jueves</option>
                        <option value="5" selected>Viernes</option>
                        <option value="6">Sábado</option>
                        <option value="0">Domingo</option>
                    </select>
                </div>
                <div>
                    <div class="flex items-center justify-between mb-1">
                        <label class="text-xs text-isivi-300">Selecciona los turnos recurrentes a bloquear *</label>
                        <button type="button" onclick="selectAllRecurringSlots(true)" class="text-[10px] text-isivi-gold hover:underline">Todos</button>
                    </div>
                    <div id="admin-recurring-slots-container" class="grid grid-cols-2 sm:grid-cols-3 gap-2 max-h-36 overflow-y-auto p-2 rounded-xl border border-stone-800 bg-stone-900/60">
                        <!-- Inyectado dinámicamente -->
                    </div>
                </div>
                <div class="grid grid-cols-1 sm:grid-cols-2 gap-2">
                    <div>
                        <label class="block text-xs text-isivi-300 mb-1">Desde (Fecha inicio)</label>
                        <input id="admin-recurring-date-start" type="date" class="w-full rounded-xl border border-stone-700 bg-stone-900 px-3.5 py-2.5 text-xs text-white focus:border-isivi-gold focus:outline-none">
                    </div>
                    <div>
                        <label class="block text-xs text-isivi-300 mb-1">Hasta (Fecha fin opcional)</label>
                        <input id="admin-recurring-date-end" type="date" class="w-full rounded-xl border border-stone-700 bg-stone-900 px-3.5 py-2.5 text-xs text-white focus:border-isivi-gold focus:outline-none">
                    </div>
                </div>
                <div>
                    <label class="block text-xs text-isivi-300 mb-1">Motivo (Opcional)</label>
                    <input id="admin-recurring-reason" type="text" placeholder="Ej. Reunión de equipo, Mantenimiento semanal..." class="w-full rounded-xl border border-stone-700 bg-stone-900 px-3.5 py-2.5 text-xs text-white focus:border-isivi-gold focus:outline-none">
                </div>
            </div>
            <div class="flex justify-end gap-2 pt-3 border-t border-stone-800">
                <button type="button" onclick="closeAdminRecurringBlockModal()" class="px-4 py-2 bg-stone-800 hover:bg-stone-700 text-white rounded-xl text-xs font-bold">Cancelar</button>
                <button type="button" id="btn-submit-recurring-block" onclick="submitRecurringBlock(false)" class="px-5 py-2 bg-purple-700 hover:bg-purple-600 text-white rounded-xl text-xs font-bold shadow flex items-center gap-1.5">
                    <i class="fa-solid fa-arrows-rotate"></i> <span>Guardar Recurrencia</span>
                </button>
                <button type="button" id="btn-confirm-recurring-force" onclick="submitRecurringBlock(true)" class="hidden px-5 py-2 bg-purple-500 hover:bg-purple-400 text-black rounded-xl text-xs font-bold shadow">
                    Confirmar de todos modos
                </button>
            </div>
        </div>
    </div>


    <!-- ============ MODAL: NUEVA EXCEPCIÓN / HORARIO ESPECIAL ============ -->
    <div id="modal-admin-exception" class="fixed inset-0 z-50 bg-black/80 backdrop-blur-md hidden flex items-center justify-center p-4 overflow-y-auto">
        <div class="bg-stone-950 rounded-3xl max-w-lg w-full overflow-hidden shadow-2xl border border-isivi-500/40 animate-fade-scale text-white p-6 space-y-4 my-6 max-h-[90vh] overflow-y-auto">
            <div class="flex justify-between items-center border-b border-stone-800 pb-3">
                <div>
                    <h3 class="font-serif-title font-bold text-isivi-gold text-base"><i class="fa-solid fa-calendar-plus mr-2"></i>Excepción por Fecha</h3>
                    <p class="text-xs text-isivi-300 mt-0.5">Cierra un día específico o crea un horario especial para una fecha.</p>
                </div>
                <button type="button" onclick="closeAdminExceptionModal()" class="text-stone-400 hover:text-white"><i class="fa-solid fa-xmark"></i></button>
            </div>
            <div class="space-y-3">
                <div id="admin-exception-error" class="hidden bg-red-950/90 border border-red-500/80 p-3 rounded-2xl text-red-200 text-xs flex items-center gap-2" role="alert">
                    <i class="fa-solid fa-circle-exclamation text-red-400"></i>
                    <span></span>
                </div>
                <!-- Alerta de Conflicto -->
                <div id="admin-exception-conflict-box" class="hidden bg-amber-950/90 border border-amber-500/80 p-4 rounded-2xl text-amber-200 text-xs space-y-2">
                    <div class="flex items-center gap-2 font-bold text-amber-300">
                        <i class="fa-solid fa-triangle-exclamation text-base"></i>
                        <span>Conflicto con reservas existentes</span>
                    </div>
                    <p id="admin-exception-conflict-msg" class="text-[11px] leading-relaxed text-amber-200/90"></p>
                    <div id="admin-exception-conflict-list" class="max-h-28 overflow-y-auto space-y-1 p-2 rounded-xl bg-black/40 border border-amber-500/30 text-[11px]"></div>
                    <p class="text-[10px] text-stone-400 italic">Nota: Las reservas existentes NO se cancelarán automáticamente.</p>
                </div>
                <div class="grid grid-cols-1 sm:grid-cols-2 gap-3">
                    <div>
                        <label class="block text-xs text-isivi-300 mb-1">Fecha *</label>
                        <input id="admin-exception-date" type="date" class="w-full rounded-xl border border-stone-700 bg-stone-900 px-3.5 py-2.5 text-xs text-white focus:border-isivi-gold focus:outline-none">
                    </div>
                    <div>
                        <label class="block text-xs text-isivi-300 mb-1">Tipo de Excepción *</label>
                        <select id="admin-exception-type" onchange="toggleAdminExceptionTypeFields()" class="w-full rounded-xl border border-stone-700 bg-stone-900 px-3.5 py-2.5 text-xs text-white focus:border-isivi-gold focus:outline-none">
                            <option value="CERRADO">🔴 Día Cerrado (Sin Atención)</option>
                            <option value="HORARIO_ESPECIAL">🟢 Horario Especial / Turnos Específicos</option>
                        </select>
                    </div>
                </div>
                <div id="admin-exception-special-hours-container" class="hidden space-y-2 pt-1 border-t border-stone-800">
                    <label class="block text-xs text-isivi-300">Turnos para este día especial</label>
                    <div id="admin-exception-slots-chips" class="flex flex-wrap gap-1.5 p-2 rounded-xl border border-stone-800 bg-stone-900/60 max-h-28 overflow-y-auto"></div>
                    <div class="flex gap-2">
                        <select id="admin-exception-hour" class="min-w-0 rounded-xl border border-stone-700 bg-stone-900 px-2.5 py-2 text-xs text-white focus:border-isivi-gold focus:outline-none">
                            <option value="08">08</option><option value="09">09</option><option value="10">10</option><option value="11">11</option><option value="12">12</option><option value="01">01</option><option value="02">02</option><option value="03">03</option><option value="04">04</option><option value="05">05</option><option value="06">06</option><option value="07">07</option>
                        </select>
                        <select id="admin-exception-minutes" class="min-w-0 rounded-xl border border-stone-700 bg-stone-900 px-2.5 py-2 text-xs text-white focus:border-isivi-gold focus:outline-none">
                            <option value="00">00</option><option value="01">01</option><option value="02">02</option><option value="03">03</option><option value="04">04</option><option value="05">05</option><option value="06">06</option><option value="07">07</option><option value="08">08</option><option value="09">09</option><option value="10">10</option><option value="11">11</option><option value="12">12</option><option value="13">13</option><option value="14">14</option><option value="15">15</option><option value="16">16</option><option value="17">17</option><option value="18">18</option><option value="19">19</option><option value="20">20</option><option value="21">21</option><option value="22">22</option><option value="23">23</option><option value="24">24</option><option value="25">25</option><option value="26">26</option><option value="27">27</option><option value="28">28</option><option value="29">29</option><option value="30">30</option><option value="31">31</option><option value="32">32</option><option value="33">33</option><option value="34">34</option><option value="35">35</option><option value="36">36</option><option value="37">37</option><option value="38">38</option><option value="39">39</option><option value="40">40</option><option value="41">41</option><option value="42">42</option><option value="43">43</option><option value="44">44</option><option value="45">45</option><option value="46">46</option><option value="47">47</option><option value="48">48</option><option value="49">49</option><option value="50">50</option><option value="51">51</option><option value="52">52</option><option value="53">53</option><option value="54">54</option><option value="55">55</option><option value="56">56</option><option value="57">57</option><option value="58">58</option><option value="59">59</option>
                        </select>
                        <select id="admin-exception-period" class="min-w-0 rounded-xl border border-stone-700 bg-stone-900 px-2.5 py-2 text-xs text-white focus:border-isivi-gold focus:outline-none">
                            <option value="AM">AM</option><option value="PM">PM</option>
                        </select>
                        <button type="button" onclick="addExceptionSlotChip()" class="rounded-xl bg-stone-800 px-3 py-2 text-xs font-bold text-isivi-gold hover:bg-stone-700 transition">
                            + Turno
                        </button>
                    </div>
                </div>
                <div>
                    <label class="block text-xs text-isivi-300 mb-1">Motivo (Opcional)</label>
                    <input id="admin-exception-reason" type="text" placeholder="Ej. Festivo nacional, Mantenimiento preventivo, Sábado extendido..." class="w-full rounded-xl border border-stone-700 bg-stone-900 px-3.5 py-2.5 text-xs text-white focus:border-isivi-gold focus:outline-none">
                </div>
            </div>
            <div class="flex justify-end gap-2 pt-3 border-t border-stone-800">
                <button type="button" onclick="closeAdminExceptionModal()" class="px-4 py-2 bg-stone-800 hover:bg-stone-700 text-white rounded-xl text-xs font-bold">Cancelar</button>
                <button type="button" id="btn-submit-exception" onclick="submitAdminException(false)" class="px-5 py-2 bg-isivi-gold hover:bg-yellow-500 text-isivi-black rounded-xl text-xs font-bold shadow flex items-center gap-1.5">
                    <i class="fa-solid fa-floppy-disk"></i> <span>Guardar Excepción</span>
                </button>
                <button type="button" id="btn-confirm-exception-force" onclick="submitAdminException(true)" class="hidden px-5 py-2 bg-amber-500 hover:bg-yellow-400 text-black rounded-xl text-xs font-bold shadow">
                    Confirmar de todos modos
                </button>
            </div>
        </div>
    </div>

    <footer class="bg-stone-900 border-t border-stone-800 py-4 text-center text-xs text-stone-500">ISIVI Administrative System — Panel de Control Interno</footer>
</div>

<script src="/js/isivi.js?v=1.6.0"></script>
</body>
</html>

```

## 3. isivi.css
```css
/* ============================================================
   ISIVI - Production Standalone Design System & CSS
   Peluquería & Cuidado Capilar Natural
   100% Self-Contained Design System (No external CDN dependency)
   ============================================================ */

/* ---------- 1. CSS Custom Properties / Tokens ---------- */
:root {
    /* Brand Gold & Earth Tones */
    --isivi-gold: #d4af37;
    --isivi-gold-hover: #e5be3b;
    --isivi-gold-dark: #b59226;
    --isivi-black: #0f0e0d;
    --isivi-900: #1a1613;
    --isivi-800: #5c3d25;
    --isivi-700: #7a522e;
    --isivi-600: #9b6e3b;
    --isivi-500: #b88a4c;
    --isivi-400: #cca877;
    --isivi-300: #e0c8a5;
    --isivi-200: #eee0cc;
    --isivi-100: #f7f0e6;
    --isivi-50: #fdfbf7;

    /* Stone Dark Spectrum - Optimized for high contrast and warm champagne aesthetic */
    --stone-950: #0c0a09;
    --stone-900: #1c1917;
    --stone-850: #231f1d;
    --stone-800: #35302b;
    --stone-700: #544e47;
    --stone-600: #8c857b;
    --stone-500: #a1998f;
    --stone-400: #c4b7a6;
    --stone-300: #e1d6c5;
    --stone-200: #eee0cc;
    --stone-100: #f7f0e6;

    /* Emerald Feedback */
    --emerald-950: #022c22;
    --emerald-900: #064e3b;
    --emerald-800: #065f46;
    --emerald-700: #047857;
    --emerald-600: #059669;
    --emerald-500: #10b981;
    --emerald-400: #34d399;
    --emerald-300: #6ee7b7;

    /* Amber / Warning Feedback */
    --amber-950: #451a03;
    --amber-900: #78350f;
    --amber-800: #92400e;
    --amber-700: #b45309;
    --amber-600: #d97706;
    --amber-500: #f59e0b;
    --amber-400: #fbbf24;
    --amber-300: #fcd34d;

    /* Red / Danger Feedback */
    --red-950: #450a0a;
    --red-900: #7f1d1d;
    --red-800: #991b1b;
    --red-700: #b91c1c;
    --red-600: #dc2626;
    --red-500: #ef4444;
    --red-400: #f87171;
    --red-300: #fca5a5;
    --red-200: #fecaca;
}

/* ---------- 2. Reset, Base & Fallback Typography ---------- */
*, *::before, *::after {
    box-sizing: border-box;
    margin: 0;
    padding: 0;
}

html {
    scroll-behavior: smooth;
    -webkit-text-size-adjust: 100%;
}

body {
    font-family: 'Plus Jakarta Sans', system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
    background-color: var(--isivi-black);
    color: var(--isivi-100);
    line-height: 1.5;
    min-height: 100vh;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    -webkit-font-smoothing: antialiased;
    -moz-osx-font-smoothing: grayscale;
    overflow-x: clip;
}

body::before {
    content: '';
    position: fixed;
    top: 0;
    left: 0;
    width: 100vw;
    height: 100vh;
    z-index: -10;
    background-color: var(--isivi-black);
    background-image: 
        radial-gradient(circle at 80% 20%, rgba(212, 175, 55, 0.04) 0%, transparent 50%),
        radial-gradient(circle at 15% 60%, rgba(184, 138, 76, 0.03) 0%, transparent 40%),
        radial-gradient(circle at 50% 90%, rgba(16, 185, 129, 0.02) 0%, transparent 50%);
    pointer-events: none;
}

.font-serif-title,
.font-serif {
    font-family: 'Cinzel', 'Playfair Display', Georgia, 'Times New Roman', serif !important;
}

.font-sans {
    font-family: 'Plus Jakarta Sans', system-ui, -apple-system, sans-serif !important;
}

/* Scrollbars */
::-webkit-scrollbar {
    width: 8px;
    height: 8px;
}
::-webkit-scrollbar-track {
    background: #1a1613;
}
::-webkit-scrollbar-thumb {
    background: #b88a4c;
    border-radius: 4px;
}
::-webkit-scrollbar-thumb:hover {
    background: #d4af37;
}

/* ---------- 3. Layout, Flex & Grid System ---------- */
.block { display: block; }
.inline-block { display: inline-block; }
.inline-flex { display: inline-flex; }
.flex { display: flex; }
.grid { display: grid; }
.hidden { display: none !important; }

.flex-row { flex-direction: row; }
.flex-col { flex-direction: column; }
.flex-wrap { flex-wrap: wrap; }
.flex-1 { flex: 1 1 0%; }
.flex-shrink-0 { flex-shrink: 0; }

.items-center { align-items: center; }
.items-start { align-items: flex-start; }
.items-end { align-items: flex-end; }

.justify-center { justify-content: center; }
.justify-between { justify-content: space-between; }
.justify-start { justify-content: flex-start; }
.justify-end { justify-content: flex-end; }

.grid-cols-1 { grid-template-columns: repeat(1, minmax(0, 1fr)); }
.grid-cols-2 { grid-template-columns: repeat(2, minmax(0, 1fr)); }
.grid-cols-3 { grid-template-columns: repeat(3, minmax(0, 1fr)); }
.grid-cols-4 { grid-template-columns: repeat(4, minmax(0, 1fr)); }
.grid-cols-5 { grid-template-columns: repeat(5, minmax(0, 1fr)); }
.grid-cols-7 { grid-template-columns: repeat(7, minmax(0, 1fr)); }
.grid-cols-12 { grid-template-columns: repeat(12, minmax(0, 1fr)); }
.col-span-full { grid-column: 1 / -1; }

.gap-1 { gap: 0.25rem; }
.gap-1\.5 { gap: 0.375rem; }
.gap-2 { gap: 0.5rem; }
.gap-2\.5 { gap: 0.625rem; }
.gap-3 { gap: 0.75rem; }
.gap-3\.5 { gap: 0.875rem; }
.gap-4 { gap: 1rem; }
.gap-6 { gap: 1.5rem; }
.gap-7 { gap: 1.75rem; }
.gap-8 { gap: 2rem; }
.gap-12 { gap: 3rem; }
.gap-x-3 { column-gap: 0.75rem; }
.gap-x-4 { column-gap: 1rem; }
.gap-y-1 { row-gap: 0.25rem; }

/* Positioning & Z-Index */
.relative { position: relative; }
.absolute { position: absolute; }
.fixed { position: fixed; }
.sticky { position: sticky; }

.inset-0 { inset: 0px; }
.inset-x-0 { left: 0px; right: 0px; }
.top-0 { top: 0px; }
.top-3 { top: 0.75rem; }
.top-5 { top: 1.25rem; }
.top-8 { top: 2rem; }
.right-3 { right: 0.75rem; }
.right-4 { right: 1rem; }
.right-5 { right: 1.25rem; }
.bottom-4 { bottom: 1rem; }
.bottom-5 { bottom: 1.25rem; }
.bottom-6 { bottom: 1.5rem; }
.left-0 { left: 0px; }
.left-6 { left: 1.5rem; }
.right-6 { right: 1.5rem; }
.left-1\/2 { left: 50%; }
.top-1\/2 { top: 50%; }
.top-full { top: 100%; }

.-translate-x-1\/2 { transform: translateX(-50%); }
.-translate-y-1\/2 { transform: translateY(-50%); }
.-translate-x-1\/2.-translate-y-1\/2 { transform: translate(-50%, -50%); }

.z-10 { z-index: 10; }
.z-40 { z-index: 40; }
.z-50 { z-index: 50; }
.z-\[60\] { z-index: 60; }

.pointer-events-none { pointer-events: none; }
.cursor-pointer { cursor: pointer; }
.cursor-not-allowed { cursor: not-allowed; }

/* Sizing & Dimension Utilities */
.w-full { width: 100%; }
.w-auto { width: auto; }
.w-1 { width: 0.25rem; }
.w-2 { width: 0.5rem; }
.w-5 { width: 1.25rem; }
.w-8 { width: 2rem; }
.w-9 { width: 2.25rem; }
.w-10 { width: 2.5rem; }
.w-11 { width: 2.75rem; }
.w-12 { width: 3rem; }
.w-14 { width: 3.5rem; }
.w-\[calc\(100\%-2rem\)\] { width: calc(100% - 2rem); }

.h-full { height: 100%; }
.h-auto { height: auto; }
.h-1 { height: 0.25rem; }
.h-2 { height: 0.5rem; }
.h-5 { height: 1.25rem; }
.h-8 { height: 2rem; }
.h-9 { height: 2.25rem; }
.h-10 { height: 2.5rem; }
.h-11 { height: 2.75rem; }
.h-12 { height: 3rem; }
.h-14 { height: 3.5rem; }
.h-20 { height: 5rem; }
.h-36 { height: 9rem; }
.h-44 { height: 11rem; }
.h-52 { height: 13rem; }
.h-64 { height: 16rem; }
.h-72 { height: 18rem; }
.h-80 { height: 20rem; }
.h-\[90vh\] { height: 90vh; }

.min-h-screen { min-height: 100vh; }
.min-w-0 { min-width: 0px; }
.min-w-40 { min-width: 10rem; }
.min-w-\[250px\] { min-width: 250px; }
.min-w-\[260px\] { min-width: 260px; }
.min-w-\[270px\] { min-width: 270px; }
.min-w-\[280px\] { min-width: 280px; }
.min-w-\[300px\] { min-width: 300px; }
.min-w-\[780px\] { min-width: 780px; }

.max-w-xs { max-w: 20rem; }
.max-w-sm { max-width: 24rem; }
.max-w-md { max-width: 28rem; }
.max-w-lg { max-width: 32rem; }
.max-w-xl { max-width: 36rem; }
.max-w-2xl { max-width: 42rem; }
.max-w-3xl { max-width: 48rem; }
.max-w-5xl { max-width: 64rem; }
.max-w-7xl { max-width: 80rem; }
.max-w-\[140px\] { max-width: 140px; }
.max-w-\[180px\] { max-width: 180px; }

.max-h-56 { max-height: 14rem; }
.max-h-72 { max-height: 18rem; }
.max-h-\[780px\] { max-height: 780px; }

.aspect-\[4\/5\] { aspect-ratio: 4 / 5; }
.object-cover { object-fit: cover; }
.object-contain { object-fit: contain; }
.overflow-hidden { overflow: hidden; }
.overflow-x-auto { overflow-x: auto; }
.overflow-y-auto { overflow-y: auto; }
.snap-x { scroll-snap-type: x mandatory; }
.snap-mandatory { scroll-snap-type: x mandatory; }
.snap-start { scroll-snap-align: start; }

.mx-auto { margin-left: auto; margin-right: auto; }

/* ---------- 4. Colors, Backgrounds, Borders & Text ---------- */
/* Backgrounds */
.bg-isivi-black { background-color: var(--isivi-black) !important; }
.bg-isivi-900 { background-color: var(--isivi-900) !important; }
.bg-isivi-800 { background-color: var(--isivi-800) !important; }
.bg-isivi-700 { background-color: var(--isivi-700) !important; }
.bg-isivi-600 { background-color: var(--isivi-600) !important; }
.bg-isivi-500 { background-color: var(--isivi-500) !important; }
.bg-isivi-gold { background-color: var(--isivi-gold) !important; }

.bg-stone-950 { background-color: var(--stone-950) !important; }
.bg-stone-900 { background-color: var(--stone-900) !important; }
.bg-stone-850 { background-color: var(--stone-850) !important; }
.bg-stone-800 { background-color: var(--stone-800) !important; }
.bg-stone-700 { background-color: var(--stone-700) !important; }

.bg-emerald-950 { background-color: var(--emerald-950) !important; }
.bg-emerald-900 { background-color: var(--emerald-900) !important; }
.bg-emerald-700 { background-color: var(--emerald-700) !important; }
.bg-emerald-600 { background-color: var(--emerald-600) !important; }
.bg-emerald-500 { background-color: var(--emerald-500) !important; }

.bg-amber-950 { background-color: var(--amber-950) !important; }
.bg-amber-900 { background-color: var(--amber-900) !important; }
.bg-amber-500 { background-color: var(--amber-500) !important; }

.bg-red-950 { background-color: var(--red-950) !important; }
.bg-red-900 { background-color: var(--red-900) !important; }
.bg-red-800 { background-color: var(--red-800) !important; }
.bg-red-600 { background-color: var(--red-600) !important; }

/* Opacity Backgrounds */
.bg-stone-950\/70 { background-color: rgba(12, 10, 9, 0.70) !important; }
.bg-stone-950\/80 { background-color: rgba(12, 10, 9, 0.80) !important; }
.bg-stone-950\/90 { background-color: rgba(12, 10, 9, 0.90) !important; }
.bg-stone-950\/95 { background-color: rgba(12, 10, 9, 0.95) !important; }
.bg-stone-900\/40 { background-color: rgba(28, 25, 23, 0.40) !important; }
.bg-stone-900\/50 { background-color: rgba(28, 25, 23, 0.50) !important; }
.bg-stone-900\/60 { background-color: rgba(28, 25, 23, 0.60) !important; }
.bg-stone-900\/80 { background-color: rgba(28, 25, 23, 0.80) !important; }
.bg-stone-900\/90 { background-color: rgba(28, 25, 23, 0.90) !important; }
.bg-stone-900\/95 { background-color: rgba(28, 25, 23, 0.95) !important; }

.bg-isivi-900\/50 { background-color: rgba(26, 22, 19, 0.50) !important; }
.bg-isivi-900\/60 { background-color: rgba(26, 22, 19, 0.60) !important; }
.bg-isivi-900\/95 { background-color: rgba(26, 22, 19, 0.95) !important; }
.bg-isivi-500\/10 { background-color: rgba(184, 138, 76, 0.10) !important; }
.bg-isivi-500\/15 { background-color: rgba(184, 138, 76, 0.15) !important; }
.bg-isivi-500\/20 { background-color: rgba(184, 138, 76, 0.20) !important; }

.bg-emerald-950\/20 { background-color: rgba(2, 44, 34, 0.20) !important; }
.bg-emerald-950\/40 { background-color: rgba(2, 44, 34, 0.40) !important; }
.bg-emerald-500\/20 { background-color: rgba(16, 185, 129, 0.20) !important; }

.bg-amber-950\/40 { background-color: rgba(69, 26, 3, 0.40) !important; }
.bg-amber-500\/20 { background-color: rgba(245, 158, 11, 0.20) !important; }

.bg-red-950\/40 { background-color: rgba(69, 10, 10, 0.40) !important; }
.bg-red-950\/60 { background-color: rgba(69, 10, 10, 0.60) !important; }
.bg-red-950\/80 { background-color: rgba(69, 10, 10, 0.80) !important; }
.bg-red-950\/90 { background-color: rgba(69, 10, 10, 0.90) !important; }
.bg-red-500\/20 { background-color: rgba(239, 68, 68, 0.20) !important; }

.bg-black\/50 { background-color: rgba(0, 0, 0, 0.50) !important; }
.bg-black\/70 { background-color: rgba(0, 0, 0, 0.70) !important; }
.bg-black\/80 { background-color: rgba(0, 0, 0, 0.80) !important; }
.bg-black\/90 { background-color: rgba(0, 0, 0, 0.90) !important; }

/* Text Colors */
.text-white { color: #ffffff !important; }
.text-isivi-black { color: var(--isivi-black) !important; }
.text-isivi-gold { color: var(--isivi-gold) !important; }
.text-isivi-100 { color: var(--isivi-100) !important; }
.text-isivi-200 { color: var(--isivi-200) !important; }
.text-isivi-300 { color: var(--isivi-300) !important; }
.text-isivi-400 { color: var(--isivi-400) !important; }
.text-isivi-500 { color: var(--isivi-500) !important; }

.text-stone-200 { color: var(--stone-200) !important; }
.text-stone-300 { color: var(--stone-300) !important; }
.text-stone-400 { color: var(--stone-400) !important; }
.text-stone-500 { color: var(--stone-500) !important; }
.text-stone-600 { color: var(--stone-600) !important; }
.text-stone-700 { color: var(--stone-700) !important; }

.text-emerald-300 { color: var(--emerald-300) !important; }
.text-emerald-400 { color: var(--emerald-400) !important; }
.text-emerald-400\/80 { color: rgba(52, 211, 153, 0.80) !important; }
.text-emerald-500 { color: var(--emerald-500) !important; }

.text-amber-300 { color: var(--amber-300) !important; }
.text-amber-400 { color: var(--amber-400) !important; }
.text-amber-400\/80 { color: rgba(251, 191, 36, 0.80) !important; }

.text-red-200 { color: var(--red-200) !important; }
.text-red-300 { color: var(--red-300) !important; }
.text-red-400 { color: var(--red-400) !important; }
.text-red-400\/80 { color: rgba(248, 113, 113, 0.80) !important; }

.text-purple-400 { color: #c084fc !important; }

/* Borders & Rings */
.border { border-width: 1px; border-style: solid; }
.border-2 { border-width: 2px; border-style: solid; }
.border-t { border-top-width: 1px; border-top-style: solid; }
.border-b { border-bottom-width: 1px; border-bottom-style: solid; }
.border-b-2 { border-bottom-width: 2px; border-bottom-style: solid; }
.border-l { border-left-width: 1px; border-left-style: solid; }
.border-r { border-right-width: 1px; border-right-style: solid; }
.border-y { border-top-width: 1px; border-bottom-width: 1px; border-style: solid; }
.border-0 { border-width: 0px !important; }

.border-transparent { border-color: transparent !important; }
.border-isivi-gold { border-color: var(--isivi-gold) !important; }
.border-isivi-500 { border-color: var(--isivi-500) !important; }

.border-isivi-500\/20 { border-color: rgba(184, 138, 76, 0.20) !important; }
.border-isivi-500\/30 { border-color: rgba(184, 138, 76, 0.30) !important; }
.border-isivi-500\/40 { border-color: rgba(184, 138, 76, 0.40) !important; }
.border-isivi-500\/50 { border-color: rgba(184, 138, 76, 0.50) !important; }
.border-isivi-500\/60 { border-color: rgba(184, 138, 76, 0.60) !important; }
.border-isivi-gold\/70 { border-color: rgba(212, 175, 55, 0.70) !important; }

.border-stone-900 { border-color: var(--stone-900) !important; }
.border-stone-850 { border-color: var(--stone-850) !important; }
.border-stone-800 { border-color: var(--stone-800) !important; }
.border-stone-800\/50 { border-color: rgba(41, 37, 36, 0.50) !important; }
.border-stone-800\/80 { border-color: rgba(41, 37, 36, 0.80) !important; }
.border-stone-700 { border-color: var(--stone-700) !important; }

.border-emerald-500\/30 { border-color: rgba(16, 185, 129, 0.30) !important; }
.border-emerald-500\/40 { border-color: rgba(16, 185, 129, 0.40) !important; }
.border-emerald-800 { border-color: var(--emerald-800) !important; }

.border-amber-500\/30 { border-color: rgba(245, 158, 11, 0.30) !important; }
.border-amber-500\/40 { border-color: rgba(245, 158, 11, 0.40) !important; }
.border-amber-800 { border-color: var(--amber-800) !important; }

.border-red-500\/80 { border-color: rgba(239, 68, 68, 0.80) !important; }
.border-red-800 { border-color: var(--red-800) !important; }
.border-red-800\/50 { border-color: rgba(153, 27, 27, 0.50) !important; }
.border-red-800\/60 { border-color: rgba(153, 27, 27, 0.60) !important; }

.ring-1 { box-shadow: 0 0 0 1px var(--isivi-gold) !important; }
.ring-isivi-gold { box-shadow: 0 0 0 1px var(--isivi-gold) !important; }

/* Rounded Corners */
.rounded { border-radius: 0.25rem; }
.rounded-md { border-radius: 0.375rem; }
.rounded-lg { border-radius: 0.5rem; }
.rounded-xl { border-radius: 0.75rem; }
.rounded-2xl { border-radius: 1rem; }
.rounded-3xl { border-radius: 1.5rem; }
.rounded-full { border-radius: 9999px; }

/* Shadows */
.shadow-sm { box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.05); }
.shadow { box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.1), 0 1px 2px -1px rgba(0, 0, 0, 0.1); }
.shadow-md { box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.3), 0 2px 4px -2px rgba(0, 0, 0, 0.3); }
.shadow-lg { box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.5), 0 4px 6px -4px rgba(0, 0, 0, 0.5); }
.shadow-xl { box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.6), 0 8px 10px -6px rgba(0, 0, 0, 0.6); }
.shadow-2xl { box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.75); }
.shadow-isivi-gold\/10 { box-shadow: 0 10px 15px -3px rgba(212, 175, 55, 0.10); }
.shadow-isivi-500\/20 { box-shadow: 0 10px 15px -3px rgba(184, 138, 76, 0.20); }
.shadow-emerald-950\/70 { box-shadow: 0 20px 25px -5px rgba(2, 44, 34, 0.70); }

/* Typography & Weights */
.text-\[8px\] { font-size: 8px; }
.text-\[9px\] { font-size: 9px; }
.text-\[10px\] { font-size: 10px; }
.text-\[11px\] { font-size: 11px; }
.text-xs { font-size: 0.75rem; line-height: 1rem; }
.text-sm { font-size: 0.875rem; line-height: 1.25rem; }
.text-base { font-size: 1rem; line-height: 1.5rem; }
.text-lg { font-size: 1.125rem; line-height: 1.75rem; }
.text-xl { font-size: 1.25rem; line-height: 1.75rem; }
.text-2xl { font-size: 1.5rem; line-height: 2rem; }
.text-3xl { font-size: 1.875rem; line-height: 2.25rem; }
.text-4xl { font-size: 2.25rem; line-height: 2.5rem; }
.text-5xl { font-size: 3rem; line-height: 1.15; }
.text-6xl { font-size: 3.75rem; line-height: 1.1; }

.font-light { font-weight: 300; }
.font-normal { font-weight: 400; }
.font-medium { font-weight: 500; }
.font-semibold { font-weight: 600; }
.font-bold { font-weight: 700; }
.font-mono { font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace; }

.uppercase { text-transform: uppercase; }
.capitalize { text-transform: capitalize; }
.italic { font-style: italic; }
.text-center { text-align: center; }
.text-left { text-align: left; }
.text-right { text-align: right; }
.tracking-wide { letter-spacing: 0.025em; }
.tracking-wider { letter-spacing: 0.05em; }
.tracking-widest { letter-spacing: 0.1em; }
.tracking-\[0\.2em\] { letter-spacing: 0.2em; }
.leading-none { line-height: 1; }
.leading-tight { line-height: 1.25; }
.leading-snug { line-height: 1.375; }
.leading-relaxed { line-height: 1.625; }

/* Spacing (Padding & Margins) */
.p-1 { padding: 0.25rem; }
.p-2 { padding: 0.5rem; }
.p-2\.5 { padding: 0.625rem; }
.p-3 { padding: 0.75rem; }
.p-3\.5 { padding: 0.875rem; }
.p-4 { padding: 1rem; }
.p-5 { padding: 1.25rem; }
.p-6 { padding: 1.5rem; }
.p-7 { padding: 1.75rem; }
.p-8 { padding: 2rem; }
.p-12 { padding: 3rem; }

.px-2 { padding-left: 0.5rem; padding-right: 0.5rem; }
.px-2\.5 { padding-left: 0.625rem; padding-right: 0.625rem; }
.px-3 { padding-left: 0.75rem; padding-right: 0.75rem; }
.px-3\.5 { padding-left: 0.875rem; padding-right: 0.875rem; }
.px-4 { padding-left: 1rem; padding-right: 1rem; }
.px-5 { padding-left: 1.25rem; padding-right: 1.25rem; }
.px-6 { padding-left: 1.5rem; padding-right: 1.5rem; }
.px-8 { padding-left: 2rem; padding-right: 2rem; }

.py-0\.5 { padding-top: 0.125rem; padding-bottom: 0.125rem; }
.py-1 { padding-top: 0.25rem; padding-bottom: 0.25rem; }
.py-1\.5 { padding-top: 0.375rem; padding-bottom: 0.375rem; }
.py-2 { padding-top: 0.5rem; padding-bottom: 0.5rem; }
.py-2\.5 { padding-top: 0.625rem; padding-bottom: 0.625rem; }
.py-3 { padding-top: 0.75rem; padding-bottom: 0.75rem; }
.py-3\.5 { padding-top: 0.875rem; padding-bottom: 0.875rem; }
.py-4 { padding-top: 1rem; padding-bottom: 1rem; }
.py-5 { padding-top: 1.25rem; padding-bottom: 1.25rem; }
.py-6 { padding-top: 1.5rem; padding-bottom: 1.5rem; }
.py-8 { padding-top: 2rem; padding-bottom: 2rem; }
.py-12 { padding-top: 3rem; padding-bottom: 3rem; }
.py-16 { padding-top: 4rem; padding-bottom: 4rem; }

.pt-1 { padding-top: 0.25rem; }
.pt-2 { padding-top: 0.5rem; }
.pt-3 { padding-top: 0.75rem; }
.pt-6 { padding-top: 1.5rem; }
.pb-1 { padding-bottom: 0.25rem; }
.pb-3 { padding-bottom: 0.75rem; }
.pb-4 { padding-bottom: 1rem; }
.pb-5 { padding-bottom: 1.25rem; }
.pl-1 { padding-left: 0.25rem; }
.pr-1 { padding-right: 0.25rem; }

.m-0 { margin: 0px; }
.mt-0\.5 { margin-top: 0.125rem; }
.mt-1 { margin-top: 0.25rem; }
.mt-1\.5 { margin-top: 0.375rem; }
.mt-2 { margin-top: 0.5rem; }
.mt-3 { margin-top: 0.75rem; }
.mt-4 { margin-top: 1rem; }
.mt-5 { margin-top: 1.25rem; }
.mb-1 { margin-bottom: 0.25rem; }
.mb-2 { margin-bottom: 0.5rem; }
.mb-3 { margin-bottom: 0.75rem; }
.mb-4 { margin-bottom: 1rem; }
.mb-5 { margin-bottom: 1.25rem; }
.mb-8 { margin-bottom: 2rem; }
.mb-10 { margin-bottom: 2.5rem; }
.mb-12 { margin-bottom: 3rem; }
.mr-1 { margin-right: 0.25rem; }
.mr-1\.5 { margin-right: 0.375rem; }
.mr-2 { margin-right: 0.5rem; }
.ml-2 { margin-left: 0.5rem; }

.space-y-1 > :not([hidden]) ~ :not([hidden]) { margin-top: 0.25rem; }
.space-y-1\.5 > :not([hidden]) ~ :not([hidden]) { margin-top: 0.375rem; }
.space-y-2 > :not([hidden]) ~ :not([hidden]) { margin-top: 0.5rem; }
.space-y-2\.5 > :not([hidden]) ~ :not([hidden]) { margin-top: 0.625rem; }
.space-y-3 > :not([hidden]) ~ :not([hidden]) { margin-top: 0.75rem; }
.space-y-4 > :not([hidden]) ~ :not([hidden]) { margin-top: 1rem; }
.space-y-5 > :not([hidden]) ~ :not([hidden]) { margin-top: 1.25rem; }
.space-y-6 > :not([hidden]) ~ :not([hidden]) { margin-top: 1.5rem; }
.space-y-8 > :not([hidden]) ~ :not([hidden]) { margin-top: 2rem; }

.divide-y > :not([hidden]) ~ :not([hidden]) { border-top-width: 1px; border-top-style: solid; }
.divide-stone-800 > :not([hidden]) ~ :not([hidden]) { border-color: var(--stone-800) !important; }

/* Backdrop Blur & Gradients */
.backdrop-blur-sm { backdrop-filter: blur(4px); -webkit-backdrop-filter: blur(4px); }
.backdrop-blur-md { backdrop-filter: blur(12px); -webkit-backdrop-filter: blur(12px); }

.bg-gradient-to-r {
    background-image: linear-gradient(to right, var(--tw-gradient-stops, var(--isivi-500), var(--isivi-600)));
}
.from-isivi-500 { --tw-gradient-from: #b88a4c; --tw-gradient-stops: #b88a4c, var(--tw-gradient-to, rgba(184, 138, 76, 0)); }
.to-isivi-600 { --tw-gradient-to: #9b6e3b; }
.from-isivi-900 { --tw-gradient-from: #1a1613; --tw-gradient-stops: #1a1613, var(--tw-gradient-to, rgba(26, 22, 19, 0)); }
.via-stone-900 { --tw-gradient-stops: var(--tw-gradient-from), #1c1917, var(--tw-gradient-to); }
.to-isivi-900 { --tw-gradient-to: #1a1613; }
.to-stone-950 { --tw-gradient-to: #0c0a09; }

@keyframes goldShimmer {
    0% { background-position: 0% 50%; }
    50% { background-position: 100% 50%; }
    100% { background-position: 0% 50%; }
}

.gold-gradient-text {
    background: linear-gradient(90deg, #fceabb 0%, #f8b500 25%, #fceabb 50%, #cca877 75%, #fceabb 100%);
    background-size: 200% auto;
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    animation: goldShimmer 12s infinite linear;
}

.gold-border-glow {
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.4), 0 0 20px rgba(212, 175, 55, 0.15);
}

.glass-nav {
    background: rgba(15, 14, 13, 0.98);
    border-bottom: 1px solid rgba(212, 175, 55, 0.12);
    box-shadow: 0 4px 30px rgba(0, 0, 0, 0.2);
}

/* Premium Navigation Underline */
.nav-link-premium {
    position: relative;
    padding-bottom: 0.25rem;
    transition: color 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}

.nav-link-premium::after {
    content: '';
    position: absolute;
    bottom: -2px;
    left: 50%;
    width: 0;
    height: 2px;
    background: var(--isivi-gold);
    transition: width 0.3s cubic-bezier(0.16, 1, 0.3, 1), left 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}

.nav-link-premium:hover::after,
.nav-link-premium.active::after {
    width: 100%;
    left: 0;
}

/* Scroll Reveal animations */
.reveal-on-scroll {
    opacity: 0;
    transform: translateY(15px);
    transition: opacity 1.2s cubic-bezier(0.16, 1, 0.3, 1), transform 1.2s cubic-bezier(0.16, 1, 0.3, 1);
    will-change: opacity, transform;
}

.reveal-on-scroll.is-visible {
    opacity: 1;
    transform: translateY(0);
}

/* Editorial Card Layering and Hover Effects */
.editorial-card-wrapper {
    perspective: 1000px;
}

.editorial-card-inner {
    transition: transform 0.6s cubic-bezier(0.16, 1, 0.3, 1), box-shadow 0.6s cubic-bezier(0.16, 1, 0.3, 1);
    transform-style: preserve-3d;
}

@media (min-width: 1024px) {
    .editorial-card-wrapper:hover .editorial-card-inner {
        transform: translateY(-5px) rotateX(2.5deg) rotateY(-2.5deg);
        box-shadow: 0 25px 50px -12px rgba(212, 175, 55, 0.12), 0 0 25px rgba(212, 175, 55, 0.05);
    }
}

.editorial-card-overlay {
    background: rgba(26, 22, 19, 0.85);
    backdrop-filter: blur(12px);
    -webkit-backdrop-filter: blur(12px);
    border: 1px solid rgba(212, 175, 55, 0.2);
    transition: border-color 0.3s ease;
}

.editorial-card-wrapper:hover .editorial-card-overlay {
    border-color: rgba(212, 175, 55, 0.45);
}

.hero-indicator-module {
    padding: 0.75rem;
    border-radius: 0.75rem;
    background: rgba(26, 22, 19, 0.35);
    border: 1px solid rgba(212, 175, 55, 0.08);
    transition: all 0.4s cubic-bezier(0.16, 1, 0.3, 1);
}

.hero-indicator-module:hover {
    background: rgba(26, 22, 19, 0.65);
    border-color: rgba(212, 175, 55, 0.28);
    transform: translateY(-2px);
}

@media (prefers-reduced-motion: reduce) {
    .reveal-on-scroll {
        opacity: 1 !important;
        transform: none !important;
        transition: none !important;
    }
    .gold-gradient-text {
        animation: none !important;
    }
    .editorial-card-wrapper:hover .editorial-card-inner {
        transform: none !important;
        box-shadow: none !important;
    }
}

/* ---------- 5. Form Elements, Buttons & Interactive States ---------- */
button, input, select, textarea {
    font-family: inherit;
    font-size: inherit;
    color: inherit;
}

input, select, textarea {
    background-color: var(--stone-950);
    border: 1px solid var(--stone-800);
    color: #ffffff;
    border-radius: 0.75rem;
    transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

input:focus, select:focus, textarea:focus {
    outline: none;
    border-color: var(--isivi-gold) !important;
    box-shadow: 0 0 0 1px var(--isivi-gold);
}

input::placeholder, textarea::placeholder {
    color: #57534e;
}

/* File Input Custom Styling */
input[type="file"]::file-selector-button {
    margin-right: 0.75rem;
    padding: 0.375rem 0.75rem;
    border-radius: 0.5rem;
    border: none;
    background-color: var(--isivi-gold);
    color: var(--isivi-black);
    font-size: 0.75rem;
    font-weight: 700;
    cursor: pointer;
    transition: background-color 0.2s ease;
}
input[type="file"]::file-selector-button:hover {
    background-color: var(--isivi-gold-hover);
}

button {
    cursor: pointer;
    border: none;
    background: none;
    transition: all 0.2s ease;
}

button:disabled, input:disabled {
    cursor: not-allowed;
    opacity: 0.6;
}

.hover\:bg-yellow-500:hover { background-color: #eab308 !important; }
.hover\:bg-emerald-600:hover { background-color: #059669 !important; }
.hover\:bg-emerald-700:hover { background-color: #047857 !important; }
.hover\:bg-stone-800:hover { background-color: #292524 !important; }
.hover\:bg-stone-700:hover { background-color: #44403c !important; }
.hover\:bg-red-900:hover { background-color: #7f1d1d !important; }
.hover\:bg-isivi-500\/15:hover { background-color: rgba(184, 138, 76, 0.15) !important; }
.hover\:bg-isivi-500\/20:hover { background-color: rgba(184, 138, 76, 0.20) !important; }

.hover\:border-isivi-gold:hover { border-color: var(--isivi-gold) !important; }
.hover\:border-isivi-500:hover { border-color: var(--isivi-500) !important; }
.hover\:text-isivi-gold:hover { color: var(--isivi-gold) !important; }
.hover\:text-white:hover { color: #ffffff !important; }

/* Scale and Glow on Hover */
.hover\:scale-105:hover { transform: scale(1.05); }
.hover\:scale-110:hover { transform: scale(1.10); }

/* ---------- 6. Hero & Visual Components ---------- */
.hero-isivi {
    isolation: isolate;
    background: #12100e;
    position: relative;
    border-radius: 28px !important;
    overflow: hidden !important;
    margin-left: 1rem !important;
    margin-right: 1rem !important;
    margin-top: 1rem !important;
}
@media (min-width: 768px) {
    .hero-isivi {
        border-radius: 32px !important;
        margin-left: 2rem !important;
        margin-right: 2rem !important;
        margin-top: 1.5rem !important;
    }
}

.hero-background-image {
    position: absolute;
    inset: -18px;
    z-index: 0;
    width: calc(100% + 36px);
    height: calc(100% + 36px);
    object-fit: cover;
    object-position: center 42%;
    filter: blur(8px);
    opacity: 0.82;
    max-width: none;
}

.hero-isivi::after {
    content: '';
    position: absolute;
    inset: 0;
    z-index: 1;
    background: linear-gradient(90deg, rgba(18, 16, 14, 0.70) 0%, rgba(18, 16, 14, 0.48) 52%, rgba(18, 16, 14, 0.38) 100%);
}

.hero-isivi > div {
    position: relative;
    z-index: 2;
}

/* Floating WhatsApp button */
a[href*="wa.me"] {
    display: flex;
    align-items: center;
    justify-content: center;
    text-decoration: none;
}

/* ---------- 7. Responsive Breakpoints (Desktop & Mobile) ---------- */
@media (min-width: 640px) {
    .sm\:inline { display: inline; }
    .sm\:flex { display: flex; }
    .sm\:hidden { display: none !important; }
    .sm\:flex-row { flex-direction: row; }
    .sm\:grid-cols-2 { grid-template-columns: repeat(2, minmax(0, 1fr)); }
    .sm\:grid-cols-3 { grid-template-columns: repeat(3, minmax(0, 1fr)); }
    .sm\:grid-cols-4 { grid-template-columns: repeat(4, minmax(0, 1fr)); }
    .sm\:grid-cols-5 { grid-template-columns: repeat(5, minmax(0, 1fr)); }
    .sm\:col-span-2 { grid-column: span 2 / span 2; }
    .sm\:col-span-3 { grid-column: span 3 / span 3; }
    .sm\:col-span-5 { grid-column: span 5 / span 5; }
    .sm\:w-auto { width: auto; }
    .sm\:px-6 { padding-left: 1.5rem; padding-right: 1.5rem; }
    .sm\:px-8 { padding-left: 2rem; padding-right: 2rem; }
    .sm\:px-12 { padding-left: 3rem; padding-right: 3rem; }
    .sm\:py-4 { padding-top: 1rem; padding-bottom: 1rem; }
    .sm\:gap-4 { gap: 1rem; }
    .sm\:gap-6 { gap: 1.5rem; }
    .sm\:text-sm { font-size: 0.875rem; }
    .sm\:text-base { font-size: 1rem; }
    .sm\:text-lg { font-size: 1.125rem; }
    .sm\:text-4xl { font-size: 2.25rem; line-height: 2.5rem; }
    .sm\:text-5xl { font-size: 3rem; line-height: 1.15; }
    .sm\:min-w-\[250px\] { min-width: 250px; }
    .sm\:min-w-\[280px\] { min-width: 280px; }
    .sm\:min-w-\[300px\] { min-width: 300px; }
    .sm\:bottom-5 { bottom: 1.25rem; }
    .sm\:right-5 { right: 1.25rem; }
    .sm\:h-14 { height: 3.5rem; }
    .sm\:w-14 { width: 3.5rem; }
    .sm\:text-2xl { font-size: 1.5rem; }
    .sm\:items-center { align-items: center; }
    .sm\:justify-between { justify-content: space-between; }
    .sm\:justify-start { justify-content: flex-start; }
}

@media (min-width: 768px) {
    .md\:flex { display: flex; }
    .md\:hidden { display: none !important; }
    .md\:flex-row { flex-direction: row; }
    .md\:grid-cols-2 { grid-template-columns: repeat(2, minmax(0, 1fr)); }
    .md\:grid-cols-4 { grid-template-columns: repeat(4, minmax(0, 1fr)); }
    .md\:w-5\/12 { width: 41.666667%; }
    .md\:h-64 { height: 16rem; }
    .md\:py-20 { padding-top: 5rem; padding-bottom: 5rem; }
    .md\:items-end { align-items: flex-end; }
}

@media (min-width: 1024px) {
    .lg\:flex { display: flex; }
    .lg\:hidden { display: none !important; }
    .lg\:grid-cols-4 { grid-template-columns: repeat(4, minmax(0, 1fr)); }
    .lg\:grid-cols-5 { grid-template-columns: repeat(5, minmax(0, 1fr)); }
    .lg\:grid-cols-7 { grid-template-columns: repeat(7, minmax(0, 1fr)); }
    .lg\:grid-cols-12 { grid-template-columns: repeat(12, minmax(0, 1fr)); }
    .lg\:col-span-2 { grid-column: span 2 / span 2; }
    .lg\:col-span-4 { grid-column: span 4 / span 4; }
    .lg\:col-span-5 { grid-column: span 5 / span 5; }
    .lg\:col-span-7 { grid-column: span 7 / span 7; }
    .lg\:col-span-12 { grid-column: span 12 / span 12; }
    .lg\:text-left { text-align: left; }
    .lg\:justify-start { justify-content: flex-start; }
    .lg\:text-6xl { font-size: 3.75rem; line-height: 1.1; }
    .lg\:px-8 { padding-left: 2rem; padding-right: 2rem; }
    .lg\:max-w-none { max-width: none; }
}

@media (max-width: 767px) {
    html, body {
        overflow-x: clip;
        max-width: 100vw;
        width: 100%;
    }

    #page-client {
        max-width: 100vw;
        width: 100%;
    }

    header.glass-nav {
        position: sticky;
        top: 0;
        z-index: 40;
        width: 100%;
        height: auto;
        min-height: 4.25rem;
    }

    header img[src*="logo"],
    header img.h-14 {
        height: 46px !important;
        max-height: 46px !important;
        width: auto !important;
        max-width: 140px !important;
        object-fit: contain !important;
    }

    .hero-isivi {
        padding-top: 1.75rem !important;
        padding-bottom: 2.5rem !important;
        overflow: hidden;
    }

    .hero-background-image {
        position: absolute;
        inset: 0;
        width: 100% !important;
        height: 100% !important;
        object-fit: cover !important;
        object-position: center 30% !important;
        filter: blur(7px) !important;
        opacity: 0.72 !important;
    }

    .hero-isivi::after {
        background: linear-gradient(180deg, rgba(18, 16, 14, 0.70) 0%, rgba(18, 16, 14, 0.82) 55%, rgba(18, 16, 14, 0.95) 100%) !important;
    }

    .hero-isivi h1 {
        font-size: 2rem !important;
        line-height: 1.2 !important;
        text-align: center !important;
    }

    .hero-isivi p {
        font-size: 0.9375rem !important;
        line-height: 1.55 !important;
        text-align: center !important;
    }

    .hero-isivi .flex-col > a,
    .hero-isivi a[href="#servicios"],
    .hero-isivi a[href="#productos"] {
        width: 100% !important;
        text-align: center !important;
        padding: 0.875rem 1.5rem !important;
        border-radius: 9999px !important;
    }

    .hero-isivi .lg\:col-span-5 {
        margin-top: 1.5rem;
    }

    .hero-isivi .aspect-\[4\/5\] {
        max-width: 22rem;
        margin-left: auto;
        margin-right: auto;
        border-radius: 1.5rem;
    }

    #services-grid,
    #products-grid,
    #kits-grid {
        display: flex;
        gap: 1rem;
        overflow-x: auto;
        scroll-snap-type: x mandatory;
        -webkit-overflow-scrolling: touch;
        padding-bottom: 1rem;
    }

    #services-grid > * {
        scroll-snap-align: start;
        flex-shrink: 0;
        min-width: 280px;
    }

    #products-grid > * {
        scroll-snap-align: start;
        flex-shrink: 0;
        width: 260px;
        max-width: 100%;
        min-width: 0 !important;
    }

    #kits-grid > * {
        scroll-snap-align: start;
        flex-shrink: 0;
        width: 280px;
        max-width: 100%;
        min-width: 0 !important;
    }
}

/* ---------- 8. Keyframe Animations & Components ---------- */
@keyframes fadeScaleIn {
    0% {
        opacity: 0;
        transform: scale(0.94) translateY(10px);
    }
    100% {
        opacity: 1;
        transform: scale(1) translateY(0);
    }
}

.animate-fade-scale {
    animation: fadeScaleIn 0.35s cubic-bezier(0.16, 1, 0.3, 1) forwards;
}

@keyframes pulseGlow {
    0%, 100% {
        box-shadow: 0 0 15px rgba(212, 175, 55, 0.2);
    }
    50% {
        box-shadow: 0 0 25px rgba(212, 175, 55, 0.45);
    }
}

.animate-pulse-glow {
    animation: pulseGlow 2.5s infinite ease-in-out;
}

@keyframes pulse {
    0%, 100% { opacity: 1; }
    50% { opacity: 0.4; }
}

.animate-pulse {
    animation: pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite;
}

.dashboard-card-hover {
    transition: transform 0.2s ease, border-color 0.2s ease, box-shadow 0.2s ease;
}

.dashboard-card-hover:hover {
    transform: translateY(-2px);
    border-color: rgba(212, 175, 55, 0.5);
    box-shadow: 0 8px 24px -4px rgba(0, 0, 0, 0.6);
}

/* ============ INTERMITTENT BELL ALERT ANIMATION ============ */
@keyframes isivi-bell-ring {
    0%, 70%, 100% {
        transform: rotate(0deg);
    }
    72% {
        transform: rotate(14deg);
    }
    76% {
        transform: rotate(-12deg);
    }
    80% {
        transform: rotate(10deg);
    }
    84% {
        transform: rotate(-8deg);
    }
    88% {
        transform: rotate(4deg);
    }
    92% {
        transform: rotate(-2deg);
    }
    96% {
        transform: rotate(0deg);
    }
}

.isivi-bell-intermittent {
    display: inline-block;
    transform-origin: top center;
    animation: isivi-bell-ring 3.5s ease-in-out infinite;
}

@media (prefers-reduced-motion: reduce) {
    .isivi-bell-intermittent {
        animation: none !important;
    }
}

/* ============ DESKTOP HAMBURGER MENU & DRAWER (100% LOCAL CSS) ============ */
.isivi-desktop-menu-btn {
    display: none;
    width: 2.75rem; /* 44px (h-11 w-11) */
    height: 2.75rem;
    align-items: center;
    justify-content: center;
    border-radius: 9999px;
    border: 1px solid rgba(184, 138, 76, 0.5);
    background: transparent;
    color: var(--isivi-gold);
    cursor: pointer;
    font-size: 1.125rem;
    transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
    outline: none;
}

.isivi-desktop-menu-btn:hover {
    background: rgba(184, 138, 76, 0.15);
    border-color: var(--isivi-gold);
    color: #ffffff;
    box-shadow: 0 0 12px rgba(212, 175, 55, 0.25);
}

.isivi-desktop-menu-btn:focus-visible {
    outline: 2px solid var(--isivi-gold);
    outline-offset: 2px;
}

@media (min-width: 768px) {
    .isivi-desktop-menu-btn {
        display: inline-flex;
    }
}

.isivi-desktop-overlay {
    position: fixed;
    inset: 0;
    z-index: 9990;
    background-color: rgba(0, 0, 0, 0.75);
    backdrop-filter: blur(4px);
    -webkit-backdrop-filter: blur(4px);
    opacity: 0;
    visibility: hidden;
    transition: opacity 0.3s ease, visibility 0.3s ease;
    pointer-events: none;
}

.isivi-desktop-overlay.is-open {
    opacity: 1;
    visibility: visible;
    pointer-events: auto;
}

.isivi-desktop-drawer {
    position: fixed;
    top: 0;
    right: 0;
    bottom: 0;
    width: 20rem; /* 320px */
    max-width: calc(100vw - 2rem);
    height: 100vh;
    z-index: 9999;
    background-color: var(--stone-950);
    border-left: 1px solid rgba(184, 138, 76, 0.4);
    box-shadow: -10px 0 30px rgba(0, 0, 0, 0.8), 0 0 20px rgba(212, 175, 55, 0.1);
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    padding: 1.5rem;
    overflow-y: auto;
    transform: translateX(100%);
    visibility: hidden;
    transition: transform 0.3s cubic-bezier(0.16, 1, 0.3, 1), visibility 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}

.isivi-desktop-drawer.is-open {
    transform: translateX(0);
    visibility: visible;
}

.isivi-drawer-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    border-bottom: 1px solid var(--stone-800);
    padding-bottom: 1.25rem;
}

.isivi-drawer-close-btn {
    width: 2.25rem;
    height: 2.25rem;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 9999px;
    border: 1px solid rgba(184, 138, 76, 0.3);
    background-color: var(--stone-900);
    color: var(--isivi-300);
    cursor: pointer;
    transition: all 0.2s ease;
}

.isivi-drawer-close-btn:hover {
    background-color: var(--stone-800);
    color: #ffffff;
    border-color: var(--isivi-gold);
}

.isivi-drawer-nav {
    margin-top: 1.5rem;
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
}

.isivi-drawer-link {
    display: flex;
    align-items: center;
    gap: 0.75rem;
    padding: 0.75rem 1rem;
    border-radius: 1rem;
    color: var(--isivi-200);
    font-size: 0.875rem;
    font-weight: 500;
    text-decoration: none;
    background: transparent;
    border: 1px solid transparent;
    cursor: pointer;
    width: 100%;
    text-align: left;
    transition: all 0.2s ease;
}

.isivi-drawer-link:hover {
    background-color: rgba(184, 138, 76, 0.15);
    border-color: rgba(212, 175, 55, 0.3);
    color: var(--isivi-gold);
    transform: translateX(3px);
}

.isivi-drawer-link i {
    width: 1.25rem;
    text-align: center;
    color: var(--isivi-gold);
}

.isivi-drawer-footer {
    border-top: 1px solid var(--stone-800);
    padding-top: 1rem;
    font-size: 0.75rem;
    color: var(--isivi-300);
}

/* ============ PREMIUM UI SYSTEM & CONVERSION UTILITIES ============ */
.isivi-btn-primary {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    gap: 0.5rem;
    padding: 0.75rem 1.75rem;
    min-height: 44px;
    border-radius: 9999px;
    font-size: 0.875rem;
    font-weight: 600;
    text-align: center;
    background: linear-gradient(135deg, var(--isivi-500) 0%, var(--isivi-600) 100%);
    color: #ffffff;
    border: 1px solid rgba(212, 175, 55, 0.4);
    box-shadow: 0 4px 14px rgba(184, 138, 76, 0.35);
    transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
    cursor: pointer;
    text-decoration: none;
    position: relative;
    overflow: hidden;
}

.isivi-btn-primary::before {
    content: '';
    position: absolute;
    top: 0;
    left: -100%;
    width: 100%;
    height: 100%;
    background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.12), transparent);
    transition: left 0.6s cubic-bezier(0.16, 1, 0.3, 1);
}

.isivi-btn-primary:hover::before {
    left: 100%;
}

.isivi-btn-primary:hover {
    background: linear-gradient(135deg, var(--isivi-600) 0%, var(--isivi-700) 100%);
    transform: translateY(-2px);
    box-shadow: 0 8px 24px rgba(184, 138, 76, 0.45);
    color: #ffffff;
}

.isivi-btn-primary:active {
    transform: translateY(0) scale(0.98);
}

.isivi-btn-secondary {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    gap: 0.5rem;
    padding: 0.75rem 1.75rem;
    min-height: 44px;
    border-radius: 9999px;
    font-size: 0.875rem;
    font-weight: 600;
    text-align: center;
    background: var(--stone-900);
    color: var(--isivi-200);
    border: 1px solid rgba(184, 138, 76, 0.4);
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.4);
    transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
    cursor: pointer;
    text-decoration: none;
}

.isivi-btn-secondary:hover {
    background: var(--stone-850);
    border-color: var(--isivi-gold);
    color: #ffffff;
    transform: translateY(-2px);
    box-shadow: 0 8px 20px rgba(0, 0, 0, 0.5), 0 0 12px rgba(212, 175, 55, 0.15);
}

.isivi-btn-secondary:active {
    transform: translateY(0) scale(0.98);
}

.isivi-card-premium {
    background-color: var(--stone-950);
    border: 1px solid rgba(184, 138, 76, 0.25);
    border-radius: 1.25rem;
    transition: transform 0.25s ease, border-color 0.25s ease, box-shadow 0.25s ease;
}

.isivi-card-premium:hover {
    transform: translateY(-3px);
    border-color: rgba(212, 175, 55, 0.6);
    box-shadow: 0 12px 28px rgba(0, 0, 0, 0.6), 0 0 15px rgba(212, 175, 55, 0.08);
}

/* ---------- Smart Contextual Mobile Action Bar ---------- */
.isivi-mobile-context-bar {
    position: fixed;
    bottom: 0;
    left: 0;
    right: 0;
    z-index: 35;
    background: rgba(12, 10, 9, 0.98);
    border-top: 1px solid rgba(212, 175, 55, 0.35);
    padding: 0.75rem 1rem;
    padding-bottom: calc(0.75rem + env(safe-area-inset-bottom, 0px));
    box-shadow: 0 -8px 24px rgba(0, 0, 0, 0.7);
    transition: transform 0.25s cubic-bezier(0.16, 1, 0.3, 1), opacity 0.25s ease;
    transform: translateY(0);
    opacity: 1;
}

.isivi-mobile-context-bar.is-hidden {
    transform: translateY(105%);
    opacity: 0;
    pointer-events: none;
}

/* ---------- WhatsApp Floating Button & Context Bar Coordination ---------- */
.isivi-whatsapp-float {
    position: fixed;
    right: 1rem;
    bottom: calc(1rem + env(safe-area-inset-bottom, 0px));
    z-index: 40;
    transition: bottom 0.28s cubic-bezier(0.16, 1, 0.3, 1), transform 0.2s ease, background-color 0.2s ease;
}

@media (min-width: 640px) {
    .isivi-whatsapp-float {
        right: 1.25rem;
        bottom: calc(1.25rem + env(safe-area-inset-bottom, 0px));
    }
}

@media (max-width: 767px) {
    body.has-mobile-context-bar .isivi-whatsapp-float {
        bottom: calc(4.75rem + env(safe-area-inset-bottom, 0px));
    }
}

/* Narrow mobile screen optimization (320px–360px) */
@media (max-width: 360px) {
    .isivi-mobile-context-bar {
        padding: 0.5rem 0.625rem;
        padding-bottom: calc(0.5rem + env(safe-area-inset-bottom, 0px));
    }
    #mobile-context-label {
        font-size: 9px;
    }
    #mobile-context-subtext {
        font-size: 11px;
    }
    #mobile-context-btn {
        padding: 0.5rem 0.75rem;
        font-size: 11px;
    }
}

/* Microinteraction Animations */
@keyframes isivi-badge-pop {
    0% { transform: scale(1); }
    50% { transform: scale(1.3); }
    100% { transform: scale(1); }
}

.animate-badge-pop {
    animation: isivi-badge-pop 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

.card-btn-success {
    background: var(--emerald-700) !important;
    border-color: var(--emerald-400) !important;
    color: #ffffff !important;
    box-shadow: 0 0 12px rgba(16, 185, 129, 0.4) !important;
}

/* Step Progress Indicator (Compact Stepper inside Cart Drawer) */
.isivi-progress-stepper {
    position: relative;
    user-select: none;
    max-width: 540px;
    margin: 0 auto;
}

.isivi-progress-line {
    position: absolute;
    top: 13px;
    left: 8%;
    right: 8%;
    height: 2px;
    background: rgba(68, 64, 60, 0.6);
    z-index: 1;
}

.isivi-progress-fill {
    height: 100%;
    background: linear-gradient(90deg, var(--isivi-gold), var(--emerald-400));
    transition: width 0.3s ease;
    width: 0%;
}

.isivi-step-node {
    position: relative;
    z-index: 2;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 0.25rem;
}

.isivi-step-circle {
    width: 26px;
    height: 26px;
    border-radius: 9999px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 10px;
    font-weight: 700;
    background: var(--stone-900);
    border: 2px solid var(--stone-700);
    color: var(--stone-400);
    transition: all 0.25s ease;
}

.isivi-step-node.is-completed .isivi-step-circle {
    background: var(--emerald-950);
    border-color: var(--emerald-500);
    color: var(--emerald-300);
    box-shadow: 0 0 8px rgba(16, 185, 129, 0.35);
}

.isivi-step-node.is-completed .step-num {
    display: none;
}

.isivi-step-node.is-completed .step-check {
    display: inline-block !important;
}

.isivi-step-node.is-active .isivi-step-circle {
    background: var(--isivi-gold);
    border-color: #ffffff;
    color: var(--isivi-black);
    box-shadow: 0 0 10px rgba(212, 175, 55, 0.6);
    transform: scale(1.1);
}

.isivi-step-label {
    font-size: 9px;
    font-weight: 600;
    color: var(--stone-400);
    text-transform: uppercase;
    letter-spacing: 0.04em;
    transition: color 0.25s ease;
}

.isivi-step-node.is-active .isivi-step-label {
    color: var(--isivi-gold);
    font-weight: 700;
}

.isivi-step-node.is-completed .isivi-step-label {
    color: var(--emerald-400);
}

/* ============ CROSS-BROWSER & CROSS-DEVICE OPTIMIZATIONS (iOS Safari / Android Chrome / Desktop) ============ */

/* Prevent iOS Safari Automatic Viewport Zoom on Input Focus */
@media (max-width: 767px) {
    input[type="text"],
    input[type="tel"],
    input[type="email"],
    input[type="password"],
    input[type="search"],
    input[type="number"],
    input[type="date"],
    input[type="month"],
    select,
    textarea {
        font-size: 16px !important;
    }
}

/* Remove grey WebKit tap highlight and callout menus on iOS Safari */
button, a, input, select, textarea {
    -webkit-tap-highlight-color: transparent;
}

/* Force dark color scheme on native HTML5 date & month pickers */
input[type="date"],
input[type="month"] {
    color-scheme: dark;
}

/* Cross-browser flex shrink & image container stability */
.card-img-container {
    flex-shrink: 0;
    width: 100%;
    position: relative;
}

/* iOS Safe Area Inset Support for Floating Elements */
@supports (padding-bottom: env(safe-area-inset-bottom)) {
    .isivi-whatsapp-float {
        bottom: calc(1rem + env(safe-area-inset-bottom, 0px));
    }
    @media (min-width: 640px) {
        .isivi-whatsapp-float {
            bottom: calc(1.25rem + env(safe-area-inset-bottom, 0px));
        }
    }
}

/* ============================================================
   Banners Carousel Custom Styling
   ============================================================ */
#client-banners .carousel-viewport {
    perspective: 1000px;
    position: relative;
    width: 100%;
}

#client-banners .carousel-slide {
    will-change: transform, opacity;
}

#client-banners .carousel-slide article {
    border-color: rgba(212, 175, 55, 0.1);
    transition: transform 0.5s cubic-bezier(0.25, 1, 0.5, 1), 
                opacity 0.5s cubic-bezier(0.25, 1, 0.5, 1), 
                filter 0.5s cubic-bezier(0.25, 1, 0.5, 1), 
                border-color 0.5s cubic-bezier(0.25, 1, 0.5, 1);
}

#client-banners .carousel-control {
    outline: none;
}

#client-banners .carousel-control:focus-visible,
#client-banners .carousel-indicator:focus-visible {
    outline: 2px solid var(--isivi-gold);
    outline-offset: 2px;
}

#client-banners .carousel-indicator[aria-current="true"] {
    color: var(--isivi-gold) !important;
}

#client-banners .carousel-cta {
    transition: all 0.3s cubic-bezier(0.25, 1, 0.5, 1);
}

#client-banners .carousel-cta:active {
    box-shadow: 0 0 10px rgba(212, 175, 55, 0.6);
    transform: scale(0.97);
}

/* Premium Mobile Carousel Overrides */
@media (max-width: 767px) {
    #client-banners {
        padding-top: 0.75rem !important;
        padding-bottom: 0.5rem !important;
        padding-left: 0.5rem !important;
        padding-right: 0.5rem !important;
    }
    
    #servicios {
        padding-top: 1.5rem !important;
        padding-bottom: 2rem !important;
    }

    #client-banners .carousel-control {
        display: none !important;
    }
}
/* Editorial Ambient Lighting & Section Blends */
#productos {
    background-image: 
        radial-gradient(circle at 10% 20%, rgba(184, 138, 76, 0.025) 0%, transparent 50%),
        radial-gradient(circle at 90% 80%, rgba(52, 211, 153, 0.02) 0%, transparent 50%);
    background-size: cover;
}

#kits {
    background-image: 
        radial-gradient(circle at 50% 50%, rgba(184, 138, 76, 0.03) 0%, transparent 60%);
    background-size: cover;
}

.editorial-divider {
    height: 1px;
    background: linear-gradient(to right, transparent, rgba(212, 175, 55, 0.35) 50%, transparent);
    width: 100%;
    pointer-events: none;
    position: relative;
}

.editorial-divider::after {
    content: '✦';
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    color: rgba(212, 175, 55, 0.55);
    font-size: 8px;
    padding: 0 10px;
    background: #0f0e0d; /* var(--isivi-black) */
}

/* Success Modal Animation & Ticket Accent */
#success-icon-container {
    animation: successIconPop 0.5s cubic-bezier(0.16, 1, 0.3, 1) both;
}

#success-booking-code {
    letter-spacing: 0.08em;
    background: rgba(184, 138, 76, 0.08) !important;
    border: 1px solid rgba(184, 138, 76, 0.4) !important;
    box-shadow: 0 4px 12px rgba(184, 138, 76, 0.1);
}

@keyframes successIconPop {
    0% { transform: scale(0.6); opacity: 0; }
    100% { transform: scale(1); opacity: 1; }
}

/* Uniform Carousel Banner Dimensions */
#client-banners .carousel-slide article,
#client-banners article.mx-auto {
    height: 22rem !important; /* Constant height on mobile */
    display: flex !important;
    flex-direction: column !important; /* Vertical stack: text below image */
    box-sizing: border-box !important;
    border-radius: 26px !important; /* Oval corners */
}

#client-banners .carousel-slide img,
#client-banners article.mx-auto img {
    height: 11rem !important; /* Large image area */
    width: 100% !important;
    object-fit: cover !important;
    flex-shrink: 0 !important;
    border-top-left-radius: 26px !important;
    border-top-right-radius: 26px !important;
}

@media (min-width: 768px) {
    #client-banners .carousel-slide {
        max-width: 720px !important; /* Downscaled desktop banner width (5-10% less) */
    }

    #client-banners .carousel-slide article,
    #client-banners article.mx-auto {
        height: 23rem !important; /* Downscaled desktop banner height (5-10% less) */
        flex-direction: column !important; /* Always vertical stack */
        border-radius: 28px !important;
    }

    #client-banners .carousel-slide img,
    #client-banners article.mx-auto img {
        height: 12.8rem !important; /* Downscaled desktop image height */
        width: 100% !important;
        object-fit: cover !important;
        border-top-left-radius: 28px !important;
        border-top-right-radius: 28px !important;
    }
}

/* Unified Card Sizing System */
:root {
    /* Mobile sizes: slightly more vertical (+5-8% card height, +8-12% image height) */
    --catalog-card-width-base: 250px;
    --catalog-card-height: 325px; 
    --catalog-card-height-service: 355px;
    --catalog-card-image-height: 121px;
    --catalog-card-image-height-service: 154px;
}

/* PC / Desktop scaling progression */
@media (min-width: 1024px) {
    :root {
        --catalog-card-width-base: 280px;
        --catalog-card-height: 360px;
        --catalog-card-height-service: 390px;
        --catalog-card-image-height: 140px;
        --catalog-card-image-height-service: 175px;
    }
}
@media (min-width: 1440px) {
    :root {
        --catalog-card-width-base: 310px;
        --catalog-card-height: 390px;
        --catalog-card-height-service: 420px;
        --catalog-card-image-height: 155px;
        --catalog-card-image-height-service: 195px;
    }
}

/* Base styles for premium catalog cards */
.isivi-card-premium {
    width: var(--catalog-card-width-base) !important;
    height: var(--catalog-card-height) !important;
    box-sizing: border-box !important;
    display: flex !important;
    flex-direction: column !important;
    justify-content: space-between !important;
    flex-shrink: 0 !important;
    min-width: 0 !important;
    max-width: 100% !important;
    overflow: hidden !important;
    position: relative !important;
    min-height: 0 !important;
    border-radius: 26px !important; /* Oval corners (Referencia 1) */
}

/* Ensure inner wrappers align with the rounded corners */
.isivi-card-premium .relative.overflow-hidden.bg-stone-900,
.isivi-card-premium .relative.overflow-hidden.rounded-xl {
    border-top-left-radius: 26px !important;
    border-top-right-radius: 26px !important;
    border-bottom-left-radius: 16px !important;
    border-bottom-right-radius: 16px !important;
}

/* Services card is slightly wider (110% of base) */
#services-grid .isivi-card-premium {
    width: calc(var(--catalog-card-width-base) * 1.1) !important; /* 275px on mobile, scales on PC */
    height: var(--catalog-card-height-service) !important;
}

/* PC Font Smoothing, Nitidez & scaling overrides */
@media (min-width: 1024px) {
    body {
        -webkit-font-smoothing: antialiased !important;
        -moz-osx-font-smoothing: grayscale !important;
    }
    
    /* Desktop card content font scaling & padding */
    .isivi-card-premium {
        padding: 1.125rem !important; /* larger padding on desktop */
    }

    .isivi-card-premium h3, 
    .isivi-card-premium h4 {
        color: var(--isivi-gold) !important;
        font-weight: 800 !important;
        opacity: 1 !important;
        font-size: 0.95rem !important; /* larger titles on desktop */
        line-height: 1.3 !important;
        -webkit-font-smoothing: antialiased !important;
        -moz-osx-font-smoothing: grayscale !important;
    }
    
    .isivi-card-premium p {
        color: #f8f9fa !important;
        opacity: 1 !important;
        font-size: 0.8125rem !important; /* larger description on desktop */
        line-height: 1.4 !important;
        -webkit-font-smoothing: antialiased !important;
        -moz-osx-font-smoothing: grayscale !important;
    }

    .isivi-card-premium .text-isivi-gold {
        font-size: 1rem !important;
        font-weight: 900 !important;
        -webkit-font-smoothing: antialiased !important;
        -moz-osx-font-smoothing: grayscale !important;
    }

    .isivi-card-premium button {
        font-size: 0.75rem !important; /* larger button text on desktop */
        padding: 0.5rem 1rem !important;
    }
}

/* Responsive Mobile overrides to guarantee stability in iOS Safari */
@media (max-width: 480px) {
    #services-grid > *, #products-grid > *, #kits-grid > * {
        width: var(--catalog-card-width-base) !important;
        max-width: 100% !important;
        min-width: 0 !important;
        flex-shrink: 0 !important;
    }
    
    .isivi-card-premium {
        height: var(--catalog-card-height) !important;
        width: var(--catalog-card-width-base) !important;
    }

    #services-grid .isivi-card-premium {
        width: calc(var(--catalog-card-width-base) * 1.1) !important;
        height: var(--catalog-card-height-service) !important;
    }
}

#servicios, #productos, #kits, #client-banners {
    border-radius: 36px !important;
    margin-left: 1rem !important;
    margin-right: 1rem !important;
    margin-top: 1.5rem !important;
}

@media (min-width: 1024px) {
    #servicios, #productos, #kits, #client-banners {
        margin-left: 2rem !important;
        margin-right: 2rem !important;
        margin-top: 2rem !important;
    }
}

/* Ambient Lighting & Depth for Catalog Sections */
#servicios {
    background-image: 
        radial-gradient(circle at 15% 20%, rgba(212, 175, 55, 0.07) 0%, transparent 60%),
        radial-gradient(circle at 85% 80%, rgba(212, 175, 55, 0.03) 0%, transparent 50%) !important;
    position: relative;
}

#servicios .max-w-3xl {
    position: relative;
    background: radial-gradient(circle at 50% 50%, rgba(212, 175, 55, 0.05) 0%, transparent 70%);
}

#servicios::before {
    content: '';
    position: absolute;
    top: 6px;
    left: 50%;
    transform: translateX(-50%);
    width: 4px;
    height: 4px;
    background-color: var(--isivi-gold);
    border-radius: 50%;
    opacity: 0.6;
    box-shadow: 0 0 8px var(--isivi-gold);
}

#productos {
    background-image: 
        radial-gradient(circle at 75% 20%, rgba(184, 138, 76, 0.09) 0%, transparent 60%),
        radial-gradient(circle at 25% 80%, rgba(212, 175, 55, 0.04) 0%, transparent 55%) !important;
    position: relative;
}

#productos::before {
    content: '';
    position: absolute;
    top: 6px;
    left: 50%;
    transform: translateX(-50%);
    width: 4px;
    height: 4px;
    background-color: var(--isivi-gold);
    border-radius: 50%;
    opacity: 0.6;
    box-shadow: 
        0 0 8px var(--isivi-gold),
        -30px 4px 0 rgba(212, 175, 55, 0.15),
        30px -4px 0 rgba(184, 138, 76, 0.15);
}

#kits {
    background-image: 
        radial-gradient(circle at 50% 30%, rgba(181, 146, 38, 0.08) 0%, transparent 65%),
        radial-gradient(circle at 80% 80%, rgba(16, 185, 129, 0.03) 0%, transparent 50%),
        radial-gradient(circle at 20% 20%, rgba(212, 175, 55, 0.04) 0%, transparent 40%) !important;
    position: relative;
}

#kits::before {
    content: '';
    position: absolute;
    top: 6px;
    left: 50%;
    transform: translateX(-50%);
    width: 4px;
    height: 4px;
    background-color: var(--isivi-gold);
    border-radius: 50%;
    opacity: 0.6;
    box-shadow: 
        0 0 8px var(--isivi-gold),
        -20px 0 0 rgba(16, 185, 129, 0.25),
        20px 0 0 rgba(212, 175, 55, 0.25);
}

/* Banners Section Atmosphere Decor */
#client-banners {
    background-image: radial-gradient(circle at 50% 50%, rgba(212, 175, 55, 0.05) 0%, transparent 60%) !important;
    position: relative;
    border-bottom: 1px solid transparent !important;
    border-image: linear-gradient(to right, transparent, rgba(212, 175, 55, 0.25) 50%, transparent) 1 !important;
}

#client-banners::before {
    content: '';
    position: absolute;
    bottom: 0;
    left: 50%;
    transform: translate(-50%, 50%);
    width: 6px;
    height: 6px;
    background-color: var(--stone-950);
    border: 1px solid var(--isivi-gold);
    border-radius: 50%;
    z-index: 10;
    box-shadow: 0 0 6px rgba(212, 175, 55, 0.3);
}

/* ==========================================================================
   GLOBAL TEXT SHARPNESS & HIGH-CONTRAST SYSTEM (ISIVI Premium Edition)
   ========================================================================== */

/* 1. Global Font Smoothing reset for all text containers */
body, button, input, select, textarea, span, p, h1, h2, h3, h4, h5, h6, strong, a, label {
    -webkit-font-smoothing: antialiased !important;
    -moz-osx-font-smoothing: grayscale !important;
    text-rendering: optimizeLegibility !important;
}

/* 2. Form fields, placeholders and controls optimization */
input::placeholder, textarea::placeholder {
    color: rgba(224, 200, 165, 0.7) !important;
    opacity: 1 !important;
}

input, select, textarea {
    color: #ffffff !important;
    font-weight: 500 !important;
    border-color: rgba(212, 175, 55, 0.3) !important;
}

input:focus, select:focus, textarea:focus {
    border-color: var(--isivi-gold) !important;
    box-shadow: 0 0 0 1px var(--isivi-gold) !important;
}

/* 3. Secondary texts contrast adjustments (prevent muted gray) */
.text-isivi-300 {
    color: #ebdcb9 !important;
    opacity: 1 !important;
}

.text-stone-400, .text-stone-500 {
    color: #d1c2a5 !important;
    opacity: 1 !important;
}

/* 6. Buttons legibility & contrast */
button {
    font-weight: 700 !important;
    letter-spacing: 0.05em !important;
}

/* 7. Success Screen text refinement */
#success-booking-code, .text-emerald-400 {
    color: #10b981 !important;
    text-shadow: none !important;
}

/* 8. Cart Drawer & Modal label high contrast overrides */
#cart-drawer label, 
.isivi-modal label,
.isivi-modal span,
#cart-drawer span,
#cart-drawer p,
.isivi-modal p {
    color: #ebdcb9 !important; /* champagne color */
    opacity: 1 !important;
}

#cart-drawer strong,
.isivi-modal strong {
    color: #ffffff !important;
    opacity: 1 !important;
}

```

## 4. isivi.js
```javascript
// ============================================================
// ISIVI - Frontend conectado al backend Spring Boot (REST API)
// Reemplaza el antiguo localStorage por fetch() al backend real
// ============================================================

const API_BASE = '/api';
const SALON_WHATSAPP_DETAL = "573008949050";

// ---------- Capa de Analítica y Conversión (100% Privada, Idempotente y Segura) ----------
const trackedUniqueAnalyticsEvents = new Set();
const SENSITIVE_ANALYTICS_KEYS = new Set([
    'password', 'password_confirmation', 'token', 'admin_token',
    'card', 'cardNumber', 'cvv', 'cvc', 'expMonth', 'expYear',
    'private_key', 'integrity_secret', 'events_secret', 'secret',
    'phone', 'telefono', 'email', 'correo', 'address', 'direccion',
    'customerPhone', 'customerEmail', 'customerAddress'
]);

function sanitizeAnalyticsParams(params) {
    if (!params || typeof params !== 'object') return {};
    const sanitized = {};
    for (const [key, value] of Object.entries(params)) {
        const lowerKey = key.toLowerCase();
        if (!SENSITIVE_ANALYTICS_KEYS.has(key) &&
            !lowerKey.includes('pass') &&
            !lowerKey.includes('secret') &&
            !lowerKey.includes('token') &&
            !lowerKey.includes('phone') &&
            !lowerKey.includes('tel')) {
            if (typeof value === 'string' || typeof value === 'number' || typeof value === 'boolean' || Array.isArray(value)) {
                sanitized[key] = value;
            }
        }
    }
    return sanitized;
}

function trackEvent(eventName, eventParams = {}, uniqueKey = null) {
    try {
        if (uniqueKey) {
            const deduplicationId = `${eventName}_${uniqueKey}`;
            if (trackedUniqueAnalyticsEvents.has(deduplicationId)) {
                return;
            }
            trackedUniqueAnalyticsEvents.add(deduplicationId);
        }

        const safeParams = sanitizeAnalyticsParams(eventParams);
        const payload = {
            event: eventName,
            timestamp: new Date().toISOString(),
            ...safeParams
        };

        window.dataLayer = window.dataLayer || [];
        window.dataLayer.push(payload);

        if (typeof window.dispatchEvent === 'function') {
            window.dispatchEvent(new CustomEvent('isivi_analytics_event', { detail: payload }));
        }

        if (window.ISIVI_ANALYTICS_DEBUG) {
            console.debug('[ISIVI Analytics]', eventName, safeParams);
        }
    } catch (e) {
        // Silencioso para garantizar cero impacto en la UI
    }
}

// ---------- Conexión Asíncrona y Segura con Google Analytics 4 (GA4) ----------
function initGA4IfConfigured() {
    try {
        const metaTag = document.querySelector('meta[name="ga-measurement-id"]');
        const measurementId = window.ISIVI_GA_MEASUREMENT_ID || (metaTag ? metaTag.getAttribute('content') : null);

        if (measurementId && measurementId.startsWith('G-') && measurementId !== 'G-XXXXXXXXXX') {
            if (!document.getElementById('isivi-ga4-script')) {
                const script = document.createElement('script');
                script.id = 'isivi-ga4-script';
                script.async = true;
                script.src = `https://www.googletagmanager.com/gtag/js?id=${encodeURIComponent(measurementId)}`;
                document.head.appendChild(script);

                window.dataLayer = window.dataLayer || [];
                function gtag() { window.dataLayer.push(arguments); }
                window.gtag = window.gtag || gtag;
                gtag('js', new Date());
                gtag('config', measurementId, {
                    send_page_view: false, // El page_view se gestiona de forma centralizada con trackEvent
                    anonymize_ip: true
                });
            }
        }
    } catch (e) {
        // Silencioso: GA4 jamás bloquea la interfaz
    }
}

// ---------- Generación Dinámica de Schema.org desde Fuente de Verdad ----------
const DAY_SCHEMA_MAP = {
    0: 'Sunday',
    1: 'Monday',
    2: 'Tuesday',
    3: 'Wednesday',
    4: 'Thursday',
    5: 'Friday',
    6: 'Saturday'
};

function convertSlotTo24h(slotStr) {
    if (!slotStr) return '08:00';
    const parts = slotStr.trim().split(' ');
    if (parts.length < 2) return slotStr;
    const [h, m] = parts[0].split(':').map(Number);
    const period = parts[1].toUpperCase();
    let hour24 = h;
    if (period === 'PM' && h < 12) hour24 += 12;
    if (period === 'AM' && h === 12) hour24 = 0;
    return `${String(hour24).padStart(2, '0')}:${String(m).padStart(2, '0')}`;
}

function updateDynamicSchemaJsonLd() {
    try {
        const workingDays = (agendaConfiguration && agendaConfiguration.diasLaborales) || [0, 2, 3, 4, 5, 6];
        const slots = (agendaConfiguration && agendaConfiguration.horarios) || appointmentTimeSlots;
        const schemaDays = workingDays.map(d => DAY_SCHEMA_MAP[d]).filter(Boolean);

        const openTime = slots.length ? convertSlotTo24h(slots[0]) : '08:00';
        const lastSlot = slots.length ? slots[slots.length - 1] : '06:00 PM';
        const closeTime = slots.length ? convertSlotTo24h(lastSlot).replace(/^(\d{2}):/, (_, h) => `${String(Math.min(23, Number(h) + 1)).padStart(2, '0')}:`) : '19:00';

        const paymentMethods = ["Wompi", "Transferencia Bancolombia", "Transferencia Nequi", "Transferencia Daviplata"];

        const schemaData = {
            "@context": "https://schema.org",
            "@type": "BeautySalon",
            "name": "ISIVI - Peluquería & Cuidado Capilar Natural",
            "image": "https://isivi-app.onrender.com/images/isivi-card.jpg",
            "logo": "https://isivi-app.onrender.com/images/isivi-logo-transparent.png",
            "@id": "https://isivi-app.onrender.com/#beautysalon",
            "url": "https://isivi-app.onrender.com/",
            "telephone": "+573008949050",
            "priceRange": "$$",
            "address": {
                "@type": "PostalAddress",
                "addressLocality": "Cartagena",
                "addressRegion": "Bolívar",
                "addressCountry": "CO"
            },
            "geo": {
                "@type": "GeoCoordinates",
                "latitude": 10.3997,
                "longitude": -75.5144
            },
            "openingHoursSpecification": [
                {
                    "@type": "OpeningHoursSpecification",
                    "dayOfWeek": schemaDays,
                    "opens": openTime,
                    "closes": closeTime
                }
            ],
            "paymentAccepted": paymentMethods.join(', '),
            "currenciesAccepted": "COP",
            "description": "Salón de belleza y peluquería en Cartagena especializado en cuidado capilar natural, tratamientos sin sal ni sulfatos agresivos, agendamiento de citas online y venta de productos artesanales."
        };

        let script = document.getElementById('isivi-schema-jsonld');
        if (!script) {
            script = document.createElement('script');
            script.id = 'isivi-schema-jsonld';
            script.type = 'application/ld+json';
            document.head.appendChild(script);
        }
        script.textContent = JSON.stringify(schemaData, null, 2);
    } catch (err) {
        console.warn('No se pudo actualizar Schema.org dinámico', err);
    }
}

let businessConfiguration = {
    ciudad: 'Cartagena',
    pais: 'Colombia',
    diasAtencion: 'Mar - Sáb',
    horaApertura: '08:00',
    horaCierre: '19:00',
    telefonoMayorista: '+57 300 962 3174'
};

function agendaTimeInMinutes(timeStr) {
    if (!timeStr || typeof timeStr !== 'string') return 0;
    const clean = timeStr.trim();
    if (!clean) return 0;

    const is12h = clean.toUpperCase().includes('AM') || clean.toUpperCase().includes('PM');
    if (is12h) {
        const parts = clean.split(/\s+/);
        const timePart = parts[0] || '';
        const period = (parts[1] || '').toUpperCase();
        const [hStr, mStr] = timePart.split(':');
        let h = parseInt(hStr, 10) || 0;
        const m = parseInt(mStr, 10) || 0;
        if (period === 'PM' && h < 12) h += 12;
        if (period === 'AM' && h === 12) h = 0;
        return h * 60 + m;
    } else {
        const [hStr, mStr] = clean.split(':');
        const h = parseInt(hStr, 10) || 0;
        const m = parseInt(mStr, 10) || 0;
        return h * 60 + m;
    }
}

function formatBusiness12hTime(timeStr, defaultFormatted) {
    if (!timeStr) return defaultFormatted;
    try {
        const clean = timeStr.trim();
        if (clean.toUpperCase().includes('AM') || clean.toUpperCase().includes('PM')) {
            return clean;
        }
        const [hStr, mStr] = clean.split(':');
        let h = parseInt(hStr, 10) || 0;
        const m = mStr !== undefined ? String(parseInt(mStr, 10) || 0).padStart(2, '0') : '00';
        const period = h >= 12 ? 'PM' : 'AM';
        let h12 = h % 12;
        if (h12 === 0) h12 = 12;
        return `${h12}:${m} ${period}`;
    } catch (_) {
        return timeStr;
    }
}

function applyBusinessConfigToUI() {
    const locText = `${businessConfiguration.ciudad || 'Cartagena'}, ${businessConfiguration.pais || 'Colombia'}`;
    const ap12 = formatBusiness12hTime(businessConfiguration.horaApertura, '8:00 AM');
    const ci12 = formatBusiness12hTime(businessConfiguration.horaCierre, '7:00 PM');
    const hoursText = `${businessConfiguration.diasAtencion || 'Mar - Sáb'}: ${ap12} - ${ci12}`;
    const mayoristaPhone = businessConfiguration.telefonoMayorista || '+57 300 962 3174';
    const mayoristaCleanPhone = mayoristaPhone.replace(/\D/g, '');

    const headerLocEl = document.getElementById('header-location-text');
    if (headerLocEl) headerLocEl.textContent = locText;

    const headerHoursEl = document.getElementById('header-hours-text');
    if (headerHoursEl) headerHoursEl.textContent = hoursText;

    const footerLocEl = document.getElementById('footer-location-text');
    if (footerLocEl) footerLocEl.textContent = locText;

    const footerMayoristaTextEl = document.getElementById('footer-mayorista-text');
    if (footerMayoristaTextEl) {
        const phoneFormatted = mayoristaPhone.startsWith('+') ? mayoristaPhone : `(+57) ${mayoristaPhone}`;
        footerMayoristaTextEl.textContent = `${phoneFormatted} (Mayorista)`;
    }

    const footerMayoristaLinkEl = document.getElementById('footer-mayorista-link');
    if (footerMayoristaLinkEl && mayoristaCleanPhone) {
        footerMayoristaLinkEl.href = `https://wa.me/${mayoristaCleanPhone}?text=Hola%20ISIVI%2C%20quiero%20informaci%C3%B3n%20para%20compras%20al%20por%20mayor.`;
    }
}

async function loadBusinessConfiguration() {
    try {
        const config = await apiGet('/configuracion');
        if (config && config.ciudad) {
            businessConfiguration = { ...businessConfiguration, ...config };
        }
    } catch (e) {
        console.warn('Usando configuración de negocio por defecto', e);
    } finally {
        applyBusinessConfigToUI();
    }
}

async function loadAgendaConfiguration() {
    try {
        const config = await apiGet('/agenda');
        if (config && Array.isArray(config.diasLaborales) && config.diasLaborales.length > 0) {
            agendaConfiguration = config;
            if (Array.isArray(config.horarios) && config.horarios.length > 0) {
                appointmentTimeSlots = [...config.horarios];
            }
        }
    } catch (e) {
        console.warn('Usando agenda por defecto', e);
    } finally {
        updateDynamicSchemaJsonLd();
        loadBusinessConfiguration();
    }
}

let serviceCategories = [{ id: 'todos', name: 'Todos los Servicios' }];

const transferOptions = {
    bancolombia: { title: 'Bancolombia - Cuenta Ahorros', number: '300-894-9050', holder: 'ISIVI Belleza Natural', icon: 'fa-building-columns', color: 'text-isivi-gold' },
    nequi: { title: 'Nequi', number: '3008949050', holder: 'ISIVI Capilar', icon: 'fa-mobile-screen-button', color: 'text-purple-400' },
    daviplata: { title: 'Daviplata', number: '3008949050', holder: 'ISIVI Capilar', icon: 'fa-wallet', color: 'text-red-400' }
};

// Estado en memoria (se llena desde el backend)
let productsData = [];
let kitsData = [];
let servicesData = [];
let serviceCategoriesData = [];
let productCategoriesData = [];
let bookingsList = [];
let historyBookingsList = [];
let bannersData = [];
let isSavingReservation = false;

let selectedServices = [];
let selectedProducts = [];
let selectedKits = [];
// Las listas mantienen compatibilidad con el flujo existente; las cantidades son la fuente del carrito.
let productQuantities = {};
let kitQuantities = {};
let productCardQuantities = {};
let kitCardQuantities = {};
let activeCategory = 'todos';
let activeProductCategory = 'todos';
let activeKitCategory = 'todos';
let selectedProductVariants = {}; // { [productId]: variantId }
let currentEditingVariants = []; // Editor de variantes admin
let checkoutMode = 'services';

let currentDate = new Date();
let cartCalendarDate = new Date();
let adminWeekStart = getMonday(new Date());
let selectedDateStr = formatDateKey(new Date());
let selectedTimeSlot = '08:00 AM';
let currentPaymentMethod = 'bancolombia';
let appointmentTimeSlots = ["08:00 AM", "09:30 AM", "11:00 AM", "01:30 PM", "03:00 PM", "04:30 PM", "06:00 PM"];
let agendaConfiguration = { diasLaborales: [0, 2, 3, 4, 5, 6], horarios: [...appointmentTimeSlots] };

// ---------- Helpers de API ----------
async function parseErrorResponse(res, defaultMsg) {
    let msg = defaultMsg;
    let data = null;
    try {
        data = await res.json();
        if (data && data.mensaje) msg = data.mensaje;
        else if (data && data.message) msg = data.message;
        else if (data && data.error && typeof data.error === 'string') msg = data.error;
    } catch (_) {}

    // Mensaje contextual amigable según código HTTP si no vino mensaje específico
    if (!data || (!data.mensaje && !data.message)) {
        if (res.status === 400) msg = 'Los datos ingresados no son válidos. Revisa el formulario.';
        else if (res.status === 401) msg = adminAuthToken ? 'Tu sesión expiró. Vuelve a iniciar sesión.' : 'Se requiere autorización para realizar esta acción.';
        else if (res.status === 403) msg = 'No tienes permiso para realizar esta acción.';
        else if (res.status === 404) msg = adminAuthToken ? 'Registro no encontrado.' : 'El recurso que buscas ya no está disponible.';
        else if (res.status === 409) msg = 'El horario o ítem seleccionado ya no está disponible.';
        else if (res.status === 429) msg = 'Demasiadas solicitudes. Por favor espera unos segundos antes de reintentar.';
        else if (res.status === 502 || res.status === 503 || res.status === 504) msg = 'Estamos conectando con el servidor. Intenta nuevamente en unos segundos.';
        else if (res.status >= 500) msg = 'No pudimos completar la operación. Por favor intenta nuevamente.';
    }

    const err = new Error(msg);
    err.status = res.status;
    err.data = data;
    err.errorCode = data && data.error ? data.error : null;
    return err;
}

// Listeners globales de conectividad de red
if (typeof window !== 'undefined') {
    window.addEventListener('offline', () => {
        showToast('Sin conexión. Algunas funciones están temporalmente no disponibles.', 'error');
    });
    window.addEventListener('online', () => {
        showToast('Conexión restaurada.', 'success');
    });
}

let adminAuthToken = sessionStorage.getItem('isivi_admin_token') || null;

function getAuthHeaders(extraHeaders = {}) {
    const headers = { ...extraHeaders };
    if (adminAuthToken) {
        headers['Authorization'] = `Bearer ${adminAuthToken}`;
    }
    return headers;
}

function handleAuthError(err) {
    if (err && err.status === 401 && adminAuthToken) {
        adminAuthToken = null;
        sessionStorage.removeItem('isivi_admin_token');
        sessionStorage.removeItem('isivi_admin_user');
        showToast('Tu sesión de administrador ha expirado. Por favor inicia sesión nuevamente.', 'error');
        showAdminLogin();
    }
}

const REQUEST_TIMEOUT_MS = 15000;

async function fetchWithTimeout(url, options = {}, timeoutMs = REQUEST_TIMEOUT_MS) {
    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), timeoutMs);
    try {
        const response = await fetch(url, {
            ...options,
            signal: controller.signal
        });
        clearTimeout(timeoutId);
        return response;
    } catch (error) {
        clearTimeout(timeoutId);
        if (error.name === 'AbortError') {
            const timeoutErr = new Error('La conexión tardó demasiado tiempo. Por favor verifica tu señal móvil o Wi-Fi.');
            timeoutErr.name = 'TimeoutError';
            timeoutErr.status = 408;
            throw timeoutErr;
        }
        if (typeof navigator !== 'undefined' && navigator.onLine === false) {
            const offlineErr = new Error('Sin conexión a internet. Revisa tus datos móviles o Wi-Fi.');
            offlineErr.name = 'OfflineError';
            throw offlineErr;
        }
        throw error;
    }
}

const apiCache = new Map();

function clearApiCache() {
    apiCache.clear();
}

function apiGet(path, timeoutMs = REQUEST_TIMEOUT_MS) {
    const cached = apiCache.get(path);
    const ahora = Date.now();
    if (cached && (ahora - cached.timestamp < 3000)) {
        return cached.promise;
    }

    const promise = (async () => {
        const res = await fetchWithTimeout(`${API_BASE}${path}`, {
            headers: getAuthHeaders()
        }, timeoutMs);
        if (!res.ok) {
            const err = await parseErrorResponse(res, `GET ${path} -> ${res.status}`);
            handleAuthError(err);
            throw err;
        }
        return res.json();
    })();

    apiCache.set(path, { timestamp: ahora, promise: promise });
    return promise;
}

async function apiPost(path, body, timeoutMs = REQUEST_TIMEOUT_MS) {
    clearApiCache();
    const res = await fetchWithTimeout(`${API_BASE}${path}`, {
        method: 'POST',
        headers: getAuthHeaders({ 'Content-Type': 'application/json' }),
        body: JSON.stringify(body)
    }, timeoutMs);
    if (!res.ok) {
        const err = await parseErrorResponse(res, `POST ${path} -> ${res.status}`);
        handleAuthError(err);
        throw err;
    }
    if (res.status === 204) return null;
    const text = await res.text();
    return text ? JSON.parse(text) : null;
}

async function apiPut(path, body, timeoutMs = REQUEST_TIMEOUT_MS) {
    clearApiCache();
    const res = await fetchWithTimeout(`${API_BASE}${path}`, {
        method: 'PUT',
        headers: getAuthHeaders({ 'Content-Type': 'application/json' }),
        body: JSON.stringify(body)
    }, timeoutMs);
    if (!res.ok) {
        const err = await parseErrorResponse(res, `PUT ${path} -> ${res.status}`);
        handleAuthError(err);
        throw err;
    }
    if (res.status === 204) return null;
    const text = await res.text();
    return text ? JSON.parse(text) : null;
}

async function apiPatch(path, body = null, timeoutMs = REQUEST_TIMEOUT_MS) {
    clearApiCache();
    const options = { method: 'PATCH', headers: getAuthHeaders() };
    if (body) {
        options.headers = getAuthHeaders({ 'Content-Type': 'application/json' });
        options.body = JSON.stringify(body);
    }
    const res = await fetchWithTimeout(`${API_BASE}${path}`, options, timeoutMs);
    if (!res.ok) {
        const err = await parseErrorResponse(res, `PATCH ${path} -> ${res.status}`);
        handleAuthError(err);
        throw err;
    }
    if (res.status === 204) return null;
    const text = await res.text();
    return text ? JSON.parse(text) : null;
}

async function apiDelete(path, timeoutMs = REQUEST_TIMEOUT_MS) {
    clearApiCache();
    const res = await fetchWithTimeout(`${API_BASE}${path}`, {
        method: 'DELETE',
        headers: getAuthHeaders()
    }, timeoutMs);
    if (!res.ok && res.status !== 204) {
        const err = await parseErrorResponse(res, `DELETE ${path} -> ${res.status}`);
        handleAuthError(err);
        throw err;
    }
}

// ---------- Helper Centralizado de WhatsApp ----------
function normalizeColombianPhone(rawPhone) {
    if (!rawPhone) return '573008949050';
    let digits = String(rawPhone).replace(/\D/g, '');
    if (!digits) return '573008949050';
    while (digits.startsWith('5757')) {
        digits = digits.substring(2);
    }
    if (digits.length === 10 && digits.startsWith('3')) {
        return '57' + digits;
    }
    if (digits.startsWith('57') && digits.length === 12) {
        return digits;
    }
    if (digits.length >= 7 && digits.length <= 10) {
        return '57' + digits;
    }
    return digits;
}

function openWhatsApp(message, rawPhone = '573008949050') {
    const msg = (message || 'Hola ISIVI 👋, quiero más información.').trim();
    const phone = normalizeColombianPhone(rawPhone);
    const encodedText = encodeURIComponent(msg);
    const waUrl = `https://wa.me/${phone}?text=${encodedText}`;

    try {
        const opened = window.open(waUrl, '_blank', 'noopener,noreferrer');
        if (!opened || opened.closed || typeof opened.closed === 'undefined') {
            window.location.href = waUrl;
        }
        return true;
    } catch (err) {
        console.warn('Fallback al intentar abrir WhatsApp:', err);
        try {
            window.location.href = waUrl;
            return true;
        } catch (e) {
            showToast('No pudimos abrir WhatsApp automáticamente. Puedes escribirnos directamente al (+57) 300 894 9050.', 'info');
            return false;
        }
    }
}

function openAdminWhatsAppChat(rawPhone, bookingCode = '', customerName = '') {
    if (!rawPhone) {
        showToast('Esta reserva no cuenta con número telefónico registrado.', 'info');
        return;
    }
    const msg = `Hola ${customerName || ''} 👋, te escribimos de ISIVI respecto a tu reserva ${bookingCode || ''}.`;
    openWhatsApp(msg, rawPhone);
}

// ============ GESTIÓN DE RESERVA PROPIA RETENIDA TEMPORALMENTE ============
let currentPendingHold = null;
let pendingHoldInterval = null;

function getPendingHold() {
    try {
        let raw = null;
        try { raw = sessionStorage.getItem('isivi_pending_reservation'); } catch (e) {}
        if (!raw) {
            try { raw = localStorage.getItem('isivi_pending_reservation'); } catch (e) {}
        }
        if (!raw) return null;
        const parsed = JSON.parse(raw);
        if (!parsed || !parsed.id) {
            clearPendingHold();
            return null;
        }
        const expTime = parsed.expiration ? new Date(parsed.expiration).getTime() : 0;
        if (expTime && expTime <= Date.now()) {
            clearPendingHold();
            return null;
        }
        return parsed;
    } catch (e) {
        clearPendingHold();
        return null;
    }
}

function restoreCartFromHold(hold) {
    if (!hold || !Array.isArray(hold.itemsInventario)) return;
    selectedServices = [];
    selectedProducts = [];
    selectedKits = [];
    productQuantities = {};
    kitQuantities = {};
    hold.itemsInventario.forEach(item => {
        const id = String(item.id);
        const qty = Number(item.cantidad) || 1;
        if (item.tipo === 'servicio') {
            if (!selectedServices.includes(id)) {
                selectedServices.push(id);
            }
        } else if (item.tipo === 'producto') {
            const varId = item.varianteId ? String(item.varianteId) : null;
            const cartKey = varId ? `${id}_${varId}` : id;
            if (!selectedProducts.includes(cartKey)) {
                selectedProducts.push(cartKey);
            }
            productQuantities[cartKey] = qty;
            if (varId) {
                selectedProductVariants[id] = varId;
            }
        } else if (item.tipo === 'kit') {
            if (!selectedKits.includes(id)) {
                selectedKits.push(id);
            }
            kitQuantities[id] = qty;
        }
    });
}

function setPendingHold(reserva, expirationOverride) {
    if (!reserva || !reserva.id) {
        clearPendingHold();
        return;
    }
    // Determinar si es una CITA o un PEDIDO PURO
    const inv = reserva.itemsInventario || [];
    const hasService = inv.length > 0 
        ? inv.some(i => i.tipo === 'servicio') 
        : (reserva.hasService === true || Boolean((reserva.fechaCita || reserva.date) && (reserva.horaCita || reserva.time)));
    const isPureOrder = reserva.isPureOrder === true || (!hasService && (inv.length > 0 || Boolean(reserva.tipoEntrega || reserva.deliveryMethod)));

    const exp = expirationOverride || reserva.fechaExpiracionPago || reserva.paymentExpiration || (currentPendingHold && currentPendingHold.expiration) || new Date(Date.now() + 15 * 60 * 1000).toISOString();
    
    // Extraer services e itemsInventario para persistir en la recarga
    const services = reserva.services || (reserva.itemsInventario ? reserva.itemsInventario.filter(i => i.tipo === 'servicio').map(i => i.nombre) : []) || (currentPendingHold && currentPendingHold.services) || [];
    const itemsInventario = reserva.itemsInventario || (currentPendingHold && currentPendingHold.itemsInventario) || [];

    if (isPureOrder || !hasService) {
        currentPendingHold = {
            id: String(reserva.id),
            code: reserva.codigoReserva || reserva.code || (currentPendingHold && currentPendingHold.code) || '',
            customerName: reserva.nombreCliente || reserva.customerName || (currentPendingHold && currentPendingHold.customerName) || '',
            phone: reserva.telefono || reserva.phone || (currentPendingHold && currentPendingHold.phone) || '',
            expiration: exp,
            hasService: false,
            isPureOrder: true,
            subtotal: Number(reserva.subtotal) || (currentPendingHold && currentPendingHold.subtotal) || 0,
            deposit: Number(reserva.anticipo) || (currentPendingHold && currentPendingHold.deposit) || 0,
            services: services,
            itemsInventario: itemsInventario
        };
    } else {
        currentPendingHold = {
            id: String(reserva.id),
            code: reserva.codigoReserva || reserva.code || (currentPendingHold && currentPendingHold.code) || '',
            customerName: reserva.nombreCliente || reserva.customerName || (currentPendingHold && currentPendingHold.customerName) || '',
            phone: reserva.telefono || reserva.phone || (currentPendingHold && currentPendingHold.phone) || '',
            date: reserva.fechaCita || reserva.date || (currentPendingHold && currentPendingHold.date) || selectedDateStr || '',
            time: reserva.horaCita || reserva.time || (currentPendingHold && currentPendingHold.time) || selectedTimeSlot || '',
            expiration: exp,
            hasService: true,
            isPureOrder: false,
            subtotal: Number(reserva.subtotal) || (currentPendingHold && currentPendingHold.subtotal) || 0,
            deposit: Number(reserva.anticipo) || (currentPendingHold && currentPendingHold.deposit) || 0,
            services: services,
            itemsInventario: itemsInventario
        };
    }
    try { sessionStorage.setItem('isivi_pending_reservation', JSON.stringify(currentPendingHold)); } catch (e) {}
    try { localStorage.setItem('isivi_pending_reservation', JSON.stringify(currentPendingHold)); } catch (e) {}
    restoreCartFromHold(currentPendingHold);
    startPendingHoldTimer();
    renderPendingHoldUI();
}

function clearPendingHold() {
    currentPendingHold = null;
    try { sessionStorage.removeItem('isivi_pending_reservation'); } catch (e) {}
    try { localStorage.removeItem('isivi_pending_reservation'); } catch (e) {}
    stopPendingHoldTimer();
    renderPendingHoldUI();
    renderTimeSlots();
}

async function syncPendingHoldWithBackend() {
    const hold = getPendingHold();
    if (!hold || !hold.id) return;
    try {
        const res = await apiGet(`/reservas/${encodeURIComponent(hold.id)}?codigo=${encodeURIComponent(hold.code || '')}&telefono=${encodeURIComponent(hold.phone || '')}`);
        if (!res) {
            clearPendingHold();
            return;
        }
        const isPending = res.estado === 'Pendiente Pago' || res.estado === 'Pendiente Comprobante' || res.estado === 'PENDIENTE_PAGO' || res.estado === 'PENDIENTE_COMPROBANTE' || res.estado === 'Pendiente';
        const isUnpaid = res.estadoPago !== 'APROBADO' && res.estadoPago !== 'PAGO_CONFIRMADO' && res.estadoPago !== 'APROVADO';

        if (!isPending || !isUnpaid) {
            if (res.estadoPago === 'APROBADO' || res.estadoPago === 'PAGO_CONFIRMADO') {
                showToast('Pago aprobado. Pedido confirmado.', 'success');
            } else {
                showToast('El tiempo de pago expiró. La unidad vuelve a estar disponible.', 'warning');
            }
            clearPendingHold();
            await refreshCatalogStock();
        } else {
            const exp = res.fechaExpiracionPago;
            if (exp) {
                setPendingHold(res);
            }
        }
    } catch (err) {
        console.warn('Error al sincronizar hold con backend:', err);
    }
}

function startPendingHoldTimer() {
    stopPendingHoldTimer();
    currentPendingHold = getPendingHold();
    if (!currentPendingHold) return;

    function update() {
        if (!currentPendingHold) {
            stopPendingHoldTimer();
            renderPendingHoldUI();
            return;
        }
        const remainingMs = new Date(currentPendingHold.expiration).getTime() - Date.now();
        if (remainingMs <= 0) {
            stopPendingHoldTimer();
            const wasPureOrder = currentPendingHold.isPureOrder === true;
            currentPendingHold = null;
            try { sessionStorage.removeItem('isivi_pending_reservation'); } catch (e) {}
            try { localStorage.removeItem('isivi_pending_reservation'); } catch (e) {}
            if (wasPureOrder) {
                showToast('El tiempo de pago expiró. La unidad vuelve a estar disponible.', 'warning');
            } else {
                showToast('El tiempo de reserva de tu horario ha terminado. Puedes elegir un nuevo horario conservando tu carrito.', 'warning');
            }
            renderPendingHoldUI();
            renderTimeSlots();
            return;
        }

        const totalSecs = Math.floor(remainingMs / 1000);
        const mins = Math.floor(totalSecs / 60);
        const secs = totalSecs % 60;
        const formattedTimer = `${String(mins).padStart(2, '0')}:${String(secs).padStart(2, '0')}`;
        
        const expDate = new Date(currentPendingHold.expiration);
        const formattedExpTime = expDate.toLocaleTimeString('es-CO', { hour: '2-digit', minute: '2-digit', hour12: true, timeZone: 'America/Bogota' });

        document.querySelectorAll('.pending-hold-timer-text').forEach(el => {
            el.textContent = formattedTimer;
        });
        document.querySelectorAll('.pending-hold-expiry-text').forEach(el => {
            el.textContent = formattedExpTime;
        });
    }

    update();
    pendingHoldInterval = setInterval(update, 1000);
}

function stopPendingHoldTimer() {
    if (pendingHoldInterval) {
        clearInterval(pendingHoldInterval);
        pendingHoldInterval = null;
    }
}

function renderPendingHoldUI() {
    const hold = getPendingHold();
    const cartBanner = document.getElementById('cart-pending-hold-banner');
    const itemsList = document.getElementById('cart-items-list');

    // Determinar si el carrito actual es exclusivamente de productos
    const hasCartProducts = selectedProducts.length > 0 || selectedKits.length > 0;
    const hasCartServices = selectedServices.length > 0;
    const isCartPureOrder = !hasCartServices && hasCartProducts;

    if (!hold) {
        if (cartBanner) cartBanner.classList.add('hidden');
        updateMobileContextBar();
        return;
    }

    const isPureOrderHold = hold.isPureOrder === true;

    if (isPureOrderHold) {
        if (!cartBanner) {
            const bannerDiv = document.createElement('div');
            bannerDiv.id = 'cart-pending-hold-banner';
            bannerDiv.className = 'mx-5 mb-3 p-4 rounded-2xl bg-amber-950/70 border border-amber-500/60 text-amber-200 space-y-2.5 shadow-lg';
            bannerDiv.innerHTML = `
                <div class="flex items-center justify-between">
                    <span class="text-xs font-bold uppercase tracking-wider flex items-center gap-1.5 text-amber-300">
                        <i class="fa-solid fa-credit-card text-amber-400"></i> PAGO PENDIENTE
                    </span>
                    <span class="pending-hold-timer-text px-2.5 py-0.5 rounded-full text-xs font-mono font-bold bg-amber-900 text-amber-100 border border-amber-500/50" aria-live="polite">--:--</span>
                </div>
                <div class="text-xs text-amber-100/90 space-y-0.5">
                    <div>Tu pedido sigue disponible temporalmente.</div>
                    <div class="text-[11px] text-amber-300/80 pt-0.5">Puedes retomar el pago o liberar el pedido antes de las <span class="pending-hold-expiry-text font-bold">--:--</span></div>
                </div>
                <div class="flex items-center gap-2 pt-1">
                    <button type="button" onclick="resumeWompiPayment()" class="flex-1 py-2.5 px-3 rounded-xl bg-isivi-gold text-isivi-black font-bold text-xs hover:brightness-110 transition flex items-center justify-center gap-1.5 shadow">
                        <i class="fa-solid fa-credit-card"></i> RETOMAR PAGO
                    </button>
                    <button type="button" onclick="cancelPendingHold()" class="py-2.5 px-3 rounded-xl bg-stone-900 text-stone-300 border border-stone-700 hover:text-white font-medium text-xs transition" title="Liberar pedido y devolver stock">
                        Liberar pedido
                    </button>
                </div>
            `;
            itemsList.parentNode.insertBefore(bannerDiv, itemsList);
        } else {
            cartBanner.className = 'mx-5 mb-3 p-4 rounded-2xl bg-amber-950/70 border border-amber-500/60 text-amber-200 space-y-2.5 shadow-lg';
            cartBanner.innerHTML = `
                <div class="flex items-center justify-between">
                    <span class="text-xs font-bold uppercase tracking-wider flex items-center gap-1.5 text-amber-300">
                        <i class="fa-solid fa-credit-card text-amber-400"></i> PAGO PENDIENTE
                    </span>
                    <span class="pending-hold-timer-text px-2.5 py-0.5 rounded-full text-xs font-mono font-bold bg-amber-900 text-amber-100 border border-amber-500/50" aria-live="polite">--:--</span>
                </div>
                <div class="text-xs text-amber-100/90 space-y-0.5">
                    <div>Tu pedido sigue disponible temporalmente.</div>
                    <div class="text-[11px] text-amber-300/80 pt-0.5">Puedes retomar el pago o liberar el pedido antes de las <span class="pending-hold-expiry-text font-bold">--:--</span></div>
                </div>
                <div class="flex items-center gap-2 pt-1">
                    <button type="button" onclick="resumeWompiPayment()" class="flex-1 py-2.5 px-3 rounded-xl bg-isivi-gold text-isivi-black font-bold text-xs hover:brightness-110 transition flex items-center justify-center gap-1.5 shadow">
                        <i class="fa-solid fa-credit-card"></i> RETOMAR PAGO
                    </button>
                    <button type="button" onclick="cancelPendingHold()" class="py-2.5 px-3 rounded-xl bg-stone-900 text-stone-300 border border-stone-700 hover:text-white font-medium text-xs transition" title="Liberar pedido y devolver stock">
                        Liberar pedido
                    </button>
                </div>
            `;
            cartBanner.classList.remove('hidden');
        }
    } else {
        if (isCartPureOrder) {
            if (cartBanner) cartBanner.classList.add('hidden');
            updateMobileContextBar();
            return;
        }

        if (!cartBanner) {
            const bannerDiv = document.createElement('div');
            bannerDiv.id = 'cart-pending-hold-banner';
            bannerDiv.className = 'mx-5 mb-3 p-4 rounded-2xl bg-amber-950/70 border border-amber-500/60 text-amber-200 space-y-2.5 shadow-lg';
            bannerDiv.innerHTML = `
                <div class="flex items-center justify-between">
                    <span class="text-xs font-bold uppercase tracking-wider flex items-center gap-1.5 text-amber-300">
                        <i class="fa-solid fa-clock text-amber-400"></i> TU RESERVA SIGUE ACTIVA
                    </span>
                    <span class="pending-hold-timer-text px-2.5 py-0.5 rounded-full text-xs font-mono font-bold bg-amber-900 text-amber-100 border border-amber-500/50" aria-live="polite">--:--</span>
                </div>
                <div class="text-xs text-amber-100/90 space-y-0.5">
                    <div>Fecha: <strong class="text-white">${hold.date || 'Fecha seleccionada'}</strong></div>
                    <div>Hora: <strong class="text-white">${hold.time || 'Hora seleccionada'}</strong></div>
                    <div class="text-[11px] text-amber-300/80 pt-0.5">Retenido temporalmente hasta las <span class="pending-hold-expiry-text font-bold">--:--</span></div>
                </div>
                <div class="flex items-center gap-2 pt-1">
                    <button type="button" onclick="resumeWompiPayment()" class="flex-1 py-2.5 px-3 rounded-xl bg-isivi-gold text-isivi-black font-bold text-xs hover:brightness-110 transition flex items-center justify-center gap-1.5 shadow">
                        <i class="fa-solid fa-credit-card"></i> RETOMAR PAGO
                    </button>
                    <button type="button" onclick="cancelPendingHold()" class="py-2.5 px-3 rounded-xl bg-stone-900 text-stone-300 border border-stone-700 hover:text-white font-medium text-xs transition" title="Liberar este horario y elegir otro">
                        Elegir otro horario
                    </button>
                </div>
            `;
            itemsList.parentNode.insertBefore(bannerDiv, itemsList);
        } else {
            cartBanner.className = 'mx-5 mb-3 p-4 rounded-2xl bg-amber-950/70 border border-amber-500/60 text-amber-200 space-y-2.5 shadow-lg';
            cartBanner.innerHTML = `
                <div class="flex items-center justify-between">
                    <span class="text-xs font-bold uppercase tracking-wider flex items-center gap-1.5 text-amber-300">
                        <i class="fa-solid fa-clock text-amber-400"></i> TU RESERVA SIGUE ACTIVA
                    </span>
                    <span class="pending-hold-timer-text px-2.5 py-0.5 rounded-full text-xs font-mono font-bold bg-amber-900 text-amber-100 border border-amber-500/50" aria-live="polite">--:--</span>
                </div>
                <div class="text-xs text-amber-100/90 space-y-0.5">
                    <div>Fecha: <strong class="text-white">${hold.date || 'Fecha seleccionada'}</strong></div>
                    <div>Hora: <strong class="text-white">${hold.time || 'Hora seleccionada'}</strong></div>
                    <div class="text-[11px] text-amber-300/80 pt-0.5">Retenido temporalmente hasta las <span class="pending-hold-expiry-text font-bold">--:--</span></div>
                </div>
                <div class="flex items-center gap-2 pt-1">
                    <button type="button" onclick="resumeWompiPayment()" class="flex-1 py-2.5 px-3 rounded-xl bg-isivi-gold text-isivi-black font-bold text-xs hover:brightness-110 transition flex items-center justify-center gap-1.5 shadow">
                        <i class="fa-solid fa-credit-card"></i> RETOMAR PAGO
                    </button>
                    <button type="button" onclick="cancelPendingHold()" class="py-2.5 px-3 rounded-xl bg-stone-900 text-stone-300 border border-stone-700 hover:text-white font-medium text-xs transition" title="Liberar este horario y elegir otro">
                        Elegir otro horario
                    </button>
                </div>
            `;
            cartBanner.classList.remove('hidden');
        }
    }

    updateMobileContextBar();
}

async function releaseHoldImmediately(hold) {
    if (!hold || !hold.id) return;
    clearPendingHold();
    try {
        await apiPost(`/reservas/${encodeURIComponent(hold.id)}/liberar-retencion`, {
            telefono: hold.phone || '',
            codigoReserva: hold.code || ''
        });
    } catch (err) {
        console.warn('Liberación de retención en backend:', err);
    }
}

async function cancelPendingHold() {
    const hold = getPendingHold();
    const wasPureOrder = hold && hold.isPureOrder === true;
    if (hold && hold.id) {
        await releaseHoldImmediately(hold);
    } else {
        clearPendingHold();
    }
    selectedTimeSlot = '';
    if (wasPureOrder) {
        showToast('Pedido cancelado. La unidad vuelve a estar disponible.', 'info');
    } else {
        showToast('Horario liberado. Puedes elegir un nuevo horario conservando tus servicios y productos seleccionados.', 'info');
    }
    await refreshCatalogStock();
    await renderTimeSlots();
    await renderCartSchedule();
    updateSummary();
    updateBookingProgressTracker();
    updateMobileContextBar();
}

async function syncCartItemsWithHold(holdId, holdCode, holdPhone) {
    const items = [
        ...selectedServices.map(id => ({ id, tipo: 'servicio', cantidad: 1 })),
        ...selectedProducts.map(id => {
            const key = id;
            const itemId = key.includes('_') ? key.split('_')[0] : key;
            const varId = key.includes('_') ? key.split('_')[1] : null;
            return {
                id: itemId,
                tipo: 'producto',
                cantidad: productQuantities[key] || 1,
                varianteId: varId
            };
        }),
        ...selectedKits.map(id => ({
            id: id,
            tipo: 'kit',
            cantidad: kitQuantities[id] || 1
        }))
    ];
    const url = `/reservas/${encodeURIComponent(holdId)}/items?codigo=${encodeURIComponent(holdCode)}&telefono=${encodeURIComponent(holdPhone)}`;
    const updated = await apiPost(url, items);
    return updated;
}

async function resumeWompiPayment(overrideReserva) {
    if (typeof navigator !== 'undefined' && !navigator.onLine) {
        showToast('Sin conexión a internet. Revisa tu conexión e inténtalo nuevamente.', 'error');
        return;
    }

    const hold = overrideReserva ? {
        id: overrideReserva.id,
        code: overrideReserva.codigoReserva || overrideReserva.code,
        phone: overrideReserva.telefono || overrideReserva.phone,
        expiration: overrideReserva.fechaExpiracionPago || overrideReserva.paymentExpiration
    } : (currentPendingHold || getPendingHold());

    if (!hold || !hold.id) {
        return showToast('No se encontró una reserva pendiente para retomar.', 'error');
    }

    if (hold.expiration && new Date(hold.expiration).getTime() <= Date.now()) {
        clearPendingHold();
        return showToast('El tiempo de retención para este horario ha expirado. Por favor selecciona otro turno.', 'error');
    }

    try {
        const updated = await syncCartItemsWithHold(hold.id, hold.code, hold.phone);
        setPendingHold(updated);
    } catch (err) {
        console.error('Error al sincronizar items antes de retomar:', err);
        if (err.status === 409 || (err.message && (err.message.includes('stock') || err.message.includes('agotase') || err.message.includes('disponible')))) {
            showToast(err.message || 'El producto ya no está disponible por cambios en el stock.', 'error');
            await refreshCatalogStock();
            return;
        }
        showToast('No se pudo sincronizar el pedido para retomar el pago.', 'error');
        return;
    }

    if (isSavingReservation) return;
    isSavingReservation = true;
    trackEvent('payment_retry', { method: 'WOMPI', booking_id: hold.id });

    let timeoutWarningTimer = setTimeout(() => {
        if (isSavingReservation) {
            showToast('Wompi está tardando más de lo esperado. Mantén esta pestaña abierta...', 'info');
        }
    }, 6000);

    try {
        const payload = {
            telefono: hold.phone || document.getElementById('cart-cust-phone')?.value || '',
            codigoReserva: hold.code || ''
        };
        const checkout = await apiPost(`/pagos/wompi/retomar/${encodeURIComponent(hold.id)}`, payload);
        clearTimeout(timeoutWarningTimer);

        if (typeof WidgetCheckout === 'undefined') throw new Error('El widget de Wompi no pudo cargarse en el navegador.');

        const expiration = checkout.fechaExpiracionPago || hold.expiration;
        setPendingHold({ ...hold, expiration: expiration });

        showToast('Abriendo pasarela de pago...', 'info');

        new WidgetCheckout({
            currency: checkout.moneda,
            amountInCents: checkout.montoCentavos,
            reference: checkout.referencia,
            publicKey: checkout.llavePublica,
            signature: { integrity: checkout.firmaIntegridad },
            redirectUrl: checkout.urlRedireccion
        }).open(async (result) => {
            const status = result?.transaction?.status || 'PENDING';
            if (status === 'APPROVED') {
                clearPendingHold();
                resetCartState();
                showToast("Pago aprobado. Pedido confirmado.", "success");
                showSuccessConfirmation(mapFromApiReserva(hold), 'APPROVED');
                await renderTimeSlots();
            } else if (status === 'DECLINED') {
                clearPendingHold();
                syncCartState();
                showToast("El pago no fue aprobado. Puedes intentarlo nuevamente.", "error");
                showSuccessConfirmation(mapFromApiReserva(hold), 'DECLINED');
                await renderTimeSlots();
            } else {
                // Cancelado o cerrado sin finalizar:
                setPendingHold({ ...hold, expiration: expiration });
                syncCartState();
                openCartDrawer();
                showToast("Pago pendiente. Puedes retomar el pago mientras tu reserva temporal siga vigente.", "warning");
                await renderTimeSlots();
            }
        });

        closePaymentModal();
        closeCartDrawer();
    } catch (err) {
        clearTimeout(timeoutWarningTimer);
        console.error(err);
        if (err.status === 409 || (err.message && (err.message.includes('disponible') || err.message.includes('stock') || err.message.includes('agotase') || err.message.includes('unidades')))) {
            showToast(err.message || 'El producto ya no está disponible.', 'error');
            await refreshCatalogStock();
        } else if (err.status === 400 && (err.message && err.message.includes('expirado') || err.error === 'RESERVA_EXPIRADA')) {
            clearPendingHold();
            showToast('El tiempo de reserva de este horario terminó. Selecciona otro horario disponible.', 'warning');
            await renderTimeSlots();
        } else if (err.status === 403 || err.error === 'NO_AUTORIZADO') {
            showToast('No fue posible validar la titularidad de la reserva. Por favor verifica tus datos.', 'error');
        } else if (err.name === 'AbortError' || err.status === 504 || err.status === 503) {
            showToast('No pudimos completar la conexión con la pasarela. Puedes reintentar sin perder tu reserva.', 'warning');
        } else {
            showToast(err.message || 'No se pudo retomar el pago Wompi.', 'error');
        }
    } finally {
        clearTimeout(timeoutWarningTimer);
        isSavingReservation = false;
    }
}



function resumePaymentFromLookup() {
    if (!currentLookupBooking) return;
    closeLookupModal();
    resumeWompiPayment(currentLookupBooking);
}

// ---------- Helpers de Precios Centralizados (COP) ----------
function parsePrice(input) {
    if (input === null || input === undefined) return NaN;
    if (typeof input === 'number') {
        if (isNaN(input) || !isFinite(input) || input < 0) return NaN;
        if (!Number.isInteger(input)) return NaN;
        return input;
    }
    let str = String(input).trim();
    if (!str) return NaN;

    str = str.replace(/[$]/g, '').replace(/\bCOP\b/gi, '').trim();

    if (str.endsWith(',00')) {
        str = str.slice(0, -3).trim();
    } else if (str.endsWith('.00') && !/^\d{1,3}(\.\d{3})+$/.test(str)) {
        str = str.slice(0, -3).trim();
    }

    if (str.includes(',')) {
        return NaN;
    }

    if (/^\d{1,3}(\.\d{3})+$/.test(str)) {
        const clean = str.replace(/\./g, '');
        const val = parseInt(clean, 10);
        return isNaN(val) || val < 0 ? NaN : val;
    }

    if (/^\d+$/.test(str)) {
        const val = parseInt(str, 10);
        return isNaN(val) || val < 0 ? NaN : val;
    }

    return NaN;
}

function formatPrice(value) {
    const num = Number(value);
    if (isNaN(num) || num < 0) return '$0';
    return `$${Math.round(num).toLocaleString('es-CO')}`;
}

function formatPriceNumber(value) {
    const num = Number(value);
    if (isNaN(num) || num < 0) return '0';
    return Math.round(num).toLocaleString('es-CO');
}

function attachPriceInputFormatter(inputId) {
    const input = document.getElementById(inputId);
    if (!input) return;
    input.addEventListener('blur', () => {
        const val = input.value.trim();
        if (val) {
            const parsed = parsePrice(val);
            if (!isNaN(parsed) && parsed >= 0) {
                input.value = formatPriceNumber(parsed);
            }
        }
    });
}

window.parsePrice = parsePrice;
window.formatPrice = formatPrice;
window.formatPriceNumber = formatPriceNumber;

// Adaptador: el backend usa nombre/precio/descripcion/imagenUrl/enStock
// El frontend usa name/price/desc/img/inStock (misma forma que la version original)
function mapFromApiProducto(p) {
    return {
        id: String(p.id),
        type: 'product',
        name: p.nombre || 'Producto sin nombre',
        price: Number(p.precio) || 0,
        desc: p.descripcion || '',
        img: p.imagenUrl || '',
        inStock: p.enStock !== false,
        quantity: Number.isInteger(p.cantidad) ? p.cantidad : (p.enStock !== false ? 1 : 0),
        category: p.categoriaId ? String(p.categoriaId) : '',
        priceType: p.tipoPrecio || 'UNICO',
        temporalmenteReservado: p.temporalmenteReservado === true,
        variants: Array.isArray(p.variantes) ? p.variantes.map(v => ({
            id: String(v.id),
            nombre: v.nombre || 'Variante',
            precio: Number(v.precio) || 0,
            cantidad: Number.isInteger(v.cantidad) ? v.cantidad : 0,
            enStock: v.enStock !== false,
            activo: v.activo !== false,
            temporalmenteReservado: v.temporalmenteReservado === true
        })) : []
    };
}
function mapFromApiKit(k) {
    return {
        id: String(k.id),
        type: 'kit',
        name: k.nombre || 'Kit sin nombre',
        price: Number(k.precio) || 0,
        desc: k.descripcion || '',
        img: k.imagenUrl || '',
        inStock: k.enStock !== false,
        quantity: Number.isInteger(k.cantidad) ? k.cantidad : (k.enStock !== false ? 1 : 0),
        category: k.categoriaId ? String(k.categoriaId) : '',
        temporalmenteReservado: k.temporalmenteReservado === true
    };
}

function mapFromApiServicio(s) {
    return { id: String(s.id), category: s.categoria || 'Sin categoría', name: s.nombre || 'Servicio sin nombre', duration: s.duracion || '60 min', price: Number(s.precio) || 0, desc: s.descripcion || '', img: s.imagenUrl || '' };
}
function mapFromApiCategoriaServicio(c) {
    return { id: String(c.id), name: c.nombre || 'Categoría sin nombre' };
}
function mapFromApiCategoriaProducto(c) {
    return { id: String(c.id), name: c.nombre || 'Categoría sin nombre', activo: c.activo !== false };
}
function mapToApiProducto(item) {
    return {
        nombre: item.name,
        precio: item.price,
        descripcion: item.desc,
        imagenUrl: item.img,
        enStock: item.inStock,
        cantidad: item.quantity,
        categoriaId: item.category || null,
        tipoPrecio: item.priceType || 'UNICO',
        variantes: item.variants || []
    };
}
function mapToApiServicio(item) {
    return { nombre: item.name, categoria: item.category, precio: item.price, duracion: item.duration, descripcion: item.desc, imagenUrl: item.img };
}
function mapFromApiReserva(b) {
    const hasDate = Boolean(b.fechaCita || b.date);
    const hasTime = Boolean(b.horaCita || b.time);
    const hasAppointment = hasDate && hasTime;
    const inv = b.itemsInventario || [];
    const hasServiceItem = inv.length > 0 ? inv.some(i => i.tipo === 'servicio') : (hasAppointment);
    const hasProductItem = inv.length > 0 ? inv.some(i => i.tipo === 'producto' || i.tipo === 'kit') : (!hasAppointment || Boolean(b.tipoEntrega || b.deliveryMethod));

    const isPureAppt = hasServiceItem && !hasProductItem;
    const isPureOrd = !hasServiceItem && hasProductItem;
    const isMix = hasServiceItem && hasProductItem;

    return {
        id: String(b.id),
        code: b.codigoReserva || b.code || '',
        customerName: b.nombreCliente || b.customerName || '',
        phone: b.telefono || b.phone || '',
        email: b.email || '',
        city: b.ciudad || b.city || '',
        services: b.items || b.services || [],
        itemsInventario: inv,
        date: b.fechaCita || b.date || '',
        time: b.horaCita || b.time || '',
        subtotal: Number(b.subtotal) || 0,
        deposit: Number(b.anticipo) || Number(b.deposit) || 0,
        balance: Number(b.saldo) || Math.max(0, (Number(b.subtotal) || 0) - (Number(b.anticipo) || Number(b.deposit) || 0)),
        paymentMethod: b.medioPago || b.paymentMethod || '',
        medioPago: b.medioPago || b.paymentMethod || '',
        wompiReference: b.referenciaWompi || b.wompiReference || '',
        referenciaWompi: b.referenciaWompi || b.wompiReference || '',
        paymentStatus: b.estadoPago || b.paymentStatus || '',
        estadoPago: b.estadoPago || b.paymentStatus || '',
        wompiTransactionId: b.transaccionWompiId || b.wompiTransactionId || '',
        transaccionWompiId: b.transaccionWompiId || b.wompiTransactionId || '',
        wompiPaymentMethod: b.metodoPagoWompi || b.wompiPaymentMethod || '',
        metodoPagoWompi: b.metodoPagoWompi || b.wompiPaymentMethod || '',
        paymentDate: b.fechaPago || b.paymentDate || '',
        fechaPago: b.fechaPago || b.paymentDate || '',
        fechaExpiracionPago: b.fechaExpiracionPago || b.paymentExpiration || null,
        paymentExpiration: b.fechaExpiracionPago || b.paymentExpiration || null,
        deliveryMethod: b.tipoEntrega || b.deliveryMethod || 'pickup',
        tipoEntrega: b.tipoEntrega || b.deliveryMethod || 'pickup',
        deliveryAddress: b.direccionEntrega || b.deliveryAddress || '',
        direccionEntrega: b.direccionEntrega || b.deliveryAddress || '',
        isPureOrder: isPureOrd,
        isPureAppointment: isPureAppt,
        isMixed: isMix,
        hasAppointment: hasAppointment,
        emailConfirmacionEnviada: b.emailConfirmacionEnviada === true,
        fechaEnvioConfirmacion: b.fechaEnvioConfirmacion || null,
        emailErrorEnvio: b.emailErrorEnvio || null,
        status: b.estado || b.status || 'Pendiente Comprobante',
        estado: b.estado || b.status || 'Pendiente Comprobante',
        fechaCancelacion: b.fechaCancelacion || null,
        fechaSolicitudCancelacion: b.fechaSolicitudCancelacion || null,
        canceladaPor: b.canceladaPor || '',
        motivoCancelacion: b.motivoCancelacion || '',
        motivoRechazoCancelacion: b.motivoRechazoCancelacion || '',
        estadoPrevioCancelacion: b.estadoPrevioCancelacion || '',
        notificacionCancelacionVista: b.notificacionCancelacionVista === true,
        archived: b.archivada === true || b.archived === true,
        registeredAt: b.fechaRegistro || b.registeredAt || '',
        archivedAt: b.fechaArchivado || b.archivedAt || '',
        estadoPedido: b.estadoPedido || ''
    };
}

// Clasificador visual exhaustivo de métodos de pago y estados administrativos
function getPaymentMethodInfo(b) {
    if (!b) {
        return {
            isWompi: false,
            isApproved: false,
            badgeHtml: '<span class="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-stone-900 text-stone-300 border border-stone-700"><i class="fa-solid fa-building-columns"></i> 🏦 Transferencia</span>',
            statusBadgeHtml: '<span class="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-amber-950 text-amber-300 border border-amber-500/30">PENDIENTE</span>',
            actionRequired: false,
            actionLabel: '',
            methodTitle: 'Transferencia Bancaria',
            methodSubtitle: 'Validación manual',
            detailBoxHtml: ''
        };
    }

    const medioPago = String(b.medioPago || b.paymentMethod || '').trim().toUpperCase();
    const wompiRef = String(b.referenciaWompi || b.wompiReference || '').trim();
    const wompiTx = String(b.transaccionWompiId || b.wompiTransactionId || '').trim();
    const isWompi = medioPago === 'WOMPI' || Boolean(wompiRef) || Boolean(wompiTx);

    const estadoReserva = String(b.estado || b.status || 'Pendiente Comprobante').trim();
    const estadoPago = String(b.estadoPago || b.paymentStatus || '').trim().toUpperCase();

    if (isWompi) {
        if (estadoPago === 'APROBADO' || estadoReserva === 'Confirmado') {
            return {
                isWompi: true,
                isApproved: true,
                badgeHtml: `<span class="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-emerald-950/90 text-emerald-300 border border-emerald-500/40 shadow-sm" title="Pago automático confirmado por pasarela Wompi"><i class="fa-solid fa-credit-card text-emerald-400"></i> 💳 Wompi · Automático</span>`,
                statusBadgeHtml: `<span class="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-emerald-950 text-emerald-300 border border-emerald-500/30"><i class="fa-solid fa-check"></i> PAGADO</span>`,
                actionRequired: false,
                actionLabel: '✓ Pago automático verificado',
                methodTitle: '💳 Wompi (Pasarela Sandbox)',
                methodSubtitle: 'Pago automático verificado',
                detailBoxHtml: `
                    <div class="p-3.5 rounded-2xl bg-emerald-950/40 border border-emerald-500/30 space-y-2">
                        <div class="flex items-center justify-between">
                            <span class="text-xs font-bold text-white flex items-center gap-1.5"><i class="fa-solid fa-credit-card text-emerald-400"></i> Método: Wompi (Automático)</span>
                            <span class="px-2 py-0.5 rounded-full text-[10px] font-bold bg-emerald-900 text-emerald-200 border border-emerald-500/40">✓ APROBADO</span>
                        </div>
                        <p class="text-[11px] text-emerald-300/80">Esta reserva fue pagada y confirmada automáticamente mediante la pasarela Wompi. <strong>No requiere aprobación manual del administrador.</strong></p>
                        ${wompiRef ? `<div class="text-[10px] text-stone-300 font-mono pt-1 border-t border-emerald-900/50">Referencia: <span class="text-white">${wompiRef}</span> ${wompiTx ? `· Tx ID: <span class="text-white">${wompiTx}</span>` : ''}</div>` : ''}
                    </div>`
            };
        } else if (estadoPago === 'RECHAZADO' || estadoReserva === 'Denegada') {
            return {
                isWompi: true,
                isApproved: false,
                badgeHtml: `<span class="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-red-950/90 text-red-300 border border-red-500/40" title="Pago rechazado por pasarela Wompi"><i class="fa-solid fa-credit-card text-red-400"></i> 💳 Wompi · Rechazado</span>`,
                statusBadgeHtml: `<span class="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-red-950 text-red-300 border border-red-500/30"><i class="fa-solid fa-xmark"></i> RECHAZADO</span>`,
                actionRequired: false,
                actionLabel: 'Pago rechazado por pasarela',
                methodTitle: '💳 Wompi (Pasarela Sandbox)',
                methodSubtitle: 'Pago rechazado',
                detailBoxHtml: `
                    <div class="p-3.5 rounded-2xl bg-red-950/40 border border-red-500/30 space-y-2">
                        <div class="flex items-center justify-between">
                            <span class="text-xs font-bold text-white flex items-center gap-1.5"><i class="fa-solid fa-credit-card text-red-400"></i> Método: Wompi (Rechazado)</span>
                            <span class="px-2 py-0.5 rounded-full text-[10px] font-bold bg-red-900 text-red-200 border border-red-500/40">✕ RECHAZADO</span>
                        </div>
                        <p class="text-[11px] text-red-300/80">El pago fue rechazado por la pasarela. El inventario reservado fue liberado automáticamente.</p>
                        ${wompiRef ? `<div class="text-[10px] text-stone-300 font-mono pt-1 border-t border-red-900/50">Referencia: <span class="text-white">${wompiRef}</span></div>` : ''}
                    </div>`
            };
        } else if (estadoReserva === 'Expirada' || (estadoReserva === 'Pendiente Pago' && b.fechaExpiracionPago && new Date(b.fechaExpiracionPago).getTime() <= Date.now() && estadoPago !== 'APROBADO')) {
            return {
                isWompi: true,
                isApproved: false,
                badgeHtml: `<span class="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-stone-900 text-stone-400 border border-stone-700"><i class="fa-solid fa-clock-rotate-left text-stone-400"></i> 💳 Wompi · Expirada</span>`,
                statusBadgeHtml: `<span class="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-stone-900 text-stone-400 border border-stone-700"><i class="fa-solid fa-clock-rotate-left"></i> EXPIRADA</span>`,
                actionRequired: false,
                actionLabel: 'Tiempo agotado',
                methodTitle: '💳 Wompi (Pasarela Sandbox)',
                methodSubtitle: 'Retención de horario expirada',
                detailBoxHtml: `
                    <div class="p-3.5 rounded-2xl bg-stone-900 border border-stone-800 space-y-2">
                        <div class="flex items-center justify-between">
                            <span class="text-xs font-bold text-stone-300 flex items-center gap-1.5"><i class="fa-solid fa-clock-rotate-left text-stone-400"></i> Método: Wompi (Expirada)</span>
                            <span class="px-2 py-0.5 rounded-full text-[10px] font-bold bg-stone-800 text-stone-300 border border-stone-700">⏱ EXPIRADA</span>
                        </div>
                        <p class="text-[11px] text-stone-400">El tiempo de retención temporal para completar el pago expiró. El horario fue liberado automáticamente para otros clientes.</p>
                        ${wompiRef ? `<div class="text-[10px] text-stone-400 font-mono pt-1 border-t border-stone-800">Referencia: <span class="text-white">${wompiRef}</span></div>` : ''}
                    </div>`
            };
        } else {
            // Wompi Pendiente con retención activa
            const expTime = b.fechaExpiracionPago ? new Date(b.fechaExpiracionPago).toLocaleTimeString('es-CO', { hour: '2-digit', minute: '2-digit', hour12: true, timeZone: 'America/Bogota' }) : '';
            const subTitleText = expTime ? `Retenido temporalmente hasta ${expTime}` : 'En proceso de pago';
            return {
                isWompi: true,
                isApproved: false,
                badgeHtml: `<span class="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-sky-950/90 text-sky-300 border border-sky-500/40" title="Pago en proceso en pasarela Wompi. ${subTitleText}"><i class="fa-solid fa-credit-card text-sky-400"></i> 💳 Wompi · Pendiente</span>`,
                statusBadgeHtml: `<span class="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-sky-950 text-sky-300 border border-sky-500/30"><i class="fa-solid fa-clock"></i> EN PROCESO</span>`,
                actionRequired: false,
                actionLabel: expTime ? `Retenido hasta ${expTime}` : 'Esperando pasarela Wompi',
                methodTitle: '💳 Wompi (Pasarela Sandbox)',
                methodSubtitle: subTitleText,
                detailBoxHtml: `
                    <div class="p-3.5 rounded-2xl bg-sky-950/40 border border-sky-500/30 space-y-2">
                        <div class="flex items-center justify-between">
                            <span class="text-xs font-bold text-white flex items-center gap-1.5"><i class="fa-solid fa-credit-card text-sky-400"></i> Método: Wompi (Pendiente)</span>
                            <span class="px-2 py-0.5 rounded-full text-[10px] font-bold bg-sky-900 text-sky-200 border border-sky-500/40">⏳ EN PROCESO</span>
                        </div>
                        <p class="text-[11px] text-sky-300/80">El cliente inició el pago en Wompi. ${expTime ? `El horario se encuentra <strong>retenido temporalmente hasta las ${expTime}</strong>.` : 'El sistema actualizará el estado automáticamente cuando Wompi envíe la confirmación.'}</p>
                        ${wompiRef ? `<div class="text-[10px] text-stone-300 font-mono pt-1 border-t border-sky-900/50">Referencia: <span class="text-white">${wompiRef}</span></div>` : ''}
                    </div>`
            };
        }
    }
 else {
        // Transferencia
        if (estadoReserva === 'Confirmado' || estadoPago === 'APROBADO') {
            return {
                isWompi: false,
                isApproved: true,
                badgeHtml: `<span class="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-stone-900 text-isivi-gold border border-isivi-gold/40" title="Comprobante de transferencia bancaria validado"><i class="fa-solid fa-building-columns text-isivi-gold"></i> 🏦 Transferencia · Validada</span>`,
                statusBadgeHtml: `<span class="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-emerald-950 text-emerald-300 border border-emerald-500/30"><i class="fa-solid fa-check-double"></i> CONFIRMADA</span>`,
                actionRequired: false,
                actionLabel: '✓ Comprobante validado',
                methodTitle: '🏦 Transferencia Bancaria',
                methodSubtitle: 'Validada manualmente',
                detailBoxHtml: `
                    <div class="p-3.5 rounded-2xl bg-stone-900/90 border border-isivi-500/30 space-y-2">
                        <div class="flex items-center justify-between">
                            <span class="text-xs font-bold text-white flex items-center gap-1.5"><i class="fa-solid fa-building-columns text-isivi-gold"></i> Método: Transferencia (Comprobante)</span>
                            <span class="px-2 py-0.5 rounded-full text-[10px] font-bold bg-emerald-900 text-emerald-200 border border-emerald-500/40">✓ VALIDADA</span>
                        </div>
                        <p class="text-[11px] text-isivi-300">El comprobante de transferencia fue validado y aprobado manualmente por el administrador.</p>
                    </div>`
            };
        } else if (estadoReserva === 'Denegada' || estadoPago === 'RECHAZADO') {
            return {
                isWompi: false,
                isApproved: false,
                badgeHtml: `<span class="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-red-950/90 text-red-300 border border-red-500/40" title="Comprobante denegado"><i class="fa-solid fa-building-columns text-red-400"></i> 🏦 Transferencia · Rechazado</span>`,
                statusBadgeHtml: `<span class="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-red-950 text-red-300 border border-red-500/30"><i class="fa-solid fa-xmark"></i> DENEGADA</span>`,
                actionRequired: false,
                actionLabel: 'Comprobante denegado',
                methodTitle: '🏦 Transferencia Bancaria',
                methodSubtitle: 'Comprobante denegado',
                detailBoxHtml: `
                    <div class="p-3.5 rounded-2xl bg-red-950/40 border border-red-500/30 space-y-2">
                        <div class="flex items-center justify-between">
                            <span class="text-xs font-bold text-white flex items-center gap-1.5"><i class="fa-solid fa-building-columns text-red-400"></i> Método: Transferencia (Comprobante)</span>
                            <span class="px-2 py-0.5 rounded-full text-[10px] font-bold bg-red-900 text-red-200 border border-red-500/40">✕ DENEGADA</span>
                        </div>
                        <p class="text-[11px] text-red-300/80">Esta solicitud fue denegada por el administrador.</p>
                    </div>`
            };
        } else if (estadoReserva === 'Solicitud Cancelación') {
            return {
                isWompi: false,
                isApproved: false,
                badgeHtml: `<span class="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-amber-950/90 text-amber-300 border border-amber-500/50 shadow-sm"><i class="fa-solid fa-calendar-xmark text-amber-400"></i> Solicitud de Cancelación</span>`,
                statusBadgeHtml: `<span class="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-amber-950 text-amber-300 border border-amber-500/40"><i class="fa-solid fa-clock-rotate-left"></i> EN REVISIÓN</span>`,
                actionRequired: true,
                actionLabel: '⚠ En revisión',
                methodTitle: 'Solicitud de Cancelación',
                methodSubtitle: 'En revisión por administración',
                detailBoxHtml: `
                    <div class="p-3.5 rounded-2xl bg-amber-950/50 border border-amber-500/40 space-y-2">
                        <span class="text-xs font-bold text-amber-300 flex items-center gap-1.5"><i class="fa-solid fa-clock-rotate-left"></i> Solicitud de Cancelación</span>
                        <p class="text-[11px] text-amber-200">El cliente solicitó cancelar su cita y está pendiente de evaluación.</p>
                    </div>`
            };
        } else if (estadoReserva === 'Cancelada') {
            return {
                isWompi: false,
                isApproved: false,
                badgeHtml: `<span class="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-stone-900 text-stone-400 border border-stone-700"><i class="fa-solid fa-ban text-stone-400"></i> Cancelada</span>`,
                statusBadgeHtml: `<span class="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-stone-900 text-stone-400 border border-stone-700"><i class="fa-solid fa-ban"></i> CANCELADA</span>`,
                actionRequired: false,
                actionLabel: 'Cancelada',
                methodTitle: 'Cita Cancelada',
                methodSubtitle: 'Cancelada',
                detailBoxHtml: `
                    <div class="p-3.5 rounded-2xl bg-stone-900 border border-stone-800 space-y-2">
                        <span class="text-xs font-bold text-stone-300 flex items-center gap-1.5"><i class="fa-solid fa-ban"></i> Cancelada</span>
                        <p class="text-[11px] text-stone-400">La cita o pedido fue cancelado.</p>
                    </div>`
            };
        } else {
            // Transferencia Pendiente Comprobante
            return {
                isWompi: false,
                isApproved: false,
                badgeHtml: `<span class="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-amber-950/90 text-amber-300 border border-amber-500/50 shadow-sm animate-pulse" title="Requiere validación manual del comprobante"><i class="fa-solid fa-building-columns text-amber-400"></i> 🏦 Transferencia · Requiere validación</span>`,
                statusBadgeHtml: `<span class="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-amber-950 text-amber-300 border border-amber-500/40"><i class="fa-solid fa-hourglass-half text-amber-400"></i> ⏳ PENDIENTE COMPROBANTE</span>`,
                actionRequired: true,
                actionLabel: '⚠ Requiere validación',
                methodTitle: '🏦 Transferencia Bancaria',
                methodSubtitle: 'Requiere validación manual',
                detailBoxHtml: `
                    <div class="p-3.5 rounded-2xl bg-amber-950/50 border border-amber-500/40 space-y-2">
                        <div class="flex items-center justify-between">
                            <span class="text-xs font-bold text-amber-300 flex items-center gap-1.5"><i class="fa-solid fa-building-columns text-amber-400"></i> Método: Transferencia Bancaria</span>
                            <span class="px-2 py-0.5 rounded-full text-[10px] font-bold bg-amber-900 text-amber-200 border border-amber-500/40">⏳ PENDIENTE COMPROBANTE</span>
                        </div>
                        <p class="text-[11px] text-amber-200/90 flex items-center gap-1.5 font-medium"><i class="fa-solid fa-triangle-exclamation text-amber-400"></i> <strong>Requiere validación:</strong> Revisa el comprobante enviado por WhatsApp para aprobar o denegar la cita.</p>
                    </div>`
            };
        }
    }
}

async function loadHistoryBookings() {
    try {
        historyBookingsList = (await apiGet('/reservas?historial=true')).map(mapFromApiReserva);
        renderAdminHistory();
    } catch (err) {
        console.error(err);
        showToast('No se pudo cargar el historial', 'error');
    }
}
function mapFromApiBanner(b) {
    return { id: String(b.id), title: b.titulo || 'Promoción ISIVI', description: b.descripcion || '', image: b.imagenUrl || '', buttonText: b.textoBoton || 'Conocer más', buttonLink: b.enlaceBoton || '#servicios', active: b.activo !== false };
}

// ---------- Estados de Carga y Resiliencia ----------
function renderServicesLoadingState() {
    const container = document.getElementById('services-grid');
    if (!container) return;
    container.innerHTML = `
        <div class="min-w-[280px] sm:min-w-[300px] snap-start bg-stone-950/70 rounded-2xl p-5 border border-stone-850 animate-pulse flex flex-col justify-between h-80">
            <div class="h-44 bg-stone-900 rounded-xl mb-4"></div>
            <div class="h-4 bg-stone-800 rounded w-3/4 mb-2"></div>
            <div class="h-3 bg-stone-900 rounded w-1/2"></div>
        </div>
        <div class="min-w-[280px] sm:min-w-[300px] snap-start bg-stone-950/70 rounded-2xl p-5 border border-stone-850 animate-pulse flex flex-col justify-between h-80">
            <div class="h-44 bg-stone-900 rounded-xl mb-4"></div>
            <div class="h-4 bg-stone-800 rounded w-3/4 mb-2"></div>
            <div class="h-3 bg-stone-900 rounded w-1/2"></div>
        </div>
    `;
}

function renderProductsLoadingState() {
    const container = document.getElementById('products-grid');
    if (!container) return;
    container.innerHTML = `
        <div class="min-w-[260px] sm:min-w-[250px] snap-start bg-stone-950/70 rounded-2xl p-4 border border-stone-850 animate-pulse flex flex-col justify-between h-72">
            <div class="h-36 bg-stone-900 rounded-xl mb-3"></div>
            <div class="h-4 bg-stone-800 rounded w-3/4 mb-2"></div>
            <div class="h-3 bg-stone-900 rounded w-1/2"></div>
        </div>
        <div class="min-w-[260px] sm:min-w-[250px] snap-start bg-stone-950/70 rounded-2xl p-4 border border-stone-850 animate-pulse flex flex-col justify-between h-72">
            <div class="h-36 bg-stone-900 rounded-xl mb-3"></div>
            <div class="h-4 bg-stone-800 rounded w-3/4 mb-2"></div>
            <div class="h-3 bg-stone-900 rounded w-1/2"></div>
        </div>
    `;
}

function renderKitsLoadingState() {
    const container = document.getElementById('kits-grid');
    if (!container) return;
    container.innerHTML = `
        <div class="min-w-[270px] sm:min-w-[280px] snap-start bg-stone-950/70 rounded-2xl p-5 border border-stone-850 animate-pulse flex flex-col justify-between h-72">
            <div class="h-36 bg-stone-900 rounded-xl mb-3"></div>
            <div class="h-4 bg-stone-800 rounded w-3/4 mb-2"></div>
            <div class="h-3 bg-stone-900 rounded w-1/2"></div>
        </div>
    `;
}

function renderServicesErrorState() {
    const container = document.getElementById('services-grid');
    if (!container) return;
    container.innerHTML = `
        <div class="w-full py-8 text-center bg-stone-950/80 rounded-2xl border border-stone-800 p-6">
            <i class="fa-solid fa-cloud-arrow-down text-2xl text-isivi-gold mb-2 block"></i>
            <p class="text-xs text-isivi-200 mb-1 font-semibold">No se pudieron cargar los servicios</p>
            <p class="text-[11px] text-isivi-300 mb-3">Revisa tu conexión de datos móviles e intenta de nuevo.</p>
            <button onclick="retryLoadServices()" class="px-4 py-2 rounded-xl bg-isivi-gold text-isivi-black font-bold text-xs hover:bg-yellow-500 transition inline-flex items-center gap-1.5 shadow">
                <i class="fa-solid fa-arrow-rotate-right"></i> Reintentar
            </button>
        </div>
    `;
}

function renderProductsErrorState() {
    const container = document.getElementById('products-grid');
    if (!container) return;
    container.innerHTML = `
        <div class="w-full py-8 text-center bg-stone-950/80 rounded-2xl border border-stone-800 p-6">
            <i class="fa-solid fa-cloud-arrow-down text-2xl text-isivi-gold mb-2 block"></i>
            <p class="text-xs text-isivi-200 mb-1 font-semibold">No se pudieron cargar los productos</p>
            <p class="text-[11px] text-isivi-300 mb-3">Revisa tu conexión de datos móviles e intenta de nuevo.</p>
            <button onclick="retryLoadProducts()" class="px-4 py-2 rounded-xl bg-isivi-gold text-isivi-black font-bold text-xs hover:bg-yellow-500 transition inline-flex items-center gap-1.5 shadow">
                <i class="fa-solid fa-arrow-rotate-right"></i> Reintentar
            </button>
        </div>
    `;
}

function renderKitsErrorState() {
    const container = document.getElementById('kits-grid');
    if (!container) return;
    container.innerHTML = `
        <div class="w-full py-8 text-center bg-stone-950/80 rounded-2xl border border-stone-800 p-6">
            <i class="fa-solid fa-cloud-arrow-down text-2xl text-isivi-gold mb-2 block"></i>
            <p class="text-xs text-isivi-200 mb-1 font-semibold">No se pudieron cargar los kits especiales</p>
            <p class="text-[11px] text-isivi-300 mb-3">Revisa tu conexión de datos móviles e intenta de nuevo.</p>
            <button onclick="retryLoadKits()" class="px-4 py-2 rounded-xl bg-isivi-gold text-isivi-black font-bold text-xs hover:bg-yellow-500 transition inline-flex items-center gap-1.5 shadow">
                <i class="fa-solid fa-arrow-rotate-right"></i> Reintentar
            </button>
        </div>
    `;
}

async function retryLoadServices() {
    renderServicesLoadingState();
    try {
        const [categorias, servicios] = await Promise.all([
            apiGet('/categorias-servicio').catch(() => []),
            apiGet('/servicios')
        ]);
        if (Array.isArray(categorias) && categorias.length > 0) {
            serviceCategoriesData = categorias.map(mapFromApiCategoriaServicio);
            refreshServiceCategories();
        }
        servicesData = Array.isArray(servicios) ? servicios.map(mapFromApiServicio) : [];
        renderCategoryTabs();
        renderServicesGrid();
        showToast('Servicios cargados con éxito', 'success');
    } catch (err) {
        console.error('Error reintentando servicios:', err);
        renderServicesErrorState();
        showToast('Aún no se pudo conectar. Verifica tu red móvil.', 'error');
    }
}

async function retryLoadProducts() {
    renderProductsLoadingState();
    try {
        const productos = await apiGet('/productos');
        productsData = Array.isArray(productos) ? productos.map(mapFromApiProducto) : [];
        renderProductsGrid();
        showToast('Productos cargados con éxito', 'success');
    } catch (err) {
        console.error('Error reintentando productos:', err);
        renderProductsErrorState();
        showToast('Aún no se pudo conectar. Verifica tu red móvil.', 'error');
    }
}

async function retryLoadKits() {
    renderKitsLoadingState();
    try {
        const kits = await apiGet('/kits');
        kitsData = Array.isArray(kits) ? kits.map(mapFromApiKit) : [];
        renderKitsGrid();
        showToast('Kits cargados con éxito', 'success');
    } catch (err) {
        console.error('Error reintentando kits:', err);
        renderKitsErrorState();
        showToast('Aún no se pudo conectar. Verifica tu red móvil.', 'error');
    }
}

// ---------- Carga Resiliente de datos desde el backend ----------
async function loadPublicData() {
    renderServicesLoadingState();
    renderProductsLoadingState();
    renderKitsLoadingState();

    try {
        const results = await Promise.allSettled([
            apiGet('/productos'),
            apiGet('/kits'),
            apiGet('/servicios'),
            apiGet('/categorias-servicio'),
            apiGet('/categorias-producto?soloActivas=true'),
            apiGet('/agenda'),
            apiGet('/banners')
        ]);

        const [resProductos, resKits, resServicios, resCategorias, resCategoriasProd, resAgenda, resBanners] = results;

        if (resCategorias.status === 'fulfilled' && Array.isArray(resCategorias.value)) {
            serviceCategoriesData = resCategorias.value.map(mapFromApiCategoriaServicio);
            refreshServiceCategories();
        }

        if (resCategoriasProd.status === 'fulfilled' && Array.isArray(resCategoriasProd.value)) {
            productCategoriesData = resCategoriasProd.value.map(mapFromApiCategoriaProducto);
            renderProductCategoryPills();
            renderKitCategoryPills();
        }

        if (resServicios.status === 'fulfilled' && Array.isArray(resServicios.value)) {
            servicesData = resServicios.value.map(mapFromApiServicio);
            renderCategoryTabs();
            renderServicesGrid();
        } else {
            console.warn('Fallo al cargar servicios:', resServicios.reason);
            renderServicesErrorState();
        }

        if (resProductos.status === 'fulfilled' && Array.isArray(resProductos.value)) {
            productsData = resProductos.value.map(mapFromApiProducto);
            renderProductsGrid();
        } else {
            console.warn('Fallo al cargar productos:', resProductos.reason);
            renderProductsErrorState();
        }

        if (resKits.status === 'fulfilled' && Array.isArray(resKits.value)) {
            kitsData = resKits.value.map(mapFromApiKit);
            renderKitsGrid();
        } else {
            console.warn('Fallo al cargar kits:', resKits.reason);
            renderKitsErrorState();
        }

        if (resAgenda.status === 'fulfilled' && resAgenda.value) {
            agendaConfiguration = resAgenda.value;
            appointmentTimeSlots = resAgenda.value.horarios || appointmentTimeSlots;
            ensureSelectedDateIsWorking();
            if (!appointmentTimeSlots.includes(selectedTimeSlot)) {
                selectedTimeSlot = appointmentTimeSlots[0] || '';
            }
            renderCalendar();
            renderTimeSlots();
        } else {
            console.warn('Fallo al cargar agenda:', resAgenda.reason);
            renderCalendar();
            renderTimeSlots();
        }

        if (resBanners.status === 'fulfilled' && Array.isArray(resBanners.value)) {
            bannersData = resBanners.value.map(mapFromApiBanner);
        } else {
            bannersData = [];
        }
        renderClientBanners();
        syncCartState();
    } catch (err) {
        console.error('Error inesperado en loadPublicData:', err);
    }
}

async function loadAdminData() {
    if (!adminAuthToken) return;
    try {
        const [reservasRes, catProdRes] = await Promise.allSettled([
            apiGet('/reservas?tipo=citas'),
            apiGet('/categorias-producto')
        ]);
        if (reservasRes.status === 'fulfilled' && Array.isArray(reservasRes.value)) {
            bookingsList = reservasRes.value.map(mapFromApiReserva).filter(b => b.isPureOrder !== true);
            renderAdminBookingsTable();
        }
        if (catProdRes.status === 'fulfilled' && Array.isArray(catProdRes.value)) {
            productCategoriesData = catProdRes.value.map(mapFromApiCategoriaProducto);
            renderAdminProductCategories();
            populateProductCategorySelect();
            renderProductCategoryPills();
            renderKitCategoryPills();
        }
        renderAdminWeeklyScheduleView();
        renderAdminExceptionsList();
        renderAdminWeeklyCalendar();
        renderAdminScheduleManager();
        await loadHistoryBookings();
    } catch (err) {
        console.warn('Error cargando reservas de administración:', err);
    }
}

async function loadAllData() {
    await loadPublicData();
    if (adminAuthToken) {
        await loadAdminData();
    }
}

document.addEventListener('DOMContentLoaded', async () => {
    try { initGA4IfConfigured(); } catch (e) { console.warn('GA4 init:', e); }
    try { bindValidationListeners(); } catch (e) { console.warn('bindValidationListeners:', e); }
    try { bindCartValidationListeners(); } catch (e) { console.warn('bindCartValidationListeners:', e); }
    try { attachPriceInputFormatter('prod-price'); } catch (e) { console.warn('attachPriceInputFormatter prod-price:', e); }
    try { attachPriceInputFormatter('new-var-price'); } catch (e) { console.warn('attachPriceInputFormatter new-var-price:', e); }
    try { attachPriceInputFormatter('serv-price'); } catch (e) { console.warn('attachPriceInputFormatter serv-price:', e); }

    // 1. Configurar inmediatamente la vista inicial antes de cualquier llamada asíncrona
    try {
        if (window.location.hash === '#admin') {
            if (adminAuthToken) {
                navigateTo('admin');
            } else {
                showAdminLogin();
            }
        } else {
            navigateTo('client', false);
        }
    } catch (e) { console.warn('initial navigation:', e); }
 
    try {
        const initialHold = getPendingHold();
        if (initialHold) restoreCartFromHold(initialHold);
    } catch (e) { console.warn('restoreCartFromHold initial:', e); }

    try { renderCategoryTabs(); } catch (e) { console.warn('renderCategoryTabs:', e); }
    try { renderServicesGrid(); } catch (e) { console.warn('renderServicesGrid:', e); }
    try { renderProductsGrid(); } catch (e) { console.warn('renderProductsGrid:', e); }
    try { renderKitsGrid(); } catch (e) { console.warn('renderKitsGrid:', e); }
    try { renderClientBanners(); } catch (e) { console.warn('renderClientBanners:', e); }
    try { renderCalendar(); } catch (e) { console.warn('renderCalendar:', e); }
    try { renderTimeSlots(); } catch (e) { console.warn('renderTimeSlots:', e); }
    try { syncCartState(); } catch (e) { console.warn('syncCartState:', e); }
    try { await syncPendingHoldWithBackend(); } catch (e) { console.warn('syncPendingHoldWithBackend:', e); }
    try { startPendingHoldTimer(); } catch (e) { console.warn('startPendingHoldTimer:', e); }
    try { renderPendingHoldUI(); } catch (e) { console.warn('renderPendingHoldUI:', e); }
    try { selectPaymentMethod('bancolombia'); } catch (e) { console.warn('selectPaymentMethod:', e); }
    try { updateNavigationOnScroll(); } catch (e) { console.warn('updateNavigationOnScroll:', e); }
    try { initializeScrollReveal(); } catch (e) { console.warn('initializeScrollReveal:', e); }

    window.addEventListener('scroll', updateNavigationOnScroll, { passive: true });
    window.addEventListener('scroll', updateMobileContextBar, { passive: true });
    window.addEventListener('resize', () => {
        if (window.innerWidth >= 768) closeMobileMenu();
        updateMobileContextBar();
    }, { passive: true });

    // 2. Cargar datos públicos del servidor de forma resiliente
    try {
        await loadPublicData();
    } catch (err) {
        console.error('Error al cargar datos públicos:', err);
    }

    try {
        await loadAgendaConfiguration();
    } catch (err) {
        console.warn('Error al cargar configuración de agenda:', err);
    }

    // 3. Si hay sesión activa de administrador y estamos en #admin, cargar datos admin
    if (adminAuthToken && window.location.hash === '#admin') {
        try {
            await loadAdminData();
        } catch (err) {
            console.warn('Error al cargar datos administrativos:', err);
        }
    }

    // 4. Revisar si el usuario regresa de una pasarela Wompi
    try {
        await checkWompiRedirectReturn();
    } catch (err) {
        console.warn('Error en retorno Wompi:', err);
    }

    try { syncCartState(); } catch (e) { console.warn('syncCartState post load:', e); }
    try { renderPendingHoldUI(); } catch (e) { console.warn('renderPendingHoldUI post load:', e); }
    try { trackEvent('page_view', { path: window.location.pathname }, 'initial_page_load'); } catch (e) {}
});

async function checkWompiRedirectReturn() {
    const urlParams = new URLSearchParams(window.location.search);
    const wompiTxId = urlParams.get('id');
    const wompiRef = urlParams.get('reference');

    if (wompiTxId) {
        try {
            const txData = await apiGet(`/pagos/wompi/transaccion/${encodeURIComponent(wompiTxId)}`);
            if (txData && txData.reserva) {
                const booking = mapFromApiReserva(txData.reserva);
                const statusCase = txData.estado === 'APPROVED' ? 'APPROVED' : (txData.estado === 'DECLINED' ? 'DECLINED' : 'PENDING');
                if (statusCase === 'APPROVED') {
                    clearPendingHold();
                    resetCartState();
                } else if (statusCase === 'DECLINED') {
                    clearPendingHold();
                    syncCartState();
                } else {
                    setPendingHold(booking);
                    syncCartState();
                }
                await refreshCatalogStock();
                showSuccessConfirmation(booking, statusCase);
                window.history.replaceState({}, document.title, window.location.pathname);
                return;
            }
        } catch (e) {
            console.warn('Verificación retorno Wompi por ID:', e);
        }
    }

    if (wompiRef) {
        try {
            const pagoInfo = await apiGet(`/pagos/wompi/estado/${encodeURIComponent(wompiRef)}`);
            if (pagoInfo) {
                const matchingBooking = bookingsList.find(b => b.code === pagoInfo.codigoReserva || b.wompiReference === wompiRef) || {
                    code: pagoInfo.codigoReserva,
                    status: pagoInfo.estadoReserva,
                    paymentStatus: pagoInfo.estadoPago,
                    deposit: (pagoInfo.montoCentavos || 0) / 100
                };
                const statusCase = pagoInfo.estadoPago === 'APROBADO' ? 'APPROVED' : (pagoInfo.estadoPago === 'RECHAZADO' || pagoInfo.estadoPago === 'ERROR' ? 'DECLINED' : 'PENDING');
                if (statusCase === 'APPROVED') {
                    clearPendingHold();
                    resetCartState();
                } else if (statusCase === 'DECLINED') {
                    clearPendingHold();
                    syncCartState();
                } else {
                    setPendingHold(matchingBooking);
                    syncCartState();
                }
                await refreshCatalogStock();
                showSuccessConfirmation(matchingBooking, statusCase);
                window.history.replaceState({}, document.title, window.location.pathname);
            }
        } catch (err) {
            console.warn('Verificación retorno Wompi por referencia:', err);
        }
    }
}

// ---------- Router ----------
function initializeScrollReveal() {
    if (window.matchMedia('(prefers-reduced-motion: reduce)').matches) {
        document.querySelectorAll('.reveal-on-scroll').forEach(el => {
            el.classList.add('is-visible');
        });
        return;
    }

    const observerOptions = {
        root: null,
        rootMargin: '0px',
        threshold: 0.08
    };

    const observer = new IntersectionObserver((entries, obs) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.classList.add('is-visible');
                obs.unobserve(entry.target);
            }
        });
    }, observerOptions);

    document.querySelectorAll('.reveal-on-scroll').forEach(el => {
        observer.observe(el);
    });
}

function setActiveNavigation(section) {
    const links = ['inicio', 'servicios', 'productos', 'nosotros'];
    links.forEach(name => {
        const link = document.getElementById(`nav-${name}`);
        if (!link) return;
        const active = name === section;
        link.classList.toggle('active', active);
        link.classList.toggle('border-isivi-gold', active);
        link.classList.toggle('text-isivi-gold', active);
        link.classList.toggle('font-bold', active);
        link.classList.toggle('border-transparent', !active);
        link.classList.toggle('text-isivi-200', !active);
        link.classList.toggle('font-medium', !active);
        link.setAttribute('aria-current', active ? 'page' : 'false');
    });
}

function updateNavigationOnScroll() {
    const navigationSections = [
        { id: 'servicios', nav: 'servicios' },
        { id: 'productos', nav: 'productos' },
        { id: 'nosotros', nav: 'nosotros' }
    ];
    const currentPosition = window.scrollY + 120;
    let active = 'inicio';
    navigationSections.forEach(section => {
        const element = document.getElementById(section.id);
        if (element && !element.classList.contains('hidden') && element.offsetTop <= currentPosition) active = section.nav;
    });
    setActiveNavigation(active);
}

// ============ NAVEGACIÓN CENTRALIZADA & MENÚS ============
function toggleMobileMenu() {
    const menu = document.getElementById('mobile-nav');
    const toggle = document.getElementById('mobile-menu-toggle');
    if (!menu || !toggle) return;
    const isClosed = menu.classList.contains('hidden');
    if (isClosed) {
        menu.classList.remove('hidden');
        toggle.setAttribute('aria-expanded', 'true');
        toggle.setAttribute('aria-label', 'Cerrar menú de navegación');
        toggle.innerHTML = '<i class="fa-solid fa-xmark text-lg"></i>';
    } else {
        closeMobileMenu();
    }
}

function closeMobileMenu() {
    const menu = document.getElementById('mobile-nav');
    const toggle = document.getElementById('mobile-menu-toggle');
    if (!menu || menu.classList.contains('hidden')) return;
    menu.classList.add('hidden');
    if (toggle) {
        toggle.setAttribute('aria-expanded', 'false');
        toggle.setAttribute('aria-label', 'Abrir menú de navegación');
        toggle.innerHTML = '<i class="fa-solid fa-bars text-lg"></i>';
    }
}

function toggleDesktopMenu() {
    const drawer = document.getElementById('desktop-nav-drawer');
    const overlay = document.getElementById('desktop-nav-overlay');
    const toggle = document.getElementById('desktop-menu-toggle');
    if (!drawer || !overlay || !toggle) return;
    const isOpen = drawer.classList.contains('is-open');
    if (isOpen) {
        closeDesktopMenu();
    } else {
        openDesktopMenu();
    }
}

function openDesktopMenu() {
    const drawer = document.getElementById('desktop-nav-drawer');
    const overlay = document.getElementById('desktop-nav-overlay');
    const toggle = document.getElementById('desktop-menu-toggle');
    if (!drawer || !overlay) return;
    drawer.classList.add('is-open');
    overlay.classList.add('is-open');
    if (toggle) {
        toggle.setAttribute('aria-expanded', 'true');
        toggle.setAttribute('aria-label', 'Cerrar menú de navegación');
        toggle.innerHTML = '<i class="fa-solid fa-xmark text-lg"></i>';
    }
}

function closeDesktopMenu() {
    const drawer = document.getElementById('desktop-nav-drawer');
    const overlay = document.getElementById('desktop-nav-overlay');
    const toggle = document.getElementById('desktop-menu-toggle');
    if (!drawer || !overlay) return;
    drawer.classList.remove('is-open');
    overlay.classList.remove('is-open');
    if (toggle) {
        toggle.setAttribute('aria-expanded', 'false');
        toggle.setAttribute('aria-label', 'Abrir menú de navegación');
        toggle.innerHTML = '<i class="fa-solid fa-bars text-lg"></i>';
    }
}

// Global keydown listener for Escape
window.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') {
        closeDesktopMenu();
        closeMobileMenu();
        if (typeof closeCartDrawer === 'function') closeCartDrawer();
        if (typeof closePaymentModal === 'function') closePaymentModal();
        if (typeof closeLookupModal === 'function') closeLookupModal();
        if (typeof closeClientRescheduleModal === 'function') closeClientRescheduleModal();
        if (typeof closeAdminRescheduleModal === 'function') closeAdminRescheduleModal();
        if (typeof closeAdminBookingDetailModal === 'function') closeAdminBookingDetailModal();
        const bannerModal = document.getElementById('banner-modal');
        if (bannerModal && !bannerModal.classList.contains('hidden')) bannerModal.classList.add('hidden');
        const itemModal = document.getElementById('item-modal');
        if (itemModal && !itemModal.classList.contains('hidden')) itemModal.classList.add('hidden');
        document.body.classList.remove('overflow-hidden');
    }
});

function navigateToSection(section, event) {
    if (event && typeof event.preventDefault === 'function') {
        event.preventDefault();
    }
    closeMobileMenu();
    closeDesktopMenu();

    const clientPage = document.getElementById('page-client');
    const adminPage = document.getElementById('page-admin');
    if (clientPage && clientPage.classList.contains('hidden')) {
        adminPage?.classList.add('hidden');
        clientPage.classList.remove('hidden');
    }

    showClientCatalog('all');

    if (section === 'inicio') {
        checkoutMode = 'services';
        setActiveNavigation('inicio');
        window.scrollTo({ top: 0, behavior: 'smooth' });
        return;
    }

    if (section === 'servicios') {
        checkoutMode = 'services';
        setActiveNavigation('servicios');
        updateSummary();
        const el = document.getElementById('servicios');
        if (el) {
            el.classList.remove('hidden');
            setTimeout(() => {
                const headerOffset = 80;
                const elementPosition = el.getBoundingClientRect().top;
                const offsetPosition = elementPosition + window.pageYOffset - headerOffset;
                window.scrollTo({ top: offsetPosition, behavior: 'smooth' });
            }, 20);
        }
        return;
    }

    if (section === 'productos') {
        checkoutMode = 'products';
        setActiveNavigation('productos');
        updateSummary();
        const el = document.getElementById('productos');
        if (el) {
            el.classList.remove('hidden');
            setTimeout(() => {
                const headerOffset = 80;
                const elementPosition = el.getBoundingClientRect().top;
                const offsetPosition = elementPosition + window.pageYOffset - headerOffset;
                window.scrollTo({ top: offsetPosition, behavior: 'smooth' });
            }, 20);
        }
        return;
    }

    if (section === 'kits') {
        setActiveNavigation('productos');
        updateSummary();
        const el = document.getElementById('kits');
        if (el) {
            el.classList.remove('hidden');
            setTimeout(() => {
                const headerOffset = 80;
                const elementPosition = el.getBoundingClientRect().top;
                const offsetPosition = elementPosition + window.pageYOffset - headerOffset;
                window.scrollTo({ top: offsetPosition, behavior: 'smooth' });
            }, 20);
        }
        return;
    }

    if (section === 'nosotros') {
        setActiveNavigation('nosotros');
        const el = document.getElementById('nosotros');
        if (el) {
            el.classList.remove('hidden');
            setTimeout(() => {
                const headerOffset = 80;
                const elementPosition = el.getBoundingClientRect().top;
                const offsetPosition = elementPosition + window.pageYOffset - headerOffset;
                window.scrollTo({ top: offsetPosition, behavior: 'smooth' });
            }, 20);
        }
        return;
    }
}

function handleMobileNavigation(section, event) {
    navigateToSection(section, event);
}

function openHome(event) {
    navigateToSection('inicio', event);
}

function openProductShop(event) {
    navigateToSection('productos', event);
}

function openServicesShop(event) {
    navigateToSection('servicios', event);
}

function openAboutSection(event) {
    navigateToSection('nosotros', event);
}

function showClientCatalog(section) {
    const servicios = document.getElementById('servicios');
    const productos = document.getElementById('productos');
    const kits = document.getElementById('kits');
    if (servicios) servicios.classList.remove('hidden');
    if (productos) productos.classList.remove('hidden');
    if (kits) kits.classList.remove('hidden');
}

function openAppointmentBooking() {
    checkoutMode = 'services';
    selectedProducts = [];
    selectedKits = [];
    productQuantities = {};
    kitQuantities = {};
    productCardQuantities = {};
    kitCardQuantities = {};
    showClientCatalog('services');
    renderServicesGrid();
    updateSummary();
    renderProductsGrid();
    renderKitsGrid();
    setActiveNavigation('servicios');
    setTimeout(() => document.getElementById('servicios').scrollIntoView({ behavior: 'smooth' }), 0);
}

function navigateTo(pageName) {
    const clientPage = document.getElementById('page-client');
    const adminPage = document.getElementById('page-admin');
    const loginPage = document.getElementById('admin-login');

    loginPage.classList.add('hidden');

    if (pageName === 'admin') {
        if (!adminAuthToken) {
            showAdminLogin();
            return;
        }
        clientPage.classList.add('hidden');
        adminPage.classList.remove('hidden');
        window.location.hash = 'admin';
        switchAdminTab('dashboard');
        loadAdminData();
        renderAdminProductsTable();
        renderAdminServicesTable();
        renderAdminBookingsTable();
        renderAdminScheduleManager();
        renderAdminWeeklyCalendar();
        renderAgendaSettings();
        renderAdminUsers();
        renderAdminBanners();
        window.scrollTo(0, 0);
        showToast("Bienvenido al Panel de Administración ISIVI", "info");
    } else {
        adminPage.classList.add('hidden');
        clientPage.classList.remove('hidden');
        if (window.location.hash === '#admin') window.location.hash = '';
        showClientCatalog('all');
        setActiveNavigation('inicio');
        renderServicesGrid();
        renderProductsGrid();
        renderKitsGrid();
        window.scrollTo(0, 0);
    }
}

function requestAdminAccess() {
    if (adminAuthToken) {
        navigateTo('admin');
    } else {
        showAdminLogin();
    }
}

function showAdminLogin() {
    document.getElementById('page-client').classList.add('hidden');
    document.getElementById('page-admin').classList.add('hidden');
    document.getElementById('admin-login').classList.remove('hidden');
    document.getElementById('admin-login-error').classList.add('hidden');
    window.location.hash = 'admin';
    setTimeout(() => document.getElementById('admin-username').focus(), 0);
}

function cancelAdminLogin() {
    navigateTo('client');
}

async function handleAdminLogin(event) {
    event.preventDefault();
    const error = document.getElementById('admin-login-error');
    error.classList.add('hidden');
    try {
        const response = await apiPost('/auth/login', {
            usuario: document.getElementById('admin-username').value,
            contrasena: document.getElementById('admin-password').value
        });
        if (!response.autenticado) {
            error.textContent = 'Usuario o contraseña incorrectos.';
            error.classList.remove('hidden');
            return;
        }
        if (response.token) {
            adminAuthToken = response.token;
            sessionStorage.setItem('isivi_admin_token', response.token);
            sessionStorage.setItem('isivi_admin_user', response.usuario || 'admin');
        }
        document.getElementById('admin-password').value = '';
        navigateTo('admin');
    } catch (err) {
        console.error(err);
        error.textContent = 'No fue posible validar las credenciales.';
        error.classList.remove('hidden');
    }
}

function logoutAdmin() {
    adminAuthToken = null;
    sessionStorage.removeItem('isivi_admin_token');
    sessionStorage.removeItem('isivi_admin_user');
    showToast('Sesión de administración cerrada correctamente', 'info');
    navigateTo('client');
}

// ---------- Categorias / Servicios ----------
function refreshServiceCategories() {
    serviceCategories = [{ id: 'todos', name: 'Todos los Servicios' }, ...serviceCategoriesData];
    if (!serviceCategories.some(category => category.id === activeCategory)) activeCategory = 'todos';
    renderServiceCategorySelect();
    renderAdminServiceCategories();
}

function renderServiceCategorySelect(selectedId) {
    const select = document.getElementById('serv-category');
    if (!select) return;
    const current = selectedId || select.value;
    select.innerHTML = serviceCategoriesData.map(category => `<option value="${category.id}">${category.name}</option>`).join('');
    if (serviceCategoriesData.some(category => category.id === current)) select.value = current;
}

function renderAdminServiceCategories() {
    const container = document.getElementById('admin-service-categories');
    if (!container) return;
    container.innerHTML = serviceCategoriesData.map(category => {
        const count = servicesData.filter(service => normalizeServiceCategory(service.category) === category.id).length;
        return `<span class="inline-flex items-center gap-2 rounded-xl border border-stone-700 bg-stone-950 px-3 py-2 text-xs text-isivi-200"><span>${category.name} <span class="text-isivi-gold">(${count})</span></span><button type="button" onclick="deleteServiceCategory('${category.id}', '${category.name.replace(/'/g, "\\'")}')" class="text-red-300 hover:text-red-200" title="Eliminar categoría"><i class="fa-solid fa-trash"></i></button></span>`;
    }).join('') || '<p class="text-xs text-isivi-300">Aún no hay categorías. Añade una para poder crear servicios.</p>';
}

function categoryIdFromName(name) {
    return name.normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLowerCase().trim()
        .replace(/[^a-z0-9]+/g, '-').replace(/(^-|-$)/g, '');
}

async function handleCategorySubmit(event) {
    event.preventDefault();
    const input = document.getElementById('service-category-name');
    const name = input.value.trim();
    const id = categoryIdFromName(name);
    if (!id) { showToast('Escribe un nombre de categoría válido', 'error'); return; }
    try {
        const created = await apiPost('/categorias-servicio', { id, nombre: name });
        serviceCategoriesData.push(mapFromApiCategoriaServicio(created));
        refreshServiceCategories();
        renderCategoryTabs();
        input.value = '';
        showToast('Categoría añadida y publicada en Servicios');
    } catch (err) {
        console.error(err);
        showToast('No se pudo añadir: ya existe una categoría con ese nombre', 'error');
    }
}

async function deleteServiceCategory(id, name) {
    const count = servicesData.filter(service => normalizeServiceCategory(service.category) === id).length;
    if (count > 0) { showToast(`No puedes eliminar "${name}" porque tiene ${count} servicio(s). Reasígnalos o elimínalos primero.`, 'error'); return; }
    if (!confirm(`¿Eliminar la categoría "${name}"?`)) return;
    try {
        await apiDelete(`/categorias-servicio/${id}`);
        serviceCategoriesData = serviceCategoriesData.filter(category => category.id !== id);
        refreshServiceCategories();
        renderCategoryTabs();
        renderServicesGrid();
        showToast('Categoría eliminada correctamente.', 'success');
    } catch (err) {
        console.error(err);
        showToast(err.message || 'No se pudo eliminar la categoría.', 'error');
    }
}

function renderCategoryTabs() {
    const container = document.getElementById('service-category-tabs');
    if (!container) return;
    container.innerHTML = serviceCategories.map(cat => `
        <button onclick="setCategory('${cat.id}')" class="px-4 py-2 rounded-full text-xs font-semibold transition ${activeCategory === cat.id ? 'bg-isivi-gold text-isivi-black font-bold shadow' : 'bg-stone-900 border border-isivi-500/30 text-isivi-200 hover:border-isivi-gold'}">
            ${cat.name}
        </button>
    `).join('');
}

function setCategory(catId) {
    activeCategory = catId;
    trackEvent('view_service_category', { category: catId });
    renderCategoryTabs();
    renderServicesGrid();
}

function normalizeServiceCategory(category) {
    return String(category || '').trim().toLocaleLowerCase('es-CO');
}

function scrollCatalog(containerId, direction) {
    const container = document.getElementById(containerId);
    if (container) container.scrollBy({ left: direction * Math.max(280, container.clientWidth * 0.75), behavior: 'smooth' });
}

let carouselState = {
    track: null,
    viewport: null,
    slides: [],
    prevBtn: null,
    nextBtn: null,
    indicators: [],
    currentIndex: 1,
    isTransitioning: false,
    autoplayTimer: null,
    resumeTimer: null,
    isDragging: false,
    startX: 0,
    currentTranslate: 0,
    prevTranslate: 0,
    dragged: false,
    dragThreshold: 80,
    autoplayDuration: 5000,
    originalCount: 0,
    isHovered: false,
    dragStartY: 0,
    dragDirectionDetected: false,
    boundHandlePointerDown: null,
    boundHandlePointerMove: null,
    boundHandlePointerUp: null,
    boundHandleMouseEnter: null,
    boundHandleMouseLeave: null,
    boundHandlePrevClick: null,
    boundHandleNextClick: null,
    boundHandleIndicatorClick: null,
    boundHandleKeyDown: null,
    boundHandleVisibilityChange: null,
    boundHandleResize: null
};

function cleanupBannersCarousel() {
    if (carouselState.autoplayTimer) {
        clearTimeout(carouselState.autoplayTimer);
        carouselState.autoplayTimer = null;
    }
    if (carouselState.resumeTimer) {
        clearTimeout(carouselState.resumeTimer);
        carouselState.resumeTimer = null;
    }

    if (carouselState.viewport) {
        if (carouselState.boundHandlePointerDown) {
            carouselState.viewport.removeEventListener('pointerdown', carouselState.boundHandlePointerDown);
        }
        if (carouselState.boundHandlePointerMove) {
            carouselState.viewport.removeEventListener('pointermove', carouselState.boundHandlePointerMove);
        }
        if (carouselState.boundHandlePointerUp) {
            carouselState.viewport.removeEventListener('pointerup', carouselState.boundHandlePointerUp);
            carouselState.viewport.removeEventListener('pointercancel', carouselState.boundHandlePointerUp);
        }
        if (carouselState.boundHandleMouseEnter) {
            carouselState.viewport.removeEventListener('mouseenter', carouselState.boundHandleMouseEnter);
        }
        if (carouselState.boundHandleMouseLeave) {
            carouselState.viewport.removeEventListener('mouseleave', carouselState.boundHandleMouseLeave);
        }
        carouselState.viewport.removeEventListener('click', handleViewportClick, true);
        carouselState.viewport.removeEventListener('keydown', carouselState.boundHandleKeyDown);
    }

    if (carouselState.prevBtn && carouselState.boundHandlePrevClick) {
        carouselState.prevBtn.removeEventListener('click', carouselState.boundHandlePrevClick);
    }
    if (carouselState.nextBtn && carouselState.boundHandleNextClick) {
        carouselState.nextBtn.removeEventListener('click', carouselState.boundHandleNextClick);
    }

    if (carouselState.indicators.length > 0 && carouselState.boundHandleIndicatorClick) {
        carouselState.indicators.forEach(ind => {
            ind.removeEventListener('click', carouselState.boundHandleIndicatorClick);
        });
    }

    if (carouselState.boundHandleVisibilityChange) {
        document.removeEventListener('visibilitychange', carouselState.boundHandleVisibilityChange);
    }
    if (carouselState.boundHandleResize) {
        window.removeEventListener('resize', carouselState.boundHandleResize);
    }
}

function handleViewportClick(e) {
    if (carouselState.dragged) {
        e.preventDefault();
        e.stopPropagation();
        carouselState.dragged = false;
    }
}

function initializeBannersCarousel(N) {
    carouselState.originalCount = N;
    carouselState.currentIndex = 1;
    carouselState.isTransitioning = false;
    carouselState.isHovered = false;
    carouselState.isDragging = false;
    carouselState.dragged = false;

    carouselState.viewport = document.querySelector('#client-banners .carousel-viewport');
    carouselState.track = document.querySelector('#client-banners .carousel-track');
    carouselState.slides = Array.from(document.querySelectorAll('#client-banners .carousel-slide'));
    carouselState.prevBtn = document.querySelector('#client-banners .carousel-control.prev');
    carouselState.nextBtn = document.querySelector('#client-banners .carousel-control.next');
    carouselState.indicators = Array.from(document.querySelectorAll('#client-banners .carousel-indicator'));

    if (!carouselState.viewport || !carouselState.track) return;

    // Define and bind all event handlers
    carouselState.boundHandlePointerDown = (e) => {
        if (carouselState.isTransitioning) return;
        if (e.button !== 0) return; // Only primary button
        carouselState.isDragging = true;
        carouselState.dragged = false;
        carouselState.startX = e.clientX;
        carouselState.dragStartY = e.clientY;
        carouselState.dragDirectionDetected = false;
        stopAutoplay();
        if (carouselState.resumeTimer) {
            clearTimeout(carouselState.resumeTimer);
            carouselState.resumeTimer = null;
        }
        carouselState.viewport.style.cursor = 'grabbing';
        carouselState.track.style.transition = 'none';
    };

    carouselState.boundHandlePointerMove = (e) => {
        if (!carouselState.isDragging) return;
        const deltaX = e.clientX - carouselState.startX;
        const deltaY = e.clientY - carouselState.dragStartY;

        if (!carouselState.dragDirectionDetected) {
            const absX = Math.abs(deltaX);
            const absY = Math.abs(deltaY);
            if (absX > 6 || absY > 6) {
                if (absX > absY) {
                    carouselState.dragDirectionDetected = 'horizontal';
                } else {
                    carouselState.dragDirectionDetected = 'vertical';
                    carouselState.isDragging = false;
                    carouselState.viewport.style.cursor = 'grab';
                    moveToSlide(carouselState.currentIndex, true);
                    return;
                }
            } else {
                return;
            }
        }

        if (carouselState.dragDirectionDetected === 'horizontal') {
            if (e.cancelable) e.preventDefault();
            carouselState.dragged = true;
            const newTranslate = carouselState.prevTranslate + deltaX;
            carouselState.track.style.transform = `translateX(${newTranslate}px)`;
            carouselState.currentTranslate = newTranslate;
        }
    };

    carouselState.boundHandlePointerUp = (e) => {
        if (!carouselState.isDragging) return;
        carouselState.isDragging = false;
        carouselState.viewport.style.cursor = 'grab';
        const deltaX = e.clientX - carouselState.startX;

        if (carouselState.dragged && carouselState.dragDirectionDetected === 'horizontal') {
            const prefersReduced = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
            carouselState.track.style.transition = prefersReduced ? 'none' : 'transform 0.5s cubic-bezier(0.25, 1, 0.5, 1)';
            if (deltaX < -carouselState.dragThreshold) {
                moveToSlide(carouselState.currentIndex + 1);
            } else if (deltaX > carouselState.dragThreshold) {
                moveToSlide(carouselState.currentIndex - 1);
            } else {
                moveToSlide(carouselState.currentIndex);
            }
            resetAutoplayOnInteraction();
        } else {
            moveToSlide(carouselState.currentIndex, false);
            if (!carouselState.isHovered) {
                startAutoplay();
            }
        }
    };

    carouselState.boundHandleMouseEnter = () => {
        carouselState.isHovered = true;
        stopAutoplay();
    };

    carouselState.boundHandleMouseLeave = () => {
        carouselState.isHovered = false;
        if (!carouselState.isDragging) {
            startAutoplay();
        }
    };

    carouselState.boundHandlePrevClick = (e) => {
        e.preventDefault();
        moveToSlide(carouselState.currentIndex - 1);
        resetAutoplayOnInteraction();
    };

    carouselState.boundHandleNextClick = (e) => {
        e.preventDefault();
        moveToSlide(carouselState.currentIndex + 1);
        resetAutoplayOnInteraction();
    };

    carouselState.boundHandleIndicatorClick = (e) => {
        e.preventDefault();
        const targetIdx = parseInt(e.currentTarget.getAttribute('data-slide-index'), 10);
        moveToSlide(targetIdx + 1);
        resetAutoplayOnInteraction();
    };

    carouselState.boundHandleKeyDown = (e) => {
        if (e.key === 'ArrowLeft') {
            e.preventDefault();
            moveToSlide(carouselState.currentIndex - 1);
            resetAutoplayOnInteraction();
        } else if (e.key === 'ArrowRight') {
            e.preventDefault();
            moveToSlide(carouselState.currentIndex + 1);
            resetAutoplayOnInteraction();
        }
    };

    carouselState.boundHandleVisibilityChange = () => {
        if (document.hidden) {
            stopAutoplay();
        } else {
            if (!carouselState.isHovered && !carouselState.isDragging) {
                startAutoplay();
            }
        }
    };

    carouselState.boundHandleResize = () => {
        moveToSlide(carouselState.currentIndex, false);
    };

    // Attach listeners
    carouselState.viewport.addEventListener('pointerdown', carouselState.boundHandlePointerDown);
    carouselState.viewport.addEventListener('pointermove', carouselState.boundHandlePointerMove);
    carouselState.viewport.addEventListener('pointerup', carouselState.boundHandlePointerUp);
    carouselState.viewport.addEventListener('pointercancel', carouselState.boundHandlePointerUp);
    carouselState.viewport.addEventListener('mouseenter', carouselState.boundHandleMouseEnter);
    carouselState.viewport.addEventListener('mouseleave', carouselState.boundHandleMouseLeave);
    carouselState.viewport.addEventListener('click', handleViewportClick, true);
    carouselState.viewport.addEventListener('keydown', carouselState.boundHandleKeyDown);

    if (carouselState.prevBtn) {
        carouselState.prevBtn.addEventListener('click', carouselState.boundHandlePrevClick);
    }
    if (carouselState.nextBtn) {
        carouselState.nextBtn.addEventListener('click', carouselState.boundHandleNextClick);
    }

    carouselState.indicators.forEach(ind => {
        ind.addEventListener('click', carouselState.boundHandleIndicatorClick);
    });

    document.addEventListener('visibilitychange', carouselState.boundHandleVisibilityChange);
    window.addEventListener('resize', carouselState.boundHandleResize);

    // Initial position
    moveToSlide(1, false);
    startAutoplay();
}

function updateCarouselPosition(withTransition = true) {
    if (!carouselState.track || !carouselState.viewport) return;
    const containerWidth = carouselState.viewport.clientWidth;
    const activeSlide = carouselState.slides[carouselState.currentIndex];
    if (!activeSlide) return;
    const slideWidth = activeSlide.offsetWidth;

    let totalOffset = 0;
    for (let i = 0; i < carouselState.currentIndex; i++) {
        totalOffset += carouselState.slides[i].offsetWidth;
    }

    const translateX = containerWidth / 2 - (totalOffset + slideWidth / 2);
    const prefersReduced = window.matchMedia('(prefers-reduced-motion: reduce)').matches;

    if (withTransition && !prefersReduced) {
        carouselState.track.style.transition = 'transform 0.5s cubic-bezier(0.25, 1, 0.5, 1)';
    } else {
        carouselState.track.style.transition = 'none';
    }

    carouselState.track.style.transform = `translateX(${translateX}px)`;
    carouselState.currentTranslate = translateX;
    carouselState.prevTranslate = translateX;
}

function updateSlideStyles() {
    if (carouselState.slides.length === 0) return;
    const isMobile = window.innerWidth < 768;

    carouselState.slides.forEach((slide, idx) => {
        const article = slide.querySelector('article');
        if (!article) return;
        article.classList.remove('scale-100', 'opacity-100', 'shadow-2xl', 'border-isivi-gold/50', 'z-10');
        article.classList.remove('scale-90', 'opacity-40', 'blur-[1px]', 'shadow-sm', 'z-0');

        article.style.transition = 'transform 0.5s cubic-bezier(0.25, 1, 0.5, 1), opacity 0.5s cubic-bezier(0.25, 1, 0.5, 1), filter 0.5s cubic-bezier(0.25, 1, 0.5, 1), border-color 0.5s cubic-bezier(0.25, 1, 0.5, 1)';

        if (idx === carouselState.currentIndex) {
            article.classList.add('scale-100', 'opacity-100', 'shadow-2xl', 'z-10');
            article.style.filter = 'none';
            if (isMobile) {
                article.style.boxShadow = '0 0 15px rgba(212, 175, 55, 0.22), 0 10px 25px -5px rgba(0, 0, 0, 0.85)';
                article.style.borderColor = 'rgba(212, 175, 55, 0.35)';
                article.style.transform = 'scale(1)';
                article.style.opacity = '1';
            } else {
                article.classList.add('border-isivi-gold/50');
                article.style.boxShadow = '';
                article.style.borderColor = '';
                article.style.transform = '';
                article.style.opacity = '';
            }
        } else {
            article.classList.add('shadow-sm', 'z-0');
            if (isMobile) {
                article.style.transform = 'scale(0.96)';
                article.style.opacity = '0.7';
                article.style.filter = 'none';
                article.style.boxShadow = 'none';
                article.style.borderColor = 'rgba(212, 175, 55, 0.05)';
            } else {
                article.classList.add('scale-90', 'opacity-40');
                article.style.transform = '';
                article.style.opacity = '';
                const prefersReduced = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
                article.style.filter = prefersReduced ? 'none' : 'blur(1px)';
                article.style.boxShadow = '';
                article.style.borderColor = '';
            }
        }
    });

    const N = carouselState.originalCount;
    const originalIndex = (carouselState.currentIndex - 1 + N) % N;

    carouselState.indicators.forEach((indicator, i) => {
        const isActive = i === originalIndex;
        indicator.setAttribute('aria-current', isActive ? 'true' : 'false');
        const progress = indicator.querySelector('.indicator-progress');
        if (isActive) {
            indicator.classList.remove('bg-stone-800');
            indicator.classList.add('bg-stone-700');
        } else {
            indicator.classList.remove('bg-stone-700');
            indicator.classList.add('bg-stone-800');
            if (progress) {
                progress.style.transition = 'none';
                progress.style.width = '0%';
            }
        }
    });
}

function moveToSlide(index, withTransition = true) {
    if (carouselState.isTransitioning && withTransition) return;
    if (withTransition) {
        carouselState.isTransitioning = true;
    }
    carouselState.currentIndex = index;
    updateCarouselPosition(withTransition);
    updateSlideStyles();

    if (withTransition) {
        setTimeout(() => {
            handleTransitionEnd();
        }, 500);
    }
}

function handleTransitionEnd() {
    carouselState.isTransitioning = false;
    const N = carouselState.originalCount;
    if (carouselState.currentIndex === 0) {
        moveToSlide(N, false);
    } else if (carouselState.currentIndex === N + 1) {
        moveToSlide(1, false);
    }
    if (!carouselState.isDragging && !carouselState.isHovered) {
        startAutoplay();
    }
}

function startAutoplay() {
    const N = carouselState.originalCount;
    if (N <= 1) return;
    stopAutoplay();
    animateIndicatorProgress();
    carouselState.autoplayTimer = setTimeout(() => {
        moveToSlide(carouselState.currentIndex + 1);
    }, carouselState.autoplayDuration);
}

function stopAutoplay() {
    if (carouselState.autoplayTimer) {
        clearTimeout(carouselState.autoplayTimer);
        carouselState.autoplayTimer = null;
    }
    stopIndicatorProgress();
}

function animateIndicatorProgress() {
    const N = carouselState.originalCount;
    const originalIndex = (carouselState.currentIndex - 1 + N) % N;
    const activeIndicator = carouselState.indicators[originalIndex];
    if (!activeIndicator) return;
    const progress = activeIndicator.querySelector('.indicator-progress');
    if (!progress) return;

    progress.style.transition = 'none';
    progress.style.width = '0%';
    progress.offsetHeight; // force reflow

    const prefersReduced = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
    if (!prefersReduced) {
        progress.style.transition = `width ${carouselState.autoplayDuration}ms linear`;
        progress.style.width = '100%';
    } else {
        progress.style.width = '100%';
    }
}

function stopIndicatorProgress() {
    const N = carouselState.originalCount;
    const originalIndex = (carouselState.currentIndex - 1 + N) % N;
    const activeIndicator = carouselState.indicators[originalIndex];
    if (!activeIndicator) return;
    const progress = activeIndicator.querySelector('.indicator-progress');
    if (!progress) return;

    const computedWidth = window.getComputedStyle(progress).width;
    progress.style.transition = 'none';
    progress.style.width = computedWidth;
}

function resetAutoplayOnInteraction() {
    stopAutoplay();
    if (carouselState.resumeTimer) {
        clearTimeout(carouselState.resumeTimer);
    }
    carouselState.resumeTimer = setTimeout(() => {
        if (!carouselState.isHovered && !carouselState.isDragging) {
            startAutoplay();
        }
    }, 5000);
}

function renderClientBanners() {
    const container = document.getElementById('client-banners');
    if (!container) return;
    const activeBanners = bannersData.filter(banner => banner.active);
    
    cleanupBannersCarousel();

    if (activeBanners.length === 0) {
        container.classList.add('hidden');
        return;
    }
    
    container.classList.remove('hidden');

    if (activeBanners.length === 1) {
        const banner = activeBanners[0];
        container.innerHTML = `
            <div class="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
                <article class="mx-auto flex max-w-7xl flex-col overflow-hidden bg-isivi-900 shadow-2xl">
                    <img src="${banner.image}" alt="${banner.title}" loading="lazy" decoding="async" class="object-cover" onerror="this.onerror=null; this.src='/images/isivi-logo-transparent.png'; this.classList.add('p-6','bg-stone-900','object-contain');">
                    <div class="flex flex-1 flex-col justify-center items-center text-center bg-gradient-to-br from-isivi-900 to-stone-950 p-4 sm:p-6">
                        <span class="mb-1 text-[9px] sm:text-[10px] font-bold uppercase tracking-[0.2em] text-isivi-gold">Promoción especial</span>
                        <h2 class="font-serif-title text-base sm:text-xl font-bold text-white">${banner.title}</h2>
                        <p class="mt-1 max-w-xl text-[10px] sm:text-xs leading-relaxed text-isivi-200">${banner.description}</p>
                        <div class="mt-2.5 sm:mt-3">
                            <a href="${banner.buttonLink}" class="inline-flex rounded-xl bg-isivi-gold px-4 py-2 text-[10px] sm:text-xs font-bold text-isivi-black transition hover:bg-yellow-500">${banner.buttonText}</a>
                        </div>
                    </div>
                </article>
            </div>
        `;
        return;
    }

    const N = activeBanners.length;
    const slides = [activeBanners[N - 1], ...activeBanners, activeBanners[0]];
    
    let slidesHtml = slides.map((banner, index) => {
        return `
            <div class="carousel-slide flex-shrink-0 w-[90%] sm:w-[90%] md:w-[75%] lg:w-[70%] px-1.5 sm:px-4 transition-all duration-500 ease-out" data-index="${index}">
                <article class="flex flex-col overflow-hidden bg-isivi-900 shadow-2xl h-full select-none">
                    <img src="${banner.image}" alt="${banner.title}" draggable="false" loading="lazy" decoding="async" class="w-full object-cover select-none pointer-events-none" onerror="this.onerror=null; this.src='/images/isivi-logo-transparent.png'; this.classList.add('p-6','bg-stone-900','object-contain');">
                    <div class="flex flex-1 flex-col justify-center items-center text-center bg-gradient-to-br from-isivi-900 via-stone-950 to-isivi-900 p-4 sm:p-6">
                        <span class="mb-1 text-[9px] sm:text-[10px] font-bold uppercase tracking-[0.2em] text-isivi-gold">Promoción especial</span>
                        <h2 class="font-serif-title text-base sm:text-xl font-bold text-white">${banner.title}</h2>
                        <p class="mt-1 max-w-xl text-[10px] sm:text-xs leading-relaxed text-isivi-200">${banner.description}</p>
                        <div class="mt-2.5 sm:mt-3">
                            <a href="${banner.buttonLink}" class="carousel-cta inline-flex rounded-xl bg-isivi-gold px-4 py-2 text-[10px] sm:text-xs font-bold text-isivi-black transition hover:bg-yellow-500">${banner.buttonText}</a>
                        </div>
                    </div>
                </article>
            </div>
        `;
    }).join('');
    
    let indicatorsHtml = activeBanners.map((_, i) => {
        return `
            <button class="carousel-indicator relative font-serif text-[10px] font-bold text-stone-500 hover:text-isivi-gold transition-colors duration-300 focus:outline-none px-1" data-slide-index="${i}" aria-label="Ir al banner ${i + 1}" aria-current="${i === 0 ? 'true' : 'false'}">
                <span>0${i + 1}</span>
            </button>
            ${i < activeBanners.length - 1 ? '<span class="text-stone-700/60 select-none text-[8px] mx-0.5">━━━</span>' : ''}
        `;
    }).join('');

    container.innerHTML = `
        <div class="relative mx-auto max-w-7xl overflow-hidden px-1 sm:px-8 py-2">
            <div class="text-center mb-6">
                <span class="font-serif text-[10px] sm:text-xs text-isivi-gold tracking-[0.25em] uppercase block">THE ISIVI EDIT</span>
                <h3 class="font-serif-title text-base sm:text-lg font-bold text-white mt-1">Cartagena · Beauty Stories</h3>
                <div class="flex items-center justify-center gap-2 mt-2">
                    <div class="w-8 h-[1px] bg-gradient-to-r from-transparent to-isivi-gold/30"></div>
                    <span class="text-isivi-gold/40 text-[9px]">✦</span>
                    <div class="w-8 h-[1px] bg-gradient-to-l from-transparent to-isivi-gold/30"></div>
                </div>
            </div>
            <div class="carousel-viewport overflow-hidden relative cursor-grab active:cursor-grabbing" tabindex="0" role="region" aria-roledescription="carousel" aria-label="Banners de promociones">
                <div class="carousel-track flex transition-transform duration-500 ease-out" style="transform: translateX(0px);">
                    ${slidesHtml}
                </div>
            </div>
            
            <button type="button" class="carousel-control prev absolute left-2 sm:left-4 top-1/2 -translate-y-1/2 flex h-9 w-9 sm:h-11 sm:w-11 items-center justify-center rounded-full border border-isivi-gold/30 bg-stone-950/80 text-isivi-gold shadow-lg backdrop-blur-sm transition hover:bg-isivi-gold hover:text-isivi-black hover:border-isivi-gold active:scale-95 z-20" aria-label="Banner anterior">
                <i class="fa-solid fa-chevron-left text-xs sm:text-sm"></i>
            </button>
            <button type="button" class="carousel-control next absolute right-2 sm:right-4 top-1/2 -translate-y-1/2 flex h-9 w-9 sm:h-11 sm:w-11 items-center justify-center rounded-full border border-isivi-gold/30 bg-stone-950/80 text-isivi-gold shadow-lg backdrop-blur-sm transition hover:bg-isivi-gold hover:text-isivi-black hover:border-isivi-gold active:scale-95 z-20" aria-label="Siguiente banner">
                <i class="fa-solid fa-chevron-right text-xs sm:text-sm"></i>
            </button>
            
            <div class="carousel-indicators-container flex justify-center items-center gap-1.5 mt-3 sm:mt-5">
                ${indicatorsHtml}
            </div>
        </div>
    `;

    initializeBannersCarousel(N);
}

function renderServicesGrid() {
    const container = document.getElementById('services-grid');
    if (!container) return;
    let filtered = activeCategory === 'todos' ? servicesData : servicesData.filter(s => normalizeServiceCategory(s.category) === activeCategory);

    // Fallback de resiliencia: si la categoría no tiene elementos pero hay servicios, mostrar 'todos'
    if (filtered.length === 0 && servicesData.length > 0 && activeCategory !== 'todos') {
        activeCategory = 'todos';
        renderCategoryTabs();
        filtered = servicesData;
    }

    if (filtered.length === 0) {
        container.innerHTML = '<p class="w-full py-8 text-center text-sm text-isivi-300">Aún no hay servicios en esta categoría.</p>';
        return;
    }

    container.innerHTML = filtered.map((service, index) => {
        const isSelected = selectedServices.includes(service.id);
        const deposit = Math.round(service.price * 0.25);
        return `
            <div class="snap-start isivi-card-premium rounded-2xl overflow-hidden border-2 ${isSelected ? 'border-isivi-gold ring-1 ring-isivi-gold' : 'border-isivi-gold/60'} shadow-lg flex flex-col justify-between transition-transform duration-300 hover:-translate-y-1" style="height: var(--catalog-card-height-service); width: calc(var(--catalog-card-width-base) * 1.1);">
                <div class="relative overflow-hidden bg-stone-900 flex-shrink-0" style="height: var(--catalog-card-image-height-service);">
                    <img src="${service.img}" alt="${service.name}" class="w-full h-full object-cover transition-transform duration-700 hover:scale-105" onerror="this.onerror=null; this.src='/images/isivi-logo-transparent.png'; this.classList.add('p-6','bg-stone-900','object-contain');">
                    <span class="absolute top-3 right-3 bg-stone-950/90 text-isivi-gold text-[10px] font-bold px-2 py-0.5 rounded-full border border-isivi-500/30">
                        <i class="fa-regular fa-clock mr-1"></i> ${service.duration || '60 min'}
                    </span>
                </div>
                <div class="p-4 flex-1 flex flex-col justify-between min-h-0 overflow-hidden">
                    <div class="min-h-0 overflow-hidden">
                        <h3 class="font-bold text-isivi-gold text-xs mb-1 truncate">${service.name}</h3>
                        <p class="text-isivi-300 text-[10px] leading-snug overflow-hidden text-ellipsis" style="display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical;">${service.desc || ''}</p>
                    </div>
                    <div class="pt-2 border-t border-stone-900 flex items-center justify-between flex-shrink-0">
                        <div class="flex flex-col gap-0.5">
                            <div>
                                <span class="text-[8px] text-stone-400 uppercase tracking-wider font-semibold block leading-none">Precio Total</span>
                                <span class="font-extrabold text-isivi-gold text-[13px] block leading-tight">$${service.price.toLocaleString('es-CO')}</span>
                            </div>
                            <div class="inline-flex items-center gap-1 mt-0.5 px-2 py-0.5 rounded-full bg-emerald-500/10 border border-emerald-500/20 text-emerald-400">
                                <span class="text-[7px] uppercase tracking-wider font-bold">Anticipo</span>
                                <span class="font-extrabold text-[8px]">$${deposit.toLocaleString('es-CO')}</span>
                            </div>
                        </div>
                        <button onclick="toggleService('${service.id}')" class="px-3.5 py-1.5 rounded-xl text-[10px] font-bold transition flex items-center gap-1.5 shadow ${isSelected ? 'bg-emerald-600 text-white' : 'bg-gradient-to-r from-isivi-500 to-isivi-600 text-white hover:from-isivi-600 hover:to-isivi-700'}">
                            ${isSelected ? '<i class="fa-solid fa-check"></i> En carrito' : '<i class="fa-solid fa-calendar-plus"></i> Agendar'}
                        </button>
                    </div>
                </div>
            </div>
        `;
    }).join('');
}

async function toggleService(id) {
    const serv = servicesData.find(s => s.id === id);
    if (selectedServices.includes(id)) {
        selectedServices = selectedServices.filter(sId => sId !== id);
        showToast("Servicio removido");
        trackEvent('remove_from_cart', { item_type: 'service', item_id: id });
        if (selectedServices.length === 0) {
            const hold = getPendingHold();
            if (hold && hold.id) {
                selectedTimeSlot = '';
                await releaseHoldImmediately(hold);
                showToast('Horario de cita liberado al no tener servicios en el carrito.', 'info');
                await renderTimeSlots();
                await renderCartSchedule();
            }
        }
    } else {
        selectedServices.push(id);
        showToast("✓ Servicio seleccionado", "success");
        trackEvent('add_to_cart', {
            item_type: 'service',
            item_id: id,
            item_name: serv ? serv.name : 'Servicio',
            quantity: 1,
            unit_price: serv ? serv.price : 0,
            value: serv ? serv.price : 0,
            currency: 'COP'
        });
        openCartDrawer();
    }

    renderServicesGrid();
    syncCartState();
}


// ---------- Categorías y Productos Cliente ----------
function getProductCartCount(productId, variantId = null) {
    const items = getCartItems();
    return items
        .filter(item => item.type === 'product' && item.id === productId && (!variantId || item.variantId === variantId))
        .reduce((sum, item) => sum + (item.quantity || 1), 0);
}

function getKitCartCount(kitId) {
    const items = getCartItems();
    return items
        .filter(item => item.type === 'kit' && item.id === kitId)
        .reduce((sum, item) => sum + (item.quantity || 1), 0);
}

function renderProductCategoryPills() {
    const container = document.getElementById('product-categories-pills');
    if (!container) return;

    const activeCats = (productCategoriesData || []).filter(c => c.activo !== false);
    if (activeCats.length === 0) {
        container.innerHTML = '';
        container.classList.add('hidden');
        return;
    }
    container.classList.remove('hidden');

    const pills = [
        { id: 'todos', name: 'Todos los productos' },
        ...activeCats.map(c => ({ id: c.id, name: c.name }))
    ];

    container.innerHTML = pills.map(cat => `
        <button type="button" onclick="selectProductCategory('${cat.id}')" class="px-4 py-2 rounded-full text-xs font-semibold transition ${activeProductCategory === cat.id ? 'bg-isivi-gold text-isivi-black font-bold shadow' : 'bg-stone-900 border border-isivi-500/30 text-isivi-200 hover:border-isivi-gold'}">
            ${cat.name}
        </button>
    `).join('');
}

function selectProductCategory(catId) {
    activeProductCategory = catId;
    trackEvent('view_product_category', { category: catId });
    renderProductCategoryPills();
    renderProductsGrid();
}

function renderKitCategoryPills() {
    const container = document.getElementById('kit-categories-pills');
    if (!container) return;

    const activeCats = (productCategoriesData || []).filter(c => c.activo !== false);
    if (activeCats.length === 0) {
        container.innerHTML = '';
        container.classList.add('hidden');
        return;
    }
    container.classList.remove('hidden');

    const pills = [
        { id: 'todos', name: 'Todos los kits' },
        ...activeCats.map(c => ({ id: c.id, name: c.name }))
    ];

    container.innerHTML = pills.map(cat => `
        <button type="button" onclick="selectKitCategory('${cat.id}')" class="px-4 py-2 rounded-full text-xs font-semibold transition ${activeKitCategory === cat.id ? 'bg-isivi-gold text-isivi-black font-bold shadow' : 'bg-stone-900 border border-isivi-500/30 text-isivi-200 hover:border-isivi-gold'}">
            ${cat.name}
        </button>
    `).join('');
}

function selectKitCategory(catId) {
    activeKitCategory = catId;
    trackEvent('view_kit_category', { category: catId });
    renderKitCategoryPills();
    renderKitsGrid();
}

function selectProductVariant(productId, variantId) {
    selectedProductVariants[productId] = variantId;
    renderProductsGrid();
}

function renderProductsGrid() {
    const container = document.getElementById('products-grid');
    if (!container) return;

    let filtered = productsData;
    if (activeProductCategory !== 'todos') {
        filtered = productsData.filter(p => p.category === activeProductCategory);
    }

    if (filtered.length === 0) {
        container.innerHTML = '<div class="col-span-full w-full py-12 text-center text-xs text-isivi-300"><i class="fa-solid fa-boxes-stacked text-2xl text-stone-600 mb-2 block"></i>No hay productos disponibles en esta categoría actualmente.</div>';
        return;
    }

    container.innerHTML = filtered.map(product => {
        const isSelected = selectedProducts.some(k => k === product.id || k.startsWith(product.id + '_'));
        const isVariantType = product.priceType === 'VARIANTES' && Array.isArray(product.variants) && product.variants.length > 0;
        
        let currentVar = null;
        let activeVars = [];
        let priceToDisplay = product.price;
        let inStock = product.inStock !== false && product.quantity > 0;
        let availableStock = product.quantity;

        if (isVariantType) {
            activeVars = product.variants.filter(v => v.activo !== false);
            const savedVarId = selectedProductVariants[product.id];
            currentVar = activeVars.find(v => v.id === savedVarId) || activeVars[0];
            if (currentVar) {
                selectedProductVariants[product.id] = currentVar.id;
                priceToDisplay = Number(currentVar.precio) || 0;
                availableStock = Number(currentVar.cantidad) || 0;
                inStock = currentVar.enStock !== false && availableStock > 0;
            }
        }

        const chosenQuantity = cardQuantity('product', product.id);
        const inCartCount = getProductCartCount(product.id, currentVar ? currentVar.id : null);

        let variantChipsHtml = '';
        if (isVariantType && activeVars.length > 0) {
            variantChipsHtml = `
                <div class="mb-3 space-y-1">
                    <span class="text-[10px] text-isivi-300 block font-semibold">Opciones / Presentación:</span>
                    <div class="flex flex-wrap gap-1">
                        ${activeVars.map(v => {
                            const isVarSel = currentVar && currentVar.id === v.id;
                            const isVarOut = v.enStock === false || v.cantidad <= 0;
                            return `
                                <button type="button" onclick="selectProductVariant('${product.id}', '${v.id}')" ${isVarOut ? 'disabled' : ''} class="px-2.5 py-1 rounded-lg text-[10px] font-bold transition border ${isVarSel ? 'bg-isivi-gold text-isivi-black border-isivi-gold shadow-sm' : isVarOut ? 'bg-stone-900/60 text-stone-600 border-stone-800 cursor-not-allowed line-through' : 'bg-stone-900 text-stone-300 border-stone-700 hover:border-isivi-gold'}">
                                    ${v.nombre} · $${Number(v.precio || 0).toLocaleString('es-CO')}
                                </button>
                            `;
                        }).join('')}
                    </div>
                </div>
            `;
        }

        let actionButtonHtml = '';
        const isReservado = isVariantType 
            ? (currentVar && currentVar.temporalmenteReservado === true)
            : (product.temporalmenteReservado === true);

        if (!inStock) {
            if (isReservado) {
                actionButtonHtml = `<button disabled class="w-full py-2.5 rounded-xl text-xs font-bold bg-sky-950 text-sky-400 border border-sky-800 cursor-not-allowed">Reservado</button>`;
            } else {
                actionButtonHtml = `<button disabled class="w-full py-2.5 rounded-xl text-xs font-bold bg-stone-900 text-stone-600 cursor-not-allowed">Agotado</button>`;
            }
        } else if (inCartCount > 0) {
            actionButtonHtml = `
                <div class="flex items-center gap-1.5 w-full">
                    <div class="flex items-center rounded-lg border border-stone-700 bg-stone-900 flex-1 justify-between">
                        <button type="button" onclick="changeCardQuantity('product','${product.id}',-1)" class="px-3 py-1.5 text-isivi-gold font-bold text-sm">-</button>
                        <span class="text-xs font-bold text-white">${inCartCount}</span>
                        <button type="button" onclick="changeCardQuantity('product','${product.id}',1)" ${inCartCount >= availableStock ? 'disabled' : ''} class="px-3 py-1.5 text-isivi-gold font-bold text-sm disabled:text-stone-600">+</button>
                    </div>
                    <button onclick="addItemToCart('product','${product.id}')" class="px-3 py-2.5 rounded-xl text-xs font-bold bg-rose-600 text-white hover:bg-rose-700 transition shadow flex items-center gap-1" title="Quitar del carrito">
                        <i class="fa-solid fa-trash-can"></i> <span>Quitar</span>
                    </button>
                </div>
            `;
        } else {
            actionButtonHtml = `
                <button onclick="addItemToCart('product','${product.id}')" class="w-full py-2.5 rounded-xl text-xs font-bold bg-gradient-to-r from-isivi-500 to-isivi-600 text-white hover:from-isivi-600 hover:to-isivi-700 transition flex items-center justify-center gap-1.5 shadow" aria-label="Agregar ${product.name} al carrito">
                    <i class="fa-solid fa-cart-plus mr-0.5"></i> <span>Agregar</span>
                </button>
            `;
        }

        let badgeHtml = '';
        if (inCartCount === 0) {
            if (availableStock > 0) {
                if (isReservado) {
                    badgeHtml = `<span class="absolute top-3 right-3 bg-amber-950 text-amber-400 border border-amber-800 text-[10px] font-bold px-2 py-0.5 rounded-full z-10 flex items-center gap-1"><i class="fa-solid fa-clock"></i> RESERVADO PARCIALMENTE</span>`;
                }
            } else {
                if (isReservado) {
                    badgeHtml = `<span class="absolute top-3 right-3 bg-sky-950 text-sky-400 border border-sky-800 text-[10px] font-bold px-2 py-0.5 rounded-full z-10 flex items-center gap-1"><i class="fa-solid fa-clock"></i> RESERVADO TEMPORALMENTE</span>`;
                } else {
                    badgeHtml = `<span class="absolute top-3 right-3 bg-red-950 text-red-400 border border-red-800 text-[10px] font-bold px-2 py-0.5 rounded-full z-10">AGOTADO</span>`;
                }
            }
        }

        return `
            <div class="snap-start isivi-card-premium rounded-2xl p-4 border-2 ${isSelected ? 'border-isivi-gold ring-1 ring-isivi-gold' : 'border-isivi-gold/60'} shadow-md flex flex-col justify-between relative" style="height: var(--catalog-card-height); width: var(--catalog-card-width-base);">
                ${badgeHtml}
                <div class="relative overflow-hidden rounded-xl bg-stone-900 flex-shrink-0" style="height: var(--catalog-card-image-height);">
                    <img src="${product.img}" alt="${product.name}" class="w-full h-full object-cover transition-transform duration-700 hover:scale-105 ${!inStock ? 'opacity-40 grayscale' : ''}" onerror="this.onerror=null; this.src='/images/isivi-logo-transparent.png'; this.classList.add('p-4','bg-stone-900','object-contain');">
                </div>
                <div class="flex-1 flex flex-col justify-between min-h-0 overflow-hidden mt-2">
                    <div class="min-h-0 overflow-hidden">
                        <h4 class="font-bold text-isivi-gold text-xs mb-1 truncate">${product.name}</h4>
                        <p class="text-isivi-300 text-[10px] leading-snug overflow-hidden text-ellipsis" style="display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical;">${product.desc || ''}</p>
                        ${variantChipsHtml}
                    </div>
                    <div class="pt-2 border-t border-stone-900 flex flex-col gap-1.5 flex-shrink-0">
                        <div class="flex flex-col">
                            <span class="text-[8px] text-stone-400 uppercase tracking-wider font-semibold block leading-none">${isVariantType ? 'Precio Variante' : 'Precio Total'}</span>
                            <span class="font-extrabold text-isivi-gold text-[13px] block leading-tight mt-0.5">$${priceToDisplay.toLocaleString('es-CO')}</span>
                        </div>
                        <div class="w-full">
                            ${actionButtonHtml}
                        </div>
                    </div>
                </div>
            </div>
        `;
    }).join('');
}

async function toggleProduct(id) {
    const product = productsData.find(p => p.id === id);
    if (product && product.inStock === false) {
        showToast("Producto agotado", "error");
        return;
    }
    const cartKey = (product && product.priceType === 'VARIANTES' && selectedProductVariants[product.id]) 
        ? `${product.id}_${selectedProductVariants[product.id]}` 
        : id;

    const isAdded = selectedProducts.includes(cartKey);
    const hold = getPendingHold();

    if (isAdded) {
        selectedProducts = selectedProducts.filter(pId => pId !== cartKey);
        const prevQty = productQuantities[cartKey];
        delete productQuantities[cartKey];
        
        if (hold && hold.id) {
            try {
                const updated = await syncCartItemsWithHold(hold.id, hold.code, hold.phone);
                setPendingHold(updated);
                const hasServiceRemaining = selectedServices.length > 0;
                if (hasServiceRemaining) {
                    showToast('Producto eliminado. Tu cita continúa reservada.', 'info');
                } else {
                    showToast("Producto removido");
                }
                await refreshCatalogStock();
            } catch (err) {
                selectedProducts.push(cartKey);
                productQuantities[cartKey] = prevQty;
                showToast(err.message || 'No se pudo remover el producto del hold.', 'error');
                return;
            }
        } else {
            showToast("Producto removido");
        }
    } else {
        selectedProducts.push(cartKey);
        productQuantities[cartKey] = 1;
        
        if (hold && hold.id) {
            try {
                const updated = await syncCartItemsWithHold(hold.id, hold.code, hold.phone);
                setPendingHold(updated);
                showToast("Producto agregado a tu pedido");
                await refreshCatalogStock();
            } catch (err) {
                selectedProducts = selectedProducts.filter(pId => pId !== cartKey);
                delete productQuantities[cartKey];
                showToast(err.message || 'No hay stock suficiente para agregar este producto.', 'error');
                return;
            }
        } else {
            showToast("Producto agregado a tu pedido");
        }
    }
    renderProductsGrid();
    syncCartState();
}

function itemQuantity(type, id) { return Number((type === 'product' ? productQuantities : kitQuantities)[id]) || 1; }
function cardQuantity(type, id) { return Number((type === 'product' ? productCardQuantities : kitCardQuantities)[id]) || 1; }
async function changeCardQuantity(type, id, delta) {
    const catalog = type === 'product' ? productsData : kitsData;
    const item = catalog.find(candidate => candidate.id === id);
    if (!item) return;

    let variantId = null;
    let maxStock = item.quantity;
    if (type === 'product' && item.priceType === 'VARIANTES' && Array.isArray(item.variants)) {
        const activeVars = item.variants.filter(v => v.activo !== false);
        const selVarId = selectedProductVariants[item.id] || (activeVars[0] ? activeVars[0].id : null);
        const selVar = activeVars.find(v => v.id === selVarId);
        if (selVar) {
            variantId = selVar.id;
            maxStock = Number(selVar.cantidad) || 0;
        }
    }

    const cartKey = (type === 'product' && variantId) ? `${id}_${variantId}` : id;
    const selected = type === 'product' ? selectedProducts : selectedKits;
    const quantities = type === 'product' ? productQuantities : kitQuantities;

    const inCart = selected.includes(cartKey);

    if (inCart) {
        const hold = getPendingHold();
        const currentQty = Number(quantities[cartKey]) || 1;
        const next = currentQty + delta;
        if (next < 1) {
            removeCartItem(type, cartKey);
            showToast('Artículo removido');
            return;
        }
        if (delta > 0 && hold && hold.id) {
            showToast('No puedes agregar más unidades de un producto con pago pendiente.', 'error');
            return;
        }
        if (delta > 0 && next > maxStock) {
            showToast('Solo quedan ' + maxStock + ' unidades disponibles.', 'error');
            return;
        }
        quantities[cartKey] = next;

        if (hold && hold.id) {
            try {
                const updated = await syncCartItemsWithHold(hold.id, hold.code, hold.phone);
                setPendingHold(updated);
                await refreshCatalogStock();
            } catch (err) {
                console.warn('Error al sincronizar cantidad:', err);
                quantities[cartKey] = currentQty; // revertir
                showToast(err.message || 'No se pudo actualizar la cantidad.', 'error');
                return;
            }
        }

        type === 'product' ? renderProductsGrid() : renderKitsGrid();
        syncCartState();
    } else {
        const localQuantities = type === 'product' ? productCardQuantities : kitCardQuantities;
        const currentQty = Number(localQuantities[id]) || 1;
        const next = Math.max(1, Math.min(maxStock, currentQty + delta));
        localQuantities[id] = next;
        type === 'product' ? renderProductsGrid() : renderKitsGrid();
    }
}

function addItemToCart(type, id) {
    const catalog = type === 'product' ? productsData : kitsData;
    const selected = type === 'product' ? selectedProducts : selectedKits;
    const quantities = type === 'product' ? productQuantities : kitQuantities;
    const item = catalog.find(candidate => candidate.id === id);
    if (!item) return;

    let targetPrice = item.price;
    let targetStock = item.quantity;
    let targetName = item.name;
    let variantId = null;
    let variantName = null;

    if (type === 'product' && item.priceType === 'VARIANTES' && Array.isArray(item.variants) && item.variants.length > 0) {
        const activeVars = item.variants.filter(v => v.activo !== false);
        const selVarId = selectedProductVariants[item.id] || (activeVars[0] ? activeVars[0].id : null);
        const selVar = activeVars.find(v => v.id === selVarId) || activeVars[0];
        if (!selVar) return showToast('No hay variantes disponibles', 'error');
        if (selVar.enStock === false || selVar.cantidad < 1) return showToast('Este producto está agotado.', 'error');

        variantId = selVar.id;
        variantName = selVar.nombre;
        targetPrice = Number(selVar.precio) || 0;
        targetStock = Number(selVar.cantidad) || 0;
        targetName = `${item.name} (${variantName})`;
    } else {
        if (item.inStock === false || item.quantity < 1) return showToast('Este producto está agotado.', 'error');
    }

    const cartKey = (type === 'product' && variantId) ? `${id}_${variantId}` : id;

    // Toggle: si ya está en el carrito, se remueve
    if (selected.includes(cartKey)) {
        removeCartItem(type, cartKey);
        showToast('Artículo removido');
        return;
    }

    // Agrega exactamente 1 unidad
    const requested = 1;
    if (requested > targetStock) {
        return showToast('Este producto está agotado.', 'error');
    }

    selected.push(cartKey);
    quantities[cartKey] = requested;
    (type === 'product' ? productCardQuantities : kitCardQuantities)[id] = 1;

    type === 'product' ? renderProductsGrid() : renderKitsGrid();
    syncCartState();
    showToast('✓ Agregado al carrito', 'success');

    const badge = document.getElementById('nav-cart-badge');
    if (badge) {
        badge.classList.remove('animate-badge-pop');
        void badge.offsetWidth;
        badge.classList.add('animate-badge-pop');
    }

    trackEvent('add_to_cart', {
        item_type: type,
        item_id: id,
        variant_id: variantId,
        item_name: targetName,
        quantity: requested,
        unit_price: targetPrice,
        value: targetPrice * requested,
        currency: 'COP'
    });
}

async function changeCartQuantity(type, cartKey, delta) {
    const id = (type === 'product' && cartKey.includes('_')) ? cartKey.split('_')[0] : cartKey;
    const catalog = type === 'product' ? productsData : kitsData;
    const item = catalog.find(candidate => candidate.id === id);
    const quantities = type === 'product' ? productQuantities : kitQuantities;
    if (!item) {
        showToast('Verificando disponibilidad del artículo...', 'info');
        return;
    }

    let maxStock = item.inStock !== false ? (Number.isInteger(item.quantity) ? item.quantity : 0) : 0;
    if (type === 'product' && cartKey.includes('_')) {
        const varId = cartKey.split('_')[1];
        const varObj = (item.variants || []).find(v => v.id === varId);
        if (varObj) {
            maxStock = (varObj.enStock !== false && varObj.activo !== false) ? (Number.isInteger(varObj.cantidad) ? varObj.cantidad : 0) : 0;
        }
    }

    const hold = getPendingHold();
    const currentQty = Number(quantities[cartKey]) || 1;
    const next = currentQty + delta;
    if (next < 1) {
        removeCartItem(type, cartKey);
        showToast('Artículo removido');
        return;
    }
    if (delta > 0 && hold && hold.id) {
        showToast('No puedes agregar más unidades de un producto con pago pendiente.', 'error');
        return;
    }
    if (delta > 0 && (maxStock <= 0 || next > maxStock)) {
        showToast(maxStock <= 0 ? 'Artículo agotado' : `Solo hay ${maxStock} unidades disponibles`, 'error');
        return;
    }
    quantities[cartKey] = next;

    if (hold && hold.id) {
        try {
            const updated = await syncCartItemsWithHold(hold.id, hold.code, hold.phone);
            setPendingHold(updated);
            await refreshCatalogStock();
        } catch (err) {
            console.warn('Error al sincronizar cantidad:', err);
            quantities[cartKey] = currentQty; // revertir
            showToast(err.message || 'No se pudo actualizar la cantidad.', 'error');
            return;
        }
    }

    type === 'product' ? renderProductsGrid() : renderKitsGrid();
    syncCartState();
}

// ---------- Kits ----------
function renderKitsGrid() {
    const container = document.getElementById('kits-grid');
    if (!container) return;

    let filtered = kitsData;
    if (activeKitCategory !== 'todos') {
        filtered = kitsData.filter(k => k.category === activeKitCategory);
    }

    if (filtered.length === 0) {
        container.innerHTML = '<div class="col-span-full w-full py-12 text-center text-xs text-isivi-300"><i class="fa-solid fa-boxes-stacked text-2xl text-stone-600 mb-2 block"></i>No hay kits disponibles en esta categoría actualmente.</div>';
        return;
    }

    container.innerHTML = filtered.map(kit => {
        const isSelected = selectedKits.includes(kit.id);
        const availableStock = kit.quantity;
        const inStock = kit.inStock !== false && availableStock > 0;
        const chosenQuantity = cardQuantity('kit', kit.id);
        const inCartCount = getKitCartCount(kit.id);

        let kitActionHtml = '';
        const isReservado = kit.temporalmenteReservado === true;

        if (!inStock) {
            if (isReservado) {
                kitActionHtml = `<button disabled class="w-full py-2.5 rounded-xl text-xs font-bold bg-sky-950 text-sky-400 border border-sky-800 cursor-not-allowed">Reservado</button>`;
            } else {
                kitActionHtml = `<button disabled class="w-full py-2.5 rounded-xl text-xs font-bold bg-stone-900 text-stone-600 cursor-not-allowed">Agotado</button>`;
            }
        } else if (inCartCount > 0) {
            kitActionHtml = `
                <div class="flex items-center gap-1.5 w-full">
                    <div class="flex items-center rounded-lg border border-stone-700 bg-stone-900 flex-1 justify-between">
                        <button type="button" onclick="changeCardQuantity('kit','${kit.id}',-1)" class="px-3 py-1.5 text-isivi-gold font-bold text-sm">-</button>
                        <span class="text-xs font-bold text-white">${inCartCount}</span>
                        <button type="button" onclick="changeCardQuantity('kit','${kit.id}',1)" ${inCartCount >= kit.quantity ? 'disabled' : ''} class="px-3 py-1.5 text-isivi-gold font-bold text-sm disabled:text-stone-600">+</button>
                    </div>
                    <button onclick="addItemToCart('kit','${kit.id}')" class="px-3 py-2.5 rounded-xl text-xs font-bold bg-rose-600 text-white hover:bg-rose-700 transition shadow flex items-center gap-1" title="Quitar del carrito">
                        <i class="fa-solid fa-trash-can"></i> <span>Quitar</span>
                    </button>
                </div>
            `;
        } else {
            kitActionHtml = `
                <button onclick="addItemToCart('kit','${kit.id}')" class="w-full py-2.5 rounded-xl text-xs font-bold bg-gradient-to-r from-isivi-500 to-isivi-600 text-white hover:from-isivi-600 hover:to-isivi-700 transition flex items-center justify-center gap-1.5 shadow" aria-label="Agregar ${kit.name} al carrito">
                    <i class="fa-solid fa-cart-plus mr-0.5"></i> <span>Agregar</span>
                </button>
            `;
        }

        let badgeHtml = '';
        if (inCartCount === 0) {
            if (availableStock > 0) {
                if (isReservado) {
                    badgeHtml = `<span class="absolute top-8 right-3 bg-amber-950 text-amber-400 border border-amber-800 text-[9px] font-bold px-2 py-0.5 rounded-full z-10 flex items-center gap-1"><i class="fa-solid fa-clock"></i> RESERVADO PARCIALMENTE</span>`;
                }
            } else {
                if (isReservado) {
                    badgeHtml = `<span class="absolute top-8 right-3 bg-sky-950 text-sky-400 border border-sky-800 text-[9px] font-bold px-2 py-0.5 rounded-full z-10 flex items-center gap-1"><i class="fa-solid fa-clock"></i> RESERVADO TEMPORALMENTE</span>`;
                } else {
                    badgeHtml = `<span class="absolute top-8 right-3 bg-red-950 text-red-400 border border-red-800 text-[9px] font-bold px-2 py-0.5 rounded-full z-10">AGOTADO</span>`;
                }
            }
        }


        return `
            <div class="snap-start isivi-card-premium rounded-2xl p-4 border-2 ${isSelected ? 'border-isivi-gold ring-1 ring-isivi-gold' : 'border-isivi-gold/60'} shadow-xl flex flex-col justify-between relative overflow-hidden" style="height: var(--catalog-card-height); width: var(--catalog-card-width-base);">
                <span class="absolute top-3 left-3 bg-isivi-gold text-isivi-black text-[9px] font-bold px-2.5 py-0.5 rounded-full uppercase z-10 shadow">Kit Especial</span>
                ${badgeHtml}
                <div class="relative overflow-hidden rounded-xl bg-stone-900 flex-shrink-0" style="height: var(--catalog-card-image-height);">
                    <img src="${kit.img}" alt="${kit.name}" class="w-full h-full object-cover transition-transform duration-700 hover:scale-105 ${!inStock ? 'opacity-40 grayscale' : ''}" onerror="this.onerror=null; this.src='/images/isivi-logo-transparent.png'; this.classList.add('p-4','bg-stone-900','object-contain');">
                </div>
                <div class="flex-1 flex flex-col justify-between min-h-0 overflow-hidden mt-2">
                    <div class="min-h-0 overflow-hidden">
                        <h4 class="font-bold text-isivi-gold text-xs mb-1 truncate">${kit.name}</h4>
                        <p class="text-isivi-300 text-[10px] leading-snug overflow-hidden text-ellipsis" style="display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical;">${kit.desc || ''}</p>
                    </div>
                    <div class="pt-2 border-t border-stone-900 flex flex-col gap-1.5 flex-shrink-0">
                        <div class="flex flex-col">
                            <span class="text-[8px] text-stone-400 uppercase tracking-wider font-semibold block leading-none">Precio Total Kit</span>
                            <span class="font-extrabold text-isivi-gold text-[13px] block leading-tight mt-0.5">$${kit.price.toLocaleString('es-CO')}</span>
                        </div>
                        <div class="w-full">
                            ${kitActionHtml}
                        </div>
                    </div>
                </div>
            </div>
        `;
    }).join('');
}

async function toggleKit(id) {
    const kit = kitsData.find(k => k.id === id);
    if (kit && kit.inStock === false) {
        showToast("Kit agotado", "error");
        return;
    }

    const isAdded = selectedKits.includes(id);
    const hold = getPendingHold();

    if (isAdded) {
        selectedKits = selectedKits.filter(kId => kId !== id);
        const prevQty = kitQuantities[id];
        delete kitQuantities[id];

        if (hold && hold.id) {
            try {
                const updated = await syncCartItemsWithHold(hold.id, hold.code, hold.phone);
                setPendingHold(updated);
                const hasServiceRemaining = selectedServices.length > 0;
                if (hasServiceRemaining) {
                    showToast('Producto eliminado. Tu cita continúa reservada.', 'info');
                } else {
                    showToast("Kit removido");
                }
                await refreshCatalogStock();
            } catch (err) {
                selectedKits.push(id);
                kitQuantities[id] = prevQty;
                showToast(err.message || 'No se pudo remover el kit del hold.', 'error');
                return;
            }
        } else {
            showToast("Kit removido");
        }
    } else {
        selectedKits.push(id);
        kitQuantities[id] = 1;

        if (hold && hold.id) {
            try {
                const updated = await syncCartItemsWithHold(hold.id, hold.code, hold.phone);
                setPendingHold(updated);
                showToast("Kit agregado a tu pedido");
                await refreshCatalogStock();
            } catch (err) {
                selectedKits = selectedKits.filter(kId => kId !== id);
                delete kitQuantities[id];
                showToast(err.message || 'No hay stock suficiente para agregar este kit.', 'error');
                return;
            }
        } else {
            showToast("Kit agregado a tu pedido");
        }
    }
    renderKitsGrid();
    updateSummary();
}

// ---------- Calendario y Horarios ----------
function formatDateKey(dateObj) {
    const y = dateObj.getFullYear();
    const m = String(dateObj.getMonth() + 1).padStart(2, '0');
    const d = String(dateObj.getDate()).padStart(2, '0');
    return `${y}-${m}-${d}`;
}

function getBogotaNow() {
    const now = new Date();
    const formatter = new Intl.DateTimeFormat('en-CA', {
        timeZone: 'America/Bogota',
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit',
        hour12: false
    });
    const formatted = formatter.format(now);
    const parts = formatted.replace(',', '').trim().split(' ');
    const dateStr = parts[0];
    const timeParts = (parts[1] || '00:00:00').split(':');
    const hours = parseInt(timeParts[0], 10);
    const minutes = parseInt(timeParts[1], 10);
    return {
        dateStr: dateStr,
        hours: hours,
        minutes: minutes,
        totalMinutes: hours * 60 + minutes
    };
}

function parseTimeSlotToMinutes(slotStr) {
    if (!slotStr || typeof slotStr !== 'string') return null;
    const match = slotStr.trim().match(/^(\d{1,2}):(\d{2})\s*(AM|PM)$/i);
    if (!match) return null;
    let h = parseInt(match[1], 10);
    const m = parseInt(match[2], 10);
    const period = match[3].toUpperCase();
    if (period === 'AM') {
        if (h === 12) h = 0;
    } else if (period === 'PM') {
        if (h !== 12) h += 12;
    }
    return h * 60 + m;
}

function isPastTimeSlot(dateStr, slotStr) {
    if (!dateStr || !slotStr) return false;
    const bogota = getBogotaNow();
    if (dateStr < bogota.dateStr) {
        return true;
    }
    if (dateStr > bogota.dateStr) {
        return false;
    }
    const slotMinutes = parseTimeSlotToMinutes(slotStr);
    if (slotMinutes === null) return false;
    return slotMinutes <= bogota.totalMinutes;
}

function changeMonth(delta) {
    currentDate.setMonth(currentDate.getMonth() + delta);
    renderCalendar();
}

function renderCalendar() {
    renderCartCalendar();

    const title = document.getElementById('calendar-month-year');
    const container = document.getElementById('calendar-days-grid');
    if (!title || !container) return;

    const year = currentDate.getFullYear();
    const month = currentDate.getMonth();

    const monthNames = ["Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"];
    title.textContent = `${monthNames[month]} ${year}`;

    const firstDayIndex = new Date(year, month, 1).getDay();
    const totalDays = new Date(year, month + 1, 0).getDate();
    const todayStr = formatDateKey(new Date());

    container.innerHTML = '';

    for (let i = 0; i < firstDayIndex; i++) {
        container.innerHTML += `<div class="p-2"></div>`;
    }

    for (let day = 1; day <= totalDays; day++) {
        const dayDate = new Date(year, month, day);
        const dayKey = formatDateKey(dayDate);
        const isPast = dayDate < new Date().setHours(0, 0, 0, 0);

        // Comprobación de excepciones de fecha
        const dayException = (agendaConfiguration.excepciones || []).find(ex => ex.fecha === dayKey);
        const isExplicitlyClosed = dayException && dayException.tipo === 'CERRADO';
        const hasSpecialHours = dayException && dayException.tipo === 'HORARIO_ESPECIAL';
        const isNonWorkingDay = isExplicitlyClosed || (!hasSpecialHours && !agendaConfiguration.diasLaborales.includes(dayDate.getDay()));
        const isSelected = dayKey === selectedDateStr;

        let btnClass = "p-2.5 rounded-xl text-center transition font-semibold text-xs flex flex-col items-center justify-center relative ";

        if (isPast || isNonWorkingDay) {
            btnClass += "text-stone-700 bg-stone-900/40 cursor-not-allowed";
        } else if (isSelected) {
            btnClass += "bg-isivi-gold text-isivi-black font-bold scale-105 shadow-lg";
        } else {
            btnClass += "bg-stone-900 border border-stone-800 hover:border-isivi-gold text-white";
        }

        container.innerHTML += `
            <button onclick="selectDate('${dayKey}')" ${isPast || isNonWorkingDay ? 'disabled' : ''} class="${btnClass}">
                <span>${day}</span>
                ${dayKey === todayStr ? '<span class="w-1 h-1 bg-isivi-gold rounded-full mt-0.5"></span>' : ''}
                ${isNonWorkingDay ? '<span class="text-[8px] text-stone-600">Cerrado</span>' : (hasSpecialHours ? '<span class="text-[8px] text-emerald-400">Especial</span>' : '')}
            </button>
        `;
    }
}

function selectDate(dateKey) {
    selectedDateStr = dateKey;
    trackEvent('select_date', { selected_date: dateKey });
    renderCalendar();
    renderTimeSlots();
    updateSummary();
    updateBookingProgressTracker();
    updateMobileContextBar();

    const [y, m, d] = dateKey.split('-');
    const label = document.getElementById('selected-date-label');
    if (label) label.textContent = `${d}/${m}/${y}`;
    showToast(`✓ Fecha seleccionada: ${d}/${m}/${y}`, 'info');
}

async function renderTimeSlots() {
    renderCartSchedule();

    const container = document.getElementById('time-slots-container');
    if (!container) return;

    let occupiedTimes = [];
    let loadFailed = false;
    try {
        occupiedTimes = await apiGet(`/reservas/disponibilidad?fecha=${encodeURIComponent(selectedDateStr)}`);
    } catch (err) {
        console.warn('No se pudo actualizar la disponibilidad de agenda', err);
        loadFailed = true;
    }

    if (loadFailed && (!occupiedTimes || occupiedTimes.length === 0)) {
        container.innerHTML = `
            <div class="col-span-full py-3 px-4 rounded-xl bg-red-950/40 border border-red-500/30 text-center text-xs text-red-200 flex flex-col sm:flex-row items-center justify-center gap-2">
                <span>No pudimos conectar con la agenda en tiempo real.</span>
                <button type="button" onclick="renderTimeSlots()" class="px-2.5 py-1 rounded-lg bg-stone-900 border border-isivi-gold text-isivi-gold font-bold hover:bg-isivi-gold hover:text-isivi-black transition">Reintentar</button>
            </div>
        `;
        return;
    }

    // Verificar si para la fecha seleccionada hay horario especial
    let currentSlots = appointmentTimeSlots;
    const dayException = (agendaConfiguration.excepciones || []).find(ex => ex.fecha === selectedDateStr);
    if (dayException && dayException.tipo === 'HORARIO_ESPECIAL' && Array.isArray(dayException.horarios) && dayException.horarios.length > 0) {
        currentSlots = dayException.horarios;
    }

    // Ocultar horarios pasados del día actual
    const validSlots = currentSlots.filter(slot => !isPastTimeSlot(selectedDateStr, slot));

    if (validSlots.length === 0) {
        selectedTimeSlot = '';
        updateSummary();
        updateBookingProgressTracker();
        updateMobileContextBar();
        container.innerHTML = '<span class="col-span-full py-4 text-center text-xs text-isivi-300">No quedan horarios disponibles para hoy.</span>';
        return;
    }

    // Si el turno seleccionado ya pasó o está ocupado (y no es el propio turno retenido), seleccionar el primer turno válido disponible
    const activeHold = getPendingHold();
    const isOwnHoldOnDate = activeHold && activeHold.date === selectedDateStr;

    if (selectedTimeSlot && (!validSlots.includes(selectedTimeSlot) || (occupiedTimes.includes(selectedTimeSlot) && (!isOwnHoldOnDate || activeHold.time !== selectedTimeSlot)))) {
        const firstAvailable = validSlots.find(s => !occupiedTimes.includes(s) || (isOwnHoldOnDate && activeHold.time === s));
        selectedTimeSlot = firstAvailable || '';
        updateSummary();
        updateBookingProgressTracker();
        updateMobileContextBar();
    } else if (!selectedTimeSlot && validSlots.length > 0) {
        const firstAvailable = isOwnHoldOnDate && validSlots.includes(activeHold.time) ? activeHold.time : validSlots.find(s => !occupiedTimes.includes(s));
        if (firstAvailable) {
            selectedTimeSlot = firstAvailable;
            updateSummary();
            updateBookingProgressTracker();
            updateMobileContextBar();
        }
    }

    container.innerHTML = validSlots.map(slot => {
        const isOwnHoldSlot = isOwnHoldOnDate && activeHold.time === slot;
        const isOccupied = occupiedTimes.includes(slot) && !isOwnHoldSlot;
        const isSelected = (selectedTimeSlot === slot && !isOccupied) || isOwnHoldSlot;

        if (isOwnHoldSlot) {
            return `
                <button type="button" onclick="selectTimeSlot('${slot}')" class="p-2.5 rounded-xl text-xs font-bold transition flex items-center justify-center gap-1.5 bg-amber-950/90 text-amber-300 border border-amber-500/80 shadow-md scale-105" title="Horario reservado por ti">
                    <i class="fa-solid fa-clock-rotate-left text-amber-400"></i> ${slot} (Tu reserva)
                </button>
            `;
        }

        if (isOccupied) {
            return `
                <div class="bg-stone-900/60 text-stone-600 border border-stone-800 p-2.5 rounded-xl text-xs font-medium text-center cursor-not-allowed flex items-center justify-center gap-1.5" title="Horario reservado por otro cliente">
                    <i class="fa-solid fa-lock text-[10px]"></i> ${slot} (Ocupado)
                </div>
            `;
        }

        return `
            <button type="button" onclick="selectTimeSlot('${slot}')" class="p-2.5 rounded-xl text-xs font-bold transition flex items-center justify-center gap-1.5 ${isSelected ? 'bg-isivi-gold text-isivi-black shadow-md scale-105' : 'bg-stone-900 border border-stone-800 hover:border-isivi-500 text-white'}">
                <i class="fa-regular fa-clock"></i> ${slot}
            </button>
        `;
    }).join('');
}


async function selectTimeSlot(slot) {
    const activeHold = getPendingHold();
    if (activeHold && (activeHold.time !== slot || activeHold.date !== selectedDateStr)) {
        await releaseHoldImmediately(activeHold);
    }
    selectedTimeSlot = slot;
    trackEvent('select_time', { time_slot: slot });
    renderTimeSlots();
    updateSummary();
    updateBookingProgressTracker();
    updateMobileContextBar();
    showToast(`✓ Hora seleccionada: ${slot}`, 'info');
}


function ensureSelectedDateIsWorking() {
    let candidate = new Date(`${selectedDateStr}T12:00:00`);
    for (let attempts = 0; attempts < 14; attempts++) {
        const candidateKey = formatDateKey(candidate);
        const dayException = (agendaConfiguration.excepciones || []).find(ex => ex.fecha === candidateKey);
        const isExplicitlyClosed = dayException && dayException.tipo === 'CERRADO';
        const hasSpecialHours = dayException && dayException.tipo === 'HORARIO_ESPECIAL';
        const isWorking = hasSpecialHours || (!isExplicitlyClosed && agendaConfiguration.diasLaborales.includes(candidate.getDay()));
        if (isWorking) {
            selectedDateStr = candidateKey;
            return;
        }
        candidate.setDate(candidate.getDate() + 1);
    }
    selectedDateStr = formatDateKey(candidate);
}

function agendaTimeInMinutes(value) {
    const match = value.match(/(\d{1,2}):(\d{2})\s*(AM|PM)/i);
    if (!match) return 0;
    let hours = Number(match[1]) % 12;
    if (match[3].toUpperCase() === 'PM') hours += 12;
    return hours * 60 + Number(match[2]);
}

function renderAgendaSettings() {
    const daysContainer = document.getElementById('admin-working-days');
    const hoursContainer = document.getElementById('admin-agenda-hours');
    const dateInput = document.getElementById('admin-agenda-config-date');
    const dateLabel = document.getElementById('admin-agenda-config-date-label');
    if (!daysContainer || !hoursContainer) return;

    if (dateInput && !dateInput.value) dateInput.value = formatDateKey(new Date());
    if (dateInput && dateLabel) {
        const [year, month, day] = dateInput.value.split('-');
        dateLabel.textContent = `${day}/${month}/${year}`;
    }

    const days = ['Domingo', 'Lunes', 'Martes', 'Miércoles', 'Jueves', 'Viernes', 'Sábado'];
    daysContainer.innerHTML = days.map((day, index) => `<label class="cursor-pointer rounded-xl border ${agendaConfiguration.diasLaborales.includes(index) ? 'border-isivi-gold bg-isivi-500/20 text-isivi-gold' : 'border-stone-700 bg-stone-950 text-isivi-300'} px-3 py-2 text-xs font-bold"><input class="mr-1.5 accent-yellow-500" type="checkbox" value="${index}" ${agendaConfiguration.diasLaborales.includes(index) ? 'checked' : ''}>${day}</label>`).join('');
    hoursContainer.innerHTML = appointmentTimeSlots.map(time => `<span class="rounded-xl border border-stone-700 bg-stone-950 px-3 py-2 text-xs font-bold text-white">${time}<button type="button" onclick="removeAgendaTime('${time}')" class="ml-2 text-red-400 hover:text-red-300" title="Eliminar horario"><i class="fa-solid fa-xmark"></i></button></span>`).join('');
}

function selectAdminAgendaDate() {
    const configDate = document.getElementById('admin-agenda-config-date')?.value;
    const scheduleDate = document.getElementById('admin-schedule-date');
    if (!configDate || !scheduleDate) return;
    scheduleDate.value = configDate;
    renderAgendaSettings();
    renderAdminScheduleManager();
    renderAdminWeeklyCalendar();
}

function selectAdminScheduleDate() {
    const scheduleDate = document.getElementById('admin-schedule-date')?.value;
    const configDate = document.getElementById('admin-agenda-config-date');
    if (scheduleDate && configDate) configDate.value = scheduleDate;
    renderAgendaSettings();
    renderAdminScheduleManager();
    renderAdminWeeklyCalendar();
}

function addAgendaTime() {
    const hour = document.getElementById('admin-agenda-hour')?.value;
    const minutes = document.getElementById('admin-agenda-minutes')?.value;
    const period = document.getElementById('admin-agenda-period')?.value;
    if (!hour || !minutes || !period) return;
    const time = `${hour}:${minutes} ${period}`;
    if (appointmentTimeSlots.includes(time)) {
        showToast('Ese horario ya existe', 'error');
        return;
    }
    appointmentTimeSlots.push(time);
    appointmentTimeSlots.sort((a, b) => agendaTimeInMinutes(a) - agendaTimeInMinutes(b));
    renderAgendaSettings();
}

function removeAgendaTime(time) {
    if (appointmentTimeSlots.length === 1) {
        showToast('La agenda debe tener al menos un horario', 'error');
        return;
    }
    appointmentTimeSlots = appointmentTimeSlots.filter(slot => slot !== time);
    renderAgendaSettings();
}

async function saveAgendaSettings() {
    const selectedDays = [...document.querySelectorAll('#admin-working-days input:checked')].map(input => Number(input.value));
    if (selectedDays.length === 0) {
        showToast('Selecciona al menos un día laboral', 'error');
        return;
    }
    try {
        agendaConfiguration = await apiPut('/agenda', { diasLaborales: selectedDays, horarios: appointmentTimeSlots });
        appointmentTimeSlots = agendaConfiguration.horarios || appointmentTimeSlots;
        ensureSelectedDateIsWorking();
        if (!appointmentTimeSlots.includes(selectedTimeSlot)) selectedTimeSlot = appointmentTimeSlots[0];
        renderAgendaSettings();
        renderAdminScheduleManager();
        renderCalendar();
        renderTimeSlots();
        updateSummary();
        showToast('Configuración de agenda guardada', 'success');
    } catch (err) {
        console.error(err);
        showToast('No se pudo guardar la agenda', 'error');
    }
}

// ---------- Resumen / Reserva ----------
function updateSummary() {
    const itemsListContainer = document.getElementById('summary-items-list');
    let itemsHTML = '';
    let subtotal = 0;
    let serviceSubtotal = 0;
    let productSubtotal = 0;

    const cartItems = getCartItems();
    cartItems.forEach(item => {
        const itemTotal = item.price * item.quantity;
        subtotal += itemTotal;
        if (item.type === 'service') {
            serviceSubtotal += itemTotal;
            itemsHTML += `
                <div class="flex items-center justify-between text-xs py-1.5 border-b border-stone-900">
                    <span class="text-white"><i class="fa-solid fa-scissors text-isivi-gold mr-1.5"></i> ${item.name}</span>
                    <span class="font-semibold text-isivi-gold">$${item.price.toLocaleString('es-CO')}</span>
                </div>
            `;
        } else if (item.type === 'product') {
            productSubtotal += itemTotal;
            itemsHTML += `
                <div class="flex items-center justify-between text-xs py-1.5 border-b border-stone-900">
                    <span class="text-white"><i class="fa-solid fa-bottle-droplet text-isivi-gold mr-1.5"></i> ${item.name} x${item.quantity}</span>
                    <span class="font-semibold text-isivi-gold">$${itemTotal.toLocaleString('es-CO')}</span>
                </div>
            `;
        } else if (item.type === 'kit') {
            productSubtotal += itemTotal;
            itemsHTML += `
                <div class="flex items-center justify-between text-xs py-1.5 border-b border-stone-900">
                    <span class="text-white"><i class="fa-solid fa-box-open text-isivi-gold mr-1.5"></i> ${item.name} x${item.quantity}</span>
                    <span class="font-semibold text-isivi-gold">$${itemTotal.toLocaleString('es-CO')}</span>
                </div>
            `;
        }
    });

    const totalItemCount = getCartTotalUnits();

    if (itemsListContainer) {
        if (totalItemCount === 0) {
            itemsListContainer.innerHTML = `<div class="text-isivi-300 text-xs italic text-center py-4">No has seleccionado servicios ni productos aún.</div>`;
        } else {
            itemsListContainer.innerHTML = itemsHTML;
        }
    }

    const hasServices = selectedServices.length > 0;
    const isProductOrder = checkoutMode === 'products' || (!hasServices && (selectedProducts.length > 0 || selectedKits.length > 0));
    const serviceDeposit = Math.round(serviceSubtotal * 0.25);
    const deposit = isProductOrder ? 0 : serviceDeposit;
    const remaining = subtotal - deposit;
    const hasOnlyProducts = isProductOrder;

    const subtotalEl = document.getElementById('summary-subtotal');
    const remainingEl = document.getElementById('summary-remaining');
    const depositEl = document.getElementById('summary-deposit');
    if (subtotalEl) subtotalEl.textContent = `$${subtotal.toLocaleString('es-CO')}`;
    if (remainingEl) remainingEl.textContent = `$${remaining.toLocaleString('es-CO')}`;
    if (depositEl) depositEl.textContent = `$${deposit.toLocaleString('es-CO')}`;

    const schedulePanel = document.getElementById('booking-schedule-panel');
    const summaryPanel = document.getElementById('booking-summary-panel');
    const datetimeCard = document.getElementById('summary-datetime-card');
    if (schedulePanel && summaryPanel && datetimeCard) {
        schedulePanel.classList.toggle('hidden', hasOnlyProducts);
        summaryPanel.classList.toggle('lg:col-span-5', !hasOnlyProducts);
        summaryPanel.classList.toggle('lg:col-span-12', hasOnlyProducts);
        summaryPanel.classList.toggle('max-w-5xl', hasOnlyProducts);
        summaryPanel.classList.toggle('mx-auto', hasOnlyProducts);
        datetimeCard.classList.toggle('hidden', hasOnlyProducts);
        document.getElementById('reservation-heading').textContent = hasOnlyProducts ? 'Finaliza tu compra' : 'Calendario de Disponibilidad Real';
        const reservationDescription = document.querySelector('#reservation-intro p');
        if (reservationDescription) reservationDescription.textContent = hasOnlyProducts ? 'Tu pedido no requiere agendar fecha ni horario.' : 'Selecciona tu fecha y turno. Se calculará automáticamente el 25% de anticipo.';
        const checkoutTitle = document.getElementById('checkout-title');
        const totalsCard = document.getElementById('checkout-totals-card');
        const actionButton = document.getElementById('checkout-action-button');
        if (checkoutTitle) checkoutTitle.textContent = hasOnlyProducts ? 'Tu pedido' : 'Resumen de Reserva';
        if (totalsCard) {
            const rows = totalsCard.querySelectorAll(':scope > div');
            if (rows[0]) rows[0].firstElementChild.textContent = hasOnlyProducts ? 'Total de productos:' : 'Valor total de la solicitud:';
            if (rows[1]) rows[1].classList.toggle('hidden', hasOnlyProducts);
            if (rows[2]) rows[2].classList.toggle('hidden', hasOnlyProducts);
            if (rows[2] && !hasOnlyProducts) {
                rows[2].querySelector('span').textContent = 'Pago requerido ahora:';
                rows[2].querySelector('span + span').textContent = productSubtotal > 0 ? '25% de servicios + productos completos' : 'Anticipo del 25% para reservar tu turno';
            }
        }
        if (actionButton) {
            actionButton.innerHTML = hasOnlyProducts ? '<i class="fa-brands fa-whatsapp text-base"></i><span>Solicitar pedido por WhatsApp</span>' : '<i class="fa-solid fa-shield-halved text-xs"></i><span>Pagar Anticipo (25%) &amp; Confirmar</span>';
            const help = actionButton.nextElementSibling;
            if (help) help.textContent = hasOnlyProducts ? 'Te contactaremos para confirmar el pedido y el pago.' : 'Redirección directa a WhatsApp con comprobante';
        }
    }

    const [y, m, d] = selectedDateStr.split('-');
    const displayEl = document.getElementById('summary-datetime-display');
    if (displayEl) displayEl.textContent = `${d}/${m}/${y} - ${selectedTimeSlot}`;
}

function getCartItems() {
    return [
        ...selectedServices.map(id => {
            const item = servicesData.find(service => service.id === id);
            return item ? { id, cartKey: id, type: 'service', label: 'Servicio', name: item.name, price: item.price, quantity: 1, image: item.img, isLoaded: true, availableStock: null } : null;
        }),
        ...selectedProducts.map(cartKey => {
            const [id, varId] = cartKey.split('_');
            const item = productsData.find(product => product.id === id);
            const isLoaded = Boolean(item);
            let variantObj = null;
            let price = item ? item.price : 0;
            let name = item ? item.name : 'Producto en catálogo';
            let availableStock = isLoaded ? (item.inStock !== false ? (Number.isInteger(item.quantity) ? item.quantity : 0) : 0) : null;

            if (item && item.priceType === 'VARIANTES' && Array.isArray(item.variants)) {
                variantObj = item.variants.find(v => v.id === varId) || (item.variants.length === 1 ? item.variants[0] : null);
                if (variantObj) {
                    price = Number(variantObj.precio) || price;
                    name = `${item.name} (${variantObj.nombre})`;
                    availableStock = (variantObj.enStock !== false && variantObj.activo !== false) ? (Number.isInteger(variantObj.cantidad) ? variantObj.cantidad : 0) : 0;
                }
            }

            return {
                id,
                cartKey,
                variantId: variantObj ? variantObj.id : null,
                variantName: variantObj ? variantObj.nombre : null,
                type: 'product',
                label: variantObj ? `Producto · ${variantObj.nombre}` : 'Producto',
                name,
                price,
                quantity: Number(productQuantities[cartKey]) || 1,
                image: item ? item.img : '/images/isivi-logo-transparent.png',
                isLoaded,
                availableStock
            };
        }),
        ...selectedKits.map(id => {
            const item = kitsData.find(kit => kit.id === id);
            const isLoaded = Boolean(item);
            const availableStock = isLoaded ? (item.inStock !== false ? (Number.isInteger(item.quantity) ? item.quantity : 0) : 0) : null;
            return {
                id,
                cartKey: id,
                type: 'kit',
                label: 'Kit especial',
                name: item ? item.name : 'Kit en catálogo',
                price: item ? item.price : 0,
                quantity: Number(kitQuantities[id]) || 1,
                image: item ? item.img : '/images/isivi-logo-transparent.png',
                isLoaded,
                availableStock
            };
        })
    ].filter(Boolean);
}

// removeCartItem duplicate removed

function getCartTotalUnits() {
    const items = getCartItems();
    return items.reduce((sum, item) => sum + (item.quantity || 1), 0);
}

function getCartTotalAmount() {
    const items = getCartItems();
    return items.reduce((sum, item) => sum + item.price * (item.quantity || 1), 0);
}

function updateCartBadge() {
    const totalUnits = getCartTotalUnits();
    const badge = document.getElementById('nav-cart-badge');
    if (badge) {
        if (totalUnits > 0) {
            badge.textContent = totalUnits;
            badge.classList.remove('hidden');
            badge.classList.add('flex');
        } else {
            badge.textContent = '0';
            badge.classList.add('hidden');
            badge.classList.remove('flex');
        }
    }
}

function syncCartState() {
    const items = getCartItems();
    let showAdjustedToast = false;
    let showRemovedToast = false;
    let lastAdjustedQty = 0;

    for (const item of items) {
        if (item.type === 'product' || item.type === 'kit') {
            const quantities = item.type === 'product' ? productQuantities : kitQuantities;
            if (item.availableStock !== null) {
                if (item.availableStock <= 0) {
                    if (item.type === 'product') {
                        selectedProducts = selectedProducts.filter(k => k !== item.cartKey);
                        delete productQuantities[item.cartKey];
                    } else {
                        selectedKits = selectedKits.filter(id => id !== item.id);
                        delete kitQuantities[item.id];
                    }
                    showRemovedToast = true;
                } else if (item.quantity > item.availableStock) {
                    quantities[item.cartKey] = item.availableStock;
                    lastAdjustedQty = item.availableStock;
                    showAdjustedToast = true;
                }
            }
        }
    }

    if (showRemovedToast) {
        showToast('El producto se agotó y fue retirado de tu carrito.', 'error');
    }
    if (showAdjustedToast) {
        showToast(`El stock disponible cambió. Ajustamos tu cantidad a ${lastAdjustedQty} unidades.`, 'info');
    }

    updateSummary();
    updateCartBadge();
    renderCartDrawer();
    updateDeliveryOptions();
    updateBookingProgressTracker();
    updateMobileContextBar();
}

async function refreshCatalogStock() {
    try {
        apiCache.delete('/productos');
        apiCache.delete('/kits');
        
        const [prodList, kitList] = await Promise.all([
            apiGet('/productos'),
            apiGet('/kits')
        ]);
        
        if (Array.isArray(prodList)) {
            productsData = prodList.map(mapFromApiProducto);
        }
        if (Array.isArray(kitList)) {
            kitsData = kitList.map(mapFromApiKit);
        }
        
        renderProductsGrid();
        renderKitsGrid();
        syncCartState();
    } catch (err) {
        console.error('Error al refrescar stock del catálogo:', err);
    }
}

function renderCartDrawer() {
    const container = document.getElementById('cart-items-list');
    if (!container) return;

    const items = getCartItems();
    const total = getCartTotalAmount();
    const hasServices = items.some(item => item.type === 'service');
    const hasProducts = items.some(item => item.type === 'product' || item.type === 'kit');
    const paymentToday = hasServices ? Math.round(total * 0.25) : total;
    document.getElementById('cart-item-count').textContent = `${items.length} ${items.length === 1 ? 'artículo' : 'artículos'}`;
    document.getElementById('cart-total').textContent = `$${paymentToday.toLocaleString('es-CO')}`;
    document.getElementById('cart-total-label').textContent = hasServices ? 'Pago requerido hoy (25%)' : 'Total';
    document.getElementById('cart-help').textContent = items.some(item => item.type === 'service')
        ? 'Completa tus datos, agenda tu cita y asegura tu turno con el 25% de anticipo.'
        : items.length ? 'Completa tus datos y envía tu pedido desde aquí.' : 'Añade productos, kits o servicios para continuar.';

    container.innerHTML = items.length ? items.map(item => {
        const isProductOrKit = item.type !== 'service';
        let stockBadge = '';
        let canIncrement = false;

        if (isProductOrKit) {
            const hold = getPendingHold();
            const hasHold = hold && hold.id;

            if (!item.isLoaded) {
                stockBadge = '<span class="text-[10px] text-amber-400 font-medium block mt-0.5"><i class="fa-solid fa-spinner fa-spin mr-1"></i>Verificando disponibilidad...</span>';
                canIncrement = false;
            } else if (hasHold && item.availableStock === 0) {
                stockBadge = '<span class="text-[10px] text-emerald-400 font-bold block mt-0.5"><i class="fa-solid fa-clock mr-1"></i>Reservado temporalmente</span>';
                canIncrement = false;
            } else if (item.availableStock === 0) {
                stockBadge = '<span class="text-[10px] text-red-400 font-bold block mt-0.5"><i class="fa-solid fa-circle-exclamation mr-1"></i>Agotado</span>';
                canIncrement = false;
            } else if (item.quantity > item.availableStock) {
                stockBadge = `<span class="text-[10px] text-amber-400 font-bold block mt-0.5"><i class="fa-solid fa-triangle-exclamation mr-1"></i>Solo quedan ${item.availableStock} disp.</span>`;
                canIncrement = false;
            } else if (item.quantity === item.availableStock) {
                stockBadge = `<span class="text-[10px] text-stone-400 font-medium block mt-0.5">Máximo disponible</span>`;
                canIncrement = false;
            } else {
                canIncrement = !hasHold;
            }
        }

        return `
            <article class="flex gap-3 rounded-2xl border border-stone-800 bg-stone-950 p-3">
                <img src="${item.image}" alt="${item.name}" class="h-16 w-16 rounded-xl object-cover" onerror="this.style.visibility='hidden'">
                <div class="min-w-0 flex-1">
                    <span class="text-[10px] font-bold uppercase tracking-wide text-isivi-gold">${item.label}</span>
                    <h3 class="mt-0.5 truncate text-sm font-bold text-white">${item.name}</h3>
                    <p class="mt-0.5 text-xs text-isivi-300">$${item.price.toLocaleString('es-CO')} c/u</p>
                    ${stockBadge}
                    ${isProductOrKit ? `
                        <div class="mt-2 flex items-center gap-2">
                            <button type="button" onclick="changeCartQuantity('${item.type}','${item.cartKey}',-1)" class="rounded bg-stone-800 px-2 py-1 text-xs text-isivi-gold disabled:text-stone-600 transition hover:bg-stone-700" aria-label="Reducir cantidad">-</button>
                            <span class="text-xs font-bold text-white min-w-4 text-center">${item.quantity}</span>
                            <button type="button" onclick="changeCartQuantity('${item.type}','${item.cartKey}',1)" ${!canIncrement ? 'disabled' : ''} class="rounded bg-stone-800 px-2 py-1 text-xs text-isivi-gold disabled:text-stone-600 transition hover:bg-stone-700" aria-label="Aumentar cantidad">+</button>
                            <span class="ml-auto text-xs font-bold text-isivi-gold">$${(item.price * item.quantity).toLocaleString('es-CO')}</span>
                        </div>
                    ` : ''}
                </div>
                <button type="button" onclick="removeCartItem('${item.type}', '${item.cartKey}')" class="self-start rounded-lg p-2 text-stone-400 transition hover:bg-red-950 hover:text-red-300" aria-label="Quitar ${item.name}"><i class="fa-solid fa-trash"></i></button>
            </article>
        `;
    }).join('') : '<div class="flex h-full min-h-52 flex-col items-center justify-center rounded-2xl border border-dashed border-stone-700 px-6 text-center"><i class="fa-solid fa-bag-shopping mb-3 text-3xl text-stone-600"></i><p class="text-sm font-bold text-isivi-200">Tu carrito está vacío</p><p class="mt-1 text-xs text-isivi-300">Agrega lo que deseas comprar o agendar.</p></div>';

    document.getElementById('cart-checkout').classList.toggle('hidden', !items.length);
    document.getElementById('cart-delivery-options').classList.toggle('hidden', !hasProducts);
    document.getElementById('cart-schedule-section').classList.toggle('hidden', !hasServices);
    document.getElementById('cart-payment-section').classList.toggle('hidden', !hasServices);

    const financialCard = document.getElementById('cart-service-financial-card');
    if (financialCard) {
        financialCard.classList.toggle('hidden', !hasServices);
        if (hasServices) {
            const serviceTotal = items.filter(i => i.type === 'service').reduce((sum, i) => sum + i.price, 0);
            const deposit = Math.round(serviceTotal * 0.25);
            const remaining = serviceTotal - deposit;
            const totalEl = document.getElementById('cart-service-total-val');
            const remEl = document.getElementById('cart-service-remaining-val');
            const depEl = document.getElementById('cart-service-deposit-val');
            if (totalEl) totalEl.textContent = `$${serviceTotal.toLocaleString('es-CO')}`;
            if (remEl) remEl.textContent = `$${remaining.toLocaleString('es-CO')}`;
            if (depEl) depEl.textContent = `$${deposit.toLocaleString('es-CO')}`;
        }
    }

    const stepCustNum = document.getElementById('cart-step-cust-num');
    const stepPayNum = document.getElementById('cart-step-pay-num');
    if (stepCustNum) stepCustNum.textContent = hasServices ? '2' : '1';
    if (stepPayNum) stepPayNum.textContent = hasServices ? '3' : '2';

    document.getElementById('cart-checkout-button').innerHTML = hasServices
        ? '<i class="fa-brands fa-whatsapp"></i><span>Enviar comprobante por WhatsApp</span>'
        : '<i class="fa-brands fa-whatsapp"></i><span>Solicitar pedido por WhatsApp</span>';
    if (hasServices) {
        renderCartSchedule();
        updateCartPaymentDetails();
    }
    renderPendingHoldUI();
}


function openCartDrawer() {
    copyCheckoutDataToCart();
    bindCartValidationListeners();
    clearCartFormErrors();
    const [year, month] = selectedDateStr.split('-').map(Number);
    cartCalendarDate = new Date(year, month - 1, 1);
    renderCartDrawer();
    document.getElementById('cart-overlay').classList.remove('hidden');
    document.getElementById('cart-drawer').classList.remove('hidden');
    document.body.classList.add('overflow-hidden');
    updateMobileContextBar();
}

function closeCartDrawer() {
    document.getElementById('cart-overlay').classList.add('hidden');
    document.getElementById('cart-drawer').classList.add('hidden');
    document.body.classList.remove('overflow-hidden');
    updateMobileContextBar();
}

async function removeCartItem(type, id) {
    if (type === 'service') {
        selectedServices = selectedServices.filter(itemId => itemId !== id);
        if (selectedServices.length === 0) {
            const hold = getPendingHold();
            if (hold && hold.id) {
                selectedTimeSlot = '';
                await releaseHoldImmediately(hold);
                showToast('Horario de cita liberado al no tener servicios en el carrito.', 'info');
                await renderTimeSlots();
                await renderCartSchedule();
            }
        }
    }
    if (type === 'product') { 
        selectedProducts = selectedProducts.filter(itemId => itemId !== id); 
        delete productQuantities[id]; 
    }
    if (type === 'kit') { 
        selectedKits = selectedKits.filter(itemId => itemId !== id); 
        delete kitQuantities[id]; 
    }

    const hold = getPendingHold();
    if (hold && hold.id) {
        const hasItems = selectedProducts.length > 0 || selectedKits.length > 0 || selectedServices.length > 0;
        if (!hasItems) {
            await cancelPendingHold();
        } else {
            try {
                const updated = await syncCartItemsWithHold(hold.id, hold.code, hold.phone);
                setPendingHold(updated);
                const hasServiceRemaining = selectedServices.length > 0;
                if ((type === 'product' || type === 'kit') && hasServiceRemaining) {
                    showToast('Producto eliminado. Tu cita continúa reservada.', 'info');
                } else {
                    showToast('Artículo liberado del pedido.', 'info');
                }
                await refreshCatalogStock();
            } catch (err) {
                console.warn('Error al sincronizar remoción con hold:', err);
            }
        }
    }

    trackEvent('remove_from_cart', { item_type: type, item_id: id });
    renderServicesGrid();
    renderProductsGrid();
    renderKitsGrid();
    syncCartState();
}


function copyCheckoutDataToCart() {
    const custName = document.getElementById('cust-name');
    const custPhone = document.getElementById('cust-phone');
    const custEmail = document.getElementById('cust-email');
    const custCity = document.getElementById('cust-city');
    const delivMethod = document.getElementById('delivery-method');
    const delivAddr = document.getElementById('delivery-address');

    const cartName = document.getElementById('cart-cust-name');
    const cartPhone = document.getElementById('cart-cust-phone');
    const cartEmail = document.getElementById('cart-cust-email');
    const cartCity = document.getElementById('cart-cust-city');
    const cartMethod = document.getElementById('cart-delivery-method');
    const cartAddr = document.getElementById('cart-delivery-address');
    const cartPay = document.getElementById('cart-payment-method');

    if (cartName && custName && custName.value) cartName.value = custName.value;
    if (cartPhone && custPhone && custPhone.value) cartPhone.value = custPhone.value;
    if (cartEmail && custEmail && custEmail.value) cartEmail.value = custEmail.value;
    if (cartCity && custCity && custCity.value) cartCity.value = custCity.value;
    if (cartMethod && delivMethod && delivMethod.value) cartMethod.value = delivMethod.value;
    if (cartAddr && delivAddr && delivAddr.value) cartAddr.value = delivAddr.value;
    if (cartPay) cartPay.value = currentPaymentMethod;
    toggleCartDeliveryAddress();
}

function copyCartDataToCheckout() {
    const cartName = document.getElementById('cart-cust-name');
    const cartPhone = document.getElementById('cart-cust-phone');
    const cartEmail = document.getElementById('cart-cust-email');
    const cartCity = document.getElementById('cart-cust-city');
    const cartMethod = document.getElementById('cart-delivery-method');
    const cartAddr = document.getElementById('cart-delivery-address');
    const cartPay = document.getElementById('cart-payment-method');

    const custName = document.getElementById('cust-name');
    const custPhone = document.getElementById('cust-phone');
    const custEmail = document.getElementById('cust-email');
    const custCity = document.getElementById('cust-city');
    const delivMethod = document.getElementById('delivery-method');
    const delivAddr = document.getElementById('delivery-address');

    if (custName && cartName) custName.value = cartName.value.trim();
    if (custPhone && cartPhone) custPhone.value = cartPhone.value.trim();
    if (custEmail && cartEmail) custEmail.value = cartEmail.value.trim();
    if (custCity && cartCity) custCity.value = cartCity.value.trim();
    if (delivMethod && cartMethod) delivMethod.value = cartMethod.value;
    if (delivAddr && cartAddr) delivAddr.value = cartAddr.value.trim();
    if (cartPay) currentPaymentMethod = cartPay.value;
}

function toggleCartDeliveryAddress() {
    const wrap = document.getElementById('cart-delivery-address-wrap');
    const address = document.getElementById('cart-delivery-address');
    const methodEl = document.getElementById('cart-delivery-method');
    const errorEl = document.getElementById('cart-delivery-address-error');
    if (!methodEl) return;
    const isDelivery = methodEl.value === 'delivery';
    if (wrap) {
        wrap.classList.toggle('hidden', !isDelivery);
    }
    if (address) {
        address.classList.remove('hidden');
        address.required = isDelivery;
        address.setAttribute('aria-required', isDelivery ? 'true' : 'false');
        if (!isDelivery) {
            address.value = '';
            address.classList.remove('border-red-500');
            if (errorEl) errorEl.classList.add('hidden');
        }
    }
}

function updateCartPaymentDetails() {
    const select = document.getElementById('cart-payment-method');
    if (!select) return;
    const data = transferOptions[select.value];
    updatePaymentMethodSelection(select.value);
    document.getElementById('cart-payment-details').innerHTML = `<div class="mt-2 rounded-xl border border-emerald-500/20 bg-stone-950 p-3"><div class="flex items-center gap-2"><i class="fa-solid ${data.icon} ${data.color} text-lg"></i><span class="font-bold text-isivi-gold">${data.title}</span></div><span class="mt-1 block text-[11px] text-isivi-300">Titular: ${data.holder}</span><div class="mt-2 flex items-center justify-between gap-2"><span class="text-base font-bold tracking-wider text-white">${data.number}</span><button type="button" onclick="copyToClipboard('${data.number}')" class="rounded-lg border border-isivi-gold bg-isivi-500/20 px-3 py-1.5 text-[10px] font-bold text-isivi-gold hover:bg-isivi-gold hover:text-isivi-black"><i class="fa-regular fa-copy mr-1"></i>Copiar</button></div></div>`;
}

function setCartPaymentMethod(method) {
    const select = document.getElementById('cart-payment-method');
    if (!select) return;
    select.value = method;
    currentPaymentMethod = method;
    trackEvent('select_payment_method', { method: method === 'wompi' ? 'WOMPI' : 'TRANSFER', transfer_channel: method });
    updateCartPaymentDetails();
}

function updatePaymentMethodSelection(method) {
    document.querySelectorAll('.payment-method-btn, .cart-payment-method-btn').forEach(button => {
        const isSelected = button.dataset.method === method || button.id === `pay-btn-${method}`;
        button.classList.toggle('border-2', isSelected);
        button.classList.toggle('border-isivi-gold', isSelected);
        button.classList.toggle('bg-isivi-500/15', isSelected);
        button.classList.toggle('ring-1', isSelected);
        button.classList.toggle('ring-isivi-gold/50', isSelected);
        button.classList.toggle('border-stone-700', !isSelected && button.classList.contains('cart-payment-method-btn'));
        button.classList.toggle('border-stone-800', !isSelected && button.classList.contains('payment-method-btn'));
        button.setAttribute('aria-pressed', String(isSelected));
    });
}

async function renderCartSchedule() {
    const section = document.getElementById('cart-schedule-section');
    if (!section || section.classList.contains('hidden')) return;
    renderCartCalendar();
    const [year, month, day] = selectedDateStr.split('-');
    document.getElementById('cart-selected-date-label').textContent = `${day}/${month}/${year}`;
    const container = document.getElementById('cart-time-slots');
    container.innerHTML = '<span class="col-span-full text-center text-[11px] text-isivi-300">Cargando horarios...</span>';
    try {
        const occupiedTimes = await apiGet(`/reservas/disponibilidad?fecha=${encodeURIComponent(selectedDateStr)}`);
        const validSlots = appointmentTimeSlots.filter(slot => !isPastTimeSlot(selectedDateStr, slot));
        if (validSlots.length === 0) {
            container.innerHTML = '<span class="col-span-full py-2 text-center text-[11px] text-isivi-300">No quedan horarios disponibles para hoy.</span>';
            return;
        }
        const activeHold = getPendingHold();
        const isOwnHoldOnDate = activeHold && activeHold.date === selectedDateStr;
        container.innerHTML = validSlots.map(slot => {
            const isOwnHoldSlot = isOwnHoldOnDate && activeHold.time === slot;
            const occupied = occupiedTimes.includes(slot) && !isOwnHoldSlot;
            if (isOwnHoldSlot) {
                return `<button type="button" onclick="selectCartAppointmentTime('${slot}')" class="rounded-xl border border-amber-500/80 bg-amber-950/90 text-amber-300 px-2 py-2 text-[10px] font-bold shadow">${slot}<small class="mt-0.5 block font-semibold text-amber-400">Tu reserva</small></button>`;
            }
            return occupied
                ? `<span class="rounded-xl border border-stone-800 bg-stone-900 px-2 py-2 text-center text-[10px] text-stone-500">${slot}<small class="mt-0.5 block">Ocupado</small></span>`
                : `<button type="button" onclick="selectCartAppointmentTime('${slot}')" class="rounded-xl border px-2 py-2 text-[10px] font-bold transition ${selectedTimeSlot === slot ? 'border-isivi-gold bg-isivi-gold text-isivi-black' : 'border-stone-700 bg-isivi-900 text-white hover:border-isivi-gold'}">${slot}</button>`;
        }).join('');
    } catch (err) {

        console.warn('Error al cargar agenda en carrito:', err);
        container.innerHTML = '<span class="col-span-full py-2 text-center text-[11px] text-red-300">No se pudo cargar la agenda. <button type="button" onclick="renderCartSchedule()" class="underline font-bold text-isivi-gold ml-1">Reintentar</button></span>';
    }
}

function changeCartCalendarMonth(delta) {
    cartCalendarDate.setMonth(cartCalendarDate.getMonth() + delta);
    renderCartCalendar();
}

function renderCartCalendar() {
    const year = cartCalendarDate.getFullYear();
    const month = cartCalendarDate.getMonth();
    const monthNames = ['Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio', 'Julio', 'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre'];
    const title = document.getElementById('cart-calendar-month-year');
    const container = document.getElementById('cart-calendar-days-grid');
    if (!title || !container) return;

    title.textContent = `${monthNames[month]} ${year}`;
    const firstDayIndex = new Date(year, month, 1).getDay();
    const totalDays = new Date(year, month + 1, 0).getDate();
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const todayKey = formatDateKey(today);
    let days = '';

    for (let index = 0; index < firstDayIndex; index++) days += '<span></span>';

    for (let day = 1; day <= totalDays; day++) {
        const date = new Date(year, month, day);
        const dateKey = formatDateKey(date);
        const isPast = date < today;
        const isWorkingDay = agendaConfiguration.diasLaborales.includes(date.getDay());
        const isSelected = dateKey === selectedDateStr;
        const unavailable = isPast || !isWorkingDay;
        const style = unavailable
            ? 'cursor-not-allowed border border-transparent bg-stone-950/60 text-stone-600'
            : isSelected
                ? 'border border-isivi-gold bg-isivi-gold text-isivi-black shadow'
                : 'border border-stone-700 bg-stone-950 text-white hover:border-isivi-gold';
        days += `<button type="button" ${unavailable ? 'disabled' : `onclick="selectCartAppointmentDate('${dateKey}')"`} class="relative flex h-9 items-center justify-center rounded-lg text-[11px] font-bold transition ${style}" aria-label="${day} de ${monthNames[month]}"><span>${day}</span>${dateKey === todayKey ? '<span class="absolute bottom-1 h-1 w-1 rounded-full bg-current"></span>' : ''}</button>`;
    }
    container.innerHTML = days;
}

function selectCartAppointmentDate(date) {
    if (!date) return;
    selectedDateStr = date;
    trackEvent('select_date', { selected_date: date });
    const [year, month] = date.split('-').map(Number);
    cartCalendarDate = new Date(year, month - 1, 1);
    const dateErr = document.getElementById('cart-date-error');
    if (dateErr) dateErr.classList.add('hidden');
    renderCartCalendar();
    renderCartSchedule();
    updateSummary();
    updateBookingProgressTracker();
    updateMobileContextBar();
}

async function selectCartAppointmentTime(time) {
    const activeHold = getPendingHold();
    if (activeHold && (activeHold.time !== time || activeHold.date !== selectedDateStr)) {
        await releaseHoldImmediately(activeHold);
    }
    selectedTimeSlot = time;
    trackEvent('select_time', { time_slot: time });
    const timeErr = document.getElementById('cart-time-error');
    if (timeErr) timeErr.classList.add('hidden');
    renderCartSchedule();
    updateSummary();
    updateBookingProgressTracker();
    updateMobileContextBar();
}


function completeCartCheckout() {
    bindCartValidationListeners();
    const validation = validateCartForm();
    if (!validation.isValid) {
        return;
    }
    copyCartDataToCheckout();
    const items = getCartItems();
    checkoutMode = items.some(item => item.type === 'service') ? 'services' : 'products';
    trackEvent('begin_checkout', { method: 'TRANSFER', items_count: items.length });
    closeCartDrawer();
    openPaymentModal();
}

function startCartWompiPayment() {
    bindCartValidationListeners();
    const validation = validateCartForm();
    if (!validation.isValid) {
        return;
    }
    const items = getCartItems();
    const hasServices = items.some(item => item.type === 'service');
    const hasProducts = items.some(item => item.type === 'product' || item.type === 'kit');
    if (hasServices && hasProducts) {
        renderCartErrors([{
            fieldId: 'cart-wompi-button',
            message: 'Para pagar con Wompi, realiza por separado la reserva de servicios y el pedido de productos. También puedes solicitar tu pedido combinado con la opción Transferencia / WhatsApp.'
        }]);
        return;
    }
    copyCartDataToCheckout();
    checkoutMode = hasServices ? 'services' : 'products';

    // Si ya existe una retención pendiente activa para este mismo horario / pedido, retomar directamente
    const activeHold = getPendingHold();
    if (activeHold && activeHold.id && (!hasServices || (activeHold.date === selectedDateStr && activeHold.time === selectedTimeSlot))) {
        resumeWompiPayment(activeHold);
        return;
    }

    trackEvent('begin_checkout', { method: 'WOMPI', items_count: items.length });
    trackEvent('payment_started', { method: 'WOMPI' });
    closeCartDrawer();
    processWompiPayment();
}


function updateDeliveryOptions() {
    const deliveryOptions = document.getElementById('product-delivery-options');
    const hasProducts = selectedProducts.length > 0 || selectedKits.length > 0;
    if (deliveryOptions) {
        deliveryOptions.classList.toggle('hidden', !hasProducts);
        if (!hasProducts) {
            const methodEl = document.getElementById('delivery-method');
            const addrEl = document.getElementById('delivery-address');
            if (methodEl) methodEl.value = 'pickup';
            if (addrEl) addrEl.value = '';
        }
        toggleDeliveryAddress();
    }
}

function toggleDeliveryAddress() {
    const deliveryOptions = document.getElementById('product-delivery-options');
    const addressWrap = document.getElementById('delivery-address-wrap');
    const addressInput = document.getElementById('delivery-address');
    if (!deliveryOptions || !addressWrap || !addressInput) return;
    const isDelivery = !deliveryOptions.classList.contains('hidden') && document.getElementById('delivery-method')?.value === 'delivery';
    addressWrap.classList.toggle('hidden', !isDelivery);
    addressInput.required = isDelivery;
    if (!isDelivery) addressInput.value = '';
}

// ============ VALIDACIONES FORMULARIO, CARRITO Y MODAL ============
function clearAllFormErrors() {
    ['cust-name', 'cust-phone', 'cust-email', 'delivery-address', 'cart-cust-name', 'cart-cust-phone', 'cart-cust-email', 'cart-delivery-address'].forEach(id => {
        const el = document.getElementById(id);
        const err = document.getElementById(`${id}-error`);
        if (el) {
            el.classList.remove('border-red-500', 'ring-1', 'ring-red-500');
            el.classList.add('border-stone-800');
            el.removeAttribute('aria-invalid');
        }
        if (err) err.classList.add('hidden');
    });
    const summaryEl = document.getElementById('payment-modal-error-summary');
    if (summaryEl) summaryEl.classList.add('hidden');
}

function clearCartFormErrors() {
    ['cart-cust-name', 'cart-cust-phone', 'cart-cust-email', 'cart-delivery-address'].forEach(id => {
        const el = document.getElementById(id);
        const err = document.getElementById(`${id}-error`);
        if (el) {
            el.classList.remove('border-red-500', 'ring-1', 'ring-red-500');
            el.classList.add('border-stone-800');
            el.removeAttribute('aria-invalid');
        }
        if (err) err.classList.add('hidden');
    });
    const dateErr = document.getElementById('cart-date-error');
    if (dateErr) dateErr.classList.add('hidden');
    const timeErr = document.getElementById('cart-time-error');
    if (timeErr) timeErr.classList.add('hidden');
    const summaryEl = document.getElementById('cart-error-summary');
    if (summaryEl) summaryEl.classList.add('hidden');
}

function renderCartErrors(errors) {
    clearCartFormErrors();
    const summaryEl = document.getElementById('cart-error-summary');
    if (summaryEl) {
        summaryEl.classList.remove('hidden');
    }

    errors.forEach(err => {
        const inputEl = document.getElementById(err.fieldId);
        const errorMsgEl = document.getElementById(`${err.fieldId}-error`);
        if (inputEl) {
            inputEl.classList.add('border-red-500', 'ring-1', 'ring-red-500');
            inputEl.classList.remove('border-stone-800');
            inputEl.setAttribute('aria-invalid', 'true');
        }
        if (errorMsgEl) {
            const span = errorMsgEl.querySelector('span');
            if (span) span.textContent = err.message;
            errorMsgEl.classList.remove('hidden');
        }
    });

    const firstError = errors[0];
    if (firstError) {
        const targetEl = document.getElementById(firstError.fieldId);
        if (targetEl) {
            targetEl.scrollIntoView({ behavior: 'smooth', block: 'center' });
            if (typeof targetEl.focus === 'function' && targetEl.tagName === 'INPUT') {
                targetEl.focus();
            }
        }
    }
}

function renderFormErrors(errors, inModal = false) {
    clearAllFormErrors();
    errors.forEach(err => {
        const inputEl = document.getElementById(err.fieldId);
        const errorMsgEl = document.getElementById(`${err.fieldId}-error`);
        if (inputEl) {
            inputEl.classList.add('border-red-500', 'ring-1', 'ring-red-500');
            inputEl.classList.remove('border-stone-800');
            inputEl.setAttribute('aria-invalid', 'true');
        }
        if (errorMsgEl) {
            const span = errorMsgEl.querySelector('span');
            if (span) span.textContent = err.message;
            errorMsgEl.classList.remove('hidden');
        }
    });

    const summaryEl = document.getElementById('payment-modal-error-summary');
    if (summaryEl) {
        summaryEl.classList.remove('hidden');
    }

    const firstError = errors[0];
    if (inModal) {
        const modalScroll = document.querySelector('#payment-modal .max-h-\\[80vh\\]');
        if (modalScroll) {
            modalScroll.scrollTo({ top: 0, behavior: 'smooth' });
        }
    } else {
        const firstEl = document.getElementById(firstError.fieldId);
        if (firstEl) {
            firstEl.scrollIntoView({ behavior: 'smooth', block: 'center' });
            if (typeof firstEl.focus === 'function') firstEl.focus();
        }
    }
}

function validateCartForm() {
    const errors = [];
    const items = getCartItems();
    if (!items.length) {
        errors.push({ fieldId: 'cart-cust-name', message: 'Añade al menos un producto, kit o servicio a tu carrito para continuar.' });
        renderCartErrors(errors);
        return { isValid: false, errors };
    }

    const hasServices = items.some(item => item.type === 'service');
    const hasProducts = items.some(item => item.type === 'product' || item.type === 'kit');

    const nameInput = document.getElementById('cart-cust-name');
    const phoneInput = document.getElementById('cart-cust-phone');
    const emailInput = document.getElementById('cart-cust-email');
    const deliveryMethodSelect = document.getElementById('cart-delivery-method');
    const deliveryAddressInput = document.getElementById('cart-delivery-address');

    const name = (nameInput?.value || '').trim();
    const phone = (phoneInput?.value || '').trim();
    const email = (emailInput?.value || '').trim();
    const phoneDigits = phone.replace(/\D/g, '');
    const isDelivery = hasProducts && deliveryMethodSelect?.value === 'delivery';
    const deliveryAddress = (deliveryAddressInput?.value || '').trim();
    const emailRegex = /^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/;

    if (!name) {
        errors.push({ fieldId: 'cart-cust-name', message: 'Ingresa tu nombre completo.' });
    }

    if (!phone) {
        errors.push({ fieldId: 'cart-cust-phone', message: 'Ingresa tu número de WhatsApp.' });
    } else if (phoneDigits.length < 7 || phoneDigits.length > 15) {
        errors.push({ fieldId: 'cart-cust-phone', message: 'Ingresa un número de WhatsApp válido (ej. 300 123 4567).' });
    }

    if (!email) {
        errors.push({ fieldId: 'cart-cust-email', message: 'Ingresa tu correo electrónico.' });
    } else if (!emailRegex.test(email)) {
        errors.push({ fieldId: 'cart-cust-email', message: 'Ingresa un correo electrónico válido.' });
    }

    if (isDelivery && !deliveryAddress) {
        errors.push({ fieldId: 'cart-delivery-address', message: 'Ingresa la dirección para el envío a domicilio.' });
    }

    if (hasServices) {
        if (!selectedDateStr) {
            errors.push({ fieldId: 'cart-date', message: 'Selecciona una fecha válida en el calendario.' });
        }
        if (!selectedTimeSlot) {
            errors.push({ fieldId: 'cart-time', message: 'Debes seleccionar un horario disponible.' });
        }
    }

    if (hasProducts) {
        const productItems = items.filter(item => item.type !== 'service');
        for (const item of productItems) {
            if (!item.isLoaded) {
                errors.push({ fieldId: 'cart-cust-name', message: `Estamos verificando la disponibilidad de "${item.name}". Por favor espera un momento.` });
            } else if (item.availableStock === 0) {
                errors.push({ fieldId: 'cart-cust-name', message: `El artículo "${item.name}" está agotado. Por favor retíralo de tu carrito para continuar.` });
            } else if (item.quantity > item.availableStock) {
                errors.push({ fieldId: 'cart-cust-name', message: `Solo hay ${item.availableStock} unidades disponibles de "${item.name}". Por favor ajusta la cantidad.` });
            }
        }
    }

    if (errors.length > 0) {
        renderCartErrors(errors);
        return { isValid: false, errors };
    }

    clearCartFormErrors();
    return { isValid: true, errors: [] };
}

function bindCartValidationListeners() {
    ['cart-cust-name', 'cart-cust-phone', 'cart-cust-email', 'cart-delivery-address'].forEach(id => {
        const el = document.getElementById(id);
        if (el && !el.dataset.cartValidationBound) {
            el.dataset.cartValidationBound = 'true';
            const clearThisError = () => {
                el.classList.remove('border-red-500', 'ring-1', 'ring-red-500');
                el.classList.add('border-stone-800');
                el.removeAttribute('aria-invalid');
                const err = document.getElementById(`${id}-error`);
                if (err) err.classList.add('hidden');
                const remainingErrors = document.querySelectorAll('#cart-checkout [aria-invalid="true"]');
                if (remainingErrors.length === 0) {
                    const summaryEl = document.getElementById('cart-error-summary');
                    if (summaryEl) summaryEl.classList.add('hidden');
                }
                updateBookingProgressTracker();
            };
            el.addEventListener('input', clearThisError);
            el.addEventListener('change', clearThisError);
        }
    });

    const deliveryMethod = document.getElementById('cart-delivery-method');
    if (deliveryMethod && !deliveryMethod.dataset.cartValidationBound) {
        deliveryMethod.dataset.cartValidationBound = 'true';
        deliveryMethod.addEventListener('change', () => {
            const err = document.getElementById('cart-delivery-address-error');
            const addr = document.getElementById('cart-delivery-address');
            if (deliveryMethod.value !== 'delivery') {
                if (err) err.classList.add('hidden');
                if (addr) {
                    addr.classList.remove('border-red-500', 'ring-1', 'ring-red-500');
                    addr.removeAttribute('aria-invalid');
                }
            }
        });
    }
}

function validateReservationForm(inModal = false) {
    const errors = [];
    const nameInput = document.getElementById('cart-cust-name') || document.getElementById('cust-name');
    const phoneInput = document.getElementById('cart-cust-phone') || document.getElementById('cust-phone');
    const emailInput = document.getElementById('cart-cust-email') || document.getElementById('cust-email');
    const deliveryAddressInput = document.getElementById('cart-delivery-address') || document.getElementById('delivery-address');
    const deliveryMethodSelect = document.getElementById('cart-delivery-method') || document.getElementById('delivery-method');

    const name = (nameInput?.value || '').trim();
    const phone = (phoneInput?.value || '').trim();
    const email = (emailInput?.value || '').trim();
    const phoneDigits = phone.replace(/\D/g, '');
    const emailRegex = /^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/;

    const hasServices = selectedServices.length > 0;
    const hasProducts = selectedProducts.length > 0 || selectedKits.length > 0;
    const isDelivery = hasProducts && deliveryMethodSelect?.value === 'delivery';
    const deliveryAddress = (deliveryAddressInput?.value || '').trim();

    clearAllFormErrors();

    const nameFieldId = nameInput?.id || 'cart-cust-name';
    const phoneFieldId = phoneInput?.id || 'cart-cust-phone';
    const emailFieldId = emailInput?.id || 'cart-cust-email';
    const addressFieldId = deliveryAddressInput?.id || 'cart-delivery-address';

    if (!hasServices && !hasProducts) {
        errors.push({ fieldId: nameFieldId, message: 'Selecciona al menos un servicio o producto antes de continuar.' });
    }

    if (hasServices && (!selectedDateStr || !selectedTimeSlot)) {
        errors.push({ fieldId: nameFieldId, message: 'Selecciona una fecha y un horario disponibles en el calendario.' });
    }

    if (!name) {
        errors.push({ fieldId: nameFieldId, message: 'El nombre completo es obligatorio.' });
    }

    if (!phone) {
        errors.push({ fieldId: phoneFieldId, message: 'Ingresa tu número de WhatsApp.' });
    } else if (phoneDigits.length < 7 || phoneDigits.length > 15) {
        errors.push({ fieldId: phoneFieldId, message: 'Ingresa un número de WhatsApp válido (ej. 300 123 4567).' });
    }

    if (!email) {
        errors.push({ fieldId: emailFieldId, message: 'Ingresa tu correo electrónico.' });
    } else if (!emailRegex.test(email)) {
        errors.push({ fieldId: emailFieldId, message: 'Ingresa un correo electrónico válido.' });
    }

    if (isDelivery && !deliveryAddress) {
        errors.push({ fieldId: addressFieldId, message: 'Ingresa la dirección para el envío a domicilio.' });
    }

    if (hasProducts) {
        const productItems = getCartItems().filter(item => item.type !== 'service');
        for (const item of productItems) {
            if (!item.isLoaded) {
                errors.push({ fieldId: nameFieldId, message: `Estamos verificando la disponibilidad de "${item.name}". Por favor espera un momento.` });
            } else if (item.availableStock === 0) {
                errors.push({ fieldId: nameFieldId, message: `El artículo "${item.name}" está agotado. Por favor retíralo de tu pedido para continuar.` });
            } else if (item.quantity > item.availableStock) {
                errors.push({ fieldId: nameFieldId, message: `Solo hay ${item.availableStock} unidades disponibles de "${item.name}". Por favor ajusta la cantidad.` });
            }
        }
    }

    if (errors.length > 0) {
        renderFormErrors(errors, inModal);
        return { isValid: false, errors };
    }

    return { isValid: true, errors: [] };
}

function bindValidationListeners() {
    ['cust-name', 'cust-phone', 'delivery-address'].forEach(id => {
        const el = document.getElementById(id);
        if (el && !el.dataset.validationBound) {
            el.dataset.validationBound = 'true';
            const clearThisError = () => {
                el.classList.remove('border-red-500', 'ring-1', 'ring-red-500');
                el.classList.add('border-stone-800');
                el.removeAttribute('aria-invalid');
                const err = document.getElementById(`${id}-error`);
                if (err) err.classList.add('hidden');
                const remainingErrors = document.querySelectorAll('[aria-invalid="true"]');
                if (remainingErrors.length === 0) {
                    const summaryEl = document.getElementById('payment-modal-error-summary');
                    if (summaryEl) summaryEl.classList.add('hidden');
                }
                updateBookingProgressTracker();
            };
            el.addEventListener('input', clearThisError);
            el.addEventListener('change', clearThisError);
        }
    });
}

function openPaymentModal() {
    bindValidationListeners();
    const validation = validateReservationForm(false);
    if (!validation.isValid) {
        return;
    }

    const isProductOrder = checkoutMode === 'products';
    const subtotalEl = document.getElementById('summary-subtotal');
    const depositEl = document.getElementById('summary-deposit');
    const cartTotalEl = document.getElementById('cart-total');
    let paymentValue = '$0';
    if (isProductOrder && subtotalEl) paymentValue = subtotalEl.textContent;
    else if (!isProductOrder && depositEl) paymentValue = depositEl.textContent;
    else if (cartTotalEl) paymentValue = cartTotalEl.textContent;

    const modalDepositEl = document.getElementById('modal-deposit-amount');
    const instructionDepositEl = document.getElementById('instruction-deposit-val');
    if (modalDepositEl) modalDepositEl.textContent = paymentValue;
    if (instructionDepositEl) instructionDepositEl.textContent = paymentValue.replace('$', '');
    const modalTitle = document.querySelector('#payment-modal h3');
    if (modalTitle) modalTitle.textContent = isProductOrder ? 'Pago de Pedido' : 'Pago de Anticipo (25%)';

    trackEvent('begin_checkout', { mode: isProductOrder ? 'products' : 'services', items_count: getCartItems().length });

    selectPaymentMethod(currentPaymentMethod || 'bancolombia');
    clearAllFormErrors();
    const modalEl = document.getElementById('payment-modal');
    if (modalEl) modalEl.classList.remove('hidden');
}

function closePaymentModal() {
    const modalEl = document.getElementById('payment-modal');
    if (modalEl) modalEl.classList.add('hidden');
    clearAllFormErrors();
    updateMobileContextBar();
}

function selectPaymentMethod(method) {
    currentPaymentMethod = method;
    trackEvent('select_payment_method', { method: method === 'wompi' ? 'WOMPI' : 'TRANSFER', transfer_channel: method });
    const data = transferOptions[method];
    if (!data) return;
    const cartSelect = document.getElementById('cart-payment-method');
    if (cartSelect) cartSelect.value = method;
    updatePaymentMethodSelection(method);

    const box = document.getElementById('transfer-details-box');
    if (box) {
        box.innerHTML = `
            <div class="flex justify-between items-center text-xs border-b border-stone-800 pb-2">
                <span class="text-isivi-gold font-bold"><i class="fa-solid ${data.icon} ${data.color} mr-1"></i>${data.title}</span>
                <span class="text-[10px] text-isivi-300">Titular: ${data.holder}</span>
            </div>
            <div class="flex justify-between items-center pt-1">
                <div>
                    <span class="text-[10px] text-isivi-300 block">Número de Transferencia</span>
                    <span class="font-bold text-white text-base tracking-wider">${data.number}</span>
                </div>
                <button onclick="copyToClipboard('${data.number}')" class="bg-isivi-500/20 text-isivi-gold border border-isivi-gold hover:bg-isivi-gold hover:text-isivi-black px-3 py-1 rounded-xl text-xs font-bold transition">
                    <i class="fa-regular fa-copy mr-1"></i> Copiar
                </button>
            </div>
        `;
    }
}

function copyToClipboard(text) {
    const dummy = document.createElement("textarea");
    document.body.appendChild(dummy);
    dummy.value = text;
    dummy.select();
    document.execCommand("copy");
    document.body.removeChild(dummy);
    showToast("Número copiado al portapapeles");
}

function resetCartState() {
    selectedServices = [];
    selectedProducts = [];
    selectedKits = [];
    productQuantities = {};
    kitQuantities = {};
    productCardQuantities = {};
    kitCardQuantities = {};

    const custName = document.getElementById('cust-name');
    const custPhone = document.getElementById('cust-phone');
    const custEmail = document.getElementById('cust-email');
    const custCity = document.getElementById('cust-city');
    const delivMethod = document.getElementById('delivery-method');
    const delivAddr = document.getElementById('delivery-address');
    if (custName) custName.value = '';
    if (custPhone) custPhone.value = '';
    if (custEmail) custEmail.value = '';
    if (custCity) custCity.value = '';
    if (delivMethod) delivMethod.value = 'pickup';
    if (delivAddr) delivAddr.value = '';

    const cartName = document.getElementById('cart-cust-name');
    const cartPhone = document.getElementById('cart-cust-phone');
    const cartEmail = document.getElementById('cart-cust-email');
    const cartCity = document.getElementById('cart-cust-city');
    const cartMethod = document.getElementById('cart-delivery-method');
    const cartAddr = document.getElementById('cart-delivery-address');
    if (cartName) cartName.value = '';
    if (cartPhone) cartPhone.value = '';
    if (cartEmail) cartEmail.value = '';
    if (cartCity) cartCity.value = '';
    if (cartMethod) cartMethod.value = 'pickup';
    if (cartAddr) cartAddr.value = '';

    renderServicesGrid();
    renderProductsGrid();
    renderKitsGrid();
    syncCartState();
}

async function refreshCheckoutAfterReservation() {
    resetCartState();
    await renderTimeSlots();
}

// Global state for success screen and lookup
let currentSuccessBooking = null;
let currentLookupBooking = null;

function formatFriendlyDate(dateStr) {
    if (!dateStr) return 'Fecha por coordinar';
    const parts = dateStr.split('-');
    if (parts.length !== 3) return dateStr;
    const d = new Date(Number(parts[0]), Number(parts[1]) - 1, Number(parts[2]));
    if (isNaN(d.getTime())) return dateStr;
    const options = { weekday: 'long', day: 'numeric', month: 'long' };
    const formatted = new Intl.DateTimeFormat('es-CO', options).format(d);
    return formatted.charAt(0).toUpperCase() + formatted.slice(1);
}

// ============ MEJORA 1: PANTALLA DE ÉXITO POST-RESERVA / POST-PAGO ============
function showSuccessConfirmation(booking, statusCase = 'PENDING') {
    currentSuccessBooking = booking;

    const modal = document.getElementById('modal-success-confirmation');
    if (!modal) return;

    const iconContainer = document.getElementById('success-icon-container');
    const icon = document.getElementById('success-icon');
    const title = document.getElementById('success-title');
    const subtitle = document.getElementById('success-subtitle');
    const codeText = document.getElementById('success-booking-code-text');
    const dateWrap = document.getElementById('success-appointment-datetime-wrap');
    const dateText = document.getElementById('success-date');
    const timeText = document.getElementById('success-time');
    const itemsList = document.getElementById('success-items-list');
    const depositLabel = document.getElementById('success-deposit-label');
    const depositVal = document.getElementById('success-deposit-val');
    const remainingRow = document.getElementById('success-remaining-row');
    const remainingVal = document.getElementById('success-remaining-val');
    const totalVal = document.getElementById('success-total-val');
    const btnCalendar = document.getElementById('success-btn-calendar');
    const btnWhatsApp = document.getElementById('success-btn-whatsapp');
    const btnWhatsAppText = document.getElementById('success-btn-whatsapp-text');
    const btnRetry = document.getElementById('success-btn-retry-payment');
    const deliveryWrap = document.getElementById('success-delivery-wrap');
    const deliveryLabel = document.getElementById('success-delivery-label');
    const deliveryInfo = document.getElementById('success-delivery-info');
    const deliveryIcon = document.getElementById('success-delivery-icon');
    const locationWrap = document.getElementById('success-location-wrap');
    const locationInfo = document.getElementById('success-location-info');

    // Identificación canónica de tipo (alineada con Reserva.java y mapFromApiReserva)
    const inv = Array.isArray(booking.itemsInventario) ? booking.itemsInventario : [];
    const hasServiceItem = inv.length > 0 
        ? inv.some(i => i.tipo === 'servicio') 
        : Boolean((booking.date || booking.fechaCita) && (booking.time || booking.horaCita));
    const hasProductItem = inv.length > 0 
        ? inv.some(i => i.tipo === 'producto' || i.tipo === 'kit') 
        : (booking.isPureOrder === true || (Array.isArray(booking.services) && booking.services.some(s => s.toLowerCase().includes('kit') || s.toLowerCase().includes('shampoo') || s.toLowerCase().includes('óleo') || s.toLowerCase().includes('termo') || s.toLowerCase().includes('gotas') || s.toLowerCase().includes('tratamiento'))));

    const isPureAppointment = booking.isPureAppointment === true || (hasServiceItem && !hasProductItem && booking.isPureOrder !== true && booking.isMixed !== true);
    const isPureOrder = booking.isPureOrder === true || (!hasServiceItem && hasProductItem);
    const isMixed = booking.isMixed === true || (hasServiceItem && hasProductItem);
    const hasServiceBooking = !isPureOrder && hasServiceItem;
    const hasProductBooking = isPureOrder || isMixed || hasProductItem;

    // Código real de backend
    codeText.textContent = booking.code || 'ISV-0000';
    if (dateText) dateText.textContent = booking.date ? formatFriendlyDate(booking.date) : 'Solo productos';
    if (timeText) timeText.textContent = booking.time || 'N/A';

    // Lista de servicios y productos
    const itemsLabel = document.getElementById('success-items-label');
    if (itemsLabel) {
        if (isPureAppointment) {
            itemsLabel.textContent = 'Servicios';
        } else if (isPureOrder) {
            itemsLabel.textContent = 'Productos y Kits';
        } else {
            itemsLabel.textContent = 'Servicios y Productos';
        }
    }
    const items = Array.isArray(booking.services) ? booking.services : [booking.services || 'Servicio ISIVI'];
    if (itemsList) itemsList.innerHTML = items.map(it => `<div>${it}</div>`).join('');

    // Visibilidad de fecha/hora de cita (estrictamente oculta para pedidos puros)
    if (dateWrap) {
        dateWrap.classList.toggle('hidden', isPureOrder);
    }

    // Lugar de la cita (para Solo Cita y Cita + Productos)
    if (locationWrap) {
        if (hasServiceBooking) {
            locationWrap.classList.remove('hidden');
            if (locationInfo) locationInfo.textContent = 'ISIVI Salón Cartagena';
        } else {
            locationWrap.classList.add('hidden');
        }
    }

    // Entrega / Entrega de productos
    if (deliveryWrap) {
        if (isPureOrder) {
            // SOLO PEDIDO -> ENTREGA
            deliveryWrap.classList.remove('hidden');
            if (deliveryLabel) deliveryLabel.textContent = 'Entrega';
            const deliveryMethodNormalized = String(booking.tipoEntrega || booking.deliveryMethod || 'pickup').trim().toLowerCase();
            const isDeliv = deliveryMethodNormalized === 'delivery' || deliveryMethodNormalized === 'domicilio';
            const dir = booking.deliveryAddress || booking.direccionEntrega || '';
            if (deliveryIcon) deliveryIcon.className = `fa-solid ${isDeliv ? 'fa-truck' : 'fa-store'} text-isivi-gold text-base w-5 text-center mt-0.5`;
            if (deliveryInfo) {
                deliveryInfo.innerHTML = isDeliv 
                    ? `<span class="text-amber-300 font-bold">🏠 Envío a domicilio:</span> ${dir || 'Dirección registrada'}`
                    : `<span class="text-isivi-gold font-bold">📍 Recoger en el local:</span> ISIVI Salón Cartagena`;
            }
        } else if (isMixed && (hasProductBooking || booking.deliveryMethod || booking.tipoEntrega)) {
            // CITA + PRODUCTOS -> ENTREGA DE PRODUCTOS
            deliveryWrap.classList.remove('hidden');
            if (deliveryLabel) deliveryLabel.textContent = 'Entrega de productos';
            const deliveryMethodNormalized = String(booking.tipoEntrega || booking.deliveryMethod || 'pickup').trim().toLowerCase();
            const isDeliv = deliveryMethodNormalized === 'delivery' || deliveryMethodNormalized === 'domicilio';
            const dir = booking.deliveryAddress || booking.direccionEntrega || '';
            if (deliveryIcon) deliveryIcon.className = `fa-solid ${isDeliv ? 'fa-truck' : 'fa-store'} text-isivi-gold text-base w-5 text-center mt-0.5`;
            if (deliveryInfo) {
                deliveryInfo.innerHTML = isDeliv 
                    ? `<span class="text-amber-300 font-bold">🏠 Envío a domicilio:</span> ${dir || 'Dirección registrada'}`
                    : `<span class="text-isivi-gold font-bold">📍 Recoger en el local:</span> ISIVI Salón Cartagena`;
            }
        } else {
            // SOLO CITA -> NO mostrar entrega
            deliveryWrap.classList.add('hidden');
        }
    }

    // Cálculos financieros reales
    const total = Number(booking.subtotal) || (Number(booking.deposit) * 4) || 0;
    const deposit = hasServiceBooking ? (Number(booking.deposit) || Math.round(total * 0.25)) : total;
    const paidAmount = hasServiceBooking ? deposit : total;
    const remaining = hasServiceBooking ? Math.max(0, total - deposit) : 0;

    if (totalVal) totalVal.textContent = `$${total.toLocaleString('es-CO')} COP`;

    // 3 Casos de visualización
    if (statusCase === 'APPROVED' || booking.status === 'Confirmado' || booking.status === 'Pago Confirmado' || booking.paymentStatus === 'APROBADO') {
        // Caso A: Pago Aprobado
        if (iconContainer) iconContainer.className = 'w-16 h-16 rounded-full bg-emerald-500/20 border-2 border-emerald-500 flex items-center justify-center text-emerald-400 text-3xl mx-auto shadow-lg shadow-emerald-500/20';
        if (icon) icon.className = 'fa-solid fa-check';

        if (isPureOrder) {
            if (title) title.textContent = 'Pago aprobado. Pedido confirmado.';
            if (subtitle) subtitle.textContent = 'Tu compra ha sido procesada y registrada exitosamente.';
            if (depositLabel) depositLabel.textContent = 'Pago total:';
            if (depositVal) {
                depositVal.textContent = `$${total.toLocaleString('es-CO')} COP`;
                depositVal.className = 'font-bold text-sm text-emerald-400';
            }
            if (remainingRow) remainingRow.classList.add('hidden');
            if (btnCalendar) btnCalendar.classList.add('hidden');
        } else if (isMixed) {
            if (title) title.textContent = 'Pago aprobado. Pedido confirmado.';
            if (subtitle) subtitle.textContent = 'Tu cita y tus productos han sido confirmados exitosamente.';
            if (depositLabel) depositLabel.textContent = 'Anticipo (25%):';
            if (depositVal) {
                depositVal.textContent = `$${paidAmount.toLocaleString('es-CO')} COP`;
                depositVal.className = 'font-bold text-sm text-emerald-400';
            }
            if (remainingRow) {
                remainingRow.classList.remove('hidden');
                if (remainingVal) remainingVal.textContent = `$${remaining.toLocaleString('es-CO')} COP`;
            }
            if (btnCalendar) btnCalendar.classList.remove('hidden');
        } else {
            if (title) title.textContent = '¡Tu cita está reservada! ✨';
            if (subtitle) subtitle.textContent = 'Tu turno ha sido confirmado y registrado en la agenda.';
            if (depositLabel) depositLabel.textContent = 'Anticipo (25%):';
            if (depositVal) {
                depositVal.textContent = `$${paidAmount.toLocaleString('es-CO')} COP`;
                depositVal.className = 'font-bold text-sm text-emerald-400';
            }
            if (remainingRow) {
                remainingRow.classList.remove('hidden');
                if (remainingVal) remainingVal.textContent = `$${remaining.toLocaleString('es-CO')} COP`;
            }
            if (btnCalendar) btnCalendar.classList.remove('hidden');
        }

        if (btnWhatsApp) btnWhatsApp.classList.remove('hidden');
        if (btnWhatsAppText) btnWhatsAppText.textContent = isPureOrder ? 'Contactar por WhatsApp' : 'Escribir por WhatsApp';
        if (btnRetry) btnRetry.classList.add('hidden');

        trackEvent('payment_success', {
            method: booking.wompiReference ? 'WOMPI' : 'TRANSFER',
            value: paidAmount,
            currency: 'COP',
            is_service: hasServiceBooking
        }, booking.code);

        trackEvent('purchase', {
            transaction_id: booking.code,
            value: paidAmount,
            currency: 'COP',
            items_count: items.length
        }, booking.code);

        trackEvent('reservation_confirmed', {
            method: booking.wompiReference ? 'WOMPI' : 'TRANSFER',
            has_service: hasServiceBooking
        }, booking.code);

    } else if (statusCase === 'DECLINED' || booking.paymentStatus === 'RECHAZADO') {
        // Caso C: Pago rechazado
        if (iconContainer) iconContainer.className = 'w-16 h-16 rounded-full bg-red-500/20 border-2 border-red-500 flex items-center justify-center text-red-400 text-3xl mx-auto shadow-lg shadow-red-500/20';
        if (icon) icon.className = 'fa-solid fa-triangle-exclamation';
        if (title) title.textContent = 'El pago no fue aprobado. Puedes intentarlo nuevamente.';
        if (subtitle) subtitle.textContent = 'Tu solicitud sigue guardada temporalmente, pero el pago no fue aprobado. Puedes intentar nuevamente o pagar por transferencia.';
        if (depositLabel) depositLabel.textContent = hasServiceBooking ? 'Anticipo pendiente:' : 'Total pendiente:';
        if (depositVal) {
            depositVal.textContent = `$${deposit.toLocaleString('es-CO')} COP`;
            depositVal.className = 'font-bold text-sm text-amber-400';
        }
        if (hasServiceBooking) {
            if (remainingRow) remainingRow.classList.remove('hidden');
            if (remainingVal) remainingVal.textContent = `$${remaining.toLocaleString('es-CO')} COP`;
        } else {
            if (remainingRow) remainingRow.classList.add('hidden');
        }
        if (btnCalendar) btnCalendar.classList.add('hidden');
        if (btnWhatsApp) btnWhatsApp.classList.remove('hidden');
        if (btnWhatsAppText) btnWhatsAppText.textContent = 'Pagar por transferencia vía WhatsApp';
        if (btnRetry) btnRetry.classList.remove('hidden');

        trackEvent('payment_failure', { method: booking.wompiReference ? 'WOMPI' : 'TRANSFER', status: 'DECLINED' }, booking.code);

    } else if (statusCase === 'ERROR' || booking.paymentStatus === 'ERROR') {
        // Caso D: Error en pago
        if (iconContainer) iconContainer.className = 'w-16 h-16 rounded-full bg-red-500/20 border-2 border-red-500 flex items-center justify-center text-red-400 text-3xl mx-auto shadow-lg shadow-red-500/20';
        if (icon) icon.className = 'fa-solid fa-triangle-exclamation';
        if (title) title.textContent = 'No fue posible completar el pago. Puedes intentarlo nuevamente.';
        if (subtitle) subtitle.textContent = 'Ocurrió un error al procesar el pago con la pasarela. Puedes intentarlo nuevamente o pagar por transferencia.';
        if (depositLabel) depositLabel.textContent = hasServiceBooking ? 'Anticipo pendiente:' : 'Total pendiente:';
        if (depositVal) {
            depositVal.textContent = `$${deposit.toLocaleString('es-CO')} COP`;
            depositVal.className = 'font-bold text-sm text-amber-400';
        }
        if (hasServiceBooking) {
            if (remainingRow) remainingRow.classList.remove('hidden');
            if (remainingVal) remainingVal.textContent = `$${remaining.toLocaleString('es-CO')} COP`;
        } else {
            if (remainingRow) remainingRow.classList.add('hidden');
        }
        if (btnCalendar) btnCalendar.classList.add('hidden');
        if (btnWhatsApp) btnWhatsApp.classList.remove('hidden');
        if (btnWhatsAppText) btnWhatsAppText.textContent = 'Pagar por transferencia vía WhatsApp';
        if (btnRetry) btnRetry.classList.remove('hidden');

        trackEvent('payment_failure', { method: booking.wompiReference ? 'WOMPI' : 'TRANSFER', status: 'ERROR' }, booking.code);

    } else {
        // Caso B: Reserva o Pedido creado + comprobante pendiente
        if (iconContainer) iconContainer.className = 'w-16 h-16 rounded-full bg-amber-500/20 border-2 border-amber-500 flex items-center justify-center text-amber-400 text-3xl mx-auto shadow-lg shadow-amber-500/20';
        if (icon) icon.className = 'fa-solid fa-clock-rotate-left';

        if (isPureOrder) {
            if (title) title.textContent = 'Pedido creado. Tienes 24 horas para enviar el comprobante.';
            if (subtitle) subtitle.textContent = 'Tu pedido ha sido recibido. Envíanos el comprobante de transferencia por WhatsApp para despacharlo.';
            if (depositLabel) depositLabel.textContent = 'Total a transferir:';
            if (depositVal) {
                depositVal.textContent = `$${total.toLocaleString('es-CO')} COP`;
                depositVal.className = 'font-bold text-sm text-amber-400';
            }
            if (remainingRow) remainingRow.classList.add('hidden');
            if (btnCalendar) btnCalendar.classList.add('hidden');
        } else if (isMixed) {
            if (title) title.textContent = 'Pedido creado. Tienes 24 horas para enviar el comprobante.';
            if (subtitle) subtitle.textContent = 'Tu turno y tus productos han sido registrados. Envíanos el comprobante de transferencia para confirmar.';
            if (depositLabel) depositLabel.textContent = 'Anticipo requerido (25% cita + productos):';
            if (depositVal) {
                depositVal.textContent = `$${deposit.toLocaleString('es-CO')} COP`;
                depositVal.className = 'font-bold text-sm text-amber-400';
            }
            if (remainingRow) {
                remainingRow.classList.remove('hidden');
                if (remainingVal) remainingVal.textContent = `$${remaining.toLocaleString('es-CO')} COP`;
            }
            if (btnCalendar) btnCalendar.classList.remove('hidden');
        } else {
            if (title) title.textContent = '¡Reserva creada exitosamente! ✨';
            if (subtitle) subtitle.textContent = 'Tu turno está reservado. Envíanos el comprobante del 25% para que tu cita quede 100% confirmada.';
            if (depositLabel) depositLabel.textContent = 'Anticipo requerido (25%):';
            if (depositVal) {
                depositVal.textContent = `$${deposit.toLocaleString('es-CO')} COP`;
                depositVal.className = 'font-bold text-sm text-amber-400';
            }
            if (remainingRow) {
                remainingRow.classList.remove('hidden');
                if (remainingVal) remainingVal.textContent = `$${remaining.toLocaleString('es-CO')} COP`;
            }
            if (btnCalendar) btnCalendar.classList.remove('hidden');
        }

        if (btnWhatsApp) btnWhatsApp.classList.remove('hidden');
        if (btnWhatsAppText) btnWhatsAppText.textContent = 'Enviar comprobante por WhatsApp';
        if (btnRetry) btnRetry.classList.add('hidden');

        trackEvent('reservation_created', { method: 'TRANSFER', has_service: hasServiceBooking }, booking.code);
    }

    modal.classList.remove('hidden');
}

function closeSuccessConfirmation() {
    const modal = document.getElementById('modal-success-confirmation');
    if (modal) modal.classList.add('hidden');
    updateMobileContextBar();
}

function copyBookingCode() {
    if (!currentSuccessBooking || !currentSuccessBooking.code) return;
    navigator.clipboard.writeText(currentSuccessBooking.code).then(() => {
        showToast(`Código ${currentSuccessBooking.code} copiado al portapapeles`, 'success');
    }).catch(() => {
        showToast(`Código: ${currentSuccessBooking.code}`, 'info');
    });
}

function downloadCalendarEvent() {
    if (!currentSuccessBooking || !currentSuccessBooking.date || !currentSuccessBooking.time) {
        showToast('Esta orden no incluye una cita presencial con horario.', 'info');
        return;
    }

    const b = currentSuccessBooking;
    const dateParts = b.date.split('-'); // [YYYY, MM, DD]
    if (dateParts.length !== 3) {
        showToast('Fecha de cita no válida', 'error');
        return;
    }

    // Parse hour
    let [hourStr, minuteStr] = b.time.replace(/AM|PM|am|pm/g, '').trim().split(':');
    let hour = parseInt(hourStr, 10);
    const minute = minuteStr ? parseInt(minuteStr, 10) : 0;
    const isPM = /PM|pm/.test(b.time);
    const isAM = /AM|am/.test(b.time);
    if (isPM && hour < 12) hour += 12;
    if (isAM && hour === 12) hour = 0;

    const pad = (n) => String(n).padStart(2, '0');
    const startStr = `${dateParts[0]}${dateParts[1]}${dateParts[2]}T${pad(hour)}${pad(minute)}00`;
    
    // Default duration 90 min
    const endHour = hour + Math.floor((minute + 90) / 60);
    const endMin = (minute + 90) % 60;
    const endStr = `${dateParts[0]}${dateParts[1]}${dateParts[2]}T${pad(endHour)}${pad(endMin)}00`;

    const serviceName = Array.isArray(b.services) ? b.services.join(', ') : (b.services || 'Cuidado Capilar');
    const total = Number(b.subtotal) || (Number(b.deposit) * 4) || 0;
    const deposit = Number(b.deposit) || Math.round(total * 0.25);
    const remaining = Math.max(0, total - deposit);

    const description = `Reserva ISIVI: ${b.code}\\n` +
        `Servicios: ${serviceName}\\n` +
        `Cliente: ${b.customerName || ''}\\n` +
        `Anticipo: $${deposit.toLocaleString('es-CO')} COP\\n` +
        `Saldo en peluquería: $${remaining.toLocaleString('es-CO')} COP\\n` +
        `Contacto: (+57) 300 894 9050\\n` +
        `¡Te esperamos en ISIVI!`;

    const icsContent = [
        'BEGIN:VCALENDAR',
        'VERSION:2.0',
        'PRODID:-//ISIVI//Peluqueria y Cuidado Capilar//ES',
        'CALSCALE:GREGORIAN',
        'METHOD:PUBLISH',
        'BEGIN:VEVENT',
        `UID:isivi-${b.code}-${Date.now()}@isivi.com`,
        `DTSTAMP:${startStr}Z`,
        `DTSTART:${startStr}`,
        `DTEND:${endStr}`,
        `SUMMARY:Cita ISIVI - ${serviceName}`,
        `DESCRIPTION:${description}`,
        'LOCATION:ISIVI Peluquería & Cuidado Capilar Natural, Cartagena, Colombia',
        'STATUS:CONFIRMED',
        'BEGIN:VALARM',
        'TRIGGER:-PT2H',
        'ACTION:DISPLAY',
        'DESCRIPTION:Recordatorio Cita ISIVI',
        'END:VALARM',
        'END:VEVENT',
        'END:VCALENDAR'
    ].join('\r\n');

    const blob = new Blob([icsContent], { type: 'text/calendar;charset=utf-8;' });
    const link = document.createElement('a');
    link.href = window.URL.createObjectURL(blob);
    link.setAttribute('download', `Cita-ISIVI-${b.code}.ics`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    showToast('Evento descargado para tu calendario (Google, Apple, Outlook)', 'success');
}

function sendWhatsAppConfirmation() {
    if (!currentSuccessBooking) return;
    trackEvent('whatsapp_click', { context: 'payment_receipt' });
    const b = currentSuccessBooking;
    const serviceName = Array.isArray(b.services) ? b.services.join(', ') : (b.services || 'Servicio ISIVI');
    const total = Number(b.subtotal) || (Number(b.deposit) * 4) || 0;
    const deposit = Number(b.deposit) || Math.round(total * 0.25);
    const remaining = Math.max(0, total - deposit);
    const friendlyDate = b.date ? formatFriendlyDate(b.date) : 'Por coordinar';

    const message = b.date
        ? `Hola ISIVI 👋\nQuiero enviar el comprobante de mi anticipo.\n\nReserva: ${b.code}\nCliente: ${b.customerName || 'Cliente'}\nServicio: ${serviceName}\nFecha: ${friendlyDate}\nHora: ${b.time || 'N/A'}\nAnticipo: $${deposit.toLocaleString('es-CO')} COP\nSaldo en peluquería: $${remaining.toLocaleString('es-CO')} COP\n\nAdjunto aquí el comprobante de transferencia.`
        : `Hola ISIVI 👋\nQuiero confirmar mi pedido de productos.\n\nOrden: ${b.code}\nCliente: ${b.customerName || 'Cliente'}\nProductos: ${serviceName}\nTotal: $${total.toLocaleString('es-CO')} COP\n\nAdjunto aquí el comprobante de transferencia.`;

    const opened = openWhatsApp(message, SALON_WHATSAPP_DETAL);
    if (!opened) {
        showToast(`Puedes escribirnos directamente al +57 300 894 9050 con el código ${b.code}`, 'info');
    }
}

function retryPaymentFromSuccess() {
    closeSuccessConfirmation();
    openPaymentModal();
}

function lookupCurrentBookingFromSuccess() {
    const code = currentSuccessBooking ? currentSuccessBooking.code : '';
    const phone = currentSuccessBooking ? currentSuccessBooking.phone : '';
    closeSuccessConfirmation();
    openLookupModal(code || phone);
}

// ============ CLIENT RESERVATION LOOKUP ============
function openLookupModal(prefillQuery = '', prefillPhone = '') {
    const modal = document.getElementById('modal-consultar-reserva');
    if (!modal) return;
    trackEvent('reservation_lookup_opened', { prefilled: Boolean(prefillQuery || prefillPhone) });
    const val = prefillQuery || prefillPhone || '';
    const queryInput = document.getElementById('lookup-query');
    if (queryInput && val) queryInput.value = val;
    modal.classList.remove('hidden');
    updateMobileContextBar();
    if (val) {
        document.getElementById('lookup-form').dispatchEvent(new Event('submit'));
    }
}

function closeLookupModal() {
    const modal = document.getElementById('modal-consultar-reserva');
    if (modal) modal.classList.add('hidden');
    updateMobileContextBar();
}

async function handleLookupSubmit(e) {
    if (e) e.preventDefault();
    const queryInput = document.getElementById('lookup-query');
    const query = (queryInput?.value || document.getElementById('lookup-code')?.value || '').trim();
    const resultCard = document.getElementById('lookup-result-card');
    const errorEl = document.getElementById('lookup-error-message');

    if (errorEl) errorEl.classList.add('hidden');

    if (!query) {
        if (errorEl) {
            errorEl.querySelector('span').textContent = 'Ingresa tu código de reserva o WhatsApp para consultar el estado de tu reserva.';
            errorEl.classList.remove('hidden');
        } else {
            showToast('Ingresa tu código de reserva o WhatsApp', 'error');
        }
        if (queryInput) queryInput.focus();
        return;
    }

    try {
        const data = await apiGet(`/reservas/consultar?query=${encodeURIComponent(query)}`);
        currentLookupBooking = mapFromApiReserva(data);

        const isPureOrder = currentLookupBooking.isPureOrder;
        const isMixed = currentLookupBooking.isMixed;
        const isPureAppointment = currentLookupBooking.isPureAppointment;
        const hasAppointment = currentLookupBooking.hasAppointment;

        document.getElementById('lookup-res-code').textContent = currentLookupBooking.code;
        document.getElementById('lookup-res-name').textContent = currentLookupBooking.customerName;

        const datetimeWrap = document.getElementById('lookup-res-datetime-wrap');
        const datetimeEl = document.getElementById('lookup-res-datetime');
        if (datetimeWrap) {
            datetimeWrap.classList.toggle('hidden', isPureOrder);
        }
        if (datetimeEl && !isPureOrder) {
            datetimeEl.textContent = `${currentLookupBooking.date} · ${currentLookupBooking.time}`;
        }

        const itemsLabel = document.getElementById('lookup-res-items-label');
        if (itemsLabel) {
            if (isPureAppointment) {
                itemsLabel.textContent = 'Servicios';
            } else if (isPureOrder) {
                itemsLabel.textContent = 'Productos y Kits';
            } else {
                itemsLabel.textContent = 'Servicios y Productos';
            }
        }
        const items = Array.isArray(currentLookupBooking.services) ? currentLookupBooking.services.join(', ') : currentLookupBooking.services;
        document.getElementById('lookup-res-items').textContent = items;

        const deliveryWrap = document.getElementById('lookup-res-delivery-wrap');
        const deliveryLabel = document.getElementById('lookup-res-delivery-label');
        const deliveryEl = document.getElementById('lookup-res-delivery');
        if (deliveryWrap) {
            if (isPureOrder || isMixed) {
                deliveryWrap.classList.remove('hidden');
                if (deliveryLabel) {
                    deliveryLabel.textContent = isPureOrder ? 'Entrega' : 'Entrega de productos';
                }
                if (deliveryEl) {
                    const deliveryMethodNormalized = String(currentLookupBooking.tipoEntrega || currentLookupBooking.deliveryMethod || 'pickup').trim().toLowerCase();
                    const isDeliv = deliveryMethodNormalized === 'delivery' || deliveryMethodNormalized === 'domicilio';
                    const dir = currentLookupBooking.deliveryAddress || currentLookupBooking.direccionEntrega || '';
                    deliveryEl.innerHTML = isDeliv 
                        ? `<span class="text-amber-300 font-bold"><i class="fa-solid fa-truck mr-1"></i> Envío a domicilio:</span> ${dir || 'Dirección registrada'}`
                        : `<span class="text-isivi-gold font-bold"><i class="fa-solid fa-store mr-1"></i> Recoger en el local:</span> ISIVI Salón Cartagena`;
                }
            } else {
                deliveryWrap.classList.add('hidden');
            }
        }

        const total = Number(currentLookupBooking.subtotal) || (Number(currentLookupBooking.deposit) * 4) || 0;
        const depositWrap = document.getElementById('lookup-res-deposit-wrap');
        const balanceWrap = document.getElementById('lookup-res-balance-wrap');
        
        if (isPureOrder) {
            if (depositWrap) depositWrap.classList.add('hidden');
            if (balanceWrap) balanceWrap.classList.add('hidden');
        } else {
            if (depositWrap) depositWrap.classList.remove('hidden');
            if (balanceWrap) {
                balanceWrap.classList.remove('hidden');
                const balanceEl = document.getElementById('lookup-res-balance');
                if (balanceEl) balanceEl.textContent = `$${(currentLookupBooking.balance || 0).toLocaleString('es-CO')}`;
            }
            document.getElementById('lookup-res-deposit').textContent = `$${(currentLookupBooking.deposit || 0).toLocaleString('es-CO')}`;
        }
        document.getElementById('lookup-res-total').textContent = `$${total.toLocaleString('es-CO')}`;

        const badge = document.getElementById('lookup-res-badge');
        badge.textContent = currentLookupBooking.status;
        const stLower = (currentLookupBooking.status || '').toLowerCase();
        if (stLower === 'confirmado' || stLower === 'pago confirmado' || stLower === 'listo para recoger' || stLower === 'listo para envío' || stLower === 'listo para envio' || stLower === 'en camino' || stLower === 'entregado' || stLower === 'recogido' || stLower === 'realizada') {
            badge.className = 'px-2.5 py-1 rounded-full text-[10px] font-bold bg-emerald-950 text-emerald-300 border border-emerald-500/30';
        } else if (stLower === 'cancelada' || stLower === 'denegada' || stLower === 'expirada') {
            badge.className = 'px-2.5 py-1 rounded-full text-[10px] font-bold bg-red-950 text-red-300 border border-red-500/30';
        } else {
            badge.className = 'px-2.5 py-1 rounded-full text-[10px] font-bold bg-amber-950 text-amber-300 border border-amber-500/30';
        }

        const isCancelled = currentLookupBooking.status === 'Cancelada';
        const isDenied = currentLookupBooking.status === 'Denegada';
        const hoursRemaining = hasAppointment ? calculateHoursUntilAppointment(currentLookupBooking.date, currentLookupBooking.time) : 999;
        const isPast = hasAppointment && isPastTimeSlot(currentLookupBooking.date, currentLookupBooking.time);
        
        const cancelPolicyNotice = document.getElementById('lookup-cancel-policy-notice');
        const btnCancel = document.getElementById('lookup-btn-cancel');
        const btnReschedule = document.getElementById('lookup-btn-reschedule');

        if (isPureOrder) {
            // VISTA ESPECÍFICA PARA PEDIDOS
            if (cancelPolicyNotice) {
                let stepsHtml = '';
                const st = (currentLookupBooking.status || currentLookupBooking.estado || '').trim();
                const ep = (currentLookupBooking.estadoPedido || currentLookupBooking.orderStatus || '').trim().toUpperCase();
                const deliveryMethodNormalized = String(currentLookupBooking.tipoEntrega || currentLookupBooking.deliveryMethod || 'pickup').trim().toLowerCase();
                const isDeliv = deliveryMethodNormalized === 'delivery' || deliveryMethodNormalized === 'domicilio';
                const dir = currentLookupBooking.deliveryAddress || currentLookupBooking.direccionEntrega || '';

                if (isDeliv) {
                    // FLUJO DOMICILIO: Pago confirmado -> En preparación -> Listo para envío -> En camino -> Entregado
                    const isPaid = ['PAGO CONFIRMADO', 'CONFIRMADO', 'EN PREPARACIÓN', 'EN PREPARACION', 'LISTO PARA ENVÍO', 'LISTO PARA ENVIO', 'EN CAMINO', 'ENTREGADO'].includes(st.toUpperCase())
                        || ['PENDIENTE_PREPARACION', 'EN_PREPARACION', 'LISTO_ENVIO', 'EN_CAMINO', 'ENTREGADO'].includes(ep);

                    const isPrep = ['EN PREPARACIÓN', 'EN PREPARACION', 'LISTO PARA ENVÍO', 'LISTO PARA ENVIO', 'EN CAMINO', 'ENTREGADO'].includes(st.toUpperCase())
                        || ['EN_PREPARACION', 'LISTO_ENVIO', 'EN_CAMINO', 'ENTREGADO'].includes(ep);

                    const isReadyShip = ['LISTO PARA ENVÍO', 'LISTO PARA ENVIO', 'EN CAMINO', 'ENTREGADO'].includes(st.toUpperCase())
                        || ['LISTO_ENVIO', 'EN_CAMINO', 'ENTREGADO'].includes(ep);

                    const isOnTheWay = ['EN CAMINO', 'ENTREGADO'].includes(st.toUpperCase())
                        || ['EN_CAMINO', 'ENTREGADO'].includes(ep);

                    const isDelivered = st.toUpperCase() === 'ENTREGADO' || ep === 'ENTREGADO';

                    stepsHtml = `
                        <div class="p-3 bg-stone-950 rounded-2xl border border-stone-800 space-y-2 text-xs">
                            <span class="text-[10px] text-isivi-300 uppercase tracking-wider font-bold block text-center">CONSULTA DE PEDIDO · ESTADO</span>
                            <div class="grid grid-cols-5 gap-1 text-center text-[8px] sm:text-[9px] font-bold pt-1">
                                <div class="p-1 rounded-lg ${isPaid ? 'bg-emerald-950 text-emerald-300 border border-emerald-500/40' : 'bg-stone-900 text-stone-500'}">✓ Pagado</div>
                                <div class="p-1 rounded-lg ${isPrep ? 'bg-amber-950 text-amber-300 border border-amber-500/40' : 'bg-stone-900 text-stone-500'}">● Prep.</div>
                                <div class="p-1 rounded-lg ${isReadyShip ? 'bg-emerald-950 text-emerald-300 border border-emerald-500/40' : 'bg-stone-900 text-stone-500'}">📦 Listo</div>
                                <div class="p-1 rounded-lg ${isOnTheWay ? 'bg-emerald-950 text-emerald-300 border border-emerald-500/40' : 'bg-stone-900 text-stone-500'}">🚚 Camino</div>
                                <div class="p-1 rounded-lg ${isDelivered ? 'bg-emerald-900 text-white font-extrabold shadow' : 'bg-stone-900 text-stone-500'}">✨ Entregado</div>
                            </div>
                            <div class="pt-2 text-[11px] text-stone-300 text-center">
                                <p><strong>Modalidad:</strong> <span class="text-amber-300 font-bold">🏠 Envío a Domicilio</span></p>
                                <p class="text-white mt-0.5"><strong>Dirección:</strong> ${dir || 'Registrada en la orden'}</p>
                            </div>
                        </div>
                    `;
                } else {
                    // FLUJO PICKUP: Pago confirmado -> En preparación -> Listo para recoger -> Recogido
                    const isPaid = ['PAGO CONFIRMADO', 'CONFIRMADO', 'EN PREPARACIÓN', 'EN PREPARACION', 'LISTO PARA RECOGER', 'RECOGIDO', 'ENTREGADO'].includes(st.toUpperCase())
                        || ['PENDIENTE_PREPARACION', 'EN_PREPARACION', 'LISTO_RECOGER', 'RECOGIDO', 'ENTREGADO'].includes(ep);

                    const isPrep = ['EN PREPARACIÓN', 'EN PREPARACION', 'LISTO PARA RECOGER', 'RECOGIDO', 'ENTREGADO'].includes(st.toUpperCase())
                        || ['EN_PREPARACION', 'LISTO_RECOGER', 'RECOGIDO', 'ENTREGADO'].includes(ep);

                    const isReadyPickup = ['LISTO PARA RECOGER', 'RECOGIDO', 'ENTREGADO'].includes(st.toUpperCase())
                        || ['LISTO_RECOGER', 'RECOGIDO', 'ENTREGADO'].includes(ep);

                    const isPickedUp = ['RECOGIDO', 'ENTREGADO'].includes(st.toUpperCase())
                        || ['RECOGIDO', 'ENTREGADO'].includes(ep);

                    stepsHtml = `
                        <div class="p-3 bg-stone-950 rounded-2xl border border-stone-800 space-y-2 text-xs">
                            <span class="text-[10px] text-isivi-300 uppercase tracking-wider font-bold block text-center">CONSULTA DE PEDIDO · ESTADO</span>
                            <div class="grid grid-cols-4 gap-1 text-center text-[9px] font-bold pt-1">
                                <div class="p-1.5 rounded-lg ${isPaid ? 'bg-emerald-950 text-emerald-300 border border-emerald-500/40' : 'bg-stone-900 text-stone-500'}">✓ Pagado</div>
                                <div class="p-1.5 rounded-lg ${isPrep ? 'bg-amber-950 text-amber-300 border border-amber-500/40' : 'bg-stone-900 text-stone-500'}">● En Prep.</div>
                                <div class="p-1.5 rounded-lg ${isReadyPickup ? 'bg-emerald-950 text-emerald-300 border border-emerald-500/40' : 'bg-stone-900 text-stone-500'}">📍 Listo</div>
                                <div class="p-1.5 rounded-lg ${isPickedUp ? 'bg-emerald-900 text-white font-extrabold shadow' : 'bg-stone-900 text-stone-500'}">✨ Recogido</div>
                            </div>
                            <div class="pt-2 text-[11px] text-stone-300 text-center">
                                <p><strong>Punto de entrega:</strong> 📍 ISIVI Salón & Cuidado Capilar · Cartagena, Colombia</p>
                                <p class="text-[10px] text-stone-500 mt-0.5">Presenta tu código <strong>${currentLookupBooking.code}</strong> o WhatsApp en el local.</p>
                            </div>
                        </div>
                    `;
                }
                cancelPolicyNotice.className = 'p-0';
                cancelPolicyNotice.innerHTML = stepsHtml;
                cancelPolicyNotice.classList.remove('hidden');
            }
            if (btnCancel) btnCancel.style.display = 'none';
            if (btnReschedule) btnReschedule.style.display = 'none';
        } else {
            // VISTA ESPECÍFICA PARA CITAS
            if (cancelPolicyNotice) {
                if (isCancelled) {
                    cancelPolicyNotice.className = 'p-3 rounded-2xl bg-stone-900 border border-stone-800 text-stone-400 text-xs text-center space-y-0.5';
                    cancelPolicyNotice.innerHTML = `
                        <p class="font-bold text-stone-300"><i class="fa-solid fa-ban text-stone-400 mr-1.5"></i> Esta cita ya fue cancelada.</p>
                        <p class="text-[11px] text-stone-500">${currentLookupBooking.canceladaPor ? `Cancelada por: ${currentLookupBooking.canceladaPor}` : ''}</p>
                    `;
                    cancelPolicyNotice.classList.remove('hidden');
                    if (btnCancel) btnCancel.style.display = 'none';
                    if (btnReschedule) btnReschedule.style.display = 'none';
                } else if (isDenied) {
                    cancelPolicyNotice.className = 'p-3 rounded-2xl bg-red-950/60 border border-red-800/40 text-red-300 text-xs text-center';
                    cancelPolicyNotice.innerHTML = `<p class="font-bold"><i class="fa-solid fa-xmark mr-1.5"></i> Esta solicitud fue denegada.</p>`;
                    cancelPolicyNotice.classList.remove('hidden');
                    if (btnCancel) btnCancel.style.display = 'none';
                    if (btnReschedule) btnReschedule.style.display = 'none';
                } else if (isPast) {
                    cancelPolicyNotice.className = 'p-3 rounded-2xl bg-stone-900 border border-stone-800 text-stone-400 text-xs text-center';
                    cancelPolicyNotice.innerHTML = `<p class="font-bold"><i class="fa-solid fa-clock-rotate-left mr-1.5"></i> Esta cita ya fue realizada.</p>`;
                    cancelPolicyNotice.classList.remove('hidden');
                    if (btnCancel) btnCancel.style.display = 'none';
                    if (btnReschedule) btnReschedule.style.display = 'none';
                } else if (hoursRemaining <= 24) {
                    cancelPolicyNotice.className = 'p-3 rounded-2xl bg-amber-950/70 border border-amber-500/50 text-amber-200 text-xs space-y-1.5';
                    cancelPolicyNotice.innerHTML = `
                        <div class="flex items-center gap-2 font-bold text-amber-300">
                            <i class="fa-solid fa-lock text-sm"></i>
                            <span>Cancelación no disponible</span>
                        </div>
                        <p class="text-[11px] text-amber-200/90 leading-tight">
                            Las cancelaciones deben realizarse con más de 24 horas de anticipación. Tu cita sigue activa.
                        </p>
                    `;
                    cancelPolicyNotice.classList.remove('hidden');
                    if (btnCancel) btnCancel.style.display = 'none';
                    if (btnReschedule) btnReschedule.style.display = 'flex';
                } else {
                    const deadlineText = formatCancellationDeadline(currentLookupBooking.date, currentLookupBooking.time);
                    cancelPolicyNotice.className = 'p-2.5 rounded-xl bg-stone-900/80 border border-stone-800 text-stone-400 text-[11px]';
                    cancelPolicyNotice.innerHTML = `
                        <i class="fa-solid fa-circle-info text-isivi-gold mr-1"></i> Puedes cancelar hasta el <strong>${deadlineText}</strong>.
                    `;
                    cancelPolicyNotice.classList.remove('hidden');
                    if (btnCancel) btnCancel.style.display = 'flex';
                    if (btnReschedule) btnReschedule.style.display = 'flex';
                }
            }
        }

        const isPendingPayment = currentLookupBooking.status === 'Pendiente Pago' || (currentLookupBooking.paymentStatus === 'PENDIENTE' && currentLookupBooking.paymentExpiration && new Date(currentLookupBooking.paymentExpiration).getTime() > Date.now());
        const holdBanner = document.getElementById('lookup-hold-banner');
        if (holdBanner) {
            if (isPendingPayment && !isPureOrder && hasAppointment) {
                setPendingHold(data);
                holdBanner.classList.remove('hidden');
                const expDate = new Date(currentLookupBooking.paymentExpiration);
                const expTime = expDate.toLocaleTimeString('es-CO', { hour: '2-digit', minute: '2-digit', hour12: true, timeZone: 'America/Bogota' });
                const countdownEl = document.getElementById('lookup-hold-countdown');
                if (countdownEl) countdownEl.textContent = `Hasta ${expTime}`;
            } else {
                holdBanner.classList.add('hidden');
            }
        }

        if (errorEl) errorEl.classList.add('hidden');
        resultCard.classList.remove('hidden');
        trackEvent('reservation_lookup', { lookup_success: true, is_order: isPureOrder });

    } catch (err) {
        resultCard.classList.add('hidden');
        if (errorEl) {
            errorEl.querySelector('span').textContent = err.message || 'No se encontró la reserva o pedido con los datos ingresados.';
            errorEl.classList.remove('hidden');
        } else {
            showToast(err.message || 'No se encontró la reserva o pedido con los datos ingresados.', 'error');
        }
        trackEvent('reservation_lookup', { lookup_success: false });
    }

}

function parseAppointmentDateTime(dateStr, timeStr) {
    if (!dateStr) return null;
    const [year, month, day] = dateStr.split('-').map(Number);
    if (!year || !month || !day) return null;
    
    let hours = 9;
    let minutes = 0;
    if (timeStr) {
        const match = timeStr.match(/(\d{1,2}):(\d{2})\s*(AM|PM)/i);
        if (match) {
            hours = Number(match[1]) % 12;
            if (match[3].toUpperCase() === 'PM') hours += 12;
            minutes = Number(match[2]);
        }
    }
    return new Date(year, month - 1, day, hours, minutes, 0);
}

function calculateHoursUntilAppointment(dateStr, timeStr) {
    const apptDate = parseAppointmentDateTime(dateStr, timeStr);
    if (!apptDate) return 999;
    const now = getBogotaDateNow();
    return (apptDate.getTime() - now.getTime()) / (1000 * 60 * 60);
}

function formatCancellationDeadline(dateStr, timeStr) {
    const apptDate = parseAppointmentDateTime(dateStr, timeStr);
    if (!apptDate) return '';
    const deadline = new Date(apptDate.getTime() - 24 * 60 * 60 * 1000);
    const day = String(deadline.getDate()).padStart(2, '0');
    const month = String(deadline.getMonth() + 1).padStart(2, '0');
    const year = deadline.getFullYear();
    let hours = deadline.getHours();
    const minutes = String(deadline.getMinutes()).padStart(2, '0');
    const ampm = hours >= 12 ? 'PM' : 'AM';
    hours = hours % 12 || 12;
    const formattedHours = String(hours).padStart(2, '0');
    return `${day}/${month}/${year} a las ${formattedHours}:${minutes} ${ampm}`;
}

function openClientRescheduleFromLookup() {
    if (!currentLookupBooking) return;
    const modal = document.getElementById('modal-client-reschedule');
    const dateInput = document.getElementById('client-reschedule-date');
    const todayStr = formatDateKey(new Date());
    dateInput.min = todayStr;
    dateInput.value = currentLookupBooking.date || todayStr;
    handleClientRescheduleDateChange();
    modal.classList.remove('hidden');
}

function closeClientRescheduleModal() {
    const modal = document.getElementById('modal-client-reschedule');
    if (modal) modal.classList.add('hidden');
}

async function handleClientRescheduleDateChange() {
    const date = document.getElementById('client-reschedule-date').value;
    const select = document.getElementById('client-reschedule-time');
    select.innerHTML = '<option value="">Cargando horarios...</option>';
    if (!date) return;

    try {
        const occupiedTimes = await apiGet(`/reservas/disponibilidad?fecha=${encodeURIComponent(date)}`);
        const slots = appointmentTimeSlots.filter(h => !isPastTimeSlot(date, h) && (!occupiedTimes.includes(h) || (currentLookupBooking && currentLookupBooking.date === date && currentLookupBooking.time === h)));
        if (slots.length === 0) {
            select.innerHTML = '<option value="">No hay horarios disponibles para esta fecha</option>';
        } else {
            select.innerHTML = slots.map(h => `<option value="${h}" ${currentLookupBooking && currentLookupBooking.time === h ? 'selected' : ''}>${h}</option>`).join('');
        }
    } catch (err) {
        console.error(err);
        const fallbackSlots = appointmentTimeSlots.filter(h => !isPastTimeSlot(date, h));
        if (fallbackSlots.length === 0) {
            select.innerHTML = '<option value="">No hay horarios disponibles para esta fecha</option>';
        } else {
            select.innerHTML = fallbackSlots.map(h => `<option value="${h}">${h}</option>`).join('');
        }
    }
}

let isSubmittingReschedule = false;

async function submitClientReschedule() {
    if (!currentLookupBooking || isSubmittingReschedule) return;
    const newDate = document.getElementById('client-reschedule-date').value;
    const newTime = document.getElementById('client-reschedule-time').value;
    const errorEl = document.getElementById('client-reschedule-error');
    if (errorEl) errorEl.classList.add('hidden');

    if (!newDate || !newTime) {
        if (errorEl) {
            errorEl.querySelector('span').textContent = 'Selecciona una fecha y un horario disponibles.';
            errorEl.classList.remove('hidden');
        } else {
            showToast('Selecciona una fecha y un horario disponibles', 'error');
        }
        return;
    }

    isSubmittingReschedule = true;
    try {
        const res = await apiPatch(`/reservas/${currentLookupBooking.id}/reprogramar`, {
            codigoReserva: currentLookupBooking.code,
            telefono: currentLookupBooking.phone,
            fechaCita: newDate,
            horaCita: newTime
        });
        showToast('Cita reprogramada con éxito', 'success');
        trackEvent('reservation_rescheduled', { success: true });
        closeClientRescheduleModal();
        currentLookupBooking = mapFromApiReserva(res);
        handleLookupSubmit();
        await loadAllData();
    } catch (err) {
        const errorText = (err.status === 409 || (err.message && err.message.includes('disponible')))
            ? 'Este horario acaba de ser reservado por otro cliente. Selecciona otro horario.'
            : (err.message || 'No se pudo reprogramar la cita');
        if (errorEl) {
            errorEl.querySelector('span').textContent = errorText;
            errorEl.classList.remove('hidden');
            handleClientRescheduleDateChange();
        } else {
            showToast(errorText, 'error');
        }
    } finally {
        isSubmittingReschedule = false;
    }
}

// ============ MODAL PROPIO DE CANCELACIÓN CLIENTE (SIN WINDOW.PROMPT) ============
let selectedCancelReasonChip = '';
let isCancellingBooking = false;

function openClientCancelModalFromLookup() {
    if (!currentLookupBooking) return;

    // Validar anticipadamente horas restantes
    if (currentLookupBooking.date && currentLookupBooking.time) {
        const hours = calculateHoursUntilAppointment(currentLookupBooking.date, currentLookupBooking.time);
        if (hours <= 24) {
            showToast('Las cancelaciones deben realizarse con más de 24 horas de anticipación.', 'info');
            return;
        }
    }

    selectedCancelReasonChip = '';
    const customReasonInput = document.getElementById('client-cancel-custom-reason');
    if (customReasonInput) customReasonInput.value = '';

    document.querySelectorAll('.cancel-reason-chip').forEach(btn => {
        btn.className = 'cancel-reason-chip px-2.5 py-2 bg-stone-900 hover:bg-stone-800 border border-stone-700 hover:border-isivi-gold text-stone-300 rounded-xl text-[11px] text-center transition';
    });

    const codeEl = document.getElementById('cancel-modal-code');
    const datetimeEl = document.getElementById('cancel-modal-datetime');
    if (codeEl) codeEl.textContent = currentLookupBooking.code || 'ISV-0000';
    if (datetimeEl) datetimeEl.textContent = `${currentLookupBooking.date || ''} · ${currentLookupBooking.time || ''}`;

    const errBox = document.getElementById('client-cancel-error');
    if (errBox) errBox.classList.add('hidden');

    const modal = document.getElementById('modal-client-cancel');
    if (modal) modal.classList.remove('hidden');
}

function closeClientCancelModal() {
    const modal = document.getElementById('modal-client-cancel');
    if (modal) modal.classList.add('hidden');
}

function selectCancelReason(reason) {
    selectedCancelReasonChip = reason;
    document.querySelectorAll('.cancel-reason-chip').forEach(btn => {
        const isSelected = btn.textContent.trim() === reason || (reason === 'Ya no necesito el servicio' && btn.textContent.includes('Ya no lo necesito'));
        if (isSelected) {
            btn.className = 'cancel-reason-chip px-2.5 py-2 bg-red-950 text-red-200 border-2 border-red-500 rounded-xl text-[11px] font-bold text-center transition';
        } else {
            btn.className = 'cancel-reason-chip px-2.5 py-2 bg-stone-900 hover:bg-stone-800 border border-stone-700 hover:border-isivi-gold text-stone-300 rounded-xl text-[11px] text-center transition';
        }
    });
}

async function submitClientCancellationModal() {
    if (!currentLookupBooking || isCancellingBooking) return;

    const customInput = document.getElementById('client-cancel-custom-reason');
    const customReason = customInput ? customInput.value.trim() : '';
    const finalReason = customReason || selectedCancelReasonChip || 'Sin motivo indicado';

    const btnSubmit = document.getElementById('btn-submit-client-cancel');
    const errBox = document.getElementById('client-cancel-error');
    if (errBox) errBox.classList.add('hidden');

    if (btnSubmit) {
        btnSubmit.disabled = true;
        btnSubmit.innerHTML = '<i class="fa-solid fa-spinner fa-spin mr-1.5"></i> Cancelando...';
    }

    isCancellingBooking = true;
    try {
        const res = await apiPatch(`/reservas/${currentLookupBooking.id}/cancelar`, {
            codigoReserva: currentLookupBooking.code,
            telefono: currentLookupBooking.phone,
            motivoCancelacion: finalReason
        });

        closeClientCancelModal();
        showToast(res.mensaje || 'Tu cita ha sido cancelada exitosamente y el horario fue liberado.', 'success');
        trackEvent('reservation_cancelled', { success: true });

        if (res.reserva) {
            currentLookupBooking = mapFromApiReserva(res.reserva);
        } else {
            currentLookupBooking = mapFromApiReserva(res);
        }
        handleLookupSubmit();
        await loadAllData();
    } catch (err) {
        console.warn('Error cancelacion cliente', err);
        const isNotAllowed = err.status === 409 || (err.data && err.data.code === 'CANCELACION_NO_PERMITIDA');
        const errorMsg = isNotAllowed 
            ? 'Las cancelaciones deben realizarse con más de 24 horas de anticipación. Tu cita sigue activa.'
            : (err.message || 'No se pudo cancelar la cita.');

        if (errBox) {
            errBox.querySelector('span').textContent = errorMsg;
            errBox.classList.remove('hidden');
        } else {
            showToast(errorMsg, 'error');
        }
    } finally {
        isCancellingBooking = false;
        if (btnSubmit) {
            btnSubmit.disabled = false;
            btnSubmit.innerHTML = '<i class="fa-solid fa-ban"></i> <span>Confirmar cancelación</span>';
        }
    }
}

function whatsappClientBookingFromLookup() {
    if (!currentLookupBooking) return;
    trackEvent('whatsapp_click', { context: 'booking_inquiry' });
    const b = currentLookupBooking;
    const msg = `Hola ISIVI 👋\nTengo una consulta sobre mi reserva ${b.code} a nombre de ${b.customerName}.`;
    openWhatsApp(msg, SALON_WHATSAPP_DETAL);
}

// ============ PROCESAMIENTO DE RESERVAS (CONCURRENCY SAFE & WHATSAPP) ============
async function processWhatsAppReservation() {
    if (isSavingReservation) return;

    // 1. Validar campos requeridos dentro del modal o carrito
    const validation = validateReservationForm(true);
    if (!validation.isValid) {
        return;
    }

    const name = (document.getElementById('cart-cust-name')?.value || document.getElementById('cust-name')?.value || '').trim();
    const phone = (document.getElementById('cart-cust-phone')?.value || document.getElementById('cust-phone')?.value || '').trim();
    const email = (document.getElementById('cart-cust-email')?.value || document.getElementById('cust-email')?.value || '').trim();
    const city = (document.getElementById('cart-cust-city')?.value || document.getElementById('cust-city')?.value || '').trim() || 'Cartagena';
    const hasProducts = selectedProducts.length > 0 || selectedKits.length > 0;
    const hasServices = selectedServices.length > 0;
    const deliveryMethod = hasProducts ? (document.getElementById('cart-delivery-method')?.value || document.getElementById('delivery-method')?.value || '') : '';
    const deliveryAddress = hasProducts && deliveryMethod === 'delivery' ? (document.getElementById('cart-delivery-address')?.value || document.getElementById('delivery-address')?.value || '').trim() : '';

    const selectedNames = [
        ...selectedServices.map(id => servicesData.find(s => s.id === id)?.name),
        ...selectedProducts.map(id => `${productsData.find(p => p.id === id)?.name || ''} x${itemQuantity('product', id)}`),
        ...selectedKits.map(id => `${kitsData.find(k => k.id === id)?.name || ''} x${itemQuantity('kit', id)}`)
    ].filter(Boolean);

    const nuevaReserva = {
        nombreCliente: name,
        telefono: phone,
        email: email,
        ciudad: city,
        items: selectedNames,
        fechaCita: hasServices ? selectedDateStr : null,
        horaCita: hasServices ? selectedTimeSlot : null,
        medioPago: hasServices ? currentPaymentMethod.toUpperCase() : 'POR DEFINIR',
        tipoEntrega: deliveryMethod,
        direccionEntrega: deliveryAddress,
        estado: 'Pendiente Comprobante',
        itemsInventario: [
            ...selectedServices.map(id => ({ id, tipo: 'servicio', cantidad: 1 })),
            ...selectedProducts.map(id => ({ id, tipo: 'producto', cantidad: itemQuantity('product', id) })),
            ...selectedKits.map(id => ({ id, tipo: 'kit', cantidad: itemQuantity('kit', id) }))
        ]
    };

    const btnText = document.getElementById('btn-submit-whatsapp-text');
    const btnSubmit = document.getElementById('btn-submit-whatsapp-payment');
    if (btnText) btnText.innerHTML = '<i class="fa-solid fa-spinner fa-spin mr-1"></i> Creando reserva...';
    if (btnSubmit) btnSubmit.disabled = true;

    isSavingReservation = true;
    trackEvent('payment_started', { method: 'TRANSFER', channel: currentPaymentMethod });
    let guardada = null;
    try {
        guardada = await apiPost('/reservas', nuevaReserva);

        if (!guardada || !guardada.codigoReserva) {
            throw new Error('No se pudo generar el código de reserva en el servidor.');
        }

        const mapped = mapFromApiReserva(guardada);
        bookingsList.unshift(mapped);

        // Construcción del mensaje dinámico con datos reales
        const serviceName = Array.isArray(mapped.services) ? mapped.services.join(', ') : (mapped.services || 'Servicio ISIVI');
        const friendlyDate = mapped.date ? formatFriendlyDate(mapped.date) : 'Por coordinar';
        const depositFormatted = (mapped.deposit || 0).toLocaleString('es-CO');
        const totalFormatted = (mapped.subtotal || 0).toLocaleString('es-CO');
        const remainingFormatted = Math.max(0, (mapped.subtotal || 0) - (mapped.deposit || 0)).toLocaleString('es-CO');

        const waMessage = hasServices
            ? `Hola ISIVI 👋\nQuiero enviar el comprobante de mi anticipo.\n\nReserva: ${mapped.code}\nCliente: ${mapped.customerName}\nServicio: ${serviceName}\nFecha: ${friendlyDate}\nHora: ${mapped.time || 'N/A'}\nAnticipo: $${depositFormatted} COP\nSaldo en peluquería: $${remainingFormatted} COP\n\nAdjunto aquí el comprobante de transferencia.`
            : `Hola ISIVI 👋\nQuiero confirmar mi pedido de productos.\n\nOrden: ${mapped.code}\nCliente: ${mapped.customerName}\nProductos: ${serviceName}\nTotal: $${totalFormatted} COP\nEntrega: ${mapped.deliveryMethod === 'delivery' ? `Envío a domicilio (${mapped.deliveryAddress})` : 'Recoger en local'}\n\nAdjunto aquí el comprobante de transferencia.`;

        // Apertura directa de WhatsApp
        const opened = openWhatsApp(waMessage, SALON_WHATSAPP_DETAL);
        if (!opened) {
            console.warn('No se pudo abrir WhatsApp automáticamente.');
        }

        closePaymentModal();
        showToast("Pedido creado. Tienes 24 horas para enviar el comprobante.", "success");
        showSuccessConfirmation(mapped, 'PENDING');
        await refreshCheckoutAfterReservation();
    } catch (err) {
        console.error('Error al procesar reserva:', err);
        const summaryEl = document.getElementById('payment-modal-error-summary');
        const listEl = document.getElementById('payment-modal-error-list');

        let errorText = err.message || 'No se pudo guardar la reserva en el servidor. Intenta nuevamente.';
        if (err.errorCode === 'HORARIO_NO_DISPONIBLE' || (err.status === 409 && (!err.errorCode || err.errorCode === 'HORARIO_NO_DISPONIBLE'))) {
            errorText = 'El horario seleccionado acaba de ser reservado por otro cliente. Por favor selecciona otro horario en la agenda.';
            await renderTimeSlots();
        } else if (err.status === 409 || err.errorCode === 'STOCK_NO_DISPONIBLE' || (err.message && (err.message.includes('stock') || err.message.includes('agotase') || err.message.includes('disponible') || err.message.includes('unidades')))) {
            errorText = err.message || 'El producto ya no está disponible por cambios en el stock.';
            await refreshCatalogStock();
        } else if (err.errorCode === 'HORARIO_FUERA_DE_AGENDA') {
            errorText = 'La fecha u horario seleccionado no está disponible en la agenda del salón. Por favor selecciona otro turno.';
            await renderTimeSlots();
        } else if (err.errorCode === 'DATOS_INVALIDOS') {
            errorText = err.message || 'Por favor revisa los datos ingresados en el formulario.';
        } else if (err.status >= 500) {
            errorText = 'Ocurrió un error en el servidor al procesar la reserva. Intenta nuevamente o contáctanos por WhatsApp.';
        }

        if (summaryEl && listEl) {
            listEl.innerHTML = `<li>${errorText}</li>`;
            summaryEl.classList.remove('hidden');
            const modalScroll = document.querySelector('#payment-modal .max-h-\\[80vh\\]');
            if (modalScroll) modalScroll.scrollTo({ top: 0, behavior: 'smooth' });
        } else {
            showToast(errorText, 'error');
        }
    } finally {
        isSavingReservation = false;
        if (btnText) btnText.textContent = 'Enviar Comprobante por WhatsApp';
        if (btnSubmit) btnSubmit.disabled = false;
    }
}

async function processWompiPayment() {
    if (typeof navigator !== 'undefined' && !navigator.onLine) {
        showToast('Sin conexión a internet. Revisa tu conexión e inténtalo nuevamente.', 'error');
        return;
    }

    if (isSavingReservation) return;
    const name = (document.getElementById('cart-cust-name')?.value || document.getElementById('cust-name')?.value || '').trim();
    const phone = (document.getElementById('cart-cust-phone')?.value || document.getElementById('cust-phone')?.value || '').trim();
    const email = (document.getElementById('cart-cust-email')?.value || document.getElementById('cust-email')?.value || '').trim();
    const city = (document.getElementById('cart-cust-city')?.value || document.getElementById('cust-city')?.value || '').trim() || 'Cartagena';
    const hasProducts = selectedProducts.length > 0 || selectedKits.length > 0;
    const hasServices = selectedServices.length > 0;
    const deliveryMethod = hasProducts ? (document.getElementById('cart-delivery-method')?.value || document.getElementById('delivery-method')?.value || '') : '';
    const deliveryAddress = hasProducts && deliveryMethod === 'delivery' ? (document.getElementById('cart-delivery-address')?.value || document.getElementById('delivery-address')?.value || '').trim() : '';

    if (!name || !phone || !email || (!hasProducts && !hasServices)) return showToast('Completa tus datos de contacto y selecciona al menos un servicio o producto', 'error');
    if (hasProducts && hasServices) return showToast('Para Wompi, paga por separado el servicio y los productos. WhatsApp permite el pedido combinado.', 'error');
    if (hasProducts && deliveryMethod === 'delivery' && !deliveryAddress) return showToast('Ingresa la dirección para el envío a domicilio', 'error');

    try {
        const wompiDisponible = await apiGet('/pagos/wompi/disponible');
        if (!wompiDisponible.disponible) return showToast('Wompi no está configurado en el servidor.', 'error');
    } catch (err) {
        console.error(err);
        return showToast('No se pudo verificar la configuración de Wompi.', 'error');
    }

    // Si el usuario ya tiene una reserva pendiente activa para este mismo horario de cita, retomar directamente
    const activeHold = getPendingHold();
    if (activeHold && activeHold.id && hasServices && activeHold.date === selectedDateStr && activeHold.time === selectedTimeSlot) {
        return resumeWompiPayment(activeHold);
    }

    const nuevaReserva = {
        nombreCliente: name,
        telefono: phone,
        email: email,
        ciudad: city,
        medioPago: 'WOMPI',
        tipoEntrega: deliveryMethod,
        direccionEntrega: deliveryAddress,
        fechaCita: hasServices ? selectedDateStr : null,
        horaCita: hasServices ? selectedTimeSlot : null,
        itemsInventario: [
            ...selectedServices.map(id => ({ id, tipo: 'servicio', cantidad: 1 })),
            ...selectedProducts.map(id => ({ id, tipo: 'producto', cantidad: itemQuantity('product', id) })),
            ...selectedKits.map(id => ({ id, tipo: 'kit', cantidad: itemQuantity('kit', id) }))
        ]
    };

    isSavingReservation = true;
    let timeoutWarningTimer = setTimeout(() => {
        if (isSavingReservation) {
            showToast('Wompi está tardando más de lo esperado. Mantén esta pestaña abierta...', 'info');
        }
    }, 6000);

    try {
        const reserva = await apiPost('/reservas', nuevaReserva);
        const mapped = mapFromApiReserva({ ...reserva, estado: 'Pendiente Pago', estadoPago: 'PENDIENTE' });
        bookingsList.unshift(mapped);

        const checkout = await apiPost(`/pagos/wompi/preparar/${encodeURIComponent(reserva.id)}`, {});
        clearTimeout(timeoutWarningTimer);

        if (typeof WidgetCheckout === 'undefined') throw new Error('El widget de Wompi no pudo cargarse en el navegador.');

        const expiration = checkout.fechaExpiracionPago || reserva.fechaExpiracionPago || new Date(Date.now() + 15 * 60 * 1000).toISOString();
        setPendingHold({ ...reserva, fechaExpiracionPago: expiration });
        if (hasServices) {
            showToast('Horario reservado temporalmente mientras completas tu pago.', 'info');
        } else {
            showToast('Pedido creado. Tu unidad está reservada temporalmente durante 15 minutos.', 'success');
        }

        new WidgetCheckout({
            currency: checkout.moneda,
            amountInCents: checkout.montoCentavos,
            reference: checkout.referencia,
            publicKey: checkout.llavePublica,
            signature: { integrity: checkout.firmaIntegridad },
            redirectUrl: checkout.urlRedireccion
        }).open(async (result) => {
            const status = result?.transaction?.status || 'PENDING';
            if (status === 'APPROVED') {
                clearPendingHold();
                resetCartState();
                showToast("Pago aprobado. Pedido confirmado.", "success");
                showSuccessConfirmation(mapped, 'APPROVED');
                if (hasServices) await renderTimeSlots();
                await refreshCatalogStock();
            } else if (status === 'DECLINED') {
                clearPendingHold();
                syncCartState();
                showToast("El pago no fue aprobado. Puedes intentarlo nuevamente.", "error");
                showSuccessConfirmation(mapped, 'DECLINED');
                if (hasServices) await renderTimeSlots();
                await refreshCatalogStock();
            } else {
                // Cancelado o cerrado sin finalizar:
                setPendingHold({ ...reserva, fechaExpiracionPago: expiration });
                syncCartState();
                openCartDrawer();
                showToast("Pago pendiente. Puedes retomar el pago mientras tu reserva temporal siga vigente.", "warning");
                if (hasServices) await renderTimeSlots();
                await refreshCatalogStock();
            }
        });

        closePaymentModal();

    } catch (err) {
        clearTimeout(timeoutWarningTimer);
        console.error(err);
        if (err.status === 409 || (err.message && (err.message.includes('stock') || err.message.includes('agotase') || err.message.includes('disponible') || err.message.includes('unidades')))) {
            showToast(err.message || 'El producto ya no está disponible por cambios en el stock.', 'error');
            await refreshCatalogStock();
        } else if (err.status === 409 || (err.message && err.message.includes('horario'))) {
            showToast('Este horario acaba de ser reservado por otro cliente. Selecciona otro horario.', 'error');
            await renderTimeSlots();
        } else if (err.name === 'AbortError' || err.status === 504 || err.status === 503) {
            showToast('No pudimos conectar con la pasarela. Puedes reintentar sin perder tu carrito.', 'warning');
        } else {
            showToast(err.message || 'No se pudo preparar el pago Wompi.', 'error');
        }
    } finally {
        clearTimeout(timeoutWarningTimer);
        isSavingReservation = false;
    }
}



// ============ MEJORA 2: DASHBOARD EJECUTIVO ADMIN ============
function switchAdminTab(tabName) {
    const tabs = ['dashboard', 'orders', 'products', 'services', 'bookings', 'admins', 'banners', 'history', 'settings'];
    tabs.forEach(t => {
        const tabEl = document.getElementById(`adm-tab-${t}`);
        const navEl = document.getElementById(`adm-nav-${t}`);
        if (tabEl) tabEl.classList.add('hidden');
        if (navEl) navEl.className = 'px-4 py-2 rounded-xl text-xs font-bold transition flex items-center gap-2 text-isivi-300 hover:text-white bg-stone-950 border border-stone-800';
    });

    const activeTab = document.getElementById(`adm-tab-${tabName}`);
    const activeNav = document.getElementById(`adm-nav-${tabName}`);
    if (activeTab) activeTab.classList.remove('hidden');
    if (activeNav) activeNav.className = 'px-4 py-2 rounded-xl text-xs font-bold transition flex items-center gap-2 bg-isivi-gold text-isivi-black shadow';

    if (tabName === 'dashboard') loadDashboardData();
    if (tabName === 'orders') loadAdminOrders();
    if (tabName === 'bookings') { 
        loadAdminData(); 
        renderAdminWeeklyScheduleView();
        renderAdminRecurringBlocksList();
        renderAdminExceptionsList();
        renderAdminWeeklyCalendar(); 
        renderAdminScheduleManager();
    }

    if (tabName === 'history') loadHistoryBookings();
    if (tabName === 'products') renderAdminProductsTable();
    if (tabName === 'services') renderAdminServicesTable();
    if (tabName === 'settings') renderAdminBusinessConfig();
}

function renderAdminBusinessConfig() {
    const errorEl = document.getElementById('admin-business-config-error');
    const successEl = document.getElementById('admin-business-config-success');
    if (errorEl) errorEl.classList.add('hidden');
    if (successEl) successEl.classList.add('hidden');

    const ciudadEl = document.getElementById('admin-cfg-ciudad');
    const paisEl = document.getElementById('admin-cfg-pais');
    const diasEl = document.getElementById('admin-cfg-dias');
    const aperturaEl = document.getElementById('admin-cfg-apertura');
    const cierreEl = document.getElementById('admin-cfg-cierre');
    const mayoristaEl = document.getElementById('admin-cfg-mayorista');

    if (ciudadEl) ciudadEl.value = businessConfiguration.ciudad || 'Cartagena';
    if (paisEl) paisEl.value = businessConfiguration.pais || 'Colombia';
    if (diasEl) diasEl.value = businessConfiguration.diasAtencion || 'Mar - Sáb';
    if (aperturaEl) aperturaEl.value = businessConfiguration.horaApertura || '08:00';
    if (cierreEl) cierreEl.value = businessConfiguration.horaCierre || '19:00';
    if (mayoristaEl) mayoristaEl.value = businessConfiguration.telefonoMayorista || '+57 300 962 3174';
}

async function handleAdminBusinessConfigSubmit(event) {
    event.preventDefault();
    const errorEl = document.getElementById('admin-business-config-error');
    const successEl = document.getElementById('admin-business-config-success');
    const submitBtn = document.getElementById('btn-save-business-config');

    if (errorEl) errorEl.classList.add('hidden');
    if (successEl) successEl.classList.add('hidden');

    const ciudad = (document.getElementById('admin-cfg-ciudad')?.value || '').trim();
    const pais = (document.getElementById('admin-cfg-pais')?.value || '').trim();
    const diasAtencion = (document.getElementById('admin-cfg-dias')?.value || '').trim();
    const horaApertura = (document.getElementById('admin-cfg-apertura')?.value || '').trim();
    const horaCierre = (document.getElementById('admin-cfg-cierre')?.value || '').trim();
    const telefonoMayorista = (document.getElementById('admin-cfg-mayorista')?.value || '').trim();

    if (!ciudad || !pais || !diasAtencion || !horaApertura || !horaCierre) {
        if (errorEl) {
            errorEl.querySelector('span').textContent = 'Por favor completa todos los campos requeridos.';
            errorEl.classList.remove('hidden');
        }
        return;
    }

    const payload = {
        ciudad,
        pais,
        diasAtencion,
        horaApertura,
        horaCierre,
        telefonoMayorista
    };

    if (submitBtn) {
        submitBtn.disabled = true;
        submitBtn.innerHTML = '<i class="fa-solid fa-spinner fa-spin mr-1"></i> Guardando...';
    }

    try {
        const guardada = await apiPut('/configuracion', payload);
        businessConfiguration = { ...businessConfiguration, ...guardada };
        applyBusinessConfigToUI();
        if (successEl) successEl.classList.remove('hidden');
        showToast('Configuración del negocio guardada y publicada exitosamente', 'success');
    } catch (err) {
        console.error('Error al guardar configuración del negocio:', err);
        if (errorEl) {
            errorEl.querySelector('span').textContent = err.message || 'No se pudo guardar la configuración del negocio.';
            errorEl.classList.remove('hidden');
        }
        showToast(err.message || 'Error al guardar configuración', 'error');
    } finally {
        if (submitBtn) {
            submitBtn.disabled = false;
            submitBtn.innerHTML = '<i class="fa-solid fa-floppy-disk mr-1"></i> <span>Guardar Configuración del Negocio</span>';
        }
    }
}


async function loadDashboardData() {
    const refreshIcon = document.getElementById('dash-refresh-icon');
    const errorBanner = document.getElementById('adm-dash-error-banner');
    if (refreshIcon) refreshIcon.classList.add('fa-spin');
    if (errorBanner) errorBanner.classList.add('hidden');

    try {
        const data = await apiGet('/dashboard/resumen');
        renderAdminDashboard(data);
    } catch (err) {
        console.warn('Dashboard API error, rendering local metrics fallback', err);
        if (errorBanner) errorBanner.classList.remove('hidden');
        renderAdminDashboardLocalFallback();
    } finally {
        if (refreshIcon) refreshIcon.classList.remove('fa-spin');
    }
}

function quickRestockProduct(id, type) {
    switchAdminTab('products');
    setTimeout(() => {
        const itemType = (type || 'producto').toLowerCase();
        editProduct(id, itemType);
    }, 100);
}

function formatUpcomingRemainingTime(dateStr, timeStr) {
    const apptDate = parseAppointmentDateTime(dateStr, timeStr);
    if (!apptDate) return 'Fecha por coordinar';
    const now = getBogotaDateNow();
    const diffMs = apptDate.getTime() - now.getTime();
    if (diffMs <= 0) return '⏳ En curso / Ahora';
    const totalMinutes = Math.floor(diffMs / (1000 * 60));
    const days = Math.floor(totalMinutes / (24 * 60));
    const hours = Math.floor((totalMinutes % (24 * 60)) / 60);
    const minutes = totalMinutes % 60;
    if (days > 0) return `⏳ Faltan ${days} d ${hours} h ${minutes} min`;
    if (hours > 0) return `⏳ Faltan ${hours} h ${minutes} min`;
    return `⏳ Faltan ${minutes} min`;
}

let upcomingCountdownInterval = null;
function startUpcomingCountdownTimer() {
    if (upcomingCountdownInterval) clearInterval(upcomingCountdownInterval);
    upcomingCountdownInterval = setInterval(() => {
        document.querySelectorAll('.upcoming-timer[data-date][data-time]').forEach(el => {
            const dateStr = el.getAttribute('data-date');
            const timeStr = el.getAttribute('data-time');
            if (dateStr && timeStr) {
                el.textContent = formatUpcomingRemainingTime(dateStr, timeStr);
            }
        });
    }, 30000);
}

function renderAdminDashboard(data) {
    if (!data) return;

    // 1. Saludo dinámico y Fecha
    const now = new Date();
    const hours = now.getHours();
    let greeting = 'Buenos días 👋';
    if (hours >= 12 && hours < 18) greeting = 'Buenas tardes 👋';
    else if (hours >= 18 || hours < 6) greeting = 'Buenas noches 👋';

    const greetingEl = document.getElementById('dash-greeting-title');
    if (greetingEl) greetingEl.textContent = greeting;

    const dateOptions = { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' };
    const dateFormatted = new Intl.DateTimeFormat('es-CO', dateOptions).format(now);
    const dateLabel = document.getElementById('dash-date-label');
    if (dateLabel) dateLabel.textContent = dateFormatted.charAt(0).toUpperCase() + dateFormatted.slice(1);

    // 2. KPIs con desgloses contextuales de datos reales
    const kpiCitasEl = document.getElementById('adm-dash-kpi-citas');
    const kpiPedidosEl = document.getElementById('adm-dash-kpi-pedidos');
    const kpiVentasEl = document.getElementById('adm-dash-kpi-ventas');
    const kpiPendientesEl = document.getElementById('adm-dash-kpi-pendientes');
    const kpiStockEl = document.getElementById('adm-dash-kpi-stock-bajo');

    if (kpiCitasEl) kpiCitasEl.textContent = data.citasHoy ?? 0;
    if (kpiPedidosEl) kpiPedidosEl.textContent = data.pedidosActivos ?? 0;
    if (kpiVentasEl) kpiVentasEl.textContent = `$${(data.ventasHoy ?? 0).toLocaleString('es-CO')}`;
    if (kpiPendientesEl) kpiPendientesEl.textContent = data.pendientes ?? 0;
    if (kpiStockEl) kpiStockEl.textContent = data.stockBajo ?? 0;

    // Badge en navegación de pedidos
    const ordersNavBadge = document.getElementById('adm-nav-orders-badge');
    if (ordersNavBadge) {
        const activeCount = Number(data.pedidosActivos ?? 0);
        if (activeCount > 0) {
            ordersNavBadge.textContent = activeCount;
            ordersNavBadge.classList.remove('hidden');
        } else {
            ordersNavBadge.classList.add('hidden');
        }
    }

    // Desglose KPI 1 (Citas hoy)
    const citasBreakdownEl = document.getElementById('adm-dash-kpi-citas-breakdown');
    if (citasBreakdownEl) {
        if (data.citasHoyDesglose) {
            const conf = data.citasHoyDesglose.confirmadas || 0;
            const pend = data.citasHoyDesglose.pendientes || 0;
            const comp = data.citasHoyDesglose.completadas || 0;
            citasBreakdownEl.innerHTML = `<span class="text-emerald-400 font-bold">${conf}</span> confirmadas · <span class="text-amber-400 font-bold">${pend}</span> pendientes${comp > 0 ? ` · <span class="text-stone-300">${comp} compl.</span>` : ''}`;
        } else {
            citasBreakdownEl.innerHTML = `<span class="text-stone-400">turnos agendados para hoy</span>`;
        }
    }

    // Desglose KPI 2 (Pedidos activos)
    const pedidosBreakdownEl = document.getElementById('adm-dash-kpi-pedidos-breakdown');
    if (pedidosBreakdownEl) {
        if (data.pedidosActivosDesglose) {
            const prep = data.pedidosActivosDesglose.enPreparacion || 0;
            const ready = data.pedidosActivosDesglose.listosParaRecoger || 0;
            const conf = data.pedidosActivosDesglose.pagoConfirmado || 0;
            pedidosBreakdownEl.innerHTML = `<span class="text-amber-400 font-bold">${prep}</span> en prep. · <span class="text-emerald-400 font-bold">${ready}</span> listos${conf > 0 ? ` · <span class="text-stone-300">${conf} nuevos</span>` : ''}`;
        } else {
            pedidosBreakdownEl.innerHTML = `<span class="text-stone-400">pedidos de productos</span>`;
        }
    }


    // Desglose KPI 2 (Ventas hoy)
    const ventasBreakdownEl = document.getElementById('adm-dash-kpi-ventas-breakdown');
    if (ventasBreakdownEl) {
        if (data.ventasHoyDetalle) {
            const cambio = data.ventasHoyDetalle.cambioVsAyerPct;
            let trendHtml = '';
            if (cambio !== null && cambio !== undefined) {
                if (cambio > 0) {
                    trendHtml = `<span class="text-emerald-400 font-bold"><i class="fa-solid fa-arrow-trend-up mr-0.5"></i>↑ +${cambio}% vs ayer</span>`;
                } else if (cambio < 0) {
                    trendHtml = `<span class="text-red-400 font-bold"><i class="fa-solid fa-arrow-trend-down mr-0.5"></i>↓ ${cambio}% vs ayer</span>`;
                } else {
                    trendHtml = `<span class="text-stone-400">0% vs ayer</span>`;
                }
            }
            const ticketHoy = data.ventasHoyDetalle.ticketPromedioHoy || 0;
            const ticketStr = ticketHoy > 0 ? `Ticket: $${ticketHoy.toLocaleString('es-CO')}` : 'Sin ventas hoy';
            ventasBreakdownEl.innerHTML = trendHtml ? `${trendHtml} · <span class="text-stone-300">${ticketStr}</span>` : `<span class="text-stone-300">${ticketStr}</span>`;
        } else {
            ventasBreakdownEl.innerHTML = `<span class="text-emerald-400/80">pagos e ingresos confirmados</span>`;
        }
    }

    // Desglose KPI 3 (Pendientes)
    const pendBreakdownEl = document.getElementById('adm-dash-kpi-pendientes-breakdown');
    if (pendBreakdownEl) {
        if (data.pendientesDesglose) {
            const comp = data.pendientesDesglose.comprobantes || 0;
            const reprog = data.pendientesDesglose.reprogramaciones || 0;
            const solic = data.pendientesDesglose.solicitudesCancelacion || 0;
            let desgloseText = `<span class="text-amber-400 font-bold">${comp}</span> comp. · <span class="text-purple-300 font-bold">${reprog}</span> reprog.`;
            if (solic > 0) {
                desgloseText += ` · <span class="text-amber-300 font-bold">${solic} solic.</span>`;
            }
            pendBreakdownEl.innerHTML = desgloseText;
        } else {
            pendBreakdownEl.innerHTML = `<span class="text-amber-400/80">requieren acción administrativa</span>`;
        }
    }

    // Desglose KPI 4 (Stock bajo)
    const stockBreakdownEl = document.getElementById('adm-dash-kpi-stock-breakdown');
    if (stockBreakdownEl) {
        if (data.stockBajoDesglose) {
            const reponer = data.stockBajoDesglose.porReponer || 0;
            const agot = data.stockBajoDesglose.agotados || 0;
            stockBreakdownEl.innerHTML = `<span class="text-amber-400 font-bold">${reponer}</span> por reponer · <span class="text-red-400 font-bold">${agot} agotados</span>`;
        } else {
            stockBreakdownEl.innerHTML = `<span class="text-red-400/80">stock bajo o agotado</span>`;
        }
    }

    // 3. Indicador de campanita intermitente de alerta y Sección "Atención Ahora"
    const bellIndicatorBtn = document.getElementById('adm-dash-bell-indicator');
    const bellCountEl = document.getElementById('adm-dash-bell-count');
    const bellIconEl = document.getElementById('adm-dash-bell-icon');
    const kpiBellIconEl = document.getElementById('adm-dash-kpi-bell-icon');

    const pendingCount = Number(data.pendientes ?? 0);
    const unviewedCancelsCount = Number(data.cancelacionesNoVistas ?? 0);
    const activeAttentionCount = pendingCount + unviewedCancelsCount;

    if (bellIndicatorBtn && bellCountEl) {
        if (activeAttentionCount > 0) {
            bellIndicatorBtn.classList.remove('hidden');
            bellIndicatorBtn.classList.add('flex');
            bellCountEl.textContent = activeAttentionCount;
            bellIndicatorBtn.setAttribute('aria-label', `${activeAttentionCount} ${activeAttentionCount === 1 ? 'pendiente que requiere atención' : 'pendientes que requieren atención'}`);
            bellIndicatorBtn.setAttribute('title', `${activeAttentionCount} ${activeAttentionCount === 1 ? 'pendiente que requiere atención' : 'pendientes que requieren atención'}`);
            if (bellIconEl) bellIconEl.classList.add('isivi-bell-intermittent');
            if (kpiBellIconEl) kpiBellIconEl.classList.add('isivi-bell-intermittent');
        } else {
            bellIndicatorBtn.classList.add('hidden');
            bellIndicatorBtn.classList.remove('flex');
            bellCountEl.textContent = '0';
            bellIndicatorBtn.setAttribute('aria-label', 'Sin alertas pendientes');
            bellIndicatorBtn.setAttribute('title', 'Sin alertas pendientes');
            if (bellIconEl) bellIconEl.classList.remove('isivi-bell-intermittent');
            if (kpiBellIconEl) kpiBellIconEl.classList.remove('isivi-bell-intermittent');
        }
    }

    const alertsContainer = document.getElementById('adm-dash-alerts-container');
    const alertsList = document.getElementById('adm-dash-alerts-list');

    if (alertsContainer && alertsList) {
        const alerts = Array.isArray(data.alertasAtencion) ? data.alertasAtencion : [];
        if (alerts.length === 0) {
            alertsContainer.classList.add('hidden');
        } else {
            alertsContainer.classList.remove('hidden');
            alertsList.innerHTML = alerts.map(a => {
                const count = Number(a.cantidad) || 1;
                const isSingle = count === 1;
                const targetId = a.targetId || '';
                const targetCodigo = a.targetCodigo || '';
                const targetTipo = a.targetTipo || '';
                const targetNombre = a.targetNombre || '';
                const isPureOrder = Boolean(a.esPedido);
                const items = Array.isArray(a.items) ? a.items : [];

                let itemChipsHtml = '';
                if (items.length > 1) {
                    itemChipsHtml = `
                        <div class="flex flex-wrap items-center gap-1.5 mt-2 pt-2 border-t border-stone-800/80">
                            <span class="text-[10px] text-stone-400 font-bold uppercase tracking-wider">Afectados:</span>
                            ${items.slice(0, 5).map(it => {
                                const itName = it.nombre || it.cliente || it.codigo || 'Ítem';
                                const itId = it.id || '';
                                const itType = it.tipo || targetTipo || '';
                                const itCode = it.codigo || '';
                                const itIsOrder = Boolean(it.esPedido);
                                return `
                                    <button type="button" onclick="event.stopPropagation(); navigateToPendingAlertItem('${a.tipo || ''}', '${itId}', '${itType}', '${itCode}', ${itIsOrder})" class="px-2 py-0.5 rounded-lg text-[10px] font-bold bg-stone-950 hover:bg-stone-850 text-isivi-gold border border-stone-800 hover:border-isivi-gold transition flex items-center gap-1">
                                        <i class="fa-solid fa-arrow-right text-[8px]"></i>
                                        <span class="truncate max-w-[120px]">${itName}</span>
                                    </button>
                                `;
                            }).join('')}
                            ${items.length > 5 ? `<span class="text-[10px] text-stone-500 font-bold">+${items.length - 5} más</span>` : ''}
                        </div>
                    `;
                }

                return `
                    <div class="p-3.5 bg-stone-900/90 rounded-2xl border border-stone-800 flex flex-col gap-2 hover:border-amber-500/40 transition">
                        <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
                            <div class="flex items-center gap-3">
                                <div class="w-9 h-9 rounded-xl bg-amber-500/20 text-amber-300 flex items-center justify-center text-sm shrink-0 shadow-inner">
                                    <i class="fa-solid ${a.icono || 'fa-bell'}"></i>
                                </div>
                                <div>
                                    <div class="flex items-center gap-2 flex-wrap">
                                        <span class="font-bold text-white text-xs leading-tight">${a.titulo}</span>
                                        <span class="px-2 py-0.2 rounded-full text-[9px] font-extrabold bg-amber-950 text-amber-300 border border-amber-500/30 font-mono">${count}</span>
                                    </div>
                                    <span class="text-[11px] text-isivi-300 block mt-0.5">${a.mensaje}</span>
                                    ${isSingle && targetNombre ? `<span class="text-[10px] text-isivi-gold font-medium block mt-0.5"><i class="fa-solid fa-crosshairs mr-1"></i>${targetNombre} ${targetCodigo ? '(' + targetCodigo + ')' : ''}</span>` : ''}
                                </div>
                            </div>
                            <div class="flex items-center gap-2 self-end sm:self-center">
                                ${isSingle ? `
                                    <button type="button" onclick="navigateToPendingAlertItem('${a.tipo || ''}', '${targetId}', '${targetTipo}', '${targetCodigo}', ${isPureOrder})" class="px-3 py-1.5 bg-stone-950 hover:bg-stone-800 text-isivi-gold border border-stone-700 hover:border-isivi-gold rounded-xl text-[11px] font-bold whitespace-nowrap flex items-center gap-1.5 shadow-sm transition">
                                        <span>Atender</span> <i class="fa-solid fa-arrow-right text-[9px]"></i>
                                    </button>
                                ` : `
                                    <button type="button" onclick="navigateToPendingAlertCategory('${a.tipo || ''}', '${a.accionTab || a.modulo || 'bookings'}', '${a.filtro || ''}')" class="px-3 py-1.5 bg-stone-950 hover:bg-stone-800 text-isivi-gold border border-stone-700 hover:border-isivi-gold rounded-xl text-[11px] font-bold whitespace-nowrap flex items-center gap-1.5 shadow-sm transition">
                                        <span>Atender todos</span> <i class="fa-solid fa-arrow-right text-[9px]"></i>
                                    </button>
                                `}
                            </div>
                        </div>
                        ${itemChipsHtml}
                    </div>
                `;
            }).join('');
        }
    }

    // 4. Próximas Citas (Próxima Cita + Top Próximas Citas Activas)
    const upcomingContainer = document.getElementById('adm-dash-upcoming-list');
    const upcomingBadge = document.getElementById('adm-dash-upcoming-count-badge');
    const proximasList = (Array.isArray(data.proximasCitas) && data.proximasCitas.length > 0)
        ? data.proximasCitas
        : (data.proximaCita ? [data.proximaCita] : []);

    if (upcomingBadge) upcomingBadge.textContent = `${proximasList.length} próximas`;

    if (upcomingContainer) {
        if (proximasList.length === 0) {
            upcomingContainer.innerHTML = '<div class="text-center py-6 text-xs text-isivi-300">No hay citas activas programadas próximamente.</div>';
        } else {
            upcomingContainer.innerHTML = proximasList.map((item, index) => {
                const payInfo = getPaymentMethodInfo(item);
                const itemsStr = Array.isArray(item.items) ? item.items.join(', ') : (item.items || 'Servicio');
                const isFirst = index === 0;
                const phoneClean = (item.telefono || '').replace(/\D/g, '');
                const timeRemaining = formatUpcomingRemainingTime(item.fechaCita, item.horaCita);

                return `
                    <div class="p-4 ${isFirst ? 'bg-gradient-to-r from-isivi-900 via-stone-900 to-isivi-950 border-isivi-gold/60 shadow-xl ring-1 ring-isivi-gold/30' : 'bg-stone-950 border-stone-800'} rounded-2xl border flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3 dashboard-card-hover transition-all">
                        <div class="flex items-start sm:items-center gap-3">
                            <div class="px-3 py-2 rounded-xl ${isFirst ? 'bg-isivi-gold text-isivi-black font-extrabold shadow-md' : 'bg-stone-900 text-isivi-gold border border-stone-700 font-bold'} font-mono text-xs mt-0.5 sm:mt-0 flex flex-col items-center justify-center min-w-[75px]">
                                <span class="text-[9px] uppercase tracking-wider opacity-80">${item.fechaCita || ''}</span>
                                <span class="text-xs font-black">${item.horaCita || 'Turno'}</span>
                            </div>
                            <div class="space-y-1">
                                <div class="flex flex-wrap items-center gap-1.5">
                                    <span class="font-bold text-white text-xs">${item.nombreCliente || 'Cliente'}</span>
                                    ${isFirst ? `<span class="px-2 py-0.5 rounded-full text-[9px] font-bold bg-isivi-gold text-isivi-black border border-isivi-gold flex items-center gap-1 shadow-sm"><i class="fa-solid fa-star text-[8px]"></i> PRÓXIMA</span>` : ''}
                                    <span class="px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-amber-950/80 text-amber-300 border border-amber-500/40 font-mono upcoming-timer" data-date="${item.fechaCita || ''}" data-time="${item.horaCita || ''}">
                                        ${timeRemaining}
                                    </span>
                                    ${payInfo.badgeHtml}
                                    ${payInfo.statusBadgeHtml}
                                </div>
                                <p class="text-[11px] text-isivi-300 line-clamp-1">${itemsStr}</p>
                                <div class="flex flex-wrap items-center gap-2 text-[10px] text-stone-400">
                                    <span class="font-mono text-isivi-gold">${item.codigoReserva || ''}</span>
                                    <span>·</span>
                                    <button type="button" onclick="openAdminWhatsAppChat('${item.telefono}', '${item.codigoReserva || ''}', '${item.nombreCliente || ''}')" class="text-emerald-400 hover:underline flex items-center gap-1"><i class="fa-brands fa-whatsapp"></i> ${item.telefono || ''}</button>
                                </div>
                            </div>
                        </div>
                        <div class="flex items-center gap-1.5 w-full sm:w-auto justify-end pt-2 sm:pt-0 border-t sm:border-t-0 border-stone-850 flex-wrap">
                            <button onclick="openAdminBookingDetail('${item.id}')" class="px-2.5 py-1 bg-stone-900 hover:bg-stone-800 text-isivi-gold border border-stone-700 rounded-lg text-[11px] font-bold"><i class="fa-solid fa-eye mr-1"></i>Detalle</button>
                            ${phoneClean ? `<button type="button" onclick="openAdminWhatsAppChat('${item.telefono}', '${item.codigoReserva || ''}', '${item.nombreCliente || ''}')" class="px-2.5 py-1 bg-emerald-950 hover:bg-emerald-900 text-emerald-400 border border-emerald-800/60 rounded-lg text-[11px] font-bold flex items-center gap-1"><i class="fa-brands fa-whatsapp"></i> Chat</button>` : ''}
                            <button onclick="openAdminRescheduleModal('${item.id}')" class="px-2.5 py-1 bg-stone-900 hover:bg-stone-800 text-stone-300 border border-stone-700 rounded-lg text-[11px] font-bold"><i class="fa-solid fa-calendar-days"></i> Reprogramar</button>
                        </div>
                    </div>
                `;
            }).join('');
        }
    }
    startUpcomingCountdownTimer();

    // 5. Agenda de Hoy
    const todayContainer = document.getElementById('adm-dash-today-list');
    const nextBookingContainer = document.getElementById('adm-dash-next-booking-container');
    const agendaList = data.agendaHoy || [];
    let nextBookingId = null;

    if (agendaList.length > 0) {
        const activeUpcoming = agendaList.filter(b => {
            const estado = (b.estado || '').toLowerCase();
            return estado !== 'completada' && 
                   estado !== 'realizada' && 
                   estado !== 'cancelada' && 
                   estado !== 'denegada' && 
                   estado !== 'expirada';
        });
        if (activeUpcoming.length > 0) {
            nextBookingId = activeUpcoming[0].id;
        }
    }

    if (nextBookingContainer) {
        nextBookingContainer.innerHTML = '';
        nextBookingContainer.classList.add('hidden');
    }

    if (agendaList.length === 0) {
        todayContainer.innerHTML = '<div class="text-center py-6 text-xs text-isivi-300">No hay citas agendadas para hoy.</div>';
    } else {
        todayContainer.innerHTML = agendaList.map(item => {
            const payInfo = getPaymentMethodInfo(item);
            const itemsStr = Array.isArray(item.items) ? item.items.join(', ') : (item.items || 'Servicio');
            const isNext = item.id === nextBookingId;
            const phoneClean = (item.telefono || '').replace(/\D/g, '');
            return `
                <div class="p-3.5 ${isNext ? 'bg-gradient-to-r from-isivi-900 via-stone-900 to-isivi-950 border-isivi-gold/50 shadow-lg' : 'bg-stone-950 border-stone-800'} rounded-2xl border flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3 dashboard-card-hover transition-all">
                    <div class="flex items-start sm:items-center gap-3">
                        <div class="px-3 py-1.5 rounded-xl ${isNext ? 'bg-isivi-gold text-isivi-black font-extrabold shadow-md' : 'bg-stone-900 text-isivi-gold border border-stone-700 font-bold'} font-mono text-xs mt-0.5 sm:mt-0 flex items-center gap-1.5">
                            <i class="fa-solid fa-clock"></i> <span>${item.horaCita || 'Turno'}</span>
                        </div>
                        <div class="space-y-1">
                            <div class="flex flex-wrap items-center gap-1.5">
                                <span class="font-bold text-white text-xs">${item.nombreCliente || 'Cliente'}</span>
                                ${isNext ? `<span class="px-2 py-0.5 rounded-full text-[9px] font-bold bg-isivi-gold/20 text-isivi-gold border border-isivi-gold/40 flex items-center gap-1"><i class="fa-solid fa-star text-[8px]"></i> PRÓXIMA</span>` : ''}
                                ${payInfo.badgeHtml}
                                ${payInfo.statusBadgeHtml}
                            </div>
                            <p class="text-[11px] text-isivi-300 line-clamp-1">${itemsStr}</p>
                            <div class="flex flex-wrap items-center gap-2 text-[10px] text-stone-400">
                                <span class="font-mono">${item.codigoReserva || ''}</span>
                                <span>·</span>
                                <button type="button" onclick="openAdminWhatsAppChat('${item.telefono}', '${item.codigoReserva || ''}', '${item.nombreCliente || ''}')" class="text-emerald-400 hover:underline flex items-center gap-1"><i class="fa-brands fa-whatsapp"></i> ${item.telefono || ''}</button>
                                ${payInfo.actionRequired ? `<span class="text-amber-300 font-bold flex items-center gap-1"><i class="fa-solid fa-triangle-exclamation"></i> Requiere validación</span>` : ''}
                            </div>
                        </div>
                    </div>
                    <div class="flex items-center gap-1.5 w-full sm:w-auto justify-end pt-2 sm:pt-0 border-t sm:border-t-0 border-stone-850 flex-wrap">
                        ${payInfo.actionRequired ? `<button onclick="quickApproveBooking('${item.id}')" class="px-2.5 py-1 bg-emerald-700 hover:bg-emerald-600 text-white rounded-lg text-[11px] font-bold shadow"><i class="fa-solid fa-check mr-1"></i>${item.isPureOrder ? 'Confirmar comprobante' : 'Aprobar'}</button>` : ''}
                        <button onclick="openAdminBookingDetail('${item.id}')" class="px-2.5 py-1 bg-stone-900 hover:bg-stone-800 text-isivi-gold border border-stone-700 rounded-lg text-[11px] font-bold"><i class="fa-solid fa-eye mr-1"></i>Detalle</button>
                        ${phoneClean ? `<button type="button" onclick="openAdminWhatsAppChat('${item.telefono}', '${item.codigoReserva || ''}', '${item.nombreCliente || ''}')" class="px-2.5 py-1 bg-emerald-950 hover:bg-emerald-900 text-emerald-400 border border-emerald-800/60 rounded-lg text-[11px] font-bold flex items-center gap-1"><i class="fa-brands fa-whatsapp"></i> Chat</button>` : ''}
                        <button onclick="openAdminRescheduleModal('${item.id}')" class="px-2.5 py-1 bg-stone-900 hover:bg-stone-800 text-stone-300 border border-stone-700 rounded-lg text-[11px] font-bold"><i class="fa-solid fa-calendar-days"></i> Reprogramar</button>
                    </div>
                </div>
            `;
        }).join('');
    }

    // 5. Solicitudes que Requieren Acción
    const pendingContainer = document.getElementById('adm-dash-pending-list');
    const pendingBadge = document.getElementById('adm-dash-pending-count-badge');
    const pendingList = data.pendientesLista || [];
    if (pendingBadge) pendingBadge.textContent = pendingList.length;

    if (pendingContainer) {
        if (pendingList.length === 0) {
            pendingContainer.innerHTML = '<div class="text-center py-6 text-xs text-isivi-300">No hay solicitudes pendientes por revisar.</div>';
        } else {
            pendingContainer.innerHTML = pendingList.map(item => {
                const payInfo = getPaymentMethodInfo(item);
                const itemsStr = Array.isArray(item.items) ? item.items.join(', ') : (item.items || 'Servicio / Producto');
                const isReprog = item.estado === 'Pendiente Reprogramación' || item.estado === 'Pendiente Reprogramacion';
                const isSolicitudCancel = item.estado === 'Solicitud Cancelación' || item.estado === 'Solicitud Cancelacion';
                const isPureOrder = Boolean(item.tipoEntrega) || (!item.fechaCita && !item.horaCita);

                let priorityBadge = payInfo.badgeHtml;
                if (isReprog) {
                    priorityBadge = '<span class="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-purple-950 text-purple-300 border border-purple-500/30"><i class="fa-solid fa-calendar-days"></i> 🔄 Reprogramación</span>';
                } else if (isSolicitudCancel) {
                    priorityBadge = '<span class="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-amber-950 text-amber-300 border border-amber-500/30"><i class="fa-solid fa-calendar-xmark"></i> 🟡 Solicitud Cancelación</span>';
                }

                return `
                    <div class="p-3.5 bg-stone-950 rounded-2xl border border-stone-800 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3 dashboard-card-hover cursor-pointer" onclick="if (!event.target.closest('button')) navigateToPendingAction('${isReprog ? 'reprogramacion' : isSolicitudCancel ? 'solicitud_cancelacion' : 'comprobante'}', '${item.id}', '${item.codigoReserva || ''}', ${isPureOrder})">
                        <div class="space-y-1">
                            <div class="flex flex-wrap items-center gap-1.5">
                                <span class="font-bold text-white text-xs">${item.nombreCliente || 'Cliente'}</span>
                                ${priorityBadge}
                                ${payInfo.statusBadgeHtml}
                            </div>
                            <p class="text-[11px] text-isivi-300 mt-0.5">${item.fechaCita || 'Sin cita'} · ${item.horaCita || ''} · ${itemsStr}</p>
                            <div class="flex flex-wrap items-center gap-2 text-[10px] text-stone-400 mt-0.5">
                                <span>Anticipo: <strong class="text-emerald-400">$${(item.anticipo || 0).toLocaleString('es-CO')}</strong></span>
                                <span>·</span>
                                <span class="font-mono">${item.codigoReserva || ''}</span>
                                ${isSolicitudCancel && item.motivoCancelacion ? `<span class="text-amber-300 italic">Motivo: "${item.motivoCancelacion}"</span>` : ''}
                                ${payInfo.actionRequired && !isSolicitudCancel ? `<span class="text-amber-300 font-bold flex items-center gap-1"><i class="fa-solid fa-triangle-exclamation"></i> Validar comprobante WhatsApp</span>` : ''}
                                ${payInfo.isWompi && !payInfo.isApproved ? `<span class="text-sky-300 font-medium flex items-center gap-1"><i class="fa-solid fa-hourglass-half"></i> Esperando pasarela</span>` : ''}
                            </div>
                        </div>
                        <div class="flex items-center gap-1.5 w-full sm:w-auto justify-end flex-wrap">
                            ${isSolicitudCancel ? `
                                <button onclick="quickApproveCancellation('${item.id}')" class="px-2.5 py-1 bg-emerald-700 hover:bg-emerald-600 text-white rounded-lg text-[11px] font-bold shadow"><i class="fa-solid fa-check mr-1"></i>Aprobar</button>
                                <button onclick="quickRejectCancellation('${item.id}')" class="px-2 py-1 bg-red-950 hover:bg-red-900 text-red-300 rounded-lg text-[11px] font-bold"><i class="fa-solid fa-xmark mr-1"></i>Rechazar</button>
                            ` : (payInfo.actionRequired || isReprog ? `<button onclick="quickApproveBooking('${item.id}')" class="px-2.5 py-1 bg-emerald-700 hover:bg-emerald-600 text-white rounded-lg text-[11px] font-bold"><i class="fa-solid fa-check mr-1"></i>${item.isPureOrder ? 'Confirmar comprobante' : 'Aprobar'}</button><button onclick="quickDenyBooking('${item.id}')" class="px-2 py-1 bg-red-950 hover:bg-red-900 text-red-300 rounded-lg text-[11px] font-bold"><i class="fa-solid fa-xmark mr-1"></i>Denegar</button>` : '')}
                            <button onclick="navigateToPendingAction('${isReprog ? 'reprogramacion' : isSolicitudCancel ? 'solicitud_cancelacion' : 'comprobante'}', '${item.id}', '${item.codigoReserva || ''}', ${isPureOrder})" class="px-2.5 py-1 bg-stone-900 hover:bg-stone-800 text-isivi-gold border border-stone-700 rounded-lg text-[11px] font-bold"><i class="fa-solid fa-arrow-up-right-from-square mr-1"></i>Atender</button>
                            <button type="button" onclick="openAdminWhatsAppValidation('${item.id}')" title="Contactar al cliente por WhatsApp con mensaje preparado" class="px-2.5 py-1 bg-emerald-950 hover:bg-emerald-900 text-emerald-400 border border-emerald-800/60 rounded-lg text-[11px] font-bold flex items-center gap-1"><i class="fa-brands fa-whatsapp"></i> <span class="hidden sm:inline">WhatsApp</span></button>
                        </div>
                    </div>
                `;
            }).join('');
        }
    }

    // 6. Stock Bajo / Reponer
    const stockContainer = document.getElementById('adm-dash-low-stock-list');
    const lowStockList = data.stockBajoLista || [];
    if (stockContainer) {
        if (lowStockList.length === 0) {
            stockContainer.innerHTML = '<div class="text-center py-6 text-xs text-isivi-300">Todos los productos tienen stock óptimo.</div>';
        } else {
            stockContainer.innerHTML = lowStockList.map(item => {
                const isAgotado = (item.cantidad <= 0) || (item.enStock === false);
                return `
                    <div class="p-2.5 bg-stone-950 rounded-2xl border border-stone-800 flex items-center justify-between gap-3 dashboard-card-hover">
                        <div class="flex items-center gap-2.5">
                            <img src="${item.imagenUrl || '/images/isivi-logo-transparent.png'}" alt="${item.nombre}" class="w-9 h-9 object-cover rounded-lg border border-stone-800" onerror="this.src='/images/isivi-logo-transparent.png'">
                            <div>
                                <span class="font-bold text-white text-xs block leading-tight">${item.nombre}</span>
                                <span class="text-[10px] text-isivi-300">$${(item.precio || 0).toLocaleString('es-CO')} · <span class="text-stone-400 font-mono">${item.tipo || 'Producto'}</span></span>
                            </div>
                        </div>
                        <div class="flex items-center gap-1.5">
                            <span class="px-2 py-0.5 rounded-full text-[10px] font-bold ${isAgotado ? 'bg-red-950 text-red-300 border border-red-500/30' : 'bg-amber-950 text-amber-300 border border-amber-500/30'}">
                                ${isAgotado ? '🔴 Agotado' : `${item.cantidad} rest.`}
                            </span>
                            <button onclick="quickRestockProduct('${item.id}', '${item.tipo}')" title="Reponer inventario" class="px-2.5 py-1 bg-stone-900 hover:bg-stone-800 text-isivi-gold border border-stone-700 hover:border-isivi-gold rounded-lg text-[10px] font-bold flex items-center gap-1">
                                <i class="fa-solid fa-pen-to-square"></i> <span class="hidden sm:inline">Reponer</span>
                            </button>
                        </div>
                    </div>
                `;
            }).join('');
        }
    }

    // 7. Resumen de Ventas
    const salesTodayEl = document.getElementById('adm-dash-sales-today');
    const salesWeekEl = document.getElementById('adm-dash-sales-week');
    const salesMonthEl = document.getElementById('adm-dash-sales-month');
    const ticketPromBadge = document.getElementById('adm-dash-ticket-prom-badge');
    const salesServiciosEl = document.getElementById('adm-dash-sales-servicios');
    const salesProductosEl = document.getElementById('adm-dash-sales-productos');

    if (salesTodayEl) salesTodayEl.textContent = `$${(data.ventasHoy ?? 0).toLocaleString('es-CO')}`;
    if (salesWeekEl) salesWeekEl.textContent = `$${(data.ventasSemana ?? 0).toLocaleString('es-CO')}`;
    if (salesMonthEl) salesMonthEl.textContent = `$${(data.ventasMes ?? 0).toLocaleString('es-CO')}`;

    const ticketSemana = data.ventasSemanaDetalle ? data.ventasSemanaDetalle.ticketPromedioSemana : (data.ventasHoyDetalle ? data.ventasHoyDetalle.ticketPromedioHoy : 0);
    if (ticketPromBadge) {
        ticketPromBadge.textContent = ticketSemana > 0 ? `Ticket prom: $${ticketSemana.toLocaleString('es-CO')}` : 'Ticket prom: $0';
    }

    const serv = data.ventasSemanaDetalle ? data.ventasSemanaDetalle.serviciosSemana : (data.ventasHoyDetalle ? data.ventasHoyDetalle.serviciosHoy : 0);
    const prod = data.ventasSemanaDetalle ? data.ventasSemanaDetalle.productosSemana : (data.ventasHoyDetalle ? data.ventasHoyDetalle.productosHoy : 0);
    if (salesServiciosEl) salesServiciosEl.textContent = `$${serv.toLocaleString('es-CO')}`;
    if (salesProductosEl) salesProductosEl.textContent = `$${prod.toLocaleString('es-CO')}`;

    // 8. Gráfico SVG de Últimos 7 Días
    renderSalesChart(data.ventas7Dias || []);
}

function renderSalesChart(sales7Days) {
    const chartContainer = document.getElementById('adm-dash-chart-container');
    if (!chartContainer) return;

    if (!sales7Days || sales7Days.length === 0) {
        chartContainer.innerHTML = '<div class="text-center py-4 text-xs text-isivi-300">Sin datos de ventas en los últimos 7 días.</div>';
        return;
    }

    const maxVal = Math.max(...sales7Days.map(d => d.total || 0), 100000);
    const barsHtml = sales7Days.map(item => {
        const val = item.total || 0;
        const heightPct = Math.max(8, Math.round((val / maxVal) * 100));
        const dayLabel = item.fecha ? item.fecha.slice(5) : (item.dia || ''); // MM-DD
        const formattedVal = val >= 1000000 ? `$${(val/1000000).toFixed(1)}M` : (val >= 1000 ? `$${(val/1000).toFixed(0)}k` : `$${val.toLocaleString('es-CO')}`);
        const fullTooltip = `${item.dia || ''} ${item.fecha || ''}: $${val.toLocaleString('es-CO')}`;

        return `
            <div class="flex-1 flex flex-col items-center gap-1.5 h-28 justify-end group relative" title="${fullTooltip}">
                <span class="text-[9px] font-mono text-isivi-gold opacity-0 group-hover:opacity-100 transition whitespace-nowrap absolute -top-5 bg-stone-900 px-1.5 py-0.5 rounded border border-stone-700 shadow z-10">${formattedVal}</span>
                <div class="w-full max-w-[28px] rounded-t-lg bg-gradient-to-t from-isivi-600 to-isivi-gold hover:from-yellow-400 hover:to-yellow-300 transition shadow" style="height: ${heightPct}%;"></div>
                <span class="text-[9px] text-isivi-300 font-mono">${item.dia || dayLabel}</span>
            </div>
        `;
    }).join('');

    chartContainer.innerHTML = `<div class="flex items-end gap-2 justify-between w-full h-full">${barsHtml}</div>`;
}

function renderAdminDashboardLocalFallback() {
    const todayStr = formatDateKey(new Date());
    const todayBookings = bookingsList.filter(b => b.date === todayStr);
    const deposits = bookingsList.filter(b => b.status === 'Confirmado').reduce((sum, b) => sum + (b.deposit || 0), 0);
    const pendingList = bookingsList.filter(b => b.status !== 'Confirmado' && b.status !== 'Cancelada' && b.status !== 'Denegada');

    renderAdminDashboard({
        citasHoy: todayBookings.length,
        ventasHoy: deposits,
        ventasSemana: deposits,
        ventasMes: deposits,
        pendientes: pendingList.length,
        stockBajo: productsData.filter(p => !p.inStock || (p.quantity ?? 0) <= 3).length,
        agendaHoy: todayBookings,
        pendientesLista: pendingList,
        stockBajoLista: productsData.filter(p => !p.inStock || (p.quantity ?? 0) <= 3),
        ventas7Dias: []
    });
}

function scrollToAdminAlerts() {
    const alertsContainer = document.getElementById('adm-dash-alerts-container');
    const pendingSection = document.getElementById('adm-dash-pending-list');

    if (alertsContainer && !alertsContainer.classList.contains('hidden')) {
        alertsContainer.scrollIntoView({ behavior: 'smooth', block: 'center' });
        alertsContainer.classList.add('ring-2', 'ring-amber-400', 'transition-all');
        setTimeout(() => alertsContainer.classList.remove('ring-2', 'ring-amber-400'), 1500);
    } else if (pendingSection) {
        pendingSection.scrollIntoView({ behavior: 'smooth', block: 'center' });
        const parentCard = pendingSection.closest('.bg-isivi-900') || pendingSection;
        if (parentCard) {
            parentCard.classList.add('ring-2', 'ring-amber-400', 'transition-all');
            setTimeout(() => parentCard.classList.remove('ring-2', 'ring-amber-400'), 1500);
        }
    }
}

function highlightAndScrollElement(elementId) {
    if (!elementId) return;
    const el = document.getElementById(elementId);
    if (el) {
        el.scrollIntoView({ behavior: 'smooth', block: 'center' });
        el.classList.add('ring-2', 'ring-amber-400', 'bg-amber-500/25', 'transition-all', 'duration-500');
        setTimeout(() => {
            el.classList.remove('ring-2', 'ring-amber-400', 'bg-amber-500/25');
        }, 3500);
    }
}

function highlightAndScrollProduct(id) {
    highlightAndScrollElement(`adm-product-row-${id}`);
}

function highlightAndScrollOrder(id) {
    highlightAndScrollElement(`adm-order-row-${id}`);
}

function highlightAndScrollBooking(id) {
    highlightAndScrollElement(`booking-row-${id}`);
}

function highlightAndScrollHistory(id) {
    highlightAndScrollElement(`adm-history-row-${id}`);
}

function navigateToPendingAlertItem(tipo, targetId, targetTipo, targetCodigo, esPedido = false) {
    if (tipo === 'agotado') {
        switchAdminTab('products');
        filterProductsByStatus('AGOTADOS');
        if (targetId) {
            setTimeout(() => {
                const itemType = (targetTipo || 'producto').toLowerCase();
                editProduct(targetId, itemType);
                highlightAndScrollProduct(targetId);
            }, 120);
        }
        return;
    }

    if (tipo === 'pedido_listo') {
        switchAdminTab('orders');
        filterOrdersByStatus('LISTO PARA RECOGER');
        const s = document.getElementById('adm-orders-search');
        if (s && targetCodigo) {
            s.value = targetCodigo;
            applyOrdersFilters();
        }
        if (targetId) {
            setTimeout(() => {
                openAdminOrderDetail(targetId);
                highlightAndScrollOrder(targetId);
            }, 120);
        }
        return;
    }

    if (tipo === 'pedido_por_preparar') {
        switchAdminTab('orders');
        filterOrdersByStatus('PAGO CONFIRMADO');
        const s = document.getElementById('adm-orders-search');
        if (s && targetCodigo) {
            s.value = targetCodigo;
            applyOrdersFilters();
        }
        if (targetId) {
            setTimeout(() => {
                openAdminOrderDetail(targetId);
                highlightAndScrollOrder(targetId);
            }, 120);
        }
        return;
    }

    if (tipo === 'cancelacion_cliente') {
        switchAdminTab('history');
        const historySearch = document.getElementById('adm-history-search');
        if (historySearch) {
            historySearch.value = targetCodigo || '';
            if (typeof applyHistoryFilters === 'function') applyHistoryFilters();
        }
        if (targetId) {
            setTimeout(() => {
                openAdminBookingDetail(targetId);
                highlightAndScrollHistory(targetId);
            }, 120);
        }
        return;
    }

    if (esPedido) {
        switchAdminTab('orders');
        const s = document.getElementById('adm-orders-search');
        if (s) {
            s.value = targetCodigo || '';
            applyOrdersFilters();
        }
        if (targetId) {
            setTimeout(() => {
                openAdminOrderDetail(targetId);
                highlightAndScrollOrder(targetId);
            }, 120);
        }
        return;
    }

    // Por defecto citas activas (reprogramación, solicitud cancelación, comprobante)
    switchAdminTab('bookings');
    const filterInput = document.getElementById('booking-filter-text');
    if (filterInput) {
        filterInput.value = targetCodigo || '';
        applyBookingFilters();
    }
    if (targetId) {
        setTimeout(() => {
            if (tipo === 'reprogramacion') {
                if (typeof openAdminRescheduleModal === 'function') {
                    openAdminRescheduleModal(targetId);
                } else {
                    openAdminBookingDetail(targetId);
                }
            } else {
                openAdminBookingDetail(targetId);
            }
            highlightAndScrollBooking(targetId);
        }, 120);
    }
}

function navigateToPendingAlertCategory(tipo, modulo, filtro) {
    if (tipo === 'agotado') {
        switchAdminTab('products');
        filterProductsByStatus('AGOTADOS');
        return;
    }
    if (tipo === 'pedido_listo') {
        switchAdminTab('orders');
        filterOrdersByStatus('LISTO PARA RECOGER');
        return;
    }
    if (tipo === 'pedido_por_preparar') {
        switchAdminTab('orders');
        filterOrdersByStatus('PAGO CONFIRMADO');
        return;
    }
    if (tipo === 'cancelacion_cliente') {
        switchAdminTab('history');
        const historySearch = document.getElementById('adm-history-search');
        if (historySearch) {
            historySearch.value = '';
            if (typeof applyHistoryFilters === 'function') applyHistoryFilters();
        }
        return;
    }
    if (tipo === 'solicitud_cancelacion' || tipo === 'reprogramacion' || tipo === 'comprobante') {
        switchAdminTab('bookings');
        const filterInput = document.getElementById('booking-filter-text');
        if (filterInput) {
            filterInput.value = '';
            applyBookingFilters();
        }
        return;
    }
    if (modulo) {
        switchAdminTab(modulo);
    }
}

function navigateToPendingAlert(tipo, accionTab) {
    navigateToPendingAlertCategory(tipo, accionTab, '');
}

function navigateToPendingAction(tipo, id, codigo, esPedido = false) {
    navigateToPendingAlertItem(tipo, id, '', codigo, esPedido);
}

// Quick Actions from Dashboard
let isAdminActionInProgress = false;

async function quickApproveBooking(id) {
    if (isAdminActionInProgress) return;
    const b = (typeof bookingsList !== 'undefined' ? bookingsList.find(item => item.id === id) : null)
        || (typeof adminOrdersList !== 'undefined' ? adminOrdersList.find(item => item.id === id) : null)
        || (typeof historyBookingsList !== 'undefined' ? historyBookingsList.find(item => item.id === id) : null);
    const isOrder = b ? b.isPureOrder : false;
    isAdminActionInProgress = true;
    try {
        await apiPatch(`/reservas/${id}/aprobar`);
        showToast(isOrder ? 'Pedido confirmado con éxito' : 'Reserva aprobada con éxito', 'success');
        await loadAllData();
        await loadDashboardData();
    } catch (err) {
        showToast(err.message || (isOrder ? 'No se pudo confirmar el comprobante' : 'No se pudo aprobar la reserva'), 'error');
    } finally {
        isAdminActionInProgress = false;
    }
}

async function quickDenyBooking(id) {
    if (isAdminActionInProgress) return;
    if (!confirm('¿Deseas denegar esta reserva?')) return;
    isAdminActionInProgress = true;
    try {
        await apiPatch(`/reservas/${id}/denegar`);
        showToast('Reserva denegada', 'info');
        await loadAllData();
        await loadDashboardData();
    } catch (err) {
        showToast(err.message || 'No se pudo denegar la reserva', 'error');
    } finally {
        isAdminActionInProgress = false;
    }
}

async function quickApproveCancellation(id) {
    if (isAdminActionInProgress) return;
    if (!confirm('¿Deseas aprobar la cancelación de esta cita? El horario quedará liberado inmediatamente para nuevos clientes.')) return;
    isAdminActionInProgress = true;
    try {
        await apiPatch(`/reservas/${id}/aprobar-cancelacion`);
        showToast('Cancelación aprobada. Horario liberado exitosamente.', 'success');
        await loadAllData();
        await loadDashboardData();
    } catch (err) {
        showToast(err.message || 'No se pudo aprobar la cancelación', 'error');
    } finally {
        isAdminActionInProgress = false;
    }
}

async function quickRejectCancellation(id) {
    if (isAdminActionInProgress) return;
    const motivo = prompt('Ingresa el motivo del rechazo de la cancelación (opcional):', 'Por estar demasiado próxima a la cita');
    if (motivo === null) return; // Cancelado por admin en el prompt
    isAdminActionInProgress = true;
    try {
        await apiPatch(`/reservas/${id}/rechazar-cancelacion`, { motivo: motivo && motivo.trim() ? motivo.trim() : 'Sin motivo indicado' });
        showToast('Solicitud de cancelación rechazada. La cita sigue confirmada.', 'info');
        await loadAllData();
        await loadDashboardData();
    } catch (err) {
        showToast(err.message || 'No se pudo rechazar la solicitud', 'error');
    } finally {
        isAdminActionInProgress = false;
    }
}

async function markCancellationViewed(id) {
    if (isAdminActionInProgress) return;
    isAdminActionInProgress = true;
    try {
        await apiPatch(`/reservas/${id}/marcar-cancelacion-vista`);
        showToast('Cancelación marcada como revisada', 'success');
        await loadAllData();
        await loadDashboardData();
        closeAdminBookingDetailModal();
    } catch (err) {
        showToast(err.message || 'No se pudo actualizar el estado de la notificación', 'error');
    } finally {
        isAdminActionInProgress = false;
    }
}

function openAdminWhatsAppValidation(bookingId) {
    let booking = null;
    if (adminDashboardData && Array.isArray(adminDashboardData.pendientesLista)) {
        booking = adminDashboardData.pendientesLista.find(b => b.id === bookingId);
    }
    if (!booking && adminDashboardData && Array.isArray(adminDashboardData.agendaHoy)) {
        booking = adminDashboardData.agendaHoy.find(b => b.id === bookingId);
    }
    if (!booking && Array.isArray(bookingsList)) {
        booking = bookingsList.find(b => b.id === bookingId);
    }
    if (!booking && Array.isArray(historyBookingsList)) {
        booking = historyBookingsList.find(b => b.id === bookingId);
    }

    if (!booking) {
        showToast('No se encontró la información de la reserva.', 'error');
        return;
    }

    const rawPhone = String(booking.telefono || booking.phone || '').replace(/\D/g, '');
    if (!rawPhone || rawPhone.length < 7) {
        showToast('El teléfono registrado no es válido para abrir WhatsApp.', 'error');
        return;
    }

    const nombre = (booking.nombreCliente || booking.customerName || 'Cliente').trim();
    const codigo = (booking.codigoReserva || booking.code || '').trim();
    const fecha = booking.fechaCita || booking.date || '';
    const hora = booking.horaCita || booking.time || '';
    const itemsRaw = booking.items || booking.services || [];
    const items = Array.isArray(itemsRaw) ? itemsRaw.join(', ') : String(itemsRaw || '');

    let msg = `Hola ${nombre}, te saludamos de ISIVI Salón.`;
    if (codigo) {
        msg += ` Estamos revisando el comprobante de pago de tu reserva *${codigo}*`;
    } else {
        msg += ` Estamos revisando el comprobante de pago de tu reserva`;
    }
    if (fecha && hora) {
        msg += ` programada para el *${fecha} a las ${hora}*`;
    }
    if (items) {
        msg += ` (${items})`;
    }
    msg += `. ¿Podrías confirmarnos por este medio si tienes alguna duda o deseas enviarnos el comprobante actualizado? ¡Muchas gracias!`;

    trackEvent('whatsapp_click', {
        context: 'payment_receipt_pending',
        booking_id: booking.id || bookingId
    });

    openWhatsApp(msg, rawPhone);
}

// ============ ADMIN RESCHEDULE MODAL ============

function openAdminRescheduleModal(bookingId) {
    const booking = bookingsList.find(b => b.id === bookingId);
    document.getElementById('admin-reschedule-booking-id').value = bookingId;
    const label = document.getElementById('admin-reschedule-target-label');
    if (booking) label.textContent = `${booking.customerName} (${booking.code}) · Cita actual: ${booking.date} ${booking.time}`;
    
    const todayStr = formatDateKey(new Date());
    const dateInput = document.getElementById('admin-reschedule-date');
    dateInput.min = todayStr;
    dateInput.value = booking ? (booking.date || todayStr) : todayStr;
    handleAdminRescheduleDateChange();

    document.getElementById('admin-reschedule-modal').classList.remove('hidden');
}

function closeAdminRescheduleModal() {
    document.getElementById('admin-reschedule-modal').classList.add('hidden');
}

async function handleAdminRescheduleDateChange() {
    const date = document.getElementById('admin-reschedule-date').value;
    const select = document.getElementById('admin-reschedule-time');
    const bookingId = document.getElementById('admin-reschedule-booking-id').value;
    const currentBooking = bookingsList.find(b => b.id === bookingId);

    select.innerHTML = '<option value="">Cargando horarios...</option>';
    if (!date) return;

    try {
        const occupiedTimes = await apiGet(`/reservas/disponibilidad?fecha=${encodeURIComponent(date)}`);
        const slots = appointmentTimeSlots.filter(h => !isPastTimeSlot(date, h) && (!occupiedTimes.includes(h) || (currentBooking && currentBooking.date === date && currentBooking.time === h)));
        if (slots.length === 0) {
            select.innerHTML = '<option value="">No hay horarios disponibles para esta fecha</option>';
        } else {
            select.innerHTML = slots.map(h => `<option value="${h}" ${currentBooking && currentBooking.time === h ? 'selected' : ''}>${h}</option>`).join('');
        }
    } catch (err) {
        console.error(err);
        const fallbackSlots = appointmentTimeSlots.filter(h => !isPastTimeSlot(date, h));
        if (fallbackSlots.length === 0) {
            select.innerHTML = '<option value="">No hay horarios disponibles para esta fecha</option>';
        } else {
            select.innerHTML = fallbackSlots.map(h => `<option value="${h}">${h}</option>`).join('');
        }
    }
}

async function submitAdminReschedule() {
    if (isAdminActionInProgress) return;
    const id = document.getElementById('admin-reschedule-booking-id').value;
    const date = document.getElementById('admin-reschedule-date').value;
    const time = document.getElementById('admin-reschedule-time').value;
    const errorEl = document.getElementById('admin-reschedule-error');
    if (errorEl) errorEl.classList.add('hidden');

    if (!id || !date || !time) {
        if (errorEl) {
            errorEl.querySelector('span').textContent = 'Selecciona fecha y horario disponibles para reprogramar.';
            errorEl.classList.remove('hidden');
        } else {
            showToast('Selecciona fecha y horario', 'error');
        }
        return;
    }

    isAdminActionInProgress = true;
    try {
        await apiPatch(`/reservas/${id}/admin-reprogramar`, {
            fechaCita: date,
            horaCita: time
        });
        showToast('Cita reprogramada correctamente por el Administrador', 'success');
        closeAdminRescheduleModal();
        await loadAllData();
        await loadDashboardData();
        renderAdminBookingsTable();
    } catch (err) {
        const errorText = (err.status === 409 || (err.message && err.message.includes('disponible')))
            ? 'Este horario acaba de ser ocupado. Selecciona otro horario en la agenda.'
            : (err.message || 'Error al reprogramar cita');
        if (errorEl) {
            errorEl.querySelector('span').textContent = errorText;
            errorEl.classList.remove('hidden');
            handleAdminRescheduleDateChange();
        } else {
            showToast(errorText, 'error');
        }
    } finally {
        isAdminActionInProgress = false;
    }
}


// --- Categorías de Productos CRUD Admin ---
function renderAdminProductCategories() {
    const container = document.getElementById('admin-product-categories') || document.getElementById('admin-product-categories-list');
    if (!container) return;

    if (!productCategoriesData || productCategoriesData.length === 0) {
        container.innerHTML = '<p class="text-xs text-isivi-300">Aún no hay categorías de productos. Añade una para clasificar tus productos.</p>';
        return;
    }

    container.innerHTML = productCategoriesData.map(category => {
        const prodCount = productsData.filter(prod => prod.category === category.id).length;
        const kitCount = kitsData.filter(kit => kit.category === category.id).length;
        const count = prodCount + kitCount;
        return `<span class="inline-flex items-center gap-2 rounded-xl border border-stone-700 bg-stone-950 px-3 py-2 text-xs text-isivi-200"><span>${category.name} <span class="text-isivi-gold">(${count})</span></span><button type="button" onclick="deleteProductCategory('${category.id}', '${category.name.replace(/'/g, "\\'")}')" class="text-red-300 hover:text-red-200" title="Eliminar categoría"><i class="fa-solid fa-trash"></i></button></span>`;
    }).join('');
}

function populateProductCategorySelect(selectedId = '') {
    const select = document.getElementById('prod-category');
    if (!select) return;

    const options = [
        '<option value="">Sin categoría asignada</option>',
        ...(productCategoriesData || []).map(cat => `<option value="${cat.id}" ${selectedId === cat.id ? 'selected' : ''}>${cat.name}</option>`)
    ];
    select.innerHTML = options.join('');
}

async function handleProductCategorySubmit(e) {
    if (e) e.preventDefault();
    const input = document.getElementById('product-category-name') || document.getElementById('new-prod-category-name');
    const name = (input?.value || '').trim();
    if (!name) {
        showToast('Escribe un nombre de categoría válido', 'error');
        return;
    }

    try {
        const nueva = await apiPost('/categorias-producto', { nombre: name });
        productCategoriesData.push(mapFromApiCategoriaProducto(nueva));
        if (input) input.value = '';
        renderAdminProductCategories();
        populateProductCategorySelect();
        renderProductCategoryPills();
        renderKitCategoryPills();
        showToast('Categoría añadida y publicada en Productos y Kits', 'success');
    } catch (err) {
        showToast(err.message || 'No se pudo crear la categoría', 'error');
    }
}

async function toggleProductCategoryActive(id) {
    try {
        const actualizada = await apiPatch(`/categorias-producto/${id}/toggle`);
        const idx = productCategoriesData.findIndex(c => c.id === id);
        if (idx >= 0) productCategoriesData[idx] = mapFromApiCategoriaProducto(actualizada);
        renderAdminProductCategories();
        populateProductCategorySelect();
        renderProductCategoryPills();
        renderKitCategoryPills();
        showToast('Estado de categoría actualizado', 'success');
    } catch (err) {
        showToast(err.message || 'Error al actualizar categoría', 'error');
    }
}

async function deleteProductCategory(id, name = '') {
    const cat = productCategoriesData.find(c => c.id === id);
    const catName = name || (cat ? cat.name : 'esta categoría');
    if (!confirm(`¿Deseas eliminar la categoría "${catName}"?`)) return;

    try {
        await apiDelete(`/categorias-producto/${id}`);
        productCategoriesData = productCategoriesData.filter(c => c.id !== id);
        renderAdminProductCategories();
        populateProductCategorySelect();
        renderProductCategoryPills();
        renderKitCategoryPills();
        showToast('Categoría eliminada correctamente', 'success');
    } catch (err) {
        const msg = (err.status === 409 || (err.message && err.message.toLowerCase().includes('producto')))
            ? 'La categoría no puede eliminarse porque tiene productos o kits asociados.'
            : (err.message || 'No se pudo eliminar la categoría');
        showToast(msg, 'error');
    }
}

function onProductTypeChange() {
    const type = document.getElementById('prod-type')?.value;
    const catWrap = document.getElementById('prod-category-wrap');
    const priceTypeWrap = document.getElementById('prod-price-type-wrap');
    const isKit = type === 'kit';

    if (catWrap) catWrap.classList.remove('hidden');
    if (priceTypeWrap) priceTypeWrap.classList.toggle('hidden', isKit);

    if (isKit) {
        const varSection = document.getElementById('prod-variants-section');
        const singlePriceWrap = document.getElementById('prod-single-price-wrap');
        const singleQtyWrap = document.getElementById('prod-single-qty-wrap');
        if (varSection) varSection.classList.add('hidden');
        if (singlePriceWrap) singlePriceWrap.classList.remove('hidden');
        if (singleQtyWrap) singleQtyWrap.classList.remove('hidden');
    } else {
        toggleProdPriceTypeFields();
    }
}

function toggleProdPriceTypeFields() {
    const type = document.getElementById('prod-type')?.value;
    if (type === 'kit') return;

    const priceType = document.getElementById('prod-price-type')?.value || 'UNICO';
    const isVariants = priceType === 'VARIANTES';

    const varSection = document.getElementById('prod-variants-section');
    const singlePriceWrap = document.getElementById('prod-single-price-wrap');
    const singleQtyWrap = document.getElementById('prod-single-qty-wrap');

    if (varSection) varSection.classList.toggle('hidden', !isVariants);
    if (singlePriceWrap) singlePriceWrap.classList.toggle('hidden', isVariants);
    if (singleQtyWrap) singleQtyWrap.classList.toggle('hidden', isVariants);

    if (isVariants) {
        renderAdminVariantsList();
    }
}

function renderAdminVariantsList() {
    const container = document.getElementById('prod-variants-list');
    if (!container) return;

    if (!currentEditingVariants || currentEditingVariants.length === 0) {
        container.innerHTML = '<div class="text-center py-4 text-xs text-stone-500">Agrega al menos una variante (ej: 60ml, 120ml o Con Dispensador)</div>';
        return;
    }

    container.innerHTML = currentEditingVariants.map((v, index) => {
        return `
            <div class="flex items-center justify-between p-2.5 rounded-xl bg-stone-900 border border-stone-800 gap-2">
                <div class="space-y-0.5 min-w-0 flex-1">
                    <span class="font-bold text-white text-xs block truncate">${v.nombre}</span>
                    <span class="text-[11px] text-isivi-gold font-mono">$${Number(v.precio || 0).toLocaleString('es-CO')} · <span class="text-stone-300">${v.cantidad || 0} unidades</span></span>
                </div>
                <button type="button" onclick="removeVariantFromForm(${index})" class="px-2 py-1 bg-red-950 hover:bg-red-900 text-red-300 rounded-lg text-xs font-bold" title="Eliminar variante">
                    <i class="fa-solid fa-trash"></i>
                </button>
            </div>
        `;
    }).join('');
}

function addVariantToForm() {
    const nameInput = document.getElementById('new-var-name');
    const priceInput = document.getElementById('new-var-price');
    const qtyInput = document.getElementById('new-var-qty');

    const name = (nameInput?.value || '').trim();
    const price = parsePrice(priceInput?.value);
    const qty = parseInt(qtyInput?.value, 10);

    if (!name) return showToast('Ingresa el nombre de la variante', 'error');
    if (isNaN(price) || price <= 0) return showToast('El precio de la variante debe ser un valor válido mayor a 0 COP (ej: 30.000)', 'error');
    if (isNaN(qty) || qty < 0) return showToast('La cantidad debe ser 0 o superior', 'error');

    currentEditingVariants.push({
        id: 'var-' + Date.now() + '-' + Math.floor(Math.random() * 1000),
        nombre: name,
        precio: price,
        cantidad: qty,
        enStock: qty > 0,
        activo: true
    });

    if (nameInput) nameInput.value = '';
    if (priceInput) priceInput.value = '';
    if (qtyInput) qtyInput.value = '1';

    renderAdminVariantsList();
}

function removeVariantFromForm(index) {
    if (index >= 0 && index < currentEditingVariants.length) {
        const v = currentEditingVariants[index];
        if (!confirm(`¿Eliminar la variante "${v.nombre}" del formulario?`)) return;
        currentEditingVariants.splice(index, 1);
        renderAdminVariantsList();
        showToast('Variante eliminada correctamente.', 'success');
    }
}

// --- Productos & Kits CRUD & Filtros ---
let currentProductFilterStatus = 'TODOS';

function filterProductsByStatus(status) {
    currentProductFilterStatus = status || 'TODOS';
    document.querySelectorAll('.adm-prod-filter-pill').forEach(btn => {
        btn.className = 'adm-prod-filter-pill px-3 py-1.5 rounded-xl text-xs font-bold transition bg-stone-950 text-stone-300 border border-stone-800 hover:border-isivi-gold';
    });
    const activeBtn = document.getElementById(`adm-prod-pill-${currentProductFilterStatus}`);
    if (activeBtn) {
        activeBtn.className = 'adm-prod-filter-pill px-3 py-1.5 rounded-xl text-xs font-bold transition bg-isivi-gold text-isivi-black shadow-sm';
    }
    renderAdminProductsTable();
}

function applyAdminProductFilters() {
    renderAdminProductsTable();
}

function getFilteredAdminProducts() {
    const allItems = [
        ...productsData.map(p => {
            const catId = p.category || p.categoriaId;
            const cat = productCategoriesData.find(c => String(c.id) === String(catId));
            return {
                ...p,
                typeLabel: 'Producto Detal',
                categoryLabel: cat ? cat.name : (p.categoryName || (catId ? String(catId) : 'Sin categoría asignada'))
            };
        }),
        ...kitsData.map(k => {
            const catId = k.category || k.categoriaId;
            const cat = productCategoriesData.find(c => String(c.id) === String(catId));
            return {
                ...k,
                typeLabel: 'Kit Capilar',
                categoryLabel: cat ? cat.name : (k.categoryName || (catId ? String(catId) : 'Sin categoría asignada'))
            };
        })
    ];

    const searchQuery = (document.getElementById('adm-products-search')?.value || '').trim().toLocaleLowerCase('es-CO');

    return allItems.filter(item => {
        const inStock = item.inStock !== false;
        const qty = item.quantity ?? 0;
        const isAgotado = !inStock || qty <= 0;
        const isStockBajo = isAgotado || qty <= 3;

        // Filtro por Estado
        if (currentProductFilterStatus === 'EN_STOCK' && isAgotado) return false;
        if (currentProductFilterStatus === 'AGOTADOS' && !isAgotado) return false;
        if (currentProductFilterStatus === 'STOCK_BAJO' && !isStockBajo) return false;

        // Filtro por búsqueda
        if (searchQuery) {
            const name = (item.name || '').toLocaleLowerCase('es-CO');
            const cat = (item.categoryLabel || '').toLocaleLowerCase('es-CO');
            const type = (item.typeLabel || '').toLocaleLowerCase('es-CO');
            if (!name.includes(searchQuery) && !cat.includes(searchQuery) && !type.includes(searchQuery)) {
                return false;
            }
        }

        return true;
    });
}

function renderAdminProductsTable() {
    const tbody = document.getElementById('adm-products-table-body');
    const badge = document.getElementById('adm-products-count-badge');
    if (!tbody) return;

    const visibleItems = getFilteredAdminProducts();
    const totalItems = productsData.length + kitsData.length;

    if (badge) {
        badge.textContent = `${visibleItems.length} de ${totalItems}`;
    }

    if (visibleItems.length === 0) {
        const msg = totalItems === 0
            ? 'Aún no hay productos ni kits registrados.'
            : 'No se encontraron productos o kits con los filtros seleccionados.';
        tbody.innerHTML = `<tr><td colspan="7" class="p-6 text-center text-isivi-300">${msg}</td></tr>`;
        return;
    }

    tbody.innerHTML = visibleItems.map(item => {
        const inStock = item.inStock !== false;
        const qty = item.quantity ?? 0;
        const isAgotado = !inStock || qty <= 0;
        const isVariantType = item.priceType === 'VARIANTES' && Array.isArray(item.variants) && item.variants.length > 0;
        
        let priceDisplay = `$${(item.price || 0).toLocaleString('es-CO')}`;
        let stockDisplay = qty;

        if (isVariantType) {
            const prices = item.variants.map(v => Number(v.precio || 0));
            const minP = Math.min(...prices);
            const maxP = Math.max(...prices);
            priceDisplay = minP === maxP ? `$${minP.toLocaleString('es-CO')}` : `$${minP.toLocaleString('es-CO')} - $${maxP.toLocaleString('es-CO')}`;
            stockDisplay = `${qty} (${item.variants.length} var.)`;
        }

        return `
            <tr class="hover:bg-stone-900 transition" id="adm-product-row-${item.id}" data-product-id="${item.id}">
                <td class="p-3"><img src="${item.img || '/images/isivi-logo-transparent.png'}" alt="${item.name}" class="w-10 h-10 object-cover rounded-lg border border-stone-800" onerror="this.src='/images/isivi-logo-transparent.png'"></td>
                <td class="p-3">
                    <span class="font-bold text-white block">${item.name}</span>
                    <div class="flex items-center gap-1.5 flex-wrap mt-0.5">
                        <span class="text-[10px] text-isivi-gold font-semibold">${item.typeLabel}</span>
                        ${isVariantType ? `<span class="px-1.5 py-0.2 rounded text-[9px] font-bold bg-amber-950 text-amber-300 border border-amber-500/30">Variantes</span>` : ''}
                    </div>
                </td>
                <td class="p-3">
                    <span class="text-xs text-stone-300 font-mono">${item.categoryLabel}</span>
                </td>
                <td class="p-3 font-semibold text-isivi-gold whitespace-nowrap">${priceDisplay}</td>
                <td class="p-3 text-center font-bold text-white">${stockDisplay}</td>
                <td class="p-3 text-center">
                    <button onclick="toggleStockStatus('${item.id}', '${item.type}')" class="px-3 py-1 rounded-full text-[10px] font-bold transition ${!isAgotado ? 'bg-emerald-950 text-emerald-300 border border-emerald-500/30' : 'bg-red-950 text-red-300 border border-red-500/30'}">
                        ${!isAgotado ? '<i class="fa-solid fa-check mr-1"></i> En Stock' : '<i class="fa-solid fa-ban mr-1"></i> Agotado'}
                    </button>
                </td>
                <td class="p-3 text-right space-x-1 whitespace-nowrap">
                    <button onclick="editProduct('${item.id}', '${item.type}')" class="px-2.5 py-1 bg-stone-900 border border-stone-800 hover:border-isivi-gold text-isivi-gold rounded text-[11px] font-bold"><i class="fa-solid fa-pen"></i> Editar</button>
                    <button onclick="deleteProduct('${item.id}', '${item.type}')" class="px-2 py-1 bg-red-950 hover:bg-red-900 text-red-300 rounded text-[11px] font-bold"><i class="fa-solid fa-trash"></i> Eliminar</button>
                </td>
            </tr>
        `;
    }).join('');
}

async function deleteProduct(id, type) {
    const isKit = type === 'kit';
    const label = isKit ? 'kit' : 'producto';
    if (!confirm(`¿Seguro que deseas eliminar este ${label}?`)) return;
    try {
        const path = isKit ? `/kits/${id}` : `/productos/${id}`;
        await apiDelete(path);
        if (isKit) {
            kitsData = kitsData.filter(k => k.id !== id);
        } else {
            productsData = productsData.filter(p => p.id !== id);
        }
        renderAdminProductsTable();
        renderProductsGrid();
        renderKitsGrid();
        loadDashboardData();
        updateAdminStats();
        showToast(isKit ? 'Kit eliminado correctamente.' : 'Producto eliminado correctamente.', 'success');
    } catch (err) {
        console.error(err);
        const backendMsg = err.message || err.mensaje;
        if (backendMsg && backendMsg.toLowerCase().includes('pedido')) {
            showToast(isKit ? 'El kit no puede eliminarse porque está siendo utilizado en pedidos.' : 'El producto no puede eliminarse porque está siendo utilizado en pedidos.', 'error');
        } else {
            showToast(backendMsg || (isKit ? 'No se pudo eliminar el kit.' : 'No se pudo eliminar el producto.'), 'error');
        }
    }
}

async function toggleStockStatus(id, type) {
    try {
        if (type === 'kit') {
            const actualizado = await apiPatch(`/kits/${id}/stock`);
            const kit = kitsData.find(k => k.id === id);
            if (kit) kit.inStock = actualizado.enStock !== false;
        } else {
            const actualizado = await apiPatch(`/productos/${id}/stock`);
            const prod = productsData.find(p => p.id === id);
            if (prod) prod.inStock = actualizado.enStock !== false;
        }
        renderAdminProductsTable();
        renderProductsGrid();
        renderKitsGrid();
        loadDashboardData();
        showToast("Estado de stock actualizado correctamente");
    } catch (err) {
        console.error(err);
        showToast("No se pudo actualizar el stock", "error");
    }
}

async function handleProductFormSubmit(e) {
    e.preventDefault();
    const errorEl = document.getElementById('prod-form-error');
    if (errorEl) errorEl.classList.add('hidden');

    const editId = document.getElementById('prod-edit-id').value;
    const name = document.getElementById('prod-name').value.trim();
    const type = document.getElementById('prod-type').value;
    const categoryId = document.getElementById('prod-category')?.value || null;
    const priceType = document.getElementById('prod-price-type')?.value || 'UNICO';
    let price = parsePrice(document.getElementById('prod-price')?.value);
    let quantity = parseInt(document.getElementById('prod-quantity').value, 10);
    const imageInput = document.getElementById('prod-img');
    const desc = document.getElementById('prod-desc').value.trim();
    const inStock = document.getElementById('prod-stock').value === 'true';

    if (!name) {
        if (errorEl) { errorEl.querySelector('span').textContent = 'Ingresa el nombre del producto o kit.'; errorEl.classList.remove('hidden'); }
        document.getElementById('prod-name')?.focus();
        return;
    }

    if (type !== 'kit' && priceType === 'VARIANTES') {
        if (!currentEditingVariants || currentEditingVariants.length === 0) {
            if (errorEl) { errorEl.querySelector('span').textContent = 'Debes añadir al menos una variante con precio y cantidad.'; errorEl.classList.remove('hidden'); }
            return;
        }
        price = Math.min(...currentEditingVariants.map(v => Number(v.precio || 0)));
        quantity = currentEditingVariants.reduce((sum, v) => sum + (Number(v.cantidad) || 0), 0);
    } else {
        if (isNaN(price) || price <= 0) {
            if (errorEl) { errorEl.querySelector('span').textContent = 'El precio debe ser un valor válido mayor a 0 COP (ej: 30.000).'; errorEl.classList.remove('hidden'); }
            document.getElementById('prod-price')?.focus();
            return;
        }
        if (isNaN(quantity) || quantity < 0) {
            if (errorEl) { errorEl.querySelector('span').textContent = 'La cantidad en inventario debe ser 0 o superior.'; errorEl.classList.remove('hidden'); }
            document.getElementById('prod-quantity')?.focus();
            return;
        }
    }

    const existingItem = editId
        ? [...productsData, ...kitsData].find(item => item.id === editId)
        : null;
    let img = existingItem ? existingItem.img : '';

    if (imageInput.files.length > 0) {
        const imageFile = imageInput.files[0];
        if (!['image/jpeg', 'image/png', 'image/webp'].includes(imageFile.type)) {
            if (errorEl) { errorEl.querySelector('span').textContent = 'Selecciona una imagen JPG, PNG o WebP.'; errorEl.classList.remove('hidden'); }
            return;
        }
        if (imageFile.size > 5 * 1024 * 1024) {
            if (errorEl) { errorEl.querySelector('span').textContent = 'La imagen no puede superar los 5 MB.'; errorEl.classList.remove('hidden'); }
            return;
        }
        try {
            img = await readImageFile(imageFile);
        } catch (err) {
            console.error(err);
            if (errorEl) { errorEl.querySelector('span').textContent = 'No se pudo leer la imagen seleccionada.'; errorEl.classList.remove('hidden'); }
            return;
        }
    }

    if (!img) {
        if (errorEl) { errorEl.querySelector('span').textContent = 'Selecciona una imagen para el producto o kit.'; errorEl.classList.remove('hidden'); }
        return;
    }

    const itemPayload = {
        name,
        price,
        img,
        desc,
        inStock,
        quantity,
        category: categoryId || null,
        priceType: type === 'kit' ? 'UNICO' : priceType,
        variants: type === 'kit' ? [] : (priceType === 'VARIANTES' ? currentEditingVariants : [])
    };

    try {
        if (editId) {
            const path = type === 'kit' ? `/kits/${editId}` : `/productos/${editId}`;
            const actualizado = await apiPut(path, mapToApiProducto(itemPayload));
            if (type === 'kit') {
                const idx = kitsData.findIndex(k => k.id === editId);
                if (idx >= 0) kitsData[idx] = mapFromApiKit(actualizado);
            } else {
                const idx = productsData.findIndex(p => p.id === editId);
                if (idx >= 0) productsData[idx] = mapFromApiProducto(actualizado);
            }
            showToast(type === 'kit' ? 'Kit actualizado correctamente.' : 'Producto actualizado correctamente.', 'success');
        } else {
            const path = type === 'kit' ? '/kits' : '/productos';
            const creado = await apiPost(path, mapToApiProducto(itemPayload));
            if (type === 'kit') kitsData.push(mapFromApiKit(creado));
            else productsData.push(mapFromApiProducto(creado));
            showToast(type === 'kit' ? 'Kit creado correctamente.' : 'Producto creado correctamente.', 'success');
        }
    } catch (err) {
        console.error(err);
        const errorAction = editId ? (type === 'kit' ? 'No se pudo actualizar el kit.' : 'No se pudo actualizar el producto.') : (type === 'kit' ? 'No se pudo crear el kit.' : 'No se pudo crear el producto.');
        showToast(err.message || errorAction, 'error');
        return;
    }

    resetProductForm();
    renderAdminProductsTable();
    renderProductsGrid();
    renderKitsGrid();
    updateAdminStats();
    loadDashboardData();
}

async function readImageFile(file) {
    showToast('Subiendo imagen a la nube...', 'info');
    try {
        const formData = new FormData();
        formData.append('file', file);
        const headers = getAuthHeaders();
        const res = await fetchWithTimeout(`${API_BASE}/uploads/imagen`, {
            method: 'POST',
            headers: headers,
            body: formData
        }, REQUEST_TIMEOUT_MS);
        if (!res.ok) {
            const errorMsg = `HTTP ${res.status}`;
            let errorDetail = '';
            try {
                const errJson = await res.json();
                errorDetail = errJson.mensaje || JSON.stringify(errJson);
            } catch (e) {
                try {
                    errorDetail = await res.text();
                } catch (t) {}
            }
            throw new Error(`${errorMsg} - ${errorDetail}`);
        }
        const data = await res.json();
        if (data && data.secure_url) {
            showToast('Imagen subida correctamente a Cloudinary.', 'success');
            return data.secure_url;
        } else {
            throw new Error('La respuesta no contiene secure_url.');
        }
    } catch (err) {
        console.error('Error al subir a Cloudinary, usando base64:', err);
        try {
            await fetch(`${API_BASE}/uploads/log-fallback`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    motivo: err.message || 'Error general de red/upload',
                    detalle: err.stack || String(err)
                })
            });
        } catch (logErr) {
            console.error('No se pudo registrar fallback en backend:', logErr);
        }
        showToast('Guardando en base64 por falla en Cloudinary.', 'warning');
        return new Promise((resolve, reject) => {
            const reader = new FileReader();
            reader.onload = () => resolve(reader.result);
            reader.onerror = () => reject(reader.error);
            reader.readAsDataURL(file);
        });
    }
}


function editProduct(id, type) {
    const item = (type === 'kit') ? kitsData.find(k => k.id === id) : productsData.find(p => p.id === id);
    if (!item) return;

    document.getElementById('prod-edit-id').value = item.id;
    document.getElementById('prod-name').value = item.name;
    document.getElementById('prod-type').value = item.type || type;
    document.getElementById('prod-price').value = formatPriceNumber(item.price);
    document.getElementById('prod-quantity').value = item.quantity ?? 0;
    document.getElementById('prod-img').value = '';
    document.getElementById('prod-img-help').textContent = 'Imagen actual conservada. Selecciona otra solo si deseas reemplazarla (JPG, PNG o WebP; máximo 5 MB).';
    document.getElementById('prod-desc').value = item.desc || '';
    document.getElementById('prod-stock').value = (item.inStock !== false) ? 'true' : 'false';

    const catSelect = document.getElementById('prod-category');
    if (catSelect) catSelect.value = item.category || '';

    const priceTypeSelect = document.getElementById('prod-price-type');
    if (priceTypeSelect) priceTypeSelect.value = item.priceType || 'UNICO';

    currentEditingVariants = Array.isArray(item.variants) ? item.variants.map(v => ({ ...v })) : [];

    onProductTypeChange();

    document.getElementById('prod-form-title').innerHTML = `<i class="fa-solid fa-pen-to-square"></i> Editando: ${item.name}`;
    document.getElementById('cancel-prod-edit-btn').classList.remove('hidden');
    window.scrollTo({ top: 0, behavior: 'smooth' });
}

function resetProductForm() {
    document.getElementById('prod-edit-id').value = '';
    document.getElementById('product-editor-form').reset();
    document.getElementById('prod-quantity').value = 1;
    document.getElementById('prod-price-type').value = 'UNICO';
    currentEditingVariants = [];
    onProductTypeChange();
    document.getElementById('prod-img-help').textContent = 'Selecciona una imagen JPG, PNG o WebP (máximo 5 MB).';
    document.getElementById('prod-form-title').innerHTML = `<i class="fa-solid fa-circle-plus"></i> Crear Nuevo Producto o Kit Capilar`;
    document.getElementById('cancel-prod-edit-btn').classList.add('hidden');
}

function showAdminUserForm() {
    const modal = document.getElementById('admin-user-modal');
    modal.classList.remove('hidden');
    modal.classList.add('flex');
    document.getElementById('new-admin-error').classList.add('hidden');
    setTimeout(() => document.getElementById('new-admin-username').focus(), 0);
}

function hideAdminUserForm() {
    const modal = document.getElementById('admin-user-modal');
    modal.classList.add('hidden');
    modal.classList.remove('flex');
    document.getElementById('new-admin-username').value = '';
    document.getElementById('new-admin-password').value = '';
}

async function handleAdminUserSubmit(event) {
    event.preventDefault();
    const error = document.getElementById('new-admin-error');
    error.classList.add('hidden');
    try {
        await apiPost('/auth/administradores', {
            usuario: document.getElementById('new-admin-username').value.trim(),
            contrasena: document.getElementById('new-admin-password').value
        });
        hideAdminUserForm();
        renderAdminUsers();
        showToast('Administrador creado correctamente', 'success');
    } catch (err) {
        console.error(err);
        error.textContent = 'No se pudo crear el usuario. Verifica que no exista ya.';
        error.classList.remove('hidden');
    }
}

async function renderAdminUsers() {
    const tableBody = document.getElementById('adm-admins-table-body');
    if (!tableBody) return;
    tableBody.innerHTML = '<tr><td colspan="2" class="p-4 text-center text-isivi-300">Cargando administradores...</td></tr>';
    try {
        const administrators = await apiGet('/auth/administradores');
        tableBody.innerHTML = administrators.length
            ? administrators.map(administrator => `
                <tr class="bg-stone-950/40">
                    <td class="p-3 font-semibold text-white"><i class="fa-solid fa-user-shield mr-2 text-isivi-gold"></i>${administrator.usuario}</td>
                    <td class="p-3 text-right"><button type="button" onclick="deleteAdminUser('${administrator.id}', '${administrator.usuario.replace(/'/g, "\\'")}')" ${administrators.length <= 1 ? 'disabled title="Debe permanecer un administrador"' : ''} class="rounded-lg px-3 py-1.5 text-[11px] font-bold ${administrators.length <= 1 ? 'cursor-not-allowed bg-stone-800 text-stone-500' : 'bg-red-950 text-red-300 hover:bg-red-900'}"><i class="fa-solid fa-trash mr-1"></i>Eliminar</button></td>
                </tr>`).join('')
            : '<tr><td colspan="2" class="p-4 text-center text-isivi-300">No hay administradores registrados.</td></tr>';
    } catch (err) {
        console.error(err);
        tableBody.innerHTML = '<tr><td colspan="2" class="p-4 text-center text-red-300">No se pudieron cargar los administradores.</td></tr>';
    }
}

async function deleteAdminUser(id, username) {
    if (!confirm(`¿Eliminar al administrador ${username}? Esta acción no se puede deshacer.`)) return;
    try {
        await apiDelete(`/auth/administradores/${encodeURIComponent(id)}`);
        await renderAdminUsers();
        showToast('Administrador eliminado correctamente', 'success');
    } catch (err) {
        console.error(err);
        showToast('No se pudo eliminar el administrador. Debe permanecer al menos uno activo.', 'error');
    }
}

async function renderAdminBanners() {
    const container = document.getElementById('adm-banners-list');
    if (!container) return;
    container.innerHTML = '<p class="text-xs text-isivi-300">Cargando banners...</p>';
    try {
        const banners = await apiGet('/banners');
        bannersData = banners.map(mapFromApiBanner);
        renderClientBanners();
        container.innerHTML = bannersData.length ? bannersData.map(banner => `
            <article class="overflow-hidden rounded-2xl border border-stone-800 bg-stone-950"><img src="${banner.image}" alt="${banner.title}" class="h-32 w-full object-cover"><div class="p-4"><div class="flex items-start justify-between gap-3"><div><h4 class="font-bold text-white">${banner.title}</h4><p class="mt-1 text-xs text-isivi-300">${banner.description}</p></div><span class="rounded-full px-2 py-1 text-[9px] font-bold ${banner.active ? 'bg-emerald-950 text-emerald-300' : 'bg-stone-800 text-stone-400'}">${banner.active ? 'VISIBLE' : 'OCULTO'}</span></div><div class="mt-4 flex gap-2"><button type="button" onclick="toggleBanner('${banner.id}')" class="rounded-lg bg-stone-800 px-3 py-1.5 text-[10px] font-bold text-isivi-gold hover:bg-stone-700">${banner.active ? 'Ocultar' : 'Mostrar'}</button><button type="button" onclick="deleteBanner('${banner.id}', '${banner.title.replace(/'/g, "\\'")}')" class="rounded-lg bg-red-950 px-3 py-1.5 text-[10px] font-bold text-red-300 hover:bg-red-900"><i class="fa-solid fa-trash mr-1"></i>Eliminar</button></div></div></article>`).join('') : '<p class="text-xs text-isivi-300">Aún no hay banners creados.</p>';
    } catch (err) {
        console.error(err);
        container.innerHTML = '<p class="text-xs text-red-300">No se pudieron cargar los banners.</p>';
    }
}

async function handleBannerSubmit(event) {
    event.preventDefault();
    const imageInput = document.getElementById('banner-image');
    const imageFile = imageInput.files[0];
    if (!imageFile) {
        showToast('Selecciona una imagen para el banner', 'error');
        return;
    }
    if (!['image/jpeg', 'image/png', 'image/webp'].includes(imageFile.type)) {
        showToast('Selecciona una imagen JPG, PNG o WebP', 'error');
        return;
    }
    if (imageFile.size > 5 * 1024 * 1024) {
        showToast('La imagen no puede superar los 5 MB', 'error');
        return;
    }
    let imagenUrl;
    try {
        imagenUrl = await readImageFile(imageFile);
    } catch (err) {
        console.error(err);
        showToast('No se pudo leer la imagen seleccionada', 'error');
        return;
    }
    const payload = {
        titulo: document.getElementById('banner-title').value.trim(),
        descripcion: document.getElementById('banner-description').value.trim(),
        imagenUrl,
        textoBoton: document.getElementById('banner-button-text').value.trim() || 'Conocer más',
        enlaceBoton: document.getElementById('banner-button-link').value.trim() || '#servicios',
        activo: true
    };
    try {
        await apiPost('/banners', payload);
        event.target.reset();
        await renderAdminBanners();
        showToast('Banner creado y publicado', 'success');
    } catch (err) {
        console.error(err);
        showToast('No se pudo crear el banner', 'error');
    }
}

async function toggleBanner(id) {
    try {
        await apiPatch(`/banners/${id}/activo`);
        await renderAdminBanners();
    } catch (err) {
        console.error(err);
        showToast('No se pudo actualizar el banner', 'error');
    }
}

async function deleteBanner(id, title) {
    if (!confirm(`¿Eliminar el banner "${title}"?`)) return;
    try {
        await apiDelete(`/banners/${id}`);
        await renderAdminBanners();
        showToast('Banner eliminado correctamente.', 'success');
    } catch (err) {
        console.error(err);
        showToast(err.message || 'No se pudo eliminar el banner.', 'error');
    }
}

// --- Servicios CRUD ---
function renderAdminServicesTable() {
    const tbody = document.getElementById('adm-services-table-body');
    if (servicesData.length === 0) {
        tbody.innerHTML = '<tr><td colspan="5" class="p-6 text-center text-isivi-300">Aún no hay servicios registrados.</td></tr>';
        return;
    }
    tbody.innerHTML = servicesData.map(serv => `
        <tr class="hover:bg-stone-900 transition">
            <td class="p-3"><img src="${serv.img}" alt="${serv.name}" class="w-10 h-10 object-cover rounded-lg border border-stone-800"></td>
            <td class="p-3"><span class="font-bold text-white block">${serv.name}</span><span class="text-[10px] text-isivi-300 block">${serv.desc || ''}</span></td>
            <td class="p-3 whitespace-nowrap"><span class="font-semibold text-isivi-gold block capitalize">${serv.category}</span><span class="text-stone-400 text-[10px]">${serv.duration || '60 min'}</span></td>
            <td class="p-3 font-semibold text-isivi-gold whitespace-nowrap">$${serv.price.toLocaleString('es-CO')}</td>
            <td class="p-3 text-right space-x-1 whitespace-nowrap">
                <button onclick="editService('${serv.id}')" class="px-2.5 py-1 bg-stone-900 border border-stone-800 hover:border-isivi-gold text-isivi-gold rounded text-[11px] font-bold"><i class="fa-solid fa-pen"></i> Editar</button>
                <button onclick="deleteService('${serv.id}')" class="px-2 py-1 bg-red-950 hover:bg-red-900 text-red-300 rounded text-[11px] font-bold"><i class="fa-solid fa-trash"></i> Eliminar</button>
            </td>
        </tr>
    `).join('');
}

async function handleServiceFormSubmit(e) {
    e.preventDefault();
    const errorEl = document.getElementById('serv-form-error');
    if (errorEl) errorEl.classList.add('hidden');

    const editId = document.getElementById('serv-edit-id').value;
    const name = document.getElementById('serv-name').value.trim();
    const category = document.getElementById('serv-category').value;
    const price = parsePrice(document.getElementById('serv-price')?.value);
    const duration = document.getElementById('serv-duration').value.trim() || '60 min';
    const imageInput = document.getElementById('serv-img');
    const desc = document.getElementById('serv-desc').value.trim();
    const existingService = editId ? servicesData.find(item => item.id === editId) : null;
    let img = existingService ? existingService.img : '';

    if (!name) {
        if (errorEl) { errorEl.querySelector('span').textContent = 'Ingresa el nombre del servicio.'; errorEl.classList.remove('hidden'); }
        document.getElementById('serv-name')?.focus();
        return;
    }
    if (!category) {
        if (errorEl) { errorEl.querySelector('span').textContent = 'Selecciona una categoría de servicio.'; errorEl.classList.remove('hidden'); }
        document.getElementById('serv-category')?.focus();
        return;
    }
    if (isNaN(price) || price <= 0) {
        if (errorEl) { errorEl.querySelector('span').textContent = 'El precio debe ser un valor válido mayor a 0 COP (ej: 75.000).'; errorEl.classList.remove('hidden'); }
        document.getElementById('serv-price')?.focus();
        return;
    }

    if (imageInput.files.length > 0) {
        const imageFile = imageInput.files[0];
        if (!['image/jpeg', 'image/png', 'image/webp'].includes(imageFile.type)) {
            if (errorEl) { errorEl.querySelector('span').textContent = 'Selecciona una imagen JPG, PNG o WebP.'; errorEl.classList.remove('hidden'); }
            return;
        }
        if (imageFile.size > 5 * 1024 * 1024) {
            if (errorEl) { errorEl.querySelector('span').textContent = 'La imagen no puede superar los 5 MB.'; errorEl.classList.remove('hidden'); }
            return;
        }
        try {
            img = await readImageFile(imageFile);
        } catch (err) {
            console.error(err);
            if (errorEl) { errorEl.querySelector('span').textContent = 'No se pudo leer la imagen seleccionada.'; errorEl.classList.remove('hidden'); }
            return;
        }
    }

    if (!img) {
        if (errorEl) { errorEl.querySelector('span').textContent = 'Selecciona una imagen para el servicio.'; errorEl.classList.remove('hidden'); }
        return;
    }

    const payload = mapToApiServicio({ name, category, price, duration, img, desc });

    try {
        if (editId) {
            const actualizado = await apiPut(`/servicios/${editId}`, payload);
            const idx = servicesData.findIndex(s => s.id === editId);
            if (idx >= 0) servicesData[idx] = mapFromApiServicio(actualizado);
            showToast("Servicio editado con éxito");
        } else {
            const creado = await apiPost('/servicios', payload);
            servicesData.push(mapFromApiServicio(creado));
            showToast("Nuevo servicio añadido al catálogo");
        }
    } catch (err) {
        console.error(err);
        showToast("No se pudo guardar el servicio", "error");
        return;
    }

    resetServiceForm();
    renderAdminServicesTable();
    renderAdminServiceCategories();
    renderCategoryTabs();
    renderServicesGrid();
    updateAdminStats();
}

function editService(id) {
    const s = servicesData.find(item => item.id === id);
    if (!s) return;

    document.getElementById('serv-edit-id').value = s.id;
    document.getElementById('serv-name').value = s.name;
    renderServiceCategorySelect(s.category);
    document.getElementById('serv-price').value = formatPriceNumber(s.price);
    document.getElementById('serv-duration').value = s.duration || '';
    document.getElementById('serv-img').value = '';
    document.getElementById('serv-img-help').textContent = 'Imagen actual conservada. Selecciona otra solo si deseas reemplazarla (JPG, PNG o WebP; máximo 5 MB).';
    document.getElementById('serv-desc').value = s.desc || '';

    document.getElementById('serv-form-title').innerHTML = `<i class="fa-solid fa-pen-to-square"></i> Editando Servicio: ${s.name}`;
    document.getElementById('cancel-serv-edit-btn').classList.remove('hidden');
    window.scrollTo({ top: 0, behavior: 'smooth' });
}

function resetServiceForm() {
    document.getElementById('serv-edit-id').value = '';
    document.getElementById('service-editor-form').reset();
    renderServiceCategorySelect();
    document.getElementById('serv-img-help').textContent = 'Selecciona una imagen JPG, PNG o WebP (máximo 5 MB).';
    document.getElementById('serv-form-title').innerHTML = `<i class="fa-solid fa-scissors"></i> Crear Nuevo Servicio de Peluquería`;
    document.getElementById('cancel-serv-edit-btn').classList.add('hidden');
}

async function deleteService(id) {
    const serv = servicesData.find(s => s.id === id);
    const servName = serv ? serv.name : 'este servicio';
    if (!confirm(`¿Seguro que deseas eliminar el servicio "${servName}"?`)) return;
    try {
        await apiDelete(`/servicios/${id}`);
        servicesData = servicesData.filter(s => s.id !== id);
        renderAdminServicesTable();
        renderAdminServiceCategories();
        renderServicesGrid();
        updateAdminStats();
        showToast('Servicio eliminado correctamente.', 'success');
    } catch (err) {
        console.error(err);
        showToast(err.message || 'No se pudo eliminar el servicio.', 'error');
    }
}

// --- Reservas / Citas ---
function getMonday(date) {
    const result = new Date(date);
    result.setHours(12, 0, 0, 0);
    const day = result.getDay();
    result.setDate(result.getDate() - (day === 0 ? 6 : day - 1));
    return result;
}

function changeAdminWeek(delta) {
    adminWeekStart.setDate(adminWeekStart.getDate() + (delta * 7));
    renderAdminWeeklyCalendar();
}

function selectAdminWeekDay(date) {
    const dateInput = document.getElementById('admin-schedule-date');
    if (dateInput) {
        dateInput.value = date;
        selectAdminScheduleDate();
    }
    document.getElementById('admin-schedule-slots')?.scrollIntoView({ behavior: 'smooth', block: 'center' });
}

// ============ GESTIÓN AVANZADA DE HORARIO SEMANAL ============
let adminModalWorkingDays = [];
let adminModalAgendaSlots = [];
let adminExceptionSpecialSlots = [];

function renderAdminWeeklyScheduleView() {
    const container = document.getElementById('admin-weekly-schedule-cards');
    if (!container) return;

    const daysInfo = [
        { index: 1, name: 'Lunes', short: 'Lun' },
        { index: 2, name: 'Martes', short: 'Mar' },
        { index: 3, name: 'Miércoles', short: 'Mié' },
        { index: 4, name: 'Jueves', short: 'Jue' },
        { index: 5, name: 'Viernes', short: 'Vie' },
        { index: 6, name: 'Sábado', short: 'Sáb' },
        { index: 0, name: 'Domingo', short: 'Dom' }
    ];

    const sortedSlots = [...appointmentTimeSlots].sort((a, b) => agendaTimeInMinutes(a) - agendaTimeInMinutes(b));
    const rangeText = sortedSlots.length > 0 ? `${sortedSlots[0]} – ${sortedSlots[sortedSlots.length - 1]}` : 'Sin turnos';

    container.innerHTML = daysInfo.map(d => {
        const isOpen = agendaConfiguration.diasLaborales.includes(d.index);
        return `
            <div class="rounded-2xl border ${isOpen ? 'border-stone-800 bg-stone-950 hover:border-isivi-500/40' : 'border-stone-900 bg-stone-950/40 opacity-70'} p-3.5 flex flex-col justify-between space-y-2 transition">
                <div class="flex items-center justify-between">
                    <span class="font-bold text-xs ${isOpen ? 'text-white' : 'text-stone-500'}">${d.name}</span>
                    <span class="px-2 py-0.5 rounded-full text-[9px] font-bold ${isOpen ? 'bg-emerald-950 text-emerald-300 border border-emerald-500/30' : 'bg-stone-900 text-stone-500 border border-stone-800'}">
                        ${isOpen ? 'Abierto' : 'Cerrado'}
                    </span>
                </div>
                <div>
                    <span class="block text-[10px] ${isOpen ? 'text-isivi-gold font-medium' : 'text-stone-600'}">${isOpen ? rangeText : 'Día no laboral'}</span>
                    <span class="text-[9px] text-stone-500">${isOpen ? `${sortedSlots.length} turnos configurados` : 'Sin atención al público'}</span>
                </div>
            </div>
        `;
    }).join('');
}

function openAdminWeeklyScheduleModal() {
    const modal = document.getElementById('modal-admin-weekly-schedule');
    const errBox = document.getElementById('admin-weekly-error');
    if (!modal) return;
    if (errBox) errBox.classList.add('hidden');

    adminModalWorkingDays = [...agendaConfiguration.diasLaborales];
    adminModalAgendaSlots = [...appointmentTimeSlots].sort((a, b) => agendaTimeInMinutes(a) - agendaTimeInMinutes(b));

    renderAdminWeeklyModalDays();
    renderAdminWeeklyModalSlots();
    modal.classList.remove('hidden');
}

function closeAdminWeeklyScheduleModal() {
    document.getElementById('modal-admin-weekly-schedule')?.classList.add('hidden');
}

function renderAdminWeeklyModalDays() {
    const container = document.getElementById('admin-modal-working-days');
    if (!container) return;

    const daysInfo = [
        { index: 1, name: 'Lunes' },
        { index: 2, name: 'Martes' },
        { index: 3, name: 'Miércoles' },
        { index: 4, name: 'Jueves' },
        { index: 5, name: 'Viernes' },
        { index: 6, name: 'Sábado' },
        { index: 0, name: 'Domingo' }
    ];

    container.innerHTML = daysInfo.map(d => {
        const checked = adminModalWorkingDays.includes(d.index);
        return `
            <label class="cursor-pointer flex items-center gap-2 p-2.5 rounded-xl border ${checked ? 'border-isivi-gold bg-isivi-500/10 text-white font-bold' : 'border-stone-800 bg-stone-900/60 text-stone-400 font-medium'} text-xs transition">
                <input type="checkbox" onchange="toggleWeeklyModalDay(${d.index}, this.checked)" ${checked ? 'checked' : ''} class="accent-yellow-500 rounded">
                <span>${d.name}</span>
            </label>
        `;
    }).join('');
}

function toggleWeeklyModalDay(dayIndex, isChecked) {
    if (isChecked) {
        if (!adminModalWorkingDays.includes(dayIndex)) adminModalWorkingDays.push(dayIndex);
    } else {
        adminModalWorkingDays = adminModalWorkingDays.filter(d => d !== dayIndex);
    }
    renderAdminWeeklyModalDays();
}

function renderAdminWeeklyModalSlots() {
    const container = document.getElementById('admin-modal-agenda-hours');
    if (!container) return;

    if (adminModalAgendaSlots.length === 0) {
        container.innerHTML = '<span class="text-xs text-stone-500 py-2">No hay turnos configurados. Añade al menos uno.</span>';
        return;
    }

    container.innerHTML = adminModalAgendaSlots.map(time => `
        <span class="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-xl border border-stone-700 bg-stone-950 text-xs font-bold text-white">
            <i class="fa-regular fa-clock text-[10px] text-isivi-gold"></i>
            ${time}
            <button type="button" onclick="removeModalAgendaTime('${time}')" class="text-stone-400 hover:text-red-400 ml-1 transition" title="Eliminar turno">
                <i class="fa-solid fa-xmark text-xs"></i>
            </button>
        </span>
    `).join('');
}

function addModalAgendaTime() {
    const hour = document.getElementById('admin-modal-agenda-hour')?.value;
    const minutes = document.getElementById('admin-modal-agenda-minutes')?.value;
    const period = document.getElementById('admin-modal-agenda-period')?.value;
    if (!hour || !minutes || !period) return;

    const time = `${hour}:${minutes} ${period}`;
    if (adminModalAgendaSlots.includes(time)) {
        showToast('Ese turno ya está en la lista', 'info');
        return;
    }
    adminModalAgendaSlots.push(time);
    adminModalAgendaSlots.sort((a, b) => agendaTimeInMinutes(a) - agendaTimeInMinutes(b));
    renderAdminWeeklyModalSlots();
}

function removeModalAgendaTime(time) {
    if (adminModalAgendaSlots.length <= 1) {
        showToast('Debes mantener al menos un turno de atención', 'error');
        return;
    }
    adminModalAgendaSlots = adminModalAgendaSlots.filter(t => t !== time);
    renderAdminWeeklyModalSlots();
}

async function submitWeeklyScheduleModal() {
    const btn = document.getElementById('btn-save-weekly-schedule');
    const errBox = document.getElementById('admin-weekly-error');
    if (errBox) errBox.classList.add('hidden');

    if (adminModalWorkingDays.length === 0) {
        if (errBox) {
            errBox.querySelector('span').textContent = 'Selecciona al menos un día laboral habitual.';
            errBox.classList.remove('hidden');
        }
        return;
    }

    if (adminModalAgendaSlots.length === 0) {
        if (errBox) {
            errBox.querySelector('span').textContent = 'Configura al menos un turno de atención.';
            errBox.classList.remove('hidden');
        }
        return;
    }

    if (btn) {
        btn.disabled = true;
        btn.innerHTML = '<i class="fa-solid fa-spinner fa-spin mr-1"></i> Guardando...';
    }

    try {
        const payload = {
            diasLaborales: adminModalWorkingDays.sort(),
            horarios: adminModalAgendaSlots
        };
        const updated = await apiPut('/agenda', payload);
        agendaConfiguration = updated;
        appointmentTimeSlots = updated.horarios || adminModalAgendaSlots;

        ensureSelectedDateIsWorking();
        renderAdminWeeklyScheduleView();
        renderAdminScheduleManager();
        renderAdminWeeklyCalendar();
        renderCalendar();
        renderTimeSlots();
        updateSummary();

        closeAdminWeeklyScheduleModal();
        showToast('Horario semanal actualizado correctamente', 'success');
    } catch (err) {
        console.error(err);
        if (errBox) {
            errBox.querySelector('span').textContent = err.message || 'No se pudo actualizar el horario semanal.';
            errBox.classList.remove('hidden');
        }
    } finally {
        if (btn) {
            btn.disabled = false;
            btn.innerHTML = '<i class="fa-solid fa-floppy-disk mr-1"></i> <span>Guardar horario semanal</span>';
        }
    }
}

// ============ GESTIÓN DE EXCEPCIONES Y HORARIOS ESPECIALES ============
async function renderAdminExceptionsList() {
    const container = document.getElementById('admin-exceptions-list');
    if (!container) return;

    container.innerHTML = '<div class="p-4 text-center text-xs text-isivi-300"><i class="fa-solid fa-spinner fa-spin mr-2"></i>Cargando excepciones...</div>';

    try {
        const excepciones = await apiGet('/agenda/excepciones');
        if (!Array.isArray(excepciones) || excepciones.length === 0) {
            container.innerHTML = `
                <div class="p-6 rounded-2xl border border-stone-800/80 bg-stone-950/40 text-center space-y-1">
                    <p class="text-xs font-semibold text-stone-400"><i class="fa-solid fa-calendar-check mr-1.5 text-isivi-gold"></i>No hay excepciones ni días cerrados programados</p>
                    <p class="text-[11px] text-stone-600">El salón atenderá con su horario habitual de Lunes a Domingo.</p>
                </div>
            `;
            return;
        }

        container.innerHTML = excepciones.map(ex => {
            const isClosed = ex.tipo === 'CERRADO';
            const [y, m, d] = (ex.fecha || '').split('-');
            const dateFormatted = `${d}/${m}/${y}`;
            const typeBadge = isClosed 
                ? '<span class="px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-red-950 text-red-300 border border-red-500/30">🔴 Día Cerrado</span>'
                : '<span class="px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-emerald-950 text-emerald-300 border border-emerald-500/30">🟢 Horario Especial</span>';

            const detail = isClosed 
                ? '<span class="text-xs text-stone-400">Sin atención durante toda la jornada</span>'
                : `<span class="text-xs text-isivi-200">${(ex.horarios || []).join(', ')}</span>`;

            return `
                <div class="p-3.5 rounded-2xl border border-stone-800 bg-stone-950 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3 hover:border-stone-700 transition">
                    <div class="space-y-1">
                        <div class="flex items-center gap-2 flex-wrap">
                            <span class="font-bold text-xs text-white font-mono">${dateFormatted}</span>
                            ${typeBadge}
                            ${ex.motivo ? `<span class="text-[11px] text-stone-400 font-medium">· ${ex.motivo}</span>` : ''}
                        </div>
                        <div>${detail}</div>
                    </div>
                    <div class="flex items-center justify-end">
                        <button type="button" onclick="deleteAdminException('${ex.id}', '${dateFormatted}')" class="px-3 py-1.5 bg-red-950 hover:bg-red-900 text-red-300 rounded-xl text-xs font-bold transition flex items-center gap-1">
                            <i class="fa-solid fa-trash text-[10px]"></i> Eliminar
                        </button>
                    </div>
                </div>
            `;
        }).join('');
    } catch (err) {
        console.error(err);
        container.innerHTML = '<div class="p-4 text-center text-xs text-red-300">No se pudieron cargar las excepciones de agenda.</div>';
    }
}

function openAdminExceptionModal() {
    const modal = document.getElementById('modal-admin-exception');
    const errBox = document.getElementById('admin-exception-error');
    const conflictBox = document.getElementById('admin-exception-conflict-box');
    const btnForce = document.getElementById('btn-confirm-exception-force');
    const btnSubmit = document.getElementById('btn-submit-exception');
    const dateInput = document.getElementById('admin-exception-date');
    const typeSelect = document.getElementById('admin-exception-type');
    const reasonInput = document.getElementById('admin-exception-reason');

    if (!modal) return;
    if (errBox) errBox.classList.add('hidden');
    if (conflictBox) conflictBox.classList.add('hidden');
    if (btnForce) btnForce.classList.add('hidden');
    if (btnSubmit) btnSubmit.classList.remove('hidden');

    if (dateInput) dateInput.value = formatDateKey(new Date());
    if (typeSelect) typeSelect.value = 'CERRADO';
    if (reasonInput) reasonInput.value = '';

    adminExceptionSpecialSlots = [...appointmentTimeSlots].slice(0, 4);
    toggleAdminExceptionTypeFields();
    renderExceptionSlotChips();

    modal.classList.remove('hidden');
}

function closeAdminExceptionModal() {
    document.getElementById('modal-admin-exception')?.classList.add('hidden');
}

function toggleAdminExceptionTypeFields() {
    const type = document.getElementById('admin-exception-type')?.value;
    const container = document.getElementById('admin-exception-special-hours-container');
    if (container) {
        if (type === 'HORARIO_ESPECIAL') {
            container.classList.remove('hidden');
        } else {
            container.classList.add('hidden');
        }
    }
}

function renderExceptionSlotChips() {
    const container = document.getElementById('admin-exception-slots-chips');
    if (!container) return;

    if (adminExceptionSpecialSlots.length === 0) {
        container.innerHTML = '<span class="text-xs text-stone-500">Agrega los turnos específicos para este día.</span>';
        return;
    }

    container.innerHTML = adminExceptionSpecialSlots.map(time => `
        <span class="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-xl border border-stone-700 bg-stone-900 text-xs font-bold text-white">
            <i class="fa-regular fa-clock text-[9px] text-isivi-gold"></i>
            ${time}
            <button type="button" onclick="removeExceptionSlotChip('${time}')" class="text-stone-400 hover:text-red-400 ml-1">
                <i class="fa-solid fa-xmark text-xs"></i>
            </button>
        </span>
    `).join('');
}

function addExceptionSlotChip() {
    const hour = document.getElementById('admin-exception-hour')?.value;
    const minutes = document.getElementById('admin-exception-minutes')?.value;
    const period = document.getElementById('admin-exception-period')?.value;
    if (!hour || !minutes || !period) return;

    const time = `${hour}:${minutes} ${period}`;
    if (!adminExceptionSpecialSlots.includes(time)) {
        adminExceptionSpecialSlots.push(time);
        adminExceptionSpecialSlots.sort((a, b) => agendaTimeInMinutes(a) - agendaTimeInMinutes(b));
        renderExceptionSlotChips();
    }
}

function removeExceptionSlotChip(slot) {
    adminExceptionSpecialSlots = adminExceptionSpecialSlots.filter(s => s !== slot);
    renderExceptionSlotChips();
}

async function submitAdminException(forzar = false) {
    const dateInput = document.getElementById('admin-exception-date');
    const typeSelect = document.getElementById('admin-exception-type');
    const reasonInput = document.getElementById('admin-exception-reason');
    const errBox = document.getElementById('admin-exception-error');
    const conflictBox = document.getElementById('admin-exception-conflict-box');
    const btnSubmit = document.getElementById('btn-submit-exception');
    const btnForce = document.getElementById('btn-confirm-exception-force');

    if (errBox) errBox.classList.add('hidden');
    if (conflictBox) conflictBox.classList.add('hidden');

    const fecha = dateInput?.value;
    const tipo = typeSelect?.value;
    const motivo = reasonInput?.value || '';

    if (!fecha) {
        if (errBox) {
            errBox.querySelector('span').textContent = 'Selecciona una fecha válida.';
            errBox.classList.remove('hidden');
        }
        return;
    }

    if (tipo === 'HORARIO_ESPECIAL' && adminExceptionSpecialSlots.length === 0) {
        if (errBox) {
            errBox.querySelector('span').textContent = 'Define al menos un turno para el horario especial.';
            errBox.classList.remove('hidden');
        }
        return;
    }

    const payload = {
        fecha: fecha,
        tipo: tipo,
        horarios: tipo === 'HORARIO_ESPECIAL' ? adminExceptionSpecialSlots : [],
        motivo: motivo,
        forzar: Boolean(forzar)
    };

    if (btnSubmit) {
        btnSubmit.disabled = true;
        btnSubmit.innerHTML = '<i class="fa-solid fa-spinner fa-spin mr-1"></i> Guardando...';
    }

    try {
        await apiPost('/agenda/excepciones', payload);
        await loadAgendaConfiguration();
        renderAdminExceptionsList();
        renderAdminScheduleManager();
        renderAdminWeeklyCalendar();
        renderCalendar();
        renderTimeSlots();

        closeAdminExceptionModal();
        showToast('Excepción de agenda guardada exitosamente', 'success');
    } catch (err) {
        console.warn('Excepcion save response', err);
        if (err.data && err.data.conflicto && Array.isArray(err.data.reservasAfectadas)) {
            // Mostrar conflicto detallado al administrador
            if (conflictBox) {
                const listEl = document.getElementById('admin-exception-conflict-list');
                const msgEl = document.getElementById('admin-exception-conflict-msg');
                if (msgEl) msgEl.textContent = err.data.mensaje || 'Este cambio coincide con citas activas existentes.';
                if (listEl) {
                    listEl.innerHTML = err.data.reservasAfectadas.map(r => `
                        <div class="flex items-center justify-between p-1.5 rounded-lg bg-stone-900 border border-stone-800 text-[10px]">
                            <span class="font-mono font-bold text-isivi-gold">${r.codigo || 'ISV-0000'}</span>
                            <span class="text-white">${r.cliente || 'Cliente'}</span>
                            <span class="text-amber-300 font-bold">${r.hora || 'Turno'}</span>
                        </div>
                    `).join('');
                }
                conflictBox.classList.remove('hidden');
            }
            if (btnForce) btnForce.classList.remove('hidden');
        } else {
            if (errBox) {
                errBox.querySelector('span').textContent = err.message || 'No se pudo guardar la excepción.';
                errBox.classList.remove('hidden');
            }
        }
    } finally {
        if (btnSubmit) {
            btnSubmit.disabled = false;
            btnSubmit.innerHTML = '<i class="fa-solid fa-floppy-disk mr-1"></i> <span>Guardar Excepción</span>';
        }
    }
}

async function deleteAdminException(id, dateLabel) {
    if (!confirm(`¿Eliminar la excepción para la fecha ${dateLabel}? Se restablecerá el horario habitual.`)) return;

    try {
        await apiDelete(`/agenda/excepciones/${id}`);
        await loadAgendaConfiguration();
        renderAdminExceptionsList();
        renderAdminScheduleManager();
        renderAdminWeeklyCalendar();
        renderCalendar();
        renderTimeSlots();
        showToast(`Excepción de ${dateLabel} eliminada`, 'success');
    } catch (err) {
        console.error(err);
        showToast('No se pudo eliminar la excepción', 'error');
    }
}

// ============ MODAL BLOQUEO DE RANGO HORARIO ============
function openAdminBlockRangeModal() {
    const modal = document.getElementById('modal-admin-block-range');
    const errBox = document.getElementById('admin-block-range-error');
    const conflictBox = document.getElementById('admin-block-range-conflict-box');
    const btnForce = document.getElementById('btn-confirm-block-range-force');
    const btnSubmit = document.getElementById('btn-submit-block-range');
    const dateInput = document.getElementById('admin-block-range-date');
    const reasonInput = document.getElementById('admin-block-range-reason');

    if (!modal) return;
    if (errBox) errBox.classList.add('hidden');
    if (conflictBox) conflictBox.classList.add('hidden');
    if (btnForce) btnForce.classList.add('hidden');
    if (btnSubmit) btnSubmit.classList.remove('hidden');

    if (dateInput) dateInput.value = formatDateKey(new Date());
    if (reasonInput) reasonInput.value = '';

    handleBlockRangeDateChange();
    modal.classList.remove('hidden');
}

function closeAdminBlockRangeModal() {
    document.getElementById('modal-admin-block-range')?.classList.add('hidden');
}

function handleBlockRangeDateChange() {
    const date = document.getElementById('admin-block-range-date')?.value;
    const container = document.getElementById('admin-block-range-slots-container');
    if (!container) return;

    if (!date) {
        container.innerHTML = '<span class="col-span-full text-center text-xs text-stone-500 py-2">Selecciona una fecha</span>';
        return;
    }

    let slots = [...appointmentTimeSlots].sort((a, b) => agendaTimeInMinutes(a) - agendaTimeInMinutes(b));
    const dayException = (agendaConfiguration.excepciones || []).find(ex => ex.fecha === date);
    if (dayException && dayException.tipo === 'HORARIO_ESPECIAL' && Array.isArray(dayException.horarios) && dayException.horarios.length > 0) {
        slots = dayException.horarios;
    }

    container.innerHTML = slots.map((slot, index) => `
        <label class="cursor-pointer flex items-center gap-2 p-2 rounded-xl border border-stone-800 bg-stone-900 text-xs text-white hover:border-amber-500/50 transition">
            <input type="checkbox" value="${slot}" class="accent-amber-500 rounded block-range-slot-checkbox" ${index < 3 ? 'checked' : ''}>
            <span>${slot}</span>
        </label>
    `).join('');
}

function selectAllBlockRangeSlots(checkAll) {
    document.querySelectorAll('.block-range-slot-checkbox').forEach(cb => {
        cb.checked = checkAll;
    });
}

async function submitBlockRange(forzar = false) {
    const dateInput = document.getElementById('admin-block-range-date');
    const reasonInput = document.getElementById('admin-block-range-reason');
    const errBox = document.getElementById('admin-block-range-error');
    const conflictBox = document.getElementById('admin-block-range-conflict-box');
    const btnSubmit = document.getElementById('btn-submit-block-range');
    const btnForce = document.getElementById('btn-confirm-block-range-force');

    if (errBox) errBox.classList.add('hidden');
    if (conflictBox) conflictBox.classList.add('hidden');

    const fecha = dateInput?.value;
    const motivo = reasonInput?.value || '';
    const selectedSlots = [...document.querySelectorAll('.block-range-slot-checkbox:checked')].map(cb => cb.value);

    if (!fecha) {
        if (errBox) {
            errBox.querySelector('span').textContent = 'Selecciona una fecha válida.';
            errBox.classList.remove('hidden');
        }
        return;
    }

    if (selectedSlots.length === 0) {
        if (errBox) {
            errBox.querySelector('span').textContent = 'Selecciona al menos un turno para bloquear.';
            errBox.classList.remove('hidden');
        }
        return;
    }

    const payload = {
        fecha: fecha,
        horas: selectedSlots,
        motivo: motivo,
        forzar: Boolean(forzar)
    };

    if (btnSubmit) {
        btnSubmit.disabled = true;
        btnSubmit.innerHTML = '<i class="fa-solid fa-spinner fa-spin mr-1"></i> Guardando...';
    }

    try {
        await apiPost('/reservas/bloqueos/rango', payload);
        await renderAdminScheduleManager();
        renderAdminWeeklyCalendar();
        renderTimeSlots();

        closeAdminBlockRangeModal();
        showToast(`${selectedSlots.length} turno(s) bloqueados correctamente`, 'success');
    } catch (err) {
        console.warn('Bloqueo rango response', err);
        if (err.data && err.data.conflicto && Array.isArray(err.data.reservasAfectadas)) {
            if (conflictBox) {
                const listEl = document.getElementById('admin-block-range-conflict-list');
                const msgEl = document.getElementById('admin-block-range-conflict-msg');
                if (msgEl) msgEl.textContent = err.data.mensaje || 'El rango seleccionado contiene reservas activas existentes.';
                if (listEl) {
                    listEl.innerHTML = err.data.reservasAfectadas.map(r => `
                        <div class="flex items-center justify-between p-1.5 rounded-lg bg-stone-900 border border-stone-800 text-[10px]">
                            <span class="font-mono font-bold text-isivi-gold">${r.codigo || 'ISV-0000'}</span>
                            <span class="text-white">${r.cliente || 'Cliente'}</span>
                            <span class="text-amber-300 font-bold">${r.hora || 'Turno'}</span>
                        </div>
                    `).join('');
                }
                conflictBox.classList.remove('hidden');
            }
            if (btnForce) btnForce.classList.remove('hidden');
        } else {
            if (errBox) {
                errBox.querySelector('span').textContent = err.message || 'No se pudo guardar el bloqueo.';
                errBox.classList.remove('hidden');
            }
        }
    } finally {
        if (btnSubmit) {
            btnSubmit.disabled = false;
            btnSubmit.innerHTML = '<i class="fa-solid fa-lock mr-1"></i> <span>Guardar Bloqueo</span>';
        }
    }
}

// ============ GESTIÓN DE BLOQUEOS RECURRENTES ============
async function renderAdminRecurringBlocksList() {
    const container = document.getElementById('admin-recurring-blocks-list');
    if (!container) return;

    try {
        const list = await apiGet('/agenda/bloqueos-recurrentes');
        if (!Array.isArray(list) || list.length === 0) {
            container.innerHTML = '';
            return;
        }

        const daysNames = ['Domingo', 'Lunes', 'Martes', 'Miércoles', 'Jueves', 'Viernes', 'Sábado'];
        container.innerHTML = `
            <div class="space-y-2 pt-2 border-t border-stone-800/60">
                <h4 class="text-xs font-bold text-purple-300 flex items-center gap-1.5">
                    <i class="fa-solid fa-arrows-rotate"></i> Bloqueos Recurrentes Activos
                </h4>
                ${list.map(b => {
                    const dayName = daysNames[b.diaSemana] || `Día ${b.diaSemana}`;
                    const slotsStr = (b.horarios || []).join(', ');
                    const activeBadge = b.activo 
                        ? '<span class="px-2 py-0.5 rounded-full text-[10px] font-bold bg-purple-950 text-purple-300 border border-purple-500/30">Activo</span>'
                        : '<span class="px-2 py-0.5 rounded-full text-[10px] font-bold bg-stone-900 text-stone-400 border border-stone-700">Pausado</span>';

                    return `
                        <div class="p-3 rounded-2xl border border-stone-800 bg-stone-950 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3 hover:border-purple-500/40 transition">
                            <div class="space-y-1">
                                <div class="flex items-center gap-2 flex-wrap">
                                    <span class="font-bold text-xs text-white">Todos los ${dayName}</span>
                                    ${activeBadge}
                                    ${b.motivo ? `<span class="text-[11px] text-stone-400 font-medium">· ${b.motivo}</span>` : ''}
                                </div>
                                <div class="text-xs text-purple-200">${slotsStr}</div>
                            </div>
                            <div class="flex items-center justify-end gap-2">
                                <button type="button" onclick="toggleRecurringBlock('${b.id}', ${!b.activo})" class="px-3 py-1.5 rounded-xl text-xs font-bold transition ${b.activo ? 'bg-stone-900 hover:bg-stone-800 text-stone-300' : 'bg-purple-950 hover:bg-purple-900 text-purple-300 border border-purple-500/30'}">
                                    ${b.activo ? 'Pausar' : 'Activar'}
                                </button>
                                <button type="button" onclick="deleteRecurringBlock('${b.id}')" class="px-3 py-1.5 bg-red-950 hover:bg-red-900 text-red-300 rounded-xl text-xs font-bold transition flex items-center gap-1">
                                    <i class="fa-solid fa-trash text-[10px]"></i>
                                </button>
                            </div>
                        </div>
                    `;
                }).join('')}
            </div>
        `;
    } catch (err) {
        console.warn('Error al cargar bloqueos recurrentes:', err);
    }
}

function openAdminRecurringBlockModal() {
    const modal = document.getElementById('modal-admin-recurring-block');
    const errBox = document.getElementById('admin-recurring-error');
    const conflictBox = document.getElementById('admin-recurring-conflict-box');
    const btnForce = document.getElementById('btn-confirm-recurring-force');
    const btnSubmit = document.getElementById('btn-submit-recurring-block');
    const dateStart = document.getElementById('admin-recurring-date-start');
    const dateEnd = document.getElementById('admin-recurring-date-end');
    const reasonInput = document.getElementById('admin-recurring-reason');
    const container = document.getElementById('admin-recurring-slots-container');

    if (!modal) return;
    if (errBox) errBox.classList.add('hidden');
    if (conflictBox) conflictBox.classList.add('hidden');
    if (btnForce) btnForce.classList.add('hidden');
    if (btnSubmit) btnSubmit.classList.remove('hidden');

    if (dateStart) dateStart.value = formatDateKey(new Date());
    if (dateEnd) dateEnd.value = '';
    if (reasonInput) reasonInput.value = '';

    if (container) {
        const slots = [...appointmentTimeSlots].sort((a, b) => agendaTimeInMinutes(a) - agendaTimeInMinutes(b));
        container.innerHTML = slots.map((slot, index) => `
            <label class="cursor-pointer flex items-center gap-2 p-2 rounded-xl border border-stone-800 bg-stone-900 text-xs text-white hover:border-purple-500/50 transition">
                <input type="checkbox" value="${slot}" class="accent-purple-500 rounded recurring-slot-checkbox" ${index === slots.length - 1 ? 'checked' : ''}>
                <span>${slot}</span>
            </label>
        `).join('');
    }

    modal.classList.remove('hidden');
}

function closeAdminRecurringBlockModal() {
    document.getElementById('modal-admin-recurring-block')?.classList.add('hidden');
}

function selectAllRecurringSlots(checkAll) {
    document.querySelectorAll('.recurring-slot-checkbox').forEach(cb => {
        cb.checked = checkAll;
    });
}

async function submitRecurringBlock(forzar = false) {
    const daySelect = document.getElementById('admin-recurring-day');
    const dateStart = document.getElementById('admin-recurring-date-start');
    const dateEnd = document.getElementById('admin-recurring-date-end');
    const reasonInput = document.getElementById('admin-recurring-reason');
    const errBox = document.getElementById('admin-recurring-error');
    const conflictBox = document.getElementById('admin-recurring-conflict-box');
    const btnForce = document.getElementById('btn-confirm-recurring-force');
    const btnSubmit = document.getElementById('btn-submit-recurring-block');

    if (errBox) errBox.classList.add('hidden');
    if (conflictBox) conflictBox.classList.add('hidden');

    const diaSemana = parseInt(daySelect?.value || '5', 10);
    const selectedSlots = Array.from(document.querySelectorAll('.recurring-slot-checkbox:checked')).map(cb => cb.value);

    if (!selectedSlots.length) {
        if (errBox) {
            errBox.querySelector('span').textContent = 'Selecciona al menos un turno recurrente para bloquear.';
            errBox.classList.remove('hidden');
        }
        return;
    }

    const payload = {
        diaSemana: diaSemana,
        horarios: selectedSlots,
        fechaInicio: dateStart?.value || null,
        fechaFin: dateEnd?.value || null,
        motivo: (reasonInput?.value || '').trim(),
        forzar: forzar
    };

    if (btnSubmit) {
        btnSubmit.disabled = true;
        btnSubmit.innerHTML = '<i class="fa-solid fa-spinner fa-spin mr-1"></i> <span>Guardando...</span>';
    }

    try {
        await apiPost('/agenda/bloqueos-recurrentes', payload);
        await renderAdminRecurringBlocksList();
        renderAdminWeeklyCalendar();
        renderTimeSlots();

        closeAdminRecurringBlockModal();
        showToast('Bloqueo recurrente guardado correctamente', 'success');
    } catch (err) {
        console.warn('Bloqueo recurrente response', err);
        if (err.data && err.data.conflicto && Array.isArray(err.data.reservasAfectadas)) {
            if (conflictBox) {
                const listEl = document.getElementById('admin-recurring-conflict-list');
                const msgEl = document.getElementById('admin-recurring-conflict-msg');
                if (msgEl) msgEl.textContent = err.data.mensaje || 'Existen reservas activas en las fechas de esta recurrencia.';
                if (listEl) {
                    listEl.innerHTML = err.data.reservasAfectadas.map(r => `
                        <div class="flex items-center justify-between p-1.5 rounded-lg bg-stone-900 border border-stone-800 text-[10px]">
                            <span class="font-mono font-bold text-isivi-gold">${r.codigo || 'ISV-0000'}</span>
                            <span class="text-white">${r.cliente || 'Cliente'}</span>
                            <span class="text-purple-300 font-bold">${r.fecha || ''} ${r.hora || ''}</span>
                        </div>
                    `).join('');
                }
                conflictBox.classList.remove('hidden');
            }
            if (btnForce) btnForce.classList.remove('hidden');
        } else {
            if (errBox) {
                errBox.querySelector('span').textContent = err.message || 'No se pudo guardar el bloqueo recurrente.';
                errBox.classList.remove('hidden');
            }
        }
    } finally {
        if (btnSubmit) {
            btnSubmit.disabled = false;
            btnSubmit.innerHTML = '<i class="fa-solid fa-arrows-rotate mr-1"></i> <span>Guardar Recurrencia</span>';
        }
    }
}

async function toggleRecurringBlock(id, activo) {
    try {
        await apiPut(`/agenda/bloqueos-recurrentes/${encodeURIComponent(id)}`, { activo: activo });
        await renderAdminRecurringBlocksList();
        renderAdminWeeklyCalendar();
        renderTimeSlots();
        showToast(activo ? 'Bloqueo reactivado' : 'Bloqueo pausado', 'info');
    } catch (err) {
        console.error(err);
        showToast('No se pudo actualizar el bloqueo recurrente', 'error');
    }
}

async function deleteRecurringBlock(id) {
    if (!confirm('¿Deseas eliminar permanentemente esta regla de bloqueo recurrente?')) return;
    try {
        await apiDelete(`/agenda/bloqueos-recurrentes/${encodeURIComponent(id)}`);
        await renderAdminRecurringBlocksList();
        renderAdminWeeklyCalendar();
        renderTimeSlots();
        showToast('Bloqueo recurrente eliminado', 'success');
    } catch (err) {
        console.error(err);
        showToast('No se pudo eliminar el bloqueo recurrente', 'error');
    }
}

// ============ AGENDA SEMANAL Y GESTIÓN POR FECHA ============
async function renderAdminWeeklyCalendar() {

    const container = document.getElementById('admin-week-calendar');
    const label = document.getElementById('admin-week-label');
    if (!container || !label) return;

    const weekDays = Array.from({ length: 7 }, (_, index) => {
        const date = new Date(adminWeekStart);
        date.setDate(date.getDate() + index);
        return date;
    });
    const monthFormatter = new Intl.DateTimeFormat('es-CO', { day: 'numeric', month: 'short' });
    label.textContent = `${monthFormatter.format(weekDays[0])} — ${monthFormatter.format(weekDays[6])}`;
    container.innerHTML = '<div class="p-6 text-center text-xs text-isivi-300">Cargando agenda semanal...</div>';

    try {
        const blocksByDay = await Promise.all(weekDays.map(async date => {
            const dateKey = formatDateKey(date);
            const blocks = await apiGet(`/reservas/bloqueos?fecha=${encodeURIComponent(dateKey)}`);
            return [dateKey, blocks.map(block => block.horaCita)];
        }));
        const blockedTimes = Object.fromEntries(blocksByDay);
        const dayNames = ['Dom', 'Lun', 'Mar', 'Mié', 'Jue', 'Vie', 'Sáb'];
        const todayKey = formatDateKey(new Date());
        const headers = weekDays.map(date => {
            const dateKey = formatDateKey(date);
            const selected = document.getElementById('admin-schedule-date')?.value === dateKey;
            const dayEx = (agendaConfiguration.excepciones || []).find(ex => ex.fecha === dateKey);
            const exBadge = dayEx ? (dayEx.tipo === 'CERRADO' ? '<span class="text-[8px] text-red-400 block font-bold">Cerrado</span>' : '<span class="text-[8px] text-emerald-400 block font-bold">Especial</span>') : '';
            return `<button type="button" onclick="selectAdminWeekDay('${dateKey}')" class="border-b p-3 text-center transition ${selected ? 'border-isivi-gold bg-isivi-500/10' : 'border-stone-800 hover:bg-stone-900'}"><span class="block text-[10px] font-bold uppercase ${dateKey === todayKey ? 'text-isivi-gold' : 'text-isivi-300'}">${dayNames[date.getDay()]}</span><span class="mt-1 inline-flex h-7 w-7 items-center justify-center rounded-full text-xs font-bold ${dateKey === todayKey ? 'bg-isivi-gold text-isivi-black' : 'text-white'}">${date.getDate()}</span>${exBadge}</button>`;
        }).join('');
        const slots = appointmentTimeSlots.map(slot => weekDays.map(date => {
            const dateKey = formatDateKey(date);
            const reservation = bookingsList.find(item => item.date === dateKey && item.time === slot && item.status !== 'Cancelada' && item.status !== 'Denegada');
            const manuallyBlocked = blockedTimes[dateKey].includes(slot);

            const dayEx = (agendaConfiguration.excepciones || []).find(ex => ex.fecha === dateKey);
            const isExplicitlyClosed = dayEx && dayEx.tipo === 'CERRADO';
            const hasSpecialHours = dayEx && dayEx.tipo === 'HORARIO_ESPECIAL';
            const isWorkingDay = hasSpecialHours || (!isExplicitlyClosed && agendaConfiguration.diasLaborales.includes(date.getDay()));
            const isPast = isPastTimeSlot(dateKey, slot);

            let stateClass = 'border-stone-800 bg-stone-900/40 text-stone-500';
            let description = 'Disponible';
            let icon = 'fa-circle';
            if (!isWorkingDay) {
                stateClass = 'border-transparent bg-stone-950 text-stone-700';
                description = isExplicitlyClosed ? 'Cerrado (Excepción)' : 'Cerrado';
                icon = 'fa-minus';
            } else if (manuallyBlocked) {
                stateClass = 'border-red-800/70 bg-red-950/50 text-red-300';
                description = 'Bloqueado';
                icon = 'fa-lock';
            } else if (reservation) {
                const confirmed = reservation.status === 'Confirmado';
                stateClass = confirmed ? 'border-emerald-700/70 bg-emerald-950/50 text-emerald-200' : 'border-amber-700/70 bg-amber-950/50 text-amber-200';
                description = confirmed ? `${reservation.customerName} · Confirmada` : `${reservation.customerName} · Pendiente`;
                icon = confirmed ? 'fa-check' : 'fa-clock';
            } else if (isPast) {
                stateClass = 'border-stone-900 bg-stone-950 text-stone-600';
                description = 'Pasado';
                icon = 'fa-clock-rotate-left';
            }
            return `<button type="button" onclick="selectAdminWeekDay('${dateKey}')" title="${slot}: ${description}" class="m-1 min-h-12 rounded-lg border px-1 py-1.5 text-center text-[10px] transition hover:brightness-125 ${stateClass}"><span class="block font-bold">${slot}</span><span class="mt-0.5 block truncate"><i class="fa-solid ${icon} mr-1 text-[8px]"></i>${description}</span></button>`;
        }).join('')).join('');
        container.innerHTML = `<div class="grid grid-cols-7">${headers}</div><div class="grid grid-cols-7 border-t border-stone-800">${slots}</div>`;
    } catch (err) {
        console.error(err);
        container.innerHTML = '<div class="p-6 text-center text-xs text-red-300">No se pudo cargar la agenda semanal.</div>';
    }
}

async function renderAdminScheduleManager() {
    const dateInput = document.getElementById('admin-schedule-date');
    const container = document.getElementById('admin-schedule-slots');
    const dateLabel = document.getElementById('admin-schedule-date-label');
    if (!dateInput || !container) return;

    if (!dateInput.value) dateInput.value = formatDateKey(new Date());
    const date = dateInput.value;
    if (dateLabel) {
        const [year, month, day] = date.split('-');
        dateLabel.textContent = `${day}/${month}/${year}`;
    }
    container.innerHTML = '<span class="col-span-full py-2 text-center text-xs text-isivi-300">Consultando disponibilidad...</span>';

    try {
        const [occupiedTimes, manualBlocks] = await Promise.all([
            apiGet(`/reservas/disponibilidad?fecha=${encodeURIComponent(date)}`),
            apiGet(`/reservas/bloqueos?fecha=${encodeURIComponent(date)}`)
        ]);
        const manuallyBlockedTimes = manualBlocks.map(block => block.horaCita);

        let slotsToRender = appointmentTimeSlots;
        const dayEx = (agendaConfiguration.excepciones || []).find(ex => ex.fecha === date);
        if (dayEx && dayEx.tipo === 'HORARIO_ESPECIAL' && Array.isArray(dayEx.horarios) && dayEx.horarios.length > 0) {
            slotsToRender = dayEx.horarios;
        }

        container.innerHTML = slotsToRender.map(slot => {
            const manuallyBlocked = manuallyBlockedTimes.includes(slot);
            const occupied = occupiedTimes.includes(slot);
            const isPast = isPastTimeSlot(date, slot);
            if (manuallyBlocked) {
                return `<button onclick="toggleAdminScheduleSlot('${date}', '${slot}', true)" class="rounded-xl border border-red-700 bg-red-950 px-3 py-3 text-xs font-bold text-red-200 transition hover:bg-red-900"><i class="fa-solid fa-lock mr-1"></i>${slot}<span class="mt-1 block text-[9px] font-medium">Liberar</span></button>`;
            }
            if (occupied) {
                return `<button onclick="releaseReservedScheduleSlot('${date}', '${slot}')" class="rounded-xl border border-amber-700/60 bg-amber-950/50 px-3 py-3 text-xs font-bold text-amber-200 transition hover:bg-amber-900"><i class="fa-solid fa-calendar-check mr-1"></i>${slot}<span class="mt-1 block text-[9px] font-medium">Liberar reserva</span></button>`;
            }
            if (isPast) {
                return `<div class="rounded-xl border border-stone-800 bg-stone-900/50 px-3 py-3 text-xs font-medium text-stone-500 text-center cursor-not-allowed"><i class="fa-solid fa-clock-rotate-left mr-1"></i>${slot}<span class="mt-1 block text-[9px] font-medium text-stone-600">Pasado</span></div>`;
            }
            return `<button onclick="toggleAdminScheduleSlot('${date}', '${slot}', false)" class="rounded-xl border border-emerald-700/60 bg-emerald-950/40 px-3 py-3 text-xs font-bold text-emerald-200 transition hover:bg-emerald-900"><i class="fa-solid fa-lock-open mr-1"></i>${slot}<span class="mt-1 block text-[9px] font-medium">Ocupar</span></button>`;
        }).join('');
    } catch (err) {
        console.error(err);
        container.innerHTML = '<span class="col-span-full py-2 text-center text-xs text-red-300">No se pudo cargar la disponibilidad.</span>';
    }
}

async function toggleAdminScheduleSlot(date, time, isManuallyBlocked) {
    try {
        if (isManuallyBlocked) {
            await apiDelete(`/reservas/bloqueos?fecha=${encodeURIComponent(date)}&hora=${encodeURIComponent(time)}`);
            showToast(`Horario ${time} liberado`, 'success');
        } else {
            await apiPost('/reservas/bloqueos', { fechaCita: date, horaCita: time });
            showToast(`Horario ${time} marcado como ocupado`, 'success');
        }
        await renderAdminScheduleManager();
        renderAdminWeeklyCalendar();
        renderTimeSlots();
    } catch (err) {
        console.error(err);
        showToast('No se pudo actualizar el horario', 'error');
    }
}

async function releaseReservedScheduleSlot(date, time) {
    const booking = bookingsList.find(item => item.date === date && item.time === time && item.status !== 'Cancelada' && item.status !== 'Denegada');
    if (!booking) {
        showToast('No encontramos la reserva asociada a ese horario', 'error');
        return;
    }
    if (!confirm(`¿Liberar el horario ${time}? La cita ${booking.code} quedará denegada.`)) return;

    try {
        const actualizado = await apiPatch(`/reservas/${booking.id}/denegar`);
        booking.status = actualizado.estado;
        renderAdminBookingsTable();
        await renderAdminScheduleManager();
        renderAdminWeeklyCalendar();
        await renderTimeSlots();
        updateAdminStats();
        showToast(`Horario ${time} liberado`, 'success');
    } catch (err) {
        console.error(err);
        showToast('No se pudo liberar el horario', 'error');
    }
}

function populateBookingFilterOptions() {
    const timeSelect = document.getElementById('booking-filter-time');
    const daySelect = document.getElementById('booking-filter-day');
    const monthSelect = document.getElementById('booking-filter-month');
    if (!timeSelect || !daySelect || !monthSelect) return;

    const selectedTime = timeSelect.value;
    const selectedDay = daySelect.value;
    const selectedMonth = monthSelect.value;
    const times = [...new Set(bookingsList.map(b => b.time).filter(Boolean))].sort();
    const days = [...new Set(bookingsList.map(b => String(b.date || '').slice(8, 10)).filter(Boolean))].sort((a, b) => Number(a) - Number(b));
    const months = [...new Set(bookingsList.map(b => String(b.date || '').slice(5, 7)).filter(Boolean))].sort();
    const monthNames = ['Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio', 'Julio', 'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre'];

    timeSelect.innerHTML = '<option value="">Todas las horas</option>' + times.map(time => `<option value="${time}">${time}</option>`).join('');
    daySelect.innerHTML = '<option value="">Todos los días</option>' + days.map(day => `<option value="${day}">${Number(day)}</option>`).join('');
    monthSelect.innerHTML = '<option value="">Todos los meses</option>' + months.map(month => `<option value="${month}">${monthNames[Number(month) - 1] || month}</option>`).join('');
    timeSelect.value = selectedTime;
    daySelect.value = selectedDay;
    monthSelect.value = selectedMonth;
}

function getFilteredBookings() {
    const text = (document.getElementById('booking-filter-text')?.value || '').trim().toLocaleLowerCase('es-CO');
    const time = document.getElementById('booking-filter-time')?.value || '';
    const day = document.getElementById('booking-filter-day')?.value || '';
    const month = document.getElementById('booking-filter-month')?.value || '';

    return bookingsList.filter(booking => {
        if (booking.isPureOrder === true) return false;
        const date = String(booking.date || '');
        const searchable = `${booking.customerName || ''} ${booking.code || ''} ${(booking.services || []).join(' ')}`.toLocaleLowerCase('es-CO');
        return (!text || searchable.includes(text)) &&
            (!time || booking.time === time) &&
            (!day || date.slice(8, 10) === day) &&
            (!month || date.slice(5, 7) === month);
    });
}

function applyBookingFilters() {
    renderAdminBookingsTable(false);
}

function clearBookingFilters() {
    const ids = ['booking-filter-text', 'booking-filter-time', 'booking-filter-day', 'booking-filter-month'];
    ids.forEach(id => { const element = document.getElementById(id); if (element) element.value = ''; });
    renderAdminBookingsTable(false);
}

function renderAdminBookingsTable(refreshFilters = true) {
    const tbody = document.getElementById('adm-bookings-table-body');
    if (refreshFilters) populateBookingFilterOptions();
    const visibleBookings = getFilteredBookings();
    if (visibleBookings.length === 0) {
        const message = bookingsList.length === 0 ? 'No hay citas activas registradas en la agenda.' : 'No hay citas que coincidan con los filtros seleccionados.';
        tbody.innerHTML = `<tr><td colspan="6" class="p-6 text-center text-isivi-300">${message}</td></tr>`;
        return;
    }

    tbody.innerHTML = visibleBookings.map(b => {
        const payInfo = getPaymentMethodInfo(b);
        return `
            <tr class="hover:bg-stone-900 transition" id="booking-row-${b.id}">
                <td class="p-3">
                    <span class="font-bold text-isivi-gold block font-mono">${b.code}</span>
                    <span class="text-white font-medium">${b.customerName}</span>
                    <span class="text-[10px] text-isivi-300 block">${b.phone} (${b.city || 'Cartagena'})</span>
                </td>
                <td class="p-3 text-isivi-200 font-medium">${b.services ? b.services.join(', ') : 'Servicio'}</td>
                <td class="p-3 whitespace-nowrap"><span class="font-semibold block text-white">${b.date}</span><span class="text-isivi-300">${b.time}</span></td>
                <td class="p-3 whitespace-nowrap">
                    <span class="font-bold text-emerald-400 block">$${(b.deposit || 0).toLocaleString('es-CO')}</span>
                    <span class="text-[10px] text-stone-400">Total: $${(b.subtotal || 0).toLocaleString('es-CO')}</span>
                </td>
                <td class="p-3">
                    <div class="space-y-1">
                        <div class="flex flex-wrap items-center gap-1.5">
                            ${payInfo.badgeHtml}
                            ${payInfo.statusBadgeHtml}
                        </div>
                        ${payInfo.actionRequired ? `<span class="text-[10px] text-amber-300 font-bold block"><i class="fa-solid fa-triangle-exclamation mr-1"></i>Requiere validación</span>` : ''}
                        ${b.wompiReference ? `<span class="text-[9px] text-stone-400 font-mono block">Ref: ${b.wompiReference}</span>` : ''}
                    </div>
                </td>
                <td class="p-3 text-right whitespace-nowrap">
                    <div class="flex justify-end items-center gap-1.5 flex-wrap">
                        <button onclick="openAdminBookingDetail('${b.id}')" title="Ver detalle completo" class="px-2 py-1 bg-stone-900 hover:bg-stone-800 text-isivi-gold border border-stone-700 rounded-lg text-[11px] font-bold"><i class="fa-solid fa-eye mr-1"></i>Detalle</button>
                        ${payInfo.actionRequired ? `<button onclick="approveBooking('${b.id}')" class="px-2.5 py-1 bg-emerald-600 hover:bg-emerald-700 text-white rounded-lg text-[11px] font-bold shadow"><i class="fa-solid fa-check mr-1"></i>${b.isPureOrder ? 'Confirmar comprobante' : 'Aprobar'}</button><button onclick="denyBooking('${b.id}')" class="px-2 py-1 bg-red-700 hover:bg-red-800 text-white rounded-lg text-[11px] font-bold">Denegar</button>` : ''}
                        ${b.status === 'Confirmado' ? `<button onclick="openAdminRescheduleModal('${b.id}')" title="Reprogramar cita" class="px-2 py-1 bg-stone-900 hover:bg-stone-800 text-stone-300 border border-stone-700 rounded-lg text-[11px] font-bold"><i class="fa-solid fa-calendar-days"></i></button>` : ''}
                        ${getDeleteBtnHtml(b)}
                    </div>
                </td>
            </tr>
        `;
    }).join('');
}

async function approveBooking(id) {
    if (isAdminActionInProgress) return;
    const b = bookingsList.find(item => item.id === id)
        || adminOrdersList.find(item => item.id === id)
        || historyBookingsList.find(item => item.id === id);
    const isOrder = b ? b.isPureOrder : false;
    isAdminActionInProgress = true;
    try {
        const actualizado = await apiPatch(`/reservas/${id}/aprobar`);
        if (b) {
            b.status = actualizado.estado;
            b.estado = actualizado.estado;
        }
        renderAdminBookingsTable();
        await renderAdminScheduleManager();
        renderAdminWeeklyCalendar();
        await renderTimeSlots();
        updateAdminStats();
        showToast(isOrder ? `Pedido ${b ? b.code : id} confirmado con éxito` : `Cita ${b ? b.code : id} aprobada con éxito`, 'success');
    } catch (err) {
        console.error(err);
        showToast(isOrder ? "No se pudo confirmar el comprobante" : "No se pudo aprobar la cita", "error");
    } finally {
        isAdminActionInProgress = false;
    }
}

async function denyBooking(id) {
    if (isAdminActionInProgress) return;
    const b = bookingsList.find(item => item.id === id);
    if (!confirm(`¿Deseas denegar la cita ${b ? b.code : ''}? Esta acción liberará el horario.`)) return;
    isAdminActionInProgress = true;
    try {
        const actualizado = await apiPatch(`/reservas/${id}/denegar`);
        if (b) {
            b.status = actualizado.estado;
            b.estado = actualizado.estado;
        }
        renderAdminBookingsTable();
        await renderAdminScheduleManager();
        renderAdminWeeklyCalendar();
        await renderTimeSlots();
        updateAdminStats();
        showToast(`Cita ${b ? b.code : id} denegada`, 'info');
    } catch (err) {
        console.error(err);
        showToast("No se pudo denegar la cita", "error");
    } finally {
        isAdminActionInProgress = false;
    }
}

function getDeleteBtnHtml(b) {
    if (!b) return '';
    const est = String(b.status || '').trim().toUpperCase();
    const estPago = String(b.paymentStatus || b.estadoPago || '').trim().toUpperCase();
    const estPed = String(b.estadoPedido || '').trim().toUpperCase();
    
    const blockedStates = ['CONFIRMADO', 'PAGO CONFIRMADO', 'PAGO_CONFIRMADO', 'EN_PREPARACION', 'LISTO_ENVIO', 'EN_CAMINO', 'ENTREGADO', 'LISTO_RECOGER', 'RECOGIDO'];
    if (blockedStates.includes(est) || blockedStates.includes(estPed) || estPago === 'APROBADO') {
        return ''; 
    }
    
    const tieneHuella = b.wompiReference || b.wompiTransactionId || b.referenciaWompi || b.transaccionWompiId || b.medioPago === 'WOMPI' || b.paymentMethod === 'WOMPI' || b.medioPago === 'TRANSFERENCIA' || b.paymentMethod === 'TRANSFERENCIA' || estPago === 'RECHAZADO' || estPago === 'ERROR';
    const actionLabel = tieneHuella ? 'Archivar' : 'Eliminar';
    const actionTitle = tieneHuella ? 'Archivar registro financiero' : 'Eliminar registro';
    const iconClass = tieneHuella ? 'fa-box-archive' : 'fa-trash';
    return `<button onclick="deleteBooking('${b.id}')" title="${actionTitle}" class="px-2 py-1 bg-red-950 hover:bg-red-900 text-red-300 rounded-lg text-[11px] font-bold"><i class="fa-solid ${iconClass} mr-1"></i>${actionLabel}</button>`;
}

function getHistoryDeleteBtnHtml(b) {
    if (!b) return '';
    const est = String(b.status || '').trim().toUpperCase();
    const estPago = String(b.paymentStatus || b.estadoPago || '').trim().toUpperCase();
    const estPed = String(b.estadoPedido || '').trim().toUpperCase();
    
    const blockedStates = ['CONFIRMADO', 'PAGO CONFIRMADO', 'PAGO_CONFIRMADO', 'EN_PREPARACION', 'LISTO_ENVIO', 'EN_CAMINO', 'ENTREGADO', 'LISTO_RECOGER', 'RECOGIDO'];
    if (blockedStates.includes(est) || blockedStates.includes(estPed) || estPago === 'APROBADO') {
        return ''; 
    }
    return `<button type="button" onclick="deleteHistoryBooking('${b.id}', '${b.code}')" class="rounded-lg bg-red-950 px-2.5 py-1 text-[10px] font-bold text-red-300 hover:bg-red-900"><i class="fa-solid fa-trash mr-1"></i>Eliminar</button>`;
}

async function deleteBooking(id) {
    if (isAdminActionInProgress) return;
    const b = bookingsList.find(item => item.id === id);
    if (!b) return;
    
    const est = String(b.status || '').trim().toUpperCase();
    const estPago = String(b.paymentStatus || b.estadoPago || '').trim().toUpperCase();
    const estPed = String(b.estadoPedido || '').trim().toUpperCase();
    
    const blockedStates = ['CONFIRMADO', 'PAGO CONFIRMADO', 'PAGO_CONFIRMADO', 'EN_PREPARACION', 'LISTO_ENVIO', 'EN_CAMINO', 'ENTREGADO', 'LISTO_RECOGER', 'RECOGIDO'];
    if (blockedStates.includes(est) || blockedStates.includes(estPed) || estPago === 'APROBADO') {
        showToast("No se permite eliminar o archivar pedidos confirmados, pagados o en proceso operativo.", "error");
        return;
    }

    const tieneHuella = b.wompiReference || b.wompiTransactionId || b.referenciaWompi || b.transaccionWompiId || b.medioPago === 'WOMPI' || b.paymentMethod === 'WOMPI' || b.medioPago === 'TRANSFERENCIA' || b.paymentMethod === 'TRANSFERENCIA' || estPago === 'RECHAZADO' || estPago === 'ERROR';
    const msg = `¿${tieneHuella ? 'Archivar' : 'Eliminar'} definitivamente la solicitud ${b.code}? ${tieneHuella ? 'Contiene información financiera que será preservada en el historial.' : 'Esta acción no se puede deshacer.'}`;
    
    if (!confirm(msg)) return;
    isAdminActionInProgress = true;
    try {
        await apiDelete(`/reservas/${id}`);
        bookingsList = bookingsList.filter(item => item.id !== id);
        renderAdminBookingsTable();
        renderAdminScheduleManager();
        renderAdminWeeklyCalendar();
        updateAdminStats();
        if (typeof loadAdminHistory === 'function') loadAdminHistory();
        showToast(tieneHuella ? `Registro ${b.code} archivado correctamente.` : `Registro ${b.code} eliminado correctamente.`, 'success');
    } catch (err) {
        console.error(err);
        showToast(err.message || err.mensaje || (tieneHuella ? "No se pudo archivar el registro." : "No se pudo eliminar el registro."), "error");
    } finally {
        isAdminActionInProgress = false;
    }
}

async function clearAllBookings() {
    try {
        await apiDelete('/reservas');
        bookingsList = [];
        renderAdminBookingsTable();
        renderAdminScheduleManager();
        renderAdminWeeklyCalendar();
        updateAdminStats();
        showToast("Historial de citas borrado");
    } catch (err) {
        console.error(err);
        showToast("No se pudo borrar el historial", "error");
    }
}

let currentHistoryDatePreset = 'all';
let currentHistoryDateRange = null;

function getBogotaDateNow() {
    try {
        const nowBogotaStr = new Date().toLocaleString('en-US', { timeZone: 'America/Bogota' });
        return new Date(nowBogotaStr);
    } catch (e) {
        return new Date();
    }
}

function setHistoryDatePreset(preset) {
    currentHistoryDatePreset = preset;
    const now = getBogotaDateNow();
    const todayStr = formatDateKey(now);

    document.querySelectorAll('.preset-pill').forEach(btn => {
        btn.className = 'preset-pill px-2.5 py-1 rounded-lg text-[11px] font-bold transition bg-stone-900 text-stone-300 hover:text-white border border-stone-800';
    });
    const activeBtn = document.getElementById(`preset-btn-${preset}`);
    if (activeBtn) {
        activeBtn.className = 'preset-pill px-2.5 py-1 rounded-lg text-[11px] font-bold transition bg-isivi-gold text-isivi-black shadow-sm';
    }

    const rangeLabelEl = document.getElementById('history-date-range-label');
    const monthInput = document.getElementById('history-filter-date');
    if (monthInput) monthInput.value = '';

    if (preset === 'all') {
        currentHistoryDateRange = null;
        if (rangeLabelEl) rangeLabelEl.classList.add('hidden');
    } else if (preset === 'today') {
        currentHistoryDateRange = { start: todayStr, end: todayStr, label: `Hoy (${todayStr})` };
    } else if (preset === 'yesterday') {
        const y = new Date(now);
        y.setDate(y.getDate() - 1);
        const yStr = formatDateKey(y);
        currentHistoryDateRange = { start: yStr, end: yStr, label: `Ayer (${yStr})` };
    } else if (preset === 'this_week') {
        const dayOfWeek = now.getDay() === 0 ? 6 : now.getDay() - 1;
        const mon = new Date(now); mon.setDate(now.getDate() - dayOfWeek);
        const sun = new Date(mon); sun.setDate(mon.getDate() + 6);
        currentHistoryDateRange = { start: formatDateKey(mon), end: formatDateKey(sun), label: `Esta semana (${formatDateKey(mon)} al ${formatDateKey(sun)})` };
    } else if (preset === 'last_week') {
        const dayOfWeek = now.getDay() === 0 ? 6 : now.getDay() - 1;
        const mon = new Date(now); mon.setDate(now.getDate() - dayOfWeek - 7);
        const sun = new Date(mon); sun.setDate(mon.getDate() + 6);
        currentHistoryDateRange = { start: formatDateKey(mon), end: formatDateKey(sun), label: `Semana pasada (${formatDateKey(mon)} al ${formatDateKey(sun)})` };
    } else if (preset === 'this_month') {
        const firstDay = new Date(now.getFullYear(), now.getMonth(), 1);
        const lastDay = new Date(now.getFullYear(), now.getMonth() + 1, 0);
        currentHistoryDateRange = { start: formatDateKey(firstDay), end: formatDateKey(lastDay), label: `Este mes (${formatDateKey(firstDay)} al ${formatDateKey(lastDay)})` };
    } else if (preset === 'last_month') {
        const firstDay = new Date(now.getFullYear(), now.getMonth() - 1, 1);
        const lastDay = new Date(now.getFullYear(), now.getMonth(), 0);
        currentHistoryDateRange = { start: formatDateKey(firstDay), end: formatDateKey(lastDay), label: `Mes anterior (${formatDateKey(firstDay)} al ${formatDateKey(lastDay)})` };
    } else if (preset === 'last_7_days') {
        const past = new Date(now); past.setDate(now.getDate() - 6);
        currentHistoryDateRange = { start: formatDateKey(past), end: todayStr, label: `Últimos 7 días (${formatDateKey(past)} al ${todayStr})` };
    } else if (preset === 'last_30_days') {
        const past = new Date(now); past.setDate(now.getDate() - 29);
        currentHistoryDateRange = { start: formatDateKey(past), end: todayStr, label: `Últimos 30 días (${formatDateKey(past)} al ${todayStr})` };
    }

    if (currentHistoryDateRange && rangeLabelEl) {
        rangeLabelEl.textContent = `📅 Filtro activo: ${currentHistoryDateRange.label}`;
        rangeLabelEl.classList.remove('hidden');
    }

    trackEvent('report_filter_used', { preset: preset });
    renderAdminHistory();
}

function onManualHistoryMonthChange() {
    currentHistoryDatePreset = 'manual';
    currentHistoryDateRange = null;
    document.querySelectorAll('.preset-pill').forEach(btn => {
        btn.className = 'preset-pill px-2.5 py-1 rounded-lg text-[11px] font-bold transition bg-stone-900 text-stone-300 hover:text-white border border-stone-800';
    });
    const rangeLabelEl = document.getElementById('history-date-range-label');
    if (rangeLabelEl) rangeLabelEl.classList.add('hidden');
    renderAdminHistory();
}

let historySearchDebounceTimer = null;

function onHistorySearchInput(query) {
    clearTimeout(historySearchDebounceTimer);
    historySearchDebounceTimer = setTimeout(() => {
        renderAdminHistory();
        renderHistoryAutocomplete(query);
    }, 250);
}

function onHistorySearchFocus() {
    const input = document.getElementById('history-filter-text');
    if (input && input.value.trim().length >= 1) {
        renderHistoryAutocomplete(input.value);
    }
}

function renderHistoryAutocomplete(rawQuery) {
    const dropdown = document.getElementById('history-search-autocomplete');
    if (!dropdown) return;

    const query = (rawQuery || '').trim().toLocaleLowerCase('es-CO');
    if (query.length < 1) {
        dropdown.classList.add('hidden');
        dropdown.innerHTML = '';
        return;
    }

    const allBookings = [...historyBookingsList, ...bookingsList];
    const uniqueBookings = [];
    const seenIds = new Set();
    for (const b of allBookings) {
        if (b && b.id && !seenIds.has(b.id)) {
            seenIds.add(b.id);
            uniqueBookings.push(b);
        }
    }

    const matches = uniqueBookings.filter(b => {
        const searchable = `${b.code || ''} ${b.customerName || ''} ${b.phone || ''} ${(b.services || []).join(' ')} ${b.status || ''}`.toLocaleLowerCase('es-CO');
        return searchable.includes(query);
    }).slice(0, 6);

    if (!matches.length) {
        dropdown.innerHTML = `
            <div class="p-2 text-center text-[11px] text-stone-500">
                <i class="fa-solid fa-magnifying-glass mr-1"></i> No se encontraron coincidencias para "${rawQuery}"
            </div>
        `;
        dropdown.classList.remove('hidden');
        return;
    }

    dropdown.innerHTML = matches.map(b => {
        const isHistory = historyBookingsList.some(hb => hb.id === b.id);
        const sourceBadge = isHistory 
            ? '<span class="px-1.5 py-0.2 rounded text-[9px] font-bold bg-stone-900 text-stone-400 border border-stone-800">Historial</span>'
            : '<span class="px-1.5 py-0.2 rounded text-[9px] font-bold bg-emerald-950 text-emerald-300 border border-emerald-500/30">Activa</span>';

        return `
            <div onclick="selectHistoryAutocompleteResult('${b.id}')" class="p-2 rounded-lg hover:bg-stone-900 cursor-pointer transition flex items-center justify-between gap-2 border border-transparent hover:border-isivi-gold/30">
                <div class="space-y-0.5 min-w-0 flex-1">
                    <div class="flex items-center gap-1.5 flex-wrap">
                        <span class="font-mono font-bold text-isivi-gold text-xs">${b.code || 'ISV-0000'}</span>
                        <span class="font-medium text-white text-xs truncate">${b.customerName || 'Cliente'}</span>
                        ${sourceBadge}
                    </div>
                    <div class="text-[11px] text-stone-400 truncate">${(b.services || []).join(', ') || 'Servicio/Producto'} · ${b.phone || ''}</div>
                </div>
                <div class="text-right shrink-0">
                    <span class="block text-[11px] font-bold text-amber-300">${b.date || ''} ${b.time || ''}</span>
                    <span class="text-[10px] text-stone-500">${b.status || ''}</span>
                </div>
            </div>
        `;
    }).join('');

    dropdown.classList.remove('hidden');
}

function selectHistoryAutocompleteResult(bookingId) {
    const dropdown = document.getElementById('history-search-autocomplete');
    if (dropdown) dropdown.classList.add('hidden');

    const b = historyBookingsList.find(item => item.id === bookingId) || bookingsList.find(item => item.id === bookingId);
    if (b) {
        const input = document.getElementById('history-filter-text');
        if (input) input.value = b.code || b.customerName || '';
        renderAdminHistory();
        openAdminBookingDetail(b.id);
    }
}

function closeHistoryAutocomplete() {
    const dropdown = document.getElementById('history-search-autocomplete');
    if (dropdown) dropdown.classList.add('hidden');
}

document.addEventListener('click', (e) => {
    const input = document.getElementById('history-filter-text');
    const dropdown = document.getElementById('history-search-autocomplete');
    if (dropdown && !dropdown.contains(e.target) && e.target !== input) {
        dropdown.classList.add('hidden');
    }
});

document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') {
        closeHistoryAutocomplete();
    }
});

let currentHistoryTypeFilter = 'TODOS';

function setHistoryTypeFilter(type) {
    currentHistoryTypeFilter = type;
    document.querySelectorAll('.hist-type-pill').forEach(btn => {
        btn.className = 'hist-type-pill px-3 py-1.5 rounded-xl text-xs font-bold transition bg-stone-950 text-stone-300 border border-stone-800 hover:border-isivi-gold';
    });
    const activeBtn = document.getElementById(`hist-type-pill-${type}`);
    if (activeBtn) {
        activeBtn.className = 'hist-type-pill px-3 py-1.5 rounded-xl text-xs font-bold transition bg-isivi-gold text-isivi-black shadow-sm';
    }
    renderAdminHistory();
}

function getFilteredHistoryBookings() {
    const text = (document.getElementById('history-filter-text')?.value || '').trim().toLocaleLowerCase('es-CO');
    const status = document.getElementById('history-filter-status')?.value || '';
    const month = document.getElementById('history-filter-date')?.value || '';
    return historyBookingsList.filter(booking => {
        const searchable = `${booking.code || ''} ${booking.customerName || ''} ${booking.phone || ''} ${(booking.services || []).join(' ')}`.toLocaleLowerCase('es-CO');
        const bookingDate = booking.registeredAt || booking.date || '';

        const matchesText = !text || searchable.includes(text);
        const matchesStatus = !status || booking.status === status || booking.estado === status;
        const matchesMonth = !month || bookingDate.startsWith(month);

        let matchesType = true;
        const isCita = Boolean(booking.date && booking.time);
        if (currentHistoryTypeFilter === 'CITAS') {
            matchesType = isCita;
        } else if (currentHistoryTypeFilter === 'PEDIDOS') {
            matchesType = !isCita;
        }

        let matchesRange = true;
        if (currentHistoryDateRange && currentHistoryDateRange.start && currentHistoryDateRange.end) {
            const datePart = bookingDate.slice(0, 10);
            matchesRange = datePart >= currentHistoryDateRange.start && datePart <= currentHistoryDateRange.end;
        }

        return matchesText && matchesStatus && matchesMonth && matchesRange && matchesType;
    });
}



function renderAdminHistory() {
    const tbody = document.getElementById('adm-history-table-body');
    const summary = document.getElementById('history-summary');
    if (!tbody || !summary) return;
    const visible = getFilteredHistoryBookings();
    const confirmed = visible.filter(booking => booking.status === 'Confirmado').length;
    const deposits = visible.reduce((sum, booking) => sum + (booking.deposit || 0), 0);
    summary.innerHTML = [
        ['Registros', visible.length], ['Confirmadas', confirmed],
        ['Anticipos', `$${deposits.toLocaleString('es-CO')}`], ['Mostrando', historyBookingsList.length ? `${visible.length} de ${historyBookingsList.length}` : '0']
    ].map(([label, value]) => `<div class="rounded-xl border border-stone-800 bg-stone-950 p-3"><p class="text-[10px] uppercase tracking-wide text-isivi-300">${label}</p><p class="mt-1 text-lg font-bold text-isivi-gold">${value}</p></div>`).join('');
    if (visible.length === 0) {
        const emptyMsg = (historyBookingsList.length === 0)
            ? 'No hay registros históricos todavía.'
            : 'No hay registros que coincidan con los filtros seleccionados.';
        tbody.innerHTML = `<tr><td colspan="7" class="p-6 text-center text-isivi-300">${emptyMsg}</td></tr>`;
        return;
    }
    tbody.innerHTML = visible.map(booking => {
        const payInfo = getPaymentMethodInfo(booking);
        let statusDetailHtml = payInfo.statusBadgeHtml;
        if (booking.status === 'Cancelada' || booking.estado === 'Cancelada') {
            const cancelOrigin = booking.canceladaPor === 'CLIENTE' ? 'por CLIENTE' : (booking.canceladaPor === 'ADMIN' ? 'por ADMIN' : (booking.canceladaPor === 'LIBERACION_PROVISIONAL' ? 'Liberación prov.' : ''));
            statusDetailHtml = `
                <span class="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-red-950 text-red-300 border border-red-500/30">
                    <i class="fa-solid fa-ban text-[9px]"></i> Cancelada ${cancelOrigin}
                </span>
                ${booking.motivoCancelacion ? `<span class="block text-[10px] text-stone-400 italic truncate max-w-[180px]" title="${booking.motivoCancelacion}">"${booking.motivoCancelacion}"</span>` : ''}
            `;
        }

        return `
            <tr class="hover:bg-stone-900 transition" id="adm-history-row-${booking.id}">
                <td class="p-3"><span class="block font-bold text-isivi-gold font-mono">${booking.code}</span><span class="font-medium text-white">${booking.customerName}</span><span class="block text-[10px] text-isivi-300">${booking.phone}</span></td>
                <td class="p-3 text-isivi-200">${(booking.services || []).join(', ') || 'Sin detalle'}</td>
                <td class="p-3 whitespace-nowrap">
                    <span class="block text-white">Registro: ${booking.registeredAt || 'No disponible'}</span>
                    ${booking.isPureOrder ? `
                        <span class="inline-flex items-center gap-1 text-[11px] ${booking.deliveryMethod === 'delivery' ? 'text-amber-300' : 'text-isivi-gold'}">
                            <i class="fa-solid ${booking.deliveryMethod === 'delivery' ? 'fa-truck' : 'fa-store'} text-[10px]"></i>
                            ${booking.deliveryMethod === 'delivery' ? 'Envío a domicilio' : 'Recogida en el local'}
                        </span>
                    ` : `
                        <span class="text-isivi-300">Cita: ${booking.date || '—'} ${booking.time || ''}</span>
                    `}
                </td>
                <td class="p-3 font-bold text-emerald-400">$${(booking.deposit || 0).toLocaleString('es-CO')}</td>
                <td class="p-3">
                    <div class="space-y-1">
                        <div class="flex flex-wrap items-center gap-1.5">
                            ${payInfo.badgeHtml}
                            ${statusDetailHtml}
                        </div>
                    </div>
                </td>
                <td class="p-3 text-isivi-300">${booking.archivedAt || '—'}</td>
                <td class="p-3 text-right">
                    <div class="flex justify-end items-center gap-1.5">
                        <button type="button" onclick="openAdminBookingDetail('${booking.id}')" class="rounded-lg bg-stone-900 border border-stone-700 px-2.5 py-1 text-[10px] font-bold text-isivi-gold hover:bg-stone-800"><i class="fa-solid fa-eye mr-1"></i>Detalle</button>
                        ${getHistoryDeleteBtnHtml(booking)}
                    </div>
                </td>
            </tr>
        `;
    }).join('');
}

// ============ MODAL DETALLE ADMINISTRATIVO DE RESERVA ============
function openAdminBookingDetail(bookingId) {
    let b = bookingsList.find(item => item.id === bookingId) 
        || historyBookingsList.find(item => item.id === bookingId)
        || adminOrdersList.find(item => item.id === bookingId);
    
    if (!b) {
        showToast('Cargando información...', 'info');
        apiGet(`/reservas`).then(list => {
            if (Array.isArray(list)) {
                const found = list.find(r => r.id === bookingId);
                if (found) {
                    const mapped = mapFromApiReserva(found);
                    if (mapped.isPureOrder) {
                        openAdminOrderDetail(mapped.id);
                        return;
                    }
                    renderAdminBookingDetailModal(mapped);
                    return;
                }
            }
            showToast('No se encontró el registro seleccionado', 'error');
        }).catch(err => {
            console.error(err);
            showToast('Error al consultar el registro', 'error');
        });
        return;
    }

    if (b.isPureOrder) {
        openAdminOrderDetail(b.id);
        return;
    }

    renderAdminBookingDetailModal(b);
}

let currentAdminDetailBooking = null;

function renderAdminBookingDetailModal(b) {
    currentAdminDetailBooking = b;
    const payInfo = getPaymentMethodInfo(b);
    
    document.getElementById('adm-detail-code').textContent = b.code || 'ISV-0000';
    
    const pill = document.getElementById('adm-detail-status-pill');
    pill.className = `px-2.5 py-0.5 rounded-full text-[10px] font-bold ${b.status === 'Confirmado' ? 'bg-emerald-950 text-emerald-300 border border-emerald-500/30' : b.status === 'Denegada' ? 'bg-red-950 text-red-300 border border-red-500/30' : 'bg-amber-950 text-amber-300 border border-amber-500/30'}`;
    pill.textContent = b.status || 'Pendiente';

    document.getElementById('adm-detail-customer').textContent = `${b.customerName || 'Cliente'} · ${b.city || 'Cartagena'}`;
    
    // Identificación canónica
    const isPureAppt = b.isPureAppointment === true;
    const isPureOrd = b.isPureOrder === true;
    const isMix = b.isMixed === true;

    if (isPureOrd) {
        document.getElementById('adm-detail-datetime').textContent = 'Solo despacho de productos';
    } else {
        document.getElementById('adm-detail-datetime').textContent = `${b.date || 'Sin fecha de cita'} · ${b.time || 'Sin turno'}`;
    }

    document.getElementById('adm-detail-phone').textContent = b.phone || 'No registrado';
    
    const emailEl = document.getElementById('adm-detail-email');
    if (emailEl) emailEl.textContent = b.email || 'No registrado';

    const emailStatusEl = document.getElementById('adm-detail-email-status');
    const resendBtn = document.getElementById('adm-detail-btn-resend-email');
    if (emailStatusEl) {
        if (b.emailConfirmacionEnviada) {
            emailStatusEl.innerHTML = '<span class="text-emerald-400 font-bold">✓ Confirmación enviada</span>';
        } else if (b.emailErrorEnvio) {
            emailStatusEl.innerHTML = '<span class="text-red-400 font-bold">✕ Error de envío</span>';
        } else if (b.email) {
            emailStatusEl.innerHTML = '<span class="text-amber-400 font-bold">⚠ Pendiente de envío</span>';
        } else {
            emailStatusEl.innerHTML = '<span class="text-stone-500">Sin correo registrado</span>';
        }
    }

    if (resendBtn) {
        if (b.email && (b.status === 'Confirmado' || b.estado === 'Confirmado')) {
            resendBtn.classList.remove('hidden');
        } else {
            resendBtn.classList.add('hidden');
        }
    }

    const itemsLabel = document.getElementById('adm-detail-items-label');
    if (itemsLabel) {
        if (isPureAppt) {
            itemsLabel.textContent = 'Servicios Solicitados';
        } else if (isPureOrd) {
            itemsLabel.textContent = 'Productos y Kits Solicitados';
        } else {
            itemsLabel.textContent = 'Servicios / Productos Solicitados';
        }
    }
    document.getElementById('adm-detail-items').textContent = (b.services || []).join(', ') || 'Servicios / Productos';

    const delRow = document.getElementById('adm-detail-delivery-row');
    const delLabel = document.getElementById('adm-detail-delivery-label');
    const delVal = document.getElementById('adm-detail-delivery');

    if (isPureOrd || isMix) {
        if (delRow) delRow.classList.remove('hidden');
        if (delLabel) delLabel.textContent = isPureOrd ? 'Entrega' : 'Entrega de productos';
        if (delVal) {
            const deliveryMethodNormalized = String(b.tipoEntrega || b.deliveryMethod || 'pickup').trim().toLowerCase();
            const isDeliv = deliveryMethodNormalized === 'delivery' || deliveryMethodNormalized === 'domicilio';
            const address = b.deliveryAddress || b.direccionEntrega || '';
            delVal.innerHTML = isDeliv 
                ? `<span class="text-amber-300 font-bold"><i class="fa-solid fa-truck mr-1"></i> Envío a domicilio:</span> ${address || 'Dirección registrada'}`
                : `<span class="text-isivi-gold font-bold"><i class="fa-solid fa-store mr-1"></i> Recoger en el local:</span> ISIVI Salón Cartagena`;
        }
    } else {
        if (delRow) delRow.classList.add('hidden');
    }

    const totalLabel = document.getElementById('adm-detail-subtotal-label');
    const depLabel = document.getElementById('adm-detail-deposit-label');
    if (totalLabel) totalLabel.textContent = isPureOrd ? 'Pago total:' : 'Subtotal / Total:';
    if (depLabel) depLabel.textContent = isPureOrd ? 'Pago total:' : 'Anticipo (25%):';

    document.getElementById('adm-detail-subtotal').textContent = `$${(b.subtotal || 0).toLocaleString('es-CO')}`;
    document.getElementById('adm-detail-deposit').textContent = `$${(b.deposit || 0).toLocaleString('es-CO')}`;
    document.getElementById('adm-detail-balance').textContent = `$${(b.balance || Math.max(0, (b.subtotal || 0) - (b.deposit || 0))).toLocaleString('es-CO')}`;

    // Inyectar sección de método de pago y trazabilidad
    let detailBoxExtra = payInfo.detailBoxHtml;
    if (b.status === 'Solicitud Cancelación' || b.estado === 'Solicitud Cancelación') {
        const solFecha = b.fechaSolicitudCancelacion ? new Date(b.fechaSolicitudCancelacion).toLocaleString('es-CO', { timeZone: 'America/Bogota' }) : 'Reciente';
        detailBoxExtra = `
            <div class="p-3.5 rounded-2xl bg-amber-950/60 border border-amber-500/50 space-y-2">
                <div class="flex items-center justify-between">
                    <span class="text-xs font-bold text-amber-300 flex items-center gap-1.5"><i class="fa-solid fa-calendar-xmark text-amber-400"></i> Solicitud de Cancelación de Cliente</span>
                    <span class="px-2 py-0.5 rounded-full text-[10px] font-bold bg-amber-900 text-amber-200 border border-amber-500/40">⏳ EN REVISIÓN</span>
                </div>
                <p class="text-[11px] text-amber-200"><strong>Fecha solicitud:</strong> ${solFecha}</p>
                <p class="text-[11px] text-amber-200"><strong>Motivo del cliente:</strong> "${b.motivoCancelacion || 'Sin motivo indicado'}"</p>
                <p class="text-[10px] text-amber-300/80">El horario e inventario permanecen retenidos hasta que decidas aprobar o rechazar la solicitud.</p>
            </div>
        `;
    } else if (b.status === 'Cancelada' || b.estado === 'Cancelada') {
        const canFecha = b.fechaCancelacion ? new Date(b.fechaCancelacion).toLocaleString('es-CO', { timeZone: 'America/Bogota' }) : 'Previamente';
        detailBoxExtra = `
            <div class="p-3.5 rounded-2xl bg-stone-900 border border-stone-800 space-y-2">
                <div class="flex items-center justify-between">
                    <span class="text-xs font-bold text-stone-300 flex items-center gap-1.5"><i class="fa-solid fa-ban text-stone-400"></i> Cancelación Registrada</span>
                    <span class="px-2 py-0.5 rounded-full text-[10px] font-bold bg-stone-800 text-stone-300 border border-stone-700">CANCELADA</span>
                </div>
                <p class="text-[11px] text-stone-400"><strong>Fecha cancelación:</strong> ${canFecha}</p>
                <p class="text-[11px] text-stone-400"><strong>Cancelada por:</strong> ${b.canceladaPor || 'CLIENTE'}</p>
                <p class="text-[11px] text-stone-400"><strong>Motivo:</strong> "${b.motivoCancelacion || 'Sin motivo indicado'}"</p>
            </div>
        `;
    } else if (b.motivoRechazoCancelacion) {
        detailBoxExtra += `
            <div class="p-2.5 rounded-xl bg-stone-900 border border-stone-800 text-[11px] text-amber-300/90">
                <i class="fa-solid fa-info-circle mr-1"></i> Solicitud de cancelación previa fue rechazada: "${b.motivoRechazoCancelacion}".
            </div>
        `;
    }
    document.getElementById('adm-detail-payment-container').innerHTML = detailBoxExtra;

    // Botones de acción contextuales
    const actionsContainer = document.getElementById('adm-detail-actions');

    let actionsHtml = `
        <button type="button" onclick="openAdminWhatsAppValidation('${b.id}')" title="Contactar al cliente por WhatsApp" class="px-3.5 py-2 bg-emerald-950 text-emerald-400 hover:bg-emerald-900 border border-emerald-800/60 rounded-xl text-xs font-bold flex items-center gap-1.5">
            <i class="fa-brands fa-whatsapp text-sm"></i> WhatsApp
        </button>
    `;

    if (b.status === 'Solicitud Cancelación' || b.estado === 'Solicitud Cancelación') {
        actionsHtml += `
            <button type="button" onclick="closeAdminBookingDetailModal(); quickApproveCancellation('${b.id}');" class="px-4 py-2 bg-emerald-600 hover:bg-emerald-500 text-white rounded-xl text-xs font-bold shadow flex items-center gap-1.5">
                <i class="fa-solid fa-check"></i> Aprobar Cancelación
            </button>
            <button type="button" onclick="closeAdminBookingDetailModal(); quickRejectCancellation('${b.id}');" class="px-3.5 py-2 bg-red-950 hover:bg-red-900 text-red-300 rounded-xl text-xs font-bold flex items-center gap-1.5">
                <i class="fa-solid fa-xmark"></i> Rechazar Solicitud
            </button>
        `;
    } else if (b.status === 'Cancelada' || b.estado === 'Cancelada') {
        if (!b.notificacionCancelacionVista) {
            actionsHtml += `
                <button type="button" onclick="markCancellationViewed('${b.id}')" class="px-3.5 py-2 bg-stone-900 hover:bg-stone-800 text-isivi-gold border border-stone-700 rounded-xl text-xs font-bold flex items-center gap-1.5">
                    <i class="fa-solid fa-check-double"></i> Marcar como revisada
                </button>
            `;
        }
    } else if (payInfo.actionRequired) {
        actionsHtml += `
            <button type="button" onclick="approveBookingFromDetail('${b.id}')" class="px-4 py-2 bg-emerald-600 hover:bg-emerald-500 text-white rounded-xl text-xs font-bold shadow flex items-center gap-1.5">
                <i class="fa-solid fa-check"></i> ${isPureOrd ? 'Confirmar comprobante' : 'Aprobar Reserva'}
            </button>
            <button type="button" onclick="denyBookingFromDetail('${b.id}')" class="px-3.5 py-2 bg-red-950 hover:bg-red-900 text-red-300 rounded-xl text-xs font-bold flex items-center gap-1.5">
                <i class="fa-solid fa-xmark"></i> Denegar
            </button>
        `;
    } else if (b.status === 'Confirmado') {
        actionsHtml += `
            <button type="button" onclick="closeAdminBookingDetailModal(); openAdminRescheduleModal('${b.id}');" class="px-3.5 py-2 bg-stone-900 hover:bg-stone-800 text-isivi-gold border border-stone-700 rounded-xl text-xs font-bold flex items-center gap-1.5">
                <i class="fa-solid fa-calendar-days"></i> Reprogramar Cita
            </button>
        `;
    }

    actionsHtml += `
        <button type="button" onclick="closeAdminBookingDetailModal()" class="px-3.5 py-2 bg-stone-800 hover:bg-stone-700 text-stone-300 rounded-xl text-xs font-bold">
            Cerrar
        </button>
    `;

    actionsContainer.innerHTML = actionsHtml;
    document.getElementById('admin-booking-detail-modal').classList.remove('hidden');
}

function closeAdminBookingDetailModal() {
    const modal = document.getElementById('admin-booking-detail-modal');
    if (modal) modal.classList.add('hidden');
}

async function resendEmailFromDetail() {
    if (!currentAdminDetailBooking || !currentAdminDetailBooking.id) return;
    await adminResendConfirmationEmail(currentAdminDetailBooking.id);
}

async function adminResendConfirmationEmail(reservaId) {
    if (isAdminActionInProgress) return;
    isAdminActionInProgress = true;
    try {
        const resp = await apiPost(`/reservas/${reservaId}/reenviar-email`, {});
        showToast(resp.mensaje || 'Comprobante reenviado con éxito por correo.', resp.success ? 'success' : 'info');
        await loadAllData();
        const updated = bookingsList.find(b => b.id === reservaId) || historyBookingsList.find(b => b.id === reservaId);
        if (updated) renderAdminBookingDetailModal(updated);
    } catch (err) {
        showToast(err.message || 'No se pudo reenviar el correo de confirmación', 'error');
    } finally {
        isAdminActionInProgress = false;
    }
}

async function approveBookingFromDetail(id) {
    closeAdminBookingDetailModal();
    await quickApproveBooking(id);
}

async function denyBookingFromDetail(id) {
    closeAdminBookingDetailModal();
    await quickDenyBooking(id);
}

async function deleteHistoryBooking(id, code) {
    const b = historyBookingsList.find(item => item.id === id);
    if (b) {
        const est = String(b.status || '').trim().toUpperCase();
        const estPago = String(b.paymentStatus || b.estadoPago || '').trim().toUpperCase();
        const estPed = String(b.estadoPedido || '').trim().toUpperCase();
        
        const blockedStates = ['CONFIRMADO', 'PAGO CONFIRMADO', 'PAGO_CONFIRMADO', 'EN_PREPARACION', 'LISTO_ENVIO', 'EN_CAMINO', 'ENTREGADO', 'LISTO_RECOGER', 'RECOGIDO'];
        if (blockedStates.includes(est) || blockedStates.includes(estPed) || estPago === 'APROBADO') {
            showToast("No se permite eliminar registros confirmados, pagados o en proceso operativo del historial.", "error");
            return;
        }
    }
    if (!confirm(`¿Eliminar definitivamente del historial la solicitud ${code}? Esta acción no se puede deshacer.`)) return;
    try {
        await apiDelete(`/reservas/${id}`);
        historyBookingsList = historyBookingsList.filter(booking => booking.id !== id);
        renderAdminHistory();
        showToast('Registro eliminado del historial');
    } catch (err) {
        console.error(err);
        showToast(err.mensaje || 'No se pudo eliminar el registro', 'error');
    }
}

function exportHistoryCsv() {
    const rows = getFilteredHistoryBookings();
    if (!rows.length) { showToast('No hay registros para exportar', 'error'); return; }
    const quote = value => `"${String(value ?? '').replace(/"/g, '""')}"`;
    const csv = [
        ['Código', 'Cliente', 'Teléfono', 'Servicios / productos', 'Registro', 'Fecha de cita', 'Hora', 'Anticipo', 'Estado', 'Archivado'],
        ...rows.map(booking => [booking.code, booking.customerName, booking.phone, (booking.services || []).join(' | '), booking.registeredAt, booking.date, booking.time, booking.deposit || 0, booking.status, booking.archivedAt])
    ].map(row => row.map(quote).join(',')).join('\n');
    const url = URL.createObjectURL(new Blob([`\uFEFF${csv}`], { type: 'text/csv;charset=utf-8;' }));
    const link = document.createElement('a');
    link.href = url; link.download = `historial-isivi-${formatDateKey(new Date())}.csv`; link.click();
    URL.revokeObjectURL(url);
}

// ---------- Toasts ----------
function showToast(message, type = 'info') {
    const container = document.getElementById('toast-container');
    const toast = document.createElement('div');

    const bgColor = type === 'error' ? 'bg-red-950 text-white border-red-800' : (type === 'success' ? 'bg-emerald-950 text-white border-emerald-800' : 'bg-stone-900 text-white border-isivi-500/40');
    const icon = type === 'error' ? 'fa-circle-exclamation text-red-400' : (type === 'success' ? 'fa-circle-check text-emerald-400' : 'fa-bell text-isivi-gold');

    toast.className = `${bgColor} p-3.5 rounded-2xl shadow-2xl text-xs font-medium flex items-center justify-between gap-3 pointer-events-auto transform transition duration-300 translate-y-2 opacity-0 border`;
    toast.innerHTML = `
        <div class="flex items-center gap-2.5"><i class="fa-solid ${icon}"></i><span>${message}</span></div>
        <button onclick="this.parentElement.remove()" class="text-stone-400 hover:text-white"><i class="fa-solid fa-xmark text-xs"></i></button>
    `;

    container.appendChild(toast);

    setTimeout(() => { toast.classList.remove('translate-y-2', 'opacity-0'); }, 10);
    setTimeout(() => {
        toast.classList.add('opacity-0', 'translate-y-2');
        setTimeout(() => toast.remove(), 300);
    }, 4000);
}

// ============ MEJORA DE EXPERIENCIA DE USUARIO: STEPPER, BARRA MÓVIL Y MICROINTERACCIONES ============

function updateBookingProgressTracker() {
    const stepper = document.getElementById('booking-progress-stepper');
    if (!stepper) return;

    const hasServices = selectedServices.length > 0;

    // Si no hay servicios en el pedido (ej. solo productos o vacío), ocultar el stepper de agendamiento
    if (!hasServices) {
        stepper.classList.add('hidden');
        return;
    } else {
        stepper.classList.remove('hidden');
    }

    const hasDate = Boolean(selectedDateStr);
    const hasTime = Boolean(selectedTimeSlot);
    const custName = (document.getElementById('cart-cust-name')?.value || document.getElementById('cust-name')?.value || '').trim();
    const custPhone = (document.getElementById('cart-cust-phone')?.value || document.getElementById('cust-phone')?.value || '').trim();
    const hasData = custName.length >= 3 && custPhone.length >= 7;

    const stepNodes = {
        service: document.getElementById('step-node-service'),
        date: document.getElementById('step-node-date'),
        time: document.getElementById('step-node-time'),
        data: document.getElementById('step-node-data'),
        confirm: document.getElementById('step-node-confirm')
    };

    if (!stepNodes.service) return;

    const isServiceDone = hasServices;
    const isDateDone = isServiceDone && hasDate;
    const isTimeDone = isDateDone && hasTime;
    const isDataDone = isTimeDone && hasData;
    const isConfirmActive = isDataDone;

    const setNodeState = (node, isDone, isActive) => {
        if (!node) return;
        node.classList.toggle('is-completed', isDone);
        node.classList.toggle('is-active', isActive && !isDone);
    };

    setNodeState(stepNodes.service, isServiceDone, !isServiceDone);
    setNodeState(stepNodes.date, isDateDone, isServiceDone && !isDateDone);
    setNodeState(stepNodes.time, isTimeDone, isDateDone && !isTimeDone);
    setNodeState(stepNodes.data, isDataDone, isTimeDone && !isDataDone);
    setNodeState(stepNodes.confirm, false, isConfirmActive);

    const fillBar = document.getElementById('booking-progress-fill');
    if (fillBar) {
        let percent = 0;
        if (isDataDone) percent = 100;
        else if (isTimeDone) percent = 75;
        else if (isDateDone) percent = 50;
        else if (isServiceDone) percent = 25;
        fillBar.style.width = `${percent}%`;
    }
}

function updateMobileContextBar() {
    const bar = document.getElementById('mobile-context-bar');
    if (!bar) return;

    // 1. Ocultar si está en Desktop
    if (window.innerWidth >= 768) {
        bar.classList.add('is-hidden');
        document.body.classList.remove('has-mobile-context-bar');
        return;
    }

    // 2. Ocultar si el panel de administración está activo
    const adminPage = document.getElementById('page-admin');
    if (adminPage && !adminPage.classList.contains('hidden')) {
        bar.classList.add('is-hidden');
        document.body.classList.remove('has-mobile-context-bar');
        return;
    }

    // 3. Ocultar si el drawer del carrito está abierto o modales críticos están abiertos
    const cartDrawer = document.getElementById('cart-drawer');
    if (cartDrawer && !cartDrawer.classList.contains('hidden')) {
        bar.classList.add('is-hidden');
        document.body.classList.remove('has-mobile-context-bar');
        return;
    }

    const paymentModal = document.getElementById('payment-modal');
    const wompiModal = document.getElementById('wompi-info-modal');
    const lookupModal = document.getElementById('modal-consultar-reserva');
    const successModal = document.getElementById('modal-success-confirmation');
    if ((paymentModal && !paymentModal.classList.contains('hidden')) ||
        (wompiModal && !wompiModal.classList.contains('hidden')) ||
        (lookupModal && !lookupModal.classList.contains('hidden')) ||
        (successModal && !successModal.classList.contains('hidden'))) {
        bar.classList.add('is-hidden');
        document.body.classList.remove('has-mobile-context-bar');
        return;
    }

    // 4. Ocultar si el scroll está muy cerca del final (footer)
    const scrollPosition = window.innerHeight + window.scrollY;
    const documentHeight = document.documentElement.scrollHeight || document.body.scrollHeight;
    if (scrollPosition >= documentHeight - 200) {
        bar.classList.add('is-hidden');
        document.body.classList.remove('has-mobile-context-bar');
        return;
    }

    // 5. Determinar estado y contenido contextual
    const items = getCartItems();
    const labelEl = document.getElementById('mobile-context-label');
    const subtextEl = document.getElementById('mobile-context-subtext');
    const iconEl = document.getElementById('mobile-context-icon');
    const btnTextEl = document.getElementById('mobile-context-btn-text');

    const hasServices = items.some(item => item.type === 'service');
    const totalCartItems = getCartTotalUnits();

    const productosSection = document.getElementById('productos');
    const kitsSection = document.getElementById('kits');

    const inProducts = isElementInViewport(productosSection) || isElementInViewport(kitsSection);

    if (totalCartItems > 0 && inProducts) {
        if (labelEl) labelEl.innerHTML = `<i class="fa-solid fa-bag-shopping text-isivi-gold text-[9px]"></i> <span>Mi Carrito (${totalCartItems})</span>`;
        if (subtextEl) {
            const subtotal = getCartTotalAmount();
            subtextEl.textContent = `Total: $${subtotal.toLocaleString('es-CO')}`;
        }
        if (iconEl) iconEl.className = 'fa-solid fa-bag-shopping text-xs';
        if (btnTextEl) btnTextEl.textContent = 'Ver Carrito';
        bar.dataset.action = 'cart';
        bar.classList.remove('is-hidden');
        document.body.classList.add('has-mobile-context-bar');
    } else if (hasServices) {
        const firstService = items.find(s => s.type === 'service');
        const serviceName = firstService ? firstService.name : 'Servicio';

        if (selectedDateStr && selectedTimeSlot) {
            const [y, m, d] = selectedDateStr.split('-');
            if (labelEl) labelEl.innerHTML = `<i class="fa-solid fa-calendar-check text-emerald-400 text-[9px]"></i> <span>${d}/${m} · ${selectedTimeSlot}</span>`;
            if (subtextEl) {
                const serviceTotal = items.filter(i => i.type === 'service').reduce((sum, i) => sum + i.price, 0);
                const deposit = Math.round(serviceTotal * 0.25);
                subtextEl.textContent = `${serviceName} (Anticipo $${deposit.toLocaleString('es-CO')})`;
            }
            if (iconEl) iconEl.className = 'fa-solid fa-arrow-right text-xs';
            if (btnTextEl) btnTextEl.textContent = 'Completar Reserva';
            bar.dataset.action = 'cart';
        } else {
            if (labelEl) labelEl.innerHTML = `<i class="fa-solid fa-scissors text-isivi-gold text-[9px]"></i> <span>${serviceName}</span>`;
            if (subtextEl) subtextEl.textContent = 'Selecciona tu fecha y turno';
            if (iconEl) iconEl.className = 'fa-solid fa-calendar-days text-xs';
            if (btnTextEl) btnTextEl.textContent = 'Elegir Horario';
            bar.dataset.action = 'cart';
        }
        bar.classList.remove('is-hidden');
        document.body.classList.add('has-mobile-context-bar');
    } else {
        if (window.scrollY > 250) {
            if (labelEl) labelEl.innerHTML = `<i class="fa-solid fa-sparkles text-isivi-gold text-[9px]"></i> <span>ISIVI Peluquería</span>`;
            if (subtextEl) subtextEl.textContent = 'Cuidado Capilar Natural en Cartagena';
            if (iconEl) iconEl.className = 'fa-solid fa-calendar-plus text-xs';
            if (btnTextEl) btnTextEl.textContent = 'Ver Servicios';
            bar.dataset.action = 'services';
            bar.classList.remove('is-hidden');
            document.body.classList.add('has-mobile-context-bar');
        } else {
            bar.classList.add('is-hidden');
            document.body.classList.remove('has-mobile-context-bar');
        }
    }
}

function isElementInViewport(el) {
    if (!el || el.classList.contains('hidden')) return false;
    const rect = el.getBoundingClientRect();
    return rect.top < window.innerHeight && rect.bottom > 0;
}

function handleMobileContextAction() {
    const bar = document.getElementById('mobile-context-bar');
    const action = bar ? bar.dataset.action : 'services';

    if (action === 'cart') {
        openCartDrawer();
    } else {
        const servSection = document.getElementById('servicios');
        if (servSection) {
            servSection.scrollIntoView({ behavior: 'smooth' });
        }
    }
}

// Cerrar modal de cancelación con la tecla Escape
document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') {
        const cancelModal = document.getElementById('modal-client-cancel');
        if (cancelModal && !cancelModal.classList.contains('hidden')) {
            closeClientCancelModal();
        }
        const orderModal = document.getElementById('admin-order-detail-modal');
        if (orderModal && !orderModal.classList.contains('hidden')) {
            closeAdminOrderDetailModal();
        }
    }
});

// ============================================================================
// GESTIÓN COMPLETA DE PEDIDOS DE PRODUCTOS Y KITS (ADMIN)
// ============================================================================
let adminOrdersList = [];
let currentOrderFilterStatus = 'TODOS';

async function loadAdminOrders() {
    const refreshIcon = document.getElementById('adm-orders-refresh-icon');
    if (refreshIcon) refreshIcon.classList.add('fa-spin');

    try {
        // Obtenemos todos los pedidos (activos e históricos para los filtros)
        const [activeOrders, historyOrders] = await Promise.all([
            apiGet('/reservas?historial=false&tipo=pedidos'),
            apiGet('/reservas?historial=true&tipo=pedidos')
        ]);

        const allMapped = [
            ...(Array.isArray(activeOrders) ? activeOrders : []).map(mapFromApiReserva),
            ...(Array.isArray(historyOrders) ? historyOrders : []).map(mapFromApiReserva)
        ];

        // Eliminar posibles duplicados por id
        const seen = new Set();
        adminOrdersList = [];
        for (const item of allMapped) {
            if (item && item.id && !seen.has(item.id)) {
                seen.add(item.id);
                adminOrdersList.push(item);
            }
        }

        // Ordenar por fecha de registro descendente
        adminOrdersList.sort((a, b) => {
            const dateA = a.registeredAt || a.date || '';
            const dateB = b.registeredAt || b.date || '';
            return dateB.localeCompare(dateA);
        });

        // Actualizar mini estadísticas
        updateAdminOrdersStats();

        // Renderizar tabla
        renderAdminOrdersTable();
    } catch (err) {
        console.error('Error al cargar pedidos:', err);
        showToast('No se pudieron cargar los pedidos de productos', 'error');
    } finally {
        if (refreshIcon) refreshIcon.classList.remove('fa-spin');
    }
}

function updateAdminOrdersStats() {
    const statActiveEl = document.getElementById('adm-orders-stat-active');
    const statPrepEl = document.getElementById('adm-orders-stat-prep');
    const statReadyEl = document.getElementById('adm-orders-stat-ready');
    const statDeliveredEl = document.getElementById('adm-orders-stat-delivered');

    let active = 0;
    let prep = 0;
    let ready = 0;
    let delivered = 0;
    let delivDomicilio = 0;
    let delivPickup = 0;

    for (const o of adminOrdersList) {
        const payInfo = getPaymentMethodInfo(o);
        if (payInfo.isWompi && !payInfo.isApproved) {
            continue;
        }

        const st = (o.estadoPedido || o.status || o.estado || '').toUpperCase();
        const deliveryMethodNormalized = String(o.tipoEntrega || o.deliveryMethod || 'pickup').trim().toLowerCase();
        const isDelivery = deliveryMethodNormalized === 'delivery' || deliveryMethodNormalized === 'domicilio';

        const isFinal = ['ENTREGADO', 'RECOGIDO', 'CANCELADO', 'CANCELADA', 'DENEGADO', 'DENEGADA', 'EXPIRADA'].includes(st);
        const isPendingTransfer = !payInfo.isWompi && (st === 'PENDIENTE_COMPROBANTE' || st === 'PENDIENTE COMPROBANTE' || st === 'PENDIENTE');

        if (!isFinal && (isPendingTransfer || payInfo.isApproved || st === 'EN_PREPARACION' || st === 'EN PREPARACIÓN' || st === 'EN PREPARACION' || st === 'LISTO_ENVIO' || st === 'LISTO PARA ENVÍO' || st === 'LISTO PARA ENVIO' || st === 'EN_CAMINO' || st === 'EN CAMINO' || st === 'LISTO_RECOGER' || st === 'LISTO PARA RECOGER')) {
            active++;
        }

        if (st === 'EN_PREPARACION' || st === 'EN PREPARACIÓN' || st === 'EN PREPARACION') {
            prep++;
        } else if (!isDelivery && (st === 'LISTO_RECOGER' || st === 'LISTO PARA RECOGER')) {
            ready++;
        } else if (st === 'ENTREGADO' || st === 'RECOGIDO') {
            delivered++;
            if (isDelivery) {
                delivDomicilio++;
            } else {
                delivPickup++;
            }
        }
    }

    if (statActiveEl) statActiveEl.textContent = active;
    if (statPrepEl) statPrepEl.textContent = prep;
    if (statReadyEl) statReadyEl.textContent = ready;
    if (statDeliveredEl) {
        statDeliveredEl.innerHTML = `${delivered} <span class="text-[11px] text-stone-500 font-normal ml-1">(${delivDomicilio} 🚚 · ${delivPickup} 🛍️)</span>`;
    }
}

function filterOrdersByStatus(status) {
    currentOrderFilterStatus = status;
    document.querySelectorAll('.order-filter-pill').forEach(btn => {
        btn.className = 'order-filter-pill px-3 py-1.5 rounded-xl text-xs font-bold transition bg-stone-950 text-stone-300 border border-stone-800 hover:border-isivi-gold';
    });
    const activeBtn = document.getElementById(`order-pill-${status}`);
    if (activeBtn) {
        activeBtn.className = 'order-filter-pill px-3 py-1.5 rounded-xl text-xs font-bold transition bg-isivi-gold text-isivi-black shadow-sm';
    }
    renderAdminOrdersTable();
}

function applyOrdersFilters() {
    renderAdminOrdersTable();
}

function getFilteredAdminOrders() {
    const query = (document.getElementById('adm-orders-search')?.value || '').trim().toLocaleLowerCase('es-CO');
    const dateFilter = document.getElementById('adm-orders-filter-date')?.value || '';

    return adminOrdersList.filter(order => {
        const payInfo = getPaymentMethodInfo(order);
        if (payInfo.isWompi && !payInfo.isApproved) {
            return false;
        }

        const st = (order.estadoPedido || order.status || order.estado || '').toUpperCase();
        const deliveryMethodNormalized = String(order.tipoEntrega || order.deliveryMethod || 'pickup').trim().toLowerCase();
        const isDelivery = deliveryMethodNormalized === 'delivery' || deliveryMethodNormalized === 'domicilio';

        const code = (order.code || '').toLocaleLowerCase('es-CO');
        const customer = (order.customerName || '').toLocaleLowerCase('es-CO');
        const phone = (order.phone || '').toLocaleLowerCase('es-CO');
        const itemsStr = Array.isArray(order.services) ? order.services.join(' ').toLocaleLowerCase('es-CO') : '';
        const dateStr = order.registeredAt || order.date || '';

        // Filtro por Estado
        let matchesStatus = true;
        if (currentOrderFilterStatus !== 'TODOS') {
            const filterUpper = currentOrderFilterStatus.toUpperCase();
            if (filterUpper === 'PAGO CONFIRMADO') {
                matchesStatus = (st === 'PAGO CONFIRMADO' || st === 'CONFIRMADO' || st === 'PENDIENTE_PREPARACION');
            } else if (filterUpper === 'EN PREPARACIÓN') {
                matchesStatus = (st === 'EN PREPARACIÓN' || st === 'EN PREPARACION' || st === 'EN_PREPARACION');
            } else if (filterUpper === 'LISTO PARA RECOGER') {
                matchesStatus = (st === 'LISTO_RECOGER' || st === 'LISTO PARA RECOGER' || st === 'LISTO_ENVIO' || st === 'LISTO PARA ENVÍO' || st === 'LISTO PARA ENVIO' || st === 'EN_CAMINO' || st === 'EN CAMINO');
            } else if (filterUpper === 'ENTREGADO') {
                matchesStatus = (st === 'ENTREGADO' || st === 'RECOGIDO');
            } else if (filterUpper === 'CANCELADO') {
                matchesStatus = (st === 'CANCELADO' || st === 'CANCELADA');
            }
        }

        // Filtro por Búsqueda
        let matchesSearch = true;
        if (query) {
            matchesSearch = code.includes(query) || customer.includes(query) || phone.includes(query) || itemsStr.includes(query);
        }

        // Filtro por Fecha
        let matchesDate = true;
        if (dateFilter) {
            matchesDate = dateStr.startsWith(dateFilter);
        }

        return matchesStatus && matchesSearch && matchesDate;
    });
}

function renderAdminOrdersTable() {
    const tbody = document.getElementById('adm-orders-table-body');
    if (!tbody) return;

    const visibleOrders = getFilteredAdminOrders();

    if (visibleOrders.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="6" class="p-8 text-center text-isivi-300">
                    <i class="fa-solid fa-bag-shopping text-2xl text-stone-600 block mb-2"></i>
                    No se encontraron pedidos con los filtros seleccionados.
                </td>
            </tr>
        `;
        return;
    }

    tbody.innerHTML = visibleOrders.map(order => {
        const payInfo = getPaymentMethodInfo(order);
        const st = (order.estadoPedido || order.status || order.estado || '').toUpperCase();
        const deliveryMethodNormalized = String(order.tipoEntrega || order.deliveryMethod || 'pickup').trim().toLowerCase();
        const isDelivery = deliveryMethodNormalized === 'delivery' || deliveryMethodNormalized === 'domicilio';
        
        let statusBadge = '';
        if (st === 'EN_PREPARACION' || st === 'EN PREPARACIÓN' || st === 'EN PREPARACION') {
            statusBadge = '<span class="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-amber-950 text-amber-300 border border-amber-500/30"><i class="fa-solid fa-box-open"></i> En preparación</span>';
        } else if (st === 'LISTO_ENVIO' || st === 'LISTO PARA ENVÍO' || st === 'LISTO PARA ENVIO') {
            statusBadge = '<span class="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-emerald-950 text-emerald-300 border border-emerald-500/30"><i class="fa-solid fa-boxes-packing"></i> Listo para envío</span>';
        } else if (st === 'EN_CAMINO' || st === 'EN CAMINO') {
            statusBadge = '<span class="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-sky-950 text-sky-300 border border-sky-500/30"><i class="fa-solid fa-truck-fast"></i> En camino</span>';
        } else if (st === 'LISTO_RECOGER' || st === 'LISTO PARA RECOGER') {
            statusBadge = '<span class="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-emerald-950 text-emerald-300 border border-emerald-500/30"><i class="fa-solid fa-shop"></i> Listo para recoger</span>';
        } else if (st === 'RECOGIDO') {
            statusBadge = '<span class="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-stone-900 text-stone-300 border border-stone-700"><i class="fa-solid fa-circle-check text-emerald-400"></i> Recogido</span>';
        } else if (st === 'ENTREGADO') {
            statusBadge = '<span class="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-stone-900 text-stone-300 border border-stone-700"><i class="fa-solid fa-circle-check text-emerald-400"></i> Entregado</span>';
        } else if (st === 'CANCELADO' || st === 'CANCELADA') {
            statusBadge = '<span class="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-red-950 text-red-300 border border-red-500/30"><i class="fa-solid fa-ban"></i> Cancelado</span>';
        } else if (st === 'PENDIENTE_PAGO' || st === 'PENDIENTE PAGO') {
            statusBadge = '<span class="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-sky-950 text-sky-300 border border-sky-500/30"><i class="fa-solid fa-hourglass-half"></i> Pago Pendiente</span>';
        } else if (st === 'PENDIENTE_COMPROBANTE' || st === 'PENDIENTE COMPROBANTE' || st === 'PENDIENTE') {
            statusBadge = '<span class="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-amber-950 text-amber-300 border border-amber-500/30"><i class="fa-solid fa-clock"></i> Pendiente Comprobante</span>';
        } else if (st === 'EXPIRADA' || st === 'EXPIRADO') {
            statusBadge = '<span class="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-stone-900 text-stone-400 border border-stone-700"><i class="fa-solid fa-clock-rotate-left"></i> Expirado</span>';
        } else if (st === 'PENDIENTE_PREPARACION' || st === 'PAGO CONFIRMADO' || st === 'PAGO_CONFIRMADO' || st === 'CONFIRMADO') {
            statusBadge = '<span class="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-emerald-950 text-emerald-300 border border-emerald-500/30"><i class="fa-solid fa-receipt"></i> Pago Confirmado</span>';
        } else {
            // Fallback según estado de aprobación real de pago
            if (payInfo.isApproved || order.paymentStatus === 'APROBADO') {
                statusBadge = '<span class="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-emerald-950 text-emerald-300 border border-emerald-500/30"><i class="fa-solid fa-receipt"></i> Pago Confirmado</span>';
            } else if (payInfo.isWompi) {
                statusBadge = '<span class="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-sky-950 text-sky-300 border border-sky-500/30"><i class="fa-solid fa-hourglass-half"></i> Pago Pendiente</span>';
            } else {
                statusBadge = '<span class="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-amber-950 text-amber-300 border border-amber-500/30"><i class="fa-solid fa-clock"></i> Pendiente Comprobante</span>';
            }
        }

        let deliveryBadge = '';
        if (isDelivery) {
            deliveryBadge = '<span class="inline-flex items-center gap-1 px-2 py-0.5 rounded text-[9px] font-bold bg-amber-500/20 text-amber-300 border border-amber-500/30 uppercase tracking-wider block w-max mt-1"><i class="fa-solid fa-truck"></i> Domicilio</span>';
        } else {
            deliveryBadge = '<span class="inline-flex items-center gap-1 px-2 py-0.5 rounded text-[9px] font-bold bg-isivi-gold/20 text-isivi-gold border border-isivi-gold/30 uppercase tracking-wider block w-max mt-1"><i class="fa-solid fa-store"></i> Pickup</span>';
        }

        const itemsDisplay = Array.isArray(order.services) && order.services.length > 0 
            ? order.services.map(i => `<span class="inline-block bg-stone-900 border border-stone-800 rounded-md px-1.5 py-0.5 text-[11px] text-isivi-200 mr-1 mb-1">${i}</span>`).join('')
            : '<span class="text-stone-500 italic text-xs">Sin items</span>';

        const totalAmount = Number(order.subtotal) || Number(order.deposit) || 0;

        return `
            <tr class="hover:bg-stone-900/60 transition" id="adm-order-row-${order.id}">
                <td class="p-3">
                    <span class="font-mono font-bold text-isivi-gold block text-xs">${order.code || 'ISV-0000'}</span>
                    <span class="text-[10px] text-stone-400 block">${order.registeredAt || order.date || 'Reciente'}</span>
                </td>
                <td class="p-3">
                    <span class="font-bold text-white block text-xs">${order.customerName || 'Cliente'}</span>
                    <span class="text-[11px] text-isivi-300 block">${order.phone || 'Sin teléfono'}</span>
                </td>
                <td class="p-3 max-w-[240px]">
                    <div class="flex flex-wrap">${itemsDisplay}</div>
                    <span class="text-[10px] text-isivi-gold/90 block mt-0.5">${isDelivery ? '🏠 Envío a domicilio (' + (order.deliveryAddress || order.direccionEntrega || 'Dirección registrada') + ')' : '📍 Recoger en el local'}</span>
                </td>
                <td class="p-3 whitespace-nowrap">
                    <span class="font-bold text-emerald-400 block text-xs">$${totalAmount.toLocaleString('es-CO')}</span>
                    <div class="flex items-center gap-1 mt-0.5">${payInfo.badgeHtml}</div>
                </td>
                <td class="p-3">
                    ${statusBadge}
                    ${deliveryBadge}
                </td>
                <td class="p-3 text-right whitespace-nowrap">
                    <div class="flex items-center justify-end gap-1.5 flex-wrap">
                        ${isDelivery ? `
                            ${(st === 'PENDIENTE_PREPARACION' || st === 'PAGO CONFIRMADO' || st === 'PAGO_CONFIRMADO' || st === 'CONFIRMADO') && (order.paymentStatus === 'APROBADO' || payInfo.isApproved) ? `
                                <button type="button" onclick="adminSetOrderPreparing('${order.id}')" title="Iniciar preparación" class="px-2.5 py-1 bg-amber-600 hover:bg-amber-500 text-stone-950 font-bold rounded-lg text-[10px] shadow transition">
                                    <i class="fa-solid fa-box-open mr-1"></i> Preparar
                                </button>
                            ` : ''}
                            ${st === 'EN_PREPARACION' || st === 'EN PREPARACIÓN' || st === 'EN PREPARACION' ? `
                                <button type="button" onclick="adminSetOrderReadyForShipping('${order.id}')" title="Marcar listo para envío" class="px-2.5 py-1 bg-emerald-600 hover:bg-emerald-500 text-white font-bold rounded-lg text-[10px] shadow transition">
                                    <i class="fa-solid fa-boxes-packing mr-1"></i> Listo envío
                                </button>
                            ` : ''}
                            ${st === 'LISTO_ENVIO' || st === 'LISTO PARA ENVÍO' || st === 'LISTO PARA ENVIO' ? `
                                <button type="button" onclick="adminSetOrderOnTheWay('${order.id}')" title="Marcar en camino a la dirección" class="px-2.5 py-1 bg-sky-600 hover:bg-sky-500 text-white font-bold rounded-lg text-[10px] shadow transition">
                                    <i class="fa-solid fa-truck-fast mr-1"></i> En camino
                                </button>
                            ` : ''}
                            ${st === 'EN_CAMINO' || st === 'EN CAMINO' ? `
                                <button type="button" onclick="adminSetOrderDelivered('${order.id}')" title="Confirmar entrega al cliente" class="px-2.5 py-1 bg-emerald-700 hover:bg-emerald-600 text-white font-bold rounded-lg text-[10px] shadow transition">
                                    <i class="fa-solid fa-circle-check mr-1"></i> Entregar
                                </button>
                            ` : ''}
                        ` : `
                            ${(st === 'PENDIENTE_PREPARACION' || st === 'PAGO CONFIRMADO' || st === 'PAGO_CONFIRMADO' || st === 'CONFIRMADO') && (order.paymentStatus === 'APROBADO' || payInfo.isApproved) ? `
                                <button type="button" onclick="adminSetOrderPreparing('${order.id}')" title="Iniciar preparación" class="px-2.5 py-1 bg-amber-600 hover:bg-amber-500 text-stone-950 font-bold rounded-lg text-[10px] shadow transition">
                                    <i class="fa-solid fa-box-open mr-1"></i> Preparar
                                </button>
                            ` : ''}
                            ${st === 'EN_PREPARACION' || st === 'EN PREPARACIÓN' || st === 'EN PREPARACION' ? `
                                <button type="button" onclick="adminSetOrderReadyForPickup('${order.id}')" title="Listo para recoger y notificar por Brevo" class="px-2.5 py-1 bg-emerald-600 hover:bg-emerald-500 text-white font-bold rounded-lg text-[10px] shadow transition">
                                    <i class="fa-solid fa-shop mr-1"></i> Listo recoger
                                </button>
                            ` : ''}
                            ${st === 'LISTO_RECOGER' || st === 'LISTO PARA RECOGER' ? `
                                <button type="button" onclick="adminSetOrderDelivered('${order.id}')" title="Marcar como retirado por el cliente" class="px-2.5 py-1 bg-emerald-700 hover:bg-emerald-600 text-white font-bold rounded-lg text-[10px] shadow transition">
                                    <i class="fa-solid fa-hand-holding-heart mr-1"></i> Recogido
                                </button>
                            ` : ''}
                        `}
                        <button type="button" onclick="openAdminOrderDetail('${order.id}')" title="Ver detalle del pedido" class="px-2 py-1 bg-stone-900 hover:bg-stone-800 text-isivi-gold border border-stone-700 rounded-lg text-[10px] font-bold transition">
                            <i class="fa-solid fa-eye mr-1"></i> Detalle
                        </button>
                        <button type="button" onclick="openAdminOrderWhatsApp('${order.id}')" title="Contactar por WhatsApp" class="px-2 py-1 bg-emerald-950 hover:bg-emerald-900 text-emerald-400 border border-emerald-800/60 rounded-lg text-[10px] font-bold transition flex items-center gap-1">
                            <i class="fa-brands fa-whatsapp"></i>
                        </button>
                    </div>
                </td>
            </tr>
        `;
    }).join('');
}

let currentAdminOrderDetail = null;

function openAdminOrderDetail(orderId) {
    let order = adminOrdersList.find(o => o.id === orderId)
        || historyBookingsList.find(o => o.id === orderId)
        || bookingsList.find(o => o.id === orderId);

    if (!order) {
        showToast('Consultando información del pedido...', 'info');
        apiGet('/reservas').then(list => {
            if (Array.isArray(list)) {
                const found = list.find(r => r.id === orderId);
                if (found) {
                    renderAdminOrderDetailModal(mapFromApiReserva(found));
                    return;
                }
            }
            showToast('No se encontró el pedido solicitado', 'error');
        }).catch(err => {
            console.error(err);
            showToast('Error al consultar el pedido', 'error');
        });
        return;
    }

    renderAdminOrderDetailModal(order);
}

function renderAdminOrderDetailModal(order) {
    currentAdminOrderDetail = order;

    document.getElementById('adm-order-detail-code').textContent = order.code || 'ISV-0000';
    document.getElementById('adm-order-detail-customer').textContent = `${order.customerName || 'Cliente'} · ${order.city || 'Cartagena'}`;
    document.getElementById('adm-order-detail-date').textContent = order.registeredAt || order.date || 'Reciente';
    document.getElementById('adm-order-detail-phone').textContent = order.phone || 'No registrado';
    document.getElementById('adm-order-detail-email').textContent = order.email || 'No registrado';

    const deliveryMethodNormalized = String(order.tipoEntrega || order.deliveryMethod || 'pickup').trim().toLowerCase();
    const isDelivery = deliveryMethodNormalized === 'delivery' || deliveryMethodNormalized === 'domicilio';
    const delivEl = document.getElementById('adm-order-detail-delivery');
    if (delivEl) {
        if (isDelivery) {
            delivEl.innerHTML = `<span class="text-amber-300 font-bold"><i class="fa-solid fa-truck mr-1"></i> Envío a domicilio:</span> ${order.deliveryAddress || order.direccionEntrega || 'Dirección registrada'}`;
        } else {
            delivEl.innerHTML = `<span class="text-isivi-gold font-bold"><i class="fa-solid fa-store mr-1"></i> Recoger en el local:</span> ISIVI Salón Cartagena`;
        }
    }

    const st = (order.estadoPedido || order.status || order.estado || '').toUpperCase();
    const pill = document.getElementById('adm-order-detail-status-pill');
    if (pill) {
        if (st === 'ENTREGADO' || st === 'RECOGIDO') {
            pill.textContent = isDelivery ? 'Entregado' : 'Recogido';
            pill.className = 'px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-stone-800 text-stone-300 border border-stone-700';
        } else if (st === 'CANCELADO' || st === 'CANCELADA') {
            pill.textContent = 'Cancelado';
            pill.className = 'px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-red-950 text-red-300 border border-red-500/30';
        } else if (st === 'EN_CAMINO' || st === 'EN CAMINO') {
            pill.textContent = 'En camino';
            pill.className = 'px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-sky-950 text-sky-300 border border-sky-500/30';
        } else if (st === 'LISTO_ENVIO' || st === 'LISTO PARA ENVÍO' || st === 'LISTO PARA ENVIO') {
            pill.textContent = 'Listo para envío';
            pill.className = 'px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-emerald-950 text-emerald-300 border border-emerald-500/30';
        } else if (st === 'LISTO_RECOGER' || st === 'LISTO PARA RECOGER') {
            pill.textContent = 'Listo para recoger';
            pill.className = 'px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-emerald-950 text-emerald-300 border border-emerald-500/30';
        } else if (st === 'EN_PREPARACION' || st === 'EN PREPARACIÓN' || st === 'EN PREPARACION') {
            pill.textContent = 'En preparación';
            pill.className = 'px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-amber-950 text-amber-300 border border-amber-500/30';
        } else {
            pill.textContent = 'Pendiente de preparación';
            pill.className = 'px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-emerald-950 text-emerald-300 border border-emerald-500/30';
        }
    }

    // Tracker de Progreso por Forma de Entrega
    const trackerSteps = document.getElementById('adm-order-tracker-steps');
    if (trackerSteps) {
        if (isDelivery) {
            const isPaid = true;
            const isPrep = st === 'EN_PREPARACION' || st === 'EN PREPARACIÓN' || st === 'EN PREPARACION' || st === 'LISTO_ENVIO' || st === 'LISTO PARA ENVÍO' || st === 'LISTO PARA ENVIO' || st === 'EN_CAMINO' || st === 'EN CAMINO' || st === 'ENTREGADO';
            const isReadyShip = st === 'LISTO_ENVIO' || st === 'LISTO PARA ENVÍO' || st === 'LISTO PARA ENVIO' || st === 'EN_CAMINO' || st === 'EN CAMINO' || st === 'ENTREGADO';
            const isOnWay = st === 'EN_CAMINO' || st === 'EN CAMINO' || st === 'ENTREGADO';
            const isDelivered = st === 'ENTREGADO';

            trackerSteps.className = 'grid grid-cols-5 gap-1 text-center text-[10px]';
            trackerSteps.innerHTML = `
                <div class="p-2 rounded-xl border ${isPaid ? 'bg-emerald-950 border-emerald-500/40 text-emerald-300 font-bold' : 'bg-stone-950 border-stone-800 text-stone-500'}">
                    <i class="fa-solid fa-receipt block mb-0.5 text-xs"></i> 1. Pagado
                </div>
                <div class="p-2 rounded-xl border ${isPrep ? 'bg-amber-950 border-amber-500/40 text-amber-300 font-bold' : 'bg-stone-950 border-stone-800 text-stone-500'}">
                    <i class="fa-solid fa-box-open block mb-0.5 text-xs"></i> 2. En prep.
                </div>
                <div class="p-2 rounded-xl border ${isReadyShip ? 'bg-emerald-950 border-emerald-500/40 text-emerald-300 font-bold' : 'bg-stone-950 border-stone-800 text-stone-500'}">
                    <i class="fa-solid fa-boxes-packing block mb-0.5 text-xs"></i> 3. Listo envío
                </div>
                <div class="p-2 rounded-xl border ${isOnWay ? 'bg-sky-950 border-sky-500/40 text-sky-300 font-bold' : 'bg-stone-950 border-stone-800 text-stone-500'}">
                    <i class="fa-solid fa-truck-fast block mb-0.5 text-xs"></i> 4. En camino
                </div>
                <div class="p-2 rounded-xl border ${isDelivered ? 'bg-emerald-900 border-emerald-400 text-white font-extrabold shadow' : 'bg-stone-950 border-stone-800 text-stone-500'}">
                    <i class="fa-solid fa-circle-check block mb-0.5 text-xs"></i> 5. Entregado
                </div>
            `;
        } else {
            const isPaid = true;
            const isPrep = st === 'EN_PREPARACION' || st === 'EN PREPARACIÓN' || st === 'EN PREPARACION' || st === 'LISTO_RECOGER' || st === 'LISTO PARA RECOGER' || st === 'RECOGIDO' || st === 'ENTREGADO';
            const isReady = st === 'LISTO_RECOGER' || st === 'LISTO PARA RECOGER' || st === 'RECOGIDO' || st === 'ENTREGADO';
            const isPickedUp = st === 'RECOGIDO' || st === 'ENTREGADO';

            trackerSteps.className = 'grid grid-cols-4 gap-1 text-center text-[10px]';
            trackerSteps.innerHTML = `
                <div class="p-2 rounded-xl border ${isPaid ? 'bg-emerald-950 border-emerald-500/40 text-emerald-300 font-bold' : 'bg-stone-950 border-stone-800 text-stone-500'}">
                    <i class="fa-solid fa-receipt block mb-0.5 text-xs"></i> 1. Pagado
                </div>
                <div class="p-2 rounded-xl border ${isPrep ? 'bg-amber-950 border-amber-500/40 text-amber-300 font-bold' : 'bg-stone-950 border-stone-800 text-stone-500'}">
                    <i class="fa-solid fa-box-open block mb-0.5 text-xs"></i> 2. En prep.
                </div>
                <div class="p-2 rounded-xl border ${isReady ? 'bg-emerald-950 border-emerald-500/40 text-emerald-300 font-bold' : 'bg-stone-950 border-stone-800 text-stone-500'}">
                    <i class="fa-solid fa-shop block mb-0.5 text-xs"></i> 3. Listo recoger
                </div>
                <div class="p-2 rounded-xl border ${isPickedUp ? 'bg-emerald-900 border-emerald-400 text-white font-extrabold shadow' : 'bg-stone-950 border-stone-800 text-stone-500'}">
                    <i class="fa-solid fa-circle-check block mb-0.5 text-xs"></i> 4. Recogido
                </div>
            `;
        }
    }

    // Lista de Items
    const itemsList = document.getElementById('adm-order-detail-items-list');
    if (itemsList) {
        if (Array.isArray(order.services) && order.services.length > 0) {
            itemsList.innerHTML = order.services.map(itemStr => `
                <div class="flex items-center justify-between py-1.5 text-xs">
                    <span class="text-white font-medium"><i class="fa-solid fa-bag-shopping text-isivi-gold mr-2 text-[10px]"></i>${itemStr}</span>
                    <span class="text-stone-400 text-[10px]">Unidad / Paquete</span>
                </div>
            `).join('');
        } else {
            itemsList.innerHTML = '<div class="py-2 text-stone-500 text-xs italic">Sin detalle de productos</div>';
        }
    }

    const payInfo = getPaymentMethodInfo(order);
    const totalAmount = Number(order.subtotal) || Number(order.deposit) || 0;

    document.getElementById('adm-order-detail-payment-method').textContent = payInfo.isWompi ? 'Pasarela Wompi Bancolombia' : 'Transferencia Bancolombia';
    document.getElementById('adm-order-detail-payment-status').textContent = payInfo.isApproved ? 'APROBADO & COBRADO' : 'PENDIENTE DE VALIDAR';
    document.getElementById('adm-order-detail-total').textContent = `$${totalAmount.toLocaleString('es-CO')}`;

    // Acciones Contextuales
    const actionsContainer = document.getElementById('adm-order-detail-actions');
    if (actionsContainer) {
        let actionButtons = '';
        if (!payInfo.isApproved && !payInfo.isWompi) {
            actionButtons = `
                <button type="button" onclick="adminApproveOrderFromDetail('${order.id}')" class="px-4 py-2 bg-emerald-600 hover:bg-emerald-500 text-white rounded-xl text-xs font-bold shadow transition">
                    <i class="fa-solid fa-check mr-1"></i> Confirmar comprobante
                </button>
            `;
        } else if (isDelivery) {
            if (st === 'PENDIENTE_PREPARACION' || st === 'PAGO CONFIRMADO' || st === 'CONFIRMADO') {
                actionButtons = `
                    <button type="button" onclick="adminSetOrderPreparing('${order.id}'); closeAdminOrderDetailModal();" class="px-4 py-2 bg-amber-500 hover:bg-amber-400 text-stone-950 rounded-xl text-xs font-bold shadow transition">
                        <i class="fa-solid fa-box-open mr-1"></i> Iniciar Preparación
                    </button>
                `;
            } else if (st === 'EN_PREPARACION' || st === 'EN PREPARACIÓN' || st === 'EN PREPARACION') {
                actionButtons = `
                    <button type="button" onclick="adminSetOrderReadyForShipping('${order.id}'); closeAdminOrderDetailModal();" class="px-4 py-2 bg-emerald-600 hover:bg-emerald-500 text-white rounded-xl text-xs font-bold shadow transition">
                        <i class="fa-solid fa-boxes-packing mr-1"></i> Marcar Listo para Envío
                    </button>
                `;
            } else if (st === 'LISTO_ENVIO' || st === 'LISTO PARA ENVÍO' || st === 'LISTO PARA ENVIO') {
                actionButtons = `
                    <button type="button" onclick="adminSetOrderOnTheWay('${order.id}'); closeAdminOrderDetailModal();" class="px-4 py-2 bg-sky-600 hover:bg-sky-500 text-white rounded-xl text-xs font-bold shadow transition">
                        <i class="fa-solid fa-truck-fast mr-1"></i> Marcar En Camino
                    </button>
                `;
            } else if (st === 'EN_CAMINO' || st === 'EN CAMINO') {
                actionButtons = `
                    <button type="button" onclick="adminSetOrderDelivered('${order.id}'); closeAdminOrderDetailModal();" class="px-4 py-2 bg-emerald-700 hover:bg-emerald-600 text-white rounded-xl text-xs font-bold shadow transition">
                        <i class="fa-solid fa-circle-check mr-1"></i> Confirmar Entrega
                    </button>
                `;
            }
        } else {
            if (st === 'PENDIENTE_PREPARACION' || st === 'PAGO CONFIRMADO' || st === 'CONFIRMADO') {
                actionButtons = `
                    <button type="button" onclick="adminSetOrderPreparing('${order.id}'); closeAdminOrderDetailModal();" class="px-4 py-2 bg-amber-500 hover:bg-amber-400 text-stone-950 rounded-xl text-xs font-bold shadow transition">
                        <i class="fa-solid fa-box-open mr-1"></i> Iniciar Preparación
                    </button>
                `;
            } else if (st === 'EN_PREPARACION' || st === 'EN PREPARACIÓN' || st === 'EN PREPARACION') {
                actionButtons = `
                    <button type="button" onclick="adminSetOrderReadyForPickup('${order.id}'); closeAdminOrderDetailModal();" class="px-4 py-2 bg-emerald-600 hover:bg-emerald-500 text-white rounded-xl text-xs font-bold shadow transition">
                        <i class="fa-solid fa-shop mr-1"></i> Marcar Listo para Recoger (Brevo)
                    </button>
                `;
            } else if (st === 'LISTO_RECOGER' || st === 'LISTO PARA RECOGER') {
                actionButtons = `
                    <button type="button" onclick="adminSetOrderDelivered('${order.id}'); closeAdminOrderDetailModal();" class="px-4 py-2 bg-emerald-700 hover:bg-emerald-600 text-white rounded-xl text-xs font-bold shadow transition">
                        <i class="fa-solid fa-hand-holding-heart mr-1"></i> Confirmar Retiro en Salón
                    </button>
                `;
            }
        }

        actionsContainer.innerHTML = `
            <button type="button" onclick="openAdminOrderWhatsApp('${order.id}')" class="px-3 py-2 bg-emerald-950 hover:bg-emerald-900 text-emerald-400 border border-emerald-800/60 rounded-xl text-xs font-bold transition flex items-center gap-1.5">
                <i class="fa-brands fa-whatsapp text-sm"></i> WhatsApp
            </button>
            ${actionButtons}
        `;
    }

    async function adminApproveOrderFromDetail(orderId) {
        closeAdminOrderDetailModal();
        await approveBooking(orderId);
        await loadAdminOrders();
    }

    document.getElementById('admin-order-detail-modal')?.classList.remove('hidden');
}

function closeAdminOrderDetailModal() {
    document.getElementById('admin-order-detail-modal')?.classList.add('hidden');
}

async function adminSetOrderPreparing(orderId) {
    if (isAdminActionInProgress) return;
    isAdminActionInProgress = true;
    try {
        await apiPatch(`/reservas/${orderId}/pedido/preparar`);
        showToast('Pedido marcado en preparación 📦', 'info');
        await loadAdminOrders();
        await loadDashboardData();
    } catch (err) {
        console.error(err);
        showToast(err.message || 'No se pudo actualizar el pedido a preparación', 'error');
    } finally {
        isAdminActionInProgress = false;
    }
}

async function adminSetOrderReadyForShipping(orderId) {
    if (isAdminActionInProgress) return;
    isAdminActionInProgress = true;
    try {
        await apiPatch(`/reservas/${orderId}/pedido/listo-envio`);
        showToast('Pedido marcado listo para envío 📦', 'success');
        await loadAdminOrders();
        await loadDashboardData();
    } catch (err) {
        console.error(err);
        showToast(err.message || 'No se pudo actualizar a listo para envío', 'error');
    } finally {
        isAdminActionInProgress = false;
    }
}

async function adminSetOrderOnTheWay(orderId) {
    if (isAdminActionInProgress) return;
    isAdminActionInProgress = true;
    try {
        await apiPatch(`/reservas/${orderId}/pedido/en-camino`);
        showToast('Pedido marcado en camino 🚚', 'info');
        await loadAdminOrders();
        await loadDashboardData();
    } catch (err) {
        console.error(err);
        showToast(err.message || 'No se pudo actualizar a en camino', 'error');
    } finally {
        isAdminActionInProgress = false;
    }
}

async function adminSetOrderReadyForPickup(orderId) {
    if (isAdminActionInProgress) return;
    isAdminActionInProgress = true;
    try {
        await apiPatch(`/reservas/${orderId}/pedido/listo-recoger`);
        showToast('Pedido listo para recoger. Brevo notificó al cliente 🛍️', 'success');
        await loadAdminOrders();
        await loadDashboardData();
    } catch (err) {
        console.error(err);
        showToast(err.message || 'No se pudo actualizar el pedido a listo para recoger', 'error');
    } finally {
        isAdminActionInProgress = false;
    }
}

async function adminSetOrderDelivered(orderId) {
    if (isAdminActionInProgress) return;
    if (!confirm('¿Confirmas que el pedido fue entregado/retirado por el cliente? El pedido pasará al Historial.')) return;
    isAdminActionInProgress = true;
    try {
        await apiPatch(`/reservas/${orderId}/pedido/entregar`);
        showToast('Pedido entregado exitosamente ✨', 'success');
        await loadAdminOrders();
        await loadDashboardData();
    } catch (err) {
        console.error(err);
        showToast(err.message || 'No se pudo marcar el pedido como entregado', 'error');
    } finally {
        isAdminActionInProgress = false;
    }
}

function openAdminOrderWhatsApp(orderId) {
    const order = adminOrdersList.find(o => o.id === orderId);
    if (!order) {
        showToast('No se encontró el pedido', 'error');
        return;
    }

    const rawPhone = String(order.phone || '').replace(/\D/g, '');
    if (!rawPhone || rawPhone.length < 7) {
        showToast('El teléfono registrado no es válido para abrir WhatsApp.', 'error');
        return;
    }

    const nombre = (order.customerName || 'Cliente').trim();
    const codigo = (order.code || '').trim();
    const items = Array.isArray(order.services) ? order.services.join(', ') : (order.services || 'Productos');
    const st = (order.status || order.estado || '').toLowerCase();

    let msg = `Hola ${nombre}, te saludamos de ISIVI Salón Cartagena.`;
    if (st.includes('listo')) {
        msg += ` Te informamos que tu pedido *${codigo}* (${items}) ya está *listo para recoger* en nuestro salón 🛍️✨. Te esperamos en nuestro horario habitual.`;
    } else if (st.includes('prep')) {
        msg += ` Te informamos que ya estamos preparando los productos de tu pedido *${codigo}* (${items}) 📦.`;
    } else {
        msg += ` Te escribimos respecto a tu pedido *${codigo}* (${items}). ¿En qué podemos ayudarte?`;
    }

    openWhatsApp(msg, rawPhone);
}

function updateAdminStats() {
    if (typeof updateAdminOrdersStats === 'function') {
        updateAdminOrdersStats();
    }
    if (typeof loadDashboardData === 'function') {
        loadDashboardData();
    }
}


```

## 5. Otros archivos frontend
- **No existen archivos JS/CSS adicionales en la carpeta static.** Todo el diseño está autocontenido de manera premium en `isivi.css` y `isivi.js`.

## 6. Assets utilizados
- `/images/isivi-logo-transparent.png` (Logo principal del header, drawer y modales)
- `/images/isivi-hero.jpg` (Imagen de portada del Hero)
- `/images/isivi-card.jpg` (Imagen del bloque de revista/editorial)
- `/images/isivi-logo.jpeg` y `/images/isivi-logo.png` (Logotipos de reserva)

## 7. Dependencias visuales
- **Tailwind CSS (v2.2.19 & Play CDN):** Utilizado para clases de estructura rápida.
- **FontAwesome 6.4.0 (CDN):** Paquete de iconos de la interfaz.
- **Google Fonts (Cinzel / Plus Jakarta Sans):** Fuentes tipográficas corporativas.
- **Wompi Widget Script (`checkout.wompi.co/widget.js`):** Script para inicializar la pasarela segura de cobro de anticipos.

## 8. Estructura visual actual
- **Header:** Contiene el logo, enlaces de navegación, botón flotante de carrito y acceso al menú hamburguesa en desktop.
- **Hero:** Cabecera con título animado degradado dorado, descripción de servicios, módulos indicadores e interactivos.
- **Servicios:** Catálogo de turnos con precio total dorado, duración e indicador verde de anticipo requerido.
- **Productos:** Grid con variantes de presentación, detalles financieros de stock y controles del carrito.
- **Kits:** Packs promocionales especiales con control de stock y reserva.
- **Banners:** Carrusel rotativo de promociones con autoplay.
- **Carrito:** Drawer que guía al usuario en 5 pasos (Servicio, Fecha, Hora, Datos, Pago).
- **Checkout:** Formulario de recogida/envío, datos de contacto y selección de transferencia o Wompi.
- **Modales:** Diálogos flotantes para consultar reservas o mostrar detalles adicionales.
- **Éxito:** Resumen de reserva/pedido con códigos QR, desglose de pago y saldos.
- **Footer:** Enlaces de redes, horarios e información legal.

## 9. Responsive actual
- **Desktop (min-width: 1024px):** Escalamiento de fuentes, nitidez y alineación horizontal.
- **Móvil (max-width: 480px / 767px):** Tarjetas verticales apiladas de ancho base `var(--catalog-card-width-base)` para evitar desbordamiento en Safari de iPhone.

## 10. Restricciones funcionales
Para evitar romper la aplicación durante el rediseño estético, NO se deben alterar los siguientes fragmentos lógicos de `isivi.js`:
- **Integración Wompi:** Funciones `startCartWompiPayment` y la gestión del widget Wompi.
- **Reserva de Horarios:** El cálculo automático del 25% de anticipo en el frontend, la selección en el calendario y slots de tiempo.
- **Gestión de Holds:** El control del stock temporal de inventario (`completeCartCheckout`).
- **Selectores DOM y Enlaces de Eventos:** Los atributos `id` y `onclick` de las tarjetas que vinculan la interactividad con las listas de datos.
