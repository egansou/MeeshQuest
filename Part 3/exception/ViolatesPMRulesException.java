package cmsc420.exception;

public class ViolatesPMRulesException extends Throwable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8189472481952963455L;

	public ViolatesPMRulesException() {
	}

	public ViolatesPMRulesException(String message) {
		super(message);
	}
}
