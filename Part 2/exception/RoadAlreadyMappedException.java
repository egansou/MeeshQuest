package cmsc420.exception;

/**
 * Thrown if city is already in the spatial map upon attempted insertion.
 */
public class RoadAlreadyMappedException extends Throwable {
	private static final long serialVersionUID = -4096614031875292057L;

	public RoadAlreadyMappedException() {
	}

	public RoadAlreadyMappedException(String message) {
		super(message);
	}
}
