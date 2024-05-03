package com.gdsc.bingo.ui.profil.panduan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import com.gdsc.bingo.databinding.FragmentProfilPanduanBinding

class ProfilPanduanFragment : Fragment() {

    private var _binding: FragmentProfilPanduanBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfilPanduanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val panduanText = """
            <br><br>
            <div style="text-align: center;"><h2><b>Panduan Penggunaan Bin-Go</b></h2></div>
            <br><br>
            <b>Daftar atau log in akun Bin-Go</b><br>
            1. Buka aplikasi Bin-Go<br>
            2. Pergi ke halaman Profil melalui pintasan pada navigation bar<br>
            3. Pilih ikon akun pada bagian atas halaman<br>
            4. Pilih menu daftar apabila belum memiliki akun atau pilih menu login apabila sudah memiliki akun<br>
            5. Daftar atau masuk dengan e-mail aktif. Jika mendaftar, pastikan semua kolom (nama pengguna, e-mail, kata sandi) terisi<br>
            6. Klik daftar jika mendaftar atau klik log in jika masuk dengan akun yang sudah ada<br><br>
            
            <b>Menggunakan fitur Bin-Locator</b><br>
            1. Pastikan GPS Anda sudah aktif<br>
            2. Pergi ke halaman Bin-Locator melalui card di halaman beranda atau pintasan di navigation bar<br>
            3. Pilih menu Bin-Locator di bagian atas layar<br>
            4. Navigasi tempat sampah terdekat berdasarkan GPS Anda atau cari tempat sampah dengan fitur search di bagian atas halaman<br>
            5. Pilih ikon tempat sampah yang tersedia untuk melihat keterangan lebih lanjut mengenai tempat sampah, meliputi nama tempat, alamat, keterangan jam buka, rating, dan rute<br>
            6. Tekan tombol rute untuk pergi ke Google Maps dan navigasi arah menuju tempat sampah<br><br>
            
            <b>Memposting pengetahuan di Forum</b><br>
            1. Pastikan Anda sudah log in atau mendaftar<br>
            2. Tekan pilihan "berbagi ide dan pengalaman" di halaman beranda atau pergi ke halaman Forum melalui pintasan di navigation bar dan tekan tombol bertanda plus<br>
            3. Pilih tipe informasi yang ingin dibagikan (Bin-Tricks untuk tips dan trik mengolah sampah atau Bin-Learn untuk informasi umum) kemudian isi judul dan detail postingan. Ceritakan pengalamanmu atau bagikan idemu dalam menangani sampah. Tambahkan media gambar atau video melalui link youtube bila perlu<br>
            4. Tekan tombol bertanda silang di pojok kiri atas untuk membatalkan atau tombol bertanda centang di pojok kanan atas untuk memposting<br><br>
            
            <b>Melapor masalah sampah melalui Bin-Report</b><br>
            1. Pastikan Anda sudah log in atau mendaftar<br>
            2. Tekan pilihan untuk melapor dengan simbol pengeras suara di halaman beranda atau pergi ke halaman Forum melalui pintasan di navigation bar dan tekan tombol kecil dengan simbol pengeras suara<br>
            3. Pilih titik lokasi dari google maps dan tekan simpan kemudian isi judul dan detail laporan. Ceritakan masalahmu dan tambahkan gambar bila perlu<br>
            4. Tekan tombol bertanda silang di pojok kiri atas untuk membatalkan atau tombol bertanda centang di pojok kanan atas untuk memposting laporan<br><br>
            
            <b>Melihat Bin-Points</b><br>
            1. Dalam sistem Bin-Go, Anda akan mendapat Bin-Points setelah memposting, melapor, atau menyelesaikan permasalahan sampah<br>
            2. Pastikan Anda sudah log in atau mendaftar untuk mendapatkan Bin-Points<br>
            3. Cek Bin-Points melalui menu Bin-Points di halaman beranda atau pergi ke halaman Profil melalui pintasan di navigation bar kemudian memilih menu Bin-Points di bawah informasi akun Anda<br>
            4. Cek Bin-Points dan peringkat Anda di leaderboard. Pastikan Anda selalu berada di peringkat teratas untuk mendapatkan hadiah menarik<br>
            """.trimIndent()

        binding.textViewPanduan.text = HtmlCompat.fromHtml(panduanText, HtmlCompat.FROM_HTML_MODE_LEGACY)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}