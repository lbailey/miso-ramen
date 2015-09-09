package controllers

import play.api._
import play.api.mvc._
//import play.api.cache.Cache
//import play.api.Play.current
//import play.api.db._

import models.Secure
import models.Secure.Tools
import controllers.Authentication.Authenticated

object Application extends Controller with Tools {

  def pingServer = Action {
    Ok("Server is up.")
  }
  
  def index = Authenticated { Action { implicit request =>
    Ok(views.html.index())
  }}
}
