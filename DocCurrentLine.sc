+ Document {
	currentLineStart {
		var pos = this.selectionStart;
		var string = this.string(0, pos);
		var i = pos-1;
		var c = $0;
		
		while({(i > 0) && (c != Char.nl)}, {
			c = string[i];
			i = i - 1;
		});
		
		^(i+1);
	}
	
	currentLineEnd {
		var pos = this.selectionStart;
		var string = this.string(pos, 1024);
		var len = string.size;
		var c = $0;
		var i = pos;
				
		while({(i < (pos + len)) && (c != Char.nl)}, {
			c = string[i-pos];
			i = i + 1;
		});
		
		^i-1;	
	}	
}