import java.io.File

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

fun writeEllipticCurve(ellipticCurve: EllipticCurve, filePath: String) {
    File(filePath).writeText(
    "${ellipticCurve.p}\n${ellipticCurve.r}\n${ellipticCurve.coefficientA}\n${ellipticCurve.q}"
    )
    println("Эллиптическая кривая записана в файл")
}

fun readEllipticCurve(filePath: String): EllipticCurve? {
    val file = File(filePath)
    if (!file.exists()) return null
    val lines = file.readLines()
    return try {
        val p = lines[0].toBigInteger()
        val r = lines[1].toBigInteger()
        val coefficientA = lines[2].toBigInteger()
        val (x, y) = lines[3].split(", ").map { it.toBigInteger() }
        if (!pointBelongsToCurve(Point(x, y), p, coefficientA)) return null
        EllipticCurve(p = p, r = r, coefficientA = coefficientA, q = Point(x, y))
    } catch (e: Exception) {
        null
    }
}