package d4lib.ahocorasick;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class PatternMatchingMachineTest {

	@Test
	public void testStandardExample() {
		var pmm = PatternMatchingMachine.create("he", "she", "his", "hers");
		var result = pmm.match("ushers");

		var locations = result.getLocations();
		assertEquals(List.of(1), locations.get("she"));
		assertEquals(List.of(2), locations.get("he"));
		assertEquals(List.of(2), locations.get("hers"));
		assertNull(locations.get("his"));
	}

	@Test
	public void testReversedKeywordOrder() {
		var pmm = PatternMatchingMachine.create("hers", "her");
		var result = pmm.match("ushers");

		var locations = result.getLocations();
		assertEquals(List.of(2), locations.get("her"));
		assertEquals(List.of(2), locations.get("hers"));
	}

	@Test
	public void testDuplicatesAndEmptyStrings() {
		var pmm = PatternMatchingMachine.create("he", "", "he", "she");
		var result = pmm.match("ushers");

		var locations = result.getLocations();
		assertEquals(2, locations.size());
		assertEquals(List.of(2), locations.get("he"));
		assertEquals(List.of(1), locations.get("she"));
	}

	@Test
	public void testNoMatch() {
		var pmm = PatternMatchingMachine.create("xyz", "abc");
		var result = pmm.match("ushers");

		assertTrue(result.getLocations().isEmpty());
	}

	@Test
	public void testSingleCharacterKeywords() {
		var pmm = PatternMatchingMachine.create("u", "s", "e");
		var result = pmm.match("ushers");

		var locations = result.getLocations();
		assertEquals(List.of(0), locations.get("u"));
		assertEquals(List.of(1, 5), locations.get("s"));
		assertEquals(List.of(3), locations.get("e"));
	}

	@Test
	public void testOverlappingPatterns() {
		var pmm = PatternMatchingMachine.create("ab", "abc", "bc", "c");
		var result = pmm.match("abcabc");

		var locations = result.getLocations();
		assertEquals(List.of(0, 3), locations.get("ab"));
		assertEquals(List.of(0, 3), locations.get("abc"));
		assertEquals(List.of(1, 4), locations.get("bc"));
		assertEquals(List.of(2, 5), locations.get("c"));
	}

	@Test
	public void testEmptyText() {
		var pmm = PatternMatchingMachine.create("he", "she");
		var result = pmm.match("");

		assertTrue(result.getLocations().isEmpty());
	}

	@Test
	public void testKeywordIsEntireText() {
		var pmm = PatternMatchingMachine.create("hello");
		var result = pmm.match("hello");

		assertEquals(List.of(0), result.getLocations().get("hello"));
	}

	@Test
	public void testMultipleOccurrences() {
		var pmm = PatternMatchingMachine.create("ana");
		var result = pmm.match("bananaana");

		assertEquals(List.of(1, 3, 6), result.getLocations().get("ana"));
	}

	@Test
	public void testListBasedCreate() {
		var keywords = List.of("he", "she", "his", "hers");
		var pmm = PatternMatchingMachine.create(keywords);
		var result = pmm.match("ushers");

		assertEquals(3, result.getLocations().size());
	}
}
