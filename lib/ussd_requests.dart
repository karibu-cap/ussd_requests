import 'models/ussd_single_session_response.dart';
import 'ussd_requests_platform_interface.dart';

class UssdRequests {
  Future<String?> getPlatformVersion() {
    return UssdRequestsPlatform.instance.getPlatformVersion();
  }

  static Future<UssdSingleSessionResponse?> singleSessionUssdRequest({
    required String ussdCode,
    required int subscriptionId,
  }) async {
    return await UssdRequestsPlatform.instance.singleSessionUssdRequest(
        subscriptionId: subscriptionId, ussdCode: ussdCode);
  }

  static Future<String?> multipleSessionBackgroundUssdRequest({
    required String ussdCode,
    required int simSlot,
    List<String>? selectableOption,
    bool? cancelAtTheEnd = true,
  }) async {
    return await UssdRequestsPlatform.instance
        .multipleSessionBackgroundUssdRequest(
            selectableOption: selectableOption,
            cancelAtTheEnd: cancelAtTheEnd,
            simSlot: simSlot,
            ussdCode: ussdCode);
  }

  static Future<bool> isAccessibilityServicesEnable() async {
    return await UssdRequestsPlatform.instance.isAccessibilityServicesEnable();
  }

  static Stream<bool> get streamAccessibilityServiceEnabled {
    return UssdRequestsPlatform.instance.streamAccessibilityServiceEnabled;
  }
}
