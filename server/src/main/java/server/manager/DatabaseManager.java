package server.manager;

import java.sql.*;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.List;

import server.utils.hashSHA_384;
import common.model.movie.*;
import common.model.person.*;
import common.utils.Config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
    
public class DatabaseManager {  
    private static final Logger log = LogManager.getLogger(DatabaseManager.class.getName());

    private Connection connection;

    public DatabaseManager(CollectionManager cl) throws SQLException {
        this.connection = DriverManager.getConnection(Config.getDbUrl(), Config.getDbUser(), Config.getDbPassword());
        cl.setMovies(loadCollection());
        log.info("Подключение к БД успешно!");
    }

    public Connection getConnection() {
        return connection;
    }

    public List<String> getEnumValues(String typeName) throws SQLException {
        List<String> values = new ArrayList<>();
        String sql = "SELECT e.enumlabel " +
                    "FROM pg_enum e " +
                    "JOIN pg_type t ON e.enumtypid = t.oid " +
                    "WHERE t.typname = ? " +
                    "ORDER BY e.enumsortorder"; 

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, typeName);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    values.add(rs.getString("enumlabel"));
                }
            }
        }
        return values;
    }

    public LinkedList<Movie> loadCollection() throws SQLException {
        LinkedList<Movie> collection = new LinkedList<>();
        
        String sql = "SELECT m.*, p.name as p_name, p.height as p_height,p.eye_color, p.hair_color, p.nationality FROM movies m " +
                    "LEFT JOIN person p ON m.screenwriter_id = p.id";

        try (Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Person writer = new Person(
                    rs.getString("p_name"),
                    rs.getInt("p_height"),
                    EyeColor.valueOf(rs.getString("eye_color")),
                    HairColor.valueOf(rs.getString("hair_color")),
                    Country.valueOf(rs.getString("nationality"))
                );

                Coordinates coords = new Coordinates(
                    rs.getLong("x"),
                    rs.getFloat("y")
                );

                Movie movie = new Movie(
                    rs.getLong("id"),
                    rs.getString("name"),
                    coords,
                    rs.getDate("creation_date").toLocalDate(),
                    rs.getInt("oscars_count"),
                    rs.getDouble("total_box_office"),
                    rs.getLong("usa_box_office"),
                    Genre.valueOf(rs.getString("genre")),
                    writer,
                    rs.getString("owner_login")
                );
                
                collection.add(movie);
            }
        }
        return collection;
    }

    public long insertMovieToDb(Movie movie, String ownerLogin) throws SQLException {

        String personSql = "INSERT INTO person (name, height, eye_color, hair_color, nationality) " +
                           "VALUES (?, ?, ?::person_eye_color, ?::person_hair_color, ?::person_nationality) RETURNING id";
        
        int personId;
        try (PreparedStatement ps = connection.prepareStatement(personSql)) {
            Person p = movie.getScreenWriter();
            ps.setString(1, p.getName());
            ps.setInt(2, p.getHeight());
            ps.setString(3, p.getEyeColor().toString());
            ps.setString(4, p.getHairColor().toString());
            ps.setString(5, p.getNationality().toString());
            
            ResultSet rs = ps.executeQuery();
            rs.next();
            personId = rs.getInt(1);
        }

        String movieSql = "INSERT INTO movies (name, x, y, creation_date, oscars_count, total_box_office, usa_box_office, genre, screenwriter_id, owner_login) " +
                          "VALUES (?, ?, ?, ?, ?, ?, ?, ?::movie_genre, ?, ?)";
        
        try (PreparedStatement ps = connection.prepareStatement(movieSql)) {
            ps.setString(1, movie.getName());
            ps.setLong(2, movie.getCoordinates().getX());
            ps.setFloat(3, movie.getCoordinates().getY());
            ps.setDate(4, java.sql.Date.valueOf(movie.getCreationDate()));
            ps.setInt(5, movie.getOscarsCount());
            ps.setDouble(6, movie.getTotalBoxOffice());
            ps.setLong(7, movie.getUsaBoxOffice());
            ps.setString(8, movie.getGenre().toString());
            ps.setInt(9, personId);
            ps.setString(10, ownerLogin); 
            
            ps.executeUpdate();
        }
            return personId;
        }

    public boolean deleteMovieById(long movieId, String userLogin) throws SQLException {
        String sql = "DELETE FROM movies WHERE id = ? AND owner_login = ?";
        
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, movieId);
            ps.setString(2, userLogin);
            
            int rowsAffected = ps.executeUpdate();
            
            return rowsAffected > 0;
        }
    }

    public boolean deleteMoviesGreaterThan(long id, String userLogin) throws SQLException {
            String sql = "DELETE FROM movies WHERE id > ? AND owner_login = ?";
            
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setLong(1, id);
                ps.setString(2, userLogin);

                int rowsAffected = ps.executeUpdate();
                return rowsAffected > 0;
            }


    }

    public boolean updateMovie(long id, Movie newMovie, String userLogin) throws SQLException {
        String sql = "UPDATE movies SET name = ?, x = ?, y = ?, oscars_count = ?, total_box_office = ?, usa_box_office = ?, genre = ?::movie_genre WHERE id = ? AND owner_login = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, newMovie.getName());
            ps.setLong(2, newMovie.getCoordinates().getX());
            ps.setFloat(3, newMovie.getCoordinates().getY());
            ps.setInt(4, newMovie.getOscarsCount());
            ps.setDouble(5, newMovie.getTotalBoxOffice());
            ps.setLong(6, newMovie.getUsaBoxOffice());
            ps.setString(7, newMovie.getGenre().toString());
            ps.setLong(8, id);
            ps.setString(9, userLogin);

            return ps.executeUpdate() > 0;
        }
    }

    public boolean checkAuth(String userLogin, String userPassword) throws SQLException {
        String sql = "SELECT id AS userID FROM users WHERE login = ? AND password_hash = ?";
        String hashedPass = hashSHA_384.hash(userPassword);

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, userLogin);
            ps.setString(2, hashedPass);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();            
            }
        }
    }   

    public boolean checkPermissionById(long movieID, String userLogin) throws SQLException {
        String sql = "SELECT id FROM movies WHERE owner_login = ? AND id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, userLogin);
            ps.setLong(2, movieID);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();            
            }
        }
    } 
    
    public void createUser(String newUserPassword, String newUserLogin) throws SQLException {
        String userSql = "INSERT INTO users (password_hash, login) VALUES (?, ?) RETURNING id";
        
        try (PreparedStatement ps = connection.prepareStatement(userSql)) {
            String hashedPass = hashSHA_384.hash(newUserPassword);
            
            ps.setString(1, hashedPass);
            ps.setString(2, newUserLogin);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int generatedId = rs.getInt(1);
                    log.info("Создан пользователь с ID: " + generatedId);
                }
            }
        }
    }
}
