import geb.spock.GebReportingSpec
import grails.test.mixin.integration.Integration
import grails.transaction.Rollback
import pages.LoginPage

@Integration
@Rollback
abstract class AbstractSecuritySpec extends GebReportingSpec {

	void setupSpec() {
		go 'testData/reset'
	}

	void setup() {
		logout()
	}

	protected void login(String user) {
		to LoginPage
		username = user
		password = 'password'
		loginButton.click()
	}

	protected void logout() {
		go 'logout'
		browser.clearCookies()
	}

	protected void assertContentContains(String expected) {
		assert $().text().contains(expected)
	}

	protected void assertContentDoesNotContain(String unexpected) {
		assert !$().text().contains(unexpected)
	}
}
