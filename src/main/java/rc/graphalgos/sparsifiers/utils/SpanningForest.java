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
    private int level=1;
    private static final long MiB = 1024L * 1024L;

    /***
     * default constructor
     * @param graphStream Graph object
     */
    SpanningForest(GraphStream graphStream) {
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
        OpenMapRealMatrix[] sketchMatrices = getSketchMatrices(1);
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
     * sample an edge given a node vector
     * @param nodeVector vector of distinct pair of vertices
     * @return sampled edge, or -1 if no edge exists
     */
    int sampleEdge2(OpenMapRealVector nodeVector){
        Random random = new Random();
        int edgeIndex;
        if(nodeVector.getMaxValue() == 0 && nodeVector.getMinValue() == 0)
            return -1;
        do {
            edgeIndex = random.nextInt(nodeVector.getDimension());
        } while(nodeVector.getEntry(edgeIndex) ==0);
        return edgeIndex;
    }

    /***
     * find connected components in input graph using sketches
     * @param graphMatrix list of node vectors
     * @param superNodes aggregated nodes constituting connected components
     * @return connected components of input graph
     */
    private List<List<Integer>> buildSpanningForest(List<OpenMapRealVector> graphMatrix, List<List<Integer>>
            superNodes) {

        Set<Integer> sampledEdges = new LinkedHashSet<>();
        for(List superNode: superNodes) {
            OpenMapRealVector superNodeVector = new OpenMapRealVector(graphMatrix.get(0).getDimension());
            for(Object node: superNode) {
                superNodeVector = superNodeVector.add(graphMatrix.get((int)node-1));
            }
            sampledEdges.add(sampleEdge2(superNodeVector));
        }
        level++;
        if(Collections.frequency(sampledEdges, -1) == sampledEdges.size() || level >numSketches){
            System.out.print("\n**No Edges sampled**\n");
            return superNodes;
        } else {
            return buildSpanningForest(graphMatrix, findConnectedComponents(sampledEdges, superNodes));
        }
    }

    /***
     * find a spanning forest using sketches of input graph
     * @param graphMatrix list of node vectors
     * @param nodeCount # nodes in graph
     */
    List<List<Integer>> findSpanningForest(List<OpenMapRealVector> graphMatrix, int nodeCount) {
        // loop over sketches and construct super-nodes
        // initialize super nodes
//        Runtime runtime = Runtime.getRuntime();
//        runtime.gc();
//        long memUsage = runtime.totalMemory() - runtime.freeMemory();
//        System.out.println("Memory usage: " + bytesToMegabytes(memUsage) + " MiB");
        List<List<Integer>> superNodes = new ArrayList<>();
        for(int i=1; i<=nodeCount; i++) {
            List<Integer> nodes = new ArrayList<>();
            nodes.add(i);
            superNodes.add(nodes);
        }
        return buildSpanningForest(graphMatrix, superNodes);
    }

    private List<List<Integer>> findConnectedComponents(Set<Integer> sampledEdges, List<List<Integer>> superNodes) {
        List<int[]> edges = new ArrayList<>();
        // sampledEdges might have duplicates, remove them
        for(int edge: sampledEdges) {
            edges.add(getEdgeFromIndex(edge, numNodes));
        }
        for(int[] edge: edges) {
            //check if a sampled edge has an endpoint in a super-node
            List<Integer> ccIndices = new ArrayList<>();
            for(int endPoint: edge) {
                for(int i=0; i<superNodes.size(); i++) {
                    List superNode = superNodes.get(i);
                    if(superNode.indexOf(endPoint) != -1) {
                        ccIndices.add(i);
                    }
                }

            }
            //if ccIndices has <2 elements, skip further processing
            if(ccIndices.size() <= 1)
                continue;
            //if all nodes have collapsed into a single super-node, break the loop and return
            if(superNodes.size() == 1)
                break;
            //combine two super-nodes that are connected by a sampled edge
            superNodes.get(ccIndices.get(0)).addAll(superNodes.get(ccIndices.get(1)));
            superNodes.remove(superNodes.get(ccIndices.get(1)));
        }
        return superNodes;
    }

    int[] getEdgeFromIndex(int edgeIndex, int numNodes) {
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

    private static long bytesToMegabytes(long bytes) {
        return bytes / MiB;
    }

    public static void main(String args[]) {
        GraphStream graphStream = new GraphStream("Spanning Forest Test");
        graphStream.buildGraph(args[0]);
        SpanningForest spanningForestObj = new SpanningForest(graphStream);
        List<OpenMapRealVector> nodeVectors = graphStream.buildGraphMatrix();
        Runtime runtime = Runtime.getRuntime();
        runtime.gc();
        long memUsage = runtime.totalMemory() - runtime.freeMemory();
        System.out.println("Memory usage: " + bytesToMegabytes(memUsage) + " MiB");
//        spanningForestObj.getSketches(nodeVectors);
//        long memUsage = runtime.totalMemory() - runtime.freeMemory();
//        System.out.println("Memory usage: " + bytesToMegabytes(memUsage) + " MiB");
        List<List<Integer>> spanningForest = spanningForestObj.findSpanningForest(nodeVectors, graphStream
                .getNodeCount());
        for(List<Integer> cc: spanningForest) {
            for(int node: cc) {
                System.out.format("%d\t", node);
            }
            System.out.print("\n-------------------\n");
        }
    }
}
