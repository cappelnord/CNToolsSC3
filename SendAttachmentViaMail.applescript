on run argv
	tell application "Mail"
		set theMessage to make new outgoing message with properties {visible:true, subject: item 1 of argv, content:"SuperCollider rocks again:\n\n"} 
		tell content of theMessage 
			make new attachment with properties {file name:item 2 of argv} at after last paragraph 
		end tell
	end tell
end run