package stickysampling

import frequencycount.Item
import frequencycount.stickysampling.StickySamplingModel
import org.scalatestplus.mockito.MockitoSugar
import unitspec.UnitSpec
import utils.RandomNumberGenerator
import utils.Utils
import org.mockito.Mockito
import testutils.TestUtils

class StickySamplingModelSpec extends UnitSpec with MockitoSugar {

  "Sticky sampling model" should "increment all frequencies that have not been seen by 1" in {

    val frequency = 0.5
    val error = 0.1 * frequency
    val probabilityOfFailure = 0.1 * error

    val model = new StickySamplingModel[String](frequency, error, probabilityOfFailure)
    //t = 119
    val t = (1.0 / error) * Math.log(1.0 / (frequency * probabilityOfFailure))

    val incomingStream = List.concat(
      Utils.create(19, Item.Red),
      Utils.create(11, Item.Blue),
      Utils.create(10, Item.Yellow),
      Utils.create(10, Item.Brown),
      Utils.create(0, Item.Green)
    )

    val step0 = model.process(incomingStream.iterator)


    assert(step0.getMap(Item.Red.toString) === 19)
    assert(step0.getMap(Item.Blue.toString) === 11)
    assert(step0.getMap(Item.Yellow.toString) === 10)
    assert(step0.getMap(Item.Brown.toString) === 10)
    assert(step0.getMap.get(Item.Green.toString) === None)
  }

  it should "give correct output for elements with frequency below, in false-positive range and above the set frequency" in {
    val frequency = 0.2
    val error = 0.1 * frequency
    val probabilityOfFailure = 0.1 * error

    //red freq = 47 / 100 Pass
    //blue freq = 19 / 100 Pass
    //yellow freq = 18 / 100 Pass
    //brown freq = 8/100 Fail
    //green freq = 0/100 Fail
    val stream = List.concat(
      Utils.create(47, Item.Red),
      Utils.create(19, Item.Blue),
      Utils.create(18, Item.Yellow),
      Utils.create(8, Item.Brown),
      Utils.create(0, Item.Green)
    )

    val model = new StickySamplingModel[String](frequency, error, probabilityOfFailure)
    model.process(stream.iterator)

    val output = model.computeOutput()

    assert(output.length === 3)
    TestUtils.assertColourAndCount(output(0), Item.Red.toString, 47)
    TestUtils.assertColourAndCount(output(1), Item.Blue.toString, 19)
    TestUtils.assertColourAndCount(output(2), Item.Yellow.toString, 18)
  }

  it should "sample items that do not exist" in {
    val frequency = 0.5
    val error = 0.1 * frequency
    val probabilityOfFailure = 0.1 * error

    val mockRng = mock[RandomNumberGenerator]
    val model = new StickySamplingModel[String](frequency, error, probabilityOfFailure).withRng(mockRng)

    val t = (1.0 / error) * Math.log(1.0 / (frequency * probabilityOfFailure))
    println(s"The first $t elements will be sampled with rate 1, the next ${2*t} with probability 0.5 etc")
    //insert the first 120 items. The first 119 should be always selected (probability 1). The next 238 should be selected if unknown with prob 0.5
    val stream = List.concat(
      Utils.create(50, Item.Red),
      Utils.create(50, Item.Blue),
      Utils.create(10, Item.Yellow),
      Utils.create(8, Item.Brown)
    )
    Mockito.when(mockRng.getNextDouble).thenReturn(0.3)

    val step1Model = model.process(stream.iterator)
    assert(step1Model.getMap.isEmpty === false)

    //insert a different item and don't pick it
    //this should also trigger a rate change and toin coss for each entry
    Mockito.when(mockRng.getNextDouble).thenReturn(0.7)
    val unknownItemStream = Utils.create(2, Item.Green)
    val unknownItemModel = step1Model.process(unknownItemStream.iterator)

    assert(unknownItemModel.getMap.get(Item.Green.toString) === None)

    //as a result all frequencies should be empty now that the rate changed and that the toin coss is always negative

    val step2Output = unknownItemModel.computeOutput()
    assert(step2Output.isEmpty === true)
    assert(unknownItemModel.getMap.isEmpty === true)
  }


}
