package com.grgrie;

import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;

import com.shekhargulati.urlcleaner.UrlCleaner;


public class Crawler implements Runnable{

    private int maxDepth;
    private String startURL;
    private Thread thread;
    private InputStreamReader reader;
    private static URL url;
    private String encoding = "ISO-8859-1";


    private Boolean isInH1 = false;
    private Boolean isInH2 = false;
    private Boolean isInH3 = false;
    private Boolean isInH4 = false;
    private Boolean isInH5 = false;
    private Boolean isInH6 = false;
    private Boolean isInP = false;
    private Boolean isInTitle = false;
    private Boolean isInSpanText = false;
    private Boolean isInA = false;
    private Boolean isInLi = false;

    private ArrayList<String> bannedWords = new ArrayList<>();
    //private LinkedList<String> visitedURLs = new LinkedList<String>();


    public  Crawler (String url, int maxDepth){
        startURL = url;
        maxDepth = this.maxDepth;
        thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        fillBannedWords(bannedWords);
        try{
            URL webpage = new URL(startURL);
            url = webpage;
            URLConnection connection = webpage.openConnection();
            InputStreamReader read = new InputStreamReader(connection.getInputStream(), encoding);
            reader = read;
            Scanner scanner = new Scanner(read);
            scanner.useDelimiter("\\Z");
            PrintStream printOut = new PrintStream(new FileOutputStream("output.txt"));
            printOut.println(scanner.next());
        } catch(Exception e) {
            e.printStackTrace();
        }
        
        System.out.println("Finished successfully");
    }

    protected InputStreamReader getReader(){
        return reader;
    }

    public void parse() throws IOException {
        GetterParser kit = new GetterParser();
        HTMLEditorKit.Parser parser = kit.getParser();
        InputStream in = url.openStream();
        InputStreamReader inputReader = new InputStreamReader(in, encoding);
        FileOutputStream fileOutput = new FileOutputStream("parsed.txt");
        HTMLEditorKit.ParserCallback parserCallback = new Parser(new OutputStreamWriter(fileOutput, Charset.forName(encoding)));
        parser.parse(inputReader, parserCallback, false);
        FileReader file = new FileReader("parsed.txt");
        stemIt(file);
    }


    private void stemIt(FileReader file) throws IOException{
      List<String> fileStrings = new ArrayList<>();
      Stemmer stem = new Stemmer();
      Scanner sc = new Scanner(file).useDelimiter("\n");
      char[] stringToCharArray;
      String line;
      while(sc.hasNext()){
        line = sc.nextLine();
        stringToCharArray = line.toCharArray();
        stem.add(stringToCharArray, stringToCharArray.length);
        stem.stem();
        if(!stem.toString().equals("is")){
          if(!bannedWords.contains(stem.toString().trim())){  
          fileStrings.add(stem.toString());
        }
        } 
      }
    }
    
    
class Parser extends HTMLEditorKit.ParserCallback {

  private Writer out;

  public Parser(Writer out) {
    this.out = out;
  }

  public void handleStartTag(HTML.Tag tag, MutableAttributeSet attributes, int position) {

    if(tag == HTML.Tag.H1) isInH1 = true;
    if(tag == HTML.Tag.H2) isInH2 = true;
    if(tag == HTML.Tag.H3) isInH3 = true;
    if(tag == HTML.Tag.H4) isInH4 = true;
    if(tag == HTML.Tag.H5) isInH5 = true;
    if(tag == HTML.Tag.H6) isInH6 = true;
    if(tag == HTML.Tag.P)  isInP  = true;
    if(tag == HTML.Tag.TITLE) isInTitle = true;
    if(tag == HTML.Tag.SPAN) isInSpanText = true;
    if(tag == HTML.Tag.A) isInA = true;
    if(tag == HTML.Tag.LI) isInLi = true;
    
  }
 
  public void handleEndTag(HTML.Tag tag, int position) {

    if(tag == HTML.Tag.H1) isInH1 = false;
    if(tag == HTML.Tag.H2) isInH2 = false;
    if(tag == HTML.Tag.H3) isInH3 = false;
    if(tag == HTML.Tag.H4) isInH4 = false;
    if(tag == HTML.Tag.H5) isInH5 = false;
    if(tag == HTML.Tag.H6) isInH6 = false;
    if(tag == HTML.Tag.P) isInP = false;
    if(tag == HTML.Tag.TITLE) isInTitle = false;
    if(tag == HTML.Tag.SPAN) isInSpanText = false;
    if(tag == HTML.Tag.A) isInA = false;
    if(tag == HTML.Tag.LI) isInLi = false;
  }

  public void handleText(char[] text, int position) {

    if(isInH3 || isInH1 || isInH2 || isInH4 || isInH5 || isInH6 || isInP || isInTitle || isInSpanText || isInA || isInLi){
      parseIt(text);
    }

  }

  private void parseIt(char[] text){ 
    String parsedString = "";
    for(int i = 0; i < text.length; i++){
      if(text[i] != ' ' && text[i] != ',' && text[i] != '.' && text[i] != ':' && text[i] != ';' && text[i] != '|' && text[i] != '[' && text[i] != '&' && text[i] != '!'
      && text[i] != ']' && text[i] != '"' && text[i] != '?' && text[i] != '©' && text[i] != '>' && text[i] != '<' && text[i] != '“' && text[i] != '„')
          parsedString += text[i];
      else{
        text[i] = '\n';
        if(i != 0 && text[i-1] != '\n') {
          parsedString += "\n";
          try {
          out.write(parsedString.toLowerCase());
          out.flush();
          parsedString = "";
        } catch (IOException e) {
          e.printStackTrace();
        }} // End of catch and if
      } // End of if
    }   // End of for loop
  }     // End of method
}
  private void fillBannedWords(ArrayList<String> bannedWords){
    this.bannedWords = bannedWords;
    FileReader fr;
    try {
      fr = new FileReader("bannedWords.txt");
      Scanner sc = new Scanner(fr).useDelimiter("\n");
    while(sc.hasNext()){
      String line = sc.nextLine().trim();
      if(line.contains("|")){
        line = line.substring(0, line.indexOf("|"));
      }
      if(!line.isEmpty())
        bannedWords.add(line);
    }
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    
    //printBannedWords();

  }


  private void printBannedWords(){
    for(int i = 0; i < bannedWords.size(); i++)
      System.out.println(bannedWords.get(i));
  }
}
class GetterParser extends HTMLEditorKit {
    public HTMLEditorKit.Parser getParser() {
        return super.getParser();
    }
}