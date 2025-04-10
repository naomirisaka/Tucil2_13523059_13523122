# Tugas Kecil 2 IF2211 Strategi Algoritma
### Kompresi Gambar Dengan Metode Quadtree 
| Nama | NIM |
|------|-----|
| Jessica Allen | 13523059 |
| Naomi Risaka Sitorus | 13523122 |

## Deskripsi
Program ini mampu melakukan kompresi gambar dengan metode quadtree yang menerapkan pendekatan divide and conquer. Secara rekursif, blok gambar dibagi menjadi 4 upa-blok hingga ukurannya mencapai ukuran blok minimum atau blok dianggap homogen sebab nilai keseragaman warnanya di bawah threshold. Nilai keseragaman warna gambar diperoleh dari salah satu dari 5 metode pengukuran error RGB, yaitu variance, mean absolute deviation (MAD), maximum pixel difference, entropy, atau structural similarity index measure (SSIM). Program ini juga dapat melakukan kompresi gambar sesuai target rasio yang diinginkan pengguna di mana threshold disesuaikan secara otomatis. Program ini menghasilkan keluaran berupa gambar hasil kompresi serta GIF proses pembentukan quadtree untuk kompresi (opsional). 

## Cara Menjalankan
> [!NOTE]
> Untuk menjalankan program diperlukan IDE yang mendukung bahasa Java serta terinstalasi JDK 8 atau versi yang lebih baru.
1. Clone repository ini dengan menjalankan perintah di bawah ini pada terminal IDE:
   ```sh
   git clone https://github.com/naomirisaka/Tucil2_13523059_13523122.git
2. Buka folder hasil clone di IDE.
3. Pindah ke directory source code dengan:
   ```sh
   cd src
4. Jalankan program utama dengan:
   ```
   javac -d ../bin Main.java
   java -cp ../bin Main
   ```
   
## Cara Menggunakan
1. Setelah menjalankan program, masukkan nama gambar masukan berupa alamat absolut yang ingin dikompresi.
2. Masukkan parameter untuk kompresi gambar.
3. Masukkan nama file keluaran berupa alamat absolut hasil kompresi. 
4. Masukkan pilihan Anda ketika program menawarkan untuk menyimpan solusi dalam bentuk GIF.
5. Jika gambar berhasil dikompresi, program akan menampilkan lokasi hasil kompresi tersebut di terminal.
6. Jika gambar tidak berhasil dikompresi, program akan menampilkan pesan error.
