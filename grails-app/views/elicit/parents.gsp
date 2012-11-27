<%@ page import="bn.elicitator.AppProperties; bn.elicitator.Variable" %>
<!doctype html>
<html>

	<head>
		<meta name="layout" content="main">
		<title>Elicit Parents of Variable</title>

		<script type="text/javascript">
			// Only doing this here because I don't know how to put JavaScript in a TagLib so that it is executed
			// after the javascript in the .gsp.
			var sliderValues = {};
		</script>

		<g:javascript>
		var values = [ 0, 25, 50, 75, 100 ];
		var valueLabels = [ "Not confident", "Somewhat confident", "About 50-50", "Very confident", "Certain" ];

		/**
		 * Sets the human readable value of the slider in a span next to it.
		 * Ideally we would use ticks in the slider itself, however we don't have evenly spaced values, and so
		 * we instead clamp to the closet value from the values array.
		 */
		function setSliderValue( parentLabel, value ) {
			var sliderDiv = $( '#' + parentLabel + '-confidence-slider' );
			sliderDiv.slider( 'value', value );
			$( '#potential-parents-list' ).find( 'input[name=' + parentLabel + '-confidence]' ).val( value );

			var minDistance = 1000;
			var smallestIndex = -1;
			for ( var i = 0; i < values.length; i ++ ) {
				var dist = Math.abs( value - values[ i ] );
				if ( dist < minDistance ) {
					minDistance = dist;
					smallestIndex = i;
				}
			}
			if ( smallestIndex != -1 ) {
				$( '#' + parentLabel + '-details' ).find( '.confidence-label' ).html( valueLabels[ smallestIndex ] );
			}
		}

        /**
        * Keep track of all of the dialogs here, indexed by their parent label. We store them here because we'll be
        * detatching them from the DOM (rather than removing them completely), and don't want to lose them when we do so.
        * @type {Object}
        */
		var detailsDivs = {};

		/**
		 * The currently showing variable dialog...
		 * @type {string}
		 */
		var currentVariable = null;

		/**
		 * If they do any sort of modification to a variable (e.g. check the relationship checkbox, change the confidence
		 * slider, or modify their reason), we store it here until they hit save. This allows us to both add visual feedback
		 * to the screen and also prevent them clicking the 'finished' button down the bottom.
		 * @type {Array}
		 */
		var unsavedVariables = [];

		function onFinish() {
			if ( unsavedVariables.length > 0 )
			{
				var plural = unsavedVariables.length == 1 ? '' : 's';
				alert( "Cannot finish yet.\n\nYou still have " + unsavedVariables.length + " unsaved variable" + plural + ".\n\nClick 'Show Details' next to the variables with a red exclamation mark, then save the details." );
			}
			else
			{
				document.location = '${createLink( action: 'completedVariable', params: [ variable: "${variable.label}" ] )}';
			}
		}

		$( document ).ready( function() {

			var potentialParentsList = $( '#potential-parents-list' );

            /*
             * Setup sliders for each slider in the form. We add a listener which updates the span that shows an English
             * representation of the value (e.g. somewhat confident).
             */
			potentialParentsList.find( '.slider' ).slider({
				range: 'min',
				slide: function( event, ui ) {
					setSliderValue( $( this ).attr( 'id' ).split( '-' ).shift(), ui.value );
				}
			});

            /**
             * The sliderValues variable was populated by each variable (when it was being rendered by the taglib), and
             * it is now time to take those values and set the labels appropriately.
             */
			for ( var variable in sliderValues )
			{
			    if ( sliderValues.hasOwnProperty( variable ) )
			    {
				    setSliderValue( variable, sliderValues[ variable ] );
				}
			}

			potentialParentsList.find( 'input[class=potential-parent]' ).change( onToggleListCheckbox );

			$( '#inputNewVariableLabel' ).autocomplete( {
				source: "<g:createLink action='getVariablesFromOntology' controller='data'/>",
				minLength: 2,
				select: function( event, ui ) {
					console.log( ui );
				}
			});

			// There are several 'save' buttons (two per form, and two 'save and hide') per form...
			var btnHolders = $( '.save-wrapper' );
			btnHolders.find( 'button.save' ).click( onSave );
			btnHolders.find( 'button.close' ).click( hideVarDetails );

			$( '.var-details' ).detach().each( function( i, item ) {
				var label = item.id.substring( 0, item.id.length - '-details'.length );

				$( item ).find( '#input-' + label + '-form' ).change( onToggleFormCheckbox );
				$( item ).find( 'textarea[name=comment]' ).change( function(){ markUnsaved( label ); } );
				$( item ).find( '#' + label + '-confidence-slider' ).bind( 'slide', function() { markUnsaved( label ); } );

				detailsDivs[ label ] = item;

				if ( label == 'Age' )
				{
					console.log( i + " - " + label );
				}
			});

			// Add listeners to anything which signifies change, so that we can mark them as unsaved...

			function onChange( variable )
			{

			}

			var itemsToHide = $( 'li.hidden' );
			if ( itemsToHide.length > 0 )
			{
				var numUnused = itemsToHide.length;
				var plural = numUnused == 1 ? '' : 's';

				$( '#unused-items-tooltip' ).show();
				var btn = $( '#unused-items-btn' );
				btn.show();
				btn.val( btn.val().replace( "[NumUnused]", numUnused ) );

				btn.click( function() {

					if ( btn.val().substr( 0, 4 ) == 'Show' )
					{
						btn.val( btn.val().replace( 'Show', 'Hide' ) );
						itemsToHide.show( 'fast' );
					}
					else
					{
						btn.val( btn.val().replace( 'Hide', 'Show' ) );
						itemsToHide.hide( 'fast' );
					}

				});
			}

			$( '.unsaved-icon' ).click( function() {
				alert( 'This variable has unsaved changes.\n\nClick "Show Details" and save them before continuing.' )
			})

		});

		/**
		 * Updates the summary string (in the variables li) which tells the user if they agree with others or not.
		 * We call this after returning from the server after a successful save.
		 * @param variable
		 * @param agree
		 */
		function setAgree( variable, agree ) {
			var li = $( '#' + variable + '-variable-item' );
			if ( agree )
			{
				li.removeClass( 'disagree' );
			}
			else
			{
				li.addClass( 'disagree' );
			}
		}

		/**
		 * Adds a new comment to the top of the list of comments for the dialog belonging to 'variable'.
		 * The comment will be styled differently if the relationship is stated as existing or not.
		 * @param variable
		 * @param exists
		 * @param comment
		 */
		function addComment( variable, exists, comment ) {

			var dialog = detailsDivs[ variable ];
			var list = $( dialog ).find( '.reasons-list' );
			var phaseClass = 'phase-${AppProperties.properties.delphiPhase}';

			var li = list.find( '.me.' + phaseClass );
			if ( comment != null && comment.length == 0 )
			{
				if ( li.length > 0 )
				{
					li.remove();
				}

				if ( list.children().length > 0 )
				{
					list.children().remove();
				}
			}
			else
			{
				$( dialog ).find( '.no-reasons' ).remove();

				var liContents = '"' + comment + '"<div class="author"> - Myself</div>';
				if ( li.length > 0 )
				{
					li.removeClass( 'exists doesnt-exist' );
					li.addClass( exists ? 'exists' : 'doesnt-exist' );
					li.html( liContents );
				}
				else
				{
					var classes = [ 'me', phaseClass ];
					classes.push( exists ? 'exists' : 'doesnt-exist' );
					list.prepend( '<li class="' + classes.join( ' ' ) + '">' + liContents + '</li>' );
				}
			}

		}

		/**
		 * Collects data from the form and posts it via AJAX to the server.
		 * On returning, we will check the resulting JSON data to see if the server thinks we agree or disagree
		 * with others.
		 * @param event
		 * @return {Boolean}
		 */
		function onSave( event ) {

			var button = $( event.target );
			var dialog = $( '#var-details-dialog' );
			var parent = dialog.find( 'input:checkbox[name=parents]' ).val();
			var comment = dialog.find( 'textarea[name=comment]' ).val();
			var exists = dialog.find( 'input:checkbox[name=parents]' ).prop( 'checked' );
			var confidence = $( '#' + parent + '-confidence-slider' ).slider( 'option', 'value' );

			if ( exists && confidence <= 0 )
			{
				alert( "You have not specified how confident you are in this relationship. Please drag the slider below the 'I think it does' checkbox to indicate how confident you are." );
			}
			else
			{
				var allButtonsInDialog = dialog.find( 'button.save' );
				allButtonsInDialog.html( 'Saving...' ).prop( 'disabled', true );

				$.ajax({

					type: 'post',

					url: '${createLink( action: 'save' )}',

					data: {
						child: '${variable.label}',
						parent: parent,
						confidence: confidence,
						comment: comment,
						exists: exists
					},

					dataType: 'text json',

					success: function( data ) {

						markSaved( parent );

						allButtonsInDialog
							.prop( 'disabled', false )
							.html( "Save" )
							.filter( ".and-hide" )
								.html( "Save and Hide" );

						<g:if test="${delphiPhase > 1}">

							setAgree( parent, data.agree );

						</g:if>

						addComment( parent, exists, comment );
					}
				});
			}

			return false;

		}

		/**
		 * Show the details form for the variable which the li belongs to.
		 * Then, make sure that the confidence slider is toggled appropraitely.
		 * @param event
		 */
        function onToggleListCheckbox( event ) {

            var checked = $( event.target ).prop( 'checked' );
            var label = $( event.target ).attr( 'value' );

            showVarDetails( label );
			toggleRelationship( label, checked );

        }

        /**
         * Apart from toggling the appropriate stuff in the form (i.e. confidence slider), we will also
		 * @param event
		 */
        function onToggleFormCheckbox( event ) {

            var checked = $( event.target ).prop( 'checked' );
            var label = $( event.target ).attr( 'value' );

        	toggleRelationship( label, checked );

        }

		/**
		 * Takes a variable and adds it to the unsavedVariables array (if it isn't already in there).
		 * @param variableLabel
		 */
		function markUnsaved( variableLabel ) {

			if ( unsavedVariables.indexOf( variableLabel ) == -1 )
			{
				unsavedVariables.push( variableLabel );
				$( '#' + variableLabel + '-variable-item' ).addClass( 'unsaved' );
			}

		}

		function markSaved( variableLabel ) {

			var index = unsavedVariables.indexOf( variableLabel );
			if ( index >= 0 )
			{
				unsavedVariables.splice( index, 1 );
				$( '#' + variableLabel + '-variable-item' ).removeClass( 'unsaved' );
			}

		}

		function isUnsaved( variableLabel ) {
			return ( unsavedVariables.indexOf( variableLabel ) != -1 );
		}

        /**
         * Show or hide the confidence slider for a particular relationship (it lives in the details form for that variable).
		 * @param variableLabel
		 * @param hasRelationship
		 */
        function toggleRelationship( variableLabel, hasRelationship ) {

			markUnsaved( variableLabel );

			var item = $( '#' + variableLabel + '-variable-item' );
            var detailsForm = $( detailsDivs[ variableLabel ] );

            var listCheckbox = $( '#input-' + variableLabel );
            var formCheckbox = $( '#input-' + variableLabel + '-form' );

            if ( hasRelationship ) {

                // Purposly set this to '' rather than 0, so that we can tell if they didn't actually
                // touch the slider at all at submission time...
                item.find( 'input[name=' + variableLabel + '-confidence]' ).val( '' );

                detailsForm.find( '.confidence-label' ).html( 'How confident are you?' );
                detailsForm.find( '.var-confidence-slider' ).slider( 'value', 0 );
                detailsForm.find( '.my-confidence' ).removeClass( 'hidden' );

                listCheckbox.prop( 'checked', true );
                formCheckbox.prop( 'checked', true );

            } else {

                detailsForm.find( '.my-confidence' ).addClass( 'hidden' );

                listCheckbox.prop( 'checked', false );
                formCheckbox.prop( 'checked', false );

            }

        }

        /**
        * Find the dialog with the form in it, detach anything which was previously in it, and then append the form
        * which belongs to parentLabel. We then set the location of the form appropriately (aligned with the list item
        * that belongs to the parent), and then shows it with an animation.
        * @param parentLabel
        */
		function showVarDetails( parentLabel ) {

			currentVariable = parentLabel;

			// Need to move the dialog before showing, or it doesn't work...
			var dialog = $( '#var-details-dialog' );
			var offset = $( '#' + parentLabel + '-variable-item' ).offset().top - dialog.parent().offset().top;
			var contents = dialog.find( '.contents' );
			contents.children().detach();
			contents.append( detailsDivs[ parentLabel ] );
			dialog.find( 'legend' ).html( 'Does ' + parentLabel + '<br />influence ${variable.readableLabel}?' );
			dialog.css( 'padding-top', offset + 'px' );
			dialog.show( 'fast' );

		}

		function hideVarDetails() {

			var safe = true;

			if ( isUnsaved( currentVariable ) )
			{
				safe = confirm( 'You have unsaved changes about this variable. Close anyway?' );
			}

			if ( safe )
			{
				$( '#var-details-dialog' ).hide( 'fast' );
				currentVariable = null;
			}

		}

		function toggleAddVariable( show ) {
			var form = $( '#new-var-form' );
			show ? form.show( 'fast' ) : form.hide( 'fast' );
		}

		function finish() {
			document.location = '${createLink( action: 'finished' )}';
		}

		function showDescription( description ) {
			alert( description );
		}

		<shiro:hasRole name="admin">
		function download() {
			document.location = '${createLink( controller: 'bn', action: 'download' )}';
		}
		</shiro:hasRole>

		</g:javascript>

		<r:require module="elicit" />

	</head>
	
	<body>

		<div class="elicit-parents">

			<div class="column-wrapper">

				<div class="column-left">

					<input type="hidden" name="currentVar" value="${variable.label}" />
					<input type="hidden" name="nextVar" value="" />
					<input type="hidden" name="isFinished" value="" />
					<input type="hidden" name="downloadBn" value="0" />

					<fieldset class="default ">

						<legend>
							${variable.readableLabel} <g:variableDescription var="${variable}" />
						</legend>

						<p>
							${variable.usageDescription.replace( '\n', '<br />' )}
						</p>
						<br />


						<input id="unused-items-btn" class="hidden" style="margin-bottom: 0.3em" type="button" value="Show [NumUnused] unused items" />
						<g:tooltip id="unused-items-tooltip" classes="hidden">Last round, everybody agreed that some variable had no influence on ${variable.readableLabel}, so they are hidden by default.</g:tooltip>


						<ul id="potential-parents-list" class="variable-list ">

							<g:potentialParentsList potentialParents="${potentialParents}" child="${variable}" />

							<li id="add-variable-item" class=" variable-item new-var">

								<a href="javascript:toggleAddVariable( true )">
									<img src='${resource(dir: 'images/icons', file: 'pencil.png')}' />
									Add another variable which is not listed
								</a>

								<div id="new-var-form" class="dialog" style="display: none;">

									%{--
										I guess if we're getting picky, we really shouldn't be here (because we
										can't elicit parents for variables with no potential parents
									--}%
									<g:if test="${variable.variableClass.potentialParents.size() == 0}">

										<p>
											Sorry, but because ${variable.readableLabel} is a ${variable.variableClass.name}
											variable, we wont be modelling any other variables which influence it.
										</p>

										<input type="button" value="Okay" onclick="toggleAddVariable( false )" class="" />

									</g:if>
									<g:else>

										<g:form action="addVariable">

											<g:hiddenField name="returnToVar" value="${variable.label}" />

											<label for="inputNewVariableLabel">Name:</label>
											<g:textField id="inputNewVariableLabel" name="label" />

											<g:if test="${variable.variableClass.potentialParents.size() == 1}">

												%{-- We don't need to ask if there is only one possibility --}%
												<g:hiddenField name="variableClassName" value="${variable.variableClass.potentialParents[ 0 ].name}" />

											</g:if>
											<g:else>

												<label for="newVarClass">Type</label>
												<select id="newVarClass" name="variableClassName">
													<g:each in="${variable.variableClass.potentialParents}" var="${variableClass}">
														<option value="${variableClass.name}">${variableClass.niceName} variable</option>
													</g:each>
												</select>

												<g:tooltip>This helps us decide which other variables your new one will be allowed to influence. We will describe them using examples from a model of diagnosing lung cancer:

 - Problem Variables: The variables of interest (e.g. does the patient have cancer?).

 - Background variables: Information available before the problem variables occur (e.g. does the patient smoke?).

 - Symptom variables: Observable consequences of problem variables (e.g. shortness of breath).

 - Mediating variables: unobservable variables which may also cause the same symptoms as the problem variables (e.g. are they asthmatic?). This helps to correctly model the relationship between problem and symptom variables</g:tooltip>

											</g:else>

											<label for="newVarDescription">Description:</label>
											<g:textArea id="newVarDescription" name="description"></g:textArea>

											<input type="button" value="Cancel" onclick="toggleAddVariable( false )" class="" />
											<input type="submit" value="Save" class="" />

										</g:form>

									</g:else>
								</div>

							</li>

						</ul>
					</fieldset>

					<input type="button" style="margin-top: 5px;" value="Finished with ${variable.readableLabel}" class="big " onclick="onFinish()"/>

				</div>

				<div class="column-right">

					<!--<img id="bn-image" src="${createLink( controller: 'data', action: 'displaySnippet', params: [ 'for': variable.label ] ) }" />-->

					<div id="var-details-dialog" class="" style="display: none;">

						<fieldset class="default ">

							<legend class="">Details</legend>

							<div class='contents'>

							</div>

						</fieldset>

					</div>

				</div>

				<div class="column-footer">

				</div>

			</div>

		</div>

	</body>
	
</html>