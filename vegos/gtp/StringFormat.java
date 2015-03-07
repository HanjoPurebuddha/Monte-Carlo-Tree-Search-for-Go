package com.ideanest.vegos.gtp;

import java.text.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class StringFormat extends Format {
	private static final Matcher matcher = Pattern.compile(" *(\\S+)").matcher("");
	public Object parseObject(String source, ParsePosition pos) {
		matcher.reset(source);
		if (!matcher.find(pos.getIndex())) {
			pos.setErrorIndex(pos.getIndex());
			return null;
		}
		pos.setIndex(matcher.end(1));
		return matcher.group(1);
	}
	public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
		if (!(obj instanceof String)) throw new IllegalArgumentException();
		return toAppendTo.append((String) obj);
	}
}

