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

        System.out.println("================================================================================");
        System.out.println("                           Program Kompresi Gambar");
        System.out.println("                           dengan Quadtree Method");
        System.out.println("================================================================================");

        String fileName = "";
        while (true) {
            System.out.println();
            System.out.print("Masukkan nama file gambar (diakhiri dengan .jpg, .jpeg, atau .png): ");
            fileName = scanner.nextLine();
            if (fileName.toLowerCase().endsWith(".jpg") || fileName.toLowerCase().endsWith(".jpeg")|| 
            fileName.toLowerCase().endsWith(".png")) {
                break;
            } else {
                System.out.println("Format file tidak didukung. Harus diakhiri dengan .jpg atau .png.");
            }
        }
        String inputPath = "../test/input/" + fileName;

        File inputFile = new File(inputPath);
        if (!inputFile.exists()) {
            System.out.println("File input tidak ditemukan: " + inputPath);
            return;
        }

        System.out.println();
        System.out.println("================================================================================");
        System.out.println("METODE PENGUKURAN ERROR:");
        System.out.println("================================================================================");
        System.out.println("1. Variance");
        System.out.println("2. Mean Absolute Deviation (MAD)");
        System.out.println("3. Max Pixel Difference");
        System.out.println("4. Entropy");
        System.out.println("5. Structural Similarity Index (SSIM)");
        System.out.println("================================================================================");
        int method = 0;
        while (true) {
            System.out.print("Masukkan metode yang ingin digunakan: ");
            if (scanner.hasNextInt()) {
                method = scanner.nextInt();
                if (method >= 1 && method <= 5) {
                    break;
                } else {
                    System.out.println("Masukkan angka antara 1 sampai 5.");
                }
            } else {
                System.out.println("Input harus berupa angka.");
                scanner.next(); 
            }
        }

        if (method == 5) {
            System.out.println();
            System.out.println("================================================================================");
            System.out.println("Anda memilih metode SSIM.");
            System.out.println("Threshold SSIM berkisar antara 0 sampai 1.");
            System.out.println("Semakin tinggi threshold, maka blok akan digabung jika sangat mirip.");
            System.out.println("Semakin rendah threshold, maka blok digabung walau kurang mirip.");
            System.out.println("================================================================================");
            System.out.println();
        } else {
            System.out.println();
        }

        double threshold = 0;
        if (method == 5) {
            while (true) {
                System.out.print("Masukkan nilai ambang batas (threshold SSIM antara 0 dan 1): ");
                threshold = scanner.nextDouble();
                if (threshold >= 0 && threshold <= 1) {
                    break;
                } else {
                    System.out.println("Nilai threshold SSIM harus antara 0 dan 1.");
                }
            }
        } else {
            System.out.print("Masukkan nilai ambang batas (threshold): ");
            threshold = scanner.nextDouble();
        }

        int minBlockSize = 0;
        while (true) {
            System.out.print("Masukkan ukuran blok minimum: ");
            minBlockSize = scanner.nextInt();
            if (minBlockSize > 0) {
                break;
            } else {
                System.out.println("Ukuran blok minimum harus lebih besar dari 0.");
            }
        }

        scanner.nextLine();

        String outputName = "";
        while (true) {
            System.out.print("Masukkan nama file hasil kompresi (diakhiri dengan .jpg, .jpeg, atau .png): ");
            outputName = scanner.nextLine().trim().toLowerCase();
            if (outputName.endsWith(".jpg") || outputName.endsWith(".jpeg") || outputName.endsWith(".png")) {
                break;
            } else {
                System.out.println("Format file tidak didukung. Harus berakhir dengan .jpg, .jpeg, atau .png.");
            }
        }

        String outputPath = "../test/output/" + outputName;

        String format = outputName.substring(outputName.lastIndexOf('.') + 1);
        if (format.equals("jpeg")) {
            format = "jpg";
        }

        try {
            BufferedImage ogImage = ImageIO.read(inputFile);
            int ogSize = (int) new File(inputPath).length();
            
            long startTime = System.nanoTime();
            BufferedImage compressedImage = compressImage(ogImage, method, threshold, minBlockSize);
            long endTime = System.nanoTime();

            ImageIO.write(compressedImage, "png", new File(outputPath)); // no default extension yet
            int compressedSize = (int) new File(outputPath).length();

            double compressionRatio = (1 - (double) compressedSize / ogSize) * 100;
            double executionTime = (endTime - startTime) / 1e6;

            System.out.println();
            System.out.println("================================================================================");
            System.out.println("HASIL KOMPRESI GAMBAR");
            System.out.println("================================================================================");
            System.out.println("Kompresi gambar berhasil.");
            System.out.println("Waktu eksekusi: " + executionTime + " ms");
            System.out.println("Ukuran gambar asli: " + ogSize + " bytes");
            System.out.println("Ukuran gambar hasil kompresi: " + compressedSize + " bytes");
            System.out.printf("Persentase kompresi: %.2f%%\n", compressionRatio);
            System.out.println("Keadalaman pohon: " + maxDepth);
            System.out.println("Jumlah simpul pada pohon: " + nodeAmt);
            System.out.println("================================================================================");
            System.out.println("Gambar hasil kompresi disimpan di: " + outputPath);
            System.out.println("================================================================================");
            System.out.println();
        } catch (Exception e) {
            System.out.println();
            System.out.println("================================================================================");
            System.out.println("HASIL KOMPRESI GAMBAR");
            System.out.println("================================================================================");
            System.out.println("Gambar gagal dikompresi: " + e.getMessage());
            System.out.println();
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
            case 5: return calculateSSIM(img, x, y, w, h) >= threshold;
            default: return false;
        }
    }

    private static double calculateVariance(BufferedImage img, int x, int y, int w, int h) {
        long sumR = 0, sumG = 0, sumB = 0;
        int count = 0;
    
        for (int i = x; i < x + w; i++) {
            for (int j = y; j < y + h; j++) {
                Color c = new Color(img.getRGB(i, j));
                sumR += c.getRed();
                sumG += c.getGreen();
                sumB += c.getBlue();
                count++;
            }
        }
    
        double avgR = sumR / (double) count;
        double avgG = sumG / (double) count;
        double avgB = sumB / (double) count;
    
        double variance = 0.0;
        for (int i = x; i < x + w; i++) {
            for (int j = y; j < y + h; j++) {
                Color c = new Color(img.getRGB(i, j));
                variance += Math.pow(c.getRed() - avgR, 2);
                variance += Math.pow(c.getGreen() - avgG, 2);
                variance += Math.pow(c.getBlue() - avgB, 2);
            }
        }
    
        return variance / count; 
    }
    
    private static double calculateMAD(BufferedImage img, int x, int y, int w, int h) {
        int avgRGB = getAvgColor(img, x, y, w, h);
        Color avgColor = new Color(avgRGB);
    
        double totalDiff = 0;
        int count = 0;
    
        for (int i = x; i < x + w; i++) {
            for (int j = y; j < y + h; j++) {
                Color c = new Color(img.getRGB(i, j));
                int diff = Math.abs(c.getRed() - avgColor.getRed()) +
                           Math.abs(c.getGreen() - avgColor.getGreen()) +
                           Math.abs(c.getBlue() - avgColor.getBlue());
                totalDiff += diff;
                count++;
            }
        }
    
        return totalDiff / count;
    }
    
    private static double calculateMaxPixelDifference(BufferedImage img, int x, int y, int w, int h) {
        int avgRGB = getAvgColor(img, x, y, w, h);
        Color avgColor = new Color(avgRGB);
    
        int maxDiff = 0;
        for (int i = x; i < x + w; i++) {
            for (int j = y; j < y + h; j++) {
                Color c = new Color(img.getRGB(i, j)); 
                int diff = Math.abs(c.getRed() - avgColor.getRed()) +
                           Math.abs(c.getGreen() - avgColor.getGreen()) +
                           Math.abs(c.getBlue() - avgColor.getBlue());
                maxDiff = Math.max(maxDiff, diff);
            }
        }
    
        return maxDiff;
    }
    
    private static double calculateEntropy(BufferedImage img, int x, int y, int w, int h) {
        int[] histogram = new int[256];
        int total = 0;

        for (int i = x; i < x + w; i++) {
            for (int j = y; j < y + h; j++) {
                Color c = new Color(img.getRGB(i, j));
                int gray = (c.getRed() + c.getGreen() + c.getBlue()) / 3;
                histogram[gray]++;
                total++;
            }
        }

        double entropy = 0.0;
        for (int i = 0; i < 256; i++) {
            if (histogram[i] > 0) {
                double p = (double) histogram[i] / total;
                entropy -= p * (Math.log(p) / Math.log(2)); 
            }
        }

        return entropy;
    }

    private static double calculateSSIM(BufferedImage original, int x, int y, int w, int h) {
        int count = w * h;
        double C1 = 6.5025, C2 = 58.5225;
    
        Color avgColor = new Color(getAvgColor(original, x, y, w, h));
        
        double meanX = 0, meanY = 0;
        double varX = 0, varY = 0;
        double covXY = 0;
    
        for (int i = x; i < x + w; i++) {
            for (int j = y; j < y + h; j++) {
                Color c = new Color(original.getRGB(i, j));
                int r1 = c.getRed();
                int r2 = avgColor.getRed();
    
                meanX += r1;
                meanY += r2;
            }
        }
    
        meanX /= count;
        meanY /= count;
    
        for (int i = x; i < x + w; i++) {
            for (int j = y; j < y + h; j++) {
                Color c = new Color(original.getRGB(i, j));
                int r1 = c.getRed();
                int r2 = avgColor.getRed();
    
                varX += Math.pow(r1 - meanX, 2);
                varY += Math.pow(r2 - meanY, 2);
                covXY += (r1 - meanX) * (r2 - meanY);
            }
        }
    
        varX /= count;
        varY /= count;
        covXY /= count;
    
        double numerator = (2 * meanX * meanY + C1) * (2 * covXY + C2);
        double denominator = (meanX * meanX + meanY * meanY + C1) * (varX + varY + C2);
        return numerator / denominator;
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
