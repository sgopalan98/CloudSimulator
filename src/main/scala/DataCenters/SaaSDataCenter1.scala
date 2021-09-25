package DataCenters


import HelperUtils.ObtainConfigReference
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
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletSchedulerSpaceShared
import org.cloudbus.cloudsim.vms.VmSimple
import org.cloudsimplus.listeners.EventInfo

class SaaSDataCenter1 extends SaaSDataCenter {
//
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
//  private val HOST_BW: Long = 16000L //Mb/s
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

  @Override
  def createHosts(logger: Logger, hostNos: Int, pes: Int, mips: Long, ram: Long, bw: Long, storage: Long): IndexedSeq[Host] = {
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

  @Override
  def createDataCenter(simulation: CloudSim, hosts: List[Host], allocationPolicy: VmAllocationPolicy, costPerSecond: Double, costPerMem: Double, costPerStorage: Double, costPerBw: Double) : DatacenterSimple =
    val dataCenter = DatacenterSimple(simulation, hosts.asJava, allocationPolicy)
    dataCenter.getCharacteristics()
      .setCostPerSecond(costPerSecond)
      .setCostPerMem(costPerMem)
      .setCostPerStorage(costPerStorage)
      .setCostPerBw(costPerBw)
    dataCenter

  @Override
  def createVms(vmNumbers: Int, vmMips: Long, vmPes: Int, vmRam: Long, vmBandwith: Long): List[VmSimple] =
    val vms = (1 to vmNumbers).map(index => {
      val vm: VmSimple = new VmSimple(vmMips, vmPes)
        .setRam(vmRam)
        .setBw(vmBandwith)
        .asInstanceOf[VmSimple]

      vm.enableUtilizationStats()
      vm
    }).toList
    vms

}
