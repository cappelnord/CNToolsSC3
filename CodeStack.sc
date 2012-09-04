CodeStack {
	classvar stack;
	*initClass {
		CodeStack.reset();
	}
	
	*push {|code|
		var func = nil;
		func = thisProcess.interpreter.compile(code);
		
		(func != nil).if ({
			stack.add(func);
			("CodeStack Size: " ++ stack.size).postln;
		}, {
			("Could not compile!").postln;
		});
	}
	
	*execute {

		"Executing CodeStack".postln;		
		stack.do {|code|
			try {
				code.value().postln;
			};	
		};
		
		CodeStack.reset();
	}
	
	*reset {
		stack = LinkedList();	
	}
}