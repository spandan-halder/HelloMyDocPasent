package com.hellomydoc

import android.app.Activity
import android.content.ContextWrapper
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.provider.Settings
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.startup.AppInitializer
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.hellomydoc.activities.AbstractActivity
import com.hellomydoc.activities.LoginActivity
import com.hellomydoc.activities.RegisterActivity
import com.hellomydoc.chat.ChatBox
import com.hellomydoc.chat.app.ChatApp
import dagger.hilt.android.HiltAndroidApp
import net.danlew.android.joda.JodaTimeInitializer

@HiltAndroidApp
class App : ChatApp() {
    companion object{
        var currentActivity: AbstractActivity? = null
        var glogin: GLogin? = null
        lateinit var instance: App
    }

    override fun onCreateActions() {
        instance = this
        instance = this
        Metar.initialize(instance)
        AppInitializer.getInstance(this).initializeComponent(JodaTimeInitializer::class.java)
        // Initialize the Prefs class
        Prefs.Builder()
            .setContext(this)
            .setMode(ContextWrapper.MODE_PRIVATE)
            .setPrefsName(packageName)
            .setUseDefaultSharedPreference(true)
            .build()

        registerActivityCallback()
    }

    private fun registerActivityCallback() {
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, bundle: Bundle?) {
                if(activity is AbstractActivity){
                    currentActivity = activity
                }
                if((activity is LoginActivity)||(activity is RegisterActivity))
                {
                    App.glogin = GLogin()
                }
                else{
                    App.glogin = null
                }
            }

            override fun onActivityStarted(activity: Activity) {

            }

            override fun onActivityResumed(activity: Activity) {
                if(activity is AbstractActivity){
                    currentActivity = activity
                }
            }

            override fun onActivityPaused(activity: Activity) {

            }

            override fun onActivityStopped(activity: Activity) {

            }

            override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) {

            }

            override fun onActivityDestroyed(activity: Activity) {

            }
        })

    }

    override fun getPrefString(key: String): String? {
        return Prefs.getString(key)
    }

    override fun setFcmTokenSynced(b: Boolean) {
        Prefs.putBoolean("fcm_synced",b)
    }

    override fun setAndroidId(id: String) {
        Prefs.putString(Settings.Secure.ANDROID_ID,id)
    }

    override fun fetchFcmToken(callback: (String) -> Unit) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                callback("")
            }
            val token = task.result
            callback(token)
        })
    }

    override fun setFcmToken(token: String) {
        Prefs.putString(Constants.FCM_TOKEN,token)
    }

    override val myIdKey: String
        get() = Constants.MY_ID
    override val fcmSynced: Boolean
        get() = Prefs.getBoolean(Constants.FCM_SYNCED)
    override val fcmToken: String?
        get() = Prefs.getString("fcm_token")
    override val userId: String
        get() = chatUserId
    override val peerNotFoundText: String
        get() = R.string.doctor_not_found.string
    override val userNotFoundString: String?
        get() = R.string.user_not_found.string
    override val userIdKey: String
        get() = Constants.USER_UID
    override val peerIdKey: String
        get() = Constants.PEER_ID_KEY

    ////////////////////////////
    val chatUserId: String
        get(){
            val userId = Prefs.getString(Constants.USER_UID)?:""
            return if(userId.isEmpty){
                ""
            } else{
                "client_$userId"
            }
        }

    fun stringResource(@StringRes stringId: Int):String
    {
        return try {
            resources.getString(stringId)
        } catch (e: Exception) {
            ""
        }
    }

    fun drawableResource(@DrawableRes drawableId: Int): Drawable?
    {
        return try {
            ContextCompat.getDrawable(this,drawableId)
        } catch (e: Exception) {
            null
        }
    }

    fun dimensionResource(@DimenRes dimenRes: Int):Float?
    {
        return try {
            resources.getDimension(dimenRes)
        } catch (e: Exception) {
            0f
        }
    }

    fun colorResource(@ColorRes colorId: Int):Int
    {
        return try {
            ContextCompat.getColor(this,colorId)
        } catch (e: Exception) {
            Color.TRANSPARENT
        }
    }

    fun syncFcmToken() {
        ChatBox.updateFcmToken(this)
    }

}
/*

@HiltAndroidApp
class App : Application(), AppDelegate {

    val chatUserId: String
    get(){
        val userId = Prefs.getString(Constants.USER_UID)?:""
        return if(userId.isEmpty){
            ""
        } else{
            "client_$userId"
        }
    }

    companion object{
        var currentActivity: AbstractActivity? = null
        var glogin: GLogin? = null
        lateinit var instance: App
    }

    override fun onCreate() {
        super.onCreate()
        ChatBox.onAppInit(this)
        instance = this
        Metar.initialize(instance)
        AppInitializer.getInstance(this).initializeComponent(JodaTimeInitializer::class.java)
        // Initialize the Prefs class
        Prefs.Builder()
            .setContext(this)
            .setMode(ContextWrapper.MODE_PRIVATE)
            .setPrefsName(packageName)
            .setUseDefaultSharedPreference(true)
            .build()

        updateFcmTokenIfNeeded()

        registerActivityCallback()
    }

    @SuppressLint("HardwareIds")
    private fun updateFcmTokenIfNeeded() {
        ChatBox.updateFcmTokenFromApp(this)
    }

    private fun registerActivityCallback() {
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, bundle: Bundle?) {
                if(activity is AbstractActivity){
                    currentActivity = activity
                }
                if((activity is LoginActivity)||(activity is RegisterActivity))
                {
                    App.glogin = GLogin()
                }
                else{
                    App.glogin = null
                }
            }

            override fun onActivityStarted(activity: Activity) {

            }

            override fun onActivityResumed(activity: Activity) {
                if(activity is AbstractActivity){
                    currentActivity = activity
                }
            }

            override fun onActivityPaused(activity: Activity) {

            }

            override fun onActivityStopped(activity: Activity) {

            }

            override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) {

            }

            override fun onActivityDestroyed(activity: Activity) {

            }
        })

    }
    */
/**globally accessible resources by ID**//*

    fun stringResource(@StringRes stringId: Int):String
    {
        return try {
            resources.getString(stringId)
        } catch (e: Exception) {
            ""
        }
    }

    fun drawableResource(@DrawableRes drawableId: Int): Drawable?
    {
        return try {
            ContextCompat.getDrawable(this,drawableId)
        } catch (e: Exception) {
            null
        }
    }

    fun dimensionResource(@DimenRes dimenRes: Int):Float?
    {
        return try {
            resources.getDimension(dimenRes)
        } catch (e: Exception) {
            0f
        }
    }

    fun colorResource(@ColorRes colorId: Int):Int
    {
        return try {
            ContextCompat.getColor(this,colorId)
        } catch (e: Exception) {
            Color.TRANSPARENT
        }
    }

    //////////////////////
    override val peerIdKey: String
        get() = Constants.PEER_ID_KEY

    override fun getPrefString(key: String): String? {
        return Prefs.getString(key)
    }

    override fun setFcmTokenSynced(b: Boolean) {
        Prefs.putBoolean("fcm_synced",b)
    }

    override fun setAndroidId(id: String) {
        Prefs.putString(Settings.Secure.ANDROID_ID,id)
    }

    override val fcmSynced: Boolean
        get() = Prefs.getBoolean(Constants.FCM_SYNCED)

    override val androidId: String
        @SuppressLint("HardwareIds")
        get() = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)

    override val fcmToken: String?
        get() = Prefs.getString("fcm_token")

    override val userId: String
        get() = chatUserId

    override val peerNotFoundText: String
        get() = R.string.doctor_not_found.string

    override val userNotFoundString: String?
        get() = R.string.user_not_found.string

    override val userIdKey: String
        get() = Constants.USER_UID

    override fun fetchFcmToken(callback: (String)->Unit){
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                callback("")
            }
            val token = task.result
            callback(token)
        })
    }

    override fun setFcmToken(it: String) {
        Prefs.putString(Constants.FCM_TOKEN,it)
    }

    override val myIdKey: String
        get() = Constants.MY_ID

    override val chatFirebaseDatabaseUrl: String
        get() = Metar[Constants.CHAT_DATABASE_URL]

    override val chatFirebaseAppName: String
        get() = Metar[Constants.CHAT_FIREBASE_APP_NAME]

    override val app: Application
        get() = this

    override val chatApiKey: String
        get() = Metar[Constants.CHAT_API_KEY_KEY]

    override val chatApplicationId: String
        get() = Metar[Constants.CHAT_APPLICATION_ID_KEY]

    override val chatProjectId: String?
        get() = Metar[Constants.CHAT_PROJECT_ID_KEY]
}*/
