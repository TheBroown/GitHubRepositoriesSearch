package com.dmitrystepanishchev.testgithubsearch

import android.os.Parcel
import android.os.Parcelable

class Repo() : Parcelable {

    var RepoURL: String=""
    var Title: String=""
    var Description: String=""
    var AvatarUser: String=""

    constructor(parcel: Parcel) : this() {
        RepoURL = parcel.readString() ?: ""
        Title = parcel.readString() ?: ""
        Description = parcel.readString() ?: ""
        AvatarUser = parcel.readString() ?: ""
    }

    constructor(_repoURL: String, _title: String, _desc: String, _avatarURL: String) : this() {
        RepoURL = _repoURL
        Title = _title
        Description = _desc
        AvatarUser = _avatarURL
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(RepoURL)
        parcel.writeString(Title)
        parcel.writeString(Description)
        parcel.writeString(AvatarUser)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Repo> {
        override fun createFromParcel(parcel: Parcel): Repo {
            return Repo(parcel)
        }

        override fun newArray(size: Int): Array<Repo?> {
            return arrayOfNulls(size)
        }
    }
}