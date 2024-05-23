package com.example.hito2alejandropozuelo;

import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.bson.Document;

import java.io.IOException;

public class RegistroController {
    @FXML
    private TextField tf_usuario;
    @FXML
    private PasswordField pf_contrasena;
    @FXML
    private Label lbl_mensaje;

    private final MongoDatabase database = MongoClients.create("mongodb+srv://alejandro:123@alejandropozuelobdd.eikqllx.mongodb.net/").getDatabase("hito2");
    private final MongoCollection<Document> usuarios = database.getCollection("usuarios");

    @FXML
    private void registrar() {
        String usuario = tf_usuario.getText();
        String contrasena = pf_contrasena.getText();
        Document userDoc = usuarios.find(new Document("usuario", usuario)).first();
        if (userDoc == null) {
            usuarios.insertOne(new Document("usuario", usuario).append("contrasena", contrasena));
            lbl_mensaje.setText("Usuario registrado exitosamente");
        } else {
            lbl_mensaje.setText("El usuario ya existe");
        }
    }

    @FXML
    private void irAInicioSesion() throws IOException {
        HelloApplication.setRoot("login");
    }
}
