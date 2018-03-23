package me.cpele.watchbee.ui

import android.arch.lifecycle.Observer
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import me.cpele.watchbee.databinding.ViewItemBinding

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
        val binding = ViewItemBinding.inflate(layoutInflater)
        CustomApp.instance.beeRepository
                .asyncFindGoalTimingBySlug(slug)
                .observe(this, Observer {
                    binding.model = it
                    setContentView(binding.root)
                })
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
