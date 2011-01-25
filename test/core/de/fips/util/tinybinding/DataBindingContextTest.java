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

import static de.fips.util.tinybinding.Observables.observe;
import static org.fest.assertions.Assertions.assertThat;

import javax.swing.JFrame;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests the {@link DataBindingContext}.
 * 
 * @author Philipp Eichhorn
 */
public class DataBindingContextTest {
	private JFrame frame;
	private IObservableValue<String> gui;
	private IObservableValue<String> model;
	private DataBindingContext context;

	@Before
	public void setUp() {
		frame = new JFrame();
		gui = observe(frame).title();
		model = ObservableValue.nil();
		context = new DataBindingContext();
	}

	@After
	public void tearDown() {
		context.unbind(gui, model);
	}

	/**
	 * GUI and model update(synchronize) each other
	 */
	@Test
	public void test_bind_autoUpdate() {
		assertThat(model.get()).isNull();
		assertThat(gui.get()).isNotNull();
		context.bind(gui, model);
		assertThat(model.get()).isNotNull().isEqualTo(gui.get());
		frame.setTitle("Good Title");
		assertThat(gui.get()).isEqualTo("Good Title");
		assertThat(model.get()).isEqualTo("Good Title");
		model.set("Even Better Title");
		assertThat(gui.get()).isEqualTo("Even Better Title");
		assertThat(model.get()).isEqualTo("Even Better Title");
	}

	/**
	 * GUI updates model, but not the other way around
	 */
	@Test
	public void test_bind_customUpdate() {
		assertThat(model.get()).isNull();
		assertThat(gui.get()).isNotNull();
		context.bind(gui, model, new UpdateStrategy<String, String>(), null);
		assertThat(model.get()).isNotNull().isEqualTo(gui.get());
		frame.setTitle("Good Title");
		assertThat(gui.get()).isEqualTo("Good Title");
		assertThat(model.get()).isEqualTo("Good Title");
		model.set("Even Better Title");
		assertThat(model.get()).isEqualTo("Even Better Title");
		assertThat(gui.get()).isEqualTo("Good Title");
		assertThat(frame.getTitle()).isEqualTo("Good Title");
	}
}
