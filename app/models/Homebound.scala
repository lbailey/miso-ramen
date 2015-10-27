package models


import play.api._
import play.api.Logger
import play.api.libs.Crypto
import play.api.Play.current
import play.api.mvc.Request
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.format.Formats._

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
import models.LocationGPS

case class Homebound(_id: ObjectId = new ObjectId, 
					homeboundName: String, 
					gpsLocation: LocationGPS, 
					isMoved: Boolean = false, 
					hasSponsor: Boolean = false, 
					alreadyInHome: Boolean = false,
					createdByUser: String,
					urlProfilePic: Option[String] = None, 
					homeAddress: Option[String] = None, 
					bio: Option[String] = None,
					age: Option[Int] = None, 
					hasPets: Option[Boolean], 
					hasKids: Option[Boolean], 
					hasBox: Option[Boolean],
					emailAddress: Option[String] = None, 
					phoneNumber: Option[String] = None, 
					currentSponsorId: Option[String] = None) {} //User ID


object HomeboundDAO extends SalatDAO[Homebound, ObjectId](collection = MongoConnection()(
    current.configuration.getString("mongodb.default.db")
      .getOrElse(throw new PlayException("Configuration error",
      "Could not find mongodb.default.db in settings"))
  )("homebounds"))
  
  
object HomeboundOps extends Object {

  def all(): List[Homebound] = {
    val findList = HomeboundDAO.find(MongoDBObject.empty).toList
    findList //return
  }
  
  def allReverse(): List[Homebound] = {
    val findList = HomeboundDAO.find(MongoDBObject.empty).toList
    findList.reverse //return
  }
  
  def recallByUser(partnerUserName: String): List[Homebound] = {
  	val findList = HomeboundDAO.find(MongoDBObject("createdByUser" -> partnerUserName))
  							   .sort(orderBy = MongoDBObject("_id" -> -1)).toList
  	findList //return
  }

  def add(hb: Homebound) {
    val fixHomebound: Homebound = hb.copy(_id = new ObjectId)
	Logger.debug("adding homebound: " + hb.homeboundName)
  	val _id = HomeboundDAO.insert(fixHomebound)
  }
  
  def remove(hb: Homebound) {
    Logger.debug("removing user: " + hb.homeboundName)
    HomeboundDAO.removeById(recall(hb).get._id) 
  }
  
  def removeById(_id: ObjectId) {
    Logger.debug("removing Homebound: " + _id)
    HomeboundDAO.removeById(_id) 
  }
  
  def recallById(hb: Homebound): Option[Homebound] = {
  	val find = HomeboundDAO.findOneById(hb._id)
  	find //return
  }
  
  def recallById(_id: ObjectId): Option[Homebound] = {
  	val find = HomeboundDAO.findOneById(_id)
  	find //return
  }
  
  def recall(hb: Homebound): Option[Homebound] = {
  	val find = HomeboundDAO.findOne(MongoDBObject("homeboundName" -> hb.homeboundName))
  	find //return
  }
  
  var creatorForm: Form[Homebound] = Form(
    mapping(
      "_id" -> ignored(new ObjectId),
      "homeboundName" -> nonEmptyText,
      "location" -> mapping(
         "latitude" -> of(doubleFormat),
         "longitude" -> of(doubleFormat)
      	 )(LocationGPS.apply)(LocationGPS.unapply),
      "isMoved" -> boolean,
      "hasSponsor" -> boolean,
      "alreadyHome" -> boolean,
      "createdByUser" -> nonEmptyText,
      "urlProfilePic" -> optional(text),
      "homeAddress" -> optional(text),
      "bio" -> optional(text),
      "age" -> optional(number),
      "hasPets" -> optional(boolean),
      "hasKids" -> optional(boolean),
      "hasBox" -> optional(boolean),
      "emailAddress" -> optional(text),
      "phoneNumber" -> optional(text),
      "currentSponsorId" -> optional(text)
    )(Homebound.apply)(Homebound.unapply)
  )
}
  