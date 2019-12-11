package org.snakeskin.scope.client

var idx = 0
val arr = DoubleArray(65535) { Double.NaN }

fun pushValue(value: Double) {
    if (idx <= arr.lastIndex) {
        arr[idx] = value
        idx++
    } else {
        //Left shift array
        val start = System.nanoTime()
        System.arraycopy(arr, 1, arr, 0, arr.lastIndex)
        val dt = System.nanoTime() - start
        println("arraycopy took $dt nanos")
        //Place element
        arr[arr.lastIndex] = value
    }
}

fun main() {
    for (i in 0 until 65535) {
        pushValue(i.toDouble())
    }

    pushValue(10.0)

    while (true) {
        pushValue(10.0)
        Thread.sleep(10)
    }
}