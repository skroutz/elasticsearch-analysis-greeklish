package org.elasticsearch.index.analysis;

import org.apache.lucene.analysis.TokenStream;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.assistedinject.Assisted;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.settings.IndexSettings;

public class GreeklishTokenFilterFactory  extends AbstractTokenFilterFactory {
	private final int maxExpansions;

	@Inject
	public GreeklishTokenFilterFactory(Index index, @IndexSettings Settings indexSettings, @Assisted String name, @Assisted Settings settings) {
        super(index, indexSettings, name, settings);

        this.maxExpansions = settings.getAsInt("max_expansions", 20);
	}

	@Override
	public TokenStream create(TokenStream tokenStream) {
		return new GreeklishTokenFilter(tokenStream, maxExpansions);
    }

}
