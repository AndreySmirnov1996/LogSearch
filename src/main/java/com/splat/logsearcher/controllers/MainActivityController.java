package com.splat.logsearcher.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

public class MainActivityController {
    @FXML
    public Button search_button;

    @FXML
    private TextField path;

    @FXML
    private TextField exchange;

    @FXML
    private TextField text;


    public void initialize() {
        exchange.setText("log");
    }

    @FXML
    private void click(ActionEvent event) {
        boolean flagPath = false;
        boolean flagExchange = false;
        boolean flagText = false;
        String pathToDirectory = path.getText();
        String exchangeOfFile = exchange.getText();
        String searchingString = text.getText();

        //Check all input data
        //Check directory
        if (new File(pathToDirectory).exists()) {
            flagPath = true;
        } else {
            path.clear();
            path.setPromptText("Enter a valid address");
        }

        //Check text
        if (!searchingString.equals("")) {
            flagText = true;
        } else {
            text.setPromptText("Enter a string for searching");
        }

        //Check exchange
        if (!exchangeOfFile.equals("")) {
            flagExchange = true;
        } else {
            exchange.setPromptText("Enter an exchange of file");
        }

        if (flagExchange && flagPath && flagText) {
            System.out.println("ОК))))))))))");
            showFiles(pathToDirectory, "." + exchangeOfFile);
        } else {
            System.out.println("Не ок(((((((((((((((()");
        }
    }

    public static void showFiles(String path, String exch) {
        try {
            Files.walk(Paths.get(path))
                    .filter(p -> p.toString().endsWith(exch))
                    .map(Path::toFile)
                    .collect(Collectors.toList())
                    .forEach(System.out::println);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
