package ru.feodor0090.njtai.ui;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemCommandListener;
import javax.microedition.lcdui.List;

import ru.feodor0090.njtai.models.MangaObjects;

public class MangaList extends Form {
	
	

	public MangaList(String title, Displayable prev, MangaObjects items) {
		super(title);
		// TODO Auto-generated constructor stub
	}

	public static class OpenMangaButtonHandler implements ItemCommandListener {
		private int num;

		public static Command openCmd = new Command("Open", Command.ITEM, 1);

		public OpenMangaButtonHandler(int num) {
			this.num = num;

		}

		public void commandAction(Command arg0, Item arg1) {
			// TODO Auto-generated method stub
			System.out.println("Opening " + num);
		}
	}
}
