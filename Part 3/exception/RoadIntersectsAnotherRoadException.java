package cmsc420.exception;


/**
 * Thrown if a city attempted to be mapped is an isolated city
 */
public class RoadIntersectsAnotherRoadException extends Throwable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4972047014298760613L;

	public RoadIntersectsAnotherRoadException() {
	}
	public RoadIntersectsAnotherRoadException(String message) {
		super(message);
	}
}