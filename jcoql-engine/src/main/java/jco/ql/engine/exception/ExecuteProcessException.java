package jco.ql.engine.exception;

public class ExecuteProcessException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ExecuteProcessException() {
		super();
	}

	public ExecuteProcessException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ExecuteProcessException(String message, Throwable cause) {
		super(message, cause);
	}

	public ExecuteProcessException(String message) {
		super(message);
	}

	public ExecuteProcessException(String message, Object... args) {
		super(String.format(message, args));
	}

	public ExecuteProcessException(Throwable cause) {
		super(cause);
	}

}
