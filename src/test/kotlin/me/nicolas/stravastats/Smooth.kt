package me.nicolas.stravastats

internal object Smooth {
    @JvmStatic
    fun main(args: Array<String>) {
        val signal = intArrayOf(1, 4, 4, 4, 5, 5, 5, 8, 8, 8, 7, 7)
        val l = signal.size
        val smooth = IntArray(l)

        // compute the smoothed value for each
        //  cell of the array smooth
        smooth[0] = signal[0]
        smooth[l - 1] = signal[l - 1]
        for (i in 1 until l - 1) {
            smooth[i] = (signal[i - 1] + signal[i] + signal[i + 1]) / 3
        }

        // write out the input
        for (j in 0 until l) {
            print(signal[j].toString() + " ")
        }
        println()

        // write out the result
        for (j in 0 until l) {
            print(smooth[j].toString() + " ")
        }
    }
}