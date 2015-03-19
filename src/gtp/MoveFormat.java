package gtp;

import java.text.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import game.*;


class MoveFormat extends Format {
	private final Format colorFormat = new ColorFormat(), vertexFormat = new VertexFormat();
	public Object parseObject(String source, ParsePosition pos) {
		ParsePosition pp = new ParsePosition(pos.getIndex());
		Color color = (Color) colorFormat.parseObject(source, pp);
		Vertex vertex = (Vertex) vertexFormat.parseObject(source, pp);
		if (color == null || vertex == null) {
			pos.setErrorIndex(pos.getIndex());
			return null;
		}
		pos.setIndex(pp.getIndex());
		return new Move(color, vertex);
	}
	public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
		if (!(obj instanceof Move)) throw new IllegalArgumentException();
		toAppendTo.append(obj);
		return toAppendTo;
	}
}

