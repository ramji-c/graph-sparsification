package rc.graphalgos.sparsifiers.utils;

import org.apache.commons.math3.linear.OpenMapRealMatrix;
import org.apache.commons.math3.linear.OpenMapRealVector;

import java.util.Random;

/***
 * implementation of a L0 sampler.
 */
class LzeroSampler {
    OpenMapRealMatrix hashingMatrix;
    private int numRows;
    private int numColumns;

    /***
     * default constructor
     * @param numRows is bounded by log n + 2, where n is the number of nodes in the graph
     * @param numColumns is bounded # of distinct pairs of vertices
     */
    LzeroSampler(int numRows, int numColumns) {
        hashingMatrix = new OpenMapRealMatrix(numRows, numColumns);
        this.numRows = numRows;
        this.numColumns = numColumns;
    }

    /***
     * generate random bounded hash values
     * @param start lower bound
     * @param end upper bound
     * @param numValues # of values to generate
     * @return array of hash values
     */
    private double[] getHashValues(int start, int end, int numValues) {
        Random randomHash = new Random(end);
        return randomHash.ints(numValues, start, end+1).mapToDouble((x) -> ((double)x)).toArray();
    }

    /***
     * build hash matrix using getHashValues()
     */
    void buildHashMatrix(){
        for(int row=0; row<numRows; row++) {
            int start = 0;
            int end = (int)(Math.pow(2, row) - 1);
            hashingMatrix.setRow(row, getHashValues(start, end, numColumns));
        }
    }

    /***
     * return index of a non-zero element from sketch
     * @param sketchMatrix l0-sampling sketch
     * @return index of non-zero element, or -1 if no such element found
     */
    int sampleItem(OpenMapRealMatrix sketchMatrix){
        for(int row=0; row<numRows; row++){
            double[] sketchVector = sketchMatrix.getRow(row);
            //check if sketchVector[0] ~= 1, return sketchVector[1]/sketchVector[2]
            if(sketchVector[0] == 1) {
                return (int)sketchVector[1]/(int)sketchVector[2];
            }
        }
        return -1;
    }

    /***
     * build a sketch using hash matrix and given graph matrix
     * @param nodeVector vector of input node in graph
     * @return matrix of Dj, Sj, Cj
     */
    OpenMapRealMatrix buildSketch(OpenMapRealVector nodeVector){
        OpenMapRealMatrix sketchMatrix = new OpenMapRealMatrix(numRows, 3);
        //sketchRow[0] = Dj, sketchRow[1] = Sj, sketchRow[2] = Cj
        double[] sketchRow;
        for(int row=0; row<numRows; row++) {
            int cnt = 0;
            int dotProd = 0;
            int itemFreq = 0;
            sketchRow = new double[3];
            double[] hashVector = hashingMatrix.getRow(row);
            for(int col=0; col<hashVector.length; col++) {
                if (hashVector[col] == 0.0) {
                    cnt++;
                    dotProd += (col * nodeVector.getEntry(col));
                    itemFreq += nodeVector.getEntry(col);
                }
            }
            //calculate Dj, Sj, Cj
            sketchRow[0] = cnt;
            sketchRow[1] = dotProd;
            sketchRow[2] = itemFreq;
            sketchMatrix.setRow(row, sketchRow);
        }
        return sketchMatrix;
    }
}
