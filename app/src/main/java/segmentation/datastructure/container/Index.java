package segmentation.datastructure.container;

/**
 * Basically (i, j) representing an index of a 2d array
 */
public class Index {
    public int i;
    public int j;

    /**
     * Constructs an index of (i, j)
     * @param i row
     * @param j column
     */
    public Index(int i, int j) {
        this.i = i;
        this.j = j;
    }

    @Override
    public String toString() {
        return "(" + i + ", " + j + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof Index)) {
            return false;
        } else {
            Index other = (Index) o;
            return this.i == other.i && this.j == other.j;
        }
    }

    @Override
    public int hashCode() {
        return 31 * i + j;
    }
}
