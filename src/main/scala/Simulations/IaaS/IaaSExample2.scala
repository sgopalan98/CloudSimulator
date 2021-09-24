/*
 * CloudSim Plus: A modern, highly-extensible and easier-to-use Framework for
 * Modeling and Simulation of Cloud Computing Infrastructures and Services.
 * http://cloudsimplus.org
 *
 *     Copyright (C) 2015-2018 Universidade da Beira Interior (UBI, Portugal) and
 *     the Instituto Federal de Educação Ciência e Tecnologia do Tocantins (IFTO, Brazil).
 *
 *     This file is part of CloudSim Plus.
 *
 *     CloudSim Plus is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     CloudSim Plus is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with CloudSim Plus. If not, see <http://www.gnu.org/licenses/>.
 */

package Simulations.IaaS

import ch.qos.logback.classic.Level
import org.cloudbus.cloudsim.allocationpolicies._
import org.cloudbus.cloudsim.allocationpolicies.migration.VmAllocationPolicyMigrationBestFitStaticThreshold
import org.cloudbus.cloudsim.allocationpolicies.migration.VmAllocationPolicyMigrationStaticThreshold
import org.cloudbus.cloudsim.brokers.DatacenterBroker
import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple
import org.cloudbus.cloudsim.cloudlets.Cloudlet
import org.cloudbus.cloudsim.cloudlets.CloudletSimple
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.datacenters.Datacenter
import org.cloudbus.cloudsim.datacenters.DatacenterSimple
import org.cloudbus.cloudsim.hosts.Host
import org.cloudbus.cloudsim.hosts.HostSimple
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple
import org.cloudbus.cloudsim.provisioners.ResourceProvisionerSimple
import org.cloudbus.cloudsim.resources.Pe
import org.cloudbus.cloudsim.resources.PeSimple
import org.cloudbus.cloudsim.schedulers.MipsShare
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletSchedulerTimeShared
import org.cloudbus.cloudsim.schedulers.vm.VmSchedulerTimeShared
import org.cloudbus.cloudsim.selectionpolicies.VmSelectionPolicyMinimumUtilization
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModel
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelDynamic
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelFull
import org.cloudbus.cloudsim.vms.Vm
import org.cloudbus.cloudsim.vms.VmSimple
import org.cloudsimplus.builders.tables.CloudletsTableBuilder
import org.cloudsimplus.builders.tables.HostHistoryTableBuilder
import Simulations.IaaS.IaaSExample2._
import org.cloudsimplus.listeners.DatacenterBrokerEventInfo
import org.cloudsimplus.listeners.EventListener
import org.cloudsimplus.listeners.VmHostEventInfo
import org.cloudsimplus.util.Log

//remove if not needed
import scala.collection.JavaConverters.*

object IaaSExample2 {

  /**
   * @see Datacenter#getSchedulingInterval()
   */ /**
   * @see Datacenter#getSchedulingInterval()
   */
  private val SCHEDULING_INTERVAL: Int = 1

  /**
   * The percentage of host CPU usage that trigger VM migration
   * due to under utilization (in scale from 0 to 1, where 1 is 100%).
   */ /**
   * The percentage of host CPU usage that trigger VM migration
   * due to under utilization (in scale from 0 to 1, where 1 is 100%).
   */
  private val HOST_UNDER_UTILIZATION_THRESHOLD_FOR_VM_MIGRATION: Double = 0.1

  /**
   * The percentage of host CPU usage that trigger VM migration
   * due to over utilization (in scale from 0 to 1, where 1 is 100%).
   */ /**
   * The percentage of host CPU usage that trigger VM migration
   * due to over utilization (in scale from 0 to 1, where 1 is 100%).
   */
  private val HOST_OVER_UTILIZATION_THRESHOLD_FOR_VM_MIGRATION: Double = 0.7

  /**
  @see Datacenter#setHostSearchRetryDelay(double)
   */ /**
  @see Datacenter#setHostSearchRetryDelay(double)
   */
  private val HOST_SEARCH_RETRY_DELAY: Int = 60

  //Mb/s
  private val HOST_BW: Long = 16000L

  //for each PE
  private val HOST_MIPS: Int = 1000

  //host memory (MB)
  private val HOST_RAM: List[Long] = List(15000L, 500000L, 25000L)

  //host storage
  private val HOST_STORAGE: Long = 1000000

  /**
   * An array where each item defines the number of PEs for each Host to be created.
   * The length of the array represents the number of Hosts.
   */ /**
   * An array where each item defines the number of PEs for each Host to be created.
   * The length of the array represents the number of Hosts.
   */
  private val HOST_PES: List[Int] = List(4, 5, 5)

  private val VM_PES: List[Int] = List(2, 2, 2, 1)

  //for each PE
  private val VM_MIPS: Int = 1000

  //image size (MB)
  private val VM_SIZE: Long = 1000

  //VM memory (MB)
  private val VM_RAM: Int = 10000

  private val VM_BW: Double = HOST_BW / VM_PES.length.toDouble

  private val CLOUDLET_LENGTH: Long = 20000

  private val CLOUDLET_FILESIZE: Long = 300

  private val CLOUDLET_OUTPUTSIZE: Long = 300

  /**
   * The percentage of CPU that a cloudlet will use when
   * it starts executing (in scale from 0 to 1, where 1 is 100%).
   * For each cloudlet create, this value is used
   * as a base to define CPU usage.
   * @see #createAndSubmitCloudlets(DatacenterBroker)
   */ /**
   * The percentage of CPU that a cloudlet will use when
   * it starts executing (in scale from 0 to 1, where 1 is 100%).
   * For each cloudlet create, this value is used
   * as a base to define CPU usage.
   * @see #createAndSubmitCloudlets(DatacenterBroker)
   */
  private val CLOUDLET_INITIAL_CPU_PERCENTAGE: Double = 0.8

  /**
   * Defines the speed (in percentage) that CPU usage of a cloudlet
   * will increase during the simulation execution.
   * (in scale from 0 to 1, where 1 is 100%).
   * @see #createCpuUtilizationModel(double, double)
   */ /**
   * Defines the speed (in percentage) that CPU usage of a cloudlet
   * will increase during the simulation execution.
   * (in scale from 0 to 1, where 1 is 100%).
   * @see #createCpuUtilizationModel(double, double)
   */
  private val CLOUDLET_CPU_INCREMENT_PER_SECOND: Double = 0.04

  def main(args: Array[String]): Unit = {
    new IaaSExample2()
  }

}

/**
 * An example showing how to create 1 Datacenter with: 5 hosts
 * with increasing number of PEs (starting at 4 PEs for the 1st host); 3 VMs with 2 PEs each one;
 * and 1 cloudlet by VM, each one having the same number of PEs from its VM.
 *
 *
 * <p>The example then performs VM migration using
 * a {@link VmAllocationPolicyMigrationBestFitStaticThreshold}.
 * Such a policy migrates VMs based on
 * a static host CPU utilization threshold.
 * The VmAllocationPolicy used in this example ignores power usage of Hosts.
 * This way, it isn't required to set a PowerModel for Hosts.</p>
 *
 * <p>According to the allocation policy, VM 0 will be allocated to Host 0.
 * Since Host 0 has just 4 PEs, allocating a second VM into it
 * would cause overload.
 * Each cloudlet will start using 80% of its VM CPU.
 * As the VM 0 will run one Cloudlet and requires just 2 PEs from Host 0 (which has 4 PEs),
 * the initial Host CPU usage will be just 40% (1 VM using 80% of 2 PEs from a total of 4 Host PEs = 0.8*2 / 4).
 *
 * Allocating a second VM into Host 0 would double the Host CPU utilization,
 * overreaching its upper utilization threshold (defined as 70%).
 * This way, VMs 1 and 2 are allocated to Host 1 which has 5 PEs.
 * </p>
 *
 * <p>The {@link VmAllocationPolicyMigrationBestFitStaticThreshold}
 * allows the definition of static under and over CPU utilization thresholds to
 * enable VM migration.
 * The example uses a {@link UtilizationModelDynamic} to define that the CPU usage of cloudlets
 * increases along the simulation time.
 * The first 2 Cloudlets all start with a usage of 80% of CPU,
 * that increases along the time (see {@link #CLOUDLET_CPU_INCREMENT_PER_SECOND}).
 * The third Cloudlet starts a a lower CPU usage and increases in the same way.
 * </p>
 *
 * <p>Some constants are used to create simulation objects such as
 * {@link  DatacenterSimple}, {@link  Host} and {@link  Vm}.
 * The values of these constants were careful and accordingly chosen to allow
 * migration of VMs due to either under and overloaded hosts and
 * to allow one developer to know exactly how the simulation will run
 * and what will be the final results.
 * Several values impact the simulation results, such as
 * hosts CPU capacity and number of PEs, VMs and cloudlets requirements
 * and even VM bandwidth (which defines the VM migration time).</p>
 *
 * <p>This way, if you want to change these values, you must
 * define new appropriated ones to allow the simulation
 * to run correctly.</p>
 *
 * <p>Realize that the Host State History is just collected
 * if {@link Host#isStateHistoryEnabled() history is enabled}
 * by calling {@link Host#enableStateHistory()}.</p>
 *
 * @author Manoel Campos da Silva Filho
 *
 * TODO Verify if inter-datacenter VM migration is working by default using the DatacenterBroker class.
 */
class IaaSExample2 private () {

  /**
   * List of all created VMs.
   */ /**
   * List of all created VMs.
   */
  //  private val vmList: List[Vm] = new ArrayList()

  private val simulation: CloudSim = new CloudSim()

  private val broker: DatacenterBrokerSimple = new DatacenterBrokerSimple(
    simulation)



  //  private var allocationPolicy: VmAllocationPolicyMigrationStaticThreshold = _

  //  private var hostList: List[Host] = _

  private var migrationsNumber: Int = 0

  if (HOST_PES.length != HOST_RAM.length) {
    throw new IllegalStateException(
      "The length of arrays HOST_PES and HOST_RAM must match.")
  }

  println("Starting " + getClass.getSimpleName)

  Log.setLevel(CloudSim.LOGGER, Level.WARN)

  val hostList = createHostList()
  val allocationPolicy = createAllocationPolicy()
  val datacenter0: Datacenter = createDatacenter(hostList, allocationPolicy)

  Log.setLevel(DatacenterBroker.LOGGER, Level.WARN)

  val vmList = createAndSubmitVms(broker)

  val cloudletList = createAndSubmitCloudlets(broker, vmList)

//  broker.addOnVmsCreatedListener((info) => {
//    allocationPolicy.setOverUtilizationThreshold(
//      HOST_OVER_UTILIZATION_THRESHOLD_FOR_VM_MIGRATION)
//    broker.removeOnVmsCreatedListener(info.getListener)
//
//    vmList.map((vm) => showVmAllocatedMips(vm, vm.getHost, info.getTime))
//    println()
//    hostList.map((host) => showHostAllocatedMips(info.getTime, host))
//    println()
//  })

  simulation.start()

  val finishedList: List[Cloudlet] = broker.getCloudletFinishedList.asScala.toList

  //  finishedList.sort(
  //    Comparator
  //      .comparingLong((c: Cloudlet) => c.getVm.getHost.getId)
  //      .thenComparingLong((c) => c.getVm.getId))

  new CloudletsTableBuilder(finishedList.asJava).build()

  System.out.printf(
    "%nHosts CPU usage History (when the allocated MIPS is lower than the requested, it is due to VM migration overhead)%n")


  hostList.filter(host => host.getId <=2).map(this.printHostStateHistory(_))
  //  hostList
  //    .stream()
  //    .filter((h) => h.getId <= 2)
  //    .forEach(this.printHostStateHistory)

  System.out.printf("Number of VM migrations: %d%n", migrationsNumber)

  println(getClass.getSimpleName + " finished!")

  /**
   * A listener method that is called when a VM migration starts.
   * @param info information about the happened event
   *
   * @see #createAndSubmitVms(DatacenterBroker)
   * @see Vm#addOnMigrationFinishListener(EventListener)
   */
  private def startMigration(info: VmHostEventInfo): Unit = {
    val vm: Vm = info.getVm
    val targetHost: Host = info.getHost
    System.out.printf(
      "# %.2f: %s started migrating to %s (you can perform any operation you want here)%n",
      info.getTime,
      vm,
      targetHost)
    showVmAllocatedMips(vm, targetHost, info.getTime)
    //VM current host (source)
    showHostAllocatedMips(info.getTime, vm.getHost)
    //Migration host (target)
    showHostAllocatedMips(info.getTime, targetHost)
    System.out.println("Migrations happening")
    //    println() { migrationsNumber += 1; migrationsNumber - 1 }
    if (migrationsNumber > 1) {
      return
    }
    //After the first VM starts being migrated, tracks some metrics along simulation time
    simulation.addOnClockTickListener(
      (clock) =>
        if (clock.getTime <= 2 || (clock.getTime >= 11 && clock.getTime <= 15))
          showVmAllocatedMips(vm, targetHost, clock.getTime))
  }

  private def showVmAllocatedMips(vm: Vm,
                                  targetHost: Host,
                                  time: Double): Unit = {
    val msg: String =
      String.format("# %.2f: %s in %s: total allocated", time, vm, targetHost)
    val allocatedMips: MipsShare =
      targetHost.getVmScheduler.getAllocatedMips(vm)
    val msg2: String =
      if (allocatedMips.totalMips() == VM_MIPS * 0.9)
        " - reduction due to migration overhead"
      else ""
    System.out.printf("%s %.0f MIPs (divided by %d PEs)%s\n",
      msg,
      allocatedMips.totalMips(),
      allocatedMips.pes(),
      msg2)
  }

  /**
   * A listener method that is called when a VM migration finishes.
   * @param info information about the happened event
   *
   * @see #createAndSubmitVms(DatacenterBroker)
   * @see Vm#addOnMigrationStartListener(EventListener)
   */
  private def finishMigration(info: VmHostEventInfo): Unit = {
    val host: Host = info.getHost
    System.out.printf(
      "# %.2f: %s finished migrating to %s (you can perform any operation you want here)%n",
      info.getTime,
      info.getVm,
      host)
    System.out.print("\t\t")
    //    showHostAllocatedMips(info.getTime, hostList.get(1))
    //    System.out.print("\t\t")
    //    showHostAllocatedMips(info.getTime, host)
  }

  private def showHostAllocatedMips(time: Double, host: Host): Unit = {
    System.out.printf(
      "%.2f: %s allocated %.2f MIPS from %.2f total capacity%n",
      time,
      host,
      host.getTotalAllocatedMips,
      host.getTotalMipsCapacity)
  }

  private def printHostStateHistory(host: Host): Unit = {
    new HostHistoryTableBuilder(host).setTitle(host.toString).build()
  }

  def createAndSubmitCloudlets(broker: DatacenterBroker, vmList: List[Vm]): List[Cloudlet] = {
    //    val list: List[Cloudlet] = new ArrayList[Cloudlet](VM_PES.length)
    var cloudlet: Cloudlet = Cloudlet.NULL
//    val um: UtilizationModelDynamic =
      createCpuUtilizationModel(CLOUDLET_INITIAL_CPU_PERCENTAGE, 1)
    val cloudletList = vmList.map(vm => {
      val utilizationModelFull: UtilizationModel = new UtilizationModelFull()
      val cloudlet: Cloudlet =
        new CloudletSimple(CLOUDLET_LENGTH, vm.getNumberOfPes.toInt)
          .setFileSize(CLOUDLET_FILESIZE)
          .setOutputSize(CLOUDLET_OUTPUTSIZE)
          .setUtilizationModelRam(utilizationModelFull)
          .setUtilizationModelBw(utilizationModelFull)
      broker.bindCloudletToVm(cloudlet, vm)
      cloudlet
    })
    //    for (vm <- vmList) {
    //      cloudlet = createCloudlet(vm, broker, um)
    //      list.add(cloudlet)
    //    }
    //Changes the CPU usage of the last cloudlet to start at a lower value and increase dynamically up to 100%
//    cloudlet.setUtilizationModelCpu(createCpuUtilizationModel(0.2, 1))
    broker.submitCloudletList(cloudletList.asJava)
    cloudletList
  }

  /**
   * Creates a Cloudlet.
   *
   * @param vm the VM that will run the Cloudlets
   * @param broker the broker that the created Cloudlets belong to
   * @param cpuUtilizationModel the CPU UtilizationModel for the Cloudlet
   * @return the created Cloudlets
   */
  def createCloudlet(vm: Vm,
                     broker: DatacenterBroker,
                     cpuUtilizationModel: UtilizationModel): Cloudlet = {
    val utilizationModelFull: UtilizationModel = new UtilizationModelFull()
    val cloudlet: Cloudlet =
      new CloudletSimple(CLOUDLET_LENGTH, vm.getNumberOfPes.toInt)
        .setFileSize(CLOUDLET_FILESIZE)
        .setOutputSize(CLOUDLET_OUTPUTSIZE)
        .setUtilizationModelRam(utilizationModelFull)
        .setUtilizationModelBw(utilizationModelFull)
        .setUtilizationModelCpu(cpuUtilizationModel)
    broker.bindCloudletToVm(cloudlet, vm)
    cloudlet
  }

  def createAndSubmitVms(broker: DatacenterBroker): List[Vm] = {
    val vmList = VM_PES.map(vmPe => {
      val vm: Vm = new VmSimple(VM_MIPS, vmPe)
      vm.setRam(VM_RAM)
        .setBw(VM_BW.toLong)
        .setSize(VM_SIZE)
        .setCloudletScheduler(new CloudletSchedulerTimeShared())
//      vm.addOnMigrationStartListener(this.startMigration)
//      vm.addOnMigrationFinishListener(this.finishMigration)
      vm
    })
    broker.submitVmList(vmList.asJava)
    vmList
  }

  def createVm(pes: Int): Vm = {
    val vm: Vm = new VmSimple(VM_MIPS, pes)
    vm.setRam(VM_RAM)
      .setBw(VM_BW.toLong)
      .setSize(VM_SIZE)
      .setCloudletScheduler(new CloudletSchedulerTimeShared())
    vm
  }

  /**
   * Creates a CPU UtilizationModel for a Cloudlet.
   * If the initial usage is lower than the max usage, the usage will
   * be dynamically incremented along the time, according to the
   * {@link #getCpuUsageIncrement(UtilizationModelDynamic)}
   * function. Otherwise, the CPU usage will be static, according to the
   * defined initial usage.
   *
   * @param initialCpuUsagePercent the percentage of CPU utilization
   * that created Cloudlets will use when they start to execute.
   * If this value is greater than 1 (100%), it will be changed to 1.
   * @param maxCpuUsagePercentage the maximum percentage of
   * CPU utilization that created Cloudlets are allowed to use.
   * If this value is greater than 1 (100%), it will be changed to 1.
   * It must be equal or greater than the initial CPU usage.
   * @return
   */
  private def createCpuUtilizationModel(
                                         initialCpuUsagePercent: Double,
                                         maxCpuUsagePercentage: Double): UtilizationModelDynamic = {
    if (maxCpuUsagePercentage < initialCpuUsagePercent) {
      throw new IllegalArgumentException(
        "Max CPU usage must be equal or greater than the initial CPU usage.")
    }
    //    val initialCpuUsagePercent = Math.min(initialCpuUsagePercent, 1)
    //    val maxCpuUsagePercentage = Math.min(maxCpuUsagePercentage, 1)
    var um: UtilizationModelDynamic = if (Math.min(initialCpuUsagePercent, 1) < Math.min(maxCpuUsagePercentage, 1))
      new UtilizationModelDynamic(Math.min(initialCpuUsagePercent, 1))
        .setUtilizationUpdateFunction(this.getCpuUsageIncrement)
    else new UtilizationModelDynamic(Math.min(initialCpuUsagePercent, 1))
    um.setMaxResourceUtilization(Math.min(maxCpuUsagePercentage, 1))
    um
  }

  /**
   * Increments the CPU resource utilization, that is defined in percentage values.
   * @return the new resource utilization after the increment
   */
  private def getCpuUsageIncrement(um: UtilizationModelDynamic): Double =
    um.getUtilization + um.getTimeSpan * CLOUDLET_CPU_INCREMENT_PER_SECOND


  private def createHostList() : List[Host] = {
    val hostList: List[Host] = (1 to HOST_PES.length).map(index => {
      val pes = HOST_PES(index-1)
      val ram = HOST_RAM(index-1)
      val peList: List[PeSimple] = (1 to pes).map(index => new PeSimple(HOST_MIPS, new PeProvisionerSimple())).toList
      val host: Host = new HostSimple(ram, HOST_BW, HOST_STORAGE, peList.asJava)
      host
        .setRamProvisioner(new ResourceProvisionerSimple())
        .setBwProvisioner(new ResourceProvisionerSimple())
        .setVmScheduler(new VmSchedulerTimeShared())
      host.enableStateHistory()
      host
    }).toList
    hostList
  }

  private def createAllocationPolicy() = {
    val allocationPolicy = new VmAllocationPolicyFirstFit
    allocationPolicy
  }
  /**
   * Creates a Datacenter with number of Hosts defined by the length of {@link #HOST_PES},
   * but only some of these Hosts will be active (powered on) initially.
   *
   * @return
   */
  private def createDatacenter(hostList: List[Host], allocationPolicy: VmAllocationPolicy): Datacenter = {

    val dc: DatacenterSimple =
      new DatacenterSimple(simulation, hostList.asJava, allocationPolicy)
    dc
  }



}
