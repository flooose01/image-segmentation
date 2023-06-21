package segmentation;

import segmentation.datastructure.FlowNetwork;
import segmentation.datastructure.container.Index;
import segmentation.datastructure.link.FlowEdge;
import segmentation.datastructure.node.Voxel;

import org.apache.commons.math3.distribution.NormalDistribution;
import java.awt.Color;
import java.util.HashSet;
import java.util.Set;

/**
 *  The {@code ImageSegmentation} class separates the foreground/object and
 *  the background of a given image represented by a 2D Color array using
 *  given seeds.
 *  <p>
 *  This implementation uses the Graph Cut to segment the image into 2 parts.
 *  It uses the {@code FordFulkerson} as it's underlying algorithm
 *  The {@code getSegmentation} takes &Theta;(1) time.
 *  <p>
 *
 *  This implementation is adapted from
 *  <a href="https://www.csd.uwo.ca/~yboykov/Papers/ijcv06.pdf">Graph Cuts and Efficient N-DI mage Segmentation</a>
 *  by Boykov and Funka-lea
 */
public class ImageSegmentation {
    private static final int SIGMA = 60;
    private static final int LAMBDA = 1;
    private static final int DIST = 50;

    private FlowNetwork<Voxel> flowNetwork;     // flow network representing the image
    private int K;                              // K
    private Set<Index> minCut;                  // object cut

    /**
     * Segment the pic into object and background
     * @param pic the picture
     * @param seedObj pixels that indicate an object
     * @param seedBkg pixels that indicate a background
     * @throws IllegalArgumentException if one {@code pic}'s dimension is 0
     * @throws IllegalArgumentException if seed is empty
     */
    public ImageSegmentation(Color[][] pic, Set<Index> seedObj, Set<Index> seedBkg) {
        if (pic.length <= 0 || pic[0].length <= 0) {
            throw new IllegalArgumentException("Invalid picture size");
        }

        if (seedObj.isEmpty() || seedBkg.isEmpty()) {
            throw new IllegalArgumentException("seeds must not be empty");
        }

        System.out.println("Constructing flow network...");
        flowNetwork = new FlowNetwork<>();
        Voxel[][] voxels = createVoxels(pic);
        Voxel s = new Voxel(Voxel.SOURCE, pic);
        Voxel t = new Voxel(Voxel.SINK, pic);

        K = makeNLinks(voxels);
        makeTLinks(voxels, seedObj, seedBkg, s, t);

        System.out.println("Calculating min cut...");
        FordFulkerson<Voxel> ff = new FordFulkerson<>(flowNetwork, s, t);
        Set<Voxel> minCutVoxel = ff.getMinCut();
        minCut = new HashSet<>();
        for (Voxel vox : minCutVoxel) {
            if (!vox.isSink() && !vox.isSource()) {
                minCut.add(vox.getIndex());
            }
        }
    }

    /**
     * @return segmentation of the picture (set of pixels representing object)
     */
    public Set<Index> getSegmentation() {
        return minCut;
    }

    // creates a 2D array of voxels
    private Voxel[][] createVoxels(Color[][] pic) {
        Voxel[][] voxels = new Voxel[pic.length][pic[0].length];
        for (int i = 0; i < pic.length; i++) {
            for (int j = 0; j < pic[0].length; j++) {
                voxels[i][j] = new Voxel(new Index(i, j), pic);
            }
        }
        return voxels;
    }

    // make n-links between the voxels
    private int makeNLinks(Voxel[][] voxels) {
        // Make links between voxels
        // And get K
        double maxB = 0;
        for (int i = 0; i < voxels.length; i++) {
            for (int j = 0; j < voxels[0].length; j++) {
                double sumB = 0;
                // Every neighbor, get B_pq

                // There's always left pixel
                if (j > 0) {
                    double b = B(voxels[i][j - 1], voxels[i][j]);
                    flowNetwork.addEdge(new FlowEdge<>(voxels[i][j], voxels[i][j - 1], (int) b));
                    sumB += b;
                }

                // There's always right pixel
                if (j < voxels[0].length - 1) {
                    double b = B(voxels[i][j + 1], voxels[i][j]);
                    flowNetwork.addEdge(new FlowEdge<>(voxels[i][j], voxels[i][j + 1], (int) b));
                    sumB += b;
                }

                // There's always top pixel
                if (i > 0) {
                    double b = B(voxels[i - 1][j], voxels[i][j]);
                    flowNetwork.addEdge(new FlowEdge<>(voxels[i][j], voxels[i - 1][j], (int) b));
                    sumB += b;
                }

                // There's always bottom pixel
                if (i < voxels.length - 1) {
                    double b = B(voxels[i + 1][j], voxels[i][j]);
                    flowNetwork.addEdge(new FlowEdge<>(voxels[i][j], voxels[i + 1][j], (int) b));
                    sumB += b;
                }
                maxB = Math.max(maxB, sumB);
            }
        }

        // K = 1 + max(B_pq)
        return (int) (1 + maxB);
    }

    // make t-links between voxels and source and sink nodes
    private void makeTLinks(Voxel[][] voxels, Set<Index> seedObj, Set<Index> seedBkg, Voxel s, Voxel t) {
        IntensityHistogram histogram = new IntensityHistogram(voxels, seedObj, seedObj);

        for (int i = 0; i < voxels.length; i++) {
            for (int j = 0; j < voxels[0].length; j++) {
                Voxel voxel = voxels[i][j];
                if (seedObj.contains(voxel.getIndex())) {
                    FlowEdge<Voxel> e = new FlowEdge<>(s, voxel, K);
                    flowNetwork.addEdge(e);
                } else if (seedBkg.contains(voxel.getIndex())) {
                    FlowEdge<Voxel> e = new FlowEdge<>(voxel, t, K);
                    flowNetwork.addEdge(e);
                } else {
                    // Not in bkg nor obj, need to put weight based on histogram
                    FlowEdge<Voxel> e1 = new FlowEdge<>(s, voxel, (int) (LAMBDA * histogram.objR(voxel)));
                    FlowEdge<Voxel> e2 = new FlowEdge<>(voxel, t, (int) (LAMBDA * histogram.bkgR(voxel)));
                    flowNetwork.addEdge(e1);
                    flowNetwork.addEdge(e2);

                }
            }
        }
    }

    // calculate B_pq
    private double B(Voxel p, Voxel q) {
        if (p.equals(q)) {
            return 0;
        }
        double diffI = p.getIntensity() - q.getIntensity();
        double expo = - (diffI * diffI / (2 * SIGMA * SIGMA));

        return Math.exp(expo) * DIST;
    }

    // This class represents an intensity histogram based on the seeds given.
    private static class IntensityHistogram {
        private static final int NUM_INTENSITY = 256;
        private final int[] objHistogram;   // stores the count of each intensity 0 - 255 in seedObj
        private final int totalObj;         // sum of count in objHistogram

        private final int[] bkgHistogram;   // stores the count of each intensity 0 - 255 in seedBkg
        private final int totalBkg;         // sum of count in bkgHistogram

        /**
         * Constructs an intensity histogram
         * @param voxels voxels representing the image
         * @param seedObj object seed
         * @param seedBkg background seed
         */
        public IntensityHistogram(Voxel[][] voxels, Set<Index> seedObj, Set<Index> seedBkg) {
            objHistogram = new int[NUM_INTENSITY];
            bkgHistogram = new int[NUM_INTENSITY];

            NormalDistribution nd = new NormalDistribution(0, 10);

            count(objHistogram, seedObj, voxels, nd);
            count(bkgHistogram, seedBkg, voxels, nd);

            totalObj = sum(objHistogram);
            totalBkg = sum(bkgHistogram);
        }

        // Calculates the log-likelihood whether p is an object
        public double objR(Voxel p) {
            return -(Math.log(objHistogram[p.getIntensity()]) - Math.log(totalObj));
        }

        // Calculates the log-likelihood whether p is a background
        public double bkgR(Voxel p) {
            return -(Math.log(bkgHistogram[p.getIntensity()]) - Math.log(totalBkg));
        }

        // Increase the count of each index's intensity in seed
        private void count(int[] arr, Set<Index> seed, Voxel[][] voxels, NormalDistribution nd) {

            for (Index ij : seed) {
                int intensity = voxels[ij.i][ij.j].getIntensity();

                for (int i = 0; i < NUM_INTENSITY; i++) {
                    arr[i] += nd.density(i - intensity) * 1000;
                }
            }
        }

        // Helper method to sum an array
        private int sum(int[] arr) {
            int sum = 0;
            for (int num : arr) {
                sum += num;
            }
            return sum;
        }
    }
}
