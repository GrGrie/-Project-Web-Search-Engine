package com.grgrie;

import java.net.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Crawler implements Runnable {

  private int maxDepth;
  private int currentDepth;
  private int maxDocumentsToCrawl;
  private final int numberOfCrawledDocuments;
  private boolean isAllowedToLeaveDomain;


  private URL currentUrl;
  private String encoding = "ISO-8859-1";
  private String startURL;

  private Indexer indexer;
  private DBhandler dbHandler;

  private LinkedList<String> visitedURLs = new LinkedList<String>();
  private LinkedList<String> queueURL = new LinkedList<String>();
  private List<String> foundURLs = new LinkedList<>();

  public  Crawler (String startUrl, int maxDepth, DBhandler dbHandler){
    this.maxDepth = maxDepth;
    this.dbHandler = dbHandler;
    this.startURL = startUrl;
    numberOfCrawledDocuments = 0;
    this.indexer = new Indexer(encoding, dbHandler);
  }

  public  Crawler (String startUrl, int maxDepth, DBhandler dbHandler, int maxDocumentsToCrawl){
    this.maxDepth = maxDepth;
    this.dbHandler = dbHandler;
    this.startURL = startUrl;
    this.numberOfCrawledDocuments = maxDocumentsToCrawl;
    this.indexer = new Indexer(encoding, dbHandler);
  }

  public  Crawler (String startURL, int maxDepth, boolean isAllowedToLeaveDomain, DBhandler dbHandler){
    this(startURL, maxDepth, dbHandler);
    this.isAllowedToLeaveDomain = isAllowedToLeaveDomain;
  }

  public  Crawler (String startURL, int maxDepth, int maxDocumentsToCrawl, DBhandler dbHandler){
    this(startURL, maxDepth, dbHandler);
    this.maxDocumentsToCrawl = maxDocumentsToCrawl;
  }

  public  Crawler (String startURL, int maxDepth, int maxDocumentsToCrawl, boolean isAllowedToLeaveDomain, DBhandler dbHandler){
    this(startURL, maxDepth, maxDocumentsToCrawl, dbHandler);
    this.isAllowedToLeaveDomain = isAllowedToLeaveDomain;
  }



  @Override
  public void run() {
    try {
      if(dbHandler.nullCheck("documents")){                   // If documents table is empty
        //System.out.println("Inside FIRST if in Crawler.run()");
        currentDepth = 0;
        storeLinkInDB(startURL, true, currentDepth);         // Then just store links in DB and call cycle for queue. 
      } else {                                                           // Else (if documents table isn't empty) get top link, crawl it and update crawled date
        //System.out.println("Inside SECOND if in Crawler.run()");
        // Iterator<Map.Entry<String, Integer>> iterator = dbHandler.getTopNotVisitedPage().entrySet().iterator();
        // Map.Entry<String, Integer> map = iterator.next();
        // startURL = map.getKey();
        // currentDepth = map.getValue();
        if(startURL != "-1") dbHandler.updateCrawledDate(startURL, currentDepth);   
        currentDepth++;        
      }
      if(startURL != "-1") {
        System.out.println("Going to index page in Crawler :: ");
        indexer.indexPage(new URL(startURL), startURL);      // After this operation indexer has all the links and the words
        System.out.println("Indexed page " + startURL + " successfully");
        if(currentDepth <= maxDepth){
          dbHandler.storeLinks(indexer.getLinks(), currentDepth); 
          System.out.println("Links stored in DB");           // Store found links in DB
        }
      }
        System.out.println("Crawler run method has finished!!!");
    } catch (MalformedURLException | SQLException e) {
      e.printStackTrace();
    }
    
  }

  public List<String> crawl(String startURL){
    boolean isVisited = true;
    //storeLinkInDB(startURL  , isVisited);
    queueURL.add(startURL);
    queueURL.add(null);
    int currentDepth = 0;
    int documentsCrawled = 0;
    while(currentDepth <= maxDepth || (maxDocumentsToCrawl != 0 && documentsCrawled <= maxDocumentsToCrawl)){            //BFS
      try{
        if(queueURL.peek() == null){
          currentDepth++;
          queueURL.removeFirst();
          System.out.println("Increased currentDepth to " + currentDepth);

        } else {
          String firstQueueUrl = queueURL.getFirst();
          currentUrl = new URL(firstQueueUrl);

          System.out.println("Currently on page :: " + firstQueueUrl);
          indexer.indexPage(currentUrl, firstQueueUrl);
          visitedURLs.add(firstQueueUrl);
          //updateCrawledDateInDB(firstQueueUrl);
          queueURL.poll();
          foundURLs = indexer.getLinks();
          for (String url : foundURLs) {
            if(!visitedURLs.contains(url)){
              //storeLinkInDB(url, false);
              queueURL.add(url);
              visitedURLs.add(url);
            }
          }
          documentsCrawled++;
          Thread.sleep(300);
          queueURL.offer(null); 
          //Indexer.printList(queueURL);
        }
      } catch(Exception e) {
        e.printStackTrace();
      }
     
    }
    System.out.println("Crawling finished successfully");
    return visitedURLs;
  }

  private Connection getConnection(DBhandler dbHandler) throws SQLException{
    return dbHandler.connect();
  }

  private void storeLinkInDB(String link, boolean isVisited, int depth){
      try {
        dbHandler.insertInDocumentsTable(link, isVisited, currentDepth);
      } catch (SQLException e) {
        e.printStackTrace();
      }
  }

}