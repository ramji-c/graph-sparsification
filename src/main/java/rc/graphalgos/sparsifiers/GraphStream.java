package rc.graphalgos.sparsifiers;

import org.apache.commons.math3.linear.OpenMapRealVector;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.*;

import java.io.*;
import java.util.*;

/***
 * wrapper class to represent Graph as a dynamic stream. Reads an input graph file and wraps it in a GraphStream API
 * object. contains additional transformation and representation functions, as well as utility display functions
 * author: Ramji Chandrasekaran
 * date: 21-Oct-2017
 */
public class GraphStream {
    Graph graph;

    public int getNodeCount() {
        return graph.getNodeCount();
    }

    public Graph getGraph() {
        return graph;
    }

    /***
     * default constructor
     * @param graph_name name of input graph
     */
    public GraphStream(String graph_name) {
        graph = new MultiGraph(graph_name);
        graph.setAutoCreate(true);
        graph.setStrict(false);
    }

    /***
     * build graph from input file. Input should be an edge list of the graph
     * @param graph_file path of file containing graph
     */
    public void buildGraph(String graph_file) {
        try(BufferedReader reader= new BufferedReader(new FileReader(new File(graph_file)))){
            String line;
            String[] edges;
            while((line=reader.readLine()) !=null) {
                edges = line.split(" ");
                //drop self loops
                if(edges[0].equals(edges[1])) {
                    continue;
                }
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
     * build a matrix representation of graph by streaming edges
     * edge stream here is simulated by iterating over edgeset of GraphStream object
     */
    public List<OpenMapRealVector> buildGraphMatrix() {
        List<OpenMapRealVector> nodeVectors = new ArrayList<>();
        for(int i=1; i<=getNodeCount(); i++) {
            nodeVectors.add(new OpenMapRealVector((getNodeCount() * getNodeCount()-1)/2));
        }
        //fill edge value list and construct the graphMatrix
        for(Edge edge: graph.getEdgeSet()) {
           int firstNode = edge.getNode0().getAttribute("nodeNum");
           int secondNode = edge.getNode1().getAttribute("nodeNum");

//           System.out.println(firstNode + " " + secondNode);
           // add this edge value to all node vectors
           for(int nodeNum=1; nodeNum<=getNodeCount(); nodeNum++) {
               //check if either of the nodes of edge are the current node
               //and set the correct edge value
               int edgeValue;
               if(nodeNum == firstNode) {
                   //set edge value
                   edgeValue = 1;
               } else if (nodeNum == secondNode){
                   edgeValue = -1;
               } else {
                   edgeValue = 0;
               }
               //insert the current edge value
               nodeVectors.get(nodeNum-1).addToEntry(computeEdgeIndex(firstNode, secondNode, getNodeCount()),
                       edgeValue);
           }
       }
       return nodeVectors;
    }

    /***
     * given an edge, compute an index in the node vector to store its value
     * @param node1 first node of given edge
     * @param node2 second node of given edge
     * @param numNodes # nodes in given graph
     * @return integer index
     */
    int computeEdgeIndex(int node1, int node2, int numNodes) {
        int index = 0;
        int limit = node1>node2?(numNodes-node2):(numNodes-node1);
        for(long i=numNodes-1; i>limit; i--) {
            index+=i;
        }
        return node1==0||node2==0?index+Math.abs(node1-node2):index+Math.abs(node1-node2)-1;
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
        int nodeId = 1;
        for(Node node: graph.getNodeSet()) {
            node.setAttribute("nodeNum", nodeId++);
        }
    }

    public void printGraphStats() {
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
