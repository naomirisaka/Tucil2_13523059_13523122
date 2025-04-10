import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.stream.FileImageOutputStream;
import java.awt.Graphics2D;

public class GIFExporter {
    public static void exportGIFPerDepth(
        BufferedImage original,
        int method,
        double threshold,
        int minBlockSize,
        String gifPath,
        int maxDepth,
        int width,
        int height
    ) {
        try {
            ArrayList<BufferedImage> gifFrames = new ArrayList<>();
            Color backgroundColor = getAvgColorAsColor(original, 0, 0, width, height);

            for (int targetDepth = 0; targetDepth <= maxDepth; targetDepth++) {
                BufferedImage frame = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

                Graphics2D g2d = frame.createGraphics();
                g2d.setColor(backgroundColor);
                g2d.fillRect(0, 0, width, height);
                g2d.dispose();

                compressToDepth(original, frame, 0, 0, width, height, 0, method, threshold, minBlockSize, targetDepth);
                gifFrames.add(frame);
            }

            FileImageOutputStream outputGIF = new FileImageOutputStream(new File(gifPath));
            GIFSequenceWriter writer = new GIFSequenceWriter(outputGIF, BufferedImage.TYPE_INT_RGB, 500, true);
            for (BufferedImage frame : gifFrames) {
                writer.writeToSequence(frame);
            }
            writer.close();
            outputGIF.close();

            System.out.println("GIF proses kompresi berhasil disimpan di: " + gifPath);

        } catch (IOException e) {
            System.out.println("Gagal menyimpan GIF: " + e.getMessage());
        }
    }

    private static void compressToDepth(
        BufferedImage original,
        BufferedImage output,
        int x, int y,
        int sizeX, int sizeY,
        int depth,
        int method,
        double threshold,
        int minSize,
        int maxAllowedDepth
    ) {
        if (sizeX <= minSize || sizeY <= minSize || depth >= maxAllowedDepth || shouldMerge(original, x, y, sizeX, sizeY, method, threshold)) {
            Color avgColor = getAvgColorAsColor(original, x, y, sizeX, sizeY);
            fillBlock(output, x, y, sizeX, sizeY, avgColor);
        } else {
            int halfX = sizeX / 2;
            int halfY = sizeY / 2;

            compressToDepth(original, output, x, y, halfX, halfY, depth + 1, method, threshold, minSize, maxAllowedDepth);
            compressToDepth(original, output, x + halfX, y, halfX, halfY, depth + 1, method, threshold, minSize, maxAllowedDepth);
            compressToDepth(original, output, x, y + halfY, halfX, halfY, depth + 1, method, threshold, minSize, maxAllowedDepth);
            compressToDepth(original, output, x + halfX, y + halfY, halfX, halfY, depth + 1, method, threshold, minSize, maxAllowedDepth);
        }
    }

    private static boolean shouldMerge(BufferedImage img, int x, int y, int w, int h, int method, double threshold) {
        return Compression.isHomogenous(img, x, y, w, h, method, threshold);
    }

    private static Color getAvgColorAsColor(BufferedImage img, int x, int y, int w, int h) {
        long r = 0, g = 0, b = 0;
        int count = 0;
        for (int i = x; i < x + w; i++) {
            for (int j = y; j < y + h; j++) {
                Color c = new Color(img.getRGB(i, j));
                r += c.getRed();
                g += c.getGreen();
                b += c.getBlue();
                count++;
            }
        }
        return new Color((int)(r / count), (int)(g / count), (int)(b / count));
    }

    private static void fillBlock(BufferedImage img, int x, int y, int w, int h, Color color) {
        int rgb = color.getRGB();
        for (int i = x; i < x + w; i++) {
            for (int j = y; j < y + h; j++) {
                img.setRGB(i, j, rgb);
            }
        }
    }
}
