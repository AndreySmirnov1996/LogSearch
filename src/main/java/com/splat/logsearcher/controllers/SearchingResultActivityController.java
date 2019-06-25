package com.splat.logsearcher.controllers;

import com.splat.logsearcher.pojo.Result;
import com.splat.logsearcher.services.SubStringSearcher;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class SearchingResultActivityController {
    private static final Logger log = LoggerFactory.getLogger(SearchingResultActivityController.class);

    private static final int NUMBER_ON_PAGE_ON_TAB = 8;
    private static final int  PAGE_SIZE = 4096;

    @FXML
    private TableView<Result> tableResults;

    @FXML
    private TableColumn<Result, Long> byteColumn;

    @FXML
    private TableColumn<Result, String> pathColumn;

    @FXML
    private TableColumn<Result, String> lineColumn;

    public SearchingResultActivityController() {
    }

    public void initialize() {
        pathColumn.setCellValueFactory(new PropertyValueFactory<>("path"));
        byteColumn.setCellValueFactory(new PropertyValueFactory<>("startByte"));
        lineColumn.setCellValueFactory(new PropertyValueFactory<>("line"));
        ObservableList<Result> resultsData =
                FXCollections.observableArrayList(SubStringSearcher.getListOfResults());
        tableResults.setItems(resultsData);
        tableResults.setRowFactory(tv -> {
            TableRow<Result> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    Result rowData = row.getItem();
                    log.info("User chooses row: " + rowData);
                    try {
                        viewLogs(rowData);
                    } catch (IOException e) {
                        log.error(e.getMessage());
                        e.printStackTrace();
                    }
                }
            });
            return row;
        });
    }

    private void viewLogs(Result result) throws IOException {
//        try {
//            Desktop.getDesktop().open(file);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

//        VBox root = new VBox();
//        Stage stage = new Stage();
//        stage.initModality(Modality.APPLICATION_MODAL);
//        root.setPadding(new Insets(10));
//        root.setSpacing(5);
//        root.getChildren().add(new Label("Enter message:"));
//        TextArea textArea = new TextArea();
//        root.getChildren().add(textArea);
//        Scene scene = new Scene(root, 320, 150);
//        stage.setTitle("JavaFX TextArea (o7planning.org)");
//        stage.setScene(scene);
//        stage.show();
        File file = new File(result.getPath());
        RandomAccessFile raf = new RandomAccessFile(file, "r");
        int pageOfFile = (int) file.length() / PAGE_SIZE + 1;
        int searchPage = (int) (result.getStartByte() / PAGE_SIZE + 1);
        Pagination pagination = new Pagination(pageOfFile, searchPage);

        final boolean[] firstTimePAgeView = {true};
        pagination.setStyle("-fx-border-color:red;");
        pagination.setPageFactory(pageIndex -> {
            try {
                long pos = pageIndex * PAGE_SIZE - PAGE_SIZE / 2;
                if (firstTimePAgeView[0]) {
                    pos = result.getStartByte() - PAGE_SIZE / 2;
                    firstTimePAgeView[0] = false;
                }
                if (pos < 0) pos = 0;
                log.info("Click page = " + pageIndex + "\t position=" + pos);
                raf.seek(pos);
                raf.readLine(); // skip first line
                VBox box = new VBox();
                while(raf.getFilePointer() < pos + PAGE_SIZE / 2){
                    String line = raf.readLine();
                    VBox element = new VBox();
                    element.getChildren().add(new Label(line));
                    box.getChildren().add(element);
                }
                return box;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        });

        AnchorPane anchor = new AnchorPane();
        AnchorPane.setTopAnchor(pagination, 10.0);
        AnchorPane.setRightAnchor(pagination, 10.0);
        AnchorPane.setBottomAnchor(pagination, 10.0);
        AnchorPane.setLeftAnchor(pagination, 10.0);
        anchor.getChildren().addAll(pagination);
        Scene scene = new Scene(anchor, 700, 500);
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.setTitle("Log Viewer");
        stage.show();
    }
}
