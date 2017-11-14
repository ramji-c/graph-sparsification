package rc.graphalgos.sparsifiers.utils;

import org.apache.commons.math3.linear.OpenMapRealMatrix;
import org.apache.commons.math3.linear.OpenMapRealVector;
import org.junit.jupiter.api.Test;


class LzeroSamplerTest {
    private int numRows = 20;
    private int numColumns = 5000;
    @Test
    void buildHashMatrix() {
            LzeroSampler sampler = new LzeroSampler(numRows, numColumns);
            OpenMapRealMatrix hashingMatrix = sampler.buildHashMatrix();
            for(int i=0; i<numRows; i++){
                int numZeros = 0;
                for(double d: hashingMatrix.getRow(i)){
                    //System.out.print(d + "\t");
                    if (d == 0) {
                        numZeros++;
                    }
                }
                System.out.println("# of 0s found: " + numZeros);
            }
     }

    @Test
    void buildSketch() {
        LzeroSampler sampler = new LzeroSampler(numRows, numColumns);
        OpenMapRealMatrix hashingMatrix = sampler.buildHashMatrix();
        OpenMapRealVector vector = new OpenMapRealVector(numColumns);
        for(int i =0; i<numColumns; i++) {
            vector.addToEntry(i, i%3==0?1:0);
        }
        OpenMapRealMatrix sketchMatrix = sampler.buildSketch(vector, hashingMatrix);
        for(int i=0; i< numRows; i++) {
            for(double d: sketchMatrix.getRow(i)) {
                System.out.print(d + "\t");
            }
            System.out.println();
        }
    }

    @Test
    void sampleItem() {
        LzeroSampler sampler = new LzeroSampler(numRows, numColumns);
        OpenMapRealMatrix hashingMatrix = sampler.buildHashMatrix();
        OpenMapRealVector vector = new OpenMapRealVector(numColumns);
        for(int i =0; i<numColumns; i++) {
            vector.addToEntry(i, i%2==0?1:0);
        }
        OpenMapRealMatrix sketchMatrix = sampler.buildSketch(vector, hashingMatrix);
        System.out.println(sampler.sampleItem(sketchMatrix));
    }
}