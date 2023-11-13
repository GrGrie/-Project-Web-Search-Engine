package com.grgrie;

import java.sql.*;


public class DBhandler {
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
                    }
                }

                connectionDBhandler = DriverManager.getConnection(defaultURL + DBname.toLowerCase(), user, password);
                statementDBhandler = connectionDBhandler.createStatement();
                createTables();
            } catch (SQLException e) {
                e.printStackTrace();
            } 
    }

    protected void connectTo(String DBname){
        verifyIntegrity(DBname);
        if(connectionDBhandler != null) System.out.println("Connected successfully to database " + DBname);
        else System.out.println("Failed to connect to database " + DBname);

    }

    private void createTables(){
        String sqlQuery2 = "CREATE TABLE features ("
                    + "docid INTEGER,"
                    + "term VARCHAR(20),"
                    + "term_frequency numeric(10,3))";
        String sqlQuery3 = "CREATE TABLE documents ("
                    + "docid int,"
                    + "url varchar(200),"
                    + "crawled_on_date varchar(10)"
                    + ");";
        String sqlQuery4 = "CREATE TABLE links ("
                    + "from_docid int,"
                    + "to_docid int"
                    + ");";
        try {
            statementDBhandler.executeUpdate(sqlQuery2);
            statementDBhandler.executeUpdate(sqlQuery3);
            statementDBhandler.executeUpdate(sqlQuery4);
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private void verifyIntegrity(String DBname){
        defaultURL = defaultURL + DBname.toLowerCase();
        try {
            Connection conn = DriverManager.getConnection(defaultURL, user, password);
            ResultSet tmpResultSet = conn.getMetaData().getCatalogs();
            tmpResultSet.next();
                if(tmpResultSet.getString(1).equals(DBname.toLowerCase())){
                connectionDBhandler = conn;
                statementDBhandler = connectionDBhandler.createStatement();
                }
        } catch (SQLException e) {
            e.printStackTrace();
        }      
    }



}
