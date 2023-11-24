package com.grgrie;

import java.net.*;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Crawler implements Runnable {

  private int maxDepth;
  private int currentDepth;
  private int maxDocumentsToCrawl = Integer.MAX_VALUE;
  private static int numberOfCrawledDocuments = 0;
  private boolean isAllowedToLeaveDomain;


  private URL currentUrl;
  private String encoding = "ISO-8859-1";
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
      if(dbHandler.isNullTable("documents")){                   // If documents table is empty
        //System.out.println("Inside FIRST if in Crawler.run()");
        currentDepth = 0;
        storeLinkInDB(startURL, true, currentDepth);         // Then just store links in DB and call cycle for queue. 
      }
      if(startURL != "-1" && currentDepth <= maxDepth && numberOfCrawledDocuments < maxDocumentsToCrawl) {
        indexer.indexPage(new URL(startURL), startURL);      // After this operation indexer has all the links and the words
        System.out.println("Indexed page " + startURL + " successfully");
        int linkId = dbHandler.getLinkId(startURL);
        System.out.println("Storing links in DB");
        dbHandler.storeLinks(indexer.getLinks(), currentDepth, linkId); 
        System.out.println("Links stored in DB\nStoring words in DB");
        dbHandler.storeWords(indexer.getResultMap(), startURL);
        System.out.println("Words stored in DB");           // Store found words in DB
        dbHandler.updateCrawledDate(startURL, currentDepth);   
        currentDepth++;
        numberOfCrawledDocuments++;  
      }
        System.out.println("Crawler run method has finished!!!");
    } catch (MalformedURLException | SQLException e) {
      e.printStackTrace();
    }
    
  }

  private void storeLinkInDB(String link, boolean isVisited, int depth){
      try {
        dbHandler.insertInDocumentsTable(link, isVisited, currentDepth);
      } catch (SQLException e) {
        e.printStackTrace();
      }
  }

}