package common.model.person;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;

@XmlEnum
@XmlType(name = "hairColor")
public enum HairColor {
    GREEN,
    RED,
    ORANGE,
    WHITE;
}