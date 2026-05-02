package client.controller;

import common.model.movie.Movie;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class MovieDataController {

    public static List<Movie> filter(List<Movie> list, String field, String query) {
        if (query == null || query.trim().isEmpty() || field == null || list == null) {
            return list;
        }

        String q = query.toLowerCase().trim();

        return list.stream().filter(m -> {
            if (m == null) return false;
            switch (field) {
                case "name":
                    return m.getName() != null && m.getName().toLowerCase().contains(q);
                case "id":
                    return String.valueOf(m.getId()).contains(q);
                case "oscars":
                    return String.valueOf(m.getOscarsCount()).contains(q);
                case "owner":
                    return m.getOwner_login() != null && m.getOwner_login().toLowerCase().contains(q);
                case "genre":
                    return m.getGenre() != null && m.getGenre().toString().toLowerCase().contains(q);
                case "totalBoxOffice":
                    return m.getTotalBoxOffice() != null && String.valueOf(m.getTotalBoxOffice()).contains(q);
                default:
                    return true;
            }
        }).collect(Collectors.toList());
    }


    public static List<Movie> sort(List<Movie> list, String sortType) {
        if (sortType == null || list == null || list.isEmpty()) {
            return list;
        }
        Comparator<Movie> comparator = null;

        switch (sortType) {
            case "id": comparator = Comparator.comparingLong(Movie::getId);
                break;
            case "name": comparator = Comparator.comparing(Movie::getName, Comparator.nullsLast(String::compareTo));
                break;
            case "x": comparator = Comparator.comparingLong(m -> m.getCoordinates().getX());
                break;
            case "y": comparator = Comparator.comparingDouble(m -> m.getCoordinates().getY());
                break;
            case "date": comparator = Comparator.comparing(Movie::getCreationDate, Comparator.nullsLast(Comparator.naturalOrder()));
                break;
            case "oscars": comparator = Comparator.comparingInt(Movie::getOscarsCount);
                break;
            case "totalBoxOffice": comparator = Comparator.comparing(Movie::getTotalBoxOffice, Comparator.nullsLast(Double::compareTo));
                break;
            case "usaBoxOffice": comparator = Comparator.comparing(Movie::getUsaBoxOffice, Comparator.nullsLast(Long::compareTo));
                break;
            case "genre": comparator = Comparator.comparing(m -> m.getGenre() != null ? m.getGenre().toString() : "", Comparator.nullsLast(String::compareTo));
                break;
            case "owner": comparator = Comparator.comparing(Movie::getOwner_login, Comparator.nullsLast(String::compareTo));
                break;
            default:
                return list;
        }

        return list.stream().sorted(comparator).collect(Collectors.toList());
    }
}
