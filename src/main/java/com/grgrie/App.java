package com.grgrie;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
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
        String startUrl = "https://www.cs.rptu.de/en/studium/bewerber/schulen";
        //String startUrl = "https://www.heapsort.org/";
        String encoding = "ISO-8859-1";

        Queue<String> urlQueue = new LinkedList<>();
        urlQueue.add(startUrl);
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);


        System.out.println( "Starting the job" );
        
        DBhandler dbHandler = new DBhandler("dbis"); 
        // dbHandler.initDB("DBIS");
        // dbHandler.connectTo("DBIS");

        while (!urlQueue.isEmpty()) {
            String url = "emptyUrlFromMainApp";
            //TODO
            //DBhandler dbHandler = new DBhandler("dbis");
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
                        if(url != "-1") urlQueue.add(url); 
                    }
                     
                }
            }   catch (InterruptedException | ExecutionException | SQLException e) {
                e.printStackTrace();
            }
            System.out.println("URL QUEUE :: \n");
            Indexer.printList(urlQueue);
            System.out.println();
        } 
        executorService.shutdown();       
    }

    private static Boolean isValidUrl(String url){
        Boolean isValidUrl = true;

        if(url.endsWith(".pdf") || url.endsWith(".txt") || url.endsWith(".doc") || url.endsWith(".docs"))
            isValidUrl = false;

        return isValidUrl;
    }
}
