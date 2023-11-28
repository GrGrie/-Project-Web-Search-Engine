package com.grgrie;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.*;
public class JSONBuilder {

    private App app;
    /*
     * {
        "resultList": [
                        {
                        "rank": 1,
                        "url": "http ://cs.uni -kl.de/en/organisation/",
                        "score": 0.9
                        },
                        {
                        "rank": 2,
                        "url": "http ://cs.uni -kl.de/en/studium/",
                        "score": 0.7
                        },
                        {
                        "rank": 3,
                        "url": "www.cs.uni -kl.de",
                        "score": 0.65
                        },
                        ...
        ],
        "query": {
        "k": 5,
        "query": "computer␣science␣department"
        },
        "stat": [
                    {
                    "term": "computer",
                    "df": 50
                    },
                    ...
        ],
        "cw":10000
        }
     */
    // df = number of documents, containing term
    // cw = number of terms in the collection
    protected JSONObject createJSON(List<String> urls, String queryString){

        //TODO: Implement stemmer for input query


        JSONObject answer = new JSONObject();
        List<String> queryList = new ArrayList<>(Arrays.asList(queryString.split(" ")));
        for (String queryStringInList : queryList) {
            if(queryStringInList.contains("site:"))
                queryList.remove(queryStringInList);
        }


        JSONArray result;
        String jsonArrayStringInput = "rank, url, score \n";
        for(int i = 0; i < urls.size(); i++){
            jsonArrayStringInput += i+1 + ", ";
            int commaBeforeTFIDF = urls.get(i).lastIndexOf(",");
            String url = urls.get(i).substring(1, commaBeforeTFIDF);
            String tfidfScore = urls.get(i).substring(commaBeforeTFIDF + 1, urls.get(i).length() - 1);
            jsonArrayStringInput += url + ", ";
            jsonArrayStringInput += tfidfScore + " \n";
        }
        result = CDL.toJSONArray(jsonArrayStringInput);
        answer.put("resultList", result);


        JSONObject query = new JSONObject();
        query.put("k", queryList.size());
        query.put("query", queryString);
        answer.put("query", query);


        JSONArray stat;
        jsonArrayStringInput = "term, df \n";
        for (String queryStringInList : queryList) {
            int df = app.getNumberOfTermOccurances(queryStringInList);
            jsonArrayStringInput += queryStringInList + ", ";
            jsonArrayStringInput += df + " \n"; 
        }
        stat = CDL.toJSONArray(jsonArrayStringInput);
        answer.put("stat", stat);


        answer.put("cw", app.getTotalNumberOfTerms());

        return answer;
    }

    protected JSONObject createJSON(String queryString){
        App tmpApp = new App();
        this.app = tmpApp;
        return createJSON(tmpApp.googleSearch(queryString, true), queryString);
    }
}
