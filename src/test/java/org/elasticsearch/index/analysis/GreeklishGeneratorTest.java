package org.elasticsearch.index.analysis;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.Assert;

public class GreeklishGeneratorTest {

	private static final int MAX_EXPANSIONS = 10;
	private static final boolean USE_SPECIAL_MAPPING_ON = true;
	private static final boolean USE_SPECIAL_MAPPING_OFF = false;

	/**
	 * a sample of greek words to generate their greeklish
	 * counterparts.
	 */
	private static final String[] greekWords = { "αυτοκινητο", "ομπρελα",
			"ξεσκεπαστοσ", };

	/**
	 * a special sample of greek words to generate their greeklish
	 * counterparts.
	 */
	private static final String[] greekWordsSpecial = { "ωιψηυ" };

	/**
	 * the greeklish counterparts that should be generated from the
	 * greek words.
	 */
	private static final String[] generatedGreeklishWords = {
			"autokinhto", "aftokinhto", "avtokinhto", "aytokinhto",
			"autokinito", "aftokinito", "avtokinito", "aytokinito",
			"omprela", "obrela", "kseskepastos", "xeskepastos"
	};

	/**
	 * the special greeklish counterparts that should be generated from the
	 * greek words.
	 */
	private static final String[] generatedGreeklishWordsSpecial = {
			"oichu", "wichi", "wichu", "vipsiy",
			"oipsiy", "wipsiy", "viciy", "oiciy",
			"wiciy", "vipshy", "oipshy", "wipshy",
			"vichy", "oichy", "wichy"
	};

	private GreeklishGenerator generator;

	private List<String> inputGreekList = new ArrayList<String>();

	private List<StringBuilder> greeklishWords;

	private List<String> convertedGreeklishStrings = new ArrayList<String>();

	@BeforeClass
	public void populateInputGreekList() {
		this.generator = new GreeklishGenerator(MAX_EXPANSIONS, USE_SPECIAL_MAPPING_OFF);

		for (String word : greekWords) {
			inputGreekList.add(word);
		}
	}

	@BeforeMethod
	public void setUp() {
		convertedGreeklishStrings.clear();
	}

	@Test
	public void testGreekTokenConversionForValidWords() {
		for (int i = 0; i < greekWords.length; i++) {
			greeklishWords = generator.generateGreeklishWords(inputGreekList);

			populateConvertedStringsList();

			Assert.assertFalse(greeklishWords.isEmpty(),
					"Greeklish words should be generated");
			for (String greeklishWord : generatedGreeklishWords) {
				Assert.assertTrue(
						convertedGreeklishStrings.contains(greeklishWord),
						"It should contain the greeklish word: "
								+ greeklishWord);
			}
		}
	}

	@Test
	public void testMaxGreeklishWordGenerations() {
		int newMaxExpansions = 2;
		generator = new GreeklishGenerator(newMaxExpansions, USE_SPECIAL_MAPPING_OFF);

		greeklishWords = generator.generateGreeklishWords(inputGreekList);

		Assert.assertEquals(greeklishWords.size(), newMaxExpansions
				* inputGreekList.size(),
				"The generated words should be limited by the maxExpansions paramater");

	}

	@Test
	public void testGreekTokenConversionForValidWordsSpecial() {
		inputGreekList.clear();
		for (String word : greekWordsSpecial) {
			inputGreekList.add(word);
		}

		for (int i = 0; i < greekWordsSpecial.length; i++) {
			int newMaxExpansions = 20;
			generator = new GreeklishGenerator(newMaxExpansions, USE_SPECIAL_MAPPING_ON);
			greeklishWords = generator.generateGreeklishWords(inputGreekList);

			populateConvertedStringsList();

			Assert.assertFalse(greeklishWords.isEmpty(),
					"Greeklish words should be generated");
			for (String greeklishWord : generatedGreeklishWordsSpecial) {
				Assert.assertTrue(
						convertedGreeklishStrings.contains(greeklishWord),
						"It should contain the greeklish word: "
								+ greeklishWord);
			}
		}
	}

	private final void populateConvertedStringsList() {
		for (StringBuilder word : greeklishWords) {
			convertedGreeklishStrings.add(word.toString());
		}
	}
}
