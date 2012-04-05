PfuncRewrite : FilterPattern {
	var <>func, <>levels;
	
	*new {|pattern, func, levels=1|
		func = func ? {|in| [in]};
		^super.new(pattern).func_(func).levels_(levels);
	}
	
	embedInStream {|event|
		var stream, value;
		stream = pattern.asStream;
		stream.do {|value|
			this.prProcess([value], levels);
		};
		^event;
	}
	
	prProcess {|inVal, level|
		(level <= 0).if({
			inVal.do {|val|
				val.yield;	
			};
		}, {
			inVal.do {|val|
				val = func.value(val);
				(val.isKindOf(Collection).not).if { val = [val];};
				this.prProcess(val, level-1);	
			};
		});
	}
}