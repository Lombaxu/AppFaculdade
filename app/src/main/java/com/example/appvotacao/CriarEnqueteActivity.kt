package com.example.appvotacao

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.appvotacao.databinding.ActivityCriarEnqueteBinding

class CriarEnqueteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCriarEnqueteBinding
    private lateinit var db: DatabaseHelper
    private lateinit var session: SessionManager
    private val opcoesList = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCriarEnqueteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        android.util.Log.d("DEBUG_ACTIVITY", "CriarEnqueteActivity iniciada")

        try {
            db = DatabaseHelper(this)
            session = SessionManager(this)
            android.util.Log.d("DEBUG_ACTIVITY", "Database e Session criados")

        } catch (e: Exception) {
            android.util.Log.e("DEBUG_ACTIVITY", "Erro ao criar database: ${e.message}")
            Toast.makeText(this, "Erro no banco: ${e.message}", Toast.LENGTH_LONG).show()
        }

        binding.btnAdicionarOpcao.setOnClickListener {
            val novaOpcao = binding.editNovaOpcao.text.toString().trim()
            android.util.Log.d("DEBUG_ACTIVITY", "Botão adicionar clicado: $novaOpcao")
            if (novaOpcao.isNotEmpty()) {
                opcoesList.add(novaOpcao)
                atualizarListaOpcoes()
                binding.editNovaOpcao.text.clear()
                android.util.Log.d("DEBUG_ACTIVITY", "Opção adicionada. Total: ${opcoesList.size}")
            } else {
                Toast.makeText(this, "Digite uma opção", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnCriarEnquete.setOnClickListener {
            android.util.Log.d("DEBUG_ACTIVITY", "Botão criar enquete clicado")

            val titulo = binding.editTituloEnquete.text.toString().trim()
            android.util.Log.d("DEBUG_ACTIVITY", "Título: $titulo, Opções: ${opcoesList.size}")

            if (titulo.isEmpty()) {
                Toast.makeText(this, "Digite um título", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (opcoesList.size < 2) {
                Toast.makeText(this, "Adicione pelo menos 2 opções", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val usuarioEmail = session.getUsuarioLogado()
            android.util.Log.d("DEBUG_ACTIVITY", "Usuário logado: $usuarioEmail")

            if (usuarioEmail != null) {
                try {
                    android.util.Log.d("DEBUG_ACTIVITY", "Chamando db.criarEnquete...")
                    val sucesso = db.criarEnquete(titulo, opcoesList, usuarioEmail)
                    android.util.Log.d("DEBUG_ACTIVITY", "Resultado da criação: $sucesso")

                    if (sucesso) {
                        Toast.makeText(this, "Enquete criada com sucesso!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, VotacaoActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, "Erro ao criar enquete", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    android.util.Log.e("DEBUG_ACTIVITY", "Erro ao criar enquete: ${e.message}")
                    Toast.makeText(this, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(this, "Usuário não logado", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnVoltar.setOnClickListener {
            startActivity(Intent(this, VotacaoActivity::class.java))
            finish()
        }
    }

    private fun atualizarListaOpcoes() {
        val textoOpcoes = opcoesList.joinToString("\n") { "• $it" }
        binding.txtOpcoesAdicionadas.text = "Opções adicionadas:\n$textoOpcoes"
    }
}