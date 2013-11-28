/*
 * Copyright 2012 Johns Hopkins University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dataconservancy.query.gqmpsql.lang.parser;


public class CharSequenceParserInput implements ParserInput {
	private final CharSequence input;
	private int next;
	private int mark;
	
	public CharSequenceParserInput(CharSequence input) {
		this.input = input;
		this.next = 0;
		this.mark = -1;
	}
	
	public boolean more() {
		return next < input.length();
	}

	public char next() throws ParseException {
		if (next == input.length()) {
			throw new ParseException(this, "Premature end of input");
		}
		
		return input.charAt(next++);
	}

	public char peek() throws ParseException {
		if (next == input.length()) {
			throw new ParseException(this, "Premature end of input");
		}
		
		return input.charAt(next);
	}

	public void mark() {
		mark = next;
	}

	public void unmark() {
		mark = -1;
	}
	
	public void rewind() {
		next = mark;
	}

	public String marked() {
		return input.subSequence(mark, next).toString();
	}
	
	public String toString() {
		if (next < input.length()) {
			return input.subSequence(0, next) + " >" + input.charAt(next)
			       + "< " + input.subSequence(next + 1, input.length());
		} else {
			return input.toString();
		}
	}
}
