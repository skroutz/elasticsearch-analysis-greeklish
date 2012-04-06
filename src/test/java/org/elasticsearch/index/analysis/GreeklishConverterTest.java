package org.elasticsearch.index.analysis;

import java.util.List;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.Assert;

public class GreeklishConverterTest {
	private static final String[] greekWords = {
			"αυτοκινητο", "ομπρελα", "ξεσκεπαστοσ",
	};
	
	private static final String[] invalidWords = {
			"mobile", "αυριο64", "καλάθι", "ΣΠιτι", "ομορφος"
	};
	
	private GreeklishConverter converter;

	private List<StringBuilder> greeklishWords;
	
	@BeforeMethod
	public void setUp() {
		this.converter = new GreeklishConverter();
	}

	@Test
	public void testGreekTokenConversionForValidWords() {
		for(String greekWord : greekWords) {
			greeklishWords = converter.convert(greekWord.toCharArray(), greekWord.length());

			Assert.assertFalse(greeklishWords.isEmpty(), "Greeklish words should be generated");
		}
	}

	@Test
	public void testGreekTokenConversionsForInvalidWords() {
		for(String invalidWord : invalidWords) {
			greeklishWords = converter.convert(invalidWord.toCharArray(), invalidWord.length());

			Assert.assertNull(greeklishWords, "It should not create any greeklish words");
		}
	}
}