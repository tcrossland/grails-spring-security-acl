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
package grails.plugin.springsecurity.acl

import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException
import test.TestClassAnnotatedService

/**
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */
class TestClassAnnotatedServiceSpec extends AbstractAclSpec {

	TestClassAnnotatedService testClassAnnotatedService

	void 'check that the notAnnotated method inherits ROLE_ADMIN from the class annotation'() {

		when:
		testClassAnnotatedService.notAnnotated()

		then:
		thrown AuthenticationCredentialsNotFoundException

		when:
		authenticateAsUser()
		testClassAnnotatedService.notAnnotated()

		then:
		thrown AccessDeniedException

		when:
		authenticateAsAdmin()
		testClassAnnotatedService.notAnnotated()

		then:
		notThrown()
	}

	void 'check that the userAnnotated method overides the class annotation and requires ROLE_USER'() {

		when:
		testClassAnnotatedService.userAnnotated()

		then:
		thrown AuthenticationCredentialsNotFoundException

		when:
		authenticateAsAdmin()
		testClassAnnotatedService.userAnnotated()

		then:
		thrown AccessDeniedException

		when:
		authenticateAsUser()
		testClassAnnotatedService.userAnnotated()

		then:
		notThrown()
	}
}
