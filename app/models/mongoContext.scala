package models

import com.novus.salat.dao._
import com.novus.salat.annotations._
import com.mongodb.casbah.Imports._
import com.novus.salat.{TypeHintFrequency, StringTypeHintStrategy, Context}
import play.api.Play
import play.api.Play.current

/*
 * All models must contain an implicit salat Context.
 * The context is somewhat like a hibernate dialect. 
 * You can override mapping names and configure how salat does it's type hinting. 
 * Read more about it here: https://github.com/novus/salat/wiki/CustomContext
 *
 * This is our custom mongoContext.
 * Partly because we need to add plays classloader to salat so it knows when to reload it's graters, 
 * but also so we can override all models id fields to be serialized to MongoDB's _id.
 */
package object mongoContext {
  implicit val context = {
    val context = new Context {
      val name = "global"
      override val typeHintStrategy = StringTypeHintStrategy(when = TypeHintFrequency.WhenNecessary, typeHint = "_t")
    }
    context.registerGlobalKeyOverride(remapThis = "id", toThisInstead = "_id")
    context.registerClassLoader(Play.classloader)
 //   context.registerCustomTransformer(new models.LocationTransformer)
    context
  }
}