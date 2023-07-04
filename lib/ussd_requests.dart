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

  static Future<UssdSingleSessionResponse?>
      multipleSessionBackgroundUssdRequest({
    required String ussdCode,
    required int simSlot,
    required List<String> selectableOption,
  }) async {
    return await UssdRequestsPlatform.instance
        .multipleSessionBackgroundUssdRequest(
            selectableOption: selectableOption,
            simSlot: simSlot,
            ussdCode: ussdCode);
  }
}
