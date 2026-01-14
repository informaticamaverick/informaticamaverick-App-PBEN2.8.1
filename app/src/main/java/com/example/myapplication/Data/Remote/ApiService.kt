package com.example.myapplication.Data.Remote

import com.example.myapplication.Data.Model.LoginResquest
import com.example.myapplication.Data.Model.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginResquest): Response<LoginResponse>
}