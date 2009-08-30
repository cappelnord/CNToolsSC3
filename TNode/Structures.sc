// Control Structures

/*
	BooleanFuncTNode
	
*/

BooleanTNode : FunctionTNode
{
	process {|value| 
		if(function.value(value) == false,
		{
			doTrigger = false;
		});
		^value;
	}
}