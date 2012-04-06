package org.elasticsearch.index.analysis;

import java.util.List;

public class GreeklishConverterTest {
	private final String[] greekWords = { 
			"αυτοκινητο", "ομπρελα", "ξεσκεπαστος",
	};
	
	private final String[] forbiddenWords = {
			"mobile", "αυριο64", "καλάθι", "ΣΠιτι"
	};
	
	private GreeklishConverter converter;
	
	private List<StringBuilder> greeklishWords;
	

}
