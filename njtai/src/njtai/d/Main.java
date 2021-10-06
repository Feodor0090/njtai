package njtai.d;

public class Main {

	public static void main(String[] args) {
		try
		{
			Object d = Class.forName("njtai.d.NJTAID").newInstance();
			((Runnable)d).run();
		} catch (Throwable t) {
			System.out.println();
			System.out.println("Desktop frontend can't be loaded. Are you using mobile-only build?");
			try {
				Thread.sleep(4000);
			} catch (InterruptedException e) {
			}
			System.exit(1);
		}
	}
}
