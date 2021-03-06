package me.cpele.fleabrainer.ui

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import me.cpele.fleabrainer.R
import me.cpele.fleabrainer.domain.GoalTiming
import me.cpele.fleabrainer.domain.Status
import me.cpele.fleabrainer.repository.BeeRepository

private const val ARG_ACCESS_TOKEN = "ACCESS_TOKEN"

class MainActivity : AppCompatActivity(), GoalGeneralViewHolder.Listener {

    private lateinit var mAdapter: GoalAdapter

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

        supportActionBar?.title = getString(R.string.app_name)

        if (savedInstanceState == null) {
            viewModel.refresh()
            sendBroadcast(BeeJobReceiver.CustomIntent(extraAuthToken))
        }

        mAdapter = GoalAdapter(this)
        main_rv.adapter = mAdapter

        main_sr.setOnRefreshListener { viewModel.refresh() }

        viewModel.status.observe(this, Observer {
            Log.d(localClassName, "Activity received status: $it")
            when (it?.status) {
                Status.AUTH_ERROR -> {
                    SignInActivity.start(
                        context = this@MainActivity,
                        clearToken = true
                    )
                    main_sr.isRefreshing = false
                }
                Status.LOADING -> main_sr.isRefreshing = true
                else -> main_sr.isRefreshing = false
            }
            it?.message?.apply {
                Toast.makeText(this@MainActivity, this, Toast.LENGTH_SHORT).show()
            }
        })

        viewModel.goalTimings.observe(this, Observer {
            Log.d(localClassName, "Activity received goals: $it")
            supportActionBar?.subtitle = it?.firstOrNull()?.user
            val timings = it?.sorted() ?: emptyList()
            mAdapter.submitList(timings)
            mAdapter.firstRunningItemPosition()?.let { pos ->
                handler.postDelayed({ main_rv.smoothScrollToPosition(pos) }, 1000)
            }
        })
    }

    override fun onResume() {
        super.onResume()

        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            main_rv.layoutManager = LinearLayoutManager(this)
        } else {
            main_rv.layoutManager = GridLayoutManager(this, 2)
            main_rv.addItemDecoration(CenterMarginFix())
        }

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun onPersist(goalTiming: GoalTiming) {
        viewModel.persist(goalTiming)
    }

    override fun onSubmit(goalTiming: GoalTiming) {
        viewModel.submit(this, goalTiming)
    }

    private val handler: Handler by lazy { Handler() }

    override fun onOpen(goalTiming: GoalTiming) {
        DetailActivity.start(this, goalTiming.user, goalTiming.goal.slug)
    }

    override fun onPause() {
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        super.onPause()
    }

    override fun toggleThenStopOthers(slug: String) = viewModel.toggleThenStopOthers(slug)
}

