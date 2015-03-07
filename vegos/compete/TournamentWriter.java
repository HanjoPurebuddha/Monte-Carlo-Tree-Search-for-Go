package com.ideanest.vegos.compete;

import java.io.*;
import java.io.PrintWriter;
import java.io.Writer;
import java.text.NumberFormat;
import java.util.*;
import java.util.Iterator;
import java.util.Map;

import com.ideanest.vegos.ai.Player;
import com.ideanest.vegos.game.Color;
import com.ideanest.vegos.game.Game;

/**
 * Print tournament results out in HTML.
 * @author Piotr Kaminski
 */

public class TournamentWriter extends FilterWriter {
	private final PrintWriter out;
	public TournamentWriter(Writer out) {
		super(new PrintWriter(out));
		this.out = (PrintWriter) super.out;
	}
	
	private final NumberFormat pctFormat = NumberFormat.getPercentInstance();
	private Game lastGame;
	private Map players = new LinkedHashMap();
	
	public void write(Tournament t) {
		out.println("<html>");
		out.println("<head><title>Tournament Results</title></head>");
		out.println("<body>");
		out.println("<p>Tournament over " + t.getNumGamesPerMatch() * t.getMatches().size() + " games.</p>");
		out.println("<table border='1'>");
		out.println("<tr><th>ID</th><th>Player</th><th>Score</th></tr>");
		for (Iterator it = t.getMatches().iterator(); it.hasNext();) {
			Match m = (Match) it.next();
			for (int i=0; i<2; i++) {
				Player p = m.getPlayer(Color.get(i));
				Float scoreObj = (Float) players.get(p);
				float score = scoreObj == null ? 0 : scoreObj.floatValue();
				score = score + m.getStats().getNumGamesWon(Color.get(i)) + m.getStats().getNumGamesDrawn() / 2f;
				players.put(p, new Float(score));
			}
		}
		char id = 'A';
		for (Iterator it = players.entrySet().iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry) it.next();
			out.println("<tr><td>" + id + "</td><td>" + entry.getKey() + "</td><td>" + entry.getValue() + "</td></tr>");
			entry.setValue(String.valueOf(id));
			id++;
		}
		out.println("</table>");
		out.println("<p>Each match averaged over " + t.getNumGamesPerMatch() + " games.</p>");
		out.println("<table border='1'>");
		out.println("<tr><th>Game</th><th>Players</th><th>Results</th></tr>");
		lastGame = null;
		for (Iterator it = t.getMatches().iterator(); it.hasNext();) {
			write ((Match) it.next());
		}
		out.println("</table>");
		out.println("</body>");
		out.println("</html>");
	}
	
	protected void write(Match m) {
		out.print("<tr><td>");
		if (lastGame != m.getGame()) {
			lastGame = m.getGame();
			out.print(lastGame);
		}
		String blackid = (String) players.get(m.getPlayer(Color.BLACK));
		String whiteid = (String) players.get(m.getPlayer(Color.WHITE));
		out.print("</td><td align='center'>");
		out.print(blackid);
		out.print(" (black)<br/>vs<br/>");
		out.print(whiteid);
		out.println(" (white)</td><td>");
		for (Iterator it = m.getStats().getGameResults().iterator(); it.hasNext(); ) {
			write ((Statistics.GameResult) it.next(), blackid, whiteid);
			if (it.hasNext()) out.println("<br/>");
			out.println();
		}
		out.println("</td></tr>");
	}
	
	protected void write(Statistics.GameResult gr, String blackid, String whiteid) {
		if (gr.score == 0) {
			out.print("draw");
		} else {
			out.print(gr.score > 0 ? blackid : whiteid);
			out.print(" by ");
			out.print(Math.abs(gr.score));
		}
		out.print(" (" + gr.numMoves + " moves)");
	}

}
