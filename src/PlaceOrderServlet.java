import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.*;

@WebServlet("/api/payment")
public class PlaceOrderServlet extends HttpServlet {
    private DataSource dataSource;

    @Override
    public void init() throws ServletException {
        try {
            javax.naming.Context initCtx = new javax.naming.InitialContext();
            javax.naming.Context envCtx = (javax.naming.Context) initCtx.lookup("java:comp/env");
            dataSource = (DataSource) envCtx.lookup("jdbc/moviedbexample");
        } catch (Exception e) {
            throw new ServletException("DB connection error: " + e.getMessage(), e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");

        String firstName = request.getParameter("firstName");
        String lastName = request.getParameter("lastName");
        String creditCardNumber = request.getParameter("creditCardNumber");
        String expiration = request.getParameter("expiration");
        String user_email = request.getParameter("sessionusername");

        try (Connection conn = dataSource.getConnection()) {
            if (validateCreditCard(conn, firstName, lastName, creditCardNumber, expiration)) {
                int saleId = recordSale(conn, user_email);
                if (saleId > 0) {
                    response.getWriter().write("{\"status\": \"success\", \"message\": \"Payment processed and sale recorded.\", \"saleId\": " + saleId + "}");
                } else {
                    response.getWriter().write("{\"status\": \"fail\", \"message\": \"Failed to record the sale.\"}");
                }
            } else {
                response.getWriter().write("{\"status\": \"fail\", \"message\": \"Invalid credit card details.\"}");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.getWriter().write("{\"status\": \"fail\", \"message\": \"Database error during transaction: " + e.getMessage() + "\"}");
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

    private int recordSale(Connection conn, String user_email) throws SQLException {
        String randomMovieId = "tt0094859";
        String customerIdQuery = "SELECT id FROM customers WHERE email = ?";

        try (PreparedStatement customerStatement = conn.prepareStatement(customerIdQuery)) {
            customerStatement.setString(1, user_email);
            ResultSet customerRs = customerStatement.executeQuery();
            if (customerRs.next()) {
                int customerId = customerRs.getInt("id");
                String insertSaleQuery = "INSERT INTO sales (customerId, movieId, saleDate) VALUES (?, ?, CURDATE())";

                try (PreparedStatement saleStatement = conn.prepareStatement(insertSaleQuery, Statement.RETURN_GENERATED_KEYS)) {
                    saleStatement.setInt(1, customerId);
                    saleStatement.setString(2, randomMovieId);
                    int affectedRows = saleStatement.executeUpdate();
                    if (affectedRows > 0) {
                        ResultSet keys = saleStatement.getGeneratedKeys();
                        if (keys.next()) {
                            return keys.getInt(1);
                        }
                    }
                    throw new SQLException("Failed to record sale, no ID generated.");
                }
            } else {
                throw new SQLException("Customer not found with email: " + user_email);
            }
        }
    }
}