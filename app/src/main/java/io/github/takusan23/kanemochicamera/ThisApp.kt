package io.github.takusan23.kanemochicamera

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.net.toUri
import kotlinx.android.synthetic.main.activity_this_app.*
import java.net.URL

class ThisApp : AppCompatActivity() {

    /*
    * 作者へのアクセス（いる？
    * */
    val twitter = "https://twitter.com/takusan__23"
    val mastodon = "https://best-friends.chat/@takusan_23"
    val github = "https://github.com/takusan23/KanemochiCamera"

    /*
    * バージョン
    * */
    val version = "1.0 2019/11/03"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_this_app)

        this_app_version.text = version

        this_app_twitter.setOnClickListener {
            lunchBrowser(twitter)
        }
        this_app_mastodon.setOnClickListener {
            lunchBrowser(mastodon)
        }
        this_app_github.setOnClickListener {
            lunchBrowser(github)
        }

    }

    fun lunchBrowser(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, url.toUri())
        startActivity(intent)
    }

}
