import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:ussd_requests/ussd_requests_method_channel.dart';

void main() {
  MethodChannelUssdRequests platform = MethodChannelUssdRequests();
  const MethodChannel channel = MethodChannel('ussd_requests');

  TestWidgetsFlutterBinding.ensureInitialized();

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('getPlatformVersion', () async {
    expect(await platform.getPlatformVersion(), '42');
  });
}
