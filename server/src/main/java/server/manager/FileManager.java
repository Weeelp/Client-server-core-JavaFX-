package server.manager;

import server.io.*;
import common.model.movie.Movie;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FileManager {
    private static final Logger log = LogManager.getLogger(FileManager.class.getName());

    private FileReader fileReader;
    private FileWriter fileWriter;
    private String currentFilePath;
    
    public FileManager(CollectionManager cm) {
        this.fileReader = new FileReader();
        this.fileWriter = new FileWriter();
        getEnvFileName(cm);
    }
    
    public LinkedList<Movie> loadFromFile(String filePath) {
        this.currentFilePath = filePath;
        try {
            LinkedList<Movie> movies = fileReader.loadFromFile(filePath);
            log.info("Загружено фильмов: " + movies.size());
            return movies;
        } catch (FileNotFoundException e) {
            log.error("Файл не найден. Будет создан новый файл: " + currentFilePath);
            createEmptyFile(currentFilePath);
            return new LinkedList<>();
        } catch (Exception e) {
            int dotIndex = filePath.lastIndexOf('.');
            if (dotIndex > 0) {
                this.currentFilePath = filePath.substring(0, dotIndex) + "_new.xml";
            } else { this.currentFilePath = filePath + "_new.xml"; }

            log.error("Файл повреждён или имеет неверный формат. Будет создан новый файл: " + currentFilePath);
            createEmptyFile(currentFilePath);
            return new LinkedList<>();
        }
    }
    
    private void createEmptyFile(String filePath) {
        try {
            fileWriter.saveToFile(filePath, new LinkedList<>());
        } catch (IOException ex) {
            log.error("Не удалось создать пустой файл: " + ex.getMessage());
        }
    }
    
    public void saveToFile(CollectionManager cm) throws IOException {
        if (currentFilePath == null) {
            throw new IOException("Путь к файлу не указан");
        }
        cm.sortByIdAscending();
        fileWriter.saveToFile(currentFilePath, cm.getAll());
        log.info("Сохранено фильмов: " + cm.size());
    }
    
    public void saveToFile(String filePath, LinkedList<Movie> movies) throws IOException {
        this.currentFilePath = filePath;
        fileWriter.saveToFile(filePath, movies);
        log.info("Сохранено фильмов: " + movies.size());
    }

    public void getEnvFileName (CollectionManager cm){String FILE_PATH = System.getenv("DATA_PATH");
        if (FILE_PATH == null || FILE_PATH.trim().isEmpty()) {
            FILE_PATH = "repo.xml";
            log.error("DATA_PATH не задана, используем " + FILE_PATH);
        }
        try {
            cm.setMovies(loadFromFile(FILE_PATH));
            log.info("Данные загружены из файла: " + FILE_PATH);
        } catch (Exception e) {
            log.error("Не удалось загрузить данные: " + e.getMessage());
            log.info("Будет создана пустая коллекция.");
        }}
    
    public String getCurrentFilePath() {
        return currentFilePath;
    }
}