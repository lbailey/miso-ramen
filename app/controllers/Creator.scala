package controllers

import play.api._
import play.api.Logger
import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._

import controllers.Authentication.Authenticated
import controllers.Authentication.loginForm

import models.Location.GPS
import models.Bowl
import models.BowlOps
import models.Nood
import models.NoodOps

import models.Secure
import models.Secure.Tools

object Creator extends Controller with Tools {
  
 // def nood = Authenticated { Action { implicit request =>
 //     Ok(views.html.noodform(NoodOps.creatorForm))
 // }}
  
  /*
  *   Ok(views.html.homeboundform(HomeboundOps.allReverse()))
  *   Ok(views.html.homeboundform(HomeboundOps.recallByUser(toolbar.user.getOrElse("")))) //bad!
  */
  
  def bowl = Authenticated { Action { implicit request =>
    if (Secure.isAdmin(request) || toolbar.canWrite) { 
      Ok(views.html.bowlform(BowlOps.allReverse()))
    } else Redirect("/home/")
  }}
  
  def nood = Authenticated { Action { implicit request =>
 //   if (Secure.isAdmin(request) /*|| toolbar.canWrite*/) { 
      Ok(views.html.noodleform(NoodOps.allReverse()))
 //   } else Redirect("/home/")
  }}
  
  def addBowl =  Action { implicit request =>
    BowlOps.creatorForm.bindFromRequest.fold(
      hasErrors = { form =>
        BadRequest(views.html.index())
      },
      
      success = { data =>    
        if (Secure.isAuthorized(request) && (toolbar.canWrite || Secure.isAdmin(request))) { 
          Logger.debug("Adding Bowl " + data)
      	  BowlOps.add(data)
      	  Redirect(routes.Creator.bowl)
      	} else Ok("not admin")
    })
  }

  def addNood =  Action { implicit request =>
    NoodOps.creatorForm.bindFromRequest.fold(
      hasErrors = { form =>
        BadRequest(views.html.index())
      },
      
      success = { data =>    
        if (Secure.isAuthorized(request)) { 
      	  NoodOps.add(data)
      	  Redirect(routes.Creator.nood)
      	} else Ok("not admin")
    })
  }
  
}