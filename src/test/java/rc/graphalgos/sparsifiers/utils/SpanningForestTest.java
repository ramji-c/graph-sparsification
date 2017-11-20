package rc.graphalgos.sparsifiers.utils;

import org.apache.commons.math3.linear.OpenMapRealVector;
import org.junit.jupiter.api.Test;
import rc.graphalgos.sparsifiers.GraphStream;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


class SpanningForestTest {

    private static final long MiB = 1024L * 1024L;

    void setup(){
        Logger logger = Logger.getLogger(SpanningForestTest.class.getName());
        logger.setLevel(Level.INFO);

    }
    @Test
    void sampleEdge2() {
        GraphStream graphStream = new GraphStream("wiki_votes");
        graphStream.buildGraph("/home/ramji/IdeaProjects/graph_sparsification/src/main" +
                "/resources/graphs/test_graph.txt");
        SpanningForest spanningForest = new SpanningForest(graphStream);
        List<OpenMapRealVector> graphMatrix = graphStream.buildGraphMatrix();
        for(int i=0; i<graphStream.getNodeCount(); i++) {
            int[] nodes = spanningForest.getEdgeFromIndex(spanningForest.sampleEdge2(graphMatrix.get(i)), graphStream
                            .getNodeCount());
            System.out.format("%d %d\n", nodes[0], nodes[1]);
        }
    }

    @Test
    void findSpanningForest() {
        GraphStream graphStream = new GraphStream("wiki_votes");
        graphStream.buildGraph("/home/ramji/IdeaProjects/graph_sparsification/src/main" +
                "/resources/graphs/web-Google1.txt");
//        graphStream.printGraphStats();
        SpanningForest spanningForestObj = new SpanningForest(graphStream);
        List<OpenMapRealVector> nodeVectors = graphStream.buildGraphMatrix();
        Runtime runtime = Runtime.getRuntime();
        runtime.gc();
        long memUsage = runtime.totalMemory() - runtime.freeMemory();
        System.out.println("Memory usage: " + bytesToMegabytes(memUsage) + " MiB");
        List<List<Integer>> spanningForest = spanningForestObj.findSpanningForest(nodeVectors, graphStream
                .getNodeCount());
        for(List<Integer> cc: spanningForest) {
            for(int node: cc) {
                System.out.format("%d\t", node);
            }
            System.out.print("\n-------------------\n");
        }
    }

    @Test
    void getSketches() {
        GraphStream graphStream = new GraphStream("wiki_votes");
        graphStream.buildGraph("/home/ramji/IdeaProjects/graph_sparsification/src/main" +
                "/resources/graphs/facebook_combined.txt");
        SpanningForest spanningForest = new SpanningForest(graphStream);
        List<OpenMapRealVector> graphMatrix = graphStream.buildGraphMatrix();
        Runtime runtime = Runtime.getRuntime();
        runtime.gc();
        long memUsage = runtime.totalMemory() - runtime.freeMemory();
        System.out.println("Memory usage: " + bytesToMegabytes(memUsage) + " MiB");
        spanningForest.getSketches(graphMatrix);
        runtime.gc();
        memUsage = runtime.totalMemory() - runtime.freeMemory();
        System.out.println("Memory usage: " + bytesToMegabytes(memUsage) + " MiB");
    }

    private static long bytesToMegabytes(long bytes) {
        return bytes / MiB;
    }
}