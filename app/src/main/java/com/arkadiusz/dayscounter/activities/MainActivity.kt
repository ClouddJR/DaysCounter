package com.arkadiusz.dayscounter.activities

import PreferenceUtils.defaultPrefs
import PreferenceUtils.get
import PreferenceUtils.set
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.arkadiusz.dayscounter.R
import com.arkadiusz.dayscounter.adapters.ViewPagerAdapter
import com.arkadiusz.dayscounter.fragments.FutureFragment
import com.arkadiusz.dayscounter.fragments.PastFragment
import com.arkadiusz.dayscounter.purchaseutils.IabHelper
import com.arkadiusz.dayscounter.repositories.DatabaseProvider
import com.arkadiusz.dayscounter.repositories.DatabaseRepository
import com.arkadiusz.dayscounter.repositories.UserRepository
import com.arkadiusz.dayscounter.settings.SettingsActivity
import com.arkadiusz.dayscounter.utils.PurchasesUtils
import com.arkadiusz.dayscounter.utils.PurchasesUtils.displayPremiumInfoDialog
import com.arkadiusz.dayscounter.utils.PurchasesUtils.isPremiumUser
import com.arkadiusz.dayscounter.utils.ThemeUtils.getThemeFromPreferences
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.startActivity


class MainActivity : AppCompatActivity() {


    private val databaseRepository = DatabaseProvider.provideRepository()
    private val userRepository = UserRepository()

    private lateinit var viewPagerAdapter: ViewPagerAdapter

    private lateinit var prefs: SharedPreferences
    private lateinit var helper: IabHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(getThemeFromPreferences(false, this))
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initRealm()
        logEvents()
        setUpPreferences()
        setUpToolbar()
        setUpViewPager()
        setUpFABClickListener()
        checkForPurchases()
        //showChangelog()
    }


    private fun initRealm() {
        DatabaseRepository.RealmInitializer.initRealm(applicationContext)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        hideRemoveButtonIfPurchased(menu)
        return true
    }

    private fun hideRemoveButtonIfPurchased(menu: Menu?) {
        if (isPremiumUser(this)) {
            menu?.removeItem(R.id.action_remove_ads)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {

            R.id.action_syncing -> {
                if (isPremiumUser(this)) {
                    if (!userRepository.isLoggedIn()) {
                        startActivity<LoginActivity>()
                        finish()
                    } else {
                        displaySignOutDialog()
                    }
                } else {
                    displayPremiumInfoDialog(this)
                }
            }

            R.id.action_remove_ads -> {
                startActivity<PremiumActivity>()
            }

            R.id.action_settings -> {
                startActivity<SettingsActivity>()
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun displaySignOutDialog() {
        alert(getString(R.string.main_activity_email, userRepository.getUserEmail())) {
            positiveButton(getString(R.string.main_activity_sign_out)) {
                userRepository.signOut()
            }

            negativeButton(getString(R.string.add_activity_back_button_cancel)) {
                it.dismiss()
            }
        }.show()
    }

    private fun logEvents() {
        databaseRepository.logEvents()
    }

    private fun setUpPreferences() {
        prefs = defaultPrefs(this)
    }

    private fun setUpToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setTitle(R.string.app_name)
    }

    private fun setUpViewPager() {
        viewPagerAdapter = ViewPagerAdapter(supportFragmentManager)

        //checking the default fragment from settings
        if (prefs["default_fragment"] ?: "" == getString(R.string.main_activity_left_tab)) {
            viewPagerAdapter.addFragment(PastFragment(), getString(R.string.main_activity_left_tab))
            viewPagerAdapter.addFragment(FutureFragment(), getString(R.string.main_activity_right_tab))
        } else {
            viewPagerAdapter.addFragment(FutureFragment(), getString(R.string.main_activity_right_tab))
            viewPagerAdapter.addFragment(PastFragment(), getString(R.string.main_activity_left_tab))
        }

        viewPager.adapter = viewPagerAdapter
        viewPager.addOnPageChangeListener(object : androidx.viewpager.widget.ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                fab.show()
            }
        })
        tabLayout.setupWithViewPager(viewPager)
    }

    private fun setUpFABClickListener() {
        fab.setOnClickListener {
            val eventType = if (viewPagerAdapter.getItem(viewPager.currentItem) is PastFragment) {
                "past"
            } else {
                "future"
            }
            startActivity<AddActivity>("Event Type" to eventType)
        }
    }

    private fun checkForPurchases() {
        helper = IabHelper(this, PurchasesUtils.base64EncodedPublicKey)
        PurchasesUtils.sharedPreferences = defaultPrefs(this)
        helper.startSetup { result ->
            if (!result.isSuccess) {
                // Problem
            } else {
                try {
                    helper.queryInventoryAsync(PurchasesUtils.GotInventoryListener)
                } catch (e: IabHelper.IabAsyncInProgressException) {
                    e.printStackTrace()
                }
            }
        }
        invalidateOptionsMenu()
    }

    private fun showChangelog() {
        if (!wasDialogSeenBefore()) {

            alert(getString(R.string.changelog_dialog_content)) {
                title = getString(R.string.changelog_dialog_title)
                positiveButton("OK") {}
                negativeButton(R.string.add_activity_back_button_cancel) {}
            }.show()

            prefs["dialog-seen-202"] = true
        }
    }

    private fun wasDialogSeenBefore() = prefs["dialog-seen-202"] ?: false

}