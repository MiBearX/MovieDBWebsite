import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;


@WebServlet(name = "SingleMovieServlet", urlPatterns = "/api/single-movie")
public class SingleMovieServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;

    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedbexample");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }


    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json"); // Response mime type

        // Retrieve parameter id from url request.
        String id = request.getParameter("id");

        // The log message can be found in localhost log
        request.getServletContext().log("getting id: " + id);

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {
            // Get a connection from dataSource

            // Construct a query with parameter represented by "?"
            /*String query = "SELECT m.id, m.title, m.year, m.director, ra.rating," +
                    "GROUP_CONCAT(DISTINCT genres.name ORDER BY genres.name) AS genres, " +
                    "GROUP_CONCAT(DISTINCT CONCAT(stars.name, ':', stars.id) ORDER BY stars.name) AS starsWithId FROM movies m " +
                    "LEFT JOIN ratings ra ON m.id = ra.movieId LEFT JOIN genres_in_movies gm ON m.id = gm.movieId " +
                    "LEFT JOIN genres ON gm.genreId = genres.id LEFT JOIN stars_in_movies sm ON m.id = sm.movieId " +
                    "LEFT JOIN stars ON sm.starId = stars.id WHERE m.id = ? GROUP BY m.id, m.title, m.year, m.director, " +
                    "ra.rating ORDER BY ra.rating DESC LIMIT 20;";*/
            String query = "SELECT m.id, m.title, m.year, m.director, ra.rating, " +
                    "GROUP_CONCAT(DISTINCT CONCAT(genres.name, ':', genres.id) ORDER BY genres.name) AS genres, " +
                    "GROUP_CONCAT(DISTINCT CONCAT(stars.name, ':', stars.id) " +
                    "ORDER BY star_counts.stars_played DESC, stars.name) AS starsWithId FROM movies m " +
                    "LEFT JOIN ratings ra ON m.id = ra.movieId LEFT JOIN genres_in_movies gm ON m.id = " +
                    "gm.movieId LEFT JOIN genres ON gm.genreId = genres.id LEFT JOIN stars_in_movies sm " +
                    "ON m.id = sm.movieId LEFT JOIN stars ON sm.starId = stars.id " +
                    "LEFT JOIN (SELECT starId, COUNT(*) AS stars_played FROM stars_in_movies GROUP BY starId) AS" +
                    " star_counts ON stars.id = star_counts.starId WHERE m.id = ? " +
                    "GROUP BY m.id, m.title, m.year, m.director, ra.rating, star_counts.stars_played ORDER BY star_counts.stars_played DESC;";


            // Declare our statement
            PreparedStatement statement = conn.prepareStatement(query);

            // Set the parameter represented by "?" in the query to the id we get from url,
            // num 1 indicates the first "?" in the query
            statement.setString(1, id);

            // Perform the query
            ResultSet rs = statement.executeQuery();

            JsonArray jsonArray = new JsonArray();

            // Iterate through each row of rs
            while (rs.next()) {

                String movie_id = rs.getString("id");
                String movie_title = rs.getString("title");
                String movie_year = rs.getString("year");
                String movie_director = rs.getString("director");
                String genres = rs.getString("genres");
                String starsWithID = rs.getString("starsWithId"); // strings have star:ID format
                String rating = rs.getString("rating");


                // Create a JsonObject based on the data we retrieve from rs
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("movie_id", movie_id);
                jsonObject.addProperty("movie_title", movie_title);
                jsonObject.addProperty("movie_year", movie_year);
                jsonObject.addProperty("movie_director", movie_director);
                jsonObject.addProperty("genres", genres);
                jsonObject.addProperty("stars", starsWithID);
                jsonObject.addProperty("rating", rating);

                jsonArray.add(jsonObject);
            }
            rs.close();
            statement.close();

            // Write JSON string to output
            out.write(jsonArray.toString());
            // Set response status to 200 (OK)
            response.setStatus(200);

        } catch (Exception e) {
            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // Log error to localhost log
            request.getServletContext().log("Error:", e);
            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            out.close();
        }

        // Always remember to close db connection after usage. Here it's done by try-with-resources

    }
}
