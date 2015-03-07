package com.ideanest.vegos.gtp;

import java.text.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ideanest.vegos.game.*;


class ColorFormat extends Format {
	private static final Matcher matcher = Pattern.compile(" *((white)|(w)|(black)|(b))\\b", Pattern.CASE_INSENSITIVE).matcher("");
	public Object parseObject(String source, ParsePosition pos) {
		matcher.reset(source);
		if (!matcher.find(pos.getIndex())) {
			pos.setErrorIndex(pos.getIndex());
			return null;
		}
		pos.setIndex(matcher.end(1));
		return Character.toLowerCase(matcher.group(1).charAt(0)) == 'w' ? Color.WHITE : Color.BLACK;
	}
	public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
		if (!(obj instanceof Color)) throw new IllegalArgumentException();
		toAppendTo.append(obj);
		return toAppendTo;
	}
}

