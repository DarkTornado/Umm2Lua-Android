package com.darktornado.umm2lua;

public class Umm2Lua {

	public static String compile(String source) {
		Compiler cmp = new Compiler();
		cmp.setSource(source);
		return cmp.execute();
	}

}
