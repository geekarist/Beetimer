package me.cpele.fleabrainer.api

import kotlinx.coroutines.experimental.Deferred
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface BeeminderApi {

    @GET("/api/v1/users/me.json")
    fun getUser(@Query("access_token") accessToken: String): Deferred<Response<User>?>

    @GET("/api/v1/users/{user}/goals.json")
    fun getGoals(
            @Path("user") user: String,
            @Query("access_token") accessToken: String
    ): Deferred<Response<List<Goal>>?>

    @POST("/api/v1/users/{user}/goals/{goal}/datapoints.json")
    fun postDatapoint(
            @Path("user") userName: String,
            @Path("goal") goalSlug: String,
            @Query("value") datapointValue: Float,
            @Query("comment") comment: String,
            @Query("access_token") accessToken: String
    ): Deferred<Datapoint?>

    @GET("/api/v1/users/{user}/goals/{goal}/datapoints.json")
    fun getDataPoints(
            @Path("user") userName: String,
            @Path("goal") slug: String,
            @Query("access_token") accessToken: String
    ): Deferred<Response<List<Datapoint>>?>
}

