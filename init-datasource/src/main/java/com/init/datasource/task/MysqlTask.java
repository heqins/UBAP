package com.init.datasource.task;

import com.api.common.util.MyProperties;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

@Slf4j
public class MysqlTask {

    public static void main(String[] args) {
        Connection connection = null;
        Statement statement = null;

        try {
            // Load the JDBC driver
            String driverName = MyProperties.getStrValue("mysql.driver");
            String url = MyProperties.getStrValue("mysql.url");
            String username = MyProperties.getStrValue("mysql.username");
            String password = MyProperties.getStrValue("mysql.password");
            String filePath = MyProperties.getStrValue("mysql.sql-file");

            Class.forName(driverName);

            // Create a connection to the database
            connection = DriverManager.getConnection(url, username, password);

            // Read the SQL file
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            StringBuilder sqlContent = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sqlContent.append(line);
            }
            reader.close();

            // Split SQL commands
            String[] sqlCommands = sqlContent.toString().split(";");

            // Execute SQL commands
            statement = connection.createStatement();
            for (String sqlCommand : sqlCommands) {
                boolean execute = statement.execute(sqlCommand);
            }
            // Close the resources
            statement.close();
            connection.close();
        }catch (Exception e) {
            if (statement != null) {
                try {
                    statement.close();
                }catch (SQLException se) {
                    log.error("close statement error", se);
                }
            }

            if (connection != null) {
                try {
                    connection.close();
                }catch (SQLException se) {
                    log.error("close connection error", se);
                }
            }
        }
    }
}
