import grails.test.ControllerUnitTestCase
import grails.plugins.springsecurity.SpringSecurityService
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils

class LoginControllerTests extends ControllerUnitTestCase {

    protected void setUp() {
        super.setUp()
        def mockedConfig = new ConfigObject()
        mockedConfig.successHandler.defaultTargetUrl = "defaultTargetUrl"
        SpringSecurityUtils.metaClass.'static'.getSecurityConfig = { return mockedConfig }
    }

    void testIndexLoggedIn() {
        def springSecurityService = mockFor(SpringSecurityService)
        springSecurityService.demand.isLoggedIn(1) {-> return true}
        controller.springSecurityService = springSecurityService.createMock()

        controller.index()
        springSecurityService.verify()

        assertEquals('defaultTargetUrl', redirectArgs.uri)
    }

    void testIndexNotLoggedIn() {
        def springSecurityService = mockFor(SpringSecurityService)
        springSecurityService.demand.isLoggedIn(1) {-> return false}
        controller.springSecurityService = springSecurityService.createMock()

        controller.index()
        springSecurityService.verify()

        assertEquals(controller.auth, redirectArgs.action)
    }
}
