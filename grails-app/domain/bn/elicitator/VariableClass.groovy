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

import org.apache.commons.lang.StringUtils
import org.apache.commons.lang.builder.EqualsBuilder
import org.apache.commons.lang.builder.HashCodeBuilder

/**
 * Variable classes have been defined by Kjærulff, U. B. & Madsen, A. L. (Chapter 6, page 150).
 */
class VariableClass {

	static hasMany = [
		potentialChildren: VariableClass
	]

	static constraints = {
		readableLabel( nullable: true )
	}

	String name;

	String readableLabel;

	Integer priority = 1;

	List<VariableClass> potentialChildren = []

	String toString() { return readableLabel ?: name }

	/**
	 * Capitalized version of the name.
	 */
	String getNiceName()
	{
		return StringUtils.capitalize( name );
	}

	// http://stackoverflow.com/questions/27581/overriding-equals-and-hashcode-in-java/27609#27609
	boolean equals( Object obj ) {
		if ( obj == null ) {
			return false
		}
		if ( !( obj instanceof VariableClass ) ) {
			return false
		}
		VariableClass rhs = (VariableClass)obj;
		new EqualsBuilder().append( name, rhs.name ).isEquals()
	}

	int hashCode() {
		new HashCodeBuilder( 5, 7 ).append( name ).hashCode()
	}

	static final BACKGROUND = "background";
	static final MEDIATING = "mediating";
	static final PROBLEM = "problem";
	static final SYMPTOM = "symptom";

	static VariableClass getBackground() { return VariableClass.findByName( BACKGROUND ) }
	static VariableClass getMediating() { return VariableClass.findByName( MEDIATING ) }
	static VariableClass getProblem() { return VariableClass.findByName( PROBLEM ) }
	static VariableClass getSymptom() { return VariableClass.findByName( SYMPTOM ) }

}
