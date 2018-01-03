package me.cpele.watchbee.api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface BeeminderApi {

    @GET("/api/v1/users/me.json")
    fun getUser(@Query("access_token") accessToken: String): Call<User>

    @GET("/api/v1/users/{user}/goals.json")
    fun getGoals(
            @Path("user") user: String,
            @Query("access_token") accessToken: String
    ): Call<List<Goal>>

    @POST("/api/v1/users/{user}/goals/{goal}/datapoints.json")
    fun postDatapoint(
            @Path("user") userName: String,
            @Path("goal") goalSlug: String,
            @Query("value") datapointValue: String,
            @Query("comment") comment: String,
            @Query("access_token") accessToken: String
    ): Call<Datapoint>
}

