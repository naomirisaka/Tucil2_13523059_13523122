import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Scanner;
import javax.imageio.ImageIO;

// to do: threshold validation, threshold validation per method, compression ratio validation (biar ga minus)
public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("================================================================================");
        System.out.println("                           Program Kompresi Gambar");
        System.out.println("                           dengan Quadtree Method");
        System.out.println("================================================================================");

        String inputPath;
        while (true) {
            System.out.println();
            System.out.print("Masukkan nama file gambar (diakhiri dengan .jpg, .jpeg, atau .png): ");
            inputPath = scanner.nextLine();
            if (inputPath.toLowerCase().endsWith(".jpg") || inputPath.toLowerCase().endsWith(".jpeg") || 
                inputPath.toLowerCase().endsWith(".png")) {
                break;
            } else {
                System.out.println("Format file tidak didukung. Harus diakhiri dengan .jpg, .jpeg, atau .png.");
            }
        }

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
                if (method >= 1 && method <= 5) break;
                else System.out.println("Masukkan angka antara 1 sampai 5.");
            } else {
                System.out.println("Input harus berupa angka.");
                scanner.next(); 
            }
        }

        System.out.println();
        System.out.print("Masukkan nilai ambang batas (threshold): ");
        double threshold = scanner.nextDouble();

        int minBlockSize = 0;
        while (true) {
            System.out.print("Masukkan ukuran blok minimum: ");
            minBlockSize = scanner.nextInt();
            if (minBlockSize > 0) break;
            else System.out.println("Ukuran blok minimum harus lebih besar dari 0.");
        }

        scanner.nextLine();

        String outputPath;
        while (true) {
            System.out.print("Masukkan nama file hasil kompresi (diakhiri dengan .jpg, .jpeg, atau .png): ");
            outputPath = scanner.nextLine().trim().toLowerCase();
            if (outputPath.endsWith(".jpg") || outputPath.endsWith(".jpeg") || outputPath.endsWith(".png")) break;
            else System.out.println("Format file tidak didukung. Harus berakhir dengan .jpg, .jpeg, atau .png.");
        }

        try {
            BufferedImage ogImage = ImageIO.read(inputFile);
            int ogSize = (int) inputFile.length();
            
            long startTime = System.nanoTime();
            BufferedImage compressedImage = Compression.compressImage(ogImage, method, threshold, minBlockSize);
            long endTime = System.nanoTime();

            ImageIO.write(compressedImage, "png", new File(outputPath));
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
            System.out.println("Keadalaman pohon: " + Compression.getMaxDepth());
            System.out.println("Jumlah simpul pada pohon: " + Compression.getNodeCount());
            System.out.println("================================================================================");
            System.out.println("Gambar hasil kompresi disimpan di: " + outputPath);
            System.out.println("================================================================================");
            System.out.println();
        } catch (Exception e) {
            System.out.println("Gambar gagal dikompresi: " + e.getMessage());
        }
    }
}
