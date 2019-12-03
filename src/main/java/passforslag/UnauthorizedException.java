package passforslag;

class UnauthorizedException extends RuntimeException {

  UnauthorizedException() {
  }

  UnauthorizedException(String message) {
    super(message);
  }

  UnauthorizedException(String message, Throwable cause) {
    super(message, cause);
  }
}
