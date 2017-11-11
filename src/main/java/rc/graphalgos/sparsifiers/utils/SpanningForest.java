package rc.graphalgos.sparsifiers.utils;

import org.apache.commons.math3.linear.OpenMapRealMatrix;
import org.apache.commons.math3.linear.OpenMapRealVector;
import rc.graphalgos.sparsifiers.GraphStream;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/***
 * generates spanning forest of a graph by sketching
 * author: Ramji Chandrasekaran
 * date: 27-Oct-2017
 */
class SpanningForest {
    private int numRows;    //# of nodes in graph
    private LzeroSampler lzeroSampler;
    private int numSketches;

    /***
     * default constructor
     * @param graphStream Graph object
     */
    SpanningForest(GraphStream graphStream) {
        numRows = graphStream.getNodeCount();
        int numColumns = (numRows * (numRows - 1)) / 2;
        numSketches = (int)Math.ceil(Math.log(numRows)/Math.log(2.0d));
        lzeroSampler = new LzeroSampler(numSketches, numColumns);
    }

    /***
     * build log n sketch matrices
     * @param numSketches log n
     * @return sketch matrices
     */
    private OpenMapRealMatrix[] getSketchMatrices(int numSketches) {
        OpenMapRealMatrix[] sketchMatrices = new OpenMapRealMatrix[numSketches];
        for(int i=0; i<numSketches; i++) {
            sketchMatrices[i] = lzeroSampler.buildHashMatrix();
        }
        return sketchMatrices;
    }

    /***
     * build sketches for each node vector in graph matrix
     * @param nodeVectors node vectors of graph
     * @return sketches for each node vector
     */
    List<Map> getSketches(List<OpenMapRealVector> nodeVectors) {
        OpenMapRealMatrix[] sketchMatrices = getSketchMatrices(numSketches);
        List<Map> nodeSketches = new ArrayList<>();
        for(int i=0; i<numRows; i++) {
            Map<Integer, OpenMapRealMatrix> nodeSketch = new HashMap<>();
            for(int sketchId=0; sketchId<numSketches; sketchId++) {
                OpenMapRealMatrix sketch = lzeroSampler.
                        buildSketch(nodeVectors.get(i), sketchMatrices[sketchId]);
                nodeSketch.put(sketchId, sketch);
            }
            nodeSketches.add(i, nodeSketch);
        }
        return nodeSketches;
    }

    /***
     * sample an edge given the sketch of a node
     * @param sketches L0 sampling sketch of a super-node
     * @return sampled edge's index or -1 if no such edge found
     */
    private int sampleEdge(List<OpenMapRealMatrix> sketches) {
        //combine sketches of given nodes
        //set combinedSketch to the first sketch and add other sketches iteratively
        OpenMapRealMatrix combinedSketch = sketches.get(0);
        System.out.println(combinedSketch);
        for(OpenMapRealMatrix sketch: sketches.subList(1, sketches.size())) {
            combinedSketch = combinedSketch.add(sketch);
        }
        return lzeroSampler.sampleItem(combinedSketch);
    }

    /***
     * find connected components in input graph using sketches
     * @param nodeSketches log n sketches of nodes
     * @param superNodes aggregated nodes constituting connected components
     * @param level recursion level - different sketch is used for each level
     * @return connected components of input graph
     */
    private List<Map> getConnectedComponents(List<Map> nodeSketches, List<List> superNodes, int level) {
        for(List superNode: superNodes) {
            List<OpenMapRealMatrix> superNodeSketches = new ArrayList<>();
            List<Integer> sampledEdges = new ArrayList<>();
            for(Object node: superNode) {
                superNodeSketches.add((OpenMapRealMatrix)nodeSketches.get((int)node).get(level));
            }
            int sampledEdge = sampleEdge(superNodeSketches);
            System.out.println(sampledEdge);
            sampledEdges.add(sampledEdge);
            // build connected component from the list of sampled edges

        }
        return null;
    }

    /***
     * find a spanning forest using sketches of input graph
     * @param graphMatrix list of node vectors
     * @param nodeCount # nodes in graph
     */
    protected void findSpanningForest(List<OpenMapRealVector> graphMatrix, int nodeCount) {
        // loop over sketches and construct super-nodes
        List<Map> sketches = getSketches(graphMatrix);
        // initialize super nodes
        List<List> superNodes = new ArrayList<>();
        for(int i=0; i<nodeCount; i++) {
            List<Integer> nodes = new ArrayList<>();
            nodes.add(i);
            superNodes.add(nodes);
        }
        List<Map> connectedComponents = getConnectedComponents(sketches, superNodes, 0);
    }
}
