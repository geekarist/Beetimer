package me.cpele.watchbee.ui

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_sign_in.*
import me.cpele.watchbee.R

private const val BMNDR_CLIENT_ID = "31ictv1j0ieiwnmcjxrs3ncsr"
private const val BMNDR_REDIRECT_URI = "watchbee://auth_callback"

class SignInActivity : AppCompatActivity() {

    companion object {
        const val PREF_ACCESS_TOKEN = "ACCESS_TOKEN"
        private const val EXTRA_CLEAR_TOKEN = "EXTRA_CLEAR_TOKEN"

        fun start(context: Activity, clearToken: Boolean) {
            val intent = Intent(context, SignInActivity::class.java)
            intent.putExtra(EXTRA_CLEAR_TOKEN, clearToken)
            context.startActivity(intent)
            context.finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = PreferenceManager.getDefaultSharedPreferences(this)

        // 1. Try getting token from preferences
        if (trySavedToken(prefs)) return

        // 2. Try getting token from intent
        if (tryProvidedToken(prefs, intent)) return

        // 3. Setup view to initiate signin process

        setContentView(R.layout.activity_sign_in)

        val signInButton = sign_in_bt
        signInButton.setOnClickListener {
            val uri = Uri.parse(
                    "https://www.beeminder.com/apps/authorize?" +
                            "client_id=${BMNDR_CLIENT_ID}&" +
                            "redirect_uri=${BMNDR_REDIRECT_URI}&" +
                            "response_type=token"
            )
            val intent = Intent(Intent.ACTION_VIEW, uri)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        tryProvidedToken(prefs, intent)
    }

    private fun tryProvidedToken(prefs: SharedPreferences, intent: Intent?): Boolean {
        val data = intent?.data
        val action = intent?.action
        val scheme = data?.scheme
        val host = data?.host
        if (action == Intent.ACTION_VIEW && scheme == "watchbee" && host == "auth_callback") {
            val accessToken = data.getQueryParameter("access_token")
            prefs.edit().putString(PREF_ACCESS_TOKEN, accessToken).apply()
            MainActivity.start(this, accessToken)
            finish()
            return true
        }
        return false
    }

    private fun trySavedToken(prefs: SharedPreferences): Boolean {

        if (intent.getBooleanExtra(EXTRA_CLEAR_TOKEN, false)) {
            prefs.edit().remove(PREF_ACCESS_TOKEN).apply()
        } else if (prefs.contains(PREF_ACCESS_TOKEN)) {
            val token = prefs.getString(PREF_ACCESS_TOKEN, null)
            MainActivity.start(this, token)
            finish()
            return true
        }
        return false
    }
}
