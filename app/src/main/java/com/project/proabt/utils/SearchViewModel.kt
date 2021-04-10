package com.project.proabt.utils

import androidx.lifecycle.LiveData

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel


class SearchViewModel:ViewModel() {

    private val query = MutableLiveData<String>()

    fun setQuery(queryData: String) {
        query.value = queryData
    }

    fun getQuery(): LiveData<String>? {
        return query
    }
}