package com.dmitrystepanishchev.testgithubsearch

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request

class MainActivity : AppCompatActivity() {

    private var client = OkHttpClient()
    private var requestIsEnded = false
    private var userIsAuthorized = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun unauthorizedAccess(view: View) {
        val cm = baseContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = cm.activeNetworkInfo
        if (networkInfo != null && networkInfo.isConnected) {
            val intent = Intent(this, SearchReposActivity::class.java)
            intent.putExtra("isAuthorized", false)
            startActivity(intent)
        } else {
            Toast.makeText(this, "Нет подключения к интернету", Toast.LENGTH_LONG).show()
        }
    }

    fun logInGitHub(view: View) {
        val cm = baseContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = cm.activeNetworkInfo
        val editTextToken: EditText = findViewById(R.id.tokenET)
        val tokenText: String = editTextToken.text.toString()

        if (networkInfo != null && networkInfo.isConnected) {
            //check the token is correct
            GlobalScope.launch {
                val request = Request.Builder().url("https://api.github.com/user")
                    .addHeader("Authorization", "token $tokenText").build()

                client.newCall(request).execute().use { response ->
                    userIsAuthorized = response.code == 200
                }
                requestIsEnded = true
            }

            while (!requestIsEnded) { }

            if (userIsAuthorized) {
                val intent = Intent(this, SearchReposActivity::class.java)
                intent.putExtra("isAuthorized", true)
                intent.putExtra("tokenString", tokenText)
                startActivity(intent)

            } else {
                Toast.makeText(this, "Неверный токен", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(this, "Нет подключения к интернету", Toast.LENGTH_LONG).show()
        }
    }
}