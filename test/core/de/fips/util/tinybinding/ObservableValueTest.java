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

import static de.fips.util.tinybinding.util.Cast.uncheckedCast;
import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.*;

import org.junit.Test;

/**
 * Tests the {@link ObservableValue}.
 * 
 * @author Philipp Eichhorn
 */
public class ObservableValueTest {
	@Test
	public void test_nil() {
		ObservableValue<Integer> value = ObservableValue.nil();
		assertThat(value.get()).isNull();
	}

	@Test
	public void test_of() {
		ObservableValue<Integer> value = ObservableValue.of(100);
		assertThat(value.get()).isEqualTo(Integer.valueOf(100));
	}

	@Test
	public void test_setValue_changesValue() {
		ObservableValue<Integer> value = ObservableValue.of(100);
		assertThat(value.get()).isEqualTo(Integer.valueOf(100));
		value.set(50);
		assertThat(value.get()).isEqualTo(Integer.valueOf(50));
	}

	@Test
	public void test_setValue_notifiesValueObserver() {
		ObservableValue<Integer> value = ObservableValue.of(100);
		IValueObserver<Integer> observer = uncheckedCast(mock(IValueObserver.class));
		value.addObserver(observer);
		value.set(50);
		verify(observer, times(1)).valueChanged(50, 100);
	}
}