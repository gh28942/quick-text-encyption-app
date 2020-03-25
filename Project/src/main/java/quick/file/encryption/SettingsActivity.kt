package quick.file.encryption

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : AppCompatActivity()  {

    private val PREFS_FILENAME = "quick.file.encryption.prefs"

    //read the password hint if the user provided one
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        //get String
        val sharedPreferences = this.getSharedPreferences(PREFS_FILENAME, 0)
        val keyHint = sharedPreferences.getString("key_hint", "")
        editTextKeyHint.setText(keyHint)
    }

    //store password hint when exiting this view
    override fun onBackPressed() {
        super.onBackPressed()

        //save String
        val sharedPreferences = this.getSharedPreferences(PREFS_FILENAME, 0)
        val editor = sharedPreferences.edit()
        editor.putString("key_hint", editTextKeyHint.text.toString())
        editor.apply()
    }
}