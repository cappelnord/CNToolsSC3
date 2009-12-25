/*
	NES2Freqs (for use with NES Ugens)
	(c) 2009 by Patrick Borgeat <patrick@borgeat.de>
	http://www.cappel-nord.de
	
	Part of CNToolsSC3
	http://github.com/cappelnord/CNToolsSC3
*/

+ SimpleNumber
{
	cpsnes{
		var result = 1790000 / (this * 16) - 1;
		^result.asInteger;
	}
	
	midines{
		var result = 1790000 / (this.midicps * 16) - 1;
		^result.asInteger;
	}
	
	nescps{
		^1790000 / ((this + 1) * 16);
	}
	
	
	nesmidi{
		^(1790000 / ((this + 1) * 16)).cpsmidi;
	}
}

+ SequenceableCollection 
{
	cpsnes  { ^this.performUnaryOp('cpsnes') }
	midines { ^this.performUnaryOp('midines') }
	nescps  { ^this.performUnaryOp('nescps') }
	nesmidi { ^this.performUnaryOp('nesmidi') }
}


// i don't know if this is too complicated, but it works!

+ UGen 
{	
	cpsnes  { ^BinaryOpUGen('/', 1790000, BinaryOpUGen('-', BinaryOpUGen('*', this, 16), 1))}
	midines { ^BinaryOpUGen('/', 1790000, BinaryOpUGen('-', BinaryOpUGen('*', UnaryOpUGen('midicps', this), 16), 1))}
	nescps  { ^BinaryOpUGen('/', 1790000, BinaryOpUGen('*', BinaryOpUGen('+', this, 1), 16))}
	nesmidi { ^UnaryOpUGen('cpsmidi', BinaryOpUGen('/', 1790000, BinaryOpUGen('*', BinaryOpUGen('+', this, 1), 16)))}
}



