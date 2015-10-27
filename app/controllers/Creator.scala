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

object Creator extends Controller with Tools {
  
  def sponsor = Authenticated { Action { implicit request =>
      Ok(views.html.sponsorform(SponsorOps.creatorForm))
  }}
  
  /*
  *   Ok(views.html.homeboundform(HomeboundOps.allReverse()))
  *   Ok(views.html.homeboundform(HomeboundOps.recallByUser(toolbar.user.getOrElse("")))) //bad!
  */
  
  def homebound = Authenticated { Action { implicit request =>
    if (Secure.isAdmin(request) || toolbar.canWrite) { 
      Ok(views.html.homeboundform(HomeboundOps.allReverse()))
    } else Redirect("/go/")
  }}
  
  def addHomebound =  Action { implicit request =>
    HomeboundOps.creatorForm.bindFromRequest.fold(
      hasErrors = { form =>
        BadRequest(views.html.index())
      },
      
      success = { data =>    
        if (Secure.isAuthorized(request) && Secure.isAdmin(request)) { 
          Logger.debug("Adding Homebound " + data)
      	  HomeboundOps.add(data)
      	  Redirect(routes.Creator.homebound)
      	} else Ok("not admin")
    })
  }

  def addSponsor =  Action { implicit request =>
    SponsorOps.creatorForm.bindFromRequest.fold(
      hasErrors = { form =>
        BadRequest(views.html.index())
      },
      
      success = { data =>    
        if (Secure.isAuthorized(request)) { 
      	  SponsorOps.add(data)
      	  Redirect(routes.Dashboard.index)
      	} else Ok("not admin")
    })
  }
  
}