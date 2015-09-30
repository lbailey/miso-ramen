package controllers

import play.api._
import play.api.Logger
import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._

import controllers.Authentication.Authenticated
import controllers.Authentication.loginForm

import models.Location.GPS
import models.Homebound
import models.HomeboundOps
import models.Sponsor
import models.SponsorOps

import models.Secure
import models.Secure.Tools

object Service extends Controller with Tools {

  def empty = Action { implicit request =>	
      Ok("")
  }
  
  def uniqueLogin(id:String) = Action { implicit request =>	
      val check:Boolean = !Secure.checkUnique(id)
      Ok(check.toString)
  }
}