package org.elasticsearch.index.analysis;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;

/**
 * @author Tasos Stathopoulos </p> Generates tokens with latin characters from
 *         Greek tokens. It matches one or more latin characters for each Greek
 *         character of the token. A Greek character may have one or more latin
 *         counterparts. So, from a Greek token one or more latin tokens are
 *         generated. </p> Greek words have combination of vowels called
 *         digraphs. Because digraphs are special cases, they are treated in
 *         isolation.
 */
public class GreeklishConverter {
	/**
	 * Elastic Search logger
	 */
	protected final ESLogger logger;

	/**
	 * Tokens that contain only these characters will be affected by this
	 * filter.
	 */
	public static final String GREEK_CHARACTERS = "αβγδεζηθικλμνξοπρστυφχψω";

	/**
	 * Keep the generated greek words from the greek reverse stemmer.
	 */
	private List<String> greekWords;

	/**
	 * Input token converted into String.
	 */
	private String tokenString;

	private final GreekReverseStemmer reverseStemmer;

	private final GreeklishGenerator greeklishGenerator;

	private final boolean generateGreekVariants;

	// Constructor
	public GreeklishConverter(int maxExpansions, boolean generateGreekVariants) {

		this.logger = Loggers.getLogger("greeklish.converter");

		this.greekWords = new ArrayList<String>();

		this.reverseStemmer = new GreekReverseStemmer();

		this.greeklishGenerator = new GreeklishGenerator(maxExpansions);

		this.generateGreekVariants = generateGreekVariants;

		logger.debug("Max expansions: [{}]", maxExpansions);
		logger.debug("Generate Greek Variants: [{}]", generateGreekVariants);
	}

	/**
	 * The actual conversion is happening here. </p>
	 *
	 * @param inputToken
	 *            the Greek token
	 * @param tokenLength
	 *            the length of the input token
	 * @return A list of the generated strings
	 */
	public final List<StringBuilder> convert(char[] inputToken, int tokenLength) {
		// Convert to string in order to replace the digraphs with
		// special characters.
		tokenString = new String(inputToken, 0, tokenLength);
		// Is this a Greek word?
		if (!identifyGreekWord(tokenString)) {
			return null;
		}

		if (generateGreekVariants) {
			greekWords = reverseStemmer.generateGreekVariants(tokenString);
		} else {
			greekWords.add(tokenString);
		}

		if (greekWords.size() > 0) {
			return greeklishGenerator.generateGreeklishWords(greekWords);
		} else {
			return null;
		}
	}

	/**
	 * Identifies words with only Greek lowercase characters. </p>
	 *
	 * @param input
	 *            The string that will examine
	 * @return true if the string contains only Greek characters
	 */
	private boolean identifyGreekWord(String input) {
		if (StringUtils.containsOnly(input, GREEK_CHARACTERS)) {
			return true;
		} else {
			return false;
		}
	}
}
