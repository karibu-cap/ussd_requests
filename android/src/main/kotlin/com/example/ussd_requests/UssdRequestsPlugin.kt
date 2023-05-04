package com.example.ussd_requests

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.telephony.TelephonyManager
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import java.util.concurrent.CompletableFuture

/** UssdRequestsPlugin */
class UssdRequestsPlugin: FlutterPlugin, MethodCallHandler {
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private var channelName: String = "com.karibu.ussd_requests/plugin_channel"
  private val makeRequestUssdName = "singleSessionUssdRequest"
  private var context: Context? = null
  private var channel: MethodChannel? = null

  val logTag = "karibu.ussd_requests "

  // The method calls by execution of platform channel.
  override fun onAttachedToEngine(binding: FlutterPlugin.FlutterPluginBinding) {
    initialize(binding.applicationContext, binding.binaryMessenger)
  }

  private fun initialize(context: Context, messenger: BinaryMessenger) {

    this.context = context
    channel =
      MethodChannel(messenger, channelName)
    channel!!.setMethodCallHandler(this)
  }

  override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
    channel?.setMethodCallHandler(null)
    channel = null
    context = null
  }


  // The method calls by execution of platform channel.
  override fun onMethodCall(call: MethodCall, result: Result) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O
    ) {
      throw RequestExecutionException("Build.VERSION.SDK_INT invalid version")
    }
    if (call.method != makeRequestUssdName) {
      result.notImplemented()
    }
    try {
      val mParams = UssdRequestParams(call)
      makeRequest(mParams).exceptionally { e: Throwable ->
        result.error(
          RequestExecutionException.type,
          e.message,
          null
        )
        null
      }.thenAccept { resultToSend: HashMap<String, String> ->
        result.success(
          resultToSend
        )
      }
    } catch (e: RequestParamsException) {
      result.error(RequestParamsException.type, e.message, null)
    } catch (e: RequestExecutionException) {
      result.error(RequestParamsException.type, e.message, null)
    } catch (e: Exception) {
      result.error("unknown_exception", e.message, null)
    }

  }

  private fun makeRequest(ussdRequestParams : UssdRequestParams): CompletableFuture<HashMap<String, String>> {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O
    ) {
      throw RequestExecutionException("Build.VERSION.SDK_INT invalid version")
    }

    val completableFuture = CompletableFuture<HashMap<String, String>>()
     val response = HashMap<String, String>()

     val callback: TelephonyManager.UssdResponseCallback =
      @RequiresApi(Build.VERSION_CODES.O)
      object : TelephonyManager.UssdResponseCallback() {
        override fun onReceiveUssdResponse(
          telephonyManager: TelephonyManager, request: String, resp: CharSequence
        ) {
          Log.i(logTag, "UssdRequest: $request")
          Log.i(logTag, "UssdRequest: $resp")
          response.apply {
            put("responseReceived",resp.toString())
          }
          completableFuture.complete(response)
        }

        override fun onReceiveUssdResponseFailed(
          telephonyManager: TelephonyManager, request: String, failureCode: Int
        ) {
          Log.i(logTag, "UssdRequest: $request")
          Log.i(logTag, "UssdRequest: $failureCode")
          when {
            (failureCode == TelephonyManager.USSD_ERROR_SERVICE_UNAVAIL) -> {
              response.apply {
                put("responseFailure","USSD_ERROR_SERVICE_UNAVAILABLE")
              }
            }
            (failureCode == TelephonyManager.USSD_RETURN_FAILURE) -> {
              response.apply {
                put("responseFailure","USSD_RETURN_FAILURE")
              }
            }
            else -> {
              response.apply {
                put("responseFailure","USSD_ERROR_UNKNOWN")
              }
            }
          }
          response.apply {
            put("errorCode",failureCode.toString())
          }

          completableFuture.complete(response)
        }
      }
    if (context?.let { ContextCompat.checkSelfPermission(it, Manifest.permission.CALL_PHONE) }
      != PackageManager.PERMISSION_GRANTED
    ) {
      throw RequestExecutionException("CALL_PHONE permission missing")
    }
    val manager = context!!
      .getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    val simManager = manager.createForSubscriptionId(ussdRequestParams.subscriptionId)
    simManager.sendUssdRequest(ussdRequestParams.ussdCode, callback, Handler(Looper.getMainLooper()))
    return completableFuture
  }


  private class UssdRequestParams(call: MethodCall) {
    var subscriptionId: Int
    var ussdCode: String?

    init {
      val subscriptionIdInteger = call.argument<Int>("subscriptionId")
        ?: throw RequestParamsException(
          "Incorrect parameter type: `subscriptionId` must be an int"
        )
      subscriptionId = subscriptionIdInteger
      if (subscriptionId < 0) {
        throw RequestParamsException(
          "Incorrect parameter value: `subscriptionId` must be >= 0"
        )
      }
      ussdCode = call.argument<String>("ussdCode")
      if (ussdCode == null) {
        throw RequestParamsException("Incorrect parameter type: `ussdCode` must be a String")
      }
      if (ussdCode!!.isEmpty()) {
        throw RequestParamsException(
          "Incorrect parameter value: `code` must not be an empty string"
        )
      }
    }
  }



  private class RequestExecutionException constructor(override var message: String) :
    Exception() {
    companion object {
      var type = "ussd_plugin_ussd_execution_failure"
    }
  }

  private class RequestParamsException constructor(override var message: String) :
    java.lang.Exception() {
    companion object {
      var type = "ussd_plugin_incorrect__parameters"
    }
  }
}
