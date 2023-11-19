package com.grgrie;

import java.sql.*;
import java.time.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class  DBhandler {
    private String defaultURL = "jdbc:postgresql://localhost/";
    private final String user = "postgres";
    private final String password = "postgres";
    private Connection connectionDBhandler = null;
    private Statement statementDBhandler = null;

    public DBhandler(){

    }

    public DBhandler(String DBname){
        connectTo(DBname);
    }


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
                    + "url VARCHAR(300),"
                    + "crawled_on_date DATE,"
                    + "depth int NOT NULL,"
                    + "UNIQUE (url) "
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
        Connection conn = DriverManager.getConnection(defaultURL, user, password);
        return conn;
    }


    public int getCrawledDepth(String link) throws SQLException{
        int result = 0;
        String SQL = "SELECT documents.depth FROM documents WHERE documents.url = ?";

        try (Connection connection = DriverManager.getConnection(defaultURL, user, password)) {
            connection.setAutoCommit(false);
            try(PreparedStatement statement = connection.prepareStatement(SQL);){
                statement.setString(1, link);
                statement.executeUpdate();
            // Commit the transaction
            connection.commit();
            return result;
            } catch (SQLException e) {
            // Roll back the transaction
            connection.rollback();
            throw e;
            }
        }  
    }

    public int insertInDocumentsTable(String link, boolean isCrawled, int depth) throws SQLException{
        String SQL = "INSERT INTO documents(url, crawled_on_date, depth) "
                    + "VALUES (?, ?, ?)";
        int id = 0;
        
        try {
            connectionDBhandler.setAutoCommit(false);
            PreparedStatement preparedStatement = connectionDBhandler.prepareStatement(SQL);

            preparedStatement.setString(1, link);
            if(isCrawled) preparedStatement.setObject(2, LocalDateTime.now());
            else preparedStatement.setObject(2, null);
            preparedStatement.setInt(3, depth);
            preparedStatement.addBatch();
            preparedStatement.executeBatch();
            connectionDBhandler.commit();
            
        } catch (SQLException e) {
            connectionDBhandler.rollback();
            throw e;
        }
        return id;
    }

    // Updates crawled date in documents table
    public int updateCrawledDate(String link, int depth) throws SQLException{
        String SQL = "UPDATE documents "
                + "SET crawled_on_date = ? , depth = ?"
                + " WHERE url = ?";

        int affectedrows = 0;
        try(Connection connection = DriverManager.getConnection(defaultURL, user, password)){
            connection.setAutoCommit(false);
        
        try (
            PreparedStatement preparedStatement = connection.prepareStatement(SQL)) {

            preparedStatement.setObject(1, LocalDateTime.now());
            preparedStatement.setInt(2, depth);
            preparedStatement.setString(3, link);

            affectedrows = preparedStatement.executeUpdate();
            connection.commit();
            connection.close();
        } catch (SQLException ex) {
            connection.rollback();
            connection.close();
            System.out.println(ex.getMessage());
        }
        }
        
        return affectedrows;
    }

    public void storeLinks(List<String> links, int depth) throws SQLException {
        try (Connection connection = DriverManager.getConnection(defaultURL, user, password)) {
            connection.setAutoCommit(false);
        try {
           // Perform the database operations
           for (String link : links) {
               PreparedStatement statement = connection.prepareStatement("INSERT INTO documents (url, depth) VALUES (?, ?) ON CONFLICT (url) DO NOTHING");
               statement.setString(1, link);
               statement.setInt(2, depth);
               statement.executeUpdate();
           }

           // Commit the transaction
           connection.commit();
        } catch (SQLException e) {
           // Roll back the transaction
           System.out.println("DBHANDLER storeLinks ERROR!!!");
           connection.rollback();
           throw e;
        }
        }
    }

    /**
     * Checks whether the {@code link} is already in our database.
     * Returns {@code -1} if it isn't the case, otherwise returns {@code id}
     * @param link to search for
     * @return {@code docid} of a tuple, containing link
     * @throws SQLException
     */
    public int idOfLink(String link) throws SQLException{
        int id = -1;
        if(nullCheck("documents")){
            String SQL = "SELECT documents.docid FROM documents WHERE crawled_on_date IS NULL ORDER BY docid LIMIT 1";

            try (Connection connection = DriverManager.getConnection(defaultURL, user, password)) {
                connection.setAutoCommit(false);
            try(Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(SQL)){
                resultSet.next();
                id = resultSet.getInt(1); 
            
        
                
                
            // Commit the transaction
            connection.commit();
            } catch (SQLException e) {
            // Roll back the transaction
            connection.rollback();
            throw e;
            }

            }   
        }
        

        return id;
    }

    public Map<String,Integer> getTopEightNotVisitedPages() throws SQLException{
        Map<String,Integer> result = new HashMap<>();
        String SQL = "SELECT documents.url, documents.depth FROM documents WHERE crawled_on_date IS NULL AND url NOT LIKE '%.pdf' AND url NOT LIKE '%.txt' ORDER BY docid LIMIT 8 FOR UPDATE";

        try (Connection connection = DriverManager.getConnection(defaultURL, user, password)) {
            connection.setAutoCommit(false);
            try(Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(SQL)){
                while(resultSet.next())
                    result.put(resultSet.getString(1), resultSet.getInt(2));
                // if(!resultSet.next()) 
                //     result.put("-1", -1);
                
            // Commit the transaction
            connection.commit();
            return result;
            } catch (SQLException e) {
            // Roll back the transaction
            connection.rollback();
            throw e;
            }
        }  
    }

    public Boolean nullCheck(String tableName) throws SQLException {

        Boolean isNull = true;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        String qry = "SELECT * FROM " + tableName + " LIMIT 1";
 
        try {
             stmt = (PreparedStatement) connectionDBhandler.prepareStatement(qry);
             rs =  stmt.executeQuery();
             int count = 0;
             while(rs.next()){
                count++;
             }
             if(count != 0){ // if not equal to 0 then the table contains smth
                isNull = false;
             }
        return isNull;
        } catch (SQLException ex) {
            throw ex;
        }
  }
}
