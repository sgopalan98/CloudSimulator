

package Simulations.IaaS

import HelperUtils.ObtainConfigReference
import ch.qos.logback.classic.Level
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicy
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
import org.cloudsimplus.listeners.DatacenterBrokerEventInfo
import org.cloudsimplus.listeners.EventListener
import org.cloudsimplus.listeners.VmHostEventInfo
import org.cloudsimplus.util.Log

//remove if not needed
import scala.collection.JavaConverters.*

class IaaSExample1 {

  def Start() = {

    val configFile = "iaasconfig.conf"

    val iaasConfig = ObtainConfigReference("DataCenter", configFile) match {
      case Some(value) => value
      case None => throw new RuntimeException("Cannot obtain a reference to the config data.")
    }

    val SCHEDULING_INTERVAL: Int = iaasConfig.getInt("DataCenter.SCHEDULING_INTERVAL")

    val HOST_UNDER_UTILIZATION_THRESHOLD_FOR_VM_MIGRATION: Double = iaasConfig.getDouble("DataCenter.HOST_UNDER_UTILIZATION_THRESHOLD_FOR_VM_MIGRATION")

    val HOST_OVER_UTILIZATION_THRESHOLD_FOR_VM_MIGRATION: Double = iaasConfig.getDouble("DataCenter.HOST_OVER_UTILIZATION_THRESHOLD_FOR_VM_MIGRATION")

    val HOST_SEARCH_RETRY_DELAY: Int = iaasConfig.getInt("DataCenter.HOST_SEARCH_RETRY_DELAY")

    val HOST_BW: Long = iaasConfig.getLong("DataCenter.HOST_BW")


    val HOST_MIPS: Int = iaasConfig.getInt("DataCenter.HOST_MIPS")

    //host memory (MB)
    val HOST_RAM: List[Long] = List(iaasConfig.getLong("DataCenter.host1.RAMInMBs"), iaasConfig.getLong("DataCenter.host2.RAMInMBs"), iaasConfig.getLong("DataCenter.host3.RAMInMBs"))

    val HOST_STORAGE: Long = iaasConfig.getLong("DataCenter.HOST_STORAGE")

    val HOST_PES: List[Int] = List(iaasConfig.getInt("DataCenter.host1.PEs"), iaasConfig.getInt("DataCenter.host2.PEs"), iaasConfig.getInt("DataCenter.host3.PEs"))

    val VM_PES: List[Int] = List(iaasConfig.getInt("DataCenter.vm1.PEs"), iaasConfig.getInt("DataCenter.vm2.PEs"), iaasConfig.getInt("DataCenter.vm3.PEs"), iaasConfig.getInt("DataCenter.vm4.PEs"))

    val VM_MIPS: Int = iaasConfig.getInt("DataCenter.VM_MIPS")

    //image size (MB)
    val VM_SIZE: Long = iaasConfig.getLong("DataCenter.VM_SIZE")

    //VM memory (MB)
    val VM_RAM: Int = iaasConfig.getInt("DataCenter.VM_RAM")

    val VM_BW: Double = HOST_BW / VM_PES.length.toDouble

    val CLOUDLET_LENGTH: Long = iaasConfig.getLong("DataCenter.CLOUDLET_LENGTH")

    val CLOUDLET_FILESIZE: Long = iaasConfig.getLong("DataCenter.CLOUDLET_FILESIZE")

    val CLOUDLET_OUTPUTSIZE: Long = iaasConfig.getLong("DataCenter.CLOUDLET_OUTPUTSIZE")

    val CLOUDLET_INITIAL_CPU_PERCENTAGE: Double = iaasConfig.getDouble("DataCenter.CLOUDLET_INITIAL_CPU_PERCENTAGE")

    val CLOUDLET_CPU_INCREMENT_PER_SECOND: Double = iaasConfig.getDouble("DataCenter.CLOUDLET_CPU_INCREMENT_PER_SECOND")

    val simulation: CloudSim = new CloudSim()

    val broker: DatacenterBrokerSimple = new DatacenterBrokerSimple(
      simulation)

    var migrationsNumber: Int = 0

    println("Starting " + getClass.getSimpleName)

    Log.setLevel(CloudSim.LOGGER, Level.WARN)

    val hostList = createHostList(HOST_PES, HOST_RAM, HOST_MIPS, HOST_BW, HOST_STORAGE)
    val allocationPolicy = createAllocationPolicy(HOST_OVER_UTILIZATION_THRESHOLD_FOR_VM_MIGRATION, HOST_UNDER_UTILIZATION_THRESHOLD_FOR_VM_MIGRATION)
    val datacenter0: Datacenter = createDatacenter(hostList, allocationPolicy, simulation, SCHEDULING_INTERVAL, HOST_SEARCH_RETRY_DELAY)

    Log.setLevel(DatacenterBroker.LOGGER, Level.WARN)

    val vmList = createAndSubmitVms(broker, VM_PES, VM_MIPS, VM_RAM, VM_BW, VM_SIZE, simulation)

    val cloudletList = createAndSubmitCloudlets(broker, vmList, CLOUDLET_INITIAL_CPU_PERCENTAGE, CLOUDLET_LENGTH, CLOUDLET_FILESIZE, CLOUDLET_OUTPUTSIZE, CLOUDLET_CPU_INCREMENT_PER_SECOND)

    broker.addOnVmsCreatedListener((info) => {
      allocationPolicy.setOverUtilizationThreshold(
        HOST_OVER_UTILIZATION_THRESHOLD_FOR_VM_MIGRATION)
      broker.removeOnVmsCreatedListener(info.getListener)

      vmList.map((vm) => showVmAllocatedMips(vm, vm.getHost, info.getTime, VM_MIPS))
      println()
      hostList.map((host) => showHostAllocatedMips(info.getTime, host))
      println()
    })

    simulation.start()

    val finishedList: List[Cloudlet] = broker.getCloudletFinishedList.asScala.toList

    new CloudletsTableBuilder(finishedList.asJava).build()

    System.out.printf(
      "%nHosts CPU usage History (when the allocated MIPS is lower than the requested, it is due to VM migration overhead)%n")


    hostList.filter(host => host.getId <= 2).map(printHostStateHistory(_))

    System.out.printf("Number of VM migrations: %d%n", migrationsNumber)

    println(getClass.getSimpleName + " finished!")
  }


    def showVmAllocatedMips(vm: Vm,
                                    targetHost: Host,
                                    time: Double, VM_MIPS: Long): Unit = {
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

    def finishMigration(info: VmHostEventInfo): Unit = {
      val host: Host = info.getHost
      System.out.printf(
        "# %.2f: %s finished migrating to %s (you can perform any operation you want here)%n",
        info.getTime,
        info.getVm,
        host)
      System.out.print("\t\t")
    }

    def showHostAllocatedMips(time: Double, host: Host): Unit = {
      System.out.printf(
        "%.2f: %s allocated %.2f MIPS from %.2f total capacity%n",
        time,
        host,
        host.getTotalAllocatedMips,
        host.getTotalMipsCapacity)
    }

    def printHostStateHistory(host: Host): Unit = {
      new HostHistoryTableBuilder(host).setTitle(host.toString).build()
    }

    def createAndSubmitCloudlets(broker: DatacenterBroker, vmList: List[Vm], CLOUDLET_INITIAL_CPU_PERCENTAGE: Double, CLOUDLET_LENGTH: Long, CLOUDLET_FILESIZE: Long,CLOUDLET_OUTPUTSIZE: Long, CLOUDLET_CPU_INCREMENT_PER_SECOND: Double): List[Cloudlet] = {
      //    val list: List[Cloudlet] = new ArrayList[Cloudlet](VM_PES.length)
      var cloudlet: Cloudlet = Cloudlet.NULL
      val um: UtilizationModelDynamic =
        createCpuUtilizationModel(CLOUDLET_INITIAL_CPU_PERCENTAGE, 1, CLOUDLET_CPU_INCREMENT_PER_SECOND)
      val cloudletList = vmList.map(vm => {
        val utilizationModelFull: UtilizationModel = new UtilizationModelFull()
        val cloudlet: Cloudlet =
          new CloudletSimple(CLOUDLET_LENGTH, vm.getNumberOfPes.toInt)
            .setFileSize(CLOUDLET_FILESIZE)
            .setOutputSize(CLOUDLET_OUTPUTSIZE)
            .setUtilizationModelRam(utilizationModelFull)
            .setUtilizationModelBw(utilizationModelFull)
            .setUtilizationModelCpu(um)
        broker.bindCloudletToVm(cloudlet, vm)
        cloudlet
      })
      broker.submitCloudletList(cloudletList.asJava)
      cloudletList
    }

    def createAndSubmitVms(broker: DatacenterBroker, VM_PES: List[Int], VM_MIPS: Int, VM_RAM: Long, VM_BW: Double, VM_SIZE: Long, simulation: CloudSim): List[Vm] = {
      val vmList = VM_PES.map(vmPe => {
        val vm: Vm = new VmSimple(VM_MIPS, vmPe)
        vm.setRam(VM_RAM)
          .setBw(VM_BW.toLong)
          .setSize(VM_SIZE)
          .setCloudletScheduler(new CloudletSchedulerTimeShared())
        vm.addOnMigrationStartListener((info) => {
          val vm: Vm = info.getVm
          val targetHost: Host = info.getHost
          System.out.printf(
            "# %.2f: %s started migrating to %s (you can perform any operation you want here)%n",
            info.getTime,
            vm,
            targetHost)
          showVmAllocatedMips(vm, targetHost, info.getTime, VM_MIPS)
          //VM current host (source)
          showHostAllocatedMips(info.getTime, vm.getHost)
          //Migration host (target)
          showHostAllocatedMips(info.getTime, targetHost)
          System.out.println("Migrations happening")
          //    println() { migrationsNumber += 1; migrationsNumber - 1 }
          //After the first VM starts being migrated, tracks some metrics along simulation time
          simulation.addOnClockTickListener(
            (clock) =>
              if (clock.getTime <= 2 || (clock.getTime >= 11 && clock.getTime <= 15))
                showVmAllocatedMips(vm, targetHost, clock.getTime, VM_MIPS))
        })
        vm.addOnMigrationFinishListener(finishMigration)
        vm
      })
      broker.submitVmList(vmList.asJava)
      vmList
    }



    def createCpuUtilizationModel(initialCpuUsagePercent: Double,
                                           maxCpuUsagePercentage: Double, CLOUDLET_CPU_INCREMENT_PER_SECOND: Double): UtilizationModelDynamic = {
      if (maxCpuUsagePercentage < initialCpuUsagePercent) {
        throw new IllegalArgumentException(
          "Max CPU usage must be equal or greater than the initial CPU usage.")
      }
      //    val initialCpuUsagePercent = Math.min(initialCpuUsagePercent, 1)
      //    val maxCpuUsagePercentage = Math.min(maxCpuUsagePercentage, 1)
      var um: UtilizationModelDynamic = if (Math.min(initialCpuUsagePercent, 1) < Math.min(maxCpuUsagePercentage, 1))
        new UtilizationModelDynamic(Math.min(initialCpuUsagePercent, 1))
          .setUtilizationUpdateFunction((um) => um.getUtilization + um.getTimeSpan * CLOUDLET_CPU_INCREMENT_PER_SECOND)
      else new UtilizationModelDynamic(Math.min(initialCpuUsagePercent, 1))
      um.setMaxResourceUtilization(Math.min(maxCpuUsagePercentage, 1))
      um
    }



    def createHostList(HOST_PES: List[Int], HOST_RAM: List[Long], HOST_MIPS: Long, HOST_BW: Long, HOST_STORAGE: Long): List[Host] = {
      val hostList: List[Host] = (1 to HOST_PES.length).map(index => {
        val pes = HOST_PES(index - 1)
        val ram = HOST_RAM(index - 1)
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

    def createAllocationPolicy(HOST_OVER_UTILIZATION_THRESHOLD_FOR_VM_MIGRATION: Double, HOST_UNDER_UTILIZATION_THRESHOLD_FOR_VM_MIGRATION: Double) = {
      val allocationPolicy =
        new VmAllocationPolicyMigrationBestFitStaticThreshold(
          new VmSelectionPolicyMinimumUtilization(),
          HOST_OVER_UTILIZATION_THRESHOLD_FOR_VM_MIGRATION + 0.2)
      Log.setLevel(VmAllocationPolicy.LOGGER, Level.WARN)
      allocationPolicy.setUnderUtilizationThreshold(
        HOST_UNDER_UTILIZATION_THRESHOLD_FOR_VM_MIGRATION)
      allocationPolicy
    }

    /**
     * Creates a Datacenter with number of Hosts defined by the length of {@link #HOST_PES},
     * but only some of these Hosts will be active (powered on) initially.
     *
     * @return
     */
    def createDatacenter(hostList: List[Host], allocationPolicy: VmAllocationPolicy, simulation: CloudSim, SCHEDULING_INTERVAL: Double, HOST_SEARCH_RETRY_DELAY: Long): Datacenter = {

      /**
       * Sets an upper utilization threshold higher than the
       * {@link #HOST_OVER_UTILIZATION_THRESHOLD_FOR_VM_MIGRATION}
       * to enable placing VMs which will use more CPU than
       * defined by the value in the mentioned constant.
       * After VMs are all submitted to Hosts, the threshold is changed
       * to the value of the constant.
       * This is used to  place VMs into a Host which will
       * become overloaded in order to trigger the migration.
       */

      val dc: DatacenterSimple =
        new DatacenterSimple(simulation, hostList.asJava, allocationPolicy)
      dc.setSchedulingInterval(SCHEDULING_INTERVAL)
        .setHostSearchRetryDelay(HOST_SEARCH_RETRY_DELAY)
      dc
    }

  //  private def onVmsCreatedListener(info: DatacenterBrokerEventInfo): Unit =

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