import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'models/ussd_single_session_response.dart';
import 'ussd_requests_method_channel.dart';

abstract class UssdRequestsPlatform extends PlatformInterface {
  /// Constructs a UssdRequestsPlatform.
  UssdRequestsPlatform() : super(token: _token);

  static final Object _token = Object();

  static UssdRequestsPlatform _instance = MethodChannelUssdRequests();

  /// The default instance of [UssdRequestsPlatform] to use.
  ///
  /// Defaults to [MethodChannelUssdRequests].
  static UssdRequestsPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [UssdRequestsPlatform] when
  /// they register themselves.
  static set instance(UssdRequestsPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<String?> getPlatformVersion() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }

  Future<UssdSingleSessionResponse?> singleSessionUssdRequest({
    required String ussdCode,
    required int subscriptionId,
  }) {
    throw UnimplementedError(
        'singleSessionUssdRequest() has not been implemented.');
  }

  Future<String?> multipleSessionBackgroundUssdRequest({
    required String ussdCode,
    required int simSlot,
    List<String>? selectableOption,
    bool? cancelAtTheEnd = true,
  }) {
    throw UnimplementedError(
        'multipleSessionBackgroundUssdRequest() has not been implemented.');
  }

  Future<bool> isAccessibilityServicesEnable() async {
    return false;
  }

  Stream<bool> streamAccessibilityServiceEnabled(String packageName) {
    return Stream.value(false);
  }
}
