package me.cpele.fleabrainer.ui

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.LinearLayoutManager.VERTICAL
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.MenuItem
import android.view.WindowManager
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_detail.*
import me.cpele.fleabrainer.R
import me.cpele.fleabrainer.databinding.ActivityDetailBinding
import me.cpele.fleabrainer.domain.Status

class DetailActivity : AppCompatActivity() {

    companion object {

        private const val ARG_SLUG = "ARG_SLUG"
        private const val ARG_USER_NAME = "ARG_USER_NAME"

        fun start(context: Context, userName: String, slug: String) {
            val intent = Intent(context, DetailActivity::class.java)
            intent.putExtra(ARG_SLUG, slug)
            intent.putExtra(ARG_USER_NAME, userName)
            context.startActivity(intent)
        }
    }

    private val slug: String
        get () = intent.getStringExtra(ARG_SLUG)
            ?: throw IllegalStateException(
                "Slug should not be null. Did you use the start() method for instantiation?"
            )

    private val userName: String
        get () = intent.getStringExtra(ARG_USER_NAME)
            ?: throw IllegalStateException(
                "User name should not be null. Did you use the start() method for instantiation?"
            )

    private lateinit var adapter: DetailAdapter

    private val viewModel: DetailViewModel
        get() = ViewModelProviders.of(
            this,
            DetailViewModel.Factory(
                application,
                CustomApp.instance.beeRepository,
                userName,
                slug
            )
        ).get(DetailViewModel::class.java)

    private val handler = Handler()

    private val runnableForceRefresh: Runnable = object : Runnable {
        override fun run() {
            viewModel.forceRefresh()
            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = layoutInflater.inflate(R.layout.activity_detail, null, false)

        val detailBinding = ActivityDetailBinding.bind(view)
        detailBinding.setLifecycleOwner(this)
        detailBinding.model = viewModel
        detailBinding.listener = this

        detailBinding.detailViewTimer.model = viewModel
        detailBinding.detailViewTimer.listener = this

        setContentView(view)
        supportActionBar?.subtitle = intent.getStringExtra(ARG_SLUG)

        adapter = DetailAdapter()
        detail_datapoints.adapter = adapter
        detail_datapoints.layoutManager = LinearLayoutManager(this, VERTICAL, false)

        viewModel.status.observe(this, Observer {
            Log.d(localClassName, "Activity received status: $it")
            if (it?.status == Status.AUTH_ERROR) {
                SignInActivity.start(context = this@DetailActivity, clearToken = true)
            }
            it?.message?.apply {
                Toast.makeText(this@DetailActivity, this, Toast.LENGTH_LONG).show()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        handler.post(runnableForceRefresh)

        viewModel.findDatapoints(this).observe(this, Observer {
            it?.apply {
                adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
                    override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                        detail_datapoints.scrollToPosition(positionStart)
                        adapter.unregisterAdapterDataObserver(this)
                    }
                })
                adapter.submitList(
                    this.sortedWith(kotlin.Comparator { p1, p2 ->
                        val pendingComparison = p2.pending.compareTo(p1.pending)
                        if (pendingComparison != 0) {
                            pendingComparison
                        } else {
                            p2.updatedAt.compareTo(p1.updatedAt)
                        }
                    })
                )
            }
        })

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean = when (item?.itemId) {
        android.R.id.home -> {
            finish()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onPause() {
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        super.onPause()
        handler.removeCallbacks(runnableForceRefresh)
    }
}