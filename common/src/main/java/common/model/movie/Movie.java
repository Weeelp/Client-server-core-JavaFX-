package common.model.movie;

import java.io.Serializable;
import java.time.LocalDate;

import common.model.person.Person;


public class Movie implements Serializable{  
    private static final long serialVersionUID = 1L;
    
    private long id;
    private String name; 
    private Coordinates coordinates; 
    private LocalDate creationDate;
    private int oscarsCount; 
    private Double totalBoxOffice; 
    private Long usaBoxOffice; 
    private Genre genre; 
    private Person screenWriter;
    private String owner_login;
    
    public Movie() {}
    
    public Movie(long id, String name, 
                Coordinates coordinates, 
                LocalDate creationDate,
                int oscarsCount,
                Double totalBoxOffice, 
                Long usaBoxOffice, 
                Genre genre, 
                Person screenWriter,
                String owner_login) {
        this.id = id;
        this.name = name;
        this.coordinates = coordinates;
        this.creationDate = creationDate;
        this.oscarsCount = oscarsCount;
        this.totalBoxOffice = totalBoxOffice;
        this.usaBoxOffice = usaBoxOffice;
        this.genre = genre;
        this.screenWriter = screenWriter;
        this.owner_login = owner_login;
    }
    
    public long getId (){ return this.id; }

    public String getName (){ return this.name; }

    public LocalDate getCreationDate (){ return this.creationDate; }

    public Coordinates getCoordinates (){ return this.coordinates; }

    public int getOscarsCount (){ return this.oscarsCount; }

    public Double getTotalBoxOffice (){ return this.totalBoxOffice; }

    public Long getUsaBoxOffice (){ return this.usaBoxOffice; }

    public Genre getGenre (){ return this.genre; }

    public Person getScreenWriter (){ return this.screenWriter; }

    public String getOwner_login (){ return this.owner_login; }

    public void setId (long id){ this.id = id; }

    public void setName (String name){ this.name = name; }
    
    public void setCreationDate (LocalDate creationDate){ this.creationDate = creationDate; }

    public void setCoordinates (Coordinates coordinates){ this.coordinates = coordinates; }

    public void setOscarsCount (int oscarsCount){ this.oscarsCount = oscarsCount; }
    
    public void setTotalBoxOffice (Double totalBoxOffice){ this.totalBoxOffice = totalBoxOffice; }
    
    public void setUsaBoxOffice (Long usaBoxOffice){ this.usaBoxOffice = usaBoxOffice; }
    
    public void setGenre (Genre genre){ this.genre = genre; }

    public void setScreenWriter (Person screenWriter){ this.screenWriter = screenWriter;}

    public void setOwner_login (String owner_login){ this.owner_login = owner_login; }

    @Override
    public String toString() {
        return id + ": " + name + " (" + screenWriter + ") - " + genre + " " + oscarsCount + " (" + creationDate + ")";
    }
}