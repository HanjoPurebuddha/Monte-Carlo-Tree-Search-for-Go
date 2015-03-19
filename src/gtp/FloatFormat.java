package gtp;

import java.text.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class FloatFormat extends Format {
	private static final Matcher matcher = Pattern.compile(" *(\\d+(\\.\\d+)?)\\b").matcher("");
	public Object parseObject(String source, ParsePosition pos) {
		matcher.reset(source);
		if (!matcher.find(pos.getIndex())) {
			pos.setErrorIndex(pos.getIndex());
			return null;
		}
		pos.setIndex(matcher.end(1));
		return new Float(matcher.group(1));
	}		
	public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
		if (!(obj instanceof Float)) throw new IllegalArgumentException();
		return toAppendTo.append(obj);
	}
}

