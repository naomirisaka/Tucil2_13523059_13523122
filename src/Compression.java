import java.awt.image.BufferedImage;

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

    private static void quadtreeCompress(BufferedImage original, BufferedImage output, int x, int y, int sizeX, int sizeY, int depth, int method, double threshold, int minSize) {
        nodeAmt++;
        maxDepth = Math.max(maxDepth, depth);

        if (sizeX <= minSize || sizeY <= minSize || shouldMerge(original, x, y, sizeX, sizeY, method, threshold)) {
            int avgColor = ErrorEvaluation.getAvgColor(original, x, y, sizeX, sizeY);
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
            case 1: return ErrorEvaluation.calculateVariance(img, x, y, w, h) <= threshold;
            case 2: return ErrorEvaluation.calculateMAD(img, x, y, w, h) <= threshold;
            case 3: return ErrorEvaluation.calculateMaxPixelDifference(img, x, y, w, h) <= threshold;
            case 4: return ErrorEvaluation.calculateEntropy(img, x, y, w, h) <= threshold;
            case 5: return ErrorEvaluation.calculateSSIM(img, x, y, w, h) <= threshold;
            default: return false;
        }
    }

    private static void fillBlock(BufferedImage img, int x, int y, int w, int h, int color) {
        for (int i = x; i < x + w; i++) {
            for (int j = y; j < y + h; j++) {
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
