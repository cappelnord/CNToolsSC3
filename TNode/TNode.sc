// Base Class and Simple Helper TNodes

/*
	TNode
	DumpTNode
	FunctionTNode
	SynthNode
	ObjectTNode
*/

TNode
{
	var <>enabled = true;
	var <>target;
	var <lastValue;
	var <>name;
	
	var doTrigger = true;
	
	var <>triggerPost = nil; 	// Wenn dieser String gesetzt ist wird er bei jedem Trigger ausgegeben.
							// Natürlich stellt sich auch die Frage ob das nicht eigentlich Aufgabe einer
							// PostTNode Klasse wäre.
							
	var <>guiColor;
	var <>guiPositiveColor;
	var <>guiNegativeColor;
	var <>guiFontColor;
	
	var blinkView = nil;
	var view = nil;
	var win = nil;
	
	var blinkButton;
	var <> blinkTime = 0.1;
	
	var baseBounds;
	
	*new {|target = nil, name = nil|
		^super.new.init(target, name);
	}
	
	init {|target, name|
	
		this.target = target;	
		this.name = name;
		

		guiColor = Color.new(0.7,0.7,0.7);
		guiPositiveColor = Color.new(0.2,1,0.2);
		guiNegativeColor = Color.new(0.8,0,0);
		guiFontColor = Color.new(0,0,0);
		
		baseBounds = Rect(0,0,100,40);
	}
	
	process {|value = nil|
		// empty
		^value;
	}
	
	// Siehe auch BooleanFuncTNode
	trigger {|value = nil|
		doTrigger = true;
		if(enabled,
		{	
		
			if(triggerPost != nil, {
				triggerPost.postln;
			});
		
			value = this.process(value);
			lastValue = value;
			
			if(doTrigger, {
				target.do{|item|	
					if(item.respondsTo(\trigger),{	
						item.trigger(value);
					});
				};
			});
			
			if(view != nil,{
				this.processGui(value);
			});
			
			if(blinkView != nil,{
				this.processBlink(value);
			});

			
		});
	}
	
	asString {
	
		var retString;
		
		if(name == nil, {
			retString = "<" ++ this.class.asString ++ ">";
		}, {
			retString = "<" ++ this.class.asString ++ " \"" ++ name ++ "\">";
		});
		
		^retString;
	}
	
	trace {|preString = nil| // Feedbacks führen zum crash!
	
		if(preString == nil,
		{
			preString = "";
		});
		
		preString = preString ++ this.asString;
		
		if(target.isArray,
		{
			preString = preString ++ " --> <multiple targets, can't trace yet!>";
		},{
			if(target != nil,
			{
				preString = preString ++ " --> ";
				preString = target.trace(preString);
			});
		});
		
		^preString;
	}
	
	window {|argName, bounds|
		
		bounds = bounds ? baseBounds;
		
		if(argName == nil, {
			argName = this.name;
		});
	
		win = GUI.window.new(argName, bounds, false, true);
		view = this.gui(win);
		win.front;
		^win;
	}
	
	gui {|win, bounds|
		^this.blink(win, bounds);
	}
	
	blink {|win, bounds|
	
		bounds = bounds ? baseBounds;
		
		blinkView = GUI.compositeView.new(win, bounds);
		blinkButton = GUI.button.new(blinkView, bounds);
		blinkButton.states_([[name, guiFontColor, guiColor], [name, guiFontColor, guiPositiveColor]]);
		blinkButton.action_({|button| button.value = 0;});
		^blinkView;	
	}
	
	processBlink{|value|
	
		{blinkButton.value_(1);}.defer(0.0001);
		{blinkButton.value_(0);}.defer(blinkTime);

	}
	
	processGui{|value|
		// stub
	}
}

DumpTNode : TNode
{
	process {|value = nil|
		(this.asString ++ ":").post;
		if(value == nil,{
			" Triggered".postln;
		},{
			"".postln;
			value.dump;
		});
	 	^value;
	}
}

FunctionTNode : TNode
{
	var <> function;
	
	*new {|target = nil, name = nil, function = nil|
		^super.new.init(target, name, function);
	}
	
	init {|target = nil, name = nil, function = nil|
	
		this.function = function;
		super.init(target, name);
	
	}
	
	process {|value|
	
		value = function.value(value);
		^value;
	}

}

SynthTNode : TNode
{
	var <> synth;
	var <> arguments;
	
	*new {|target = nil, name = nil, synth = nil, arguments = nil|
		^super.new.init(target, name, synth, arguments);
	}
	
	init {|target = nil, name = nil, synth = nil, arguments = nil|
	
		this.synth = synth;
		this.arguments = arguments;
		super.init(target, name);
	
	}
	
	process {|value|
	
		Synth.grain(synth,arguments);
		^value;
	}

}


ObjectTNode : TNode
{
	var <> object;
	
	*new {|target = nil, name = nil, object = nil|
		^super.new.init(target, name, object);
	}
	
	init {|target = nil, name = nil, object = nil|
	
		this.object = object;
		super.init(target, name);
		
	}
	
	process {|value|
	
		value = this.object.copy;
		^value;
	}
}