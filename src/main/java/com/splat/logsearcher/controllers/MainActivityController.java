package com.splat.logsearcher.controllers;

import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static java.util.concurrent.TimeUnit.SECONDS;

public class MainActivityController {
    private static final Logger log = LoggerFactory.getLogger(MainActivityController.class);
    public static final Integer THREAD_NUMBER = Runtime.getRuntime().availableProcessors();
    @FXML
    public Button search_button;

    @FXML
    private TextField path;

    @FXML
    private TextField exchange;

    @FXML
    private TextField text;

    private String searchingString;
    private String pathToDirectory;
    private String exchangeOfFile;

    private static ConcurrentHashMap<String, String> searchResult;
    private static ExecutorService executorService;

    private int numberOfNewLineCharacter;

    public static ExecutorService getExecutorService() {
        return executorService;
    }

    public static void setExecutorService(ExecutorService executorService) {
        MainActivityController.executorService = executorService;
    }

    public void initialize() {
        path.setText("E:\\testdir\\1");
        text.setText("lol");
        exchange.setText("log");
        String osName = System.getProperty("os.name");
        if(osName.contains("Windows")){
            numberOfNewLineCharacter = 2;
        } else {
            numberOfNewLineCharacter = 1;
        }
        executorService = Executors.newFixedThreadPool(THREAD_NUMBER);
    }

    private Callable<String> getThreadWorker(File file, long startByte, long endByte) {
        return () -> {
            log.info("Thread name: " + Thread.currentThread().getName() +
                    " Reading " + file.getName() + "(" + startByte +", " + endByte + ")");
            try {
                RandomAccessFile raf = new RandomAccessFile(file, "r");
                if(startByte == 0){
                    raf.seek(startByte);
                } else {
                    raf.seek(startByte + raf.readLine().length());
                }
                String line = null;
                while(raf.getFilePointer() <= endByte){
                    line = raf.readLine();
                    if(line.contains(searchingString)){
                        searchResult.put(file.getName() + "\t Byte number: " + (raf.getFilePointer() - (line.length() + numberOfNewLineCharacter)) , line);
                    }
                }
            } catch (FileNotFoundException e) {
                log.error("File " + file.getName() + " doesn't exist");
                e.printStackTrace();
            } catch (IOException e) {
                log.error(e.getMessage());
                e.printStackTrace();
            }
            return "Done";
        };
    }

    @FXML
    private void click(ActionEvent event) {
        searchResult = new ConcurrentHashMap<>();
        pathToDirectory = path.getText();
        exchangeOfFile = exchange.getText();
        searchingString = text.getText();
        if (checkParameters()) {
            log.info("Correct parameters");
            List<File> files = getFilesWithExchange(pathToDirectory, "." + exchangeOfFile);
            if (files != null && files.size() != 0) {
                Collection<Future> results = new ArrayList<>();
                for (File file : files) {
                    log.info("File " + file.getName() + " Length = " + file.length());
                    long capacity = file.length() / THREAD_NUMBER;
                    for (int i = 0; i < THREAD_NUMBER; i++) {
                        long startByte = i * capacity;
                        long endByte = (i + 1) * capacity;
                        if (i == THREAD_NUMBER - 1) {
                            endByte = file.length() - 1;
                        }
                        results.add(executorService.submit(getThreadWorker(file, startByte, endByte)));
                    }
                }
                for (Future done : results) {
                    try {
                        done.get();
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                }
                viewResults();
            } else {
                log.info("Files with exchange ." + exchangeOfFile + " in directory: " + pathToDirectory + " don't exist!");
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Result of searching");
                alert.setContentText("Files with exchange ." + exchangeOfFile + " in directory: " + pathToDirectory + " don't exist!");
                alert.showAndWait();
            }
        } else {
            log.info("Incorrect parameters");
        }
    }

    private void viewResults(){
        searchResult.forEach(
                (p, k)-> System.out.println(p + " " + k));

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/SearchingResultActivity.fxml"));
        try {
            Parent root1 = fxmlLoader.load();
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            //stage.initStyle(StageStyle.UNDECORATED);
            stage.setTitle("Results of searching");
            stage.setScene(new Scene(root1));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static List<File> getFilesWithExchange(String path, String exch) {
        try {
            return Files.walk(Paths.get(path))
                    .filter(p -> p.toString().endsWith(exch))
                    .map(Path::toFile)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
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
