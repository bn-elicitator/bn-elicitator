package bn.elicitator.init

import bn.elicitator.feedback.Option
import bn.elicitator.feedback.Question

class FeedbackQuestionLoader {

	public void initQuestions() {
		drivingExperience()
		ownCar()
		geography()
		language()
		inPersonOrOnline()
		jargon()
		helpSection()
		wereYouFrustrated()
		whatMadeYouParticipate()
	}

	private void drivingExperience() {
		Question license = new Question( "Do you/have you ever held a valid drivers license?" )
		Option yes = license.addToOptions( "Yes" )
		license.addToOptions( "No" )
		license.save( flush : true, failOnError : true )

		Question howLong = new Question( "How long have you held/did you hold this for?", yes )
		howLong.addToOptions( [ "Less than 1 year", "1 to 4 years", "5 to 10 years", "Longer than 10 years" ] )
		howLong.save( flush : true, failOnError : true )
	}

	private void ownCar() {
		Question own = new Question( "Do you currently own a car?" )
		Option yes = own.addToOptions( "Yes" )
		own.addToOptions( "No" )
		own.save( flush : true, failOnError : true )

		Question insurance = new Question( "How have you insured it?", yes )
		insurance.addToOptions( [ "Not insured", "3rd party", "Comprehensive" ] )
		insurance.save( flush : true, failOnError : true );
	}

	private void whatMadeYouParticipate() {
		new Question( "Why did you participate in this survey?" ).save( flush : true, failOnError : true )
	}

	private void inPersonOrOnline() {
		Question question = new Question( "Would you prefer to answer questions via:" )
		question.addToOptions( [ "An online survey", "An interview with the researcher", "Discussion with other participants at a workshop" ] )
		question.save( flush : true, failOnError : true )
	}

	private void geography() {
		Question q = new Question( "Where do you live?" )
		Option aus   = q.addToOptions( "Australia" )
		Option other = q.addToOptions( "Other country" )
		q.save( flush : true, failOnError : true )

		Question state = new Question( "Which state/territory?", aus )
		state.addToOptions( [ "Vic", "NSW", "QLD", "NT", "WA", "SA", "Tas", "ACT" ] )
		state.save( flush : true, failOnError : true )

		new Question( "What country do you live in?", other ).save( flush : true, failOnError : true )
	}

	private void wereYouFrustrated() {
		Question q = new Question( "Were you at any stage frustrated completing the survey?" )
		Option yes = q.addToOptions( "Yes" )
		q.addToOptions( "No" )
		q.save( flush : true, failOnError : true )

		new Question( "Describe what caused the frustration.", yes ).save( flush : true, failOnError : true )
	}

	private void language() {
		Question language = new Question( label : "Is English your first language?" )
		language.addToOptions( "Yes" )
		Option notEnglish = language.addToOptions( "No" )
		language.save( flush : true, failOnError : true )

		Question otherLanguage = new Question( "What is your first language?", notEnglish )
		otherLanguage.save( flush : true, failOnError : true )
	}

	private void jargon() {
		Question jargon = new Question( label : "Were there any words or jargon used that you did not understand?" )
		Option yesJargon = jargon.addToOptions( "Yes" )
		jargon.addToOptions( "No" )
		jargon.save( flush : true, failOnError : true )

		new Question( "Describe any problems you experienced with jargon", yesJargon ).save( flush : true, failOnError : true )
	}

	private void helpSection() {
		Question question = new Question( "Did you visit the help section at any point?" )
		Option neededHelp = question.addToOptions( "Yes" )
		question.addToOptions( "No" )
		Option didntKnow  = question.addToOptions( "I didn't know help was available" )
		question.save( flush : true, failOnError : true )

		new Question( "What did you require help with?", neededHelp ).save( flush : true, failOnError : true )

		Question wantedButDidntUse = new Question( "Did you require help with anything?", didntKnow )
		Option yes = wantedButDidntUse.addToOptions( "Yes" )
		wantedButDidntUse.addToOptions( "No" )
		wantedButDidntUse.save( flush : true, failOnError : true )

		new Question( "What did you require help with?", yes ).save( flush : true, failOnError : true )
	}

}
