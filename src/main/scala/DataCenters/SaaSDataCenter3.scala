package DataCenters


import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicy
import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.hosts.{Host, HostSimple}
import org.cloudbus.cloudsim.provisioners.{PeProvisionerSimple, ResourceProvisionerSimple}
import org.cloudbus.cloudsim.schedulers.vm.VmSchedulerTimeShared

import collection.JavaConverters.*
import org.cloudbus.cloudsim.resources.{Pe, PeSimple, Processor}
import org.slf4j.Logger
import org.cloudbus.cloudsim.datacenters.DatacenterSimple
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletSchedulerTimeShared
import org.cloudbus.cloudsim.vms.{Vm, VmSimple}
import org.cloudsimplus.autoscaling.{HorizontalVmScalingSimple, VerticalVmScaling, VerticalVmScalingSimple}
import org.cloudsimplus.listeners.EventInfo

class SaaSDataCenter3 extends SaaSDataCenter{

  override def createVms(vmNumbers: Int, vmMips: Long, vmPes: Int, vmRam: Long, vmBandwith: Long): List[VmSimple] = {
    val vms = (1 to vmNumbers).map(index => {
      val verticalVmScalingSimple = new VerticalVmScalingSimple(classOf[Pe], 0.1)
      verticalVmScalingSimple.setResourceScaling((vs: VerticalVmScaling) => 2 * vs.getScalingFactor * vs.getAllocatedResource)
      verticalVmScalingSimple.setLowerThresholdFunction((vm:Vm) => 0.1D)
      verticalVmScalingSimple.setUpperThresholdFunction((vm:Vm) => 0.8D)
      val vm = new VmSimple(index, vmMips, 2)
        .setPeVerticalScaling(verticalVmScalingSimple)
      vm.enableUtilizationStats()
      vm.asInstanceOf[VmSimple]
    }).toList
    vms
  }

  override def createHosts(logger: Logger, hostNos: Int, pes: Int, mips: Long, ram: Long, bw: Long, storage: Long): IndexedSeq[Host] = {
    val hosts = (1 to hostNos).map(HOSTS => {
      (1 to pes).map(pes => PeSimple(mips, new PeProvisionerSimple())
      )
    }).map(peList =>
      new HostSimple(ram, bw, storage, peList.toList.asJava)
        .setRamProvisioner(new ResourceProvisionerSimple())
        .setBwProvisioner(new ResourceProvisionerSimple())
        .setVmScheduler(new VmSchedulerTimeShared());
    )
    hosts
  }

  def createDataCenter(simulation: CloudSim, hosts: List[Host], allocationPolicy: VmAllocationPolicy, costPerSecond: Double, costPerMem: Double, costPerStorage: Double, costPerBw: Double) : DatacenterSimple =
    val dataCenter = DatacenterSimple(simulation, hosts.asJava, allocationPolicy)
    dataCenter.getCharacteristics()
      .setCostPerSecond(costPerSecond)
      .setCostPerMem(costPerMem)
      .setCostPerStorage(costPerStorage)
      .setCostPerBw(costPerBw)
    dataCenter

  }

