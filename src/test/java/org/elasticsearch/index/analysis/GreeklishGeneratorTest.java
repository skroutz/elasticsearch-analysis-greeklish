package org.elasticsearch.index.analysis;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.Assert;

public class GreeklishGeneratorTest {

	private static final int MAX_EXPANSIONS = 10;

	/**
	 * a sample of greek words to generate their greeklish
	 * counterparts.
	 */
	private static final String[] greekWords = { "αυτοκινητο", "ομπρελα",
			"ξεσκεπαστοσ", };

	/**
	 * the greeklish counterparts that should be generated from the
	 * greek words.
	 */
	private static final String[] generatedGreeklishWords = {
			"autokinhto", "aftokinhto", "avtokinhto", "aytokinhto",
			"autokinito", "aftokinito", "avtokinito", "aytokinito",
			"omprela", "obrela", "kseskepastos", "xeskepastos"
	};

	private GreeklishGenerator generator;

	private List<String> inputGreekList = new ArrayList<String>();

	private List<StringBuilder> greeklishWords;

	private List<String> convertedGreeklishStrings = new ArrayList<String>();

	@BeforeClass
	public void populateInputGreekList() {
		this.generator = new GreeklishGenerator(MAX_EXPANSIONS);

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
		generator = new GreeklishGenerator(newMaxExpansions);

		greeklishWords = generator.generateGreeklishWords(inputGreekList);

		Assert.assertEquals(greeklishWords.size(), newMaxExpansions
				* inputGreekList.size(),
				"The generated words should be limited by the maxExpansions paramater");

	}

	private final void populateConvertedStringsList() {
		for (StringBuilder word : greeklishWords) {
			convertedGreeklishStrings.add(word.toString());
		}
	}
}
