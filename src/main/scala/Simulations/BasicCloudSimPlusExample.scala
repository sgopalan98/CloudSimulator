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
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelDynamic
import org.cloudbus.cloudsim.vms.VmSimple
import org.cloudsimplus.builders.tables.CloudletsTableBuilder
import java.util

import collection.JavaConverters.*

class BasicCloudSimPlusExample

object BasicCloudSimPlusExample:
  val config = ObtainConfigReference("cloudSimulator") match {
    case Some(value) => value
    case None => throw new RuntimeException("Cannot obtain a reference to the config data.")
  }
  val logger = CreateLogger(classOf[BasicCloudSimPlusExample])

  def Start() =
    val cloudsim = new CloudSim();
    val broker0 = new DatacenterBrokerSimple(cloudsim);

    val hostPes = Seq(new PeSimple(config.getLong("cloudSimulator.host.mipsCapacity")))
    val hostPes2 = Seq(new PeSimple(config.getLong("cloudSimulator.host.mipsCapacity")))
    logger.info(s"Created one processing element: $hostPes")

    val hostListTemp = List(new HostSimple(config.getLong("cloudSimulator.host.RAMInMBs"),
      config.getLong("cloudSimulator.host.StorageInMBs"),
      config.getLong("cloudSimulator.host.BandwidthInMBps"),
      hostPes.asJava),
      new HostSimple(config.getLong("cloudSimulator.host.RAMInMBs"),
        config.getLong("cloudSimulator.host.StorageInMBs"),
        config.getLong("cloudSimulator.host.BandwidthInMBps"),
        hostPes2.asJava))

//    logger.info(s"Created one host: $hostList")

    val a = new util.ArrayList[Host]
    val hostList = hostListTemp.map(a.add(_))

    val dc0 = new DatacenterSimple(cloudsim, a);

    val vmList = List(
      new VmSimple(config.getLong("cloudSimulator.vm.mipsCapacity"), hostPes.length)
      .setRam(config.getLong("cloudSimulator.vm.RAMInMBs"))
      .setBw(config.getLong("cloudSimulator.vm.BandwidthInMBps"))
      .setSize(config.getLong("cloudSimulator.vm.StorageInMBs")),
      new VmSimple(config.getLong("cloudSimulator.vm.mipsCapacity"), hostPes.length)
        .setRam(config.getLong("cloudSimulator.vm.RAMInMBs"))
        .setBw(config.getLong("cloudSimulator.vm.BandwidthInMBps"))
        .setSize(config.getLong("cloudSimulator.vm.StorageInMBs"))
    )
    logger.info(s"Created one virtual machine: $vmList")

    val utilizationModel = new UtilizationModelDynamic(config.getDouble("cloudSimulator.utilizationRatio"));
    val cloudletList = new CloudletSimple(config.getLong("cloudSimulator.cloudlet.size"), config.getInt("cloudSimulator.cloudlet.PEs"), utilizationModel) ::
      new CloudletSimple(config.getLong("cloudSimulator.cloudlet.size"), config.getInt("cloudSimulator.cloudlet.PEs"), utilizationModel) :: Nil

    logger.info(s"Created a list of cloudlets: $cloudletList")

    broker0.submitVmList(vmList.asJava);
    broker0.submitCloudletList(cloudletList.asJava);

    logger.info("Starting cloud simulation...")
    cloudsim.start();

    new CloudletsTableBuilder(broker0.getCloudletFinishedList()).build();