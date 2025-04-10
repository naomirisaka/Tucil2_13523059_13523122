import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.stream.FileImageOutputStream;

public class GIFExporter {
    // Mengekspor gambar ke format GIF dengan kompresi quadtree
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
            int backgroundColor = ErrorEvaluation.getAvgColor(original, 0, 0, width, height);

            for (int targetDepth = 0; targetDepth <= maxDepth; targetDepth++) {
                BufferedImage frame = new BufferedImage(original.getWidth(), original.getHeight(), original.getType());

                Graphics2D g2d = frame.createGraphics();
                g2d.setColor(new Color(backgroundColor));
                g2d.fillRect(0, 0, width, height);
                g2d.dispose();

                quadtreeCompressGIF(original, frame, 0, 0, width, height, 0, method, threshold, minBlockSize, targetDepth);
                gifFrames.add(frame);
            }

            FileImageOutputStream outputGIF = new FileImageOutputStream(new File(gifPath));
            GIFSequenceWriter writer = new GIFSequenceWriter(outputGIF, BufferedImage.TYPE_INT_RGB, 500, true);

            for (BufferedImage frame : gifFrames) {
                writer.writeToSequence(frame);
            }

            writer.close();
            outputGIF.close();

        } catch (IOException e) {
            System.out.println("Gagal menyimpan GIF: " + e.getMessage());
        }
    }

    // Fungsi rekursif utama untuk kompresi quadtree versi GIF
    private static void quadtreeCompressGIF(
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
        if (
            sizeX <= minSize || sizeY <= minSize || depth >= maxAllowedDepth ||
            Compression.isHomogenous(original, x, y, sizeX, sizeY, method, threshold)
        ) {
            int avgColorInt = ErrorEvaluation.getAvgColor(original, x, y, sizeX, sizeY);
            Compression.fillBlock(output, x, y, sizeX, sizeY, avgColorInt);
        } else {
            int halfX = sizeX / 2;
            int halfY = sizeY / 2;
            int remX = sizeX - halfX;
            int remY = sizeY - halfY;

            quadtreeCompressGIF(original, output, x, y, halfX, halfY, depth + 1, method, threshold, minSize, maxAllowedDepth);
            quadtreeCompressGIF(original, output, x + halfX, y, remX, halfY, depth + 1, method, threshold, minSize, maxAllowedDepth);
            quadtreeCompressGIF(original, output, x, y + halfY, halfX, remY, depth + 1, method, threshold, minSize, maxAllowedDepth);
            quadtreeCompressGIF(original, output, x + halfX, y + halfY, remX, remY, depth + 1, method, threshold, minSize, maxAllowedDepth);
        }
    }
}
