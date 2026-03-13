package d4lib.ahocorasick;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * PatternMatchingMachine basiert auf dem Algorithmus von Alfred V. Aho und
 * Margaret J. Corasick.
 *
 * <p>String Matching wird dabei auf einen deterministischen Zustandsautomaten
 * zurueckgefuehrt. Ein Wort ist dann in einem Text enthalten, wenn ein Zustand
 * beim Iterieren in einen Endzustand gelangt. Findet ein Match statt, so wird
 * der Index der letzten Matchposition zurueckgegeben sowie alle moeglichen
 * Woerter, die durch diesen Zustand erreicht werden koennen.</p>
 *
 * <h3>Benutzung</h3>
 * <pre>
 * PatternMatchingMachine pmm = PatternMatchingMachine.create("he", "she", "his", "hers");
 * KeywordLocations locations = pmm.match("ushers");
 * locations.print();
 * </pre>
 *
 * @author florian
 * @see KeywordLocations
 */
public class PatternMatchingMachine {

	private record GoToKey(int state, char a) implements Comparable<GoToKey> {
		@Override
		public int compareTo(GoToKey o) {
			int ret = Integer.compare(state, o.state);
			return ret != 0 ? ret : Character.compare(a, o.a);
		}
	}

	private static final int FAIL = -1;

	private final TreeMap<Integer, List<String>> output;
	private final TreeMap<GoToKey, Integer> gotoFunction;
	private final TreeMap<Integer, Integer> failureFunction;
	private final TreeMap<GoToKey, Integer> deltaFunction;

	private PatternMatchingMachine(List<String> keywords) {
		output = new TreeMap<>();
		gotoFunction = new TreeMap<>();
		failureFunction = new TreeMap<>();

		var sanitized = sanitizeKeywords(keywords);
		buildGoTo(sanitized);
		buildFailureAndOutput();
		deltaFunction = buildDelta();
	}

	/**
	 * Erstellt eine neue PatternMatchingMachine mit den gegebenen Keywords.
	 *
	 * @param keywords Schluesselwoerter fuer die Suche
	 * @return initialisierte PatternMatchingMachine
	 */
	public static PatternMatchingMachine create(List<String> keywords) {
		return new PatternMatchingMachine(keywords);
	}

	/**
	 * Erstellt eine neue PatternMatchingMachine mit den gegebenen Keywords.
	 *
	 * @param keywords Schluesselwoerter fuer die Suche
	 * @return initialisierte PatternMatchingMachine
	 */
	public static PatternMatchingMachine create(String... keywords) {
		return new PatternMatchingMachine(List.of(keywords));
	}

	/**
	 * Sucht im Text nach den Schluesselwoertern.
	 *
	 * @param text Text in dem gesucht werden soll
	 * @return Positionen der gefundenen Schluesselwoerter
	 */
	public KeywordLocations match(String text) {
		var keywordLocations = new KeywordLocations();
		int state = 0;

		for (int i = 0; i < text.length(); i++) {
			state = delta(state, text.charAt(i));

			var out = output.get(state);
			if (out != null && !out.isEmpty()) {
				keywordLocations.addLocation(out, i);
			}
		}

		return keywordLocations;
	}

	private static List<String> sanitizeKeywords(List<String> keywords) {
		var unique = new LinkedHashSet<String>();
		for (var keyword : keywords) {
			if (keyword != null && !keyword.isEmpty()) {
				unique.add(keyword);
			}
		}
		var result = new ArrayList<>(unique);
		result.sort(Comparator.comparingInt(String::length));
		return result;
	}

	// === Algorithmus 2: GoTo-Funktion ===

	private void buildGoTo(List<String> keywords) {
		int newstate = 0;
		for (var keyword : keywords) {
			newstate = enter(keyword, newstate);
		}
	}

	private int enter(String word, int newstate) {
		int state = 0;
		int j = 0;

		while (j < word.length() && oldG(state, word.charAt(j)) != FAIL) {
			state = oldG(state, word.charAt(j));
			j++;
		}

		for (int p = j; p < word.length(); p++) {
			newstate++;
			gotoFunction.put(new GoToKey(state, word.charAt(p)), newstate);
			state = newstate;
		}

		output.computeIfAbsent(state, k -> new ArrayList<>()).add(word);
		return newstate;
	}

	// === Algorithmus 3: Failure- und Output-Funktion ===

	private void buildFailureAndOutput() {
		Queue<Integer> queue = new LinkedList<>();

		for (var entry : gotoFunction.entrySet()) {
			if (entry.getKey().state() == 0 && entry.getValue() != 0) {
				queue.add(entry.getValue());
				failureFunction.put(entry.getValue(), 0);
			}
		}

		while (!queue.isEmpty()) {
			int r = queue.poll();

			for (var entry : gotoFunction.entrySet()) {
				if (entry.getKey().state() == r) {
					int s = entry.getValue();
					queue.add(s);

					int state = f(r);
					while (g(state, entry.getKey().a()) == FAIL) {
						state = f(state);
					}
					failureFunction.put(s, g(state, entry.getKey().a()));

					var outputFs = output.get(f(s));
					if (outputFs != null) {
						output.computeIfAbsent(s, k -> new ArrayList<>()).addAll(outputFs);
					}
				}
			}
		}
	}

	// === Algorithmus 4: Deterministische Delta-Funktion ===

	private TreeMap<GoToKey, Integer> buildDelta() {
		var delta = new TreeMap<GoToKey, Integer>();
		var alphabet = collectAlphabet();
		Queue<Integer> queue = new LinkedList<>();

		for (char a : alphabet) {
			int nextState = g(0, a);
			delta.put(new GoToKey(0, a), nextState);
			if (nextState != 0) {
				queue.add(nextState);
			}
		}

		while (!queue.isEmpty()) {
			int r = queue.poll();

			for (char a : alphabet) {
				int s = g(r, a);
				if (s != FAIL) {
					queue.add(s);
					delta.put(new GoToKey(r, a), s);
				} else {
					Integer resolved = delta.get(new GoToKey(f(r), a));
					delta.put(new GoToKey(r, a), resolved != null ? resolved : 0);
				}
			}
		}

		return delta;
	}

	private List<Character> collectAlphabet() {
		var seen = new TreeSet<Character>();
		for (var key : gotoFunction.keySet()) {
			seen.add(key.a());
		}
		return new ArrayList<>(seen);
	}

	// === Zustandsfunktionen ===

	private int delta(int state, char a) {
		var s = deltaFunction.get(new GoToKey(state, a));
		return s != null ? s : 0;
	}

	private int f(int state) {
		var result = failureFunction.get(state);
		return result != null ? result : 0;
	}

	private int g(int state, char a) {
		var i = gotoFunction.get(new GoToKey(state, a));
		if (i != null) {
			return i;
		}
		return state == 0 ? 0 : FAIL;
	}

	private int oldG(int state, char a) {
		var i = gotoFunction.get(new GoToKey(state, a));
		return i != null ? i : FAIL;
	}

}
