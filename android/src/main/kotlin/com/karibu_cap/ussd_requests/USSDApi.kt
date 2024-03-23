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

import android.content.Context
import android.content.pm.ApplicationInfo
import java.util.*
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.CompletableFuture

/**
 *
 * @author Romell Dominguez
 * @version 1.1.i 2019/04/18
 * @since 1.1.i
 */
interface USSDApi {
    fun send(text: String, callbackMessage: (String) -> Unit)
    fun cancel()
    fun callUSSDInvoke(context: Context, ussdPhoneNumber: String,
                       map: HashMap<String, List<String>>,
                       callbackInvoke: USSDController.CallbackInvoke)

    fun callUSSDInvoke(context: Context, ussdPhoneNumber: String, simSlot: Int,
                       map: HashMap<String, List<String>>,
                       callbackInvoke: USSDController.CallbackInvoke)

    fun callUSSDOverlayInvoke(context: Context, ussdPhoneNumber: String,
                              map: HashMap<String, List<String>>,
                              callbackInvoke: USSDController.CallbackInvoke)

    fun callUSSDOverlayInvoke(context: Context, ussdPhoneNumber: String, simSlot: Int,
                              map: HashMap<String, List<String>>,
                              callbackInvoke: USSDController.CallbackInvoke)
    fun verifyAccessibilityAccess(context: Context): Boolean
    fun verifyOverLay(context: Context): Boolean
    fun isAccessibilityServicesEnabled(context: Context): Boolean
    fun isAccessibilityServicesEnabledStream(context: Context): Flow<Boolean>
    fun getEnabledAccessibilityApps(context: Context): List<Map<String, Any>>
}
