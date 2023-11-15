package com.grgrie;

import java.net.*;
import java.util.LinkedList;
import java.util.List;
import java.util.TimerTask;

public class Crawler extends TimerTask{

  private int maxDepth;
  private int maxDocumentsToCrawl;
  private boolean isAllowedToLeaveDomain;

  private String startURL;
  private Thread thread;
  private URL currentUrl;
  private String encoding = "ISO-8859-1";

  private Indexer indexer;
  private DBhandler dbHandler;

  private LinkedList<String> visitedURLs = new LinkedList<String>();
  private LinkedList<String> queueURL = new LinkedList<String>();
  private List<String> foundURLs = new LinkedList<>();


  public  Crawler (String url, int maxDepth, DBhandler dbHandler){
    startURL = url;
    this.maxDepth = maxDepth;
    this.dbHandler = dbHandler;

    Indexer indexer = new Indexer(encoding);

    this.indexer = indexer;
    thread = new Thread(this);
    thread.start();
  }

  public  Crawler (String url, int maxDepth, boolean isAllowedToLeaveDomain, DBhandler dbHandler){
    this(url, maxDepth, dbHandler);
    this.isAllowedToLeaveDomain = isAllowedToLeaveDomain;
  }

  public  Crawler (String url, int maxDepth, int maxDocumentsToCrawl, DBhandler dbHandler){
    this(url, maxDepth, dbHandler);
    this.maxDocumentsToCrawl = maxDocumentsToCrawl;
  }

  public  Crawler (String url, int maxDepth, int maxDocumentsToCrawl, boolean isAllowedToLeaveDomain, DBhandler dbHandler){
    this(url, maxDepth, maxDocumentsToCrawl, dbHandler);
    this.isAllowedToLeaveDomain = isAllowedToLeaveDomain;
  }


  @Override
  public void run() {
    boolean isVisited = true;
    storeLinkInDB(startURL  , isVisited);
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
          updateCrawledDateInDB(firstQueueUrl);
          queueURL.poll();
          foundURLs = indexer.getLinks();
          for (String url : foundURLs) {
            if(!visitedURLs.contains(url)){
              storeLinkInDB(url, false);
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
    System.out.println("Finished successfully");
  }

  private int storeLinkInDB(String link, boolean isVisited){
    return dbHandler.insertInDocumentsTable(link, isVisited);
  }

  private void updateCrawledDateInDB(String link){
    dbHandler.updateCrawledDate(link);
  }


  public long getThreadId(){
    return thread.getId();
  }
}