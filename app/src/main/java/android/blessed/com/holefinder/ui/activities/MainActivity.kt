package android.blessed.com.holefinder.ui.activities

import android.blessed.com.holefinder.R
import android.os.Bundle
import android.blessed.com.holefinder.ui.fragments.ProfileFragment
import android.blessed.com.holefinder.ui.fragments.RoutsFragment
import android.blessed.com.holefinder.ui.fragments.StatisticsFragment
import androidx.fragment.app.Fragment
import com.arellomobile.mvp.MvpAppCompatActivity

import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : MvpAppCompatActivity() {

    private var routsFragment : RoutsFragment = RoutsFragment()
    private var statisticsFragment : StatisticsFragment = StatisticsFragment()
    private var profileFragment : ProfileFragment = ProfileFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setBottomNavigationBar()

        if (savedInstanceState != null) {
            routsFragment = supportFragmentManager.getFragment(savedInstanceState, "Routs") as RoutsFragment
            statisticsFragment = supportFragmentManager.getFragment(savedInstanceState, "Routs") as StatisticsFragment
            profileFragment = supportFragmentManager.getFragment(savedInstanceState, "Routs") as ProfileFragment
        }

        // экран открывается с фрагмента карты
        replaceFragment(routsFragment)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        // сохраняем состояния фрагментов
        if (routsFragment.isAdded) {
            supportFragmentManager.putFragment(outState, "Routs", routsFragment)
        }

        if (routsFragment.isAdded) {
            supportFragmentManager.putFragment(outState, "Statistics", statisticsFragment)
        }

        if (routsFragment.isAdded) {
            supportFragmentManager.putFragment(outState, "Profile", profileFragment)
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_container, fragment)
        fragmentTransaction.commit()
    }

    private fun setBottomNavigationBar() {
        bottom_navigation_view.setOnNavigationItemSelectedListener { item ->
            when(item.itemId) {
                R.id.bottom_navigation_routs -> {
                    replaceFragment(routsFragment)
                    return@setOnNavigationItemSelectedListener true
                }

                R.id.bottom_navigation_statistics -> {
                    replaceFragment(statisticsFragment)
                    return@setOnNavigationItemSelectedListener true
                }

                R.id.bottom_navigation_profile -> {
                    replaceFragment(profileFragment)
                    return@setOnNavigationItemSelectedListener true
                }
                else -> return@setOnNavigationItemSelectedListener false
            }
        }
    }
}
