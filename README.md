Greeklish Token Filter for ElasticSearch
========================================

The Greeklish plugin generates tokens with latin characters from greek tokens.

The generated tokens have the save position and the same offset with the original greek tokens.

In order to install the plugin, simply run: `bin/plugin -install skroutz/elasticsearch-analysis-greeklish/0.6`.

    -------------------------------------
    | Greeklish Plugin | ElasticSearch	|
    -------------------------------------
    | 0.6              | 0.19.2         |
    -------------------------------------


Expansions
----------

There are more than one combinations of latin characters that can substitute each character of the greek alphabet. So, a greek token is expanded to as many greeklish tokens as the the combinations of the latin characters for each greek character of a token and in some cases this produces an enormous number of expansions. In order to prevent this from happening, a threshold of the max expansions is set.  The default value is 20.

However, a threshold of the max expansions can be set in the elasticsearch.yml. When this threshold is reached the remaining characters are substitute with the most common variant of the greek character.

Example usage:

	index:
	  analysis:
	    filter:
	      greeklish_analysis:
	        type: greeklish
	        max_expansions: 15

Warning
-------

This filter acts only on greek lowercase characters and for this reason it should be applied after greek lowercase filter.
