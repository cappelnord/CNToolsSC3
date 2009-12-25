/*
	BufferPool
	(c) 2009 by Patrick Borgeat <patrick@borgeat.de>
	http://www.cappel-nord.de
	
	Part of CNToolsSC3
	http://github.com/cappelnord/CNToolsSC3
*/

BufferPool 
{
	var <>server;
	var <>bufferLength;
	var <>channels;
	var buffers;
	var bufferCurrent;
	var bufferNum;
	
	*new {|server, bufferLength, channels = 1,  num = 4|
		^super.new.init(server, bufferLength, channels, num);
	}
	
	init {|server, bufferLength, channels, num|
	
		this.server = server;
		this.bufferLength = bufferLength;
		bufferNum = num;
		this.channels = channels;
		bufferCurrent = 0;
		
		buffers = Array.newClear(num);
		
		num.do{|i|
			buffers[i] = Buffer.alloc(server, bufferLength, channels);
		}
	}
	
	free {
		buffers.do{|item|
			item.free;
		}
	}
	
	next {
		var retBuffer = buffers[bufferCurrent];
		bufferCurrent = bufferCurrent + 1;
		
		(bufferCurrent >= bufferNum).if({
			bufferCurrent = 0;
		});
		^retBuffer;
	}
}