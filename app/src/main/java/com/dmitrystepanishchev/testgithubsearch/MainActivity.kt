package com.dmitrystepanishchev.testgithubsearch

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.IOException
import java.net.URL

class MainActivity : AppCompatActivity() {

    private var requestIsEnded = false
    private var userIsAuthorized = false
    private var client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun nextActivity(view: View) {
        val intent = Intent(this, SearchReposActivity::class.java)
        intent.putExtra("isAuthorized", false)
        startActivity(intent)
    }

    fun logInGitHub(view: View) {
        val editTextToken: EditText = findViewById(R.id.tokenET)

        val tokenText: String = editTextToken.text.toString()

        GlobalScope.launch {
            val request = Request.Builder().url("https://api.github.com/user")
                .addHeader("Authorization", "token $tokenText").build()

            client.newCall(request).execute().use { response ->
                userIsAuthorized = response.code == 200
            }
            requestIsEnded=true
        }

        while(!requestIsEnded){}
        if(userIsAuthorized){
            val intent = Intent(this, SearchReposActivity::class.java)
            intent.putExtra("isAuthorized", true)
            intent.putExtra("tokenString", tokenText)
            startActivity(intent)

        }else{
            Toast.makeText(this,"Неверный токен", Toast.LENGTH_LONG).show()
        }
    }
}