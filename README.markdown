Grails, Spring Security, and Mongodb

Chris Smith, December 5, 2010
Twitter: http://twitter.com/evilangelist42

I'm currently writing a web-based game (http://helmage.com) and thought I would take a look at Grails, Spring Security, and Mongodb for the platform.  I'm noob for all these technologies, so I need to do bunch of learning on each, but here's my first crack at it.  Hope you can find it useful.  See README.html for better formatting.

Prerequisites:
    * Grails 1.3.5 is setup


1. Create the app:
        grails create-app

2. Setup the plugins:
        grails uninstall-plugin hibernate
        grails install-plugin spring-security-core
        grails install-plugin mongodb

3. Create the default spring-security domain objects, controller classes, and GSP views.  This will also update Config.groovy with properties that point to the new security domain classes.
        grails s2-quickstart com.helmage User Role
    
4. Add an id property of type org.bson.types.ObjectId for each of the domain classes.  This is more ideal for Mongodb.  Remove the 'id composite' line from the UserRole mapping directive because composite primary keys are not supported in this case.

5. Modify the DataSource.groovy config file to have Mongodb specific configuration.  Remove all the hibernate specific configuration.

6. Setup Mongodb:
    - Make sure Mongodb is running (<MONGODB_HOME>/bin/mongod)
    - Startup the Mongodb shell (<MONGODB_HOME>/bin/mongo)
    - Switch to the database specified in the grails config: > use helmage-dev
    - Create the user specified in the grails config: > db.addUser("helmage-web","helmage");

7. Create controllers for User, Role, UserRole so we can add some users easily:
        grails create-controller com.helmage.User
        grails create-controller com.helmage.Role
        grails create-controller com.helmage.UserRole

10. Setup the UserController, RoleController and the UserRoleController to use scaffolding (static scaffold = &lt;domainClass&gt;).

11. Update the UserController 'save' and 'update' actions to generate a password that is hashed with the username used as the salt (as per the spring-security-core docs http://burtbeckwith.github.com/grails-spring-security-core/docs/manual/index.html). Configure spring-security to base64 encode the password, and know to use the username as the salt in Config.groovy:
		grails.plugins.springsecurity.password.encodeHashAsBase64=true
		grails.plugins.springsecurity.dao.reflectionSaltSourceProperty = 'username'

12. Create our own implementation of the UserDetailsService to remove any Hibernate specific references and to use the Mongodb GORM setup.  To do this, a MongoUserDetailsService class is created that implements GrailsUserDetailsService.  It is pretty much a copy of GrailsUserDetailsService, but with a few tweaks for the Mongodb setup.
        grails create-service com.helmage.MongoUserDetails

    - Update the resources.groovy file to now user this implementation of UserDetailsService:

		beans = {
			userDetailsService(com.helmage.MongoUserDetailsService)
		}

13. Setup a URL map to configure Spring Security.  Make sure when setting up role names in spring security to prefix with 'ROLE_' (this is configurable through the RoleVoter component by specifying rolePrefix, see the Spring Security docs for more information).  Update Config.groovy:
		grails.plugins.springsecurity.securityConfigType = SecurityConfigType.InterceptUrlMap
		grails.plugins.springsecurity.interceptUrlMap = [
			'/secure/**':    ['ROLE_USER','IS_AUTHENTICATED_REMEMBERED'],
			'/js/**':        ['IS_AUTHENTICATED_ANONYMOUSLY'],
			'/css/**':       ['IS_AUTHENTICATED_ANONYMOUSLY'],
			'/images/**':    ['IS_AUTHENTICATED_ANONYMOUSLY'],
			'/*':            ['IS_AUTHENTICATED_ANONYMOUSLY'],
			'/login/**':     ['IS_AUTHENTICATED_ANONYMOUSLY'],
			'/logout/**':    ['IS_AUTHENTICATED_ANONYMOUSLY']
		]

14. Create a test controller that will sit under the /secure/ directory to test that authentication will work.
        grails create-controller com.helmage.Secure

    - Add a view to display the currently logged on user using the spring-security plugin taglib

15. Run the application.
    - Using the built-in Grails CRUD pages, create a role named 'ROLE_USER'
    - Create a user (with the enabled property checked)
    - Create a UserRole that specifies the user and role just created
    - browse to the SecureController and voila!

Next Steps:
    * refactor UserRole so HQL isn't used because it isn't supported in the Mongodb GORM implementation
    * probably not a bad idea to secure the User, Role, and UserRole controllers :)
    * setup tests for all the objects created


References:
    * Grails Doc: http://www.mongodb.org/display/DOCS/Tutorial
    * Grails Mongodb plugin Doc: http://grails.github.com/inconsequential/mongo/manual/index.html
    * Mongodb: http://www.mongodb.org/display/DOCS/Tutorial
    * Spring Security Core Grails plugin: http://www.mongodb.org/display/DOCS/Tutorial
