package android.blessed.com.holefinder.ui.fragments

import android.blessed.com.holefinder.R
import android.blessed.com.holefinder.ui.activities.LoginActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_profile.*
import android.content.Intent
import android.widget.CompoundButton

class ProfileFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        login_button.setOnClickListener {
            val intent = Intent(activity, LoginActivity::class.java)
            startActivity(intent)
        }

        wifi_send_data_switch.setOnCheckedChangeListener { buttonView, isChecked ->
            // делаем что-то
        }

        auto_recording_switch.setOnCheckedChangeListener { buttonView, isChecked ->
            // делаем что-то
        }
    }
}
