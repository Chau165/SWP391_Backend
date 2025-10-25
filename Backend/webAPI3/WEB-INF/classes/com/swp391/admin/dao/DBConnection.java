package com.swp391.admin.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

// Utility to obtain JDBC connection to SQL Server.
public class DBConnection {
    // Update these values to match your environment
    private static final String URL = "jdbc:sqlserver://localhost:1433;databaseName=BatterySwapDBVer2";
    private static final String USER = "sa"; // change to your DB user
    private static final String PASS = "your_password"; // change to your DB password

    static {
        try {
            // For Microsoft JDBC Driver
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}
