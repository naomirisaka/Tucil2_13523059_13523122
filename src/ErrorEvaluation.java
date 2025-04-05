import java.awt.Color;
import java.awt.image.BufferedImage;

public class ErrorEvaluation {
    public static int getAvgColor(BufferedImage img, int x, int y, int w, int h) {
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
    
    public static double calculateVariance(BufferedImage img, int x, int y, int w, int h) {
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
    
    public static double calculateMAD(BufferedImage img, int x, int y, int w, int h) {
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
    
    public static double calculateMaxPixelDifference(BufferedImage img, int x, int y, int w, int h) {
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
    
    public static double calculateEntropy(BufferedImage img, int x, int y, int w, int h) {
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

    public static double calculateSSIM(BufferedImage original, int x, int y, int w, int h) {
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
}