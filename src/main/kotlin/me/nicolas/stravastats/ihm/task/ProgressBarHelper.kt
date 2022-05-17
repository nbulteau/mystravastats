package me.nicolas.stravastats.ihm.task

class ProgressBarHelper {

    companion object {

        fun displayProgressBar(progressPercentage: Double) {
            val width = 100 // progress bar width in chars
            print("\r[")
            var i = 0
            while (i <= (progressPercentage * width).toInt()) {
                print(".")
                i++
            }
            while (i < width) {
                print(" ")
                i++
            }
            print("]")
        }
    }
}