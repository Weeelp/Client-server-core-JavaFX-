package common.model.person;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;

@XmlEnum
@XmlType(name = "country")
public enum Country {
    RUSSIA,
    UNITED_KINGDOM,
    GERMANY,
    ITALY,
    JAPAN,
    AMERICA;
}