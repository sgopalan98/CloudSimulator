package HelperUtils

import org.cloudbus.cloudsim.vms.{VmCost, VmSimple}
import org.slf4j.Logger

class Measurements

object Measurements:
  
  def getCost(vmExecList: List[VmSimple], logger: Logger) =
    logger.info(s"The total length is ${vmExecList.length}")
    val cost = vmExecList.map(vm => if(vm.hasStartedSomeCloudlet) new VmCost(vm).getTotalCost else 0).sum
    logger.info(s"The total cost is ${cost}")
