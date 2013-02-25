package bn.elicitator.events

import bn.elicitator.Variable

class FinishedVariableEvent extends LoggedEvent {

	Variable variable

	static logEvent( Variable var ) {
		saveEvent( new FinishedVariableEvent( variable: var ) )
	}

}
