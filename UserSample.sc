UserSample : Object
{
	classvar dict;
	// you have to change this in order tu use it properly.
	classvar <>base = "";
	
	*new {|path|
		var buf;
		dict.isNil.if {dict = Dictionary.new;};
		Server.default.serverRunning.not.if {"Default Server is not running.".throw;};
		
		dict.at(path).isNil.if ({
			buf = Buffer.read(Server.default, base ++ path);
			dict.put(path, buf);
		},{
			buf = dict.at(path);
		});
		
		^buf;
	}
	
	*at {|path|
		^dict.at(path);
	}
	
	*list {
		dict.do {|item|
			item.postln;
		}
	}
	
	*free {|path|
		dict.at(path).notNil.if{
			dict.at(path).free;
			dict.removeAt(path);
		};
	}
	
	*freeAll {
		dict.do {|item|
			item.free;
		};
		dict = nil;
	}
}

UserSampleDragNDrop : Object
{
	var path;
	
	*new {|p|
		^super.new.init(p);
	}
	
	init {|p|
		path = p;	
	}	
	
	asCompileString {
		^"UserSample(\"" ++ path ++ "\")";	
	}
	
	asString {
		^path.basename;
	}
}