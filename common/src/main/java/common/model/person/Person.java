package common.model.person;

import java.io.Serializable;

public class Person implements Serializable{
    private static final long serialVersionUID = 1L;
    private String name; 
    private int height;
    private EyeColor eyeColor; 
    private HairColor hairColor; 
    private Country nationality; 

    public Person() {}

    public Person(String name, int height, EyeColor eyeColor, HairColor hairColor, Country nationality) {
        this.name = name;
        this.height = height;
        this.eyeColor = eyeColor;
        this.hairColor = hairColor;
        this.nationality = nationality;
    }
    
    public String getName() { return this.name; }
    
    public int getHeight() { return this.height; }
    
    public EyeColor getEyeColor() { return this.eyeColor; }
    
    public HairColor getHairColor() { return this.hairColor; }
    
    public Country getNationality() { return this.nationality; }

    public void setName(String name) { this.name = name; }

    public void setHeight(int height) { this.height = height; }
    
    public void setEyeColor(EyeColor eyeColor) { this.eyeColor = eyeColor; }

    public void setHairColor(HairColor hairColor) { this.hairColor = hairColor; }

    public void setNationality(Country nationality) { this.nationality = nationality; }
       
    @Override
    public String toString() {
        return nationality + " " + name + " (" + height + "). Eye color: " + eyeColor + "; hair color: " + hairColor;
    }
}