
import 'ussd_requests_platform_interface.dart';

class UssdRequests {
  Future<String?> getPlatformVersion() {
    return UssdRequestsPlatform.instance.getPlatformVersion();
  }
}
