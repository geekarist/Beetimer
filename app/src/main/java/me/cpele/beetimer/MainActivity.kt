package me.cpele.beetimer

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    val BMNDR_CLIENT_ID = "30zxgk213ellu3dj730wto3qj"
    val BMNDR_REDIRECT_URI = "beetimer://auth_callback"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val signInButton = main_bt_sign_in
        signInButton.setOnClickListener {
            val uri = Uri.parse("https://www.beeminder.com/apps/authorize?" +
                    "client_id=$BMNDR_CLIENT_ID&redirect_uri=$BMNDR_REDIRECT_URI&response_type=token")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }
    }

}