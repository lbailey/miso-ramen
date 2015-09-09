package controllers

import play.api._
import play.api.Logger
import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._

import controllers.Authentication.Authenticated
import models.Location.GPS
import models.Homebound
import models.HomeboundOps
import models.Sponsor
import models.SponsorOps

import models.Secure
import models.Secure.Tools

object Creator extends Controller with Tools {
  
  def sponsor = Authenticated { Action { implicit request =>
    if (Secure.isAuthorized(request)) { 
      Ok(views.html.sponsorform(SponsorOps.creatorForm))
    }
    else Ok(views.html.dashboard())
  }}
  
  def homebound = Authenticated { Action { implicit request =>
    if (Secure.isAuthorized(request) && Secure.isAdmin(request)) { 
      Ok(views.html.homeboundform(HomeboundOps.creatorForm))
    }
    else Ok(views.html.dashboard())
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
          //var sponsor = data.copy(userLoginId = Secure.getUser(request))
          //data.userLoginId = Secure.getUser(request)
          //Logger.debug("user is " + sponsor.userLoginId)
      	  Logger.debug("data is " + data)
      	  SponsorOps.add(data)
      	  Ok("Whee! created sponsor")
      	} else Ok("not admin")
    })
  }
  
}