package common.utils.validator;

import common.exceptions.*;

public class MovieValidator {
    
    public static String validateName(String name) throws ValidationException {
        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException("Имя фильма не может быть пустым");
        }
        
        return name.trim();
    }
    
    public static long validateX(String xStr) throws ValidationException {
        try {
            long x = Long.parseLong(xStr.trim());
            return x; 
        } catch (NumberFormatException e) {
            throw new ValidationException("X должен быть целым числом");
        }
    }
    
    public static float validateY(String yStr) throws ValidationException {
        try {
            float y = Float.parseFloat(yStr.trim());
            return y;
        } catch (NumberFormatException e) {
            throw new ValidationException("Y должен быть числом");
        }
    }
    
    public static int validateOscarsCount(String countStr) throws ValidationException {
        try {
            int count = Integer.parseInt(countStr.trim());
            if (count < 0) {
                throw new ValidationException("Количество Оскаров должно быть положительным");
            }
            return count;
        } catch (NumberFormatException e) {
            throw new ValidationException("Количество Оскаров должно быть целым числом");
        }
    }
    
    public static double validateTotalBoxOffice(String boxStr) throws ValidationException {
        try {
            double box = Double.parseDouble(boxStr.trim());
            if (box < 0) {
                throw new ValidationException("Общий сбор должен быть положительным");
            }
            return box;
        } catch (NumberFormatException e) {
            throw new ValidationException("Общий сбор должен быть числом");
        }
    }
    
    public static long validateUsaBoxOffice(String boxStr) throws ValidationException {
        try {
            long box = Long.parseLong(boxStr.trim());
            if (box < 0) {
                throw new ValidationException("Сборы в США должны быть положительными");
            }
            return box;
        } catch (NumberFormatException e) {
            throw new ValidationException("Сборы в США должны быть целым числом");
        }
    }
    
    public static <T extends Enum<T>> T validateEnum(String input, Class<T> enumClass, String allowedValues) throws ValidationException {
        try {
            return Enum.valueOf(enumClass, input.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Допустимые значения: " + allowedValues);
        }
    }
    
    public static String validatePersonName(String name) throws ValidationException {
        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException("Имя сценариста не может быть пустым");
        }
        return name.trim();
    }
    
    public static int validatePersonHeight(String heightStr) throws ValidationException {
        try {
            int height = Integer.parseInt(heightStr.trim());
            if (height <= 0) {
                throw new ValidationException("Рост должен быть положительным");
            }
            return height;
        } catch (NumberFormatException e) {
            throw new ValidationException("Рост должен быть целым числом");
        }
    }
}