package org.elasticsearch.index.analysis;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
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
 *         digraphs. Because digraphs are special cases, they are treated in
 *         isolation.
 */
public class GreeklishConverter {

	/**
	 * Elastic Search logger
	 */
	protected final ESLogger logger;

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
	 * Constant variable that represent suffixes for pluralization of
	 * greeklish tokens.
	 */
	private static final String SUFFIX_MATOS = "ματοσ";
	private static final String SUFFIX_MATA = "ματα";
	private static final String SUFFIX_MATWN = "ματων";
	private static final String SUFFIX_AS = "ασ";
	private static final String SUFFIX_EIA = "εια";
	private static final String SUFFIX_EIO = "ειο";
	private static final String SUFFIX_EIOY = "ειου";
	private static final String SUFFIX_EIWN = "ειων";
	private static final String SUFFIX_IOY = "ιου";
	private static final String SUFFIX_IA = "ια";
	private static final String SUFFIX_IWN = "ιων";
	private static final String SUFFIX_OS = "οσ";
	private static final String SUFFIX_OI = "οι";
	private static final String SUFFIX_EIS = "εισ";
	private static final String SUFFIX_ES = "εσ";
	private static final String SUFFIX_HS = "ησ";
	private static final String SUFFIX_WN = "ων";
	private static final String SUFFIX_OY = "ου";
	private static final String SUFFIX_O = "ο";
	private static final String SUFFIX_H = "η";
	private static final String SUFFIX_A = "α";
	private static final String SUFFIX_I = "ι";

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
	 * Each digraph is replaced by a special capital Greek character.
	 */
	private final Map<String, String> digraphs = new HashMap<String, String>();

	/**
	 * This hash has as keys all the suffixes that we want to handle in order
	 * to generate singular/plurar greek words.
	 */
	private final Map<String, String[]> suffixes = new HashMap<String, String[]>();

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
	 * The possible suffix strings.
	 */
	private static final String[][] suffixStrings = new String[][] {
		{SUFFIX_MATOS, "μα", "ματων", "ματα"},  // κουρεματος, ασυρματος
		{SUFFIX_MATA, "μα", "ματων", "ματοσ"},  // ενδυματα
		{SUFFIX_MATWN, "μα", "ματα", "ματοσ"},  // ασυρματων, ενδυματων
		{SUFFIX_AS, "α", "ων", "εσ"},  // πορτας, χαρτοφυλακας
		{SUFFIX_EIA, "ειο", "ειων", "ειου", "ειασ"},  // γραφεια, ενεργεια
		{SUFFIX_EIO, "εια", "ειων", "ειου"},  // γραφειο
		{SUFFIX_EIOY, "εια", "ειου", "ειο"},  // γραφειου
		{SUFFIX_EIWN, "εια", "ειου", "ειο", "ειασ"},  // ασφαλειων, γραφειων
		{SUFFIX_IOY, "ι", "ια", "ιων"},  // πεδιου, κυνηγιου
		{SUFFIX_IA, "ιου", "ι", "ιων", "ιασ", "ιο"},  // πεδία, αρμονια
		{SUFFIX_IWN, "ιου", "ια", "ι", "ιο"},  // καλωδιων, κατοικιδιων
		{SUFFIX_OS, "η", "ουσ", "ου", "οι", "ων"},  // κλιματισμος
		{SUFFIX_OI, "οσ", "ου", "ων"},  // μυλοι, οδηγοι, σταθμοι
		{SUFFIX_EIS, "η", "ησ", "εων"},  // συνδεσεις, τηλεορασεις
		{SUFFIX_ES, "η", "ασ", "ων", "ησ", "α"},  // αλυσιδες
		{SUFFIX_HS, "ων", "εσ", "η", "εων"},  // γυμναστικης, εκτυπωσης
		{SUFFIX_WN, "οσ", "εσ", "α", "η", "ησ", "ου", "οι", "ο", "α"},  //  ινων, καπνιστων, καρτων, κατασκευων 
		{SUFFIX_OY, "ων", "α", "ο", "οσ"},  // λαδιου, μοντελισμου, παιδικου
		{SUFFIX_O, "α", "ου", "εων", "ων"},  // αυτοκινητο, δισκος
		{SUFFIX_H, "οσ", "ουσ", "εων", "εισ", "ησ", "ων"},  //βελη, ψυξη, τηλεοραση, αποτριχωση
		{SUFFIX_A, "ο" , "ου", "ων"},  // γιλεκα, εσωρουχα, 
		{SUFFIX_I, "ιου", "ια", "ιων"}  // γιαουρτι, γραναζι
	};

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
	private String tokenString;

	/**
	 * Input token converted into String without substitutions.
	 * It is used for logging the processing token.
	 */
	private String initialToken;

	/**
	 * The greeklish word buffer
	 */
	private Stack<String> greekWords = new Stack<String>();


	// Constructor
	public GreeklishConverter(int maxExpansions) {

		this.logger = Loggers.getLogger("greeklish.converter");

		this.maxExpansions = maxExpansions;

		this.greeklishList = new CopyOnWriteArrayList<StringBuilder>();

		// populate digraphs
		for (String[] digraphCase : digraphCases) {
			digraphs.put(digraphCase[0], digraphCase[1]);
		}

		// populate conversions
		for (String[] convertString : convertStrings) {
			conversions.put(convertString[0].charAt(0),
					Arrays.copyOfRange(convertString, 1, convertString.length));
		}

		// populate suffixes
		for (String[] suffix : suffixStrings) {
			suffixes.put(suffix[0], Arrays.copyOfRange(suffix, 1, suffix.length));
		}
		logger.debug("Max expansions: [{}]", maxExpansions);
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
		greekWords.clear();
		greeklishList.clear();
		// Convert to string in order to replace the digraphs with
		// special characters.
		tokenString = new String(inputToken, 0, tokenLength);

		// Keep the initial state of the tokenString
		initialToken = tokenString;

		// Is this a Greek word?
		if (!identifyGreekWord(tokenString)) {
			return null;
		}

		greekWords.add(tokenString);

		for (String[] suffix : suffixStrings) {
			if (tokenString.endsWith(suffix[0])) {
				generate_more_greek_words(suffix[0]);
				break;
			}
		}

		for (String greekWord : greekWords) {

			List<StringBuilder> per_word = new CopyOnWriteArrayList<StringBuilder>();

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
				addCharacter(conversions.get(greekChar), allocatedSpace, per_word);
			}
			greeklishList.addAll(per_word);
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
	private void addCharacter(String[] convertStrings, int bufferSize, List<StringBuilder> perWord) {
		// If the token list is empty, create a new StringBuilder and add the
		// latin characters
		if (perWord.isEmpty()) {
			for (String convertString : convertStrings) {
				if (perWord.size() >= maxExpansions) {
					logger.debug("Skipping for token [{}]", initialToken);
					break;
				}
				StringBuilder greeklishWord = new StringBuilder(bufferSize);
				greeklishWord.append(convertString);
				perWord.add(greeklishWord);
			}
			// Add the latin characters to each saved greeklish token, and
			// generate new ones
			// when the combinations are more than one.
		} else {
			for (StringBuilder atoken : perWord) {
				for (String convertString : Arrays.copyOfRange(convertStrings,
						1, convertStrings.length)) {
					if (perWord.size() >= maxExpansions) {
						logger.debug("Skipping for token [{}]", initialToken);
						break;
					}
					StringBuilder newToken = new StringBuilder(atoken);
					newToken.append(convertString);
					perWord.add(newToken);
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

	/**
	 * Generates more greek words based on the suffix of the original word
	 * @param inputSuffix the suffix that matched
	 */
	private void generate_more_greek_words(final String inputSuffix) {
		for (String suffix : suffixes.get(inputSuffix)) {
			greekWords.add(tokenString.replaceAll(inputSuffix + "$", suffix));
		}
	}
}
