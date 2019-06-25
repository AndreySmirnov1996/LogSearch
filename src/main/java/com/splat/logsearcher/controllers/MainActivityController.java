package com.splat.logsearcher.controllers;

import com.splat.logsearcher.Main;
import com.splat.logsearcher.services.SubStringSearcher;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class MainActivityController {
    private static final Logger log = LoggerFactory.getLogger(MainActivityController.class);

    @FXML
    public Button search_button;

    @FXML
    public Button path_button;

    @FXML
    private TextField path;

    @FXML
    private TextField exchange;

    @FXML
    private TextField text;

    private String searchingString;
    private String pathToDirectory;
    private String exchangeOfFile;

    public MainActivityController() {
    }

    public void initialize() {
        path.setText("E:\\testdir\\3");
        text.setText("lol");
        exchange.setText("txt");
    }

    @FXML
    private void clickPathButton(ActionEvent event) {
        path.clear();
        final DirectoryChooser directoryChooser = new DirectoryChooser();
        // Set title for DirectoryChooser
        directoryChooser.setTitle("Select a directory for searching");
        // Set Initial Directory
        directoryChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        File dir = directoryChooser.showDialog(Main.stage);
        if (dir != null) {
            path.setText(dir.getAbsolutePath());
        } else {
            path.clear();
        }
    }


    @FXML
    private void clickSearchButton(ActionEvent event) {
        pathToDirectory = path.getText();
        exchangeOfFile = exchange.getText();
        searchingString = text.getText();
        if (checkParameters()) {
            log.info("Correct parameters");
            SubStringSearcher searcher = new SubStringSearcher(pathToDirectory, searchingString, exchangeOfFile);
            Collection<Future> results = searcher.search();
            if (results != null) {
                for (Future done : results) {
                    try {
                        done.get();
                    } catch (InterruptedException | ExecutionException e) {
                        log.error(e.getMessage());
                        e.printStackTrace();
                    }
                }
                if (SubStringSearcher.getListOfResults().size() > 0) {
                    searcher.viewResults();
                } else {
                    showAlert("In directory: " + pathToDirectory + " the files in text: " + searchingString + "do not contain");
                }
            }
            log.info("Searching is done");
        } else {
            log.info("Incorrect parameters");
        }
    }

    public static void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Result of searching");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private boolean checkParameters() { //Check all input data
        boolean flagPath = false;
        boolean flagExchange = false;
        boolean flagText = false;
        if (new File(pathToDirectory).exists()) { //Check directory
            flagPath = true;
        } else {
            path.clear();
            path.setPromptText("Enter a valid address");
        }
        if (!searchingString.equals("")) {  //Check text
            flagText = true;
        } else {
            text.setPromptText("Enter a string for searching");
        }
        if (!exchangeOfFile.equals("")) { //Check exchange
            flagExchange = true;
        } else {
            exchange.setPromptText("Enter an exchange of file");
        }
        return flagExchange && flagPath && flagText;
    }
}
