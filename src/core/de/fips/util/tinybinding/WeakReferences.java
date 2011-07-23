/*
Copyright © 2010-2011 Philipp Eichhorn.

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

import de.fips.util.tinybinding.weaklistener.WeakListenerWithType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Creates all kinds of useful {@link java.lang.ref.WeakReference WeakReferences}.
 *
 * @author Philipp Eichhorn
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class WeakReferences {

	public static <SUPER_TYPE, TYPE extends SUPER_TYPE> WeakListenerWithType<SUPER_TYPE, TYPE> weakListener(final Class<SUPER_TYPE> listenerType, final TYPE listener) {
		return new WeakListenerWithType<SUPER_TYPE, TYPE>(listenerType, listener);
	}
}
