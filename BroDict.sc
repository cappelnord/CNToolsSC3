BroDict : EnvironmentRedirect {
	
	var net;
	
	*new {|addr|
		^super.new.init(addr);	
	}
	
	init {|addr|
		net = addr;
	}
	
	put {|key, obj|
		var payload;
		
		super.put(key, obj);
		key = key.asSymbol;
				
		(obj.isCollection && obj.isString.not).if ({
			payload = obj.collect {|item| this.asTransmittableValue(item)};
		}, {
			payload = [this.asTransmittableValue(obj)];
		});

		net.sendMsg("/brodict", key.asString, *payload);		
	}
	
	asTransmittableValue {|value|
		value.isInteger.if {
			^value;
		};
		value.isNumber.if {
			^value.asFloat;	
		};
		// default, cast as String for security reasons
		^value.asString;	
	}
	
	signal {|key|
		this.put(key, 1);	
	}
}
