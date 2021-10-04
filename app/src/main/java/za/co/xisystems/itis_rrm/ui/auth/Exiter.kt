package za.co.xisystems.itis_rrm.ui.auth

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class Exiter : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        finish()
        System.exit(0)
    }
}
