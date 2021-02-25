import java.lang.Exception
import java.math.BigInteger
import java.security.SecureRandom
import kotlin.math.sqrt
import kotlin.random.Random
import kotlin.random.nextLong

const val CERTAINTY = 100
const val THRESHOLD = 12
val SECURE_RND = SecureRandom()

/**
 * Шаг 1
 *
 * Генерация простого числа p = 1 mod 4 длины l бит
 *
 * @param bitCount a prime number bit length
 * @return a prime number satisfying the conditions
 */
fun generateRandom(bitCount: Int): BigInteger {
    val start = BigInteger.TWO.pow(bitCount - 1).toLong()
    val end = BigInteger.TWO.pow(bitCount).toLong()
    var random = Random.nextLong(start..end)
    while (!solovayStrassenTest(random) && random % 4 != 1L) {
        random = Random.nextLong(start..end)
    }
    return random.toBigInteger()
}

/**
 * Проверка на простоту методом Соловея-Штрассена
 *
 * @param number a number to check
 * @param rounds a number of rounds
 * @return is the number prime or not
 */
fun solovayStrassenTest(number: Long, rounds: Int=CERTAINTY): Boolean {
    if (number % 2 == 0L || number % 3 == 0L || number % 5 == 0L) {
        return false
    }
    if (number < 100) {
        for (i in 4..sqrt(number.toDouble()).toInt()) {
            if (number % i == 0L) {
                return false
            }
        }
        return true
    }
    for (iter in 1..rounds) {
        val a = Random.nextLong(2 until number)
        val x = legendre(a, number)
        val y = a.toBigInteger().modPow(((number - 1) / 2).toBigInteger(), number.toBigInteger()).toLong()
        if (x == 0L || y != x % number) {
            return false
        }
    }
    return true
}

/**
 * Нахождение символа Лежандра
 *
 * @param a an integer number
 * @param p a prime number
 * @return a legendre symbol
 */
fun legendre(a: Long, p: Long): Long {
    assert(p >= 2)
    if (a == 0L || a == 1L) {
        return a
    }
    var r: Long
    if (a % 2L == 0L) {
        r = legendre(a / 2L, p)
        if (p * p - 1L and 8L != 0L) {
            r *= -1
        }
    } else {
        r = legendre(p % a, a)
        if ((a - 1) * (p - 1) and 4L != 0L) {
            r *= -1
        }
    }
    return r
}

/**
 * Алгритм 7.8.1
 * Часть шага 2
 *
 * Разложение простого числа на сумму квадратов: p = a * a + d * b * b
 *
 * @param prime the prime number
 * @param d the ratio
 * @return a pair of a and b
 */
fun sq2(prime: BigInteger, d: BigInteger=BigInteger.ONE): Pair<BigInteger, BigInteger>? {
    if (prime.bitLength() < THRESHOLD) {
        return trialSq2(prime)
    }

    if (legendre(-1, prime.toLong()) == -1L) {
        throw Exception("Символ Лежандра (-1/$prime) равен -1")
    }

    val sqrt = fieldSqrt(-BigInteger.ONE, prime) ?: return null
    val uList = mutableListOf(sqrt)
    val mList = mutableListOf(prime)
    var i = 0

    while (true) {
        val uCur = uList[i]
        val mCur = mList[i]
        val mNext = (uCur * uCur + d) / mCur

        val first = uCur % mNext
        val second = (mNext - uCur) % mNext
        val uNext = first.min(second)

        mList.add(mNext)
        uList.add(uNext)

        if (mList[i + 1] == BigInteger.ONE) {
            break
        }
        i++
    }

    val aList = MutableList(i + 1) { it.toBigInteger() }
    aList[i] = uList[i]
    val bList = MutableList(i + 1) { it.toBigInteger() }
    bList[i] = BigInteger.ONE

    while (i > 0) {
        val denominator = aList[i] * aList[i] + bList[i] * bList[i]

        val numeratorAWithPlus = uList[i - 1] * aList[i] + d * bList[i]
        val numeratorAWithMinus = -uList[i - 1] * aList[i] + d * bList[i]
        if (numeratorAWithPlus % denominator == BigInteger.ZERO) {
            aList[i - 1] = numeratorAWithPlus / denominator
        } else {
            aList[i - 1] = numeratorAWithMinus / denominator
        }

        val numeratorBWithPlus = -aList[i] + uList[i - 1] * bList[i]
        val numeratorBWithMinus = -aList[i] - uList[i - 1] * bList[i]
        if (numeratorBWithPlus % denominator == BigInteger.ZERO) {
            bList[i - 1] = numeratorBWithPlus / denominator
        } else {
            bList[i - 1] = numeratorBWithMinus / denominator
        }

        i--
    }

    return Pair(aList[i], bList[i])
}

/**
 * Алгоритм 7.5.2
 * Часть шага 2
 *
 * Извлечение квадратного корня в поле
 *
 * @param a the field element
 * @param prime the field characteristic
 * @return a field square root
 */
fun fieldSqrt(a: BigInteger, prime: BigInteger): BigInteger? {
    if (prime % (8).toBigInteger() == (5).toBigInteger()) {
        val b = a.modPow((prime + 3.toBigInteger()) / 8.toBigInteger(), prime)
        val c = a.modPow((prime - BigInteger.ONE) / 4.toBigInteger(), prime)
        return when {
            c.abs() != BigInteger.ONE -> null
            c == BigInteger.ONE -> b
            else -> {
                val i = 2.toBigInteger().modPow((prime - BigInteger.ONE) / 4.toBigInteger(), prime)
                (b * i) % prime
            }
        }
    }

    if (prime % (16).toBigInteger() == (9).toBigInteger()) {
        val b = a.modPow((prime + (7).toBigInteger()) / (16).toBigInteger(), prime)
        val c = a.modPow((prime - BigInteger.ONE) / (8).toBigInteger(), prime)
        if (c == prime - BigInteger.ONE) {
            val j = BigInteger.TWO.modPow((prime - BigInteger.ONE) / (8).toBigInteger(), prime)
            return (b * j) % prime
        }
        return b
    }

    return null
}

/**
 * Часть шага 2
 *
 * Тривиальное нахождение разложения p = a * a + factor * b * b
 *
 * @param p the prime number
 * @param factor the ratio
 * @return a pair of a and b
 */
fun trialSq2(p: BigInteger, factor: BigInteger= BigInteger.ONE): Pair<BigInteger, BigInteger>? {
    var a = BigInteger.ONE
    while (a < p) {
        var b = BigInteger.ONE
        while (b < p) {
            if (a * a + factor * b * b == p) {
                return a to b
            }
            b++
        }
        a++
    }
    return null
}

/**
 * Шаг 3
 *
 * Нахождение N и r по параметрам a и b, где a * a + b * b = p
 *
 * @param p the prime number
 * @param a the first factor
 * @param b the second factor
 * @return a pair of N and r
 */
fun getNAndR(p: BigInteger, a: BigInteger, b: BigInteger): Pair<BigInteger, BigInteger>? {
    val possibleT = listOf(
        -BigInteger.TWO * a, BigInteger.TWO * a,
        -BigInteger.TWO * b, BigInteger.TWO * b
    )

    possibleT.forEach { t ->
        val n = p + BigInteger.ONE + t
        if (n % BigInteger.TWO == BigInteger.ZERO &&
            (n / BigInteger.TWO).isProbablePrime(CERTAINTY)) {
                return n to (n / BigInteger.TWO)
        } else if (n % (4).toBigInteger() == BigInteger.ZERO &&
            (n / (4).toBigInteger()).isProbablePrime(CERTAINTY)) {
            return n to (n / (4).toBigInteger())
        }
    }

    return null
}

/**
 * Шаг 3
 *
 * Проверка на безопасность подобранных параметров
 *
 * @param p a prime number
 * @param r a prime number less than p
 * @param m a parameter of security
 * @return is p, r secure
 */
fun isSecure(p: BigInteger, r: BigInteger, m: BigInteger): Boolean {
    if (p == r) return false
    for (i in 1..m.toLong()) {
        if (p.modPow(i.toBigInteger(), r) == BigInteger.ONE) return false
    }
    return true
}

/**
 * Шаг 5
 *
 * Генерация начальной точки согласно условиям
 *
 * @param p a prime number
 * @param n a parameter N
 * @param ratio a ration of N and r
 * @return an initial point and coefficient A
 */
fun generatePoint(p: BigInteger, n: BigInteger, ratio: BigInteger): Pair<Point, BigInteger> {
    while (true) {
        val x = BigInteger(p.bitLength(), SECURE_RND).mod(p)
        val y = BigInteger(p.bitLength(), SECURE_RND).mod(p)
        if (x == BigInteger.ZERO || y == BigInteger.ZERO) {
            continue
        }

        val a = ((y * y - x * x * x) * x.modInverse(p)).mod(p)

        if (ratio == BigInteger.TWO && isQuadraticResidue(-a, p)) continue
        else if (ratio == (4).toBigInteger() && !isQuadraticResidue(-a, p)) continue

        val point = Point(x, y)

        if (checkInfinitePoint(point, n, p, a)) return point to a
    }
}

/**
 * Проверка на квадратичный вычет
 *
 * @param a an integer number
 * @param p a prime number
 * @return is a quadratic residue modulo p
 */
fun isQuadraticResidue(a: BigInteger, p: BigInteger): Boolean {
    if (a.mod(p).modPow((p - BigInteger.ONE) / BigInteger.TWO, p) == BigInteger.ONE)
        return true
    return false
}

/**
 * Шаг 6
 * Проверка сгенерированной точки
 *
 * @param point a point to check
 * @param n a parameter N
 * @param p a modulo
 * @param coefficientA the A in equation y * y = x * x * x + a * x + b
 * @return is point matched or not
 */
fun checkInfinitePoint(
    point: Point,
    n: BigInteger,
    p: BigInteger,
    coefficientA: BigInteger
): Boolean {
    if (sumPointNTimes(point, n, p, coefficientA) == null) {
        return true
    }
    return false
}

/**
 * Сложение точки times раз
 *
 * @param a a point to add
 * @param times a number of addition
 * @param p a modulo
 * @param coefficientA the A in equation y * y = x * x * x + a * x + b
 * @return the result of times addition
 */
fun sumPointNTimes(
    a: Point,
    times: BigInteger,
    p: BigInteger,
    coefficientA: BigInteger
): Point? {
    var tempPoint: Point? = a; var ans: Point? = null
    val timesBits = mutableListOf<Boolean>()

    var n = times
    while (n > BigInteger.ZERO) {
        timesBits.add(n % 2.toBigInteger() == BigInteger.ONE)
        n /= 2.toBigInteger()
    }

    if (timesBits[0]) {
        ans = a
    }
    timesBits.removeAt(0)

    for (bit in timesBits) {
        tempPoint = sumPoints(tempPoint, tempPoint, p, coefficientA)
        if (bit) {
            ans = sumPoints(ans, tempPoint, p, coefficientA)
        }
    }

    return ans
}

/**
 * Сложение точек кривой
 *
 * @param a the first point
 * @param b the second point
 * @param p the modulo
 * @param coefficientA the A in equation y * y = x * x * x + a * x + b
 * @return the result of addition
 */
fun sumPoints(
    a: Point?,
    b: Point?,
    p: BigInteger,
    coefficientA: BigInteger
): Point? {
    if (a == null) return b
    if (b == null) return a

    if ((a == b && a.y == BigInteger.ZERO) || (a != b && a.x == b.x)) {
        return null
    }

    val lambda = if (a == b) {
        (((3).toBigInteger() * a.x * a.x + coefficientA) * (BigInteger.TWO * a.y).modInverse(p)).mod(p)
    } else {
        ((b.y - a.y).mod(p) * (b.x - a.x).mod(p).modInverse(p)).mod(p)
    }

    val x = (lambda * lambda - b.x - a.x).mod(p)
    val y = (lambda * (a.x - x) - a.y).mod(p)
    return Point(x, y)
}

fun getCurvePoints(q: Point?, p: BigInteger, coefficientA: BigInteger): List<Point?> {
    var currentPoint = sumPoints(q, q, p, coefficientA)
    val pointsList = mutableListOf(q, currentPoint)

    while (currentPoint != null) {
        currentPoint = sumPoints(currentPoint, q, p, coefficientA)
        pointsList.add(currentPoint)
    }

    return pointsList.toList()
}