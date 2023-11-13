package com.grgrie;

import java.io.IOException;
import java.util.Timer;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        String url = "https://www.cs.rptu.de/en";
        System.out.println( "Starting the job" );
        Crawler crawl = new Crawler(url, 0);
        DBhandler db = new DBhandler(); 
        //db.initDB("DBIS");
        db.connectTo("DBIS");

        //crawl.run();

    }
}
