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

/**
 * Implementations of this interface are capable of receiving data changes
 * of a {@link java.util.Map Map} data-structure.
 *
 * @author Philipp Eichhorn
 */
public interface IMapObserver<KEY_TYPE, VALUE_TYPE> {

	public void valueChanged(IObservableMap<KEY_TYPE, VALUE_TYPE> map, KEY_TYPE key, VALUE_TYPE lastValue);

	public void valueAdded(IObservableMap<KEY_TYPE, VALUE_TYPE> map, KEY_TYPE key);

	public void valueRemoved(IObservableMap<KEY_TYPE, VALUE_TYPE> map, KEY_TYPE key, VALUE_TYPE value);
}
