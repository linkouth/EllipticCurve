import java.io.File
import kotlin.system.exitProcess
import org.knowm.xchart.SwingWrapper
import org.knowm.xchart.XYChartBuilder
import org.knowm.xchart.XYSeries
import org.knowm.xchart.style.Styler

const val FIRST_TASK_OUTPUT = "firstTask"

const val MODE = "-mode"

const val BIT_LENGTH = "-l"
const val SECURITY_ROUNDS = "-m"
const val DRAW_CHART = "-chart"

fun main(args: Array<String>) {
    val arguments = parseArguments(args)

    when (arguments[MODE]) {
        "1" -> firstTask(arguments)
    }
}

fun parseArguments(args: Array<String>): Map<String, String> {
    return args.fold(Pair(emptyMap<String, List<String>>(), "")) { (map, lastKey), elem ->
        if (elem.startsWith("-"))  Pair(map + (elem to emptyList()), elem)
        else Pair(map + (lastKey to map.getOrDefault(lastKey, emptyList()) + elem), lastKey)
    }.first.mapValues { it.value.first() }
}

fun firstTask(arguments: Map<String, String>) {
    val l: Int = arguments[BIT_LENGTH]?.toInt() ?: 16
    if (l < 4) {
        println("Ошибка: l < 4")
        exitProcess(1)
    }
    val m = arguments[SECURITY_ROUNDS]?.toBigInteger() ?: (5).toBigInteger()
    val ellipticCurve = EllipticCurve(l, m)

    if (arguments[DRAW_CHART] == null || arguments[DRAW_CHART] == "true") {
        drawChart(ellipticCurve.points)
    }
    writeCurveParams(ellipticCurve)
    writePoints(ellipticCurve.points)
}

fun drawChart(points: List<Point?>) {
    val chart =
        XYChartBuilder()
            .width(1000)
            .height(1000)
            .title("Gaussian Blobs")
            .xAxisTitle("X")
            .yAxisTitle("Y")
            .build()

    chart.styler.defaultSeriesRenderStyle = XYSeries.XYSeriesRenderStyle.Scatter
    chart.styler.isChartTitleVisible = false
    chart.styler.legendPosition = Styler.LegendPosition.InsideSW
    chart.styler.markerSize = 4

    chart.addSeries(
        "Elliptic curve",
        points.map { it?.x?.toLong() },
        points.map { it?.y?.toLong() }
    )

    SwingWrapper(chart).displayChart()
}

fun writePoints(points: List<Point?>, filePath: String="$FIRST_TASK_OUTPUT/points.txt") {
    val directory = File(FIRST_TASK_OUTPUT)
    if (directory.exists().not()) {
        directory.mkdir()
    }

    File(filePath).printWriter().use { outStream ->
        points.forEach { point ->
            if (point == null) {
                outStream.println("inf, inf")
            } else {
                outStream.println("$point")
            }
        }
    }
}

fun writeCurveParams(ellipticCurve: EllipticCurve, filePath: String="$FIRST_TASK_OUTPUT/curve.txt") {
    val directory = File(FIRST_TASK_OUTPUT)
    if (directory.exists().not()) {
        directory.mkdir()
    }

    File(filePath).printWriter().use { outStream ->
        outStream.println("p: ${ellipticCurve.p}")
        outStream.println("A: ${ellipticCurve.coefficientA}")
        outStream.println("Q: ${ellipticCurve.q}")
        outStream.println("r: ${ellipticCurve.r}")
    }
}