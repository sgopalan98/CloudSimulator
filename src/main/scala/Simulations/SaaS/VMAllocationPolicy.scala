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
import org.cloudbus.cloudsim.resources.{Pe, PeSimple}
import org.cloudbus.cloudsim.schedulers.vm.*
import org.cloudbus.cloudsim.utilizationmodels.{UtilizationModelDynamic, UtilizationModelFull}
import org.cloudbus.cloudsim.vms.{VmCost, VmSimple}
import org.cloudsimplus.builders.tables.CloudletsTableBuilder
import org.cloudbus.cloudsim.schedulers.cloudlet.{CloudletSchedulerCompletelyFair, CloudletSchedulerSpaceShared, CloudletSchedulerTimeShared}

import java.util
import collection.JavaConverters.*

class VMAllocationPolicy

object VMAllocationPolicy:
  val logger = CreateLogger(classOf[VMAllocationPolicy])
  private val SCHEDULE_INTERVAL = 5

  private val HOSTS = 5
  private val VMS = 3

  private val HOST_MIPS = 1000 //for each PE


  private val HOST_INITIAL_PES = 4
  private val HOST_RAM: Long = 500000 //host memory (MB)

  private val HOST_STORAGE: Long = 1000000 //host storage


  private val HOST_BW: Long  = 16000L //Mb/s


  private val VM_MIPS = 1000L
  private val VM_SIZE = 1000 //image size (MB)

  private val VM_RAM = 10000 //VM memory (MB)

  private val VM_BW = HOST_BW / VMS.toLong
  private val VM_PES = 2

  private val CLOUDLET_LENGHT = 2000000L
  private val CLOUDLET_FILESIZE = 300L
  private val CLOUDLET_OUTPUTSIZE = 300L

  def Start(fileNo: Int) =
    val simulation = new CloudSim()
    //creating DataCenter
    val hosts = (1 to HOSTS).map(HOSTS => {
      (1 to HOSTS).map(pes => PeSimple(HOST_MIPS, new PeProvisionerSimple())
      )
    }).map(peList =>
      logger.info(s"Pe LIST -> $peList")
      new HostSimple(HOST_RAM*(51 - peList.length)*10 , HOST_BW, HOST_STORAGE, peList.toList.asJava)
        .setRamProvisioner(new ResourceProvisionerSimple())
        .setBwProvisioner(new ResourceProvisionerSimple())
        .setVmScheduler(new VmSchedulerTimeShared());
    )
//    val allocationPolicy = new VmAllocationPolicyRandom(new UniformDistr())
    val allocationPolicy = new VmAllocationPolicyFirstFit()
    val dataCenter = DatacenterSimple(simulation, hosts.asJava, allocationPolicy).getCharacteristics().setCostPerSecond(0.01)
      .setCostPerMem(0.02)
      .setCostPerStorage(0.001)
      .setCostPerBw(0.005)

    //Creating broker

    val broker = DatacenterBrokerSimple(simulation)

    //Creating VMs
    val vms = (1 to VMS).map(index => VmSimple(VM_MIPS, 4 - index).setRam(VM_RAM).setBw(VM_BW).setCloudletScheduler(new CloudletSchedulerTimeShared)).toList
    broker.submitVmList(vms.asJava)

    //Creating cloudlets

    val cloudlets:List[Cloudlet] = vms.flatMap(vm => {
      val list = CloudletSimple(CLOUDLET_LENGHT, 1)
        .setFileSize(CLOUDLET_FILESIZE)
        .setOutputSize(CLOUDLET_OUTPUTSIZE) :: CloudletSimple(CLOUDLET_LENGHT, 1)
        .setFileSize(CLOUDLET_FILESIZE)
        .setOutputSize(CLOUDLET_OUTPUTSIZE) :: Nil
      list
      //broker.bindCloudletToVm(cloudlet, vm)
    }
    )

    broker.submitCloudletList(cloudlets.asJava)

    //Run simulation
    simulation.start()


    new CloudletsTableBuilder(broker.getCloudletSubmittedList()).build();
    val vmExecList: List[VmSimple] = broker.getVmCreatedList.asScala.toList
    val cost = vmExecList.map(vm => new VmCost(vm).getTotalCost).sum
    logger.info(s"The total cost is ${cost}")






