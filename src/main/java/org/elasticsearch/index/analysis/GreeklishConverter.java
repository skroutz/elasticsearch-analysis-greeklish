package org.elasticsearch.index.analysis;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.common.logging.Loggers;


/**
 * @author Tasos Stathopoulos Generates singular/plural variants of greek
 * 		   tokens and converts them to tokens with latin characters from which are
 * 		   matched to the corresponding greek characters.
 * 		   A Greek character may have one or more latin counterparts. So,
 * 		   from a Greek token one or more latin tokens are
 *         generated. Greek words have combination of vowels called
 *         digraphs. Because digraphs are special cases, they are treated separately.
 */
public class GreeklishConverter {
	/**
	 * Elastic Search logger
	 */
    private static final Logger logger =
			Loggers.getLogger(GreeklishConverter.class,
												GreeklishConverter.class.getSimpleName());

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

	/**
	 * Instance of the reverse stemmer that generates the word variants
	 * of the greek token.
	 */
	private final GreekReverseStemmer reverseStemmer;

	/**
	 * Instance of the greeklish generator that generates the greeklish
	 * words from the words that are returned by the greek reverse stemmer.
	 */
	private final GreeklishGenerator greeklishGenerator;

	/**
	 * Setting that which is set in the configuration file that defines
	 * whether the user wants to generate greek variants.
	 */
	private final boolean generateGreekVariants;

	// Constructor
	public GreeklishConverter(int maxExpansions, boolean generateGreekVariants) {

		// Initialize greekWords list
		this.greekWords = new ArrayList<String>();

		// Initialize reverse stemmer
		this.reverseStemmer = new GreekReverseStemmer();

		// Initialize greeklish generator
		this.greeklishGenerator = new GreeklishGenerator(maxExpansions);

		// Initialize setting for generating greek variants
		this.generateGreekVariants = generateGreekVariants;

		logger.debug("Max expansions: [{}] Generate Greek Variants [{}]", maxExpansions, generateGreekVariants);
	}

	/**
	 * The actual conversion is happening here.
	 *
	 * @param inputToken
	 *            the Greek token
	 * @param tokenLength
	 *            the length of the input token
	 * @return A list of the generated strings
	 */
	public final List<StringBuilder> convert(char[] inputToken, int tokenLength) {
		// Convert to string in order to pass it to the reverse stemmer.
		tokenString = new String(inputToken, 0, tokenLength);
		// Is this a Greek word?
		if (!identifyGreekWord(tokenString)) {
			return null;
		}

		// if generating greek variants is on
		if (generateGreekVariants) {
			// generate them
			greekWords = reverseStemmer.generateGreekVariants(tokenString);
		} else {
			greekWords.add(tokenString);
		}

		// if there are greek words
		if (greekWords.size() > 0) {
			// generate their greeklish version
			return greeklishGenerator.generateGreeklishWords(greekWords);
		} else {
			return null;
		}
	}

	/**
	 * Identifies words with only Greek lowercase characters.
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
