package algorithm.stringmatching;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.TreeMap;

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

	private static class GoToKey implements Comparable<GoToKey> {
		private final int state;
		private final char a;

		GoToKey(int state, char a) {
			this.state = state;
			this.a = a;
		}

		@Override
		public int compareTo(GoToKey o) {
			int ret = Integer.compare(state, o.state);
			return ret != 0 ? ret : Character.compare(a, o.a);
		}
	}

	private static final int FAIL = -1;

	private TreeMap<Integer, List<String>> output;
	private TreeMap<GoToKey, Integer> gotoFunction;
	private TreeMap<Integer, Integer> failureFunction;
	private TreeMap<GoToKey, Integer> deltaFunction;

	private PatternMatchingMachine(List<String> keywords) {
		List<String> sanitized = sanitizeKeywords(keywords);
		buildGoTo(sanitized);
		buildFailureAndOutput();
		buildDelta();
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
		return new PatternMatchingMachine(Arrays.asList(keywords));
	}

	/**
	 * Sucht im Text nach den Schluesselwoertern.
	 *
	 * @param text Text in dem gesucht werden soll
	 * @return Positionen der gefundenen Schluesselwoerter
	 */
	public KeywordLocations match(String text) {
		KeywordLocations keywordLocations = new KeywordLocations();
		int state = 0;

		for (int i = 0; i < text.length(); i++) {
			state = delta(state, text.charAt(i));

			List<String> out = output.get(state);
			if (out != null && !out.isEmpty()) {
				keywordLocations.addLocation(out, i);
			}
		}

		return keywordLocations;
	}

	/**
	 * Statische Match-Methode fuer Rueckwaertskompatibilitaet.
	 *
	 * @deprecated Verwende stattdessen die Instanzmethode {@link #match(String)}
	 */
	@Deprecated
	public static KeywordLocations match(String text, PatternMatchingMachine pmm) {
		return pmm.match(text);
	}

	private static List<String> sanitizeKeywords(List<String> keywords) {
		LinkedHashSet<String> unique = new LinkedHashSet<>();
		for (String keyword : keywords) {
			if (keyword != null && !keyword.isEmpty()) {
				unique.add(keyword);
			}
		}
		List<String> result = new ArrayList<>(unique);
		result.sort(Comparator.comparingInt(String::length));
		return result;
	}

	// === Algorithmus 2: GoTo-Funktion ===

	private void buildGoTo(List<String> keywords) {
		output = new TreeMap<>();
		gotoFunction = new TreeMap<>();

		int newstate = 0;
		for (String keyword : keywords) {
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
		failureFunction = new TreeMap<>();
		Queue<Integer> queue = new LinkedList<>();

		for (GoToKey key : gotoFunction.keySet()) {
			if (key.state == 0) {
				int s = gotoFunction.get(key);
				if (s != 0) {
					queue.add(s);
					failureFunction.put(s, 0);
				}
			}
		}

		while (!queue.isEmpty()) {
			int r = queue.poll();

			for (GoToKey key : gotoFunction.keySet()) {
				if (key.state == r) {
					int s = gotoFunction.get(key);
					queue.add(s);

					int state = f(r);
					while (g(state, key.a) == FAIL) {
						state = f(state);
					}
					failureFunction.put(s, g(state, key.a));

					List<String> outputFs = output.get(f(s));
					if (outputFs != null) {
						output.computeIfAbsent(s, k -> new ArrayList<>()).addAll(outputFs);
					}
				}
			}
		}
	}

	// === Algorithmus 4: Deterministische Delta-Funktion ===

	private void buildDelta() {
		deltaFunction = new TreeMap<>();
		List<Character> alphabet = collectAlphabet();
		Queue<Integer> queue = new LinkedList<>();

		for (char a : alphabet) {
			int nextState = g(0, a);
			deltaFunction.put(new GoToKey(0, a), nextState);
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
					deltaFunction.put(new GoToKey(r, a), s);
				} else {
					deltaFunction.put(new GoToKey(r, a), delta(f(r), a));
				}
			}
		}
	}

	private List<Character> collectAlphabet() {
		TreeMap<Character, Boolean> seen = new TreeMap<>();
		for (GoToKey key : gotoFunction.keySet()) {
			seen.put(key.a, true);
		}
		return new ArrayList<>(seen.keySet());
	}

	// === Zustandsfunktionen ===

	private int delta(int state, char a) {
		Integer s = deltaFunction.get(new GoToKey(state, a));
		return s != null ? s : 0;
	}

	private int f(int state) {
		Integer result = failureFunction.get(state);
		return result != null ? result : 0;
	}

	private int g(int state, char a) {
		Integer i = gotoFunction.get(new GoToKey(state, a));
		if (i != null) {
			return i;
		}
		return state == 0 ? 0 : FAIL;
	}

	private int oldG(int state, char a) {
		Integer i = gotoFunction.get(new GoToKey(state, a));
		return i != null ? i : FAIL;
	}

	public static void main(String[] args) {
		System.out.println("=== Test 1: Standardbeispiel ===");
		PatternMatchingMachine pmm = PatternMatchingMachine.create("he", "she", "his", "hers");
		pmm.match("ushers").print();

		System.out.println("\n=== Test 2: hers vor her (ehemaliger Crash) ===");
		PatternMatchingMachine pmm2 = PatternMatchingMachine.create("hers", "her");
		pmm2.match("ushers").print();

		System.out.println("\n=== Test 3: Duplikate und leere Strings ===");
		PatternMatchingMachine pmm3 = PatternMatchingMachine.create("he", "", "he", "she");
		pmm3.match("ushers").print();
	}
}
