package example.sparkscala

import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO

case class ImgStats(mean: Double, std: Double, min: Double, max: Double)
case class Prediction(label: String, confidence: Float)

object ImageUtils {

  def decodeAndResize(bytes: Array[Byte], targetW: Int, targetH: Int, channels: Int = 1): Option[Array[Float]] = {
    try {
      val img = ImageIO.read(new ByteArrayInputStream(bytes))
      if (img == null) return None

      val imgType = if (channels == 1) BufferedImage.TYPE_BYTE_GRAY else BufferedImage.TYPE_INT_RGB
      val resized = new BufferedImage(targetW, targetH, imgType)
      val g = resized.createGraphics()
      g.drawImage(img, 0, 0, targetW, targetH, null)
      g.dispose()

      val pixels = new Array[Float](targetH * targetW * channels)
      var idx = 0
      for (y <- 0 until targetH; x <- 0 until targetW) {
        if (channels == 1) {
          val gray = resized.getRaster.getSample(x, y, 0)
          pixels(idx) = gray.toFloat
          idx += 1
        } else {
          val rgb = resized.getRGB(x, y)
          pixels(idx)     = ((rgb >> 16) & 0xFF).toFloat
          pixels(idx + 1) = ((rgb >> 8) & 0xFF).toFloat
          pixels(idx + 2) = (rgb & 0xFF).toFloat
          idx += 3
        }
      }

      Some(pixels)
    } catch {
      case _: Exception => None
    }
  }

  def descriptiveStats(features: Seq[Float]): ImgStats = {
    if (features.isEmpty) return ImgStats(0.0, 0.0, 0.0, 0.0)
    val n = features.length
    val mean = features.sum / n
    val variance = features.map(f => math.pow(f - mean, 2)).sum / n
    val std = math.sqrt(variance)
    ImgStats(mean, std, features.min.toDouble, features.max.toDouble)
  }
}
