package me.cpele.beetimer

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

private const val BMNDR_CLIENT_ID = "30zxgk213ellu3dj730wto3qj"
private const val BMNDR_REDIRECT_URI = "beetimer://auth_callback"

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupSignInButton()
        setupTokenHandler()
    }

    private fun setupSignInButton() {
        val signInButton = main_bt_sign_in
        signInButton.setOnClickListener {
            val uri = Uri.parse("https://www.beeminder.com/apps/authorize?" +
                    "client_id=$BMNDR_CLIENT_ID&redirect_uri=$BMNDR_REDIRECT_URI&response_type=token")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }
    }

    private fun setupTokenHandler() {
        val data = intent?.data
        val action = intent?.action
        val scheme = data?.scheme
        val host = data?.host
        if (action == Intent.ACTION_VIEW && scheme == "beetimer" && host == "auth_callback") {
            val accessToken = data.getQueryParameter("access_token")
            fetchGoals(accessToken)
        }
    }

    private fun fetchGoals(accessToken: String) {
        val retrofit = Retrofit.Builder()
                .baseUrl("https://www.beeminder.com")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        val api = retrofit.create(BeeminderApi::class.java)
        api.getUser(accessToken).enqueue(object: Callback<User> {
            override fun onFailure(call: Call<User>?, t: Throwable?) {
                TODO("not implemented")
            }

            override fun onResponse(call: Call<User>?, response: Response<User>?) {
                response?.body()?.goals?.apply {
                    Toast.makeText(this@MainActivity, toString(), Toast.LENGTH_LONG).show()
                }
            }
        })
    }
}

interface BeeminderApi {
    @GET("/api/v1/users/me.json")
    fun getUser(@Query("access_token") accessToken: String): Call<User>
}

data class User(val username: String, val goals: List<String>)

