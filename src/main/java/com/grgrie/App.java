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
        //String url = "https://www.cs.rptu.de/en/studium/bewerber/schulen";
        String url = "https://www.heapsort.org/";
        System.out.println( "Starting the job" );
        
        DBhandler dbHandler = new DBhandler(); 
        //dbHandler.initDB("DBIS");
        dbHandler.connectTo("DBIS");
        
        Crawler crawl = new Crawler(url, 1, dbHandler);

        //crawl.run();-

    }
}
