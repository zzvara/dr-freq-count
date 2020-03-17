package frequencycount

trait FrequencyCount[T] {
  def process(dataWindow: Iterator[T]): FrequencyCount[T]
  def computeOutput(): Array[(T, Int)]
}
