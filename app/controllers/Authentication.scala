package controllers

import play.api._
import play.api.mvc._
import play.api.cache.Cache
import play.api.Logger
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.Crypto
import scala.util.Random
import scala.concurrent.Future

import models.Secure.RedirectUrlRE
import models.Secure.Tools
import models.Secure.Toolbar
import models.Secure.Link
import models.Vault
import models._

object Authentication extends Controller with Tools {

  val maxAge:Option[Int] = Some(86400) //a day
  
  var loginForm: Form[Secure.Login] = Form(
    mapping(
      "username" -> text,
      "password" -> text
    )
    ((username, password) => Secure.Login(username, password))
	((sec: Secure.Login) => Some((sec.username, sec.password)))
  )
  
  def Authenticated[A](action: Action[A]): Action[A] = {
  	Action.async(action.parser) { implicit request =>
      if (Secure.isAuthorized(request)) action(request)  
      else {	Future.successful(Ok(views.html.login(loginForm))
      	.withCookies(Cookie("adcms_state", "0"), Cookie("adcms_user","")))
      }
    }
  }

  def login = Authenticated { Action { implicit request =>
    Redirect("/dashboard/")
  }}
  
  def logout = Action { implicit request =>
    Redirect("/login/").withSession(request2session - "user")
    	.withCookies(Cookie("adcms_state", "0"), Cookie("adcms_user",""))
  }
  
  def authenticate =  Action { implicit request =>
    loginForm.bindFromRequest.fold(
      hasErrors = { form =>
        BadRequest(views.html.login(loginForm))
      },
      
      success = { secureCreds => 
        //TODO: set initial login somewhere else, ref it here
        var admin = Secure.Login("admin", "admin")
        if (secureCreds.username == "admin" && !Secure.authorize(admin)) {
        	Vault.writeNew(admin)
        }
    	var creds = Secure.Login(secureCreds.username, secureCreds.password)
        val win:Boolean = Secure.authorize(creds)
        if (win) {
          val encodedUser = Secure.encodeUser(secureCreds.username)
    	  Redirect("/").withSession(request2session + ("user" -> encodedUser))
        	.withCookies(Cookie("adcms_state", "1", maxAge), Cookie("adcms_user", creds.username, maxAge))
        } else {
          Redirect("/login/")
        	.withCookies(Cookie("adcms_state", "3"), Cookie("adcms_user",""))
        }
      })
  } 
  
  def tools = Action { implicit request =>
    Ok(views.html.toolbar())
  }
}

object ManageUsers extends Controller with Tools {
  
  def manage = Action { implicit request =>
    if (Secure.isAuthorized(request) && Secure.isAdmin(request)) { 
      Ok(views.html.loginmanager(Secure.userList()))
    }
    else Redirect("/")
  }
  
  def create = Action { implicit request =>
    Ok(views.html.logincreator(Authentication.loginForm))
  }
  
  def addUser =  Action { implicit request =>
    Authentication.loginForm.bindFromRequest.fold(
      hasErrors = { form =>
        BadRequest(views.html.index())
      },
      
      success = { secureCreds =>    
        var creds = Secure.Login(secureCreds.username, secureCreds.password)
		if (Secure.isAuthorized(request) && Secure.isAdmin(request)) { 
      	  Vault.writeNew(creds)
      	  Ok(views.html.loginmanager(Secure.userList()))
      	} else Ok("not admin")
    })
  } 
  
  def removeUser =  Action { implicit request =>
    Authentication.loginForm.bindFromRequest.fold(
      hasErrors = { form =>
        BadRequest(views.html.index())
      },
      
      success = { secureCreds =>    
        var creds = Secure.Login(secureCreds.username, secureCreds.password)
		if (Secure.isAuthorized(request) && Secure.isAdmin(request)) { 
      	  Vault.remove(creds)
      	  Ok(views.html.loginmanager(Secure.userList()))
      	} else Ok("not admin")
    })
  } 
  
  def updateUser =  Action { implicit request =>
    Authentication.loginForm.bindFromRequest.fold(
      hasErrors = { form =>
        BadRequest(views.html.index())
      },
      
      success = { secureCreds =>    
        var creds = Secure.Login(secureCreds.username, secureCreds.password)
		if (Secure.isAuthorized(request) && Secure.isAdmin(request)) { 
      	  Vault.remove(creds)
      	  Ok(views.html.loginmanager(Secure.userList()))
      	} else Ok("not admin")
    })
  } 
  
}