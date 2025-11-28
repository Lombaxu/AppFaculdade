package com.example.appvotacao

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import com.example.appvotacao.databinding.ActivityResultadosBinding

class ResultadosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultadosBinding
    private lateinit var db: DatabaseHelper
    private var enqueteId: Int = 0
    private var enqueteTitulo: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultadosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DatabaseHelper(this)

        enqueteId = intent.getIntExtra("ENQUETE_ID", 0)
        enqueteTitulo = intent.getStringExtra("ENQUETE_TITULO") ?: ""

        binding.textTituloEnquete.text = "Resultados: $enqueteTitulo"

        carregarResultados()

        binding.btnVoltar.setOnClickListener {
            finish()
        }
    }

    private fun carregarResultados() {
        val resultados = db.getResultadosEnquete(enqueteId)
        val container = binding.containerResultados

        container.removeAllViews()

        if (resultados.isEmpty()) {
            val textView = TextView(this)
            textView.text = "Nenhum voto registrado ainda."
            textView.textSize = 16f
            textView.gravity = android.view.Gravity.CENTER
            textView.setPadding(0, 50, 0, 0)
            container.addView(textView)
            return
        }

        val totalVotos = resultados.sumOf { it.votos }

        val textTotal = TextView(this)
        textTotal.text = "Total de votos: $totalVotos"
        textTotal.textSize = 18f
        textTotal.setPadding(0, 0, 0, 32)
        container.addView(textTotal)

        for (resultado in resultados) {
            val card = LinearLayout(this)
            card.orientation = LinearLayout.VERTICAL
            card.setPadding(32, 24, 32, 24)
            card.setBackgroundResource(android.R.drawable.dialog_holo_light_frame)

            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.setMargins(0, 0, 0, 16)
            card.layoutParams = layoutParams

            val textOpcao = TextView(this)
            textOpcao.text = resultado.opcao
            textOpcao.textSize = 18f
            textOpcao.setPadding(0, 0, 0, 8)

            val textDetalhes = TextView(this)
            textDetalhes.text = "${resultado.votos} votos (${String.format("%.1f", resultado.percentual)}%)"
            textDetalhes.textSize = 16f
            textDetalhes.setTextColor(0xFF666666.toInt())

            card.addView(textOpcao)
            card.addView(textDetalhes)
            container.addView(card)
        }
    }
}