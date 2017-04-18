package org.elasticsearch.index.analysis;

import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.Index;

import org.elasticsearch.test.ESTestCase;
import org.elasticsearch.plugin.analysis.greeklish.GreeklishPlugin;

import java.io.IOException;

import static org.hamcrest.Matchers.instanceOf;

class SimpleSkroutzGreekStemmerAnalysisTest extends ESTestCase {
    public void testSkroutzGreekStemmerAnalysis() throws IOException {
        TestAnalysis analysis = createTestAnalysis(new Index("test", "_na_"),
                Settings.EMPTY, new GreeklishPlugin());

        TokenFilterFactory filterFactory = analysis.tokenFilter.get
                ("skroutz_greeklish");
        assertThat(filterFactory, instanceOf(GreeklishTokenFilterFactory.class));
    }
}
