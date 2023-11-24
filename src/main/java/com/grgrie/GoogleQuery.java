package com.grgrie;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GoogleQuery {
    
    private List<String> inputStringsList;
    private boolean isConjunctive;
    private int numberOfTopResults;
    private DBhandler dbHandler;

    
    public List<String> getInputStringsList() {
        return this.inputStringsList;
    }

    public void setDBhandler(DBhandler dbHandler){
        this.dbHandler = dbHandler;
    }

    public boolean isConjunctive() {
        return isConjunctive;
    }

    public int getNumberOfTopResults() {
        return numberOfTopResults;
    }

    public GoogleQuery(List<String> inputStringsList, boolean isConjunctive, int numberOfTopResults){
        this.inputStringsList = inputStringsList;
        this. isConjunctive = isConjunctive;
        this.numberOfTopResults = numberOfTopResults;
    }

    public List<String> executeGoogleQuery() throws SQLException{
        List<String> resultLinks = new ArrayList<>();
        String sqlQuerry;
        if(isConjunctive){ // Implementing conjunctive (AND) query, meaning all words from query should appear in the result
            sqlQuerry = "SELECT url FROM ( " + 
                        "SELECT docid, SUM(tfidf) tfidf FROM features WHERE features.term IN (?";
            // for each word add an argument to sql querry
            for(int i = 0; i < inputStringsList.size() - 1; i++)
                sqlQuerry += ", ?";
        
            sqlQuerry += ") GROUP BY docid HAVING COUNT(DISTINCT term) = " + inputStringsList.size() +" ORDER BY tfidf DESC LIMIT " + numberOfTopResults + 
                         ") as words JOIN documents ON documents.docid = words.docid ORDER BY words.tfidf DESC";
        } else {         // Implementing disjunctive (OR) query, meaning any words from query should appear in the result
            sqlQuerry = "SELECT url FROM ( " + 
                        "SELECT docid, SUM(tfidf) tfidf FROM features WHERE features.term IN (?";
            // for each word add an argument to sql querry
            for(int i = 0; i < inputStringsList.size() - 1; i++)
                sqlQuerry += ", ?";
        
            sqlQuerry += ") GROUP BY docid ORDER BY tfidf DESC LIMIT " + numberOfTopResults + 
                         ") as words JOIN documents ON documents.docid = words.docid ORDER BY words.tfidf DESC";
        }
// SELECT url, words.tfidf FROM (SELECT docid, SUM(tfidf) tfidf FROM features WHERE features.term IN ('calculu', 'averag') GROUP BY docid ORDER BY tfidf DESC) AS words JOIN documents ON documents.docid = words.docid ORDER BY tfidf DESC
        try(Connection connection = DriverManager.getConnection(dbHandler.getDatabaseUrl(), dbHandler.getUser(), dbHandler.getPassword())){
            connection.setAutoCommit(false);
        try (

            PreparedStatement preparedStatement1 = connection.prepareStatement(sqlQuerry)) {

            // Filling prepared statement with values
            for (int i = 0; i < inputStringsList.size(); i++) {
                preparedStatement1.setString(i + 1, inputStringsList.get(i).toLowerCase());
            }
            System.out.println(preparedStatement1.toString());
            
            ResultSet rs = preparedStatement1.executeQuery();
            while(rs.next()){
                String resultSQLLink = rs.getString(1);
                resultLinks.add(resultSQLLink);
            }
            connection.commit();
            connection.close();
        } catch (SQLException ex) {
            connection.rollback();
            connection.close();
            System.out.println("|*| Error in executeGoogleQuerry method |*|");
            System.out.println(ex.getMessage());
        }
        }


        return resultLinks;
    }

}
