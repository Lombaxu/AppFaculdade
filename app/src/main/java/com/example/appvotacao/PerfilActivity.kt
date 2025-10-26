package com.example.appvotacao

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.appvotacao.databinding.ActivityPerfilBinding

class PerfilActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPerfilBinding
    private lateinit var db: DatabaseHelper
    private lateinit var session: SessionManager
    private var usuarioAtual: Usuario? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPerfilBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DatabaseHelper(this)
        session = SessionManager(this)

        carregarDadosUsuario()

        binding.btnSalvar.setOnClickListener {
            salvarAlteracoes()
        }

        binding.btnVoltar.setOnClickListener {
            startActivity(Intent(this, VotacaoActivity::class.java))
            finish()
        }
    }

    private fun carregarDadosUsuario() {
        val emailLogado = session.getUsuarioLogado()
        if (emailLogado != null) {
            usuarioAtual = db.getUsuarioPorEmail(emailLogado)
            usuarioAtual?.let { usuario ->
                binding.editNome.setText(usuario.nome)
                binding.editEmail.setText(usuario.email)
            }
        } else {
            Toast.makeText(this, "Usuário não logado", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun salvarAlteracoes() {
        val novoNome = binding.editNome.text.toString().trim()
        val novoEmail = binding.editEmail.text.toString().trim()

        if (novoNome.isEmpty() || novoEmail.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
            return
        }

        usuarioAtual?.let { usuario ->
            val sucesso = db.atualizarUsuario(usuario.email, novoNome, novoEmail)
            if (sucesso) {
                if (novoEmail != usuario.email) {
                    session.salvarUsuarioLogado(novoEmail)
                }
                Toast.makeText(this, "Perfil atualizado com sucesso!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Erro ao atualizar perfil", Toast.LENGTH_SHORT).show()
            }
        }
    }
}