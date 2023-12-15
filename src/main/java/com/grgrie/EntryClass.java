package com.grgrie;

public class EntryClass {

    public static void main(String[] args) {
        App app = new App();
        app.setMaxDepth(1);                                // 0 by default, so only initial page will be crawled
        app.setMaxNumberOfDocuments(10);       // 0 by default, representing any number of documents
        app.setIsAllowedToLeaveDomain(true); // True by default
        //app.crawl("https://www.heapsort.org/");
        //app.emptyDatabase("dbis");
        //app.crawl("https://www.cs.rptu.de/en/");
        app.updateTFIDF();
        //Indexer.printList(app.googleSearch("informatik", false));
    }
}