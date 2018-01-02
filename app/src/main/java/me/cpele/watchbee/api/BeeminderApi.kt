package me.cpele.watchbee.api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface BeeminderApi {
    @GET("/api/v1/users/me.json")
    fun getUser(@Query("access_token") accessToken: String): Call<me.cpele.watchbee.api.User>

    @GET("/api/v1/users/{user}/goals.json")
    fun getGoals(
            @Path("user") user: String,
            @Query("access_token") accessToken: String): Call<List<me.cpele.watchbee.api.Goal>>
}