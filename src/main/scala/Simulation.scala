import HelperUtils.{CreateLogger, ObtainConfigReference}
import Simulations.{BasicCloudSimPlusExample, PaaSSimulation}
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory
import Simulations.SaaS.SmallApplicationSaaS
import Simulations.IaaS._

object Simulation:
  val logger = CreateLogger(classOf[Simulation])

  @main def runSimulation =
    logger.info("Constructing a cloud model...")
    new IaaSExample2().Start()
    logger.info("Finished cloud simulation...")

class Simulation