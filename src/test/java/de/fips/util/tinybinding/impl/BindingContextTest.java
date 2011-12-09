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
package de.fips.util.tinybinding.impl;

import static de.fips.util.tinybinding.Bindings.bind;
import static de.fips.util.tinybinding.Observables.observe;
import static org.fest.assertions.Assertions.assertThat;
import static org.fest.swing.edt.GuiActionRunner.execute;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JTextField;

import org.fest.swing.edt.GuiQuery;
import org.fest.swing.fixture.FrameFixture;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import de.fips.util.tinybinding.IBindingContext;
import de.fips.util.tinybinding.IObservableValue;
import de.fips.util.tinybinding.junit.FailOnThreadViolation;

/**
 * Tests {@link BindingContext}.
 */
@RunWith(JUnit4.class)
public class BindingContextTest {
	@ClassRule
	public static final FailOnThreadViolation checkThreadViolation = new FailOnThreadViolation();

	private FrameFixture window;
	private IObservableValue<String> gui;
	private IObservableValue<String> model;
	private IBindingContext context;

	@Before
	public void setUp() throws Exception {
		window = new FrameFixture(execute(new GuiQuery<JFrame>() {
			protected JFrame executeInEDT() {
				final JTextField textBox = new JTextField();

				final JFrame frame = new JFrame();
				frame.setLayout(new BorderLayout());
				frame.getContentPane().add(textBox, BorderLayout.CENTER);
				frame.pack();
				return frame;
			}
		}));
		window.show();

		context = new BindingContext();
		model = observe().nil();
		gui = observe(window.textBox().component()).text();
		window.robot.waitForIdle();
	}

	@After
	public void tearDown() throws Exception {
		context.unbind(gui, model);
		window.cleanUp();
	}

	/**
	 * GUI and model update(synchronize) each other
	 */
	@Test
	public void test_bind_autoUpdate() {
		assertThat(model.get()).isNull();
		assertThat(gui.get()).isNotNull();
		bind(gui).to(model).in(context);
		assertThat(model.get()).isNotNull().isEqualTo(gui.get());
		window.textBox().setText("Good Title");
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
		bind(gui).to(model).updateTarget().in(context);
		assertThat(model.get()).isNotNull().isEqualTo(gui.get());
		window.textBox().setText("Good Title");
		assertThat(gui.get()).isEqualTo("Good Title");
		assertThat(model.get()).isEqualTo("Good Title");
		model.set("Even Better Title");
		assertThat(model.get()).isEqualTo("Even Better Title");
		assertThat(gui.get()).isEqualTo("Good Title");
		window.textBox().requireText("Good Title");
	}
}