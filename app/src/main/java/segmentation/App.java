package segmentation;

import segmentation.datastructure.container.Index;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import javax.imageio.ImageIO;

/**
 * The {@code App} is a program that takes in an image from the user, lets the user identify which are object
 * and background. The program segments the image into object and background, then saves it.
 */
public class App {

    private static Canvas canvas;
    private static JFrame frame;
    private static Set<Index> seedObj;
    private static Set<Index> seedBkg;

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        System.out.println("Current directory: " + System.getProperty("user.dir"));
        System.out.print("Enter filename: ");
        String name = in.next();

        seedObj = new HashSet<>();
        seedBkg = new HashSet<>();
        SwingUtilities.invokeLater(() -> createAndShowGUI(name));
    }

    // creates overall GUI (panel + buttons)
    private static void createAndShowGUI(String imageName) {
        frame = new JFrame("Image Drawing App");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        canvas = new Canvas();
        canvas.loadImage(imageName);

        frame.add(canvas, BorderLayout.CENTER);

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout());

        JButton redButton = new JButton("Obj");
        redButton.addActionListener(e -> canvas.setColor(Color.RED));

        JButton blueButton = new JButton("Bkg");
        blueButton.addActionListener(e -> canvas.setColor(Color.BLUE));

        JButton segmentButton = new JButton("Segment");
        segmentButton.addActionListener(e -> canvas.finishDrawing());

        controlPanel.add(redButton);
        controlPanel.add(blueButton);
        controlPanel.add(segmentButton);

        frame.add(controlPanel, BorderLayout.SOUTH);

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    // This class represents a canvas where we can draw onto the image associated with the canvas.
    // Things to note:
    //      When the image is drawn onto, the image pixels change. Therefore, need to use a copy to draw onto
    //      1 square in image = brushSize x brushSize pixels
    private static class Canvas extends JPanel {
        private BufferedImage image;
        private Graphics2D graphics;
        private Color currentColor;
        private String filename;
        private int width;
        private int height;
        private int brushSize;
        private BufferedImage origImage;

        /**
         * Load image with given file name
         * @param name the file name
         * @throws IllegalArgumentException if {@code name} is null
         * @throws IllegalArgumentException if opening/reading image failed
         */
        public void loadImage(String name) {
            if (name == null) throw new IllegalArgumentException("constructor argument is null");

            this.filename = name;
            try {
                // try to read from file in working directory
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

                width  = image.getWidth(null);
                height = image.getHeight(null);

                if (width <= 100 || height <= 100) {
                    brushSize = 2;
                } else if (width <= 500 || height <= 500) {
                    brushSize = 5;
                } else {
                    brushSize = 10;
                }

                setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
                origImage = new BufferedImage(width, height, image.getType());
                copyImage(image, origImage);
                graphics = image.createGraphics();
                currentColor = Color.RED;
                addMouseListener(new MouseAdapter() {
                    public void mousePressed(MouseEvent e) {
                        startDrawing(e.getX(), e.getY());
                    }

                    public void mouseReleased(MouseEvent e) {

                    }
                });
                addMouseMotionListener(new MouseMotionAdapter() {
                    public void mouseDragged(MouseEvent e) {
                        continueDrawing(e.getX(), e.getY());
                    }
                });
            }
            catch (IOException ioe) {
                throw new IllegalArgumentException("could not open image: " + name, ioe);
            }
        }

        // Sets current color as color
        public void setColor(Color color) {
            currentColor = color;
        }

        // Starts drawing at (x, y) / (j, i)
        public void startDrawing(int x, int y) {
            if (currentColor.equals(Color.RED)) {
                addIndexTo(seedObj, x, y);
            } else if (currentColor.equals(Color.BLUE)) {
                addIndexTo(seedBkg, x, y);
            }
            graphics.setColor(currentColor);
            graphics.fillRect(x, y, brushSize, brushSize);
            repaint();
        }

        // Finish drawing, and segment the image
        public void finishDrawing() {
            graphics.dispose();
            frame.dispose();
            ImageSegmentation is = new ImageSegmentation(getPixels(), seedObj, seedBkg);
            Set<Index> set = is.getSegmentation();
            colorImage(set);

            StringBuilder sb = new StringBuilder(filename);
            sb.insert(filename.lastIndexOf('.'), "-segmented");

            save(sb.toString());
        }

        // Continue drawing (x, y) / (j, i)
        public void continueDrawing(int x, int y) {
            if (currentColor.equals(Color.RED)) {
                addIndexTo(seedObj, x, y);
            } else if (currentColor.equals(Color.BLUE)) {
                addIndexTo(seedBkg, x, y);
            }
            graphics.fillRect(x, y, brushSize, brushSize);
            repaint();
        }

        // Drawing 1 square in picture, same as drawing brushSize x brushSize pixels
        // This method adds these brushSize x brushSize pixels to set
        private void addIndexTo(Set<Index> set, int col, int row) {
            for (int i = 0; i < brushSize; i++) {
                for (int j = 0; j < brushSize; j++) {
                    if (row + i <= height - 1 && col + j <= width - 1) {
                        set.add(new Index(row + i, col + j));
                    }
                }
            }
        }

        // Gets a 2D array version of the original image
        private Color[][] getPixels() {
            Color[][] pixels = new Color[height][width];
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    pixels[j][i] = new Color(origImage.getRGB(i, j));
                }
            }

            return pixels;
        }

        // Color the image's pixel specified by the given indexes with 0xff0000
        private void colorImage(Set<Index> object) {
            Color transparentRed = new Color(255, 0, 0, 100);
            for (Index index : object) {
                origImage.setRGB(index.j, index.i, transparentRed.getRGB());
            }
        }

        // copy image from 'from' to 'to'
        private void copyImage(BufferedImage from, BufferedImage to) {
            Graphics2D g2d = to.createGraphics();
            g2d.drawImage(from, 0, 0, null);
            g2d.dispose();
        }

        // Save current image to a file with given name
        private void save(String name) {
            if (name == null) throw new IllegalArgumentException("argument to save() is null");
            File file = new File(name);
            filename = file.getName();
            if (frame != null) frame.setTitle(filename);
            String suffix = filename.substring(filename.lastIndexOf('.') + 1);
            if ("jpg".equalsIgnoreCase(suffix) || "png".equalsIgnoreCase(suffix)) {
                try {
                    ImageIO.write(origImage, suffix, file);
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else {
                throw new IllegalArgumentException("Error: filename must end in .jpg or .png");
            }
        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (image != null) {
                g.drawImage(image, 0, 0, null);
            }
        }
    }
}