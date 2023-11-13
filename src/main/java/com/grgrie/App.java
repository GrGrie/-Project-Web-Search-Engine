package com.grgrie;

import java.io.IOException;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        String url = "https://www.cs.rptu.de/en/";
        System.out.println( "Starting the job" );
        Crawler crawl = new Crawler(url, 1);
        try {
            crawl.parse();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        DBhandler db = new DBhandler(); 
        //db.initDB("DBIS");
        db.connectTo("DBIS");

        //crawl.run();

    }
}
