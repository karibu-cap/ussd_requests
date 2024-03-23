/// Representation of a custom app info.
class CustomAppInfo {
  /// The stored key ref for the [packageName] property.
  static const keyPackageName = 'packageName';

  /// The stored key ref for the [applicationName] property.
  static const keyApplicationName = 'applicationName';

  /// The stored key ref for the [buildNumber] property.
  static const keyBuildNumber = 'buildNumber';

  /// The package name.
  final String packageName;

  /// The application name.
  final String applicationName;

  /// The build number.
  final String buildNumber;

  /// Constructs a new [CustomAppInfo] from [Map] object.
  CustomAppInfo.fromJson({required Map<String, dynamic> json})
      : packageName = json[keyPackageName],
        applicationName = json[keyApplicationName],
        buildNumber = json[keyBuildNumber];

  /// Constructs a new [Map] object from [CustomAppInfo].
  Map<String, dynamic> toJson() => {
        keyPackageName: packageName,
        keyApplicationName: applicationName,
        keyBuildNumber: buildNumber,
      };
}
