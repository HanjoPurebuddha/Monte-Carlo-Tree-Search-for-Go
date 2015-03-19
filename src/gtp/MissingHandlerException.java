package gtp;

public class MissingHandlerException extends RuntimeException {
	public MissingHandlerException() {
		super();
	}
	public MissingHandlerException(String message) {
		super(message);
	}
}
