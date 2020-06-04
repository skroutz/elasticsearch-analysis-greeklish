package org.elasticsearch.index.analysis;

import java.io.IOException;
import java.util.List;
import java.util.Stack;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.util.AttributeSource;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.common.logging.Loggers;

/**
 * @author Tasos Stathopoulos
 * It generates greeklish tokens(tokens with latin characters) from Greek tokens.
 * The generated tokens will have the same position and the same offset with the
 * original Greek tokens, and their type will be {@code greeklish_word}.
 * This filters acts <b>only on Greek lowercase characters</b> and for this reason
 * it should be applied after lowercase filter for Greek language.
 */
public class GreeklishTokenFilter extends TokenFilter {
	private static final Logger logger =
		Loggers.getLogger(GreeklishConverter.class,
											GreeklishConverter.class.getSimpleName());
	/**
	 * The type of the generated tokens
	 */
	public static final String TOKEN_TYPE = "greeklish_word";

	/**
	 * The greeklish word buffer
	 */
	private Stack<char[]> greeklishWords = new Stack<char[]>();

	private AttributeSource.State current;
	private final CharTermAttribute termAttribute = addAttribute(CharTermAttribute.class);
	private final PositionIncrementAttribute posIncAttribute = addAttribute(PositionIncrementAttribute.class);
	private final TypeAttribute typeAttribute = addAttribute(TypeAttribute.class);

	/**
	 * The greeklish converter that makes all the real work.
	 */
	private GreeklishConverter greeklishConverter;

	// Constructor
	public GreeklishTokenFilter(TokenStream tokenStream, int maxExpansions, boolean generateGreekVariants) {
		super(tokenStream);
		this.greeklishConverter = new GreeklishConverter(maxExpansions, generateGreekVariants);
	}

	@Override
	public boolean incrementToken() throws IOException {
		// If the stack has greeklish tokens, now it is the
		// right time to put them in the token stream
		if (greeklishWords.size() > 0) {
			char[] greeklishWord = greeklishWords.pop();
			restoreState(current);
			termAttribute.copyBuffer(greeklishWord, 0, greeklishWord.length);
			termAttribute.setLength(greeklishWord.length);
			posIncAttribute.setPositionIncrement(0);
			typeAttribute.setType(TOKEN_TYPE);
			return true;
		}
		// No more tokens in the token stream, it's over
		if (!input.incrementToken()) {
			return false;
		}

		// if this token is useful to generate greeklish tokens
		// hold the current state, because we have work to do.
		if (addWordsToStack()) {
			current = captureState();
		}

		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void reset() throws IOException {
		super.reset();
	}

	/**
	 * This method checks if a token can be used to generate greeklish tokens.
	 * If it is valid, it populates the greeklish token buffer with greeklish
	 * tokens.
	 * @return false if no tokens are generated, true elsewhere.
	 * @throws IOException
	 */
	private boolean addWordsToStack() throws IOException {
		// Did the converter returned any greeklish tokens
		// If true, place the in the token buffer, or else go to the next
		// Greek token of the token stream.
		List<StringBuilder> greeklishTokens = greeklishConverter.convert(termAttribute.buffer(), termAttribute.length());
		if (greeklishTokens == null || greeklishTokens.isEmpty()) {
			return false;
		}
		for (StringBuilder word : greeklishTokens) {
			greeklishWords.push(word.toString().toCharArray());
		}
		return true;
	}


}
