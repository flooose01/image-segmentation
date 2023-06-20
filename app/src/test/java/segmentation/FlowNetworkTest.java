package segmentation;

import org.junit.BeforeClass;
import org.junit.Test;
import segmentation.datastructure.FlowNetwork;
import segmentation.datastructure.link.FlowEdge;
import segmentation.datastructure.node.Vertex;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class FlowNetworkTest {

    private static Vertex[] v;

    @BeforeClass
    public static void setup() {
        v = new Vertex[10];
        for (int i = 0; i < v.length; i++) {
            v[i] = new Vertex(i);
        }
    }

    @Test
    public void test_NetworkAddEdge() {
        FlowNetwork<Vertex> network = new FlowNetwork<>();
        FlowEdge<Vertex> e1 = new FlowEdge<>(v[0], v[1], 1);
        FlowEdge<Vertex> e2 = new FlowEdge<>(v[1], v[2], 3);
        FlowEdge<Vertex> e3 = new FlowEdge<>(v[2], v[1], 2);
        network.addEdge(e1);
        network.addEdge(e2);
        network.addEdge(e3);

        assertEquals(network.getVertices(), Set.of(v[0], v[1], v[2]));
        assertEquals(network.getInEdges(v[1]), List.of(e1, e3));
        assertEquals(network.getOutEdges(v[1]), List.of(e2));
        assertEquals(network.getNeighbors(v[1]), List.of(e2, e1, e3));
    }
}
