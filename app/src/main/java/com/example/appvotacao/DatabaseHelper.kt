package com.example.appvotacao

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, "votacao.db", null, 4) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE usuarios (id INTEGER PRIMARY KEY AUTOINCREMENT, nome TEXT, email TEXT, senha TEXT)")
        db.execSQL("CREATE TABLE enquetes (id INTEGER PRIMARY KEY AUTOINCREMENT, titulo TEXT, criador_email TEXT, data_expiracao TEXT)")
        db.execSQL("CREATE TABLE opcoes_enquete (id INTEGER PRIMARY KEY AUTOINCREMENT, enquete_id INTEGER, texto TEXT, votos INTEGER DEFAULT 0)")
        db.execSQL("CREATE TABLE votos_usuario (id INTEGER PRIMARY KEY AUTOINCREMENT, usuario_email TEXT, enquete_id INTEGER, opcao_id INTEGER)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS usuarios")
        db.execSQL("DROP TABLE IF EXISTS enquetes")
        db.execSQL("DROP TABLE IF EXISTS opcoes_enquete")
        db.execSQL("DROP TABLE IF EXISTS votos_usuario")
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

        try {
            val valuesEnquete = ContentValues().apply {
                put("titulo", titulo)
                put("criador_email", criadorEmail)
            }

            val enqueteId = db.insert("enquetes", null, valuesEnquete)

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
            return false
        }
    }

    fun criarEnqueteComData(titulo: String, opcoes: List<String>, criadorEmail: String, dataExpiracao: String): Boolean {
        val db = writableDatabase

        try {
            val valuesEnquete = ContentValues().apply {
                put("titulo", titulo)
                put("criador_email", criadorEmail)
                put("data_expiracao", dataExpiracao)
            }

            val enqueteId = db.insert("enquetes", null, valuesEnquete)

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
                    criadorEmail = cursor.getString(cursor.getColumnIndexOrThrow("criador_email")),
                    dataExpiracao = cursor.getString(cursor.getColumnIndexOrThrow("data_expiracao"))
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

    fun registrarVotoComHistorico(usuarioEmail: String, enqueteId: Int, opcaoId: Int): Boolean {
        val db = writableDatabase

        try {
            val cursor = db.rawQuery(
                "SELECT * FROM votos_usuario WHERE usuario_email = ? AND enquete_id = ?",
                arrayOf(usuarioEmail, enqueteId.toString())
            )
            val jaVotou = cursor.count > 0
            cursor.close()

            if (jaVotou) return false

            val valuesVoto = ContentValues().apply {
                put("usuario_email", usuarioEmail)
                put("enquete_id", enqueteId)
                put("opcao_id", opcaoId)
            }
            db.insert("votos_usuario", null, valuesVoto)

            db.execSQL("UPDATE opcoes_enquete SET votos = votos + 1 WHERE id = ?", arrayOf(opcaoId.toString()))

            return true
        } catch (e: Exception) {
            return false
        }
    }

    fun getHistoricoVotos(usuarioEmail: String): List<HistoricoVoto> {
        val historico = mutableListOf<HistoricoVoto>()
        val db = readableDatabase

        val cursor = db.rawQuery("""
            SELECT e.titulo as enquete_titulo, o.texto as opcao_votada, e.id as enquete_id
            FROM votos_usuario v
            JOIN enquetes e ON v.enquete_id = e.id
            JOIN opcoes_enquete o ON v.opcao_id = o.id
            WHERE v.usuario_email = ?
            ORDER BY v.id DESC
        """, arrayOf(usuarioEmail))

        while (cursor.moveToNext()) {
            val item = HistoricoVoto(
                enqueteTitulo = cursor.getString(cursor.getColumnIndexOrThrow("enquete_titulo")),
                opcaoVotada = cursor.getString(cursor.getColumnIndexOrThrow("opcao_votada")),
                enqueteId = cursor.getInt(cursor.getColumnIndexOrThrow("enquete_id"))
            )
            historico.add(item)
        }
        cursor.close()

        return historico
    }

    fun usuarioJaVotou(usuarioEmail: String, enqueteId: Int): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM votos_usuario WHERE usuario_email = ? AND enquete_id = ?",
            arrayOf(usuarioEmail, enqueteId.toString())
        )
        val jaVotou = cursor.count > 0
        cursor.close()
        return jaVotou
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

    fun getResultadosEnquete(enqueteId: Int): List<ResultadoVoto> {
        val resultados = mutableListOf<ResultadoVoto>()
        val db = readableDatabase

        val cursor = db.rawQuery(
            "SELECT texto, votos FROM opcoes_enquete WHERE enquete_id = ? ORDER BY votos DESC",
            arrayOf(enqueteId.toString())
        )

        while (cursor.moveToNext()) {
            val resultado = ResultadoVoto(
                opcao = cursor.getString(cursor.getColumnIndexOrThrow("texto")),
                votos = cursor.getInt(cursor.getColumnIndexOrThrow("votos"))
            )
            resultados.add(resultado)
        }
        cursor.close()

        val totalVotos = resultados.sumOf { it.votos }
        if (totalVotos > 0) {
            resultados.forEach { resultado ->
                resultado.percentual = (resultado.votos * 100.0) / totalVotos
            }
        }

        return resultados
    }

    fun isCriadorDaEnquete(enqueteId: Int, usuarioEmail: String): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM enquetes WHERE id = ? AND criador_email = ?",
            arrayOf(enqueteId.toString(), usuarioEmail)
        )
        val isCriador = cursor.count > 0
        cursor.close()
        return isCriador
    }

    fun getEnquetePorId(enqueteId: Int): Enquete? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM enquetes WHERE id = ?", arrayOf(enqueteId.toString()))

        return if (cursor.moveToFirst()) {
            val enquete = Enquete(
                id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                titulo = cursor.getString(cursor.getColumnIndexOrThrow("titulo")),
                criadorEmail = cursor.getString(cursor.getColumnIndexOrThrow("criador_email"))
            )
            cursor.close()
            enquete
        } else {
            cursor.close()
            null
        }
    }

    fun atualizarEnquete(enqueteId: Int, novoTitulo: String, novasOpcoes: List<String>): Boolean {
        val db = writableDatabase
        try {
            val valuesEnquete = ContentValues().apply {
                put("titulo", novoTitulo)
            }
            db.update("enquetes", valuesEnquete, "id = ?", arrayOf(enqueteId.toString()))

            db.delete("opcoes_enquete", "enquete_id = ?", arrayOf(enqueteId.toString()))

            for (opcao in novasOpcoes) {
                val valuesOpcao = ContentValues().apply {
                    put("enquete_id", enqueteId)
                    put("texto", opcao)
                    put("votos", 0)
                }
                db.insert("opcoes_enquete", null, valuesOpcao)
            }

            return true
        } catch (e: Exception) {
            return false
        }
    }

    fun excluirEnquete(enqueteId: Int): Boolean {
        val db = writableDatabase
        try {
            db.delete("opcoes_enquete", "enquete_id = ?", arrayOf(enqueteId.toString()))

            val linhasAfetadas = db.delete("enquetes", "id = ?", arrayOf(enqueteId.toString()))

            return linhasAfetadas > 0
        } catch (e: Exception) {
            return false
        }
    }

    fun isEnqueteExpirada(enqueteId: Int): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT data_expiracao FROM enquetes WHERE id = ?", arrayOf(enqueteId.toString()))

        return if (cursor.moveToFirst()) {
            val dataExpiracaoStr = cursor.getString(cursor.getColumnIndexOrThrow("data_expiracao"))
            cursor.close()

            if (dataExpiracaoStr.isNullOrEmpty()) {
                false
            } else {
                val dataExpiracao = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).parse(dataExpiracaoStr)
                val hoje = java.util.Date()
                hoje.after(dataExpiracao)
            }
        } else {
            cursor.close()
            false
        }
    }

    fun getDataExpiracao(enqueteId: Int): String? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT data_expiracao FROM enquetes WHERE id = ?", arrayOf(enqueteId.toString()))

        return if (cursor.moveToFirst()) {
            val data = cursor.getString(cursor.getColumnIndexOrThrow("data_expiracao"))
            cursor.close()
            data
        } else {
            cursor.close()
            null
        }
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
    val criadorEmail: String,
    val dataExpiracao: String? = null
)

data class Opcao(
    val id: Int,
    val enqueteId: Int,
    val texto: String,
    val votos: Int
)

data class ResultadoVoto(
    val opcao: String,
    val votos: Int,
    var percentual: Double = 0.0
)

data class HistoricoVoto(
    val enqueteTitulo: String,
    val opcaoVotada: String,
    val enqueteId: Int
)