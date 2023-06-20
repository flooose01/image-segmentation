package segmentation;

import segmentation.datastructure.FlowNetwork;
import segmentation.datastructure.link.FlowEdge;
import segmentation.datastructure.node.Vertex;

import java.util.*;

/**
 *  The {@code FordFulkerson} class represents a data type for computing a
 *  <em>maximum st-flow</em> and <em>minimum st-cut</em> in a flow
 *  network.
 *  <p>
 *  This implementation uses the <em>Ford-Fulkerson</em> algorithm with
 *  the <em>shortest augmenting path</em> heuristic.
 *  The constructor takes <em>O</em>(<em>E V</em> (<em>E</em> + <em>V</em>))
 *  time, where <em>V</em> is the number of vertices and <em>E</em> is
 *  the number of edges. In practice, the algorithm will run much faster.
 *  The {@code inCut()} and {@code value()} methods take &Theta;(1) time.
 *  <p>
 *
 *  This implementation is adapted from
 *  <a href="https://algs4.cs.princeton.edu/64maxflow">Section 6.4</a> of
 *  <i>Algorithms, 4th Edition</i> by Robert Sedgewick and Kevin Wayne.
 */
public class FordFulkerson<T extends Vertex> {

    private FlowNetwork<T> G;               // flow network to find min cut
    private Set<T> marked;                  // marked.contains(v) iff s->v path in residual graph
    private Map<T, FlowEdge<T>> edgeTo;     // edgeTo[v] = last edge on shortest residual s->v path
    private int value;                      // current value of max flow

    /**
     * Compute a maximum flow and minimum cut in the network {@code G}
     * from vertex {@code s} to vertex {@code t}.
     * @param G the flow network
     * @param s the source vertex
     * @param t the sink vertex
     * @throws IllegalArgumentException unless {@code s} in {@code G}
     * @throws IllegalArgumentException unless {@code t} in {@code G}
     * @throws IllegalArgumentException if {@code s.equals(t)}
     * @throws IllegalArgumentException if initial flow is infeasible
     */
    public FordFulkerson(FlowNetwork<T> G, T s, T t) {
        this.G = G;
        validate(s);
        validate(t);
        if (s.equals(t))       throw new IllegalArgumentException("Source equals sink");
        if (!isFeasible(s, t)) throw new IllegalArgumentException("Initial flow is infeasible");

        // while there exists an augmenting path, use it
        value = excess(t);
        while (hasAugmentingPath(s, t)) {

            // compute bottleneck capacity
            int bottle = Integer.MAX_VALUE;
            T curr = t;
            while (!curr.equals(s)) {
                bottle = Math.min(bottle, edgeTo.get(curr).getResidualCapacity(curr));
                curr = edgeTo.get(curr).getOther(curr);
            }

            curr = t;
            while (!curr.equals(s)) {
                edgeTo.get(curr).addResidualFlowTo(curr, bottle);
                curr = edgeTo.get(curr).getOther(curr);
            }

            value += bottle;
        }

        // check optimality conditions
        assert check(s, t);
    }

    /**
     * Returns true if the specified vertex is on the {@code s} side of the mincut.
     *
     * @param  v vertex
     * @return {@code true} if vertex {@code v} is on the {@code s} side of the mincut;
     *         {@code false} otherwise
     * @throws IllegalArgumentException unless {@code 0 <= v < V}
     */
    public boolean inCut(T v)  {
        validate(v);
        return marked.contains(v);
    }

    /**
     * Gets the resulting min cut
     * @return the set of vertices in min cut
     */
    public Set<T> getMinCut() {
        return marked;
    }

    /**
     * Gets the maximum flow
     * @return max flow
     */
    public int getMaxFlow() {
        return value;
    }


    // is there an augmenting path?
    // if so, upon termination edgeTo[] will contain a parent-link representation of such a path
    // this implementation finds a shortest augmenting path (fewest number of edges),
    // which performs well both in theory and in practice
    private boolean hasAugmentingPath(T s, T t) {
        edgeTo = new HashMap<>();
        marked = new HashSet<>();

        // breadth-first search
        Queue<T> queue = new LinkedList<>();
        queue.add(s);
        marked.add(s);
        while (!queue.isEmpty() && !marked.contains(t)) {
            T v = queue.remove();

            for (FlowEdge<T> e : G.getNeighbors(v)) {
                T w = e.getOther(v);

                // if residual capacity from v to w
                if (e.getResidualCapacity(w) > 0) {
                    if (!marked.contains(w)) {
                        edgeTo.put(w, e);
                        marked.add(w);
                        queue.add(w);
                    }
                }
            }
        }

        // is there an augmenting path?
        return marked.contains(t);
    }


    // throw an IllegalArgumentException if v is not inside network
    private void validate(T v)  {
        if (!G.containsVertex(v))
            throw new IllegalArgumentException("vertex " + v + " is not in network");
    }

    // return excess flow at vertex v
    // negative means there are more out flow than in flow.
    // positive means otherwise
    private int excess(T v) {
        int excess = 0;
        for (FlowEdge<T> e : G.getOutEdges(v)) {
            excess -= e.getFlow();
        }

        for (FlowEdge<T> e : G.getInEdges(v)) {
            excess += e.getFlow();
        }

        return excess;
    }

    // checks whether flow is feasible
    private boolean isFeasible(T s, T t) {
        // check that capacity constraints are satisfied
        for (T v : G.getVertices()) {
            for (FlowEdge<T> e : G.getOutEdges(v)) {
                if (e.getFlow() < 0 || e.getFlow() > e.getCapacity()) {
                    System.err.println("Edge does not satisfy capacity constraints: " + e);
                    return false;
                }
            }
        }

        // check that net flow into a vertex equals zero, except at source and sink
        if (Math.abs(value + excess(s)) > 0) {
            System.err.println("Excess at source = " + excess(s));
            System.err.println("Max flow         = " + value);
            return false;
        }
        if (Math.abs(value - excess(t)) > 0) {
            System.err.println("Excess at sink   = " + excess(t));
            System.err.println("Max flow         = " + value);
            return false;
        }
        for (T v : G.getVertices()) {
            if (v.equals(s) || v.equals(t)) {
                continue;
            } else if (Math.abs(excess(v)) > 0) {
                System.err.println("Net flow out of " + v + " doesn't equal zero");
                return false;
            }
        }
        return true;
    }

    // check optimality conditions
    private boolean check(T s, T t) {

        // check that flow is feasible
        if (!isFeasible(s, t)) {
            System.err.println("Flow is infeasible");
            return false;
        }

        // check that s is on the source side of min cut and that t is not on source side
        if (!inCut(s)) {
            System.err.println("source " + s + " is not on source side of min cut");
            return false;
        }
        if (inCut(t)) {
            System.err.println("sink " + t + " is on source side of min cut");
            return false;
        }

        // check that value of min cut = value of max flow
        int mincutValue = 0;
        for (T v : G.getVertices()) {
            for (FlowEdge<T> e : G.getNeighbors(v)) {
                if (v.equals(e.getSource()) && inCut(e.getSource()) && !inCut(e.getDestination())) {
                    mincutValue += e.getCapacity();
                }
            }
        }

        if (Math.abs(mincutValue - value) > 0) {
            System.err.println("Max flow value = " + value + ", min cut value = " + mincutValue);
            return false;
        }

        return true;
    }
}
