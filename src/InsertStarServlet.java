import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jasypt.util.password.StrongPasswordEncryptor;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@WebServlet(name = "InsertStarServlet", urlPatterns = "/api/insertStar")
public class InsertStarServlet extends HttpServlet {
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedbexample");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json"); // MIME type for JSON response
        String starName = request.getParameter("starName");
        String starYear = request.getParameter("starYear");
        PrintWriter out = response.getWriter();
        try (Connection conn = dataSource.getConnection()) {
            String query;
            if (!starYear.isEmpty()) {
                query = "INSERT INTO stars (id, name, birthYear) \n" +
                        "SELECT CONCAT('nm', LPAD(CAST(SUBSTRING(MAX(id), 3) AS UNSIGNED) + 1, 7, '0')), ?, ? \n" +
                        "FROM stars;";
            } else {
                query = "INSERT INTO stars (id, name, birthYear) \n" +
                        "SELECT CONCAT('nm', LPAD(CAST(SUBSTRING(MAX(id), 3) AS UNSIGNED) + 1, 7, '0')), ?, NULL \n" +
                        "FROM stars;";
            }
            try (PreparedStatement statement = conn.prepareStatement(query)) {
                statement.setString(1, starName);
                if (!starYear.isEmpty()) {
                    statement.setInt(2, Integer.parseInt(starYear));
                }
                int rowsInserted = statement.executeUpdate();
                if (rowsInserted > 0) {
                    String generatedIDQuery = "SELECT MAX(id) AS starID FROM stars;";
                    PreparedStatement generatedIDStatement = conn.prepareStatement(generatedIDQuery);
                    ResultSet rs = generatedIDStatement.executeQuery();
                    System.out.println(rs.next() == true);
                    String newStarId = rs.getString("starID");
                    System.out.println(newStarId);


                    //System.out.println(newStarId);
                    JsonObject jsonResponse = new JsonObject();
                    jsonResponse.addProperty("status", "success");
                    jsonResponse.addProperty("message", "Star added successfully");
                    jsonResponse.addProperty("starID", newStarId); // Add starID to the response
                    response.getWriter().write(jsonResponse.toString());

                    //response.getWriter().write("{\"status\": \"success\",\"message\":\"Star added successfully\"}");
                } else {
                    response.getWriter().write("{\"status\": \"fail\",\"message\":\"Star already exists\"}");
                }

            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.getWriter().write("{\"status\": \"fail\", \"message\": \"Database error: " + e.getMessage() + "\"}");
        }

    }
}