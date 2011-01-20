package com.helmage

class UserController {
    static scaffold = User

    def springSecurityService

    def save = {
       def userInstance = new User(params)
       userInstance.password = springSecurityService.encodePassword(
                     params.password, userInstance.username)
       if (!userInstance.save(flush: true)) {
          render view: 'create', model: [userInstance: userInstance]
          return
       }

       flash.message = "The user was created"
       redirect action: 'show', id: userInstance.id
    }

    def update = {
       def userInstance = User.get(params.id)

       if (userInstance.password != params.password) {
          params.password = springSecurityService.encodePassword(
                     params.password, userInstance.username)
       }
       userInstance.properties = params
       if (!userInstance.save(flush: true)) {
          render view: 'edit', model: [userInstance: userInstance]
          return
       }

       if (springSecurityService.isLoggedIn() &&
                springSecurityService.principal.username == userInstance.username) {
          springSecurityService.reauthenticate userInstance.username
       }

       flash.message = "The user was updated"
       redirect action: 'show', id: userInstance.id
    }
}
