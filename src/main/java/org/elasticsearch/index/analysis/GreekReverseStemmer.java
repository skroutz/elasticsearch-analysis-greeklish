package org.elasticsearch.index.analysis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.common.logging.Loggers;

/**
 * @author Tasos Stathopoulos
 * Generates singular/plural variants of a greek word based
 * on a combination of predefined rules.
 */
public class GreekReverseStemmer {
	private static final Logger logger =
		Loggers.getLogger(GreeklishConverter.class,
											GreeklishConverter.class.getSimpleName());

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
	 * This hash has as keys all the suffixes that we want to handle in order
	 * to generate singular/plural greek words.
	 */
	private final Map<String, String[]> suffixes = new HashMap<String, String[]>();

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
		{SUFFIX_EIOY, "εια", "ειου", "ειο", "ειων"},  // γραφειου
		{SUFFIX_EIWN, "εια", "ειου", "ειο", "ειασ"},  // ασφαλειων, γραφειων
		{SUFFIX_IOY, "ι", "ια", "ιων", "ιο"},  // πεδιου, κυνηγιου
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
		{SUFFIX_A, "ο" , "ου", "ων", "ασ", "εσ"},  // γιλεκα, εσωρουχα, ομπρελλα
		{SUFFIX_I, "ιου", "ια", "ιων"}  // γιαουρτι, γραναζι
	};

	/**
	 * The greek word list
	 */
	private List<String> greekWords = new ArrayList<String>();

	// Constructor
	public GreekReverseStemmer() {

		// populate suffixes
		for (String[] suffix : suffixStrings) {
			suffixes.put(suffix[0], Arrays.copyOfRange(suffix, 1, suffix.length));
		}
	}

	/**
	 * This method generates the greek variants of the greek token that
	 * receives.
	 *
	 * @param tokenString the greek word
	 * @return a list of the generated greek word variations
	 */
	public List<String> generateGreekVariants(String tokenString) {
		// clear the list from variations of the previous greek token
		greekWords.clear();

		// add the initial greek token in the greek words
		greekWords.add(tokenString);

		// Find the first matching suffix and generate the
		// the variants of this word
		for (String[] suffix : suffixStrings) {
			if (tokenString.endsWith(suffix[0])) {
				// Add to greekWords the tokens with the desired suffixes
				generate_more_greek_words(tokenString, suffix[0]);
				break;
			}
		}
		return greekWords;
	}

	/**
	 * Generates more greek words based on the suffix of the original word
	 * @param inputSuffix the suffix that matched
	 */
	private void generate_more_greek_words(final String inputToken, final String inputSuffix) {
		for (String suffix : suffixes.get(inputSuffix)) {
			greekWords.add(inputToken.replaceAll(inputSuffix + "$", suffix));
		}
	}
}
