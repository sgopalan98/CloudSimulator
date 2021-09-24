package Simulations

import Simulations.BasicCloudSimPlusExample.config
import DataCenters.{SaaSDataCenter1, SaaSDataCenter2, SaaSDataCenter3}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class SaaSDataCenterTest extends AnyFlatSpec with Matchers {
  behavior of "SaaSDataCenter(1,2,3) configuration files"

  "DataCenter1" should "match DataCenter 2 in all host configration except hosts" in {
    val dataCenter1 = new SaaSDataCenter1
    val dataCenter2 = new SaaSDataCenter2

    dataCenter1.getNoHosts("datacenter1.conf") shouldBe dataCenter2.getNoHosts("datacenter2.conf")
    dataCenter1.getHostMips("datacenter1.conf") shouldBe dataCenter2.getHostMips("datacenter2.conf")
    dataCenter1.getHostBw("datacenter1.conf") shouldBe dataCenter2.getHostBw("datacenter2.conf")
    dataCenter1.getHostStorage("datacenter1.conf") shouldBe dataCenter2.getHostStorage("datacenter2.conf")
  }

  "DataCenter1" should "match DataCenter 2 in all VM configration" in {
    val dataCenter1 = new SaaSDataCenter1
    val dataCenter2 = new SaaSDataCenter2

    dataCenter1.getVmNumbers("datacenter1.conf") shouldBe dataCenter2.getVmNumbers("datacenter2.conf")
    dataCenter1.getVmMips("datacenter1.conf") shouldBe dataCenter2.getVmMips("datacenter2.conf")
    dataCenter1.getVmPEs("datacenter1.conf") shouldBe dataCenter2.getVmPEs("datacenter2.conf")
    dataCenter1.getVmRam("datacenter1.conf") shouldBe dataCenter2.getVmRam("datacenter2.conf")
    dataCenter1.getVmBw("datacenter1.conf") shouldBe dataCenter2.getVmBw("datacenter2.conf")
  }

  "DataCenter2" should "match DataCenter 3 in all host configration except hosts" in {
    val dataCenter1 = new SaaSDataCenter2
    val dataCenter2 = new SaaSDataCenter3

    dataCenter1.getNoHosts("datacenter1.conf") shouldBe dataCenter2.getNoHosts("datacenter2.conf")
    dataCenter1.getHostMips("datacenter1.conf") shouldBe dataCenter2.getHostMips("datacenter2.conf")
    dataCenter1.getHostBw("datacenter1.conf") shouldBe dataCenter2.getHostBw("datacenter2.conf")
    dataCenter1.getHostStorage("datacenter1.conf") shouldBe dataCenter2.getHostStorage("datacenter2.conf")
  }

  "DataCenter2" should "match DataCenter 3 in all VM configration" in {
    val dataCenter1 = new SaaSDataCenter2
    val dataCenter2 = new SaaSDataCenter3

    dataCenter1.getVmNumbers("datacenter1.conf") shouldBe dataCenter2.getVmNumbers("datacenter2.conf")
    dataCenter1.getVmMips("datacenter1.conf") shouldBe dataCenter2.getVmMips("datacenter2.conf")
    dataCenter1.getVmPEs("datacenter1.conf") shouldBe dataCenter2.getVmPEs("datacenter2.conf")
    dataCenter1.getVmRam("datacenter1.conf") shouldBe dataCenter2.getVmRam("datacenter2.conf")
    dataCenter1.getVmBw("datacenter1.conf") shouldBe dataCenter2.getVmBw("datacenter2.conf")
  }

  it should "get the correct number of hosts - common between all three datacenters" in {
    val dataCenter = new SaaSDataCenter1
    dataCenter.getNoHosts("datacenter1.conf") shouldBe 5
  }

  it should "get the correct HostBw - common between all three datacenters" in {
    val dataCenter = new SaaSDataCenter1
    dataCenter.getHostBw("datacenter1.conf") shouldBe 16000
  }

  it should "get the correct Host Storage - common between all three datacenters" in {
    val dataCenter = new SaaSDataCenter1
    dataCenter.getHostStorage("datacenter1.conf") shouldBe 1000000
  }

  it should "get the correct HostMips - common between all three datacenters" in {
    val dataCenter = new SaaSDataCenter1
    dataCenter.getHostMips("datacenter1.conf") shouldBe 20000
  }

  it should "get the correct HostRam - common between all three datacenters" in {
    val dataCenter = new SaaSDataCenter1
    dataCenter.getHostRam("datacenter1.conf") shouldBe 500000
  }
}
