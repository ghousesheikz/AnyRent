package com.shaikhomes.smartdiary

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.shaikhomes.anyrent.R
import com.shaikhomes.anyrent.databinding.ActivityMainBinding
import com.shaikhomes.smartdiary.ui.utils.PrefManager


class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    protected val prefmanager: PrefManager by lazy {
        PrefManager(this)
    }
    private var permission = arrayOf(android.Manifest.permission.CALL_PHONE)
    val MY_PERMISSIONS_REQUEST_CALL_PHONE = 102
    val getNumbers = arrayListOf<String>("8688589282", "9182488911", "9949192977", "9652715115")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        binding.appBarMain.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_manageLeads, R.id.nav_employeeData, R.id.nav_scheduleOn
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        //navView.setupWithNavController(navController)
        navView.visibility = View.GONE
        supportActionBar?.setDisplayHomeAsUpEnabled(false);
        supportActionBar?.setHomeButtonEnabled(false);
//        navView.getHeaderView(0).findViewById<TextView>(R.id.userName).apply {
//            text = prefmanager.userData?.UserName
//        }
//        navView.getHeaderView(0).findViewById<TextView>(R.id.userEmail).apply {
//            text = prefmanager.userData?.Address
//        }
//        if (prefmanager.userData?.IsAdmin == "2") {
//            hideEmpMenu(navView)
//        }
        binding.appBarMain.contentMain.bottomNavigation.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.dashboard -> {
                    navController.navigate(R.id.nav_home)
                    true
                }

                R.id.tenants -> {
                    startActivity(Intent(this, TenantsActivity::class.java))
                    true
                }

                else -> {
                    true
                }
            }
        }
    }

    private fun hideEmpMenu(navView: NavigationView) {
        val empMenu = navView.menu
        empMenu.findItem(R.id.nav_employeeData).setVisible(false)
    }

    private fun askPermission() {
        ActivityCompat.requestPermissions(
            this,
            permission,
            MY_PERMISSIONS_REQUEST_CALL_PHONE
        )
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_logout) {
            AlertDialog.Builder(this).apply {
                this.setMessage("Do you want to logout?")
                this.setPositiveButton(
                    "YES"
                ) { p0, p1 ->
                    prefmanager.isLoggedIn = false
                    prefmanager.userData = null
                    p0.dismiss()
                    startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                    finish()
                }
                this.setNegativeButton(
                    "NO"
                ) { p0, p1 ->
                    p0.dismiss()
                }
                this.setCancelable(true)
                this.show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    fun setTitle(title: String) {
        supportActionBar?.title = title
    }
}