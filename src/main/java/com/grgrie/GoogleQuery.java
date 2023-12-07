package com.grgrie;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GoogleQuery {
    
    private List<String> inputStringsList;
    private List<String> inputANDstringList;
    private boolean isConjunctive;
    private int numberOfTopResults;

    private Stemmer stemmer;
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
        this.isConjunctive = isConjunctive;
        this.numberOfTopResults = numberOfTopResults;
        stemmer = new Stemmer();
    }

    public GoogleQuery(List<String> inputStringsList, boolean isConjunctive){
        this(inputStringsList, isConjunctive, 20);
        stemmer = new Stemmer();
    }

    public GoogleQuery(List<String> inputStringsList){
        this(inputStringsList, 20);
        stemmer = new Stemmer();
    }

    public GoogleQuery(List<String> inputStringsList, int numberOfTopResults){
        this.inputStringsList = inputStringsList;
        this.numberOfTopResults = numberOfTopResults;
        stemmer = new Stemmer();
    }

    public List<String> executeGoogleQuery() throws SQLException{
        List<String> resultLinks = new ArrayList<>();
        
        try(Connection connection = dbHandler.connect();){
        connection.setAutoCommit(false);
    
        String sqlQuerry = getQuotationMarkSQLstring(false);

        try{
            PreparedStatement preparedStatement1 = connection.prepareStatement(sqlQuerry);
            for (int i = 0; i < inputStringsList.size(); i++) {
                String termWithoutQuotes;
                String currentString = inputStringsList.get(i);
                if(currentString.contains("\""))
                    termWithoutQuotes = currentString.substring(1, currentString.length() - 1);
                else
                    termWithoutQuotes = currentString;
                preparedStatement1.setString(i + 1, stemWord(termWithoutQuotes)); 
            }
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

    public List<String> executeGoogleQuery(boolean includeTFIDF) throws SQLException{
        if(!includeTFIDF) return executeGoogleQuery();

        List<String> resultLinks = new ArrayList<>();
        
        try(Connection connection = dbHandler.connect();){
        connection.setAutoCommit(false);
    
        String sqlQuerry = getQuotationMarkSQLstring(true);

        try{
            PreparedStatement preparedStatement1 = connection.prepareStatement(sqlQuerry);
            for (int i = 0; i < inputStringsList.size(); i++) {
                String termWithoutQuotes;
                String currentString = inputStringsList.get(i);
                if(currentString.contains("\""))
                    termWithoutQuotes = currentString.substring(1, currentString.length() - 1);
                else
                    termWithoutQuotes = currentString;
                preparedStatement1.setString(i + 1, stemWord(termWithoutQuotes)); 
            }
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

    public List<String> executeDomainOnlyGoogleQuery(String domain) throws SQLException{
        List<String> resultLinks = new ArrayList<>();
        
        try(Connection connection = dbHandler.connect();){
        connection.setAutoCommit(false);
    
        String sqlQuerry = getQuotationMarkSQLstring(false);
         
        try{
            PreparedStatement preparedStatement1 = connection.prepareStatement(sqlQuerry);
            for (int i = 0; i < inputStringsList.size(); i++) {
                String termWithoutQuotes;
                String currentString = inputStringsList.get(i);
                if(currentString.contains("\""))
                    termWithoutQuotes = currentString.substring(1, currentString.length() - 1);
                else
                    termWithoutQuotes = currentString;
                preparedStatement1.setString(i + 1, stemWord(termWithoutQuotes));
            }  

            ResultSet rs = preparedStatement1.executeQuery();
            while(rs.next()){
                String resultSQLLink = rs.getString(1);
                if(resultSQLLink.contains(domain))
                    resultLinks.add(resultSQLLink);
            }
            connection.commit();
            connection.close();
        } catch (SQLException ex) {
            connection.rollback();
            connection.close();
            System.out.println("|*| Error in executeDomainOnlyGoogleQuery method |*|");
            System.out.println(ex.getMessage());
        }
        }


        return resultLinks;
    }
    
    public List<String> executeDomainOnlyGoogleQuery(String domain, boolean includeTFIDF) throws SQLException{
        if(!includeTFIDF) return executeDomainOnlyGoogleQuery(domain);
        List<String> resultLinks = new ArrayList<>();
        
        try(Connection connection = dbHandler.connect();){
        connection.setAutoCommit(false);
    
        String sqlQuerry = getQuotationMarkSQLstring(false);
         
        try{
            PreparedStatement preparedStatement1 = connection.prepareStatement(sqlQuerry);
            for (int i = 0; i < inputStringsList.size(); i++) {
                String termWithoutQuotes;
                String currentString = inputStringsList.get(i);
                if(currentString.contains("\""))
                    termWithoutQuotes = currentString.substring(1, currentString.length() - 1);
                else
                    termWithoutQuotes = currentString;
                preparedStatement1.setString(i + 1, stemWord(termWithoutQuotes));
            }  

            ResultSet rs = preparedStatement1.executeQuery();
            while(rs.next()){
                String resultSQLLink = rs.getString(1);
                if(resultSQLLink.contains(domain))
                    resultLinks.add(resultSQLLink);
            }
            connection.commit();
            connection.close();
        } catch (SQLException ex) {
            connection.rollback();
            connection.close();
            System.out.println("|*| Error in executeDomainOnlyGoogleQuery method |*|");
            System.out.println(ex.getMessage());
        }
        }


        return resultLinks;
    }

    private boolean containsAndTerms(){
        if(inputANDstringList != null) // If we call this method 2nd time, no need to add anything to the list
            return true;
        boolean containsAndTerms = false;
        inputANDstringList = new ArrayList<>();
        String termWithoutQuotes;
        for (String string : inputStringsList) {
            if(string.startsWith("\"") && string.endsWith("\"")){
                termWithoutQuotes = string.substring(1, string.length()-1);
                inputANDstringList.add(termWithoutQuotes);
                containsAndTerms = true;
            }
        }
        return containsAndTerms;
    }

    private String getQuotationMarkSQLstring(boolean includeTFIDF){
        String sqlQuerry;
        if(containsAndTerms()){ // Implementing conjunctive (AND) query, meaning all words in quotes ("") should appear in the result
            if(includeTFIDF){
                sqlQuerry = "SELECT (url,tfidf) FROM ( " + 
                        "SELECT docid, STRING_AGG(term::text, ',' ORDER BY term) as term, SUM(tfidf) tfidf FROM features WHERE features.term IN (?";
                // for each word add an argument to sql querry
                for(int i = 0; i < inputStringsList.size() - 1; i++)
                sqlQuerry += ", ?";
        
                sqlQuerry += ") GROUP BY docid ORDER BY tfidf DESC LIMIT 20"  + 
                         ") as words JOIN documents ON documents.docid = words.docid WHERE ";
                for (String string : inputANDstringList) {
                    sqlQuerry = sqlQuerry + "words.term LIKE '%" + string + "%'";
                    if(string != inputANDstringList.get(inputANDstringList.size() - 1)) // If not last, add AND clause
                        sqlQuerry += " AND ";
                }         
           
                sqlQuerry += " ORDER BY words.tfidf DESC";
            } else {
                sqlQuerry = "SELECT url FROM ( " + 
                        "SELECT docid, STRING_AGG(term::text, ',' ORDER BY term) as term, SUM(tfidf) tfidf FROM features WHERE features.term IN (?";
                // for each word add an argument to sql querry
                for(int i = 0; i < inputStringsList.size() - 1; i++)
                sqlQuerry += ", ?";
        
                sqlQuerry += ") GROUP BY docid ORDER BY tfidf DESC LIMIT 20"  + 
                         ") as words JOIN documents ON documents.docid = words.docid WHERE ";
                for (String string : inputANDstringList) {
                    sqlQuerry = sqlQuerry + "words.term LIKE '%" + string + "%'";
                    if(string != inputANDstringList.get(inputANDstringList.size() - 1)) // If not last, add AND clause
                        sqlQuerry += " AND ";
                }         
                sqlQuerry += " ORDER BY words.tfidf DESC";
            }
             
        } else {                // Implementing disjunctive (OR) query, meaning any words from query should appear in the result
            if(includeTFIDF){
                sqlQuerry = "SELECT (url, words.tfidf) FROM ( " + 
                        "SELECT docid, SUM(tfidf) tfidf FROM features WHERE features.term IN (?";
                // for each word add an argument to sql querry
                for(int i = 0; i < inputStringsList.size() - 1; i++)
                sqlQuerry += ", ?";
        
                sqlQuerry += ") GROUP BY docid ORDER BY tfidf DESC LIMIT 20" + 
                         ") as words JOIN documents ON documents.docid = words.docid ORDER BY words.tfidf DESC";
            } else {
                sqlQuerry = "SELECT url FROM ( " + 
                        "SELECT docid, SUM(tfidf) tfidf FROM features WHERE features.term IN (?";
                // for each word add an argument to sql querry
                for(int i = 0; i < inputStringsList.size() - 1; i++)
                sqlQuerry += ", ?";
        
                sqlQuerry += ") GROUP BY docid ORDER BY tfidf DESC LIMIT 20" + 
                         ") as words JOIN documents ON documents.docid = words.docid ORDER BY words.tfidf DESC";
            }     
            
        }
// SELECT url, words.tfidf FROM (SELECT docid, SUM(tfidf) tfidf FROM features WHERE features.term IN ('calculu', 'averag') GROUP BY docid ORDER BY tfidf DESC) AS words JOIN documents ON documents.docid = words.docid ORDER BY tfidf DESC
        System.out.println(sqlQuerry);
        return sqlQuerry;
    }

    private String stemWord(String word){
        stemmer.add(word.toLowerCase().toCharArray(), word.length());
        stemmer.stem();
        return stemmer.toString();
    }

}