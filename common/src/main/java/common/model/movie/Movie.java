package common.model.movie;

import jakarta.xml.bind.annotation.*;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import java.io.Serializable;
import java.time.LocalDate;

import common.utils.LocalDateAdapter;
import common.model.person.Person;

@XmlRootElement(name = "movie")
@XmlAccessorType(XmlAccessType.FIELD)
public class Movie implements Serializable{  
    private static final long serialVersionUID = 1L;
    
    @XmlElement(required = true)
    private long id;

    @XmlElement(required = true)
    private String name; 
    
    @XmlElement(required = true)
    private Coordinates coordinates; 
    
    @XmlElement(required = true)
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    private LocalDate creationDate;
    
    @XmlElement(required = true)
    private int oscarsCount; 
    
    @XmlElement(required = true)
    private Double totalBoxOffice; 

    @XmlElement(required = true)
    private Long usaBoxOffice; 

    @XmlElement(required = true)
    private Genre genre; 
    
    @XmlElement(required = true, name = "screenwriter")
    private Person screenWriter;
    
    public Movie() {}
    
    public Movie(long id, String name, 
                Coordinates coordinates, 
                LocalDate creationDate,
                int oscarsCount,
                Double totalBoxOffice, 
                Long usaBoxOffice, 
                Genre genre, 
                Person screenWriter) {
        this.id = id;
        this.name = name;
        this.coordinates = coordinates;
        this.creationDate = creationDate;
        this.oscarsCount = oscarsCount;
        this.totalBoxOffice = totalBoxOffice;
        this.usaBoxOffice = usaBoxOffice;
        this.genre = genre;
        this.screenWriter = screenWriter;
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

    public void setName (String name){ this.name = name; }
    
    public void setCreationDate (LocalDate creationDate){ this.creationDate = creationDate; }

    public void setCoordinates (Coordinates coordinates){ this.coordinates = coordinates; }

    public void setOscarsCount (int oscarsCount){ this.oscarsCount = oscarsCount; }
    
    public void setTotalBoxOffice (Double totalBoxOffice){ this.totalBoxOffice = totalBoxOffice; }
    
    public void setUsaBoxOffice (Long usaBoxOffice){ this.usaBoxOffice = usaBoxOffice; }
    
    public void setGenre (Genre genre){ this.genre = genre; }

    public void setScreenWriter (Person screenWriter){ this.screenWriter = screenWriter;}

    @Override
    public String toString() {
        return id + ": " + name + " (" + screenWriter + ") - " + genre + " " + oscarsCount + " (" + creationDate + ")";
    }
}