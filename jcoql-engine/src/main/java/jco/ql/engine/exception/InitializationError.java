package jco.ql.engine.exception;

public class InitializationError extends Exception {

	private static final long serialVersionUID = 1L;

	public InitializationError(String message, Object... args) {
		super(String.format(message, args));
	}

	public InitializationError(Throwable throwable, String message, Object... args) {
		super(String.format(message, args), throwable);
	}
}
