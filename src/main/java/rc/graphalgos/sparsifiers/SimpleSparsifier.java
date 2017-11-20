package rc.graphalgos.sparsifiers;


import org.graphstream.graph.Edge;

import java.util.*;

/***
 * implementation of simple sparsifier algorithm
 */
public class SimpleSparsifier {
    private GraphStream graphStream;
    private Random randomNumber;
    private static final long MiB = 1024L * 1024L;

    /***
     * default constructor
     * @param graph_name name of input graph
     * @param graph_file file containing graph to be sparsified
     */
    protected SimpleSparsifier(String graph_name, String graph_file) {
        graphStream = new GraphStream(graph_name);
        graphStream.buildGraph(graph_file);
        randomNumber = new Random(1152);
    }

    /***
     * uniform hash function - h: E -> {0, 1}; map each edge to 0 or 1 uniformly
     * @param edge edge whose hash value should be computed
     * TODO compute hash value using the input edge
     */
    private int getUniformHashValue(String edge) {
        float prob = randomNumber.nextFloat();
        // return 1 if random number is > 0.5, 0 otherwise
        return prob>=0.5?1:0;
    }

    /***
     * find the minimum cut of given graph
     * @param maxIter total number of iterations
     */
    private void findMinCut(int maxIter) {
        for(int iter=1; iter <=maxIter; iter++) {
            for(Edge edge: graphStream.graph.getEdgeSet()) {
                int hashProd = 1;
                // add an attribute corresponding to hash value generated at each iteration
                edge.setAttribute(Integer.toString(iter), getUniformHashValue(edge.getId()));
                // add this edge to sub-graph Gi if for all j<=i, getHashValue(edge) = 1
                // an edge attribute is added denoting membership in each sub-graph Gi
                for(int i=1; i<=iter; i++) {
                    hashProd *= (Integer)edge.getAttribute(Integer.toString(i));
                }
                if(hashProd == 1) {
                    edge.setAttribute("inG" + iter, true);
                } else {
                    edge.setAttribute("inG" + iter, false);
                }
            }
        }
    }

    /***
     * determine k-edge connectivity of given graph
     */
    private void findKEdgeConnectivity() {

    }

    /***
     * sparsify given graph - return an approximate sparsification of input graph
     */
    private void sparsify() {
        // max iterations is set to 2*log(numNodes)
        int numNodes = graphStream.graph.getNodeCount();
        int maxIter = 2 * (int)Math.ceil(Math.log(numNodes)/Math.log(2.0d));
        // run the MinCut algorithm
        findMinCut(maxIter);
    }

    private static long bytesToMegabytes(long bytes) {
        return bytes / MiB;
    }

    public static void main(String args[]) {
        if(args.length != 2) {
            System.err.println("Invalid number of arguments - required 2, got " + args.length);
        }
        SimpleSparsifier simpleSparsifier = new SimpleSparsifier(args[0], args[1]);
        simpleSparsifier.sparsify();
        Runtime runtime = Runtime.getRuntime();
        runtime.gc();
        long memUsage = runtime.totalMemory() - runtime.freeMemory();
        System.out.println("Memory usage: " + bytesToMegabytes(memUsage) + " MiB");
//        simpleSparsifier.graphStream.printGraphStats();
    }
}
