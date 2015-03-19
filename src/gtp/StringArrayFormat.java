package gtp;

import java.text.*;

class StringArrayFormat extends Format {
	public Object parseObject(String source, ParsePosition pos) {
		throw new UnsupportedOperationException();
	}
	public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
		try {
			String[] strings = (String[]) obj;
			for (int i = 0; i < strings.length; i++) {
				toAppendTo.append(strings[i]).append('\n');
			}
			return toAppendTo;
		} catch (ClassCastException e) {
			throw new IllegalArgumentException();
		}
	}
}
	
