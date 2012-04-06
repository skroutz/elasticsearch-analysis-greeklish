package org.elasticsearch.plugin.analysis.greeklish;

import org.elasticsearch.common.inject.Module;
import org.elasticsearch.index.analysis.AnalysisModule;
import org.elasticsearch.index.analysis.GreeklishBinderProcessor;
import org.elasticsearch.plugins.AbstractPlugin;

public class GreeklishPlugin extends AbstractPlugin {

	@Override
	public String description() {
		return "Generate greeklish terms from greek terms";
	}

	@Override
	public String name() {
		return "analysis-greeklish";
	}
	
    @Override public void processModule(Module module) {
        if (module instanceof AnalysisModule) {
            AnalysisModule analysisModule = (AnalysisModule) module;
            analysisModule.addProcessor(new GreeklishBinderProcessor());
        }
    }

}
