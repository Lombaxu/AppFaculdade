package com.example.appvotacao

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.appvotacao.databinding.ActivityCadastroBinding

class CadastroActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCadastroBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCadastroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnCadastrar.setOnClickListener {
            val nome = binding.editNome.text.toString().trim()
            val email = binding.editEmail.text.toString().trim()
            val senha = binding.editSenha.text.toString().trim()

            when {
                nome.isEmpty() || email.isEmpty() || senha.isEmpty() -> {
                    Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
                }
                senha.length < 6 -> {
                    Toast.makeText(this, "Senha deve ter pelo menos 6 caracteres", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    val db = DatabaseHelper(this)

                    if (db.emailExiste(email)) {
                        Toast.makeText(this, "Este e-mail já está cadastrado", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }

                    val sucesso = db.cadastrarUsuario(nome, email, senha)
                    if (sucesso) {
                        Toast.makeText(this, "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, "Erro ao cadastrar", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        binding.btnVoltarLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}