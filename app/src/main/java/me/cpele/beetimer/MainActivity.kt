package me.cpele.beetimer

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
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
        setupTokenHandler()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val displayMenu = super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.main_options_menu, menu)
        mMenu = menu
        return displayMenu
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        return when (item?.itemId) {
            R.id.main_menu_sync -> {
                startSyncAnim()
                setupTokenHandler()
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

    private fun setupTokenHandler() {

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
                failSyncAnim()
            }

            override fun onResponse(call: Call<User>?, response: Response<User>?) {
                response?.body()?.username?.let { fetchGoals(accessToken, it) }
            }
        })
    }

    private fun fetchGoals(accessToken: String, user: String) {

        CustomApp.instance.api.getGoals(user, accessToken).enqueue(object : Callback<List<Goal>> {
            override fun onFailure(call: Call<List<Goal>>?, t: Throwable?) {
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

    private fun startSyncAnim() =
            mMenu?.findItem(R.id.main_menu_sync)?.setIcon(R.drawable.ic_sync_problem_white_24dp)

    private fun succeedSyncAnim() =
            mMenu?.findItem(R.id.main_menu_sync)?.setIcon(R.drawable.ic_sync_white_24dp)

    private fun failSyncAnim() =
            mMenu?.findItem(R.id.main_menu_sync)?.setIcon(R.drawable.ic_sync_problem_white_24dp)
}