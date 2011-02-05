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
package de.fips.util.tinybinding.swing;

import static de.fips.util.tinybinding.util.Cast.uncheckedCast;

import java.awt.Color;
import java.awt.Container;
import java.awt.Rectangle;

import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.text.JTextComponent;

import lombok.RequiredArgsConstructor;
import de.fips.util.tinybinding.IObservableValue;
import de.fips.util.tinybinding.ObservableValue;

/**
 * Creates {@link ObservableValue} for Swing Components.
 *
 * @author Philipp Eichhorn
 */
@RequiredArgsConstructor
public final class SwingObservable {
	private final Container source;

	public IObservableValue<Color> background() {
		return new ObservablePropertyValue<Color>("background", Color.class, source);
	}

	public IObservableValue<Rectangle> bounds() {
		return new ObservableBoundsValue(source);
	}

	public IObservableValue<Boolean> editable() {
		return new ObservablePropertyValue<Boolean>("editable", Boolean.class, source);
	}

	public IObservableValue<Boolean> enabled() {
		return new ObservablePropertyValue<Boolean>("enabled", Boolean.class, source);
	}

	public IObservableValue<Boolean> selected() {
		return new ObservablePropertyValue<Boolean>("selected", Boolean.class, source);
	}

	public IObservableValue<Boolean> focus() {
		return new ObservableFocusValue(source);
	}

	public IObservableValue<Color> foreground() {
		return new ObservablePropertyValue<Color>("foreground", Color.class, source);
	}

	public IObservableValue<String> title() {
		return new ObservablePropertyValue<String>("title", String.class, source);
	}

	public IObservableValue<String> text() {
		if (source instanceof JTextComponent) {
			return new ObservableDocumentValue((JTextComponent) source);
		} else {
			return new ObservablePropertyValue<String>("text", String.class, source);
		}
	}

	public IObservableValue<String> tooltip() {
		return new ObservablePropertyValue<String>("ToolTipText", String.class, source);
	}

	public <T> IObservableValue<T> value() {
		if (source instanceof JComboBox) {
			return new ObservableComboBoxValue<T>((JComboBox) source);
		} else if (source instanceof JList) {
			return new ObservableListValue<T>((JList) source);
		} else {
			Class<T> clazz = uncheckedCast(Object.class);
			return new ObservablePropertyValue<T>("value", clazz, source);
		}
	}

	public IObservableValue<Boolean> visible() {
		return new ObservableVisibleValue(source);
	}
}