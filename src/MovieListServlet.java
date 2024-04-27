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

            String currentPage = request.getParameter("page");
            String moviesPerPage = request.getParameter("limit");

            String orderBy = request.getParameter("orderBy");
            String order = request.getParameter("order");

            String genreId = request.getParameter("genreId");
            String titleChar = request.getParameter("titleChar");


            // build query
            String query;
            if (currentPage == null || moviesPerPage == null) {
                // if options are null, return top 20 movies
                query = "SELECT m.id, m.title, m.year, m.director, ra.rating," +
                        "GROUP_CONCAT(DISTINCT genres.name ORDER BY genres.name) AS genres, " +
                        "GROUP_CONCAT(DISTINCT CONCAT(stars.name, ':', stars.id) ORDER BY stars.name) AS starsWithId FROM movies m " +
                        "LEFT JOIN ratings ra ON m.id = ra.movieId LEFT JOIN genres_in_movies gm ON m.id = gm.movieId " +
                        "LEFT JOIN genres ON gm.genreId = genres.id LEFT JOIN stars_in_movies sm ON m.id = sm.movieId " +
                        "LEFT JOIN stars ON sm.starId = stars.id GROUP BY m.id, m.title, m.year, m.director, ra.rating " +
                        "ORDER BY ra.rating DESC LIMIT 20 OFFSET 0";
            } else {
                int current_page = Integer.parseInt(currentPage);
                int movies_per_page = Integer.parseInt(moviesPerPage);
                int offset = (current_page - 1) * movies_per_page;

                query = "SELECT m.id, m.title, m.year, m.director, ra.rating," +
                        "GROUP_CONCAT(DISTINCT CONCAT(genres.name, ':', genres.id) ORDER BY genres.name) AS genres, " +
                        "GROUP_CONCAT(DISTINCT CONCAT(stars.name, ':', stars.id) ORDER BY stars_played DESC, stars.name) AS starsWithId FROM movies m " +
                        "LEFT JOIN ratings ra ON m.id = ra.movieId LEFT JOIN genres_in_movies gm ON m.id = gm.movieId " +
                        "LEFT JOIN genres ON gm.genreId = genres.id LEFT JOIN stars_in_movies sm ON m.id = sm.movieId " +
                        "LEFT JOIN stars ON sm.starId = stars.id LEFT JOIN (SELECT starId, COUNT(*) AS stars_played " +
                        "FROM stars_in_movies GROUP BY starId) AS star_counts ON stars.id = star_counts.starId ";

                if (genreId != null) {
                    query += "WHERE gm.genreId = " + genreId + " ";
                } else if (titleChar != null) { // assume you can't filter by genre and title at the same time
                    query += "WHERE m.title LIKE '" + titleChar + "%' ";
                }

                query +="GROUP BY m.id, m.title, m.year, m.director, ra.rating ";


                // sorting
                switch (orderBy) {
                    case "title":
                        query += "ORDER BY m.title " + order + ", ra.rating DESC LIMIT " + moviesPerPage + " OFFSET " + offset;
                        break;
                    case "rating":
                        query += "ORDER BY ra.rating " + order + ", m.title ASC LIMIT " + moviesPerPage + " OFFSET " + offset;
                        break;
                    default:
                        query += "ORDER BY ra.rating DESC LIMIT " + moviesPerPage + " OFFSET " + offset;
                        break;
                }
            }



            // Perform the query
            ResultSet rs = statement.executeQuery(query);

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



                // might have to add to jsonObject separately
                String[] stars_array = starsWithID.split(",");
                // get just first 3 stars
                String[] starsToDisplay = Arrays.copyOfRange(stars_array, 0, 3);
                String displayStarString = String.join(", ", starsToDisplay);

                // maybe limit genres to 3
                String[] genres_array = genres.split(",");


                // Create a JsonObject based on the data we retrieve from rs
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("moviesPerPage", moviesPerPage);
                jsonObject.addProperty("movie_id", movie_id);
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
