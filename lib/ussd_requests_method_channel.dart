import 'dart:async';

import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'models/custom_app_info.dart';
import 'models/ussd_single_session_response.dart';
import 'ussd_requests_platform_interface.dart';

/// An implementation of [UssdRequestsPlatform] that uses method channels.
class MethodChannelUssdRequests extends UssdRequestsPlatform {
  static const _channel = EventChannel("stream_accessibility_service_enabled");

  late final StreamSubscription? _channelStreamSubscription;

  final StreamController _controller = StreamController<bool>();

  MethodChannelUssdRequests() {
    _channelStreamSubscription =
        _channel.receiveBroadcastStream().listen((result) {
      if (!_controller.isClosed) {
        _controller.sink.add(result);
      }
    });
  }

  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel =
      const MethodChannel('com.karibu_cap.ussd_requests/plugin_channel');

  @override
  Future<String?> getPlatformVersion() async {
    final version =
        await methodChannel.invokeMethod<String>('getPlatformVersion');
    return version;
  }

  @override
  Future<UssdSingleSessionResponse?> singleSessionUssdRequest({
    required String ussdCode,
    required int subscriptionId,
  }) async {
    try {
      final result = await methodChannel.invokeMethod(
        'singleSessionBackgroundUssdRequest',
        {'subscriptionId': subscriptionId, 'ussdCode': ussdCode},
      );

      return UssdSingleSessionResponse.fromJson(
          json: Map<String, dynamic>.from(result));
    } on PlatformException catch (e) {
      if (kDebugMode) {
        print("Failed to make request : '${e.message}'.");
      }
    }

    return null;
  }

  @override
  Future<String?> multipleSessionBackgroundUssdRequest({
    required String ussdCode,
    required int simSlot,
    List<String>? selectableOption,
    bool? cancelAtTheEnd = true,
  }) async {
    try {
      final result = await methodChannel.invokeMethod(
        'multipleSessionBackgroundUssdRequest',
        {
          'simSlot': simSlot,
          'ussdCode': ussdCode,
          'selectableOption': selectableOption,
          'cancelAtTheEnd': cancelAtTheEnd
        },
      );

      return result;
    } on PlatformException catch (e) {
      if (kDebugMode) {
        print("Failed to make request : '${e.message}'.");
      }
    }

    return null;
  }

  @override
  Future<bool> isAccessibilityServicesEnabled() async {
    try {
      return await methodChannel
          .invokeMethod('isAccessibilityServicesEnabledRequest');
    } on PlatformException catch (e) {
      if (kDebugMode) {
        print('Failed to invoke Kotlin method: ${e.message}');
      }
      return false;
    }
  }

  @override
  Stream<bool> get streamAccessibilityServiceEnabled {
    return _controller.stream as Stream<bool>;
  }

  /// dispose method.
  void dispose() {
    _channelStreamSubscription?.cancel();
    _controller.close();
    _channelStreamSubscription = null;
  }

  @override
  Future<CustomAppInfo?> getEnabledAccessibilityApps() async {
    try {
      final result = await methodChannel
          .invokeMethod('getEnabledAccessibilityAppsRequest');
      return CustomAppInfo.fromJson(json: Map<String, dynamic>.from(result));
    } on PlatformException catch (e) {
      if (kDebugMode) {
        print('Failed to invoke Kotlin method: ${e.message}');
      }
    }
    return null;
  }
}
