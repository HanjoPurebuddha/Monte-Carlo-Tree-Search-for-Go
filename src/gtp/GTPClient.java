package gtp;

import java.io.*;
import java.lang.reflect.*;
import java.text.*;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ideanest.util.Safe;

import game.Color;

/**
 * A client for the Go Text Protocol.
 * 
 * @see http://www.lysator.liu.se/~gunnar/gtp/
 * @author Piotr Kaminski
 */
public class GTPClient implements Runnable {
	
	private final Map handlers = new HashMap();
	private final Map formatters = new HashMap();
	private final BufferedReader in;
	private final BufferedWriter out;
	private Thread thread;
	
	protected static final Logger log = Logger.getLogger(GTPClient.class.getName());
	protected static final Pattern commandPattern = Pattern.compile("(\\d+ +)?(\\D\\w*)( +.*)?");
	
	public GTPClient(Reader in, Writer out) {
		this.in = new BufferedReader(in);
		this.out = new BufferedWriter(out);
		registerFormat(void.class, VoidFormat.getInstance());
		registerFormat(int.class, new IntFormat());
		registerFormat(float.class, new FloatFormat());
		registerFormat(boolean.class, new BooleanFormat());
		registerFormat(String.class, new StringFormat());
		registerFormat(Vertex.class, new VertexFormat());
		registerFormat(Color.class, new ColorFormat());
		registerFormat(Move.class, new MoveFormat());
		registerFormat(Object[].class, new ObjectArrayFormat());
		registerController(new MetaController());
	}
	
	public void stop() {
		if (!isRunning()) throw new IllegalStateException("client not running");
		thread.interrupt();
		thread = null;
	}
	
	public void start() {
		if (isRunning()) throw new IllegalStateException("client already running");
		thread = new Thread(this, "GTP client thread");
		thread.start();
	}
	
	public boolean isRunning() {
		return thread != null && thread.isAlive();
	}
	
	public void join() throws InterruptedException {
		if (thread != null) thread.join();
	}
	
	/* adding a method to record startup commands */
	
	public String startupCommands(int iteration) {
		switch(iteration) {
			case 0:
				return "boardsize 3";
			case 1:
				return "clear_board";
			case 2: 
				return "play B B2";
			case 3:
				return "genmove w";
			default:
				return null;
		}
		
	}

	public void run() {
		boolean startupRun = true; //used to check if startup commands have run
		thread = Thread.currentThread();
		try {
			while(!Thread.interrupted()) {
				
				/* execute startup commands */
				
				
				if(!startupRun) {
					int amountOfStartupCommands = 4;
					for(int j=0; j<amountOfStartupCommands;j++){
						String line = startupCommands(j);
						if (line == null) break;
						int i = line.indexOf('#');
						if (i != -1) line = line.substring(0, i);
						line = line.replace('\t', ' ');
						if (line.trim().length() == 0) continue;
						Matcher lineMatcher = commandPattern.matcher(line);
						if (!lineMatcher.matches()) throw new ParseException("cannot interpret " + line, 0);
						String id = lineMatcher.group(1);
						if (id != null) id = id.trim();
						String command = lineMatcher.group(2).trim();
						String args = lineMatcher.group(3);
						char prefix;
						String result;
						try {
							result = getHandler(command).handle(command, args);
							prefix = '=';
						} catch (IOException e) {
							throw e;
						} catch (Throwable e) {
							if (e instanceof InterruptedException || e instanceof ThreadDeath) break;
							result= e.toString();
							prefix = '?';
						}
						out.write(prefix);
						if (id != null && id.length() > 0) out.write(id);
						out.write(' ');
						out.write(result);
						out.write("\n\n");
						out.flush();
					}
					startupRun = true;
				}
				
				String line = in.readLine();
				if (line == null) break;
				int i = line.indexOf('#');
				if (i != -1) line = line.substring(0, i);
				line = line.replace('\t', ' ');
				if (line.trim().length() == 0) continue;
				Matcher lineMatcher = commandPattern.matcher(line);
				if (!lineMatcher.matches()) throw new ParseException("cannot interpret " + line, 0);
				String id = lineMatcher.group(1);
				if (id != null) id = id.trim();
				String command = lineMatcher.group(2).trim();
				String args = lineMatcher.group(3);
				char prefix;
				String result;
				try {
					result = getHandler(command).handle(command, args);
					prefix = '=';
				} catch (IOException e) {
					throw e;
				} catch (Throwable e) {
					if (e instanceof InterruptedException || e instanceof ThreadDeath) break;
					result= e.toString();
					prefix = '?';
				}
				out.write(prefix);
				if (id != null && id.length() > 0) out.write(id);
				out.write(' ');
				out.write(result);
				out.write("\n\n");
				out.flush();
			}
		} catch (IOException e) {
			log.log(Level.WARNING, "GTP client closed due to IO exception", e);
		} catch (ParseException e) {
			log.log(Level.SEVERE, "GTP client closed due to command line parse exception", e);
		}
	}
	
	/**
	 * Register an object that can handle many commands.  The object's class is
	 * scanned for all public methods, and each is registered as a command with
	 * an appropriate handler adapter.  The method's parameters will be filled
	 * from the command's argument string, and its return type converted to
	 * an appropriate result value.
	 * @param o the command controller
	 * @throws MissingFormatException if a formatter for one of the parameters or the return type cannot be found
	 */
	public void registerController(Object o) throws MissingFormatException {
		Method[] methods = o.getClass().getMethods();
		for (int i = 0; i < methods.length; i++) {
			Method m = methods[i];
			if (m.getDeclaringClass() == Object.class) continue;
			if (Modifier.isPublic(m.getModifiers())) registerHandler(m.getName(), new MethodHandler(o, m));			
		}
	}
	
	/**
	 * Register the given handler for the given command, replacing any previous
	 * handler for this command.
	 * @param command the command string in lowercase
	 * @param h the handler that will deal with that command from now on
	 */
	public void registerHandler(String command, Handler h) {
		handlers.put(command, h);
	}
	
	protected Handler getHandler(String command) {
		Handler h = (Handler) handlers.get(command);
		if (h == null) throw new MissingHandlerException(command);
		return h;
	}
	
	/**
	 * Register a formatter for objects of the given type.  This formatter will be
	 * used automatically to convert parameters and return values when invoking
	 * command methods in a controller.
	 * @param type the type this formatter can convert to and from
	 * @param f the formatter itself
	 */
	public void registerFormat(Class type, Format f) {
		formatters.put(type, f);
	}
	
	protected Format getFormat(Class type) throws MissingFormatException {
		Format f = (Format) formatters.get(type);
		if (f == null && type.isArray() && Object.class.isAssignableFrom(type.getComponentType()))
			f = (Format) formatters.get(Object[].class);
		if (f == null) throw new MissingFormatException(type);
		return f;
	}
	
	protected class ObjectArrayFormat extends Format {
		public Object parseObject(String source, ParsePosition pos) {
			throw new UnsupportedOperationException();
		}
		public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
			try {
				Object[] a = (Object[]) obj;
				Class elementType = a.getClass().getComponentType();
				if (elementType.isArray()) throw new IllegalArgumentException("cannot format multi-dimensional arrays");
				Format elementFormat = getFormat(elementType);
				for (int i = 0; i < a.length; i++) {
					elementFormat.format(a[i], toAppendTo, pos);
					toAppendTo.append('\n');
				}
				return toAppendTo;
			} catch (ClassCastException e) {
				throw new IllegalArgumentException("generic formatting works only for object arrays, not " + obj.getClass().getName());
			}
		}
	}
	
	protected class MetaController {
		public String[] EMPTY_STRING_ARRAY = new String[0];
		public int protocol_version() {return 2;}
		public boolean known_command(String commandName) {return handlers.containsKey(commandName);}
		public String[] list_commands() {return (String[]) handlers.keySet().toArray(EMPTY_STRING_ARRAY);}
		public String[] help() {return list_commands();}
		public void quit() {stop();}
	}
	
	
	protected class MethodHandler implements Handler {
		private final Object controller;
		private final Method method;
		private final Format[] argConverters;
		private final Format resultConverter;
		
		protected MethodHandler(Object controller, Method method) {
			this.controller = controller;
			this.method = method;
			Class[] paramTypes = method.getParameterTypes();
			argConverters = new Format[paramTypes.length];
			for (int i = 0; i < paramTypes.length; i++) {
				argConverters[i] = getFormat(paramTypes[i]);				
			}
			resultConverter = getFormat(method.getReturnType());
		}
		
		public String handle(String command, String arguments) throws Throwable {
			assert command.equals(method.getName());
			ParsePosition pos = new ParsePosition(0);
			Object[] args = new Object[argConverters.length];
			if (args.length > 0 && arguments == null) throw new IllegalArgumentException("missing arguments");
			for (int i = 0; i < args.length; i++) {
				args[i] = argConverters[i].parseObject(arguments, pos);
				if (pos.getErrorIndex() != -1) 
					throw new ParseException(
						"failed to parse argument " + i
						+ " of type " + method.getParameterTypes()[i].getName()
						+ " at source index " + pos.getErrorIndex(),
						pos.getErrorIndex()
					);
			}
			try {
				Object result = method.invoke(controller, args);
				return resultConverter.format(result);
			} catch (InvocationTargetException e) {
				throw e.getTargetException();
			}
		}
	}
	
}
