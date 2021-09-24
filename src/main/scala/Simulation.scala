import HelperUtils.{CreateLogger, ObtainConfigReference}
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory
import Simulations.SaaS.SaaSApplication
import Simulations.IaaS._

object Simulation:
  val logger = CreateLogger(classOf[Simulation])

  @main def runSimulation =
    logger.info("Constructing a cloud model...")
//    new IaaSExample1().Start()
    SaaSApplication.Start(2)
    logger.info("Finished cloud simulation...")

class Simulation