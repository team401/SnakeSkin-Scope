package org.snakeskin.scope.client.buffer

/**
 * Buffers timestamps, providing special accessor methods for searching for indices.
 * Timestamps are automatically offset by the first timestamp added
 */
class TimestampBuffer(val size: Int) {
    val arr = DoubleArray(size)
    var offset = 0.0

    /**
     * Adds a timestamp to the buffer
     */
    fun addTimestamp(ptr: Int, timestamp: Double) {
        if (ptr == 0) offset = timestamp //Populate the offset if we are inserting the first timestamp
        arr[ptr] = timestamp - offset //Offset each timestamp upon insertion
    }

    fun reset() {
        for (i in 0 until size) {
            arr[i] = 0.0
        }
        offset = 0.0
    }

    /**
     * Returns the index of the first timestamp *less than* the value of the provided timestamp.
     * If there is no timestamp lower than the value, 0 is returned
     *
     * @param timestamp The timestamp to search for
     * @param startIndex The index to start searching at
     */
    fun searchForStart(timestamp: Double, startIndex: Int): Int {
        for (i in startIndex downTo 0) { //Search in bounds
            //Epsilon less than operation to avoid roundoff errors (1e-6 is well above our specified maximum precision of 1 ms)
            if ((timestamp - arr[i]) > 1e-6) return i
        }
        return 0 //Return 0 if no value was found
    }
}