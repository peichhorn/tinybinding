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
package de.fips.util.tinybinding.autobind;

import static org.fest.assertions.Assertions.assertThat;

import java.util.List;

import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.fips.util.tinybinding.DataBindingContext;
import de.fips.util.tinybinding.IObservableValue;
import de.fips.util.tinybinding.ObservableValue;
import de.fips.util.tinybinding.autobind.AutoBinder;
import de.fips.util.tinybinding.autobind.Form;
import de.fips.util.tinybinding.autobind.Model;

/**
 * Tests the {@link AutoBinder}.
 * 
 * @author Philipp Eichhorn
 */
public class AutoBinderTest {
	private TestModel model;
	private TestModel2 model2;
	private TestForm form;
	private TestForm2 form2;
	private DataBindingContext context;

	@Before
	public void setUp() {
		model = new TestModel();
		model2 = new TestModel2();
		form = new TestForm();
		form2 = new TestForm2();
	}

	@After
	public void shutDown() {
		if (context != null) {
			context.unbindAll();
		}
	}

	@Test
	public void test_bind() throws Exception {
		context = AutoBinder.bind(model, form);
		form.text.setText("All new input");
		assertThat(model.text.get()).isEqualTo("All new input");
		model.status.set(Boolean.TRUE);
		assertThat(form.status.isSelected()).isTrue();
	}

	@Test
	public void test_bind_subclassing() throws Exception {
		context = AutoBinder.bind(model2, form2);
		form2.text.setText("All new input");
		assertThat(model2.text.get()).isEqualTo("All new input");
		model2.status.set(Boolean.TRUE);
		assertThat(form2.status.isSelected()).isTrue();
		model2.value.set(new Double(33.3));
		assertThat((Double)form2.value.getValue()).isEqualTo(33.3);
	}

	@Test(expected = NoSuchFieldException.class)
	public void test_bind_missingFormField() throws Exception {
		context = AutoBinder.bind(model2, form);
	}

	@Test
	public void test_bind_extraFormField() throws Exception {
		context = AutoBinder.bind(model, form2);
		form2.text.setText("All new input");
		assertThat(model.text.get()).isEqualTo("All new input");
		model.status.set(Boolean.TRUE);
		assertThat(form2.status.isSelected()).isTrue();
	}

	@Test(expected = NullPointerException.class)
	public void test_bind_nullValues1() throws Exception {
		context = AutoBinder.bind(null, form2);
	}

	@Test(expected = NullPointerException.class)
	public void test_bind_nullValues2() throws Exception {
		context = AutoBinder.bind(model, null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void test_bind_missingAnnotation1() throws Exception {
		context = AutoBinder.bind("", form2);
	}

	@Test(expected = IllegalArgumentException.class)
	public void test_bind_missingAnnotation2() throws Exception {
		context = AutoBinder.bind(model, "");
	}

	@Model
	public static class TestModel {
		public IObservableValue<String> text = ObservableValue.nil();
		public IObservableValue<Boolean> status = ObservableValue.nil();
	}

	@Model
	public static class TestModel2 extends TestModel {
		public String ignoredField1;
		public List<String> ignoredField2;
		public IObservableValue<Double> value = ObservableValue.of(new Double(0.0));
	}

	@Form
	public static class TestForm {
		public JTextArea text = new JTextArea();
		public JToggleButton status = new JToggleButton();
	}

	@Form
	public static class TestForm2 extends TestForm {
		public JSpinner value = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 100.0, 1.0));
	}
}
