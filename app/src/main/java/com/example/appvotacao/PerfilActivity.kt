package com.example.appvotacao

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import android.widget.LinearLayout
import android.widget.TextView
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
        carregarHistoricoVotos()

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

    private fun carregarHistoricoVotos() {
        val usuarioEmail = session.getUsuarioLogado()
        if (usuarioEmail != null) {
            val historico = db.getHistoricoVotos(usuarioEmail)
            val container = binding.containerHistorico

            container.removeAllViews()

            if (historico.isEmpty()) {
                val textView = TextView(this)
                textView.text = "Você ainda não votou em nenhuma enquete."
                textView.textSize = 16f
                textView.gravity = android.view.Gravity.CENTER
                textView.setPadding(0, 50, 0, 0)
                container.addView(textView)
                return
            }

            val textTitulo = TextView(this)
            textTitulo.text = "Minhas Votações:"
            textTitulo.textSize = 18f
            textTitulo.setPadding(0, 0, 0, 16)
            container.addView(textTitulo)

            for (item in historico) {
                val card = LinearLayout(this)
                card.orientation = LinearLayout.VERTICAL
                card.setPadding(24, 20, 24, 20)
                card.setBackgroundResource(android.R.drawable.dialog_holo_light_frame)

                val layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                layoutParams.setMargins(0, 0, 0, 12)
                card.layoutParams = layoutParams

                val textEnquete = TextView(this)
                textEnquete.text = item.enqueteTitulo
                textEnquete.textSize = 16f
                textEnquete.setPadding(0, 0, 0, 4)

                val textOpcao = TextView(this)
                textOpcao.text = "Você votou em: ${item.opcaoVotada}"
                textOpcao.textSize = 14f
                textOpcao.setTextColor(0xFF666666.toInt())

                card.addView(textEnquete)
                card.addView(textOpcao)
                container.addView(card)
            }
        }
    }
}