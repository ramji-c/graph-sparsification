package rc.graphalgos.sparsifiers.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LzeroSamplerTest {
    @Test
    void buildHashMatrix() {
            LzeroSampler sampler = new LzeroSampler(10, 10);
            sampler.buildHashMatrix();
            for(int i=0; i<10; i++){
                for(double d: sampler.hashingMatrix.getRow(i)){
                    System.out.print(d + "\t");
                }
                System.out.println();
            }
     }
}