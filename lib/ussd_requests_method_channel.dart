import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'ussd_requests_platform_interface.dart';

/// An implementation of [UssdRequestsPlatform] that uses method channels.
class MethodChannelUssdRequests extends UssdRequestsPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('ussd_requests');

  @override
  Future<String?> getPlatformVersion() async {
    final version = await methodChannel.invokeMethod<String>('getPlatformVersion');
    return version;
  }
}
