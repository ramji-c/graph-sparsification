package rc.graphalgos.sparsifiers;

import org.apache.commons.math3.linear.OpenMapRealVector;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GraphStreamTest {
    GraphStream graphStream = new GraphStream("Test");
    @Test
    void buildGraphMatrix() {
        graphStream.buildGraph("/home/ramji/IdeaProjects/graph_sparsification/src/main" +
                "/resources/graphs/test_graph.txt");
        List<OpenMapRealVector> graphMatrix = graphStream.buildGraphMatrix();
        for(OpenMapRealVector nodeVector: graphMatrix){
            for (int i=0; i<nodeVector.getDimension(); i++) {
                System.out.print(nodeVector.getEntry(i) + " ");
            }
            System.out.println();
        }
    }

    @Test
    void computeEdgeIndex() {
//        graphStream = new GraphStream("Test");
//        System.out.println(graphStream.computeEdgeIndex(979, 1, 1005));
        for(int i=1;i<=1;i++){
            for(int j=i+1; j<=1005; j++){
                System.out.println(i+"--" +j + " " +graphStream.computeEdgeIndex(i, j, 5));
            }
        }
    }

}