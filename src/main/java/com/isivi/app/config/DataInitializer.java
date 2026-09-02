package com.isivi.app.config;

import com.isivi.app.model.Kit;
import com.isivi.app.model.Banner;
import com.isivi.app.model.Administrador;
import com.isivi.app.model.Producto;
import com.isivi.app.model.Servicio;
import com.isivi.app.model.CategoriaServicio;
import com.isivi.app.repository.KitRepository;
import com.isivi.app.repository.BannerRepository;
import com.isivi.app.repository.AdministradorRepository;
import com.isivi.app.repository.ProductoRepository;
import com.isivi.app.repository.ServicioRepository;
import com.isivi.app.repository.CategoriaServicioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.beans.factory.annotation.Value;

import com.isivi.app.repository.CategoriaProductoRepository;

@Configuration
public class DataInitializer {

    @Value("${isivi.admin.initial-password}")
    private String initialAdminPassword;

    @org.springframework.context.annotation.Bean
    public CommandLineRunner cargarDatosIniciales(ProductoRepository productoRepo,
                                            KitRepository kitRepo,
                                            ServicioRepository servicioRepo,
                                            CategoriaServicioRepository categoriaServicioRepo,
                                            AdministradorRepository administradorRepo,
                                            BannerRepository bannerRepo,
                                            CategoriaProductoRepository categoriaProductoRepo) {
        return args -> {
            // Migrar categorías de producto existentes sin tipo asignado
            categoriaProductoRepo.findAll().forEach(cat -> {
                if (cat.getTipo() == null || cat.getTipo().isBlank()) {
                    long prodCount = productoRepo.countByCategoriaId(cat.getId());
                    long kitCount = kitRepo.countByCategoriaId(cat.getId());
                    if (prodCount > 0 && kitCount == 0) {
                        cat.setTipo("PRODUCTO");
                        categoriaProductoRepo.save(cat);
                    } else if (kitCount > 0 && prodCount == 0) {
                        cat.setTipo("KIT");
                        categoriaProductoRepo.save(cat);
                    } else if (prodCount > 0 && kitCount > 0) {
                        System.out.println("COMPATIBILIDAD REPORTADA: Categoría '" + cat.getNombre() + "' (" + cat.getId() + ") está asociada a productos y kits simultáneamente. Se mantiene sin tipo para compatibilidad dual.");
                    } else {
                        cat.setTipo("PRODUCTO");
                        categoriaProductoRepo.save(cat);
                    }
                }
            });

            if (categoriaServicioRepo.count() == 0) {
                categoriaServicioRepo.save(new CategoriaServicio("tratamientos", "Tratamientos Capilares ISIVI"));
                categoriaServicioRepo.save(new CategoriaServicio("corte", "Corte & Estilo"));
                categoriaServicioRepo.save(new CategoriaServicio("color", "Color & Alisados"));
            }
            if (administradorRepo.findByUsuario("duvan").isEmpty() && !initialAdminPassword.isBlank()) {
                administradorRepo.save(new Administrador("duvan", new BCryptPasswordEncoder().encode(initialAdminPassword)));
            }
            if (bannerRepo.count() == 0) {
                bannerRepo.save(new Banner("Brilla con tu mejor versión", "Agenda tu tratamiento capilar y recibe una hidratación de cortesía.",
                        "https://images.unsplash.com/photo-1560066984-138dadb4c035?auto=format&fit=crop&w=1400&q=85", "Agenda ahora", "#servicios", true));
            }
            if (productoRepo.count() == 0) {
                productoRepo.save(new Producto("Shampoo ISIVI (450ml)", 27900.0,
                        "Fortalece el cuero cabelludo, aporta crecimiento y limpia suavemente.",
                        "https://images.unsplash.com/photo-1585232351009-aa87416fec90?auto=format&fit=crop&w=600&q=80", true));
                productoRepo.save(new Producto("Acondicionador ISIVI (450ml)", 27900.0,
                        "Desenreda, aporta brillo y suavidad extrema.",
                        "https://images.unsplash.com/photo-1535585209827-a15fcdbc4c2d?auto=format&fit=crop&w=600&q=80", true));
                productoRepo.save(new Producto("Crema para Peinar (500ml)", 32000.0,
                        "Define, hidrata, repara y protege la hebra capilar.",
                        "https://images.unsplash.com/photo-1608248597309-10018a770838?auto=format&fit=crop&w=600&q=80", true));
                productoRepo.save(new Producto("Tratamiento Capilar (250ml)", 20000.0,
                        "Reparacion intensiva para cabellos maltratados o secos.",
                        "https://images.unsplash.com/photo-1620916566398-39f1143ab7be?auto=format&fit=crop&w=600&q=80", true));
                productoRepo.save(new Producto("Gel Fluido ISIVI (250ml)", 22000.0,
                        "Moldea, define rizos u ondas y aporta alto brillo.",
                        "https://images.unsplash.com/photo-1526947425960-945c6e72858f?auto=format&fit=crop&w=600&q=80", true));
                productoRepo.save(new Producto("Tonico Capilar (60ml)", 20000.0,
                        "Estimula el crecimiento rapido y detiene la caida.",
                        "https://images.unsplash.com/photo-1601049541289-9b1b7bbbfe19?auto=format&fit=crop&w=600&q=80", true));
                productoRepo.save(new Producto("Aceite de Coco Organico (130ml)", 15000.0,
                        "Nutricion pura, sellado de puntas y brillo radiante.",
                        "https://images.unsplash.com/photo-1608248597309-10018a770838?auto=format&fit=crop&w=600&q=80", true));
            }

            // Completa el catálogo de demostración sin eliminar los productos existentes.
            if (productoRepo.count() < 12) {
                productoRepo.save(new Producto("Mascarilla Nutritiva de Aguacate", 35000.0,
                        "Hidratación profunda para recuperar suavidad, brillo y elasticidad.",
                        "https://images.unsplash.com/photo-1556228720-195a672e8a03?auto=format&fit=crop&w=600&q=80", true, 12));
                productoRepo.save(new Producto("Protector Térmico ISIVI", 28000.0,
                        "Protege el cabello del secador y la plancha sin dejar sensación pesada.",
                        "https://images.unsplash.com/photo-1620916566398-39f1143ab7be?auto=format&fit=crop&w=600&q=80", true, 10));
                productoRepo.save(new Producto("Serum Reparador de Puntas", 30000.0,
                        "Sella puntas abiertas y aporta un acabado sedoso y luminoso.",
                        "https://images.unsplash.com/photo-1612817288484-6f916006741a?auto=format&fit=crop&w=600&q=80", true, 8));
                productoRepo.save(new Producto("Shampoo Matizante Violeta", 32000.0,
                        "Neutraliza tonos amarillos y mantiene el color rubio o gris radiante.",
                        "https://images.unsplash.com/photo-1571781926291-c477ebfd024b?auto=format&fit=crop&w=600&q=80", true, 9));
                productoRepo.save(new Producto("Spray Brillo Natural", 24000.0,
                        "Finalizador ligero con brillo instantáneo y aroma floral.",
                        "https://images.unsplash.com/photo-1526947425960-945c6e72858f?auto=format&fit=crop&w=600&q=80", true, 15));
            }

            if (kitRepo.count() == 0) {
                kitRepo.save(new Kit("Kit Crecimiento ISIVI", 38000.0,
                        "Estimula el crecimiento, reduce la caida y nutre profundamente.",
                        "https://images.unsplash.com/photo-1535585209827-a15fcdbc4c2d?auto=format&fit=crop&w=600&q=80", true));
                kitRepo.save(new Kit("Kit Lavado Nutritivo", 52000.0,
                        "Limpia, fortalece y restaura la hebra capilar.",
                        "https://images.unsplash.com/photo-1585232351009-aa87416fec90?auto=format&fit=crop&w=600&q=80", true));
                kitRepo.save(new Kit("Kit Liso Perfecto", 93000.0,
                        "Libre de sal y sulfatos. Prolonga la duracion de keratina.",
                        "https://images.unsplash.com/photo-1519699047748-de8e457a634e?auto=format&fit=crop&w=600&q=80", true));
                kitRepo.save(new Kit("Kit Completo ISIVI", 160000.0,
                        "Incluye Shampoo, Acondicionador, Crema de Peinar, Gel, Tonico y Tratamiento.",
                        "https://images.unsplash.com/photo-1562322140-8baeececf3df?auto=format&fit=crop&w=600&q=80", true));
            }

            if (kitRepo.count() < 7) {
                kitRepo.save(new Kit("Kit Rizos Definidos", 78000.0,
                        "Shampoo, crema para peinar y gel fluido para una definición duradera.",
                        "https://images.unsplash.com/photo-1595476108010-b4d1f102b1b1?auto=format&fit=crop&w=600&q=80", true, 8));
                kitRepo.save(new Kit("Kit Reparación Profunda", 89000.0,
                        "Tratamiento, mascarilla y serum para revitalizar cabello reseco.",
                        "https://images.unsplash.com/photo-1522337360788-8b13dee7a37e?auto=format&fit=crop&w=600&q=80", true, 6));
                kitRepo.save(new Kit("Kit Color Protegido", 72000.0,
                        "Limpieza y protección para prolongar la intensidad del color.",
                        "https://images.unsplash.com/photo-1519699047748-de8e457a634e?auto=format&fit=crop&w=600&q=80", true, 7));
            }

            if (servicioRepo.count() == 0) {
                servicioRepo.save(new Servicio("Repolarizacion Intensiva ISIVI", "tratamientos", 75000.0, "75 min",
                        "Nutricion profunda con mascarilla y sellado termico natural.",
                        "https://images.unsplash.com/photo-1522337360788-8b13dee7a37e?auto=format&fit=crop&w=600&q=80"));
                servicioRepo.save(new Servicio("Alisado & Keratina Organica", "color", 180000.0, "150 min",
                        "Alisado sin formol que aporta sedosidad y brillo prolongado.",
                        "https://images.unsplash.com/photo-1519699047748-de8e457a634e?auto=format&fit=crop&w=600&q=80"));
                servicioRepo.save(new Servicio("Corte Disenado + Lavado Nutritivo", "corte", 45000.0, "60 min",
                        "Asesoria de visagismo, lavado completo + secado.",
                        "https://images.unsplash.com/photo-1595476108010-b4d1f102b1b1?auto=format&fit=crop&w=600&q=80"));
                servicioRepo.save(new Servicio("Terapia Crecimiento Anti-Caida", "tratamientos", 60000.0, "60 min",
                        "Exfoliacion suave de cuero cabelludo con tonico ISIVI.",
                        "https://images.unsplash.com/photo-1527799820374-dcf8d9d4a388?auto=format&fit=crop&w=600&q=80"));
            }

            if (servicioRepo.count() < 9) {
                servicioRepo.save(new Servicio("Hidratación Express", "tratamientos", 40000.0, "45 min",
                        "Lavado, mascarilla nutritiva y masaje capilar para devolver el brillo.",
                        "https://images.unsplash.com/photo-1560066984-138dadb4c035?auto=format&fit=crop&w=600&q=80"));
                servicioRepo.save(new Servicio("Coloración Completa", "color", 145000.0, "180 min",
                        "Aplicación de color, tratamiento protector y peinado final.",
                        "https://images.unsplash.com/photo-1562322140-8baeececf3df?auto=format&fit=crop&w=600&q=80"));
                servicioRepo.save(new Servicio("Balayage Iluminador", "color", 220000.0, "210 min",
                        "Iluminaciones personalizadas para un resultado natural y luminoso.",
                        "https://images.unsplash.com/photo-1519699047748-de8e457a634e?auto=format&fit=crop&w=600&q=80"));
                servicioRepo.save(new Servicio("Peinado para Evento", "corte", 55000.0, "60 min",
                        "Peinado profesional para celebraciones, sesiones y ocasiones especiales.",
                        "https://images.unsplash.com/photo-1522337360788-8b13dee7a37e?auto=format&fit=crop&w=600&q=80"));
                servicioRepo.save(new Servicio("Corte Infantil", "corte", 30000.0, "40 min",
                        "Corte cómodo y personalizado para los más pequeños.",
                        "https://images.unsplash.com/photo-1595476108010-b4d1f102b1b1?auto=format&fit=crop&w=600&q=80"));
            }
        };
    }
}
