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


public interface ParserInput {
	/**
	 * @return if there is more input
	 */
	public boolean more();

	/**
	 * @return the next character and iterate
	 * @throws ParseException
	 */

	public char next() throws ParseException;
	
	/**
	 * @return the next character without iterating.
	 * @throws ParseException 
	 */
	public char peek() throws ParseException;
	
	/**
	 * Sets the mark to the next character.
	 */
	public void mark();
	
	/**
	 * Clear the mark.
	 */
	public void unmark();
	
	/**
	 * Rewind to mark.
	 */
	public void rewind();
	
	/**
	 * @return string starting at mark and ending at 
	 *         char returned by last next 
	 */
	public String marked();
}
