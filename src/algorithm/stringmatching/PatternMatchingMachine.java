package algorithm.stringmatching;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;

/**
 * PatternMatchingMachine basiert auf dem Algorithmus von Alfred V. Aho und
 * Margaret J.Corasick.
 * 
 * String Matching wird dabei auf einen Zustandsautomaten zurückgeführt. Ein
 * Wort ist dann in einem Text enthalten wenn ein Zustand beim iterieren in
 * einen Endzustand gelangt. Findet ein Match statt so wird der Index der
 * letzten Matchposition zurückgegeben sowie alle möglichen Wörter die durch
 * diesen Zustand erreicht werden können.
 * 
 * Findet der Zustandsübergangsgraph keinen Folgezustand weil kein Zeichen
 * gematcht wird, wird über eine Fehlerfunktion der nächste Zustand ausgewählt
 * der für ein Match der vorherigen Zeichen noch gültig ist.
 * 
 * Benutzung
 * 
 * Zuerst muss eine neue PatternMatchingMachine erstellt werden durch die
 * statische {@link #create(List)} Methode.
 * <p>
 * <code> 
 * 
 * List<String> keywords = new ArrayList<String>();
 * 
 * keywords.add("he");
 * keywords.add("she");
 * keywords.add("his");
 * keywords.add("hers");
 * 
 * PatternMatchingMachine patternMatchingMachine = PatternMatchingMachine.create(keywords);
 * 
 * </code>
 * <p>
 * Die PatternMatchingMachine ist vollständig initialisiert und man kann auf
 * einen Inputtext matchen.
 * <p>
 * <code>
 * KeywordLocations keywordLocation = PatternMatchingMachine.match("ushers", pmm);
 * </code>
 * 
 * 
 * Implementierungsdetails
 * 
 * Zustände werden beginnend bei 0 aufsteigend als natürliche Zahl dargestellt.
 * Zeichen für die Zustandsübergänge werden als Character dargestellt. Der
 * Zustandsübergangsgraph wird durch eine TreeMap realisiert der zu einem
 * Zustand und dem zu lesenden Zeichen auf einen Folgezustand abbildet.
 * 
 * 
 * TODO
 * 
 * <ul>
 * <li>Deterministischer Algorithmus für die goto Funktion implementieren
 * (Algorithmus_4)
 * <li>Findet hers nicht bei der Eingabenreihenfolge {he,she,his,hers} wenn
 * delta Funktion benutzt wird!
 * <li>Crasht wenn nach dem Einfügen von hers noch ein her eingefügt wird. In
 * der Funktion {@link #enter(String, Integer)} wird solange durchiteriert bis
 * ein Nachfolgezustand ungleich FAIL ist, dann haben wir aber
 * IndexOutOfBounds->Keywords müssen der Länge nach sortiert sein und nicht
 * doppelt vorkommen. Behebt das Problem dadrüber aber nicht. Bislang gibt die
 * delta Funktion ein 0 zurück falls ein Wert nicht gefunden wurde, ist das so
 * richtig? sein.
 * </ul>
 * 
 * 
 * @author florian
 * @see KeywordLocations
 * 
 */
public class PatternMatchingMachine {

	/**
	 * Innere Klasse für einen Zustandsübergang. Durch das Attribut state wird
	 * der Zustand identifiziert und der Character a gibt die Kante für den
	 * Folgezustand an.
	 * 
	 * Für Zustandsübergänge muss eine Ordnung definiert sein um sie in einer
	 * TreeMap abspeichern zu können.
	 * 
	 * @author florian
	 * 
	 */
	private class GoToKey implements Comparable<GoToKey> {
		/**
		 * Zustand
		 */
		private final Integer state;
		/**
		 * Zeichen um in den Nachfolgezustand zu kommen (aka Kante)
		 */
		private final Character a;

		public GoToKey(final Integer state, final Character a) {
			this.state = state;
			this.a = a;
		}

		/**
		 * Vergleicht zwei Zustandsübergangsschlüssel. Zuerst werden die beiden
		 * Zustände miteinander verglichen, anschließend die Zeichen.
		 */
		public int compareTo(final GoToKey o) {
			int ret = 0;

			if ((ret = state.compareTo(o.state)) != 0) {
				return ret;
			} else {
				return a.compareTo(o.a);
			}

		}
	}

	/**
	 * Fehlerkonstante falls es keinen Folgezustand für einen Zustand s und
	 * einem gelesenen Zeichen a gibt. Dadurch wird signalisiert dass die
	 * Fehlerfunktion befragt werden muss zu welchem Zustand wir springen
	 * müssen.
	 */
	private static final int FAIL = -1;

	/**
	 * Rückgabewert wenn {@link #output} zu einem gegebenen Zustand
	 * <code>state</code> keinen Schlüssel abbilden kann.
	 */
	private static final List<String> EMPTY = null;

	/**
	 * Output Funktion als TreeMap realisiert. Ein Zustand wird auf Strings
	 * abgebildet die aus der Schlüsselmenge aus diesem Zustand aus abgebildet
	 * werden.
	 */
	private TreeMap<Integer, List<String>> output;

	/**
	 * GoTo Funktion als TreeMap realisiert. Ein Zustandübergangs-Schlüssel wird
	 * auf seinen Nachfolgezustand abgebildet.
	 */
	private TreeMap<GoToKey, Integer> goto_function;

	/**
	 * Fehlerfunktion die von einem Zustand in einen Folgezustand abbildet.
	 */
	private TreeMap<Integer, Integer> failure_function;

	/**
	 * Zustandsübergangsfunktion für den deterministischen Zustandsautomaten.
	 * Wird durch {@link #createDeterministicStateTransitionFunction()} erzeugt.
	 */
	private TreeMap<GoToKey, Integer> delta_function;

	/**
	 * Privater Konstruktor für eine PatternMatchingMachine. Zum Erstellen einer
	 * PatternMatchingMachine siehe {@link #create(List)}.
	 * 
	 * @param keywords
	 *            Die Schlüsselwörter mit denen man die Pattern Matching Machine
	 *            initialisieren will. Ein Match auf einen Text sucht diese
	 *            Schlüsselwörter im Text.
	 */
	private PatternMatchingMachine(List<String> keywords) {
		createGoTo(keywords);
		createFailureAndOutput(goto_function, output);
		createDeterministicStateTransitionFunction();
	}

	/**
	 * Statische Erzeugungsfunktion einer Pattern Matching Machine.
	 * 
	 * @param keywords
	 *            Die Schlüsselwörter mit denen man die Pattern Matching Machine
	 *            initialisieren will. Ein Match auf einen Text sucht diese
	 *            Schlüsselwörter im Text.
	 * @return PatternMatchingMachine mit den <code>keywords</code>
	 *         initialisiert.
	 */
	public static PatternMatchingMachine create(List<String> keywords) {
		PatternMatchingMachine patternMatchingMachine = new PatternMatchingMachine(
				keywords);

		return patternMatchingMachine;
	}

	/**
	 * Sucht in dem Text nach den Schlüsselwörtern mit denen die Pattern
	 * Matching Machine <code>patternMatchingMachine</code> initialisiert wurde.
	 * Der zu matchende Text <code>text</code> muss ein initialisierter String
	 * sein. Die PatternMatchingMachine <code>patternMatchingMachine</code> muss
	 * eine durch {@link #create(List)} erstellte PatternMatchingMachine sein.
	 * 
	 * Siehe Algorithmus 1 im Paper von Aho und Corasick
	 * 
	 * @param text
	 *            Text auf dem Schlüsselwörter gesucht werden sollen.
	 * @param patternMatchingMachine
	 *            Pattern Matching Machine die durch Schlüsselwörter erzeugt
	 *            wurde.
	 * @return Die Schlüsselwörter mitsamt ihren Positionen in <code>text</code>
	 *         an denen sie vorkommen.
	 * 
	 */
	public static KeywordLocations match(String text,
			PatternMatchingMachine patternMatchingMachine) {

		KeywordLocations keywordLocations = new KeywordLocations();

		int state = 0;

		for (int i = 0; i < text.length(); i++) {

			char a_i = text.charAt(i);

			// Replace by deterministic delta function delta.
			// while (patternMatchingMachine.g(state, a_i) ==
			// PatternMatchingMachine.FAIL) {
			// state = patternMatchingMachine.f(state);
			// }
			//
			// state = patternMatchingMachine.g(state, a_i);

			state = patternMatchingMachine.delta(state, a_i);

			if (patternMatchingMachine.output(state) != PatternMatchingMachine.EMPTY) {
				keywordLocations.addLocation(patternMatchingMachine
						.output(state), i);
			}
		}

		return keywordLocations;

	}

	/**
	 * Erstellt die GoTo Funktion aus einer Menge von Schlüsselwörtern basierend
	 * auf Algorithmus 2.
	 * 
	 * @param keywords
	 *            Schlüsselwörter aus denen der Zustandsübergangsgraph aufgebaut
	 *            sein soll.
	 * 
	 */
	private void createGoTo(List<String> keywords) {

		// Create new Output Function
		output = new TreeMap<Integer, List<String>>();
		goto_function = new TreeMap<GoToKey, Integer>();

		// pre-initialization
		Integer newstate = 0;
		for (String y_i : keywords) {
			newstate = enter(y_i, newstate);
		}

		// alle nichtvorhandenen Übergänge am Start
		// g(0,a) auf 0 (Startzustand )setzen.
		// -> Implizit in g enthalten
		// Lösung durch old_g und g Funktion als private Methoden.
	}

	/**
	 * Enter Prozedur aus dem Algorithmus 2. Er fügt einen String <code>a</code>
	 * in den Zustandsautomaten ein beginnend bei dem Zustand newstate+1.
	 * 
	 * @param a
	 *            das einzufügende Wort
	 * @param newstate
	 *            der zuletzte eingefügte Zustand
	 * @return Der Zustand nachdem a komplett eingefügt wurde.
	 */
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

	/**
	 * Erzeugt die Fehler und Outputfunktion basierend auf Algorithmus 3.
	 * 
	 * @param goto_function
	 *            die bislang initialisierte goto_function
	 * @param output_function
	 *            die bislang initialisierte output_function.
	 */
	private void createFailureAndOutput(
			TreeMap<GoToKey, Integer> goto_function,
			TreeMap<Integer, List<String>> output_function) {

		failure_function = new TreeMap<Integer, Integer>();

		Queue<Integer> queue = new LinkedList<Integer>();

		List<GoToKey> zero_state_transistion = get_zero_transition(goto_function);

		for (GoToKey key : zero_state_transistion) {
			Integer s = -1;
			// ?Check auf fail auch? -> nein, wnen man das Paper liest
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

	/**
	 * Erzeugt aus der GoTo Funktion {@link #g(Integer, Character)} und der
	 * Fehlerfunktion {@link #f(Integer)} eine deterministische
	 * Übergangsfunktion delta für den Zustandsautomaten nach dem Algorithmus 4.
	 * 
	 * {@link #createDeterministicStateTransitionFunction()} darf erst dann
	 * aufgerufen werden wenn {@link #g(Integer, Character)} und
	 * {@link #f(Integer)} vollständig durch {@link #createGoTo(List)} und
	 * {@link #createFailureAndOutput(TreeMap, TreeMap)} erzeugt worden sind.
	 * 
	 * 
	 */
	private void createDeterministicStateTransitionFunction() {

		delta_function = new TreeMap<GoToKey, Integer>();

		Queue<Integer> queue = new LinkedList<Integer>();

		List<GoToKey> zero_transitions = get_zero_transition(goto_function);

		for (GoToKey key : zero_transitions) {

			Integer nextState = g(key.state, key.a);

			GoToKey zero_key = new GoToKey(key.state, key.a);

			delta_function.put(zero_key, nextState);

			if (nextState != 0) {
				queue.add(nextState);
			}
		}

		while (!queue.isEmpty()) {
			Integer r = queue.poll();

			List<GoToKey> nextKeys = get_keys(goto_function, r);

			for (GoToKey key : nextKeys) {
				Integer s = g(r, key.a);

				if (!s.equals(FAIL)) {
					queue.add(s);

					GoToKey add_key = new GoToKey(r, key.a);

					delta_function.put(add_key, s);
				} else {
					GoToKey add_key = new GoToKey(r, key.a);

					delta_function.put(add_key, delta(f(r), key.a));
				}
			}
		}
	}

	/**
	 * Berechnet den Nachfolgezustand über die deterministische delta Funktion
	 * aus die vorher mit {@link #createDeterministicStateTransitionFunction()}
	 * erzeugt worden sein muss.
	 * 
	 * @param f
	 *            Zustand
	 * @param a
	 *            lesendes Zeichen
	 * @return Nachfolgezustand
	 */
	private Integer delta(Integer f, Character a) {

		GoToKey key = new GoToKey(f, a);

		Integer s = -1;

		if ((s = delta_function.get(key)) == null) {
			return 0;
		} else {
			return s;
		}

	}

	/**
	 * Liefert alle Zustandsübergänge für den Zustand r zurück die in der
	 * <code>gotoFunction</code> enthalten sind.
	 * 
	 * @param gotoFunction
	 *            GoTo-Übergangsfunktion
	 * @param r
	 *            Zustand
	 * @return Liste von Zustandsübergängen vom Zustand r.
	 */
	private List<GoToKey> get_keys(TreeMap<GoToKey, Integer> gotoFunction,
			Integer r) {
		Set<GoToKey> goto_set = goto_function.keySet();

		List<GoToKey> from_r = new ArrayList<GoToKey>();

		for (GoToKey key : goto_set) {
			if (r.equals(key.state)) {
				from_r.add(key);
			}
		}

		return from_r;
	}

	/**
	 * Liefert eine Liste von Zustandsübergängen die vom Zustand <code>0</code>
	 * heraus auf Folgezustände abbilden.
	 * 
	 * @param gotoFunction
	 *            bisher berechneten Zustandsübergänge.
	 * @return Liste von Zustandsübergängen des Zustands <code>0</code>
	 */
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
	 *            bisher berechneten Zustandsübergänge
	 * @return Liste von Zustandsübergängen die nicht in einen Fehlerzustand
	 *         überführen.
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

	/**
	 * Output Funktion für einen Zustand <code>state</code>. Liefert eine Liste
	 * von allen Schlüsselwörtern die in diesem Zustand aus abgebildet werden
	 * können.
	 * 
	 * @param state
	 *            Zustand für den der Output berechnet werden soll.
	 * @return Liste von Schlüsselwörtern die von diesem Zustand aus abgebildet
	 *         werden. Liefert {@link #EMPTY} zurück wenn kein output für den
	 *         Zustand gefunden werden kann.
	 */
	private List<String> output(Integer state) {
		List<String> values = output.get(state);

		if (values == null || values.isEmpty()) {
			return PatternMatchingMachine.EMPTY;
		} else {
			return values;
		}
	}

	/**
	 * Fehlerfunktion <code>f</code>. Berechnet für einen Zustand
	 * <code>state</code> zu welchem Zustand gesprungen werden muss wenn es zu
	 * einem Fehler kommt.
	 * 
	 * @param state
	 *            Zustand für den die Fehlerfunktion berechnet werden soll.
	 * @return Folgezustand im Fehlerfall.
	 */
	private Integer f(Integer state) {
		Integer f = failure_function.get(state);

		if (f == null) {
			return 0;
		} else {
			return f;
		}
	}

	/**
	 * GoTo Funktion <code>g</code> die für einen Zustand <code>state</code> und
	 * ein gelesenes Zeichen <code>aI</code> den Folgezustand berechnet oder
	 * {@value #FAIL} wenn es keinen Folgezustand gibt.
	 * 
	 * @param state
	 *            Zustand für den die goto Funktion berechnet werden soll.
	 * @param aI
	 *            Gelesenes Zeichen.
	 * @return Folgezustand aus dem Zustand <code>state</code> mit gelesenem
	 *         Zeichen <code>aI</code> oder {@value #FAIL} wenn es keinen
	 *         Folgezustand für das Zeichen <code>aI</code> gibt.
	 */
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

	/**
	 * Hilfsfunktion für <code>g</code> wenn noch kein Fehlerwert {@link #FAIL}
	 * zurückgegeben werden soll bevor
	 * {@link #createFailureAndOutput(TreeMap, TreeMap)} ausgeführt werden soll.
	 * 
	 * @param state
	 *            Zustand für den die goto Funktion berechnet werden soll.
	 * @param a
	 *            Gelesenes Zeichen.
	 * @return Folgezustand aus dem Zustand <code>state</code> mit gelesenem
	 *         Zeichen <code>aI</code> oder <code<0</code> wenn es keinen
	 *         Folgezustand für das Zeichen <code>aI</code> gibt.
	 */
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
				"ushers", pmm);

		keywordLocation.print();

	}
}
