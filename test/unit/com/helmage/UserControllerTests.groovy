package com.helmage

import grails.test.*
import grails.plugins.springsecurity.SpringSecurityService
import org.bson.types.ObjectId

class UserControllerTests extends ControllerUnitTestCase {
    /**
     * Ensure that the happy path save works
     */
    void testSave() {
        def springSecurityService = mockFor(SpringSecurityService)
        springSecurityService.demand.encodePassword(1) {password, username -> return 'hashedPass'}
        controller.springSecurityService = springSecurityService.createMock()
        mockDomain(User)

        controller.params.username = 'user'
        controller.params.password = 'pass'
        controller.save()

        springSecurityService.verify()

        assertEquals("The user was created", controller.flash.message)
        assertEquals("show", redirectArgs.action)

        def userInstances = User.list()
        assertEquals(1, userInstances.size())
        assertEquals('user', userInstances[0].username)
        assertEquals('hashedPass', userInstances[0].password)
        assertEquals(userInstances[0].id, redirectArgs.id)
    }

    /**
     * Ensure when simulating a save failure, things are dealt with as expected
     */
    void testSaveFail() {
        def springSecurityService = mockFor(SpringSecurityService)
        springSecurityService.demand.encodePassword(1) {password, username -> return 'hashedPass'}
        controller.springSecurityService = springSecurityService.createMock()

        User.metaClass.save = {flush -> return false}

        controller.params.username = 'user'
        controller.params.password = 'pass'
        controller.save()

        springSecurityService.verify()

        assertEquals('create', controller.modelAndView.viewName)
        assertEquals('user', controller.modelAndView.model.linkedHashMap.userInstance.username)
        assertEquals('hashedPass', controller.modelAndView.model.linkedHashMap.userInstance.password)

        GroovySystem.metaClassRegistry.removeMetaClass(User)
    }

    /**
     * Ensure that the happy path update works
     */
    void testUpdate() {
        def user = new User(id: new ObjectId(), username: 'user', password: 'pass', enabled: true)
        mockDomain(User, [user])

        def springSecurityService = mockFor(SpringSecurityService)
        springSecurityService.demand.encodePassword(1) {password, username -> return 'hashedPass'}
        springSecurityService.demand.isLoggedIn(1) {-> return true}
        springSecurityService.demand.getPrincipal(1) {-> return [username: user.username]}
        springSecurityService.demand.reauthenticate(1) {username -> }
        controller.springSecurityService = springSecurityService.createMock()

        controller.params.id = user.id.toString()
        controller.params.username = user.username
        controller.params.password = 'newPass'
        controller.params.enabled = 'false'

        controller.update()

        springSecurityService.verify()

        assertEquals("The user was updated", controller.flash.message)
        assertEquals("show", redirectArgs.action)
        assertEquals(user.id, redirectArgs.id)
        def users = User.list()
        assertEquals(1, users.size())
        assertEquals(user.username, users[0].username)
        assertEquals('hashedPass', users[0].password)
        assertEquals(false, users[0].enabled)
    }

    /**
     * Ensure when simulating an update failure, things are dealt with as expected
     */
    void testUpdateFail() {
        def user = new User(id: new ObjectId(), username: 'user', password: 'pass')
        def userId2 = new ObjectId()
        mockDomain(User, [user, new User(id: userId2, username: 'user2', password: 'pass')])

        controller.params.id = userId2.toString()
        controller.params.username = user.username
        controller.params.password = user.password

        controller.update()

        assertEquals('edit', controller.modelAndView.viewName)
        assertEquals(user.username, controller.modelAndView.model.linkedHashMap.userInstance.username)
        assertEquals(user.password, controller.modelAndView.model.linkedHashMap.userInstance.password)
    }
}
