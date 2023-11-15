package com.grgrie;

import java.sql.*;
import java.time.*;

public class  DBhandler {
    private String defaultURL = "jdbc:postgresql://localhost/";
    private final String user = "postgres";
    private final String password = "postgres";
    private Connection connectionDBhandler = null;
    private Statement statementDBhandler = null;


    protected void initDB(String DBname){
            try{
                Connection connection = DriverManager.getConnection(defaultURL, user, password);
                if(connection != null) System.out.println("Connected successfully to PostgreSQL server");
                else System.out.println("Failed to connect to PostgreSQL server");

                ResultSet resultSet = connection.getMetaData().getCatalogs();
                while(resultSet.next()){
                    String catalogs = resultSet.getString(1);
                    if(!DBname.toLowerCase().equals(catalogs)){
                        Statement statement = connection.createStatement();
                        String sql1 = "CREATE DATABASE " + DBname;
                        statement.executeUpdate(sql1);
                        System.out.println("Database successfully created!");
                        defaultURL += DBname.toLowerCase();
                    }
                }

                connectionDBhandler = connect();
                statementDBhandler = connectionDBhandler.createStatement();
                createTables();
            } catch (SQLException e) {
                e.printStackTrace();
            } 
    }

    protected void connectTo(String DBname){
        verifyIntegrity(DBname);
        if(connectionDBhandler != null) System.out.println("Connected successfully to database " + DBname + " via address :: \t" + defaultURL);
        else System.out.println("Failed to connect to database " + DBname);
    }

    private void createTables(){
        String createTableFeatures = "CREATE TABLE features ("
                    + "docid SERIAL,"
                    + "term VARCHAR(20),"
                    + "term_frequency numeric(10,3))";
        String createTableDocuments = "CREATE TABLE documents ("
                    + "docid SERIAL,"
                    + "url VARCHAR(100),"
                    + "crawled_on_date DATE"
                    + ");";
        String createTableLinks = "CREATE TABLE links ("
                    + "from_docid int,"
                    + "to_docid int"
                    + ");";
        try {
            statementDBhandler.executeUpdate(createTableFeatures);
            statementDBhandler.executeUpdate(createTableDocuments);
            statementDBhandler.executeUpdate(createTableLinks);
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private void verifyIntegrity(String DBname){
        defaultURL = defaultURL + DBname.toLowerCase();
        try {
            Connection conn = connect();
            connectionDBhandler = conn;
            ResultSet tmpResultSet = conn.getMetaData().getCatalogs();
            tmpResultSet.next();
                if(tmpResultSet.getString(1).equals(DBname.toLowerCase())){
                statementDBhandler = connectionDBhandler.createStatement();
                }
        } catch (SQLException e) {
            e.printStackTrace();
        }     
    }

    public Connection connect() throws SQLException {
        return DriverManager.getConnection(defaultURL, user, password);
    }

    public int insertInDocumentsTable(String link, boolean isCrawled){
        String SQL = "INSERT INTO documents(url, crawled_on_date) "
                    + "VALUES (?, ?)";
        int id = 0;
        
        try {
            PreparedStatement preparedStatement = connectionDBhandler.prepareStatement(SQL);

            preparedStatement.setString(1, link);
            if(isCrawled) preparedStatement.setObject(2, LocalDateTime.now());
            else preparedStatement.setObject(2, null);

            int affectedRows = preparedStatement.executeUpdate();
            // check the affected rows 
            if (affectedRows > 0) {
                // get the ID back
                try (ResultSet rs = preparedStatement.getGeneratedKeys()) {
                    if (rs.next()) {
                        id = rs.getInt(1);
                    }
                } catch (SQLException ex) {
                    System.out.println(ex.getMessage());
                }
            }
        } catch (SQLException e) {
            e.getMessage();
        }

        return id;
    }

    // Updates crawled date in documents table
    public int updateCrawledDate(String link){
        String SQL = "UPDATE documents "
                + "SET crawled_on_date = ? "
                + "WHERE url = ?";

        int affectedrows = 0;

        try (
                PreparedStatement preparedStatement = connectionDBhandler.prepareStatement(SQL)) {

            preparedStatement.setObject(1, LocalDateTime.now());
            preparedStatement.setString(2, link);

            affectedrows = preparedStatement.executeUpdate();

        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        return affectedrows;
    }

    private void deleteAllFromDocuments(){
        String SQL = "DELETE FROM documents";

        try {
            PreparedStatement preparedStatement = connectionDBhandler.prepareStatement(SQL);
            preparedStatement.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
