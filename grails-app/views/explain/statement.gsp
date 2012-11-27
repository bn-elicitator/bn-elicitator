<%@ page contentType="text/html;charset=UTF-8" %>
<html xmlns="http://www.w3.org/1999/html" xmlns="http://www.w3.org/1999/html" xmlns="http://www.w3.org/1999/html">
	<head>
		<meta name="layout" content="main">
		<title>Explanatory Statement</title>
	</head>

	<body>

		<div id="explanatory-statement">

			<g:top />
			<h1>Explanatory Statement</h1>

			<g:if test="${flash.mustCheckRead}">
				<div class="errors">
					Must check the 'I have read and understood this explanatory statement' checkbox before continuing.
				</div>
			</g:if>

			<g:form action="consent">
				<label>
					<input type="checkbox" name="readStatement" value="1"/>
					I have read and understood this explanatory statement.
				</label>

				<br /><br />

				<input id="continue" disabled="disabled" type="submit" class="" value="Continue" onclick="document.location = '${createLink( controller: 'explain', action: 'consent' )}'" />
			</g:form>

			<g:javascript>
				(function() {
					$( 'input[name=readStatement]').change( function() {
						$( "#continue").prop( 'disabled', !$( "input[name=readStatement]" ).prop( 'checked' ) );
					});
				})();
			</g:javascript>

			<br />
			<br />

			<ul id="toc" class="">
				<g:javascript>
					(function(){
						$( 'h2').each( function( i, item ) {
							var safeString = $( item ).text().replace( /[^a-zA-Z0-9]+/g, '' );
							$( item ).before( '<a name="' + safeString + '"></a>' );
							$( item ).after( "<span class='back-to-top'><a href='#top'>(back to top)</a></span>" );
							$( '#toc' ).append( '<li><a href="#' + safeString + '">' + $( item).text() + '</a></li>' );
						});
					})();
				</g:javascript>
			</ul>

			${explanatoryStatement}

		</div>

	</body>
</html>