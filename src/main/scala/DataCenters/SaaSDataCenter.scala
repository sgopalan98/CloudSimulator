package DataCenters


import HelperUtils.ObtainConfigReference
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicy
import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.datacenters.DatacenterSimple
import org.cloudbus.cloudsim.hosts.Host
import org.cloudbus.cloudsim.vms.VmSimple
import org.cloudsimplus.listeners.EventInfo
import org.slf4j.Logger

abstract class SaaSDataCenter {
  def createVms(vmNumbers: Int, vmMips: Long, vmPes: Int, vmRam: Long, vmBandwith: Long): List[VmSimple]
  def createHosts(logger: Logger, hostNos: Int, pes: Int, mips: Long, ram: Long, bw: Long, storage: Long): IndexedSeq[Host]
  def createDataCenter(simulation: CloudSim, hosts: List[Host], allocationPolicy: VmAllocationPolicy) : DatacenterSimple
  
  def getNoHosts(configFile:String) : Int = {
    val config = ObtainConfigReference("DataCenter", configFile) match {
      case Some(value) => value
      case None => throw new RuntimeException("Cannot obtain a reference to the config data.")
    }
    config.getInt("DataCenter.host.number")
  }

  def getHostMips(configFile:String) : Long = {
    val config = ObtainConfigReference("DataCenter", configFile) match {
      case Some(value) => value
      case None => throw new RuntimeException("Cannot obtain a reference to the config data.")
    }
    config.getLong("DataCenter.host.mipsCapacity")
  }

  def getHostPes(configFile:String) : Int = {
    val config = ObtainConfigReference("DataCenter", configFile) match {
      case Some(value) => value
      case None => throw new RuntimeException("Cannot obtain a reference to the config data.")
    }
    config.getInt("DataCenter.host.PEs")
  }
  
  def getHostRam(configFile:String) : Int = {
    val config = ObtainConfigReference("DataCenter", configFile) match {
      case Some(value) => value
      case None => throw new RuntimeException("Cannot obtain a reference to the config data.")
    }
    config.getInt("DataCenter.host.RAMInMBs")
  }
  
  def getHostBw(configFile:String) : Int = {
    val config = ObtainConfigReference("DataCenter", configFile) match {
      case Some(value) => value
      case None => throw new RuntimeException("Cannot obtain a reference to the config data.")
    }
    config.getInt("DataCenter.host.BandwidthInMBps")
  }
  
  def getHostStorage(configFile:String) : Int = {
    val config = ObtainConfigReference("DataCenter", configFile) match {
      case Some(value) => value
      case None => throw new RuntimeException("Cannot obtain a reference to the config data.")
    }
    config.getInt("DataCenter.host.StorageInMBs")
  }

  def getVmNumbers(configFile:String) : Int = {
    val config = ObtainConfigReference("DataCenter", configFile) match {
      case Some(value) => value
      case None => throw new RuntimeException("Cannot obtain a reference to the config data.")
    }
    config.getInt("DataCenter.vm.vmNumbers")
  }

  def getVmMips(configFile:String) : Int = {
    val config = ObtainConfigReference("DataCenter", configFile) match {
      case Some(value) => value
      case None => throw new RuntimeException("Cannot obtain a reference to the config data.")
    }
    config.getInt("DataCenter.vm.mipsCapacity")
  }

  def getVmPEs(configFile:String) : Int = {
    val config = ObtainConfigReference("DataCenter", configFile) match {
      case Some(value) => value
      case None => throw new RuntimeException("Cannot obtain a reference to the config data.")
    }
    config.getInt("DataCenter.vm.PEs")
  }

  def getVmRam(configFile:String) : Int = {
    val config = ObtainConfigReference("DataCenter", configFile) match {
      case Some(value) => value
      case None => throw new RuntimeException("Cannot obtain a reference to the config data.")
    }
    config.getInt("DataCenter.vm.RAMInMBs")
  }

  def getVmBw(configFile:String) : Int = {
    val config = ObtainConfigReference("DataCenter", configFile) match {
      case Some(value) => value
      case None => throw new RuntimeException("Cannot obtain a reference to the config data.")
    }
    config.getInt("DataCenter.vm.BandwidthInMBps")
  }
  
}
