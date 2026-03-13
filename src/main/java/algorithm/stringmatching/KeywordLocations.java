package algorithm.stringmatching;

import java.util.ArrayList;
import java.util.Collections;
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

	private final TreeMap<String, List<Integer>> keywordsAtPosition = new TreeMap<>();

	/**
	 * Speichert fuer jeden String in output die Startposition im Text.
	 * Position i ist die letzte Indexposition; die Startposition ergibt sich
	 * aus i - length(s) + 1.
	 *
	 * @param output gefundene Schluesselwoerter
	 * @param i letzte Indexposition des Matches
	 */
	public void addLocation(List<String> output, int i) {
		for (String s : output) {
			int startPos = i - s.length() + 1;
			List<Integer> positions = keywordsAtPosition.computeIfAbsent(s, k -> new ArrayList<>());
			if (!positions.contains(startPos)) {
				positions.add(startPos);
			}
		}
	}

	/**
	 * Gibt die Positionen aller gefundenen Schluesselwoerter zurueck.
	 *
	 * @return unveraenderbare Map von Keyword zu Liste der Startpositionen
	 */
	public Map<String, List<Integer>> getLocations() {
		return Collections.unmodifiableMap(keywordsAtPosition);
	}

	/**
	 * Gibt aus, an welchen Indexpositionen im Text ein Schluesselwort anfaengt.
	 */
	public void print() {
		for (Map.Entry<String, List<Integer>> entry : keywordsAtPosition.entrySet()) {
			System.out.println("Key " + entry.getKey() + " an Positionen " + entry.getValue());
		}
	}
}
