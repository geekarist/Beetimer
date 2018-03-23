package me.cpele.watchbee.ui

import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = layoutInflater.inflate(R.layout.activity_detail, null, false)
        val binding = ActivityDetailBinding.bind(view)
        binding.setLifecycleOwner(this)
        binding.model = ViewModelProviders.of(
                this,
                DetailViewModel.Factory(
                        CustomApp.instance.beeRepository,
                        slug
                )
        ).get(DetailViewModel::class.java)
        setContentView(view)
        supportActionBar?.subtitle = intent.getStringExtra(ARG_SLUG)
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
}
