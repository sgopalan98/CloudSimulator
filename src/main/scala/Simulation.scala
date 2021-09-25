import HelperUtils.{CreateLogger, ObtainConfigReference}
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory
import Simulations.SaaS.SaaSApplication
import Simulations.IaaS._

object Simulation:
  val logger = CreateLogger(classOf[Simulation])

  @main def runSimulation =
    logger.info("Running SaaS simulation - No Scaling...")
    SaaSApplication.Start(1)
    logger.info("Finished cloud simulation...")

    logger.info("Running SaaS simulation - Auto Scaling - Horizontal...")
    //    new IaaSExample1().Start()
    SaaSApplication.Start(2)
    logger.info("Finished cloud simulation...")

    logger.info("Running SaaS simulation - Auto Scaling - Vertical...")
    //    new IaaSExample1().Start()
    SaaSApplication.Start(3)
    logger.info("Finished cloud simulation...")

    logger.info("Running IaaS - One with VM Allocation policy with custom utilisation metric...")
        new IaaSSimulation1().Start()
//    SaaSApplication.Start(2)
    logger.info("Finished cloud simulation...")

    logger.info("Running IaaS - No VM Allocation policy...")
        new IaaSSimulation2().Start()
//    SaaSApplication.Start(2)
    logger.info("Finished cloud simulation...")

class Simulation