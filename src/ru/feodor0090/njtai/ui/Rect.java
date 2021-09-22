package ru.feodor0090.njtai.ui;

public class Rect {
	public int x,y,w,h;

	public Rect(int x, int y, int w, int h) {
		super();
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
	}
	
	public boolean isIn(int nx, int ny) {
		return x<=nx&&y<=ny&&nx<=x+w&&ny<=y+h;
	}
}
