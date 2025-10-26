package com.example.appvotacao

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.appvotacao.databinding.ActivityVotacaoBinding

class VotacaoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVotacaoBinding
    private lateinit var db: DatabaseHelper
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVotacaoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DatabaseHelper(this)
        session = SessionManager(this)

        binding.btnCriarEnquete.setOnClickListener {
            startActivity(Intent(this, CriarEnqueteActivity::class.java))
        }

        binding.btnVerEnquetes.setOnClickListener {
            startActivity(Intent(this, ListaEnquetesActivity::class.java))
        }

        binding.btnPerfil.setOnClickListener {
            startActivity(Intent(this, PerfilActivity::class.java))
        }

        binding.btnSair.setOnClickListener {
            session.logout()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}