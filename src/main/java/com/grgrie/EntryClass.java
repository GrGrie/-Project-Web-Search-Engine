package com.grgrie;

public class EntryClass {

    public static void main(String[] args) {
        App app = new App();
        //app.crawl("https://www.heapsort.org/");
        //app.updateTFIDF();
        Indexer.printList(app.googleSearch("algebra geometr", true));
    }
    
}
