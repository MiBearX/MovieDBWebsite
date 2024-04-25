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
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;

@WebServlet(name = "MovieListServlet", urlPatterns = "/api/movie_list")
public class MovieListServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private DataSource dataSource;

    // sets up sqlDB datasource
    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedbexample");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json"); // Response mime type

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {

            // Declare our statement
            Statement statement = conn.createStatement();

            //String query = "SELECT * from stars";
            /*String query = "SELECT m.id, m.title, m.year, m.director, m.rating, GROUP_CONCAT(DISTINCT g.name " +
                    "ORDER BY g.name LIMIT 3) AS genres, GROUP_CONCAT(DISTINCT s.name ORDER BY s.name LIMIT 3) " +
                    "AS stars FROM movies m LEFT JOIN genres_in_movies gm ON m.id = gm.movieId LEFT JOIN genres g " +
                    "ON gm.genreId = g.id LEFT JOIN stars_in_movies sm ON m.id = sm.movieId LEFT JOIN stars s " +
                    "ON sm.starId = s.id GROUP BY m.id ORDER BY m.rating DESC LIMIT 20";*/
            String query = "SELECT m.title, m.year, m.director, ra.rating," +
                    "GROUP_CONCAT(DISTINCT genres.name ORDER BY genres.name) AS genres, " +
                    "GROUP_CONCAT(DISTINCT CONCAT(stars.name, ':', stars.id) ORDER BY stars.name) AS starsWithId FROM movies m " +
                    "LEFT JOIN ratings ra ON m.id = ra.movieId LEFT JOIN genres_in_movies gm ON m.id = gm.movieId " +
                    "LEFT JOIN genres ON gm.genreId = genres.id LEFT JOIN stars_in_movies sm ON m.id = sm.movieId " +
                    "LEFT JOIN stars ON sm.starId = stars.id GROUP BY m.id, m.title, m.year, m.director, ra.rating " +
                    "ORDER BY ra.rating DESC LIMIT 20;";


            //GROUP_CONCAT(DISTINCT CONCAT(stars.name, ':', stars.id) ORDER BY stars.name) AS starsWithIds
            // old query line below
            // GROUP_CONCAT(DISTINCT stars.name ORDER BY stars.name) AS stars FROM movies m

            // Perform the query
            ResultSet rs = statement.executeQuery(query);

            JsonArray jsonArray = new JsonArray();

            // Iterate through each row of rs
            while (rs.next()) {
                String movie_title = rs.getString("title");
                String movie_year = rs.getString("year");
                String movie_director = rs.getString("director");
                String genres = rs.getString("genres");
                String starsWithID = rs.getString("starsWithId"); // strings have star:ID format
                String rating = rs.getString("rating");



                // might have to add to jsonObject separately
                String[] stars_array = starsWithID.split(",");
                // get just first 3 stars
                String[] starsToDisplay = Arrays.copyOfRange(stars_array, 0, 3);
                String displayStarString = String.join(", ", starsToDisplay);

                // maybe limit genres to 3
                String[] genres_array = genres.split(",");


                // Create a JsonObject based on the data we retrieve from rs
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("movie_title", movie_title);
                jsonObject.addProperty("movie_year", movie_year);
                jsonObject.addProperty("movie_director", movie_director);
                jsonObject.addProperty("genres", genres);
                jsonObject.addProperty("stars", displayStarString);
                jsonObject.addProperty("rating", rating);

                jsonArray.add(jsonObject);
            }
            rs.close();
            statement.close();

            // Log to localhost log
            request.getServletContext().log("getting " + jsonArray.size() + " results");

            // Write JSON string to output
            out.write(jsonArray.toString());
            // Set response status to 200 (OK)
            response.setStatus(200);

        } catch (Exception e) {

            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            out.close();
        }

        // Always remember to close db connection after usage. Here it's done by try-with-resources

    }
}
