package com.grgrie;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Hello world!
 *
 */
public class App 
{
    private ExecutorService executorService;
    private DBhandler dbHandler;

    //private String defaultStartCrawlUrl = "https://www.heapsort.org/";
    //private String startUrl = "https://www.cs.rptu.de/en/studium/bewerber/schulen";
    private static int numberOfThreads = 8;
    
    public void main()
    {
        init();
        //crawl();
        // Finished crawling, starting to work with the result:
        
        // Updating tfidf
        //updateTFIDF();

        /* 
            Implementing Google - like search
        */

        // Creating Command Line Interface
        // String inputCLIString = readCLIinput();
        

        // Parsing command line interface
        
        
    }

    protected void init(){
        System.out.println( "Initializing DB" );

        dbHandler = new DBhandler();
        String databaseName = "dbis";
        if(!dbHandler.databaseExists(databaseName)){
            dbHandler.createDatabase(databaseName);
            dbHandler = new DBhandler(databaseName);
        } else {
            dbHandler = new DBhandler(databaseName);
        }
    }

    protected String readCLIinput(){
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter your query: ");
        String query = scanner.nextLine();
        scanner.close();
        return query;
    }

    protected void crawl(String startUrl){
        init();

        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        this.executorService = executorService;
        
        Queue<String> urlQueue = new LinkedList<>();
        urlQueue.add(startUrl);

        while (!urlQueue.isEmpty()) {
            String url = "emptyUrlFromMainApp";
            try {
                url = urlQueue.poll();
                System.out.println("GIVING CRAWLER URL :: " + url);
                Future<?> future = executorService.submit(new Crawler(url, 1, dbHandler));   
                if(future.get() == null){  // If crawl is done, put new url to queue
                    Iterator<Map.Entry<String, Integer>> iterator;
                    iterator = dbHandler.getTopEightNotVisitedPages().entrySet().iterator();
                    //System.out.println("Getting top html page");
                    while(iterator.hasNext() && urlQueue.size() < numberOfThreads){
                        Map.Entry<String, Integer> map = iterator.next();
                        url = map.getKey();
                        if(url != "-1" && !urlQueue.contains(url)) urlQueue.add(url); 
                    }
                     
                }
            }   catch (InterruptedException | ExecutionException | SQLException e) {
                e.printStackTrace();
            }
            System.out.println("\nURL QUEUE ::");
            Indexer.printList(urlQueue);
            System.out.println();
        }

    executorService.shutdown();
    }

    protected void updateTFIDF(){
        try {
            int totalNumberOfDocuments = dbHandler.getTotalNumberOfDocuments();
            List<String> terms = dbHandler.getTerms();
            for (String term : terms) {
                dbHandler.updateTFIDF(term, totalNumberOfDocuments);
                System.out.println("Updated TFIDF value of " + term);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    protected List<String> googleSearch(String query) throws SQLException{
        init();
        
        List<String> findGoogle;

        // Check if allowed to leave domain
        //TODO : implement DBhandler method, that returns sites only from this domain
        boolean isAllowedToLeaveDomain = true;
        String domain = "";
        if(query.contains("site:")){
            isAllowedToLeaveDomain = false;
            int startIndexOfSite = query.indexOf("site:");
            int endIndexOfSite = query.indexOf(" ", startIndexOfSite+1);
            domain = query.substring(startIndexOfSite, endIndexOfSite);
            query.replaceFirst(domain + " ", "");
        }

        List<String> searchStringList = queryToList(query);

        int numberOfTopResults = Integer.parseInt(searchStringList.remove(0));
        boolean isConjunctive;
        String conjunctiveString = searchStringList.remove(0);
        if(conjunctiveString.equalsIgnoreCase("AND") || conjunctiveString.equalsIgnoreCase("conjunctive")) isConjunctive = true;
        else isConjunctive = false;    
    
        System.out.println("searchStringList size is " + searchStringList.size());
        GoogleQuery googleQuery = new GoogleQuery(searchStringList, isConjunctive, numberOfTopResults);
        googleQuery.setDBhandler(dbHandler);
        findGoogle = googleQuery.executeGoogleQuery();
        return findGoogle;

    }

    protected List<String> queryToList(String query){
        List<String> searchStringList = new ArrayList<>();
        String[] searchStringArray = query.split(" ");
        for (String string : searchStringArray) {
            searchStringList.add(string);
        }
        return searchStringList;
    }

    protected String success(){
        return "Successfully finished given task!";
    }
}


