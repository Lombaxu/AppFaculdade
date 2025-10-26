package com.example.appvotacao

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("AppVotacaoPrefs", Context.MODE_PRIVATE)

    fun salvarUsuarioLogado(email: String) {
        val editor = prefs.edit()
        editor.putString("usuario_logado", email)
        editor.apply()
    }

    fun getUsuarioLogado(): String? {
        return prefs.getString("usuario_logado", null)
    }

    fun logout() {
        val editor = prefs.edit()
        editor.remove("usuario_logado")
        editor.apply()
    }

    fun isUsuarioLogado(): Boolean {
        return prefs.getString("usuario_logado", null) != null
    }
}