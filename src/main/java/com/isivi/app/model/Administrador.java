package com.isivi.app.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "administradores")
public class Administrador {
    @Id
    private String id;
    private String usuario;
    private String contrasenaHash;

    public Administrador() {}

    public Administrador(String usuario, String contrasenaHash) {
        this.usuario = usuario;
        this.contrasenaHash = contrasenaHash;
    }

    public String getId() { return id; }
    public String getUsuario() { return usuario; }
    public String getContrasenaHash() { return contrasenaHash; }
}
