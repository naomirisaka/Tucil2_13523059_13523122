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
        Color avgColor = new Color(getAvgColor(original, x, y, w, h));

        double ssimR = calculateSSIMChannel(original, avgColor.getRed(), x, y, w, h, 'r');
        double ssimG = calculateSSIMChannel(original, avgColor.getGreen(), x, y, w, h, 'g');
        double ssimB = calculateSSIMChannel(original, avgColor.getBlue(), x, y, w, h, 'b');

        return 0.2989 * ssimR + 0.5870 * ssimG + 0.1140 * ssimB; // apply weights for RGB channels based on luminosity
    }
    
    private static double calculateSSIMChannel(BufferedImage img, int refValue, int x, int y, int w, int h, char channel) {
        int count = w * h;
        double L = 255.0;
        double K1 = 0.01;
        double K2 = 0.03;
        double C1 = Math.pow(K1 * L, 2);
        double C2 = Math.pow(K2 * L, 2);
    
        double meanX = 0, meanY = refValue;
        double varX = 0, varY = 0, covXY = 0;
    
        for (int i = x; i < x + w; i++) {
            for (int j = y; j < y + h; j++) {
                Color c = new Color(img.getRGB(i, j));
                int val = switch (channel) {
                    case 'r' -> c.getRed();
                    case 'g' -> c.getGreen();
                    case 'b' -> c.getBlue();
                    default -> 0;
                };
    
                meanX += val;
            }
        }
    
        meanX /= count;
    
        for (int i = x; i < x + w; i++) {
            for (int j = y; j < y + h; j++) {
                Color c = new Color(img.getRGB(i, j));
                int val = switch (channel) {
                    case 'r' -> c.getRed();
                    case 'g' -> c.getGreen();
                    case 'b' -> c.getBlue();
                    default -> 0;
                };
    
                varX += Math.pow(val - meanX, 2);
                varY += Math.pow(refValue - meanY, 2); 
                covXY += (val - meanX) * (refValue - meanY);
            }
        }
    
        varX /= count;
        varY = 0; 
        covXY /= count;
    
        double numerator = (2 * meanX * meanY + C1) * (2 * covXY + C2);
        double denominator = (meanX * meanX + meanY * meanY + C1) * (varX + varY + C2);
        return numerator / denominator;
    }    
}