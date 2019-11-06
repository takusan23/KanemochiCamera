package io.github.takusan23.kanemochicamera

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class PreferenceActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preference)

        supportFragmentManager.beginTransaction()
            .replace(R.id.activity_preference_linearlayout,PreferenceFragment())
            .commit()

    }
}
