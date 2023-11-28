package com.grgrie;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import org.json.*;
import org.json.JSONObject;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/ReturnJSON")
public class ReturnJSONServlet extends HttpServlet{
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //super.doGet(req, resp);
        String searchQuery = request.getParameter("ReturnJSON");
        JSONBuilder jsonBuilder = new JSONBuilder();
        PrintWriter out = response.getWriter();
        JSONObject json = jsonBuilder.createJSON(searchQuery);
        out.println(json.toString(4));
    
    }
}
