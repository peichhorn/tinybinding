/*
 * Copyright © 2010-2011 Philipp Eichhorn.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package de.fips.util.tinybinding.junit;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
public class Std implements TestRule {
	private enum Type {
		OUT, ERR;
	}
	
	private final ByteArrayOutputStream content = new ByteArrayOutputStream();
	private final Type type;

	public static Std out() {
		return new Std(Type.OUT);
	}

	public static Std err() {
		return new Std(Type.ERR);
	}

	public String getContent() {
		return content.toString();
	}

	public String getContent(String charsetName) throws UnsupportedEncodingException {
		return content.toString(charsetName);
	}

	public Statement apply(final Statement base, final Description description) {
		return new Statement() {
			@Override
			public void evaluate() throws Throwable {
				final PrintStream oldPrintStream; 
				switch (type) {
				case ERR:
					oldPrintStream = System.err;
					System.setErr(new PrintStream(content));
					break;
				default:
				case OUT:
					oldPrintStream = System.out;
					System.setOut(new PrintStream(content));
				}
				try {
					base.evaluate();
				} finally {
					switch (type) {
					case ERR:
						System.setErr(oldPrintStream);
						break;
					default:
					case OUT:
						System.setOut(oldPrintStream);
					}
				}
			}
		};
	}
}