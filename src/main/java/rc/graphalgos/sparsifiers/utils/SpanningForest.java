package rc.graphalgos.sparsifiers.utils;

import org.apache.commons.math3.linear.OpenMapRealMatrix;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import rc.graphalgos.sparsifiers.GraphStream;

import java.util.ArrayList;

/***
 * generates spanning forest of a graph by sketching
 * author: Ramji Chandrasekaran
 * date: 27-Oct-2017
 */
public class SpanningForest {

    /***
     * construct a matrix representation of input graph
     * @param graphStream input graph
     * @return n X C(n, 2) matrix
     */
    private OpenMapRealMatrix constructGraphMatrix(GraphStream graphStream) {
        Graph graph = graphStream.getGraph();
        long numRows = graphStream.getNodeCount();
        long numColumns = (numRows * (numRows - 1))/2; // # of columns = C(numRows, 2)
        OpenMapRealMatrix graphMatrix = new OpenMapRealMatrix((int) numRows, (int)numColumns);
        for(Node node: graph.getNodeSet()) {
            //outer loop - for each node
            int value = 0;
            //get the edge list of this node
            int nodeNum = node.getAttribute("nodeNum");
            //get the nodeNum of all neighbors of this node
            ArrayList<Integer> neighborNodeNums = new ArrayList<>();
            for(Edge neighbor: node.getEdgeSet()) {
                neighborNodeNums.add(neighbor.getOpposite(node).getAttribute("nodeNum"));
            }
            for(int j = 1; j<= numRows; j++) {
                //middle loop - for each lower node index
                for(int k = j+1; k<= numRows; k++) {
                    //inner loop - for each upper node index
                    if(j == nodeNum) {
                        value = 1;
                        if(neighborNodeNums.indexOf(k) >= 0) {
                            graphMatrix.addToEntry(j, k, value);
                        }
                    }else if(k == nodeNum) {
                        value = -1;
                        if(neighborNodeNums.indexOf(j) >=0) {
                            graphMatrix.addToEntry(j, k, value);
                        }
                    } else {
                        value = 0;
                        graphMatrix.addToEntry(j, k, value);
                    }

                }
            }
        }
        return graphMatrix;
    }
}
