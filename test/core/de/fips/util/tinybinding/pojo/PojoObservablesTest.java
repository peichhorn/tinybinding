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
package de.fips.util.tinybinding.pojo;

import static de.fips.util.tinybinding.util.Cast.uncheckedCast;
import static org.fest.assertions.Assertions.assertThat;
import static de.fips.util.tinybinding.Observables.observe;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import org.junit.Before;
import org.junit.Test;

import de.fips.util.tinybinding.IObservableValue;
import de.fips.util.tinybinding.IValueObserver;

/**
 * Tests the {@link PojoObservables}.
 *
 * @author Philipp Eichhorn
 */
public class PojoObservablesTest {
	private TestPojo1 pojo1;
	private TestPojo2 pojo2;

	@Before
	public void setUp() throws Exception {
		pojo1 = new TestPojo1();
		pojo2 = new TestPojo2();
	}

	@Test
	public void test_observeValue() {
		IObservableValue<String> text = observe(pojo1).property("text", String.class);
		IValueObserver<String> observer = uncheckedCast(mock(IValueObserver.class));
		text.addObserver(observer);
		verify(observer, times(1)).valueChanged(any(String.class), eq((String) null));
		text.set("42");
		assertThat(pojo1.getText()).isEqualTo("42");
	}
	
	@Test
	public void test_observeValue_primitive() {
		IObservableValue<Boolean> bool = observe(pojo1).property("bool", Boolean.class);
		IValueObserver<Boolean> observer = uncheckedCast(mock(IValueObserver.class));
		bool.addObserver(observer);
		verify(observer, times(1)).valueChanged(any(Boolean.class), eq((Boolean) null));
		bool.set(false);
		assertThat(pojo1.isBool()).isFalse();
	}

	@Test
	public void test_observeValue_setter_withoutPropertyChangeSupport() {
		IObservableValue<String> text = observe(pojo1).property("text", String.class);
		IValueObserver<String> observer = uncheckedCast(mock(IValueObserver.class));
		text.addObserver(observer);
		pojo1.setText("42");
		verify(observer, times(0)).valueChanged(eq("42"), any(String.class));
	}

	@Test
	public void test_observeValue_setter_withPropertyChangeSupport() {
		IObservableValue<String> text = observe(pojo2).property("text", String.class);
		IValueObserver<String> observer = uncheckedCast(mock(IValueObserver.class));
		text.addObserver(observer);
		pojo2.setText("42");
		verify(observer, times(1)).valueChanged(eq("42"), any(String.class));
	}

	@Data
	public static class TestPojo1 {
		private String text;
		private boolean bool;
	}

	@NoArgsConstructor
	public static class TestPojo2 {
		@Getter
		private String text;
		private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

		public void addPropertyChangeListener(final String propertyName,
				final PropertyChangeListener listener) {
			propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
		}

		public void setText(final String text) {
			propertyChangeSupport.firePropertyChange("text", this.text, this.text = text);
		}
	}
}
