package org.elasticsearch.index.analysis;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;

/**
 * @author Tasos Stathopoulos </p> Generates tokens with latin characters from
 *         Greek tokens. It matches one or more latin characters for each Greek
 *         character of the token. A Greek character may have one or more latin
 *         counterparts. So, from a Greek token one or more latin tokens are
 *         generated. </p> Greek words have combination of vowels called
 *         diphthongs. Because diphthongs are special cases, they are treated in
 *         isolation.
 */
public class GreeklishConverter {

	/**
	 * Elastic Search logger
	 */
	protected final ESLogger logger;

	/**
	 * Constant variables that represent the character that substitutes a
	 * diphthong.
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
	 * The maximum number of greeklish words that can be produce
	 * from a single Greek word.
	 */
	private final int maxExpansions;

	/**
	 * Tokens that contain only these characters will be affected by this
	 * filter.
	 */
	public static final String GREEK_CHARACTERS = "αβγδεζηθικλμνξοπρστυφχψω";

	/**
	 * Each diphthong is replaced by a special capital Greek character.
	 */
	private final Map<String, String> diphthongs = new HashMap<String, String>();

	/**
	 * This hash has keys all the possible conversions that can be applied and
	 * values the strings that can replace the corresponding Greek character.
	 */
	private final Map<Character, String[]> conversions = new HashMap<Character, String[]>();

	/**
	 * The possible diphthong cases.
	 */
	private static final String[][] dipthongCases = new String[][] {
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
	 * Keep the generated strings in a list. The populated list is
	 * returned to the filter.
	 * CopyOnWriteArrayList is used because it is thread safe and has the
	 * ability to add components while a thread iterates over its elements.
	 */
	private final List<StringBuilder> greeklishList;

	// Constructor
	public GreeklishConverter(int maxExpansions) {

		this.maxExpansions = maxExpansions;

		this.greeklishList = new CopyOnWriteArrayList<StringBuilder>();

		this.logger = Loggers.getLogger("greeklish.converter");
		// populate diphthongs
		for (String[] diphthongCase : dipthongCases) {
			diphthongs.put(diphthongCase[0], diphthongCase[1]);
		}

		// populate conversions
		for (String[] convertString : convertStrings) {
			conversions.put(convertString[0].charAt(0),
					Arrays.copyOfRange(convertString, 1, convertString.length));
		}
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
		greeklishList.clear();
		// Convert to string in order to replace the diphthongs with
		// special characters.
		String tokenString = new String(inputToken, 0, tokenLength);

		// Is this a Greek word?
		if (!identifyGreekWord(tokenString)) {
			return null;
		}

		for (String key : diphthongs.keySet()) {
			tokenString = tokenString.replaceAll(key, diphthongs.get(key));
		}

		// Convert it back to array of characters. The iterations of each
		// character will take place through this array.
		inputToken = tokenString.toCharArray();

		// Allocate space that is twice the length of the input token in order
		// to cover
		// worst case scenario where each Greek character is replaced by two
		// latin characters
		int allocatedSpace = 2 * tokenLength;

		// Iterate through the characters of the token and generate greeklish
		// words
		for (char greekChar : inputToken) {
			addCharacter(conversions.get(greekChar), allocatedSpace);
		}
		return greeklishList;
	}

	/**
	 * Add the matching latin characters to the generated greeklish tokens for a
	 * specific Greek character. </p> For each different combination of latin
	 * characters, a new token is generated. </p>
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
		if (greeklishList.isEmpty()) {
			for (String convertString : convertStrings) {
				if (greeklishList.size() >= maxExpansions) {
					break;
				}
				StringBuilder greeklishWord = new StringBuilder(bufferSize);
				greeklishWord.append(convertString);
				greeklishList.add(greeklishWord);
			}
			// Add the latin characters to each saved greeklish token, and
			// generate new ones
			// when the combinations are more than one.
		} else {
			for (StringBuilder atoken : greeklishList) {
				for (String convertString : Arrays.copyOfRange(convertStrings,
						1, convertStrings.length)) {
					if (greeklishList.size() >= maxExpansions) {
						break;
					}
					StringBuilder newToken = new StringBuilder(atoken);
					newToken.append(convertString);
					greeklishList.add(newToken);
				}
				atoken.append(convertStrings[0]);
			}
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
