package com.grgrie;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
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

    protected List<String> links = new ArrayList<>();
    private ArrayList<String> bannedWords = new ArrayList<>();
    String encoding;
    String baseUrl;
    URL url;


    public Indexer (String encoding){
      fillBannedWords(bannedWords);
      this.encoding = encoding;
    }

    protected void indexPage(URL url, String baseUrl){
        this.url = url;
        this.baseUrl = baseUrl;
        try {
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

      // Handling HREF links
      if(tag == HTML.Tag.A) {
        isInA = true;
        if(attributes.getAttribute(HTML.Attribute.HREF) != null){
          String address = (String) attributes.getAttribute(HTML.Attribute.HREF);
            if(address.startsWith("/")){
              address = baseUrl + address;
            } else if(address.startsWith("a")){  // Hardcode for one case of https://cs.uni-kl.de/en cuz there's a link starting straight with letter 'a'
              address = baseUrl + "/" + address;
            } else if(!address.startsWith("#") && !address.startsWith("./") && !address.startsWith("mailto")){
                //UrlCleaner.normalizeUrl(address);
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
    }

    public void handleText(char[] text, int position) {

        if(isInH3 || isInH1 || isInH2 || isInH4 || isInH5 || isInH6 || isInP || isInTitle || isInSpanText || isInA || isInLi)
            parseHTML(text);

    }

  private void parseHTML(char[] text){ 
    String parsed = "";
    for(int i = 0; i < text.length; i++){
      if(text[i] != ' ' && text[i] != ',' && text[i] != '.' && text[i] != ':' && text[i] != ';' && text[i] != '|' && text[i] != '[' && text[i] != '&' && text[i] != '!'
      && text[i] != ']' && text[i] != '"' && text[i] != '?' && text[i] != '©' && text[i] != '>' && text[i] != '<' && text[i] != '“' && text[i] != '„' && text[i] != '('
      && text[i] != ')' && text[i] != '{' && text[i] != '}' && text[i] != '=' && text[i] != '\'' && text[i] != '\"' && text[i] != '-' && text[i] != '+' && text[i] != '%'
      && text[i] != '*' && text[i] != '$' && text[i] != '/' && text[i] != '\b')
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
        urlConnection.setDoInput(true);
        InputStream in = urlConnection.getInputStream();
        
        InputStreamReader inputReader = new InputStreamReader(in, encoding);
        
        HTMLEditorKit.ParserCallback parserCallback = new Parser();
        parser.parse(inputReader, parserCallback, true);
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

  public List<String> getLinks(){
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

