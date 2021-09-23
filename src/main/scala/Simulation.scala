import HelperUtils.{CreateLogger, ObtainConfigReference}
import Simulations.{BasicCloudSimPlusExample, PaaSSimulation}
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory
import Simulations.SaaS._

object Simulation:
  val logger = CreateLogger(classOf[Simulation])

  @main def runSimulation =
    logger.info("Constructing a cloud model...")
    AutoScalingVertical.Start(1)
    logger.info("Finished cloud simulation...")

class Simulation