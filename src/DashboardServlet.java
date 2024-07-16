import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.ServletException;
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

@WebServlet(name = "DashboardServlet", urlPatterns = "/api/dashboard")
public class DashboardServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;

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
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String recaptchaResponse = request.getParameter("g-recaptcha-response"); // Extract reCAPTCHA token
        PrintWriter out = response.getWriter();
        try {
            RecaptchaVerifyUtils.verify(recaptchaResponse);
        } catch (Exception e) {
            out.println("<html>");
            out.println("<head><title>Error</title></head>");
            out.println("<body>");
            out.println("<p>recaptcha verification error</p>");
            out.println("<p>" + e.getMessage() + "</p>");
            out.println("</body>");
            out.println("</html>");

            out.close();
            return;
        }


        try (Connection conn = dataSource.getConnection()) {
            String query = "SELECT * FROM employees WHERE email = ?";
            try (PreparedStatement statement = conn.prepareStatement(query)) {
                statement.setString(1, email);

                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next()) {
                        /*String truePassword = rs.getString("password");
                        boolean success = password.equals(truePassword);*/
                        String encryptedPassword = rs.getString("password");
                        boolean success = new StrongPasswordEncryptor().checkPassword(password, encryptedPassword);
                        if (!success) {
                            response.getWriter().write("{\"status\": \"fail\", \"message\": \"Invalid password\"}");
                        } else { // happy path
                            JsonObject metaData = new JsonObject();
                            request.getSession().setAttribute("user", new User(email));
                            request.getSession().setAttribute("isLoggedIn", true);
                            String getTablesQuery = "SELECT table_name FROM information_schema.tables WHERE table_schema = 'moviedb'";
                            PreparedStatement tableStatement = conn.prepareStatement(getTablesQuery);
                            ResultSet tables = tableStatement.executeQuery();
                            while (tables.next()) {
                                JsonArray columnsArray = new JsonArray();
                                String tableName = tables.getString("table_name");
                                String getColumnsQuery = "SHOW COLUMNS FROM " + tableName;
                                PreparedStatement columnStatement = conn.prepareStatement(getColumnsQuery);
                                ResultSet columns = columnStatement.executeQuery();
                                while (columns.next()) {
                                    JsonObject columnObject = new JsonObject();
                                    String columnName = columns.getString("Field");
                                    String columnType = columns.getString("Type");
                                    columnObject.addProperty("name", columnName);
                                    columnObject.addProperty("type", columnType);
                                    columnsArray.add(columnObject);
                                }
                                metaData.add(tableName, columnsArray);
                            }
                            out.write(metaData.toString());

                            out.close();

                            //response.getWriter().write("{\"status\": \"success\", \"message\": \"Login successful\"}");
                        }
                    } else {
                        response.getWriter().write("{\"status\": \"fail\", \"message\": \"Invalid email or password\"}");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.getWriter().write("{\"status\": \"fail\", \"message\": \"Database error: " + e.getMessage() + "\"}");
        }
    }
}