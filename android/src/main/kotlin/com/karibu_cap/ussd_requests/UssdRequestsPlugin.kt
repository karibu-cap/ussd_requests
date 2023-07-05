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


/** UssdRequestsPlugin */
class UssdRequestsPlugin: FlutterPlugin, MethodCallHandler {
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private var channelName: String = "com.karibu_cap.ussd_requests/plugin_channel"
  private val singleSessionBackgroundUssdRequestName = "singleSessionBackgroundUssdRequest"
  private val multipleSessionBackgroundUssdRequestName = "multipleSessionBackgroundUssdRequest"
  private var context: Context? = null
  private var channel: MethodChannel? = null
  private var ussdApi : USSDApi = USSDController
  private val map = hashMapOf(
    "KEY_LOGIN" to listOf("espere", "waiting", "loading", "esperando"),
    "KEY_ERROR" to listOf("problema", "problem", "error", "null")
  )


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
    this.ussdApi = USSDController
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

    if (call.method != singleSessionBackgroundUssdRequestName && call.method != multipleSessionBackgroundUssdRequestName) {
      result.notImplemented()
    }

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
  private fun multipleSessionBackgroundUssdRequest(ussdRequestParams: MultipleSessionBackgroundUssdRequestParams): CompletableFuture<HashMap<String, String>> {

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

    val completableFuture = CompletableFuture<HashMap<String, String>>()
    val response = HashMap<String, String>()
    val currentIndex: Int = 0
    context?.let {
      val selectableOption = ussdRequestParams.selectableOption
      if (currentIndex < selectableOption.size) {
        Log.d("vvvvvvvvvvvvvv 1", selectableOption.size.toString())
        ussdApi.callUSSDInvoke(it, ussdRequestParams.ussdCode, map, object : USSDController.CallbackInvoke {
          override fun responseInvoke(message: String) {
            Log.d("vvvvvvvvvvv jjj", message)
            // Handle the USSD response message
            ussdApi.send(selectableOption[currentIndex] ) { responseMessage ->
              // Handle the response message from the selected option
              Log.d("vvvvvvvvvvv 2", responseMessage)
              if (currentIndex == selectableOption.size - 1) {
                // Last USSD request, complete the CompletableFuture
                response["responseReceived"] = responseMessage
                completableFuture.complete(response)
              } else {
                // Execute the next USSD request recursively
                sendData(it, ussdApi, selectableOption, ussdRequestParams.ussdCode, ussdRequestParams.simSlot, response, completableFuture, currentIndex + 1)
              }
            }
          }

          override fun over(message: String) {
            // Handle the USSD response message
            response["responseReceived"] = message
            completableFuture.complete(response)
          }
        })

      }
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
    completableFuture: CompletableFuture<HashMap<String, String>>,
    currentIndex: Int
  ) {

    ussdApi.send(selectableOption[currentIndex]) { responseMessage ->
      // Handle the response message from the selected option
      Log.d("vvvvvvvvvvv 2", responseMessage)
      if (currentIndex == selectableOption.size - 1) {
        // Last USSD request, complete the CompletableFuture
        response["responseReceived"] = responseMessage
        completableFuture.complete(response)
      } else {
        // Execute the next USSD request recursively
        sendData(context, ussdApi, selectableOption, ussdCode, simSlot, response, completableFuture, currentIndex + 1)
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
    var selectableOption: List<String>
    var ussdCode: String
    var simSlot: Int

    init {
      simSlot = call.argument<Int>("simSlot")
        ?: throw RequestParamsException(
          "Incorrect parameter type: `slotSim` must be an Int"
        )
      selectableOption = call.argument<List<String>>("selectableOption")
        ?: throw RequestParamsException(
          "Incorrect parameter type: `selectableOption` must be a list of string"
        )
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
