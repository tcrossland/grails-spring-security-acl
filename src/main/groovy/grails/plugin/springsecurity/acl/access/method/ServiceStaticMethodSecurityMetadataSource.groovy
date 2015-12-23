/* Copyright 2009-2015 the original author or authors.
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
package grails.plugin.springsecurity.acl.access.method

import grails.plugin.springsecurity.acl.util.ProxyUtils
import groovy.transform.CompileStatic
import org.springframework.security.access.ConfigAttribute
import org.springframework.security.access.SecurityConfig
import org.springframework.security.access.method.AbstractFallbackMethodSecurityMetadataSource

import java.lang.reflect.Method

/**
 * <code>MethodSecurityMetadataSource</code> that's populated by 'springSecurityACL' blocks in services.
 *
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */
@CompileStatic
class ServiceStaticMethodSecurityMetadataSource extends AbstractFallbackMethodSecurityMetadataSource {

	protected final Map<String, Map<String, List<ConfigAttribute>>> methodConfigs = [:]
	protected final Map<String, List<ConfigAttribute>> classConfigs = [:]

	@Override
	protected Collection<ConfigAttribute> findAttributes(Class<?> clazz) {
		classConfigs[ProxyUtils.unproxy(clazz).name]
	}

	@Override
	protected Collection<ConfigAttribute> findAttributes(Method method, Class<?> targetClass) {
		methodConfigs[ProxyUtils.unproxy(targetClass).name]?.get(ProxyUtils.unproxy(method).name)
	}

	Collection<ConfigAttribute> getAllConfigAttributes() {}

	/**
	 * Dependency injection for class-scope rules.
	 * @param classConfigNames  keys are class names, values are one or more tokens to apply,
	 * e.g. ROLE_ADMIN, AFTER_ACL_COLLECTION_READ.
	 */
	void setClassConfigNames(Map<String, List<String>> classConfigNames) {
		if (classConfigNames) {
			populateMap classConfigs, classConfigNames
		}
	}

	/**
	 * Dependency injection for method-scope rules.
	 * @param methodConfigNames  keys are class names, values are maps with method name keys and
	 * values are one or more tokens to apply, e.g. ROLE_ADMIN, AFTER_ACL_COLLECTION_READ.
	 */
	void setMethodConfigNames(Map<String, Map<String, List<String>>> methodConfigNames) {
		methodConfigNames.each { String key, Map<String, List<String>> value ->
			Map<String, List<ConfigAttribute>> configs = [:]
			populateMap configs, value
			methodConfigs[key] = configs
		}
	}

	protected void populateMap(Map<String, List<ConfigAttribute>> dest, Map<String, List<String>> source) {
		source.each { String key, List<String> value ->
			dest[key] = value.collect { String config -> new SecurityConfig(config) } as List
		}
	}
}
