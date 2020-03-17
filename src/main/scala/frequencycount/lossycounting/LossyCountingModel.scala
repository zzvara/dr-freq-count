package frequencycount.lossycounting

import frequencycount.{Item, FrequencyCount}
import utils.Utils

import scala.collection.mutable

class LossyCountingModel[T](val frequency: Double,
                            val error: Double) extends FrequencyCount[T] {

  private var totalProcessedElements = 0L

  private val map = mutable.HashMap.empty[T, Int]


  def process(dataWindow: Iterator[T]): LossyCountingModel[T] = {
    dataWindow.foreach { item =>
      totalProcessedElements += 1
      incrCount(item)
    }
    decreaseAllFrequencies()
    this
  }

  def computeOutput(): Array[(T, Int)] = {
    map.filter { itemWithFreq =>
      itemWithFreq._2.toDouble >= (frequency * totalProcessedElements - error * totalProcessedElements)
    }.toArray.sortWith((pair1, pair2) => pair1._2 > pair2._2)
  }

  def incrCount(item: T): Unit = {
    map.get(item) match {
      case Some(value) =>
        map.put(item, value + 1)
      case None =>
        map.put(item, 1)
    }
  }


  def decreaseAllFrequencies(): Unit = {
    map.foreach { itemFrequency =>
      if (itemShouldBeRemoved(itemFrequency)) {
        map.remove(itemFrequency._1)
      } else {
        map.put(itemFrequency._1, itemFrequency._2 - 1)
      }
    }
  }

  def itemShouldBeRemoved(itemFrequency: (T, Int)): Boolean = {
    itemFrequency._2 == 1
  }



}

object LossyCountingModel {

  def main(args: Array[String]): Unit = {
    val frequency = 0.2
    val error = 0.1 * frequency

    val itemBatches = List(
      List.concat(Utils.create(19, Item.Red), Utils.create(11, Item.Blue), Utils.create(10, Item.Yellow), Utils.create(10, Item.Brown), Utils.create(0, Item.Green)),
      List.concat(Utils.create(30, Item.Red), Utils.create(10, Item.Blue), Utils.create(10, Item.Yellow)),
      List.concat(Utils.create(30, Item.Red), Utils.create(10, Item.Blue), Utils.create(0, Item.Yellow), Utils.create(5, Item.Brown), Utils.create(5, Item.Green)),
      List.concat(Utils.create(40, Item.Red), Utils.create(10, Item.Blue)),
      List.concat(Utils.create(40, Item.Red), Utils.create(10, Item.Blue))
    )

    val model = new LossyCountingModel[String](frequency, error)
    println(s"Frequency: $frequency, Error: $error Window count: ${1.0/error}")
    for (i <- itemBatches.indices) {
      model.process(itemBatches(i).iterator)
      model.computeOutput().foreach(pair => println(pair))
      println("=============")
      Thread.sleep(1000L)
    }
  }

}
