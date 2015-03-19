package gtp;

public interface Handler {
	String handle(String command, String arguments) throws Throwable;
}
