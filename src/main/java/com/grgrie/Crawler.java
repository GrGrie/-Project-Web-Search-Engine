package com.grgrie;

import java.net.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class Crawler implements Callable {

  private int maxDepth;
  private int currentDepth;
  private int maxDocumentsToCrawl = Integer.MAX_VALUE;
  private static int numberOfCrawledDocuments = 0;
  private boolean isAllowedToLeaveDomain;

  private static List<String> visitedURLs = new ArrayList<>();
  private static List<String> foundURLs = new ArrayList<>();

  //private String encoding = "ISO-8859-1";
  private String encoding = "UTF-8";
  private String startURL;

  private Indexer indexer;
  private DBhandler dbHandler;

  public  Crawler (String startUrl, int maxDepth, DBhandler dbHandler){
    this.maxDepth = maxDepth;
    this.dbHandler = dbHandler;
    this.startURL = startUrl;    
    numberOfCrawledDocuments = 0;
    this.indexer = new Indexer(encoding, dbHandler);
  }

  public  Crawler (String startUrl, DBhandler dbHandler, int maxDocumentsToCrawl){
    this.dbHandler = dbHandler;
    this.startURL = startUrl;
    this.maxDocumentsToCrawl = maxDocumentsToCrawl;
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
    this.startURL = startURL;
    this.maxDepth = maxDepth;
    if(maxDocumentsToCrawl != 0) this.maxDocumentsToCrawl = maxDocumentsToCrawl;
    this.isAllowedToLeaveDomain = isAllowedToLeaveDomain;
    this.dbHandler = dbHandler;
    this.indexer = new Indexer(encoding, dbHandler);
  }

  @Override
  public String call() {
    try {
      if(dbHandler.isNullTable("documents")){                   // If documents table is empty
        currentDepth = 0;
        storeLinkInDB(startURL, true, currentDepth);         // Then just store links in DB and call cycle for queue.
        foundURLs.add(startURL);
      }

      currentDepth = dbHandler.getCrawledDepth(startURL);

      if(currentDepth <= maxDepth && numberOfCrawledDocuments < maxDocumentsToCrawl) {

        if(visitedURLs.isEmpty()){
          visitedURLs = dbHandler.getCrawledURLs();
          foundURLs = visitedURLs;
        }

        if(numberOfCrawledDocuments == 0)
          numberOfCrawledDocuments = getNumberOfCrawledDocuments();
        
        dbHandler.lock(startURL);
        indexer.indexPage(new URL(startURL), startURL);      // After this operation indexer object has all the links and the words
        System.out.println("Indexed page " + startURL + " successfully");
        int linkId = dbHandler.getLinkId(startURL);
        System.out.println("Storing links in DB with currendDepth = " + currentDepth);

        List<String> newURLs = keepNewURLs(indexer.getLinks());
        dbHandler.storeLinks(newURLs, currentDepth + 1, linkId); 
        addToFoundURLs(newURLs);

        System.out.println("Links from " + startURL + "stored in DB. Now storing words in DB");
        dbHandler.storeWords(indexer.getResultMap(), startURL);
        System.out.println("Words from " + startURL + " stored in DB");           // Store found words in DB
        System.out.println("Current link is\t" + startURL + " and currentDepth is  " + currentDepth);
        dbHandler.updateCrawledDate(startURL, currentDepth);
        System.out.println("Crawled date updated");  
        visitedURLs.add(startURL);
        numberOfCrawledDocuments++;
      }
        System.out.println("Crawler call method has finished!!!");
    } catch (MalformedURLException | SQLException e) {
      e.printStackTrace();
    }
    return Thread.currentThread().getName();
  }

  private void storeLinkInDB(String link, boolean isVisited, int depth){
      try {
        dbHandler.insertInDocumentsTable(link, isVisited, currentDepth);
      } catch (SQLException e) {
        e.printStackTrace();
      }
  }

  private void addToFoundURLs(List<String> urls){
    for (String url : urls) {
      if(!foundURLs.contains(url))
        foundURLs.add(url);
    }
  }

  private List<String> keepNewURLs(List<String> urls){
    List<String> result = new ArrayList<>();
    for (String url : urls) {
      if(!foundURLs.contains(url))
        result.add(url);
    }
    return result;
  }

  private int getNumberOfCrawledDocuments(){
    return dbHandler.getNumberOfVisitedURLs();
  }

}