package rc.graphalgos.sparsifiers.utils;

import org.apache.commons.math3.linear.OpenMapRealMatrix;
import org.apache.commons.math3.linear.OpenMapRealVector;
import rc.graphalgos.sparsifiers.GraphStream;

import java.util.*;

/***
 * generates spanning forest of a graph by sketching
 * author: Ramji Chandrasekaran
 * date: 27-Oct-2017
 */
class SpanningForest {
    private int numNodes;    //# of nodes in graph
    private LzeroSampler lzeroSampler;
    private int numSketches;
    private GraphStream graphStream;

    /***
     * default constructor
     * @param graphStream Graph object
     */
    SpanningForest(GraphStream graphStream) {
        this.graphStream = graphStream;
        numNodes = graphStream.getNodeCount();
        int numColumns = (numNodes * (numNodes - 1)) / 2;
        numSketches = (int)Math.ceil(Math.log(numNodes)/Math.log(2.0d));
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
        for(int i = 0; i< numNodes; i++) {
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
    private List<List<Integer>> buildSpanningForest(List<Map> nodeSketches, List<List<Integer>> superNodes, int level) {
        List<Integer> sampledEdges = new ArrayList<>();
        for(List superNode: superNodes) {
            List<OpenMapRealMatrix> superNodeSketches = new ArrayList<>();
            for(Object node: superNode) {
                superNodeSketches.add((OpenMapRealMatrix)nodeSketches.get((int)node).get(level));
            }
            sampledEdges.add(sampleEdge(superNodeSketches));
        }
        if(Collections.frequency(sampledEdges, -1) == sampledEdges.size()){
            System.out.println("No Edges sampled");
            return superNodes;
        } else {
            return buildSpanningForest(nodeSketches, findConnectedComponents(sampledEdges, superNodes), ++level);
        }
    }

    /***
     * find a spanning forest using sketches of input graph
     * @param graphMatrix list of node vectors
     * @param nodeCount # nodes in graph
     */
    protected List<List<Integer>> findSpanningForest(List<OpenMapRealVector> graphMatrix, int nodeCount) {
        // loop over sketches and construct super-nodes
        List<Map> sketches = getSketches(graphMatrix);
        // initialize super nodes
        List<List<Integer>> superNodes = new ArrayList<>();
        for(int i=0; i<nodeCount; i++) {
            List<Integer> nodes = new ArrayList<>();
            nodes.add(i);
            superNodes.add(nodes);
        }
        return buildSpanningForest(sketches, superNodes, 0);
    }

    private List<List<Integer>> findConnectedComponents(List<Integer> sampledEdges, List<List<Integer>> superNodes) {
        List<int[]> edges = new ArrayList<>();
//        List<List> connectedComponents = new ArrayList<>();
        for(int edge: sampledEdges) {
            edges.add(getEdgeFromIndex(edge, numNodes));
        }
        for(int[] edge: edges) {
            //check if a sampled edge has an endpoint in a supernode
            List<Integer> ccIndices = new ArrayList<>();
            for(int endPoint: edge) {
                for(int i=0; i<superNodes.size(); i++) {
                    List superNode = superNodes.get(i);
                    if(superNode.indexOf(endPoint) != -1) {
                        ccIndices.add(i);
                    }
                }

            }
            //combine two supernodes that are connected by a sampled edge
            superNodes.get(ccIndices.get(0)).addAll(superNodes.get(ccIndices.get(1)));
            superNodes.remove(ccIndices.get(1));
        }

        return superNodes;
    }

    public int[] getEdgeFromIndex(int edgeIndex, int numNodes) {
        int nMinusK=0;
        int index=0;
        int[] nodes = new int[2];
        for(int i=numNodes-1; i>0; i--) {
            if(nMinusK+i>edgeIndex)
                break;
            nMinusK+=i;
            index=numNodes-i;
        }
        nodes[0] = index+1;
        nodes[1] = edgeIndex - nMinusK + 1 + nodes[0];
        return nodes;
    }
}
