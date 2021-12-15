/*
 * Copyright 2002-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.validation;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.ParseException;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import org.junit.jupiter.api.Test;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.InvalidPropertyException;
import org.springframework.beans.MethodInvocationException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.NotWritablePropertyException;
import org.springframework.beans.NullValueInNestedPathException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.beans.propertyeditors.CustomCollectionEditor;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.beans.testfixture.beans.DerivedTestBean;
import org.springframework.beans.testfixture.beans.ITestBean;
import org.springframework.beans.testfixture.beans.IndexedTestBean;
import org.springframework.beans.testfixture.beans.SerializablePerson;
import org.springframework.beans.testfixture.beans.TestBean;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.format.Formatter;
import org.springframework.format.number.NumberStyleFormatter;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.lang.Nullable;
import org.springframework.tests.sample.beans.BeanWithObjectProperty;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

/**
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @author Kazuki Shimizu
 */
public class DataBinderTests {

	@Test
	public void testBindingNoErrors() throws BindException {
		TestBean rod = new TestBean();
		DataBinder binder = new DataBinder(rod, "person");
		assertThat(binder.isIgnoreUnknownFields()).isTrue();
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.add("name", "Rod");
		pvs.add("age", "032");
		pvs.add("nonExisting", "someValue");

		binder.bind(pvs);
		binder.close();

		assertThat(rod.getName().equals("Rod")).as("changed name correctly").isTrue();
		assertThat(rod.getAge() == 32).as("changed age correctly").isTrue();

		Map<?, ?> map = binder.getBindingResult().getModel();
		assertThat(map.size() == 2).as("There is one element in map").isTrue();
		TestBean tb = (TestBean) map.get("person");
		assertThat(tb.equals(rod)).as("Same object").isTrue();

		BindingResult other = new BeanPropertyBindingResult(rod, "person");
		assertThat(binder.getBindingResult()).isEqualTo(other);
		assertThat(other).isEqualTo(binder.getBindingResult());
		BindException ex = new BindException(other);
		assertThat(other).isEqualTo(ex);
		assertThat(ex).isEqualTo(other);
		assertThat(binder.getBindingResult()).isEqualTo(ex);
		assertThat(ex).isEqualTo(binder.getBindingResult());

		other.reject("xxx");
		boolean condition = !other.equals(binder.getBindingResult());
		assertThat(condition).isTrue();
	}

	@Test
	public void testBindingWithDefaultConversionNoErrors() throws BindException {
		TestBean rod = new TestBean();
		DataBinder binder = new DataBinder(rod, "person");
		assertThat(binder.isIgnoreUnknownFields()).isTrue();
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.add("name", "Rod");
		pvs.add("jedi", "on");

		binder.bind(pvs);
		binder.close();

		assertThat(rod.getName()).isEqualTo("Rod");
		assertThat(rod.isJedi()).isTrue();
	}

	@Test
	public void testNestedBindingWithDefaultConversionNoErrors() throws BindException {
		TestBean rod = new TestBean(new TestBean());
		DataBinder binder = new DataBinder(rod, "person");
		assertThat(binder.isIgnoreUnknownFields()).isTrue();
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.add("spouse.name", "Kerry");
		pvs.add("spouse.jedi", "on");

		binder.bind(pvs);
		binder.close();

		assertThat(rod.getSpouse().getName()).isEqualTo("Kerry");
		assertThat(((TestBean) rod.getSpouse()).isJedi()).isTrue();
	}

	@Test
	public void testBindingNoErrorsNotIgnoreUnknown() {
		TestBean rod = new TestBean();
		DataBinder binder = new DataBinder(rod, "person");
		binder.setIgnoreUnknownFields(false);
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.add("name", "Rod");
		pvs.add("age", 32);
		pvs.add("nonExisting", "someValue");
		assertThatExceptionOfType(NotWritablePropertyException.class).isThrownBy(() ->
				binder.bind(pvs));
	}

	@Test
	public void testBindingNoErrorsWithInvalidField() {
		TestBean rod = new TestBean();
		DataBinder binder = new DataBinder(rod, "person");
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.add("name", "Rod");
		pvs.add("spouse.age", 32);
		assertThatExceptionOfType(NullValueInNestedPathException.class).isThrownBy(() ->
				binder.bind(pvs));
	}

	@Test
	public void testBindingNoErrorsWithIgnoreInvalid() {
		TestBean rod = new TestBean();
		DataBinder binder = new DataBinder(rod, "person");
		binder.setIgnoreInvalidFields(true);
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.add("name", "Rod");
		pvs.add("spouse.age", 32);

		binder.bind(pvs);
	}

	@Test
	public void testBindingWithErrors() {
		TestBean rod = new TestBean();
		DataBinder binder = new DataBinder(rod, "person");
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.add("name", "Rod");
		pvs.add("age", "32x");
		pvs.add("touchy", "m.y");
		binder.bind(pvs);
		assertThatExceptionOfType(BindException.class).isThrownBy(
				binder::close)
			.satisfies(ex -> {
				assertThat(rod.getName()).isEqualTo("Rod");
				Map<?, ?> map = binder.getBindingResult().getModel();
				TestBean tb = (TestBean) map.get("person");
				assertThat(tb).isSameAs(rod);

				BindingResult br = (BindingResult) map.get(BindingResult.MODEL_KEY_PREFIX + "person");
				assertThat(BindingResultUtils.getBindingResult(map, "person")).isEqualTo(br);
				assertThat(BindingResultUtils.getRequiredBindingResult(map, "person")).isEqualTo(br);

				assertThat(BindingResultUtils.getBindingResult(map, "someOtherName")).isNull();
				assertThatIllegalStateException().isThrownBy(() ->
						BindingResultUtils.getRequiredBindingResult(map, "someOtherName"));

				assertThat(binder.getBindingResult()).as("Added itself to map").isSameAs(br);
				assertThat(br.hasErrors()).isTrue();
				assertThat(br.getErrorCount()).isEqualTo(2);

				assertThat(br.hasFieldErrors("age")).isTrue();
				assertThat(br.getFieldErrorCount("age")).isEqualTo(1);
				assertThat(binder.getBindingResult().getFieldValue("age")).isEqualTo("32x");
				FieldError ageError = binder.getBindingResult().getFieldError("age");
				assertThat(ageError).isNotNull();
				assertThat(ageError.getCode()).isEqualTo("typeMismatch");
				assertThat(ageError.getRejectedValue()).isEqualTo("32x");
				assertThat(ageError.contains(TypeMismatchException.class)).isTrue();
				assertThat(ageError.contains(NumberFormatException.class)).isTrue();
				assertThat(ageError.unwrap(NumberFormatException.class).getMessage()).contains("32x");
				assertThat(tb.getAge()).isEqualTo(0);

				assertThat(br.hasFieldErrors("touchy")).isTrue();
				assertThat(br.getFieldErrorCount("touchy")).isEqualTo(1);
				assertThat(binder.getBindingResult().getFieldValue("touchy")).isEqualTo("m.y");
				FieldError touchyError = binder.getBindingResult().getFieldError("touchy");
				assertThat(touchyError).isNotNull();
				assertThat(touchyError.getCode()).isEqualTo("methodInvocation");
				assertThat(touchyError.getRejectedValue()).isEqualTo("m.y");
				assertThat(touchyError.contains(MethodInvocationException.class)).isTrue();
				assertThat(touchyError.unwrap(MethodInvocationException.class).getCause().getMessage()).contains("a .");
				assertThat(tb.getTouchy()).isNull();

				DataBinder binder2 = new DataBinder(new TestBean(), "person");
				MutablePropertyValues pvs2 = new MutablePropertyValues();
				pvs2.add("name", "Rod");
				pvs2.add("age", "32x");
				pvs2.add("touchy", "m.y");
				binder2.bind(pvs2);
				assertThat(ex.getBindingResult()).isEqualTo(binder2.getBindingResult());
			});
	}

	@Test
	public void testBindingWithSystemFieldError() {
		TestBean rod = new TestBean();
		DataBinder binder = new DataBinder(rod, "person");
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.add("class.classLoader.URLs[0]", "https://myserver");
		binder.setIgnoreUnknownFields(false);
		assertThatExceptionOfType(NotWritablePropertyException.class).isThrownBy(() ->
				binder.bind(pvs))
			.withMessageContaining("classLoader");
	}

	@Test
	public void testBindingWithErrorsAndCustomEditors() {
		TestBean rod = new TestBean();
		DataBinder binder = new DataBinder(rod, "person");
		binder.registerCustomEditor(String.class, "touchy", new PropertyEditorSupport() {
			@Override
			public void setAsText(String text) throws IllegalArgumentException {
				setValue("prefix_" + text);
			}
			@Override
			public String getAsText() {
				return getValue().toString().substring(7);
			}
		});
		binder.registerCustomEditor(TestBean.class, "spouse", new PropertyEditorSupport() {
			@Override
			public void setAsText(String text) throws IllegalArgumentException {
				setValue(new TestBean(text, 0));
			}
			@Override
			public String getAsText() {
				return ((TestBean) getValue()).getName();
			}
		});
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.add("name", "Rod");
		pvs.add("age", "32x");
		pvs.add("touchy", "m.y");
		pvs.add("spouse", "Kerry");
		binder.bind(pvs);

		assertThatExceptionOfType(BindException.class).isThrownBy(
				binder::close)
			.satisfies(ex -> {
				assertThat(rod.getName()).isEqualTo("Rod");
				Map<?, ?> model = binder.getBindingResult().getModel();
				TestBean tb = (TestBean) model.get("person");
				assertThat(tb).isEqualTo(rod);

				BindingResult br = (BindingResult) model.get(BindingResult.MODEL_KEY_PREFIX + "person");
				assertThat(binder.getBindingResult()).isSameAs(br);
				assertThat(br.hasErrors()).isTrue();
				assertThat(br.getErrorCount()).isEqualTo(2);

				assertThat(br.hasFieldErrors("age")).isTrue();
				assertThat(br.getFieldErrorCount("age")).isEqualTo(1);
				assertThat(binder.getBindingResult().getFieldValue("age")).isEqualTo("32x");
				FieldError ageError = binder.getBindingResult().getFieldError("age");
				assertThat(ageError).isNotNull();
				assertThat(ageError.getCode()).isEqualTo("typeMismatch");
				assertThat(ageError.getRejectedValue()).isEqualTo("32x");
				assertThat(tb.getAge()).isEqualTo(0);

				assertThat(br.hasFieldErrors("touchy")).isTrue();
				assertThat(br.getFieldErrorCount("touchy")).isEqualTo(1);
				assertThat(binder.getBindingResult().getFieldValue("touchy")).isEqualTo("m.y");
				FieldError touchyError = binder.getBindingResult().getFieldError("touchy");
				assertThat(touchyError).isNotNull();
				assertThat(touchyError.getCode()).isEqualTo("methodInvocation");
				assertThat(touchyError.getRejectedValue()).isEqualTo("m.y");
				assertThat(tb.getTouchy()).isNull();

				assertThat(br.hasFieldErrors("spouse")).isFalse();
				assertThat(binder.getBindingResult().getFieldValue("spouse")).isEqualTo("Kerry");
				assertThat(tb.getSpouse()).isNotNull();
			});
	}

	@Test
	public void testBindingWithCustomEditorOnObjectField() {
		BeanWithObjectProperty tb = new BeanWithObjectProperty();
		DataBinder binder = new DataBinder(tb);
		binder.registerCustomEditor(Integer.class, "object", new CustomNumberEditor(Integer.class, true));
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.add("object", "1");
		binder.bind(pvs);
		assertThat(tb.getObject()).isEqualTo(new Integer(1));
	}

	@Test
	public void testBindingWithFormatter() {
		TestBean tb = new TestBean();
		DataBinder binder = new DataBinder(tb);
		FormattingConversionService conversionService = new FormattingConversionService();
		DefaultConversionService.addDefaultConverters(conversionService);
		conversionService.addFormatterForFieldType(Float.class, new NumberStyleFormatter());
		binder.setConversionService(conversionService);
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.add("myFloat", "1,2");

		LocaleContextHolder.setLocale(Locale.GERMAN);
		try {
			binder.bind(pvs);
			assertThat(tb.getMyFloat()).isEqualTo(new Float(1.2));
			assertThat(binder.getBindingResult().getFieldValue("myFloat")).isEqualTo("1,2");

			PropertyEditor editor = binder.getBindingResult().findEditor("myFloat", Float.class);
			assertThat(editor).isNotNull();
			editor.setValue(new Float(1.4));
			assertThat(editor.getAsText()).isEqualTo("1,4");

			editor = binder.getBindingResult().findEditor("myFloat", null);
			assertThat(editor).isNotNull();
			editor.setAsText("1,6");
			assertThat(editor.getValue()).isEqualTo(new Float(1.6));
		}
		finally {
			LocaleContextHolder.resetLocaleContext();
		}
	}

	@Test
	public void testBindingErrorWithFormatter() {
		TestBean tb = new TestBean();
		DataBinder binder = new DataBinder(tb);
		FormattingConversionService conversionService = new FormattingConversionService();
		DefaultConversionService.addDefaultConverters(conversionService);
		conversionService.addFormatterForFieldType(Float.class, new NumberStyleFormatter());
		binder.setConversionService(conversionService);
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.add("myFloat", "1x2");

		LocaleContextHolder.setLocale(Locale.GERMAN);
		try {
			binder.bind(pvs);
			assertThat(tb.getMyFloat()).isEqualTo(new Float(0.0));
			assertThat(binder.getBindingResult().getFieldValue("myFloat")).isEqualTo("1x2");
			assertThat(binder.getBindingResult().hasFieldErrors("myFloat")).isTrue();
		}
		finally {
			LocaleContextHolder.resetLocaleContext();
		}
	}

	@Test
	public void testBindingErrorWithParseExceptionFromFormatter() {
		TestBean tb = new TestBean();
		DataBinder binder = new DataBinder(tb);
		FormattingConversionService conversionService = new FormattingConversionService();
		DefaultConversionService.addDefaultConverters(conversionService);

		conversionService.addFormatter(new Formatter<String>() {
			@Override
			public String parse(String text, Locale locale) throws ParseException {
				throw new ParseException(text, 0);
			}
			@Override
			public String print(String object, Locale locale) {
				return object;
			}
		});

		binder.setConversionService(conversionService);
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.add("name", "test");

		binder.bind(pvs);
		assertThat(binder.getBindingResult().hasFieldErrors("name")).isTrue();
		assertThat(binder.getBindingResult().getFieldError("name").getCode()).isEqualTo("typeMismatch");
		assertThat(binder.getBindingResult().getFieldValue("name")).isEqualTo("test");
	}

	@Test
	public void testBindingErrorWithRuntimeExceptionFromFormatter() {
		TestBean tb = new TestBean();
		DataBinder binder = new DataBinder(tb);
		FormattingConversionService conversionService = new FormattingConversionService();
		DefaultConversionService.addDefaultConverters(conversionService);

		conversionService.addFormatter(new Formatter<String>() {
			@Override
			public String parse(String text, Locale locale) throws ParseException {
				throw new RuntimeException(text);
			}
			@Override
			public String print(String object, Locale locale) {
				return object;
			}
		});

		binder.setConversionService(conversionService);
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.add("name", "test");

		binder.bind(pvs);
		assertThat(binder.getBindingResult().hasFieldErrors("name")).isTrue();
		assertThat(binder.getBindingResult().getFieldError("name").getCode()).isEqualTo("typeMismatch");
		assertThat(binder.getBindingResult().getFieldValue("name")).isEqualTo("test");
	}

	@Test
	public void testBindingWithFormatterAgainstList() {
		BeanWithIntegerList tb = new BeanWithIntegerList();
		DataBinder binder = new DataBinder(tb);
		FormattingConversionService conversionService = new FormattingConversionService();
		DefaultConversionService.addDefaultConverters(conversionService);
		conversionService.addFormatterForFieldType(Float.class, new NumberStyleFormatter());
		binder.setConversionService(conversionService);
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.add("integerList[0]", "1");

		LocaleContextHolder.setLocale(Locale.GERMAN);
		try {
			binder.bind(pvs);
			assertThat(tb.getIntegerList().get(0)).isEqualTo(new Integer(1));
			assertThat(binder.getBindingResult().getFieldValue("integerList[0]")).isEqualTo("1");
		}
		finally {
			LocaleContextHolder.resetLocaleContext();
		}
	}

	@Test
	public void testBindingErrorWithFormatterAgainstList() {
		BeanWithIntegerList tb = new BeanWithIntegerList();
		DataBinder binder = new DataBinder(tb);
		FormattingConversionService conversionService = new FormattingConversionService();
		DefaultConversionService.addDefaultConverters(conversionService);
		conversionService.addFormatterForFieldType(Float.class, new NumberStyleFormatter());
		binder.setConversionService(conversionService);
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.add("integerList[0]", "1x2");

		LocaleContextHolder.setLocale(Locale.GERMAN);
		try {
			binder.bind(pvs);
			assertThat(tb.getIntegerList().isEmpty()).isTrue();
			assertThat(binder.getBindingResult().getFieldValue("integerList[0]")).isEqualTo("1x2");
			assertThat(binder.getBindingResult().hasFieldErrors("integerList[0]")).isTrue();
		}
		finally {
			LocaleContextHolder.resetLocaleContext();
		}
	}

	@Test
	public void testBindingWithFormatterAgainstFields() {
		TestBean tb = new TestBean();
		DataBinder binder = new DataBinder(tb);
		FormattingConversionService conversionService = new FormattingConversionService();
		DefaultConversionService.addDefaultConverters(conversionService);
		conversionService.addFormatterForFieldType(Float.class, new NumberStyleFormatter());
		binder.setConversionService(conversionService);
		binder.initDirectFieldAccess();
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.add("myFloat", "1,2");

		LocaleContextHolder.setLocale(Locale.GERMAN);
		try {
			binder.bind(pvs);
			assertThat(tb.getMyFloat()).isEqualTo(new Float(1.2));
			assertThat(binder.getBindingResult().getFieldValue("myFloat")).isEqualTo("1,2");

			PropertyEditor editor = binder.getBindingResult().findEditor("myFloat", Float.class);
			assertThat(editor).isNotNull();
			editor.setValue(new Float(1.4));
			assertThat(editor.getAsText()).isEqualTo("1,4");

			editor = binder.getBindingResult().findEditor("myFloat", null);
			assertThat(editor).isNotNull();
			editor.setAsText("1,6");
			assertThat(editor.getValue()).isEqualTo(new Float(1.6));
		}
		finally {
			LocaleContextHolder.resetLocaleContext();
		}
	}

	@Test
	public void testBindingErrorWithFormatterAgainstFields() {
		TestBean tb = new TestBean();
		DataBinder binder = new DataBinder(tb);
		binder.initDirectFieldAccess();
		FormattingConversionService conversionService = new FormattingConversionService();
		DefaultConversionService.addDefaultConverters(conversionService);
		conversionService.addFormatterForFieldType(Float.class, new NumberStyleFormatter());
		binder.setConversionService(conversionService);
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.add("myFloat", "1x2");

		LocaleContextHolder.setLocale(Locale.GERMAN);
		try {
			binder.bind(pvs);
			assertThat(tb.getMyFloat()).isEqualTo(new Float(0.0));
			assertThat(binder.getBindingResult().getFieldValue("myFloat")).isEqualTo("1x2");
			assertThat(binder.getBindingResult().hasFieldErrors("myFloat")).isTrue();
		}
		finally {
			LocaleContextHolder.resetLocaleContext();
		}
	}

	@Test
	public void testBindingWithCustomFormatter() {
		TestBean tb = new TestBean();
		DataBinder binder = new DataBinder(tb);
		binder.addCustomFormatter(new NumberStyleFormatter(), Float.class);
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.add("myFloat", "1,2");

		LocaleContextHolder.setLocale(Locale.GERMAN);
		try {
			binder.bind(pvs);
			assertThat(tb.getMyFloat()).isEqualTo(new Float(1.2));
			assertThat(binder.getBindingResult().getFieldValue("myFloat")).isEqualTo("1,2");

			PropertyEditor editor = binder.getBindingResult().findEditor("myFloat", Float.class);
			assertThat(editor).isNotNull();
			editor.setValue(new Float(1.4));
			assertThat(editor.getAsText()).isEqualTo("1,4");

			editor = binder.getBindingResult().findEditor("myFloat", null);
			assertThat(editor).isNotNull();
			editor.setAsText("1,6");
			assertThat(((Number) editor.getValue()).floatValue() == 1.6f).isTrue();
		}
		finally {
			LocaleContextHolder.resetLocaleContext();
		}
	}

	@Test
	public void testBindingErrorWithCustomFormatter() {
		TestBean tb = new TestBean();
		DataBinder binder = new DataBinder(tb);
		binder.addCustomFormatter(new NumberStyleFormatter());
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.add("myFloat", "1x2");

		LocaleContextHolder.setLocale(Locale.GERMAN);
		try {
			binder.bind(pvs);
			assertThat(tb.getMyFloat()).isEqualTo(new Float(0.0));
			assertThat(binder.getBindingResult().getFieldValue("myFloat")).isEqualTo("1x2");
			assertThat(binder.getBindingResult().hasFieldErrors("myFloat")).isTrue();
			assertThat(binder.getBindingResult().getFieldError("myFloat").getCode()).isEqualTo("typeMismatch");
		}
		finally {
			LocaleContextHolder.resetLocaleContext();
		}
	}

	@Test
	public void testBindingErrorWithParseExceptionFromCustomFormatter() {
		TestBean tb = new TestBean();
		DataBinder binder = new DataBinder(tb);

		binder.addCustomFormatter(new Formatter<String>() {
			@Override
			public String parse(String text, Locale locale) throws ParseException {
				throw new ParseException(text, 0);
			}
			@Override
			public String print(String object, Locale locale) {
				return object;
			}
		});

		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.add("name", "test");

		binder.bind(pvs);
		assertThat(binder.getBindingResult().hasFieldErrors("name")).isTrue();
		assertThat(binder.getBindingResult().getFieldValue("name")).isEqualTo("test");
		assertThat(binder.getBindingResult().getFieldError("name").getCode()).isEqualTo("typeMismatch");
	}

	@Test
	public void testBindingErrorWithRuntimeExceptionFromCustomFormatter() {
		TestBean tb = new TestBean();
		DataBinder binder = new DataBinder(tb);

		binder.addCustomFormatter(new Formatter<String>() {
			@Override
			public String parse(String text, Locale locale) throws ParseException {
				throw new RuntimeException(text);
			}
			@Override
			public String print(String object, Locale locale) {
				return object;
			}
		});

		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.add("name", "test");

		binder.bind(pvs);
		assertThat(binder.getBindingResult().hasFieldErrors("name")).isTrue();
		assertThat(binder.getBindingResult().getFieldValue("name")).isEqualTo("test");
		assertThat(binder.getBindingResult().getFieldError("name").getCode()).isEqualTo("typeMismatch");
	}

	@Test
	public void testConversionWithInappropriateStringEditor() {
		DataBinder dataBinder = new DataBinder(null);
		DefaultFormattingConversionService conversionService = new DefaultFormattingConversionService();
		dataBinder.setConversionService(conversionService);
		dataBinder.registerCustomEditor(String.class, new StringTrimmerEditor(true));

		NameBean bean = new NameBean("Fred");
		assertThat(dataBinder.convertIfNecessary(bean, String.class)).as("ConversionService should have invoked toString()").isEqualTo("Fred");
		conversionService.addConverter(new NameBeanConverter());
		assertThat(dataBinder.convertIfNecessary(bean, String.class)).as("Type converter should have been used").isEqualTo("[Fred]");
	}

	@Test
	public void testBindingWithAllowedFields() throws BindException {
		TestBean rod = new TestBean();
		DataBinder binder = new DataBinder(rod);
		binder.setAllowedFields("name", "myparam");
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.add("name", "Rod");
		pvs.add("age", "32x");

		binder.bind(pvs);
		binder.close();
		assertThat(rod.getName().equals("Rod")).as("changed name correctly").isTrue();
		assertThat(rod.getAge() == 0).as("did not change age").isTrue();
	}

	@Test
	public void testBindingWithDisallowedFields() throws BindException {
		TestBean rod = new TestBean();
		DataBinder binder = new DataBinder(rod);
		binder.setDisallowedFields("age");
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.add("name", "Rod");
		pvs.add("age", "32x");

		binder.bind(pvs);
		binder.close();
		assertThat(rod.getName().equals("Rod")).as("changed name correctly").isTrue();
		assertThat(rod.getAge() == 0).as("did not change age").isTrue();
		String[] disallowedFields = binder.getBindingResult().getSuppressedFields();
		assertThat(disallowedFields.length).isEqualTo(1);
		assertThat(disallowedFields[0]).isEqualTo("age");
	}

	@Test
	public void testBindingWithAllowedAndDisallowedFields() throws BindException {
		TestBean rod = new TestBean();
		DataBinder binder = new DataBinder(rod);
		binder.setAllowedFields("name", "myparam");
		binder.setDisallowedFields("age");
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.add("name", "Rod");
		pvs.add("age", "32x");

		binder.bind(pvs);
		binder.close();
		assertThat(rod.getName().equals("Rod")).as("changed name correctly").isTrue();
		assertThat(rod.getAge() == 0).as("did not change age").isTrue();
		String[] disallowedFields = binder.getBindingResult().getSuppressedFields();
		assertThat(disallowedFields.length).isEqualTo(1);
		assertThat(disallowedFields[0]).isEqualTo("age");
	}

	@Test
	public void testBindingWithOverlappingAllowedAndDisallowedFields() throws BindException {
		TestBean rod = new TestBean();
		DataBinder binder = new DataBinder(rod);
		binder.setAllowedFields("name", "age");
		binder.setDisallowedFields("age");
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.add("name", "Rod");
		pvs.add("age", "32x");

		binder.bind(pvs);
		binder.close();
		assertThat(rod.getName().equals("Rod")).as("changed name correctly").isTrue();
		assertThat(rod.getAge() == 0).as("did not change age").isTrue();
		String[] disallowedFields = binder.getBindingResult().getSuppressedFields();
		assertThat(disallowedFields.length).isEqualTo(1);
		assertThat(disallowedFields[0]).isEqualTo("age");
	}

	@Test
	public void testBindingWithAllowedFieldsUsingAsterisks() throws BindException {
		TestBean rod = new TestBean();
		DataBinder binder = new DataBinder(rod, "person");
		binder.setAllowedFields("nam*", "*ouchy");

		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.add("name", "Rod");
		pvs.add("touchy", "Rod");
		pvs.add("age", "32x");

		binder.bind(pvs);
		binder.close();

		assertThat("Rod".equals(rod.getName())).as("changed name correctly").isTrue();
		assertThat("Rod".equals(rod.getTouchy())).as("changed touchy correctly").isTrue();
		assertThat(rod.getAge() == 0).as("did not change age").isTrue();
		String[] disallowedFields = binder.getBindingResult().getSuppressedFields();
		assertThat(disallowedFields.length).isEqualTo(1);
		assertThat(disallowedFields[0]).isEqualTo("age");

		Map<?,?> m = binder.getBindingResult().getModel();
		assertThat(m.size() == 2).as("There is one element in map").isTrue();
		TestBean tb = (TestBean) m.get("person");
		assertThat(tb.equals(rod)).as("Same object").isTrue();
	}

	@Test
	public void testBindingWithAllowedAndDisallowedMapFields() throws BindException {
		TestBean rod = new TestBean();
		DataBinder binder = new DataBinder(rod);
		binder.setAllowedFields("someMap[key1]", "someMap[key2]");
		binder.setDisallowedFields("someMap['key3']", "someMap[key4]");

		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.add("someMap[key1]", "value1");
		pvs.add("someMap['key2']", "value2");
		pvs.add("someMap[key3]", "value3");
		pvs.add("someMap['key4']", "value4");

		binder.bind(pvs);
		binder.close();
		assertThat(rod.getSomeMap().get("key1")).isEqualTo("value1");
		assertThat(rod.getSomeMap().get("key2")).isEqualTo("value2");
		assertThat(rod.getSomeMap().get("key3")).isNull();
		assertThat(rod.getSomeMap().get("key4")).isNull();
		String[] disallowedFields = binder.getBindingResult().getSuppressedFields();
		assertThat(disallowedFields.length).isEqualTo(2);
		assertThat(ObjectUtils.containsElement(disallowedFields, "someMap[key3]")).isTrue();
		assertThat(ObjectUtils.containsElement(disallowedFields, "someMap[key4]")).isTrue();
	}

	/**
	 * Tests for required field, both null, non-existing and empty strings.
	 */
	@Test
	public void testBindingWithRequiredFields() {
		TestBean tb = new TestBean();
		tb.setSpouse(new TestBean());

		DataBinder binder = new DataBinder(tb, "person");
		binder.setRequiredFields("touchy", "name", "age", "date", "spouse.name");

		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.add("touchy", "");
		pvs.add("name", null);
		pvs.add("age", null);
		pvs.add("spouse.name", "     ");

		binder.bind(pvs);

		BindingResult br = binder.getBindingResult();
		assertThat(br.getErrorCount()).as("Wrong number of errors").isEqualTo(5);

		assertThat(br.getFieldError("touchy").getCode()).isEqualTo("required");
		assertThat(br.getFieldValue("touchy")).isEqualTo("");
		assertThat(br.getFieldError("name").getCode()).isEqualTo("required");
		assertThat(br.getFieldValue("name")).isEqualTo("");
		assertThat(br.getFieldError("age").getCode()).isEqualTo("required");
		assertThat(br.getFieldValue("age")).isEqualTo("");
		assertThat(br.getFieldError("date").getCode()).isEqualTo("required");
		assertThat(br.getFieldValue("date")).isEqualTo("");
		assertThat(br.getFieldError("spouse.name").getCode()).isEqualTo("required");
		assertThat(br.getFieldValue("spouse.name")).isEqualTo("");
	}

	@Test
	public void testBindingWithRequiredMapFields() {
		TestBean tb = new TestBean();
		tb.setSpouse(new TestBean());

		DataBinder binder = new DataBinder(tb, "person");
		binder.setRequiredFields("someMap[key1]", "someMap[key2]", "someMap['key3']", "someMap[key4]");

		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.add("someMap[key1]", "value1");
		pvs.add("someMap['key2']", "value2");
		pvs.add("someMap[key3]", "value3");

		binder.bind(pvs);

		BindingResult br = binder.getBindingResult();
		assertThat(br.getErrorCount()).as("Wrong number of errors").isEqualTo(1);
		assertThat(br.getFieldError("someMap[key4]").getCode()).isEqualTo("required");
	}

	@Test
	public void testBindingWithNestedObjectCreation() {
		TestBean tb = new TestBean();

		DataBinder binder = new DataBinder(tb, "person");
		binder.registerCustomEditor(ITestBean.class, new PropertyEditorSupport() {
			@Override
			public void setAsText(String text) throws IllegalArgumentException {
				setValue(new TestBean());
			}
		});

		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.add("spouse", "someValue");
		pvs.add("spouse.name", "test");
		binder.bind(pvs);

		assertThat(tb.getSpouse()).isNotNull();
		assertThat(tb.getSpouse().getName()).isEqualTo("test");
	}

	@Test
	public void testCustomEditorWithOldValueAccess() {
		TestBean tb = new TestBean();
		DataBinder binder = new DataBinder(tb, "tb");

		binder.registerCustomEditor(String.class, null, new PropertyEditorSupport() {
			@Override
			public void setAsText(String text) throws IllegalArgumentException {
				if (getValue() == null || !text.equalsIgnoreCase(getValue().toString())) {
					setValue(text);
				}
			}
		});

		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.add("name", "value");
		binder.bind(pvs);
		assertThat(tb.getName()).isEqualTo("value");

		pvs = new MutablePropertyValues();
		pvs.add("name", "vaLue");
		binder.bind(pvs);
		assertThat(tb.getName()).isEqualTo("value");
	}

	@Test
	public void testCustomEditorForSingleProperty() {
		TestBean tb = new TestBean();
		tb.setSpouse(new TestBean());
		DataBinder binder = new DataBinder(tb, "tb");

		binder.registerCustomEditor(String.class, "name", new PropertyEditorSupport() {
			@Override
			public void setAsText(String text) throws IllegalArgumentException {
				setValue("prefix" + text);
			}
			@Override
			public String getAsText() {
				return ((String) getValue()).substring(6);
			}
		});

		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.add("name", "value");
		pvs.add("touchy", "value");
		pvs.add("spouse.name", "sue");
		binder.bind(pvs);

		binder.getBindingResult().rejectValue("name", "someCode", "someMessage");
		binder.getBindingResult().rejectValue("touchy", "someCode", "someMessage");
		binder.getBindingResult().rejectValue("spouse.name", "someCode", "someMessage");

		assertThat(binder.getBindingResult().getNestedPath()).isEqualTo("");
		assertThat(binder.getBindingResult().getFieldValue("name")).isEqualTo("value");
		assertThat(binder.getBindingResult().getFieldError("name").getRejectedValue()).isEqualTo("prefixvalue");
		assertThat(tb.getName()).isEqualTo("prefixvalue");
		assertThat(binder.getBindingResult().getFieldValue("touchy")).isEqualTo("value");
		assertThat(binder.getBindingResult().getFieldError("touchy").getRejectedValue()).isEqualTo("value");
		assertThat(tb.getTouchy()).isEqualTo("value");

		assertThat(binder.getBindingResult().hasFieldErrors("spouse.*")).isTrue();
		assertThat(binder.getBindingResult().getFieldErrorCount("spouse.*")).isEqualTo(1);
		assertThat(binder.getBindingResult().getFieldError("spouse.*").getField()).isEqualTo("spouse.name");
	}

	@Test
	public void testCustomEditorForPrimitiveProperty() {
		TestBean tb = new TestBean();
		DataBinder binder = new DataBinder(tb, "tb");

		binder.registerCustomEditor(int.class, "age", new PropertyEditorSupport() {
			@Override
			public void setAsText(String text) throws IllegalArgumentException {
				setValue(new Integer(99));
			}
			@Override
			public String getAsText() {
				return "argh";
			}
		});

		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.add("age", "");
		binder.bind(pvs);

		assertThat(binder.getBindingResult().getFieldValue("age")).isEqualTo("argh");
		assertThat(tb.getAge()).isEqualTo(99);
	}

	@Test
	public void testCustomEditorForAllStringProperties() {
		TestBean tb = new TestBean();
		DataBinder binder = new DataBinder(tb, "tb");

		binder.registerCustomEditor(String.class, null, new PropertyEditorSupport() {
			@Override
			public void setAsText(String text) throws IllegalArgumentException {
				setValue("prefix" + text);
			}
			@Override
			public String getAsText() {
				return ((String) getValue()).substring(6);
			}
		});

		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.add("name", "value");
		pvs.add("touchy", "value");
		binder.bind(pvs);

		binder.getBindingResult().rejectValue("name", "someCode", "someMessage");
		binder.getBindingResult().rejectValue("touchy", "someCode", "someMessage");

		assertThat(binder.getBindingResult().getFieldValue("name")).isEqualTo("value");
		assertThat(binder.getBindingResult().getFieldError("name").getRejectedValue()).isEqualTo("prefixvalue");
		assertThat(tb.getName()).isEqualTo("prefixvalue");
		assertThat(binder.getBindingResult().getFieldValue("touchy")).isEqualTo("value");
		assertThat(binder.getBindingResult().getFieldError("touchy").getRejectedValue()).isEqualTo("prefixvalue");
		assertThat(tb.getTouchy()).isEqualTo("prefixvalue");
	}

	@Test
	public void testCustomFormatterForSingleProperty() {
		TestBean tb = new TestBean();
		tb.setSpouse(new TestBean());
		DataBinder binder = new DataBinder(tb, "tb");

		binder.addCustomFormatter(new Formatter<String>() {
			@Override
			public String parse(String text, Locale locale) throws ParseException {
				return "prefix" + text;
			}
			@Override
			public String print(String object, Locale locale) {
				return object.substring(6);
			}
		}, "name");

		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.add("name", "value");
		pvs.add("touchy", "value");
		pvs.add("spouse.name", "sue");
		binder.bind(pvs);

		binder.getBindingResult().rejectValue("name", "someCode", "someMessage");
		binder.getBindingResult().rejectValue("touchy", "someCode", "someMessage");
		binder.getBindingResult().rejectValue("spouse.name", "someCode", "someMessage");

		assertThat(binder.getBindingResult().getNestedPath()).isEqualTo("");
		assertThat(binder.getBindingResult().getFieldValue("name")).isEqualTo("value");
		assertThat(binder.getBindingResult().getFieldError("name").getRejectedValue()).isEqualTo("prefixvalue");
		assertThat(tb.getName()).isEqualTo("prefixvalue");
		assertThat(binder.getBindingResult().getFieldValue("touchy")).isEqualTo("value");
		assertThat(binder.getBindingResult().getFieldError("touchy").getRejectedValue()).isEqualTo("value");
		assertThat(tb.getTouchy()).isEqualTo("value");

		assertThat(binder.getBindingResult().hasFieldErrors("spouse.*")).isTrue();
		assertThat(binder.getBindingResult().getFieldErrorCount("spouse.*")).isEqualTo(1);
		assertThat(binder.getBindingResult().getFieldError("spouse.*").getField()).isEqualTo("spouse.name");
	}

	@Test
	public void testCustomFormatterForPrimitiveProperty() {
		TestBean tb = new TestBean();
		DataBinder binder = new DataBinder(tb, "tb");

		binder.addCustomFormatter(new Formatter<Integer>() {
			@Override
			public Integer parse(String text, Locale locale) throws ParseException {
				return 99;
			}
			@Override
			public String print(Integer object, Locale locale) {
				return "argh";
			}
		}, "age");

		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.add("age", "x");
		binder.bind(pvs);

		assertThat(binder.getBindingResult().getFieldValue("age")).isEqualTo("argh");
		assertThat(tb.getAge()).isEqualTo(99);
	}

	@Test
	public void testCustomFormatterForAllStringProperties() {
		TestBean tb = new TestBean();
		DataBinder binder = new DataBinder(tb, "tb");

		binder.addCustomFormatter(new Formatter<String>() {
			@Override
			public String parse(String text, Locale locale) throws ParseException {
				return "prefix" + text;
			}
			@Override
			public String print(String object, Locale locale) {
				return object.substring(6);
			}
		});

		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.add("name", "value");
		pvs.add("touchy", "value");
		binder.bind(pvs);

		binder.getBindingResult().rejectValue("name", "someCode", "someMessage");
		binder.getBindingResult().rejectValue("touchy", "someCode", "someMessage");

		assertThat(binder.getBindingResult().getFieldValue("name")).isEqualTo("value");
		assertThat(binder.getBindingResult().getFieldError("name").getRejectedValue()).isEqualTo("prefixvalue");
		assertThat(tb.getName()).isEqualTo("prefixvalue");
		assertThat(binder.getBindingResult().getFieldValue("touchy")).isEqualTo("value");
		assertThat(binder.getBindingResult().getFieldError("touchy").getRejectedValue()).isEqualTo("prefixvalue");
		assertThat(tb.getTouchy()).isEqualTo("prefixvalue");
	}

	@Test
	public void testJavaBeanPropertyConventions() {
		Book book = new Book();
		DataBinder binder = new DataBinder(book);

		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.add("title", "my book");
		pvs.add("ISBN", "1234");
		pvs.add("NInStock", "5");
		binder.bind(pvs);
		assertThat(book.getTitle()).isEqualTo("my book");
		assertThat(book.getISBN()).isEqualTo("1234");
		assertThat(book.getNInStock()).isEqualTo(5);

		pvs = new MutablePropertyValues();
		pvs.add("Title", "my other book");
		pvs.add("iSBN", "6789");
		pvs.add("nInStock", "0");
		binder.bind(pvs);
		assertThat(book.getTitle()).isEqualTo("my other book");
		assertThat(book.getISBN()).isEqualTo("6789");
		assertThat(book.getNInStock()).isEqualTo(0);
	}

	@Test
	public void testOptionalProperty() {
		OptionalHolder bean = new OptionalHolder();
		DataBinder binder = new DataBinder(bean);
		binder.setConversionService(new DefaultConversionService());

		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.add("id", "1");
		pvs.add("name", null);
		binder.bind(pvs);
		assertThat(bean.getId()).isEqualTo("1");
		assertThat(bean.getName().isPresent()).isFalse();

		pvs = new MutablePropertyValues();
		pvs.add("id", "2");
		pvs.add("name", "myName");
		binder.bind(pvs);
		assertThat(bean.getId()).isEqualTo("2");
		assertThat(bean.getName().get()).isEqualTo("myName");
	}

	@Test
	public void testValidatorNoErrors() throws Exception {
		TestBean tb = new TestBean();
		tb.setAge(33);
		tb.setName("Rod");
		tb.setTouchy("Rod"); // Should not throw
		TestBean tb2 = new TestBean();
		tb2.setAge(34);
		tb.setSpouse(tb2);
		DataBinder db = new DataBinder(tb, "tb");
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.add("spouse.age", "argh");
		db.bind(pvs);
		Errors errors = db.getBindingResult();
		Validator testValidator = new TestBeanValidator();
		testValidator.validate(tb, errors);

		errors.setNestedPath("spouse");
		assertThat(errors.getNestedPath()).isEqualTo("spouse.");
		assertThat(errors.getFieldValue("age")).isEqualTo("argh");
		Validator spouseValidator = new SpouseValidator();
		spouseValidator.validate(tb.getSpouse(), errors);

		errors.setNestedPath("");
		assertThat(errors.getNestedPath()).isEqualTo("");
		errors.pushNestedPath("spouse");
		assertThat(errors.getNestedPath()).isEqualTo("spouse.");
		errors.pushNestedPath("spouse");
		assertThat(errors.getNestedPath()).isEqualTo("spouse.spouse.");
		errors.popNestedPath();
		assertThat(errors.getNestedPath()).isEqualTo("spouse.");
		errors.popNestedPath();
		assertThat(errors.getNestedPath()).isEqualTo("");
		try {
			errors.popNestedPath();
		}
		catch (IllegalStateException ex) {
			// expected, because stack was empty
		}
		errors.pushNestedPath("spouse");
		assertThat(errors.getNestedPath()).isEqualTo("spouse.");
		errors.setNestedPath("");
		assertThat(errors.getNestedPath()).isEqualTo("");
		try {
			errors.popNestedPath();
		}
		catch (IllegalStateException ex) {
			// expected, because stack was reset by setNestedPath
		}

		errors.pushNestedPath("spouse");
		assertThat(errors.getNestedPath()).isEqualTo("spouse.");

		assertThat(errors.getErrorCount()).isEqualTo(1);
		boolean condition1 = !errors.hasGlobalErrors();
		assertThat(condition1).isTrue();
		assertThat(errors.getFieldErrorCount("age")).isEqualTo(1);
		boolean condition = !errors.hasFieldErrors("name");
		assertThat(condition).isTrue();
	}

	@Test
	public void testValidatorWithErrors() {
		TestBean tb = new TestBean();
		tb.setSpouse(new TestBean());

		Errors errors = new BeanPropertyBindingResult(tb, "tb");

		Validator testValidator = new TestBeanValidator();
		testValidator.validate(tb, errors);

		errors.setNestedPath("spouse.");
		assertThat(errors.getNestedPath()).isEqualTo("spouse.");
		Validator spouseValidator = new SpouseValidator();
		spouseValidator.validate(tb.getSpouse(), errors);

		errors.setNestedPath("");
		assertThat(errors.hasErrors()).isTrue();
		assertThat(errors.getErrorCount()).isEqualTo(6);

		assertThat(errors.getGlobalErrorCount()).isEqualTo(2);
		assertThat(errors.getGlobalError().getCode()).isEqualTo("NAME_TOUCHY_MISMATCH");
		assertThat((errors.getGlobalErrors().get(0)).getCode()).isEqualTo("NAME_TOUCHY_MISMATCH");
		assertThat((errors.getGlobalErrors().get(0)).getCodes()[0]).isEqualTo("NAME_TOUCHY_MISMATCH.tb");
		assertThat((errors.getGlobalErrors().get(0)).getCodes()[1]).isEqualTo("NAME_TOUCHY_MISMATCH");
		assertThat((errors.getGlobalErrors().get(0)).getObjectName()).isEqualTo("tb");
		assertThat((errors.getGlobalErrors().get(1)).getCode()).isEqualTo("GENERAL_ERROR");
		assertThat((errors.getGlobalErrors().get(1)).getCodes()[0]).isEqualTo("GENERAL_ERROR.tb");
		assertThat((errors.getGlobalErrors().get(1)).getCodes()[1]).isEqualTo("GENERAL_ERROR");
		assertThat((errors.getGlobalErrors().get(1)).getDefaultMessage()).isEqualTo("msg");
		assertThat((errors.getGlobalErrors().get(1)).getArguments()[0]).isEqualTo("arg");

		assertThat(errors.hasFieldErrors()).isTrue();
		assertThat(errors.getFieldErrorCount()).isEqualTo(4);
		assertThat(errors.getFieldError().getCode()).isEqualTo("TOO_YOUNG");
		assertThat((errors.getFieldErrors().get(0)).getCode()).isEqualTo("TOO_YOUNG");
		assertThat((errors.getFieldErrors().get(0)).getField()).isEqualTo("age");
		assertThat((errors.getFieldErrors().get(1)).getCode()).isEqualTo("AGE_NOT_ODD");
		assertThat((errors.getFieldErrors().get(1)).getField()).isEqualTo("age");
		assertThat((errors.getFieldErrors().get(2)).getCode()).isEqualTo("NOT_ROD");
		assertThat((errors.getFieldErrors().get(2)).getField()).isEqualTo("name");
		assertThat((errors.getFieldErrors().get(3)).getCode()).isEqualTo("TOO_YOUNG");
		assertThat((errors.getFieldErrors().get(3)).getField()).isEqualTo("spouse.age");

		assertThat(errors.hasFieldErrors("age")).isTrue();
		assertThat(errors.getFieldErrorCount("age")).isEqualTo(2);
		assertThat(errors.getFieldError("age").getCode()).isEqualTo("TOO_YOUNG");
		assertThat((errors.getFieldErrors("age").get(0)).getCode()).isEqualTo("TOO_YOUNG");
		assertThat((errors.getFieldErrors("age").get(0)).getObjectName()).isEqualTo("tb");
		assertThat((errors.getFieldErrors("age").get(0)).getField()).isEqualTo("age");
		assertThat((errors.getFieldErrors("age").get(0)).getRejectedValue()).isEqualTo(new Integer(0));
		assertThat((errors.getFieldErrors("age").get(1)).getCode()).isEqualTo("AGE_NOT_ODD");

		assertThat(errors.hasFieldErrors("name")).isTrue();
		assertThat(errors.getFieldErrorCount("name")).isEqualTo(1);
		assertThat(errors.getFieldError("name").getCode()).isEqualTo("NOT_ROD");
		assertThat(errors.getFieldError("name").getCodes()[0]).isEqualTo("NOT_ROD.tb.name");
		assertThat(errors.getFieldError("name").getCodes()[1]).isEqualTo("NOT_ROD.name");
		assertThat(errors.getFieldError("name").getCodes()[2]).isEqualTo("NOT_ROD.java.lang.String");
		assertThat(errors.getFieldError("name").getCodes()[3]).isEqualTo("NOT_ROD");
		assertThat((errors.getFieldErrors("name").get(0)).getField()).isEqualTo("name");
		assertThat((errors.getFieldErrors("name").get(0)).getRejectedValue()).isEqualTo(null);

		assertThat(errors.hasFieldErrors("spouse.age")).isTrue();
		assertThat(errors.getFieldErrorCount("spouse.age")).isEqualTo(1);
		assertThat(errors.getFieldError("spouse.age").getCode()).isEqualTo("TOO_YOUNG");
		assertThat((errors.getFieldErrors("spouse.age").get(0)).getObjectName()).isEqualTo("tb");
		assertThat((errors.getFieldErrors("spouse.age").get(0)).getRejectedValue()).isEqualTo(new Integer(0));
	}

	@Test
	public void testValidatorWithErrorsAndCodesPrefix() {
		TestBean tb = new TestBean();
		tb.setSpouse(new TestBean());

		BeanPropertyBindingResult errors = new BeanPropertyBindingResult(tb, "tb");
		DefaultMessageCodesResolver codesResolver = new DefaultMessageCodesResolver();
		codesResolver.setPrefix("validation.");
		errors.setMessageCodesResolver(codesResolver);

		Validator testValidator = new TestBeanValidator();
		testValidator.validate(tb, errors);

		errors.setNestedPath("spouse.");
		assertThat(errors.getNestedPath()).isEqualTo("spouse.");
		Validator spouseValidator = new SpouseValidator();
		spouseValidator.validate(tb.getSpouse(), errors);

		errors.setNestedPath("");
		assertThat(errors.hasErrors()).isTrue();
		assertThat(errors.getErrorCount()).isEqualTo(6);

		assertThat(errors.getGlobalErrorCount()).isEqualTo(2);
		assertThat(errors.getGlobalError().getCode()).isEqualTo("validation.NAME_TOUCHY_MISMATCH");
		assertThat((errors.getGlobalErrors().get(0)).getCode()).isEqualTo("validation.NAME_TOUCHY_MISMATCH");
		assertThat((errors.getGlobalErrors().get(0)).getCodes()[0]).isEqualTo("validation.NAME_TOUCHY_MISMATCH.tb");
		assertThat((errors.getGlobalErrors().get(0)).getCodes()[1]).isEqualTo("validation.NAME_TOUCHY_MISMATCH");
		assertThat((errors.getGlobalErrors().get(0)).getObjectName()).isEqualTo("tb");
		assertThat((errors.getGlobalErrors().get(1)).getCode()).isEqualTo("validation.GENERAL_ERROR");
		assertThat((errors.getGlobalErrors().get(1)).getCodes()[0]).isEqualTo("validation.GENERAL_ERROR.tb");
		assertThat((errors.getGlobalErrors().get(1)).getCodes()[1]).isEqualTo("validation.GENERAL_ERROR");
		assertThat((errors.getGlobalErrors().get(1)).getDefaultMessage()).isEqualTo("msg");
		assertThat((errors.getGlobalErrors().get(1)).getArguments()[0]).isEqualTo("arg");

		assertThat(errors.hasFieldErrors()).isTrue();
		assertThat(errors.getFieldErrorCount()).isEqualTo(4);
		assertThat(errors.getFieldError().getCode()).isEqualTo("validation.TOO_YOUNG");
		assertThat((errors.getFieldErrors().get(0)).getCode()).isEqualTo("validation.TOO_YOUNG");
		assertThat((errors.getFieldErrors().get(0)).getField()).isEqualTo("age");
		assertThat((errors.getFieldErrors().get(1)).getCode()).isEqualTo("validation.AGE_NOT_ODD");
		assertThat((errors.getFieldErrors().get(1)).getField()).isEqualTo("age");
		assertThat((errors.getFieldErrors().get(2)).getCode()).isEqualTo("validation.NOT_ROD");
		assertThat((errors.getFieldErrors().get(2)).getField()).isEqualTo("name");
		assertThat((errors.getFieldErrors().get(3)).getCode()).isEqualTo("validation.TOO_YOUNG");
		assertThat((errors.getFieldErrors().get(3)).getField()).isEqualTo("spouse.age");

		assertThat(errors.hasFieldErrors("age")).isTrue();
		assertThat(errors.getFieldErrorCount("age")).isEqualTo(2);
		assertThat(errors.getFieldError("age").getCode()).isEqualTo("validation.TOO_YOUNG");
		assertThat((errors.getFieldErrors("age").get(0)).getCode()).isEqualTo("validation.TOO_YOUNG");
		assertThat((errors.getFieldErrors("age").get(0)).getObjectName()).isEqualTo("tb");
		assertThat((errors.getFieldErrors("age").get(0)).getField()).isEqualTo("age");
		assertThat((errors.getFieldErrors("age").get(0)).getRejectedValue()).isEqualTo(new Integer(0));
		assertThat((errors.getFieldErrors("age").get(1)).getCode()).isEqualTo("validation.AGE_NOT_ODD");

		assertThat(errors.hasFieldErrors("name")).isTrue();
		assertThat(errors.getFieldErrorCount("name")).isEqualTo(1);
		assertThat(errors.getFieldError("name").getCode()).isEqualTo("validation.NOT_ROD");
		assertThat(errors.getFieldError("name").getCodes()[0]).isEqualTo("validation.NOT_ROD.tb.name");
		assertThat(errors.getFieldError("name").getCodes()[1]).isEqualTo("validation.NOT_ROD.name");
		assertThat(errors.getFieldError("name").getCodes()[2]).isEqualTo("validation.NOT_ROD.java.lang.String");
		assertThat(errors.getFieldError("name").getCodes()[3]).isEqualTo("validation.NOT_ROD");
		assertThat((errors.getFieldErrors("name").get(0)).getField()).isEqualTo("name");
		assertThat((errors.getFieldErrors("name").get(0)).getRejectedValue()).isEqualTo(null);

		assertThat(errors.hasFieldErrors("spouse.age")).isTrue();
		assertThat(errors.getFieldErrorCount("spouse.age")).isEqualTo(1);
		assertThat(errors.getFieldError("spouse.age").getCode()).isEqualTo("validation.TOO_YOUNG");
		assertThat((errors.getFieldErrors("spouse.age").get(0)).getObjectName()).isEqualTo("tb");
		assertThat((errors.getFieldErrors("spouse.age").get(0)).getRejectedValue()).isEqualTo(new Integer(0));
	}

	@Test
	public void testValidatorWithNestedObjectNull() {
		TestBean tb = new TestBean();
		Errors errors = new BeanPropertyBindingResult(tb, "tb");
		Validator testValidator = new TestBeanValidator();
		testValidator.validate(tb, errors);
		errors.setNestedPath("spouse.");
		assertThat(errors.getNestedPath()).isEqualTo("spouse.");
		Validator spouseValidator = new SpouseValidator();
		spouseValidator.validate(tb.getSpouse(), errors);
		errors.setNestedPath("");

		assertThat(errors.hasFieldErrors("spouse")).isTrue();
		assertThat(errors.getFieldErrorCount("spouse")).isEqualTo(1);
		assertThat(errors.getFieldError("spouse").getCode()).isEqualTo("SPOUSE_NOT_AVAILABLE");
		assertThat((errors.getFieldErrors("spouse").get(0)).getObjectName()).isEqualTo("tb");
		assertThat((errors.getFieldErrors("spouse").get(0)).getRejectedValue()).isEqualTo(null);
	}

	@Test
	public void testNestedValidatorWithoutNestedPath() {
		TestBean tb = new TestBean();
		tb.setName("XXX");
		Errors errors = new BeanPropertyBindingResult(tb, "tb");
		Validator spouseValidator = new SpouseValidator();
		spouseValidator.validate(tb, errors);

		assertThat(errors.hasGlobalErrors()).isTrue();
		assertThat(errors.getGlobalErrorCount()).isEqualTo(1);
		assertThat(errors.getGlobalError().getCode()).isEqualTo("SPOUSE_NOT_AVAILABLE");
		assertThat((errors.getGlobalErrors().get(0)).getObjectName()).isEqualTo("tb");
	}

	@Test
	public void testBindingStringArrayToIntegerSet() {
		IndexedTestBean tb = new IndexedTestBean();
		DataBinder binder = new DataBinder(tb, "tb");
		binder.registerCustomEditor(Set.class, new CustomCollectionEditor(TreeSet.class) {
			@Override
			protected Object convertElement(Object element) {
				return new Integer(element.toString());
			}
		});
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.add("set", new String[] {"10", "20", "30"});
		binder.bind(pvs);

		assertThat(binder.getBindingResult().getFieldValue("set")).isEqualTo(tb.getSet());
		boolean condition = tb.getSet() instanceof TreeSet;
		assertThat(condition).isTrue();
		assertThat(tb.getSet().size()).isEqualTo(3);
		assertThat(tb.getSet().contains(new Integer(10))).isTrue();
		assertThat(tb.getSet().contains(new Integer(20))).isTrue();
		assertThat(tb.getSet().contains(new Integer(30))).isTrue();

		pvs = new MutablePropertyValues();
		pvs.add("set", null);
		binder.bind(pvs);

		assertThat(tb.getSet()).isNull();
	}

	@Test
	public void testBindingNullToEmptyCollection() {
		IndexedTestBean tb = new IndexedTestBean();
		DataBinder binder = new DataBinder(tb, "tb");
		binder.registerCustomEditor(Set.class, new CustomCollectionEditor(TreeSet.class, true));
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.add("set", null);
		binder.bind(pvs);

		boolean condition = tb.getSet() instanceof TreeSet;
		assertThat(condition).isTrue();
		assertThat(tb.getSet().isEmpty()).isTrue();
	}

	@Test
	public void testBindingToIndexedField() {
		IndexedTestBean tb = new IndexedTestBean();
		DataBinder binder = new DataBinder(tb, "tb");
		binder.registerCustomEditor(String.class, "array.name", new PropertyEditorSupport() {
			@Override
			public void setAsText(String text) throws IllegalArgumentException {
				setValue("array" + text);
			}
		});
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.add("array[0]", "a");
		binder.bind(pvs);
		Errors errors = binder.getBindingResult();
		errors.rejectValue("array[0].name", "NOT_ROD", "are you sure you're not Rod?");
		errors.rejectValue("map[key1].name", "NOT_ROD", "are you sure you're not Rod?");

		assertThat(errors.getFieldErrorCount("array[0].name")).isEqualTo(1);
		assertThat(errors.getFieldError("array[0].name").getCode()).isEqualTo("NOT_ROD");
		assertThat(errors.getFieldError("array[0].name").getCodes()[0]).isEqualTo("NOT_ROD.tb.array[0].name");
		assertThat(errors.getFieldError("array[0].name").getCodes()[1]).isEqualTo("NOT_ROD.tb.array.name");
		assertThat(errors.getFieldError("array[0].name").getCodes()[2]).isEqualTo("NOT_ROD.array[0].name");
		assertThat(errors.getFieldError("array[0].name").getCodes()[3]).isEqualTo("NOT_ROD.array.name");
		assertThat(errors.getFieldError("array[0].name").getCodes()[4]).isEqualTo("NOT_ROD.name");
		assertThat(errors.getFieldError("array[0].name").getCodes()[5]).isEqualTo("NOT_ROD.java.lang.String");
		assertThat(errors.getFieldError("array[0].name").getCodes()[6]).isEqualTo("NOT_ROD");
		assertThat(errors.getFieldErrorCount("map[key1].name")).isEqualTo(1);
		assertThat(errors.getFieldErrorCount("map['key1'].name")).isEqualTo(1);
		assertThat(errors.getFieldErrorCount("map[\"key1\"].name")).isEqualTo(1);
		assertThat(errors.getFieldError("map[key1].name").getCode()).isEqualTo("NOT_ROD");
		assertThat(errors.getFieldError("map[key1].name").getCodes()[0]).isEqualTo("NOT_ROD.tb.map[key1].name");
		assertThat(errors.getFieldError("map[key1].name").getCodes()[1]).isEqualTo("NOT_ROD.tb.map.name");
		assertThat(errors.getFieldError("map[key1].name").getCodes()[2]).isEqualTo("NOT_ROD.map[key1].name");
		assertThat(errors.getFieldError("map[key1].name").getCodes()[3]).isEqualTo("NOT_ROD.map.name");
		assertThat(errors.getFieldError("map[key1].name").getCodes()[4]).isEqualTo("NOT_ROD.name");
		assertThat(errors.getFieldError("map[key1].name").getCodes()[5]).isEqualTo("NOT_ROD.java.lang.String");
		assertThat(errors.getFieldError("map[key1].name").getCodes()[6]).isEqualTo("NOT_ROD");
	}

	@Test
	public void testBindingToNestedIndexedField() {
		IndexedTestBean tb = new IndexedTestBean();
		tb.getArray()[0].setNestedIndexedBean(new IndexedTestBean());
		tb.getArray()[1].setNestedIndexedBean(new IndexedTestBean());
		DataBinder binder = new DataBinder(tb, "tb");
		binder.registerCustomEditor(String.class, "array.nestedIndexedBean.list.name", new PropertyEditorSupport() {
			@Override
			public void setAsText(String text) throws IllegalArgumentException {
				setValue("list" + text);
			}
		});
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.add("array[0].nestedIndexedBean.list[0].name", "a");
		binder.bind(pvs);
		Errors errors = binder.getBindingResult();
		errors.rejectValue("array[0].nestedIndexedBean.list[0].name", "NOT_ROD", "are you sure you're not Rod?");

		assertThat(errors.getFieldErrorCount("array[0].nestedIndexedBean.list[0].name")).isEqualTo(1);
		assertThat(errors.getFieldError("array[0].nestedIndexedBean.list[0].name").getCode()).isEqualTo("NOT_ROD");
		assertThat(errors.getFieldError("array[0].nestedIndexedBean.list[0].name").getCodes()[0]).isEqualTo("NOT_ROD.tb.array[0].nestedIndexedBean.list[0].name");
		assertThat(errors.getFieldError("array[0].nestedIndexedBean.list[0].name").getCodes()[1]).isEqualTo("NOT_ROD.tb.array[0].nestedIndexedBean.list.name");
		assertThat(errors.getFieldError("array[0].nestedIndexedBean.list[0].name").getCodes()[2]).isEqualTo("NOT_ROD.tb.array.nestedIndexedBean.list.name");
		assertThat(errors.getFieldError("array[0].nestedIndexedBean.list[0].name").getCodes()[3]).isEqualTo("NOT_ROD.array[0].nestedIndexedBean.list[0].name");
		assertThat(errors.getFieldError("array[0].nestedIndexedBean.list[0].name").getCodes()[4]).isEqualTo("NOT_ROD.array[0].nestedIndexedBean.list.name");
		assertThat(errors.getFieldError("array[0].nestedIndexedBean.list[0].name").getCodes()[5]).isEqualTo("NOT_ROD.array.nestedIndexedBean.list.name");
		assertThat(errors.getFieldError("array[0].nestedIndexedBean.list[0].name").getCodes()[6]).isEqualTo("NOT_ROD.name");
		assertThat(errors.getFieldError("array[0].nestedIndexedBean.list[0].name").getCodes()[7]).isEqualTo("NOT_ROD.java.lang.String");
		assertThat(errors.getFieldError("array[0].nestedIndexedBean.list[0].name").getCodes()[8]).isEqualTo("NOT_ROD");
	}

	@Test
	public void testEditorForNestedIndexedField() {
		IndexedTestBean tb = new IndexedTestBean();
		tb.getArray()[0].setNestedIndexedBean(new IndexedTestBean());
		tb.getArray()[1].setNestedIndexedBean(new IndexedTestBean());
		DataBinder binder = new DataBinder(tb, "tb");
		binder.registerCustomEditor(String.class, "array.nestedIndexedBean.list.name", new PropertyEditorSupport() {
			@Override
			public void setAsText(String text) throws IllegalArgumentException {
				setValue("list" + text);
			}
			@Override
			public String getAsText() {
				return ((String) getValue()).substring(4);
			}
		});
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.add("array[0].nestedIndexedBean.list[0].name", "test1");
		pvs.add("array[1].nestedIndexedBean.list[1].name", "test2");
		binder.bind(pvs);
		assertThat(((TestBean) tb.getArray()[0].getNestedIndexedBean().getList().get(0)).getName()).isEqualTo("listtest1");
		assertThat(((TestBean) tb.getArray()[1].getNestedIndexedBean().getList().get(1)).getName()).isEqualTo("listtest2");
		assertThat(binder.getBindingResult().getFieldValue("array[0].nestedIndexedBean.list[0].name")).isEqualTo("test1");
		assertThat(binder.getBindingResult().getFieldValue("array[1].nestedIndexedBean.list[1].name")).isEqualTo("test2");
	}

	@Test
	public void testSpecificEditorForNestedIndexedField() {
		IndexedTestBean tb = new IndexedTestBean();
		tb.getArray()[0].setNestedIndexedBean(new IndexedTestBean());
		tb.getArray()[1].setNestedIndexedBean(new IndexedTestBean());
		DataBinder binder = new DataBinder(tb, "tb");
		binder.registerCustomEditor(String.class, "array[0].nestedIndexedBean.list.name", new PropertyEditorSupport() {
			@Override
			public void setAsText(String text) throws IllegalArgumentException {
				setValue("list" + text);
			}
			@Override
			public String getAsText() {
				return ((String) getValue()).substring(4);
			}
		});
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.add("array[0].nestedIndexedBean.list[0].name", "test1");
		pvs.add("array[1].nestedIndexedBean.list[1].name", "test2");
		binder.bind(pvs);
		assertThat(((TestBean) tb.getArray()[0].getNestedIndexedBean().getList().get(0)).getName()).isEqualTo("listtest1");
		assertThat(((TestBean) tb.getArray()[1].getNestedIndexedBean().getList().get(1)).getName()).isEqualTo("test2");
		assertThat(binder.getBindingResult().getFieldValue("array[0].nestedIndexedBean.list[0].name")).isEqualTo("test1");
		assertThat(binder.getBindingResult().getFieldValue("array[1].nestedIndexedBean.list[1].name")).isEqualTo("test2");
	}

	@Test
	public void testInnerSpecificEditorForNestedIndexedField() {
		IndexedTestBean tb = new IndexedTestBean();
		tb.getArray()[0].setNestedIndexedBean(new IndexedTestBean());
		tb.getArray()[1].setNestedIndexedBean(new IndexedTestBean());
		DataBinder binder = new DataBinder(tb, "tb");
		binder.registerCustomEditor(String.class, "array.nestedIndexedBean.list[0].name", new PropertyEditorSupport() {
			@Override
			public void setAsText(String text) throws IllegalArgumentException {
				setValue("list" + text);
			}
			@Override
			public String getAsText() {
				return ((String) getValue()).substring(4);
			}
		});
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.add("array[0].nestedIndexedBean.list[0].name", "test1");
		pvs.add("array[1].nestedIndexedBean.list[1].name", "test2");
		binder.bind(pvs);
		assertThat(((TestBean) tb.getArray()[0].getNestedIndexedBean().getList().get(0)).getName()).isEqualTo("listtest1");
		assertThat(((TestBean) tb.getArray()[1].getNestedIndexedBean().getList().get(1)).getName()).isEqualTo("test2");
		assertThat(binder.getBindingResult().getFieldValue("array[0].nestedIndexedBean.list[0].name")).isEqualTo("test1");
		assertThat(binder.getBindingResult().getFieldValue("array[1].nestedIndexedBean.list[1].name")).isEqualTo("test2");
	}

	@Test
	public void testDirectBindingToIndexedField() {
		IndexedTestBean tb = new IndexedTestBean();
		DataBinder binder = new DataBinder(tb, "tb");
		binder.registerCustomEditor(TestBean.class, "array", new PropertyEditorSupport() {
			@Override
			public void setAsText(String text) throws IllegalArgumentException {
				DerivedTestBean tb = new DerivedTestBean();
				tb.setName("array" + text);
				setValue(tb);
			}
			@Override
			public String getAsText() {
				return ((TestBean) getValue()).getName();
			}
		});
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.add("array[0]", "a");
		binder.bind(pvs);
		Errors errors = binder.getBindingResult();
		errors.rejectValue("array[0]", "NOT_ROD", "are you sure you're not Rod?");
		errors.rejectValue("map[key1]", "NOT_ROD", "are you sure you're not Rod?");
		errors.rejectValue("map[key0]", "NOT_NULL", "should not be null");

		assertThat(errors.getFieldValue("array[0]")).isEqualTo("arraya");
		assertThat(errors.getFieldErrorCount("array[0]")).isEqualTo(1);
		assertThat(errors.getFieldError("array[0]").getCode()).isEqualTo("NOT_ROD");
		assertThat(errors.getFieldError("array[0]").getCodes()[0]).isEqualTo("NOT_ROD.tb.array[0]");
		assertThat(errors.getFieldError("array[0]").getCodes()[1]).isEqualTo("NOT_ROD.tb.array");
		assertThat(errors.getFieldError("array[0]").getCodes()[2]).isEqualTo("NOT_ROD.array[0]");
		assertThat(errors.getFieldError("array[0]").getCodes()[3]).isEqualTo("NOT_ROD.array");
		assertThat(errors.getFieldError("array[0]").getCodes()[4]).isEqualTo("NOT_ROD.org.springframework.beans.testfixture.beans.DerivedTestBean");
		assertThat(errors.getFieldError("array[0]").getCodes()[5]).isEqualTo("NOT_ROD");
		assertThat(errors.getFieldValue("array[0]")).isEqualTo("arraya");

		assertThat(errors.getFieldErrorCount("map[key1]")).isEqualTo(1);
		assertThat(errors.getFieldError("map[key1]").getCode()).isEqualTo("NOT_ROD");
		assertThat(errors.getFieldError("map[key1]").getCodes()[0]).isEqualTo("NOT_ROD.tb.map[key1]");
		assertThat(errors.getFieldError("map[key1]").getCodes()[1]).isEqualTo("NOT_ROD.tb.map");
		assertThat(errors.getFieldError("map[key1]").getCodes()[2]).isEqualTo("NOT_ROD.map[key1]");
		assertThat(errors.getFieldError("map[key1]").getCodes()[3]).isEqualTo("NOT_ROD.map");
		assertThat(errors.getFieldError("map[key1]").getCodes()[4]).isEqualTo("NOT_ROD.org.springframework.beans.testfixture.beans.TestBean");
		assertThat(errors.getFieldError("map[key1]").getCodes()[5]).isEqualTo("NOT_ROD");

		assertThat(errors.getFieldErrorCount("map[key0]")).isEqualTo(1);
		assertThat(errors.getFieldError("map[key0]").getCode()).isEqualTo("NOT_NULL");
		assertThat(errors.getFieldError("map[key0]").getCodes()[0]).isEqualTo("NOT_NULL.tb.map[key0]");
		assertThat(errors.getFieldError("map[key0]").getCodes()[1]).isEqualTo("NOT_NULL.tb.map");
		assertThat(errors.getFieldError("map[key0]").getCodes()[2]).isEqualTo("NOT_NULL.map[key0]");
		assertThat(errors.getFieldError("map[key0]").getCodes()[3]).isEqualTo("NOT_NULL.map");
		assertThat(errors.getFieldError("map[key0]").getCodes()[4]).isEqualTo("NOT_NULL");
	}

	@Test
	public void testDirectBindingToEmptyIndexedFieldWithRegisteredSpecificEditor() {
		IndexedTestBean tb = new IndexedTestBean();
		DataBinder binder = new DataBinder(tb, "tb");
		binder.registerCustomEditor(TestBean.class, "map[key0]", new PropertyEditorSupport() {
			@Override
			public void setAsText(String text) throws IllegalArgumentException {
				DerivedTestBean tb = new DerivedTestBean();
				tb.setName("array" + text);
				setValue(tb);
			}
			@Override
			public String getAsText() {
				return ((TestBean) getValue()).getName();
			}
		});
		Errors errors = binder.getBindingResult();
		errors.rejectValue("map[key0]", "NOT_NULL", "should not be null");

		assertThat(errors.getFieldErrorCount("map[key0]")).isEqualTo(1);
		assertThat(errors.getFieldError("map[key0]").getCode()).isEqualTo("NOT_NULL");
		assertThat(errors.getFieldError("map[key0]").getCodes()[0]).isEqualTo("NOT_NULL.tb.map[key0]");
		assertThat(errors.getFieldError("map[key0]").getCodes()[1]).isEqualTo("NOT_NULL.tb.map");
		assertThat(errors.getFieldError("map[key0]").getCodes()[2]).isEqualTo("NOT_NULL.map[key0]");
		assertThat(errors.getFieldError("map[key0]").getCodes()[3]).isEqualTo("NOT_NULL.map");
		// This next code is only generated because of the registered editor, using the
		// registered type of the editor as guess for the content type of the collection.
		assertThat(errors.getFieldError("map[key0]").getCodes()[4]).isEqualTo("NOT_NULL.org.springframework.beans.testfixture.beans.TestBean");
		assertThat(errors.getFieldError("map[key0]").getCodes()[5]).isEqualTo("NOT_NULL");
	}

	@Test
	public void testDirectBindingToEmptyIndexedFieldWithRegisteredGenericEditor() {
		IndexedTestBean tb = new IndexedTestBean();
		DataBinder binder = new DataBinder(tb, "tb");
		binder.registerCustomEditor(TestBean.class, "map", new PropertyEditorSupport() {
			@Override
			public void setAsText(String text) throws IllegalArgumentException {
				DerivedTestBean tb = new DerivedTestBean();
				tb.setName("array" + text);
				setValue(tb);
			}
			@Override
			public String getAsText() {
				return ((TestBean) getValue()).getName();
			}
		});
		Errors errors = binder.getBindingResult();
		errors.rejectValue("map[key0]", "NOT_NULL", "should not be null");

		assertThat(errors.getFieldErrorCount("map[key0]")).isEqualTo(1);
		assertThat(errors.getFieldError("map[key0]").getCode()).isEqualTo("NOT_NULL");
		assertThat(errors.getFieldError("map[key0]").getCodes()[0]).isEqualTo("NOT_NULL.tb.map[key0]");
		assertThat(errors.getFieldError("map[key0]").getCodes()[1]).isEqualTo("NOT_NULL.tb.map");
		assertThat(errors.getFieldError("map[key0]").getCodes()[2]).isEqualTo("NOT_NULL.map[key0]");
		assertThat(errors.getFieldError("map[key0]").getCodes()[3]).isEqualTo("NOT_NULL.map");
		// This next code is only generated because of the registered editor, using the
		// registered type of the editor as guess for the content type of the collection.
		assertThat(errors.getFieldError("map[key0]").getCodes()[4]).isEqualTo("NOT_NULL.org.springframework.beans.testfixture.beans.TestBean");
		assertThat(errors.getFieldError("map[key0]").getCodes()[5]).isEqualTo("NOT_NULL");
	}

	@Test
	public void testCustomEditorWithSubclass() {
		IndexedTestBean tb = new IndexedTestBean();
		DataBinder binder = new DataBinder(tb, "tb");
		binder.registerCustomEditor(TestBean.class, new PropertyEditorSupport() {
			@Override
			public void setAsText(String text) throws IllegalArgumentException {
				DerivedTestBean tb = new DerivedTestBean();
				tb.setName("array" + text);
				setValue(tb);
			}
			@Override
			public String getAsText() {
				return ((TestBean) getValue()).getName();
			}
		});
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.add("array[0]", "a");
		binder.bind(pvs);
		Errors errors = binder.getBindingResult();
		errors.rejectValue("array[0]", "NOT_ROD", "are you sure you're not Rod?");

		assertThat(errors.getFieldValue("array[0]")).isEqualTo("arraya");
		assertThat(errors.getFieldErrorCount("array[0]")).isEqualTo(1);
		assertThat(errors.getFieldError("array[0]").getCode()).isEqualTo("NOT_ROD");
		assertThat(errors.getFieldError("array[0]").getCodes()[0]).isEqualTo("NOT_ROD.tb.array[0]");
		assertThat(errors.getFieldError("array[0]").getCodes()[1]).isEqualTo("NOT_ROD.tb.array");
		assertThat(errors.getFieldError("array[0]").getCodes()[2]).isEqualTo("NOT_ROD.array[0]");
		assertThat(errors.getFieldError("array[0]").getCodes()[3]).isEqualTo("NOT_ROD.array");
		assertThat(errors.getFieldError("array[0]").getCodes()[4]).isEqualTo("NOT_ROD.org.springframework.beans.testfixture.beans.DerivedTestBean");
		assertThat(errors.getFieldError("array[0]").getCodes()[5]).isEqualTo("NOT_ROD");
		assertThat(errors.getFieldValue("array[0]")).isEqualTo("arraya");
	}

	@Test
	public void testBindToStringArrayWithArrayEditor() {
		TestBean tb = new TestBean();
		DataBinder binder = new DataBinder(tb, "tb");
		binder.registerCustomEditor(String[].class, "stringArray", new PropertyEditorSupport() {
			@Override
			public void setAsText(String text) throws IllegalArgumentException {
				setValue(StringUtils.delimitedListToStringArray(text, "-"));
			}
		});
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.add("stringArray", "a1-b2");
		binder.bind(pvs);
		boolean condition = !binder.getBindingResult().hasErrors();
		assertThat(condition).isTrue();
		assertThat(tb.getStringArray().length).isEqualTo(2);
		assertThat(tb.getStringArray()[0]).isEqualTo("a1");
		assertThat(tb.getStringArray()[1]).isEqualTo("b2");
	}

	@Test
	public void testBindToStringArrayWithComponentEditor() {
		TestBean tb = new TestBean();
		DataBinder binder = new DataBinder(tb, "tb");
		binder.registerCustomEditor(String.class, "stringArray", new PropertyEditorSupport() {
			@Override
			public void setAsText(String text) throws IllegalArgumentException {
				setValue("X" + text);
			}
		});
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.add("stringArray", new String[] {"a1", "b2"});
		binder.bind(pvs);
		boolean condition = !binder.getBindingResult().hasErrors();
		assertThat(condition).isTrue();
		assertThat(tb.getStringArray().length).isEqualTo(2);
		assertThat(tb.getStringArray()[0]).isEqualTo("Xa1");
		assertThat(tb.getStringArray()[1]).isEqualTo("Xb2");
	}

	@Test
	public void testBindingErrors() {
		TestBean rod = new TestBean();
		DataBinder binder = new DataBinder(rod, "person");
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.add("age", "32x");
		binder.bind(pvs);
		Errors errors = binder.getBindingResult();
		FieldError ageError = errors.getFieldError("age");
		assertThat(ageError.getCode()).isEqualTo("typeMismatch");

		ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
		messageSource.setBasename("org.springframework.validation.messages1");
		String msg = messageSource.getMessage(ageError, Locale.getDefault());
		assertThat(msg).isEqualTo("Field age did not have correct type");

		messageSource = new ResourceBundleMessageSource();
		messageSource.setBasename("org.springframework.validation.messages2");
		msg = messageSource.getMessage(ageError, Locale.getDefault());
		assertThat(msg).isEqualTo("Field Age did not have correct type");

		messageSource = new ResourceBundleMessageSource();
		messageSource.setBasename("org.springframework.validation.messages3");
		msg = messageSource.getMessage(ageError, Locale.getDefault());
		assertThat(msg).isEqualTo("Field Person Age did not have correct type");
	}

	@Test
	public void testAddAllErrors() {
		TestBean rod = new TestBean();
		DataBinder binder = new DataBinder(rod, "person");
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.add("age", "32x");
		binder.bind(pvs);
		Errors errors = binder.getBindingResult();

		BeanPropertyBindingResult errors2 = new BeanPropertyBindingResult(rod, "person");
		errors.rejectValue("name", "badName");
		errors.addAllErrors(errors2);

		FieldError ageError = errors.getFieldError("age");
		assertThat(ageError.getCode()).isEqualTo("typeMismatch");
		FieldError nameError = errors.getFieldError("name");
		assertThat(nameError.getCode()).isEqualTo("badName");
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testBindingWithResortedList() {
		IndexedTestBean tb = new IndexedTestBean();
		DataBinder binder = new DataBinder(tb, "tb");
		MutablePropertyValues pvs = new MutablePropertyValues();
		TestBean tb1 = new TestBean("tb1", 99);
		TestBean tb2 = new TestBean("tb2", 99);
		pvs.add("list[0]", tb1);
		pvs.add("list[1]", tb2);
		binder.bind(pvs);
		assertThat(binder.getBindingResult().getFieldValue("list[0].name")).isEqualTo(tb1.getName());
		assertThat(binder.getBindingResult().getFieldValue("list[1].name")).isEqualTo(tb2.getName());
		tb.getList().set(0, tb2);
		tb.getList().set(1, tb1);
		assertThat(binder.getBindingResult().getFieldValue("list[0].name")).isEqualTo(tb2.getName());
		assertThat(binder.getBindingResult().getFieldValue("list[1].name")).isEqualTo(tb1.getName());
	}

	@Test
	public void testRejectWithoutDefaultMessage() {
		TestBean tb = new TestBean();
		tb.setName("myName");
		tb.setAge(99);

		BeanPropertyBindingResult ex = new BeanPropertyBindingResult(tb, "tb");
		ex.reject("invalid");
		ex.rejectValue("age", "invalidField");

		StaticMessageSource ms = new StaticMessageSource();
		ms.addMessage("invalid", Locale.US, "general error");
		ms.addMessage("invalidField", Locale.US, "invalid field");

		assertThat(ms.getMessage(ex.getGlobalError(), Locale.US)).isEqualTo("general error");
		assertThat(ms.getMessage(ex.getFieldError("age"), Locale.US)).isEqualTo("invalid field");
	}

	@Test
	public void testBindExceptionSerializable() throws Exception {
		SerializablePerson tb = new SerializablePerson();
		tb.setName("myName");
		tb.setAge(99);

		BindException ex = new BindException(tb, "tb");
		ex.reject("invalid", "someMessage");
		ex.rejectValue("age", "invalidField", "someMessage");

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(ex);
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		ObjectInputStream ois = new ObjectInputStream(bais);

		BindException ex2 = (BindException) ois.readObject();
		assertThat(ex2.hasGlobalErrors()).isTrue();
		assertThat(ex2.getGlobalError().getCode()).isEqualTo("invalid");
		assertThat(ex2.hasFieldErrors("age")).isTrue();
		assertThat(ex2.getFieldError("age").getCode()).isEqualTo("invalidField");
		assertThat(ex2.getFieldValue("age")).isEqualTo(new Integer(99));

		ex2.rejectValue("name", "invalidField", "someMessage");
		assertThat(ex2.hasFieldErrors("name")).isTrue();
		assertThat(ex2.getFieldError("name").getCode()).isEqualTo("invalidField");
		assertThat(ex2.getFieldValue("name")).isEqualTo("myName");
	}

	@Test
	public void testTrackDisallowedFields() {
		TestBean testBean = new TestBean();
		DataBinder binder = new DataBinder(testBean, "testBean");
		binder.setAllowedFields("name", "age");

		String name = "Rob Harrop";
		String beanName = "foobar";

		MutablePropertyValues mpvs = new MutablePropertyValues();
		mpvs.add("name", name);
		mpvs.add("beanName", beanName);
		binder.bind(mpvs);

		assertThat(testBean.getName()).isEqualTo(name);
		String[] disallowedFields = binder.getBindingResult().getSuppressedFields();
		assertThat(disallowedFields.length).isEqualTo(1);
		assertThat(disallowedFields[0]).isEqualTo("beanName");
	}

	@Test
	public void testAutoGrowWithinDefaultLimit() {
		TestBean testBean = new TestBean();
		DataBinder binder = new DataBinder(testBean, "testBean");

		MutablePropertyValues mpvs = new MutablePropertyValues();
		mpvs.add("friends[4]", "");
		binder.bind(mpvs);

		assertThat(testBean.getFriends().size()).isEqualTo(5);
	}

	@Test
	public void testAutoGrowBeyondDefaultLimit() {
		TestBean testBean = new TestBean();
		DataBinder binder = new DataBinder(testBean, "testBean");

		MutablePropertyValues mpvs = new MutablePropertyValues();
		mpvs.add("friends[256]", "");
		assertThatExceptionOfType(InvalidPropertyException.class).isThrownBy(() ->
				binder.bind(mpvs))
			.satisfies(ex -> assertThat(ex.getRootCause()).isInstanceOf(IndexOutOfBoundsException.class));
	}

	@Test
	public void testAutoGrowWithinCustomLimit() {
		TestBean testBean = new TestBean();
		DataBinder binder = new DataBinder(testBean, "testBean");
		binder.setAutoGrowCollectionLimit(10);

		MutablePropertyValues mpvs = new MutablePropertyValues();
		mpvs.add("friends[4]", "");
		binder.bind(mpvs);

		assertThat(testBean.getFriends().size()).isEqualTo(5);
	}

	@Test
	public void testAutoGrowBeyondCustomLimit() {
		TestBean testBean = new TestBean();
		DataBinder binder = new DataBinder(testBean, "testBean");
		binder.setAutoGrowCollectionLimit(10);

		MutablePropertyValues mpvs = new MutablePropertyValues();
		mpvs.add("friends[16]", "");
		assertThatExceptionOfType(InvalidPropertyException.class).isThrownBy(() ->
				binder.bind(mpvs))
			.satisfies(ex -> assertThat(ex.getRootCause()).isInstanceOf(IndexOutOfBoundsException.class));
	}

	@Test
	public void testNestedGrowingList() {
		Form form = new Form();
		DataBinder binder = new DataBinder(form, "form");
		MutablePropertyValues mpv = new MutablePropertyValues();
		mpv.add("f[list][0]", "firstValue");
		mpv.add("f[list][1]", "secondValue");
		binder.bind(mpv);
		assertThat(binder.getBindingResult().hasErrors()).isFalse();
		@SuppressWarnings("unchecked")
		List<Object> list = (List<Object>) form.getF().get("list");
		assertThat(list.get(0)).isEqualTo("firstValue");
		assertThat(list.get(1)).isEqualTo("secondValue");
		assertThat(list.size()).isEqualTo(2);
	}

	@Test
	public void testFieldErrorAccessVariations() {
		TestBean testBean = new TestBean();
		DataBinder binder = new DataBinder(testBean, "testBean");
		assertThat(binder.getBindingResult().getGlobalError()).isNull();
		assertThat(binder.getBindingResult().getFieldError()).isNull();
		assertThat(binder.getBindingResult().getFieldError("")).isNull();

		MutablePropertyValues mpv = new MutablePropertyValues();
		mpv.add("age", "invalid");
		binder.bind(mpv);
		assertThat(binder.getBindingResult().getGlobalError()).isNull();
		assertThat(binder.getBindingResult().getFieldError("")).isNull();
		assertThat(binder.getBindingResult().getFieldError("b*")).isNull();
		assertThat(binder.getBindingResult().getFieldError().getField()).isEqualTo("age");
		assertThat(binder.getBindingResult().getFieldError("*").getField()).isEqualTo("age");
		assertThat(binder.getBindingResult().getFieldError("a*").getField()).isEqualTo("age");
		assertThat(binder.getBindingResult().getFieldError("ag*").getField()).isEqualTo("age");
		assertThat(binder.getBindingResult().getFieldError("age").getField()).isEqualTo("age");
	}

	@Test  // SPR-14888
	public void testSetAutoGrowCollectionLimit() {
		BeanWithIntegerList tb = new BeanWithIntegerList();
		DataBinder binder = new DataBinder(tb);
		binder.setAutoGrowCollectionLimit(257);
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.add("integerList[256]", "1");

		binder.bind(pvs);
		assertThat(tb.getIntegerList().size()).isEqualTo(257);
		assertThat(tb.getIntegerList().get(256)).isEqualTo(Integer.valueOf(1));
		assertThat(binder.getBindingResult().getFieldValue("integerList[256]")).isEqualTo(Integer.valueOf(1));
	}

	@Test  // SPR-14888
	public void testSetAutoGrowCollectionLimitAfterInitialization() {
		DataBinder binder = new DataBinder(new BeanWithIntegerList());
		binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
		assertThatIllegalStateException().isThrownBy(() ->
				binder.setAutoGrowCollectionLimit(257))
			.withMessageContaining("DataBinder is already initialized - call setAutoGrowCollectionLimit before other configuration methods");
	}

	@Test // SPR-15009
	public void testSetCustomMessageCodesResolverBeforeInitializeBindingResultForBeanPropertyAccess() {
		TestBean testBean = new TestBean();
		DataBinder binder = new DataBinder(testBean, "testBean");
		DefaultMessageCodesResolver messageCodesResolver = new DefaultMessageCodesResolver();
		messageCodesResolver.setPrefix("errors.");
		binder.setMessageCodesResolver(messageCodesResolver);
		binder.setAutoGrowCollectionLimit(512); // allow configuration after set a MessageCodesResolver
		binder.initBeanPropertyAccess();

		MutablePropertyValues mpv = new MutablePropertyValues();
		mpv.add("age", "invalid");
		binder.bind(mpv);
		assertThat(binder.getBindingResult().getFieldError("age").getCode()).isEqualTo("errors.typeMismatch");
		assertThat(BeanWrapper.class.cast(binder.getInternalBindingResult().getPropertyAccessor()).getAutoGrowCollectionLimit()).isEqualTo(512);
	}

	@Test // SPR-15009
	public void testSetCustomMessageCodesResolverBeforeInitializeBindingResultForDirectFieldAccess() {
		TestBean testBean = new TestBean();
		DataBinder binder = new DataBinder(testBean, "testBean");
		DefaultMessageCodesResolver messageCodesResolver = new DefaultMessageCodesResolver();
		messageCodesResolver.setPrefix("errors.");
		binder.setMessageCodesResolver(messageCodesResolver);
		binder.initDirectFieldAccess();

		MutablePropertyValues mpv = new MutablePropertyValues();
		mpv.add("age", "invalid");
		binder.bind(mpv);
		assertThat(binder.getBindingResult().getFieldError("age").getCode()).isEqualTo("errors.typeMismatch");
	}

	@Test  // SPR-15009
	public void testSetCustomMessageCodesResolverAfterInitializeBindingResult() {
		TestBean testBean = new TestBean();
		DataBinder binder = new DataBinder(testBean, "testBean");
		binder.initBeanPropertyAccess();
		DefaultMessageCodesResolver messageCodesResolver = new DefaultMessageCodesResolver();
		messageCodesResolver.setPrefix("errors.");
		binder.setMessageCodesResolver(messageCodesResolver);

		MutablePropertyValues mpv = new MutablePropertyValues();
		mpv.add("age", "invalid");
		binder.bind(mpv);
		assertThat(binder.getBindingResult().getFieldError("age").getCode()).isEqualTo("errors.typeMismatch");
	}

	@Test  // SPR-15009
	public void testSetMessageCodesResolverIsNullAfterInitializeBindingResult() {
		TestBean testBean = new TestBean();
		DataBinder binder = new DataBinder(testBean, "testBean");
		binder.initBeanPropertyAccess();
		binder.setMessageCodesResolver(null);

		MutablePropertyValues mpv = new MutablePropertyValues();
		mpv.add("age", "invalid");
		binder.bind(mpv);
		// Keep a default MessageCodesResolver
		assertThat(binder.getBindingResult().getFieldError("age").getCode()).isEqualTo("typeMismatch");
	}

	@Test  // SPR-15009
	public void testCallSetMessageCodesResolverTwice() {

		TestBean testBean = new TestBean();
		DataBinder binder = new DataBinder(testBean, "testBean");
		binder.setMessageCodesResolver(new DefaultMessageCodesResolver());
		assertThatIllegalStateException().isThrownBy(() ->
				binder.setMessageCodesResolver(new DefaultMessageCodesResolver()))
			.withMessageContaining("DataBinder is already initialized with MessageCodesResolver");

	}

	@SuppressWarnings("unused")
	private static class BeanWithIntegerList {

		private List<Integer> integerList;

		public List<Integer> getIntegerList() {
			return integerList;
		}

		public void setIntegerList(List<Integer> integerList) {
			this.integerList = integerList;
		}
	}


	private static class Book {

		private String Title;

		private String ISBN;

		private int nInStock;

		public String getTitle() {
			return Title;
		}

		@SuppressWarnings("unused")
		public void setTitle(String title) {
			Title = title;
		}

		public String getISBN() {
			return ISBN;
		}

		@SuppressWarnings("unused")
		public void setISBN(String ISBN) {
			this.ISBN = ISBN;
		}

		public int getNInStock() {
			return nInStock;
		}

		@SuppressWarnings("unused")
		public void setNInStock(int nInStock) {
			this.nInStock = nInStock;
		}
	}


	private static class OptionalHolder {

		private String id;

		private Optional<String> name;

		public String getId() {
			return id;
		}

		@SuppressWarnings("unused")
		public void setId(String id) {
			this.id = id;
		}

		public Optional<String> getName() {
			return name;
		}

		@SuppressWarnings("unused")
		public void setName(Optional<String> name) {
			this.name = name;
		}
	}


	private static class TestBeanValidator implements Validator {

		@Override
		public boolean supports(Class<?> clazz) {
			return TestBean.class.isAssignableFrom(clazz);
		}

		@Override
		public void validate(@Nullable Object obj, Errors errors) {
			TestBean tb = (TestBean) obj;
			if (tb.getAge() < 32) {
				errors.rejectValue("age", "TOO_YOUNG", "simply too young");
			}
			if (tb.getAge() % 2 == 0) {
				errors.rejectValue("age", "AGE_NOT_ODD", "your age isn't odd");
			}
			if (tb.getName() == null || !tb.getName().equals("Rod")) {
				errors.rejectValue("name", "NOT_ROD", "are you sure you're not Rod?");
			}
			if (tb.getTouchy() == null || !tb.getTouchy().equals(tb.getName())) {
				errors.reject("NAME_TOUCHY_MISMATCH", "name and touchy do not match");
			}
			if (tb.getAge() == 0) {
				errors.reject("GENERAL_ERROR", new String[] {"arg"}, "msg");
			}
		}
	}


	private static class SpouseValidator implements Validator {

		@Override
		public boolean supports(Class<?> clazz) {
			return TestBean.class.isAssignableFrom(clazz);
		}

		@Override
		public void validate(@Nullable Object obj, Errors errors) {
			TestBean tb = (TestBean) obj;
			if (tb == null || "XXX".equals(tb.getName())) {
				errors.rejectValue("", "SPOUSE_NOT_AVAILABLE");
				return;
			}
			if (tb.getAge() < 32) {
				errors.rejectValue("age", "TOO_YOUNG", "simply too young");
			}
		}
	}


	@SuppressWarnings("unused")
	private static class GrowingList<E> extends AbstractList<E> {

		private List<E> list;

		public GrowingList() {
			this.list = new ArrayList<>();
		}

		public List<E> getWrappedList() {
			return list;
		}

		@Override
		public E get(int index) {
			if (index >= list.size()) {
				for (int i = list.size(); i < index; i++) {
					list.add(null);
				}
				list.add(null);
				return null;
			}
			else {
				return list.get(index);
			}
		}

		@Override
		public int size() {
			return list.size();
		}

		@Override
		public boolean add(E o) {
			return list.add(o);
		}

		@Override
		public void add(int index, E element) {
			list.add(index, element);
		}

		@Override
		public boolean addAll(int index, Collection<? extends E> c) {
			return list.addAll(index, c);
		}

		@Override
		public void clear() {
			list.clear();
		}

		@Override
		public int indexOf(Object o) {
			return list.indexOf(o);
		}

		@Override
		public Iterator<E> iterator() {
			return list.iterator();
		}

		@Override
		public int lastIndexOf(Object o) {
			return list.lastIndexOf(o);
		}

		@Override
		public ListIterator<E> listIterator() {
			return list.listIterator();
		}

		@Override
		public ListIterator<E> listIterator(int index) {
			return list.listIterator(index);
		}

		@Override
		public E remove(int index) {
			return list.remove(index);
		}

		@Override
		public E set(int index, E element) {
			return list.set(index, element);
		}
	}


	private static class Form {

		private final Map<String, Object> f;

		public Form() {
			f = new HashMap<>();
			f.put("list", new GrowingList<>());
		}

		public Map<String, Object> getF() {
			return f;
		}
	}


	public static class NameBean {

		private final String name;

		public NameBean(String name) {
			this.name = name;
		}
		public String getName() {
			return name;
		}
		@Override
		public String toString() {
			return name;
		}
	}


	public static class NameBeanConverter implements Converter<NameBean, String> {

		@Override
		public String convert(NameBean source) {
			return "[" + source.getName() + "]";
		}
	}

}
