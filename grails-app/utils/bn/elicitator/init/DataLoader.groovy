package bn.elicitator.init
/*
 * Bayesian Network (BN) Elicitator
 * Copyright (C) 2012 Peter Serwylo (peter.serwylo@monash.edu)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import bn.elicitator.*
import bn.elicitator.auth.Role
import bn.elicitator.auth.User
import javax.servlet.ServletContext;

abstract class DataLoader {

	/**
	 * The servletContext is passed in so that you can access files from the app, e.g. a text
	 * file with the explanatory statement.
	 * @param servletContext
	 * @return
	 */
	abstract protected AppProperties getProperties( ServletContext servletContext );

	abstract protected List<Variable> getBackgroundVariables()
	abstract protected List<Variable> getProblemVariables()
	abstract protected List<Variable> getMediatingVariables()
	abstract protected List<Variable> getSymptomVariables()

	protected void initOther() {}

	void init( ServletContext servletContext ) {

		if ( !isInitialized() ) {
			initProperties( servletContext )
			initRolesAndAdminUser()
			initVariableClasses()
			initVariables()
			initOther()
		}

		// Performs its own initialization check for each email, so we can add new emails as required...
		initEmailTemplates()
		upgrade()
	}

	private void upgrade() {
		upgradeRoles()
		doUpgrade()
	}

	private void upgradeRoles() {
		Map<String, Role> roles = [
			(Role.ADMIN)     : Role.findByName( 'admin' ),
			(Role.EXPERT)    : Role.findByName( 'expert' ),
			(Role.CONSENTED) : Role.findByName( 'consented' ),
		]
		roles.each { entry ->
			String name = entry.key
			Role   role = entry.value
			if ( role ) {
				role.name = name
				role.save()
			}
		}
	}

	/**
	 * To be overridden by sub classes if required. For example, if they need to trigger some certain behaviour when
	 * started up after a code change.
	 */
	protected void doUpgrade() {

	}

	private void initEmailTemplates()
	{

		EmailTemplate firstPhaseStarting = new EmailTemplate(
			name: EmailTemplate.FIRST_PHASE_STARTING,
			description: "Sent to the users when the survey is about to start.",
			placeholderNames: [ EmailTemplate.PH_EXPECTED_PHASE_DURATION, EmailTemplate.PH_START_DATE ],
			subject: "Survey starting",
			body:
				"This email is to notify you that Round 1 of the first aid survey will be starting on ${EmailTemplate.PH_START_DATE}.\n" +
				"I look forward to your contributions.\n"
		)

		EmailTemplate phaseComplete = new EmailTemplate(
			name: EmailTemplate.PHASE_COMPLETE,
			description: "Phase is complete. Sent after all phases <em>except</em> the last, where the ${EmailTemplate.STUDY_COMPLETE} email is sent instead.",
			placeholderNames: [ EmailTemplate.PH_COMPLETED_PHASE, EmailTemplate.PH_NEXT_PHASE, EmailTemplate.PH_EXPECTED_PHASE_DURATION ],
			subject: "Round [NextPhase] started",
			body:
				"This email is to notify you that we've completed Round ${EmailTemplate.PH_COMPLETED_PHASE} and " +
				"are now starting Round ${EmailTemplate.PH_NEXT_PHASE}."
		)

		EmailTemplate studyComplete = new EmailTemplate(
			name: EmailTemplate.STUDY_COMPLETE,
			subject: "Study completed",
			description: "All rounds are finished. You should add a call to action here, so that they feel there is still good to come of their contributions.",
			body:
				"Thank you so much for taking part in this study. Your contributions are valued.\n" +
				"I will keep in touch to and let you know as soon as the results are in, as well as if there are any publications which arise from this."
		)

		EmailTemplate error = new EmailTemplate(
			name: EmailTemplate.ERROR,
			description: "An internal error occurred. This message will be sent to all admin users.",
			placeholderNames: [ EmailTemplate.PH_ERROR_MESSAGE, EmailTemplate.PH_EXCEPTION_MESSAGE, EmailTemplate.PH_EXCEPTION_STACK_TRACE, EmailTemplate.PH_EXCEPTION_TYPE, EmailTemplate.PH_ERROR_USER ],
			subject: "Error",
			body:
				"[ErrorMessage]\n\n[ExceptionType]: [ExceptionMessage]\n\n[ExceptionStackTrace]"
		)

		List<EmailTemplate> templates = [ firstPhaseStarting, phaseComplete, studyComplete, error ]

		String subjectPrefix = AppProperties.properties.title + ": "
		String header = "Hi [User],\n\n"
		String footer = "\n\n" +
			"Thanks\n" +
			"\n" +
			"---\n" +
			"The survey can be accessed online at [Link].\n" +
			"If you no longer wish to take part in this survey, please visit [UnsubscribeLink]."

		templates.each {
			if ( EmailTemplate.countByName( it.name ) == 0 ) {
				it.subject = subjectPrefix + it.subject
				it.body = header + it.body + footer
				it.save()
			}
		}
	}

	/**
	 * Creates an admin, expert and consented role, then adds an admin user with password "I'm an admin user".
	 */
	private void initRolesAndAdminUser()
	{
		Role adminRole = new Role( name: Role.ADMIN )
		adminRole.save( flush: true, failOnError: true )

		Role expertRole = new Role( name: Role.EXPERT )
		expertRole.save( flush: true, failOnError: true )

		// Once a user has consented, then they are allowed to view the rest of the system...
		Role consentedRole = new Role( name: Role.CONSENTED )
		consentedRole.save( flush: true, failOnError: true )

		User adminUser = new User( realName: "Admin User", email: AppProperties.properties.adminEmail, username: "admin", password: "I'm an admin user" )
		adminUser.save( flush: true, failOnError: true )

		adminRole.addUser( adminUser )
	}

	private void initVariables() {
		removeVariables()
		saveVariables( backgroundVariables, VariableClass.background );
		saveVariables( problemVariables,    VariableClass.problem    );
		saveVariables( mediatingVariables,  VariableClass.mediating  );
		saveVariables( symptomVariables,    VariableClass.symptom    );
	}

	private void removeVariables() {
		Variable.list()*.delete()
	}

	private void saveVariables( List<Variable> vars, VariableClass variableClass )
	{
		User admin = User.findByUsername( 'admin' )
		vars*.variableClass    = variableClass
		vars*.createdBy        = admin
		vars*.createdDate      = new Date()
		vars*.lastModifiedBy   = admin
		vars*.lastModifiedDate = new Date()
		vars*.save()

	}

	private Boolean isInitialized() {
		AppProperties.count() > 0
	}

	private void initProperties( ServletContext servletContext )
	{
		AppProperties props = getProperties( servletContext )
		AppProperties.properties.adminEmail           = props.adminEmail
		AppProperties.properties.url                  = props.url
		AppProperties.properties.title                = props.title
		AppProperties.properties.delphiPhase          = props.delphiPhase
		AppProperties.properties.elicitationPhase     = props.elicitationPhase
		AppProperties.properties.explanatoryStatement = props.explanatoryStatement
		AppProperties.properties.save();
	}

	/**
	 * Helper function if we want to bootstrap a bunch of test users.
	 * @param number
	 * @param hasConsented
	 */
	protected void initTestUsers( int number = 10, boolean hasConsented = true ) {
		Role consented = Role.consented
		Role expert    = Role.expert

		for ( i in 1..number ) {
			String name = "expert" + i
			User user = new User( realName: "Expert " + i, email: AppProperties.properties.adminEmail, username: name, password: name )
			user.save( flush: true )
			expert.addUser( user )
			if ( hasConsented ) {
				consented.addUser( user )
			}
		}
	}

	/**
	 * Initializes the four main variable classes:
	 *  - Background
	 *  - Mediating
	 *  - Problem
	 *  - Symptom
	 * And their relationships among themselves.
	 */
	private void initVariableClasses()
	{
		VariableClass background = new VariableClass( name: VariableClass.BACKGROUND )
		background.save( flush: true )

		VariableClass problem = new VariableClass( name: VariableClass.PROBLEM, potentialParents: [ background ] )
		problem.save( flush: true )

		VariableClass mediating = new VariableClass( name: VariableClass.MEDIATING, potentialParents: [ background, problem ] )
		mediating.save( flush: true )

		VariableClass symptom = new VariableClass( name: VariableClass.SYMPTOM, potentialParents: [ background, mediating, problem ] )
		symptom.save( flush: true )

		background.potentialParents.push( background )
		background.save( flush: true )

	}
}