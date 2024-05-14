import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.*;
import java.util.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class UpdateXMLServlet {
    private static final long serialVersionUID = 2L;
    private static DataSource dataSource;
    public static void main(String[] args) {
        try {
            // Establish database connection

            Connection conn = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/moviedb", "mytestuser", "My6$Password");
            System.out.println("Connected to database.");
            Statement stmt = conn.createStatement();
            String query = "select * from movies where title = 'Elipsis'";
            ResultSet rs = stmt.executeQuery(query);
            if (rs.next()) {
                System.out.println(rs.getString("year"));
            }

            // Load movie data and process it
            HashMap<String, Object> movieData = MovieDataLoader.loadMovies();
            HashMap<String, Film> films = (HashMap<String, Film>) movieData.get("films");
            List<String> skippedEntries = MovieDataLoader.getSkippedEntries();

            System.out.println("Film ID,Title,Year,Director");

            List<PreparedStatement> movieStmts = new ArrayList<>();
            List<PreparedStatement> starStmts = new ArrayList<>();
            List<PreparedStatement> genreStmts = new ArrayList<>();
            List<PreparedStatement> genreInMovieStmts = new ArrayList<>();
            List<PreparedStatement> ratingStmts = new ArrayList<>();
            List<PreparedStatement> starsInMoviesStmts = new ArrayList<>();

            for (Film film : films.values()) {
                prepareMovieStatement(conn, film, movieStmts);
                prepareStarsStatement(conn, film.getStars(), starStmts, film.getFid(), starsInMoviesStmts);
                prepareGenresStatement(conn, film.getGenre(), film.getFid(), genreStmts, genreInMovieStmts);
                prepareRatingsStatement(conn, film.getFid(), ratingStmts); // Added this line
                //prepareStarsInMoviesStatement(conn, film.getStars(), film.getFid());
            }

            executeBatchStatements(movieStmts, "movies");
            executeBatchStatements(starStmts, "stars");
            executeBatchStatements(genreStmts, "genres");
            executeBatchStatements(genreInMovieStmts, "genres in movies");
            executeBatchStatements(ratingStmts, "ratings");

            System.out.println("Update successful");

            System.out.println("Skipped Entries:");
            for (String entry : skippedEntries) {
                System.out.println(entry);
            }

            conn.close();
        } catch (SQLException e) {
            System.err.println("SQL error: " + e.getMessage());
            e.printStackTrace();  // Print stack trace for SQL errors
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();  // Print stack trace for any other errors
        }
    }
    // Helper methods (prepareMovieStatement, prepareStarsStatement, prepareGenresStatement, executeBatchStatements) need to be defined here


    public static void prepareMovieStatement(Connection conn, Film film, List<PreparedStatement> stmts) throws SQLException {
        // Check if film ID, title, and director are not blank
        if ((film.getFid() == null || film.getFid().isEmpty()) ||
                (film.getTitle() == null || film.getTitle().isEmpty()) ||
                (film.getDirector() == null || film.getDirector().isEmpty())) {
            System.out.println("Skipping movie: Film ID, title, or director is blank.");
            return; // Skip the entire film if any of these fields are blank
        }

        String year = film.getYear();
        // Truncate 'year' if it exceeds the allowed length
        if (year != null && year.length() > 4) {
            year = year.substring(0, 4);
        }
        // Check if 'year' consists only of digits
        if (year == null || !year.matches("\\d+")) {
            year = "4004"; // Set to 4004 if 'year' is null or not all numbers
        }

        String insertMovieSQL = "INSERT INTO movies (id, title, year, director) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE id=id;";
        PreparedStatement insertStmt = conn.prepareStatement(insertMovieSQL);
        insertStmt.setString(1, film.getFid());
        insertStmt.setString(2, film.getTitle());
        insertStmt.setString(3, year);
        insertStmt.setString(4, film.getDirector());
        stmts.add(insertStmt);
    }



    public static void prepareStarsStatement(Connection conn, List<Actor> stars, List<PreparedStatement> stmts, String movieID, List<PreparedStatement> star_in_movieStatements) throws SQLException {
        String insertStarSQL = "INSERT INTO stars (id, name, birthYear) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE id=id;";
        PreparedStatement insertStmt = conn.prepareStatement(insertStarSQL);
        for (Actor star : stars) {
            String birthYear = "4004"; // Default value if birth year is null or empty
            if (star.getDob() != null && !star.getDob().isEmpty()) {
                birthYear = star.getDob();
            }
            // Truncate 'birthYear' if it exceeds the allowed length
            if (birthYear != null && birthYear.length() > 4) {
                birthYear = birthYear.substring(0, 4);
            }
            // Check if 'birthYear' consists only of digits
            if (birthYear == null || !birthYear.matches("\\d+")) {
                birthYear = "4004"; // Set to 4004 if 'birthYear' is null or not all numbers
            }
            System.out.println("Star: " + star.getStageName() + ", Birth Year: " + birthYear); // Print statement
            String star_id = generateRandomId();
            insertStmt.setString(1, star_id);
            insertStmt.setString(2, star.getStageName());
            insertStmt.setString(3, birthYear);
            insertStmt.addBatch();
            //prepareStarsInMoviesStatement(conn, stars, movieID, star_in_movieStatements, star_id);
            String insertStarInMovieSQL = "INSERT INTO stars_in_movies (starId, movieId) VALUES (?, ?) ON DUPLICATE KEY UPDATE starId=starId;"; // SQL statement for inserting into stars_in_movies
            PreparedStatement insertStarInMovieStmt = conn.prepareStatement(insertStarInMovieSQL);
            insertStarInMovieStmt.setString(1, star_id);
            insertStarInMovieStmt.setString(2, movieID);
            //insertStmt.addBatch(); // Add batch for each star

            star_in_movieStatements.add(insertStmt); // Add the prepared statement to the list

        }
        stmts.add(insertStmt);
    }

    public static void prepareGenresStatement(Connection conn, String genre, String movieId, List<PreparedStatement> genreStmts, List<PreparedStatement> genreInMovieStmts) throws SQLException {
        // If genre is null or empty, assign it the name "Unknown"
        if (genre == null || genre.isEmpty()) {
            genre = "Unknown";
        }

        String insertGenreSQL = "INSERT INTO genres (name) VALUES (?) ON DUPLICATE KEY UPDATE id=id;";
        PreparedStatement insertGenreStmt = conn.prepareStatement(insertGenreSQL);
        insertGenreStmt.setString(1, genre);
        genreStmts.add(insertGenreStmt);

        String insertGenreInMovieSQL = "INSERT IGNORE INTO genres_in_movies (genreId, movieId) VALUES ((SELECT id FROM genres WHERE name = ?), ?);";
        PreparedStatement insertGenreInMovieStmt = conn.prepareStatement(insertGenreInMovieSQL);
        insertGenreInMovieStmt.setString(1, genre);
        insertGenreInMovieStmt.setString(2, movieId);
        genreInMovieStmts.add(insertGenreInMovieStmt);
    }

    private String truncateYear(String year) {
        if (year != null && year.length() > 4) {
            return year.substring(0, 4);
        }
        return year;
    }

    public static void prepareRatingsStatement(Connection conn, String movieId, List<PreparedStatement> stmts) throws SQLException {
        String insertRatingSQL = "INSERT INTO ratings (movieId, rating, numVotes) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE movieId=movieId;";
        PreparedStatement insertStmt = conn.prepareStatement(insertRatingSQL);
        insertStmt.setString(1, movieId);
        insertStmt.setFloat(2, generateRandomRating());
        insertStmt.setInt(3, generateRandomNumVotes());
        stmts.add(insertStmt);
    }

    public static void prepareStarsInMoviesStatement(Connection conn, List<Actor> stars, String movieId, List<PreparedStatement> stmts, String star_id) throws SQLException {
        String insertStarInMovieSQL = "INSERT INTO stars_in_movies (starId, movieId) VALUES (?, ?) ON DUPLICATE KEY UPDATE starId=starId;"; // SQL statement for inserting into stars_in_movies
        PreparedStatement insertStmt = conn.prepareStatement(insertStarInMovieSQL);
        insertStmt.setString(1, star_id);
        insertStmt.setString(2, movieId);
        insertStmt.addBatch(); // Add batch for each star

        stmts.add(insertStmt); // Add the prepared statement to the list
    }

    public static float generateRandomRating() {
        Random random = new Random();
        return random.nextFloat() * 10; // Assuming ratings are out of 10
    }

    public static int generateRandomNumVotes() {
        Random random = new Random();
        return random.nextInt(1000); // Generating a random number of votes
    }

    public static void executeBatchStatements(List<PreparedStatement> stmts, String queryType) throws SQLException {
        for (PreparedStatement stmt : stmts) {
            try {
                stmt.executeUpdate();
            }
            catch (SQLException e) {
                e.printStackTrace();
                continue;
            }

            stmt.clearBatch();
            stmt.close();
        }
        System.out.println("Batch SQL queries for " + queryType + " completed.");
    }
    public static String generateRandomId() {
        Random random = new Random();
        return String.valueOf(random.nextInt(1000000));
    }
}