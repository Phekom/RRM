/**
 * Created by Shaun McDonald on 2021/05/27
 * Last modified on 27/05/2021, 10:50
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 **/

package za.co.xisystems.itis_rrm.forge

import com.password4j.Argon2Function
import com.password4j.BCryptFunction
import com.password4j.BadParametersException
import com.password4j.types.Argon2
import com.password4j.types.Argon2.D
import com.password4j.types.Argon2.I
import com.password4j.types.Argon2.ID
import com.password4j.types.BCrypt.A
import org.junit.Assert
import org.junit.Test
import java.util.ArrayList
import java.util.Arrays
import java.util.concurrent.Callable
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors

class Argon2Test {
    internal class TestCase(
        var plainTextPassword: String?,
        var salt: String?,
        var memory: Int,
        var iterations: Int,
        var outLength: Int,
        var parallelism: Int,
        var type: Argon2?,
        var expected: String
    ) {
        var version: Int

        constructor(
            plainTextPassword: String?,
            salt: String?,
            memory: Int,
            iterations: Int,
            outLength: Int,
            parallelism: Int,
            version: Int,
            type: Argon2?,
            expected: String
        ) : this(plainTextPassword, salt, memory, iterations, outLength, parallelism, type, expected) {
            this.version = version
        }

        init {
            version = Argon2Function.ARGON2_VERSION_13
        }
    }

    @Test
    fun test() {
        for (test in CASES) {
            val f = getFunction(test.memory, test.iterations, test.parallelism, test.outLength, test.type, test.version)
            Assert.assertEquals(test.expected, f.hash(test.plainTextPassword, test.salt).result)
        }
    }

    @Test
    @Throws(InterruptedException::class, ExecutionException::class)
    fun parallelTest() {
        val executors = Executors.newCachedThreadPool()
        val tasks: MutableList<Callable<Boolean>> = ArrayList()
        for (test in CASES) {
            val c = Callable { test.expected == getFunction(test.memory, test.iterations, test.parallelism, test.outLength, test.type, test.version).hash(test.plainTextPassword, test.salt).result }
            tasks.add(c)
        }
        val results = executors.invokeAll(tasks)
        for (future in results) {
            Assert.assertTrue(future.get())
        }
    }

    @Test(expected = BadParametersException::class)
    fun badHash() {
        // GIVEN
        val invalidHash =
            "\$argon2d\$v=19Sm=1024,t=3,p=1\$a1hYRFVFUUhMdzF5dk43\$GvtgSr24rB/U/idt+1Xq2tn0DIav/H2W0BybTLZijZY"

        // WHEN
        Argon2Function.getInstanceFromHash(invalidHash)

        // TEST
    }

    private fun getFunction(
        memory: Int,
        iterations: Int,
        parallelism: Int,
        outLength: Int,
        type: Argon2?,
        version: Int
    ): Argon2Function {
        return if (version == Argon2Function.ARGON2_VERSION_13) {
            Argon2Function.getInstance(memory, iterations, parallelism, outLength, type)
        } else {
            Argon2Function.getInstance(memory, iterations, parallelism, outLength, type, version)
        }
    }

    @Test
    fun testEquality() {
        // GIVEN
        val m = 1
        val i = 2
        val p = 3
        val l = 4
        val t = D
        val v = 0x13
        val argon2 = Argon2Function.getInstance(m, i, p, l, t, v)

        // THEN
        val eqNull = argon2 == null
        val eqClass = argon2 == BCryptFunction.getInstance(A, 10)
        val sameInst = argon2 == Argon2Function.getInstance(m, i, p, l, t, v)
        val sameInst2 = argon2 == Argon2Function.getInstance(m, i, p, l, t, v)
        val toString = argon2.toString()
        val hashCode = argon2.hashCode()
        val notSameInst1 = argon2 == Argon2Function.getInstance(m + 1, i, p, l, t, v)
        val notSameInst2 = argon2 == Argon2Function.getInstance(m, i + 1, p, l, t, v)
        val notSameInst3 = argon2 == Argon2Function.getInstance(m, i, p + 1, l, t, v)
        val notSameInst4 = argon2 == Argon2Function.getInstance(m, i, p, l + 1, t, v)
        val notSameInst5 = argon2 == Argon2Function.getInstance(m, i, p, l, ID, v)
        val notSameInst6 = argon2 == Argon2Function.getInstance(m, i, p, l, t, v + 1)

        // END
        Assert.assertFalse(eqNull)
        Assert.assertFalse(eqClass)
        Assert.assertTrue(sameInst)
        Assert.assertTrue(sameInst2)
        Assert.assertNotEquals(toString, Argon2Function.getInstance(m, i + 1, p, l, t, v).toString())
        Assert.assertNotEquals(hashCode.toLong(), Argon2Function.getInstance(m, i, p, l, t, v + 1).hashCode().toLong())
        Assert.assertFalse(notSameInst1)
        Assert.assertFalse(notSameInst2)
        Assert.assertFalse(notSameInst3)
        Assert.assertFalse(notSameInst4)
        Assert.assertFalse(notSameInst5)
        Assert.assertFalse(notSameInst6)
    }

    @Test
    fun testAccessors() {
        // GIVEN
        val memory = 3
        val iterations = 5
        val parallelism = 7
        val outputLength = 11
        val variant = D
        val version = 13

        // WHEN
        val argon2Function = Argon2Function.getInstance(memory, iterations, parallelism, outputLength, variant, version)

        // THEN
        Assert.assertEquals(memory.toLong(), argon2Function.memory.toLong())
        Assert.assertEquals(iterations.toLong(), argon2Function.iterations.toLong())
        Assert.assertEquals(parallelism.toLong(), argon2Function.parallelism.toLong())
        Assert.assertEquals(outputLength.toLong(), argon2Function.outputLength.toLong())
        Assert.assertEquals(variant, argon2Function.variant)
        Assert.assertEquals(version.toLong(), argon2Function.version.toLong())
        Assert.assertEquals("Argon2Function[m=3, i=5, p=7, l=11, t=D, v=13]", argon2Function.toString())
    }

    companion object {
        private val CASES = Arrays.asList( // STANDARD
            TestCase(
                "f6c4db4a54e2a370627aff3db617", "kXXDUEQHLw1yvN7", 1024, 3, 32, 1,
                D, "\$argon2d\$v=19\$m=1024,t=3,p=1\$a1hYRFVFUUhMdzF5dk43\$GvtgSr24rB/U/idt+1Xq2tn0DIav/H2W0BybTLZijZY"
            ),
            TestCase(
                "f6c4db4a54e2a370627aff3db617", "kXXDUEQHLw1yvN7", 1024, 3, 32, 3,
                D, "\$argon2d\$v=19\$m=1024,t=3,p=3\$a1hYRFVFUUhMdzF5dk43$+2ZPk1DYKqBDxWooR+zPhLCJNCy5gfeDEkh8MaQXf4I"
            ),
            TestCase(
                "securePassowrd!!!",
                "mySalt02",
                4096,
                50,
                512,
                4,
                D,
                "\$argon2d\$v=19\$m=4096,t=50,p=4\$bXlTYWx0MDI\$Prd8OUtkdPadnP3MYv1w0DHnUHV6A0sn4tA55nbui1uxKP9AjaD/1qk0OpNkZKObpXJ9slLlb3I8mgYehtbeAGh5uPiVvpfZChfJjbwmDJd3t0d59vhU2+vGO+t/l2t71lPLHKtTwMPmjxybF6QcfCDcOCUW4JBx+RxFj3aCeM7U64aaRphYCRZhNQmidFzydgssU5nlD/EXWz1LaxPUMs+p6qFuvWLyjvyCQo59nJDk9FGtcsL7CJRGvjx7yiggv95fWNg0iTsh3SgicR5OQWfUbUIJtPrdyVuu4QLlUIUhxotjG3SuEcBhuH0Q1jN2PKO/AOP0/2JsLH79wItdPa+w0SzXotNTMFTXhs/aLpzwRtnK2qRvw4BzSTR8Rief1MimG9QxbkGk9sMbmPT5c7ZsxllWLTYy1kwt9ymDBrS34zmL6pn5vK1QOi/VgMMVws+LiXa31+CHdfeR5AtbV0RxskcWDWqNb9//MXXRhlMpMMoFHddyYeHvTSxrHnPAcfur3Dk4K2KAh1q5UEuHGKAfT12l1XhTqNobBhr5W0TbiPW3S/oxIkeee9J2iD0iNks44Cy9vbWwtS9G+z+D+FyYIm/aPBVKxeh8ZbccjAXUC98dxdEKVZ/T7uARuBmVB1Wo6TPkM7j1u+qANu705lrNBagkli+O5TYOJEaWOi4"
            ),
            TestCase(
                "f6c4db4a54e2a370627aff3db617", "kXXDUEQHLw1yvN7", 1024, 3, 32, 1,
                I, "\$argon2i\$v=19\$m=1024,t=3,p=1\$a1hYRFVFUUhMdzF5dk43\$cJfBg8Ki319tkv9pVuy3FcqpQGj3uDLlhqkw3EILJ9c"
            ),
            TestCase(
                "f6c4db4a54e2a370627aff3db617", "kXXDUEQHLw1yvN7", 1024, 3, 32, 3,
                I, "\$argon2i\$v=19\$m=1024,t=3,p=3\$a1hYRFVFUUhMdzF5dk43\$ZMPK1QfA5x1RQWMS80rzq6SE2OAs+Bdd+9ktPe7uZUo"
            ),
            TestCase(
                "securePassowrd!!!",
                "mySalt02",
                4096,
                50,
                512,
                4,
                I,
                "\$argon2i\$v=19\$m=4096,t=50,p=4\$bXlTYWx0MDI\$voZUGPEULMj+5jSK3eJiV6WXW/NLWnUNgPprUnPX1K0/JkQSkNMcPXZg07CJzNgu4d91JdHS3z4dRHlYTBK8CDTChbMJUeF9kVo4tUBtrVKaLEaZOgu4/EvuBlRBZmp5R24OkglBpGT3BbBSLx/WOjlJG/SY1WElbmbeJMVBDj3cGRMlf6uYwuumze6FSDnHVm1zV6PGMF+cOt9JyDhRrzJLUkbd8yT/Z/LMy0CKbnE8FLdW0zGqNPAV6t+GexPUWrvi0ilC5B+kk6UZA5UUKs8/D5CO7k4i6NYAoAUXOk572pGkg2qftsxl8+al4ffen2FTQQ3r4TD2vUW3VRW5W7UmT4fiIVc1XFcTIyDG+J3uFovSyWlSnGd2M1hkO2dbxGnvOY96SCL5BGBpXxKifVJclC91LHKYOWg2eVZQHZZ2jFmS5YEuzH+pFisWyncQ7VodDFiTlk/zZj5TfV6jPQnubPcO2iFkaJrgUgCotFA2l6Ddl9IUdpzVUQHmaEXceGawcZ8vN8f64rO8euTp2fAGjtBf7p6sQIVOYdYLazKhK+x2sMNur+8oJybWrQtZNu+GcN3y0cMMrPyKjnb5gwmkOS/3eYBHqFT7hgs1C6eBgrnyxMuqZbj7mb5ABiNocYnlKiFbhtMQbEiyFwroNvWqZ74yzpQScBlh4yhS1qs"
            ),
            TestCase(
                "f6c4db4a54e2a370627aff3db617", "kXXDUEQHLw1yvN7", 1024, 3, 32, 1,
                ID, "\$argon2id\$v=19\$m=1024,t=3,p=1\$a1hYRFVFUUhMdzF5dk43\$oZQjFpZE3edaKLPT88nwAqxlLLv3JQA3Et5i+0u7hso"
            ),
            TestCase(
                "f6c4db4a54e2a370627aff3db617", "kXXDUEQHLw1yvN7", 1024, 3, 32, 3,
                ID, "\$argon2id\$v=19\$m=1024,t=3,p=3\$a1hYRFVFUUhMdzF5dk43$+pbPYkP4PTHdGhl4syeyZihZHKP74mbz8PH112/pw+k"
            ),
            TestCase(
                "securePassowrd!!!",
                "mySalt02",
                4096,
                50,
                512,
                4,
                ID,
                "\$argon2id\$v=19\$m=4096,t=50,p=4\$bXlTYWx0MDI\$eI9VrXKlaAxuSmWHmEFlihNHOlWmpSRmxlKbpw33NDIgbTjS7d6AHq7RbmQ+x2A2ENN8TUygvNvYymV6ufQqiVx6QORXv2gfIyI2mPzygP3ZdCKG0r2Sx4RJa8DClkV9/SMFs4fcSTUkI3IVn4M4lguOe4oEq6ig0M9P5VzsRvgCyfCMLIBGUlQqMDxfyIyk4RA3SNvwwqvaZPDSlid+0TzXiLv5IoQkpeW3W2moehkBL9fs/PwIQZZlJVVQXGRZ40U26ny8d0HLkaIch+MKX/sT7yPaicGEfRkpdec4biI+V8BgzZRFYg7hRxM5FJTSsvMs+xomuEEKDlXWjD4LlCPtRNWF5nZ/yagUSXU7rFi3E2zM57gdqltZNDmqXGDdIH/Ev5Rw5qGm0DUxOAvTCgMy953GVLa6fuTX86cEpWDNMGVSzSSS+8aWt4d5QNbLhh5nwkEw/2eer2AyZiRI3W+JYoXaY0H4oL6xOxwWOqS2KC661aZVCtu5m8mvAtCkC4Hq9DvSBTqdZrsQXhh7salmooXNKpUeM/05ifAWf2BBRSK5HNxXzkC2iB2et4yeA5/yOqQu89e7qGfp7EU9jAtwqy4VvVLdp1CYcTuMnUy7nGAnDDOYX1+jTyY4NJ6eRxQVfjDdIqIPB95qhyjsZ8jjZIqWxaoxhMARfmi3rl4"
            ), // MINIMAL
            TestCase(
                "mini", "12345678", 8, 1, 4, 1,
                D, "\$argon2d\$v=19\$m=8,t=1,p=1\$MTIzNDU2Nzg\$zdVOjw"
            ),
            TestCase(
                "mini", "12345678", 8, 1, 4, 1,
                I, "\$argon2i\$v=19\$m=8,t=1,p=1\$MTIzNDU2Nzg\$Gn9A4g"
            ),
            TestCase(
                "mini", "12345678", 8, 1, 4, 1,
                ID, "\$argon2id\$v=19\$m=8,t=1,p=1\$MTIzNDU2Nzg\$zJ0Sag"
            ), // REPETITIVE
            TestCase(
                "first!", "11111111", 1024, 3, 32, 12,
                ID, "\$argon2id\$v=19\$m=1024,t=3,p=12\$MTExMTExMTE$0PUE8wVEaK0qdjms3b4pTZOs0+00S/+9j28WZ3gMUno"
            ),
            TestCase(
                "second?", "22222222", 1024, 3, 32, 12,
                ID, "\$argon2id\$v=19\$m=1024,t=3,p=12\$MjIyMjIyMjI\$f38E3C9DdqJ5dq5pCe27FXMQiTAlx47ulfTPKDf+feg"
            ),
            TestCase(
                "third#", "33333333", 1024, 3, 32, 12,
                ID, "\$argon2id\$v=19\$m=1024,t=3,p=12\$MzMzMzMzMzM\$DXUE5N4lm4plldg9nGMq+tYsbGhko8HWpPaADujpgFQ"
            ),
            TestCase(
                "fourth@", "44444444", 1024, 3, 32, 12,
                ID, "\$argon2id\$v=19\$m=1024,t=3,p=12\$NDQ0NDQ0NDQ\$HEoprKMypoVVGYR71EKw66gUFTndNs/p1joWXmeUVvk"
            ), // PARAM
            TestCase(
                "f6c4db4a54e2a370627aff3db617", "kXXDUEQHLw1yvN7", 512, 13, 17, 3,
                ID, "\$argon2id\$v=19\$m=512,t=13,p=3\$a1hYRFVFUUhMdzF5dk43\$GH89G/RebgaZwv4pyeWG7lU"
            ),
            TestCase(
                "mini", "12345678", 32, 10, 32, 4, 0x10,
                I, "\$argon2i\$v=16\$m=32,t=10,p=4\$MTIzNDU2Nzg\$Tu4w/edteuxnqMFDkR2QBgcDc3rjIzJeEo8C44nDiCM"
            )
        )
    }
}
