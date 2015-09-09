package models


import play.api._
import play.api.Logger
import play.api.libs.Crypto
import play.api.Play.current
import play.api.mvc.Request

import se.radley.plugin.salat._
import se.radley.plugin.salat.Binders.ObjectId

import com.novus.salat._
import com.novus.salat.dao._
import com.novus.salat.global._
import com.novus.salat.annotations._

import com.mongodb.DBObject
import com.mongodb.casbah.MongoConnection
import com.mongodb.casbah.commons.Implicits._
import com.mongodb.casbah.commons.MongoDBObject

import com.novus.salat.transformers.CustomTransformer
import com.novus.salat.Context


import mongoContext._
import org.bson.types.ObjectId

@Salat
object Location {
  case class GPS(latitude: Double, longitude: Double) 
    
}

case class LocationGPS(latitude: Double, longitude: Double) 

class LocationTransformer extends CustomTransformer[Location.GPS, DBObject] {  
  def deserialize(b: DBObject) = {
    new Location.GPS(b.getAsOrElse[Double]("latitude", 0.0), b.getAsOrElse[Double]("longitude", 0.0))
  }
  def serialize(gps: Location.GPS) = MongoDBObject("latitude" -> gps.latitude, "longitude" -> gps.longitude)
}



