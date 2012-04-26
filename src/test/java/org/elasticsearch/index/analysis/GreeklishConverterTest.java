package org.elasticsearch.index.analysis;

import java.util.List;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.Assert;

public class GreeklishConverterTest {
	private static final int MAX_EXPANSIONS = 20;
	
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
	private static final String[][] generatedGreeklishWords = {
			{ "autokinhto", "aftokinhto", "avtokinhto", "aytokinhto",
					"autokinito", "aftokinito", "avtokinito", "aytokinito" },
			{"omprela", "obrela"}, {"kseskepastos", "xeskepastos"}
	};

	/**
	 * these words should not be processed by the converter.
	 */
	private static final String[] invalidWords = { "mobile", "αυριο64",
			"καλάθι", "ΣΠιτι", "ομορφος" };

	private GreeklishConverter converter;

	private List<StringBuilder> greeklishWords;

	@BeforeMethod
	public void setUp() {
		this.converter = new GreeklishConverter(MAX_EXPANSIONS, true);
	}

	@Test
	public void testGreekTokenConversionForValidWords() {
		for (int i = 0; i < greekWords.length; i++) {
			greeklishWords = converter.convert(greekWords[i].toCharArray(),
					greekWords[i].length());

			Assert.assertFalse(greeklishWords.isEmpty(),
					"Greeklish words should be generated");
			Object[] greeklishWordsArray = greeklishWords.toArray();
			for (int j = 0; j < greeklishWordsArray.length; j ++) {
				Assert.assertEquals(greeklishWordsArray[j].toString(), generatedGreeklishWords[i][j]);				
			}
		}
	}

	@Test
	public void testGreekTokenConversionsForInvalidWords() {
		for (String invalidWord : invalidWords) {
			greeklishWords = converter.convert(invalidWord.toCharArray(),
					invalidWord.length());

			Assert.assertNull(greeklishWords,
					"It should not create any greeklish words");
		}
	}

	@Test
	public void testMaxGreeklishWordGenerations() {
		int newMaxExpansions = 2;
		converter = new GreeklishConverter(newMaxExpansions, true);

		greeklishWords = converter.convert(greekWords[0].toCharArray(),
				greekWords[0].length());
		Assert.assertEquals(greeklishWords.size(), newMaxExpansions,
				"The generated words should be limited by the maxExpansions paramater");

		Object[] greeklishWordsArray = greeklishWords.toArray();
		for (int i = 0; i < greeklishWordsArray.length; i ++) {
			Assert.assertEquals(greeklishWordsArray[i].toString(), generatedGreeklishWords[0][i]);				
		}
	}
}