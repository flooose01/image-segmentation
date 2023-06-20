package segmentation.datastructure.link;

import segmentation.datastructure.node.Vertex;

/**
 * FlowEdge represents an edge with endpoints V, a flow and capacity.
 * @param <V> The endpoints' type
 */
public class FlowEdge<V extends Vertex> {
    private final V source;
    private final V destination;
    private final int capacity;
    private int flow;

    /**
     * Constructs a flow edge with given params with default flow 0.
     * @param source Source endpoint
     * @param destination Destination endpoint
     * @param capacity Max capacity
     */
    public FlowEdge(V source, V destination, int capacity) {
        this.source = source;
        this.destination = destination;
        this.capacity = capacity;
    }

    /**
     * @return destination endpoint
     */
    public V getDestination() {
        return destination;
    }

    /**
     * @return capacity
     */
    public int getCapacity() {
        return capacity;
    }

    /**
     * @return source endpoint
     */
    public V getSource() {
        return source;
    }

    /**
     * @return current flow
     */
    public int getFlow() {
        return flow;
    }

    /**
     * Returns the residual capacity of the edge in the direction
     *  to the given {@code v}.
     * @param v one endpoint of the edge
     * @return the residual capacity of the edge in the direction to the given vertex
     *   If {@code vertex} is the tail vertex, the residual capacity equals
     *   {@code getCapacity() - getFlow()}; if {@code v} is the head vertex, the
     *   residual capacity equals {@code getFlow()}.
     * @throws IllegalArgumentException if {@code v} is not one of the endpoints of the edge
     */
    public int getResidualCapacity(V v) {
        if (v.equals(source)) {
            return flow;
        } else if (v.equals(destination)) {
            return capacity - flow;
        } else {
            throw new IllegalArgumentException(v + " not one of endpoints");
        }
    }

    /**
     * Returns the endpoint of the edge that is different from the given vertex
     * (unless the edge represents a self-loop in which case it returns the same vertex).
     * @param v one endpoint of the edge
     * @return the endpoint of the edge that is different from the given vertex
     *   (unless the edge represents a self-loop in which case it returns the same vertex)
     * @throws IllegalArgumentException if {@code v} is not one of the endpoints
     *   of the edge
     */
    public V getOther(V v) {
        if (source.equals(v)) {
            return destination;
        } else if (destination.equals(v)) {
            return source;
        } else {
            throw new IllegalArgumentException(v + " not one of endpoints");
        }
    }

    /**
     * Increases the flow on the edge in the direction to the given vertex.
     * If {@code vertex} is the tail vertex, this increases the flow on the edge by {@code delta};
     * if {@code vertex} is the head vertex, this decreases the flow on the edge by {@code delta}.
     * @param vertex one endpoint of the edge
     * @param delta amount by which to increase flow
     * @throws IllegalArgumentException if {@code vertex} is not one of the endpoints
     *         of the edge
     * @throws IllegalArgumentException if {@code delta} makes the flow
     *         on the edge either negative or larger than its capacity
     * @throws IllegalArgumentException if {@code delta} is {@code NaN}
     */
    public void addResidualFlowTo(V vertex, int delta) {
        if (delta < 0) {
            throw new IllegalArgumentException("Delta must be non-negative");
        }

        if (vertex.equals(source)) { // backward edge
            flow -= delta;
        } else if (vertex.equals(destination)) { // forward edge
            flow += delta;
        } else {
            throw new IllegalArgumentException("invalid endpoint");
        }

        if (flow < 0) {
            throw new IllegalArgumentException("Flow is negative");
        }

        if (flow > capacity){
            throw new IllegalArgumentException("Flow exceeds capacity");
        }
    }

    @Override
    public String toString() {
        return String.format("(%s, %s, %d, %d)", source, destination, capacity, flow);
    }
}
