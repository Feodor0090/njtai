package njtai.models;

/**
 * Builds an abstraction above platform's network interaction, allowing use the
 * same code to query data both on ME and SE.
 */
public abstract class WebAPIA {
	public static WebAPIA inst;

	/**
	 * Performs HTTP GET request.
	 * 
	 * @param url URL to work with.
	 * @return Null if the operation fails, response data overwise.
	 */
	public abstract byte[] get(String url);

	/**
	 * Performs HTTP GET request.
	 * 
	 * @param url URL to work with.
	 * @return Null if the operation fails, response data as UTF-8 string overwise.
	 */
	public abstract String getUtf(String url);
}
