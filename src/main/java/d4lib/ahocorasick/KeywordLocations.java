package d4lib.ahocorasick;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Verwaltet die Zuordnung von gefundenen Schluesselwoertern zu ihren
 * Startpositionen im durchsuchten Text.
 *
 * @author florian
 */
public class KeywordLocations {

	private final TreeMap<String, LinkedHashSet<Integer>> keywordsAtPosition = new TreeMap<>();

	/**
	 * Speichert fuer jeden String in output die Startposition im Text.
	 * Position i ist die letzte Indexposition; die Startposition ergibt sich
	 * aus i - length(s) + 1.
	 *
	 * @param output gefundene Schluesselwoerter
	 * @param i letzte Indexposition des Matches
	 */
	public void addLocation(List<String> output, int i) {
		for (var s : output) {
			var startPos = i - s.length() + 1;
			keywordsAtPosition.computeIfAbsent(s, k -> new LinkedHashSet<>()).add(startPos);
		}
	}

	/**
	 * Gibt die Positionen aller gefundenen Schluesselwoerter zurueck.
	 *
	 * @return unveraenderbare Map von Keyword zu Liste der Startpositionen
	 */
	public Map<String, List<Integer>> getLocations() {
		var result = new TreeMap<String, List<Integer>>();
		for (var entry : keywordsAtPosition.entrySet()) {
			result.put(entry.getKey(), List.copyOf(entry.getValue()));
		}
		return Collections.unmodifiableMap(result);
	}

	/**
	 * Gibt aus, an welchen Indexpositionen im Text ein Schluesselwort anfaengt.
	 */
	public void print() {
		for (var entry : keywordsAtPosition.entrySet()) {
			System.out.println("Key " + entry.getKey() + " an Positionen " + List.copyOf(entry.getValue()));
		}
	}
}
