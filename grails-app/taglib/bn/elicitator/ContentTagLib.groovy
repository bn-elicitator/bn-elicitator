/*
 * Bayesian Network (BN) Elicitator
 * Copyright (C) 2013 Peter Serwylo (peter.serwylo@monash.edu)
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

package bn.elicitator

class ContentTagLib {

	static namespace = "bnContent"

	/**
	 * @attr page REQUIRED
	 * @attr class
	 * @attr id
	 */
	def link = { attrs, body ->

		String alias
		String classNames = ""
		String id         = null
		Object page       = attrs.page

		if ( page instanceof ContentPage ) {
			alias = page.alias
		} else {
			alias = page.toString()
		}

		if ( attrs.containsKey( 'class' ) ) {
			classNames = attrs.remove( 'class' )
		}

		if ( attrs.containsKey( 'id' ) ) {
			id = attrs.remove( 'id' )
		}

		out << g.link( [ controller : 'contentView', elementId : id, params : [ page : alias ], class : classNames ], body )
	}
	
	
	/**
	 * @attr label REQUIRED
	 * @attr class
	 * @attr id
	 */
	def editLink = { attrs, body ->

		ContentPage page  = null
		String classNames = ""
		String id         = null

		if ( attrs.containsKey( 'class' ) ) {
			classNames = attrs.remove( 'class' )
		}

		if ( attrs.containsKey( 'id' ) ) {
			id = attrs.remove( 'id' )
		}

		if ( !attrs.containsKey( 'label' ) ) {
			throw new Exception( 'Missing required attribute "label"' )
		} else {
			String label = attrs.remove( 'label' );
			page = ContentPage.findByAlias( label )
			if ( !page ) {
				throw new Exception( "Couldn't find page with label \"$label\"" )
			}
		}

		out << g.link( [ controller : 'contentEdit', elementId : id, params : [ id : page.id ], class : classNames ], body )
	}
	
}
