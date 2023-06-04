package application;


import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        CRUDManager crudManager = new CRUDManager();

        Scene scene = new Scene(crudManager.getMainPane());
        primaryStage.setTitle("Gestion des étudiants");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}