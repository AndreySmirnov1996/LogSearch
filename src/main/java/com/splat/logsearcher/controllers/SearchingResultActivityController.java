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
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;

public class SearchingResultActivityController {
    private static final Logger log = LoggerFactory.getLogger(SearchingResultActivityController.class);

    private static final int NUMBER_ON_PAGE = 8;

    private Pagination pagination;

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
                    } catch (FileNotFoundException e) {
                        log.error(e.getMessage());
                        e.printStackTrace();
                    }
                }
            });
            return row;
        });
    }

    private void viewLogs(Result result) throws FileNotFoundException {
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
        int pageOfFile = (int) file.length() / 1500;

        pagination = new Pagination(pageOfFile, NUMBER_ON_PAGE);
        pagination.setStyle("-fx-border-color:red;");
        pagination.setPageFactory(new Callback<Integer, Node>() {
            @Override
            public Node call(Integer pageIndex) {
                return createPage(pageIndex);
            }
        });

        AnchorPane anchor = new AnchorPane();
        AnchorPane.setTopAnchor(pagination, 10.0);
        AnchorPane.setRightAnchor(pagination, 10.0);
        AnchorPane.setBottomAnchor(pagination, 10.0);
        AnchorPane.setLeftAnchor(pagination, 10.0);

        anchor.getChildren().addAll(pagination);
        Scene scene = new Scene(anchor);
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.setTitle("PaginationSample");
        stage.show();
    }


    public VBox createPage(int pageIndex) {
        VBox box = new VBox(5);
        int page = pageIndex * NUMBER_ON_PAGE;
        for (int i = page; i < page + NUMBER_ON_PAGE; i++) {
            VBox element = new VBox();
            Hyperlink link = new Hyperlink("Item " + (i + 1));
            link.setVisited(true);
            Label text = new Label("Search results\nfor " + link.getText());
            element.getChildren().addAll(link, text);
            box.getChildren().add(element);
        }
        return box;
    }
}
