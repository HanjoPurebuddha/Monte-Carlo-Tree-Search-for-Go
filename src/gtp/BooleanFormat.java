package gtp;

import java.text.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class BooleanFormat extends Format {
	private static final Matcher matcher = Pattern.compile(" *((true)|(false))\\b").matcher("");
	public Object parseObject(String source, ParsePosition pos) {
		matcher.reset(source);
		if (!matcher.find(pos.getIndex())) {
			pos.setErrorIndex(pos.getIndex());
			return null;
		}
		pos.setIndex(matcher.end(1));
		return "true".equals(matcher.group(1)) ? Boolean.TRUE : Boolean.FALSE;
	}
	public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
		if (!(obj instanceof Boolean)) throw new IllegalArgumentException();
		toAppendTo.append(obj);
		return toAppendTo;
	}
}

