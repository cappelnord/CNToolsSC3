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
	
	balanceAllReturn {|action=\execute|
		var ss, sr, color;

		DocumentCursorStack.push;
		this.balanceParens(inf);

		ss = this.selectionStart;
		sr = this.selectionSize;

		(sr == 0).if {
			var cls = this.currentLineStart;
			var cle = this.currentLineEnd;
			ss = cls;
			sr = cle - cls;
		};
		
		(action == \execute).if {
			// this.interpret(Document.current.selectedString);
			thisProcess.interpreter.cmdLine = this.selectedString;
			thisProcess.interpreter.interpretPrintCmdLine;
			color = Color(255, 0, 255);
		};
		
		(action == \stack).if {
			CodeStack.push(this.selectedString);
			color = Color(0, 255, 255);	
		};

		this.stringColor_(color, ss, sr);
		DocumentCursorStack.pop;
		{this.syntaxColorize}.defer(0.33);	
	}
}