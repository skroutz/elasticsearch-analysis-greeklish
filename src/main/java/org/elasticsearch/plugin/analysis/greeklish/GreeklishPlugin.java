package org.elasticsearch.plugin.analysis.greeklish;

import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.indices.analysis.AnalysisModule.AnalysisProvider;
import org.elasticsearch.index.analysis.TokenFilterFactory;
import org.elasticsearch.plugins.AnalysisPlugin;
import org.elasticsearch.plugins.Plugin;

import org.elasticsearch.index.analysis.GreeklishTokenFilterFactory;

import java.io.IOException;
import java.util.Map;

import static java.util.Collections.singletonMap;

public class GreeklishPlugin extends Plugin implements AnalysisPlugin {

    // Use singletonMap to register our token filter,
    // since we only have one in our plugin.
    @Override
    public Map<String, AnalysisProvider<TokenFilterFactory>> getTokenFilters() {
        return singletonMap("skroutz_greeklish", new
                AnalysisProvider<TokenFilterFactory>() {
            @Override
            public TokenFilterFactory get(IndexSettings indexSettings,
                                          Environment env,
                                          String name,
                                          Settings settings) throws IOException {
                return new GreeklishTokenFilterFactory(indexSettings, name, settings);
            }
        });
    }
}