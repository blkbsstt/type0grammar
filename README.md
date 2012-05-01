A Thue Inspired Unrestricted Grammar Implementation
===================================================

This program takes type-0 (unrestricted) grammars and produces a string generated from that grammar.

Expected Grammar Format
-----------------------

	Start Symbol
	Newline delimited list of grammar productions

where grammar productions are

	lhs -> rhs

where lhs and rhs are some strings. The " -> " in the middle is the production separator and can be changed through the use of the -s flag. (see Flags below).

###Example

	S
	S -> XTY
	T -> ATB
	T -> 
	XY -> 
	AB -> BaA
	aB -> Ba
	Aa -> aA
	XB -> X
	AY -> CY
	aC -> Ca
	AC -> CA
	XC -> D
	DC -> D
	Da -> aD
	DX -> 

This example produces strings of the language {a^n^2 | n ≥ 0}.

Usage
-----

The program expects some combination of flags, and then a file to read the grammar from.

###Flags

* Batch mode:

		-b num

	Produces _num_ strings of the language defined by the grammar, seperated by newlines. If the grammar produces less than _num_ strings, it will infinite loop. If the grammar has a very low chance to produce _num_ distinct strings, it will have a very high chance to infinite loop. (Same thing applies in the normal usage, but with _num_ being 1).

* Times to run:
		
		-t num

	Only attemps to generate a new string _num_ times. Will produce output identical to batch mode and can be used to prevent batch mode from possibly infinite looping. If both -b and -t are used, the first condition to be satisfied will be the exit condition.

* Unsorted mode:

		-u

	With the batch or times mode enabled, the list of strings is accumulated and then sorted before displaying. Unsorted mode will instead immediately display any string generated that had not previously been in the generated set.

* Custom Separator

		-s sep

	Allows for changing the separator used for the productions. Default is " -> ". There is a problem setting the separator if using sbt, because of the way sbt handles input arguments. However, running through scala, or from the jar (if I add one, on the to-do list), should work well.

* Debug mode:

		-d

	Produces output including the grammar that was input, and each step of applying grammar productions. 