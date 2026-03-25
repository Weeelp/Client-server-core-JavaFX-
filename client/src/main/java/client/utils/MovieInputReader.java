package client.utils;

import java.util.Scanner;   
import common.model.movie.*;
import common.model.person.*;
import common.utils.validator.MovieValidator;
import common.exceptions.*;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MovieInputReader {
    private static final Logger log = LogManager.getLogger(MovieInputReader.class.getName());

    private final Scanner sc;

    public MovieInputReader(Scanner sc) {
        this.sc = sc;
    }

    private void checkStop(String input) throws StopInputException {
        if (input.equalsIgnoreCase("exit")) {
            throw new ExitException();
        }
        if (input.equalsIgnoreCase("stop")) {
            throw new StopInputException();
        }
    }

    public MovieData readMovieData() throws StopInputException, ValidationException {
        String name = readName();
        Coordinates coordinates = readCoordinates();
        int oscarsCount = readOscarsCount();
        double totalBoxOffice = readTotalBoxOffice();
        long usaBoxOffice = readUsaBoxOffice();
        Genre genre = readGenre();
        Person screenWriter = readPerson();

        return new MovieData(name, coordinates, oscarsCount,
                totalBoxOffice, usaBoxOffice, genre, screenWriter);
    }

    private String readName() throws StopInputException, ValidationException {
        while (true) {
            log.info(">> Введите имя фильма:");
            String input = sc.nextLine().trim();
            checkStop(input);
            try {
                return MovieValidator.validateName(input);
            } catch (ValidationException e) {
                log.error(">> Ошибка: " + e.getMessage());
            }
        }
    }

    private Coordinates readCoordinates() throws StopInputException, ValidationException {
        long x = readX();
        float y = readY();
        return new Coordinates(x, y);
    }

    private long readX() throws StopInputException, ValidationException {
        while (true) {
            log.info(">> Введите координату X:");
            String input = sc.nextLine().trim();
            checkStop(input);
            try {
                return MovieValidator.validateX(input);
            } catch (ValidationException e) {
                log.error(">> Ошибка: " + e.getMessage());
            }
        }
    }

    private float readY() throws StopInputException, ValidationException {
        while (true) {
            log.info(">> Введите координату Y:");
            String input = sc.nextLine().trim();
            checkStop(input);
            try {
                return MovieValidator.validateY(input);
            } catch (ValidationException e) {
                log.error(">> Ошибка: " + e.getMessage());
            }
        }
    }

    private int readOscarsCount() throws StopInputException, ValidationException {
        while (true) {
            log.info(">> Введите количество Оскаров:");
            String input = sc.nextLine().trim();
            checkStop(input);
            try {
                return MovieValidator.validateOscarsCount(input);
            } catch (ValidationException e) {
                log.error(">> Ошибка: " + e.getMessage());
            }
        }
    }

    private double readTotalBoxOffice() throws StopInputException, ValidationException {
        while (true) {
            log.info(">> Введите общий сбор:");
            String input = sc.nextLine().trim();
            checkStop(input);
            try {
                return MovieValidator.validateTotalBoxOffice(input);
            } catch (ValidationException e) {
                log.error(">> Ошибка: " + e.getMessage());
            }
        }
    }

    private long readUsaBoxOffice() throws StopInputException, ValidationException {
        while (true) {
            log.info(">> Введите сборы в США:");
            String input = sc.nextLine().trim();
            checkStop(input);
            try {
                return MovieValidator.validateUsaBoxOffice(input);
            } catch (ValidationException e) {
                log.error(">> Ошибка: " + e.getMessage());
            }
        }
    }

    private Genre readGenre() throws StopInputException, ValidationException {
        while (true) {
            log.info(">> Введите жанр (WESTERN, COMEDY, TRAGEDY, SCIENCE_FICTION):");
            String input = sc.nextLine().trim();
            checkStop(input);
            try {
                return MovieValidator.validateEnum(input, Genre.class, "WESTERN, COMEDY, TRAGEDY, SCIENCE_FICTION");
            } catch (ValidationException e) {
                log.error(">> Ошибка: " + e.getMessage());
            }
        }
    }

    private Person readPerson() throws StopInputException, ValidationException {
        String name = readPersonName();
        int height = readPersonHeight();
        EyeColor eyeColor = readEyeColor();
        HairColor hairColor = readHairColor();
        Country nationality = readCountry();
        return new Person(name, height, eyeColor, hairColor, nationality);
    }

    private String readPersonName() throws StopInputException, ValidationException {
        while (true) {
            log.info(">> Введите имя сценариста:");
            String input = sc.nextLine().trim();
            checkStop(input);
            try {
                return MovieValidator.validatePersonName(input);
            } catch (ValidationException e) {
                log.error(">> Ошибка: " + e.getMessage());
            }
        }
    }

    private int readPersonHeight() throws StopInputException, ValidationException {
        while (true) {
            log.info(">> Введите рост сценариста:");
            String input = sc.nextLine().trim();
            checkStop(input);
            try {
                return MovieValidator.validatePersonHeight(input);
            } catch (ValidationException e) {
                log.error(">> Ошибка: " + e.getMessage());
            }
        }
    }

    private EyeColor readEyeColor() throws StopInputException, ValidationException {
        while (true) {
            log.info(">> Введите цвет глаз (BLACK, BLUE, WHITE, BROWN):");
            String input = sc.nextLine().trim();
            checkStop(input);
            try {
                return MovieValidator.validateEnum(input, EyeColor.class, "BLACK, BLUE, WHITE, BROWN");
            } catch (ValidationException e) {
                log.error(">> Ошибка: " + e.getMessage());
            }
        }
    }

    private HairColor readHairColor() throws StopInputException, ValidationException {
        while (true) {
            log.info(">> Введите цвет волос (GREEN, RED, ORANGE, WHITE):");
            String input = sc.nextLine().trim();
            checkStop(input);
            try {
                return MovieValidator.validateEnum(input, HairColor.class, "GREEN, RED, ORANGE, WHITE");
            } catch (ValidationException e) {
                log.error(">> Ошибка: " + e.getMessage());
            }
        }
    }

    private Country readCountry() throws StopInputException, ValidationException {
        while (true) {
            log.info(">> Введите национальность (RUSSIA, UNITED_KINGDOM, GERMANY, ITALY, JAPAN, AMERICA):");
            String input = sc.nextLine().trim();
            checkStop(input);
            try {
                return MovieValidator.validateEnum(input, Country.class, "RUSSIA, UNITED_KINGDOM, GERMANY, ITALY, JAPAN, AMERICA");
            } catch (ValidationException e) {
                log.error(">> Ошибка: " + e.getMessage());
            }
        }
    }
}