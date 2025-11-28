package com.example.appvotacao

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.example.appvotacao.databinding.ActivityListaEnquetesBinding

class ListaEnquetesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityListaEnquetesBinding
    private lateinit var db: DatabaseHelper
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListaEnquetesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DatabaseHelper(this)
        session = SessionManager(this)

        carregarEnquetes()

        binding.btnVoltar.setOnClickListener {
            startActivity(Intent(this, VotacaoActivity::class.java))
            finish()
        }
    }

    private fun carregarEnquetes() {
        val enquetes = db.getTodasEnquetes()
        val container = binding.containerEnquetes
        val usuarioLogado = session.getUsuarioLogado()

        container.removeAllViews()

        if (enquetes.isEmpty()) {
            val textView = TextView(this)
            textView.text = "Nenhuma enquete criada ainda."
            textView.textSize = 16f
            textView.gravity = android.view.Gravity.CENTER
            textView.setPadding(0, 50, 0, 0)
            container.addView(textView)
            return
        }

        for (enquete in enquetes) {
            val card = LinearLayout(this)
            card.orientation = LinearLayout.VERTICAL
            card.setPadding(32, 32, 32, 32)
            card.setBackgroundResource(android.R.drawable.dialog_holo_light_frame)

            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.setMargins(0, 0, 0, 32)
            card.layoutParams = layoutParams

            val isExpirada = db.isEnqueteExpirada(enquete.id)

            val textTitulo = TextView(this)
            val tituloComStatus = if (isExpirada) {
                "${enquete.titulo} 🕒 EXPIRADA"
            } else {
                enquete.titulo
            }
            textTitulo.text = tituloComStatus
            textTitulo.textSize = 18f
            textTitulo.setPadding(0, 0, 0, 16)

            if (isExpirada) {
                textTitulo.setTextColor(0xFFF44336.toInt())
            }

            val layoutBotoes = LinearLayout(this)
            layoutBotoes.orientation = LinearLayout.HORIZONTAL
            layoutBotoes.weightSum = 4f

            val btnVotar = Button(this)
            if (isExpirada) {
                btnVotar.text = "Expirada"
                btnVotar.isEnabled = false
                btnVotar.setBackgroundColor(0xFF9E9E9E.toInt())
            } else {
                btnVotar.text = "Votar"
                btnVotar.setBackgroundColor(0xFF2196F3.toInt())
                btnVotar.setOnClickListener {
                    val intent = Intent(this, VotarEnqueteActivity::class.java)
                    intent.putExtra("ENQUETE_ID", enquete.id)
                    intent.putExtra("ENQUETE_TITULO", enquete.titulo)
                    startActivity(intent)
                }
            }
            btnVotar.setTextColor(0xFFFFFFFF.toInt())
            btnVotar.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            btnVotar.setPadding(4, 8, 4, 8)

            val btnResultados = Button(this)
            btnResultados.text = "Resultados"
            btnResultados.setBackgroundColor(0xFF4CAF50.toInt())
            btnResultados.setTextColor(0xFFFFFFFF.toInt())
            btnResultados.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            btnResultados.setPadding(4, 8, 4, 8)

            btnResultados.setOnClickListener {
                val intent = Intent(this, ResultadosActivity::class.java)
                intent.putExtra("ENQUETE_ID", enquete.id)
                intent.putExtra("ENQUETE_TITULO", enquete.titulo)
                startActivity(intent)
            }

            val btnEditar = Button(this)
            btnEditar.text = "Editar"
            btnEditar.setBackgroundColor(0xFFFF9800.toInt())
            btnEditar.setTextColor(0xFFFFFFFF.toInt())
            btnEditar.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            btnEditar.setPadding(4, 8, 4, 8)

            val btnExcluir = Button(this)
            btnExcluir.text = "Excluir"
            btnExcluir.setBackgroundColor(0xFFF44336.toInt())
            btnExcluir.setTextColor(0xFFFFFFFF.toInt())
            btnExcluir.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            btnExcluir.setPadding(4, 8, 4, 8)

            val usuarioEmail = usuarioLogado?.toString() ?: ""
            val isCriador = usuarioEmail.isNotEmpty() && db.isCriadorDaEnquete(enquete.id, usuarioEmail)

            if (isCriador) {
                btnEditar.visibility = android.view.View.VISIBLE
                btnExcluir.visibility = android.view.View.VISIBLE

                btnEditar.setOnClickListener {
                    val intent = Intent(this, EditarEnqueteActivity::class.java)
                    intent.putExtra("ENQUETE_ID", enquete.id)
                    startActivity(intent)
                }

                btnExcluir.setOnClickListener {
                    mostrarDialogoConfirmacao(excluirEnquete = {
                        val sucesso = db.excluirEnquete(enquete.id)
                        if (sucesso) {
                            Toast.makeText(this, "Enquete excluída com sucesso!", Toast.LENGTH_SHORT).show()
                            carregarEnquetes()
                        } else {
                            Toast.makeText(this, "Erro ao excluir enquete", Toast.LENGTH_SHORT).show()
                        }
                    }, tituloEnquete = enquete.titulo)
                }
            } else {
                btnEditar.visibility = android.view.View.GONE
                btnExcluir.visibility = android.view.View.GONE
            }

            layoutBotoes.addView(btnVotar)
            layoutBotoes.addView(btnResultados)
            layoutBotoes.addView(btnEditar)
            layoutBotoes.addView(btnExcluir)

            card.addView(textTitulo)
            card.addView(layoutBotoes)
            container.addView(card)
        }
    }

    private fun mostrarDialogoConfirmacao(excluirEnquete: () -> Unit, tituloEnquete: String) {
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Confirmar Exclusão")
        builder.setMessage("Tem certeza que deseja excluir a enquete \"$tituloEnquete\"?\nEsta ação não pode ser desfeita.")

        builder.setPositiveButton("Excluir") { dialog, which ->
            excluirEnquete()
        }

        builder.setNegativeButton("Cancelar") { dialog, which ->
            dialog.dismiss()
        }

        builder.show()
    }
}