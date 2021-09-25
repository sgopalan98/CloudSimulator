package HelperUtils

import com.typesafe.config.{Config, ConfigFactory}

import scala.util.{Failure, Success, Try}

class ObtainConfigReference
object ObtainConfigReference:
//  private val config = ConfigFactory.load()
  private def loadConfig(configFile: String) : Option[Config]= Try(ConfigFactory.load(configFile)) match{
    case Failure(exception) => logger.error(s"Failed to retrieve config file $configFile for reason $exception"); None
    case Success(config) => Some(config)
  }
  private val logger = CreateLogger(classOf[ObtainConfigReference])
  //Return Some(Config) Object if file is present and key is present
  def apply(confEntry:String, configFile: String): Option[Config] = {
    loadConfig(configFile) match {
      case None => None
      case Some(config) => Try(config.getConfig(confEntry)) match {
        case Failure(exception) => logger.error(s"Failed to retrieve config entry $confEntry for reason $exception"); None
        case Success(_) => Some(config)
      }
    }
  }
