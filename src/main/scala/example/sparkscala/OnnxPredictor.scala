package example.sparkscala

import ai.onnxruntime._
import java.nio.FloatBuffer

object OnnxPredictor {

  @transient private var env: OrtEnvironment = _
  @transient private var session: OrtSession = _

  def initSession(modelBytes: Array[Byte]): Unit = {
    if (env == null) {
      env = OrtEnvironment.getEnvironment()
    }
    if (session == null) {
      session = env.createSession(modelBytes, new OrtSession.SessionOptions())
    }
  }

  def predict(features: Array[Float], height: Int, width: Int, channels: Int, classes: Array[String]): Prediction = {
    if (session == null) {
      throw new IllegalStateException("OnnxPredictor.initSession doit être appelé avant predict()")
    }

    val inputName = session.getInputNames.iterator().next()
    val shape = Array[Long](1, height, width, channels)

    var tensor: OnnxTensor = null
    var result: OrtSession.Result = null
    try {
      tensor = OnnxTensor.createTensor(env, FloatBuffer.wrap(features), shape)
      val inputs = new java.util.HashMap[String, OnnxTensor]()
      inputs.put(inputName, tensor)

      result = session.run(inputs)

      val rawOutput = result.get(0).getValue
      val scores: Array[Float] = rawOutput match {
        case arr: Array[Array[Float]] => arr(0)
        case arr: Array[Float]        => arr
        case other =>
          throw new IllegalStateException(s"Format de sortie ONNX inattendu: ${other.getClass}")
      }

      val bestIdx = scores.zipWithIndex.maxBy(_._1)._2
      val label = if (bestIdx < classes.length) classes(bestIdx) else s"class_$bestIdx"
      Prediction(label, scores(bestIdx))
    } finally {
      if (tensor != null) tensor.close()
      if (result != null) result.close()
    }
  }
}
