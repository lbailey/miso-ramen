package controllers

import play.api._
import play.api.Logger
import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._

import org.bson.types.ObjectId

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
  
  def giveAdminPerm(user:String) = Authenticated { Action { implicit request =>	
      if (Secure.isAdmin(request)) { 
         Secure.addPermission(user,3)
	  }
	  Ok(user)
  }}
  
  //
  //TODO: change to Moderated Action when my brain works
  //
  
  def giveWritePerm(user:String) = Authenticated { Action { implicit request =>	
      if (Secure.isAdmin(request) || Secure.canAdmin(toolbar.user.getOrElse(""))) { 
         Secure.addPermission(user,2)
	  }
	  Ok(user)
  }}
  
  def removePermissions(user:String) = Authenticated { Action { implicit request =>	
      if (Secure.isAdmin(request) || Secure.canAdmin(toolbar.user.getOrElse(""))) { 
         Secure.removePermissions(user)
	  }
	  Ok(user)
  }}
  
  def removeUser(user:String) = Authenticated { Action { implicit request =>	
      if (Secure.isAdmin(request) || toolbar.canMod) { 
         Secure.removeUser(user)
         SponsorOps.removeByLoginString(user)
	  }
	  Ok(user)
  }}
  
  def removeHomebound(_id:ObjectId) = Authenticated { Action { implicit request =>	
      if (Secure.isAdmin(request) || toolbar.canMod) { 
         HomeboundOps.removeById(_id)
	  }
	  Ok(_id.toString())
  }}
}