import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class Compression {
    public static int nodeAmt = 0;
    public static int maxDepth = 0;

    // Fungsi utama untuk mengompresi gambar menggunakan metode quadtree
    public static BufferedImage compressImage(BufferedImage img, int method, double threshold, int minSize) {
        int width = img.getWidth();
        int height = img.getHeight();
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        quadtreeCompress(img, result, 0, 0, width, height, 0, method, threshold, minSize);
        return result;
    }

    // Kompresi dengan penyesuaian threshold secara dinamis agar mendekati rasio kompresi target
    public static BufferedImage compressWithTargetRatio(
        BufferedImage original, int method, double ogThreshold, double tolerance, int minBlockSize, long ogSize,
        double targetRatio, String outputFormat) 
    {
        double low = method == 5 ? 0.0 : 0.0;
        double high = method == 5 ? 1.0 : Math.max(ogThreshold * 5, 500);
        double bestThreshold = ogThreshold;
        BufferedImage bestImage = null;

        double bestRatioDiff = Double.MAX_VALUE;
        int maxIter = 30; // Batas iterasi untuk mencegah loop tak berujung

        for (int i = 0; i < maxIter; i++) {
            double mid = (low + high) / 2;
            BufferedImage tempImage = compressImage(original, method, mid, minBlockSize);

            try {
                // Simpan hasil kompres sementara ke file temp
                File tempFile = File.createTempFile("temp_compressed", "." + outputFormat);
                ImageIO.write(tempImage, outputFormat, tempFile);
                long tempSize = tempFile.length();
                tempFile.delete();

                double achievedRatio = 1.0 - (double) tempSize / ogSize;
                double ratioDiff = Math.abs(achievedRatio - targetRatio);

                // Simpan hasil terbaik sejauh ini
                if (ratioDiff < bestRatioDiff) {
                    bestRatioDiff = ratioDiff;
                    bestThreshold = mid;
                    bestImage = tempImage;
                }

                // Sesuaikan batas bawah dan atas binary search
                if (achievedRatio < targetRatio) {
                    if (method == 5) high = mid;
                    else low = mid;
                } else {
                    if (method == 5) low = mid;
                    else high = mid;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Cetak threshold hasil penyesuaian
        System.out.println();
        System.out.printf("Threshold disesuaikan ke: %.4f\n", bestThreshold);
        return bestImage;
    }

    // Fungsi rekursif utama untuk kompresi quadtree
    private static void quadtreeCompress(
        BufferedImage original, BufferedImage output, int x, int y, int sizeX, int sizeY, int depth, 
        int method, double threshold, int minSize
    ) {
        nodeAmt++;
        maxDepth = Math.max(maxDepth, depth);

        // Jika blok cukup kecil atau homogen, isikan dengan warna rata-rata
        if (sizeX <= minSize || sizeY <= minSize || isHomogenous(original, x, y, sizeX, sizeY, method, threshold)) {
            int avgColor = ErrorEvaluation.getAvgColor(original, x, y, sizeX, sizeY);
            fillBlock(output, x, y, sizeX, sizeY, avgColor);
        } else {
            // Bagi blok jadi 4 kuadran dan kompres masing-masing
            int halfX = sizeX / 2;
            int halfY = sizeY / 2;
            int remX = sizeX - halfX;
            int remY = sizeY - halfY;

            quadtreeCompress(original, output, x, y, halfX, halfY, depth + 1, method, threshold, minSize);
            quadtreeCompress(original, output, x + halfX, y, remX, halfY, depth + 1, method, threshold, minSize);
            quadtreeCompress(original, output, x, y + halfY, halfX, remY, depth + 1, method, threshold, minSize);
            quadtreeCompress(original, output, x + halfX, y + halfY, remX, remY, depth + 1, method, threshold, minSize);
        }
    }

    // Mengecek apakah blok homogen berdasarkan metode dan threshold tertentu
    public static boolean isHomogenous(BufferedImage img, int x, int y, int w, int h, int method, double threshold) {
        switch (method) {
            case 1: return ErrorEvaluation.calculateVariance(img, x, y, w, h) < threshold;
            case 2: return ErrorEvaluation.calculateMAD(img, x, y, w, h) < threshold;
            case 3: return ErrorEvaluation.calculateMaxPixelDifference(img, x, y, w, h) < threshold;
            case 4: return ErrorEvaluation.calculateEntropy(img, x, y, w, h) < threshold;
            case 5: return ErrorEvaluation.calculateSSIM(img, x, y, w, h) > threshold; // SSIM lebih tinggi = lebih mirip
            default: return false;
        }
    }

    // Mengisi blok dengan satu warna (hasil rata-rata)
    public static void fillBlock(BufferedImage img, int x, int y, int w, int h, int color) {
        for (int i = x; i < x + w; i++) {
            for (int j = y; j < y + h; j++) {
                if (i >= 0 && i < img.getWidth() && j >= 0 && j < img.getHeight()) {
                    img.setRGB(i, j, color);
                }
            }
        }   
    }
}
