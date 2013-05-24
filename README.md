Greeklish Token Filter for ElasticSearch
========================================

The Greeklish plugin generates tokens with latin characters from greek tokens.

The generated tokens have the save position and the same offset with the original greek tokens.

In order to install the 0.9 version of the plugin, run `bin/plugin -install gr.skroutz/elasticsearch-analysis-greeklish/0.9`

In order to install the plugin for versions prior to 0.9, run: `bin/plugin -install skroutz/elasticsearch-analysis-greeklish/0.8`.

    -------------------------------------
    | Greeklish Plugin | ElasticSearch	|
    -------------------------------------
    | 0.9              | 0.90.0         |
    -------------------------------------
    | 0.8              | 0.19.3         |
    -------------------------------------
    | 0.7              | 0.19.2         |
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

Generation of Greek Word Variations
-----------------------------------

It is difficult to distinguish a greeklish word from an english one during a query. So, if we wanted to stem the greeklish word in order to have the same results for the different forms of this word, we should apply a stemmer in both the greeklish and english words. In order to avoid that, the 0.7 version of the plugin comes with a reverse stemmer for greek words, which produces the different forms of the a greek word (from singular to plural and vice versa) in order to produce their greeklish version.

Now the greeklish word converter has two phases. The first phase produces the diffent forms of a greek word based on some grammar rules, and the second phase produces the greeklish version of each of theses greek words.

This functionality is enabled by default. But, it can be disabled by setting greek\_variants variable in the elasticsearch configuration file.

Example usage:

	index:
	  analysis:
	    filter:
	      greeklish_analysis:
	        type: greeklish
	        max_expansions: 15
	        greek_variants: false

Warning
-------

This filter acts only on greek lowercase characters and for this reason it should be applied after greek lowercase filter.
