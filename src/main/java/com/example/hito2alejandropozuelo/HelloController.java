package com.example.hito2alejandropozuelo;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import javafx.application.Application;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.Map;
import java.util.Optional;

public class HelloController extends Application {
    @FXML
    private Label welcomeText;
    @FXML
    private TableView<Map<String, Object>> tv_datos;
    @FXML
    private TableColumn<Map<String, Object>, String> col_nombre;
    @FXML
    private TableColumn<Map<String, Object>, String> col_ciudad;
    @FXML
    private TableColumn<Map<String, Object>, Double> col_facturacion;
    @FXML
    private TextField tf_nombre;
    @FXML
    private TextField tf_ciudad;
    @FXML
    private TextField tf_facturacion;
    @FXML
    private TextField tf_buscar;
    @FXML
    private Button btn_agregar;
    @FXML
    private Button btn_buscar;

    private final String url = "mongodb+srv://alejandro:123@alejandropozuelobdd.eikqllx.mongodb.net/";
    private final MongoClient mongoClient = MongoClients.create(url);
    private final MongoDatabase database = mongoClient.getDatabase("hito2");
    private final MongoCollection<Document> collection = database.getCollection("clientes");

    public void initialize() {
        // Configurar las columnas de la tabla
        col_nombre.setCellValueFactory(cellData -> new SimpleStringProperty((String) cellData.getValue().get("nombre")));
        col_ciudad.setCellValueFactory(cellData -> new SimpleStringProperty((String) cellData.getValue().get("ciudad")));
        col_facturacion.setCellValueFactory(cellData -> new SimpleDoubleProperty((Double) cellData.getValue().get("facturacion")).asObject());

        // Establecer la política de relleno de la tabla
        tv_datos.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        ContextMenu contextMenu = new ContextMenu();
        MenuItem deleteItem = new MenuItem("Eliminar");
        MenuItem editItem = new MenuItem("Editar");


        deleteItem.setOnAction(event -> eliminarSeleccionado());
        editItem.setOnAction(event -> editarSeleccionado());


        contextMenu.getItems().addAll(deleteItem, editItem);


        tv_datos.setContextMenu(contextMenu);

        // Manejar el clic derecho en el TableView
        tv_datos.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if (event.getButton() == MouseButton.SECONDARY && !tv_datos.getSelectionModel().isEmpty()) {
                contextMenu.show(tv_datos, event.getScreenX(), event.getScreenY());
            } else {
                contextMenu.hide();
            }
        });

        // Asignar acción al botón agregar
        btn_agregar.setOnAction(event -> agregar());

        // Asignar acción al botón buscar
        btn_buscar.setOnAction(event -> buscar());

        // Mostrar los datos al inicializar
        mostrar();
    }

    @FXML
    protected void mostrar() {
        ObservableList<Map<String, Object>> datos = FXCollections.observableArrayList();
        for (Document doc : collection.find()) {
            datos.add(doc);
        }
        tv_datos.setItems(datos);
    }

    private void eliminarSeleccionado() {
        Map<String, Object> seleccionado = tv_datos.getSelectionModel().getSelectedItem();
        if (seleccionado != null) {
            ObjectId id = (ObjectId) seleccionado.get("_id");
            collection.deleteOne(Filters.eq("_id", id));
            mostrar(); // Actualizar la lista después de eliminar
        }
    }

    private void editarSeleccionado() {
        Map<String, Object> seleccionado = tv_datos.getSelectionModel().getSelectedItem();
        if (seleccionado != null) {
            ObjectId id = (ObjectId) seleccionado.get("_id");

            Dialog<Map<String, Object>> dialog = new Dialog<>();
            dialog.setTitle("Editar Cliente");

            ButtonType guardarButtonType = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(guardarButtonType, ButtonType.CANCEL);

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);

            TextField tfEditNombre = new TextField((String) seleccionado.get("nombre"));
            TextField tfEditCiudad = new TextField((String) seleccionado.get("ciudad"));
            TextField tfEditFacturacion = new TextField(String.valueOf(seleccionado.get("facturacion")));

            grid.add(new Label("Nombre:"), 0, 0);
            grid.add(tfEditNombre, 1, 0);
            grid.add(new Label("Ciudad:"), 0, 1);
            grid.add(tfEditCiudad, 1, 1);
            grid.add(new Label("Facturación:"), 0, 2);
            grid.add(tfEditFacturacion, 1, 2);

            dialog.getDialogPane().setContent(grid);

            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == guardarButtonType) {
                    try {
                        double facturacion = Double.parseDouble(tfEditFacturacion.getText());
                        return Map.of(
                                "_id", id,
                                "nombre", tfEditNombre.getText(),
                                "ciudad", tfEditCiudad.getText(),
                                "facturacion", facturacion
                        );
                    } catch (NumberFormatException e) {
                        Alert alert = new Alert(Alert.AlertType.ERROR, "Facturación debe ser un número.", ButtonType.OK);
                        alert.showAndWait();
                    }
                }
                return null;
            });

            Optional<Map<String, Object>> result = dialog.showAndWait();
            result.ifPresent(this::actualizarDocumento);
        }
    }

    private void actualizarDocumento(Map<String, Object> updatedRow) {
        ObjectId id = (ObjectId) updatedRow.get("_id");
        Document updatedDoc = new Document("nombre", updatedRow.get("nombre"))
                .append("ciudad", updatedRow.get("ciudad"))
                .append("facturacion", updatedRow.get("facturacion"));
        collection.updateOne(Filters.eq("_id", id), new Document("$set", updatedDoc));
        mostrar(); // Actualizar la lista después de actualizar
    }

    private void agregar() {
        String nombre = tf_nombre.getText();
        String ciudad = tf_ciudad.getText();
        double facturacion;
        try {
            facturacion = Double.parseDouble(tf_facturacion.getText());
        } catch (NumberFormatException e) {
            welcomeText.setText("Facturación debe ser un número.");
            return;
        }

        if (!nombre.isEmpty() && !ciudad.isEmpty()) {
            Document doc = new Document("nombre", nombre)
                    .append("ciudad", ciudad)
                    .append("facturacion", facturacion);
            collection.insertOne(doc);
            mostrar(); // Actualizar la lista después de agregar
            tf_nombre.clear();
            tf_ciudad.clear();
            tf_facturacion.clear();
        } else {
            welcomeText.setText("Nombre y Ciudad no pueden estar vacíos.");
        }
    }

    @FXML
    private void buscar() {
        String textoBusqueda = tf_buscar.getText();
        ObservableList<Map<String, Object>> datos = FXCollections.observableArrayList();
        for (Document doc : collection.find(Filters.or(
                Filters.regex("nombre", textoBusqueda, "i"),
                Filters.regex("ciudad", textoBusqueda, "i"),
                Filters.regex("facturacion", textoBusqueda, "i")
        ))) {
            datos.add(doc);
        }
        tv_datos.setItems(datos);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("hello-view.fxml"));
        Scene scene = new Scene(root);
        primaryStage.setTitle("Gestión de Clientes");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}