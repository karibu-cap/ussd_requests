package com.karibu_cap.ussd_requests

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.telephony.TelephonyManager
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import java.util.concurrent.CompletableFuture
import io.flutter.plugin.common.EventChannel
import android.content.BroadcastReceiver
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import android.content.Intent
import android.content.IntentFilter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

/** UssdRequestsPlugin */
class UssdRequestsPlugin: FlutterPlugin, MethodCallHandler, EventChannel.StreamHandler, BroadcastReceiver(), ActivityAware  {
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity

  private var eventChannel : EventChannel? = null
  private var eventSink: EventChannel.EventSink? = null
  private var channelName: String = "com.karibu_cap.ussd_requests/plugin_channel"
  private val singleSessionBackgroundUssdRequestName = "singleSessionBackgroundUssdRequest"
  private val multipleSessionBackgroundUssdRequestName = "multipleSessionBackgroundUssdRequest"
  private val isAccessibilityServicesEnableRequestName = "isAccessibilityServicesEnableRequest"
  private var context: Context? = null
  private var activity: Activity? = null
  private var methodChannel: MethodChannel? = null
  private var ussdApi : USSDApi = USSDController
  private val map = hashMapOf(
    "KEY_LOGIN" to listOf("espere", "waiting", "loading", "esperando"),
    "KEY_ERROR" to listOf("problema", "problem", "error", "null")
  )

  val logTag = "karibu.ussd_requests "
  
  // The method calls by execution of platform channel.
  override fun onAttachedToEngine(binding: FlutterPlugin.FlutterPluginBinding) {
    initialize(binding.applicationContext, binding.binaryMessenger)
    eventChannel = EventChannel(binding.binaryMessenger,"stream_accessibility_service_enabled")
    eventChannel!!.setStreamHandler(this)
  }

  private fun initialize(context: Context, messenger: BinaryMessenger) {

    this.context = context
    methodChannel =
      MethodChannel(messenger, channelName)
      methodChannel!!.setMethodCallHandler(this)
    this.ussdApi = USSDController
  }

  override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
    eventSink = events
  }

  override fun onCancel(arguments: Any?) {
    eventSink = null
  }

  override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
    methodChannel?.setMethodCallHandler(null)
    methodChannel = null
    context = null
    eventChannel = null
    eventSink = null
  }

  override fun onAttachedToActivity(p0: ActivityPluginBinding) {
    activity = p0.activity
  }

  override fun onDetachedFromActivityForConfigChanges() {
    TODO("Not yet implemented")
  }

  override fun onReattachedToActivityForConfigChanges(p0: ActivityPluginBinding) {
    TODO("Not yet implemented")
  }

  override fun onDetachedFromActivity() {
    TODO("Not yet implemented")
  }



  // The method calls by execution of platform channel.
  override fun onMethodCall(call: MethodCall, result: Result) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O
    ) {
      throw RequestExecutionException("Build.VERSION.SDK_INT invalid version")
    }
    if (call.method == singleSessionBackgroundUssdRequestName) {
      try {
        val mParams = SingleSessionBackgroundUssdRequestParams(call)
        singleSessionBackgroundUssdRequest(mParams).exceptionally { e: Throwable ->
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
    if (call.method == multipleSessionBackgroundUssdRequestName) {
      try {
        val mParams = MultipleSessionBackgroundUssdRequestParams(call)
        multipleSessionBackgroundUssdRequest(mParams).exceptionally { e: Throwable ->
          result.error(
            RequestExecutionException.type,
            e.message,
            null
          )
          null
        }.thenAccept { resultToSend: String ->
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
    if (call.method == isAccessibilityServicesEnableRequestName) {
      try {
          result.success(
            this.ussdApi.isAccessibilityServicesEnable(context!!)
          )
      } catch (e: Exception) {
        result.error(RequestParamsException.type, e.message, null)
      }
    }
    if (call.method != singleSessionBackgroundUssdRequestName && call.method != multipleSessionBackgroundUssdRequestName && call.method != isAccessibilityServicesEnableRequestName) {
      result.notImplemented()
    }
  }

  @RequiresApi(Build.VERSION_CODES.KITKAT)
  override fun onReceive(context: Context, intent: Intent) {
    Log.i(logTag, "isAccessibilityServicesEnableStream isAccessibilityServicesEnableStream: 1111")
    Log.i(logTag, "isAccessibilityServicesEnableStream isAccessibilityServicesEnableStream: 222")
    val scope = CoroutineScope(Dispatchers.Main)

    scope.launch {
      ussdApi.isAccessibilityServicesEnabledStream(context!!).collect { isEnabled: Boolean ->
            // Handle each emitted value here
            Log.i(logTag, "isAccessibilityServicesEnableStream isAccessibilityServicesEnableStream: $isEnabled")
            eventSink?.success(isEnabled)
        }
    }

    // Optionally, you can cancel the coroutine scope when it's no longer needed
    // scope.cancel()
  }

  private fun singleSessionBackgroundUssdRequest(ussdRequestParams : SingleSessionBackgroundUssdRequestParams): CompletableFuture<HashMap<String, String>> {
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

  @RequiresApi(Build.VERSION_CODES.N)
  private fun multipleSessionBackgroundUssdRequest(ussdRequestParams: MultipleSessionBackgroundUssdRequestParams): CompletableFuture<String> {

    // check permissions
    if (ContextCompat.checkSelfPermission(context!!, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
      if (!ActivityCompat.shouldShowRequestPermissionRationale(context!! as Activity, Manifest.permission.CALL_PHONE)) {
        ActivityCompat.requestPermissions(context!! as Activity, arrayOf(Manifest.permission.CALL_PHONE), 2)
      }
    }else if (ContextCompat.checkSelfPermission(context!!, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
      if (!ActivityCompat.shouldShowRequestPermissionRationale(context!! as Activity, Manifest.permission.READ_PHONE_STATE)) {
        ActivityCompat.requestPermissions(context!! as Activity, arrayOf(Manifest.permission.READ_PHONE_STATE), 2)
      }
    }

    val completableFuture = CompletableFuture<String>()
    val response = HashMap<String, String>()
    val currentIndex = 0
    val selectableOption = ussdRequestParams.selectableOption
    context?.let {
        this.ussdApi.callUSSDInvoke(it, ussdRequestParams.ussdCode, ussdRequestParams.simSlot, map, object : USSDController.CallbackInvoke {
          override fun responseInvoke(message: String) {
            // Handle the USSD response message
            if (selectableOption != null && selectableOption.isNotEmpty()) {
              ussdApi.send(selectableOption[currentIndex]) { responseMessage ->
                // Handle the response message from the selected option
                if (currentIndex == selectableOption.size - 1) {
                  // Last USSD request, complete the CompletableFuture

                  Log.i(logTag, "ussdApi lenght 1: $message")
                  completableFuture.complete(responseMessage)
                  if(ussdRequestParams.cancelAtTheEnd){
                    ussdApi.cancel()
                  }
                } else {
                  // Execute the next USSD request recursively
                  sendData(
                    it,
                    ussdApi,
                    selectableOption,
                    ussdRequestParams.ussdCode,
                    ussdRequestParams.simSlot,
                    response,
                    completableFuture,
                    currentIndex + 1,
                    ussdRequestParams.cancelAtTheEnd
                  )
                }
              }
            }else {
              completableFuture.complete(message)
              Log.i(logTag, "ussdApi not lenght: $message")
              if(ussdRequestParams.cancelAtTheEnd){
                ussdApi.cancel()
              }
            }
          }

          override fun over(message: String) {
            // Handle the USSD response message
            completableFuture.complete(message)
            Log.i(logTag, "ussdApi over: $message")
            if(ussdRequestParams.cancelAtTheEnd){
              ussdApi.cancel()
            }
          }
        })


    }
    return completableFuture
  }

  @RequiresApi(Build.VERSION_CODES.N)
  private fun sendData(
    context: Context,
    ussdApi: USSDApi,
    selectableOption: List<String>,
    ussdCode: String,
    simSlot: Int,
    response: HashMap<String, String>,
    completableFuture: CompletableFuture<String>,
    currentIndex: Int,
    cancelAtTheEnd: Boolean
  ) {

    ussdApi.send(selectableOption[currentIndex]) { responseMessage ->
      // Handle the response message from the selected option
      if (currentIndex == selectableOption.size - 1) {
        // Last USSD request, complete the CompletableFuture
        completableFuture.complete(responseMessage)
        Log.i(logTag, "ussdApi: $responseMessage")
        if(cancelAtTheEnd){
          ussdApi.cancel()
        }
      } else {
        // Execute the next USSD request recursively
        sendData(context, ussdApi, selectableOption, ussdCode, simSlot, response, completableFuture, currentIndex + 1, cancelAtTheEnd)
      }
    }
  }



  private class SingleSessionBackgroundUssdRequestParams(call: MethodCall) {
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


  private class MultipleSessionBackgroundUssdRequestParams(call: MethodCall) {
    var selectableOption: List<String>?
    var ussdCode: String
    var simSlot: Int
    var cancelAtTheEnd: Boolean

    init {
      simSlot = call.argument<Int>("simSlot")
        ?: throw RequestParamsException(
          "Incorrect parameter type: `slotSim` must be an Int"
        )
      cancelAtTheEnd = call.argument<Boolean>("cancelAtTheEnd")
        ?: throw RequestParamsException(
          "Incorrect parameter type: `cancelAtTheEnd` must be a boolean"
        )
      selectableOption = call.argument<List<String>?>("selectableOption")
      ussdCode = call.argument<String>("ussdCode")?:
        throw RequestParamsException("Incorrect parameter type: `ussdCode` must be a String")
      if (ussdCode.isEmpty()) {
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
