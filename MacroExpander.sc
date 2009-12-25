/*
	MacroExpander
	Written 2009 by Patrick Borgeat <patrick@borgeat.de>
	http://www.cappel-nord.de
*/

MacroExpander
{
	classvar >instance;
	
	var <>dict;
	
	*new {
		^super.new.init();
	}
	
	*put {|key, val|
		MacroExpander.instance.dict.put(key,val);
	}
	
	init {
		dict = Dictionary.new;
	}
	
	
	*instance {
		instance.isNil.if {
			instance = MacroExpander.new;
		}
		^instance;
	}
	
	*getCommands {
		^instance.getCommands;
	}
	
	expand {|cmd|
		
		var replacement = this.parseAndProcess(cmd);
		
		(replacement.isNil.not).if {
			this.pr_replace("\"" ++ cmd ++ "\".xx", replacement.asString);
		};
	}
	
	// private
	pr_replace {|cmd, replacement|
				
		// this would be more sensible, if it wouldn't try to replace all occurences of the expand cmd. 
		
		var string = Document.current.string;
		var pos = string.find(cmd);

		string = string.replace(cmd, replacement);
		
		Document.current.string_(string);
		Document.current.syntaxColorize;
		Document.current.selectRange(pos, replacement.size);
	}
	
	parseAndProcess {|cmd|
		
		// multiple exits method
		
		var res, key, args;
		var pos = cmd.find("#");
		
		if(pos.isNil, 
		{
			key = cmd.toLower;
		},
		{
			key =  cmd.copyRange(0,pos-1).toLower;
			args = this.pr_splitArgs(cmd.copyRange(pos + 1, cmd.size));
		});
		
		res = dict.at(key);
		
		res.isNil.if {
			("Command '" ++ key ++ "' not found in Macro Dictionary").error;
			^nil;
		};
		
		res.isKindOf(AbstractFunction).if {
			^res.value(args, key);
		};
			
		res.isKindOf(String).if {
			^this.pr_performString(res, args, key);
		};
		
		// if nothing really makes sense:
		^res.asCompileString;
	}
	
	// private
	pr_splitArgs{|string|
		string = string ? "";
		^string.split($,);
		// some cleanup?
	}
	
	// private 
	pr_performString{|res, args, key|
		var string = res;
		args.do {|item,i|
			string = string.replace("#" ++ i ++ "#", item);
			item.postln;
		};
		^string;
	}
	
	getCommands {
		var res = List.new;
		
		dict.getPairs.do {|value, i|
			if(i%2 == 0) {res.add(value)};
		};
		
		^res.asArray;
	}
}

+ String {
	xx {
		MacroExpander.instance.expand(this);
	}
}
