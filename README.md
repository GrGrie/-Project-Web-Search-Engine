# Group-04
Project created by GrGrie

## Getting started

&nbsp;&nbsp; To install a project clone or download all the files. Program has 2 lifecycles — WEB and Desktop.
&nbsp;&nbsp; If you want to use the desktop variant — you may interract with EntryClass.java.
&nbsp;&nbsp; If you want to perform only search queries, then web version may be used. For this you have to deploy folder /target/dbis_project-1.0.0 on your server.

## Using Desktop version

&nbsp;&nbsp; After cloning this repo and opening project ehad to EntryClass.java. This is your interface. 
&nbsp;&nbsp; The first thing you would probably like to do is to create an instance of App class — something, that actually runs the program
`App app = new App();`
App class has a couple of useful methods:
 * `setMaxDepth(int maxDepth)` — sets maximum crawling depth of a Web-Crawler. Default value is 0, meaning, that only initial page will be crawled.
 * `setMaxNumberOfDocuments (int maxNumberOfDocuments)` — sets maximum number of documents (pages) to be crawled. Default value is 0, meaning, that actually any number of documents will be crawled
 * `isAllowedToLeaveDomain` — boolean value, representing opportunity of Crawler to leave domain. Default value — true
 * `crawl(String url)` — crawling a web starting from given URL with aforementioned settings
 * `updateTFIDF()` — updates TFIDF values of term in the database. Should be called explicitely after each crawl methods to ensure correct values.
 * `googleSearch(String query)` —  Implements a google-like query on a queried String. Return result is a list of Strings.
 * `readCLIinput()` — allows user to input something from the console (to search in the crawled DB). Returns String, so can be used in googleSearch method
 * `emptyDatabase(String databaseName)` — empties the required database. Quite useful to test crawler

Additional things, that might be useful:
 * `Indexer.printList(List<>)` — simple printing list method. A good use to pair with `App.googleSearch()` method to print results in the terminal

## Using web search version

&nbsp;&nbsp; When database already has some term we can perform search not only via program, but via web connection as well. Index.html contains 2 input fields.
&nbsp;&nbsp; If you want to recieve just links as plain text, type your input query in the first field and then click submit.
&nbsp;&nbsp; If you want to recieve a JSON file, corresponding to your query, use the second option.

## What I want to improve
&nbsp;&nbsp; I know that code quality is bad. I can feel it myself. I should've probably spent more time actually designing the architecture. But in the beginning I didn't even know where to start (because I've never worked with Servlets, all the HTTP-Crawling stuff and had a very little experience with JDBC), so it was an impossible task at that time. Now when I'm quite aquired to these technologies I can refactor my code to make it better, but I am a slow thinker, so I don't have time for that until the deadline. But when I will have some free time — I'll do my best to make code at least readable and easy-to-follow
&nbsp;&nbsp; Next thing is performance improvements. In some places I do thing that I know is not most efficient (sometimes even below avg.) but my intention was to make at least a working program. I am proud I did that, but there's still more to consider

## Authors
Project was solely made by me, GrGrie. Some of the lines were taken from Stackoverflow, since I didn't know how to handle some things. Amount of these lines are below 2%, so I think is it fine.

## Project status
First and main stage of the project is ready, it is working as for now.