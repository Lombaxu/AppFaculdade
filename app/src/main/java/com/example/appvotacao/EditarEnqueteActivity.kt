package com.example.appvotacao

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.appvotacao.databinding.ActivityEditarEnqueteBinding

class EditarEnqueteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditarEnqueteBinding
    private lateinit var db: DatabaseHelper
    private lateinit var session: SessionManager
    private var enqueteId: Int = 0
    private val opcoesList = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditarEnqueteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DatabaseHelper(this)
        session = SessionManager(this)

        enqueteId = intent.getIntExtra("ENQUETE_ID", 0)

        carregarDadosEnquete()

        binding.btnAdicionarOpcao.setOnClickListener {
            val novaOpcao = binding.editNovaOpcao.text.toString().trim()
            if (novaOpcao.isNotEmpty()) {
                opcoesList.add(novaOpcao)
                atualizarListaOpcoes()
                binding.editNovaOpcao.text.clear()
            } else {
                Toast.makeText(this, "Digite uma opção", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnSalvarEdicao.setOnClickListener {
            salvarEdicao()
        }

        binding.btnVoltar.setOnClickListener {
            finish()
        }
    }

    private fun carregarDadosEnquete() {
        val enquete = db.getEnquetePorId(enqueteId)
        val opcoes = db.getOpcoesEnquete(enqueteId)

        if (enquete != null) {
            binding.editTituloEnquete.setText(enquete.titulo)
            opcoesList.clear()
            opcoesList.addAll(opcoes.map { it.texto })
            atualizarListaOpcoes()
        }
    }

    private fun atualizarListaOpcoes() {
        val textoOpcoes = opcoesList.joinToString("\n") { "• $it" }
        binding.txtOpcoesAdicionadas.text = "Opções:\n$textoOpcoes"
    }

    private fun salvarEdicao() {
        val novoTitulo = binding.editTituloEnquete.text.toString().trim()

        if (novoTitulo.isEmpty()) {
            Toast.makeText(this, "Digite um título", Toast.LENGTH_SHORT).show()
            return
        }

        if (opcoesList.size < 2) {
            Toast.makeText(this, "Mantenha pelo menos 2 opções", Toast.LENGTH_SHORT).show()
            return
        }

        val sucesso = db.atualizarEnquete(enqueteId, novoTitulo, opcoesList)
        if (sucesso) {
            Toast.makeText(this, "Enquete atualizada com sucesso!", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Erro ao atualizar enquete", Toast.LENGTH_SHORT).show()
        }
    }
}