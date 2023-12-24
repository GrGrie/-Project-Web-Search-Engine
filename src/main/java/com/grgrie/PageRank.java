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
    private double epsilon = 0.003;

    private Matrix matrix;
    private List<Integer> linkIDs;

    PageRank(DBhandler dbHandler){
        this.dbHandler = dbHandler;
    }
    

    public void calculatePageRank(){
        calculateNumberOfPages(); // numberOfPages         = documents.count(docid)
        calculateLinksIDs();      // List<Integer> linkIDs = DISTINCT links.from_docid

        double[] pageRankArray = new double[numberOfPages];
        Vector previousPageRankVector = new BasicVector(pageRankArray);
        Arrays.fill(pageRankArray, (Double) ((double) 1/ (double) numberOfPages));
        Vector currentPageRankVector = new BasicVector(pageRankArray);
        // for(int i = 0; i < numberOfPages; i++)
        //     System.out.print(pageRankArray[i] + " ");
        // System.out.println();
        // System.out.println(currentPageRankVector);

        // TODO: Find a way to fill the matrix with probabilities to get to one of the nodes
        // Implement Matrix * Vector multiplication as it is on Stanford's video
        
        initMatrix();
        while(currentPageRankVector.subtract(previousPageRankVector).sum() > epsilon){
            previousPageRankVector = currentPageRankVector;
            currentPageRankVector = multiplyMatrixByVector(matrix, currentPageRankVector);
        }

        //storeRankInDB(currentPageRankVector);
    }

    private void calculateNumberOfPages(){
        try {
            this.numberOfPages = dbHandler.getTotalNumberOfDocuments();
        } catch (SQLException e) {
            System.out.println("|*| Error in PageRank.calculateNumberOfPages() |*|");
            e.printStackTrace();
        }
    }

    private int getNumberOfPages(){
        return this.numberOfPages;
    }

    private void initMatrix(){
        Matrix m = new Basic2DMatrix(numberOfPages, numberOfPages);
        
        int matrixIndex = 0;
 
        while(matrixIndex < numberOfPages){
           //Filling probability vector to insert in Matrix
           Vector probabilityVector = fillProbabilityVector(matrixIndex);
            // for(int i = 0; i < numberOfPages; i++){
            //     // Number of pages that are pointed by this page (have outgoing links from this page to any other)
            //     int numberOfLinkedPages = dbHandler.getNumberOfOutgoingLinks(linkIDs.get(i));

            //     for(int j = 0; j < numberOfPages; j++){
            //         if(isLinkPointing(i, j))
            //             probabilityVector.set(j, 1.0 / (double)numberOfLinkedPages);
            //     }
            // }
            // //System.out.println();
            // System.out.println(probabilityVector);
            // //Insert vector as matrix row
            m.insertRow(matrixIndex, probabilityVector);
            matrixIndex++; 
        }

        matrix = m;
        
    }

    //TODO: method that calculates List<Integer> of all IDs (TODO IN DBhandler)
    private void calculateLinksIDs(){
        this.linkIDs = dbHandler.getAllLinksID();
    }

    private Vector multiplyMatrixByVector(Matrix m, Vector v){
        Vector resultVector = new BasicVector();

        resultVector = v.multiply(m);

        return resultVector;
    }
  
    public double getEpsilon() {
        return epsilon;
    }

    public void setEpsilon(double epsilon) {
        this.epsilon = epsilon;
    }

    private void storeRankInDB(BasicVector vector){
        double[] finalVector = vector.toArray();
        dbHandler.updatePageRank(finalVector, linkIDs);

    }

    private Vector fillProbabilityVector(int index){
        Vector probabilityVector = new BasicVector(numberOfPages);

        int currentLinkID = linkIDs.get(index);
        List<Integer> outgoingLinks = dbHandler.getOutgoingLinks(currentLinkID);
        if(!outgoingLinks.isEmpty()){
            
        } else {
            probabilityVector.setAll(0);
        }

        return probabilityVector;
    }

    private boolean isLinkPointing(int from_docid, int to_docid){
        boolean isPointing = false;

        if(dbHandler.isPointing(from_docid, to_docid))
            isPointing = true;

        return isPointing;
    }
}
