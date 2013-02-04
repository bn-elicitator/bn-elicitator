package bn.elicitator

class HelpTagLib {

	static namespace = "h"

	private static String generateHash( title, message ) {
		title = title.trim()
		message = message.trim()
		return DigestUtils.md5Hex( title + message )
	}

	private static Boolean hasRead( title, message ) {
		String hash = generateHash( title, message )
		return HelpRead.findByMessageHashAndReadBy( hash, ShiroUser.current ) != null
	}

	private static Boolean hasReadId( id ) {
		return hasRead( id, "" )
	}

	/**
	 * @attrs index REQUIRED Relative to other help messages on the page, what order do you want this one to show up?
	 * @attrs id             Uniquely identifies this message. Will be used (instead of message contact) to mark as read.
	 * @attrs title          The title string for the message. Shown above the body and more prominently.
	 * @attrs forId          The ID of a DOM element on the screen.
	 * @attrs forSelector    The ID of a DOM element on the screen.
	 * @attrs location       Either to the "left" or "right" of the DOM element specified by the "for" attribute.
	 * @attrs width          Sets the max-width  proeprty on the overlay (therefore, you should append units to this to, like "500px")
	 * @attrs height         Sets the max-height property on the overlay (therefore, you should append units to this to, like "500px")
	 * @attrs persist        Don't remove the message once viewed. Persist for each page load.
	 */
	def help = { attrs, body ->

		def index       = attrs.index

		String id               = ""
		String helpForSelector  = ""
		String title            = ""
		String location         = ""
		String width            = ""
		String height           = ""
		def    persist          = false

		if ( attrs.containsKey( "id"          ) ) id              = attrs.id
		if ( attrs.containsKey( "forId"       ) ) helpForSelector = "#${attrs.forId}"
		if ( attrs.containsKey( "forSelector" ) ) helpForSelector = attrs.forSelector
		if ( attrs.containsKey( "title"       ) ) title           = attrs.title
		if ( attrs.containsKey( "location"    ) ) location        = attrs.location
		if ( attrs.containsKey( "width"       ) ) width           = attrs.width
		if ( attrs.containsKey( "height"      ) ) height          = attrs.height
		if ( attrs.containsKey( "persist"     ) ) persist         = attrs.persist

		Boolean read = id ? hasReadId( id ) : hasRead( title, body() );
		if ( !( params.containsKey( "showHelp" ) || persist ) && read ) {
			return;
		}

		String pointDirectionClass = ""
		if ( location == "right" ) {
			pointDirectionClass = "direction-left"
		} else if ( location == "left" ) {
			pointDirectionClass = "direction-right"
		} else {
			location = ""
		}

		String style = "style='${width ? "max-width: $width;" : ''} ${height ? "max-height: $height;" : ''}'"
		String globalClass = ( helpForSelector == "" ) ? "global" : ""
		String idAttr = id ? "id='$id'" : ""
		String hash = id ? generateHash( id, "" ) : generateHash( title, body() )

		out << """
			<div $idAttr class='help-overlay $globalClass $pointDirectionClass' $style>
				<input type='hidden' name='index' value='$index' />
				<input type='hidden' name='hash' value='$hash' />
				${helpForSelector  == "" ? "" : "<input type='hidden' name='forSelector' value='$helpForSelector' />"}
				${location         == "" ? "" : "<input type='hidden' name='location' value='$location' />"}
				${location         == "" ? "" : "<div class='pointer'></div>"}
				<div class='title'>$title</div>
				<div class='body'>${body()}</div>
				<div class='button-wrapper'>
					<button class='dismiss'>Dismiss</button>
				</div>
			</div>
			"""
	}

}


import org.apache.commons.codec.digest.DigestUtils
