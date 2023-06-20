package segmentation;

import org.junit.Before;
import org.junit.Test;
import segmentation.datastructure.FlowNetwork;
import segmentation.datastructure.link.FlowEdge;
import segmentation.datastructure.node.Vertex;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class FordFulkersonTest {

    private Vertex s;
    private Vertex t;
    private FlowNetwork<Vertex> G;

    @Before
    public void setup() {
        s = new Vertex(-1);
        t = new Vertex(-2);
        G = new FlowNetwork<>();
    }

    @Test
    public void test_SimpleGraph() {
        addEdgesSimple();
        FordFulkerson<Vertex> ff = new FordFulkerson<>(G, s, t);
        Set<Vertex> set = Set.of(s);
        assertEquals(ff.getMinCut(), set);
        assertEquals(ff.getMaxFlow(), 1);
    }

    @Test
    public void test_SimpleGraph2() {
        addEdgesSimple2();
        FordFulkerson<Vertex> ff = new FordFulkerson<>(G, s, t);
        Set<Vertex> set = Set.of(s, new Vertex(0));
        assertEquals(ff.getMinCut(), set);
        assertEquals(ff.getMaxFlow(), 4);
    }

    @Test
    public void test_ComplexGraph() {
        Vertex[] v = addComplexGraph();
        FordFulkerson<Vertex> ff = new FordFulkerson<>(G, s, t);
        Set<Vertex> set = Set.of(s);
        assertEquals(ff.getMinCut(), set);
        assertEquals(ff.getMaxFlow(), 3);
    }

    @Test
    public void test_ComplexGraph2() {
        Vertex[] v = addComplexGraph2();
        FordFulkerson<Vertex> ff = new FordFulkerson<>(G, s, t);
        Set<Vertex> set = Set.of(s, v[1]);
        assertEquals(ff.getMinCut(), set);
        assertEquals(ff.getMaxFlow(), 19);
    }

    @Test
    public void test_ComplexGraph3() {
        Vertex[] v = addComplexGraph3();
        FordFulkerson<Vertex> ff = new FordFulkerson<>(G, s, t);
        Set<Vertex> set = Set.of(s, v[0], v[1], v[3]);
        assertEquals(ff.getMinCut(), set);
        assertEquals(ff.getMaxFlow(), 23);
    }

    @Test
    public void test_NoFlow() {
        Vertex v0 = new Vertex(0);
        G.addEdge(new FlowEdge<>(s, v0, 10));
        G.addEdge(new FlowEdge<>(t, v0, 10));
        FordFulkerson<Vertex> ff = new FordFulkerson<>(G, s, t);
        assertEquals(ff.getMinCut(), Set.of(s, v0));
        assertEquals(ff.getMaxFlow(), 0);
    }

    private void addEdgesSimple() {
        Vertex v0 = new Vertex(0);
        addEdgesTo(List.of(
                new FlowEdge<>(s, v0, 1),
                new FlowEdge<>(v0, t, 2)));
    }

    private void addEdgesSimple2() {
        Vertex v0 = new Vertex(0);
        addEdgesTo(List.of(
                new FlowEdge<>(s, v0, 2),
                new FlowEdge<>(v0, t, 1),
                new FlowEdge<>(s, t, 3)));
    }

    private Vertex[] addComplexGraph() {
        Vertex[] v = makeVertices(2);
        addEdgesTo(List.of(
                new FlowEdge<>(s, v[0], 2),
                new FlowEdge<>(s, v[1], 1),
                new FlowEdge<>(v[0], v[1], 3),
                new FlowEdge<>(v[0], t, 1),
                new FlowEdge<>(v[1], t, 2)));
        return v;
    }

    private Vertex[] addComplexGraph2() {
        Vertex[] v = makeVertices(4);
        addEdgesTo(List.of(
                new FlowEdge<>(s, v[0], 10),
                new FlowEdge<>(s, v[1], 10),
                new FlowEdge<>(v[0], v[1], 2),
                new FlowEdge<>(v[0], v[2], 4),
                new FlowEdge<>(v[0], v[3], 8),
                new FlowEdge<>(v[1], v[3], 9),
                new FlowEdge<>(v[3], v[2], 6),
                new FlowEdge<>(v[2], t, 10),
                new FlowEdge<>(v[3], t, 10)));
        return v;
    }

    private Vertex[] addComplexGraph3() {
        Vertex[] v = makeVertices(4);
        addEdgesTo(List.of(
                new FlowEdge<>(s, v[0], 16),
                new FlowEdge<>(s, v[1], 13),
                new FlowEdge<>(v[0], v[1], 10),
                new FlowEdge<>(v[1], v[0], 4),
                new FlowEdge<>(v[0], v[2], 12),
                new FlowEdge<>(v[2], v[1], 9),
                new FlowEdge<>(v[1], v[3], 14),
                new FlowEdge<>(v[3], v[2], 7),
                new FlowEdge<>(v[2], t, 20),
                new FlowEdge<>(v[3], t, 4)));
        return v;
    }

    private void addEdgesTo(List<FlowEdge<Vertex>> edges) {
        for (FlowEdge<Vertex> edge : edges) {
            G.addEdge(edge);
        }
    }

    private Vertex[] makeVertices(int num) {
        Vertex[] vertices = new Vertex[num];

        for (int i = 0; i < num; i++) {
            vertices[i] = new Vertex(i);
        }

        return vertices;
    }

}
