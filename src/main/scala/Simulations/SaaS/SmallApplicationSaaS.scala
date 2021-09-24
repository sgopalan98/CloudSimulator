package Simulations.SaaS

import DataCenters.{DataCenter, DataCenter1, DataCenter2, DataCenter3}
import HelperUtils.{CreateLogger, Measurements, ObtainConfigReference, CloudletHelper}
import org.cloudbus.cloudsim.allocationpolicies.*
import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple
import org.cloudbus.cloudsim.cloudlets.{Cloudlet, CloudletSimple}
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.datacenters.DatacenterSimple
import org.cloudbus.cloudsim.distributions.{ContinuousDistribution, NormalDistr, UniformDistr}
import org.cloudbus.cloudsim.hosts.{Host, HostSimple}
import org.cloudbus.cloudsim.provisioners.{PeProvisionerSimple, ResourceProvisionerSimple}
import org.cloudbus.cloudsim.resources.{Pe, PeSimple, Processor}
import org.cloudbus.cloudsim.schedulers.cloudlet.{CloudletSchedulerCompletelyFair, CloudletSchedulerSpaceShared, CloudletSchedulerTimeShared}
import org.cloudbus.cloudsim.schedulers.vm.*
import org.cloudbus.cloudsim.utilizationmodels.{UtilizationModelDynamic, UtilizationModelFull}
import org.cloudbus.cloudsim.vms.{VmCost, VmSimple}
import org.cloudsimplus.autoscaling.{HorizontalVmScalingSimple, VerticalVmScalingSimple}
import org.cloudsimplus.builders.tables.CloudletsTableBuilder
import org.cloudsimplus.listeners.{EventInfo, EventListener}

import java.util
import scala.collection.JavaConverters.*

class SmallApplicationSaaS

object SmallApplicationSaaS:
  val logger = CreateLogger(classOf[SmallApplicationSaaS])

  def Start(dataCenterValue:Int) =

    val dataCenterConfig : DataCenter = if(dataCenterValue == 1) new DataCenter1 else if(dataCenterValue == 2) new DataCenter2 else new DataCenter3

    val configFile = "datacenter" + dataCenterValue.toString + ".conf"

    val seed = 1

    val simulation = new CloudSim()

    val broker = DatacenterBrokerSimple(simulation)

    //creating DataCenter
    val hostNos = dataCenterConfig.getNoHosts(configFile)
    val hostPes = dataCenterConfig.getHostPes(configFile)
    val hostMips = dataCenterConfig.getHostMips(configFile)
    val hostRam = dataCenterConfig.getHostRam(configFile)
    val hostBw = dataCenterConfig.getHostBw(configFile)
    val hostStorage = dataCenterConfig.getHostStorage(configFile)
    
    val hosts = dataCenterConfig.createHosts(logger, hostNos, hostPes, hostMips, hostRam, hostBw, hostStorage)
    
    val allocationPolicy = new VmAllocationPolicyBestFit()

    val dataCenter = dataCenterConfig.createDataCenter(simulation, hosts.toList, allocationPolicy)
    dataCenter.setSchedulingInterval(1)

    


    //Creating broker

    broker.setVmDestructionDelay(200.0)

    val vmNumbers = dataCenterConfig.getVmNumbers(configFile)
    val vmMips = dataCenterConfig.getVmMips(configFile)
    val vmPes = dataCenterConfig.getVmPEs(configFile)
    val vmRam = dataCenterConfig.getVmRam(configFile)
    val vmBw = dataCenterConfig.getVmBw(configFile)

    val vms = dataCenterConfig.createVms(vmNumbers, vmMips, vmPes, vmRam, vmBw)
    broker.submitVmList(vms.asJava)

    //Creating cloudlets
    val applicationConfigFile: String = "SmallApplication.conf"
    
    
    val cloudletSize = CloudletHelper.getCloudletSize(applicationConfigFile)
    val cloudletFileSize = CloudletHelper.getCloudletFileSize(applicationConfigFile)
    val cloudletOutputSize = CloudletHelper.getCloudletOutputSize(applicationConfigFile)
    val cloudletPes = CloudletHelper.getCloudletPes(applicationConfigFile)
    
    val clouldets = CloudletHelper.createFirstCloudlets(vms, cloudletSize, cloudletPes, cloudletFileSize, cloudletOutputSize)


    broker.submitCloudletList(clouldets.asJava)

    simulation.addOnClockTickListener(info =>{
      if(broker.getCloudletSubmittedList.asScala.length < 10) {
        val cloudlets = CloudletHelper.delayedCloudletExecution(vms.length, cloudletSize, cloudletPes, cloudletFileSize, cloudletOutputSize)
        broker.submitCloudletList(cloudlets.asJava, 100)
      }

      val vmList : List[VmSimple] = broker.getVmCreatedList.asScala.toList
    })
    //Run simulation
    simulation.start()


    //printing
    new CloudletsTableBuilder(broker.getCloudletSubmittedList().asScala.toList.sortBy(_.getExecStartTime).asJava).build();
    //Measurements calculator
    val vmExecList: List[VmSimple] = broker.getVmCreatedList.asScala.toList

    Measurements.getCost(vmExecList, logger)







