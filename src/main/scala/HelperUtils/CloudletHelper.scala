package HelperUtils

import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple
import org.cloudbus.cloudsim.cloudlets.{Cloudlet, CloudletSimple}
import org.cloudbus.cloudsim.vms.VmSimple
import org.cloudsimplus.listeners.EventInfo

import collection.JavaConverters.*

object CloudletHelper:

  def delayedCloudletExecution(cloudletsNumber: Int, cloudletLength: Long, cloudletPes: Long, cloudletFileSize: Long, cloudletOutputSize: Long) = {
    val cloudlets: List[Cloudlet] = (1 to cloudletsNumber).map(index => {
      val cloudlet = CloudletSimple(cloudletLength, cloudletPes)
        .setFileSize(cloudletFileSize)
        .setOutputSize(cloudletOutputSize)
      cloudlet
    }
    ).toList
    cloudlets
  }
  
  def createFirstCloudlets(vms: List[VmSimple], cloudletSize: Long, cloudletPes: Long, cloudletFileSize: Long, cloudletOutputSize: Long) = {
        val cloudlets:List[Cloudlet] = vms.flatMap(vm => {
          val list = CloudletSimple(cloudletSize, cloudletPes)
            .setFileSize(cloudletFileSize)
            .setOutputSize(cloudletOutputSize) :: CloudletSimple(cloudletSize, cloudletPes)
            .setFileSize(cloudletFileSize)
            .setOutputSize(cloudletOutputSize) :: Nil
          list
        })
    cloudlets
  }



  def getCloudletSize(configFile:String) = {
    val config = ObtainConfigReference("Cloudlet", configFile) match {
      case Some(value) => value
      case None => throw new RuntimeException("Cannot obtain a reference to the config data.")
    }
    config.getLong("Cloudlet.length")
  }
    
  def getCloudletFileSize(configFile:String) = {
    val config = ObtainConfigReference("Cloudlet", configFile) match {
      case Some(value) => value
      case None => throw new RuntimeException("Cannot obtain a reference to the config data.")
    }
    config.getLong("Cloudlet.fileSize")
  }
    
  def getCloudletOutputSize(configFile:String) = {
    val config = ObtainConfigReference("Cloudlet", configFile) match {
      case Some(value) => value
      case None => throw new RuntimeException("Cannot obtain a reference to the config data.")
    }
    config.getLong("Cloudlet.outputSize")
  }

  def getCloudletPes(configFile:String) = {
    val config = ObtainConfigReference("Cloudlet", configFile) match {
      case Some(value) => value
      case None => throw new RuntimeException("Cannot obtain a reference to the config data.")
    }
    config.getLong("Cloudlet.Pes")
  }


    
