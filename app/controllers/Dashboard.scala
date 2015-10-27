package controllers

import play.api._
import play.api.mvc._

import models.Secure
import models.Secure.Tools
import models.HomeboundOps
import models.SponsorOps

import controllers.Authentication.Authenticated
import controllers.Authentication.loginForm

object Dashboard extends Controller with Tools {
  
  def index = Authenticated { Action { implicit request =>
    if (Secure.isAdmin(request)) { 
      Ok(views.html.admindash(HomeboundOps.all(), SponsorOps.all()))
    }
    else Ok(views.html.dashboard(Authentication.loginForm))
  }}
  
  def account = Authenticated { Action { implicit request =>
	  Ok(views.html.account())
    } 
  }
  
  def adminTools = Authenticated { Action { implicit request =>
	  Ok("")
    } 
  }
  
  
}