import java.io.File
import java.security.MessageDigest
import java.math.BigInteger
import kotlin.system.exitProcess
import org.knowm.xchart.SwingWrapper
import org.knowm.xchart.XYChartBuilder
import org.knowm.xchart.XYSeries
import org.knowm.xchart.style.Styler

fun main(args: Array<String>) {
    val arguments = parseArguments(args)

    when (arguments[MODE]) {
        "1" -> firstTask(arguments)
        "2" -> secondTask(arguments)
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

fun sha1(string: String): ByteArray {
    val md = MessageDigest.getInstance("SHA1")
    return md.digest(string.toByteArray())
}

fun secondTask(arguments: Map<String, String>) {
    if (arguments[STEP] == null) {
        println("Передайте номер шага")
        exitProcess(1)
    }

    when (arguments[STEP]) {
        "1" -> generateCurve()
        "2" -> generateKeys()
        "3" -> readMessage()
        "4" -> generateHashAndK()
        "5" -> generateHashFromMessage()
        "6" -> generateS()
        "7" -> checkRStreak()
        "8" -> checkSigns()
    }
}

fun generateCurve() {
    val ellipticCurve = EllipticCurve(BYTE_LENGTH)
    writeEllipticCurve(ellipticCurve, "$SECOND_TASK_OUTPUT/$ELLIPTIC_CURVE_PATH")
}

fun generateKeys() {
    val ellipticCurve = readEllipticCurve("$SECOND_TASK_OUTPUT/$ELLIPTIC_CURVE_PATH")
    if (ellipticCurve == null) {
        println("Эллиптическая кривая некорректна")
        exitProcess(1)
    }

    val schnorr = Schnorr(ellipticCurve = ellipticCurve)
    val (l, p) = schnorr.createKeys()
    File("$SECOND_TASK_OUTPUT/$SECRET_KEY_PATH").writeText("$l")
    File("$SECOND_TASK_OUTPUT/$PUBLIC_KEY_PATH").writeText("$p")
    println("Закрытый и открытый ключи записаны")
}

fun readMessage() {
    println("Введите сообщение:")
    val message = readLine() ?: "Текст пример 2021"
    File("$SECOND_TASK_OUTPUT/$MESSAGE_PATH").writeText(message)
    println("Сообщение записано в файл")
}

fun checkEllipticCurveAndMessage (): Pair<Schnorr, String> {
    val ellipticCurve = readEllipticCurve("$SECOND_TASK_OUTPUT/$ELLIPTIC_CURVE_PATH")
    if (ellipticCurve == null) {
        println("Эллиптическая кривая некорректна")
        exitProcess(1)
    }
    val schnorr = Schnorr(ellipticCurve = ellipticCurve)

    val messageFile = File("$SECOND_TASK_OUTPUT/$MESSAGE_PATH")
    if (!messageFile.exists() || messageFile.readText().isEmpty()) {
        println("Сообщение некоррекно")
        exitProcess(1)
    }

    return schnorr to messageFile.readText()
}

fun generateHashAndK() {
    val (schnorr, message) = checkEllipticCurveAndMessage()

    val (k, e) = schnorr.createK(message)
    File("$SECOND_TASK_OUTPUT/$HASH_PATH").writeText("$e")
    File("$SECOND_TASK_OUTPUT/$K_PATH").writeText("$k")
    println("Случайное число k записано в файл")
}

fun generateHashFromMessage() {
    val (schnorr, message) = checkEllipticCurveAndMessage()

    val k = try {
        File("$SECOND_TASK_OUTPUT/$K_PATH").readText().toBigInteger()
    } catch (e: Exception) {
        println("Параметр k некорректен")
        exitProcess(1)
    }

    val e = schnorr.createHash(message, k)
    File("$SECOND_TASK_OUTPUT/$HASH_PATH").writeText("$e")
    println("Хэш сообщения записан в файл")
}

fun generateS() {
    val e = try {
        File("$SECOND_TASK_OUTPUT/$HASH_PATH").readText().toBigInteger()
    } catch (e: Exception) {
        println("Хэш сообщения некорректен")
        exitProcess(1)
    }

    val k = try {
        File("$SECOND_TASK_OUTPUT/$K_PATH").readText().toBigInteger()
    } catch (e: Exception) {
        println("Параметр k некорректен")
        exitProcess(1)
    }

    val l = try {
        File("$SECOND_TASK_OUTPUT/$SECRET_KEY_PATH").readText().toBigInteger()
    } catch (e: Exception) {
        println("Параметр l некорректен")
        exitProcess(1)
    }

    val ellipticCurve = readEllipticCurve("$SECOND_TASK_OUTPUT/$ELLIPTIC_CURVE_PATH")
    if (ellipticCurve == null) {
        println("Эллиптическая кривая некорректна")
        exitProcess(1)
    }
    val schnorr = Schnorr(ellipticCurve = ellipticCurve)

    val s = schnorr.createS(l, e, k)
    File("$SECOND_TASK_OUTPUT/$S_PATH").writeText("$s")
    println("Параметр S был записан в файл")
}

fun checkRStreak() {
    val s = try {
        File("$SECOND_TASK_OUTPUT/$S_PATH").readText().toBigInteger()
    } catch (e: Exception) {
        println("Параметр s некорректен")
        exitProcess(1)
    }

    val ellipticCurve = readEllipticCurve("$SECOND_TASK_OUTPUT/$ELLIPTIC_CURVE_PATH")
    if (ellipticCurve == null) {
        println("Эллиптическая кривая некорректна")
        exitProcess(1)
    }
    val schnorr = Schnorr(ellipticCurve = ellipticCurve)

    val e = try {
        File("$SECOND_TASK_OUTPUT/$HASH_PATH").readText().toBigInteger()
    } catch (e: Exception) {
        println("Хэш сообщения некорректен")
        exitProcess(1)
    }

    val p = try {
        val (x, y) = File("$SECOND_TASK_OUTPUT/$PUBLIC_KEY_PATH")
            .readText().split(", ").map { it.toBigInteger() }
        if (!pointBelongsToCurve(Point(x, y), ellipticCurve.p, ellipticCurve.coefficientA))
            throw Exception()
        Point(x, y)
    } catch (e: Exception) {
        println("Точка P некорректна")
        exitProcess(1)
    }

    val rStreak = schnorr.calculateRStreak(s, e, p)
    File("$SECOND_TASK_OUTPUT/$R_STREAK_PATH").writeText("$rStreak")
    println("Параметр R' был записан в файл")
}

fun checkSigns() {
    val messageFile = File("$SECOND_TASK_OUTPUT/$MESSAGE_PATH")
    if (!messageFile.exists() || messageFile.readText().isEmpty()) {
        println("Сообщение некоррекно")
        exitProcess(1)
    }
    val message = messageFile.readText()

    val ellipticCurve = readEllipticCurve("$SECOND_TASK_OUTPUT/$ELLIPTIC_CURVE_PATH")
    if (ellipticCurve == null) {
        println("Эллиптическая кривая некорректна")
        exitProcess(1)
    }

    val rStreak = try {
        val (x, y) = File("$SECOND_TASK_OUTPUT/$R_STREAK_PATH")
            .readText().split(", ").map { it.toBigInteger() }
        if (!pointBelongsToCurve(Point(x, y), ellipticCurve.p, ellipticCurve.coefficientA))
            throw Exception()
        Point(x, y)
    } catch (e: Exception) {
        println("Точка-параметр R' некорректен")
        exitProcess(1)
    }

    val eStreak = BigInteger(sha1(message + rStreak)).mod(ellipticCurve.r)

    val e = try {
        File("$SECOND_TASK_OUTPUT/$HASH_PATH").readText().toBigInteger()
    } catch (e: Exception) {
        println("Хэш сообщения некорректен")
        exitProcess(1)
    }

    if (e == eStreak) {
        println("e == eStreak")
    } else {
        println("e != eStreak")
    }
}