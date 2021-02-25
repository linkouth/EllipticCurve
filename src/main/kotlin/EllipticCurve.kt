import java.math.BigInteger

data class Point(val x: BigInteger, val y: BigInteger) {
    override fun toString(): String {
        return "$x, $y"
    }
}

class EllipticCurve(l: Int, m: BigInteger) {
    val p: BigInteger
    private val n: BigInteger
    val r: BigInteger
    private var initPoint: Point
    var coefficientA: BigInteger
    val q: Point?
    val points: List<Point?>

    init {
        while (true) {
            val p = generateRandom(l)
            println("p: $p")

            // Шаг 2
            val a: BigInteger
            val b: BigInteger
            try {
                val (first, second) = sq2(p) ?: null to null
                if (first == null || second == null) {
                    continue
                }
                a = first
                b = second
                println("a: $a, b: $b")
            } catch (e: java.lang.ArithmeticException) {
                continue
            }

            // Шаг 3
            val (n, r) = getNAndR(p, a, b) ?: null to null
            if (n == null || r == null) {
                continue
            }
            println("n: $n, r: $r")

            // Шаг 4
            if (!isSecure(p, r, m)) continue

            try {
                val (initPoint, coefficientA) = generatePoint(p, n, n / r)
                this.initPoint = initPoint
                this.coefficientA = coefficientA
            } catch (e: java.lang.ArithmeticException) {
                continue
            }
            this.p = p
            this.n = n
            this.r = r

            println("initPoint: $initPoint, coefficientA: $coefficientA")

            val q = sumPointNTimes(initPoint, n / r, p, coefficientA)
            this.q = q

            println("Q: $q")

            val points = getCurvePoints(this.q, this.p, this.coefficientA)
            println("points number: ${points.size}")

            this.points = points

            break
        }
    }
}
