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
import com.mongodb.casbah.{MongoConnection, MongoClient, MongoClientURI, WriteConcern}

import com.novus.salat.Context
import org.bson.types.ObjectId

import mongoContext._
import models.LocationGPS

case class Bowl(_id: ObjectId = new ObjectId, 
					bowlName: String, 
					mapCenter: LocationGPS, 
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

//object BowlDAO extends SalatDAO[Bowl, ObjectId](collection = MongoClient(MongoClientURI("mongodb://localhost:27017/"))("ramendb")("bowls"))
object BowlDAO extends SalatDAO[Bowl, ObjectId](collection = MongoClient(MongoClientURI("mongodb://ramenadmin:admin@ds161950.mlab.com:61950/"))("ramendb")("bowls"))
  
object BowlOps extends Object {

  def all(): List[Bowl] = {
    val findList = BowlDAO.find(MongoDBObject.empty).toList
    findList //return
  }
  
  def allReverse(): List[Bowl] = {
    val findList = BowlDAO.find(MongoDBObject.empty).toList
    findList.reverse //return
  }
  
  def recallByUser(partnerUserName: String): List[Bowl] = {
  	val findList = BowlDAO.find(MongoDBObject("createdByUser" -> partnerUserName))
  							   .sort(orderBy = MongoDBObject("_id" -> -1)).toList
  	findList //return
  }

  def add(b: Bowl) {
    val fixBowl: Bowl = b.copy(_id = new ObjectId)
	Logger.debug("adding bowl: " + b.bowlName)
  	val _id = BowlDAO.insert(fixBowl)
  }
  
  def remove(b: Bowl) {
    Logger.debug("removing user: " + b.bowlName)
    BowlDAO.removeById(recall(b).get._id) 
  }
  
  def removeById(_id: ObjectId) {
    Logger.debug("removing Bowl: " + _id)
    BowlDAO.removeById(_id) 
  }
  
  def recallById(b: Bowl): Option[Bowl] = {
  	val find = BowlDAO.findOneById(b._id)
  	find //return
  }
  
  def recallById(_id: ObjectId): Option[Bowl] = {
  	val find = BowlDAO.findOneById(_id)
  	find //return
  }
  
  def recall(b: Bowl): Option[Bowl] = {
  	val find = BowlDAO.findOne(MongoDBObject("bowlName" -> b.bowlName))
  	find //return
  }
  
  var creatorForm: Form[Bowl] = Form(
    mapping(
      "_id" -> ignored(new ObjectId),
      "bowlName" -> nonEmptyText,
      "mapCenter" -> mapping(
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
    )(Bowl.apply)(Bowl.unapply)
  )
}
  