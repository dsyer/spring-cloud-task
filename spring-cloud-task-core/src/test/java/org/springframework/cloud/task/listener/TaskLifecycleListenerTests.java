/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.task.listener;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.context.event.ApplicationFailedEvent;
import org.springframework.cloud.task.repository.TaskExecution;
import org.springframework.cloud.task.repository.TaskExplorer;
import org.springframework.cloud.task.util.TestDefaultConfiguration;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

/**
 * Verifies that the TaskLifecycleListener Methods record the appropriate log header entries and
 * result codes.
 *
 * @author Glenn Renfro
 * @author Michael Minella
 */
public class TaskLifecycleListenerTests {

	private AnnotationConfigApplicationContext context;

	private TaskExplorer taskExplorer;

	@Before
	public void setUp() {
		context = new AnnotationConfigApplicationContext();
		context.setId("testTask");
		context.register(TestDefaultConfiguration.class, PropertyPlaceholderAutoConfiguration.class);
	}

	@After
	public void tearDown() {
		context.close();
	}

	@Test
	public void testTaskCreate() {
		context.refresh();
		verifyTaskExecution(0, false, 0, null);
	}

	@Test
	public void testTaskCreateWithArgs() {
		context.register(ArgsConfiguration.class);
		context.refresh();
		verifyTaskExecution(2, false, 0, null);
	}

	@Test
	public void testTaskUpdate() {
		context.refresh();

		context.publishEvent(new ContextClosedEvent(context));

		verifyTaskExecution(0, true, 0, null);
	}

	@Test
	public void testTaskFailedUpdate() {
		context.refresh();
		RuntimeException exception = new RuntimeException("This was expected");
		context.publishEvent(new ApplicationFailedEvent(new SpringApplication(), new String[0], context, exception));
		context.publishEvent(new ContextClosedEvent(context));

		verifyTaskExecution(0, true, 1, exception);
	}

	private static String stackTraceToString(Throwable exception) {
		StringWriter writer = new StringWriter();
		PrintWriter printWriter = new PrintWriter(writer);

		exception.printStackTrace(printWriter);

		return writer.toString();
	}

	private void verifyTaskExecution(int numberOfParams, boolean update, int exitCode, Throwable exception) {
		this.taskExplorer = context.getBean(TaskExplorer.class);

		Sort sort = new Sort("id");

		PageRequest request = new PageRequest(0, Integer.MAX_VALUE, sort);

		Page<TaskExecution> taskExecutionsByName = this.taskExplorer.findTaskExecutionsByName("testTask",
				request);
		assertTrue(taskExecutionsByName.iterator().hasNext());
		TaskExecution taskExecution = taskExecutionsByName.iterator().next();

		assertEquals(numberOfParams, taskExecution.getParameters().size());
		assertEquals(exitCode, taskExecution.getExitCode());

		if(exception != null) {
			assertTrue(taskExecution.getExitMessage().length() > exception.getStackTrace().length);
		}
		else {
			assertNull(taskExecution.getExitMessage());
		}

		if(update) {
			assertTrue(taskExecution.getEndTime().getTime() >= taskExecution.getStartTime().getTime());
		}
		else {
			assertNull(taskExecution.getEndTime());
		}

		assertEquals("testTask", taskExecution.getTaskName());
	}

	@Configuration
	public static class ArgsConfiguration {

		@Bean
		public ApplicationArguments args() {
			Map<String, String> args = new HashMap<>(2);

			args.put("foo", "bar");
			args.put("baz", "qux");

			return new SimpleApplicationArgs(args);
		}
	}

	private static class SimpleApplicationArgs implements ApplicationArguments {

		private Map<String, String> args;

		public SimpleApplicationArgs(Map<String, String> args) {
			this.args = args;
		}

		@Override
		public String[] getSourceArgs() {
			String [] sourceArgs = new String[this.args.size()];

			int i = 0;
			for (Map.Entry<String, String> stringStringEntry : args.entrySet()) {
				sourceArgs[i] = "--" + stringStringEntry.getKey() + "=" + stringStringEntry.getValue();
				i++;
			}

			return sourceArgs;
		}

		@Override
		public Set<String> getOptionNames() {
			return this.args.keySet();
		}

		@Override
		public boolean containsOption(String s) {
			return this.args.containsKey(s);
		}

		@Override
		public List<String> getOptionValues(String s) {
			return Arrays.asList(this.args.get(s));
		}

		@Override
		public List<String> getNonOptionArgs() {
			throw new UnsupportedOperationException("Not supported at this time.");
		}
	}
}
