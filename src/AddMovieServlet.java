import com.google.gson.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.*;

@WebServlet(name = "AddMovieServlet", urlPatterns = "/api/addMovie")
public class AddMovieServlet extends HttpServlet {
    private DataSource dataSource;

    public void init() throws ServletException {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedbexample");
        } catch (NamingException e) {
            throw new ServletException("Error initializing data source", e);
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");

        String title = request.getParameter("title");
        int year = 0;
        if (request.getParameter("myear") != null) {
            year = Integer.parseInt(request.getParameter("myear"));
        }
        String director = request.getParameter("director");
        String starName = request.getParameter("starName");
        int starBirthYear = 0;
        if (request.getParameter("starYear") != null && !request.getParameter("starYear").isEmpty()) {
            starBirthYear = Integer.parseInt(request.getParameter("starYear"));
        }
        String genreName = request.getParameter("genre");

        try (Connection conn = dataSource.getConnection()) {
            PreparedStatement checkMovieExistsStatement = conn.prepareStatement("SELECT id FROM movies WHERE title = ? AND year = ? AND director = ?");
            checkMovieExistsStatement.setString(1, title);
            checkMovieExistsStatement.setInt(2, year);
            checkMovieExistsStatement.setString(3, director);
            ResultSet existingMovie = checkMovieExistsStatement.executeQuery();

            if (existingMovie.next()) {
                System.out.println("Already existing movie");
                JsonObject jsonResponse = new JsonObject();
                jsonResponse.addProperty("status", "fail");
                jsonResponse.addProperty("message", "Movie already exists in the database");
                response.getWriter().write(jsonResponse.toString());
                return;
            }

            CallableStatement cs = conn.prepareCall("{call add_movie(?, ?, ?, ?, ?, ?)}");
            cs.setString(1, title);
            cs.setInt(2, year);
            cs.setString(3, director);
            cs.setString(4, starName);
            cs.setInt(5, starBirthYear);
            cs.setString(6, genreName);
            cs.execute();
            String fetchGenreIdQuery = "SELECT id FROM genres WHERE name = ?";
            String fetchStarIdQuery = "SELECT id FROM stars WHERE name = ?";

            PreparedStatement fetchGenreStatement = conn.prepareStatement(fetchGenreIdQuery);
            PreparedStatement fetchStarStatement = conn.prepareStatement(fetchStarIdQuery);
            fetchGenreStatement.setString(1, genreName);
            fetchStarStatement.setString(1, starName);

            ResultSet genreSet = fetchGenreStatement.executeQuery();
            ResultSet starSet = fetchStarStatement.executeQuery();
            String fetchIDsQuery = "SELECT MAX(id) FROM movies";
            String existingStarID = null;
            String existingGenreID = null;

            if (!starSet.next()) { // new star
                fetchIDsQuery += ", (SELECT MAX(id) FROM stars) AS starID";
            } else {
                existingStarID = starSet.getString(1);
            }
            if (!genreSet.next()) { // new genre
                fetchIDsQuery += ", (SELECT MAX(id) FROM genres) AS genreID";
            } else {
                existingGenreID = genreSet.getString(1);
            }
            System.out.println(genreName);
            System.out.println(fetchIDsQuery);

            ResultSet rs = conn.createStatement().executeQuery(fetchIDsQuery);
            JsonObject jsonResponse = new JsonObject();
            if (rs.next()) {
                String movieID = rs.getString(1);
                String starID;
                if (existingStarID != null) {
                    starID = existingStarID;
                } else {
                    starID = rs.getString("starID");
                }
                String genreID;
                if (existingGenreID != null) {
                    genreID = existingGenreID;
                } else {
                    genreID = rs.getString("genreID");
                }

                jsonResponse.addProperty("status", "success");
                jsonResponse.addProperty("message", "Movie added successfully");
                jsonResponse.addProperty("movieID", movieID);
                jsonResponse.addProperty("starID", starID);
                jsonResponse.addProperty("genreID", genreID);
            } else {
                jsonResponse.addProperty("status", "fail");
                jsonResponse.addProperty("message", "Failed to fetch IDs");
            }
            response.getWriter().write(jsonResponse.toString());

            //response.getWriter().write("{\"status\": \"success\", \"message\": \"Movie added successfully\"}");
        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"status\": \"error\", \"message\": \"Database error: " + e.getMessage() + "\"}");
        }
    }
}