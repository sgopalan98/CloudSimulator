package Simulations

import HelperUtils.{CreateLogger, ObtainConfigReference}
import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple
import org.cloudbus.cloudsim.cloudlets.CloudletSimple
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.datacenters.DatacenterSimple
import org.cloudbus.cloudsim.hosts.HostSimple
import org.cloudbus.cloudsim.hosts.Host
import org.cloudbus.cloudsim.resources.{Pe, PeSimple}
import org.cloudbus.cloudsim.schedulers.vm.VmSchedulerTimeShared
import org.cloudbus.cloudsim.utilizationmodels._
import org.cloudbus.cloudsim.vms.VmSimple
import org.cloudsimplus.builders.tables.CloudletsTableBuilder
import org.cloudbus.cloudsim.schedulers.cloudlet.{CloudletSchedulerSpaceShared, CloudletSchedulerTimeShared, CloudletSchedulerCompletelyFair}
import java.util

import collection.JavaConverters.*

class PaaSSimulation

object PaaSSimulation:
  val logger = CreateLogger(classOf[PaaSSimulation])

  def Start(fileNo: Int) =
    val configKey: String = "PaaSConfig" + fileNo.toString
    logger.info(s"THe config key is $configKey")
    val config = ObtainConfigReference(configKey, "PaaSConfig") match {
      case Some(value) => value
      case None => throw new RuntimeException("Cannot obtain a reference to the config data.")
    }
    val cloudsim = new CloudSim();
    val broker0 = new DatacenterBrokerSimple(cloudsim);
    
    val hostPes = (1 to 4).map(_ => new PeSimple(config.getLong(configKey+".host.mipsCapacity")))
    logger.info(s"Created one processing element: $hostPes")

    val hostList = List(new HostSimple(config.getLong(configKey+".host.RAMInMBs"),
      config.getLong(configKey+".host.StorageInMBs"),
      config.getLong(configKey+".host.BandwidthInMBps"),
      hostPes.asJava))

    logger.info(s"Created one host: $hostList")


    val dc0 = new DatacenterSimple(cloudsim, hostList.asJava)
                  .getCharacteristics
                  .setCostPerBw(02)
                  .setCostPerSecond(0.01)
                  .setCostPerMem(0.02)
                  .setCostPerStorage(0.001)

    
    val schedulingAlgorithm = config.getString(configKey+".scheduling")
    val scheduler = schedulingAlgorithm match{
      case "space" => new CloudletSchedulerSpaceShared()
      case "time" => new CloudletSchedulerTimeShared()
      case _ => new CloudletSchedulerTimeShared()
    }
    val vmList = List(
      new VmSimple(config.getLong(configKey+".vm.mipsCapacity"), 4)
        .setRam(config.getLong(configKey+".vm.RAMInMBs"))
        .setBw(config.getLong(configKey+".vm.BandwidthInMBps"))
        .setSize(config.getLong(configKey+".vm.StorageInMBs"))
        .setCloudletScheduler(scheduler)
    )
    logger.info(s"Created one virtual machine: $vmList")

    val cloudletNo = config.getInt(configKey+".cloudlet.number")
    logger.info(s"number of cloudlet No $cloudletNo")
    val cloudletList = {
      (1 to cloudletNo).map(_ => CloudletSimple(config.getLong(configKey + ".cloudlet.size"), config.getInt(configKey + ".cloudlet.PEs")))
    }

    logger.info(s"Created a list of cloudlets: $cloudletList")

    broker0.submitVmList(vmList.asJava);
    broker0.submitCloudletList(cloudletList.asJava);

    logger.info("Starting cloud simulation...")
    cloudsim.start();

    new CloudletsTableBuilder(broker0.getCloudletFinishedList()).build();