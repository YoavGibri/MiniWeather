package com.yoavgibri.miniweather.activities

import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.preference.*
import android.view.MenuItem
import androidx.core.app.NavUtils
import com.yoavgibri.miniweather.*
import com.yoavgibri.miniweather.managers.SettingsManager
import com.yoavgibri.miniweather.views.NumberPickerPreference
import java.lang.ClassCastException

/**
 * A [PreferenceActivity] that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 *
 * See [Android Design: Settings](http://developer.android.com/design/patterns/settings.html)
 * for design guidelines and the [Settings API Guide](http://developer.android.com/guide/topics/ui/settings.html)
 * for more information on developing a Settings UI.
 */
class SettingsActivity : AppCompatPreferenceActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupActionBar()
        startPreferencesFragment()
    }

    private fun startPreferencesFragment() {
        fragmentManager.beginTransaction().replace(android.R.id.content, DataSyncPreferenceFragment()).commit()
    }

    private fun setupActionBar() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onMenuItemSelected(featureId: Int, item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            if (!super.onMenuItemSelected(featureId, item)) {
                NavUtils.navigateUpFromSameTask(this)
            }
            return true
        }
        return super.onMenuItemSelected(featureId, item)
    }

    override fun onIsMultiPane(): Boolean {
        return isXLargeTablet(this)
    }

    override fun isValidFragment(fragmentName: String): Boolean {
        return PreferenceFragment::class.java.name == fragmentName
                || DataSyncPreferenceFragment::class.java.name == fragmentName
    }


    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    class DataSyncPreferenceFragment : PreferenceFragment() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.preferences)
            setHasOptionsMenu(true)


            val timeFormatPreference = findPreference(getString(R.string.sp_key_time_format)) as ListPreference
            bindPreferenceSummaryToValue(timeFormatPreference)
           // timeFormatPreference.setValue(SettingsManager.getTimeFormat())

            val unitFormatPreference = findPreference(getString(R.string.sp_key_units_format)) as ListPreference
            bindPreferenceSummaryToValue(unitFormatPreference)
           // unitFormatPreference.setValue(SettingsManager.getUnitFormat())

            val refreshIntervalPreference = findPreference(getString(R.string.sp_key_refresh_interval)) as NumberPickerPreference
            bindPreferenceSummaryToValue(refreshIntervalPreference)
           // refreshIntervalPreference.setValue(SettingsManager.getIntervalsMinutes())

            findPreference(getString(R.string.pref_key_send_feedback)).setOnPreferenceClickListener { sendFeedback() }
        }

        override fun onOptionsItemSelected(item: MenuItem): Boolean {
            val id = item.itemId
            if (id == android.R.id.home) {
                startActivity(Intent(activity, SettingsActivity::class.java))
                return true
            }
            return super.onOptionsItemSelected(item)
        }


        private fun sendFeedback(): Boolean {
            val address = arrayOf("yoavgibri@gmail.com")
            val intent = Intent(Intent.ACTION_SENDTO)
            intent.data = Uri.parse("mailto:") // only email apps should handle this
            intent.putExtra(Intent.EXTRA_EMAIL, address)
            intent.putExtra(Intent.EXTRA_SUBJECT, "Mini Weather - user feedback")
            if (intent.resolveActivity(context.packageManager) != null) {
                startActivity(intent)
            }

            return true
        }


    }


    companion object {

        /**
         * A preference value change listener that updates the preference's summary
         * to reflect its new value.
         */
        private val sBindPreferenceSummaryToValueListener = Preference.OnPreferenceChangeListener { preference, value ->
            val stringValue = value.toString()

            when (preference) {
                is ListPreference -> {
                    // For list preferences, look up the correct display value in the preference's 'entries' list.
                    val index = preference.findIndexOfValue(stringValue)
                    // Set the summary to reflect the new value.
                    preference.setSummary(
                            if (index >= 0)
                                preference.entries[index]
                            else
                                null)
                }

                is NumberPickerPreference -> {
                    AlarmManagerHelper(App.context).setRecurringAlarm(stringValue.toLong())
                    preference.summary = "$stringValue Minutes"


                }
                else -> {
                    // For all other preferences, set the summary to the value's
                    // simple string representation.
                    preference.summary = stringValue
                }
            }
            true
        }

        /**
         * Helper method to determine if the device has an extra-large screen. For
         * example, 10" tablets are extra-large.
         */
        private fun isXLargeTablet(context: Context): Boolean {
            return context.resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK >= Configuration.SCREENLAYOUT_SIZE_XLARGE
        }

        /**
         * Binds a preference's summary to its value. More specifically, when the
         * preference's value is changed, its summary (line of text below the
         * preference title) is updated to reflect the value. The summary is also
         * immediately updated upon calling this method. The exact display format is
         * dependent on the type of preference.

         * @see .sBindPreferenceSummaryToValueListener
         */
        private fun bindPreferenceSummaryToValue(preference: Preference) {
            // Set the listener to watch for value changes.
            preference.onPreferenceChangeListener = sBindPreferenceSummaryToValueListener

            // Trigger the listener immediately with the preference's
            // current value.
            try {
                sBindPreferenceSummaryToValueListener.onPreferenceChange(preference, SettingsManager.pref.getString(preference.key, ""))
            } catch (e: ClassCastException) {
                sBindPreferenceSummaryToValueListener.onPreferenceChange(preference, SettingsManager.pref.getInt(preference.key, 0))
            }
        }
    }


}
