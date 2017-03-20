/*
 *  Twittnuker - Twitter client for Android
 *
 *  Copyright (C) 2013-2017 vanita5 <mail@vanit.as>
 *
 *  This program incorporates a modified version of Twidere.
 *  Copyright (C) 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.vanita5.twittnuker.activity.shortcut

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import org.mariotaku.kpreferences.get
import org.mariotaku.ktextension.Bundle
import org.mariotaku.ktextension.set
import de.vanita5.twittnuker.R
import de.vanita5.twittnuker.TwittnukerConstants.*
import de.vanita5.twittnuker.activity.AccountSelectorActivity
import de.vanita5.twittnuker.activity.BaseActivity
import de.vanita5.twittnuker.activity.UserSelectorActivity
import de.vanita5.twittnuker.constant.nameFirstKey
import de.vanita5.twittnuker.extension.applyTheme
import de.vanita5.twittnuker.fragment.BaseDialogFragment
import de.vanita5.twittnuker.model.ParcelableUser
import de.vanita5.twittnuker.model.UserKey
import de.vanita5.twittnuker.util.IntentUtils

class CreateQuickAccessShortcutActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            val df = QuickAccessShortcutTypeDialogFragment()
            df.show(supportFragmentManager, "quick_access_shortcut_type")
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_SELECT_ACCOUNT -> {
                if (resultCode != Activity.RESULT_OK || data == null) {
                    setResult(Activity.RESULT_CANCELED)
                    finish()
                    return
                }
                val actionType = data.getBundleExtra(EXTRA_EXTRAS)?.getString(EXTRA_TYPE)
                val accountKey = data.getParcelableExtra<UserKey>(EXTRA_ACCOUNT_KEY) ?: run {
                    setResult(Activity.RESULT_CANCELED)
                    finish()
                    return
                }
                when (actionType) {
                    "user" -> {
                        val selectUserIntent = Intent(this, UserSelectorActivity::class.java)
                        selectUserIntent.putExtra(EXTRA_ACCOUNT_KEY, accountKey)
                        startActivityForResult(selectUserIntent, REQUEST_SELECT_USER)
                    }
                    else -> {
                        setResult(Activity.RESULT_CANCELED)
                        finish()
                    }
                }
            }
            REQUEST_SELECT_USER -> {
                if (resultCode != Activity.RESULT_OK || data == null) {
                    setResult(Activity.RESULT_CANCELED)
                    finish()
                    return
                }
                val user = data.getParcelableExtra<ParcelableUser>(EXTRA_USER) ?: run {
                    setResult(Activity.RESULT_CANCELED)
                    finish()
                    return
                }

                val launchIntent = IntentUtils.userProfile(user.account_key, user.key,
                        user.screen_name, profileUrl = user.extras?.statusnet_profile_url)
                val icon = Intent.ShortcutIconResource.fromContext(this, R.mipmap.ic_launcher)
                setResult(Activity.RESULT_OK, Intent().apply {
                    putExtra(Intent.EXTRA_SHORTCUT_INTENT, launchIntent)
                    putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon)
                    putExtra(Intent.EXTRA_SHORTCUT_NAME, userColorNameManager.getDisplayName(user,
                            preferences[nameFirstKey]))
                })
                finish()
            }
        }
    }

    private fun onItemSelected(which: Int) {
        val actionType = resources.getStringArray(R.array.values_quick_access_shortcut_types)[which]
        val selectAccountIntent = Intent(this, AccountSelectorActivity::class.java)
        selectAccountIntent.putExtra(EXTRA_EXTRAS, Bundle {
            this[EXTRA_TYPE] = actionType
        })
        if (actionType == "list") {
            selectAccountIntent.putExtra(EXTRA_ACCOUNT_HOST, USER_TYPE_TWITTER_COM)
        }
        startActivityForResult(selectAccountIntent, REQUEST_SELECT_ACCOUNT)
    }

    class QuickAccessShortcutTypeDialogFragment : BaseDialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val builder = AlertDialog.Builder(context)
            builder.setItems(R.array.entries_quick_access_shortcut_types) { dialog, which ->
                (activity as CreateQuickAccessShortcutActivity).onItemSelected(which)
            }
            return builder.create().apply {
                setOnShowListener { it ->
                    it as AlertDialog
                    it.applyTheme()
                }
            }
        }
    }
}