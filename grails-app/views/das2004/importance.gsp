%{--
  - Bayesian Network (BN) Elicitator
  - Copyright (C) 2013 Peter Serwylo (peter.serwylo@monash.edu)
  -
  - This program is free software: you can redistribute it and/or modify
  - it under the terms of the GNU General Public License as published by
  - the Free Software Foundation, either version 3 of the License, or
  - (at your option) any later version.
  -
  - This program is distributed in the hope that it will be useful,
  - but WITHOUT ANY WARRANTY; without even the implied warranty of
  - MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  - GNU General Public License for more details.
  -
  - You should have received a copy of the GNU General Public License
  - along with this program.  If not, see <http://www.gnu.org/licenses/>.
  --}%
<!doctype html>
<html>

<head>
	<meta name="layout" content="main">
	<title>Which has the greatest influence on ${variable}?</title>

	<g:javascript>
			$( document ).ready( function() {

				var manager = new bn.das2004.Manager( '.comparison', '${createLink( [ action : 'index', params : [ id : variable.id ] ] )}' );

				/**
				 * Figure out the two variables being compared, extract their id's and then figure out
				 * the weights to send to the server.
				 *
				 * For reference, the "name" attribute of the comaprison radio's looks like so:
				 * 	"childId=1,parentOneId=2,parentTwoId=3"
				 */
				var saveComparison = function() {

					var comparison    = manager.currentQuestion();
					var mostImportant = comparison.find( '.most-important input[ type=radio ]:checked' );

					// Example name: "childId=1,parentOneId=2,parentTwoId=3"
					var nameParts = mostImportant.attr( 'name' ).split( "," );
					var data      = {};
					for ( var j = 0; j < nameParts.length; j ++ ) {
						var keyValue = nameParts[ j ].split( "=" );
						data[ keyValue[ 0 ] ] = parseInt( keyValue[ 1 ] );
					}

					data.mostImportantParentId = parseInt( mostImportant.val() );

					if ( data.mostImportantParentId != 0 ) {
						var weight = comparison.find( '.how-much input[ type=radio ]:checked' );
						data.weight = weight.val();
					}

					$.post( '${createLink( [ action : 'ajaxSaveComparison' ] )}', data );
				};

				var mostImportant = $( '.most-important' ).find( 'input' ).button();
				mostImportant.change( function() {
					var id = parseInt( $( this ).val() );
					if ( id == 0 ) {
						// Equally weighted, so don't bother showing them the "How Much More Weighty" question...
						saveComparison();
						manager.nextQuestion();
					} else {
						var howMuch = manager.currentQuestion().find( '.how-much' ).show( 'fast', function() {
							manager.equalizeHeights( manager.currentQuestion().find( '.weights .ui-button' ) );
							bn.utils.ensureTopIsInView( $( this ) );
						});
					}
				});

				var weights = $( '.weights' ).buttonset();
				weights.find( 'input[type=radio]' ).change( function() {
					saveComparison();
					manager.nextQuestion();
				});


			});
	</g:javascript>

	<r:require module="elicitProbabilities" />

</head>

<body>

<div class="elicit-probabilities">

	<fieldset class="default">

		<legend>
			<bn:htmlMessage code="elicit.probabilities.importance.header" />
			(<span id="scenario-number">1</span> of <span id="total-scenarios"></span>)
		</legend>

		<das2004:importance variable="${variable}" />

	</fieldset>

	<div class="button-wrapper">
		<button id="btnBack" type="button" onclick="document.location = '${createLink( action : 'index' )}'">
			Back
		</button>
	</div>

</div>

</body>

</html>
