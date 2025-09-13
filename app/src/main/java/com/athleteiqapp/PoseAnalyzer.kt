package com.athleteiqapp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.PointF
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeWithCropOrPadOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer

class PoseAnalyzer(
    context: Context,
    private val listener: (List<KeyPoint>) -> Unit
) : ImageAnalysis.Analyzer {

    private val interpreter: Interpreter
    private val imageProcessor: ImageProcessor

    companion object {
        private const val MODEL_INPUT_SIZE = 192
    }

    init {
        // Load the TFLite model from the assets folder
        val model = FileUtil.loadMappedFile(context, "lightning.tflite")
        // Initialize the TFLite interpreter
        interpreter = Interpreter(model, Interpreter.Options().apply { numThreads = 4 })

        // Initialize an image processor to resize and pad images for the model
        imageProcessor = ImageProcessor.Builder()
            .add(ResizeWithCropOrPadOp(MODEL_INPUT_SIZE, MODEL_INPUT_SIZE))
            .build()
    }

    @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
    override fun analyze(image: ImageProxy) {
        // Convert the camera image to a bitmap
        val bitmap = image.toBitmap()
        if (bitmap == null) {
            image.close()
            return
        }

        // Rotate the bitmap to match the phone's orientation
        val matrix = Matrix().apply { postRotate(image.imageInfo.rotationDegrees.toFloat()) }
        val rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)

        // Prepare the image for the model
        val tensorImage = TensorImage(DataType.UINT8).apply { load(rotatedBitmap) }
        val processedImage = imageProcessor.process(tensorImage)

        // Prepare the output buffer
        val outputBuffer = TensorBuffer.createFixedSize(intArrayOf(1, 1, 17, 3), DataType.FLOAT32)

        // Run the model
        interpreter.run(processedImage.buffer, outputBuffer.buffer.rewind())

        // Parse the model's output into a list of KeyPoint objects
        val keypoints = parseOutput(outputBuffer.floatArray, rotatedBitmap.width, rotatedBitmap.height)

        // Send the results back to the main component
        listener(keypoints)

        image.close()
    }

    // This function converts the raw float array from the model into a structured list of KeyPoints
    private fun parseOutput(array: FloatArray, width: Int, height: Int): List<KeyPoint> {
        return List(17) { i ->
            val y = array[i * 3 + 0] * height
            val x = array[i * 3 + 1] * width
            val score = array[i * 3 + 2]
            KeyPoint(BodyPart.values()[i], PointF(x, y), score)
        }
    }
}

