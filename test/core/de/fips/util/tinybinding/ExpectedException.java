/*
Copyright © 2011 Philipp Eichhorn.

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/
package de.fips.util.tinybinding;

import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

public class ExpectedException implements MethodRule {

	private final org.junit.rules.ExpectedException delegate = org.junit.rules.ExpectedException.none();

	public static ExpectedException none() {
		return new ExpectedException();
	}

	private ExpectedException() {
	}

	@Override
	public Statement apply(Statement base, FrameworkMethod method, Object target) {
		return delegate.apply(base, method, target);
	}

	public void expect(Class<? extends Throwable> type, String message) {
		expect(type);
		expectMessage(message);
	}

	public void expectIllegalArgumentException(String message) {
		expect(IllegalArgumentException.class, message);
	}

	public void expectNullPointerException(String message) {
		expect(NullPointerException.class, message);
	}

	public void expect(Throwable error) {
		expect(error.getClass(), error.getMessage());
	}

	public void expect(Class<? extends Throwable> type) {
		delegate.expect(type);
	}

	public void expectMessage(String message) {
		delegate.expectMessage(message);
	}
}