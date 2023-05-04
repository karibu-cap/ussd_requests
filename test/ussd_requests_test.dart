import 'package:flutter_test/flutter_test.dart';
import 'package:ussd_requests/ussd_requests.dart';
import 'package:ussd_requests/ussd_requests_platform_interface.dart';
import 'package:ussd_requests/ussd_requests_method_channel.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockUssdRequestsPlatform
    with MockPlatformInterfaceMixin
    implements UssdRequestsPlatform {

  @override
  Future<String?> getPlatformVersion() => Future.value('42');
}

void main() {
  final UssdRequestsPlatform initialPlatform = UssdRequestsPlatform.instance;

  test('$MethodChannelUssdRequests is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelUssdRequests>());
  });

  test('getPlatformVersion', () async {
    UssdRequests ussdRequestsPlugin = UssdRequests();
    MockUssdRequestsPlatform fakePlatform = MockUssdRequestsPlatform();
    UssdRequestsPlatform.instance = fakePlatform;

    expect(await ussdRequestsPlugin.getPlatformVersion(), '42');
  });
}
