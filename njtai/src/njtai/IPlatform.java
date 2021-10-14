package njtai;

/**
 * Top interface for main platform-specific classes.
 * 
 * @author Feodor0090
 *
 */
public interface IPlatform {

	/**
	 * Loads prefs.
	 */
	void loadPrefs();

	/**
	 * Writes prefs.
	 * @return False if failed.
	 */
	boolean savePrefs();

	/**
	 * Inits APIAs.
	 */
	void initAPIAs();

	/**
	 * Quits the application.
	 */
	void exit();

	/**
	 * Forces UI update.
	 */
	void repaint();

	/**
	 * Decodes image from raw file content.
	 * @param data File content.
	 * @return Image in platform-specific type.
	 */
	Object decodeImage(byte[] data);

	/**
	 * On LCDUI platform must receive Image object and fit it to 2/3 of screen. On
	 * SWT/AWT/Swing can do nothing.
	 * 
	 * @param original Loaded image.
	 * @return Image to use in UI.
	 */
	Object prescaleCover(Object original);

	/**
	 * Shows an alert/popup with a message.
	 * 
	 * @param title
	 * @param text
	 * @param type
	 *              <ul>
	 *              <li>0 - info
	 *              <li>1 - success
	 *              <li>2 - warning
	 *              <li>3 - error
	 *              </ul>
	 * @param prev  On screen-swap-based UI engines like LCDUI, this is intended to
	 *              be a previous screen, where we are going to return.
	 */
	void showNotification(String title, String text, int type, Object prev);
}
