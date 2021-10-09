package njtai.d;

public class NJTAID {

	public static void main(String[] args) {
		System.out.println();
		System.out.println("Desktop frontend can't be loaded. Are you using mobile-only build?");
		try {
			Thread.sleep(4000);
		} catch (InterruptedException e) {
		}
		System.exit(1);
	}
}
