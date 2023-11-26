package com.grgrie;

import java.sql.*;
import java.time.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class  DBhandler {
    private String defaultURL = "jdbc:postgresql://localhost/";
    private String dbUrl = "";
    private final String user = "postgres";
    private final String password = "postgres";
    private Connection connectionDBhandler = null;
    private Statement statementDBhandler = null;
    
    public DBhandler(){

    }

    public DBhandler(String DBname){
        connectTo(DBname);
    }

    protected boolean databaseExists(String dbName){
        String sqlQuery = "SELECT 'CREATE DATABASE "+dbName+"' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = '"+dbName+"')";
        String  dbIsFoundString = "";
        boolean dbIsFound = false;
        
        try {
            
            Connection conn = connect();
            Statement statement;
            statement = conn.createStatement(); 
            ResultSet rs = statement.executeQuery(sqlQuery);
            while(rs.next()){
                dbIsFoundString = rs.getString(1);
            }
            System.out.println("dbIsFoundString :: " + dbIsFoundString);
            if(dbIsFoundString == "")
                dbIsFound = true;
        } catch (SQLException e) {
            System.out.println("|*| Error checking database existance! |*|");
            e.printStackTrace();
        }


        System.out.println("in databaseExists return dbIsFound :: ");
        System.out.println(dbIsFound);
        return dbIsFound;
    }

    protected Connection connect() throws SQLException{
        Connection conn = null;
        if(dbUrl == "")
            conn = DriverManager.getConnection(defaultURL, user, password);
        else
            conn = DriverManager.getConnection(dbUrl, user, password);
        return conn;
    }

    private void setHandlers(String dbName){
        try {
            connectionDBhandler = connect();
            statementDBhandler = connectionDBhandler.createStatement();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    protected void connectTo(String dbName){
        dbUrl = defaultURL + dbName.toLowerCase();
        setHandlers(dbName);
        if(connectionDBhandler != null) System.out.println("Connected successfully to database " + dbName + " via address :: \t" + defaultURL);
        else System.out.println("Failed to connect to database " + dbName);
    }
    
    private void createTables(){
        String createTableFeatures = "CREATE TABLE features ("
                    + "id SERIAL, "
                    + "docid INT, "
                    + "term VARCHAR(50), "
                    + "term_frequency numeric(13,10), "
                    + "tfidf numeric(13,10)"
                    + ")";
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

    protected void createDatabase(String dbName){
        try{
            Connection connection = connect();
            Statement statement = connection.createStatement();
                
            // Creating DB and storing its' URL
            String sqlQuery = "CREATE DATABASE " + dbName.toLowerCase();
            statement.executeUpdate(sqlQuery);
            dbUrl = defaultURL + dbName.toLowerCase();

            setHandlers(dbName);
            createTables();
        } catch (SQLException e) {
            System.out.println("|*| Error creating database |*|");
            e.printStackTrace();
        }
        
    }

        

    // protected void initDB(String DBname){
    //         try{
    //             Connection connection = DriverManager.getConnection(defaultURL, user, password);
    //             if(connection != null) System.out.println("Connected successfully to PostgreSQL server");
    //             else System.out.println("Failed to connect to PostgreSQL server");
    //             ResultSet resultSet = connection.getMetaData().getCatalogs();
    //             while(resultSet.next()){
    //                 String catalogs = resultSet.getString(1);
    //                 if(!DBname.toLowerCase().equals(catalogs)){
    //                     Statement statement = connection.createStatement();
    //                     String sql1 = "CREATE DATABASE " + DBname;
    //                     statement.executeUpdate(sql1);
    //                     System.out.println("Database successfully created!");
    //                     defaultURL += DBname.toLowerCase();
    //                 }
    //             }
    //             //connectionDBhandler = connect();
    //             //statementDBhandler = connectionDBhandler.createStatement();
    //             createTables();
    //         } catch (SQLException e) {
    //             e.printStackTrace();
    //         } 
    // }

    

    // private void verifyIntegrity(String DBname){
    //     defaultURL = defaultURL + DBname.toLowerCase();
    //     try {
    //         Connection conn = connect();
    //         connectionDBhandler = conn;
    //         ResultSet tmpResultSet = conn.getMetaData().getCatalogs();
    //         tmpResultSet.next();
    //             if(tmpResultSet.getString(1).equals(DBname.toLowerCase())){
    //             statementDBhandler = connectionDBhandler.createStatement();
    //             }
    //     } catch (SQLException e) {
    //         e.printStackTrace();
    //     }     
    // }

    

    public int getCrawledDepth(String link) throws SQLException{
        int result = 0;
        String SQL = "SELECT documents.depth FROM documents WHERE documents.url = ?";

        try (Connection connection = DriverManager.getConnection(dbUrl, user, password)) {
            connection.setAutoCommit(false);
            try(PreparedStatement statement = connection.prepareStatement(SQL);){
                statement.setString(1, link);
                statement.executeUpdate();
            // Commit the transaction
            connection.commit();
            connection.close();
            return result;
            } catch (SQLException e) {
            // Roll back the transaction
            connection.rollback();
            connection.close();
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
        try(Connection connection = DriverManager.getConnection(dbUrl, user, password)){
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

    public void storeLinks(List<String> links, int depth, int linkId) throws SQLException {
        try (Connection connection = DriverManager.getConnection(dbUrl, user, password)) {
            connection.setAutoCommit(false);
        try {
           // Perform the database operations
           for (String link : links) {
                PreparedStatement statement = connection.prepareStatement("INSERT INTO documents (url, depth) VALUES (?, ?) ON CONFLICT (url) DO NOTHING", Statement.RETURN_GENERATED_KEYS);
                statement.setString(1, link);
                statement.setInt(2, depth);
                statement.executeUpdate();
                ResultSet resultSet = statement.getGeneratedKeys();
                if(resultSet.next()){
                    int id = resultSet.getInt(1);
                    connectLinks(linkId, id);
                }
           }

           // Commit the transaction
           connection.commit();
           connection.close();
        } catch (SQLException e) {
           // Roll back the transaction
           System.out.println("DBHANDLER storeLinks ERROR!!!");
           connection.rollback();
           connection.close();
           throw e;
        }
        }
    }

    public void storeWords(Map<String, Integer> wordsMap, String url) throws SQLException{
        try (Connection connection = DriverManager.getConnection(dbUrl, user, password)) {
            connection.setAutoCommit(false);
        try {
            int linkId = 0;
            // Perform the database operations
            // Count term_frequency
            int sumOfWords = 0;
            for (int frequency : wordsMap.values()) {
                sumOfWords += frequency;
            }
            for (String word : wordsMap.keySet()) {
            linkId = getLinkId(url);
            PreparedStatement statement = connection.prepareStatement("INSERT INTO features (docid, term, term_frequency) VALUES (?, ?, ?)");
            statement.setInt(1, linkId);
            statement.setString(2, word);
            double term_frequency = 1 + ((double) wordsMap.get(word)/ (double) sumOfWords);
            statement.setDouble(3, term_frequency);
            statement.executeUpdate();               
           }

           // Commit the transaction
           connection.commit();
           connection.close();
        } catch (SQLException e) {
           // Roll back the transaction
           System.out.println("DBHANDLER storeWords ERROR!!!");
           connection.rollback();
           connection.close();
           throw e;
        }
        }
    }

    public void connectLinks(int from_docid, int to_docid) throws SQLException{
        try (Connection connection = DriverManager.getConnection(dbUrl, user, password)) {
            connection.setAutoCommit(false);
        try {
           // Perform the database operations
            PreparedStatement statement = connection.prepareStatement("INSERT INTO links VALUES (?, ?)");
            statement.setInt(1, from_docid);
            statement.setInt(2, to_docid);
            statement.executeUpdate();
           // Commit the transaction
           connection.commit();
           connection.close();
        } catch (SQLException e) {
           // Roll back the transaction
           System.out.println("DBHANDLER storeLinks ERROR!!!");
           connection.rollback();
           connection.close();
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
    public int getLinkId(String link) throws SQLException{
        int id = -1;
        //if(!isNullTable("documents")){
            String SQL = "SELECT docid FROM documents WHERE url = '" + link + "'";

            try (Connection connection = DriverManager.getConnection(dbUrl, user, password)) {
                connection.setAutoCommit(false);
            try(Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(SQL)){
                resultSet.next();
                id = resultSet.getInt(1); 
            // Commit the transaction
            connection.commit();
            connection.close();
            return id;
            } catch (SQLException e) {
            // Roll back the transaction
            connection.rollback();
            connection.close();
            throw e;
            }
            }   
        //}
    }

    public Map<String,Integer> getTopEightNotVisitedPages() throws SQLException{
        Map<String,Integer> result = new HashMap<>();
        String SQL = "SELECT documents.url, documents.depth FROM documents WHERE crawled_on_date IS NULL AND url NOT LIKE '%.pdf' AND url NOT LIKE '%.txt' ORDER BY docid LIMIT 8 FOR UPDATE";

        try (Connection connection = DriverManager.getConnection(dbUrl, user, password)) {
            connection.setAutoCommit(false);
            try(Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(SQL)){
                while(resultSet.next())
                    result.put(resultSet.getString(1), resultSet.getInt(2));
                // if(!resultSet.next()) 
                //     result.put("-1", -1);
                
            // Commit the transaction
            connection.commit();
            connection.close();
            return result;
            } catch (SQLException e) {
            // Roll back the transaction
            connection.rollback();
            connection.close();
            throw e;
            }
        }  
    }

    public Boolean isNullTable(String tableName) throws SQLException {

        setHandlers("dbis");
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

    public void updateTFIDF(String term, int totalNumberOfDocuments) throws SQLException{
        String getTotalDocumentsCount = "SELECT COUNT(*) FROM features WHERE term = ?";
        String setTDIDF = "UPDATE features"
                        + " SET tfidf = ?"
                        + " WHERE term = ?";
        
        try(Connection connection = DriverManager.getConnection(dbUrl, user, password)){
            connection.setAutoCommit(false);
        try (
            PreparedStatement preparedStatement1 = connection.prepareStatement(getTotalDocumentsCount);
            PreparedStatement preparedStatement2 = connection.prepareStatement(setTDIDF)) {

            preparedStatement1.setString(1, term);
            ResultSet rs = preparedStatement1.executeQuery();
            rs.next();
            int containingDocumentsCount = rs.getInt(1);

            preparedStatement2.setDouble(1, Math.log(totalNumberOfDocuments/containingDocumentsCount));
            preparedStatement2.setString(2, term);
            preparedStatement2.executeUpdate();
        
            connection.commit();
            connection.close();
        } catch (SQLException ex) {
            connection.rollback();
            connection.close();
            System.out.println(ex.getMessage());
        }
        }
    }

    public List<String> getTerms() throws SQLException{
        List<String> result = new ArrayList<>();

        String getAllTerms = "SELECT term FROM features";

        try(Connection connection = DriverManager.getConnection(dbUrl, user, password)){
            connection.setAutoCommit(false);
        try (
            PreparedStatement preparedStatement1 = connection.prepareStatement(getAllTerms)) {

            ResultSet rs = preparedStatement1.executeQuery();
            while(rs.next()){
               String term = rs.getString(1);
               result.add(term);
            }
            connection.commit();
            connection.close();
            return result;
        } catch (SQLException ex) {
            connection.rollback();
            connection.close();
            System.out.println(ex.getMessage());
        }
        }
        return result;
    }

    public int getTotalNumberOfDocuments() throws SQLException{
        String getNumberOfDocuments = "SELECT COUNT (*) FROM documents";
        try(Connection connection = DriverManager.getConnection(dbUrl, user, password)){
            connection.setAutoCommit(false);
        try (
            PreparedStatement preparedStatement1 = connection.prepareStatement(getNumberOfDocuments)) {

            ResultSet rs = preparedStatement1.executeQuery();
            rs.next();
            int totalNumberOfDocuments = rs.getInt(1);
            connection.commit();
            connection.close();
            return totalNumberOfDocuments;
        } catch (SQLException ex) {
            connection.rollback();
            connection.close();
            System.out.println(ex.getMessage());
        }
        }
        return -1;
    }

}
