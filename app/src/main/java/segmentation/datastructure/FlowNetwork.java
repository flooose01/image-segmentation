package segmentation.datastructure;

import segmentation.datastructure.link.FlowEdge;
import segmentation.datastructure.node.Vertex;

import java.util.*;

/**
 *  The {@code FlowNetwork} class represents a capacitated network
 *  with vertices of type {@link Vertex}, where each directed
 *  edge is of type {@link FlowEdge} and has a integer capacity
 *  and flow.
 *  It supports the following two primary operations: add an edge to the network,
 *  iterate over all of the edges incident to or from a vertex.
 *  Parallel edges and self-loops are permitted.
 *  <p>
 *  This implementation uses an adjacency-lists representation.
 *  All operations take constant time (in the worst case) except
 *  iterating over the edges incident to a given vertex, which takes
 *  time proportional to the number of such edges.
 *  <p>
 *
 */
public class FlowNetwork<V extends Vertex> {
    private Map<V, List<FlowEdge<V>>> adjacencyList;
    private Map<V, List<FlowEdge<V>>> inAdjacencyList;

    /**
     * Constructs an empty network.
     */
    public FlowNetwork() {
        this.adjacencyList = new HashMap<>();
        this.inAdjacencyList = new HashMap<>();
    }

    /**
     * Adds given {@code edge} to the network
     * @param edge the edge
     */
    public void addEdge(FlowEdge<V> edge) {
        if (!adjacencyList.containsKey(edge.getSource())) {
            adjacencyList.put(edge.getSource(), new LinkedList<>());
            inAdjacencyList.put(edge.getSource(), new LinkedList<>());
        }

        if (!inAdjacencyList.containsKey(edge.getDestination())) {
            adjacencyList.put(edge.getDestination(), new LinkedList<>());
            inAdjacencyList.put(edge.getDestination(), new LinkedList<>());
        }

        adjacencyList.get(edge.getSource()).add(edge);
        inAdjacencyList.get(edge.getDestination()).add(edge);
    }

    /**
     * Returns the edges pointing from vertex {@code vertex}
     * @param vertex the vertex
     * @return the edges pointing from vertex {@code vertex} as a list. The list may be modified without affecting
     *  the network
     * @throws IllegalArgumentException if vertex not in network
     */
    public List<FlowEdge<V>> getOutEdges(V vertex) {
        if (!containsVertex(vertex)) {
            throw new IllegalArgumentException(vertex + " not in network");
        }
        return new LinkedList<>(adjacencyList.get(vertex));
    }

    /**
     * Returns the edges pointing to vertex {@code vertex}
     * @param vertex the vertex
     * @return the edges pointing to vertex {@code vertex} as a list. The list may be modified without affecting
     *  the network
     * @throws IllegalArgumentException if vertex not in network
     */
    public List<FlowEdge<V>> getInEdges(V vertex) {
        if (!containsVertex(vertex)) {
            throw new IllegalArgumentException(vertex + " not in network");
        }
        return new LinkedList<>(inAdjacencyList.get(vertex));
    }

    /**
     * Returns the edges incident on vertex {@code vertex} (includes both edges pointing to
     * and from {@code vertex}).
     * @param vertex the vertex
     * @return the edges incident on vertex {@code vertex} as a list. The list may be modified without affecting
     *  the network
     * @throws IllegalArgumentException if vertex not in network
     */
    public List<FlowEdge<V>> getNeighbors(V vertex) {
        if (!containsVertex(vertex)) {
            throw new IllegalArgumentException(vertex + " not in network");
        }
        List<FlowEdge<V>> neighbors = getOutEdges(vertex);
        neighbors.addAll(getInEdges(vertex));
        return neighbors;
    }

    /**
     * @return set of all vertices in the network
     */
    public Set<V> getVertices() {
        return adjacencyList.keySet();
    }

    /**
     * Checks whether {@code v} is in network
     * @param v the vertex
     * @return true if {@code v} is in network, false otherwise
     */
    public boolean containsVertex(V v) {
        return adjacencyList.containsKey(v) || inAdjacencyList.containsKey(v);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Flow Network:");
        for (V v : adjacencyList.keySet()) {
            List<FlowEdge<V>> outEdges = adjacencyList.get(v);
            sb.append("\n").append(v).append(" --> ").append(outEdges);
        }
        return sb.toString();
    }
}