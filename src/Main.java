import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Scanner;
import javax.imageio.ImageIO;

// optimalize program for lower time + gif bonus not done 

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("========================================================================================================");
        printHeader();
        System.out.println("========================================================================================================");

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
        System.out.println("========================================================================================================");
        System.out.println("                                        METODE PENGUKURAN ERROR");
        System.out.println("========================================================================================================");
        System.out.println("1. Variance");
        System.out.println("2. Mean Absolute Deviation (MAD)");
        System.out.println("3. Max Pixel Difference");
        System.out.println("4. Entropy");
        System.out.println("5. Structural Similarity Index (SSIM)");
        System.out.println("========================================================================================================");
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
        while (true) {
            System.out.print("Masukkan nilai ambang batas (threshold): ");
            if (scanner.hasNextDouble()) {
                threshold = scanner.nextDouble();
            } else {
                System.out.println("Input harus berupa angka.");
                scanner.next(); 
            }

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
            if (scanner.hasNextInt()) {
                minBlockSize = scanner.nextInt();
                if (minBlockSize > 0) break;
                else {
                    System.out.println("Ukuran blok minimum harus lebih besar dari 0.");
                    System.out.println();
                }
            } else {
                System.out.println("Input harus berupa angka.");
                System.out.println();
                scanner.next(); 
            }
        }
        scanner.nextLine();

        double targetRatio = 0;
        double tolerance = 0; // for now dia adjust sedeket mungkin (0.3%)

        while (true) {
            System.out.print("Masukkan target rasio kompresi (0 jika tidak ingin menggunakan fitur ini): ");
            if (scanner.hasNextDouble()) {
                targetRatio = scanner.nextDouble();
                if (targetRatio < 0 || targetRatio > 1) {
                    System.out.println("Target rasio kompresi harus berada antara 0 dan 1.\n");
                } else if (targetRatio == 0) { 
                    break; // skip tolerance input
                } else {
                    tolerance = 0.003; // default tolerance
                        // while (true) {
                        //     System.out.print("Masukkan toleransi (0 jika ingin menggunakan default): ");
                        //     if (scanner.hasNextDouble()) {
                        //         tolerance = scanner.nextDouble();
                        //         if (tolerance < 0 || tolerance > 1) {
                        //             System.out.println("Toleransi harus berada antara 0 dan 1.\n");
                        //         } else {
                        //             if (tolerance == 0) {
                        //                 tolerance = 0.003; // default tolerance
                        //                 System.out.println("Toleransi default digunakan: 0.3%.\n");
                        //             }
                        //             break;
                        //         }
                        //     } else {
                        //         System.out.println("Input harus berupa angka.\n");
                        //         scanner.next();
                        //     }
                        // }
                    break;
                }
            } else {
                System.out.println("Input harus berupa angka.\n");
                scanner.next();
            }
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
            BufferedImage compressedImage;
            if (targetRatio == 0) {
                compressedImage = Compression.compressImage(ogImage, method, threshold, minBlockSize);
            } else {
                String format = outputPath.substring(outputPath.lastIndexOf('.') + 1).toLowerCase();
                compressedImage = Compression.compressWithTargetRatio(
                    ogImage,
                    method,
                    threshold, tolerance,
                    minBlockSize,
                    ogSize,
                    targetRatio,
                    format
                );
            }
            
            long endTime = System.nanoTime();

            String outputFormat = outputPath.substring(outputPath.lastIndexOf('.') + 1).toLowerCase();
            ImageIO.write(compressedImage, outputFormat, new File(outputPath));
            int compressedSize = (int) new File(outputPath).length();

            double compressionRatio = (1 - (double) compressedSize / ogSize) * 100;
            
            double executionTime = (endTime - startTime) / 1e6;

            System.out.println();
            System.out.println("========================================================================================================");
            System.out.println("                                        HASIL KOMPRESI GAMBAR");
            System.out.println("========================================================================================================");
            System.out.println("Kompresi gambar berhasil.");
            System.out.println("Waktu eksekusi: " + executionTime + " ms");
            System.out.println("Ukuran gambar asli: " + ogSize + " bytes");
            System.out.println("Ukuran gambar hasil kompresi: " + compressedSize + " bytes");
            System.out.printf("Persentase kompresi: %.2f%%\n", compressionRatio);
            if (compressionRatio < 0) {
                System.out.println("Gambar hasil kompresi memiliki ukuran lebih besar dari gambar asli.");
            }
            System.out.println("Keadalaman pohon: " + Compression.maxDepth);
            System.out.println("Jumlah simpul pada pohon: " + Compression.nodeAmt);
            System.out.println("========================================================================================================");
            System.out.println("Gambar hasil kompresi disimpan di: " + outputPath);
            System.out.println("========================================================================================================");
            System.out.println();
        } catch (Exception e) {
            System.out.println("Gambar gagal dikompres: " + e.getMessage());
        }
    }

    public static void printHeader() {
        System.out.println(" __  _   ___   ___ ___  ____  ____     ___  _____ ____       ____   ____  ___ ___  ____    ____  ____  ");
        System.out.println("|  |/ ] /   \\ |   |   ||    \\|    \\   /  _]/ ___/|    |     /    | /    ||   |   ||    \\  /    ||    \\ ");
        System.out.println("|  ' / |     || _   _ ||  o  )  D  ) /  [_(   \\_  |  |     |   __||  o  || _   _ ||  o  )|  o  ||  D  )");
        System.out.println("|    \\ |  O  ||  \\_/  ||   _/|    / |    _]\\__  | |  |     |  |  ||     ||  \\_/  ||     ||     ||    / ");
        System.out.println("|     \\|     ||   |   ||  |  |    \\ |   [_ /  \\ | |  |     |  |_ ||  _  ||   |   ||  O  ||  _  ||    \\ ");
        System.out.println("|  .  ||     ||   |   ||  |  |  .  \\|     |\\    | |  |     |     ||  |  ||   |   ||     ||  |  ||  .  \\");
        System.out.println("|__|\\_| \\___/ |___|___||__|  |__|\\_||_____| \\___||____|    |___,_||__|__||___|___||_____||__|__||__|\\_|");
        System.out.println("      _                           __  __     _           _        ___               _ _                   ");
        System.out.println("   __| |___ _ _  __ _ __ _ _ _   |  \\/  |___| |_ ___  __| |___   / _ \\ _  _ __ _ __| | |_ _ _ ___ ___     ");
        System.out.println("  / _` / -_) ' \\/ _` / _` | ' \\  | |\\/| / -_)  _/ _ \\/ _` / -_) | (_) | || / _` / _` |  _| '_/ -_) -_)    ");
        System.out.println("  \\__,_\\___|_||_\\__, \\__,_|_||_| |_|  |_\\___|\\__\\___/\\__,_\\___|  \\__\\_\\\\_,_\\__,_\\__,_|\\__|_| \\___\\___|    ");
        System.out.println("                 |___/                                                                                     ");
    }
}