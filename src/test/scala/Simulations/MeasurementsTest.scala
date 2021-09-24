package Simulations

import Simulations.BasicCloudSimPlusExample.config
import DataCenters.{SaaSDataCenter1, SaaSDataCenter2, SaaSDataCenter3}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class MeasurementsTest extends AnyFlatSpec with Matchers {
  behavior of "Measurements configuration"

  it should "get the correct costPerSecond for Datacenter1" in {
    val dataCenter = new SaaSDataCenter1
    dataCenter.getCostPerSecond("datacenter1.conf") shouldBe 0.01
  }

  it should "get the correct costPerMem for Datacenter1" in {
    val dataCenter = new SaaSDataCenter1
    dataCenter.getCostPerMem("datacenter1.conf") shouldBe 0.02
  }

  it should "get the correct costPerStorage for Datacenter1" in {
    val dataCenter = new SaaSDataCenter1
    dataCenter.getCostPerStorage("datacenter1.conf") shouldBe 0.001
  }

  it should "get the correct costPerBw for Datacenter1" in {
    val dataCenter = new SaaSDataCenter1
    dataCenter.getCostPerBw("datacenter1.conf") shouldBe 0.005
  }

  it should "get the correct costPerSecond for Datacenter2" in {
    val dataCenter = new SaaSDataCenter2
    dataCenter.getCostPerSecond("datacenter2.conf") shouldBe 0.20
  }

  it should "get the correct costPerMem for Datacenter2" in {
    val dataCenter = new SaaSDataCenter2
    dataCenter.getCostPerMem("datacenter2.conf") shouldBe 0.02
  }

  it should "get the correct costPerStorage for Datacenter2" in {
    val dataCenter = new SaaSDataCenter2
    dataCenter.getCostPerStorage("datacenter2.conf") shouldBe 0.001
  }

  it should "get the correct costPerBw for Datacenter2" in {
    val dataCenter = new SaaSDataCenter2
    dataCenter.getCostPerBw("datacenter2.conf") shouldBe 0.005
  }

  it should "get the correct costPerSecond for Datacenter3" in {
    val dataCenter = new SaaSDataCenter3
    dataCenter.getCostPerSecond("datacenter3.conf") shouldBe 0.30
  }

  it should "get the correct costPerMem for Datacenter3" in {
    val dataCenter = new SaaSDataCenter3
    dataCenter.getCostPerMem("datacenter3.conf") shouldBe 0.02
  }

  it should "get the correct costPerStorage for Datacenter3" in {
    val dataCenter = new SaaSDataCenter3
    dataCenter.getCostPerStorage("datacenter3.conf") shouldBe 0.001
  }

  it should "get the correct costPerBw for Datacenter3" in {
    val dataCenter = new SaaSDataCenter3
    dataCenter.getCostPerBw("datacenter3.conf") shouldBe 0.005
  }
}
