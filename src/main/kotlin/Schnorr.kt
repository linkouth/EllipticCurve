import java.math.BigInteger

class Schnorr(
    val ellipticCurve: EllipticCurve
) {
    fun createKeys(): Pair<BigInteger, Point> {
        var l = BigInteger(ellipticCurve.r.bitLength(), java.util.Random()).mod(ellipticCurve.r)
        var p = sumPointNTimes(ellipticCurve.q, l, ellipticCurve.p, ellipticCurve.coefficientA)
        while (p == null) {
            l = BigInteger(ellipticCurve.r.bitLength(), java.util.Random()).mod(ellipticCurve.r)
            p = sumPointNTimes(ellipticCurve.q, l, ellipticCurve.p, ellipticCurve.coefficientA)
        }
        return l to p
    }

    fun createK(message: String): Pair<BigInteger, BigInteger> {
        var k = BigInteger(ellipticCurve.r.bitLength(), java.util.Random()).mod(ellipticCurve.r)
        var r = sumPointNTimes(ellipticCurve.q, k, ellipticCurve.p, ellipticCurve.coefficientA)
        var e = BigInteger(sha1(message + r)).mod(ellipticCurve.r)
        while (e == BigInteger.ZERO) {
            k = BigInteger(ellipticCurve.r.bitLength(), java.util.Random()).mod(ellipticCurve.r)
            r = sumPointNTimes(ellipticCurve.q, k, ellipticCurve.p, ellipticCurve.coefficientA)
            e = BigInteger(sha1(message + r)).mod(ellipticCurve.r)
        }

        return k to e
    }

    fun createHash(message: String, k: BigInteger): BigInteger? {
        val r = sumPointNTimes(ellipticCurve.q, k, ellipticCurve.p, ellipticCurve.coefficientA)
        val e = BigInteger(sha1(message + r)).mod(ellipticCurve.r)
        if (e == BigInteger.ZERO) return null
        return e
    }

    fun createS(l: BigInteger, e: BigInteger, k: BigInteger): BigInteger {
        return (l * e + k).mod(ellipticCurve.r)
    }

    fun calculateRStreak(s: BigInteger, e: BigInteger, p: Point): Point? {
        val sQ = sumPointNTimes(ellipticCurve.q, s, ellipticCurve.p, ellipticCurve.coefficientA)
        val eP = sumPointNTimes(p, e, ellipticCurve.p, ellipticCurve.coefficientA)
        return subtractPoints(sQ, eP, ellipticCurve.p, ellipticCurve.coefficientA)
    }
}