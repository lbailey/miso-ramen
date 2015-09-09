package utils

import java.util.Date
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

object AdCmsUtil {

  def getFormattedDate(date: Date, time: String = null) = {
    val dateTime = new DateTime(date);
    
    val fmt = DateTimeFormat.forPattern("MMMM d, yyyy");
    
    var formattedData = fmt.print(dateTime)
    
    // FIXME: Implement this in a more elegant way
    if(time == null){
      formattedData
    }else{
      formattedData + " " + time
    }
  }
  
  def availableUsername(checkName: String) = {
  	
  }
}
