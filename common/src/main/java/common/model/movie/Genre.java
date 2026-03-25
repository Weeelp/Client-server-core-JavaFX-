package common.model.movie;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;

@XmlEnum
@XmlType(name = "genre")
public enum Genre {
    WESTERN,
    COMEDY,
    TRAGEDY,
    SCIENCE_FICTION;
}