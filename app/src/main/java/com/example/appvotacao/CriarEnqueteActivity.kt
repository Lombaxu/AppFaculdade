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
    private var dataExpiracao: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCriarEnqueteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DatabaseHelper(this)
        session = SessionManager(this)

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

        binding.btnSelecionarData.setOnClickListener {
            mostrarDatePicker()
        }

        binding.btnCriarEnquete.setOnClickListener {
            val titulo = binding.editTituloEnquete.text.toString().trim()

            if (titulo.isEmpty()) {
                Toast.makeText(this, "Digite um título", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (opcoesList.size < 2) {
                Toast.makeText(this, "Adicione pelo menos 2 opções", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (dataExpiracao.isEmpty()) {
                Toast.makeText(this, "Selecione uma data de expiração", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val usuarioEmail = session.getUsuarioLogado()
            if (usuarioEmail != null) {
                val sucesso = db.criarEnqueteComData(titulo, opcoesList, usuarioEmail, dataExpiracao)
                if (sucesso) {
                    Toast.makeText(this, "Enquete criada com sucesso!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, VotacaoActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Erro ao criar enquete", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.btnVoltar.setOnClickListener {
            startActivity(Intent(this, VotacaoActivity::class.java))
            finish()
        }
    }

    private fun mostrarDatePicker() {
        val calendar = java.util.Calendar.getInstance()
        val year = calendar.get(java.util.Calendar.YEAR)
        val month = calendar.get(java.util.Calendar.MONTH)
        val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)

        val datePicker = android.app.DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            val dataSelecionada = java.util.Calendar.getInstance()
            dataSelecionada.set(selectedYear, selectedMonth, selectedDay)

            val formato = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            dataExpiracao = formato.format(dataSelecionada.time)

            val formatoExibicao = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
            binding.textDataSelecionada.text = "Data de expiração: ${formatoExibicao.format(dataSelecionada.time)}"

        }, year, month, day)

        datePicker.datePicker.minDate = System.currentTimeMillis() - 1000
        datePicker.show()
    }

    private fun atualizarListaOpcoes() {
        val textoOpcoes = opcoesList.joinToString("\n") { "• $it" }
        binding.txtOpcoesAdicionadas.text = "Opções adicionadas:\n$textoOpcoes"
    }
}