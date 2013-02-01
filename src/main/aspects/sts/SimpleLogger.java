package sts;


public class SimpleLogger {

	private static final Object[] EMPTY_ARRAY = new Object[0];
	private String name;

	public SimpleLogger(String name) {
		this.name = name;		
	}

	public static SimpleLogger getLogger(Class<?> clazz) {
		return new SimpleLogger(clazz.getName());
	}

	public void warn(String msg, Object... args) {
		log("WARN", msg, null, args);
	}

	private void log(String level, String msg, Throwable cause, Object[] args) {
		System.err.println(level + " "+ String.format(msg, args) + " ["+name +"]");
		if(cause != null)
			cause.printStackTrace(System.err);
	}

	public boolean isDebugEnabled() {
		return true;
	}

	public void debug(String msg) {
		log("DEBUG", msg, null, EMPTY_ARRAY);
		
	}

	public void info(String msg, Object ...args) {
		log("INFO", msg, null, args);
	}

	public void error(String msg, Throwable cause) {
		log("ERROR", msg, cause, EMPTY_ARRAY);		
	}

}
