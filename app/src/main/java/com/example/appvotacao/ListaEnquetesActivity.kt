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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListaEnquetesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DatabaseHelper(this)
        carregarEnquetes()

        binding.btnVoltar.setOnClickListener {
            startActivity(Intent(this, VotacaoActivity::class.java))
            finish()
        }
    }

    private fun carregarEnquetes() {
        val enquetes = db.getTodasEnquetes()
        val container = binding.containerEnquetes

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

            val textTitulo = TextView(this)
            textTitulo.text = enquete.titulo
            textTitulo.textSize = 18f
            textTitulo.setPadding(0, 0, 0, 16)

            val btnVotar = Button(this)
            btnVotar.text = "Votar"
            btnVotar.setBackgroundColor(0xFF2196F3.toInt())
            btnVotar.setTextColor(0xFFFFFFFF.toInt())

            btnVotar.setOnClickListener {
                val intent = Intent(this, VotarEnqueteActivity::class.java)
                intent.putExtra("ENQUETE_ID", enquete.id)
                intent.putExtra("ENQUETE_TITULO", enquete.titulo)
                startActivity(intent)
            }

            card.addView(textTitulo)
            card.addView(btnVotar)
            container.addView(card)
        }
    }
}