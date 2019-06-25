package com.splat.logsearcher.services;

import com.splat.logsearcher.pojo.Result;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static com.splat.logsearcher.controllers.MainActivityController.showAlert;

public class SubStringSearcher {
    private static final Logger log = LoggerFactory.getLogger(SubStringSearcher.class);
    private static final int THREAD_NUMBER = Runtime.getRuntime().availableProcessors();
    private static final int MAP_SIZE = 4 * 1024;
    private static int numberOfNewLineCharacter;
    private static ExecutorService executorService;
    private static List<Result> listOfResults;

    private String pathToDirectory;
    private String searchingString;
    private String exchangeOfFile;

    public SubStringSearcher(String pathToDirectory, String searchingString, String exchangeOfFile) {
        listOfResults = new CopyOnWriteArrayList<>();
        executorService = Executors.newFixedThreadPool(THREAD_NUMBER);
        this.pathToDirectory = pathToDirectory;
        this.exchangeOfFile = exchangeOfFile;
        this.searchingString = searchingString;
        if (System.getProperty("os.name").contains("Windows")) {
            numberOfNewLineCharacter = 2;
        } else {
            numberOfNewLineCharacter = 1;
        }
    }

    public static ExecutorService getExecutorService() {
        return executorService;
    }

    public static List<Result> getListOfResults() {
        return listOfResults;
    }

    public Collection<Future> search() {
        listOfResults.clear();
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
            return results;
        } else {
            log.info("Files with exchange ." + exchangeOfFile + " in directory: " + pathToDirectory + " don't exist!");
            showAlert("Files with exchange ." + exchangeOfFile + " in directory: " + pathToDirectory + " don't exist!");
            return null;
        }
    }

    private Callable<String> getThreadWorker(File file, long startByte, long endByte) {
        return () -> {
            log.info("Thread name: " + Thread.currentThread().getName() +
                    " Reading " + file.getName() + "(" + startByte + ", " + endByte + ")");
            try {
                searchFor(file, startByte, endByte);
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

    private void searchFor(File file, long startByte, long endByte) throws IOException {
        final byte[] toSearch = searchingString.getBytes(StandardCharsets.UTF_8);
        int padding = 1;
        try (FileChannel channel = FileChannel.open(file.toPath(), StandardOpenOption.READ)) {
            channel.position(startByte);
            long pos = startByte;
            while (pos < endByte) {
                long remaining = endByte - pos;
                int tryMap = MAP_SIZE + toSearch.length + padding;
                int toMap = (int) Math.min(tryMap, remaining);
                int limit = tryMap == toMap ? MAP_SIZE : (toMap - toSearch.length);
                MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, pos, toMap);
                for (int i = 0; i < limit; i++) {
                    if (wordMatch(buffer, i, toSearch)) {
                        i += toSearch.length - 1;
                        listOfResults.add(new Result(file.getPath(),
                                pos + i - numberOfNewLineCharacter, getLineFromFile(file,channel,pos + i - numberOfNewLineCharacter)));
                    }
                }
                pos += (tryMap == toMap) ? MAP_SIZE : toMap;
            }
        } catch (Exception ex) {
            log.error(ex.getMessage());
            ex.printStackTrace();
        }
    }

    private static boolean wordMatch(MappedByteBuffer buffer, int pos, byte[] toSearch) {
        for (int i = 0; i < toSearch.length; i++) {
            if (toSearch[i] != buffer.get(pos + i)) {
                return false;
            }
        }
        return true;
    }

    private String getLineFromFile(File fileName, FileChannel channel, long point) {
        String str = null;
        try {
            //Getting buffer size MAP_SIZE*2 with the required line in the middle
            channel.position(point);
            long buf = point - MAP_SIZE;
            long leftBoard = buf > 0 ? buf : 0;
            buf = point + MAP_SIZE;
            long rightBoard = buf < fileName.length() - 1 ? buf : fileName.length();
            MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, leftBoard, rightBoard - leftBoard);

            int startLinePos = 0;
            int stopLinePos = MAP_SIZE*2;
            int i = leftBoard == 0 ? (int) point : MAP_SIZE;
            int j = i + searchingString.length();
            //searching start character of line
            while (i > 0) {
                if(buffer.get(i) == '\n'){
                    startLinePos = i + 1;
                    break;
                }
                i--;
            }
            //searching last character of line
            while (j < buffer.capacity() - 1){
                if(buffer.get(j) == '\n'){
                    stopLinePos = j;
                    break;
                }
                j++;
            }
            //read line from buffer to byte array and convert to String
            int size = stopLinePos - startLinePos - 1;
            byte[] lineBuffer = new byte[size];
            buffer.position(startLinePos);
            buffer.get(lineBuffer, 0, size);
            str = new String(lineBuffer);
            log.info("Add to file " + fileName + "\t Line - " + str);
        } catch (IOException e) {
            log.error(e.getMessage());
            e.printStackTrace();
        }
        return str;
    }

    public void viewResults() {
        listOfResults.forEach(f -> log.info(f.toString()));
        //Open new activity for view result
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/SearchingResultActivity.fxml"));
        try {
            Parent root1 = fxmlLoader.load();
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
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
}
