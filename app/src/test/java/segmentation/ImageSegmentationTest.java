package segmentation;

import org.junit.Test;
import segmentation.datastructure.container.Index;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class ImageSegmentationTest {

    @Test
    public void test_ImageSegmentationConstruction() {
        Color[][] colors = new Color[3][3];
        fillColor(colors, Color.WHITE);
        colors[2][1] = Color.BLACK;
        colors[2][2] = Color.BLACK;
        colors[0][0] = Color.BLACK;


        ImageSegmentation is = new ImageSegmentation(
                colors,
                Set.of(new Index(0,0), new Index(2, 2)),
                Set.of(new Index(1,0), new Index(0, 1)));
        assertEquals(is.getSegmentation(), Set.of(new Index(0,0), new Index(2, 2), new Index(2, 1)));
    }

    @Test
    public void test_ImageSegmentationSimplePicture() {
        BufferedImage img = loadImage("./images/dots.jpg");
        Color[][] colors = getPixels(img);

        ImageSegmentation is = new ImageSegmentation(
                colors,
                Set.of(
                        new Index(14, 14), new Index(14, 13), new Index(15, 13), new Index(15,14),
                        new Index(9, 7), new Index(9 ,8), new Index(10, 7), new Index(10, 8),
                        new Index(20, 18), new Index(21, 18), new Index(22, 18), new Index(20, 19)),
                surround(colors)
        );

        Set<Index> indices = is.getSegmentation();

        colorImage(img, indices);
        save(img, "./images/dots-segment.jpg");
    }

    @Test
    public void test_ImageSegmentationSimplePicture2() {
        BufferedImage img = loadImage("./images/donut.jpg");
        Color[][] colors = getPixels(img);

        ImageSegmentation is = new ImageSegmentation(
                colors,
                donutObjHelper(),
                donutBkgHelper(colors)
        );

        Set<Index> indices = is.getSegmentation();


        colorImage(img, indices);
        save(img, "./images/donut-segment.jpg");
    }

    private Set<Index> donutObjHelper() {
        Set<Index> set = new HashSet<>();
        for (int i = 0; i < 9; i++) {
            set.add(new Index(6, 11 + i));
            set.add(new Index(7, 11 + i));
            set.add(new Index(25, 10 + i));
            set.add(new Index(24, 10 + i));
        }
        return set;
    }

    private Set<Index> donutBkgHelper(Color[][] colors) {
        Set<Index> set = center(colors, 4, 4);
        set.addAll(surround(colors));
        return set;
    }

    private void fillColor(Color[][] colors, Color c) {
        for (int i = 0; i < colors.length; i++) {
            for (int j = 0; j < colors[0].length; j++) {
                colors[i][j] = c;
            }
        }
    }

    private Set<Index> center(Color[][] colors, int n, int m) {
        int midRow = colors.length / 2 - n / 2;
        int midCol = colors[0].length / 2 - m / 2;
        Set<Index> set = new HashSet<>();
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                set.add(new Index(midRow + i, midCol + j));
            }
        }
        return set;
    }

    private Set<Index> surround(Color[][] colors) {
        Set<Index> set = new HashSet<>();
        for (int i = 0; i < colors[0].length; i++) {
            set.add(new Index(i, 0));
            set.add(new Index(i, 1));
            set.add(new Index(i, colors.length - 1));
            set.add(new Index(i, colors.length));
        }

        for (int j = 0; j < colors.length; j++) {
            set.add(new Index(0, j));
            set.add(new Index(1, j));
            set.add(new Index(colors[0].length - 1, j));
            set.add(new Index(colors.length, j));
        }

        return set;
    }

    private BufferedImage loadImage(String name) {
        if (name == null) throw new IllegalArgumentException("constructor argument is null");

        BufferedImage image;
        try {
            File file = new File(name);
            if (file.isFile()) {
                image = ImageIO.read(file);
            }

            else {

                // resource relative to .class file
                URL url = getClass().getResource(name);

                // resource relative to classloader root
                if (url == null) {
                    url = getClass().getClassLoader().getResource(name);
                }

                // or URL from web
                if (url == null) {
                    try {
                        url = (new URI(name)).toURL();
                    } catch (URISyntaxException e) {
                        throw new IllegalArgumentException("could not parse " + name, e);
                    }

                }

                image = ImageIO.read(url);
            }

            if (image == null) {
                throw new IllegalArgumentException("could not read image: " + name);
            }
        }
        catch (IOException ioe) {
            throw new IllegalArgumentException("could not open image: " + name, ioe);
        }
        return image;
    }

    private Color[][] getPixels(BufferedImage image) {
        Color[][] pixels = new Color[image.getHeight()][image.getWidth()];
        for (int i = 0; i < image.getWidth(); i++) {
            for (int j = 0; j < image.getHeight(); j++) {
                pixels[j][i] = new Color(image.getRGB(i, j));
            }
        }

        return pixels;
    }

    private void colorImage(BufferedImage image, Set<Index> object) {
        for (Index index : object) {
            image.setRGB(index.j, index.i, 0xff0000);
        }
    }

    private void save(BufferedImage image, String name) {
        if (name == null) throw new IllegalArgumentException("argument to save() is null");
        File file = new File(name);
        String suffix = name.substring(name.lastIndexOf('.') + 1);
        if ("jpg".equalsIgnoreCase(suffix) || "png".equalsIgnoreCase(suffix)) {
            try {
                ImageIO.write(image, suffix, file);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            throw new IllegalArgumentException("Error: filename must end in .jpg or .png");
        }
    }
}
