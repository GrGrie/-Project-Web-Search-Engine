package com.grgrie;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.List;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/searchDB")
public class SearchDBServlet extends HttpServlet {

    private static final long serialVersionUID = 4711L;
    
    public void service(HttpServletRequest request, HttpServletResponse response) throws IOException{

        String searchQuery = request.getParameter("searchQuery");

        App app = new App();
        List<String> URLs;
        PrintWriter out = response.getWriter();
        URLs = app.googleSearch(searchQuery);
        for (String string : URLs) {
            out.println("Answer is " + string);
        }
       
        
    }
}
