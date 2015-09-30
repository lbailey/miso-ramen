package models


import play.api._
import play.api.Logger
import play.api.libs.Crypto
import play.api.Play.current
import play.api.mvc.Request
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.format.Formats._

import scala.collection.mutable._
import scala.collection.mutable.ListBuffer

import se.radley.plugin.salat._
import se.radley.plugin.salat.Binders.ObjectId

import com.novus.salat._
import com.novus.salat.dao._
import com.novus.salat.global._
import com.novus.salat.annotations._

import com.mongodb.casbah.commons.Imports._
import com.mongodb.casbah.MongoConnection

import com.novus.salat.Context
import org.bson.types.ObjectId

import mongoContext._
import models.Location.GPS

case class Sponsor(_id: ObjectId = new ObjectId, 
					sponsorName: String, 
					gpsLocation: LocationGPS, 
					hasFirstDrop: Boolean = false, 
					isAuthorized: Boolean = false, 
					workingAddress: String, 
					pointOfContact: String,
					phoneNumber: String, 
					userLoginId: String,
					completedMoves: List[Homebound]) {}


object SponsorDAO extends SalatDAO[Sponsor, ObjectId](collection = MongoConnection()(
    current.configuration.getString("mongodb.default.db")
      .getOrElse(throw new PlayException("Configuration error",
      "Could not find mongodb.default.db in settings"))
  )("sponsors"))
  
  
object SponsorOps extends Object {

  def all(): List[Sponsor] = {
    val findList = SponsorDAO.find(MongoDBObject.empty).toList
    findList //return
  }

  def add(sponsor: Sponsor) {
	Logger.debug("adding sponsor: " + sponsor.sponsorName)
  	val _id = SponsorDAO.insert(sponsor)
  }
  
  def remove(sponsor: Sponsor) {
    Logger.debug("removing user: " + sponsor.sponsorName)
    SponsorDAO.removeById(recallById(sponsor).get._id) 
  }
  
  def recallById(sponsor: Sponsor): Option[Sponsor] = {
  	val find = SponsorDAO.findOneById(sponsor._id)
  	find //return
  }
  
  def recallByName(sponsor: Sponsor): Option[Sponsor] = {
  	val find = SponsorDAO.findOne(MongoDBObject("sponsorName" -> sponsor.sponsorName))
  	find //return
  }
  
  def recallByLoginId(sponsor: Sponsor): Option[Sponsor] = {
  	val find = SponsorDAO.findOne(MongoDBObject("userLoginId" -> sponsor.userLoginId))
  	find //return
  }
  
  def recallByLoginString(associatedLogin: String): Option[Sponsor] = {
  	val find = SponsorDAO.findOne(MongoDBObject("userLoginId" -> associatedLogin))
  	find //return
  }
  
  var creatorForm: Form[Sponsor] = Form(
    mapping(
      "_id" -> ignored(new ObjectId),
      "sponsorName" -> nonEmptyText,
      "location" -> mapping(
         "latitude" -> of(doubleFormat),
         "longitude" -> of(doubleFormat)
      	 )(LocationGPS.apply)(LocationGPS.unapply),
      "hasFirstDrop" -> boolean,
      "isAuthorized" -> boolean,
      "workingAddress" -> nonEmptyText,
      "pointOfContact" -> nonEmptyText,
      "phoneNumber" -> nonEmptyText,
      "userLoginId" -> nonEmptyText,
      "completedMoves" -> ignored(List[Homebound]())
    )(Sponsor.apply)(Sponsor.unapply)
  )
}
  