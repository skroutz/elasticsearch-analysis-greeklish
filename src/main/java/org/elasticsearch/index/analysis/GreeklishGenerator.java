package org.elasticsearch.index.analysis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.logging.log4j.Logger;
import org.elasticsearch.common.logging.Loggers;

/**
 * @author Tasos Stathopoulos
 * Generates greeklish tokens for each element of list
 * of greek tokens.
 */
public class GreeklishGenerator {
	private static final Logger logger =
		Loggers.getLogger(GreeklishConverter.class,
											GreeklishConverter.class.getSimpleName());

	/**
	 * Constant variables that represent the character that substitutes a
	 * digraph.
	 */
	private static final String AI = "Α";
	private static final String EI = "Ε";
	private static final String OI = "Ο";
	private static final String OY = "Υ";
	private static final String EY = "Φ";
	private static final String AY = "Β";
	private static final String MP = "Μ";
	private static final String GG = "Γ";
	private static final String GK = "Κ";
	private static final String NT = "Ν";

	/**
	 * Each digraph is replaced by a special capital Greek character.
	 */
	private final Map<String, String> digraphs = new HashMap<String, String>();

	/**
	 * This hash has keys all the possible conversions that can be applied and
	 * values the strings that can replace the corresponding Greek character.
	 */
	private final Map<Character, String[]> conversions = new HashMap<Character, String[]>();

	/**
	 * The possible digraph cases.
	 */
	private static final String[][] digraphCases = new String[][] {
			{ "αι", AI }, { "ει", EI }, { "οι", OI }, { "ου", OY },
			{ "ευ", EY }, { "αυ", AY }, { "μπ", MP }, { "γγ", GG },
			{ "γκ", GK }, { "ντ", NT } };
	/**
	 * The possible string conversions for each case.
	 */
	private static final String[][] convertStrings = new String[][] {
			{ AI, "ai", "e" }, { EI, "ei", "i" }, { OI, "oi", "i" },
			{ OY, "ou", "oy", "u" }, { EY, "eu", "ef", "ev", "ey" },
			{ AY, "au", "af", "av", "ay" }, { MP, "mp", "b" },
			{ GG, "gg", "g" }, { GK, "gk", "g" }, { NT, "nt", "d" },
			{ "α", "a" }, { "β", "b", "v" }, { "γ", "g" }, { "δ", "d" },
			{ "ε", "e" }, { "ζ", "z" }, { "η", "h", "i" }, { "θ", "th" },
			{ "ι", "i" }, { "κ", "k" }, { "λ", "l" }, { "μ", "m" },
			{ "ν", "n" }, { "ξ", "ks", "x" }, { "ο", "o" }, { "π", "p" },
			{ "ρ", "r" }, { "σ", "s" }, { "τ", "t" }, { "υ", "y", "u", "i" },
			{ "φ", "f", "ph" }, { "χ", "x", "h", "ch" }, { "ψ", "ps" },
			{ "ω", "w", "o", "v" } };

	/**
	 * The maximum greeklish expansions per greek token.
	 */
	private final int maxExpansions;

	/**
	 * A list of greeklish token per each greek word.
	 */
	private final List<StringBuilder> perWordGreeklish;

	/**
	 * Keep the generated strings in a list. The populated list is
	 * returned to the filter.
	 * CopyOnWriteArrayList is used because it is thread safe and has the
	 * ability to add components while a thread iterates over its elements.
	 */
	private final List<StringBuilder> greeklishList;

	/**
	 * Input token converted into String.
	 */
	private char[] inputToken;

	/**
	 * Input token converted into String without substitutions.
	 * It is used for logging the processing token.
	 */
	private String initialToken;

	// Constructor
	public GreeklishGenerator(int maxExpansions) {

		this.maxExpansions = maxExpansions;

		this.greeklishList = new ArrayList<StringBuilder>();

		this.perWordGreeklish = new CopyOnWriteArrayList<StringBuilder>();

		// populate digraphs
		for (String[] digraphCase : digraphCases) {
			digraphs.put(digraphCase[0], digraphCase[1]);
		}

		// populate conversions
		for (String[] convertString : convertStrings) {
			conversions.put(convertString[0].charAt(0),
					Arrays.copyOfRange(convertString, 1, convertString.length));
		}
	}

	/**
	 * Gets a list of greek words and generates the greeklish version of
	 * each word.
	 * @param greekWords a list of greek words
	 * @return a list of greeklish words
	 */
	public List<StringBuilder> generateGreeklishWords(final List<String> greekWords) {
		greeklishList.clear();
		for (String greekWord : greekWords) {

			perWordGreeklish.clear();

			initialToken = greekWord;
			// Allocate space that is twice the length of the input token in
			// order
			// to cover
			// worst case scenario where each Greek character is replaced by two
			// latin characters
			int allocatedSpace = 2 * greekWord.length();

			for (String key : digraphs.keySet()) {
				greekWord = greekWord.replaceAll(key, digraphs.get(key));
			}

			// Convert it back to array of characters. The iterations of each
			// character will take place through this array.
			inputToken = greekWord.toCharArray();

			// Iterate through the characters of the token and generate
			// greeklish
			// words
			for (char greekChar : inputToken) {
				addCharacter(conversions.get(greekChar), allocatedSpace);
			}
			greeklishList.addAll(perWordGreeklish);
		}
		return greeklishList;
	}

	/**
	 * Add the matching latin characters to the generated greeklish tokens for a
	 * specific Greek character. For each different combination of latin
	 * characters, a new token is generated.
	 *
	 * @param convertStrings
	 *            The latin characters that will be added to the tokens
	 * @param bufferSize
	 *            The size of the buffer that will be allocated in case of new
	 *            StringBuilder
	 */
	private void addCharacter(String[] convertStrings, int bufferSize) {
		// If the token list is empty, create a new StringBuilder and add the
		// latin characters
		if (perWordGreeklish.isEmpty()) {
			for (String convertString : convertStrings) {
				if (perWordGreeklish.size() >= maxExpansions) {
					logger.debug("Skipping for token [{}]", initialToken);
					break;
				}
				StringBuilder greeklishWord = new StringBuilder(bufferSize);
				greeklishWord.append(convertString);
				perWordGreeklish.add(greeklishWord);
			}
			// Add the latin characters to each saved greeklish token, and
			// generate new ones
			// when the combinations are more than one.
		} else {
			for (StringBuilder atoken : perWordGreeklish) {
				for (String convertString : Arrays.copyOfRange(convertStrings,
						1, convertStrings.length)) {
					if (perWordGreeklish.size() >= maxExpansions) {
						logger.debug("Skipping for token [{}]", initialToken);
						break;
					}
					StringBuilder newToken = new StringBuilder(atoken);
					newToken.append(convertString);
					perWordGreeklish.add(newToken);
				}
				atoken.append(convertStrings[0]);
			}
		}
	}
}
