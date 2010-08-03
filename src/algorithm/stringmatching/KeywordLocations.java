package algorithm.stringmatching;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * Die Klasse KeywordLocations verwaltet die Zuordnung eines Strings auf die
 * Indexposition in dem der String im Text anfängt.
 * 
 * @author florian
 * 
 */
public class KeywordLocations {

	private TreeMap<String, List<Integer>> keywords_at_position;

	public KeywordLocations() {
		keywords_at_position = new TreeMap<String, List<Integer>>();
	}

	/**
	 * addLocation speichert zu den Strings in output ab an welcher
	 * Indexposition sie anfangen. Dabei ist i die letzte Indexposition an dem
	 * die Strings im Text aufhören.
	 * 
	 * Für einen String s und einem Index i gilt, dass die Stringposition im Text gleich
	 * i-lengthOf(s)+1 ist.
	 * 
	 * @param output
	 * @param i
	 */
	public void addLocation(List<String> output, int i) {

		for (String s : output) {
			
			Integer ii = i-s.length()+1;
			
			if (keywords_at_position.containsKey(s)
					&& keywords_at_position.get(s).contains(ii)) {
				// keyvalue already there
				return;
			} else if (keywords_at_position.containsKey(s)) {
				keywords_at_position.get(s).add(ii);
			} else {

				List<Integer> positions = new ArrayList<Integer>();

				positions.add(ii);

				keywords_at_position.put(s, positions);
			}
		}

	}

	/**
	 * Print gibt aus an welchen Indexpositionen im Text ein Schlüsselwort
	 * anfängt.
	 */
	public void print() {
		for (String key : keywords_at_position.keySet()) {
			List<Integer> locations = keywords_at_position.get(key);

			System.out.println("Key " + key + " an Positionen "
					+ locations.toString());
		}
	}
}