// Time based TNodes, Delays, TimedGates, etc.

/*
	DelayTNode
	*** TimedGateTNode
*/

DelayTNode : TNode
{
	var <>delayTime = 0.1;
	
	*new {|target = nil, name = nil, delayTime = nil|
		^super.new.init(target, name, delayTime);
	}
	
	init {|target = nil, name = nil, delayTime = nil|
		if(delayTime != nil, {
			this.delayTime = delayTime;
		});
		super.init(target, name);
	}
	
	trigger {|value = nil|
		// this is checked again in super.trigger
		if(enabled, {
		
			{super.trigger(value);}.defer(delayTime);

		});
	}	
}