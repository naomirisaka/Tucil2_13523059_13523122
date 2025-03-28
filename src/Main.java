import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Scanner;
import javax.imageio.ImageIO;

public class Main {
    private static int nodeAmt = 0;
    private static int maxDepth = 0;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Masukkan alamat absolut gambar yang ingin dikompresi: ");
        String inputPath = scanner.nextLine();

        System.out.println("Metode pengukuran error:");
        System.out.println("1. Variance");
        System.out.println("2. Mean Absolute Deviation (MAD)");
        System.out.println("3. Max Pixel Difference");
        System.out.println("4. Entropy");
        System.out.print("Masukkan metode yang ingin digunakan: ");   
        int method = scanner.nextInt();

        System.out.print("Masukkan nilai ambang batas (threshold): ");
        double threshold = scanner.nextDouble();

        System.out.print("Masukkan ukuran blok minimum: ");
        int minBlockSize = scanner.nextInt();

        System.out.print("Masukkan alamat absolut gambar hasil kompresi: ");
        scanner.nextLine();
        String outputPath = scanner.nextLine();
        scanner.close();

        try {
            BufferedImage ogImage = ImageIO.read(new File(inputPath));
            int ogSize = (int) new File(inputPath).length();
            
            long startTime = System.nanoTime();
            BufferedImage compressedImage = compressImage(ogImage, method, threshold, minBlockSize);
            long endTime = System.nanoTime();

            ImageIO.write(compressedImage, "png", new File(outputPath)); // no default extension yet
            int compressedSize = (int) new File(outputPath).length();

            double compressionRatio = (1 - (double) compressedSize / ogSize) * 100;
            double executionTime = (endTime - startTime) / 1e6;

            System.out.println();
            System.out.println("Kompresi gambar berhasil.");
            System.out.println("Waktu eksekusi: " + executionTime + " ms");
            System.out.println("Ukuran gambar asli: " + ogSize + " bytes");
            System.out.println("Ukuran gambar hasil kompresi: " + compressedSize + " bytes");
            System.out.printf("Persentase kompresi: %.2f%%\n", compressionRatio);
            System.out.println("Keadalaman pohon: " + maxDepth);
            System.out.println("Jumlah simpul pada pohon: " + nodeAmt);
            System.out.println("Gambar hasil kompresi disimpan di: " + outputPath);
        } catch (Exception e) {
            System.out.println("Gambar gagal dikompresi: " + e.getMessage());
        }
    }

    private static BufferedImage compressImage(BufferedImage img, int method, double threshold, int minSize) {
        int width = img.getWidth();
        int height = img.getHeight();
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        quadtreeCompress(img, result, 0, 0, width, height, 0, method, threshold, minSize);
        return result;
    }

    private static void quadtreeCompress(BufferedImage original, BufferedImage output, int x, int y, int sizeX, int sizeY, int depth, int method, double threshold, int minSize) {
        nodeAmt++;
        maxDepth = Math.max(maxDepth, depth);

        if (sizeX <= minSize || sizeY <= minSize || shouldMerge(original, x, y, sizeX, sizeY, method, threshold)) {
            int avgColor = getAvgColor(original, x, y, sizeX, sizeY);
            fillBlock(output, x, y, sizeX, sizeY, avgColor);
        } else {
            int halfX = sizeX / 2;
            int halfY = sizeY / 2;

            quadtreeCompress(original, output, x, y, halfX, halfY, depth + 1, method, threshold, minSize);
            quadtreeCompress(original, output, x + halfX, y, halfX, halfY, depth + 1, method, threshold, minSize);
            quadtreeCompress(original, output, x, y + halfY, halfX, halfY, depth + 1, method, threshold, minSize);
            quadtreeCompress(original, output, x + halfX, y + halfY, halfX, halfY, depth + 1, method, threshold, minSize);
        }
    }

    private static boolean shouldMerge(BufferedImage img, int x, int y, int w, int h, int method, double threshold) {
        switch (method) {
            case 1: return calculateVariance(img, x, y, w, h) <= threshold;
            case 2: return calculateMAD(img, x, y, w, h) <= threshold;
            case 3: return calculateMaxPixelDifference(img, x, y, w, h) <= threshold;
            case 4: return calculateEntropy(img, x, y, w, h) <= threshold;
            default: return false;
        }
    }

    private static double calculateVariance(BufferedImage img, int x, int y, int w, int h) {
        return 100; // variance calculation not done
    }
    
    private static double calculateMAD(BufferedImage img, int x, int y, int w, int h) {
        return 100; // mad calculation not done
    }
    
    private static double calculateMaxPixelDifference(BufferedImage img, int x, int y, int w, int h) {
        return 100; // max pixel difference calculation not done
    }
    
    private static double calculateEntropy(BufferedImage img, int x, int y, int w, int h) {
        return 100; // entropy calculation not done
    }

    private static int getAvgColor(BufferedImage img, int x, int y, int w, int h) {
        int r = 0, g = 0, b = 0, count = 0;
        for (int i = x; i < x + w; i++) {
            for (int j = y; j < y + h; j++) {
                Color c = new Color(img.getRGB(i, j));
                r += c.getRed();
                g += c.getGreen();
                b += c.getBlue();
                count++;
            }
        }
        return new Color(r / count, g / count, b / count).getRGB();
    }

    private static void fillBlock(BufferedImage img, int x, int y, int w, int h, int color) {
        for (int i = x; i < x + w; i++) {
            for (int j = y; j < y + h; j++) {
                img.setRGB(i, j, color);
            }
        }
    }
}
