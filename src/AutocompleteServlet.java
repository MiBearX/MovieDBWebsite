import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.ServletException;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.jasypt.util.password.StrongPasswordEncryptor;

@WebServlet(name = "AutocompleteServlet", urlPatterns = "/api/autocomplete")
public class AutocompleteServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;

    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedbexample");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json"); // MIME type for JSON response
        String userQuery = request.getParameter("query");
        String[] queryWords = userQuery.split(" ");
        String searchQuery = String.join("* ", queryWords) + "*";
        PrintWriter out = response.getWriter();

        try (Connection conn = dataSource.getConnection()) {
            String query = "SELECT id, title, year, director FROM movies WHERE MATCH(title) AGAINST(? IN BOOLEAN MODE)";

            try (PreparedStatement statement = conn.prepareStatement(query)) {
                statement.setString(1, searchQuery);
                try (ResultSet rs = statement.executeQuery()) {
                    JsonArray jsonArray = new JsonArray();
                    for (int i = 0; i < 10; i++) {
                        if (rs.next()) {
                            JsonObject jsonObject = new JsonObject();
                            JsonObject jsonDataObject = new JsonObject();
                            String movieId = rs.getString("id");
                            String movieTitle = rs.getString("title");
                            String movieYear = rs.getString("year");
                            String movieDirector = rs.getString("director");
                            jsonObject.addProperty("value", movieTitle);
                            jsonDataObject.addProperty("movieId", movieId);
                            jsonDataObject.addProperty("movieYear", movieYear);
                            jsonDataObject.addProperty("movieDirector", movieDirector);
                            jsonObject.add("data", jsonDataObject);
                            jsonArray.add(jsonObject);
                        }
                    }
                    rs.close();
                    statement.close();
                    out.write(jsonArray.toString());
                    response.setStatus(200);
                }

            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.getWriter().write("{\"status\": \"fail\", \"message\": \"Database error: " + e.getMessage() + "\"}");
        }
    }
}