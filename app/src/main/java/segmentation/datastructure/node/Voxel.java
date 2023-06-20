package segmentation.datastructure.node;

import segmentation.datastructure.container.Index;

import java.awt.*;

/**
 * Voxel represents a pixel of an image as a vertice
 */
public class Voxel extends Vertex {
    public static final Index SOURCE = new Index(-1, -1);
    public static final Index SINK = new Index(-2, -2);

    private final Index index;
    private Color color;
    private int intensity;

    /**
     * Constructs vertex with given parameters. idx = Voxel.SOURCE for source vertex
     * and idx = Voxel.SINK for sink vertex
     * @param idx index = (i, j)
     * @param pic picture
     */
    public Voxel(Index idx, Color[][] pic) {
        super(idx.i * pic.length + idx.j);
        if (idx.equals(SOURCE) || idx.equals(SINK)) {
            this.index = idx;
        } else {
            if (idx.i < 0 || idx.i >= pic.length) {
                throw new IllegalArgumentException("i must be within 0 - height-1");
            }

            if (idx.j < 0 || idx.j >= pic[0].length) {
                throw new IllegalArgumentException("j must be within 0 - width-1");
            }

            this.index = idx;
            this.color = pic[idx.i][idx.j];
            this.intensity = Math.max(color.getBlue(), Math.max(color.getRed(), color.getGreen()));
        }

    }

    /**
     * @return true if voxel is a sink vertex, false otherwise
     */
    public boolean isSink() {
        return index.equals(SINK);
    }

    /**
     * @return true if voxel is a source vertex, false otherwise
     */
    public boolean isSource() {
        return index.equals(SOURCE);
    }

    /**
     * @return intensity of pixel associated with this voxel
     */
    public int getIntensity() {
        return intensity;
    }

    /**
     * @return index associated with voxel
     */
    public Index getIndex() {
        return index;
    }

    /**
     * @return color associated with voxel
     */
    public Color getColor() {
        return color;
    }

    @Override
    public String toString() {
        if (isSink()) {
            return "sink";
        } else if (isSource()) {
            return "source";
        } else {
            return String.format("%s",
                    index);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof Voxel)) {
            return false;
        } else {
            Voxel other = (Voxel) o;
            return this.index.equals(other.index) && this.color.equals(other.color);
        }
    }

    @Override
    public int hashCode() {
        if (isSource() || isSink()) {
            return index.hashCode();
        } else {
            int hash = index.hashCode();
            hash = 31 * hash + color.hashCode();
            return hash;
        }
    }


}
