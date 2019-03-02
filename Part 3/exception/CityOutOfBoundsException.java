package cmsc420.exception;

/**
 * Thrown if a city attempted to be mapped is outside the bounds of the
 * spatial map.
 */
public class CityOutOfBoundsException extends Throwable {
	

	/**
	 * 
	 */
	private static final long serialVersionUID = 8355778062944660328L;

	public CityOutOfBoundsException() {
	}

	public CityOutOfBoundsException(String message) {
		super(message);
	}
}
