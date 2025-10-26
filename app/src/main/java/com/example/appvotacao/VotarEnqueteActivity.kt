package com.example.appvotacao

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import com.example.appvotacao.databinding.ActivityVotarEnqueteBinding

class VotarEnqueteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVotarEnqueteBinding
    private lateinit var db: DatabaseHelper
    private lateinit var session: SessionManager
    private var enqueteId: Int = 0
    private var enqueteTitulo: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVotarEnqueteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DatabaseHelper(this)
        session = SessionManager(this)

        enqueteId = intent.getIntExtra("ENQUETE_ID", 0)
        enqueteTitulo = intent.getStringExtra("ENQUETE_TITULO") ?: ""

        binding.textTituloEnquete.text = enqueteTitulo

        carregarOpcoes()

        binding.btnVotar.setOnClickListener {
            votar()
        }

        binding.btnVoltar.setOnClickListener {
            startActivity(Intent(this, ListaEnquetesActivity::class.java))
            finish()
        }
    }

    private fun carregarOpcoes() {
        val opcoes = db.getOpcoesEnquete(enqueteId)
        val radioGroup = binding.radioGroupOpcoes
        radioGroup.removeAllViews()

        for (opcao in opcoes) {
            val radioButton = RadioButton(this)
            radioButton.text = opcao.texto
            radioButton.id = opcao.id
            radioButton.textSize = 16f
            radioButton.setPadding(0, 16, 0, 16)
            radioGroup.addView(radioButton)
        }
    }

    private fun votar() {
        val radioGroup = binding.radioGroupOpcoes
        val selectedId = radioGroup.checkedRadioButtonId

        if (selectedId == -1) {
            Toast.makeText(this, "Selecione uma opção para votar", Toast.LENGTH_SHORT).show()
            return
        }

        val sucesso = db.votarNaOpcao(selectedId)
        if (sucesso) {
            Toast.makeText(this, "Voto registrado com sucesso!", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, ListaEnquetesActivity::class.java))
            finish()
        } else {
            Toast.makeText(this, "Erro ao registrar voto", Toast.LENGTH_SHORT).show()
        }
    }
}