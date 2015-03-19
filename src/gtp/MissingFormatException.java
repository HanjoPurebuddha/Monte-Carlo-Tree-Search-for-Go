package gtp;

public class MissingFormatException extends RuntimeException {
	private final Class type;
	public MissingFormatException(Class type) {
		super(type.getName());
		this.type = type;
	}
	public MissingFormatException(String message, Class type) {
		super(message);
		this.type = type;
	}
	public Class getType() {return type;}
}

