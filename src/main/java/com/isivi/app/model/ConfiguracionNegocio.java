package com.isivi.app.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Document(collection = "configuracion_negocio")
public class ConfiguracionNegocio {

    @Id
    private String id = "principal";

    private String ciudad = "Cartagena";
    private String pais = "Colombia";
    private String diasAtencion = "Mar - Sáb";
    private String horaApertura = "08:00";
    private String horaCierre = "19:00";
    private String telefonoMayorista = "+57 300 962 3174";

    private String tituloSitio = "ISIVI Beauty & Care";
    private String eslogan = "Belleza artesanal, hecha para ti";
    private String descripcion = "Descubre nuestros servicios, productos y kits de belleza.";
    private String tituloHero = "Tu belleza, nuestra pasión";
    private String descripcionHero = "Servicios y productos seleccionados para cuidar de ti.";
    private String textoBotonHero = "Descubrir servicios";
    private String nombreComercial = "ISIVI Beauty & Care";
    private String whatsapp = "+57 300 123 4567";
    private String direccion = "Cartagena, Bolívar";
    private String horarioAtencion = "Lunes a sábado · 9:00 AM – 7:00 PM";
    private String mensajeWhatsApp = "Hola, quiero obtener información sobre los servicios de ISIVI.";
    private String textoFooter = "Belleza artesanal y experiencias diseñadas para ti.";

    private String whatsappAdminNumero = "";
    private boolean whatsappAdminHabilitado = false;
    private boolean notificarNuevaCita = true;
    private boolean notificarNuevaCompra = true;
    private boolean notificarSolicitudReprogramacion = true;
    private boolean notificarSolicitudCancelacion = true;
    private boolean notificarAtencionAhora = true;
    private boolean notificarPagoAprobado = false;
    private boolean notificarPagoRechazado = false;
    private boolean notificarConflictoPago = true;

    private String numeroBancolombia = "300-894-9050";
    private String titularBancolombia = "ISIVI Belleza Natural";
    private String numeroNequi = "3008949050";
    private String titularNequi = "ISIVI Capilar";
    private String numeroDaviplata = "3008949050";
    private String titularDaviplata = "ISIVI Capilar";

    public ConfiguracionNegocio() {
    }

    public ConfiguracionNegocio(String ciudad, String pais, String diasAtencion, String horaApertura, String horaCierre, String telefonoMayorista) {
        this.ciudad = ciudad;
        this.pais = pais;
        this.diasAtencion = diasAtencion;
        this.horaApertura = horaApertura;
        this.horaCierre = horaCierre;
        this.telefonoMayorista = telefonoMayorista;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public String getPais() {
        return pais;
    }

    public void setPais(String pais) {
        this.pais = pais;
    }

    public String getDiasAtencion() {
        return diasAtencion;
    }

    public void setDiasAtencion(String diasAtencion) {
        this.diasAtencion = diasAtencion;
    }

    public String getHoraApertura() {
        return horaApertura;
    }

    public void setHoraApertura(String horaApertura) {
        this.horaApertura = horaApertura;
    }

    public String getHoraCierre() {
        return horaCierre;
    }

    public void setHoraCierre(String horaCierre) {
        this.horaCierre = horaCierre;
    }

    public String getTelefonoMayorista() {
        return telefonoMayorista;
    }

    public void setTelefonoMayorista(String telefonoMayorista) {
        this.telefonoMayorista = telefonoMayorista;
    }

    public String getUbicacionFormateada() {
        String c = (ciudad != null && !ciudad.isBlank()) ? ciudad.trim() : "Cartagena";
        String p = (pais != null && !pais.isBlank()) ? pais.trim() : "Colombia";
        return c + ", " + p;
    }

    public String getHorarioFormateado() {
        String dias = (diasAtencion != null && !diasAtencion.isBlank()) ? diasAtencion.trim() : "Mar - Sáb";
        String ap = formatearHora12h(horaApertura, "8:00 AM");
        String ci = formatearHora12h(horaCierre, "7:00 PM");
        return dias + ": " + ap + " - " + ci;
    }

    private String formatearHora12h(String horaStr, String porDefecto) {
        if (horaStr == null || horaStr.isBlank()) return porDefecto;
        try {
            String clean = horaStr.trim();
            if (clean.toUpperCase(Locale.ENGLISH).contains("AM") || clean.toUpperCase(Locale.ENGLISH).contains("PM")) {
                return clean;
            }
            LocalTime lt = LocalTime.parse(clean);
            return lt.format(DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH));
        } catch (Exception e) {
            return horaStr;
        }
    }

    public String getTituloSitio() { return tituloSitio; }
    public void setTituloSitio(String tituloSitio) { this.tituloSitio = tituloSitio; }
    public String getEslogan() { return eslogan; }
    public void setEslogan(String eslogan) { this.eslogan = eslogan; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getTituloHero() { return tituloHero; }
    public void setTituloHero(String tituloHero) { this.tituloHero = tituloHero; }
    public String getDescripcionHero() { return descripcionHero; }
    public void setDescripcionHero(String descripcionHero) { this.descripcionHero = descripcionHero; }
    public String getTextoBotonHero() { return textoBotonHero; }
    public void setTextoBotonHero(String textoBotonHero) { this.textoBotonHero = textoBotonHero; }
    public String getNombreComercial() { return nombreComercial; }
    public void setNombreComercial(String nombreComercial) { this.nombreComercial = nombreComercial; }
    public String getWhatsapp() { return whatsapp; }
    public void setWhatsapp(String whatsapp) { this.whatsapp = whatsapp; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public String getHorarioAtencion() { return horarioAtencion; }
    public void setHorarioAtencion(String horarioAtencion) { this.horarioAtencion = horarioAtencion; }
    public String getMensajeWhatsApp() { return mensajeWhatsApp; }
    public void setMensajeWhatsApp(String mensajeWhatsApp) { this.mensajeWhatsApp = mensajeWhatsApp; }
    public String getTextoFooter() { return textoFooter; }
    public void setTextoFooter(String textoFooter) { this.textoFooter = textoFooter; }

    public String getNumeroBancolombia() { return numeroBancolombia; }
    public void setNumeroBancolombia(String numeroBancolombia) { this.numeroBancolombia = numeroBancolombia; }
    public String getTitularBancolombia() { return titularBancolombia; }
    public void setTitularBancolombia(String titularBancolombia) { this.titularBancolombia = titularBancolombia; }
    public String getNumeroNequi() { return numeroNequi; }
    public void setNumeroNequi(String numeroNequi) { this.numeroNequi = numeroNequi; }
    public String getTitularNequi() { return titularNequi; }
    public void setTitularNequi(String titularNequi) { this.titularNequi = titularNequi; }
    public String getNumeroDaviplata() { return numeroDaviplata; }
    public void setNumeroDaviplata(String numeroDaviplata) { this.numeroDaviplata = numeroDaviplata; }
    public String getTitularDaviplata() { return titularDaviplata; }
    public void setTitularDaviplata(String titularDaviplata) { this.titularDaviplata = titularDaviplata; }

    public String getWhatsappAdminNumero() { return whatsappAdminNumero != null ? whatsappAdminNumero : ""; }
    public void setWhatsappAdminNumero(String whatsappAdminNumero) { this.whatsappAdminNumero = whatsappAdminNumero; }

    public boolean isWhatsappAdminHabilitado() { return whatsappAdminHabilitado; }
    public void setWhatsappAdminHabilitado(boolean whatsappAdminHabilitado) { this.whatsappAdminHabilitado = whatsappAdminHabilitado; }

    public boolean isNotificarNuevaCita() { return notificarNuevaCita; }
    public void setNotificarNuevaCita(boolean notificarNuevaCita) { this.notificarNuevaCita = notificarNuevaCita; }

    public boolean isNotificarNuevaCompra() { return notificarNuevaCompra; }
    public void setNotificarNuevaCompra(boolean notificarNuevaCompra) { this.notificarNuevaCompra = notificarNuevaCompra; }

    public boolean isNotificarSolicitudReprogramacion() { return notificarSolicitudReprogramacion; }
    public void setNotificarSolicitudReprogramacion(boolean notificarSolicitudReprogramacion) { this.notificarSolicitudReprogramacion = notificarSolicitudReprogramacion; }

    public boolean isNotificarSolicitudCancelacion() { return notificarSolicitudCancelacion; }
    public void setNotificarSolicitudCancelacion(boolean notificarSolicitudCancelacion) { this.notificarSolicitudCancelacion = notificarSolicitudCancelacion; }

    public boolean isNotificarAtencionAhora() { return notificarAtencionAhora; }
    public void setNotificarAtencionAhora(boolean notificarAtencionAhora) { this.notificarAtencionAhora = notificarAtencionAhora; }

    public boolean isNotificarPagoAprobado() { return notificarPagoAprobado; }
    public void setNotificarPagoAprobado(boolean notificarPagoAprobado) { this.notificarPagoAprobado = notificarPagoAprobado; }

    public boolean isNotificarPagoRechazado() { return notificarPagoRechazado; }
    public void setNotificarPagoRechazado(boolean notificarPagoRechazado) { this.notificarPagoRechazado = notificarPagoRechazado; }

    public boolean isNotificarConflictoPago() { return notificarConflictoPago; }
    public void setNotificarConflictoPago(boolean notificarConflictoPago) { this.notificarConflictoPago = notificarConflictoPago; }
}
