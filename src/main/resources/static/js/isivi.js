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
    telefonoMayorista: '+57 300 962 3174',
    tituloSitio: 'ISIVI Beauty & Care',
    eslogan: 'Belleza artesanal, hecha para ti',
    descripcion: 'Descubre nuestros servicios, productos y kits de belleza.',
    tituloHero: 'Tu belleza, nuestra pasión',
    descripcionHero: 'Servicios y productos seleccionados para cuidar de ti.',
    textoBotonHero: 'Descubrir servicios',
    nombreComercial: 'ISIVI Beauty & Care',
    whatsapp: '+57 300 123 4567',
    direccion: 'Cartagena, Bolívar',
    horarioAtencion: 'Lunes a sábado · 9:00 AM – 7:00 PM',
    mensajeWhatsApp: 'Hola, quiero obtener información sobre los servicios de ISIVI.',
    textoFooter: 'Belleza artesanal y experiencias diseñadas para ti.',
    numeroBancolombia: '300-894-9050',
    titularBancolombia: 'ISIVI Belleza Natural',
    numeroNequi: '3008949050',
    titularNequi: 'ISIVI Capilar',
    numeroDaviplata: '3008949050',
    titularDaviplata: 'ISIVI Capilar'
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
    if (typeof transferOptions !== 'undefined') {
        if (businessConfiguration.numeroBancolombia) transferOptions.bancolombia.number = businessConfiguration.numeroBancolombia;
        if (businessConfiguration.titularBancolombia) transferOptions.bancolombia.holder = businessConfiguration.titularBancolombia;
        if (businessConfiguration.numeroNequi) transferOptions.nequi.number = businessConfiguration.numeroNequi;
        if (businessConfiguration.titularNequi) transferOptions.nequi.holder = businessConfiguration.titularNequi;
        if (businessConfiguration.numeroDaviplata) transferOptions.daviplata.number = businessConfiguration.numeroDaviplata;
        if (businessConfiguration.titularDaviplata) transferOptions.daviplata.holder = businessConfiguration.titularDaviplata;
    }

    const locText = `${businessConfiguration.ciudad || 'Cartagena'}, ${businessConfiguration.pais || 'Colombia'}`;
    const ap12 = formatBusiness12hTime(businessConfiguration.horaApertura, '8:00 AM');
    const ci12 = formatBusiness12hTime(businessConfiguration.horaCierre, '7:00 PM');
    const hoursText = `${businessConfiguration.diasAtencion || 'Mar - Sáb'}: ${ap12} - ${ci12}`;
    const mayoristaPhone = businessConfiguration.telefonoMayorista || '+57 300 962 3174';
    const mayoristaCleanPhone = mayoristaPhone.replace(/\D/g, '');

    const headerLocEl = document.getElementById('header-location-text');
    const footerLocEl = document.getElementById('footer-location-text');
    if (businessConfiguration.direccion) {
        if (headerLocEl) headerLocEl.textContent = businessConfiguration.direccion;
        if (footerLocEl) footerLocEl.textContent = businessConfiguration.direccion;
    } else {
        if (headerLocEl) headerLocEl.textContent = locText;
        if (footerLocEl) footerLocEl.textContent = locText;
    }

    const headerHoursEl = document.getElementById('header-hours-text');
    if (businessConfiguration.horarioAtencion) {
        if (headerHoursEl) headerHoursEl.textContent = businessConfiguration.horarioAtencion;
    } else {
        if (headerHoursEl) headerHoursEl.textContent = hoursText;
    }

    const footerMayoristaTextEl = document.getElementById('footer-mayorista-text');
    if (footerMayoristaTextEl) {
        const phoneFormatted = mayoristaPhone.startsWith('+') ? mayoristaPhone : `(+57) ${mayoristaPhone}`;
        footerMayoristaTextEl.textContent = `${phoneFormatted} (Mayorista)`;
    }

    const footerMayoristaLinkEl = document.getElementById('footer-mayorista-link');
    if (footerMayoristaLinkEl && mayoristaCleanPhone) {
        footerMayoristaLinkEl.href = `https://wa.me/${mayoristaCleanPhone}?text=Hola%20ISIVI%2C%20quiero%20informaci%C3%B3n%20para%20compras%20al%20por%20mayor.`;
    }

    // Nuevos mapeos:
    const siteTitleEl = document.getElementById('site-title-tag');
    if (siteTitleEl) siteTitleEl.textContent = businessConfiguration.tituloSitio || 'ISIVI | Salón de Belleza y Peluquería en Cartagena - Reserva Online & Cuidado Capilar';

    const heroTagEl = document.getElementById('hero-tag-text');
    if (heroTagEl) heroTagEl.textContent = 'Fórmulas Artesanales sin Químicos Agresivos';

    const heroTitleEl = document.getElementById('hero-title-text');
    if (heroTitleEl) {
        heroTitleEl.innerHTML = 'Transforma tu cabello con <span class="gold-gradient-text">ISIVI</span> · Peluquería & Belleza en Cartagena';
    }

    const heroDescEl = document.getElementById('hero-desc-text');
    if (heroDescEl) {
        heroDescEl.innerHTML = 'Nuestra línea capilar combina ingredientes naturales para devolverle la fuerza, el brillo y el crecimiento a tu cabello. Agenda tus servicios de peluquería con el <strong class="text-isivi-gold font-semibold">25% de anticipo</strong> o adquiere nuestros productos en detal y kits.';
    }

    const heroBtnTextEl = document.getElementById('hero-primary-btn-text');
    if (heroBtnTextEl) heroBtnTextEl.textContent = businessConfiguration.textoBotonHero || 'Ver Servicios & Agendar';

    const footerDescEl = document.getElementById('footer-desc-text');
    if (footerDescEl) footerDescEl.textContent = businessConfiguration.descripcion || 'Cuidado capilar artesanal sin sal ni sulfatos. Devolvemos la fuerza y brillo natural a tu hebra capilar.';

    // WhatsApp Detal y flotante
    const detalPhone = businessConfiguration.whatsapp || '573008949050';
    const cleanDetalPhone = detalPhone.replace(/\D/g, '');
    const footerDetalEl = document.getElementById('footer-detal-text');
    if (footerDetalEl) {
        footerDetalEl.textContent = detalPhone.startsWith('+') ? detalPhone : `(+57) ${detalPhone}`;
    }
    const footerDetalLink = document.getElementById('footer-detal-link');
    if (footerDetalLink && cleanDetalPhone) {
        const msg = encodeURIComponent(businessConfiguration.mensajeWhatsApp || 'Hola ISIVI, quiero información sobre sus productos.');
        footerDetalLink.href = `https://wa.me/${cleanDetalPhone}?text=${msg}`;
    }
    const whatsappFloat = document.getElementById('whatsapp-float');
    if (whatsappFloat && cleanDetalPhone) {
        const msg = encodeURIComponent(businessConfiguration.mensajeWhatsApp || 'Hola ISIVI, quiero más información.');
        whatsappFloat.href = `https://wa.me/${cleanDetalPhone}?text=${msg}`;
    }

    const footerCopyrightEl = document.getElementById('footer-copyright-text');
    if (footerCopyrightEl) {
        footerCopyrightEl.innerHTML = businessConfiguration.textoFooter 
            ? `&copy; 2026 ${businessConfiguration.textoFooter}` 
            : `&copy; 2026 ISIVI - Cuidado Capilar Natural &amp; Peluquería. Todos los derechos reservados.`;
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
let servicesLoaded = false;
let productsLoaded = false;
let kitsLoaded = false;
let serviceCategoriesData = [];
let productCategoriesData = [];
let adminProductCategoriesData = [];
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
window.getAuthHeaders = getAuthHeaders;

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

function normalizePhoneNumber(val) {
    if (!val) return '';
    const hasPlus = val.trim().startsWith('+');
    const cleaned = val.replace(/\D/g, '');
    return (hasPlus ? '+' : '') + cleaned;
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
        expiration: overrideReserva.fechaExpiracionPago || overrideReserva.paymentExpiration,
        itemsInventario: overrideReserva.itemsInventario || []
    } : (currentPendingHold || getPendingHold());

    if (!hold || !hold.id) {
        return showToast('No se encontró una reserva pendiente para retomar.', 'error');
    }

    if (hold.expiration && new Date(hold.expiration).getTime() <= Date.now()) {
        clearPendingHold();
        return showToast('El tiempo de retención para este horario ha expirado. Por favor selecciona otro turno.', 'error');
    }

    const isCartEmpty = selectedServices.length === 0 && selectedProducts.length === 0 && selectedKits.length === 0;
    if (isCartEmpty) {
        if (hold && hold.itemsInventario && hold.itemsInventario.length > 0) {
            console.log('RETOMAR: carrito local vacío. Restaurando desde reserva...');
            restoreCartFromHold(hold);
            console.log('RETOMAR: items restaurados =', hold.itemsInventario.length);
        } else {
            console.warn('RETOMAR: carrito vacío y la reserva no contiene itemsInventario válidos. Evitando sincronización []');
            showToast('⚠️ No pudimos recuperar los productos de este pedido. El pedido no fue modificado. Intenta consultar nuevamente o contacta a ISIVI.', 'error');
            return;
        }
    }

    const isCartEmptyPostRestore = selectedServices.length === 0 && selectedProducts.length === 0 && selectedKits.length === 0;
    if (isCartEmptyPostRestore) {
        showToast('⚠️ No pudimos recuperar los productos de este pedido. El pedido no fue modificado. Intenta consultar nuevamente o contacta a ISIVI.', 'error');
        return;
    }

    try {
        const updated = await syncCartItemsWithHold(hold.id, hold.code, hold.phone);
        setPendingHold(updated);
        createCartBackup();
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

    const wompiBtn = document.getElementById('cart-wompi-button');
    const successRetryBtn = document.getElementById('success-btn-retry-payment');
    if (wompiBtn) {
        wompiBtn.disabled = true;
        if (!wompiBtn.dataset.originalHtml) wompiBtn.dataset.originalHtml = wompiBtn.innerHTML;
        wompiBtn.innerHTML = '<i class="fa-solid fa-spinner fa-spin text-base"></i> <span>Preparando tu pago seguro...</span>';
    }
    if (successRetryBtn) {
        successRetryBtn.disabled = true;
        if (!successRetryBtn.dataset.originalHtml) successRetryBtn.dataset.originalHtml = successRetryBtn.innerHTML;
        successRetryBtn.innerHTML = '<i class="fa-solid fa-spinner fa-spin text-xs mr-1"></i> Intentando nuevamente...';
    }

    trackEvent('payment_retry', { method: 'WOMPI', booking_id: hold.id });

    let timeoutWarningTimer = setTimeout(() => {
        if (isSavingReservation) {
            showToast('Wompi está tardando más de lo esperado. Mantén esta pestaña abierta...', 'info');
        }
    }, 6000);

    try {
        const holdInfo = currentPendingHold || getPendingHold();
        const rawSubtotal = holdInfo ? holdInfo.subtotal : 0;
        const deposit = holdInfo ? holdInfo.deposit : 0;
        const hasService = holdInfo ? holdInfo.hasService : false;
        const finalMonto = hasService ? deposit : rawSubtotal;

        console.log('RETOMAR: monto antes de Wompi =', finalMonto);
        if (!finalMonto || finalMonto <= 0 || isNaN(finalMonto)) {
            showToast('⚠️ El monto de este pedido no es válido para realizar el pago.', 'error');
            isSavingReservation = false;
            if (wompiBtn) {
                wompiBtn.disabled = false;
                wompiBtn.innerHTML = wompiBtn.dataset.originalHtml || 'Retomar pago';
            }
            if (successRetryBtn) {
                successRetryBtn.disabled = false;
                successRetryBtn.innerHTML = successRetryBtn.dataset.originalHtml || 'Intentar nuevamente';
            }
            clearTimeout(timeoutWarningTimer);
            return;
        }

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
                restoreCartFromBackup();
                openCartDrawer();
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
        const wompiBtn = document.getElementById('cart-wompi-button');
        if (wompiBtn && wompiBtn.dataset.originalHtml) {
            wompiBtn.disabled = false;
            wompiBtn.innerHTML = wompiBtn.dataset.originalHtml;
        }
        const successRetryBtn = document.getElementById('success-btn-retry-payment');
        if (successRetryBtn && successRetryBtn.dataset.originalHtml) {
            successRetryBtn.disabled = false;
            successRetryBtn.innerHTML = successRetryBtn.dataset.originalHtml;
        }
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
function clasificarOrigenImagen(url, backendOrigen) {
    if (backendOrigen) return backendOrigen;
    if (!url || url.trim() === '') return 'UNKNOWN';
    if (url.startsWith('data:image/')) return 'BASE64';
    if (url.includes('res.cloudinary.com')) return 'CLOUDINARY';
    return 'UNKNOWN';
}

function mapFromApiProducto(p) {
    return {
        id: String(p.id),
        type: 'product',
        name: p.nombre || 'Producto sin nombre',
        price: Number(p.precio) || 0,
        oldPrice: p.precioAnterior != null ? Number(p.precioAnterior) : null,
        desc: p.descripcion || '',
        img: p.imagenUrl || '',
        origenImagen: clasificarOrigenImagen(p.imagenUrl, p.origenImagen),
        imageFit: p.imageFit || 'cover',
        imageZoom: p.imageZoom != null ? Number(p.imageZoom) : 1.0,
        imagePosX: p.imagePosX != null ? Number(p.imagePosX) : 50.0,
        imagePosY: p.imagePosY != null ? Number(p.imagePosY) : 50.0,
        inStock: p.enStock !== false,
        quantity: Number.isInteger(p.cantidad) ? p.cantidad : (p.enStock !== false ? 1 : 0),
        category: p.categoriaId ? String(p.categoriaId) : '',
        priceType: p.tipoPrecio || 'UNICO',
        temporalmenteReservado: p.temporalmenteReservado === true,
        variants: Array.isArray(p.variantes) ? p.variantes.map(v => ({
            id: String(v.id),
            nombre: v.nombre || 'Variante',
            precio: Number(v.precio) || 0,
            oldPrice: v.precioAnterior != null ? Number(v.precioAnterior) : null,
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
        oldPrice: k.precioAnterior != null ? Number(k.precioAnterior) : null,
        desc: k.descripcion || '',
        img: k.imagenUrl || '',
        origenImagen: clasificarOrigenImagen(k.imagenUrl, k.origenImagen),
        imageFit: k.imageFit || 'cover',
        imageZoom: k.imageZoom != null ? Number(k.imageZoom) : 1.0,
        imagePosX: k.imagePosX != null ? Number(k.imagePosX) : 50.0,
        imagePosY: k.imagePosY != null ? Number(k.imagePosY) : 50.0,
        inStock: k.enStock !== false,
        quantity: Number.isInteger(k.cantidad) ? k.cantidad : (k.enStock !== false ? 1 : 0),
        category: k.categoriaId ? String(k.categoriaId) : '',
        temporalmenteReservado: k.temporalmenteReservado === true
    };
}

function mapFromApiServicio(s) {
    return {
        id: String(s.id),
        category: s.categoria || 'Sin categoría',
        name: s.nombre || 'Servicio sin nombre',
        duration: s.duracion || '60 min',
        price: Number(s.precio) || 0,
        desc: s.descripcion || '',
        img: s.imagenUrl || '',
        origenImagen: clasificarOrigenImagen(s.imagenUrl, s.origenImagen),
        imageFit: s.imageFit || 'cover',
        imageZoom: s.imageZoom != null ? Number(s.imageZoom) : 1.0,
        imagePosX: s.imagePosX != null ? Number(s.imagePosX) : 50.0,
        imagePosY: s.imagePosY != null ? Number(s.imagePosY) : 50.0
    };
}
function mapFromApiCategoriaServicio(c) {
    return { id: String(c.id), name: c.nombre || 'Categoría sin nombre' };
}
function mapFromApiCategoriaProducto(c) {
    return { id: String(c.id), name: c.nombre || 'Categoría sin nombre', activo: c.activo !== false, tipo: c.tipo || null };
}
function mapToApiProducto(item) {
    return {
        nombre: item.name,
        precio: item.price,
        precioAnterior: item.oldPrice != null ? Number(item.oldPrice) : null,
        descripcion: item.desc,
        imagenUrl: item.img,
        origenImagen: item.origenImagen || clasificarOrigenImagen(item.img),
        imageFit: item.imageFit || 'cover',
        imageZoom: item.imageZoom != null ? Number(item.imageZoom) : 1.0,
        imagePosX: item.imagePosX != null ? Number(item.imagePosX) : 50.0,
        imagePosY: item.imagePosY != null ? Number(item.imagePosY) : 50.0,
        enStock: item.inStock,
        cantidad: item.quantity,
        categoriaId: item.category || null,
        tipoPrecio: item.priceType || 'UNICO',
        variantes: Array.isArray(item.variants) ? item.variants.map(v => ({
            id: v.id,
            nombre: v.nombre,
            precio: v.precio,
            precioAnterior: v.oldPrice != null ? Number(v.oldPrice) : null,
            cantidad: v.cantidad,
            enStock: v.enStock,
            activo: v.activo
        })) : []
    };
}
function mapToApiServicio(item) {
    return {
        nombre: item.name,
        categoria: item.category,
        precio: item.price,
        duracion: item.duration,
        descripcion: item.desc,
        imagenUrl: item.img,
        origenImagen: item.origenImagen || clasificarOrigenImagen(item.img),
        imageFit: item.imageFit || 'cover',
        imageZoom: item.imageZoom != null ? Number(item.imageZoom) : 1.0,
        imagePosX: item.imagePosX != null ? Number(item.imagePosX) : 50.0,
        imagePosY: item.imagePosY != null ? Number(item.imagePosY) : 50.0
    };
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
    return { 
        id: String(b.id), 
        title: b.titulo || 'Promoción ISIVI', 
        description: b.descripcion || '', 
        image: b.imagenUrl || '', 
        origenImagen: clasificarOrigenImagen(b.imagenUrl, b.origenImagen),
        buttonText: b.textoBoton || 'Conocer más', 
        buttonLink: b.enlaceBoton || '#servicios', 
        active: b.activo !== false,
        tipoDestino: b.tipoDestino || 'NINGUNO',
        destinoId: b.destinoId || null,
        imageZoom: b.imageZoom != null ? Number(b.imageZoom) : 1.0,
        imagePosX: b.imagePosX != null ? Number(b.imagePosX) : 50.0,
        imagePosY: b.imagePosY != null ? Number(b.imagePosY) : 50.0
    };
}

// ---------- Estados de Carga y Resiliencia ----------
function renderServicesLoadingState() {
    const container = document.getElementById('services-grid');
    if (!container) return;
    container.innerHTML = `
        <div class="snap-start isivi-skeleton-card p-5 animate-pulse">
            <div class="h-44 isivi-skeleton rounded-xl mb-4"></div>
            <div class="h-4 isivi-skeleton rounded w-3/4 mb-2"></div>
            <div class="h-3 isivi-skeleton rounded w-1/2"></div>
        </div>
        <div class="snap-start isivi-skeleton-card p-5 animate-pulse">
            <div class="h-44 isivi-skeleton rounded-xl mb-4"></div>
            <div class="h-4 isivi-skeleton rounded w-3/4 mb-2"></div>
            <div class="h-3 isivi-skeleton rounded w-1/2"></div>
        </div>
    `;
}

function renderProductsLoadingState() {
    const container = document.getElementById('products-grid');
    if (!container) return;
    container.innerHTML = `
        <div class="snap-start isivi-skeleton-card p-4 animate-pulse" style="height: var(--catalog-card-height, 380px);">
            <div class="h-36 isivi-skeleton rounded-xl mb-3"></div>
            <div class="h-4 isivi-skeleton rounded w-3/4 mb-2"></div>
            <div class="h-3 isivi-skeleton rounded w-1/2"></div>
        </div>
        <div class="snap-start isivi-skeleton-card p-4 animate-pulse" style="height: var(--catalog-card-height, 380px);">
            <div class="h-36 isivi-skeleton rounded-xl mb-3"></div>
            <div class="h-4 isivi-skeleton rounded w-3/4 mb-2"></div>
            <div class="h-3 isivi-skeleton rounded w-1/2"></div>
        </div>
    `;
}

function renderKitsLoadingState() {
    const container = document.getElementById('kits-grid');
    if (!container) return;
    container.innerHTML = `
        <div class="snap-start isivi-skeleton-card p-5 animate-pulse" style="height: var(--catalog-card-height, 380px);">
            <div class="h-36 isivi-skeleton rounded-xl mb-3"></div>
            <div class="h-4 isivi-skeleton rounded w-3/4 mb-2"></div>
            <div class="h-3 isivi-skeleton rounded w-1/2"></div>
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
        servicesLoaded = true;
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
        productsLoaded = true;
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
        kitsLoaded = true;
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
            servicesLoaded = true;
            renderCategoryTabs();
            renderServicesGrid();
        } else {
            console.warn('Fallo al cargar servicios:', resServicios.reason);
            renderServicesErrorState();
        }

        if (resProductos.status === 'fulfilled' && Array.isArray(resProductos.value)) {
            productsData = resProductos.value.map(mapFromApiProducto);
            productsLoaded = true;
            renderProductsGrid();
        } else {
            console.warn('Fallo al cargar productos:', resProductos.reason);
            renderProductsErrorState();
        }

        if (resKits.status === 'fulfilled' && Array.isArray(resKits.value)) {
            kitsData = resKits.value.map(mapFromApiKit);
            kitsLoaded = true;
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
            adminProductCategoriesData = catProdRes.value.map(mapFromApiCategoriaProducto);
            renderAdminProductCategories();
            populateProductCategorySelect();
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
    try { attachPriceInputFormatter('prod-old-price'); } catch (e) { console.warn('attachPriceInputFormatter prod-old-price:', e); }
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
                    restoreCartFromBackup();
                    openCartDrawer();
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

    if (section === 'isivi-experiencias-section' || section === 'experiencias') {
        setActiveNavigation('experiencias');
        const el = document.getElementById('isivi-experiencias-section');
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
    if (window.IsiviExperienciasModule && typeof window.IsiviExperienciasModule.closeBeforeAfterModal === 'function') {
        window.IsiviExperienciasModule.closeBeforeAfterModal();
    }

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
        startAdminPolling();
    } else {
        stopAdminPolling();
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
    stopAdminPolling();
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
                    <div class="banner-img-wrapper">
                        <img src="${banner.image}" alt="${banner.title}" loading="lazy" decoding="async" style="transform: scale(${banner.imageZoom || 1.0}); transform-origin: ${banner.imagePosX || 50}% ${banner.imagePosY || 50}%; object-position: ${banner.imagePosX || 50}% ${banner.imagePosY || 50}%;" class="w-full h-full object-cover" onerror="this.onerror=null; this.src='/images/isivi-logo-transparent.png'; this.classList.add('p-6','bg-stone-900','object-contain');">
                    </div>
                    <div class="flex flex-1 flex-col justify-center items-center text-center bg-gradient-to-br from-isivi-900 to-stone-950 p-4 sm:p-6">
                        <span class="mb-1 text-[9px] sm:text-[10px] font-bold uppercase tracking-[0.2em] text-isivi-gold">Promoción especial</span>
                        <h2 class="font-serif-title text-base sm:text-xl font-bold text-white">${banner.title}</h2>
                        <p class="mt-1 max-w-xl text-[10px] sm:text-xs leading-relaxed text-isivi-200">${banner.description}</p>
                        <div class="mt-2.5 sm:mt-3">
                            <a href="${banner.buttonLink}" onclick="handleBannerCtaClick('${banner.id}', event)" class="inline-flex rounded-xl bg-isivi-gold px-4 py-2 text-[10px] sm:text-xs font-bold text-isivi-black transition hover:bg-yellow-500">${banner.buttonText}</a>
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
                    <div class="banner-img-wrapper">
                        <img src="${banner.image}" alt="${banner.title}" draggable="false" loading="lazy" decoding="async" style="transform: scale(${banner.imageZoom || 1.0}); transform-origin: ${banner.imagePosX || 50}% ${banner.imagePosY || 50}%; object-position: ${banner.imagePosX || 50}% ${banner.imagePosY || 50}%;" class="w-full h-full object-cover select-none pointer-events-none" onerror="this.onerror=null; this.src='/images/isivi-logo-transparent.png'; this.classList.add('p-6','bg-stone-900','object-contain');">
                    </div>
                    <div class="flex flex-1 flex-col justify-center items-center text-center bg-gradient-to-br from-isivi-900 via-stone-950 to-isivi-900 p-4 sm:p-6">
                        <span class="mb-1 text-[9px] sm:text-[10px] font-bold uppercase tracking-[0.2em] text-isivi-gold">Promoción especial</span>
                        <h2 class="font-serif-title text-base sm:text-xl font-bold text-white">${banner.title}</h2>
                        <p class="mt-1 max-w-xl text-[10px] sm:text-xs leading-relaxed text-isivi-200">${banner.description}</p>
                        <div class="mt-2.5 sm:mt-3">
                            <a href="${banner.buttonLink}" onclick="handleBannerCtaClick('${banner.id}', event)" class="carousel-cta inline-flex rounded-xl bg-isivi-gold px-4 py-2 text-[10px] sm:text-xs font-bold text-isivi-black transition hover:bg-yellow-500">${banner.buttonText}</a>
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
            <div class="text-center mb-6 select-none pointer-events-none">
                <span class="font-serif text-[11px] sm:text-xs text-isivi-gold tracking-[0.3em] uppercase font-bold block mb-2">THE ISIVI EDIT</span>
                <h3 class="font-serif-title text-lg sm:text-xl font-bold text-white tracking-wide">Cartagena · Beauty Stories</h3>
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

function handleBannerCtaClick(bannerId, event) {
    const banner = bannersData.find(b => b.id === bannerId);
    if (!banner) return;

    const { tipoDestino, destinoId } = banner;
    if (!tipoDestino || tipoDestino === 'NINGUNO') return;

    if (event) event.preventDefault();

    if (tipoDestino === 'SECCION') {
        navigateToSection(destinoId.replace('#', ''));
    } else if (tipoDestino === 'PRODUCTO') {
        navigateToSection('productos');
        setTimeout(() => {
            const btn = document.querySelector(`[onclick*="addItemToCart('product','${destinoId}')"]`);
            if (btn) {
                const card = btn.closest('.isivi-card-premium');
                if (card) {
                    card.scrollIntoView({ behavior: 'smooth', block: 'center' });
                    card.classList.add('ring-4', 'ring-isivi-gold');
                    setTimeout(() => card.classList.remove('ring-4', 'ring-isivi-gold'), 3000);
                }
            }
        }, 300);
    } else if (tipoDestino === 'KIT') {
        navigateToSection('kits');
        setTimeout(() => {
            const btn = document.querySelector(`[onclick*="addItemToCart('kit','${destinoId}')"]`);
            if (btn) {
                const card = btn.closest('.isivi-card-premium');
                if (card) {
                    card.scrollIntoView({ behavior: 'smooth', block: 'center' });
                    card.classList.add('ring-4', 'ring-isivi-gold');
                    setTimeout(() => card.classList.remove('ring-4', 'ring-isivi-gold'), 3000);
                }
            }
        }, 300);
    } else if (tipoDestino === 'SERVICIO') {
        navigateToSection('servicios');
        setTimeout(() => {
            const btn = document.querySelector(`[onclick*="toggleService('${destinoId}')"]`);
            if (btn) {
                const card = btn.closest('.isivi-card-premium');
                if (card) {
                    card.scrollIntoView({ behavior: 'smooth', block: 'center' });
                    card.classList.add('ring-4', 'ring-isivi-gold');
                    setTimeout(() => card.classList.remove('ring-4', 'ring-isivi-gold'), 3000);
                }
            }
        }, 300);
    } else if (tipoDestino === 'CATEGORIA') {
        if (destinoId.startsWith('SERVICIOCAT_')) {
            const catId = destinoId.replace('SERVICIOCAT_', '');
            navigateToSection('servicios');
            setCategory(catId);
        } else if (destinoId.startsWith('PRODUCTOCAT_')) {
            const catId = destinoId.replace('PRODUCTOCAT_', '');
            navigateToSection('productos');
            selectProductCategory(catId);
        }
    }
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
        container.innerHTML = '<div class="col-span-full w-full py-16 text-center text-isivi-300 flex flex-col items-center justify-center bg-stone-950/20 rounded-2xl border border-stone-900/60 p-6"><i class="fa-solid fa-sparkles text-3xl text-isivi-gold/30 mb-2 block"></i><p class="font-bold text-isivi-gold text-xs">No hay servicios en esta categoría</p><p class="text-[11px] text-stone-400 mt-1 max-w-[240px]">Nuestras terapeutas están actualizando la agenda de tratamientos pronto.</p></div>';
        return;
    }

    container.innerHTML = filtered.map((service, index) => {
        const isSelected = selectedServices.includes(service.id);
        const deposit = Math.round(service.price * 0.25);
        return `
            <div class="snap-start isivi-card-premium rounded-2xl overflow-hidden border-2 ${isSelected ? 'border-isivi-gold ring-1 ring-isivi-gold' : 'border-isivi-gold/60'} shadow-lg flex flex-col justify-between transition-transform duration-300 hover:-translate-y-1" style="height: var(--catalog-card-height-service); width: calc(var(--catalog-card-width-base) * 1.1);">
                <div id="serv-img-${service.id}" onclick="openServiceDetailModal('${service.id}')" class="relative overflow-hidden isivi-card-image-container rounded-t-2xl rounded-b-xl flex-shrink-0 cursor-pointer" style="height: var(--catalog-card-image-height-service);" tabindex="0" role="button" aria-label="Ver detalles de ${service.name}">
                    <img src="${service.img}" alt="${service.name}" loading="lazy" style="object-fit: ${service.imageFit || 'cover'}; transform: scale(${service.imageZoom || 1}); object-position: ${service.imagePosX || 50}% ${service.imagePosY || 50}%; transform-origin: ${service.imagePosX || 50}% ${service.imagePosY || 50}%;" class="w-full h-full transition-transform duration-700" onerror="this.onerror=null; this.src='/images/isivi-logo-transparent.png'; this.classList.add('p-6','bg-stone-900','object-contain');">
                    <span class="absolute top-3 right-3 bg-stone-950/90 text-isivi-gold text-[10px] font-bold px-2 py-0.5 rounded-full border border-isivi-500/30">
                        <i class="fa-regular fa-clock mr-1"></i> ${service.duration || '60 min'}
                    </span>
                </div>
                <div class="p-4 flex-1 flex flex-col justify-between min-h-0 overflow-hidden">
                    <div class="min-h-0 overflow-hidden">
                        <h3 id="serv-title-${service.id}" onclick="openServiceDetailModal('${service.id}')" class="font-bold text-isivi-gold text-xs mb-1 truncate cursor-pointer hover:underline" tabindex="0" role="button">${service.name}</h3>
                        <p id="card-desc-service-${service.id}" class="text-isivi-300 text-[10px] leading-snug isivi-line-clamp-3">${escapeHtml(service.desc || '')}</p>
                        <button id="ver-mas-btn-service-${service.id}" type="button" onclick="openServiceDetailModal('${service.id}')" class="isivi-ver-mas-btn hidden" aria-label="Ver más detalles de ${service.name}"><span>Ver más</span> <i class="fa-solid fa-chevron-right"></i></button>
                    </div>
                    <div class="pt-2 border-t border-stone-900 flex items-center justify-between flex-shrink-0">
                        <div class="flex flex-col gap-0.5">
                            <div>
                                <span class="text-[8px] text-stone-400 uppercase tracking-wider font-semibold block leading-none">Precio Total</span>
                                <span class="font-extrabold text-isivi-gold text-[13px] block leading-tight">$${service.price.toLocaleString('es-CO')}</span>
                            </div>
                            <div class="inline-flex items-center gap-1 mt-0.5 md:mt-1.5 px-2 py-0.5 md:px-2.5 md:py-1 rounded-full bg-emerald-500/10 border border-emerald-500/20 text-emerald-400">
                                <span class="text-[7px] md:text-[8px] uppercase tracking-wider font-bold md:font-extrabold">Anticipo</span>
                                <span class="font-extrabold text-[8px] md:text-[10px]">$${deposit.toLocaleString('es-CO')}</span>
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

    updateVerMasVisibility('service', filtered);
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

    const activeCats = (productCategoriesData || []).filter(c => c.activo !== false && (c.tipo === 'PRODUCTO' || !c.tipo));
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

    const activeCats = (productCategoriesData || []).filter(c => c.activo !== false && (c.tipo === 'KIT' || !c.tipo));
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
        container.innerHTML = '<div class="col-span-full w-full py-16 text-center text-isivi-300 flex flex-col items-center justify-center bg-stone-950/20 rounded-2xl border border-stone-900/60 p-6"><i class="fa-solid fa-boxes-stacked text-3xl text-isivi-gold/30 mb-2 block"></i><p class="font-bold text-isivi-gold text-xs">No hay productos en esta categoría</p><p class="text-[11px] text-stone-400 mt-1 max-w-[240px]">Estamos renovando el inventario de fórmulas artesanales para ti.</p></div>';
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
            const currentHold = getPendingHold();
            const hasOwnHold = currentHold && currentHold.itemsInventario && currentHold.itemsInventario.some(item => {
                if (item.tipo !== 'producto' || item.id !== product.id) return false;
                if (isVariantType) {
                    return currentVar && item.varianteId === currentVar.id;
                }
                return true;
            });

            if (availableStock > 0) {
                if (isReservado) {
                    if (hasOwnHold) {
                        badgeHtml = `<span class="absolute top-3 right-3 bg-emerald-950 text-emerald-400 border border-emerald-800 text-[10px] font-bold px-2 py-0.5 rounded-full z-10 flex items-center gap-1"><i class="fa-solid fa-clock"></i> RESERVADO PARA TI</span>`;
                    }
                }
            } else {
                if (isReservado) {
                    if (hasOwnHold) {
                        badgeHtml = `<span class="absolute top-3 right-3 bg-emerald-950 text-emerald-400 border border-emerald-800 text-[10px] font-bold px-2 py-0.5 rounded-full z-10 flex items-center gap-1"><i class="fa-solid fa-clock"></i> RESERVADO PARA TI</span>`;
                    } else {
                        badgeHtml = `<span class="absolute top-3 right-3 bg-sky-950 text-sky-400 border border-sky-800 text-[10px] font-bold px-2 py-0.5 rounded-full z-10 flex items-center gap-1"><i class="fa-solid fa-clock"></i> AGOTADO TEMPORALMENTE</span>`;
                    }
                } else {
                    badgeHtml = `<span class="absolute top-3 right-3 bg-red-950 text-red-400 border border-red-800 text-[10px] font-bold px-2 py-0.5 rounded-full z-10">AGOTADO</span>`;
                }
            }
        }

        return `
            <div class="snap-start isivi-card-premium rounded-2xl overflow-hidden border-2 ${isSelected ? 'border-isivi-gold ring-1 ring-isivi-gold' : 'border-isivi-gold/60'} shadow-md flex flex-col justify-between relative" style="height: var(--catalog-card-height); width: var(--catalog-card-width-base);">
                ${badgeHtml}
                <div id="prod-img-${product.id}" onclick="openProductDetailModal('${product.id}')" class="relative overflow-hidden isivi-card-image-container rounded-t-2xl rounded-b-xl flex-shrink-0 cursor-pointer" style="height: var(--catalog-card-image-height);" tabindex="0" role="button" aria-label="Ver detalles de ${product.name}">
                    <img src="${product.img}" alt="${product.name}" loading="lazy" style="object-fit: ${product.imageFit || 'cover'}; transform: scale(${product.imageZoom || 1}); object-position: ${product.imagePosX || 50}% ${product.imagePosY || 50}%; transform-origin: ${product.imagePosX || 50}% ${product.imagePosY || 50}%;" class="w-full h-full transition-transform duration-700 ${!inStock ? 'opacity-40 grayscale' : ''}" onerror="this.onerror=null; this.src='/images/isivi-logo-transparent.png'; this.classList.add('p-4','bg-stone-900','object-contain');">
                </div>
                <div class="p-4 flex-1 flex flex-col justify-between min-h-0 overflow-hidden">
                    <div class="min-h-0 overflow-hidden">
                        <h4 id="prod-title-${product.id}" onclick="openProductDetailModal('${product.id}')" class="font-bold text-isivi-gold text-xs mb-1 truncate cursor-pointer hover:underline" tabindex="0" role="button">${product.name}</h4>
                        <p id="card-desc-product-${product.id}" class="text-isivi-300 text-[10px] leading-snug isivi-line-clamp-3">${escapeHtml(product.desc || '')}</p>
                        <button id="ver-mas-btn-product-${product.id}" type="button" onclick="openProductDetailModal('${product.id}')" class="isivi-ver-mas-btn hidden" aria-label="Ver más detalles de ${product.name}"><span>Ver más</span> <i class="fa-solid fa-chevron-right"></i></button>
                        ${variantChipsHtml}
                    </div>
                    <div class="pt-2 border-t border-stone-900 flex flex-col gap-1.5 flex-shrink-0">
                        <div class="flex flex-col">
                            <span class="text-[8px] text-stone-400 uppercase tracking-wider font-semibold block leading-none">${isVariantType ? 'Precio Variante' : 'Precio Total'}</span>
                            ${(function() {
                                const oldP = (currentVar && currentVar.oldPrice != null) ? currentVar.oldPrice : product.oldPrice;
                                if (oldP && Number(oldP) > Number(priceToDisplay)) {
                                    const sav = Number(oldP) - Number(priceToDisplay);
                                    return `
                                        <div class="flex items-baseline gap-1.5 flex-wrap mt-0.5">
                                            <span class="font-extrabold text-isivi-gold text-[13px] block leading-tight">$${priceToDisplay.toLocaleString('es-CO')}</span>
                                            <span class="text-[10px] text-stone-500 line-through font-medium">$${Number(oldP).toLocaleString('es-CO')}</span>
                                            <span class="text-[8px] font-black text-rose-400 uppercase bg-rose-500/10 border border-rose-500/20 px-1 py-0.2 rounded">Ahorras $${sav.toLocaleString('es-CO')}</span>
                                        </div>
                                    `;
                                }
                                return `<span class="font-extrabold text-isivi-gold text-[13px] block leading-tight mt-0.5">$${priceToDisplay.toLocaleString('es-CO')}</span>`;
                            })()}
                        </div>
                        <div class="w-full">
                            ${actionButtonHtml}
                        </div>
                    </div>
                </div>
            </div>
        `;
    }).join('');

    updateVerMasVisibility('product', filtered);
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
        container.innerHTML = '<div class="col-span-full w-full py-16 text-center text-isivi-300 flex flex-col items-center justify-center bg-stone-950/20 rounded-2xl border border-stone-900/60 p-6"><i class="fa-solid fa-gift text-3xl text-isivi-gold/30 mb-2 block"></i><p class="font-bold text-isivi-gold text-xs">No hay kits disponibles en esta categoría</p><p class="text-[11px] text-stone-400 mt-1 max-w-[240px]">Pronto publicaremos nuevos paquetes y kits de ahorro combinado.</p></div>';
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
            const currentHold = getPendingHold();
            const hasOwnHold = currentHold && currentHold.itemsInventario && currentHold.itemsInventario.some(item => {
                return item.tipo === 'kit' && item.id === kit.id;
            });

            if (availableStock > 0) {
                if (isReservado) {
                    if (hasOwnHold) {
                        badgeHtml = `<span class="absolute top-8 right-3 bg-emerald-950 text-emerald-400 border border-emerald-800 text-[9px] font-bold px-2 py-0.5 rounded-full z-10 flex items-center gap-1"><i class="fa-solid fa-clock"></i> RESERVADO PARA TI</span>`;
                    }
                }
            } else {
                if (isReservado) {
                    if (hasOwnHold) {
                        badgeHtml = `<span class="absolute top-8 right-3 bg-emerald-950 text-emerald-400 border border-emerald-800 text-[9px] font-bold px-2 py-0.5 rounded-full z-10 flex items-center gap-1"><i class="fa-solid fa-clock"></i> RESERVADO PARA TI</span>`;
                    } else {
                        badgeHtml = `<span class="absolute top-8 right-3 bg-sky-950 text-sky-400 border border-sky-800 text-[9px] font-bold px-2 py-0.5 rounded-full z-10 flex items-center gap-1"><i class="fa-solid fa-clock"></i> AGOTADO TEMPORALMENTE</span>`;
                    }
                } else {
                    badgeHtml = `<span class="absolute top-8 right-3 bg-red-950 text-red-400 border border-red-800 text-[9px] font-bold px-2 py-0.5 rounded-full z-10">AGOTADO</span>`;
                }
            }
        }

        return `
            <div class="snap-start isivi-card-premium rounded-2xl border-2 ${isSelected ? 'border-isivi-gold ring-1 ring-isivi-gold' : 'border-isivi-gold/60'} shadow-xl flex flex-col justify-between relative overflow-hidden" style="height: var(--catalog-card-height); width: var(--catalog-card-width-base);">
                <span class="absolute top-3 left-3 bg-isivi-gold text-isivi-black text-[9px] font-bold px-2.5 py-0.5 rounded-full uppercase z-10 shadow">Kit Especial</span>
                ${badgeHtml}
                <div id="kit-img-${kit.id}" onclick="openKitDetailModal('${kit.id}')" class="relative overflow-hidden isivi-card-image-container rounded-t-2xl rounded-b-xl flex-shrink-0 cursor-pointer" style="height: var(--catalog-card-image-height);" tabindex="0" role="button" aria-label="Ver detalles de ${kit.name}">
                    <img src="${kit.img}" alt="${kit.name}" loading="lazy" style="object-fit: ${kit.imageFit || 'cover'}; transform: scale(${kit.imageZoom || 1}); object-position: ${kit.imagePosX || 50}% ${kit.imagePosY || 50}%; transform-origin: ${kit.imagePosX || 50}% ${kit.imagePosY || 50}%;" class="w-full h-full transition-transform duration-700 ${!inStock ? 'opacity-40 grayscale' : ''}" onerror="this.onerror=null; this.src='/images/isivi-logo-transparent.png'; this.classList.add('p-4','bg-stone-900','object-contain');">
                </div>
                <div class="p-4 flex-1 flex flex-col justify-between min-h-0 overflow-hidden">
                    <div class="min-h-0 overflow-hidden">
                        <h4 id="kit-title-${kit.id}" onclick="openKitDetailModal('${kit.id}')" class="font-bold text-isivi-gold text-xs mb-1 truncate cursor-pointer hover:underline" tabindex="0" role="button">${kit.name}</h4>
                        <p id="card-desc-kit-${kit.id}" class="text-isivi-300 text-[10px] leading-snug isivi-line-clamp-3">${escapeHtml(kit.desc || '')}</p>
                        <button id="ver-mas-btn-kit-${kit.id}" type="button" onclick="openKitDetailModal('${kit.id}')" class="isivi-ver-mas-btn hidden" aria-label="Ver más detalles de ${kit.name}"><span>Ver más</span> <i class="fa-solid fa-chevron-right"></i></button>
                    </div>
                    <div class="pt-2 border-t border-stone-900 flex flex-col gap-1.5 flex-shrink-0">
                        <div class="flex flex-col">
                            <span class="text-[8px] text-stone-400 uppercase tracking-wider font-semibold block leading-none">Precio Total Kit</span>
                            ${(function() {
                                if (kit.oldPrice && Number(kit.oldPrice) > Number(kit.price)) {
                                    const sav = Number(kit.oldPrice) - Number(kit.price);
                                    return `
                                        <div class="flex items-baseline gap-1.5 flex-wrap mt-0.5">
                                            <span class="font-extrabold text-isivi-gold text-[13px] block leading-tight">$${kit.price.toLocaleString('es-CO')}</span>
                                            <span class="text-[10px] text-stone-500 line-through font-medium">$${Number(kit.oldPrice).toLocaleString('es-CO')}</span>
                                            <span class="text-[8px] font-black text-rose-400 uppercase bg-rose-500/10 border border-rose-500/20 px-1 py-0.2 rounded">Ahorras $${sav.toLocaleString('es-CO')}</span>
                                        </div>
                                    `;
                                }
                                return `<span class="font-extrabold text-isivi-gold text-[13px] block leading-tight mt-0.5">$${kit.price.toLocaleString('es-CO')}</span>`;
                            })()}
                        </div>
                        <div class="w-full">
                            ${kitActionHtml}
                        </div>
                    </div>
                </div>
            </div>
        `;
    }).join('');

    updateVerMasVisibility('kit', filtered);
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
    const deposit = isProductOrder ? subtotal : (serviceDeposit + productSubtotal);
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
    if (servicesLoaded && Array.isArray(servicesData) && servicesData.length > 0) {
        selectedServices = selectedServices.filter(id => servicesData.some(s => s.id === id));
    }
    if (productsLoaded && Array.isArray(productsData) && productsData.length > 0) {
        selectedProducts = selectedProducts.filter(cartKey => {
            const id = cartKey.split('_')[0];
            return productsData.some(p => p.id === id);
        });
    }
    if (kitsLoaded && Array.isArray(kitsData) && kitsData.length > 0) {
        selectedKits = selectedKits.filter(id => kitsData.some(k => k.id === id));
    }

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

    if (typeof updateAccordionUI === 'function') {
        updateAccordionUI();
    }
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
    const paymentToday = items.reduce((sum, item) => {
        if (item.type === 'service') {
            return sum + Math.round(item.price * item.quantity * 0.25);
        } else {
            return sum + (item.price * item.quantity);
        }
    }, 0);
    document.getElementById('cart-item-count').textContent = `${items.length} ${items.length === 1 ? 'artículo' : 'artículos'}`;
    document.getElementById('cart-total').textContent = `$${paymentToday.toLocaleString('es-CO')}`;
    document.getElementById('cart-total-label').textContent = hasServices ? 'Pago requerido hoy (25% servicios + productos)' : 'Total';
    document.getElementById('cart-help').textContent = items.some(item => item.type === 'service')
        ? 'Completa tus datos, agenda tu cita y asegura tu turno con el 25% de anticipo del servicio.'
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
    }).join('') : `<div class="flex h-full min-h-60 flex-col items-center justify-center rounded-2xl border border-dashed border-stone-700/60 px-6 py-8 text-center bg-stone-950/20">
        <i class="fa-solid fa-bag-shopping mb-3 text-4xl text-isivi-gold/30"></i>
        <p class="text-sm font-extrabold text-isivi-gold">Tu carrito está vacío</p>
        <p class="mt-1.5 text-xs text-isivi-300 max-w-[220px] mx-auto leading-normal">Descubre nuestros exclusivos tratamientos artesanales y productos de cuidado capilar.</p>
        <button type="button" onclick="closeCartDrawer()" class="mt-4 px-5 py-2 rounded-xl bg-gradient-to-r from-isivi-500 to-isivi-600 text-isivi-black font-bold text-xs hover:brightness-110 transition shadow-md active:scale-98">
            Explorar catálogo
        </button>
    </div>`;

    document.getElementById('cart-checkout').classList.toggle('hidden', !items.length);
    document.getElementById('cart-delivery-options').classList.toggle('hidden', !hasProducts);
    document.getElementById('cart-schedule-section').classList.toggle('hidden', !hasServices);
    document.getElementById('cart-payment-section').classList.add('hidden');

    // Build the dynamic order breakdown grouped by categories
    const breakdownContainer = document.getElementById('cart-summary-items-breakdown');
    if (breakdownContainer) {
        let breakdownHTML = '';
        const services = items.filter(item => item.type === 'service');
        const products = items.filter(item => item.type === 'product');
        const kits = items.filter(item => item.type === 'kit');

        if (services.length > 0) {
            breakdownHTML += `
                <div class="space-y-2 pb-3 border-b border-stone-900/60">
                    <div class="flex items-center justify-between text-[10px] uppercase font-bold text-isivi-gold tracking-wider">
                        <span>Servicios</span>
                        <span class="px-2 py-0.5 rounded-full text-[9px] bg-emerald-950/80 text-emerald-400 border border-emerald-500/20 font-sans">25% Anticipo</span>
                    </div>
                    ${services.map(s => `
                        <div class="flex justify-between text-xs py-0.5">
                            <span class="text-stone-300 font-medium">${s.name}</span>
                            <span class="text-white font-bold">$${s.price.toLocaleString('es-CO')}</span>
                        </div>
                    `).join('')}
                </div>
            `;
        }

        if (products.length > 0) {
            breakdownHTML += `
                <div class="space-y-2 pb-3 border-b border-stone-900/60 mt-3">
                    <div class="flex items-center justify-between text-[10px] uppercase font-bold text-isivi-gold tracking-wider">
                        <span>Productos</span>
                        <span class="px-2 py-0.5 rounded-full text-[9px] bg-stone-800 text-stone-300 border border-stone-700 font-sans">100% Hoy</span>
                    </div>
                    ${products.map(p => `
                        <div class="flex justify-between text-xs py-0.5">
                            <span class="text-stone-300 font-medium">${p.name} <span class="text-[10px] text-stone-500 font-normal">(${p.quantity} x $${p.price.toLocaleString('es-CO')})</span></span>
                            <span class="text-white font-bold">$${(p.price * p.quantity).toLocaleString('es-CO')}</span>
                        </div>
                    `).join('')}
                </div>
            `;
        }

        if (kits.length > 0) {
            breakdownHTML += `
                <div class="space-y-2 pb-3 border-b border-stone-900/60 mt-3">
                    <div class="flex items-center justify-between text-[10px] uppercase font-bold text-isivi-gold tracking-wider">
                        <span>Kits Especiales</span>
                        <span class="px-2 py-0.5 rounded-full text-[9px] bg-stone-800 text-stone-300 border border-stone-700 font-sans">100% Hoy</span>
                    </div>
                    ${kits.map(k => `
                        <div class="flex justify-between text-xs py-0.5">
                            <span class="text-stone-300 font-medium">${k.name} <span class="text-[10px] text-stone-500 font-normal">(${k.quantity} x $${k.price.toLocaleString('es-CO')})</span></span>
                            <span class="text-white font-bold">$${(k.price * k.quantity).toLocaleString('es-CO')}</span>
                        </div>
                    `).join('')}
                </div>
            `;
        }

        breakdownContainer.innerHTML = breakdownHTML;
    }

    const orderTotalVal = document.getElementById('cart-order-total-val');
    if (orderTotalVal) {
        orderTotalVal.textContent = `$${total.toLocaleString('es-CO')}`;
    }

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
        ? '<i class="fa-brands fa-whatsapp text-base"></i><span>Pagar por WhatsApp</span>'
        : '<i class="fa-brands fa-whatsapp text-base"></i><span>Pagar por WhatsApp</span>';
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

    currentAccordionStep = 1;
    updateAccordionUI();
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

    const totalItemsCount = selectedServices.length + selectedProducts.length + selectedKits.length;
    if (totalItemsCount === 0) {
        clearCartBackup();
    }
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
    container.innerHTML = '<span class="col-span-full text-center text-[11px] text-isivi-gold/80 flex items-center justify-center gap-1.5 py-3"><i class="fa-solid fa-spinner fa-spin animate-spin"></i> Cargando horarios de cita...</span>';
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
    createCartBackup();
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
    
    const wompiBtn = document.getElementById('cart-wompi-button');
    if (wompiBtn) {
        wompiBtn.disabled = true;
        if (!wompiBtn.dataset.originalHtml) wompiBtn.dataset.originalHtml = wompiBtn.innerHTML;
        wompiBtn.innerHTML = '<i class="fa-solid fa-spinner fa-spin text-base"></i> <span>Preparando tu pago seguro...</span>';
    }
    
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

function getAccordionStepForField(fieldId, hasServices) {
    if (['cart-cust-name', 'cart-cust-phone', 'cart-cust-email', 'cart-delivery-address'].includes(fieldId)) {
        return 3;
    }
    if (['cart-date', 'cart-time'].includes(fieldId)) {
        return hasServices ? 2 : 1;
    }
    return 1;
}

function showCartErrorSummary(message) {
    const summaryEl = document.getElementById('cart-error-summary');
    if (summaryEl) {
        const textSpan = summaryEl.querySelector('span');
        if (textSpan) textSpan.textContent = message;
        summaryEl.classList.remove('hidden');
        summaryEl.scrollIntoView({ behavior: 'smooth', block: 'center' });
    } else {
        showToast(message, 'error');
    }
}

function renderCartErrors(errors) {
    clearCartFormErrors();
    const firstError = errors[0];
    if (firstError) {
        showCartErrorSummary(firstError.message);
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

    if (firstError) {
        const items = getCartItems();
        const hasServices = items.some(item => item.type === 'service');
        const targetStep = getAccordionStepForField(firstError.fieldId, hasServices);
        
        currentAccordionStep = targetStep;
        updateAccordionUI();

        const targetEl = document.getElementById(firstError.fieldId);
        if (targetEl) {
            if (typeof targetEl.focus === 'function' && targetEl.tagName === 'INPUT') {
                targetEl.focus();
            }
            setTimeout(() => {
                targetEl.scrollIntoView({ behavior: 'smooth', block: 'center' });
            }, 50);
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
    const phoneRaw = (phoneInput?.value || '').trim();
    const phone = normalizePhoneNumber(phoneRaw);
    if (phoneInput) phoneInput.value = phone;
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
        renderCartErrors(errors);
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

    const isProductOrder = checkoutMode === 'products' || (selectedServices.length === 0 && (selectedProducts.length > 0 || selectedKits.length > 0));
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

    clearCartBackup();

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

    // Normalizar espacios duplicados, trim, y limpiar guiones si es número de teléfono
    let normalizedQuery = query.replace(/\s+/g, ' ');
    if (/^\+?[0-9\s().-]+$/.test(normalizedQuery)) {
        normalizedQuery = normalizePhoneNumber(normalizedQuery);
    } else {
        normalizedQuery = normalizedQuery.replace(/\s/g, '').toUpperCase();
    }

    try {
        const data = await apiGet(`/reservas/consultar?query=${encodeURIComponent(normalizedQuery)}`);
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
    const phoneInputVal = (document.getElementById('cart-cust-phone')?.value || document.getElementById('cust-phone')?.value || '').trim();
    const phone = normalizePhoneNumber(phoneInputVal);
    const cartCustPhoneEl = document.getElementById('cart-cust-phone');
    if (cartCustPhoneEl) cartCustPhoneEl.value = phone;
    const custPhoneEl = document.getElementById('cust-phone');
    if (custPhoneEl) custPhoneEl.value = phone;
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
    
    const wompiBtn = document.getElementById('cart-wompi-button');
    if (wompiBtn) {
        wompiBtn.disabled = true;
        if (!wompiBtn.dataset.originalHtml) wompiBtn.dataset.originalHtml = wompiBtn.innerHTML;
        wompiBtn.innerHTML = '<i class="fa-solid fa-spinner fa-spin text-base"></i> <span>Preparando tu pago seguro...</span>';
    }

    const name = (document.getElementById('cart-cust-name')?.value || document.getElementById('cust-name')?.value || '').trim();
    const phoneInputVal = (document.getElementById('cart-cust-phone')?.value || document.getElementById('cust-phone')?.value || '').trim();
    const phone = normalizePhoneNumber(phoneInputVal);
    const cartCustPhoneEl = document.getElementById('cart-cust-phone');
    if (cartCustPhoneEl) cartCustPhoneEl.value = phone;
    const custPhoneEl = document.getElementById('cust-phone');
    if (custPhoneEl) custPhoneEl.value = phone;
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

        closeCartDrawer();

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
                restoreCartFromBackup();
                openCartDrawer();
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
        const wompiBtn = document.getElementById('cart-wompi-button');
        if (wompiBtn && wompiBtn.dataset.originalHtml) {
            wompiBtn.disabled = false;
            wompiBtn.innerHTML = wompiBtn.dataset.originalHtml;
        }
    }
}



// ============ MEJORA 2: DASHBOARD EJECUTIVO ADMIN ============
function switchAdminTab(tabName) {
    const tabs = ['dashboard', 'orders', 'products', 'services', 'bookings', 'admins', 'banners', 'history', 'settings', 'experiencias'];
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
    if (tabName === 'banners') bindBannerFormPreviewListeners();
    if (tabName === 'experiencias' && typeof loadAdminExperienciasList === 'function') loadAdminExperienciasList();
}

function updateBusinessConfigPreview() {
    const btnText = document.getElementById('admin-cfg-textoBotonHero')?.value || 'Ver Servicios & Agendar';

    const pEslogan = document.getElementById('preview-hero-tag');
    const pTitle = document.getElementById('preview-hero-title');
    const pDesc = document.getElementById('preview-hero-desc');
    const pBtn = document.getElementById('preview-hero-btn');

    if (pEslogan) pEslogan.textContent = 'FÓRMULAS ARTESANALES SIN QUÍMICOS AGRESIVOS';
    if (pTitle) {
        pTitle.innerHTML = 'Transforma tu cabello con <span class="gold-gradient-text">ISIVI</span> · Peluquería & Belleza en Cartagena';
    }
    if (pDesc) {
        pDesc.innerHTML = 'Nuestra línea capilar combina ingredientes naturales para devolverle la fuerza, el brillo y el crecimiento a tu cabello. Agenda tus servicios de peluquería con el <strong class="text-isivi-gold font-semibold">25% de anticipo</strong> o adquiere nuestros productos en detal y kits.';
    }
    if (pBtn) pBtn.textContent = btnText;
}

function renderAdminBusinessConfig() {
    const errorEl = document.getElementById('admin-business-config-error');
    const successEl = document.getElementById('admin-business-config-success');
    if (errorEl) errorEl.classList.add('hidden');
    if (successEl) successEl.classList.add('hidden');

    const inputs = {
        'admin-cfg-ciudad': businessConfiguration.ciudad || 'Cartagena',
        'admin-cfg-pais': businessConfiguration.pais || 'Colombia',
        'admin-cfg-dias': businessConfiguration.diasAtencion || 'Mar - Sáb',
        'admin-cfg-apertura': businessConfiguration.horaApertura || '08:00',
        'admin-cfg-cierre': businessConfiguration.horaCierre || '19:00',
        'admin-cfg-mayorista': businessConfiguration.telefonoMayorista || '+57 300 962 3174',
        'admin-cfg-tituloSitio': businessConfiguration.tituloSitio || 'ISIVI Beauty & Care',
        'admin-cfg-eslogan': businessConfiguration.eslogan || 'Belleza artesanal, hecha para ti',
        'admin-cfg-descripcion': businessConfiguration.descripcion || 'Descubre nuestros servicios, productos y kits de belleza.',
        'admin-cfg-tituloHero': businessConfiguration.tituloHero || 'Tu belleza, nuestra pasión',
        'admin-cfg-descripcionHero': businessConfiguration.descripcionHero || 'Servicios y productos seleccionados para cuidar de ti.',
        'admin-cfg-textoBotonHero': businessConfiguration.textoBotonHero || 'Descubrir servicios',
        'admin-cfg-nombreComercial': businessConfiguration.nombreComercial || 'ISIVI Beauty & Care',
        'admin-cfg-whatsapp': businessConfiguration.whatsapp || '+57 300 123 4567',
        'admin-cfg-direccion': businessConfiguration.direccion || 'Cartagena, Bolívar',
        'admin-cfg-horarioAtencion': businessConfiguration.horarioAtencion || 'Lunes a sábado · 9:00 AM – 7:00 PM',
        'admin-cfg-mensajeWhatsApp': businessConfiguration.mensajeWhatsApp || 'Hola, quiero obtener información sobre los servicios de ISIVI.',
        'admin-cfg-textoFooter': businessConfiguration.textoFooter || 'Belleza artesanal y experiencias diseñadas para ti.',
        'admin-cfg-numeroBancolombia': businessConfiguration.numeroBancolombia || '300-894-9050',
        'admin-cfg-titularBancolombia': businessConfiguration.titularBancolombia || 'ISIVI Belleza Natural',
        'admin-cfg-numeroNequi': businessConfiguration.numeroNequi || '3008949050',
        'admin-cfg-titularNequi': businessConfiguration.titularNequi || 'ISIVI Capilar',
        'admin-cfg-numeroDaviplata': businessConfiguration.numeroDaviplata || '3008949050',
        'admin-cfg-titularDaviplata': businessConfiguration.titularDaviplata || 'ISIVI Capilar'
    };

    for (const [id, value] of Object.entries(inputs)) {
        const el = document.getElementById(id);
        if (el) {
            el.value = value;
            if (!el.dataset.previewBound) {
                el.dataset.previewBound = "true";
                el.addEventListener('input', updateBusinessConfigPreview);
            }
        }
    }
    updateBusinessConfigPreview();

    // Cargar campos de notificaciones administrativas de WhatsApp
    const adminFields = [
        { id: 'admin-cfg-whatsappAdminNumero', key: 'whatsappAdminNumero', type: 'text', def: '' },
        { id: 'admin-cfg-whatsappAdminHabilitado', key: 'whatsappAdminHabilitado', type: 'checkbox', def: false },
        { id: 'admin-cfg-notificarNuevaCita', key: 'notificarNuevaCita', type: 'checkbox', def: true },
        { id: 'admin-cfg-notificarNuevaCompra', key: 'notificarNuevaCompra', type: 'checkbox', def: true },
        { id: 'admin-cfg-notificarSolicitudReprogramacion', key: 'notificarSolicitudReprogramacion', type: 'checkbox', def: true },
        { id: 'admin-cfg-notificarSolicitudCancelacion', key: 'notificarSolicitudCancelacion', type: 'checkbox', def: true },
        { id: 'admin-cfg-notificarAtencionAhora', key: 'notificarAtencionAhora', type: 'checkbox', def: true },
        { id: 'admin-cfg-notificarPagoAprobado', key: 'notificarPagoAprobado', type: 'checkbox', def: false },
        { id: 'admin-cfg-notificarPagoRechazado', key: 'notificarPagoRechazado', type: 'checkbox', def: false },
        { id: 'admin-cfg-notificarConflictoPago', key: 'notificarConflictoPago', type: 'checkbox', def: true }
    ];

    for (const field of adminFields) {
        const el = document.getElementById(field.id);
        if (el) {
            const val = businessConfiguration[field.key] !== undefined ? businessConfiguration[field.key] : field.def;
            if (field.type === 'checkbox') {
                el.checked = Boolean(val);
            } else {
                el.value = val;
            }
        }
    }
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

    const tituloSitio = (document.getElementById('admin-cfg-tituloSitio')?.value || '').trim();
    const eslogan = businessConfiguration.eslogan || 'Fórmulas Artesanales sin Químicos Agresivos';
    const descripcion = (document.getElementById('admin-cfg-descripcion')?.value || '').trim();
    const tituloHero = businessConfiguration.tituloHero || 'Transforma tu cabello con <span class="gold-gradient-text">ISIVI</span> · Peluquería & Belleza en Cartagena';
    const descripcionHero = businessConfiguration.descripcionHero || 'Nuestra línea capilar combina ingredientes naturales para devolverle la fuerza, el brillo y el crecimiento a tu cabello. Agenda tus servicios de peluquería con el 25% de anticipo o adquiere nuestros productos en detal y kits.';
    const textoBotonHero = (document.getElementById('admin-cfg-textoBotonHero')?.value || '').trim();
    const nombreComercial = (document.getElementById('admin-cfg-nombreComercial')?.value || '').trim();
    const whatsapp = (document.getElementById('admin-cfg-whatsapp')?.value || '').trim();
    const direccion = (document.getElementById('admin-cfg-direccion')?.value || '').trim();
    const horarioAtencion = (document.getElementById('admin-cfg-horarioAtencion')?.value || '').trim();
    const mensajeWhatsApp = (document.getElementById('admin-cfg-mensajeWhatsApp')?.value || '').trim();
    const textoFooter = (document.getElementById('admin-cfg-textoFooter')?.value || '').trim();

    const numeroBancolombia = (document.getElementById('admin-cfg-numeroBancolombia')?.value || '').trim();
    const titularBancolombia = (document.getElementById('admin-cfg-titularBancolombia')?.value || '').trim();
    const numeroNequi = (document.getElementById('admin-cfg-numeroNequi')?.value || '').trim();
    const titularNequi = (document.getElementById('admin-cfg-titularNequi')?.value || '').trim();
    const numeroDaviplata = (document.getElementById('admin-cfg-numeroDaviplata')?.value || '').trim();
    const titularDaviplata = (document.getElementById('admin-cfg-titularDaviplata')?.value || '').trim();

    const whatsappAdminNumero = (document.getElementById('admin-cfg-whatsappAdminNumero')?.value || '').trim();
    const whatsappAdminHabilitado = Boolean(document.getElementById('admin-cfg-whatsappAdminHabilitado')?.checked);
    const notificarNuevaCita = Boolean(document.getElementById('admin-cfg-notificarNuevaCita')?.checked);
    const notificarNuevaCompra = Boolean(document.getElementById('admin-cfg-notificarNuevaCompra')?.checked);
    const notificarSolicitudReprogramacion = Boolean(document.getElementById('admin-cfg-notificarSolicitudReprogramacion')?.checked);
    const notificarSolicitudCancelacion = Boolean(document.getElementById('admin-cfg-notificarSolicitudCancelacion')?.checked);
    const notificarAtencionAhora = Boolean(document.getElementById('admin-cfg-notificarAtencionAhora')?.checked);
    const notificarPagoAprobado = Boolean(document.getElementById('admin-cfg-notificarPagoAprobado')?.checked);
    const notificarPagoRechazado = Boolean(document.getElementById('admin-cfg-notificarPagoRechazado')?.checked);
    const notificarConflictoPago = Boolean(document.getElementById('admin-cfg-notificarConflictoPago')?.checked);

    if (whatsappAdminHabilitado) {
        if (!whatsappAdminNumero) {
            if (errorEl) {
                errorEl.querySelector('span').textContent = 'El número de WhatsApp del administrador es obligatorio si las notificaciones están habilitadas.';
                errorEl.classList.remove('hidden');
            }
            return;
        }
        const numClean = whatsappAdminNumero.replace(/\D/g, '');
        if (numClean.length < 7 || numClean.length > 15) {
            if (errorEl) {
                errorEl.querySelector('span').textContent = 'El número de WhatsApp del administrador debe tener entre 7 y 15 dígitos.';
                errorEl.classList.remove('hidden');
            }
            return;
        }
    }

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
        telefonoMayorista,
        tituloSitio,
        eslogan,
        descripcion,
        tituloHero,
        descripcionHero,
        textoBotonHero,
        nombreComercial,
        whatsapp,
        direccion,
        horarioAtencion,
        mensajeWhatsApp,
        textoFooter,
        numeroBancolombia,
        titularBancolombia,
        numeroNequi,
        titularNequi,
        numeroDaviplata,
        titularDaviplata,
        whatsappAdminNumero,
        whatsappAdminHabilitado,
        notificarNuevaCita,
        notificarNuevaCompra,
        notificarSolicitudReprogramacion,
        notificarSolicitudCancelacion,
        notificarAtencionAhora,
        notificarPagoAprobado,
        notificarPagoRechazado,
        notificarConflictoPago
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

async function probarWhatsAppAdmin() {
    const feedbackEl = document.getElementById('whatsapp-test-feedback');
    if (!feedbackEl) return;

    feedbackEl.textContent = 'Enviando...';
    feedbackEl.className = 'text-xs font-bold text-stone-400';
    feedbackEl.classList.remove('hidden');

    try {
        const res = await apiPost('/configuracion/whatsapp-test');
        if (res && res.ok) {
            feedbackEl.textContent = '✅ Mensaje de prueba enviado correctamente.';
            feedbackEl.className = 'text-xs font-bold text-emerald-400';
        } else {
            feedbackEl.textContent = '❌ ' + (res && res.mensaje ? res.mensaje : 'No fue posible enviar el mensaje.');
            feedbackEl.className = 'text-xs font-bold text-red-400';
        }
    } catch (err) {
        feedbackEl.textContent = '❌ ' + (err && err.message ? err.message : 'No fue posible enviar el mensaje.');
        feedbackEl.className = 'text-xs font-bold text-red-400';
    }
}


async function checkImagesPendingMigration() {
    try {
        const res = await apiPost('/admin/migrar-imagenes', { dryRun: true });
        const banner = document.getElementById('adm-dash-img-sync-banner');
        const textEl = document.getElementById('adm-dash-img-sync-text');
        if (banner && textEl) {
            if (res && res.totalPendientes > 0) {
                textEl.innerHTML = `⚠️ Hay <strong>${res.totalPendientes}</strong> imágenes guardadas temporalmente y pendientes de sincronización.`;
                banner.classList.remove('hidden');
            } else {
                banner.classList.add('hidden');
            }
        }
    } catch (err) {
        console.warn('Fallo al verificar imágenes pendientes de migración:', err);
    }
}

window.revisarImagenesTemporales = async function() {
    try {
        const res = await apiPost('/admin/migrar-imagenes', { dryRun: true });
        alert(`Imágenes guardadas temporalmente (Base64):\n\n` +
              `- Productos: ${res.productosPendientes}\n` +
              `- Kits: ${res.kitsPendientes}\n` +
              `- Servicios: ${res.serviciosPendientes}\n` +
              `- Banners: ${res.bannersPendientes}\n\n` +
              `Total: ${res.totalPendientes}`);
    } catch (err) {
        showToast('No se pudo comprobar el detalle de imágenes pendientes.', 'error');
    }
};

window.migrarImagenesCloudinary = async function() {
    const btn = document.getElementById('btn-migrar-imagenes');
    if (btn) {
        btn.disabled = true;
        btn.textContent = 'Migrando...';
    }
    showToast('Iniciando sincronización con Cloudinary...', 'info');
    try {
        const res = await apiPost('/admin/migrar-imagenes', { dryRun: false });
        const migrados = (res.productosMigrados || 0) + (res.kitsMigrados || 0) + (res.serviciosMigrados || 0) + (res.bannersMigrados || 0);
        showToast(`Migración completada. Sincronizados con éxito: ${migrados}.`, 'success');
        
        await loadDashboardData();
        
        // Recargar listas si estamos en esa pestaña
        if (document.getElementById('adm-tab-products') && !document.getElementById('adm-tab-products').classList.contains('hidden')) {
            await renderAdminProductsTable();
        }
        if (document.getElementById('adm-tab-banners') && !document.getElementById('adm-tab-banners').classList.contains('hidden')) {
            await renderAdminBanners();
        }
        if (document.getElementById('adm-tab-services') && !document.getElementById('adm-tab-services').classList.contains('hidden')) {
            if (typeof renderAdminServicesTable === 'function') {
                renderAdminServicesTable();
            }
        }
    } catch (err) {
        console.error(err);
        showToast(err.message || 'Error durante la sincronización a Cloudinary.', 'error');
    } finally {
        if (btn) {
            btn.disabled = false;
            btn.textContent = 'Migrar a Cloudinary';
        }
    }
};

async function loadDashboardData() {
    const refreshIcon = document.getElementById('dash-refresh-icon');
    const errorBanner = document.getElementById('adm-dash-error-banner');
    if (refreshIcon) refreshIcon.classList.add('fa-spin');
    if (errorBanner) errorBanner.classList.add('hidden');

    try {
        const data = await apiGet('/dashboard/resumen');
        renderAdminDashboard(data);
        await checkImagesPendingMigration();
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
        const historySearch = document.getElementById('history-filter-text');
        if (historySearch) {
            historySearch.value = targetCodigo || '';
            renderAdminHistory();
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
        const historySearch = document.getElementById('history-filter-text');
        if (historySearch) {
            historySearch.value = '';
            renderAdminHistory();
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

    const st = String(booking.status || booking.estado || '').toUpperCase();
    let msg = `Hola ${nombre}, te saludamos de ISIVI Salón.`;

    if (st.includes('CONFIRM')) {
        msg += ` Te confirmamos tu cita *${codigo}* para el día *${fecha} a las ${hora}* (${items}). ¡Te esperamos! ✨`;
    } else if (st.includes('CANCEL')) {
        msg += ` Te escribimos respecto a tu cita *${codigo}* para el día *${fecha}* (${items}), la cual se encuentra *cancelada*. ¿Deseas reagendar tu espacio? 📅`;
    } else if (st.includes('RECHAZ') || st.includes('DENEY') || st.includes('DENEG')) {
        msg += ` Te escribimos respecto a tu solicitud de reserva *${codigo}* (${items}), la cual ha sido denegada. ¿En qué podemos ayudarte?`;
    } else if (st.includes('PENDIENT') || st.includes('COMPROBANT')) {
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
    } else {
        msg += ` Te escribimos respecto a tu reserva *${codigo}* (${items}) programada para el *${fecha} a las ${hora}*. ¿En qué podemos ayudarte? 😊`;
    }

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

    if (!adminProductCategoriesData || adminProductCategoriesData.length === 0) {
        container.innerHTML = '<p class="text-xs text-isivi-300">Aún no hay categorías de productos. Añade una para clasificar tus productos.</p>';
        return;
    }

    container.innerHTML = adminProductCategoriesData.map(category => {
        const prodCount = productsData.filter(prod => prod.category === category.id).length;
        const kitCount = kitsData.filter(kit => kit.category === category.id).length;
        const count = prodCount + kitCount;
        return `<span class="inline-flex items-center gap-2 rounded-xl border border-stone-700 bg-stone-950 px-3 py-2 text-xs text-isivi-200"><span>${category.name} <span class="text-isivi-gold">(${count})</span></span><button type="button" onclick="deleteProductCategory('${category.id}', '${category.name.replace(/'/g, "\\'")}')" class="text-red-300 hover:text-red-200" title="Eliminar categoría"><i class="fa-solid fa-trash"></i></button></span>`;
    }).join('');
}

function populateProductCategorySelect(selectedId = '') {
    const select = document.getElementById('prod-category');
    if (!select) return;

    const itemType = document.getElementById('prod-type')?.value || 'product';
    const expectedType = itemType === 'kit' ? 'KIT' : 'PRODUCTO';

    const filteredCats = (adminProductCategoriesData || []).filter(cat => {
        return cat.tipo === expectedType || !cat.tipo;
    });

    const options = [
        '<option value="">Sin categoría asignada</option>',
        ...filteredCats.map(cat => `<option value="${cat.id}" ${selectedId === cat.id ? 'selected' : ''}>${cat.name}</option>`)
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

    const typeSelect = document.getElementById('product-category-type');
    const tipo = typeSelect ? typeSelect.value : 'PRODUCTO';

    try {
        const nueva = await apiPost('/categorias-producto', { nombre: name, tipo: tipo });
        const mappedNueva = mapFromApiCategoriaProducto(nueva);
        adminProductCategoriesData.push(mappedNueva);
        if (mappedNueva.activo !== false) {
            productCategoriesData.push(mappedNueva);
        }
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
        const mapped = mapFromApiCategoriaProducto(actualizada);
        
        const idx = adminProductCategoriesData.findIndex(c => c.id === id);
        if (idx >= 0) adminProductCategoriesData[idx] = mapped;

        const idxPublic = productCategoriesData.findIndex(c => c.id === id);
        if (mapped.activo !== false) {
            if (idxPublic >= 0) {
                productCategoriesData[idxPublic] = mapped;
            } else {
                productCategoriesData.push(mapped);
            }
        } else {
            if (idxPublic >= 0) {
                productCategoriesData.splice(idxPublic, 1);
            }
        }
        
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
    const cat = adminProductCategoriesData.find(c => c.id === id);
    const catName = name || (cat ? cat.name : 'esta categoría');
    if (!confirm(`¿Deseas eliminar la categoría "${catName}"?`)) return;

    try {
        await apiDelete(`/categorias-producto/${id}`);
        adminProductCategoriesData = adminProductCategoriesData.filter(c => c.id !== id);
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

    const currentSelectedCat = document.getElementById('prod-category')?.value || '';
    populateProductCategorySelect(currentSelectedCat);

    if (catWrap) catWrap.classList.remove('hidden');
    if (priceTypeWrap) priceTypeWrap.classList.toggle('hidden', isKit);

    const oldPriceLabel = document.getElementById('prod-old-price-label');
    const oldPriceHelp = document.getElementById('prod-old-price-help');
    const oldPriceInput = document.getElementById('prod-old-price');

    if (isKit) {
        if (oldPriceLabel) oldPriceLabel.textContent = 'Valor Individual Total ($ COP)';
        if (oldPriceHelp) oldPriceHelp.textContent = 'Suma de los productos por separado. Muestra "Valor indiv.: $110.000" tachado.';
        if (oldPriceInput) oldPriceInput.placeholder = 'Ej: 110.000 (Suma por separado)';

        const varSection = document.getElementById('prod-variants-section');
        const singlePriceWrap = document.getElementById('prod-single-price-wrap');
        const singleQtyWrap = document.getElementById('prod-single-qty-wrap');
        if (varSection) varSection.classList.add('hidden');
        if (singlePriceWrap) singlePriceWrap.classList.remove('hidden');
        if (singleQtyWrap) singleQtyWrap.classList.remove('hidden');
    } else {
        if (oldPriceLabel) oldPriceLabel.textContent = 'Precio Antes / Regular ($ COP)';
        if (oldPriceHelp) oldPriceHelp.textContent = 'Precio habitual antes de la oferta. Muestra "Antes: $110.000" tachado.';
        if (oldPriceInput) oldPriceInput.placeholder = 'Ej: 110.000 (Precio regular)';

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
            const cat = adminProductCategoriesData.find(c => String(c.id) === String(catId));
            return {
                ...p,
                typeLabel: 'Producto Detal',
                categoryLabel: cat ? cat.name : (p.categoryName || (catId ? String(catId) : 'Sin categoría asignada'))
            };
        }),
        ...kitsData.map(k => {
            const catId = k.category || k.categoriaId;
            const cat = adminProductCategoriesData.find(c => String(c.id) === String(catId));
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

function getImageOriginBadge(origen) {
    if (origen === 'CLOUDINARY') {
        return `<span class="px-1.5 py-0.5 rounded text-[9px] font-bold bg-stone-850 text-stone-400 border border-stone-800">Cloudinary</span>`;
    }
    if (origen === 'BASE64') {
        return `<span class="px-1.5 py-0.5 rounded text-[9px] font-bold bg-amber-950 text-amber-300 border border-amber-500/30">⚠️ Temporal</span>`;
    }
    return `<span class="px-1.5 py-0.5 rounded text-[9px] font-bold bg-blue-950 text-blue-300 border border-blue-500/30">Revisar</span>`;
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
                        ${getImageOriginBadge(item.origenImagen)}
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

function updateProductImagePreview() {
    const fit = document.getElementById('prod-fit')?.value || 'cover';
    const zoom = parseFloat(document.getElementById('prod-zoom')?.value || 1.0);
    const posX = parseInt(document.getElementById('prod-pos-x')?.value || 50, 10);
    const posY = parseInt(document.getElementById('prod-pos-y')?.value || 50, 10);
    
    const zoomVal = document.getElementById('prod-zoom-val');
    const posXVal = document.getElementById('prod-pos-x-val');
    const posYVal = document.getElementById('prod-pos-y-val');
    
    if (zoomVal) zoomVal.textContent = zoom.toFixed(2) + 'x';
    if (posXVal) posXVal.textContent = posX + '%';
    if (posYVal) posYVal.textContent = posY + '%';
    
    const previewImg = document.getElementById('prod-preview-img');
    if (previewImg) {
        previewImg.style.objectFit = fit;
        previewImg.style.transform = `scale(${zoom})`;
        previewImg.style.transformOrigin = `${posX}% ${posY}%`;
        previewImg.style.objectPosition = `${posX}% ${posY}%`;
    }
}

function setProductFitPreset(fit) {
    const fitSelect = document.getElementById('prod-fit');
    const zoomInput = document.getElementById('prod-zoom');
    const posXInput = document.getElementById('prod-pos-x');
    const posYInput = document.getElementById('prod-pos-y');
    if (fitSelect) fitSelect.value = fit;
    if (zoomInput) zoomInput.value = fit === 'contain' ? 0.95 : 1.0;
    if (posXInput) posXInput.value = 50;
    if (posYInput) posYInput.value = 50;
    updateProductImagePreview();
}

function centerProductImage() {
    const posXInput = document.getElementById('prod-pos-x');
    const posYInput = document.getElementById('prod-pos-y');
    if (posXInput) posXInput.value = 50;
    if (posYInput) posYInput.value = 50;
    updateProductImagePreview();
}

function resetProductImage() {
    const fitSelect = document.getElementById('prod-fit');
    const zoomInput = document.getElementById('prod-zoom');
    const posXInput = document.getElementById('prod-pos-x');
    const posYInput = document.getElementById('prod-pos-y');
    if (fitSelect) fitSelect.value = 'cover';
    if (zoomInput) zoomInput.value = 1.0;
    if (posXInput) posXInput.value = 50;
    if (posYInput) posYInput.value = 50;
    updateProductImagePreview();
}

function updateServiceImagePreview() {
    const fit = document.getElementById('serv-fit')?.value || 'cover';
    const zoom = parseFloat(document.getElementById('serv-zoom')?.value || 1.0);
    const posX = parseInt(document.getElementById('serv-pos-x')?.value || 50, 10);
    const posY = parseInt(document.getElementById('serv-pos-y')?.value || 50, 10);
    
    const zoomVal = document.getElementById('serv-zoom-val');
    const posXVal = document.getElementById('serv-pos-x-val');
    const posYVal = document.getElementById('serv-pos-y-val');
    
    if (zoomVal) zoomVal.textContent = zoom.toFixed(2) + 'x';
    if (posXVal) posXVal.textContent = posX + '%';
    if (posYVal) posYVal.textContent = posY + '%';
    
    const previewImg = document.getElementById('serv-preview-img');
    if (previewImg) {
        previewImg.style.objectFit = fit;
        previewImg.style.transform = `scale(${zoom})`;
        previewImg.style.transformOrigin = `${posX}% ${posY}%`;
        previewImg.style.objectPosition = `${posX}% ${posY}%`;
    }
}

function setServiceFitPreset(fit) {
    const fitSelect = document.getElementById('serv-fit');
    const zoomInput = document.getElementById('serv-zoom');
    const posXInput = document.getElementById('serv-pos-x');
    const posYInput = document.getElementById('serv-pos-y');
    if (fitSelect) fitSelect.value = fit;
    if (zoomInput) zoomInput.value = fit === 'contain' ? 0.95 : 1.0;
    if (posXInput) posXInput.value = 50;
    if (posYInput) posYInput.value = 50;
    updateServiceImagePreview();
}

function centerServiceImage() {
    const posXInput = document.getElementById('serv-pos-x');
    const posYInput = document.getElementById('serv-pos-y');
    if (posXInput) posXInput.value = 50;
    if (posYInput) posYInput.value = 50;
    updateServiceImagePreview();
}

function resetServiceImage() {
    const fitSelect = document.getElementById('serv-fit');
    const zoomInput = document.getElementById('serv-zoom');
    const posXInput = document.getElementById('serv-pos-x');
    const posYInput = document.getElementById('serv-pos-y');
    if (fitSelect) fitSelect.value = 'cover';
    if (zoomInput) zoomInput.value = 1.0;
    if (posXInput) posXInput.value = 50;
    if (posYInput) posYInput.value = 50;
    updateServiceImagePreview();
}

// Hook typing events to sync other preview card details dynamically
document.addEventListener('DOMContentLoaded', () => {
    function checkPreviewOverflow(descId, btnId) {
        requestAnimationFrame(() => {
            const descEl = document.getElementById(descId);
            const btnEl = document.getElementById(btnId);
            if (descEl && btnEl) {
                const overflows = descEl.scrollHeight > descEl.clientHeight + 1;
                if (overflows) btnEl.classList.remove('hidden');
                else btnEl.classList.add('hidden');
            }
        });
    }

    // Product Preview Event Listeners
    document.getElementById('prod-name')?.addEventListener('input', (e) => {
        const el = document.getElementById('prod-preview-title');
        if (el) el.textContent = e.target.value.trim() || 'Nombre de Ejemplo';
    });
    document.getElementById('prod-desc')?.addEventListener('input', (e) => {
        const el = document.getElementById('prod-preview-desc');
        if (el) {
            el.textContent = e.target.value.trim() || 'Descripción breve de ejemplo.';
            checkPreviewOverflow('prod-preview-desc', 'ver-mas-btn-prod-preview');
        }
    });
    document.getElementById('prod-price')?.addEventListener('input', (e) => {
        const el = document.getElementById('prod-preview-price-val');
        if (el) {
            const parsed = parsePrice(e.target.value);
            el.textContent = isNaN(parsed) ? '$0' : `$${parsed.toLocaleString('es-CO')}`;
        }
    });
    document.getElementById('prod-img')?.addEventListener('change', async (e) => {
        if (e.target.files.length > 0) {
            try {
                const url = await readImageFile(e.target.files[0]);
                const imgEl = document.getElementById('prod-preview-img');
                if (imgEl) imgEl.src = url;
            } catch(err) {}
        }
    });
    
    // Service Preview Event Listeners
    document.getElementById('serv-name')?.addEventListener('input', (e) => {
        const el = document.getElementById('serv-preview-title');
        if (el) el.textContent = e.target.value.trim() || 'Nombre del Servicio';
    });
    document.getElementById('serv-desc')?.addEventListener('input', (e) => {
        const el = document.getElementById('serv-preview-desc');
        if (el) {
            el.textContent = e.target.value.trim() || 'Descripción breve de ejemplo.';
            checkPreviewOverflow('serv-preview-desc', 'ver-mas-btn-serv-preview');
        }
    });
    document.getElementById('serv-price')?.addEventListener('input', (e) => {
        const el = document.getElementById('serv-preview-price-val');
        const depositEl = document.getElementById('serv-preview-deposit-val');
        if (el) {
            const parsed = parsePrice(e.target.value);
            const priceVal = isNaN(parsed) ? 0 : parsed;
            el.textContent = `$${priceVal.toLocaleString('es-CO')}`;
            if (depositEl) {
                const deposit = Math.round(priceVal * 0.25);
                depositEl.textContent = `$${deposit.toLocaleString('es-CO')}`;
            }
        }
    });
    document.getElementById('serv-duration')?.addEventListener('input', (e) => {
        const el = document.getElementById('serv-preview-duration-val');
        if (el) el.textContent = e.target.value.trim() || '60 min';
    });
    document.getElementById('serv-img')?.addEventListener('change', async (e) => {
        if (e.target.files.length > 0) {
            try {
                const url = await readImageFile(e.target.files[0]);
                const imgEl = document.getElementById('serv-preview-img');
                if (imgEl) imgEl.src = url;
            } catch(err) {}
        }
    });
});

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
    let rawOldPrice = parsePrice(document.getElementById('prod-old-price')?.value);
    let oldPrice = (!isNaN(rawOldPrice) && rawOldPrice > 0) ? rawOldPrice : null;
    let quantity = parseInt(document.getElementById('prod-quantity').value, 10);
    const imageInput = document.getElementById('prod-img');
    const desc = document.getElementById('prod-desc').value.trim();
    const inStock = document.getElementById('prod-stock').value === 'true';

    const fit = document.getElementById('prod-fit')?.value || 'cover';
    const zoom = parseFloat(document.getElementById('prod-zoom')?.value || 1.0);
    const posX = parseInt(document.getElementById('prod-pos-x')?.value || 50, 10);
    const posY = parseInt(document.getElementById('prod-pos-y')?.value || 50, 10);

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

    const targetEditIdStr = String(editId);
    const existingItem = editId
        ? [...productsData, ...kitsData].find(item => String(item.id) === targetEditIdStr)
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
        oldPrice,
        img,
        desc,
        inStock,
        quantity,
        category: categoryId || null,
        priceType: type === 'kit' ? 'UNICO' : priceType,
        variants: type === 'kit' ? [] : (priceType === 'VARIANTES' ? currentEditingVariants : []),
        imageFit: fit,
        imageZoom: zoom,
        imagePosX: posX,
        imagePosY: posY
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

async function compressImage(file, maxWidth = 1200, maxHeight = 1200, quality = 0.8) {
    return new Promise((resolve) => {
        const reader = new FileReader();
        reader.onload = function (event) {
            const img = new Image();
            img.onload = function () {
                const canvas = document.createElement('canvas');
                let width = img.width;
                let height = img.height;

                if (width > height) {
                    if (width > maxWidth) {
                        height = Math.round((height * maxWidth) / width);
                        width = maxWidth;
                    }
                } else {
                    if (height > maxHeight) {
                        width = Math.round((width * maxHeight) / height);
                        height = maxHeight;
                    }
                }

                canvas.width = width;
                canvas.height = height;
                const ctx = canvas.getContext('2d');
                ctx.drawImage(img, 0, 0, width, height);

                canvas.toBlob((blob) => {
                    if (!blob) {
                        resolve(file);
                        return;
                    }
                    const compressedFile = new File([blob], file.name.substring(0, file.name.lastIndexOf('.')) + '.jpg', {
                        type: 'image/jpeg',
                        lastModified: Date.now()
                    });
                    resolve(compressedFile);
                }, 'image/jpeg', quality);
            };
            img.onerror = () => resolve(file);
            img.src = event.target.result;
        };
        reader.onerror = () => resolve(file);
        reader.readAsDataURL(file);
    });
}

async function readImageFile(file) {
    showToast('Procesando y optimizando imagen para datos móviles...', 'info');
    try {
        if (file && file.type.startsWith('image/')) {
            file = await compressImage(file);
        }
    } catch (compressErr) {
        console.warn('Fallo al comprimir la imagen:', compressErr);
    }

    showToast('Subiendo imagen a la nube...', 'info');
    try {
        const formData = new FormData();
        formData.append('file', file);
        const headers = getAuthHeaders();
        const res = await fetchWithTimeout(`${API_BASE}/uploads/imagen`, {
            method: 'POST',
            headers: headers,
            body: formData
        }, 60000); // 60 segundos de timeout para soportar datos móviles lentos
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
        showToast('⚠️ Imagen guardada temporalmente y pendiente de sincronización.', 'warning');
        return new Promise((resolve, reject) => {
            const reader = new FileReader();
            reader.onload = () => resolve(reader.result);
            reader.onerror = () => reject(reader.error);
            reader.readAsDataURL(file);
        });
    }
}


function editProduct(id, type) {
    const targetIdStr = String(id);
    const item = (type === 'kit') ? kitsData.find(k => String(k.id) === targetIdStr) : productsData.find(p => String(p.id) === targetIdStr);
    if (!item) return;

    document.getElementById('prod-edit-id').value = item.id;
    document.getElementById('prod-name').value = item.name;
    document.getElementById('prod-type').value = item.type || type;
    document.getElementById('prod-price').value = formatPriceNumber(item.price);
    if (document.getElementById('prod-old-price')) document.getElementById('prod-old-price').value = item.oldPrice ? formatPriceNumber(item.oldPrice) : '';
    document.getElementById('prod-quantity').value = item.quantity ?? 0;
    document.getElementById('prod-img').value = '';
    document.getElementById('prod-img-help').textContent = 'Imagen actual conservada. Selecciona otra solo si deseas reemplazarla (JPG, PNG o WebP; máximo 5 MB).';
    document.getElementById('prod-desc').value = item.desc || '';
    document.getElementById('prod-stock').value = (item.inStock !== false) ? 'true' : 'false';

    const priceTypeSelect = document.getElementById('prod-price-type');
    if (priceTypeSelect) priceTypeSelect.value = item.priceType || 'UNICO';

    currentEditingVariants = Array.isArray(item.variants) ? item.variants.map(v => ({ ...v })) : [];

    onProductTypeChange();

    const catSelect = document.getElementById('prod-category');
    if (catSelect) catSelect.value = item.category || '';

    if (document.getElementById('prod-fit')) document.getElementById('prod-fit').value = item.imageFit || 'cover';
    document.getElementById('prod-zoom').value = item.imageZoom || 1.0;
    document.getElementById('prod-pos-x').value = item.imagePosX || 50;
    document.getElementById('prod-pos-y').value = item.imagePosY || 50;
    document.getElementById('prod-preview-img').src = item.img || '/images/isivi-logo-transparent.png';
    updateProductImagePreview();

    document.getElementById('prod-form-title').innerHTML = `<i class="fa-solid fa-pen-to-square"></i> Editando: ${item.name}`;
    document.getElementById('cancel-prod-edit-btn').classList.remove('hidden');
    window.scrollTo({ top: 0, behavior: 'smooth' });
}

function resetProductForm() {
    document.getElementById('prod-edit-id').value = '';
    document.getElementById('product-editor-form').reset();
    document.getElementById('prod-quantity').value = 1;
    document.getElementById('prod-price-type').value = 'UNICO';
    if (document.getElementById('prod-old-price')) document.getElementById('prod-old-price').value = '';
    currentEditingVariants = [];
    onProductTypeChange();

    if (document.getElementById('prod-fit')) document.getElementById('prod-fit').value = 'cover';
    document.getElementById('prod-zoom').value = 1.0;
    document.getElementById('prod-pos-x').value = 50;
    document.getElementById('prod-pos-y').value = 50;
    document.getElementById('prod-preview-img').src = '/images/isivi-logo-transparent.png';
    updateProductImagePreview();

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

function checkBannerDestinoInvalido(banner) {
    if (!banner.tipoDestino || banner.tipoDestino === 'NINGUNO') return null;
    if (banner.tipoDestino === 'SECCION') {
        const sections = ['#servicios', '#productos', '#kits', '#nosotros'];
        if (!sections.includes(banner.destinoId)) return 'Sección no disponible';
        return null;
    }
    if (banner.tipoDestino === 'PRODUCTO') {
        const found = productsData.some(p => p.id === banner.destinoId);
        if (!found) return '⚠️ El destino de este banner ya no está disponible (Producto eliminado)';
        return null;
    }
    if (banner.tipoDestino === 'KIT') {
        const found = kitsData.some(k => k.id === banner.destinoId);
        if (!found) return '⚠️ El destino de este banner ya no está disponible (Kit eliminado)';
        return null;
    }
    if (banner.tipoDestino === 'SERVICIO') {
        const found = servicesData.some(s => s.id === banner.destinoId);
        if (!found) return '⚠️ El destino de este banner ya no está disponible (Servicio eliminado)';
        return null;
    }
    if (banner.tipoDestino === 'CATEGORIA') {
        let found = false;
        if (banner.destinoId && banner.destinoId.startsWith('SERVICIOCAT_')) {
            const catId = banner.destinoId.replace('SERVICIOCAT_', '');
            found = serviceCategoriesData.some(c => c.id === catId);
        } else if (banner.destinoId && banner.destinoId.startsWith('PRODUCTOCAT_')) {
            const catId = banner.destinoId.replace('PRODUCTOCAT_', '');
            found = adminProductCategoriesData.some(c => c.id === catId);
        }
        if (!found) return '⚠️ El destino de este banner ya no está disponible (Categoría eliminada)';
        return null;
    }
    return null;
}

function handleBannerTargetTypeChange() {
    const typeSelect = document.getElementById('banner-target-type');
    const container = document.getElementById('banner-target-selector-container');
    const label = document.getElementById('banner-target-id-label');
    const select = document.getElementById('banner-target-id');
    const helpText = document.getElementById('banner-target-help-text');

    if (!typeSelect || !container || !select) return;

    const val = typeSelect.value;
    if (val === 'NINGUNO') {
        container.classList.add('hidden');
        select.innerHTML = '';
        helpText.textContent = '';
        return;
    }

    container.classList.remove('hidden');
    select.innerHTML = '';

    if (val === 'SECCION') {
        label.textContent = 'Seleccionar Sección *';
        const sections = [
            { id: '#servicios', name: 'Servicios de Peluquería' },
            { id: '#productos', name: 'Productos en Detal' },
            { id: '#kits', name: 'Kits Especiales' },
            { id: '#nosotros', name: 'Nosotros & Contacto' }
        ];
        select.innerHTML = sections.map(s => `<option value="${s.id}">${s.name}</option>`).join('');
        helpText.textContent = 'El botón llevará directamente a la sección seleccionada.';
    } else if (val === 'PRODUCTO') {
        label.textContent = 'Seleccionar Producto *';
        select.innerHTML = productsData.map(p => `<option value="${p.id}">${p.name} - $${p.price.toLocaleString()}</option>`).join('') || '<option value="">No hay productos cargados</option>';
        helpText.textContent = 'El botón llevará directamente al producto seleccionado.';
    } else if (val === 'KIT') {
        label.textContent = 'Seleccionar Kit *';
        select.innerHTML = kitsData.map(k => `<option value="${k.id}">${k.name} - $${k.price.toLocaleString()}</option>`).join('') || '<option value="">No hay kits cargados</option>';
        helpText.textContent = 'El botón llevará directamente al kit seleccionado.';
    } else if (val === 'SERVICIO') {
        label.textContent = 'Seleccionar Servicio *';
        select.innerHTML = servicesData.map(s => `<option value="${s.id}">${s.name} - $${s.price.toLocaleString()}</option>`).join('') || '<option value="">No hay servicios cargados</option>';
        helpText.textContent = 'El botón abrirá directamente este servicio.';
    } else if (val === 'CATEGORIA') {
        label.textContent = 'Seleccionar Categoría *';
        const cats = [
            ...serviceCategoriesData.map(c => ({ id: `SERVICIOCAT_${c.id}`, name: `[Servicio] ${c.name}` })),
            ...adminProductCategoriesData.map(c => ({ id: `PRODUCTOCAT_${c.id}`, name: `[Producto/Kit] ${c.name}` }))
        ];
        select.innerHTML = cats.map(c => `<option value="${c.id}">${c.name}</option>`).join('') || '<option value="">No hay categorías cargadas</option>';
        helpText.textContent = 'El botón filtrará el catálogo por la categoría seleccionada.';
    }
}

function updateBannerFormPreview() {
    const title = document.getElementById('banner-title')?.value || 'Título del banner';
    const desc = document.getElementById('banner-description')?.value || 'Descripción breve de la promoción';
    const btnText = document.getElementById('banner-button-text')?.value || 'Ver promoción';

    const pTitle = document.getElementById('preview-banner-title');
    const pDesc = document.getElementById('preview-banner-desc');
    const pBtn = document.getElementById('preview-banner-btn');

    if (pTitle) pTitle.textContent = title;
    if (pDesc) pDesc.textContent = desc;
    if (pBtn) pBtn.textContent = btnText;
}

window.updateBannerPreviewStyle = function() {
    const zoom = parseFloat(document.getElementById('banner-zoom')?.value || 1.0);
    const posX = parseInt(document.getElementById('banner-pos-x')?.value || 50, 10);
    const posY = parseInt(document.getElementById('banner-pos-y')?.value || 50, 10);
    const previewImg = document.getElementById('preview-banner-image');
    if (previewImg) {
        previewImg.style.transform = `scale(${zoom})`;
        previewImg.style.transformOrigin = `${posX}% ${posY}%`;
        previewImg.style.objectPosition = `${posX}% ${posY}%`;
    }
};

window.centerBannerImage = function() {
    const zoom = document.getElementById('banner-zoom');
    const posX = document.getElementById('banner-pos-x');
    const posY = document.getElementById('banner-pos-y');
    if (zoom) zoom.value = 1.0;
    if (posX) posX.value = 50;
    if (posY) posY.value = 50;
    updateBannerPreviewStyle();
};

window.resetBannerImage = function() {
    centerBannerImage();
};

function bindBannerFormPreviewListeners() {
    const titleEl = document.getElementById('banner-title');
    const descEl = document.getElementById('banner-description');
    const btnTextEl = document.getElementById('banner-button-text');
    const imgEl = document.getElementById('banner-image');

    const bZoom = document.getElementById('banner-zoom');
    const bPosX = document.getElementById('banner-pos-x');
    const bPosY = document.getElementById('banner-pos-y');

    if (bZoom && !bZoom.dataset.previewBound) {
        bZoom.dataset.previewBound = "true";
        bZoom.addEventListener('input', updateBannerPreviewStyle);
    }
    if (bPosX && !bPosX.dataset.previewBound) {
        bPosX.dataset.previewBound = "true";
        bPosX.addEventListener('input', updateBannerPreviewStyle);
    }
    if (bPosY && !bPosY.dataset.previewBound) {
        bPosY.dataset.previewBound = "true";
        bPosY.addEventListener('input', updateBannerPreviewStyle);
    }

    if (titleEl && !titleEl.dataset.previewBound) {
        titleEl.dataset.previewBound = "true";
        titleEl.addEventListener('input', updateBannerFormPreview);
    }
    if (descEl && !descEl.dataset.previewBound) {
        descEl.dataset.previewBound = "true";
        descEl.addEventListener('input', updateBannerFormPreview);
    }
    if (btnTextEl && !btnTextEl.dataset.previewBound) {
        btnTextEl.dataset.previewBound = "true";
        btnTextEl.addEventListener('input', updateBannerFormPreview);
    }
    if (imgEl && !imgEl.dataset.previewBound) {
        imgEl.dataset.previewBound = "true";
        imgEl.addEventListener('change', async (e) => {
            const file = e.target.files[0];
            if (file) {
                try {
                    const dataUrl = await readImageFile(file);
                    const pImg = document.getElementById('preview-banner-image');
                    const placeholder = document.getElementById('preview-banner-image-placeholder');
                    if (pImg) {
                        pImg.src = dataUrl;
                        pImg.classList.remove('hidden');
                    }
                    if (placeholder) placeholder.classList.add('hidden');
                    updateBannerPreviewStyle();
                } catch (err) {
                    console.warn(err);
                }
            }
        });
    }
    updateBannerFormPreview();
    updateBannerPreviewStyle();
    handleBannerTargetTypeChange();
}

async function renderAdminBanners() {
    const container = document.getElementById('adm-banners-list');
    if (!container) return;
    container.innerHTML = '<p class="text-xs text-isivi-300">Cargando banners...</p>';
    try {
        const banners = await apiGet('/banners');
        bannersData = banners.map(mapFromApiBanner);
        renderClientBanners();
        container.innerHTML = bannersData.length ? bannersData.map(banner => {
            const warning = checkBannerDestinoInvalido(banner);
            const warningHtml = warning ? `<div class="mt-2 text-[10px] text-amber-400 bg-amber-950/40 border border-amber-500/30 p-2 rounded-lg flex items-center gap-1.5"><i class="fa-solid fa-triangle-exclamation"></i> <span>${warning}</span></div>` : '';
            const actionBtn = warning && banner.active ? `<button type="button" onclick="toggleBanner('${banner.id}')" class="rounded-lg bg-amber-900 text-amber-100 px-3 py-1.5 text-[10px] font-bold hover:bg-amber-800">Desactivar banner</button>` : '';

            return `
            <article class="overflow-hidden rounded-2xl border border-stone-800 bg-stone-950">
                <img src="${banner.image}" alt="${banner.title}" class="h-32 w-full object-cover">
                <div class="p-4">
                    <div class="flex items-start justify-between gap-3">
                        <div>
                            <h4 class="font-bold text-white">${banner.title}</h4>
                            <p class="mt-1 text-xs text-isivi-300 mb-1.5">${banner.description}</p>
                            <div class="flex gap-1.5 flex-wrap items-center mt-1">
                                ${getImageOriginBadge(banner.origenImagen)}
                            </div>
                            ${warningHtml}
                        </div>
                        <span class="rounded-full px-2 py-1 text-[9px] font-bold ${banner.active ? 'bg-emerald-950 text-emerald-300' : 'bg-stone-800 text-stone-400'}">${banner.active ? 'VISIBLE' : 'OCULTO'}</span>
                    </div>
                    <div class="mt-4 flex gap-2 flex-wrap">
                        <button type="button" onclick="toggleBanner('${banner.id}')" class="rounded-lg bg-stone-800 px-3 py-1.5 text-[10px] font-bold text-isivi-gold hover:bg-stone-700">${banner.active ? 'Ocultar' : 'Mostrar'}</button>
                        ${actionBtn}
                        <button type="button" onclick="editBanner('${banner.id}')" class="rounded-lg bg-blue-950 px-3 py-1.5 text-[10px] font-bold text-blue-300 hover:bg-blue-900"><i class="fa-solid fa-pen-to-square mr-1"></i>Editar</button>
                        <button type="button" onclick="deleteBanner('${banner.id}', '${banner.title.replace(/'/g, "\\'")}')" class="rounded-lg bg-red-950 px-3 py-1.5 text-[10px] font-bold text-red-300 hover:bg-red-900"><i class="fa-solid fa-trash mr-1"></i>Eliminar</button>
                    </div>
                </div>
            </article>`;
        }).join('') : '<p class="text-xs text-isivi-300">Aún no hay banners creados.</p>';
    } catch (err) {
        console.error(err);
        container.innerHTML = '<p class="text-xs text-red-300">No se pudieron cargar los banners.</p>';
    }
}

async function handleBannerSubmit(event) {
    event.preventDefault();
    const editId = document.getElementById('banner-edit-id').value;
    const imageInput = document.getElementById('banner-image');
    const imageFile = imageInput.files[0];
    
    let imagenUrl = '';
    if (editId) {
        const existing = bannersData.find(b => b.id === editId);
        if (existing) {
            imagenUrl = existing.image;
        }
    }

    if (imageFile) {
        if (!['image/jpeg', 'image/png', 'image/webp'].includes(imageFile.type)) {
            showToast('Selecciona una imagen JPG, PNG o WebP', 'error');
            return;
        }
        if (imageFile.size > 5 * 1024 * 1024) {
            showToast('La imagen no puede superar los 5 MB', 'error');
            return;
        }
        try {
            imagenUrl = await readImageFile(imageFile);
        } catch (err) {
            console.error(err);
            showToast('No se pudo leer la imagen seleccionada', 'error');
            return;
        }
    } else if (!editId) {
        showToast('Selecciona una imagen para el banner', 'error');
        return;
    }

    const payload = {
        titulo: document.getElementById('banner-title').value.trim(),
        descripcion: document.getElementById('banner-description').value.trim(),
        imagenUrl,
        textoBoton: document.getElementById('banner-button-text').value.trim() || 'Conocer más',
        enlaceBoton: '#',
        activo: editId ? (bannersData.find(b => b.id === editId)?.active !== false) : true,
        tipoDestino: document.getElementById('banner-target-type')?.value || 'NINGUNO',
        destinoId: document.getElementById('banner-target-id')?.value || null,
        imageZoom: parseFloat(document.getElementById('banner-zoom')?.value || 1.0),
        imagePosX: parseFloat(document.getElementById('banner-pos-x')?.value || 50.0),
        imagePosY: parseFloat(document.getElementById('banner-pos-y')?.value || 50.0)
    };

    try {
        if (editId) {
            await apiPut(`/banners/${editId}`, payload);
            showToast('Banner actualizado correctamente', 'success');
        } else {
            await apiPost('/banners', payload);
            showToast('Banner creado y publicado', 'success');
        }
        resetBannerForm();
        await renderAdminBanners();
    } catch (err) {
        console.error(err);
        showToast(editId ? 'No se pudo actualizar el banner' : 'No se pudo crear el banner', 'error');
    }
}

window.editBanner = function(id) {
    const banner = bannersData.find(b => b.id === id);
    if (!banner) return;

    document.getElementById('banner-edit-id').value = banner.id;
    document.getElementById('banner-title').value = banner.title;
    document.getElementById('banner-description').value = banner.description;
    document.getElementById('banner-button-text').value = banner.buttonText;
    document.getElementById('banner-target-type').value = banner.tipoDestino;
    
    handleBannerTargetTypeChange();
    
    if (banner.destinoId) {
        document.getElementById('banner-target-id').value = banner.destinoId;
    }

    const zoomInput = document.getElementById('banner-zoom');
    const posXInput = document.getElementById('banner-pos-x');
    const posYInput = document.getElementById('banner-pos-y');
    if (zoomInput) zoomInput.value = banner.imageZoom;
    if (posXInput) posXInput.value = banner.imagePosX;
    if (posYInput) posYInput.value = banner.imagePosY;

    const zVal = document.getElementById('banner-zoom-val');
    const xVal = document.getElementById('banner-pos-x-val');
    const yVal = document.getElementById('banner-pos-y-val');
    if (zVal) zVal.textContent = parseFloat(banner.imageZoom).toFixed(1) + 'x';
    if (xVal) xVal.textContent = banner.imagePosX + '%';
    if (yVal) yVal.textContent = banner.imagePosY + '%';

    const pImg = document.getElementById('preview-banner-image');
    const placeholder = document.getElementById('preview-banner-image-placeholder');
    if (pImg && banner.image) {
        pImg.src = banner.image;
        pImg.classList.remove('hidden');
        updateBannerPreviewStyle();
    }
    if (placeholder) placeholder.classList.add('hidden');

    document.getElementById('banner-image').removeAttribute('required');

    document.getElementById('banner-form-title').innerHTML = `<i class="fa-solid fa-pen-to-square mr-2"></i>Editando banner: ${banner.title}`;
    document.getElementById('banner-submit-btn').innerHTML = `<i class="fa-solid fa-floppy-disk mr-1"></i>Guardar cambios`;
    document.getElementById('cancel-banner-edit-btn').classList.remove('hidden');

    const formContainer = document.getElementById('banner-form-title')?.closest('.rounded-3xl');
    if (formContainer) {
        formContainer.scrollIntoView({ behavior: 'smooth' });
    }
};

window.resetBannerForm = function() {
    document.getElementById('banner-edit-id').value = '';
    const form = document.getElementById('banner-image')?.closest('form');
    if (form) form.reset();

    document.getElementById('banner-image').setAttribute('required', 'required');

    const pImg = document.getElementById('preview-banner-image');
    const placeholder = document.getElementById('preview-banner-image-placeholder');
    if (pImg) {
        pImg.src = '';
        pImg.classList.add('hidden');
    }
    if (placeholder) placeholder.classList.remove('hidden');

    centerBannerImage();
    handleBannerTargetTypeChange();

    document.getElementById('banner-form-title').innerHTML = `<i class="fa-solid fa-panorama mr-2"></i>Nuevo banner`;
    document.getElementById('banner-submit-btn').innerHTML = `<i class="fa-solid fa-plus mr-1"></i>Crear banner`;
    document.getElementById('cancel-banner-edit-btn').classList.add('hidden');
};

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
            <td class="p-3">
                <span class="font-bold text-white block">${serv.name}</span>
                <span class="text-[10px] text-isivi-300 block mb-1">${serv.desc || ''}</span>
                ${getImageOriginBadge(serv.origenImagen)}
            </td>
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

    const fit = document.getElementById('serv-fit')?.value || 'cover';
    const zoom = parseFloat(document.getElementById('serv-zoom')?.value || 1.0);
    const posX = parseInt(document.getElementById('serv-pos-x')?.value || 50, 10);
    const posY = parseInt(document.getElementById('serv-pos-y')?.value || 50, 10);

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

    const payload = mapToApiServicio({ name, category, price, duration, img, desc, imageFit: fit, imageZoom: zoom, imagePosX: posX, imagePosY: posY });

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

    if (document.getElementById('serv-fit')) document.getElementById('serv-fit').value = s.imageFit || 'cover';
    document.getElementById('serv-zoom').value = s.imageZoom || 1.0;
    document.getElementById('serv-pos-x').value = s.imagePosX || 50;
    document.getElementById('serv-pos-y').value = s.imagePosY || 50;
    document.getElementById('serv-preview-img').src = s.img || '/images/isivi-logo-transparent.png';
    updateServiceImagePreview();

    document.getElementById('serv-form-title').innerHTML = `<i class="fa-solid fa-pen-to-square"></i> Editando Servicio: ${s.name}`;
    document.getElementById('cancel-serv-edit-btn').classList.remove('hidden');
    window.scrollTo({ top: 0, behavior: 'smooth' });
}

function resetServiceForm() {
    document.getElementById('serv-edit-id').value = '';
    document.getElementById('service-editor-form').reset();
    renderServiceCategorySelect();

    if (document.getElementById('serv-fit')) document.getElementById('serv-fit').value = 'cover';
    document.getElementById('serv-zoom').value = 1.0;
    document.getElementById('serv-pos-x').value = 50;
    document.getElementById('serv-pos-y').value = 50;
    document.getElementById('serv-preview-img').src = '/images/isivi-logo-transparent.png';
    updateServiceImagePreview();

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
    const b = (typeof bookingsList !== 'undefined' ? bookingsList.find(item => item.id === id) : null)
        || (typeof adminOrdersList !== 'undefined' ? adminOrdersList.find(item => item.id === id) : null)
        || (typeof historyBookingsList !== 'undefined' ? historyBookingsList.find(item => item.id === id) : null);
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
        // Unificar recarga de datos para consistencia entre Dashboard, Pedidos e Historial
        await loadAllData();
        await loadDashboardData();
        await loadAdminOrders();
        await loadHistoryBookings();
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
        const estPed = String(b.estadoPedido || '').trim().toUpperCase();
        const blockedStates = ['PENDIENTE', 'PENDIENTE PAGO', 'PENDIENTE_PAGO', 'EN_PREPARACION', 'LISTO_ENVIO', 'EN_CAMINO', 'LISTO_RECOGER'];
        if (blockedStates.includes(est) || blockedStates.includes(estPed)) {
            showToast("No se permite eliminar registros activos del historial.", "error");
            return;
        }
    }
    if (!confirm('¿Eliminar este registro del historial?')) return;
    try {
        await apiDelete(`/reservas/${id}`);
        historyBookingsList = historyBookingsList.filter(booking => booking.id !== id);
        renderAdminHistory();
        showToast('Registro eliminado correctamente.', 'success');
    } catch (err) {
        console.error(err);
        showToast('No fue posible eliminar el registro.', 'error');
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

    document.getElementById('admin-order-detail-modal')?.classList.remove('hidden');
}

async function adminApproveOrderFromDetail(orderId) {
    closeAdminOrderDetailModal();
    await approveBooking(orderId);
    await loadAdminOrders();
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

/* ==========================================================================
   FASE 2 — ACCESIBILIDAD, ACORDEÓN, BACKUP DE CARRITO Y QUICK VIEW
   ========================================================================== */

// 1. Resiliencia del Carrito ante Wompi (sessionStorage)
function createCartBackup() {
    try {
        const name = document.getElementById('cart-cust-name')?.value || '';
        const phone = document.getElementById('cart-cust-phone')?.value || '';
        const email = document.getElementById('cart-cust-email')?.value || '';
        const city = document.getElementById('cart-cust-city')?.value || '';
        const deliveryMethod = document.getElementById('cart-delivery-method')?.value || '';
        const deliveryAddress = document.getElementById('cart-delivery-address')?.value || '';

        const backup = {
            selectedServices,
            selectedProducts,
            selectedKits,
            productQuantities,
            kitQuantities,
            customer: { name, phone, email, city, deliveryMethod, deliveryAddress },
            selectedDateStr,
            selectedTimeSlot
        };
        sessionStorage.setItem('isivi_cart_backup', JSON.stringify(backup));
    } catch (e) {
        console.warn('Error al respaldar el carrito:', e);
    }
}

function restoreCartFromBackup() {
    try {
        const raw = sessionStorage.getItem('isivi_cart_backup');
        if (!raw) return false;
        const backup = JSON.parse(raw);
        
        selectedServices = backup.selectedServices || [];
        selectedProducts = backup.selectedProducts || [];
        selectedKits = backup.selectedKits || [];
        productQuantities = backup.productQuantities || {};
        kitQuantities = backup.kitQuantities || {};
        
        selectedDateStr = backup.selectedDateStr || '';
        selectedTimeSlot = backup.selectedTimeSlot || '';

        if (backup.customer) {
            const nameEl = document.getElementById('cart-cust-name');
            const phoneEl = document.getElementById('cart-cust-phone');
            const emailEl = document.getElementById('cart-cust-email');
            const cityEl = document.getElementById('cart-cust-city');
            const deliveryMethodEl = document.getElementById('cart-delivery-method');
            const deliveryAddressEl = document.getElementById('cart-delivery-address');

            if (nameEl) nameEl.value = backup.customer.name || '';
            if (phoneEl) phoneEl.value = backup.customer.phone || '';
            if (emailEl) emailEl.value = backup.customer.email || '';
            if (cityEl) cityEl.value = backup.customer.city || '';
            if (deliveryMethodEl) deliveryMethodEl.value = backup.customer.deliveryMethod || '';
            if (deliveryAddressEl) deliveryAddressEl.value = backup.customer.deliveryAddress || '';
        }
        
        syncCartState();
        if (selectedServices.length > 0) {
            renderCartSchedule();
        }
        return true;
    } catch (e) {
        console.warn('Error al restaurar el carrito:', e);
        return false;
    }
}

function clearCartBackup() {
    try {
        sessionStorage.removeItem('isivi_cart_backup');
    } catch (e) {
        console.warn('Error al borrar el respaldo del carrito:', e);
    }
}

// 2. Checkout por Pasos / Acordeón
let currentAccordionStep = 1;

function updateAccordionUI() {
    const items = getCartItems();
    const hasServices = items.some(item => item.type === 'service');

    const step2El = document.getElementById('step-2-schedule');
    if (step2El) {
        step2El.classList.toggle('hidden', !hasServices);
    }

    const step3Num = document.getElementById('step-3-num');
    if (step3Num) step3Num.textContent = hasServices ? '3' : '2';
    const step4Num = document.getElementById('step-4-num');
    if (step4Num) step4Num.textContent = hasServices ? '4' : '3';
    
    const step4Header = document.getElementById('step-4-header-text');
    if (step4Header) step4Header.textContent = hasServices ? 'Paso 4: Pago Seguro' : 'Paso 3: Pago Seguro';

    const step3Header = document.getElementById('step-3-header-text');
    if (step3Header) step3Header.textContent = hasServices ? 'Paso 3: Tus Datos y Entrega' : 'Paso 2: Tus Datos y Entrega';

    const stepIds = [
        'step-1-selection',
        'step-2-schedule',
        'step-3-details',
        'step-4-payment'
    ];

    for (let i = 1; i <= 4; i++) {
        const container = document.getElementById(stepIds[i - 1]);
        const body = document.getElementById(`accordion-body-${i}`);
        const icon = document.getElementById(`accordion-icon-${i}`);
        if (!container || !body) continue;

        if (i === 2 && !hasServices) {
            container.classList.add('hidden');
            continue;
        }

        // Determine step numbers
        let stepNumber = i;
        if (i === 3) stepNumber = hasServices ? 3 : 2;
        if (i === 4) stepNumber = hasServices ? 4 : 3;

        // Find header button and number indicator circle
        const button = container.querySelector('button');
        const indicator = button ? button.querySelector('.flex.h-5.w-5') || button.querySelector('.flex.h-6.w-6') || button.querySelector('span.flex') : null;
        const textSpan = button ? button.querySelector('.flex.items-center.gap-2 span:last-child') || button.querySelector('.flex.items-center.gap-3 span:last-child') : null;

        // Check if there are any validation errors visible for this step
        let hasError = false;
        if (i === 2) {
            hasError = !document.getElementById('cart-date-error')?.classList.contains('hidden') || 
                       !document.getElementById('cart-time-error')?.classList.contains('hidden');
        } else if (i === 3) {
            hasError = document.getElementById('cart-cust-name')?.getAttribute('aria-invalid') === 'true' ||
                       document.getElementById('cart-cust-phone')?.getAttribute('aria-invalid') === 'true' ||
                       document.getElementById('cart-cust-email')?.getAttribute('aria-invalid') === 'true' ||
                       document.getElementById('cart-delivery-address')?.getAttribute('aria-invalid') === 'true';
        }

        // Check if step is completed
        let isStepCompleted = false;
        if (i === 1) {
            isStepCompleted = items.length > 0;
        } else if (i === 2) {
            isStepCompleted = !hasServices || (Boolean(selectedDateStr) && Boolean(selectedTimeSlot));
        } else if (i === 3) {
            const nameVal = (document.getElementById('cart-cust-name')?.value || '').trim();
            const phoneVal = (document.getElementById('cart-cust-phone')?.value || '').trim();
            const emailVal = (document.getElementById('cart-cust-email')?.value || '').trim();
            const emailRegex = /^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/;
            const isDelivery = items.some(item => item.type === 'product' || item.type === 'kit') && document.getElementById('cart-delivery-method')?.value === 'delivery';
            const deliveryAddress = (document.getElementById('cart-delivery-address')?.value || '').trim();
            
            isStepCompleted = nameVal.length >= 3 && 
                              phoneVal.length >= 7 && 
                              emailRegex.test(emailVal) && 
                              (!isDelivery || deliveryAddress.length > 0);
        }

        // Apply visual classes based on active state
        if (i === currentAccordionStep) {
            // ACTIVO (Dominant step)
            body.classList.remove('hidden');
            if (icon) {
                icon.className = 'fa-solid fa-chevron-down text-[10px] text-isivi-gold transition-transform duration-300 rotate-180';
            }

            container.className = 'rounded-2xl border border-isivi-500/40 bg-stone-900/40 overflow-hidden shadow-xl transition-all duration-300';
            if (button) {
                button.className = 'w-full flex items-center justify-between p-4 text-xs font-bold uppercase tracking-wider text-white bg-stone-900/80 hover:bg-stone-900 transition-colors duration-200';
            }
            if (indicator) {
                indicator.className = 'flex h-6 w-6 items-center justify-center rounded-full bg-isivi-gold text-stone-950 font-bold scale-110 shadow-lg shadow-isivi-gold/20 transition-all duration-300';
                indicator.innerHTML = stepNumber;
            }
            if (textSpan) {
                textSpan.className = 'font-serif-title tracking-widest text-white font-bold';
            }
        } else {
            // NOT ACTIVE (body is closed)
            body.classList.add('hidden');
            if (icon) {
                icon.className = 'fa-solid fa-chevron-down text-[10px] text-stone-500 transition-transform duration-300';
            }

            if (hasError) {
                // ERROR
                container.className = 'rounded-2xl border border-red-500/40 bg-red-950/5 overflow-hidden shadow-lg transition-all duration-300';
                if (button) {
                    button.className = 'w-full flex items-center justify-between p-4 text-xs font-bold uppercase tracking-wider text-red-400 bg-stone-950/20 hover:bg-stone-900/40 transition-colors duration-200';
                }
                if (indicator) {
                    indicator.className = 'flex h-6 w-6 items-center justify-center rounded-full bg-red-950 text-red-400 border border-red-500/40 font-bold transition-all duration-300';
                    indicator.innerHTML = '<i class="fa-solid fa-circle-exclamation text-[10px]"></i>';
                }
                if (textSpan) {
                    textSpan.className = 'font-serif-title tracking-widest text-red-400/90';
                }
            } else if (isStepCompleted) {
                // COMPLETADO
                container.className = 'rounded-2xl border border-stone-850 bg-stone-950/20 overflow-hidden shadow-md opacity-90 transition-all duration-300';
                if (button) {
                    button.className = 'w-full flex items-center justify-between p-4 text-xs font-bold uppercase tracking-wider text-stone-300 bg-stone-950/40 hover:bg-stone-900/40 transition-colors duration-200';
                }
                if (indicator) {
                    indicator.className = 'flex h-6 w-6 items-center justify-center rounded-full bg-emerald-950 text-emerald-400 border border-emerald-500/30 font-bold transition-all duration-300';
                    indicator.innerHTML = '<i class="fa-solid fa-check text-[9px]"></i>';
                }
                if (textSpan) {
                    textSpan.className = 'font-serif-title tracking-widest text-stone-300';
                }
            } else {
                // PENDIENTE
                container.className = 'rounded-2xl border border-stone-900 bg-stone-950/10 overflow-hidden opacity-60 transition-all duration-300';
                if (button) {
                    button.className = 'w-full flex items-center justify-between p-4 text-xs font-bold uppercase tracking-wider text-stone-500 bg-stone-950/20 transition-colors duration-200';
                }
                if (indicator) {
                    indicator.className = 'flex h-6 w-6 items-center justify-center rounded-full bg-stone-900 border border-stone-800 text-[10px] text-stone-500 font-bold transition-all duration-300';
                    indicator.innerHTML = stepNumber;
                }
                if (textSpan) {
                    textSpan.className = 'font-serif-title tracking-widest text-stone-500';
                }
            }
        }
    }
}

function goToAccordionStep(step) {
    const items = getCartItems();
    const hasServices = items.some(item => item.type === 'service');

    if (step === 2 && !hasServices) {
        step = 3;
    }

    // 1. Si intenta avanzar desde el paso 2 (Reserva) hacia adelante, validar fecha/hora
    if (step >= 3 && hasServices) {
        if (!selectedDateStr || !selectedTimeSlot) {
            validateCartForm();
            return;
        }
    }

    // 2. Si intenta avanzar hacia el paso 4 (Pago), validar datos personales y de envío
    if (step >= 4) {
        const validation = validateCartForm();
        if (!validation.isValid) {
            return;
        }
    }

    currentAccordionStep = step;
    updateAccordionUI();
}

function toggleAccordionStep(step) {
    if (step === currentAccordionStep) {
        const body = document.getElementById(`accordion-body-${step}`);
        const icon = document.getElementById(`accordion-icon-${step}`);
        if (body) {
            body.classList.toggle('hidden');
            if (icon) icon.classList.toggle('rotate-180');
        }
    } else {
        goToAccordionStep(step);
    }
}

// Helper para sanitizar renderizado en HTML
function escapeHtml(str) {
    if (!str) return '';
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(new RegExp('"', 'g'), '&quot;')
        .replace(new RegExp("'", 'g'), '&#039;');
}

// Medición dinámica post-render: muestra 'Ver más' SOLO si scrollHeight > clientHeight
function updateVerMasVisibility(type, items) {
    if (!Array.isArray(items)) return;
    const checkOverflow = () => {
        items.forEach(item => {
            const itemIdStr = String(item.id);
            const descEl = document.getElementById(`card-desc-${type}-${itemIdStr}`);
            const btnEl = document.getElementById(`ver-mas-btn-${type}-${itemIdStr}`);
            if (descEl && btnEl) {
                const overflows = descEl.scrollHeight > descEl.clientHeight + 1;
                if (overflows) {
                    btnEl.classList.remove('hidden');
                } else {
                    btnEl.classList.add('hidden');
                }
            }
        });
    };

    requestAnimationFrame(checkOverflow);
    if (typeof document !== 'undefined' && document.fonts && document.fonts.ready) {
        document.fonts.ready.then(checkOverflow).catch(() => {});
    }
}

// Escuchar cambios de tamaño de pantalla para recalcular "Ver más"
window.addEventListener('resize', () => {
    if (Array.isArray(productsData)) updateVerMasVisibility('product', productsData);
    if (Array.isArray(kitsData)) updateVerMasVisibility('kit', kitsData);
    if (Array.isArray(servicesData)) updateVerMasVisibility('service', servicesData);
});

// 3. Quick View / Mini Ficha de Productos, Kits y Servicios
let activeDetailItemId = null;
let activeDetailItemType = null;

function renderDetailPriceAndSavings(price, oldPrice, itemType = 'product') {
    const priceEl = document.getElementById('product-detail-price');
    const oldPriceEl = document.getElementById('product-detail-old-price');
    const savingsBadgeEl = document.getElementById('product-detail-savings-badge');

    const p = Number(price || 0);
    const op = Number(oldPrice || 0);

    if (priceEl) priceEl.textContent = `$${p.toLocaleString('es-CO')}`;

    if (op > p) {
        const savings = op - p;
        if (oldPriceEl) {
            const prefix = (itemType === 'kit') ? 'Valor indiv.: ' : 'Antes: ';
            oldPriceEl.textContent = `${prefix}$${op.toLocaleString('es-CO')}`;
            oldPriceEl.classList.remove('hidden');
        }
        if (savingsBadgeEl) {
            savingsBadgeEl.textContent = `Ahorras $${savings.toLocaleString('es-CO')}`;
            savingsBadgeEl.classList.remove('hidden');
        }
    } else {
        if (oldPriceEl) oldPriceEl.classList.add('hidden');
        if (savingsBadgeEl) savingsBadgeEl.classList.add('hidden');
    }
}

function openItemDetailModal(type, itemId) {
    let item = null;
    const targetIdStr = String(itemId);

    if (type === 'product' && Array.isArray(productsData)) {
        item = productsData.find(p => String(p.id) === targetIdStr);
    } else if (type === 'kit' && Array.isArray(kitsData)) {
        item = kitsData.find(k => String(k.id) === targetIdStr);
    } else if (type === 'service' && Array.isArray(servicesData)) {
        item = servicesData.find(s => String(s.id) === targetIdStr);
    }
    if (!item) return;
    
    activeDetailItemId = itemId;
    activeDetailItemType = type;
    
    const titleEl = document.getElementById('product-detail-title');
    const imgEl = document.getElementById('product-detail-image');
    const priceEl = document.getElementById('product-detail-price');
    const priceLabelEl = document.getElementById('product-detail-price-label');
    const stockBadge = document.getElementById('product-detail-stock-badge');
    const durationBadge = document.getElementById('product-detail-duration-badge');
    const durationText = document.getElementById('product-detail-duration-text');
    const kitBadge = document.getElementById('product-detail-kit-badge');
    const depositContainer = document.getElementById('product-detail-deposit-container');
    const depositEl = document.getElementById('product-detail-deposit');

    const descContainer = document.getElementById('product-detail-desc-container');
    const descEl = document.getElementById('product-detail-desc');
    const ingContainer = document.getElementById('product-detail-ing-container');
    const ingEl = document.getElementById('product-detail-ing');
    const useContainer = document.getElementById('product-detail-use-container');
    const useEl = document.getElementById('product-detail-use');
    const varContainer = document.getElementById('product-detail-variants-container');
    const varGrid = document.getElementById('product-detail-variants-grid');

    if (titleEl) titleEl.textContent = item.name || '';
    if (imgEl) {
        imgEl.src = item.img || '/images/isivi-logo-transparent.png';
        imgEl.alt = item.name || '';
        imgEl.style.objectFit = `contain`;
        imgEl.style.objectPosition = `center center`;
        imgEl.style.transform = `none`;
        imgEl.style.transformOrigin = `center center`;
        imgEl.style.width = `auto`;
        imgEl.style.height = `auto`;
        imgEl.style.maxWidth = `100%`;
        imgEl.style.maxHeight = `100%`;
        imgEl.style.borderRadius = `20px`;
    }
    
    const desc = (item.desc || '').replace(/\n\s*,/g, ',').replace(/\s{2,}/g, ' ').trim();
    if (descContainer && descEl) {
        if (desc) {
            descEl.textContent = desc;
            descContainer.classList.remove('hidden');
        } else {
            descContainer.classList.add('hidden');
        }
    }

    if (durationBadge) durationBadge.classList.add('hidden');
    if (kitBadge) kitBadge.classList.add('hidden');
    if (depositContainer) depositContainer.classList.add('hidden');
    if (ingContainer) ingContainer.classList.add('hidden');
    if (useContainer) useContainer.classList.add('hidden');
    if (varContainer) varContainer.classList.add('hidden');

    if (type === 'product') {
        const ing = (item.ingredientes || '').trim();
        if (ingContainer && ingEl) {
            if (ing) { ingEl.textContent = ing; ingContainer.classList.remove('hidden'); }
        }
        const uso = (item.modoEmpleo || '').trim();
        if (useContainer && useEl) {
            if (uso) { useEl.textContent = uso; useContainer.classList.remove('hidden'); }
        }

        const isVariantType = item.priceType === 'VARIANTES' && Array.isArray(item.variants) && item.variants.length > 0;
        let currentVar = null;
        let inStock = item.inStock !== false && item.quantity > 0;
        let price = item.price;
        let availableStock = item.quantity;

        if (isVariantType) {
            if (priceLabelEl) priceLabelEl.textContent = 'Precio Variante';
            const activeVars = item.variants.filter(v => v.activo !== false);
            const savedVarId = selectedProductVariants[item.id];
            currentVar = activeVars.find(v => String(v.id) === String(savedVarId)) || activeVars[0];
            if (currentVar) {
                selectedProductVariants[item.id] = currentVar.id;
                price = Number(currentVar.precio) || 0;
                availableStock = Number(currentVar.cantidad) || 0;
                inStock = currentVar.enStock !== false && availableStock > 0;
            }

            if (varContainer && varGrid && activeVars.length > 0) {
                varContainer.classList.remove('hidden');
                varGrid.innerHTML = activeVars.map(v => {
                    const isVarSel = currentVar && String(currentVar.id) === String(v.id);
                    const isVarOut = v.enStock === false || v.cantidad <= 0;
                    return `
                        <button type="button" onclick="selectDetailProductVariant('${item.id}', '${v.id}')" ${isVarOut ? 'disabled' : ''} class="px-3 py-1 rounded-xl text-[10px] font-bold transition border ${isVarSel ? 'bg-gradient-to-r from-isivi-gold via-amber-300 to-amber-500 text-stone-950 border-amber-300 shadow-md' : isVarOut ? 'bg-stone-900/60 text-stone-600 border-stone-800 cursor-not-allowed line-through' : 'bg-stone-900/80 text-stone-300 border-stone-700/80 hover:border-isivi-gold'}">
                            ${v.nombre} · $${Number(v.precio || 0).toLocaleString('es-CO')}
                        </button>
                    `;
                }).join('');
            }
        } else {
            if (priceLabelEl) priceLabelEl.textContent = 'Precio Total';
        }

        const oldPrice = (currentVar && currentVar.oldPrice != null) 
            ? currentVar.oldPrice 
            : (item.oldPrice != null ? item.oldPrice : null);
        renderDetailPriceAndSavings(price, oldPrice, 'product');

        if (stockBadge) {
            if (inStock) {
                stockBadge.innerHTML = '<span class="w-1.5 h-1.5 rounded-full bg-emerald-400 animate-pulse"></span> En Stock';
                stockBadge.className = 'inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-[10px] font-bold bg-emerald-500/10 text-emerald-400 border border-emerald-500/30 backdrop-blur-sm shadow-sm';
            } else {
                stockBadge.textContent = 'Agotado';
                stockBadge.className = 'inline-flex items-center px-3 py-1 rounded-full text-[10px] font-bold bg-stone-900/80 text-stone-500 border border-stone-800 backdrop-blur-sm';
            }
        }

        renderDetailActionArea(item, currentVar, inStock, availableStock);

    } else if (type === 'kit') {
        if (kitBadge) kitBadge.classList.remove('hidden');
        if (priceLabelEl) priceLabelEl.textContent = 'Precio Total Kit';
        renderDetailPriceAndSavings(item.price, item.oldPrice, 'kit');

        const inStock = item.inStock !== false && item.quantity > 0;
        const availableStock = item.quantity;
        if (stockBadge) {
            if (inStock) {
                stockBadge.innerHTML = '<span class="w-1.5 h-1.5 rounded-full bg-emerald-400 animate-pulse"></span> En Stock';
                stockBadge.className = 'inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-[10px] font-bold bg-emerald-500/10 text-emerald-400 border border-emerald-500/30 backdrop-blur-sm shadow-sm';
            } else {
                stockBadge.textContent = 'Agotado';
                stockBadge.className = 'inline-flex items-center px-3 py-1 rounded-full text-[10px] font-bold bg-stone-900/80 text-stone-500 border border-stone-800 backdrop-blur-sm';
            }
        }

        renderKitDetailActionArea(item, inStock, availableStock);

    } else if (type === 'service') {
        if (durationBadge && durationText) {
            durationText.textContent = item.duration || '60 min';
            durationBadge.classList.remove('hidden');
        }

        if (priceLabelEl) priceLabelEl.textContent = 'Precio Total';
        renderDetailPriceAndSavings(item.price, null);

        const deposit = Math.round((item.price || 0) * 0.25);
        if (depositContainer && depositEl) {
            depositEl.textContent = `$${deposit.toLocaleString('es-CO')}`;
            depositContainer.classList.remove('hidden');
        }

        if (stockBadge) {
            stockBadge.innerHTML = '<span class="w-1.5 h-1.5 rounded-full bg-emerald-400 animate-pulse"></span> Disponible';
            stockBadge.className = 'inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-[10px] font-bold bg-emerald-500/10 text-emerald-400 border border-emerald-500/30 backdrop-blur-sm shadow-sm';
        }

        renderServiceDetailActionArea(item);
    }

    const modal = document.getElementById('product-detail-modal');
    if (modal) {
        modal.classList.remove('hidden');
        modal.dataset.triggerId = document.activeElement ? document.activeElement.id : '';
        modal.focus();
        trapFocus(modal);
    }
}

function openProductDetailModal(productId) {
    openItemDetailModal('product', productId);
}

function openKitDetailModal(kitId) {
    openItemDetailModal('kit', kitId);
}

function openServiceDetailModal(serviceId) {
    openItemDetailModal('service', serviceId);
}

function closeProductDetailModal() {
    const modal = document.getElementById('product-detail-modal');
    if (modal) {
        modal.classList.add('hidden');
        releaseFocus(modal);
        const triggerId = modal.dataset.triggerId;
        if (triggerId) {
            const el = document.getElementById(triggerId);
            if (el) el.focus();
        }
    }
    activeDetailItemId = null;
    activeDetailItemType = null;
}

function selectDetailProductVariant(productId, variantId) {
    selectedProductVariants[productId] = variantId;
    openItemDetailModal('product', productId);
    renderProductsGrid();
}

function renderDetailActionArea(product, currentVar, inStock, availableStock) {
    const actionContainer = document.getElementById('product-detail-action-container');
    if (!actionContainer) return;

    const inCartCount = getProductCartCount(product.id, currentVar ? currentVar.id : null);
    
    if (!inStock) {
        actionContainer.innerHTML = `<button disabled class="w-full py-2.5 sm:py-3 rounded-xl sm:rounded-2xl text-[10px] sm:text-xs font-bold bg-stone-900/80 text-stone-600 border border-stone-800 cursor-not-allowed">Agotado</button>`;
    } else if (inCartCount > 0) {
        actionContainer.innerHTML = `
            <div class="flex flex-col sm:flex-row items-stretch sm:items-center gap-1.5 w-full">
                <div class="flex items-center rounded-xl sm:rounded-2xl border border-stone-700/80 bg-stone-900/90 flex-1 justify-between px-1 py-0.5 shadow-inner min-w-0">
                    <button type="button" onclick="changeDetailCardQuantity('product', '${product.id}',-1)" class="w-7 h-7 sm:w-8 sm:h-8 rounded-lg sm:rounded-xl bg-stone-800/60 hover:bg-stone-800 text-isivi-gold font-black text-sm sm:text-base flex items-center justify-center transition flex-shrink-0">-</button>
                    <span class="text-[10px] sm:text-xs font-extrabold text-white px-1 truncate text-center">${inCartCount} <span class="text-[9px] text-stone-400 font-normal">en carrito</span></span>
                    <button type="button" onclick="changeDetailCardQuantity('product', '${product.id}',1)" ${inCartCount >= availableStock ? 'disabled' : ''} class="w-7 h-7 sm:w-8 sm:h-8 rounded-lg sm:rounded-xl bg-stone-800/60 hover:bg-stone-800 text-isivi-gold font-black text-sm sm:text-base flex items-center justify-center transition disabled:text-stone-600 disabled:bg-transparent flex-shrink-0">+</button>
                </div>
                <button onclick="addDetailItemToCart('product', '${product.id}')" class="w-full sm:w-auto px-3 py-1.5 sm:py-2.5 rounded-xl sm:rounded-2xl text-[10px] sm:text-xs font-bold bg-rose-950/80 border border-rose-800/60 text-rose-300 hover:bg-rose-900 transition shadow flex items-center justify-center gap-1.5 flex-shrink-0" title="Quitar del carrito">
                    <i class="fa-solid fa-trash-can text-[10px] sm:text-xs"></i> <span>Quitar</span>
                </button>
            </div>
        `;
    } else {
        actionContainer.innerHTML = `
            <button onclick="addDetailItemToCart('product', '${product.id}')" class="w-full py-2.5 sm:py-3.5 rounded-xl sm:rounded-2xl text-[10px] sm:text-xs font-black uppercase tracking-wider bg-gradient-to-r from-isivi-500 to-isivi-600 text-white hover:from-isivi-600 hover:to-isivi-700 shadow-lg shadow-isivi-500/20 transition-all transform hover:-translate-y-0.5 active:translate-y-0 flex items-center justify-center gap-1.5 sm:gap-2 px-2" aria-label="Agregar ${product.name} al carrito">
                <i class="fa-solid fa-bag-shopping text-xs sm:text-sm"></i> <span class="truncate">Agregar al Carrito</span>
            </button>
        `;
    }
}

function renderKitDetailActionArea(kit, inStock, availableStock) {
    const actionContainer = document.getElementById('product-detail-action-container');
    if (!actionContainer) return;

    const inCartCount = getKitCartCount(kit.id);
    
    if (!inStock) {
        actionContainer.innerHTML = `<button disabled class="w-full py-2.5 sm:py-3 rounded-xl sm:rounded-2xl text-[10px] sm:text-xs font-bold bg-stone-900/80 text-stone-600 border border-stone-800 cursor-not-allowed">Agotado</button>`;
    } else if (inCartCount > 0) {
        actionContainer.innerHTML = `
            <div class="flex flex-col sm:flex-row items-stretch sm:items-center gap-1.5 w-full">
                <div class="flex items-center rounded-xl sm:rounded-2xl border border-stone-700/80 bg-stone-900/90 flex-1 justify-between px-1 py-0.5 shadow-inner min-w-0">
                    <button type="button" onclick="changeDetailCardQuantity('kit', '${kit.id}',-1)" class="w-7 h-7 sm:w-8 sm:h-8 rounded-lg sm:rounded-xl bg-stone-800/60 hover:bg-stone-800 text-isivi-gold font-black text-sm sm:text-base flex items-center justify-center transition flex-shrink-0">-</button>
                    <span class="text-[10px] sm:text-xs font-extrabold text-white px-1 truncate text-center">${inCartCount} <span class="text-[9px] text-stone-400 font-normal">en carrito</span></span>
                    <button type="button" onclick="changeDetailCardQuantity('kit', '${kit.id}',1)" ${inCartCount >= availableStock ? 'disabled' : ''} class="w-7 h-7 sm:w-8 sm:h-8 rounded-lg sm:rounded-xl bg-stone-800/60 hover:bg-stone-800 text-isivi-gold font-black text-sm sm:text-base flex items-center justify-center transition disabled:text-stone-600 disabled:bg-transparent flex-shrink-0">+</button>
                </div>
                <button onclick="addDetailItemToCart('kit', '${kit.id}')" class="w-full sm:w-auto px-3 py-1.5 sm:py-2.5 rounded-xl sm:rounded-2xl text-[10px] sm:text-xs font-bold bg-rose-950/80 border border-rose-800/60 text-rose-300 hover:bg-rose-900 transition shadow flex items-center justify-center gap-1.5 flex-shrink-0" title="Quitar del carrito">
                    <i class="fa-solid fa-trash-can text-[10px] sm:text-xs"></i> <span>Quitar</span>
                </button>
            </div>
        `;
    } else {
        actionContainer.innerHTML = `
            <button onclick="addDetailItemToCart('kit', '${kit.id}')" class="w-full py-2.5 sm:py-3.5 rounded-xl sm:rounded-2xl text-[10px] sm:text-xs font-black uppercase tracking-wider bg-gradient-to-r from-isivi-500 to-isivi-600 text-white hover:from-isivi-600 hover:to-isivi-700 shadow-lg shadow-isivi-500/20 transition-all transform hover:-translate-y-0.5 active:translate-y-0 flex items-center justify-center gap-1.5 sm:gap-2 px-2" aria-label="Agregar ${kit.name} al carrito">
                <i class="fa-solid fa-bag-shopping text-xs sm:text-sm"></i> <span class="truncate">Agregar Kit al Carrito</span>
            </button>
        `;
    }
}

function renderServiceDetailActionArea(service) {
    const actionContainer = document.getElementById('product-detail-action-container');
    if (!actionContainer) return;

    const isSelected = selectedServices.includes(service.id);
    actionContainer.innerHTML = `
        <button onclick="addDetailServiceToCart('${service.id}')" class="w-full py-2.5 sm:py-3.5 rounded-xl sm:rounded-2xl text-[10px] sm:text-xs font-black uppercase tracking-wider transition-all transform hover:-translate-y-0.5 active:translate-y-0 flex items-center justify-center gap-1.5 sm:gap-2 px-2 shadow-lg ${isSelected ? 'bg-emerald-600 text-white hover:bg-emerald-500' : 'bg-gradient-to-r from-isivi-500 to-isivi-600 text-white hover:from-isivi-600 hover:to-isivi-700 shadow-lg shadow-isivi-500/20'}" aria-label="Agendar ${service.name}">
            ${isSelected ? '<i class="fa-solid fa-check text-xs sm:text-sm"></i> <span class="truncate">En tu Selección</span>' : '<i class="fa-solid fa-calendar-plus text-xs sm:text-sm"></i> <span class="truncate">Agendar Servicio</span>'}
        </button>
    `;
}

function changeDetailCardQuantity(type, itemId, delta) {
    const catalog = type === 'product' ? productsData : kitsData;
    const item = catalog.find(p => p.id === itemId);
    if (!item) return;

    let varId = null;
    if (type === 'product' && item.priceType === 'VARIANTES' && Array.isArray(item.variants)) {
        varId = selectedProductVariants[item.id];
    }
    const cartKey = (type === 'product' && varId) ? `${item.id}_${varId}` : itemId;
    changeCartQuantity(type, cartKey, delta);
    openItemDetailModal(type, itemId);
}

function addDetailItemToCart(type, itemId) {
    addItemToCart(type, itemId);
    openItemDetailModal(type, itemId);
}

function addDetailServiceToCart(serviceId) {
    closeProductDetailModal();
    toggleService(serviceId);
}

// 4. Focus Trap & Key Listeners (Escape)
let activeFocusTrapListener = null;

function trapFocus(modalEl) {
    if (!modalEl) return;
    const focusableElements = modalEl.querySelectorAll('button, [href], input, select, textarea, [tabindex]:not([tabindex="-1"])');
    const firstFocusable = focusableElements[0];
    const lastFocusable = focusableElements[focusableElements.length - 1];

    if (activeFocusTrapListener) {
        modalEl.removeEventListener('keydown', activeFocusTrapListener);
    }

    activeFocusTrapListener = function(e) {
        if (e.key === 'Tab') {
            if (e.shiftKey) {
                if (document.activeElement === firstFocusable) {
                    lastFocusable.focus();
                    e.preventDefault();
                }
            } else {
                if (document.activeElement === lastFocusable) {
                    firstFocusable.focus();
                    e.preventDefault();
                }
            }
        }
    };
    modalEl.addEventListener('keydown', activeFocusTrapListener);
}

function releaseFocus(modalEl) {
    if (modalEl && activeFocusTrapListener) {
        modalEl.removeEventListener('keydown', activeFocusTrapListener);
        activeFocusTrapListener = null;
    }
}

// Escuchar tecla Escape de forma global para cerrar modales activos
document.addEventListener('keydown', function(e) {
    if (e.key === 'Escape') {
        const detailModal = document.getElementById('product-detail-modal');
        if (detailModal && !detailModal.classList.contains('hidden')) {
            closeProductDetailModal();
            return;
        }

        const successModal = document.getElementById('modal-success-confirmation');
        if (successModal && !successModal.classList.contains('hidden')) {
            closeSuccessConfirmation();
            return;
        }

        const lookupModal = document.getElementById('modal-consultar-reserva');
        if (lookupModal && !lookupModal.classList.contains('hidden')) {
            closeLookupModal();
            return;
        }

        const paymentModal = document.getElementById('payment-modal');
        if (paymentModal && !paymentModal.classList.contains('hidden')) {
            closePaymentModal();
            return;
        }

        const cartDrawer = document.getElementById('cart-drawer');
        if (cartDrawer && !cartDrawer.classList.contains('hidden')) {
            closeCartDrawer();
            return;
        }
    }
});


let adminPollingInterval = null;

function startAdminPolling() {
    if (adminPollingInterval) clearInterval(adminPollingInterval);
    adminPollingInterval = setInterval(() => {
        if (window.location.hash === '#admin' && adminAuthToken) {
            const activeTab = document.querySelector('[id^="adm-tab-"]:not(.hidden)');
            if (activeTab) {
                const tabId = activeTab.id.replace('adm-tab-', '');
                if (tabId === 'dashboard') loadDashboardData();
                else if (tabId === 'orders') loadAdminOrders();
                else if (tabId === 'bookings') {
                    loadAdminData();
                }
            }
        }
    }, 30000); // 30 segundos
}

function stopAdminPolling() {
    if (adminPollingInterval) {
        clearInterval(adminPollingInterval);
        adminPollingInterval = null;
    }
}



