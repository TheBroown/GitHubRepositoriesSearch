package com.dmitrystepanishchev.testgithubsearch

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso


class RepoAdapter(var repos: ArrayList<Repo>) :
    RecyclerView.Adapter<RepoAdapter.ViewHolder>() {

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RepoAdapter.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.repo_list_item, parent, false)
                as LinearLayout
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: RepoAdapter.ViewHolder, position: Int) {
            val s = repos[position]
            var v = holder.view.findViewById<TextView>(R.id.titleTextView)
            v.text = s.Title

            v = holder.view.findViewById(R.id.descriptionTextView)
            v.text = s.Description

            var i = holder.view.findViewById(R.id.avatarImageView) as ImageView
            Picasso.get().load(s.AvatarUser.toUri()).resize(200, 200).into(i)
    }

    override fun getItemCount(): Int {
        return repos.size
    }
}

