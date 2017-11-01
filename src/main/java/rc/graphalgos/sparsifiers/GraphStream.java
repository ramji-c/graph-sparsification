package rc.graphalgos.sparsifiers;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.*;

import java.io.*;

/***
 * wrapper class to represent Graph as a dynamic stream. Reads an input graph file and wraps it in a GraphStream API
 * object. contains additional transformation and representation functions, as well as utility display functions
 * author: Ramji Chandrasekaran
 * date: 21-Oct-2017
 */
public class GraphStream {
    Graph graph;
    long nodeCount;
    long edgeCount;

    public long getNodeCount() {
        return nodeCount;
    }

    public Graph getGraph() {
        return graph;
    }

    /***
     * default constructor
     * @param graph_name name of input graph
     */
    protected GraphStream(String graph_name) {
        graph = new MultiGraph(graph_name);
        graph.setAutoCreate(true);
        graph.setStrict(false);
    }

    /***
     * build graph from input file. Input should be an edge list of the graph
     * @param graph_file path of file containing graph
     */
    protected void buildGraph(String graph_file) {
        try(BufferedReader reader= new BufferedReader(new FileReader(new File(graph_file)))){
            String line;
            String[] edges;
            while((line=reader.readLine()) !=null) {
                edges = line.split("\t");
                graph.addEdge(edges[0] + "to" + edges[1], edges[0], edges[1]);
            }
            // add nodeId to each node
            addNodeNumbers();
        } catch (FileNotFoundException e) {
            System.out.println("Invalid input file path - doesn't exist");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("IO error accessing input file");
            e.printStackTrace();
        }
    }

    /***
     * print subgraphs identified by edge attributes
     */
    protected void printSubGraphs() {
        //TODO filter edges with same value for "inGi" attribute and display resulting graph
    }

    /***
     * assign an integer id to each node in graph
     */
    private void addNodeNumbers() {
        int nodeId = 0;
        for(Node node: graph.getNodeSet()) {
            node.setAttribute("nodeNum", nodeId++);
        }
    }

    protected void printGraphStats() {
        System.out.println("=============Graph Statistics==============");
        System.out.println("Number of Nodes: " + graph.getNodeCount());
        System.out.println("Number of Edges: " + graph.getEdgeCount());
        System.out.println("Edge Attributes count: " + graph.getEdge(100).getAttributeCount());
        System.out.println("Edge Attributes: ");
        for(String attr : graph.getEdge(0).getAttributeKeySet()) {
            System.out.println(attr + ": " + graph.getEdge(0).getAttribute(attr));
        }
    }

    public static void main(String args[]) {
        GraphStream graphStream = new GraphStream("wiki_votes");
        graphStream.buildGraph("/home/ramji/IdeaProjects/graph_sparsification/src/main" +
                "/resources/graphs/Wiki-Vote.txt");
        graphStream.printGraphStats();
    }
}
