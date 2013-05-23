package org.elasticsearch.index.analysis;

public class GreeklishBinderProcessor extends AnalysisModule.AnalysisBinderProcessor {
	
	@Override
	public void processTokenFilters(TokenFiltersBindings tokenFiltersBindings) {
		tokenFiltersBindings.processTokenFilter("greeklish", GreeklishTokenFilterFactory.class);
	}

}
