package org.elasticsearch.plugin.analysis.greeklish;

import org.elasticsearch.index.analysis.AnalysisModule;
import org.elasticsearch.index.analysis.GreeklishBinderProcessor;
import org.elasticsearch.plugins.Plugin;

public class GreeklishPlugin extends Plugin {

  @Override
  public String description() {
    return "Generate greeklish terms from greek terms";
  }

  @Override
  public String name() {
    return "analysis-greeklish";
  }

  public void onModule(AnalysisModule module) {
    module.addProcessor(new GreeklishBinderProcessor());
  }
}