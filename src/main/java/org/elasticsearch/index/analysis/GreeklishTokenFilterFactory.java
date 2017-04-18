package org.elasticsearch.index.analysis;

import org.apache.lucene.analysis.TokenStream;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.assistedinject.Assisted;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.settings.IndexSettingsService;

public class GreeklishTokenFilterFactory  extends AbstractTokenFilterFactory {
	private final int maxExpansions;
	private final boolean generateGreekVariants;

	@Inject
	public GreeklishTokenFilterFactory(Index index,
									   IndexSettingsService indexSettings,
									   @Assisted String name,
									   @Assisted Settings settings) {
        super(index, indexSettings.getSettings(), name, settings);

        this.maxExpansions = settings.getAsInt("max_expansions", 20);
        this.generateGreekVariants = settings.getAsBoolean("greek_variants", true);
	}

	@Override
	public TokenStream create(TokenStream tokenStream) {
		return new GreeklishTokenFilter(tokenStream, maxExpansions, generateGreekVariants);
    }

}
