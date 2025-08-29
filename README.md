# 📚 Sistem Library

Sistem Library adalah aplikasi perpustakaan digital untuk mengelola **anggota (member)**, **koleksi buku**, **peminjaman & pengembalian**, serta **pengelolaan denda**.  
Tujuannya untuk mempermudah administrasi perpustakaan sekaligus meningkatkan pengalaman pengguna dalam meminjam buku.

---

## 🚀 Fitur Utama

### Manajemen Anggota (User & Member)
- **Registrasi Anggota:** Pendaftaran anggota baru dengan validasi data lengkap
- **Login & Autentikasi:** Sistem login berbasis JWT / session
- **Profil Anggota:** Pengelolaan data pribadi, riwayat peminjaman, dan status keanggotaan
- **Validasi Data:** Constraint database & pengecekan duplikasi

---

### Manajemen Buku
- **Katalog Buku:** Pencarian & filter berdasarkan judul, penulis, kategori, atau tahun terbit
- **Detail Buku:** Informasi lengkap termasuk ketersediaan eksemplar
- **Pengelolaan Koleksi:** CRUD untuk admin (tambah, ubah, hapus buku)
- **Status Buku:** Tersedia, Dipinjam, Hilang, atau Rusak

---

### Peminjaman & Pengembalian
- **Peminjaman Buku:** Anggota dapat meminjam buku sesuai limit keanggotaan
- **Pengembalian Buku:** Validasi keterlambatan & status buku
- **Notifikasi:** Pengingat jadwal pengembalian melalui email / dashboard
- **Riwayat Transaksi:** Catatan lengkap peminjaman & pengembalian

---

### Laporan & Denda
- **Denda Keterlambatan:** Perhitungan otomatis berdasarkan aturan perpustakaan
- **Laporan Denda:** Ringkasan denda per anggota / per periode
- **Pembayaran Denda:** Opsi pembayaran tunai / online dengan pencatatan transaksi
- **Integrasi Laporan:** Grafik & export data (PDF/Excel) untuk admin

---

### User Management & RBAC
- **Manajemen User:** Admin, Petugas, dan Member
- **Role & Permission:** Hak akses granular untuk setiap peran
- **Admin:** Mengelola user, buku, laporan, dan pembayaran
- **Petugas:** Mengelola peminjaman, pengembalian, dan validasi denda
- **Member:** Login, meminjam buku, melihat riwayat, dan membayar denda
- **Security:** Password hashing dengan BCrypt, account locking, failed login tracking

---

## 📊 Ringkasan Fitur
- 🔑 Login & autentikasi member
- 📖 Manajemen buku & katalog digital
- 📦 Peminjaman & pengembalian buku
- 💰 Laporan & pembayaran denda
- 🛡️ User role management (Admin, Petugas, Member)

---

## 🧪 Testing & Development
- Unit test & integration test tersedia di folder `/tests`
- Jalankan test dengan:

# Commit Convention

Agar riwayat commit lebih rapi, mudah dibaca, dan konsisten, gunakan aturan commit berikut.

---

## Format Pesan Commit


### Aturan:
1. **type** → Jenis perubahan (lihat daftar di bawah).
2. **scope** (opsional) → Bagian spesifik dari project yang terpengaruh (misal: `api`, `auth`, `db`).
3. **short summary** → Ringkasan singkat, diawali huruf kecil, maksimal ±50 karakter.

---

## Daftar `type`

| Type       | Deskripsi                                                                 |
|------------|---------------------------------------------------------------------------|
| **feat**   | Penambahan fitur baru                                                     |
| **fix**    | Perbaikan bug                                                             |
| **docs**   | Perubahan atau penambahan dokumentasi                                     |
| **style**  | Perubahan tampilan/format (spasi, indentasi, dll) tanpa mengubah logika   |
| **refactor** | Perubahan kode tanpa menambah fitur atau memperbaiki bug                |
| **perf**   | Peningkatan performa                                                      |
| **test**   | Penambahan atau perbaikan unit test                                       |
| **build**  | Perubahan build system atau dependency                                    |
| **ci**     | Perubahan konfigurasi CI/CD                                               |
| **chore**  | Perubahan kecil lain (misal update .gitignore)                            |
| **revert** | Membatalkan commit sebelumnya                                             |

---

## Contoh Commit

 - feat(auth): tambah fitur login dengan JWT
 - fix(api): perbaiki error 500 saat create user
 - docs(readme): update cara menjalankan project
 - style(ui): perbaiki padding pada halaman dashboard
 - refactor(service): pisahkan logika validasi dari service