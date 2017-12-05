package me.cpele.beetimer

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private const val BMNDR_CLIENT_ID = "30zxgk213ellu3dj730wto3qj"
private const val BMNDR_REDIRECT_URI = "beetimer://auth_callback"

class MainActivity : AppCompatActivity() {

    private lateinit var mApi: BeeminderApi
    private lateinit var mAdapter: GoalAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        main_vf.displayedChild = 1

        val retrofit = Retrofit.Builder()
                .baseUrl("https://www.beeminder.com")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        mApi = retrofit.create(BeeminderApi::class.java)

        mAdapter = GoalAdapter()
        main_rv.adapter = mAdapter

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
            fetchUser(accessToken)
        }
    }

    private fun fetchUser(accessToken: String) {
        mApi.getUser(accessToken).enqueue(object: Callback<User> {
            override fun onFailure(call: Call<User>?, t: Throwable?) {
                TODO("not implemented")
            }

            override fun onResponse(call: Call<User>?, response: Response<User>?) {
                response?.body()?.username?.let { fetchGoals(accessToken, it) }
            }
        })
    }

    private fun fetchGoals(accessToken: String, user: String) {
        mApi.getGoals(user, accessToken).enqueue(object: Callback<List<Goal>> {
            override fun onFailure(call: Call<List<Goal>>?, t: Throwable?) {
                TODO("not implemented")
            }

            override fun onResponse(call: Call<List<Goal>>?, response: Response<List<Goal>>?) {
                response?.body()?.let {
                    mAdapter.addAll(it)
                    main_vf.displayedChild = 0
                }
            }
        })
    }
}