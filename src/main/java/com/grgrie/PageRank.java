package com.grgrie;

import java.sql.SQLException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.la4j.*;
import org.la4j.matrix.dense.Basic2DMatrix;
import org.la4j.vector.dense.BasicVector;
public class PageRank {
    private DBhandler dbHandler;
    private int numberOfPages;
    private double epsilon = 0.00003;
    private final static double multiplyConstant = 1.0;
    private double alpha = 0.1;

    private Matrix matrix;
    private List<Integer> linkIDs;
    private Map<Integer, Integer> idsToMatrix;

    PageRank(DBhandler dbHandler){
        this.dbHandler = dbHandler;
        this.idsToMatrix = dbHandler.linksToIndexes();
    }
    

    public void calculatePageRank(){
        calculateNumberOfPages(); // numberOfPages         = documents.count(docid)
        calculateLinksIDs();      // List<Integer> linkIDs = DISTINCT links.from_docid

        Vector previousPageRankVector = BasicVector.zero(numberOfPages);
        Vector currentPageRankVector = new BasicVector(numberOfPages);
        currentPageRankVector.setAll((Double) ( multiplyConstant / (double) numberOfPages));
        double[] arr = new double[numberOfPages];

        //System.out.println(currentPageRankVector);

        initMatrix();
        int i = 0;
        //while(currentPageRankVector.subtract(previousPageRankVector).sum() > epsilon * multiplyConstant){
        Matrix currentVectorMatrix = currentPageRankVector.toColumnMatrix();
        Matrix previousVectorMatrix = previousPageRankVector.toColumnMatrix(); 
        
        // System.out.println("CurrentPageRankVector:");
        // for(int k = 0; k < currentPageRankVector.length(); k++){
        //     arr[k] = currentPageRankVector.get(k) * multiplyConstant;
        //     System.out.print(arr[k] + " ");
        // }
        
        //while(currentVectorMatrix.sum() - previousVectorMatrix.sum() > epsilon){
        while(i < 75){
            i++;
            previousVectorMatrix = currentVectorMatrix;
            currentVectorMatrix = multiplyVectorByMatrix(matrix, currentVectorMatrix);
        }
        
        currentPageRankVector = currentVectorMatrix.transpose().toRowVector();
        //System.out.println(currentPageRankVector);
        storeRanksInDB(currentPageRankVector.toDenseVector().toArray());

        System.out.println("\n\n");
        //System.out.println(matrix.slice(0, 0, 41, 41));

        // System.out.println("CurrentPageRankVector:");
        // for(int k = 0; k < currentPageRankVector.length(); k++){
        //     arr[k] = currentPageRankVector.get(k) * multiplyConstant;
        //     System.out.print(arr[k] + " ");
        // }


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
           
            //Insert vector as matrix row
            m.setRow(matrixIndex, probabilityVector);
            matrixIndex++; 
        }

        this.matrix = m;
    }

    private void calculateLinksIDs(){
        this.linkIDs = dbHandler.getAllLinksID();
    }

    private Matrix multiplyVectorByMatrix(Matrix m, Matrix columnVector){
        return (m.multiply(1.0-alpha).multiply(columnVector)).add(BasicVector.constant(numberOfPages, (alpha/(double)numberOfPages)).toColumnMatrix());
        //return m.multiply(columnVector); 
    }
  
    public double getEpsilon() {
        return epsilon;
    }

    public void setEpsilon(double epsilon) {
        this.epsilon = epsilon;
    }

    private void storeRanksInDB(double[] vectorArray){
        Map<Integer, Integer> swappedIdsToLink = new HashMap<>();
        int k = 0;
        for (int i : idsToMatrix.keySet()) {
            swappedIdsToLink.put(k++, i);
        }
        
        //System.out.println(swappedIdsToLink);
        dbHandler.updatePagesRank(vectorArray, swappedIdsToLink);

    }

    private Vector fillProbabilityVector(int index){
        Vector probabilityVector = new BasicVector(numberOfPages);

        int currentLinkID = linkIDs.get(index);
        List<Integer> outgoingLinks = dbHandler.getOutgoingLinks(currentLinkID);
        if(!outgoingLinks.isEmpty()){
            for(int i = 0; i < outgoingLinks.size(); i++){
                outgoingLinks.set(i, idsToMatrix.get(outgoingLinks.get(i)));
            }

            for(int i = 0; i < numberOfPages; i++){
                if(outgoingLinks.contains(i)){
                    probabilityVector.set(i, multiplyConstant / (double) outgoingLinks.size());
                } else {
                    probabilityVector.set(i, 0.0);
                }
            }
        } else {
            probabilityVector.setAll(0);
        }
        return probabilityVector;
    }

    private void printVectorAsArray (Vector vector){
        for(int i = 0; i < numberOfPages; i++){
            System.out.print(vector.get(i) + " ");
        }
        System.out.println("");

    }

}
