package com.grgrie;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Scanner;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import com.shekhargulati.urlcleaner.UrlCleaner;


public class Indexer {

    private String parsedString = "";
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
    private Boolean isInUL = false;
    private Boolean isInAHREF = false;

    protected List<String> links = new ArrayList<>();
    private ArrayList<String> bannedWords = new ArrayList<>();
    private DBhandler dbHandler;
    private String encoding;
    private String baseUrl;
    private String partialUrl;
    URL url;


    public Indexer (String encoding){
      fillBannedWords(bannedWords);
      this.encoding = encoding;
    }

    public Indexer (String encoding, DBhandler dbHandler){
      this(encoding);
      this.dbHandler = dbHandler;
    }

    protected void indexPage(URL url, String baseUrl){
        this.url = url;
        if(baseUrl.indexOf("/", 8) != -1) {
          int firstSlash = baseUrl.indexOf("/", 8);
          if(baseUrl.indexOf("/", firstSlash + 1) != -1){
            int secondSlash = baseUrl.indexOf("/", firstSlash + 1);
            this.partialUrl = baseUrl.substring(0, secondSlash);
          }
        }
        if(baseUrl.indexOf("/", 8) != -1) this.baseUrl = baseUrl.substring(0, baseUrl.indexOf("/", 8));
        else this.baseUrl = baseUrl;
        
        try {
            System.out.println("Indexer class :: Starting to parse the page");
            parse();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

  class Parser extends HTMLEditorKit.ParserCallback {

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
      if(tag == HTML.Tag.LI) isInLi = true;
      if(tag == HTML.Tag.UL) isInUL = true;

      // Handling HREF links
      if(tag == HTML.Tag.A || tag == HTML.Tag.LI) {
        isInA = true;
        if(attributes.getAttribute(HTML.Attribute.HREF) != null){
          isInAHREF = true;
          String address = (String) attributes.getAttribute(HTML.Attribute.HREF);
          if(!address.startsWith("#") && !address.startsWith("./") && !address.startsWith("mailto:") && !address.startsWith("/.")){
            if(address.contains(" "))
              address = address.replaceAll(" ", "%20");
            if(address.startsWith("/")){
              address = baseUrl + address;
            } else if(!address.contains("/") && !baseUrl.contains(address)){  
              address = baseUrl + "/" + address;
            } else if("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".contains(address.substring(0,1)) && !address.startsWith("http")){
              address = baseUrl + "/" + address;
            }
            if(address.startsWith("../")){
              address = partialUrl + address.substring(3);
            }
            if(address.contains("bit.ly")){
              try {
                address = UrlCleaner.unshortenUrl(address);
              } catch (IOException e) {
                e.printStackTrace();
              }
            }
              links.add(address);
          }
        }        
      }
        
    
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
        if(tag == HTML.Tag.UL) isInUL = false;
    }

    public void handleText(char[] text, int position) {

        if(isInH3 || isInH1 || isInH2 || isInH4 || isInH5 || isInH6 || isInP || isInTitle || isInSpanText || isInLi || isInUL || (isInA || isInAHREF))
          parseHTML(text);

    }

  private void parseHTML(char[] text){ 
    String parsed = "";
    for(int i = 0; i < text.length; i++){
      if(text[i] != ' ' && text[i] != ',' && text[i] != '.' && text[i] != ':' && text[i] != ';' && text[i] != '|' && text[i] != '[' && text[i] != '&' && text[i] != '!'
      && text[i] != ']' && text[i] != '"' && text[i] != '?' && text[i] != '©' && text[i] != '>' && text[i] != '<' && text[i] != '“' && text[i] != '„' && text[i] != '('
      && text[i] != ')' && text[i] != '{' && text[i] != '}' && text[i] != '=' && text[i] != '\'' && text[i] != '\"' && text[i] != '+' && text[i] != '%'
      && text[i] != '*' && text[i] != '$' && text[i] != '/')
          parsed += text[i];
      else{
        text[i] = '\n';
        if(i != 0 && text[i-1] != '\n') {
          parsedString += "\n";
          parsedString = parsedString + parsed;
          parsed = "";
        }
      }
      if(i == text.length - 1) {                                              // Handling last word on the page case
        parsedString += '\n';
        if(parsedString != null && !parsedString.equals("\n")){
          parsedString = parsedString + parsed;
          parsed = ""; 
        }
    }
    }
  }
}
  
  public void parse() throws IOException {
    GetterParser kit = new GetterParser();
    HTMLEditorKit.Parser parser = kit.getParser();

    URLConnection urlConnection = url.openConnection();
    //urlConnection.setDoInput(true);
    //urlConnection.setRequestProperty("User-Agent", "Chrome");
    InputStream in = urlConnection.getInputStream();
    InputStreamReader inputReader = new InputStreamReader(in, encoding);
    HTMLEditorKit.ParserCallback parserCallback = new Parser();

    System.out.println("In Indexer -> Parser starting parser.pasrse()");
    
    parser.parse(inputReader, parserCallback, true);
    System.out.println("Finished parser.parse()");
    List<String> result = stemIt(parsedString);
  }


    private List<String> stemIt(String stringToStem) throws IOException{
      List<String> fileStrings = new ArrayList<>();
      String[] subStrings = parsedString.split("\n");
      Stemmer stem = new Stemmer();
      char[] stringToCharArray;
      for(String subString : subStrings){
        stringToCharArray = subString.toLowerCase().toCharArray();
        stem.add(stringToCharArray, stringToCharArray.length);
        stem.stem();
        if(!stem.toString().equals("is")){
          if(!bannedWords.contains(stem.toString().trim())){  
          fileStrings.add(stem.toString());
        }
        } 
      }
      //printList(fileStrings);
      return fileStrings;
      
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
  }

  public static void printList(List<String> list){
    for(int i = 0; i < list.size(); i++)
      System.out.println(list.get(i));
  }

  public static void printList(Queue<String> list){
    for(int i = 0; i < list.size(); i++)
      System.out.println(((List<String>) list).get(i));
  }

  public List<String> getLinks(){
    //Indexer.printList(links);
    return links;
  }

  public void printResultParsedString(){
    System.out.println(parsedString);
  }
}




class GetterParser extends HTMLEditorKit {
    public HTMLEditorKit.Parser getParser() {
        return super.getParser();
    }
}

