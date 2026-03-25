package common.model.movie;

import common.model.person.Person;

import java.io.Serializable;

public class MovieData implements Serializable {
    private static final long serialVersionUID = 1L;

    public  String name;
    public  Coordinates coordinates;
    public  int oscarsCount;
    public  double totalBoxOffice;
    public  long usaBoxOffice;
    public  Genre genre;
    public  Person screenWriter;

    public MovieData () {};

    public MovieData(String name, Coordinates coordinates,
                     int oscarsCount, double totalBoxOffice, long usaBoxOffice,
                     Genre genre, Person screenWriter) {
        this.name = name;
        this.coordinates = coordinates;
        this.oscarsCount = oscarsCount;
        this.totalBoxOffice = totalBoxOffice;
        this.usaBoxOffice = usaBoxOffice;
        this.genre = genre;
        this.screenWriter = screenWriter;
    }
}