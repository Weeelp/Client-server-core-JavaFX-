import random
from datetime import date, timedelta

genres = ["WESTERN", "TRAGEDY", "COMEDY"]
eye_colors = ["WHITE", "BLACK", "BLUE"]
hair_colors = ["WHITE", "BLACK", "ORANGE", "RED", "GREEN"]
countries = ["RUSSIA", "JAPAN", "AMERICA", "GERMANY", "ITALY"]

def generate_xml(count=1100):
    xml_content = ['<?xml version="1.0" encoding="UTF-8" standalone="yes"?>', '<movies>']
    
    start_date = date(2020, 1, 1)

    for i in range(200, 200 + count):
        m_id = i
        name = f"Movie_{i}"
        x = random.randint(140, 210)
        y = random.randint(140, 210)
        c_date = start_date + timedelta(days=random.randint(0, 2000))
        oscars = random.randint(1, 1000)
        total_box = round(random.uniform(1000, 10000000), 1)
        usa_box = random.randint(1000, 500000000)
        genre = random.choice(genres)
        
        sw_name = f"Writer_{i}"
        sw_height = random.randint(140, 210)
        sw_eye = random.choice(eye_colors)
        sw_hair = random.choice(hair_colors)
        sw_nat = random.choice(countries)

        movie_xml = f"""    <movie>
        <id>{m_id}</id>
        <name>{name}</name>
        <coordinates x="{x}" y="{y}"/>
        <creationDate>{c_date}</creationDate>
        <oscarsCount>{oscars}</oscarsCount>
        <totalBoxOffice>{total_box}</totalBoxOffice>
        <usaBoxOffice>{usa_box}</usaBoxOffice>
        <genre>{genre}</genre>
        <screenwriter name="{sw_name}" height="{sw_height}" eyeColor="{sw_eye}" hairColor="{sw_hair}" nationality="{sw_nat}"/>
    </movie>"""
        xml_content.append(movie_xml)

    xml_content.append('</movies>')
    
    with open("repo.xml", "w", encoding="utf-8") as f:
        f.write("\n".join(xml_content))
    print(f"Готово! Создано {count} записей в файле repo.xml")

if __name__ == "__main__":
    generate_xml(1100)
