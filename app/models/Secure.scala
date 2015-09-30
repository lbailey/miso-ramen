package models


import play.api._
import play.api.Logger
import play.api.libs.Crypto
import play.api.Play.current
import play.api.mvc.Request
import play.api.mvc.Flash
import play.api.mvc.Cookie
import play.api.mvc.Cookies
import scala.util.matching.Regex
import scala.util.Random

//import com.mongodb.casbah.Imports._
//import com.mongodb.casbah.WriteConcern
import se.radley.plugin.salat._
import se.radley.plugin.salat.Binders.ObjectId

import com.novus.salat._
import com.novus.salat.dao._
import com.novus.salat.global._
import com.novus.salat.annotations._

import com.mongodb.casbah.commons.Imports._
import com.mongodb.casbah.MongoConnection

import com.novus.salat.Context


import mongoContext._
import models.Sponsor
import models.SponsorOps._
import org.bson.types.ObjectId


object Secure {

  case class Login(username: String, password: String, email: Option[String] = None)
  case class Toolbar(menu: Seq[Link], user: Option[String], profile: Option[Sponsor], authorized: Boolean, isAdmin: Boolean)
  case class Link(url: String, name: String)
  
  val RedirectUrlRE = new Regex("""[^/login/]""")
  
  trait Tools {
  	implicit def toolbar[A](implicit request: Request[A]) : Toolbar = {
      val menu = Seq(Link("/dashboard/", "Dashboard"),
        Link("/pencils/create/", "Pencil"),
        Link("/openingcredits/create/", "Opening Credit"),
        Link("/skins/create/", "Skin"))
      val cUser:Option[Cookie] = request.cookies.get("gobe_user")
      val isLogged:Boolean = isAuthorized(request)
      val admin:Boolean = isAdmin(request)
      val someUser:Option[String] = cUser match {
  		case None => None
  		case Some(username) => Some(cUser.get.value)
	  }	  
	  var someSponsor:Option[Sponsor] = SponsorOps.recallByLoginString(someUser.getOrElse("none"))	  
      Toolbar(menu, someUser, someSponsor, isLogged, admin)
    }
  }

  def authorize(sl: Login): Boolean = {   
	val user:Option[User] = Vault.recall(sl)	
	val cryptPassword = user match {
  		case None => ""
  		case Some(username) => user.get.p
	}
  	if (cryptPassword == Secure.encodePass(sl.password)) true
  	else false
  }

  def isAuthorized[A](implicit request: Request[A]): Boolean = {
    cookieStateSecure(request) && userCookiesMatch(request)
  }
  
  def encodeUser(username: String): (String) = {
  	Crypto.sign(username)
  }
  
  def encodePass(pass: String): (String) = {
	val md = java.security.MessageDigest.getInstance("SHA-256")
	new sun.misc.BASE64Encoder().encode(md.digest(pass.getBytes("UTF-8")))
  }
  
  def isAdmin[A](implicit request: Request[A]): Boolean = {
    val c:Option[Cookie] = request.cookies.get("gobe_user")
    if (c.get.value == "admin") true
    else false
  }
  
  def getUser[A](implicit request: Request[A]): String = {
    val c:Option[Cookie] = request.cookies.get("gobe_user")
    c.get.value
  }
  
  def cookieStateSecure[A](implicit request: Request[A]): Boolean = {
  	val c:Option[Cookie] = request.cookies.get("gobe_state")
  	val state:String = c match {
  		case None => ""
  		case Some(username) => c.get.value
	}
    
    def bool = state match {
      case "1" => true
      case _ => false
    }
    bool //return   
  }
  
  def userCookiesMatch[A](implicit request: Request[A]): Boolean = {
  	val s:Option[String] = request.session.get("user")
  	val c:Option[Cookie] = request.cookies.get("gobe_user")
  	val username:String = Crypto.sign(c.get.value)
  	val sPass:String = s.get
    
    if (sPass == username) true
    else false
  }
  
  def userList(): List[User] = {
    Vault.all()
  }
  
  def checkUnique(username:String): Boolean = {
    var unique = Secure.Login(username, "password")
    Vault.recall(unique).isDefined
  }	
}



case class User(_id: ObjectId = new ObjectId, u: String, p: String, e: String)

object UserDAO extends SalatDAO[User, ObjectId](collection = MongoConnection()(
    current.configuration.getString("mongodb.default.db")
      .getOrElse(throw new PlayException("Configuration error",
      "Could not find mongodb.default.db in settings"))
  )("vault"))
  
  
object Vault extends Object {

  def all(): List[User] = {
    val findList = UserDAO.find(MongoDBObject.empty).toList
    findList //return
  }

  def writeNew(sl: Secure.Login) {
	Logger.debug("adding user: " + sl.username)
  	val _id = UserDAO.insert(User(u = sl.username, p = Secure.encodePass(sl.password), e = sl.email.getOrElse("none")))
  }
  
  def remove(sl: Secure.Login) {
    Logger.debug("removing user: " + sl.username)
    UserDAO.removeById(recall(sl).get._id) 
  }
  
  def recall(sl: Secure.Login): Option[User] = {
  	val find = UserDAO.findOne(MongoDBObject("u" -> sl.username))
  	find //return
  }
  
  def updatePassword(_id: ObjectId, password: String) {
	UserDAO.update(q = MongoDBObject("_id" -> _id), o = MongoDBObject("$set" -> MongoDBObject("p" -> Secure.encodePass(password))), upsert = false, multi = false)
  }
  
  def updateEmail(_id: ObjectId, email: String) {
	UserDAO.update(q = MongoDBObject("_id" -> _id), o = MongoDBObject("$set" -> MongoDBObject("p" -> email)), upsert = false, multi = false)
  }
}
  