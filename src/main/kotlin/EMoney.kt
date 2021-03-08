import java.io.File
import java.math.BigInteger
import java.util.*
import kotlin.system.exitProcess

class EMoney {
    companion object {
        fun generateCurve() {
            val ellipticCurve = EllipticCurve(BYTE_LENGTH)
            writeEllipticCurve(ellipticCurve, "$FIFTH_TASK_OUTPUT/$ELLIPTIC_CURVE_PATH")
        }

        fun generateL() {
            println("Введите l")
            val l = readLine()?.toBigInteger()
            if (l == null) {
                println("Некорретное l")
                exitProcess(1)
            }

            File("$FIFTH_TASK_OUTPUT/$L_PATH").writeText("$l")
            println("Параметр l записан в файл")
        }

        fun generateP() {
            val l = try {
                File("$FIFTH_TASK_OUTPUT/$L_PATH").readText().toBigInteger()
            } catch (e: Exception) {
                println("Параметр l некорректен")
                exitProcess(1)
            }

            val ellipticCurve = readEllipticCurve("$FIFTH_TASK_OUTPUT/$ELLIPTIC_CURVE_PATH")
            if (ellipticCurve == null) {
                println("Эллиптическая кривая некорректна")
                exitProcess(1)
            }

            val p = sumPointNTimes(ellipticCurve.q, l, ellipticCurve.p, ellipticCurve.coefficientA)
            File("$FIFTH_TASK_OUTPUT/$P_PATH").writeText("$p")
            println("Точка P вычислена и записана в файл")
        }

        fun generateMessage() {
            println("Введите сообщение:")
            val message = readLine() ?: "Текст пример 2021"
            File("$FIFTH_TASK_OUTPUT/$MESSAGE_PATH").writeText(message)
            println("Сообщение записано в файл")
        }

        fun generateKStreak() {
            val ellipticCurve = readEllipticCurve("$FIFTH_TASK_OUTPUT/$ELLIPTIC_CURVE_PATH")
            if (ellipticCurve == null) {
                println("Эллиптическая кривая некорректна")
                exitProcess(1)
            }

            val kStreak = BigInteger(ellipticCurve.r.bitLength(), Random()).mod(ellipticCurve.r)
            val rStreak = sumPointNTimes(ellipticCurve.q, kStreak, ellipticCurve.p, ellipticCurve.coefficientA)

            File("$FIFTH_TASK_OUTPUT/$K_STREAK_PATH").writeText("$kStreak")
            println("Параметр k' записан в файл")
            File("$FIFTH_TASK_OUTPUT/$R_STREAK_PATH").writeText("$rStreak")
            println("Параметр R' записан в файл")
        }

        private fun getRStreak(ellipticCurve: EllipticCurve): Point {
            return try {
                val (x, y) = File("$FIFTH_TASK_OUTPUT/$R_STREAK_PATH")
                    .readText().split(", ").map { it.toBigInteger() }
                if (!pointBelongsToCurve(Point(x, y), ellipticCurve.p, ellipticCurve.coefficientA))
                    throw Exception()
                Point(x, y)
            } catch (e: Exception) {
                println("Точка R' некорректна")
                exitProcess(1)
            }
        }

        private fun getR(ellipticCurve: EllipticCurve): Point {
            return try {
                val (x, y) = File("$FIFTH_TASK_OUTPUT/$R_PATH")
                    .readText().split(", ").map { it.toBigInteger() }
                if (!pointBelongsToCurve(Point(x, y), ellipticCurve.p, ellipticCurve.coefficientA))
                    throw Exception()
                Point(x, y)
            } catch (e: Exception) {
                println("Точка R некорректна")
                exitProcess(1)
            }
        }

        private fun getP(ellipticCurve: EllipticCurve): Point {
            return try {
                val (x, y) = File("$FIFTH_TASK_OUTPUT/$P_PATH")
                    .readText().split(", ").map { it.toBigInteger() }
                if (!pointBelongsToCurve(Point(x, y), ellipticCurve.p, ellipticCurve.coefficientA))
                    throw Exception()
                Point(x, y)
            } catch (e: Exception) {
                println("Точка P некорректна")
                exitProcess(1)
            }
        }

        fun checkAndSendFRStreak() {
            val ellipticCurve = readEllipticCurve("$FIFTH_TASK_OUTPUT/$ELLIPTIC_CURVE_PATH")
            if (ellipticCurve == null) {
                println("Эллиптическая кривая некорректна")
                exitProcess(1)
            }

            val rStreak = getRStreak(ellipticCurve)

            val fRStreak = f(rStreak)
            if (fRStreak == BigInteger.ZERO) {
                println("Точка f(R') = 0. Замените k' и R'.")
                exitProcess(1)
            }
            println("Точка f(R') != 0. R' отправлен клиенту.")
        }

        fun checkRStreak() {
            val ellipticCurve = readEllipticCurve("$FIFTH_TASK_OUTPUT/$ELLIPTIC_CURVE_PATH")
            if (ellipticCurve == null) {
                println("Эллиптическая кривая некорректна")
                exitProcess(1)
            }

            getRStreak(ellipticCurve)

            println("Точка R' принадлежит кривой E(K)")
        }

        fun generateAlpha() {
            val ellipticCurve = readEllipticCurve("$FIFTH_TASK_OUTPUT/$ELLIPTIC_CURVE_PATH")
            if (ellipticCurve == null) {
                println("Эллиптическая кривая некорректна")
                exitProcess(1)
            }

            val alpha = BigInteger(ellipticCurve.r.bitLength(), Random()).mod(ellipticCurve.r)

            File("$FIFTH_TASK_OUTPUT/$ALPHA_PATH").writeText("$alpha")
            println("Параметр alpha записан в файл")
        }

        fun generateR() {
            val ellipticCurve = readEllipticCurve("$FIFTH_TASK_OUTPUT/$ELLIPTIC_CURVE_PATH")
            if (ellipticCurve == null) {
                println("Эллиптическая кривая некорректна")
                exitProcess(1)
            }

            val alpha = try {
                File("$FIFTH_TASK_OUTPUT/$ALPHA_PATH").readText().toBigInteger()
            } catch (e: Exception) {
                println("Параметр alpha некорректен")
                exitProcess(1)
            }

            val rStreak = getRStreak(ellipticCurve)

            val r = sumPointNTimes(rStreak, alpha, ellipticCurve.p, ellipticCurve.coefficientA)
            val fR = f(r)
            if (fR == BigInteger.ZERO) {
                println("Точка f(R) = 0. Замените alpha.")
                exitProcess(1)
            }
            println("Точка f(R) != 0.")
            File("$FIFTH_TASK_OUTPUT/$R_PATH").writeText("$r")
            println("Параметр R записан в файл")
        }

        fun generateBeta() {
            val ellipticCurve = readEllipticCurve("$FIFTH_TASK_OUTPUT/$ELLIPTIC_CURVE_PATH")
            if (ellipticCurve == null) {
                println("Эллиптическая кривая некорректна")
                exitProcess(1)
            }

            val r = getR(ellipticCurve)
            val rStreak = getRStreak(ellipticCurve)

            val beta = (f(r) * f(rStreak).modInverse(ellipticCurve.r)).mod(ellipticCurve.r)
            File("$FIFTH_TASK_OUTPUT/$BETA_PATH").writeText("$beta")
            println("Параметр beta записан в файл")
        }

        fun applyMask() {
            val ellipticCurve = readEllipticCurve("$FIFTH_TASK_OUTPUT/$ELLIPTIC_CURVE_PATH")
            if (ellipticCurve == null) {
                println("Эллиптическая кривая некорректна")
                exitProcess(1)
            }

            val messageFile = File("$FIFTH_TASK_OUTPUT/$MESSAGE_PATH")
            if (!messageFile.exists() || messageFile.readText().isEmpty()) {
                println("Сообщение некоррекно")
                exitProcess(1)
            }
            val m = BigInteger(sha1(messageFile.readText())).mod(ellipticCurve.r)

            val beta = try {
                File("$FIFTH_TASK_OUTPUT/$BETA_PATH").readText().toBigInteger()
            } catch (e: Exception) {
                println("Параметр beta некорректен")
                exitProcess(1)
            }

            val alpha = try {
                File("$FIFTH_TASK_OUTPUT/$ALPHA_PATH").readText().toBigInteger()
            } catch (e: Exception) {
                println("Параметр alpha некорректен")
                exitProcess(1)
            }

            val mStreak = (alpha * beta.modInverse(ellipticCurve.r) * m).mod(ellipticCurve.r)
            File("$FIFTH_TASK_OUTPUT/$M_STREAK_PATH").writeText("$mStreak")
            println("Параметр m' записан в файл")
        }

        fun checkMStreak() {
            val mStreak = try {
                File("$FIFTH_TASK_OUTPUT/$M_STREAK_PATH").readText().toBigInteger()
            } catch (e: Exception) {
                println("Параметр m' некорректен")
                exitProcess(1)
            }

            if (mStreak == BigInteger.ZERO) {
                println("Параметр m' = 0. Прервите протокол.")
                exitProcess(1)
            }
            println("Параметр m' != 0.")
        }

        fun generateSStreak() {
            val ellipticCurve = readEllipticCurve("$FIFTH_TASK_OUTPUT/$ELLIPTIC_CURVE_PATH")
            if (ellipticCurve == null) {
                println("Эллиптическая кривая некорректна")
                exitProcess(1)
            }

            val l = try {
                File("$FIFTH_TASK_OUTPUT/$L_PATH").readText().toBigInteger()
            } catch (e: Exception) {
                println("Параметр l некорректен")
                exitProcess(1)
            }

            val rStreak = getRStreak(ellipticCurve)

            val kStreak = try {
                File("$FIFTH_TASK_OUTPUT/$K_STREAK_PATH").readText().toBigInteger()
            } catch (e: Exception) {
                println("Параметр k' некорректен")
                exitProcess(1)
            }

            val mStreak = try {
                File("$FIFTH_TASK_OUTPUT/$M_STREAK_PATH").readText().toBigInteger()
            } catch (e: Exception) {
                println("Параметр m' некорректен")
                exitProcess(1)
            }

            val sStreak = (l * f(rStreak) + kStreak * mStreak).mod(ellipticCurve.r)
            File("$FIFTH_TASK_OUTPUT/$S_STREAK_PATH").writeText("$sStreak")
            println("Параметр s' записан в файл")
        }

        fun checkSStreakQ() {
            val ellipticCurve = readEllipticCurve("$FIFTH_TASK_OUTPUT/$ELLIPTIC_CURVE_PATH")
            if (ellipticCurve == null) {
                println("Эллиптическая кривая некорректна")
                exitProcess(1)
            }

            val sStreak = try {
                File("$FIFTH_TASK_OUTPUT/$S_STREAK_PATH").readText().toBigInteger()
            } catch (e: Exception) {
                println("Параметр s' некорректен")
                exitProcess(1)
            }

            val rStreak = getRStreak(ellipticCurve)

            val p = getP(ellipticCurve)

            val mStreak = try {
                File("$FIFTH_TASK_OUTPUT/$M_STREAK_PATH").readText().toBigInteger()
            } catch (e: Exception) {
                println("Параметр m' некорректен")
                exitProcess(1)
            }

            val sStreakQ = sumPointNTimes(ellipticCurve.q, sStreak, ellipticCurve.p, ellipticCurve.coefficientA)
            val fP = sumPointNTimes(p, f(rStreak), ellipticCurve.p, ellipticCurve.coefficientA)
            val mR = sumPointNTimes(rStreak, mStreak, ellipticCurve.p, ellipticCurve.coefficientA)

            if (sStreakQ != sumPoints(fP, mR, ellipticCurve.p, ellipticCurve.coefficientA)) {
                println("s'Q != f(R')P + m'R'")
            }
            println("s'Q равно f(R')P + m'R'")
        }

        fun generateS() {
            val ellipticCurve = readEllipticCurve("$FIFTH_TASK_OUTPUT/$ELLIPTIC_CURVE_PATH")
            if (ellipticCurve == null) {
                println("Эллиптическая кривая некорректна")
                exitProcess(1)
            }

            val beta = try {
                File("$FIFTH_TASK_OUTPUT/$BETA_PATH").readText().toBigInteger()
            } catch (e: Exception) {
                println("Параметр beta некорректен")
                exitProcess(1)
            }

            val sStreak = try {
                File("$FIFTH_TASK_OUTPUT/$S_STREAK_PATH").readText().toBigInteger()
            } catch (e: Exception) {
                println("Параметр s' некорректен")
                exitProcess(1)
            }

            val s = (sStreak * beta).mod(ellipticCurve.r)
            File("$FIFTH_TASK_OUTPUT/$S_PATH").writeText("$s")
            println("Параметр s записан в файл")
        }

        fun checkM() {
            val ellipticCurve = readEllipticCurve("$FIFTH_TASK_OUTPUT/$ELLIPTIC_CURVE_PATH")
            if (ellipticCurve == null) {
                println("Эллиптическая кривая некорректна")
                exitProcess(1)
            }

            val messageFile = File("$FIFTH_TASK_OUTPUT/$MESSAGE_PATH")
            if (!messageFile.exists() || messageFile.readText().isEmpty()) {
                println("Сообщение некоррекно")
                exitProcess(1)
            }
            val m = BigInteger(sha1(messageFile.readText())).mod(ellipticCurve.r)

            if (m == BigInteger.ZERO) {
                println("m = 0. Подпись s недействительна.")
                exitProcess(1)
            }
            println("m != 0. Продолжайте погашение монеты.")
        }

        fun checkFR() {
            val ellipticCurve = readEllipticCurve("$FIFTH_TASK_OUTPUT/$ELLIPTIC_CURVE_PATH")
            if (ellipticCurve == null) {
                println("Эллиптическая кривая некорректна")
                exitProcess(1)
            }

            val r = getR(ellipticCurve)
            val fR = f(r)

            if (fR == BigInteger.ZERO) {
                println("f(R) = 0. Подпись s недействительна.")
                exitProcess(1)
            }
            println("f(R) != 0. Продолжайте погашение монеты.")
        }

        fun checkSQ() {
            val ellipticCurve = readEllipticCurve("$FIFTH_TASK_OUTPUT/$ELLIPTIC_CURVE_PATH")
            if (ellipticCurve == null) {
                println("Эллиптическая кривая некорректна")
                exitProcess(1)
            }

            val r = getR(ellipticCurve)

            val p = getP(ellipticCurve)

            val s = try {
                File("$FIFTH_TASK_OUTPUT/$S_PATH").readText().toBigInteger()
            } catch (e: Exception) {
                println("Параметр s некорректен")
                exitProcess(1)
            }

            val messageFile = File("$FIFTH_TASK_OUTPUT/$MESSAGE_PATH")
            if (!messageFile.exists() || messageFile.readText().isEmpty()) {
                println("Сообщение некоррекно")
                exitProcess(1)
            }
            val m = BigInteger(sha1(messageFile.readText())).mod(ellipticCurve.r)

            val sQ = sumPointNTimes(ellipticCurve.q, s, ellipticCurve.p, ellipticCurve.coefficientA)
            val fRP = sumPointNTimes(p, f(r), ellipticCurve.p, ellipticCurve.coefficientA)
            val mR = sumPointNTimes(r, m, ellipticCurve.p, ellipticCurve.coefficientA)

            if (sQ != sumPoints(fRP, mR, ellipticCurve.p, ellipticCurve.coefficientA)) {
                println("sQ != f(R)P + mR. Подпись s недействительна.")
                exitProcess(1)
            }
            println("Подпись s подлинная.")
        }
    }
}