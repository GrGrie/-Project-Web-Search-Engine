package com.grgrie;

import java.io.IOException;
import java.io.PrintWriter;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/initNewCrawl")
public class InitNewCrawlServlet extends HttpServlet {

    private static final long serialVersionUID = 5711L;
    
    public void service(HttpServletRequest request, HttpServletResponse response) throws IOException{
        String startUrl = request.getParameter("startUrl");

        App app = new App();
        PrintWriter out = response.getWriter();
        app.crawl(startUrl);
        out.println(app.success());
        
    }
}