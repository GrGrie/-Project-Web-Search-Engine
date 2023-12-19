package com.grgrie;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import org.la4j.*;
import org.la4j.Vector;
import org.la4j.matrix.dense.Basic2DMatrix;
import org.la4j.vector.dense.BasicVector;

public class PageRank {
    private DBhandler dbHandler;
    private int numberOfPages;
    private Matrix matrix;
    private List<Integer> linkIDs;
    PageRank(DBhandler dbHandler){
        this.dbHandler = dbHandler;
    }

    public void calculatePageRank(){
        calculateNumberOfPages();
        calculateLinksIDs();

        double[] pageRankArray = new double[numberOfPages];
        Arrays.fill(pageRankArray, (1/numberOfPages));
        Vector currentPageRankVector = new BasicVector(pageRankArray);
        Vector previousPageRankVector = new BasicVector(pageRankArray);

        // TODO: Find a way to fill the matrix with probabilities to get to one of the nodes
        // Implement Matrix * Vector multiplication as it is on Stanford's video
        
        initMatrix();
    

        



    }

    private void calculateNumberOfPages(){
        try {
            this.numberOfPages = dbHandler.getTotalNumberOfDocuments();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private int getNumberOfPages(){
        return this.numberOfPages;
    }

    private void initMatrix(){
        Matrix m = new Basic2DMatrix(numberOfPages, numberOfPages);
        Vector probabilityVector = new BasicVector(numberOfPages);

        //Filling probability vector to insert in Matrix
        for(int i = 0; i < numberOfPages; i++){
            //int numberOfLinkedPages = dbHandler.getNumberOfOutgoingLinks()
            //probabilityVector.set(i, value);
        }
    }

    //TODO: method that calculates List<Integer> of all IDs (TODO IN DBhandler)
    private void calculateLinksIDs(){
        this.linkIDs = dbHandler.getAllLinksID();
    }
}
