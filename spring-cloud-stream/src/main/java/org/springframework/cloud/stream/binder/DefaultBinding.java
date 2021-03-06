/*
 * Copyright 2013-2018 the original author or authors.
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

package org.springframework.cloud.stream.binder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.context.Lifecycle;
import org.springframework.integration.support.context.NamedComponent;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * Default implementation for a {@link Binding}.
 * @author Jennifer Hickey
 * @author Mark Fisher
 * @author Gary Russell
 * @author Marius Bogoevici
 * @author Oleg Zhurakousky
 * 
 * @see org.springframework.cloud.stream.annotation.EnableBinding
 */
public class DefaultBinding<T> implements Binding<T> {
	
	private final Log logger = LogFactory.getLog(this.getClass().getName());

	protected final String name;

	protected final String group;

	protected final T target;

	protected final Lifecycle lifecycle;

	/**
	 * Creates an instance that associates a given name, group and binding target with an
	 * optional {@link Lifecycle} component, which will be stopped during unbinding.
	 *
	 * @param name the name of the binding target
	 * @param group the group (only for input targets)
	 * @param target the binding target
	 * @param lifecycle {@link Lifecycle} that runs while the binding is active and will
	 * be stopped during unbinding
	 */
	public DefaultBinding(String name, String group, T target, Lifecycle lifecycle) {
		Assert.notNull(target, "target must not be null");
		this.name = name;
		this.group = group;
		this.target = target;
		this.lifecycle = lifecycle;
	}

	public String getName() {
		return this.name;
	}

	public String getGroup() {
		return this.group;
	}
	
	public boolean isRunning() {
		return this.lifecycle != null && this.lifecycle.isRunning();
	}
	
	@Override
	public final synchronized void start() {
		if (!this.isRunning()) {
			if (this.lifecycle != null && StringUtils.hasText(this.group)) {
				this.lifecycle.start();
			}
			else {
				logger.warn("Can not re-bind an anonymous binding");
			}
		}
	}
	
	@Override
	public final synchronized void stop() {
		if (this.isRunning()) {
			this.lifecycle.stop();
		}
	}

	@Override
	public final void unbind() {
		this.stop();
		afterUnbind();
	}

	Lifecycle getEndpoint() {
		return this.lifecycle;
	}

	/**
	 * Listener method that executes after unbinding. Subclasses can implement their own
	 * behaviour on unbinding by overriding this method.
	 */
	protected void afterUnbind() {
	}

	@Override
	public String toString() {
		return " Binding [name=" + this.name + ", target=" + this.target + ", lifecycle="
				+ ((this.lifecycle instanceof NamedComponent)
						? ((NamedComponent) this.lifecycle).getComponentName()
						: ObjectUtils.nullSafeToString(this.lifecycle))
				+ "]";
	}
}
