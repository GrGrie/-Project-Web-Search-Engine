package com.grgrie;

import java.net.*;
import java.util.LinkedList;
import java.util.List;
import java.util.TimerTask;

public class Crawler extends TimerTask{

  private int maxDepth;
  private String startURL;
  private Thread thread;
  private URL currentUrl;
  private String encoding = "ISO-8859-1";
  private Indexer indexer;

  private LinkedList<String> visitedURLs = new LinkedList<String>();
  private LinkedList<String> queueURL = new LinkedList<String>();
  private List<String> foundURLs = new LinkedList<>();


  public  Crawler (String url, int maxDepth){
    startURL = url;
    this.maxDepth = maxDepth;
    Indexer indexer = new Indexer(encoding);

    this.indexer = indexer;
    thread = new Thread(this);
    thread.start();
  }

  @Override
  public void run() {
    queueURL.add(startURL);
    queueURL.add(null);
    int currentDepth = 0;
    while(currentDepth <= maxDepth){            //BFS
      try{
        if(queueURL.peek() == null){
          currentDepth++;
          queueURL.removeFirst();
          System.out.println("Increased currentDepth to " + currentDepth);
        } else {
          currentUrl = new URL(queueURL.getFirst());
          System.out.println("Currently on page :: " + queueURL.getFirst());
          indexer.indexPage(currentUrl, queueURL.getFirst());
          visitedURLs.add(queueURL.getFirst());
          queueURL.poll();
          foundURLs = indexer.getLinks();
          for (String url : foundURLs) {
            if(!visitedURLs.contains(url)){
              queueURL.add(url);
              visitedURLs.add(url);
            }
          }
          Thread.sleep(300);
          queueURL.offer(null); 
          Indexer.printList(queueURL);
        }
      } catch(Exception e) {
        e.printStackTrace();
      }
     
    }
    System.out.println("Finished successfully");
  }


  public long getThreadId(){
    return thread.getId();
  }
}