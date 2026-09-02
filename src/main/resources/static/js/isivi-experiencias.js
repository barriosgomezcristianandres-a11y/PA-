/**
 * ISIVI - Módulo Experiencias ISIVI (100% Encapsulado y Aislado)
 */
(function (window, document) {
    'use strict';

    var state = {
        experienciasPublicas: [],
        experienciasAdmin: [],
        currentIndex: 0,
        catalogoProductos: [],
        catalogoKits: [],
        catalogoServicios: [],
        fileResultado: null,
        fileAntes: null
    };

    function escapeHtml(str) {
        if (!str) return '';
        return String(str)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#39;');
    }

    function sanitizeUrl(url) {
        if (!url) return '';
        var clean = String(url).trim();
        var lower = clean.toLowerCase();
        if (lower.startsWith('javascript:') || lower.startsWith('vbscript:') || lower.startsWith('data:')) {
            return '';
        }
        return clean;
    }

    function formatBytes(bytes) {
        if (!bytes || bytes === 0) return '0 Bytes';
        var k = 1024;
        var sizes = ['Bytes', 'KB', 'MB'];
        var i = Math.floor(Math.log(bytes) / Math.log(k));
        return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
    }

    // Public API / Endpoints Render
    function loadPublicExperiencias() {
        fetch('/api/experiencias-isivi')
            .then(function (res) { return res.json(); })
            .then(function (data) {
                if (!Array.isArray(data)) return;
                state.experienciasPublicas = data;
                state.currentIndex = 0;
                renderPublicExperiencias();
            })
            .catch(function (err) {
                console.warn('Error cargando experiencias públicas ISIVI:', err);
            });
    }

    function renderPublicExperiencias() {
        var container = document.getElementById('isivi-experiencias-container');
        var section = document.getElementById('isivi-experiencias-section');
        var counter = document.getElementById('isivi-experiencias-counter');
        var controls = document.getElementById('isivi-experiencias-controls');

        if (!container || !section) return;

        if (state.experienciasPublicas.length === 0) {
            section.classList.add('hidden');
            return;
        }

        section.classList.remove('hidden');
        container.innerHTML = '';

        if (state.currentIndex >= state.experienciasPublicas.length) {
            state.currentIndex = 0;
        }
        if (state.currentIndex < 0) {
            state.currentIndex = state.experienciasPublicas.length - 1;
        }

        if (counter) {
            counter.textContent = (state.currentIndex + 1) + ' / ' + state.experienciasPublicas.length;
        }

        if (controls) {
            if (state.experienciasPublicas.length <= 1) {
                controls.classList.add('hidden');
            } else {
                controls.classList.remove('hidden');
            }
        }

        var exp = state.experienciasPublicas[state.currentIndex];
        if (!exp) return;

        var card = document.createElement('div');
        card.className = 'isivi-experiencias-card bg-stone-900 border border-isivi-500/20 rounded-3xl p-6 sm:p-8 shadow-2xl flex flex-col md:flex-row items-center gap-6';

        var starsHtml = '';
        var rating = exp.calificacion || 5;
        for (var i = 0; i < 5; i++) {
            if (i < rating) {
                starsHtml += '<i class="fa-solid fa-star isivi-experiencias-star text-sm"></i>';
            } else {
                starsHtml += '<i class="fa-regular fa-star text-stone-600 text-sm"></i>';
            }
        }

        var relBtnHtml = renderCatalogRelationBadge(exp.tipoRelacionado, exp.elementoRelacionadoId, false);

        var imagesHtml = '';
        var imgSrc = sanitizeUrl(exp.imagenPrincipal || exp.imagenDespues);
        var antesSrc = sanitizeUrl(exp.imagenAntes);
        if (imgSrc) {
            var toggleAntesBtn = '';
            if (antesSrc) {
                toggleAntesBtn = '<button type="button" onclick="window.IsiviExperienciasModule.openBeforeAfterModal(\'' + escapeHtml(exp.id) + '\')" class="absolute bottom-3 right-3 px-3 py-1.5 rounded-xl bg-stone-950/80 backdrop-blur-md border border-stone-700 text-[10px] font-bold text-isivi-gold hover:bg-stone-900 transition flex items-center gap-1.5 shadow"><i class="fa-solid fa-code-compare"></i> Ver Antes / Después</button>';
            }
            var fit = exp.fit || 'cover';
            var zoom = exp.zoom != null ? exp.zoom : 1.0;
            var posX = exp.posX != null ? exp.posX : 50;
            var posY = exp.posY != null ? exp.posY : 50;
            var imgStyle = 'object-fit:' + fit + '; transform: scale(' + zoom + '); transform-origin:' + posX + '% ' + posY + '%; object-position:' + posX + '% ' + posY + '%;';

            imagesHtml = '<div class="w-full md:w-1/2 h-64 sm:h-72 rounded-2xl overflow-hidden bg-stone-950 border border-stone-800 flex-shrink-0 relative shadow-lg">' +
                '<img src="' + imgSrc + '" alt="' + escapeHtml(exp.nombreMostrar) + '" class="w-full h-full transition-transform duration-300" style="' + imgStyle + '">' +
                toggleAntesBtn +
                '</div>';
        }

        card.innerHTML =
            imagesHtml +
            '<div class="flex-1 flex flex-col justify-between space-y-4 text-left w-full">' +
            '<div>' +
            '<div class="flex items-center justify-between mb-3"><div class="flex gap-1">' + starsHtml + '</div>' +
            (exp.destacada ? '<span class="isivi-experiencias-badge text-[10px] font-bold px-2.5 py-0.5 rounded-full flex items-center gap-1"><i class="fa-solid fa-crown text-[9px]"></i> Destacada</span>' : '') +
            '</div>' +
            '<p class="text-sm sm:text-base text-stone-200 italic leading-relaxed mb-4">"' + escapeHtml(exp.testimonio) + '"</p>' +
            '</div>' +
            '<div class="pt-4 border-t border-stone-800 flex items-center justify-between flex-wrap gap-2">' +
            '<div><h4 class="font-serif-title font-bold text-white text-base sm:text-lg">' + escapeHtml(exp.nombreMostrar || 'Cliente ISIVI') + '</h4><span class="text-xs text-isivi-300 flex items-center gap-1"><i class="fa-solid fa-sparkles text-isivi-gold text-[10px]"></i> Experiencia ISIVI</span></div>' +
            relBtnHtml +
            '</div>' +
            '</div>';

        container.appendChild(card);
    }

    function prevExperience() {
        if (state.experienciasPublicas.length === 0) return;
        state.currentIndex--;
        if (state.currentIndex < 0) {
            state.currentIndex = state.experienciasPublicas.length - 1;
        }
        renderPublicExperiencias();
    }

    function nextExperience() {
        if (state.experienciasPublicas.length === 0) return;
        state.currentIndex++;
        if (state.currentIndex >= state.experienciasPublicas.length) {
            state.currentIndex = 0;
        }
        renderPublicExperiencias();
    }

    // Resolución de elementos del catálogo (Producto / Kit / Servicio)
    function getCatalogItem(tipo, id) {
        if (!tipo || tipo === 'NINGUNA' || !id) return null;
        var idStr = String(id);

        var list = [];
        if (tipo === 'PRODUCTO') {
            list = (state.catalogoProductos && state.catalogoProductos.length > 0)
                ? state.catalogoProductos
                : (window.productsData || []);
        } else if (tipo === 'KIT') {
            list = (state.catalogoKits && state.catalogoKits.length > 0)
                ? state.catalogoKits
                : (window.kitsData || []);
        } else if (tipo === 'SERVICIO') {
            list = (state.catalogoServicios && state.catalogoServicios.length > 0)
                ? state.catalogoServicios
                : (window.servicesData || []);
        }

        var item = list.find(function (x) { return String(x.id) === idStr; });
        if (!item) return null;

        var name = item.nombre || item.name || (tipo === 'PRODUCTO' ? 'Producto' : (tipo === 'KIT' ? 'Kit' : 'Servicio'));
        var img = item.imagenUrl || item.img || '/images/isivi-logo-transparent.png';

        return {
            id: String(item.id),
            name: name,
            img: img
        };
    }

    // Renderizador de Badge/Botón con Miniatura de Catálogo
    function renderCatalogRelationBadge(tipo, id, isPreview) {
        if (!tipo || tipo === 'NINGUNA' || !id) {
            return '';
        }

        var itemInfo = getCatalogItem(tipo, id);
        var tipoLabel = tipo === 'PRODUCTO' ? 'Ver Producto' : (tipo === 'KIT' ? 'Ver Kit' : 'Ver Servicio');
        var itemName = itemInfo ? itemInfo.name : tipoLabel;
        var itemImg = itemInfo ? itemInfo.img : '';

        var imgHtml = '';
        if (itemImg) {
            imgHtml = '<div class="w-8 h-8 rounded-xl overflow-hidden bg-stone-950 border border-stone-800 flex-shrink-0 flex items-center justify-center shadow-sm">' +
                '<img src="' + escapeHtml(itemImg) + '" alt="' + escapeHtml(itemName) + '" class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300" onerror="this.onerror=null; this.src=\'/images/isivi-logo-transparent.png\';">' +
                '</div>';
        } else {
            imgHtml = '<div class="w-8 h-8 rounded-xl overflow-hidden bg-stone-950 border border-stone-800 flex-shrink-0 flex items-center justify-center text-isivi-gold text-xs shadow-sm">' +
                '<i class="fa-solid ' + (tipo === 'PRODUCTO' ? 'fa-box' : (tipo === 'KIT' ? 'fa-boxes-packing' : 'fa-sparkles')) + '"></i>' +
                '</div>';
        }

        var pointerClass = isPreview ? 'pointer-events-none' : '';

        return '<button type="button" onclick="window.IsiviExperienciasModule.openRelatedCatalogItem(\'' + escapeHtml(tipo) + '\', \'' + escapeHtml(id) + '\')" class="inline-flex items-center gap-2.5 px-3 py-1.5 rounded-2xl bg-isivi-gold/10 border border-isivi-500/40 text-isivi-gold hover:bg-isivi-gold hover:text-isivi-black transition shadow-md group text-left ' + pointerClass + '">' +
            imgHtml +
            '<div class="flex flex-col min-w-0 text-left">' +
            '<span class="block text-[9px] text-stone-400 font-semibold uppercase tracking-wider leading-none mb-0.5 group-hover:text-isivi-black/70">' + escapeHtml(tipoLabel) + '</span>' +
            '<span class="text-xs font-bold flex items-center gap-1 leading-tight truncate max-w-[130px] sm:max-w-[180px]">' +
            '<span class="truncate">' + escapeHtml(itemName) + '</span> <i class="fa-solid fa-arrow-right text-[9px] flex-shrink-0"></i>' +
            '</span>' +
            '</div>' +
            '</button>';
    }

    // Navegación hacia el catálogo relacionado (Misma lógica de Banners)
    function openRelatedCatalogItem(tipo, id) {
        if (!tipo || tipo === 'NINGUNA') return;

        var sectionName = 'productos';
        if (tipo === 'SERVICIO') sectionName = 'servicios';
        if (tipo === 'KIT') sectionName = 'kits';

        if (typeof window.navigateToSection === 'function') {
            window.navigateToSection(sectionName);
        } else {
            var targetEl = document.getElementById(sectionName);
            if (targetEl) {
                targetEl.classList.remove('hidden');
                targetEl.scrollIntoView({ behavior: 'smooth' });
            }
        }

        if (id) {
            setTimeout(function () {
                var selector = '';
                if (tipo === 'PRODUCTO') selector = '[onclick*="addItemToCart(\'product\',\'' + id + '\')"]';
                else if (tipo === 'KIT') selector = '[onclick*="addItemToCart(\'kit\',\'' + id + '\')"]';
                else if (tipo === 'SERVICIO') selector = '[onclick*="toggleService(\'' + id + '\')"]';

                var btn = selector ? document.querySelector(selector) : null;
                var cardEl = btn ? btn.closest('.isivi-card-premium') : (document.querySelector('[data-item-id="' + id + '"]') || document.getElementById('item-' + id));

                if (cardEl) {
                    cardEl.scrollIntoView({ behavior: 'smooth', block: 'center' });
                    cardEl.classList.add('ring-4', 'ring-isivi-gold');
                    setTimeout(function () {
                        cardEl.classList.remove('ring-4', 'ring-isivi-gold');
                    }, 3000);
                }
            }, 300);
        }
    }

    // Carga de catálogo para selectores desplegables
    function fetchCatalogForSelects() {
        fetch('/api/productos')
            .then(function (res) { return res.json(); })
            .then(function (data) {
                if (Array.isArray(data)) {
                    state.catalogoProductos = data.filter(function (p) { return p.tipoItem !== 'KIT'; });
                    var hiddenIdInput = document.getElementById('exp-relacion-id');
                    var selectedId = hiddenIdInput ? hiddenIdInput.value : '';
                    handleTipoRelacionChange(selectedId);
                    updateExperienceCardPreview();
                    renderPublicExperiencias();
                }
            })
            .catch(function () {});

        fetch('/api/kits')
            .then(function (res) { return res.json(); })
            .then(function (data) {
                if (Array.isArray(data)) {
                    state.catalogoKits = data;
                    var hiddenIdInput = document.getElementById('exp-relacion-id');
                    var selectedId = hiddenIdInput ? hiddenIdInput.value : '';
                    handleTipoRelacionChange(selectedId);
                    updateExperienceCardPreview();
                    renderPublicExperiencias();
                }
            })
            .catch(function () {});

        fetch('/api/servicios')
            .then(function (res) { return res.json(); })
            .then(function (data) {
                if (Array.isArray(data)) {
                    state.catalogoServicios = data;
                    var hiddenIdInput = document.getElementById('exp-relacion-id');
                    var selectedId = hiddenIdInput ? hiddenIdInput.value : '';
                    handleTipoRelacionChange(selectedId);
                    updateExperienceCardPreview();
                    renderPublicExperiencias();
                }
            })
            .catch(function () {});
    }

    // Ajustes de Imagen y Vista Previa en Tarjeta Real

    function setExperienceFitPreset(fit) {
        var fitSelect = document.getElementById('exp-fit');
        var zoomInput = document.getElementById('exp-zoom');
        var posXInput = document.getElementById('exp-pos-x');
        var posYInput = document.getElementById('exp-pos-y');
        if (fitSelect) fitSelect.value = fit;
        if (zoomInput) zoomInput.value = fit === 'contain' ? 0.95 : 1.0;
        if (posXInput) posXInput.value = 50;
        if (posYInput) posYInput.value = 50;
        updateExperienceImagePreview();

        if (fit === 'cover') {
            notify('🎯 Encuadre ajustado a Modelo / Experiencia (Cover)', 'info');
        } else {
            notify('🔍 Encuadre ajustado a Encajar Completo (Contain)', 'info');
        }
    }

    function resetExperienceImage(silent) {
        var fitSelect = document.getElementById('exp-fit');
        var zoomInput = document.getElementById('exp-zoom');
        var posXInput = document.getElementById('exp-pos-x');
        var posYInput = document.getElementById('exp-pos-y');
        if (fitSelect) fitSelect.value = 'cover';
        if (zoomInput) zoomInput.value = 1.0;
        if (posXInput) posXInput.value = 50;
        if (posYInput) posYInput.value = 50;
        updateExperienceImagePreview();
        if (!silent) {
            notify('↺ Ajustes de encuadre restablecidos', 'info');
        }
    }

    // Manejo de Tabs de Vista Previa (Tarjeta / Antes-Después)
    function switchPreviewTab(tab) {
        var btnCard = document.getElementById('exp-tab-btn-card');
        var btnBa = document.getElementById('exp-tab-btn-ba');
        var paneCard = document.getElementById('exp-preview-pane-card');
        var paneBa = document.getElementById('exp-preview-pane-ba');
        var subtitle = document.getElementById('exp-preview-subtitle');

        if (!btnCard || !btnBa || !paneCard || !paneBa) return;

        if (tab === 'card') {
            btnCard.className = 'flex-1 py-2 px-3 rounded-xl bg-isivi-gold text-isivi-black font-bold text-[11px] flex items-center justify-center gap-1.5 shadow transition';
            btnBa.className = 'flex-1 py-2 px-3 rounded-xl bg-stone-950 text-stone-400 font-bold text-[11px] flex items-center justify-center gap-1.5 hover:text-white transition';
            paneCard.classList.remove('hidden');
            paneBa.classList.add('hidden');
            if (subtitle) subtitle.textContent = 'Refleja exactamente el diseño y proporciones que verá el cliente en el sitio público.';
        } else {
            btnBa.className = 'flex-1 py-2 px-3 rounded-xl bg-isivi-gold text-isivi-black font-bold text-[11px] flex items-center justify-center gap-1.5 shadow transition';
            btnCard.className = 'flex-1 py-2 px-3 rounded-xl bg-stone-950 text-stone-400 font-bold text-[11px] flex items-center justify-center gap-1.5 hover:text-white transition';
            paneBa.classList.remove('hidden');
            paneCard.classList.add('hidden');
            if (subtitle) subtitle.textContent = 'Desliza el control interactivo para comparar visualmente los resultados Antes y Después.';
            updateBeforeAfterSlider(50);
        }
    }

    // Actualización del Slider Deslizable Interactivo Antes/Después (Admin Preview)
    function updateBeforeAfterSlider(value) {
        var val = parseInt(value, 10);
        if (isNaN(val)) val = 50;

        var wrapperBefore = document.getElementById('exp-ba-before-wrapper');
        var divider = document.getElementById('exp-ba-divider');
        var badge = document.getElementById('exp-ba-percentage-badge');
        var rangeInput = document.getElementById('exp-ba-range');

        if (rangeInput) rangeInput.value = val;
        if (wrapperBefore) wrapperBefore.style.width = val + '%';
        if (divider) divider.style.left = val + '%';
        if (badge) badge.textContent = val + '% ANTES / ' + (100 - val) + '% DESPUÉS';
    }

    // Actualización del Slider Deslizable Interactivo Antes/Después (Modal Cliente)
    function updateBeforeAfterSliderModal(value) {
        var val = parseInt(value, 10);
        if (isNaN(val)) val = 50;

        var wrapperBefore = document.getElementById('exp-modal-ba-before-wrapper');
        var divider = document.getElementById('exp-modal-ba-divider');
        var badge = document.getElementById('exp-modal-ba-percentage');
        var rangeInput = document.getElementById('exp-modal-ba-range');

        if (rangeInput) rangeInput.value = val;
        if (wrapperBefore) wrapperBefore.style.width = val + '%';
        if (divider) divider.style.left = val + '%';
        if (badge) badge.textContent = val + '% Antes / ' + (100 - val) + '% Después';
    }

    function updateExperienceCardPreview() {
        var nombre = document.getElementById('exp-nombre-cliente') ? document.getElementById('exp-nombre-cliente').value.trim() : '';
        var testimonio = document.getElementById('exp-testimonio') ? document.getElementById('exp-testimonio').value.trim() : '';
        var estrellas = document.getElementById('exp-estrellas') ? parseInt(document.getElementById('exp-estrellas').value, 10) || 5 : 5;
        var destacada = document.getElementById('exp-destacada') ? document.getElementById('exp-destacada').checked : false;

        var tipoRel = document.getElementById('exp-tipo-relacion') ? document.getElementById('exp-tipo-relacion').value : 'NINGUNA';
        var relSelect = document.getElementById('exp-relacion-select');

        var elNombre = document.getElementById('exp-card-preview-nombre');
        var elTestimonio = document.getElementById('exp-card-preview-testimonio');
        var elStars = document.getElementById('exp-card-preview-stars');
        var elBadge = document.getElementById('exp-card-preview-destacada-badge');
        var elRelBtn = document.getElementById('exp-card-preview-relacion-btn');
        var elRelLabel = document.getElementById('exp-card-preview-relacion-label');
        var elBeforeBtn = document.getElementById('exp-card-preview-before-btn');
        var elPreviewImg = document.getElementById('exp-card-preview-img');

        if (elNombre) elNombre.textContent = nombre || 'Cliente ISIVI';
        if (elTestimonio) elTestimonio.textContent = testimonio ? ('"' + testimonio + '"') : '"Escribe el testimonio del cliente..."';

        if (elStars) {
            var starsHtml = '';
            for (var i = 0; i < 5; i++) {
                if (i < estrellas) {
                    starsHtml += '<i class="fa-solid fa-star isivi-experiencias-star text-sm"></i>';
                } else {
                    starsHtml += '<i class="fa-regular fa-star text-stone-600 text-sm"></i>';
                }
            }
            elStars.innerHTML = starsHtml;
        }

        if (elBadge) {
            if (destacada) {
                elBadge.classList.remove('hidden');
            } else {
                elBadge.classList.add('hidden');
            }
        }

        if (elRelBtn) {
            var hiddenIdInput = document.getElementById('exp-relacion-id');
            var selectedId = hiddenIdInput && hiddenIdInput.value ? hiddenIdInput.value : (relSelect ? relSelect.value : '');
            if (tipoRel !== 'NINGUNA' && selectedId) {
                elRelBtn.innerHTML = renderCatalogRelationBadge(tipoRel, selectedId, true);
                elRelBtn.classList.remove('hidden');
            } else {
                elRelBtn.innerHTML = '';
                elRelBtn.classList.add('hidden');
            }
        }

        // Determinar imagen principal a mostrar (local upload o URL existente)
        var previewResBox = document.getElementById('exp-preview-resultado-img');
        var resUrlInput = document.getElementById('exp-imagen-resultado');
        var mainImgSrc = '';

        if (previewResBox && previewResBox.src && previewResBox.src.startsWith('data:image')) {
            mainImgSrc = previewResBox.src;
        } else if (resUrlInput && resUrlInput.value) {
            mainImgSrc = sanitizeUrl(resUrlInput.value);
        }

        if (elPreviewImg) {
            elPreviewImg.src = mainImgSrc || '/images/isivi-logo-transparent.png';
        }

        // Determinar si mostrar botón "Ver Antes / Después" en vista previa
        var previewAntesBox = document.getElementById('exp-preview-antes-img');
        var antesUrlInput = document.getElementById('exp-imagen-antes');
        var antesImgSrc = '';

        if (previewAntesBox && previewAntesBox.src && previewAntesBox.src.startsWith('data:image')) {
            antesImgSrc = previewAntesBox.src;
        } else if (antesUrlInput && antesUrlInput.value) {
            antesImgSrc = sanitizeUrl(antesUrlInput.value);
        }

        var hasAntes = Boolean(antesImgSrc && antesImgSrc !== '/images/isivi-logo-transparent.png');

        if (elBeforeBtn) {
            if (hasAntes) {
                elBeforeBtn.classList.remove('hidden');
            } else {
                elBeforeBtn.classList.add('hidden');
            }
        }

        // Actualizar fuentes de imagen en la comparativa Antes / Después (Tab 2)
        var baImgAfter = document.getElementById('exp-ba-img-after');
        var baImgBefore = document.getElementById('exp-ba-img-before');
        var baContainer = document.getElementById('exp-ba-interactive-container');
        var baEmptyMsg = document.getElementById('exp-ba-empty-msg');

        if (baImgAfter) baImgAfter.src = mainImgSrc || '/images/isivi-logo-transparent.png';
        if (baImgBefore) baImgBefore.src = antesImgSrc || '/images/isivi-logo-transparent.png';

        if (hasAntes) {
            if (baContainer) baContainer.classList.remove('hidden');
            if (baEmptyMsg) baEmptyMsg.classList.add('hidden');
        } else {
            if (baContainer) baContainer.classList.add('hidden');
            if (baEmptyMsg) baEmptyMsg.classList.remove('hidden');
        }

        updateExperienceImagePreview();
    }

    // Administración
    function loadAdminExperienciasList(isManual) {
        var token = sessionStorage.getItem('isivi_admin_token');
        if (!token) return;

        fetchCatalogForSelects();

        var headers = typeof window.getAuthHeaders === 'function' ? window.getAuthHeaders() : { 'Authorization': 'Bearer ' + token };
        fetch('/api/experiencias-isivi/admin', {
            headers: headers
        })
            .then(function (res) { return res.json(); })
            .then(function (data) {
                if (!Array.isArray(data)) return;
                state.experienciasAdmin = data;
                renderAdminExperienciasTable();
                if (isManual) {
                    notify('🔄 Lista de experiencias actualizada.', 'info');
                }
            })
            .catch(function (err) {
                console.error('Error cargando experiencias admin:', err);
                if (isManual) {
                    notify('❌ Error al actualizar la lista de experiencias.', 'error');
                }
            });
    }

    function applyFilters() {
        renderAdminExperienciasTable();
    }

    function renderAdminExperienciasTable() {
        var tbody = document.getElementById('admin-experiencias-table-body');
        var badge = document.getElementById('admin-experiencias-count-badge');
        var searchInput = document.getElementById('adm-exp-search');
        var filterStatus = document.getElementById('adm-exp-filter-status');
        var filterTipo = document.getElementById('adm-exp-filter-tipo');
        if (!tbody) return;

        var term = searchInput ? searchInput.value.trim().toLowerCase() : '';
        var statusVal = filterStatus ? filterStatus.value : '';
        var tipoVal = filterTipo ? filterTipo.value : '';

        var list = state.experienciasAdmin.filter(function (exp) {
            if (term) {
                var matchNombre = (exp.nombreMostrar || '').toLowerCase().indexOf(term) !== -1;
                var matchTestimonio = (exp.testimonio || '').toLowerCase().indexOf(term) !== -1;
                if (!matchNombre && !matchTestimonio) return false;
            }
            if (statusVal === 'ACTIVAS' && !exp.activa) return false;
            if (statusVal === 'INACTIVAS' && exp.activa) return false;
            if (statusVal === 'DESTACADAS' && !exp.destacada) return false;
            if (tipoVal && (exp.tipoRelacionado || 'NINGUNA') !== tipoVal) return false;
            return true;
        });

        if (badge) badge.textContent = list.length + ' registradas';
        tbody.innerHTML = '';

        if (list.length === 0) {
            tbody.innerHTML = '<tr><td colspan="8" class="p-6 text-center text-isivi-300">No se encontraron experiencias que coincidan con los filtros.</td></tr>';
            return;
        }

        list.forEach(function (exp) {
            var tr = document.createElement('tr');
            tr.className = 'hover:bg-stone-900/50 transition';

            var estadoBadge = exp.activa
                ? '<span class="px-2 py-0.5 rounded-full text-[10px] font-bold bg-emerald-950 text-emerald-400 border border-emerald-800">Activa</span>'
                : '<span class="px-2 py-0.5 rounded-full text-[10px] font-bold bg-rose-950 text-rose-400 border border-rose-800">Inactiva</span>';

            if (exp.destacada) {
                estadoBadge += ' <span class="px-2 py-0.5 rounded-full text-[10px] font-bold bg-amber-950 text-amber-400 border border-amber-800"><i class="fa-solid fa-star text-[9px]"></i></span>';
            }

            var authBadge = exp.autorizacionPublicacion
                ? '<span class="text-xs text-emerald-400"><i class="fa-solid fa-check"></i> Sí</span>'
                : '<span class="text-xs text-amber-400"><i class="fa-solid fa-triangle-exclamation"></i> No</span>';

            var imgSrc = sanitizeUrl(exp.imagenPrincipal || exp.imagenDespues);
            var thumbHtml = imgSrc
                ? '<div class="w-10 h-10 rounded-lg bg-stone-950 border border-stone-800 overflow-hidden flex-shrink-0"><img src="' + imgSrc + '" class="w-full h-full object-cover"></div>'
                : '<div class="w-10 h-10 rounded-lg bg-stone-900 border border-stone-800 flex items-center justify-center text-stone-600"><i class="fa-solid fa-image text-xs"></i></div>';

            tr.innerHTML =
                '<td class="p-3">' + thumbHtml + '</td>' +
                '<td class="p-3 font-medium text-white">' + escapeHtml(exp.nombreMostrar || '-') + '</td>' +
                '<td class="p-3 text-stone-400 max-w-xs truncate">' + escapeHtml(exp.testimonio || '-') + '</td>' +
                '<td class="p-3 text-isivi-gold font-bold">★ ' + (exp.calificacion || 5) + '</td>' +
                '<td class="p-3">' + escapeHtml(exp.tipoRelacionado || 'NINGUNA') + '</td>' +
                '<td class="p-3">' + authBadge + '</td>' +
                '<td class="p-3">' + estadoBadge + '</td>' +
                '<td class="p-3 text-right space-x-2">' +
                '<button onclick="window.IsiviExperienciasModule.openEditModal(\'' + escapeHtml(exp.id) + '\')" class="p-1.5 bg-stone-800 hover:bg-stone-700 text-isivi-gold rounded-lg transition" title="Editar"><i class="fa-solid fa-pen-to-square"></i></button>' +
                '<button onclick="window.IsiviExperienciasModule.deleteExperiencia(\'' + escapeHtml(exp.id) + '\')" class="p-1.5 bg-stone-800 hover:bg-rose-950 text-rose-400 rounded-lg transition" title="Eliminar"><i class="fa-solid fa-trash"></i></button>' +
                '</td>';

            tbody.appendChild(tr);
        });
    }

    // Manejadores de Archivos e Imágenes
    function handleFileChange(type) {
        var input = document.getElementById('exp-file-' + type);
        var err = document.getElementById('exp-form-error');
        if (!input || !input.files || input.files.length === 0) return;

        var file = input.files[0];

        // Validaciones cliente estándar (5MB, imagen)
        if (file.size > 5 * 1024 * 1024) {
            if (err) {
                err.textContent = 'La imagen no puede superar los 5 MB.';
                err.classList.remove('hidden');
            }
            notify('⚠️ La imagen no puede superar los 5 MB.', 'error');
            input.value = '';
            return;
        }

        var validTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/webp'];
        if (validTypes.indexOf(file.type.toLowerCase()) === -1) {
            if (err) {
                err.textContent = 'Solo se permiten imágenes JPG, PNG o WebP.';
                err.classList.remove('hidden');
            }
            notify('⚠️ Formato de archivo no válido. Usa JPG, PNG o WebP.', 'error');
            input.value = '';
            return;
        }

        if (err) err.classList.add('hidden');

        if (type === 'resultado') state.fileResultado = file;
        if (type === 'antes') state.fileAntes = file;

        // Preview local
        var reader = new FileReader();
        reader.onload = function (e) {
            var box = document.getElementById('exp-preview-' + type + '-box');
            var img = document.getElementById('exp-preview-' + type + '-img');
            var nameEl = document.getElementById('exp-preview-' + type + '-name');
            var sizeEl = document.getElementById('exp-preview-' + type + '-size');

            if (img) img.src = e.target.result;
            if (nameEl) nameEl.textContent = file.name;
            if (sizeEl) sizeEl.textContent = formatBytes(file.size);
            if (box) box.classList.remove('hidden');

            var fotoLabel = (type === 'resultado' ? 'principal' : 'comparativa (Antes)');
            notify('📷 Foto ' + fotoLabel + ' cargada en vista previa.', 'info');

            updateExperienceCardPreview();
        };
        reader.readAsDataURL(file);
    }

    function clearFile(type, silent) {
        var input = document.getElementById('exp-file-' + type);
        var box = document.getElementById('exp-preview-' + type + '-box');
        var img = document.getElementById('exp-preview-' + type + '-img');
        var urlInput = document.getElementById(type === 'resultado' ? 'exp-imagen-resultado' : 'exp-imagen-antes');

        if (input) input.value = '';
        if (urlInput) urlInput.value = '';
        if (img) img.src = '';
        if (box) {
            if (!box.classList.contains('hidden') && !silent) {
                var fotoLabel = (type === 'resultado' ? 'principal' : 'comparativa (Antes)');
                notify('🗑️ Foto ' + fotoLabel + ' removida.', 'info');
            }
            box.classList.add('hidden');
        }

        if (type === 'resultado') state.fileResultado = null;
        if (type === 'antes') state.fileAntes = null;

        updateExperienceCardPreview();
    }

    function showExistingImagePreview(type, url) {
        if (!url) return;
        var box = document.getElementById('exp-preview-' + type + '-box');
        var img = document.getElementById('exp-preview-' + type + '-img');
        var nameEl = document.getElementById('exp-preview-' + type + '-name');
        var sizeEl = document.getElementById('exp-preview-' + type + '-size');

        if (img) img.src = sanitizeUrl(url);
        if (nameEl) nameEl.textContent = 'Imagen guardada (' + type + ')';
        if (sizeEl) sizeEl.textContent = 'Servidor Cloudinary';
        if (box) box.classList.remove('hidden');
    }

    function updateExperienceImagePreview() {
        var fitSelect = document.getElementById('exp-fit');
        var zoomInput = document.getElementById('exp-zoom');
        var posXInput = document.getElementById('exp-pos-x');
        var posYInput = document.getElementById('exp-pos-y');

        var zoomValLabel = document.getElementById('exp-zoom-val');
        var posXValLabel = document.getElementById('exp-pos-x-val');
        var posYValLabel = document.getElementById('exp-pos-y-val');

        var fit = fitSelect ? fitSelect.value : 'cover';
        var zoom = zoomInput ? zoomInput.value : '1.0';
        var posX = posXInput ? posXInput.value : '50';
        var posY = posYInput ? posYInput.value : '50';

        if (zoomValLabel) zoomValLabel.textContent = parseFloat(zoom).toFixed(2) + 'x';
        if (posXValLabel) posXValLabel.textContent = posX + '%';
        if (posYValLabel) posYValLabel.textContent = posY + '%';

        var previewImg = document.getElementById('exp-card-preview-img');
        if (previewImg) {
            previewImg.style.objectFit = fit;
            previewImg.style.transform = 'scale(' + zoom + ')';
            previewImg.style.transformOrigin = posX + '% ' + posY + '%';
            previewImg.style.objectPosition = posX + '% ' + posY + '%';
        }
    }

    // Manejador del Selector de Relación
    function handleTipoRelacionChange(selectedItemId) {
        var tipoSelect = document.getElementById('exp-tipo-relacion');
        var container = document.getElementById('exp-relacion-select-container');
        var itemSelect = document.getElementById('exp-relacion-select');
        var label = document.getElementById('exp-relacion-label');
        var hiddenIdInput = document.getElementById('exp-relacion-id');

        if (!tipoSelect || !container || !itemSelect) return;

        var tipo = tipoSelect.value;
        itemSelect.innerHTML = '';

        if (tipo === 'NINGUNA') {
            container.classList.add('hidden');
            if (hiddenIdInput) hiddenIdInput.value = '';
            updateExperienceCardPreview();
            return;
        }

        container.classList.remove('hidden');
        var items = [];
        if (tipo === 'PRODUCTO') {
            if (label) label.textContent = 'Seleccionar Producto *';
            items = (state.catalogoProductos && state.catalogoProductos.length > 0)
                ? state.catalogoProductos
                : (window.productsData || []);
        } else if (tipo === 'KIT') {
            if (label) label.textContent = 'Seleccionar Kit *';
            items = (state.catalogoKits && state.catalogoKits.length > 0)
                ? state.catalogoKits
                : (window.kitsData || []);
        } else if (tipo === 'SERVICIO') {
            if (label) label.textContent = 'Seleccionar Servicio *';
            items = (state.catalogoServicios && state.catalogoServicios.length > 0)
                ? state.catalogoServicios
                : (window.servicesData || []);
        }

        var currentSelectedId = selectedItemId || (hiddenIdInput ? hiddenIdInput.value : '');

        if (items.length === 0) {
            itemSelect.innerHTML = '<option value="">Cargando opciones...</option>';
        } else {
            itemSelect.innerHTML = '<option value="">-- Seleccionar --</option>';
            items.forEach(function (item) {
                var opt = document.createElement('option');
                opt.value = item.id;
                opt.textContent = item.nombre || item.name || 'Elemento';
                if (currentSelectedId && String(currentSelectedId) === String(item.id)) {
                    opt.selected = true;
                }
                itemSelect.appendChild(opt);
            });
        }

        if (currentSelectedId && hiddenIdInput) {
            hiddenIdInput.value = currentSelectedId;
        }

        updateExperienceCardPreview();
    }

    function handleItemSelectChange() {
        var itemSelect = document.getElementById('exp-relacion-select');
        var hiddenIdInput = document.getElementById('exp-relacion-id');
        if (itemSelect && hiddenIdInput) {
            hiddenIdInput.value = itemSelect.value;
        }
        updateExperienceCardPreview();
    }

    function uploadImageFile(file) {
        return new Promise(function (resolve, reject) {
            if (!file) {
                resolve(null);
                return;
            }

            var formData = new FormData();
            formData.append('file', file);

            var headers = typeof window.getAuthHeaders === 'function' ? window.getAuthHeaders() : {};
            fetch('/api/uploads/imagen', {
                method: 'POST',
                headers: headers,
                body: formData
            })
                .then(function (res) {
                    if (!res.ok) {
                        return res.json().then(function (d) { throw new Error(d.mensaje || 'Error subiendo imagen'); });
                    }
                    return res.json();
                })
                .then(function (data) {
                    resolve(data.secure_url || data.url || null);
                })
                .catch(function (err) {
                    reject(err);
                });
        });
    }

    function openNewModal(silent) {
        var err = document.getElementById('exp-form-error');

        fetchCatalogForSelects();

        if (err) err.classList.add('hidden');
        var title = document.getElementById('exp-modal-title');
        if (title) title.innerHTML = '<i class="fa-solid fa-star text-isivi-gold"></i> <span>CREAR NUEVA EXPERIENCIA ISIVI</span>';

        document.getElementById('exp-id').value = '';
        document.getElementById('exp-nombre-cliente').value = '';
        document.getElementById('exp-testimonio').value = '';
        document.getElementById('exp-estrellas').value = '5';
        document.getElementById('exp-orden').value = '0';

        clearFile('resultado', true);
        clearFile('antes', true);

        document.getElementById('exp-tipo-relacion').value = 'NINGUNA';
        handleTipoRelacionChange();

        document.getElementById('exp-activa').checked = true;
        document.getElementById('exp-autoriza-publicacion').checked = true;
        document.getElementById('exp-destacada').checked = false;

        resetExperienceImage(true);
        updateExperienceCardPreview();

        var editorPanel = document.getElementById('exp-editor-panel');
        if (editorPanel && typeof editorPanel.scrollIntoView === 'function') {
            editorPanel.scrollIntoView({ behavior: 'smooth', block: 'start' });
        }

        if (!silent) {
            notify('✨ Formulario listo para crear una nueva experiencia.', 'info');
        }
    }

    function openEditModal(id) {
        var exp = state.experienciasAdmin.find(function (e) { return e.id === id; });
        if (!exp) return;

        var err = document.getElementById('exp-form-error');

        fetchCatalogForSelects();

        if (err) err.classList.add('hidden');
        var title = document.getElementById('exp-modal-title');
        if (title) title.innerHTML = '<i class="fa-solid fa-pen-to-square text-isivi-gold"></i> <span>EDITAR EXPERIENCIA ISIVI (#' + id + ')</span>';

        document.getElementById('exp-id').value = exp.id || '';
        document.getElementById('exp-nombre-cliente').value = exp.nombreMostrar || '';
        document.getElementById('exp-testimonio').value = exp.testimonio || '';
        document.getElementById('exp-estrellas').value = exp.calificacion || 5;
        document.getElementById('exp-orden').value = exp.orden || 0;

        clearFile('resultado', true);
        clearFile('antes', true);

        var resUrl = exp.imagenPrincipal || exp.imagenDespues || '';
        var antesUrl = exp.imagenAntes || '';

        document.getElementById('exp-imagen-resultado').value = resUrl;
        document.getElementById('exp-imagen-antes').value = antesUrl;

        showExistingImagePreview('resultado', resUrl);
        showExistingImagePreview('antes', antesUrl);

        document.getElementById('exp-tipo-relacion').value = exp.tipoRelacionado || 'NINGUNA';
        handleTipoRelacionChange(exp.elementoRelacionadoId);

        document.getElementById('exp-activa').checked = Boolean(exp.activa);
        document.getElementById('exp-autoriza-publicacion').checked = Boolean(exp.autorizacionPublicacion);
        document.getElementById('exp-destacada').checked = Boolean(exp.destacada);

        var fit = exp.fit || 'cover';
        var zoom = exp.zoom != null ? exp.zoom : 1.0;
        var posX = exp.posX != null ? exp.posX : 50;
        var posY = exp.posY != null ? exp.posY : 50;

        if (document.getElementById('exp-fit')) document.getElementById('exp-fit').value = fit;
        if (document.getElementById('exp-zoom')) document.getElementById('exp-zoom').value = zoom;
        if (document.getElementById('exp-pos-x')) document.getElementById('exp-pos-x').value = posX;
        if (document.getElementById('exp-pos-y')) document.getElementById('exp-pos-y').value = posY;

        updateExperienceImagePreview();
        updateExperienceCardPreview();

        var editorPanel = document.getElementById('exp-editor-panel');
        if (editorPanel && typeof editorPanel.scrollIntoView === 'function') {
            editorPanel.scrollIntoView({ behavior: 'smooth', block: 'start' });
        }

        notify('✏️ Editando Experiencia ISIVI de ' + (exp.nombreMostrar || 'cliente'), 'info');
    }

    function closeModal(isUserAction) {
        openNewModal(true);
        if (isUserAction) {
            notify('🔄 Formulario restablecido.', 'info');
        }
    }

    function saveExperiencia(event) {
        if (event) event.preventDefault();

        var token = sessionStorage.getItem('isivi_admin_token');
        if (!token) return;

        var err = document.getElementById('exp-form-error');
        var saveBtn = document.getElementById('btn-save-experiencia');
        if (err) err.classList.add('hidden');

        var isEdit = Boolean(document.getElementById('exp-id').value);

        // Validaciones previas
        var nombre = document.getElementById('exp-nombre-cliente').value.trim();
        var testimonio = document.getElementById('exp-testimonio').value.trim();
        var activa = document.getElementById('exp-activa').checked;
        var autoriza = document.getElementById('exp-autoriza-publicacion').checked;
        var tipoRel = document.getElementById('exp-tipo-relacion').value;
        var relId = document.getElementById('exp-relacion-id').value;

        if (!nombre) {
            if (err) { err.textContent = 'El nombre del cliente es obligatorio.'; err.classList.remove('hidden'); }
            notify('⚠️ El nombre del cliente es obligatorio.', 'error');
            return;
        }
        if (!testimonio) {
            if (err) { err.textContent = 'El testimonio es obligatorio.'; err.classList.remove('hidden'); }
            notify('⚠️ El testimonio es obligatorio.', 'error');
            return;
        }
        if (tipoRel !== 'NINGUNA' && !relId) {
            if (err) { err.textContent = 'Debes seleccionar el elemento del catálogo relacionado.'; err.classList.remove('hidden'); }
            notify('⚠️ Debes seleccionar el elemento relacionado del catálogo.', 'error');
            return;
        }
        if (activa && !autoriza) {
            if (err) { err.textContent = 'No se puede activar una experiencia sin la autorización explícita de publicación del cliente.'; err.classList.remove('hidden'); }
            notify('⚠️ No se puede activar una experiencia sin autorización explícita del cliente.', 'error');
            return;
        }

        // Protección contra Doble Submit
        if (saveBtn) {
            if (saveBtn.disabled) return;
            saveBtn.disabled = true;
            saveBtn.dataset.originalHtml = saveBtn.innerHTML;
            saveBtn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> <span>Subiendo imágenes / Guardando...</span>';
        }

        function restoreButton() {
            if (saveBtn) {
                saveBtn.disabled = false;
                if (saveBtn.dataset.originalHtml) {
                    saveBtn.innerHTML = saveBtn.dataset.originalHtml;
                }
            }
        }

        // Subir fotos seleccionadas si existen
        Promise.all([
            uploadImageFile(state.fileResultado),
            uploadImageFile(state.fileAntes)
        ])
            .then(function (urls) {
                var urlResultadoUploaded = urls[0];
                var urlAntesUploaded = urls[1];

                var finalImgResultado = urlResultadoUploaded || document.getElementById('exp-imagen-resultado').value;
                var finalImgAntes = urlAntesUploaded || document.getElementById('exp-imagen-antes').value;

                if (!finalImgResultado) {
                    throw new Error('La foto principal de resultado es obligatoria.');
                }

                var payload = {
                    id: document.getElementById('exp-id').value || null,
                    nombreMostrar: nombre,
                    testimonio: testimonio,
                    calificacion: parseInt(document.getElementById('exp-estrellas').value) || 5,
                    orden: parseInt(document.getElementById('exp-orden').value) || 0,
                    imagenPrincipal: finalImgResultado,
                    imagenDespues: finalImgResultado,
                    imagenAntes: finalImgAntes,
                    tipoRelacionado: tipoRel,
                    elementoRelacionadoId: relId,
                    activa: activa,
                    autorizacionPublicacion: autoriza,
                    destacada: document.getElementById('exp-destacada').checked,
                    fit: document.getElementById('exp-fit') ? document.getElementById('exp-fit').value : 'cover',
                    zoom: document.getElementById('exp-zoom') ? parseFloat(document.getElementById('exp-zoom').value) || 1.0 : 1.0,
                    posX: document.getElementById('exp-pos-x') ? parseInt(document.getElementById('exp-pos-x').value, 10) || 50 : 50,
                    posY: document.getElementById('exp-pos-y') ? parseInt(document.getElementById('exp-pos-y').value, 10) || 50 : 50
                };

                var headers = typeof window.getAuthHeaders === 'function' ? window.getAuthHeaders({ 'Content-Type': 'application/json' }) : { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + token };
                return fetch('/api/experiencias-isivi', {
                    method: 'POST',
                    headers: headers,
                    body: JSON.stringify(payload)
                });
            })
            .then(function (res) {
                if (!res.ok) {
                    return res.json().then(function (data) { throw new Error(data.error || 'Error al guardar la experiencia'); });
                }
                return res.json();
            })
            .then(function () {
                restoreButton();
                openNewModal(true);
                loadAdminExperienciasList();
                loadPublicExperiencias();
                if (isEdit) {
                    notify('✅ Experiencia ISIVI actualizada con éxito.', 'success');
                } else {
                    notify('✨ Experiencia ISIVI creada y guardada con éxito.', 'success');
                }
            })
            .catch(function (error) {
                restoreButton();
                if (err) {
                    err.textContent = error.message;
                    err.classList.remove('hidden');
                }
                notify('❌ ' + (error.message || 'Error al guardar la experiencia.'), 'error');
            });
    }

    function deleteExperiencia(id) {
        if (!confirm('¿Estás seguro de que deseas eliminar esta experiencia?')) return;

        var token = sessionStorage.getItem('isivi_admin_token');
        if (!token) return;

        var headers = typeof window.getAuthHeaders === 'function' ? window.getAuthHeaders() : { 'Authorization': 'Bearer ' + token };
        fetch('/api/experiencias-isivi/' + id, {
            method: 'DELETE',
            headers: headers
        })
            .then(function (res) {
                if (res.ok) {
                    loadAdminExperienciasList();
                    loadPublicExperiencias();
                    notify('🗑️ Experiencia ISIVI eliminada correctamente.', 'success');
                } else {
                    notify('❌ Error al eliminar la experiencia.', 'error');
                }
            })
            .catch(function (err) {
                console.error('Error eliminando experiencia:', err);
                notify('❌ Error al conectar para eliminar la experiencia.', 'error');
            });
    }

    function openBeforeAfterModal(expId) {
        var modal = document.getElementById('modal-exp-before-after');
        var elDespues = document.getElementById('exp-modal-img-despues');
        var elAntes = document.getElementById('exp-modal-img-antes');
        if (!modal || !expId) return;

        var expFound = null;
        for (var i = 0; i < state.experienciasPublicas.length; i++) {
            var item = state.experienciasPublicas[i];
            var itemId = item.id || item._id;
            if (itemId && String(itemId) === String(expId)) {
                expFound = item;
                break;
            }
        }

        if (!expFound) {
            console.warn('Experiencia no encontrada para el ID especificado:', expId);
            return;
        }

        var despuesUrl = expFound.imagenPrincipal || expFound.imagenDespues || '';
        var antesUrl = expFound.imagenAntes || '';

        if (!despuesUrl && !antesUrl) {
            console.warn('La experiencia no contiene imágenes comparativas.');
            return;
        }

        // Reset previo para evitar arrastre de imágenes de experiencias anteriores
        if (elDespues) elDespues.src = '';
        if (elAntes) elAntes.src = '';

        if (elDespues) elDespues.src = sanitizeUrl(despuesUrl);
        if (elAntes) elAntes.src = sanitizeUrl(antesUrl);

        updateBeforeAfterSliderModal(50);

        modal.classList.remove('hidden');
        modal.classList.add('flex');
        modal.setAttribute('aria-hidden', 'false');
    }

    function closeBeforeAfterModal() {
        var modal = document.getElementById('modal-exp-before-after');
        var elDespues = document.getElementById('exp-modal-img-despues');
        var elAntes = document.getElementById('exp-modal-img-antes');
        if (modal) {
            modal.classList.add('hidden');
            modal.classList.remove('flex');
            modal.setAttribute('aria-hidden', 'true');
        }
        if (elDespues) elDespues.src = '';
        if (elAntes) elAntes.src = '';
    }

    // Auto-init al cargar documento y listener de tecla Escape para modal Antes/Después
    document.addEventListener('DOMContentLoaded', function () {
        loadPublicExperiencias();
        fetchCatalogForSelects();
    });

    document.addEventListener('keydown', function (e) {
        if (e.key === 'Escape' || e.keyCode === 27) {
            var modal = document.getElementById('modal-exp-before-after');
            if (modal && !modal.classList.contains('hidden')) {
                closeBeforeAfterModal();
            }
        }
    });

    // Namespace Global Autónomo
    window.IsiviExperienciasModule = {
        loadPublicExperiencias: loadPublicExperiencias,
        loadAdminExperienciasList: loadAdminExperienciasList,
        openNewModal: openNewModal,
        openEditModal: openEditModal,
        closeModal: closeModal,
        saveExperiencia: saveExperiencia,
        deleteExperiencia: deleteExperiencia,
        openRelatedCatalogItem: openRelatedCatalogItem,
        handleFileChange: handleFileChange,
        clearFile: clearFile,
        handleTipoRelacionChange: handleTipoRelacionChange,
        handleItemSelectChange: handleItemSelectChange,
        prevExperience: prevExperience,
        nextExperience: nextExperience,
        applyFilters: applyFilters,
        openBeforeAfterModal: openBeforeAfterModal,
        closeBeforeAfterModal: closeBeforeAfterModal,
        updateExperienceImagePreview: updateExperienceImagePreview,
        updateExperienceCardPreview: updateExperienceCardPreview,
        setExperienceFitPreset: setExperienceFitPreset,
        resetExperienceImage: resetExperienceImage,
        switchPreviewTab: switchPreviewTab,
        updateBeforeAfterSlider: updateBeforeAfterSlider,
        updateBeforeAfterSliderModal: updateBeforeAfterSliderModal
    };

    // Funciones globales requeridas por handlers HTML inline
    window.loadAdminExperienciasList = loadAdminExperienciasList;
    window.openAdminExperienciaModal = openNewModal;
    window.closeAdminExperienciaModal = closeModal;
    window.saveAdminExperiencia = saveExperiencia;

})(window, document);
