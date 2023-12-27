package com.grgrie;

public class EntryClass {

    public static void main(String[] args) {
        App app = new App();
        app.setNumberOfThreads(4);                  // 4 by default
        app.setMaxDepth(2);                                // 0 by default, so only initial page will be crawled
        app.setMaxNumberOfDocuments(10);       // 0 by default, representing any number of documents
        app.setIsAllowedToLeaveDomain(true); // True by default
        //app.crawl("https://www.heapsort.org/");
        //app.emptyDatabase("dbis");
        //app.crawl("https://www.cs.rptu.de/en/");
        //app.crawl("https://informatik.uni-kl.de/en/studium/studierende/");
        //app.crawl("https://www.fachschaft.informatik.uni-kl.de/en/");
        //app.updateTFIDF();
        //Indexer.printList(app.googleSearch("informatik", false));
        
        PageRank pageRank = new PageRank(app.getDBhandler());
        pageRank.calculatePageRank();

    }
}