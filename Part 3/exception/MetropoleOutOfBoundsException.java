package cmsc420.exception;

/**
 * Thrown if a city attempted to be mapped is outside the bounds of the
 * spatial map.
 */
public class MetropoleOutOfBoundsException extends Throwable {
	private static final long serialVersionUID = -6878077114302943595L;

	public MetropoleOutOfBoundsException() {
	}

	public MetropoleOutOfBoundsException(String message) {
		super(message);
	}
}
