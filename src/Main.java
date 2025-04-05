import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Scanner;
import javax.imageio.ImageIO;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("================================================================================");
        System.out.println("                           Program Kompresi Gambar");
        System.out.println("                           dengan Quadtree Method");
        System.out.println("================================================================================");

        File inputFile = null;
        String inputPath = "";

        while (true) {
            System.out.println();
            System.out.print("Masukkan nama file gambar (diakhiri dengan .jpg, .jpeg, atau .png): ");
            inputPath = scanner.nextLine().trim();
            
            if (!(inputPath.toLowerCase().endsWith(".jpg") || inputPath.toLowerCase().endsWith(".jpeg") || inputPath.toLowerCase().endsWith(".png"))) {
                System.out.println("Format file tidak didukung. Harus diakhiri dengan .jpg, .jpeg, atau .png.");
                continue;
            }

            inputFile = new File(inputPath);
            if (!inputFile.exists()) {
                System.out.println("File input tidak ditemukan: " + inputPath);
                continue;
            }
            break;
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
            System.out.println();
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

        double threshold = 0;
        while (true) {
            System.out.print("Masukkan nilai ambang batas (threshold): ");
            threshold = scanner.nextDouble();

            boolean isValid = true;

            switch (method) {
                case 1:
                    if (threshold < 0) {
                        System.out.println("Threshold metode ini tidak boleh bernilai negatif.");
                        System.out.println();
                        isValid = false;
                    }
                    break;
                case 2:
                case 3:
                    if (threshold < 0 || threshold > 255) {
                        System.out.println("Threshold metode ini harus bernilai antara 0 sampai 255.");
                        System.out.println();
                        isValid = false;
                    }
                    break;
                case 4:
                    if (threshold < 0 || threshold > 8) {
                        System.out.println("Threshold metode ini harus bernilai antara 0 sampai 8.");
                        System.out.println();
                        isValid = false;
                    }
                    break;
                case 5:
                    if (threshold < 0 || threshold > 1) {
                        System.out.println("Threshold metode ini harus bernilai antara 0 sampai 1.");
                        System.out.println();
                        isValid = false;
                    }
                    break;
            }
            if (isValid) break;
        }

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
            outputPath = scanner.nextLine().trim();
            if (outputPath.toLowerCase().endsWith(".jpg") || outputPath.toLowerCase().endsWith(".jpeg") || outputPath.toLowerCase().endsWith(".png")) break;
            else System.out.println("Format file tidak didukung. Harus berakhir dengan .jpg, .jpeg, atau .png.\n");
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
            if (compressionRatio < 0) {
                System.out.println("Gambar hasil kompresi memiliki ukuran lebih besar dari gambar asli.");
            }
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
