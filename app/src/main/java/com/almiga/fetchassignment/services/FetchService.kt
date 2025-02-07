package com.almiga.fetchassignment.services

import com.almiga.fetchassignment.model.FetchItem
import retrofit2.Response
import retrofit2.http.GET

interface FetchService {
    @GET("/hiring.json")
    suspend fun getItems(): Response<List<FetchItem>>
}

