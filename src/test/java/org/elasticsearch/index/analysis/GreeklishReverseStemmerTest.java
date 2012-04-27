package org.elasticsearch.index.analysis;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.Assert;

public class GreeklishReverseStemmerTest {

	/**
	 * Some greek words whose variations we want to produce.
	 */
	private static final String[] greekWords = {
		"κουρεματοσ", "ενδυματα", "γραφειου", "πεδιου",
		"γραναζι", "ποδηλατα", "καλωδιων"
	};

	/**
	 * Words that should not match to any rule.
	 */
	private static final String[] nonMatchingWords = {
		"σουτιεν", "κολλαν", "αμπαλαζ", "μακιγιαζ"
	};

	/**
	 * The output we expect for each of the above words.
	 */
	private static final String[][] greekVariants = {
		{"κουρεμα", "κουρεματων", "κουρεματα"},
		{"ενδυμα", "ενδυματων", "ενδυματα", "ενδυματοσ"},
		{"γραφειο", "γραφεια", "γραφειων"},
		{"πεδια", "πεδιο", "πεδιων"},
		{"γραναζια", "γραναζιου", "γραναζιων"},
		{"ποδηλατο", "ποδηλατου", "ποδηλατα", "ποδηλατων"},
		{"καλωδιου", "καλωδια", "καλωδιο"}
	};

	private GreekReverseStemmer reverseStemmer;

	private List<String> generatedGreekVariants;

	@BeforeClass
	public void setUp() {
		this.reverseStemmer = new GreekReverseStemmer();
		this.generatedGreekVariants = new ArrayList<String>();
	}

	@BeforeMethod
	public void clearThePreviousResults() {
		generatedGreekVariants.clear();
	}

	@Test
	public void testGenerationOfGreekVariants() {
		for (int i = 0; i < greekWords.length; i++) {
			generatedGreekVariants = reverseStemmer.generateGreekVariants(greekWords[i]);

			Assert.assertTrue(generatedGreekVariants.size() > 1, "The reverse stemmer should produce results");

			for (String greekVariant : greekVariants[i]) {
				Assert.assertTrue(generatedGreekVariants.contains(greekVariant),
						"It should contain the greek variant: " + greekVariant);
			}
		}
	}

	@Test
	public void testNonMatchingWords() {
		for (String nonMatchingWord : nonMatchingWords) {
			generatedGreekVariants = reverseStemmer.generateGreekVariants(nonMatchingWord);

			Assert.assertTrue(generatedGreekVariants.size() == 1, "The reverse stemmer should not produce more results");
		}
	}
}
