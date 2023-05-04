
/// Representation of an response if single session ussd request.
class UssdSingleSessionResponse {
  /// The stored key ref for the [responseReceived] property.
  static const keyResponseReceived = 'responseReceived';

  /// The stored key ref for the [responseFailure] property.
  static const keyResponseFailure = 'responseFailure';

  /// The stored key ref for the [errorCode] property.
  static const keyErrorCode = 'errorCode';

  /// The response received.
  final String? responseReceived;

  /// The response failure.
  final String? responseFailure;

  /// The error code.
  final String? errorCode;

  /// Constructs a new [UssdSingleSessionResponse] from [Map] object.
  UssdSingleSessionResponse.fromJson({required Map<String, dynamic> json})
      : responseReceived = json[keyResponseReceived],
        responseFailure = json[keyResponseFailure],
        errorCode = json[keyErrorCode];

  /// Constructs a new [Map] object from [UssdSingleSessionResponse].
  Map<String, dynamic> toJson() => {
        keyResponseReceived: responseReceived,
        keyResponseFailure: responseFailure,
        keyErrorCode: errorCode,
      };
}
