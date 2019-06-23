package com.splat.logsearcher;

import com.splat.logsearcher.controllers.MainActivityController;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
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
