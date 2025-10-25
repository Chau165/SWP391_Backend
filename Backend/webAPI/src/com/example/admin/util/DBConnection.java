package com.example.admin.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    // Change these to match your SQL Server setup
    private static final String URL = "jdbc:sqlserver://localhost:1433;databaseName=YourDatabase;encrypt=false;";
    private static final String USER = "sa";
    private static final String PASS = "your_password";

    static {
        try {
            // MSSQL JDBC Driver
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}
