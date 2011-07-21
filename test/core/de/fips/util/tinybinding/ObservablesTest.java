package de.fips.util.tinybinding;

import static de.fips.util.tinybinding.Observables.observe;
import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.awt.Container;

import org.junit.Test;

import de.fips.util.tinybinding.pojo.PojoObservable;
import de.fips.util.tinybinding.swing.SwingObservable;

public class ObservablesTest {
	@Test
	public void test_observeContainerCreatesSwingObservable() {
		assertThat(observe(mock(Container.class))).isInstanceOf(SwingObservable.class);
	}

	@Test
	public void test_observeObjectCreatesPojoObservable() {
		assertThat(observe(mock(Object.class))).isInstanceOf(PojoObservable.class);
	}
}
