package segmentation.datastructure.node;

/**
 * A generic vertex
 */
public class Vertex {
    private final int id;

    /**
     * Creates a vertex with given id
     * @param id id
     */
    public Vertex(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "vertex(" + id + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof Vertex)) {
            return false;
        } else {
            Vertex other = (Vertex) o;
            return this.id == other.id;
        }
    }

    @Override
    public int hashCode() {
        return id;
    }
}
