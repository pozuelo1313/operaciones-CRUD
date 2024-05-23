module com.example.hito2alejandropozuelo {
    requires javafx.controls;
    requires javafx.fxml;
    requires mongo.java.driver;


    opens com.example.hito2alejandropozuelo to javafx.fxml;
    exports com.example.hito2alejandropozuelo;
}