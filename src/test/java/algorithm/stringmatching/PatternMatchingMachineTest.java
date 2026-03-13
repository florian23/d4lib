package algorithm.stringmatching;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class PatternMatchingMachineTest {

	@Test
	public void testStandardExample() {
		PatternMatchingMachine pmm = PatternMatchingMachine.create("he", "she", "his", "hers");
		KeywordLocations result = pmm.match("ushers");

		Map<String, List<Integer>> locations = result.getLocations();
		assertEquals(Arrays.asList(1), locations.get("she"));
		assertEquals(Arrays.asList(2), locations.get("he"));
		assertEquals(Arrays.asList(2), locations.get("hers"));
		assertNull(locations.get("his"));
	}

	@Test
	public void testReversedKeywordOrder() {
		PatternMatchingMachine pmm = PatternMatchingMachine.create("hers", "her");
		KeywordLocations result = pmm.match("ushers");

		Map<String, List<Integer>> locations = result.getLocations();
		assertEquals(Arrays.asList(2), locations.get("her"));
		assertEquals(Arrays.asList(2), locations.get("hers"));
	}

	@Test
	public void testDuplicatesAndEmptyStrings() {
		PatternMatchingMachine pmm = PatternMatchingMachine.create("he", "", "he", "she");
		KeywordLocations result = pmm.match("ushers");

		Map<String, List<Integer>> locations = result.getLocations();
		assertEquals(2, locations.size());
		assertEquals(Arrays.asList(2), locations.get("he"));
		assertEquals(Arrays.asList(1), locations.get("she"));
	}

	@Test
	public void testNoMatch() {
		PatternMatchingMachine pmm = PatternMatchingMachine.create("xyz", "abc");
		KeywordLocations result = pmm.match("ushers");

		assertTrue(result.getLocations().isEmpty());
	}

	@Test
	public void testSingleCharacterKeywords() {
		PatternMatchingMachine pmm = PatternMatchingMachine.create("u", "s", "e");
		KeywordLocations result = pmm.match("ushers");

		Map<String, List<Integer>> locations = result.getLocations();
		assertEquals(Arrays.asList(0), locations.get("u"));
		assertEquals(Arrays.asList(1, 5), locations.get("s"));
		assertEquals(Arrays.asList(3), locations.get("e"));
	}

	@Test
	public void testOverlappingPatterns() {
		PatternMatchingMachine pmm = PatternMatchingMachine.create("ab", "abc", "bc", "c");
		KeywordLocations result = pmm.match("abcabc");

		Map<String, List<Integer>> locations = result.getLocations();
		assertEquals(Arrays.asList(0, 3), locations.get("ab"));
		assertEquals(Arrays.asList(0, 3), locations.get("abc"));
		assertEquals(Arrays.asList(1, 4), locations.get("bc"));
		assertEquals(Arrays.asList(2, 5), locations.get("c"));
	}

	@Test
	public void testEmptyText() {
		PatternMatchingMachine pmm = PatternMatchingMachine.create("he", "she");
		KeywordLocations result = pmm.match("");

		assertTrue(result.getLocations().isEmpty());
	}

	@Test
	public void testKeywordIsEntireText() {
		PatternMatchingMachine pmm = PatternMatchingMachine.create("hello");
		KeywordLocations result = pmm.match("hello");

		assertEquals(Arrays.asList(0), result.getLocations().get("hello"));
	}

	@Test
	public void testMultipleOccurrences() {
		PatternMatchingMachine pmm = PatternMatchingMachine.create("ana");
		KeywordLocations result = pmm.match("bananaana");

		assertEquals(Arrays.asList(1, 3, 6), result.getLocations().get("ana"));
	}

	@Test
	public void testListBasedCreate() {
		List<String> keywords = Arrays.asList("he", "she", "his", "hers");
		PatternMatchingMachine pmm = PatternMatchingMachine.create(keywords);
		KeywordLocations result = pmm.match("ushers");

		assertEquals(3, result.getLocations().size());
	}

	@Test
	@SuppressWarnings("deprecation")
	public void testDeprecatedStaticMatch() {
		PatternMatchingMachine pmm = PatternMatchingMachine.create("he", "she");
		KeywordLocations result = PatternMatchingMachine.match("ushers", pmm);

		assertFalse(result.getLocations().isEmpty());
	}
}
