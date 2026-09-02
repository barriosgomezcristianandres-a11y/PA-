# ISIVI — Client Frontend Source

## 1. Contexto
Este documento contiene el código fuente real y actual de la vista del cliente de ISIVI. Incluye la estructura HTML, las hojas de estilo CSS de diseño y tipografía, y las funciones de JS responsables de renderizar y gestionar los elementos de la interfaz interactiva pública (servicios, productos, kits, banners, carrito, y pasarela Wompi).

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

    <section id="servicios" class="py-16 bg-isivi-black reveal-on-scroll">
        <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
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

    <section id="productos" class="py-16 bg-isivi-900 border-t border-isivi-500/20 reveal-on-scroll">
        <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
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

    <section id="kits" class="py-16 bg-isivi-black border-t border-isivi-500/20 reveal-on-scroll">
        <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
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
    background-image: 
        radial-gradient(circle at 80% 20%, rgba(212, 175, 55, 0.04) 0%, transparent 50%),
        radial-gradient(circle at 15% 60%, rgba(184, 138, 76, 0.03) 0%, transparent 40%),
        radial-gradient(circle at 50% 90%, rgba(16, 185, 129, 0.02) 0%, transparent 50%);
    background-attachment: fixed;
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
    background: rgba(15, 14, 13, 0.9);
    backdrop-filter: blur(16px);
    -webkit-backdrop-filter: blur(16px);
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
    transform: translateZ(0) !important;
    backface-visibility: hidden !important;
    perspective: 1000px !important;
}

.isivi-card-premium:hover {
    transform: translateY(-3px) translateZ(0) !important;
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
    background: rgba(12, 10, 9, 0.96);
    backdrop-filter: blur(16px);
    -webkit-backdrop-filter: blur(16px);
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

#client-banners .carousel-indicator {
    overflow: hidden;
}

#client-banners .indicator-progress {
    width: 0%;
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
        transform: translateZ(0);
        backface-visibility: hidden;
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

/* 2. Remove default webkit blurring transitions on text scaling & translates */
button, a, input, select, textarea {
    backface-visibility: hidden;
    transform: translateZ(0);
}

/* 3. Form fields, placeholders and controls optimization */
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

/* 4. Secondary texts contrast adjustments (prevent muted gray) */
.text-isivi-300 {
    color: #ebdcb9 !important;
    opacity: 1 !important;
}

.text-stone-400, .text-stone-500 {
    color: #d1c2a5 !important;
    opacity: 1 !important;
}

/* 5. Modals and Cart Drawer text sharpness */
.isivi-modal, #cart-drawer, .bg-stone-900, .bg-stone-950 {
    transform: translateZ(0);
    backface-visibility: hidden;
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

```

## 4. isivi.js (Relevante para Vista Cliente)
```javascript
// === function renderServicesGrid ===
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
                            <div class="flex items-center gap-1 mt-0.5">
                                <span class="text-[8px] text-emerald-400 uppercase tracking-wider block font-bold">Anticipo</span>
                                <span class="font-bold text-[9px] text-emerald-400">$${deposit.toLocaleString('es-CO')}</span>
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

// === async function toggleService ===
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

// === function renderProductsGrid ===
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

// === async function toggleKit ===
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

// === function renderKitsGrid ===
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

// === function renderClientBanners ===
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
            <button class="carousel-indicator relative h-1 w-5 sm:h-1.5 sm:w-6 rounded-full bg-stone-800 transition-all duration-300 focus:outline-none" data-slide-index="${i}" aria-label="Ir al banner ${i + 1}" aria-current="${i === 0 ? 'true' : 'false'}">
                <span class="indicator-progress absolute left-0 top-0 h-full w-0 rounded-full bg-isivi-gold transition-all"></span>
            </button>
        `;
    }).join('');

    container.innerHTML = `
        <div class="relative mx-auto max-w-7xl overflow-hidden px-1 sm:px-8 py-2">
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
            
            <div class="carousel-indicators-container flex justify-center gap-1.5 mt-3 sm:mt-5">
                ${indicatorsHtml}
            </div>
        </div>
    `;

    initializeBannersCarousel(N);
}

// === function renderCartDrawer ===
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

// === function showSuccessConfirmation ===
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

// === function toggleCartDeliveryAddress ===
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

// === function setCartPaymentMethod ===
function setCartPaymentMethod(method) {
    const select = document.getElementById('cart-payment-method');
    if (!select) return;
    select.value = method;
    currentPaymentMethod = method;
    trackEvent('select_payment_method', { method: method === 'wompi' ? 'WOMPI' : 'TRANSFER', transfer_channel: method });
    updateCartPaymentDetails();
}

// === function startCartWompiPayment ===
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

// === function completeCartCheckout ===
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

// === function openCartDrawer ===
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

// === function closeCartDrawer ===
function closeCartDrawer() {
    document.getElementById('cart-overlay').classList.add('hidden');
    document.getElementById('cart-drawer').classList.add('hidden');
    document.body.classList.remove('overflow-hidden');
    updateMobileContextBar();
}

// === function openLookupModal ===
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

// === function navigateToSection ===
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

// === function selectProductVariant ===
function selectProductVariant(productId, variantId) {
    selectedProductVariants[productId] = variantId;
    renderProductsGrid();
}

// === function changeCardQuantity ===
function changeCardQuantity(type, id, delta) {
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

// === function addItemToCart ===
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


```

## 5. Componentes visuales
- **Hero:** Sección inicial decorativa e informativa de la marca.
- **Servicios:** Tarjetas con detalles de duración, precio total y anticipo en verde esmeralda.
- **Productos:** Grid de productos al detal con variantes de presentación, precios en dorado.
- **Kits:** Grid de kits especiales con badges identificadores de stock y reservas.
- **Banners:** Carrusel rotativo de promociones y llamados a la acción.
- **Carrito:** Drawer lateral interactivo de compras y selección de turnos/datos.
- **Checkout:** Formulario de contacto, método de entrega y pasarela de pago Wompi.
- **Éxito:** Modal de confirmación final con resumen y detalles del pedido/cita.
- **Footer:** Enlaces de navegación y datos de contacto de ISIVI.

## 6. Responsive
La aplicación utiliza breakpoints estándar definidos en `isivi.css`:
- **Móvil (max-width: 480px / 767px):** Tarjetas compactas, menús desplegables tipo drawer y ocultación de controles de carrusel.
- **Desktop (min-width: 1024px):** Escalado tipográfico, mayor padding en tarjetas, nitidez mediante aceleración de hardware (translateZ) y visualización expandida.

## 7. Design Tokens
- **Colores:** Dorado ISIVI (`--isivi-gold: #d4af37`), Negro (`--isivi-black: #0f0e0d`), Fondos cálidos/Champagne, Verde Anticipo (`--emerald-400: #34d399`).
- **Sombras:** Sombras elevadas para modales y tarjetas premium (`shadow-lg`, `shadow-xl`, `gold-border-glow`).
- **Radios:** Esquinas redondeadas y suavizadas (`rounded-2xl`, `rounded-3xl` / `26px`).
- **Tipografía:** Cinzel (títulos) y Plus Jakarta Sans (cuerpos de texto).

## 8. Restricciones funcionales
Durante cualquier rediseño visual de la interfaz del cliente, NO deben alterarse las siguientes lógicas en JS/CSS:
1. Las llamadas de inicialización y respuesta del widget de **Wompi** (`startCartWompiPayment`).
2. Las funciones de control de estado del stock temporal y holds (`completeCartCheckout`).
3. La lógica de filtrado de categorías de productos y servicios.
4. El almacenamiento en memoria del carrito de compras y las variantes seleccionadas.

