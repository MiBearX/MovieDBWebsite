import java.sql.*;
import java.util.ArrayList;

import org.jasypt.util.password.PasswordEncryptor;
import org.jasypt.util.password.StrongPasswordEncryptor;

public class InsertEmployee {

    /*
     Do not run this more than once, used to insert first employee into DB.
     */
    public static void main(String[] args) throws Exception {

        String loginUser = "mytestuser";
        String loginPasswd = "My6$Password";
        String loginUrl = "jdbc:mysql://localhost:3306/moviedb";

        Class.forName("com.mysql.jdbc.Driver").newInstance();
        Connection connection = DriverManager.getConnection(loginUrl, loginUser, loginPasswd);
        Statement statement = connection.createStatement();

        // change the customers table password column from VARCHAR(20) to VARCHAR(128)
        String alterQuery = "ALTER TABLE employees MODIFY COLUMN password VARCHAR(128)";
        int alterResult = statement.executeUpdate(alterQuery);
        System.out.println("altering employee table schema completed, " + alterResult + " rows affected");


        String employeeEmail = "classta@email.edu";
        String employeePassword = "classta";
        String employeeFullName = "TA CS122B";
        // we use the StrongPasswordEncryptor from jasypt library (Java Simplified Encryption)
        //  it internally use SHA-256 algorithm and 10,000 iterations to calculate the encrypted password
        PasswordEncryptor passwordEncryptor = new StrongPasswordEncryptor();
        String encryptedPassword = passwordEncryptor.encryptPassword(employeePassword);
        String insertEmployeeQuery = "INSERT INTO employees (email, password, fullname) VALUES (?, ?, ?)";
        PreparedStatement insertEmployeeStatement = connection.prepareStatement(insertEmployeeQuery);
        insertEmployeeStatement.setString(1, employeeEmail);
        insertEmployeeStatement.setString(2, encryptedPassword);
        insertEmployeeStatement.setString(3, employeeFullName);
        int rowsAffected = insertEmployeeStatement.executeUpdate();
        System.out.println(rowsAffected);

        statement.close();
        insertEmployeeStatement.close();
        connection.close();

        System.out.println("finished");

    }

}
