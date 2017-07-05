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
import com.mongodb.casbah.{MongoConnection, MongoClient, MongoClientURI, WriteConcern}

//import com.novus.salat.Context


import mongoContext._
import models.Nood
import models.NoodOps._
import org.bson.types.ObjectId


object Secure {

  case class Login(username: String, password: String, email: Option[String] = None, priv: Option[Int] = None )
  case class Toolbar(menu: Seq[Link], user: Option[String], currentBowl: Option[Bowl], authorized: Boolean, canWrite: Boolean = false, canMod: Boolean = false, isAdmin: Boolean)
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
	  val write:Boolean = canWrite(someUser.getOrElse(""))
      val mod:Boolean = canAdmin(someUser.getOrElse("")) 
	  //var someNood:Option[Nood] = NoodOps.recallByLoginString(someUser.getOrElse("none"))	  
	  var someBowl:Option[Bowl] = getCurrentBowl(someUser.getOrElse(""))
      Toolbar(menu, someUser, someBowl, isLogged, write, mod, admin)
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
    //Logger.debug(c);
    if (c != None && c.get.value == "admin") true
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
  
  def addPermission(username:String,perm:Int) {
    Vault.addPermission(username,perm)
  }	
  
  def removePermissions(username:String) {
    addPermission(username,1)
  }	
  
  def getLogin(username:String): Option[User] = {
    Vault.findUserByName(username)
  }
  
  def getCurrentBowl(username: String): Option[Bowl] = {
  	val user:Option[User] = getLogin(username)
  	var bowl:Option[Bowl] = None
    if (user.isDefined) {
       	bowl = getLogin(username).get.currentBowl
    }
  	bowl //return
  }
  
  def canWrite(username:String): Boolean = {
    val user:Option[User] = getLogin(username)
    if (user.isDefined) {
      def bool = user.get.a match {
        case 2 => true
      	case 3 => true
        case _ => false
      } 
      bool
    } else false
  }
  
  def canWrite(sl: Login): Boolean = {
    var perm = sl.priv.getOrElse(1);
    def bool = perm match {
      case 2 => true
      case 3 => true
      case _ => false
    }
    bool
  }
  
  
  def canAdmin(username:String): Boolean = {
    val user:Option[User] = getLogin(username)
    if (user.isDefined) {
      def bool = user.get.a match {
        case 3 => true
        case _ => false
      } 
      bool
    } else false
  }
  
  def canAdmin(sl: Login): Boolean = {
    var perm = sl.priv.getOrElse(1);
    def bool = perm match {
      case 3 => true
      case _ => false
    }
    bool
  }
  
  def removeUser(user:String) {
    Vault.removeByUsername(user)
  }
  
}



case class User(_id: ObjectId = new ObjectId, u: String, p: String, e: String, a: Int = 1, 
											  noods: List[Nood] = List.empty, bowls: List[Bowl] = List.empty, 
											  currentBowl: Option[Bowl] = None, noodRankings: Map[String, Int] = Map.empty)

object UserDAO extends SalatDAO[User, ObjectId](collection = MongoClient(MongoClientURI("mongodb://ramenadmin:admin@ds161950.mlab.com:61950/"))("ramendb")("vault"))
//object UserDAO extends SalatDAO[User, ObjectId](collection = MongoClient(MongoClientURI("mongodb://localhost:27017/"))("ramendb")("vault"))
  

object Vault extends Object {

  def all(): List[User] = {
    val findList = UserDAO.find(MongoDBObject.empty).toList
    findList //return
  }

  def writeNew(sl: Secure.Login) {
	Logger.debug("adding user: " + sl.username)
	var nu:User = new User(u = sl.username, p = Secure.encodePass(sl.password), e = sl.email.getOrElse("none"), a = 1)
  	val _id = UserDAO.insert(nu)
  }
  
  def remove(sl: Secure.Login) {
    Logger.debug("removing user: " + sl.username)
    UserDAO.removeById(recall(sl).get._id) 
  }
  
  def removeByUsername(username:String) {
    Logger.debug("removing user: " + username)
    UserDAO.removeById(findUserByName(username).get._id) 
  }
  
  def recall(sl: Secure.Login): Option[User] = {
    Logger.debug("checking stuff: " + UserDAO.collection)
  	val find = UserDAO.findOne(MongoDBObject("u" -> sl.username))
  	find //return
  }
  
  def findUserByName(username: String): Option[User] = {
  	val find = UserDAO.findOne(MongoDBObject("u" -> username))
  	find //return
  }
  
  def addPermission(username: String, perm: Int) {
    val secureLogin = findUserByName(username)
    if (secureLogin.isDefined) {
      val id = secureLogin.get._id
      UserDAO.update(q = MongoDBObject("_id" -> id), o = MongoDBObject("$set" -> MongoDBObject("a" -> perm)), upsert = false, multi = false)
      Logger.debug("updated perms of " + username + " to " + perm)
    }   
  }
  
  def updateBowl(username: String, newBowl: Bowl) {
    val user = findUserByName(username)
    if (user.isDefined) {
      val id = user.get._id
      UserDAO.update(q = MongoDBObject("_id" -> id), o = MongoDBObject("$set" -> MongoDBObject("currentBowl" -> newBowl)), upsert = false, multi = false)
      Logger.debug("updated current bowl of " + username + " to " + newBowl)
    }   
  }
  
  def updatePassword(_id: ObjectId, password: String) {
	UserDAO.update(q = MongoDBObject("_id" -> _id), o = MongoDBObject("$set" -> MongoDBObject("p" -> Secure.encodePass(password))), upsert = false, multi = false)
  }
  
  def updateEmail(_id: ObjectId, email: String) {
	UserDAO.update(q = MongoDBObject("_id" -> _id), o = MongoDBObject("$set" -> MongoDBObject("e" -> email)), upsert = false, multi = false)
  }
}
  