DocumentCursorStack {
	classvar stack;
	*initClass {
		DocumentCursorStack.reset();
	}
	
	*push {|pos|
		pos = pos ?? {Document.current.selectionStart};
		stack.add(pos);
	}
	
	*pop {|jumpToPos=true|
		var pos = stack.pop;
		(jumpToPos && pos.notNil).if {
			Document.current.selectRange(pos, 0);
		};
	}
	
	*reset {
		stack = LinkedList();	
	}
}