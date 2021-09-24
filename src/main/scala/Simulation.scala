import HelperUtils.{CreateLogger, ObtainConfigReference}
import Simulations.{BasicCloudSimPlusExample, PaaSSimulation}
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory
import Simulations.SaaS.SmallApplicationSaaS

object Simulation:
  val logger = CreateLogger(classOf[Simulation])

  @main def runSimulation =
    logger.info("Constructing a cloud model...")
    SmallApplicationSaaS.Start(3)
    logger.info("Finished cloud simulation...")

class Simulation