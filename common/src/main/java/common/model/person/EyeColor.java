package common.model.person;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;

@XmlEnum
@XmlType(name = "eyeColor")
public enum EyeColor {
    BLACK,
    BLUE,
    WHITE,
    BROWN;
}