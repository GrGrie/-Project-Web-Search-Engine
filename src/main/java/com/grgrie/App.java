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
import java.util.Timer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.swing.event.SwingPropertyChangeSupport;

/**
 * Hello world!
 *
 */
public class App 
{

    private static int numberOfThreads = 8;
    
    public static void main( String[] args )
    {
        //String startUrl = "https://www.cs.rptu.de/en/studium/bewerber/schulen";
        String startUrl = "https://www.heapsort.org/";
        String encoding = "ISO-8859-1";

        Queue<String> urlQueue = new LinkedList<>();
        urlQueue.add(startUrl);
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);


        System.out.println( "Starting the job" );
        
        DBhandler dbHandler = new DBhandler("dbis"); 
        //dbHandler.initDB("DBIS");

        // while (!urlQueue.isEmpty()) {
        //     String url = "emptyUrlFromMainApp";
        //     //DBhandler dbHandler = new DBhandler("dbis");
        //     try {
        //         url = urlQueue.poll();
        //         System.out.println("GIVING CRAWLER URL :: " + url);
        //         Future<?> future = executorService.submit(new Crawler(url, 1, dbHandler));   
        //         if(future.get() == null){  // If crawl is done, put new url to queue
        //             Iterator<Map.Entry<String, Integer>> iterator;
        //             iterator = dbHandler.getTopEightNotVisitedPages().entrySet().iterator();
        //             //System.out.println("Getting top html page");
        //             while(iterator.hasNext() && urlQueue.size() < numberOfThreads){
        //                 Map.Entry<String, Integer> map = iterator.next();
        //                 url = map.getKey();
        //                 if(url != "-1" && !urlQueue.contains(url)) urlQueue.add(url); 
        //             }
                     
        //         }
        //     }   catch (InterruptedException | ExecutionException | SQLException e) {
        //         e.printStackTrace();
        //     }
        //     System.out.println("\nURL QUEUE ::");
        //     Indexer.printList(urlQueue);
        //     System.out.println();
        // }

        // // Finished crawling, starting to work with the result:
        
        // // Updating tfidf
        // try {
        //     int totalNumberOfDocuments = dbHandler.getTotalNumberOfDocuments();
        //     List<String> terms = dbHandler.getTerms();
        //     for (String term : terms) {
        //         dbHandler.updateTFIDF(term, totalNumberOfDocuments);
        //         System.out.println("Updated TFIDF value of " + term);
        //     }
        // } catch (SQLException e) {
        //     e.printStackTrace();
        // }

        /* 
            Implementing Google - like search
        */

        // Creating Command Line Interface
        List<String> searchStringList = new ArrayList<>();
        String inputCLIString = readCLIinput();
        String[] searchStringArray = inputCLIString.split(" ");
        for (String string : searchStringArray) {
            searchStringList.add(string);
        }

        // Parsing command line interface
        int numberOfTopResults = Integer.parseInt(searchStringList.remove(0));
        boolean isConjunctive;
        String conjunctiveString = searchStringList.remove(0);
        if(conjunctiveString.equalsIgnoreCase("AND")) isConjunctive = true;
        else isConjunctive = false;
        
        System.out.println("searchStringList size is " + searchStringList.size());
        try {
            GoogleQuery googleQuery = new GoogleQuery(searchStringList, isConjunctive, numberOfTopResults);
            googleQuery.setDBhandler(dbHandler);
            List <String> findGoogle = googleQuery.executeGoogleQuery();
            Indexer.printList(findGoogle);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        

        
        
        executorService.shutdown();
        
    }

    static String readCLIinput(){
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter your query: ");
        String query = scanner.nextLine();
        scanner.close();
        return query;
    }
    
}


