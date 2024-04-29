import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@WebServlet("/api/payment")
public class PlaceOrderServlet extends HttpServlet {
    private DataSource dataSource;

    public void init() throws ServletException {
        try {
            javax.naming.Context initCtx = new javax.naming.InitialContext();
            javax.naming.Context envCtx = (javax.naming.Context) initCtx.lookup("java:comp/env");
            dataSource = (DataSource) envCtx.lookup("jdbc/moviedbexample");
        } catch (Exception e) {
            throw new ServletException("DB connection error", e);
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");

        String firstName = request.getParameter("firstName");
        String lastName = request.getParameter("lastName");
        String creditCardNumber = request.getParameter("creditCardNumber");
        String expiration = request.getParameter("expiration");
        String user_email = request.getParameter("sessionusername");

        try (Connection conn = dataSource.getConnection()) {
            if (validateCreditCard(conn, firstName, lastName, creditCardNumber, expiration)) {
                if (recordSale(conn, user_email)) {
                    response.getWriter().write("{\"status\": \"success\", \"message\": \"Payment processed and sale recorded.\"}");
                } else {
                    response.getWriter().write("{\"status\": \"fail\", \"message\": \"Failed to record the sale.\"}");
                }
            } else {
                response.getWriter().write("{\"status\": \"fail\", \"message\": \"Invalid credit card details.\"}");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.getWriter().write("{\"status\": \"fail\", \"message\": \"Database error: " + e.getMessage() + "\"}");
        }
    }

    private boolean validateCreditCard(Connection conn, String firstName, String lastName, String ccNumber, String expiration) throws SQLException {
        String query = "SELECT COUNT(*) AS count FROM creditcards WHERE firstName = ? AND lastName = ? AND id = ? AND expiration = ?";
        try (PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setString(1, firstName);
            statement.setString(2, lastName);
            statement.setString(3, ccNumber);
            statement.setString(4, expiration);
            ResultSet rs = statement.executeQuery();
            return rs.next() && rs.getInt("count") > 0;
        }
    }

    private boolean recordSale(Connection conn, String user_email) throws SQLException {
        // Assume a random movie ID for the sake of this example.
        int randomMovieId = 999999999;
        String customerIdQuery = "SELECT id FROM customers WHERE email = ?";

        try (PreparedStatement customerStatement = conn.prepareStatement(customerIdQuery)) {
            customerStatement.setString(1, user_email);
            ResultSet customerRs = customerStatement.executeQuery();
            if (customerRs.next()) {
                int customerId = customerRs.getInt("id");
                String insertSaleQuery = "INSERT INTO sales (customerID, movieID, saleDate) VALUES (?, ?, CURDATE())";

                try (PreparedStatement saleStatement = conn.prepareStatement(insertSaleQuery)) {
                    saleStatement.setInt(1, customerId);
                    saleStatement.setInt(2, randomMovieId);
                    int affectedRows = saleStatement.executeUpdate();
                    return affectedRows > 0;
                }
            } else {
                throw new SQLException("Customer not found with email: " + user_email);
            }
        } catch (SQLException e) {
            throw new SQLException("Failed to record sale: " + e.getMessage());
        }
    }

}