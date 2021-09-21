package ru.feodor0090.njtai;

import java.util.Vector;

public class StringUtil {
	public static String from(String s, String f)
	{
		return from(s,f,true);
	}
	
	public static String from(String s, String f, boolean incl)
	{
		int si = s.indexOf(f);
		if(si==-1) return "";
		if(!incl) si+=f.length();
		return s.substring(si);
	}
	
	public static String range(String s, String f, String t)
	{
		return range(s,f,t,false);
	}
	
	public static String range(String s, String f, String t, boolean incl)
	{
		if(s.length()==0) return "";
		int si = s.indexOf(f);
		if(si==-1) 
		{
			si = 0;
		}
		else if(!incl) 
		{
			si+=f.length();
		}
		int ei = s.indexOf(t, si);
		if(ei==-1 || t.length()==0) return s.substring(si);
		if(incl) ei+=t.length();
		
		return s.substring(si,ei);
	}
	
	public static String[] splitRanges(String s, String f, String t, boolean incl)
	{
		Vector v = new Vector();
		int i = 0;
		while(true)
		{
			int si = s.indexOf(f, i);
			if(si==-1) break;
			if(!incl) si+=f.length();
			int ei = s.indexOf(t, si);
			i = ei + t.length();
			if(incl) ei+=t.length();
			v.addElement(s.substring(si,ei));
		}
		String[] a = new String[v.size()];
		v.copyInto(a);
		v.removeAllElements();
		v = null;
		return a;
	}
	
	//untested
	public static String[] split(String str, String k)
	{
		Vector v = new Vector(32,16);
		int lle = 0;
		while(true)
		{
			int nle = str.indexOf(k, lle);
			if(nle == -1)
			{
				v.addElement(str.substring(lle, str.length()));
				break;
			}

			v.addElement(str.substring(lle, nle));
			lle = nle+k.length();
		}
		String[] a = new String[v.size()];
		v.copyInto(a);
		v.removeAllElements();
		v.trimToSize();
		v = null;
		return a;
	}
	
	//untested
	public static int lastIndexOf(String target, String k, int from, int to)
	{
		if(to==-1) to = target.length();
		if(target.indexOf(k)==-1) return -1;
		int i = 0;
		while(true)
		{
			int ni = target.indexOf(k,i);
			if(ni==-1 || ni>to) 
			{
				if(i>from||i<to) {
					return i;
				} else {
					return -1;
				}
			}
			
			i = ni+1;
		}
	}
	
	// untested
	public static String[] splitRangesReverse(String s, String f, String t)
	{
		Vector v = new Vector();
		int i = 0;
		while(true)
		{
			// start index
			int si = s.indexOf(f, i);
			if(si==-1) break;
			si+=f.length();
			
			// next index
			int ni = s.indexOf(f, si);
			/*if(ni==-1)
			{
				int ei2 = lastIndexOf(s, t, si, -1);
				if(ei2==-1) ei2 = s.length();
				v.addElement(s.substring(si,ei2));
				break;
			}*/ // unnessesary edge case?
			int ei = lastIndexOf(s, t, si, ni);
			
			System.out.println("si:"+si+" ni:"+ni+"+ found ei:"+ei+" l:"+s.length());
			if(ei==-1) ei = s.length();
			v.addElement(s.substring(si,ei));
			i = ei + t.length();
			if(ni==-1) break;
			
		}
		String[] a = new String[v.size()];
		v.copyInto(a);
		v.removeAllElements();
		v = null;
		return a;
	}
	
	public static String toSingleLine(String s)
	{
		return s.replace('\r', ' ').replace('\n', ' ').replace('\t', ' ');
	}
	
	public static String afterLastSlash(String s)
	{
		if(s.length()==0) return "";
		return s.substring(1+s.lastIndexOf('/'));
	}
	
	public static String withoutSpaces(String s)
	{
		StringBuffer b = new StringBuffer(s.length());
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if(c!=' '&&c!='\n'&&c!='\r'&&c!='\t') b.append(c);
		}
		return b.toString();
	}
	
	// SN
	public static String[] splitFull(String str, char c)
	{
		Vector v = new Vector(32,16);
		int lle = 0;
		while(true)
		{
			int nle = str.indexOf(c, lle);
			if(nle == -1)
			{
				v.addElement(str.substring(lle, str.length()));
				break;
			}

			v.addElement(str.substring(lle, nle));
			lle = nle+1;
		}
		String[] a = new String[v.size()];
		v.copyInto(a);
		v.removeAllElements();
		v.trimToSize();
		v = null;
		return a;
	}
	
	public static String removeLocaleUrlPrefix(String str)
	{
		if(str.charAt(0)=='/'&&str.charAt(3)=='/') return str.substring(4);
		return str;
	}
	
	static void assert(boolean b)
	{
		if(!b) throw new RuntimeException("Assert failed");
	}
	static void assert(String a, String b)
	{
		if(!a.equals(b))
		{
			System.out.println("\""+a+"\" calculated, \""+b+"\" expected.");
			throw new RuntimeException("Assert failed");
		}
	}
	static void arrsEq(String[] a, String[] b)
	{
		if(a.length!=b.length) assert(false);
		for (int i = 0; i < b.length; i++) {
			if(!a[i].equals(b[i])) assert(false);
		}
	}
	/*
	 * Executes the basic testing of above functons to ensure that they work correctly.
	 */
	public static void Test()
	{
		System.out.println("StringUtil tests run");
		
		// === from ===
			
		// basic
		assert(from("qwerty123hjkl","123"),"123hjkl");
		assert(from("qwerty123hjkl","123", true),"123hjkl");
		assert(from("qwerty123hjkl","123", false),"hjkl");
		// no match
		assert(from("qwerty123hjkl","000", true),"");
		assert(from("qwerty123hjkl","000", false),"");
		// str end
		assert(from("qwerty123hjkl","kl", true),"kl");
		assert(from("qwerty123hjkl","kl", false),"");
		// empty
		assert(from("qwerty123hjkl","", true),"qwerty123hjkl");
		assert(from("qwerty123hjkl","", false),"qwerty123hjkl");
		assert(from("","", true),"");
		assert(from("","", false),"");
		assert(from("","12", true),"");
		assert(from("","12", false),"");
		// pf match
		assert(from("qwe","qwe", true),"qwe");
		assert(from("qwe","qwe", false),"");
		
		// === range ===
		
		// basic
		assert(range("qwerty123hjkl","1","3"),"2");
		assert(range("qwerty123hjkl","1","3", true),"123");
		assert(range("qwerty123hjkl","1","3", false),"2");
		// no match
		assert(range("qwerty123hjkl","000","gfg", true),"qwerty123hjkl");
		assert(range("qwerty123hjkl","000", "nm", false),"qwerty123hjkl");
		assert(range("qwerty123hjkl","12","gfg", true),"123hjkl");
		assert(range("qwerty123hjkl","12", "nm", false),"3hjkl");
		assert(range("qwerty123hjkl","000","12", true),"qwerty12");
		assert(range("qwerty123hjkl","000", "12", false),"qwerty");
		// str end
		assert(range("qwerty123hjkl","kl","0", true),"kl");
		assert(range("qwerty123hjkl","kl", "0",false),"");
		// empty
		assert(range("qwerty123hjkl","","", true),"qwerty123hjkl");
		assert(range("qwerty123hjkl","","", false),"qwerty123hjkl");
		assert(range("qwerty123hjkl","1","", true),"123hjkl");
		assert(range("qwerty123hjkl","","1", false),"qwerty");
		assert(range("","","", true),"");
		// pf match
		assert(range("qwe","q","we", true),"qwe");
		assert(range("qwe","q","we", false),"");
		// overlap
		assert(range("12345","23","34",true),"234");
		assert(range("12345","23","34",false),"45");
		
		// === afterLastSlash
		
		// target
		assert(afterLastSlash("/ru/miss-kobayashis-dragon-maid"), "miss-kobayashis-dragon-maid");
		// no slash
		assert(afterLastSlash("123"), "123");
		// last slash
		assert(afterLastSlash("qwe/1234/"),"");
		assert(afterLastSlash("/"),"");
		
		// === RLUP ===
		
		// target
		assert(removeLocaleUrlPrefix("/ru/video"),"video");
		
		// === split ===
		
		// basic
		arrsEq(splitRanges("<a><b></b></a>","<b>","</b>", true), new String[] {"<b></b>"});
		arrsEq(splitRanges("<a><b></b><b></b></a>","<b>","</b>", true), new String[] {"<b></b>","<b></b>"});
		arrsEq(splitRanges("<a><b></b><b></b></a>","<b>","</b>", false), new String[] {"",""});
		arrsEq(splitRanges("<a><b><b></b><b></b></a>","<b>","</b>", false), new String[] {"<b>",""});
		// with content
		arrsEq(splitRanges("<a><b>123</b>1<b>45</b></a>","<b>","</b>", false), new String[] {"123","45"});
		// target
		arrsEq(splitRanges("         <h2 class=\"search-header\">\n" + 
				"                          Библиотека видео                      </h2>\n" + 
				"                      \n" + 
				"<div id=\"main_results\">\n" + 
				"<p>\n" + 
				"  Показаны результаты с <strong>1</strong> по <strong>1</strong> из <strong>1</strong></p>\n" + 
				"<ul class=\"search-results\">\n" + 
				"    <li>\n" + 
				"   <a href=\"/ru/miss-kobayashis-dragon-maid\" class=\"clearfix\">\n" + 
				"   <table>\n" + 
				"     <tbody>\n" + 
				"       <tr>\n" + 
				"         <td>\n" + 
				"      <span class=\"mug\">\n" + 
				"            <img src=\"https://img1.ak.crunchyroll.com/i/spire4/a70f5494b209b7b4508ad6fe3f37dd1b1486497891_thumb.jpg\" />\n" + 
				"            </span>\n" + 
				"\n" + 
				"         </td>\n" + 
				"         <td>\n" + 
				"      <span class=\"info\">\n" + 
				"        <span class=\"name\">\n" + 
				"           Дракониха-горничная госпожи Кобаяси                      <span class=\"type\">\n" + 
				"             (Сериал)\n" + 
				"           </span>\n" + 
				"                   </span>\n" + 
				"                <span class=\"desc\">Кобаяси — самая обычная офисная сотрудница, проживающая свою скучную жизнь в маленькой квартирке. Так и тянулись её рутинные дни, пока не спасла...</span>\n" + 
				"      </span>\n" + 
				"\n" + 
				"         </td>\n" + 
				"       </tr>\n" + 
				"     </tbody>\n" + 
				"   </table>\n" + 
				"   </a>\n" + 
				"  </li>\n" + 
				" </ul>\n" + 
				"</div>\n" + 
				"<script type=\"text/javascript\">\n" + 
				"  $(\".product-info, .name\").dotdotdot({watch:\"window\"});\n" + 
				"</script>\n" + 
				"                          <div id=\"main_results_paginator\"></div> <li>lol</li>","<li>","</li>",false), 
				new String[] {"\n" + 
						"   <a href=\"/ru/miss-kobayashis-dragon-maid\" class=\"clearfix\">\n" + 
						"   <table>\n" + 
						"     <tbody>\n" + 
						"       <tr>\n" + 
						"         <td>\n" + 
						"      <span class=\"mug\">\n" + 
						"            <img src=\"https://img1.ak.crunchyroll.com/i/spire4/a70f5494b209b7b4508ad6fe3f37dd1b1486497891_thumb.jpg\" />\n" + 
						"            </span>\n" + 
						"\n" + 
						"         </td>\n" + 
						"         <td>\n" + 
						"      <span class=\"info\">\n" + 
						"        <span class=\"name\">\n" + 
						"           Дракониха-горничная госпожи Кобаяси                      <span class=\"type\">\n" + 
						"             (Сериал)\n" + 
						"           </span>\n" + 
						"                   </span>\n" + 
						"                <span class=\"desc\">Кобаяси — самая обычная офисная сотрудница, проживающая свою скучную жизнь в маленькой квартирке. Так и тянулись её рутинные дни, пока не спасла...</span>\n" + 
						"      </span>\n" + 
						"\n" + 
						"         </td>\n" + 
						"       </tr>\n" + 
						"     </tbody>\n" + 
						"   </table>\n" + 
						"   </a>\n" + 
						"  ","lol"});
		
		// === LOG ===
		System.out.println("StringUtil tests ok");
	}
}
