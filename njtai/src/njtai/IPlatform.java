package njtai;

public interface IPlatform {

	void loadPrefs();

	boolean savePrefs();

	void initAPIAs();

	void exit();

	void repaint();

	Object decodeImage(byte[] data);

	/**
	 * On LCDUI platform must receive Image object and fit it to 2/3 of screen. On SWT/AWT/Swing can do nothing.
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
