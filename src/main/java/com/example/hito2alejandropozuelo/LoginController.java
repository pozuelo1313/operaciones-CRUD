package com.example.hito2alejandropozuelo;

import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.bson.Document;

import java.io.IOException;

public class LoginController {
    @FXML
    private TextField tf_usuario;
    @FXML
    private PasswordField pf_contrasena;
    @FXML
    private Label lbl_mensaje;
    @FXML
    private VBox vbox;

    private final MongoDatabase database = MongoClients.create("mongodb+srv://alejandro:123@alejandropozuelobdd.eikqllx.mongodb.net/").getDatabase("hito2");
    private final MongoCollection<Document> usuarios = database.getCollection("usuarios");

    @FXML
    private void iniciarSesion() throws IOException {
        String usuario = tf_usuario.getText();
        String contrasena = pf_contrasena.getText();
        Document userDoc = usuarios.find(new Document("usuario", usuario).append("contrasena", contrasena)).first();
        if (userDoc != null) {
            lbl_mensaje.setText("Inicio de sesión exitoso");
            HelloApplication.setRoot("hello-view");
        } else {
            lbl_mensaje.setText("Usuario o contraseña incorrectos");
        }
    }

    @FXML
    private void irARegistro() throws IOException {
        HelloApplication.setRoot("registro");
    }
}
