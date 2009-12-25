/*
	P196 (String based Integer addition, palindrome check and better reverse addition)
	(c) 2009 by Patrick Borgeat <patrick@borgeat.de>
	http://www.cappel-nord.de
	
	Part of CNToolsSC3
	http://github.com/cappelnord/CNToolsSC3
*/

+ String {
	+++ {|anObject|
	
		var numbers = #[$0, $1, $2, $3, $4, $5, $6, $7, $8, $9];
		var longerString, shorterString, longerSize, shorterSize, result, carry;
		
		if(this.size < anObject.size,
			{longerString = anObject; shorterString = this;},
			{longerString = this; shorterString = anObject;});
			
		longerSize = longerString.size;
		shorterSize = shorterString.size;
		
		result = String.newClear(longerSize);
		carry = 0;
	
		// quite better.
		
		for(0, longerSize -1, {|i|
			var longerChar = longerString.at(longerSize-i-1);
			var shorterChar = $0;
			var res;
			if(i < shorterSize, {
				shorterChar = shorterString.at(shorterSize-i-1);
			});
			res = numbers.indexOf(longerChar) + numbers.indexOf(shorterChar) + carry;
			carry = 0;
			if(res >= 10, {carry = 1;});
			result.put(longerSize-i-1, ((res % 10) + 48).asAscii);
		});
		
		if(carry != 0, {result = carry.asString ++ result;});
		
		^result;
	}
	
	isPalindrome {
		var res = true;
		for(0, (this.size / 2) - 1, {|i|
			res = res && (this.at(i) == this.at(this.size - i - 1));
		});
		^res;
	}
	
	reverseAddition {
		var numbers = #[$0, $1, $2, $3, $4, $5, $6, $7, $8, $9];
		var result, carry, size, res;
		
		size = this.size;
		result = String.newClear(this.size);
		carry = 0;
		
		for(0, size - 1, {|i|
			res = numbers.indexOf(this.at(i)) + numbers.indexOf(this.at(size-i-1)) + carry;
			carry = 0;
			if(res >= 10, {carry = 1;});
			result.put(size-i-1, ((res % 10) + 48).asAscii);
		});
		
		if(carry != 0, {result = carry.asString ++ result;});
		
		^result;
	}
}