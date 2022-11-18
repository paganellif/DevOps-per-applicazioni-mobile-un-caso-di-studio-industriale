package it.filo.maggioliebook.android

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import it.filo.maggioliebook.android.databinding.ActivityMainBinding
import it.filo.maggioliebook.repository.user.UserRepository
import it.filo.maggioliebook.usecase.user.CheckUserLoggedUseCase
import it.filo.maggioliebook.usecase.user.GetUserInfoUseCase
import it.filo.maggioliebook.usecase.user.UserLogoutUseCase
import kotlinx.coroutines.launch

class MainActivity: AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.appBarMain.toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navHostFragment: NavHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_content_main)
                as NavHostFragment
        navController = navHostFragment.navController

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_favorite, R.id.nav_settings, R.id.nav_about
            ), drawerLayout
        )

        // AUTH "OBSERVER"
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when(destination.id){
                R.id.nav_login -> {
                    binding.appBarMain.toolbar.visibility = View.GONE
                }
                else -> {
                    if (CheckUserLoggedUseCase().invoke()){
                        lifecycleScope.launch {
                            val loggedUser = GetUserInfoUseCase().invoke()
                            binding.navView.findViewById<TextView>(R.id.textViewUserName)
                                .text = "${loggedUser.firstName} ${loggedUser.lastName}"

                            binding.navView.findViewById<TextView>(R.id.textViewUserMail)
                                .text = loggedUser.email
                        }
                    }
                    binding.appBarMain.toolbar.visibility = View.VISIBLE
                }
            }
        }

        // LOGOUT
        val logout: TextView = navView.findViewById<LinearLayout>(R.id.logout)
            .findViewById(R.id.text_logout)
        logout.setOnClickListener {
            UserLogoutUseCase().invoke()
            drawerLayout.close()
            navController.popBackStack(R.id.nav_home, false)
            navController.navigate(R.id.action_home_to_login)
        }

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}