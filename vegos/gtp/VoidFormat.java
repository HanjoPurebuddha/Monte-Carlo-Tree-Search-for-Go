package com.ideanest.vegos.gtp;

import java.text.*;

class VoidFormat extends Format {
	private static final VoidFormat instance = new VoidFormat();
	public static VoidFormat getInstance() {
		return instance;
	}

	public Object parseObject(String source, ParsePosition pos) {
		assert source == null || source.length() == 0;
		return null;
	}
	public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
		assert obj == null;
		return toAppendTo;
	}
}
