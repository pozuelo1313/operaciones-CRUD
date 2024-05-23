module com.example.hito2alejandropozuelo {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.hito2alejandropozuelo to javafx.fxml;
    exports com.example.hito2alejandropozuelo;
}