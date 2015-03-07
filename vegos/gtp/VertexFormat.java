package com.ideanest.vegos.gtp;

import java.text.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class VertexFormat extends Format {
	private static final Matcher matcher = Pattern.compile(" *((pass)|(resign)|(([a-z])(\\d\\d?)))\\b", Pattern.CASE_INSENSITIVE).matcher("");
	public Object parseObject(String source, ParsePosition pos) {
		matcher.reset(source);
		if (!matcher.find(pos.getIndex())) {
			pos.setErrorIndex(pos.getIndex());
			return null;
		}
		pos.setIndex(matcher.end(1));
		return Vertex.get(source.substring(matcher.start(1), matcher.end(1)));
	}
	public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
		if (!(obj instanceof Vertex)) throw new IllegalArgumentException();
		toAppendTo.append(obj);
		return toAppendTo;
	}
}

