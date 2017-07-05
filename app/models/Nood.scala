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

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

import com.novus.salat._
import com.novus.salat.dao._
import com.novus.salat.global._
import com.novus.salat.annotations._

import com.mongodb.casbah.commons.Imports._
import com.mongodb.casbah.MongoConnection
import com.mongodb.casbah.MongoClient
import com.mongodb.casbah.MongoClientURI

import com.novus.salat.Context
import org.bson.types.ObjectId

import mongoContext._
import models.Location.GPS
//import utils._


object NoodType extends Enumeration {	
	type NoodType = Value
    //val Food, Event, Landmark = Value
    
    val Food = Value("Food")
    val Event = Value("Event")
    val Landmark = Value("Landmark")
}

case class Nood(_id: ObjectId = new ObjectId,
					noodName: String, 
					noodType: String, 					// Food, Event, Landmark
					typeDetails: Option[String] = None, // What kind of food, event dates, etc
					hasNick: Boolean = false, 
					noodNick: Option[String] = None,
					noodDesc: Option[String] = None, 
					gpsLocation: LocationGPS, 
					dateAdded: DateTime,
					isRated: Boolean = false, 
					streetAddress: Option[String] = None, 
					phoneNumber: Option[String] = None, 
					website: Option[String] = None, 
					userLoginId: String,
					ratingPercent: Int,
					ratedBy: List[String],	//userIds
					containingBowls: List[Bowl]) {}

object NoodDAO extends SalatDAO[Nood, ObjectId](collection = MongoClient(MongoClientURI("mongodb://ramenadmin:admin@ds161950.mlab.com:61950/"))("ramendb")("noods"))  
//object NoodDAO extends SalatDAO[Nood, ObjectId](collection = MongoClient(MongoClientURI("mongodb://localhost:27017/"))("ramendb")("noods"))  
  
object NoodOps extends Object {

  //TODO: Noodle Ops respond to specific user, not entire set
  def all(): List[Nood] = {
    val findList = NoodDAO.find(MongoDBObject.empty).toList
    findList //return
  }
  
  def allReverse(): List[Nood] = {
    val findList = NoodDAO.find(MongoDBObject.empty).toList
    findList.reverse //return
  }
  

  def add(nood: Nood) {
    val fixNood: Nood = nood.copy(_id = new ObjectId)
	Logger.debug("adding nood: " + nood.noodName)
  	val _id = NoodDAO.insert(fixNood)
  }
  
  def remove(nood: Nood) {
    Logger.debug("removing user: " + nood.noodName)
    NoodDAO.removeById(recallById(nood).get._id) 
  }
  
  def removeByLoginString(associatedLogin: String) {
    Logger.debug("removing nood profile of user: " + associatedLogin)
    NoodDAO.removeById(recallByLoginString(associatedLogin).get._id) 
  }

  def recall(nood: Nood): Option[Nood] = {
  	val find = NoodDAO.findOneById(nood._id)
  	find //return
  }
  
  def recallById(nood: Nood): Option[Nood] = {
  	val find = NoodDAO.findOneById(nood._id)
  	find //return
  }
  
  def recallByName(nood: Nood): Option[Nood] = {
  	val find = NoodDAO.findOne(MongoDBObject("noodName" -> nood.noodName))
  	find //return
  }
  
  def recallByLoginId(nood: Nood): Option[Nood] = {
  	val find = NoodDAO.findOne(MongoDBObject("userLoginId" -> nood.userLoginId))
  	find //return
  }
  
  def recallByLoginString(associatedLogin: String): Option[Nood] = {
  	val find = NoodDAO.findOne(MongoDBObject("userLoginId" -> associatedLogin))
  	find //return
  }

  var creatorForm: Form[Nood] = Form(
    mapping(
      "_id" -> ignored(new ObjectId),
      "noodName" -> nonEmptyText,
      "noodType" -> nonEmptyText,
      "typeDetails" -> optional(text),
      "hasNick" -> boolean,
      "noodNick" -> optional(text),
      "noodDesc" -> optional(text),
      "location" -> mapping(
         "latitude" -> of(doubleFormat),
         "longitude" -> of(doubleFormat)
      	 )(LocationGPS.apply)(LocationGPS.unapply),
      "dateAdded" -> jodaDate,
      "isRated" -> boolean,
      "streetAddress" -> optional(text),
      "phoneNumber" -> optional(text),
      "website" -> optional(text),
      "userLoginId" -> nonEmptyText,
      "ratingPercent" -> number,
      "ratedBy" -> ignored(List[String]()),
      "containingBowls" -> ignored(List[Bowl]())
    )(Nood.apply)(Nood.unapply)
  )
}
  