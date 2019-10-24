package org.elasticsearch.index.analysis;

import org.apache.lucene.analysis.TokenStream;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.assistedinject.Assisted;
import org.elasticsearch.common.settings.Settings;

import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;

public class GreeklishTokenFilterFactory  extends AbstractTokenFilterFactory {
	private final int maxExpansions;
	private final boolean generateGreekVariants;
	private final boolean useSpecialMapping;

	@Inject
	public GreeklishTokenFilterFactory(IndexSettings indexSettings,
					   Environment env,
					   @Assisted String name,
					   @Assisted Settings settings) {

      		super(indexSettings, name, settings);
            	this.maxExpansions = settings.getAsInt("max_expansions", 20);
            	this.generateGreekVariants = settings.getAsBoolean("greek_variants", true);
				this.useSpecialMapping = settings.getAsBoolean("use_special_mapping", false);
	}

	@Override
	public TokenStream create(TokenStream tokenStream) {
		return new GreeklishTokenFilter(tokenStream, maxExpansions, generateGreekVariants, useSpecialMapping);
    }

}
