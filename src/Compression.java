import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class Compression {
    private static int nodeAmt = 0;
    private static int maxDepth = 0;

    public static BufferedImage compressImage(BufferedImage img, int method, double threshold, int minSize) {
        nodeAmt = 0;
        maxDepth = 0;

        int width = img.getWidth();
        int height = img.getHeight();
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        quadtreeCompress(img, result, 0, 0, width, height, 0, method, threshold, minSize);
        return result;
    }

    public static BufferedImage compressWithTargetRatio(
        BufferedImage original, int method, double ogThreshold, double tolerance, int minBlockSize, long ogSize,
        double targetRatio, String outputFormat) 
    {
        double low = method == 5 ? 0.0 : 0.0;
        double high = method == 5 ? 1.0 : Math.max(ogThreshold * 5, 500);
        double bestThreshold = ogThreshold;
        BufferedImage bestImage = null;

        double bestRatioDiff = Double.MAX_VALUE;
        int maxIter = 30; // limit to avoid infinite loop

        for (int i = 0; i < maxIter; i++) {
            double mid = (low + high) / 2;
            BufferedImage tempImage = compressImage(original, method, mid, minBlockSize);

            try {
                File tempFile = File.createTempFile("temp_compressed", "." + outputFormat);
                ImageIO.write(tempImage, outputFormat, tempFile);
                long tempSize = tempFile.length();
                tempFile.delete();

                double achievedRatio = 1.0 - (double) tempSize / ogSize;
                double ratioDiff = Math.abs(achievedRatio - targetRatio);

                if (ratioDiff < bestRatioDiff) {
                    bestRatioDiff = ratioDiff;
                    bestThreshold = mid;
                    bestImage = tempImage;
                        // if (ratioDiff <= tolerance) {
                        //     System.out.println("Target rasio kompresi dicapai dengan toleransi ±" + (tolerance * 100) + "%.");
                        //     break;
                        // }
                }

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

        System.out.printf("Threshold disesuaikan ke: %.4f\n", bestThreshold);
        return bestImage;
    }

    private static void quadtreeCompress(BufferedImage original, BufferedImage output, int x, int y, int sizeX, int sizeY, int depth, int method, double threshold, int minSize) {
        nodeAmt++;
        maxDepth = Math.max(maxDepth, depth);

        if (sizeX <= minSize || sizeY <= minSize || shouldMerge(original, x, y, sizeX, sizeY, method, threshold)) {
            int avgColor = ErrorEvaluation.getAvgColor(original, x, y, sizeX, sizeY);
            fillBlock(output, x, y, sizeX, sizeY, avgColor);
        } else {
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

    private static boolean shouldMerge(BufferedImage img, int x, int y, int w, int h, int method, double threshold) {
        switch (method) {
            case 1: return ErrorEvaluation.calculateVariance(img, x, y, w, h) <= threshold;
            case 2: return ErrorEvaluation.calculateMAD(img, x, y, w, h) <= threshold;
            case 3: return ErrorEvaluation.calculateMaxPixelDifference(img, x, y, w, h) <= threshold;
            case 4: return ErrorEvaluation.calculateEntropy(img, x, y, w, h) <= threshold;
            case 5: return ErrorEvaluation.calculateSSIM(img, x, y, w, h) >= threshold;
            default: return false;
        }
    }

    private static void fillBlock(BufferedImage img, int x, int y, int w, int h, int color) {
        for (int i = x; i < x + w && i < img.getWidth(); i++) {
            for (int j = y; j < y + h && j < img.getHeight(); j++) {
                img.setRGB(i, j, color);
            }
        }        
    }

    public static int getNodeCount() {
        return nodeAmt;
    }

    public static int getMaxDepth() {
        return maxDepth;
    }
}
