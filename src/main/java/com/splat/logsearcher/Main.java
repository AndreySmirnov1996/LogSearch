package com.splat.logsearcher;

import com.splat.logsearcher.controllers.MainActivityController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    public static Stage stage;

    @Override
    public void start(Stage stage) throws Exception {
        Main.stage = stage;
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/MainActivity.fxml"));
        stage.setTitle("LogSearcher");
        stage.setScene(new Scene(root));
        stage.setOnCloseRequest(event -> {
            try {
                MainActivityController.getExecutorService().shutdown();
                stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        stage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
