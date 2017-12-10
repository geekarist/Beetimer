package me.cpele.beetimer

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.animation.AnimationUtils
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private const val BMNDR_CLIENT_ID = "30zxgk213ellu3dj730wto3qj"
private const val BMNDR_REDIRECT_URI = "beetimer://auth_callback"
private const val PREF_ACCESS_TOKEN = "ACCESS_TOKEN"

class MainActivity : AppCompatActivity() {

    private lateinit var mAdapter: GoalAdapter
    private var mMenu: Menu? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        main_vf.displayedChild = 1

        mAdapter = GoalAdapter()
        main_rv.adapter = mAdapter

        setupSignInButton()
        handleToken()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val displayMenu = super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.main_options_menu, menu)
        Handler().post({ startSyncAnim() })
        mMenu = menu
        return displayMenu
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        return when (item?.itemId) {
            R.id.main_menu_sync -> {
                startSyncAnim()
                handleToken()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
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

    private fun handleToken() {

        val prefs = PreferenceManager.getDefaultSharedPreferences(this)

        if (prefs.contains(PREF_ACCESS_TOKEN)) {
            fetchUser(prefs.getString(PREF_ACCESS_TOKEN, null))
            return
        }

        val data = intent?.data
        val action = intent?.action
        val scheme = data?.scheme
        val host = data?.host
        if (action == Intent.ACTION_VIEW && scheme == "beetimer" && host == "auth_callback") {
            val accessToken = data.getQueryParameter("access_token")
            prefs.edit().putString(PREF_ACCESS_TOKEN, accessToken).apply()
            fetchUser(accessToken)
        }
    }

    private fun fetchUser(accessToken: String) {

        CustomApp.instance.api.getUser(accessToken).enqueue(object : Callback<User> {
            override fun onFailure(call: Call<User>?, t: Throwable?) {
                Toast.makeText(this@MainActivity, "Error retrieving user: ${t.toString()}", Toast.LENGTH_LONG).show()
                failSyncAnim()
            }

            override fun onResponse(call: Call<User>?, response: Response<User>?) {
                response?.body()?.username?.let {
                    fetchGoals(accessToken, it)
                }
            }
        })
    }

    private fun fetchGoals(accessToken: String, user: String) {

        CustomApp.instance.api.getGoals(user, accessToken).enqueue(object : Callback<List<Goal>> {
            override fun onFailure(call: Call<List<Goal>>?, t: Throwable?) {
                Toast.makeText(this@MainActivity, "Error retrieving goals: ${t.toString()}", Toast.LENGTH_LONG).show()
                failSyncAnim()
            }

            override fun onResponse(call: Call<List<Goal>>?, response: Response<List<Goal>>?) {
                response?.body()?.let {
                    mAdapter.addAll(it)
                    main_vf.displayedChild = 0
                    succeedSyncAnim()
                }
            }
        })
    }

    @SuppressLint("InflateParams")
    private fun startSyncAnim() {

        val syncAnimation = AnimationUtils.loadAnimation(this, R.anim.anim_sync)
        val syncActionView = LayoutInflater.from(this).inflate(R.layout.view_action_sync, null)
        syncActionView.startAnimation(syncAnimation)

        val item = mMenu?.findItem(R.id.main_menu_sync)
        item?.actionView = syncActionView
        item?.setIcon(R.drawable.ic_sync_problem_white_24dp)
    }

    private fun succeedSyncAnim() {
        val item = mMenu?.findItem(R.id.main_menu_sync)
        item?.actionView?.clearAnimation()
        item?.actionView = null
        item?.setIcon(R.drawable.ic_sync_white_24dp)
    }

    private fun failSyncAnim() {
        val item = mMenu?.findItem(R.id.main_menu_sync)
        item?.actionView?.clearAnimation()
        item?.actionView = null
        item?.setIcon(R.drawable.ic_sync_problem_white_24dp)
    }
}