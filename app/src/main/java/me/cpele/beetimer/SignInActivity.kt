package me.cpele.beetimer

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_sign_in.*

private const val BMNDR_CLIENT_ID = "30zxgk213ellu3dj730wto3qj"
private const val BMNDR_REDIRECT_URI = "beetimer://auth_callback"
private const val PREF_ACCESS_TOKEN = "ACCESS_TOKEN"

class SignInActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Try getting token from preferences

        val prefs = PreferenceManager.getDefaultSharedPreferences(this)

        if (prefs.contains(PREF_ACCESS_TOKEN)) {
            MainActivity.start(this, prefs.getString(PREF_ACCESS_TOKEN, null))
            return
        }

        // 2. Try getting token from intent

        val data = intent?.data
        val action = intent?.action
        val scheme = data?.scheme
        val host = data?.host
        if (action == Intent.ACTION_VIEW && scheme == "beetimer" && host == "auth_callback") {
            val accessToken = data.getQueryParameter("access_token")
            prefs.edit().putString(PREF_ACCESS_TOKEN, accessToken).apply()
            MainActivity.start(this, accessToken)
            return
        }

        // 3. Setup view to initiate signin process

        setContentView(R.layout.activity_sign_in)

        val signInButton = sign_in_bt
        signInButton.setOnClickListener {
            val uri = Uri.parse("https://www.beeminder.com/apps/authorize?" +
                    "client_id=$BMNDR_CLIENT_ID&redirect_uri=$BMNDR_REDIRECT_URI&response_type=token")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }
    }
}
