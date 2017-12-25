package me.cpele.beetimer.ui

import android.annotation.SuppressLint
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.animation.AnimationUtils
import kotlinx.android.synthetic.main.activity_main.*
import me.cpele.beetimer.R
import me.cpele.beetimer.repository.BeeRepository

private const val ARG_ACCESS_TOKEN = "ACCESS_TOKEN"

class MainActivity : AppCompatActivity() {

    private lateinit var mAdapter: GoalAdapter
    private var mMenu: Menu? = null

    private lateinit var repository: BeeRepository

    private val extraAuthToken
        get() = intent.getStringExtra(ARG_ACCESS_TOKEN)

    companion object {
        fun start(context: Context, token: String) {
            val intent = Intent(context, MainActivity::class.java)
            intent.putExtra(ARG_ACCESS_TOKEN, token)
            context.startActivity(intent)
        }
    }

    private val viewModel: MainViewModel
        get() = ViewModelProviders
                .of(this, MainViewModel.Factory(repository, extraAuthToken))
                .get(MainViewModel::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(localClassName, "onCreate")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        repository = CustomApp.instance.beeRepository

        mAdapter = GoalAdapter()
        main_rv.adapter = mAdapter

        viewModel.status.observe(this, Observer {
            Log.d(localClassName, "Activity received status: $it")
            triggerSyncStatus(it ?: MainViewModel.Status.LOADING)
        })

        viewModel.goals.observe(this, Observer {
            Log.d(localClassName, "Activity received goals: $it")
            mAdapter.refresh(it ?: emptyList())
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        Log.d(localClassName, "onCreateOptionsMenu")
        val displayMenu = super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.main_options_menu, menu)
        mMenu = menu
        viewModel.refresh()
        return displayMenu
    }

    private fun triggerSyncStatus(status: MainViewModel.Status) = when (status) {
        MainViewModel.Status.SUCCESS -> succeedSyncAnim()
        MainViewModel.Status.LOADING -> startSyncAnim()
        MainViewModel.Status.FAILURE -> failSyncAnim()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        Log.d(localClassName, "onOptionsItemSelected")
        return when (item?.itemId) {
            R.id.main_menu_sync -> {
                viewModel.refresh()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    @SuppressLint("InflateParams")
    private fun startSyncAnim() {

        Log.d(localClassName, "startSyncAnim")

        val syncAnimation = AnimationUtils.loadAnimation(this, R.anim.anim_sync)
        val syncActionView = LayoutInflater.from(this).inflate(R.layout.view_action_sync, null)
        syncActionView.startAnimation(syncAnimation)

        val item = mMenu?.findItem(R.id.main_menu_sync)
        item?.actionView = syncActionView
        item?.setIcon(R.drawable.ic_sync_problem_white_24dp)
    }

    private fun succeedSyncAnim() {
        Log.d(localClassName, "succeedSyncAnim")
        val item = mMenu?.findItem(R.id.main_menu_sync)
        item?.actionView?.clearAnimation()
        item?.actionView = null
        item?.setIcon(R.drawable.ic_sync_white_24dp)
    }

    private fun failSyncAnim() {
        Log.d(localClassName, "failSyncAnim")
        val item = mMenu?.findItem(R.id.main_menu_sync)
        item?.actionView?.clearAnimation()
        item?.actionView = null
        item?.setIcon(R.drawable.ic_sync_problem_white_24dp)
    }
}

