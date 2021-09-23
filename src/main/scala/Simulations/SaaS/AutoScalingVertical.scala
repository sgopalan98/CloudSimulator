package Simulations.SaaS

import HelperUtils.{CreateLogger, ObtainConfigReference}
import org.cloudbus.cloudsim.allocationpolicies.*
import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple
import org.cloudbus.cloudsim.cloudlets.{Cloudlet, CloudletSimple}
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.datacenters.DatacenterSimple
import org.cloudbus.cloudsim.distributions.{ContinuousDistribution, NormalDistr, UniformDistr}
import org.cloudbus.cloudsim.hosts.HostSimple
import org.cloudbus.cloudsim.hosts.Host
import org.cloudbus.cloudsim.provisioners.{PeProvisionerSimple, ResourceProvisionerSimple}
import org.cloudbus.cloudsim.resources.{Pe, PeSimple, Processor}
import org.cloudbus.cloudsim.schedulers.vm.*
import org.cloudbus.cloudsim.utilizationmodels.{UtilizationModelDynamic, UtilizationModelFull}
import org.cloudbus.cloudsim.vms.{Vm, VmCost, VmSimple}
import org.cloudsimplus.builders.tables.CloudletsTableBuilder
import org.cloudbus.cloudsim.schedulers.cloudlet.{CloudletSchedulerCompletelyFair, CloudletSchedulerSpaceShared, CloudletSchedulerTimeShared}
import org.cloudsimplus.autoscaling.resources.{ResourceScalingGradual, ResourceScalingInstantaneous}
import org.cloudsimplus.autoscaling.{HorizontalVmScalingSimple, VerticalVmScaling, VerticalVmScalingSimple}
import org.cloudsimplus.listeners.{EventInfo, EventListener}

import java.util
import collection.JavaConverters.*

class AutoScalingVertical

object AutoScalingVertical:
  val logger = CreateLogger(classOf[AutoScalingVertical])
  private val SCHEDULE_INTERVAL = 5

  private val HOSTS = 5
  private val VMS = 3

  private val HOST_MIPS = 1000000 //for each PE


  private val HOST_INITIAL_PES = 40
  private val HOST_RAM: Long = 500000 //host memory (MB)

  private val HOST_STORAGE: Long = 1000000 //host storage


  private val HOST_BW: Long  = 16000L //Mb/s


  private val VM_MIPS = 1000L
  private val VM_SIZE = 1000 //image size (MB)

  private val VM_RAM = 10000 //VM memory (MB)

  private val VM_BW = 1000L
  private val VM_PES = 1

  private val CLOUDLET_LENGHT = 2000000L
  private val CLOUDLET_FILESIZE = 300L
  private val CLOUDLET_OUTPUTSIZE = 300L

  def Start(fileNo: Int) =

    val seed = 1

    val simulation = new CloudSim()
    val broker = DatacenterBrokerSimple(simulation)
    simulation.addOnClockTickListener(info =>
      if(info.getTime % 10 == 0 && info.getTime <= 50)
      {
        val cloudletsNumber = 10
        val cloudlets:List[Cloudlet] = (1 to cloudletsNumber).map(index => {
          val cloudlet = CloudletSimple(CLOUDLET_LENGHT, 10)
            .setFileSize(CLOUDLET_FILESIZE)
            .setOutputSize(CLOUDLET_OUTPUTSIZE)
          cloudlet
          //broker.bindCloudletToVm(cloudlet, vm)
        }
        ).toList
        broker.submitCloudletList(cloudlets.asJava)
      })
    //creating DataCenter
    val hosts = (1 to HOSTS).map(HOSTS => {
      (1 to HOST_INITIAL_PES).map(pes => PeSimple(HOST_MIPS, new PeProvisionerSimple())
      )
    }).map(peList =>
      logger.info(s"Pe LIST -> $peList")
      new HostSimple(HOST_RAM , HOST_BW, HOST_STORAGE, peList.toList.asJava)
        .setRamProvisioner(new ResourceProvisionerSimple())
        .setBwProvisioner(new ResourceProvisionerSimple())
        .setVmScheduler(new VmSchedulerTimeShared());
    )
    //    val allocationPolicy = new VmAllocationPolicyRandom(new UniformDistr())
    val allocationPolicy = new VmAllocationPolicySimple()
    val dataCenter = DatacenterSimple(simulation, hosts.asJava, allocationPolicy).getCharacteristics()
      .setCostPerSecond(0.01)
      .setCostPerMem(0.02)
      .setCostPerStorage(0.001)
      .setCostPerBw(0.005)

    //Creating broker



    broker.setVmDestructionDelay(10.0)


    //Creating VMs
    val vms = (1 to VMS).map(index => {
      val verticalVmScalingSimple = new VerticalVmScalingSimple(classOf[Pe], 0.1)
      val ret = 2*verticalVmScalingSimple.getScalingFactor*verticalVmScalingSimple.getAllocatedResource
//      verticalVmScalingSimple.setResourceScaling((vs) => 2*vs.getScalingFactor*vs.getAllocatedResource)
//      verticalVmScalingSimple.setResourceScaling()
      verticalVmScalingSimple.setLowerThresholdFunction((vm:Vm) => 0.1D)
      verticalVmScalingSimple.setUpperThresholdFunction((vm:Vm) => 0.8D)
      val vm = new VmSimple(index, VM_MIPS, 2).setPeVerticalScaling(verticalVmScalingSimple)
      vm.enableUtilizationStats()
      vm
    }).toList
    broker.submitVmList(vms.asJava)

    //Creating cloudlets

    val cloudlets:List[Cloudlet] = vms.flatMap(vm => {
      val list = CloudletSimple(CLOUDLET_LENGHT, 2)
        .setFileSize(CLOUDLET_FILESIZE)
        .setOutputSize(CLOUDLET_OUTPUTSIZE) :: CloudletSimple(CLOUDLET_LENGHT, 10)
        .setFileSize(CLOUDLET_FILESIZE)
        .setOutputSize(CLOUDLET_OUTPUTSIZE) :: Nil
      list
      //broker.bindCloudletToVm(cloudlet, vm)
    }
    )

    broker.submitCloudletList(cloudlets.asJava)

    simulation.addOnClockTickListener(info =>{
      val vmList : List[VmSimple] = broker.getVmCreatedList.asScala.toList
      //vmList.map(vm => logger.info(s"\t\t Request - ${vm.getPeVerticalScaling.requestUpScalingIfPredicateMatches(vm.)}"))
      vmList.map(vm => {
        if(vm.getPeVerticalScaling.isVmOverloaded && vm.getNumberOfPes + 1 < vm.getHost.getFreePesNumber)
          vm.getProcessor.sumCapacity(1)
        if(vm.getPeVerticalScaling.isVmUnderloaded && vm.getNumberOfPes - 1 > 0)
          vm.getProcessor.sumCapacity(-1)
      })
      vmList.map(vm => vm.getHost.getVmScheduler.allocatePesForVm(vm))
      vmList.map(vm => logger.info(s"\t\tTime ${info.getTime}: Vm ${vm.getId} CPU Usage: ${vm.getCpuPercentUtilization()*100.0} (${vm.getNumberOfPes} vCPUs. Running Cloudlets: #${vm.getCloudletScheduler().getCloudletExecList().size()}). RAM usage: %.2f%% (%d MB)%n"))
      vmList.map(vm => logger.info(s"\t\t Is it loaded? - ${vm.getPeVerticalScaling.isVmOverloaded}"))


    })

    //Run simulation
    simulation.start()



    new CloudletsTableBuilder(broker.getCloudletSubmittedList().asScala.toList.sortBy(_.getExecStartTime).asJava).build();
    val vmExecList: List[VmSimple] = broker.getVmCreatedList.asScala.toList
    val cost = vmExecList.map(vm => new VmCost(vm).getTotalCost).sum
    logger.info(s"The total cost is ${cost}")
    logger.info(s"Total number of VMs - vmExecList - ${vmExecList.length}")
    vmExecList.map(vm => logger.info(s"The CPU util is ${vm.getCpuUtilizationStats.getMax}"))
    vmExecList.map(vm => logger.info(s"The No of PEs are ${vm.getNumberOfPes}"))



  def createCloudlets(info: EventInfo, broker : DatacenterBrokerSimple) =
    val time = info.getTime

    if(info.getTime % 10 == 0 && info.getTime <= 50){
      val cloudletsNumber = 10
      val cloudlets:List[Cloudlet] = (1 to 4).map(index => {
        val cloudlet = CloudletSimple(CLOUDLET_LENGHT, 2)
          .setFileSize(CLOUDLET_FILESIZE)
          .setOutputSize(CLOUDLET_OUTPUTSIZE)
        cloudlet
        //broker.bindCloudletToVm(cloudlet, vm)
      }
      ).toList
      broker.submitCloudletList(cloudlets.asJava)
    }

  def createVm() =
    val vm = VmSimple(VM_MIPS, 2).setRam(VM_RAM).setBw(VM_BW).setCloudletScheduler(new CloudletSchedulerTimeShared)
    vm

  def overloadedPredicate(vm: VmSimple) = {
    vm.getCpuPercentUtilization() > 0.7
  }






