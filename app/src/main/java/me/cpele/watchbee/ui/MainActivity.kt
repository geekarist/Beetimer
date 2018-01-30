package me.cpele.watchbee.ui

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
import android.view.WindowManager
import android.view.animation.AnimationUtils
import kotlinx.android.synthetic.main.activity_main.*
import me.cpele.watchbee.R
import me.cpele.watchbee.domain.GoalTiming
import me.cpele.watchbee.domain.Status
import me.cpele.watchbee.repository.BeeRepository

private const val ARG_ACCESS_TOKEN = "ACCESS_TOKEN"

class MainActivity : AppCompatActivity(), GoalViewHolder.Listener {

    private lateinit var mAdapter: GoalAdapter
    private var mMenu: Menu? = null

    private lateinit var repository: BeeRepository

    private val extraAuthToken
        get() = intent.getStringExtra(ARG_ACCESS_TOKEN)

    companion object {
        fun start(context: Context, token: String) {
            val intent = Intent(context, MainActivity::class.java)
            intent.putExtra(ARG_ACCESS_TOKEN, token)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
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

        mAdapter = GoalAdapter(this)
        main_rv.adapter = mAdapter

        supportActionBar?.title = getString(R.string.app_name)

        if (savedInstanceState == null) {
            viewModel.refresh()
            sendBroadcast(BeeJobReceiver.CustomIntent(extraAuthToken))
        }
    }

    override fun onPersist(goalTiming: GoalTiming) {
        viewModel.persist(goalTiming)
    }

    override fun onSubmit(goalTiming: GoalTiming) {
        viewModel.submit(this, goalTiming)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        Log.d(localClassName, "onCreateOptionsMenu")
        val displayMenu = super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.main_options_menu, menu)
        mMenu = menu

        viewModel.status.observe(this, Observer {
            Log.d(localClassName, "Activity received status: $it")
            triggerSyncStatus(it?.status ?: Status.LOADING)
        })

        viewModel.goalTimings.observe(this, Observer {
            Log.d(localClassName, "Activity received goals: $it")
            supportActionBar?.subtitle = it?.firstOrNull()?.user
            mAdapter.refresh(it ?: emptyList())
        })

        viewModel.isAnyTimerRunning.observe(this, Observer {
            Log.d(localClassName, "Running status changed for a timer to: $it.value")
            if (it == true) window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            else window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        })

        return displayMenu
    }

    private fun triggerSyncStatus(status: Status) = when (status) {
        Status.SUCCESS -> succeedSyncAnim()
        Status.LOADING -> startSyncAnim()
        Status.FAILURE -> failSyncAnim()
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

