package rc.graphalgos.sparsifiers.utils;

import org.apache.commons.math3.linear.OpenMapRealVector;
import org.junit.jupiter.api.Test;
import rc.graphalgos.sparsifiers.GraphStream;

import java.util.List;


class SpanningForestTest {
    @Test
    void findSpanningForest() {
        GraphStream graphStream = new GraphStream("wiki_votes");
        graphStream.buildGraph("/home/ramji/IdeaProjects/graph_sparsification/src/main" +
                "/resources/graphs/email-Eu-core.txt");
//        graphStream.printGraphStats();
        SpanningForest spanningForestObj = new SpanningForest(graphStream);
        List<List<Integer>> spanningForest = spanningForestObj.findSpanningForest(graphStream.buildGraphMatrix(), graphStream
                .getNodeCount());
        for(List<Integer> cc: spanningForest) {
            for(int node: cc) {
                System.out.format("%d\t", node);
            }
            System.out.println();
        }
    }

    @Test
    void getSketches() {
        GraphStream graphStream = new GraphStream("wiki_votes");
        graphStream.buildGraph("/home/ramji/IdeaProjects/graph_sparsification/src/main" +
                "/resources/graphs/test_graph.txt");
//        graphStream.printGraphStats();
        SpanningForest spanningForest = new SpanningForest(graphStream);
        List<OpenMapRealVector> graphMatrix = graphStream.buildGraphMatrix();
//        System.out.println(graphMatrix.get(0).toString());
        spanningForest.getSketches(graphMatrix);
    }
}