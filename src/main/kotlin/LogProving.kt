import java.io.File
import java.math.BigInteger
import java.util.*
import kotlin.system.exitProcess

class LogProving {
    companion object {
        fun generateCurve() {
            val ellipticCurve = EllipticCurve(BYTE_LENGTH)
            writeEllipticCurve(ellipticCurve, "$FOURTH_TASK_OUTPUT/$ELLIPTIC_CURVE_PATH")
        }

        fun generateP() {
            println("Введите l")
            val l = readLine()?.toBigInteger()
            if (l == null) {
                println("Некорретное l")
                exitProcess(1)
            }

            val ellipticCurve = readEllipticCurve("$FOURTH_TASK_OUTPUT/$ELLIPTIC_CURVE_PATH")
            if (ellipticCurve == null) {
                println("Эллиптическая кривая некорректна")
                exitProcess(1)
            }

            File("$FOURTH_TASK_OUTPUT/$L_PATH").writeText("$l")
            println("Параметр l записан в файл")

            val p = sumPointNTimes(ellipticCurve.q, l, ellipticCurve.p, ellipticCurve.coefficientA)
            File("$FOURTH_TASK_OUTPUT/$P_PATH").writeText("$p")
            println("Точка P вычислена и записана в файл")
        }

        fun generateK() {
            val ellipticCurve = readEllipticCurve("$FOURTH_TASK_OUTPUT/$ELLIPTIC_CURVE_PATH")
            if (ellipticCurve == null) {
                println("Эллиптическая кривая некорректна")
                exitProcess(1)
            }

            val k = BigInteger(ellipticCurve.r.bitLength(), Random()).mod(ellipticCurve.r)
            File("$FOURTH_TASK_OUTPUT/$K_PATH").writeText("$k")
            println("Параметр k записан в файл")
        }

        fun generateKStreak() {
            val ellipticCurve = readEllipticCurve("$FOURTH_TASK_OUTPUT/$ELLIPTIC_CURVE_PATH")
            if (ellipticCurve == null) {
                println("Эллиптическая кривая некорректна")
                exitProcess(1)
            }

            val l = try {
                File("$FOURTH_TASK_OUTPUT/$L_PATH").readText().toBigInteger()
            } catch (e: Exception) {
                println("Параметр l некорректен")
                exitProcess(1)
            }

            val k = try {
                File("$FOURTH_TASK_OUTPUT/$K_PATH").readText().toBigInteger()
            } catch (e: Exception) {
                println("Параметр k некорректен")
                exitProcess(1)
            }

            val kStreak = (l * k).mod(ellipticCurve.r)
            File("$FOURTH_TASK_OUTPUT/$K_STREAK_PATH").writeText("$kStreak")
            println("Параметр k' записан в файл")
        }

        fun generateR() {
            val ellipticCurve = readEllipticCurve("$FOURTH_TASK_OUTPUT/$ELLIPTIC_CURVE_PATH")
            if (ellipticCurve == null) {
                println("Эллиптическая кривая некорректна")
                exitProcess(1)
            }

            val p = try {
                val (x, y) = File("$FOURTH_TASK_OUTPUT/$P_PATH")
                    .readText().split(", ").map { it.toBigInteger() }
                if (!pointBelongsToCurve(Point(x, y), ellipticCurve.p, ellipticCurve.coefficientA))
                    throw Exception()
                Point(x, y)
            } catch (e: Exception) {
                println("Точка P некорректна")
                exitProcess(1)
            }

            val k = try {
                File("$FOURTH_TASK_OUTPUT/$K_PATH").readText().toBigInteger()
            } catch (e: Exception) {
                println("Параметр k некорректен")
                exitProcess(1)
            }

            val r = sumPointNTimes(p, k, ellipticCurve.p, ellipticCurve.coefficientA)
            File("$FOURTH_TASK_OUTPUT/$R_PATH").writeText("$r")
            println("Параметр R записан в файл")
        }

        fun generateZ() {
            val z = BigInteger(BYTE_LENGTH, Random())
            File("$FOURTH_TASK_OUTPUT/$Z_PATH").writeText("$z")
            println("Параметр z записан в файл")
        }

        fun encryptR() {
            val ellipticCurve = readEllipticCurve("$FOURTH_TASK_OUTPUT/$ELLIPTIC_CURVE_PATH")
            if (ellipticCurve == null) {
                println("Эллиптическая кривая некорректна")
                exitProcess(1)
            }

            val z = try {
                File("$FOURTH_TASK_OUTPUT/$Z_PATH").readText().toBigInteger()
            } catch (e: Exception) {
                println("Параметр z некорректен")
                exitProcess(1)
            }

            val r = try {
                val (x, y) = File("$FOURTH_TASK_OUTPUT/$R_PATH")
                    .readText().split(", ").map { it.toBigInteger() }
                if (!pointBelongsToCurve(Point(x, y), ellipticCurve.p, ellipticCurve.coefficientA))
                    throw Exception()
                Point(x, y)
            } catch (e: Exception) {
                println("Точка R некорректна")
                exitProcess(1)
            }

            val rEncrypted = aesEncrypt(r.toString(), z)
            File("$FOURTH_TASK_OUTPUT/$R_ENCRYPTED_PATH").writeText("$rEncrypted")
            println("Параметр R зашифрован и записан в файл")
        }

        fun generateI() {
            val i = kotlin.random.Random.nextInt(2)
            File("$FOURTH_TASK_OUTPUT/$I_PATH").writeText("$i")
            println("Параметр i записан в файл")
        }

        fun sendViaChannel() {
            val i = try {
                File("$FOURTH_TASK_OUTPUT/$I_PATH").readText().toInt()
            } catch (e: Exception) {
                println("Параметр i некорректен")
                exitProcess(1)
            }

            val z = try {
                File("$FOURTH_TASK_OUTPUT/$Z_PATH").readText().toBigInteger()
            } catch (e: Exception) {
                println("Параметр z некорректен")
                exitProcess(1)
            }

            val k = try {
                File("$FOURTH_TASK_OUTPUT/$K_PATH").readText().toBigInteger()
            } catch (e: Exception) {
                println("Параметр k некорректен")
                exitProcess(1)
            }

            val kStreak = try {
                File("$FOURTH_TASK_OUTPUT/$K_STREAK_PATH").readText().toBigInteger()
            } catch (e: Exception) {
                println("Параметр k' некорректен")
                exitProcess(1)
            }

            if (i == 0) {
                File("$FOURTH_TASK_OUTPUT/$C0_PATH").writeText("$z, $k")
                File("$FOURTH_TASK_OUTPUT/$C1_PATH").writeText("$z, $kStreak")
            } else {
                File("$FOURTH_TASK_OUTPUT/$C1_PATH").writeText("$z, $k")
                File("$FOURTH_TASK_OUTPUT/$C0_PATH").writeText("$z, $kStreak")
            }
            println("Параметры записан в каналы")
        }

        fun checkR() {
            val i = try {
                File("$FOURTH_TASK_OUTPUT/$I_PATH").readText().toInt()
            } catch (e: Exception) {
                println("Параметр i некорректен")
                exitProcess(1)
            }

            val z: BigInteger
            val k: BigInteger
            val kStreak: BigInteger
            if (i == 0) {
                val (zTmp, kTmp) = File("$FOURTH_TASK_OUTPUT/$C0_PATH")
                    .readText().split(", ").map { it.toBigInteger() }
                val (zStreakTmp, kStreakTmp) = File("$FOURTH_TASK_OUTPUT/$C1_PATH")
                    .readText().split(", ").map { it.toBigInteger() }
                if (zTmp != zStreakTmp) {
                    println("Параметры в канале некорректны")
                    exitProcess(1)
                }
                z = zTmp
                k = kTmp
                kStreak = kStreakTmp
            } else {
                val (zTmp, kTmp) = File("$FOURTH_TASK_OUTPUT/$C1_PATH")
                    .readText().split(", ").map { it.toBigInteger() }
                val (zStreakTmp, kStreakTmp) = File("$FOURTH_TASK_OUTPUT/$C0_PATH")
                    .readText().split(", ").map { it.toBigInteger() }
                if (zTmp != zStreakTmp) {
                    println("Параметры в канале некорректны")
                    exitProcess(1)
                }
                z = zTmp
                k = kTmp
                kStreak = kStreakTmp
            }

            val ellipticCurve = readEllipticCurve("$FOURTH_TASK_OUTPUT/$ELLIPTIC_CURVE_PATH")
            if (ellipticCurve == null) {
                println("Эллиптическая кривая некорректна")
                exitProcess(1)
            }

            val r = try {
                val encryptedR = File("$FOURTH_TASK_OUTPUT/$R_ENCRYPTED_PATH").readText()
                val stringR = aesDecrypt(encryptedR, z)
                val (x, y) = stringR.split(", ").map { it.toBigInteger() }
                if (!pointBelongsToCurve(Point(x, y), ellipticCurve.p, ellipticCurve.coefficientA))
                    throw Exception()
                val rOrder = getCurvePoints(Point(x, y), ellipticCurve.p, ellipticCurve.coefficientA).size
                if (rOrder.toBigInteger() != ellipticCurve.r)
                    throw Exception()
                Point(x, y)
            } catch (e: Exception) {
                println("Точка R некорректна")
                exitProcess(1)
            }

            val p = try {
                val (x, y) = File("$FOURTH_TASK_OUTPUT/$P_PATH")
                    .readText().split(", ").map { it.toBigInteger() }
                if (!pointBelongsToCurve(Point(x, y), ellipticCurve.p, ellipticCurve.coefficientA))
                    throw Exception()
                Point(x, y)
            } catch (e: Exception) {
                println("Точка P некорректна")
                exitProcess(1)
            }

            val kP = sumPointNTimes(p, k, ellipticCurve.p, ellipticCurve.coefficientA)
            val kStreakQ = sumPointNTimes(ellipticCurve.q, kStreak, ellipticCurve.p, ellipticCurve.coefficientA)

            if (r == kP || r == kStreakQ) {
                println("Равенство выполняется")
            }
        }
    }
}