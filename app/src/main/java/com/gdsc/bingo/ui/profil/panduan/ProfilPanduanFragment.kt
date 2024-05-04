package com.gdsc.bingo.ui.profil.panduan

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.gdsc.bingo.databinding.FragmentProfilPanduanBinding
import com.gdsc.bingo.services.textstyling.AddOnSpannableTextStyle

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
            
            <h3>Daftar atau log in akun Bin-Go</h3>
            <ol>
            <li>Buka aplikasi Bin-Go</li>
            <li>Pergi ke halaman Profil melalui pintasan pada navigation bar</li>
            <li>Pilih ikon akun pada bagian atas halaman</li>
            <li>Pilih menu daftar apabila belum memiliki akun atau pilih menu login apabila sudah memiliki akun</li>
            <li>Daftar atau masuk dengan e-mail aktif. Jika mendaftar, pastikan semua kolom (nama pengguna, e-mail, kata sandi) terisi</li>
            <li>Klik daftar jika mendaftar atau klik log in jika masuk dengan akun yang sudah ada</li>
            </ol>
            
            <h3>Menggunakan fitur Bin-Locator</h3>
            <ol>
            <li>Pastikan GPS Anda sudah aktif</li>
            <li>Pergi ke halaman Bin-Locator melalui card di halaman beranda atau pintasan di navigation bar</li>
            <li>Pilih menu Bin-Locator di bagian atas layar</li>
            <li>Navigasi tempat sampah terdekat berdasarkan GPS Anda atau cari tempat sampah dengan fitur search di bagian atas halaman</li>
            <li>Pilih ikon tempat sampah yang tersedia untuk melihat keterangan lebih lanjut mengenai tempat sampah, meliputi nama tempat, alamat, keterangan jam buka, rating, dan rute</li>
            <li>Tekan tombol rute untuk pergi ke Google Maps dan navigasi arah menuju tempat sampah</li>
            </ol>
            
            <h3>Memposting pengetahuan di Forum</h3>
            <ol>
            <li>Pastikan Anda sudah log in atau mendaftar</li>
            <li>Tekan pilihan "berbagi ide dan pengalaman" di halaman beranda atau pergi ke halaman Forum melalui pintasan di navigation bar dan tekan tombol bertanda plus</li>
            <li>Pilih tipe informasi yang ingin dibagikan (Bin-Tricks untuk tips dan trik mengolah sampah atau Bin-Learn untuk informasi umum) kemudian isi judul dan detail postingan. Ceritakan pengalamanmu atau bag
            ikan idemu dalam menangani sampah. Tambahkan media gambar atau video melalui link youtube bila perlu</li>
            <li>Tekan tombol bertanda silang di pojok kiri atas untuk membatalkan atau tombol bertanda centang di pojok kanan atas untuk memposting</li>
            </ol>
            
            <h3>Melapor masalah sampah melalui Bin-Report</h3>
            <ol>
            <li>Pastikan Anda sudah log in atau mendaftar</li>
            <li>Tekan pilihan untuk melapor dengan simbol pengeras suara di halaman beranda atau pergi ke halaman Forum melalui pintasan di navigation bar dan tekan tombol kecil dengan simbol pengeras suara</li>
            <li>Pilih titik lokasi dari google maps dan tekan simpan kemudian isi judul dan detail laporan. Ceritakan masalahmu dan tambahkan gambar bila perlu</li>
            <li>Tekan tombol bertanda silang di pojok kiri atas untuk membatalkan atau tombol bertanda centang di pojok kanan atas untuk memposting laporan</li>
            </ol>
            
            <h3>Melihat Bin-Points</h3>
            <ol>
            <li>Dalam sistem Bin-Go, Anda akan mendapat Bin-Points setelah memposting, melapor, atau menyelesaikan permasalahan sampah</li>
            <li>Pastikan Anda sudah log in atau mendaftar untuk mendapatkan Bin-Points</li>
            <li>Cek Bin-Points melalui menu Bin-Points di halaman beranda atau pergi ke halaman Profil melalui pintasan di navigation bar kemudian memilih menu Bin-Points di bawah informasi akun Anda</li>
            <li>Cek Bin-Points dan peringkat Anda di leaderboard. Pastikan Anda selalu berada di peringkat teratas untuk mendapatkan hadiah menarik</li>
            </ol>
            
            """.trimIndent()



        val spannableConverter = AddOnSpannableTextStyle()

        val spanned = spannableConverter.convertHtmlWithOrderedList(panduanText )

        binding.textViewPanduan.text = spanned
        val typeface = Typeface.DEFAULT
        binding.textViewPanduan.typeface = typeface

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}