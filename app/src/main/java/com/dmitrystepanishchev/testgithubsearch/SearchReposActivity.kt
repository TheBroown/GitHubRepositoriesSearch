package com.dmitrystepanishchev.testgithubsearch

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.lang.Exception

class SearchReposActivity : AppCompatActivity() {

    private var isAuthorized = false
    private var token = ""
    private var client = OkHttpClient()
    private var repos = ArrayList<Repo>()
    private var searchString = ""
    private lateinit var recyclerView: RecyclerView
    private var pageNumber = 1
    private var toAdd = true
    private var isFinding = false
    private lateinit var repoAdapter: RepoAdapter
    private var topElementPosition = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_repos)
        isAuthorized = intent.getBooleanExtra("isAuthorized", false)
        if (isAuthorized)
            token = intent.getStringExtra("tokenString")

        recyclerView = findViewById(R.id.listRepos)


        if (savedInstanceState != null) {
            pageNumber = savedInstanceState.getInt("pageNumber")
            if (savedInstanceState.getParcelableArrayList<Repo>("reposArray") != null) {
                repos =
                    savedInstanceState.getParcelableArrayList<Repo>("reposArray") as ArrayList<Repo>
            }
            topElementPosition = savedInstanceState.getInt("scrollPos")
            searchString = savedInstanceState.getString("searchString", "")
        }

        repoAdapter = RepoAdapter(repos)
        recyclerView.adapter = repoAdapter

        //load data before scrolling to the bottom of view
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val topElementIndex = (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                super.onScrolled(recyclerView, dx, dy)
                if (topElementIndex > repos.size - 50 && repos.size > 0 && toAdd) {
                    toAdd = false
                    findRepos()
                }
                if (topElementIndex >= repos.size - 11 && isFinding && repos.size > 0)
                    findViewById<androidx.core.widget.ContentLoadingProgressBar>(R.id.progressBar).visibility =
                        androidx.core.widget.ContentLoadingProgressBar.VISIBLE
            }
        })

        (recyclerView.layoutManager as LinearLayoutManager).scrollToPosition(topElementPosition)
    }

    fun findRepos() {
        val cm = baseContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = cm.activeNetworkInfo

        if (networkInfo != null && networkInfo.isConnected) {
            GlobalScope.launch {
                try {
                    val request: Request
                    if (isAuthorized)
                        request = Request.Builder()
                            .url("https://api.github.com/search/repositories?q=$searchString&per_page=100&page=$pageNumber")
                            .addHeader("Authorization", "token $token").build()
                    else
                        request = Request.Builder()
                            .url("https://api.github.com/search/repositories?q=$searchString&per_page=100&page=$pageNumber")
                            .build()

                    var json: JSONObject
                    isFinding = true
                    client.newCall(request).execute().use { response ->
                        json = JSONObject(response.body!!.string())
                    }

                    pageNumber++

                    val array = json.getJSONArray("items")

                    for (i in 0 until array.length()) {

                        val o = array.getJSONObject(i)

                        val newRepo = Repo(
                            o.getString("url"),
                            o.getString("name"),
                            o.getString("description"),
                            o.getJSONObject("owner").getString("avatar_url")
                        )
                        repos.add(newRepo)
                    }
                } catch (e: Exception) {
                    //Log.e("catch", e.toString())
                }
                MainScope().launch {
                    repoAdapter.notifyDataSetChanged()
                    toAdd = true
                    findViewById<androidx.core.widget.ContentLoadingProgressBar>(R.id.progressBar).visibility =
                        androidx.core.widget.ContentLoadingProgressBar.GONE

                }
            }
        } else {
            Toast.makeText(this, "Нет подключения к интернету", Toast.LENGTH_LONG).show()
            toAdd = true

        }
        isFinding = false

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_search_repos, menu)

        if (isAuthorized) {
            menu!!.findItem(R.id.quit).title = "Выйти"
        } else {
            menu!!.findItem(R.id.quit).title = "Войти"
        }

        val manager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchItem = menu.findItem(R.id.search)
        val searchView = searchItem?.actionView as SearchView

        searchView.setSearchableInfo(manager.getSearchableInfo(componentName))

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }

            override fun onQueryTextSubmit(query: String?): Boolean {
                repos.clear()
                if (query != null) {
                    searchString = query
                    pageNumber = 1
                    toAdd = false
                    findViewById<androidx.core.widget.ContentLoadingProgressBar>(R.id.progressBar).visibility =
                        androidx.core.widget.ContentLoadingProgressBar.VISIBLE
                    findRepos()
                }
                return true
            }
        })

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.quit -> {
                returnToMainActivity()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun returnToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelableArrayList("reposArray", repos)
        outState.putInt("pageNumber", pageNumber)
        outState.putString("searchString", searchString)
        outState.putInt(
            "scrollPos",
            (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
        )
    }
}


