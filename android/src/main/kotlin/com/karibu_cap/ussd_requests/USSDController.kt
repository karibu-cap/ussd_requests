/*
 * Copyright (c) 2020. BoostTag E.I.R.L. Romell D.Z.
 * All rights reserved
 * porfile.romellfudi.com
 */

/**
 * BoostTag E.I.R.L. All Copyright Reserved
 * www.boosttag.com
 */
package com.karibu_cap.ussd_requests

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.telecom.TelecomManager
import android.util.Log
import android.view.accessibility.AccessibilityManager
import androidx.annotation.RequiresApi
import java.util.stream.Stream
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.channels.awaitClose

import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn

/**
 * @author Romell Dominguez
 * @version 1.1.i 2019/04/18
 * @since 1.1.i
 */
@SuppressLint("StaticFieldLeak")
object USSDController : USSDInterface, USSDApi {

    private val accessibilityStatusChannels = mutableMapOf<String, Channel<Boolean>>()
    internal const val KEY_LOGIN = "KEY_LOGIN"
    internal const val KEY_ERROR = "KEY_ERROR"

    private val simSlotName = arrayOf("extra_asus_dial_use_dualsim",
            "com.android.phone.extra.slot", "slot", "simslot", "sim_slot", "subscription",
            "Subscription", "phone", "com.android.phone.DialingMode", "simSlot", "slot_id",
            "simId", "simnum", "phone_type", "slotId", "slotIdx")

    lateinit var context: Context
        private set

    lateinit var map: HashMap<String, List<String>>
        private set

    lateinit var callbackInvoke: CallbackInvoke

    var callbackMessage: ((String) -> Unit)? = null
        private set

    var isRunning: Boolean? = false
        private set

    var sendType: Boolean? = false
        private set

    private var ussdInterface: USSDInterface? = null

    init {
        ussdInterface = this
    }

    /**
     * Invoke a dial-up calling a ussd number
     *
     * @param ussdPhoneNumber ussd number
     * @param hashMap             Map of Login and problem messages
     * @param callbackInvoke  a listener object as to return answer
     */
    @RequiresApi(Build.VERSION_CODES.M)
    override fun callUSSDInvoke(context: Context, ussdPhoneNumber: String, hashMap: HashMap<String, List<String>>,
                                callbackInvoke: CallbackInvoke) {
        this.context = context
        callUSSDInvoke(context, ussdPhoneNumber, 0, hashMap, callbackInvoke)
    }

    /**
     * Invoke a dial-up calling a ussd number and
     * you had a overlay progress widget
     *
     * @param ussdPhoneNumber ussd number
     * @param hashMap         Map of Login and problem messages
     * @param callbackInvoke  a listener object as to return answer
     */
    @RequiresApi(Build.VERSION_CODES.M)
    override fun callUSSDOverlayInvoke(baseContext: Context, ussdPhoneNumber: String, hashMap: HashMap<String, List<String>>,
                                       callbackInvoke: CallbackInvoke) {
        context = baseContext        
        callUSSDOverlayInvoke(context, ussdPhoneNumber, 0, hashMap, callbackInvoke)
    }

    /**
     * Invoke a dial-up calling a ussd number
     *
     * ```
     * ussdApi.callUSSDInvoke(activity,"*515#",0,
     *              hashOf("KEY_LOGIN" to listOf("loading", "waiting"),
     *                      "KEY_ERROR" to listOf("null", "problem")  ),
     *              object : USSDController.CallbackInvoke {
     *                  override fun responseInvoke(message: String) {
     *                  }
     *                  override fun over(message: String) {
     *                  }
     *              }
     *         )
     * ```
     *
     * @param ussdPhoneNumber ussd number
     * @param simSlot         location number of the SIM
     * @param hashMap         Map of Login and problem messages
     * @param callback        a listener object as to return answer
     */
    @RequiresApi(Build.VERSION_CODES.M)
    override fun callUSSDInvoke(baseContext: Context, ussdPhoneNumber: String, simSlot: Int,
                                hashMap: HashMap<String, List<String>>, callback: CallbackInvoke) {
		sendType = false
        context = baseContext
        callbackInvoke = callback
        map = hashMap
        if (verifyAccessibilityAccess(context)) {
            dialUp(ussdPhoneNumber, simSlot)
        } else {
            callbackInvoke.over("Check your accessibility")
        }
    }

    /**
     * Invoke a dial-up calling a ussd number and
     * you had a overlay progress widget
     *
     * ```
     * ussdApi.callUSSDOverlayInvoke(activity,"*515#",0,
     *              hashOf("KEY_LOGIN" to listOf("loading", "waiting"),
     *                      "KEY_ERROR" to listOf("null", "problem")  ),
     *              object : USSDController.CallbackInvoke {
     *                  override fun responseInvoke(message: String) {
     *                  }
     *                  override fun over(message: String) {
     *                  }
     *              }
     *         )
     * ```
     *
     * @param ussdPhoneNumber ussd number
     * @param simSlot         simSlot number to use
     * @param hashMap         Map of Login and problem messages
     * @param callback        a listener object as to return answer
     */
    @RequiresApi(Build.VERSION_CODES.M)
    override fun callUSSDOverlayInvoke(baseContext: Context, ussdPhoneNumber: String, simSlot: Int,
                                       hashMap: HashMap<String, List<String>>, callback: CallbackInvoke) {		
        sendType = false
        context = baseContext
        callbackInvoke = callback
        map = hashMap
        if (verifyAccessibilityAccess(context) && verifyOverLay(context))
            dialUp(ussdPhoneNumber, simSlot)
        else callbackInvoke.over("Check your accessibility | overlay permission")
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun dialUp(ussdPhoneNumber: String, simSlot: Int) {
        when {
            !map.containsKey(KEY_LOGIN) || !map.containsKey(KEY_ERROR) ->
                callbackInvoke.over("Bad Mapping structure")
            ussdPhoneNumber.isEmpty() -> callbackInvoke.over("Bad ussd number")
            else -> {
                var phone = Uri.encode("#")?.let {
                    ussdPhoneNumber.replace("#", it)
                }
                isRunning = true
                context.startActivity(getActionCallIntent(Uri.parse("tel:$phone"), simSlot))
            }
        }
    }

    /**
     * get action call Intent
     * url: https://stackoverflow.com/questions/25524476/make-call-using-a-specified-sim-in-a-dual-sim-device
     *
     * @param uri     parsed uri to call
     * @param simSlot simSlot number to use
     */
    @RequiresApi(Build.VERSION_CODES.M)
    private fun getActionCallIntent(uri: Uri?, simSlot: Int): Intent {
        val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as? TelecomManager
        return Intent(Intent.ACTION_CALL, uri).apply {
            simSlotName.map { sim -> putExtra(sim, simSlot) }
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra("com.android.phone.force.slot", true)
            putExtra("Cdma_Supp", true)
            telecomManager?.callCapablePhoneAccounts?.let { handles ->
                if (handles.size > simSlot)
                    putExtra("android.telecom.extra.PHONE_ACCOUNT_HANDLE", handles[simSlot])
            }
        }
    }

    /**
     * The aim of this function is to send text via [android.widget.EditText]
     *
     * ```
     * ussdApi.sendData("12345")
     * ```
     * @param[text] String will be sent by EditText
     */
    override fun sendData(text: String) = USSDServiceKT.send(text)

    override fun stopRunning() {
        isRunning = false
    }

    /**
     * The aim of this is to send text(contains 145 characters in avg.)
     *
     * ```
     * ussdApi.send("12345") { it -> "ArrayString"
     *   it.toString()
     * }
     * ```
     *
     * @param[text] String send it into @see [android.widget.EditText]
     * @param[callbackMessage] The listener to get response, for example: ```[Hello,Cancel,Accept]```
     */
    override fun send(text: String, callbackMessage: (String) -> Unit) {
        this.callbackMessage = callbackMessage
        sendType = true
        ussdInterface?.sendData(text)
    }

    /**
     * Cancel the USSD flow in processing
     *
     * ```
     * ussdApi.callUSSDInvoke( ... ) {
     *      ussdApi.cancel()
     * }
     * ```
     *
     * @see callUSSDInvoke
     * @see callUSSDOverlayInvoke
     *
     */
    override fun cancel() = USSDServiceKT.cancel()

    /**
     * Invoke class to comunicate messages between USSD and App
     */
    interface CallbackInvoke {
        fun responseInvoke(message: String)
        fun over(message: String)
    }

    /**
     * The aim of this is to check whether accessibility is enabled or not
     * @param[context] The application context
     * @return The enable value of the accessibility
     */
    override fun verifyAccessibilityAccess(context: Context): Boolean =
            isAccessibilityServicesEnable(context).also {
                if (!it) openSettingsAccessibility(context as Activity)
            }

    /**
     * The aim of this is to check whether overlay permission is enabled or not
     * @param[context] The application context
     * @return The enable value of the permission
     */
    override fun verifyOverLay(context: Context): Boolean = (Build.VERSION.SDK_INT < Build.VERSION_CODES.M
            || Settings.canDrawOverlays(context)).also {
        if (!it) openSettingsOverlay(context as Activity)
    }

    private fun openSettingsOverlay(activity: Activity) =
            with(AlertDialog.Builder(activity)) {
                setTitle("USSD Overlay permission")
                setMessage("You must allow for the app to appear '${getNameApp(activity)}' on top of other apps.")
                setCancelable(true)
                setNeutralButton("ok") { _, _ ->
                    activity.startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:${activity.packageName}")))
                }
                create().show()
            }

    private fun getNameApp(activity: Activity): String = when (activity.applicationInfo.labelRes) {
        0 -> activity.applicationInfo.nonLocalizedLabel.toString()
        else -> activity.getString(activity.applicationInfo.labelRes)
    }

     private fun openSettingsAccessibility(activity: Activity) {
        activity.startActivityForResult(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS), 1)
    }

    override fun isAccessibilityServicesEnable(context: Context): Boolean {
        val accessibilityManager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager
        accessibilityManager?.apply {
            installedAccessibilityServiceList.forEach { service ->
                if (service.id.contains(context.packageName) &&
                    Settings.Secure.getInt(context.applicationContext.contentResolver, Settings.Secure.ACCESSIBILITY_ENABLED) == 1) {
                    val enabledServices = Settings.Secure.getString(context.applicationContext.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
                    if (enabledServices != null) {
                        val enabledServicesList = enabledServices.split(':')
                        if (enabledServicesList.contains(service.id)) {
                            Log.d("Accessibility", "Accessibility service ${service.id} is enabled for the app")
                            return true
                        }else{
                            Log.d("Accessibility", "Accessibility service: ${service.id} is unabled for the app")
                        }
                    }
                }
            }
        }
        Log.d("Accessibility", "Accessibility service is not enabled for the app")
        return false
    }
    @RequiresApi(Build.VERSION_CODES.N)
    override fun isAccessibilityServicesEnabledStream(context: Context): Flow<Boolean> = flow {
        val accessibilityManager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager
        accessibilityManager?.apply {
            installedAccessibilityServiceList.forEach { service ->
                if (service.id.contains(context.packageName) &&
                    Settings.Secure.getInt(context.applicationContext.contentResolver, Settings.Secure.ACCESSIBILITY_ENABLED) == 1) {
                    val enabledServices = Settings.Secure.getString(context.applicationContext.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
                    if (enabledServices != null) {
                        val enabledServicesList = enabledServices.split(':')
                        if (enabledServicesList.any { enabledService -> enabledService.contains(service.id) }) {
                            emit(true)
                            return@flow
                        }
                    }
                }
            }
        }
        emit(false)
    }.flowOn(Dispatchers.Default)
}
