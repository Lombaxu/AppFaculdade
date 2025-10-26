package com.example.appvotacao

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, "votacao.db", null, 2) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE usuarios (id INTEGER PRIMARY KEY AUTOINCREMENT, nome TEXT, email TEXT, senha TEXT)")
        db.execSQL("CREATE TABLE enquetes (id INTEGER PRIMARY KEY AUTOINCREMENT, titulo TEXT, criador_email TEXT)")
        db.execSQL("CREATE TABLE opcoes_enquete (id INTEGER PRIMARY KEY AUTOINCREMENT, enquete_id INTEGER, texto TEXT, votos INTEGER DEFAULT 0)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS usuarios")
        db.execSQL("DROP TABLE IF EXISTS enquetes")
        db.execSQL("DROP TABLE IF EXISTS opcoes_enquete")
        onCreate(db)
    }

    fun cadastrarUsuario(nome: String, email: String, senha: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("nome", nome)
            put("email", email)
            put("senha", senha)
        }
        return db.insert("usuarios", null, values) != -1L
    }

    fun verificarLogin(email: String, senha: String): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM usuarios WHERE email = ? AND senha = ?",
            arrayOf(email, senha)
        )
        val existe = cursor.count > 0
        cursor.close()
        return existe
    }

    fun emailExiste(email: String): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM usuarios WHERE email = ?",
            arrayOf(email)
        )
        val existe = cursor.count > 0
        cursor.close()
        return existe
    }

    fun getUsuarioPorEmail(email: String): Usuario? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM usuarios WHERE email = ?", arrayOf(email))

        return if (cursor.moveToFirst()) {
            val usuario = Usuario(
                id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                nome = cursor.getString(cursor.getColumnIndexOrThrow("nome")),
                email = cursor.getString(cursor.getColumnIndexOrThrow("email")),
                senha = cursor.getString(cursor.getColumnIndexOrThrow("senha"))
            )
            cursor.close()
            usuario
        } else {
            cursor.close()
            null
        }
    }

    fun atualizarUsuario(emailAntigo: String, novoNome: String, novoEmail: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("nome", novoNome)
            put("email", novoEmail)
        }
        val linhasAfetadas = db.update("usuarios", values, "email = ?", arrayOf(emailAntigo))
        return linhasAfetadas > 0
    }

    fun criarEnquete(titulo: String, opcoes: List<String>, criadorEmail: String): Boolean {
        val db = writableDatabase

        android.util.Log.d("DEBUG_DB", "Criando enquete: $titulo")

        try {
            val valuesEnquete = ContentValues().apply {
                put("titulo", titulo)
                put("criador_email", criadorEmail)
            }

            val enqueteId = db.insert("enquetes", null, valuesEnquete)
            android.util.Log.d("DEBUG_DB", "Enquete ID: $enqueteId")

            if (enqueteId == -1L) return false

            for (opcao in opcoes) {
                val valuesOpcao = ContentValues().apply {
                    put("enquete_id", enqueteId)
                    put("texto", opcao)
                }
                db.insert("opcoes_enquete", null, valuesOpcao)
            }

            return true
        } catch (e: Exception) {
            android.util.Log.e("DEBUG_DB", "Erro: ${e.message}")
            return false
        }
    }

    fun getTodasEnquetes(): List<Enquete> {
        val enquetes = mutableListOf<Enquete>()
        val db = readableDatabase

        try {
            val cursor = db.rawQuery("SELECT * FROM enquetes ORDER BY id DESC", null)

            while (cursor.moveToNext()) {
                val enquete = Enquete(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    titulo = cursor.getString(cursor.getColumnIndexOrThrow("titulo")),
                    criadorEmail = cursor.getString(cursor.getColumnIndexOrThrow("criador_email"))
                )
                enquetes.add(enquete)
            }
            cursor.close()
        } catch (e: Exception) {
        }

        return enquetes
    }

    fun getOpcoesEnquete(enqueteId: Int): List<Opcao> {
        val opcoes = mutableListOf<Opcao>()
        val db = readableDatabase

        try {
            val cursor = db.rawQuery("SELECT * FROM opcoes_enquete WHERE enquete_id = ?", arrayOf(enqueteId.toString()))

            while (cursor.moveToNext()) {
                val opcao = Opcao(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    enqueteId = cursor.getInt(cursor.getColumnIndexOrThrow("enquete_id")),
                    texto = cursor.getString(cursor.getColumnIndexOrThrow("texto")),
                    votos = cursor.getInt(cursor.getColumnIndexOrThrow("votos"))
                )
                opcoes.add(opcao)
            }
            cursor.close()
        } catch (e: Exception) {
        }

        return opcoes
    }

    fun votarNaOpcao(opcaoId: Int): Boolean {
        val db = writableDatabase
        try {
            val values = ContentValues().apply {
                put("votos", getVotosAtuais(opcaoId) + 1)
            }
            val linhasAfetadas = db.update("opcoes_enquete", values, "id = ?", arrayOf(opcaoId.toString()))
            return linhasAfetadas > 0
        } catch (e: Exception) {
            return false
        }
    }

    private fun getVotosAtuais(opcaoId: Int): Int {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT votos FROM opcoes_enquete WHERE id = ?", arrayOf(opcaoId.toString()))
        return if (cursor.moveToFirst()) {
            cursor.getInt(cursor.getColumnIndexOrThrow("votos"))
        } else {
            0
        }.also { cursor.close() }
    }
}

data class Usuario(
    val id: Int,
    val nome: String,
    val email: String,
    val senha: String
)

data class Enquete(
    val id: Int,
    val titulo: String,
    val criadorEmail: String
)

data class Opcao(
    val id: Int,
    val enqueteId: Int,
    val texto: String,
    val votos: Int
)