package algorithm.stringmatching;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;

public class PatternMatchingMachine {

	private class GoToKey implements Comparable<GoToKey> {
		private final Integer state;
		private final Character a;

		public GoToKey(final Integer state, final Character a) {
			this.state = state;
			this.a = a;
		}

		public int compareTo(final GoToKey o) {
			int ret = 0;

			if ((ret = state.compareTo(o.state)) != 0) {
				return ret;
			} else {
				return a.compareTo(o.a);
			}

		}
	}

	private static final int FAIL = -1;
	private static final List<String> EMPTY = null;

	private TreeMap<Integer, List<String>> output;

	private TreeMap<GoToKey, Integer> goto_function;

	private TreeMap<Integer, Integer> failure_function;

	private PatternMatchingMachine(List<String> keywords) {
		createGoTo(keywords);
		createFailureAndOutput(goto_function, output);
	}

	public static PatternMatchingMachine create(List<String> keywords) {
		PatternMatchingMachine patternMatchingMachine = new PatternMatchingMachine(
				keywords);

		return patternMatchingMachine;
	}

	public static KeywordLocations match(String text,
			PatternMatchingMachine patternMatchingMachine) {

		KeywordLocations keywordLocations = new KeywordLocations();

		int state = 0;

		for (int i = 0; i < text.length(); i++) {

			char a_i = text.charAt(i);

			while (patternMatchingMachine.g(state, a_i) == PatternMatchingMachine.FAIL) {
				state = patternMatchingMachine.f(state);
			}

			state = patternMatchingMachine.g(state, a_i);

			if (patternMatchingMachine.output(state) != PatternMatchingMachine.EMPTY) {
				keywordLocations.addLocation(patternMatchingMachine
						.output(state), i);
			}
		}

		return keywordLocations;

	}

	private void createGoTo(List<String> keywords) {

		// Create new Output Function
		output = new TreeMap<Integer, List<String>>();
		goto_function = new TreeMap<GoToKey, Integer>();

		// pre-initialization
		Integer newstate = 0;
		for (String y_i : keywords) {
			newstate = enter(y_i, newstate);
		}

		// TODO alle nichtvorhandenen Übergänge am Start
		// g(0,a) auf 0 (Startzustand )setzen.
		// -> Implizit in g enthalten
	}

	private Integer enter(String a, Integer newstate) {

		// pre-init
		Integer state = 0;
		Integer j = 0;
		Character a_j = a.charAt(j);
		// enter

		while (old_g(state, a_j) != PatternMatchingMachine.FAIL) {
			state = old_g(state, a_j);
			j++;
			a_j = a.charAt(j);
		}
		for (Integer p = j; p < a.length(); p++) {
			newstate = newstate + 1;

			Character a_p = a.charAt(p);

			GoToKey gotoKey = new GoToKey(state, a_p);

			goto_function.put(gotoKey, newstate);

			state = newstate;

		}

		if (!output.containsKey(state)) {
			ArrayList<String> value = new ArrayList<String>();

			value.add(a);

			output.put(state, value);
		} else {
			List<String> value = output.get(state);

			value.add(a);
		}

		return newstate;

	}

	private void createFailureAndOutput(
			TreeMap<GoToKey, Integer> goto_function,
			TreeMap<Integer, List<String>> output_function) {

		failure_function = new TreeMap<Integer, Integer>();

		Queue<Integer> queue = new LinkedList<Integer>();

		List<GoToKey> zero_state_transistion = get_zero_transition(goto_function);

		for (GoToKey key : zero_state_transistion) {
			Integer s = -1;
			// TODO Check auf fail auch - nein, wnen man das Paper liest
			// erst am Ende der erstellung wird goto(0,a) auf Fail gesetzt
			// wenn es keien Verbindung gibt.
			if ((s = goto_function.get(key)) != 0) {
				queue.add(s);
				failure_function.put(s, 0);
			}
		}

		while (!queue.isEmpty()) {
			// Let r be the next state in queue
			Integer r = queue.poll();

			List<GoToKey> not_fail_transition = get_not_fail(goto_function, r);

			Integer state = -1;

			for (GoToKey key : not_fail_transition) {

				Integer s = goto_function.get(key);

				queue.add(s);
				state = f(r);
				GoToKey key_state = new GoToKey(state, key.a);

				while (g(key_state.state, key_state.a) == FAIL) {
					state = f(key_state.state);
					key_state = new GoToKey(state, key.a);
				}

				failure_function.put(s, g(key_state.state, key_state.a));

				List<String> output_s = output_function.get(s);
				List<String> output_fs = output_function.get(f(s));

				if (output_fs != null) {
					output_s.addAll(output_fs);
				}
			}
		}
	}

	private List<GoToKey> get_zero_transition(
			TreeMap<GoToKey, Integer> gotoFunction) {
		List<GoToKey> zero_transition = new ArrayList<GoToKey>();

		Set<GoToKey> all_transition = gotoFunction.keySet();

		for (GoToKey key : all_transition) {
			if (key.state == 0) {
				GoToKey add = new GoToKey(key.state, key.a);

				zero_transition.add(add);
			}
		}

		return zero_transition;
	}

	/**
	 * Gibt alle Character zurück die nicht in einen Fehlerzustand überführen
	 * ausgehend vom Zustand state
	 * 
	 * @param goto_function
	 * @return
	 */
	private List<GoToKey> get_not_fail(TreeMap<GoToKey, Integer> goto_function,
			Integer state) {
		Set<GoToKey> goto_set = goto_function.keySet();

		List<GoToKey> not_fail = new ArrayList<GoToKey>();

		for (GoToKey key : goto_set) {
			if (state.equals(key.state) && g(key.state, key.a) != FAIL) {
				not_fail.add(key);
			}
		}

		return not_fail;
	}

	private List<String> output(Integer state) {
		List<String> values = output.get(state);

		if (values == null || values.isEmpty()) {
			return PatternMatchingMachine.EMPTY;
		} else {
			return values;
		}
	}

	private Integer f(Integer state) {
		Integer f = failure_function.get(state);

		if (f == null) {
			return 0;
		} else {
			return f;
		}
	}

	private Integer g(Integer state, Character aI) {
		GoToKey key = new GoToKey(state, aI);

		Integer i = goto_function.get(key);

		if (i == null && state != 0) {
			return FAIL;
		} else if (i == null && state == 0) {
			return 0;
		} else {
			return i;
		}
	}

	private Integer old_g(Integer state, Character a) {
		GoToKey key = new GoToKey(state, a);

		Integer i = goto_function.get(key);

		if (i == null) {
			return FAIL;
		} else {
			return i;
		}
	}

	public static void main(String[] args) {

		List<String> keywords = new ArrayList<String>();

		keywords.add("he");
		keywords.add("she");
		keywords.add("his");
		keywords.add("hers");

		PatternMatchingMachine pmm = PatternMatchingMachine.create(keywords);

		KeywordLocations keywordLocation = PatternMatchingMachine.match(
				"ushers has very proud of shershe", pmm);
		
		keywordLocation.print();

	}
}
