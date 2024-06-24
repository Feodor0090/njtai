package njtai.models;

import java.io.IOException;

/**
 * Builds an abstraction above platform's network interaction, allowing use the
 * same code to query data both on ME and SE.
 */
public abstract class WebAPIA {
	/**
	 * Instance to work with.
	 */
	public static WebAPIA inst;
	
	/**
	 * Performs HTTP GET request.
	 * 
	 * @param url URL to work with.
	 * @throws IOException
	 * @return Response data in bytes.
	 */
	public abstract byte[] get(String url) throws IOException;

	/**
	 * Performs HTTP GET request.
	 * 
	 * @see njtai.models.WebAPIA#get(String)
	 * @param url URL to work with.
	 * @return Null if the operation fails, response data overwise.
	 */
	public abstract byte[] getOrNull(String url);

	/**
	 * Performs HTTP GET request.
	 * 
	 * @see njtai.models.WebAPIA#get(String)
	 * @param url URL to work with.
	 * @return Response data as UTF-8 string.
	 * @throws IOException 
	 */
	public abstract String getUtf(String url) throws IOException;

	/**
	 * Performs HTTP GET request.
	 * 
	 * @see njtai.models.WebAPIA#get(String)
	 * @see njtai.models.WebAPIA#getUtf(String)
	 * @param url URL to work with.
	 * @return Null if the operation fails, response data as UTF-8 string overwise.
	 */
	public abstract String getUtfOrNull(String url);
}
