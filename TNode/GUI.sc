// Classes that provide GUI as sole purpose

/*
	ButtonTNode
*/

ButtonTNode : ObjectTNode
{
	var button;
	
	gui {|win, bounds|
	
		bounds = bounds ? baseBounds;
		view = GUI.compositeView.new(win, bounds);
		button = GUI.button.new(view, bounds);
		button.states_([[name, guiFontColor, guiColor], [name, guiFontColor, guiPositiveColor]]);
		button.action_({|button|
			this.trigger(object);
			{
				button.value = 0;
			}.defer(blinkTime);
		});
		^view;
	}
}