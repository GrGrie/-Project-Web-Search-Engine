package com.grgrie;

import java.sql.*;
import java.time.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.naming.spi.DirStateFactory.Result;
//TODO: Reading database credentials as a separate file
import javax.swing.event.SwingPropertyChangeSupport;

import org.postgresql.jdbc2.ArrayAssistantRegistry;

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
            if(dbIsFoundString == "")
                dbIsFound = true;
        } catch (SQLException e) {
            System.out.println("|*| Error checking database existance! |*|");
            e.printStackTrace();
        }

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
                    + "term_frequency INT, "
                    + "tfidf numeric(13,10)"
                    + ")";
        String createTableDocuments = "CREATE TABLE documents ("
                    + "docid SERIAL,"
                    + "url VARCHAR(500),"
                    + "crawled_on_date DATE,"
                    + "depth int NOT NULL,"
                    + "UNIQUE (url), "
                    + "crawling_status VARCHAR(20), "
                    + "pageRank numeric(13,10) "
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

    protected void emptyDatabase(String dbName){
        if(dbName.equalsIgnoreCase("dbis")){
            String deleteFeatures = "DELETE FROM features";
            String deleteDocuments = "DELETE FROM documents";
            String deleteLinks = "DELETE FROM links";

        try {
            statementDBhandler.executeUpdate(deleteFeatures);
            statementDBhandler.executeUpdate(deleteDocuments);
            statementDBhandler.executeUpdate(deleteLinks);
            System.out.println("|*| Successfully deleted tables from database|*|");
        } catch (SQLException e) {
            System.out.println("|*| Error deleting tables from database |*|");
            e.printStackTrace();
        }
        }
    }

    public int getCrawledDepth(String link) throws SQLException{
        int result = 0;
        String SQL = "SELECT documents.depth FROM documents WHERE documents.url = ?";

        try (Connection connection = DriverManager.getConnection(dbUrl, user, password)) {
            connection.setAutoCommit(false);
            try(PreparedStatement statement = connection.prepareStatement(SQL);){
                statement.setString(1, link);
                ResultSet rs = statement.executeQuery();
                if(rs.next());
                    result = rs.getInt(1);
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
                + "SET crawled_on_date = ? , depth = ? , crawling_status = 'FINISHED'"
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

    public synchronized void storeLinks(List<String> links, int depth, int linkId) throws SQLException {
        try (Connection connection = DriverManager.getConnection(dbUrl, user, password)) {
            connection.setAutoCommit(false);
        try {
           // Perform the database operations            
            for (String link : links) {
                if(!link.endsWith("/"))
                    link += "/";
                int currentLinkID = getLinkId(link);
                if(currentLinkID == -1){
                    PreparedStatement statement = connection.prepareStatement("INSERT INTO documents (url, depth) VALUES (?, ?) ON CONFLICT DO NOTHING", Statement.RETURN_GENERATED_KEYS); //ON CONFLICT (url) DO UPDATE SET depth = LEAST (documents.depth, EXCLUDED.depth)
                    statement.setString(1, link);
                    statement.setInt(2, depth);
                    statement.executeUpdate();
                    ResultSet resultSet = statement.getGeneratedKeys();
                    if(resultSet.next()){
                        int id = resultSet.getInt(1);
                        connectLinks(linkId, id);   
                    }    
                } else {
                    connectLinks(linkId, currentLinkID);
                }
                
           }

           // Commit the transaction
           connection.commit();
           connection.close();
        } catch (SQLException e) {
           // Roll back the transaction
           System.out.println("|*| DBhandler.storeLinks ERROR |*|");
           connection.rollback();
           connection.close();
           throw e;
        }
        }
    }

    public synchronized void storeWords(Map<String, Integer> wordsMap, String url) throws SQLException{
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
            //double term_frequency = Math.log(1 + ((double) wordsMap.get(word)/ (double) sumOfWords));
            double term_frequency = wordsMap.get(word);
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

    public synchronized void connectLinks(int from_docid, int to_docid) throws SQLException{
        try (Connection connection = DriverManager.getConnection(dbUrl, user, password)) {
            connection.setAutoCommit(false);
        try {
           // Perform the database operations
            PreparedStatement statement = connection.prepareStatement("INSERT INTO links VALUES (?, ?) ON CONFLICT DO NOTHING");
            statement.setInt(1, from_docid);
            statement.setInt(2, to_docid);
            statement.executeUpdate();
           // Commit the transaction
           connection.commit();
           connection.close();
        } catch (SQLException e) {
           // Roll back the transaction
           System.out.println("DBHANDLER connectLinks ERROR!!!");
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
                if(resultSet.next())
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

    public Map<String,Integer> getTopNotVisitedPages(int numberOfPagesToGet, boolean isAllowedToLeaveDomain, String url){
        Map<String,Integer> result = new HashMap<>();
        String SQL;
        if(isAllowedToLeaveDomain) SQL = "SELECT documents.url, documents.depth FROM documents WHERE crawled_on_date IS NULL AND crawling_status IS NULL AND url NOT LIKE '%.pdf' AND url NOT LIKE '%.txt' ORDER BY docid LIMIT " + numberOfPagesToGet;
        else SQL = "SELECT documents.url, documents.depth FROM documents WHERE crawled_on_date IS NULL AND crawling_status IS NULL AND url NOT LIKE '%.pdf' AND url NOT LIKE '%.txt' AND url LIKE '" + Indexer.getDomainName(url) + "%' ORDER BY docid LIMIT " + numberOfPagesToGet;
        try (Connection connection = DriverManager.getConnection(dbUrl, user, password)) {
            connection.setAutoCommit(false);
            try(Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(SQL)){
                while(resultSet.next())
                    result.put(resultSet.getString(1), resultSet.getInt(2));
                
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
        } catch (SQLException e1) {
            e1.printStackTrace();
        }
        return result;
    }

    public List<String> getTopNonFinishedCrawlingPages(int numberOfPagesToGet){
        List<String> resultList = new ArrayList<>();

        String sqlQuery = "SELECT url FROM documents WHERE crawled_on_date IS NULL AND crawling_status = 'crawling' AND url NOT LIKE '%.pdf' AND url NOT LIKE '%.txt' ORDER BY docid LIMIT " + numberOfPagesToGet;

        try {
            Statement statement = connectionDBhandler.createStatement();
            ResultSet rs = statement.executeQuery(sqlQuery);
            while(rs.next()){
                resultList.add(rs.getString(1));
            }
            return resultList;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return resultList;
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

    public void updateTFIDF(){
        String SQL = "UPDATE features " 
                    +"SET tfidf = compute.tf_idf FROM "
                    +"(SELECT t1.id, t1.docid, t1.term, t1.tf * t2.idf as tf_idf FROM "
                    +   "(SELECT id, docid, term, 1 + LOG(features.term_frequency) as tf FROM features GROUP BY docid, id, term, features.term_frequency) t1 "
                    +   "JOIN "
                    +   "(SELECT term, LOG(1 + N.numberOfAllDocs/1 + COUNT(docid)::float) as idf FROM features, (SELECT COUNT(DISTINCT docid) as numberOfAllDocs FROM features) N GROUP BY term, N.numberOfAllDocs ) t2 "
                    +"ON t1.term = t2.term ORDER BY tf_idf DESC) compute WHERE features.id = compute.id";
        try {
            statementDBhandler.executeUpdate(SQL);
        } catch (SQLException e) {
            e.printStackTrace();
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

    protected int getNumberOfTermOccurances(String term) throws SQLException{
        String getNumberOfTermOccurances = "SELECT COUNT (*) FROM features WHERE term = '" + term + "'";
        try(Connection connection = DriverManager.getConnection(dbUrl, user, password)){
            connection.setAutoCommit(false);
        try (
            PreparedStatement preparedStatement1 = connection.prepareStatement(getNumberOfTermOccurances)) {

            ResultSet rs = preparedStatement1.executeQuery();
            rs.next();
            int totalNumberOfDocuments = rs.getInt(1);
            connection.commit();
            connection.close();
            return totalNumberOfDocuments;
        } catch (SQLException ex) {
            System.out.println("|*| Error in DBhandler.getNumberOfTermOccurances(). Returning -1 |*|");
            connection.rollback();
            connection.close();
            System.out.println(ex.getMessage());
        }
        }
        return -1;
    }

    protected int getTotalNumberOfTerms() throws SQLException{
        String getTotalNumberOfTerms = "SELECT SUM(COUNT(term)) OVER() counts FROM features";
        try(Connection connection = DriverManager.getConnection(dbUrl, user, password)){
            connection.setAutoCommit(false);
        try (
            PreparedStatement preparedStatement1 = connection.prepareStatement(getTotalNumberOfTerms)) {

            ResultSet rs = preparedStatement1.executeQuery();
            rs.next();
            int totalNumberOfDocuments = rs.getInt(1);
            connection.commit();
            connection.close();
            return totalNumberOfDocuments;
        } catch (SQLException ex) {
            System.out.println("|*| Error in DBhandler.getTotalNumberOfTerms(). Returning -1 |*|");
            connection.rollback();
            connection.close();
            System.out.println(ex.getMessage());
        }
        }
        return -1;
    }

    protected List<String> getCrawledURLs(){
        List<String> result = new ArrayList<>();

        String sqlQuery = "SELECT url FROM documents WHERE crawled_on_date IS NOT NULL";

        try {
            ResultSet rs = statementDBhandler.executeQuery(sqlQuery);
            while(rs.next()){
                result.add(rs.getString(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    protected int getNumberOfVisitedURLs(){
        int result = 0;

        String SQL = "SELECT COUNT (*) FROM documents WHERE crawling_status = 'FINISHED'";

        try {
            ResultSet rs = statementDBhandler.executeQuery(SQL);
            rs.next();
            result = rs.getInt(1);
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    protected int getNumberOfOutgoingLinks(int linkID){
        int result = 0;

        String SQL = "SELECT COUNT(*) FROM links WHERE from_docid = " + linkID;

        try {
            ResultSet rs = statementDBhandler.executeQuery(SQL);
            rs.next();
            result = rs.getInt(1);
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    
        return result;
    }

    protected List<Integer> getAllLinksID(){
        List<Integer> result = new ArrayList<>();

        String SQL = "SELECT DISTINCT to_docid FROM links ORDER BY to_docid";
        try {
            ResultSet rs = statementDBhandler.executeQuery(SQL);
            while(rs.next()){
                //TODO: Finish implementing getting all links and storing them in LIst
                result.add(rs.getInt(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    protected void updatePagesRank(double[] pageRanksArray, Map<Integer, Integer> linkIDs){
        String SQL = "UPDATE documents "
                    +"SET pageRank = ? WHERE docid = ?";
        try {
            for(int i = 0; i < linkIDs.size(); i++){
                PreparedStatement preparedStatement = connectionDBhandler.prepareStatement(SQL);
                preparedStatement.setObject(1, pageRanksArray[i]);
                preparedStatement.setInt(2, linkIDs.get(i));
                //System.out.println(preparedStatement.toString());
                preparedStatement.executeUpdate();
                preparedStatement.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    protected Map<Integer, Integer> linksToIndexes(){
        Map<Integer, Integer> answer = new HashMap<>();

        String SQL = "SELECT * FROM links ORDER BY from_docid, to_docid";
        try {
            ResultSet rs = statementDBhandler.executeQuery(SQL);
            int index = 0, first, second;
            while(rs.next()){
                first = rs.getInt(1);
                second = rs.getInt(2);
                //System.out.println("First is " + first + " , second is " + second + " , and index is " + index);
                if(!answer.containsKey(first)){
                    answer.put(first, index);
                    index++;
                } else if(!answer.containsKey(second)){
                    answer.put(second, index);
                    index++;
                }
            }
        return answer;
        } catch (SQLException e) {
            System.out.println("|*| Error in DBhandler.linksToIndexes() |*|");
            e.printStackTrace();
        }

        return answer;
    }

    /**
     * Return IDs of all links that can be visited from this one. Namely, in DB it is SELECT WHERE from_id = 
     * {@code linkID}
     * @param linkID
     * @return List of integers of all IDs
    */
    protected List<Integer> getOutgoingLinks(int linkID){
        List<Integer> outgoingLinks = new ArrayList<>();
        String SQL = "SELECT to_docid FROM links WHERE from_docid = " + linkID + " ORDER BY to_docid";

        try {
            ResultSet rs = statementDBhandler.executeQuery(SQL);
            while(rs.next()){
                outgoingLinks.add(rs.getInt(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return outgoingLinks;
    }

    // Changes temporarily depth to -1, to ensure a lock
    protected void lock(String url){
        String SQL = "UPDATE documents "
                + "SET crawling_status = 'crawling' "
                + "WHERE url = ?";
        
        try{
            
            PreparedStatement preparedStatement = connectionDBhandler.prepareStatement(SQL);
            preparedStatement.setString(1, url);
            preparedStatement.executeUpdate();
            preparedStatement.close();
            connectionDBhandler.commit();
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
    }

}
