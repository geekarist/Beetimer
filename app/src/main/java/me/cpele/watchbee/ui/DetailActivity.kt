package me.cpele.watchbee.ui

import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import me.cpele.watchbee.R
import me.cpele.watchbee.databinding.ActivityDetailBinding

class DetailActivity : AppCompatActivity() {

    companion object {

        private const val ARG_SLUG = "ARG_SLUG"

        fun start(context: Context, slug: String) {
            val intent = Intent(context, DetailActivity::class.java)
            intent.putExtra(ARG_SLUG, slug)
            context.startActivity(intent)
        }
    }

    val slug: String
        get () {
            return intent.getStringExtra(ARG_SLUG)
                    ?: throw IllegalStateException(
                            "Slug should not be null. Did you use the start() method for instantiation?")
        }

    private val viewModel: DetailViewModel
        get() {
            return ViewModelProviders.of(
                    this,
                    DetailViewModel.Factory(
                            CustomApp.instance.beeRepository,
                            slug
                    )
            ).get(DetailViewModel::class.java)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = layoutInflater.inflate(R.layout.activity_detail, null, false)

        val binding = ActivityDetailBinding.bind(view)
        binding.setLifecycleOwner(this)
        binding.model = viewModel

        setContentView(view)
        supportActionBar?.subtitle = intent.getStringExtra(ARG_SLUG)
    }

    private val runnableForceRefresh: Runnable = object : Runnable {
        override fun run() {
            viewModel.forceRefresh()
            Handler().postDelayed(this, 1000)
        }
    }

    override fun onResume() {
        super.onResume()
        Handler().post(runnableForceRefresh)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onPause() {
        super.onPause()
        Handler().removeCallbacks(runnableForceRefresh)
    }
}