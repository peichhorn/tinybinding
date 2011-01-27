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

import static lombok.With.with;
import static de.fips.util.tinybinding.Observables.observe;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

import lombok.AccessLevel;
import lombok.Application;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.SwingInvokeLater;

public class SearchDemo implements Application {

	@SwingInvokeLater
	public void runApp(final String[] args) throws Throwable {
		with(new JFrame("Search Demo"),
			setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE),
			setLayout(new BorderLayout()),
			getContentPane().add(new SearchDemoViewLogic().createView(), BorderLayout.CENTER),
			pack(),
			setVisible(true));
	}
	
	public static class SearchDemoView extends JPanel {
		private static final long serialVersionUID = 2769688665317243418L;

		final IObservableValue<String> searchText;
		final IObservableValue<String> comboValue;
		final IObservableValue<Boolean> enabledValue;
		
		public SearchDemoView(final Action action, final ComboBoxModel model) {
			setLayout(new BorderLayout());
			JTextField textField = new JTextField();
			textField.setPreferredSize(new Dimension(120, 29));
			JComboBox comboBox = new JComboBox();
			comboBox.setModel(model);
			JButton searchButton = new JButton(action);
			add(textField, BorderLayout.WEST);
			add(comboBox, BorderLayout.CENTER);
			add(searchButton, BorderLayout.EAST);
			
			searchText = observe(textField).text();
			comboValue = observe(comboBox).value();
			enabledValue = observe(searchButton).enabled();
		}
	}
	
	public static class SearchDemoViewLogic {
		private final DefaultComboBoxModel model;
		private final SearchAction action;

		public SearchDemoViewLogic() {
			model = new DefaultComboBoxModel();
			model.addElement("Bing");
			model.addElement("Google");
			model.addElement("Yahoo");
			action = new SearchAction();
		}

		public JPanel createView() {
			SearchDemoView view = new SearchDemoView(action, model);
			
			AggregatedBoolean aggregatedValue = new AggregatedBoolean();
			DataBindingContext context = new DataBindingContext();
			context.bind(view.searchText, aggregatedValue.add(), StringLengthToBooleanConverter.notEmpty());
			context.bind(view.comboValue, aggregatedValue.add(), ValueToBooleanConverter.isTrue("Google"));
			context.bind(aggregatedValue, view.enabledValue);
			context.bind(view.searchText, action.getSearchText());
			return view;
		}
	}
	
	public static class SearchAction extends AbstractAction {
		private static final long serialVersionUID = -8331751772675716791L;

		@Getter
		private final IObservableValue<String> searchText = ObservableValue.of("");
		
		public SearchAction() {
			super("Search");
		}
		
		@Override
		public void actionPerformed(final ActionEvent e) {
			if (Desktop.isDesktopSupported()) {
				final Desktop desktop = Desktop.getDesktop();
				try {
					desktop.browse(new URI("http://www.google.com/search?q=" + searchText.get().replaceAll(" ", "%20")));
				} catch (final Exception ignore) {
					// ignore
				}
			}
		}
	}
	
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public static class StringLengthToBooleanConverter implements IConverter<String, Boolean> {
		private final int minimalLength;

		@Override
		public Boolean convert(final String value) {
			return (value != null) && (value.length() >= minimalLength);
		}

		public static IConverter<String, Boolean> notEmpty() {
			return new StringLengthToBooleanConverter(1);
		}
		
		public static StringLengthToBooleanConverter atLeast(final int minimalLength) {
			return new StringLengthToBooleanConverter(minimalLength);
		}
	}
	
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public static class ValueToBooleanConverter<T> implements IConverter<T, Boolean> {
		private final T trueValue;
		@Override
		public Boolean convert(final T source) {
			return trueValue.equals(source);
		}
		
		public static <T> ValueToBooleanConverter<T> isTrue(final T trueValue) {
			return new ValueToBooleanConverter<T>(trueValue);
		}
	}
	
	@NoArgsConstructor
	public static class AggregatedBoolean extends ObservableValue<Boolean> {
		private final List<IObservableValue<Boolean>> values = new ArrayList<IObservableValue<Boolean>>(); 
		
		public IObservableValue<Boolean> add() {
			IObservableValue<Boolean> value = new ObservableValue<Boolean>() {
				@Override
				protected void doSet(final Boolean value) {
					super.doSet(value);
					update();
				}
			};
			values.add(value);
			return value;
		}
		
		private void update() {
			boolean b = true;
			for (IObservableValue<Boolean> value : values) {
				b &= value.get();
			}
			set(b);
		}
	}
}
