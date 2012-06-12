package org.elasticsearch.index.analysis;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.Assert;

public class GreeklishConverterTest {

	private static final int MAX_EXPANSIONS = 10;

	private static final boolean GENERATE_GREEK_VARIANTS = true;

	private GreeklishConverter converter;
	
	/**
	 * a sample of greek words to generate their greeklish
	 * counterparts.
	 */
	private static final String[] greekWords = { "αυτοκινητο", "ομπρελα",
			"ξεσκεπαστοσ"};

	/**
	 * the greeklish counterparts that should be generated from the
	 * greek words.
	 */
	private static final String[][] generatedGreeklishWords = {
			{ "autokinhto", "aftokinhto", "avtokinhto", "aytokinhto",
					"autokinito", "aftokinito", "avtokinito", "aytokinito",
					"autokinhtwn", "aftokinhta", "avtokinhta", "aytokinhtwn"},
			{"omprela", "obrela", "ompreles", "obrelwn", "obreles", "omprelas"},
			{"kseskepastos", "xeskepastos", "kseskepastou", "xeskepastwn", "kseskepastoi"}
	};

	/**
	 * these words should not be processed by the converter.
	 */
	private static final String[] invalidWords = { "mobile", "αυριο64",
			"καλάθι", "ΣΠιτι", "ομορφος" };

	private List<StringBuilder> greeklishWords;

	private List<String> convertedGreeklishStrings = new ArrayList<String>();

	@BeforeClass
	public void setUp() {
		this.converter = new GreeklishConverter(MAX_EXPANSIONS, GENERATE_GREEK_VARIANTS);
	}

	@BeforeMethod
	public void emptyConvertedGreeklishStrings() {
		convertedGreeklishStrings.clear();
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
	public void testGreekTokenConversionForValidWords() {
		for (int i = 0; i < greekWords.length; i++) {
			greeklishWords = converter.convert(greekWords[i].toCharArray(),
					greekWords[i].length());

			populateConvertedStringsList();

			Assert.assertFalse(greeklishWords.isEmpty(),
					"Greeklish words should be generated");

			for (String greeklishWord : generatedGreeklishWords[i]) {
				Assert.assertTrue(convertedGreeklishStrings
						.contains(greeklishWord),
						"It should contain greeklish word: " + greeklishWord);
			}
		}
	}

	@Test
	public void testMaxGreeklishExpansions() {
		int newMaxExpansions = 2;
		boolean generateGreekVariants = false;
		converter = new GreeklishConverter(newMaxExpansions, generateGreekVariants);

		greeklishWords = converter.convert(greekWords[0].toCharArray(),
				greekWords[0].length());

		populateConvertedStringsList();

		Assert.assertEquals(greeklishWords.size(), newMaxExpansions,
				"The generated words should be limited by the maxExpansions paramater");

		for (int i = 0; i < newMaxExpansions; i ++) {
			Assert.assertTrue(convertedGreeklishStrings.contains(generatedGreeklishWords[0][i]),
					"It should contain greeklish word: " + generatedGreeklishWords[0][i]);
		}

		for (int j = newMaxExpansions; j < generatedGreeklishWords[0].length; j++) {
			Assert.assertFalse(convertedGreeklishStrings.contains(generatedGreeklishWords[0][j]),
					"It should not contain greeklish word: " + generatedGreeklishWords[0][j]);
		}
	}

	@Test
	public void testGreekVariantsGeneration() {
		int newMaxExpansions = 1;
		boolean generateGreekVariants = false;
		converter = new GreeklishConverter(newMaxExpansions, generateGreekVariants);

		greeklishWords = converter.convert(greekWords[0].toCharArray(),
				greekWords[0].length());

		populateConvertedStringsList();

		Assert.assertTrue(convertedGreeklishStrings.contains(generatedGreeklishWords[0][0]),
				"It should contain greeklish word: " + generatedGreeklishWords[0][0]);

		Assert.assertFalse(convertedGreeklishStrings.contains(generatedGreeklishWords[0][9]),
				"It should not contain greeklish word: " + generatedGreeklishWords[0][9]);
	}

	private final void populateConvertedStringsList() {
		for (StringBuilder word : greeklishWords) {
			convertedGreeklishStrings.add(word.toString());
		}
	}
}
