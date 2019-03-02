package cmsc420.exception;

/**
 * Thrown if a city attempted to be mapped is outside the bounds of the
 * spatial map.
 */
public class RoadOutOfBoundsException extends Throwable {
	

	/**
	 * 
	 */
	private static final long serialVersionUID = 8355778062944660328L;

	public RoadOutOfBoundsException() {
	}

	public RoadOutOfBoundsException(String message) {
		super(message);
	}
}
