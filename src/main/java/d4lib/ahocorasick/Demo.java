package d4lib.ahocorasick;

/**
 * Demonstriert die Verwendung der PatternMatchingMachine.
 */
public class Demo {

	public static void main(String[] args) {
		System.out.println("=== Test 1: Standardbeispiel ===");
		var pmm = PatternMatchingMachine.create("he", "she", "his", "hers");
		pmm.match("ushers").print();

		System.out.println("\n=== Test 2: hers vor her (ehemaliger Crash) ===");
		var pmm2 = PatternMatchingMachine.create("hers", "her");
		pmm2.match("ushers").print();

		System.out.println("\n=== Test 3: Duplikate und leere Strings ===");
		var pmm3 = PatternMatchingMachine.create("he", "", "he", "she");
		pmm3.match("ushers").print();
	}
}
