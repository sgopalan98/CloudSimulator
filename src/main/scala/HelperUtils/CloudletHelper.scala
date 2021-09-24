package HelperUtils

import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple
import org.cloudbus.cloudsim.cloudlets.{Cloudlet, CloudletSimple}
import org.cloudbus.cloudsim.vms.VmSimple
import org.cloudsimplus.listeners.EventInfo

import collection.JavaConverters.*

object CloudletHelper:
//  private val SCHEDULE_INTERVAL = 5
//
//  private val HOSTS = 5
//  private val VMS = 3
//
//  private val HOST_MIPS = 1000 //for each PE
//
//
//  private val HOST_INITIAL_PES = 4
//  private val HOST_RAM: Long = 500000 //host memory (MB)
//
//  private val HOST_STORAGE: Long = 1000000 //host storage
//
//
//  private val HOST_BW: Long  = 16000L //Mb/s
//
//
//  private val VM_MIPS = 1000L
//  private val VM_SIZE = 1000 //image size (MB)
//
//  private val VM_RAM = 10000 //VM memory (MB)
//
//  private val VM_BW = 1000L
//  private val VM_PES = 2
//
//  private val CLOUDLET_LENGHT = 2000000L
//  private val CLOUDLET_FILESIZE = 300L
//  private val CLOUDLET_OUTPUTSIZE = 300L


  
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


    
