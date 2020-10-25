import java.util.*

/**
 * Calculator library implementation
 *
 * At the moment it provides 4 operations with names:ADD,SUB,MUL,DIV.
 * Only numeric values/parameters!
 *
 * Add you own function by calling registerCustomFunction
 *
 */


class Calculator {
    companion object {
        const val OP_ADD: String = "ADD"
        const val OP_SUB: String = "SUB"
        const val OP_MUL: String = "MUL"
        const val OP_DIV: String = "DIV"
    }

    private val addFunc: (List<CalcNumber>) -> CalcNumber = { it.reduce { a, b -> a + b } }
    private val subFunc: (List<CalcNumber>) -> CalcNumber = { it.reduce { a, b -> a - b } }
    private val mulFunc: (List<CalcNumber>) -> CalcNumber = { it.reduce { a, b -> a * b } }
    private val divFunc: (List<CalcNumber>) -> CalcNumber = { it.reduce { a, b -> a / b } }

    private val ops = mutableMapOf(OP_ADD to addFunc, OP_SUB to subFunc, OP_MUL to mulFunc, OP_DIV to divFunc)


    class CalcNumber(val value: Number) {
        val getAsLong: () -> Long = { value.toLong() }
        val getAsDouble: () -> Double = { value.toDouble() }

        internal fun isInteger() = value is Int || value is Long
        internal fun isFloating() = value is Float || value is Double

        operator fun plus(v: CalcNumber): CalcNumber =
            if (isInteger() && v.isInteger())
                CalcNumber(getAsLong() + v.getAsLong())
            else
                CalcNumber(getAsDouble() + v.getAsDouble())

        operator fun minus(v: Calculator.CalcNumber): CalcNumber =
            if (isInteger() && v.isInteger())
                CalcNumber(getAsLong() - v.getAsLong())
            else
                CalcNumber(getAsDouble() - v.getAsDouble())

        operator fun times(v: CalcNumber): CalcNumber =
            if (isInteger() && v.isInteger())
                CalcNumber(getAsLong() * v.getAsLong())
            else
                CalcNumber(getAsDouble() * v.getAsDouble())

        // divisions are always double type!
        operator fun div(v: CalcNumber): CalcNumber =
            if (isInteger()) CalcNumber(getAsLong() / v.getAsDouble())
            else CalcNumber(getAsDouble() / v.getAsDouble())

    }

    /**
     * registers custom function
     * @param name - name for the function, should be unique.
     * @throws FunctionAlreadyPresent - in case when you try to register already existing function
     *
     */
    fun registerCustomFunction(name: String, function: (List<out CalcNumber>) -> CalcNumber) {
        ops.put(name, function)
    }


    /**
     * Executes single OPERATION with arguments
     *
     * @throws FunctionNotFound if requested function is missed
     */
    @Throws(FunctionNotFound::class)
    fun executeSingle(name: String, vararg params: Number): Number {
        val func = ops[name] ?: throw FunctionNotFound()
        val t = func(params.map { Calculator.CalcNumber(it) })
        return t.value
    }

    private fun isOperation(param: Any): Boolean {
        return param is String && param in ops.keys
    }

    /**
     * Executes expression in a form of list of operations and arguments sequentially
     * example : ADD,2,3,4,DIV,2,SUB,12 will be evaluated to (2+3+4)/2 - 12
     *
     * 1,2,3,ADD,1,2,... - first elements will be skipped
     * 1,2,3,4 - this expression is wrong
     *
     * @throws FunctionNotFound if requested function is missed
     * @throws WrongExpression if expression is wrong
     */
    @Throws(WrongExpression::class)
    fun executeExpression(vararg expression: Any): Number {
        try {
            val params = LinkedList<Any>()
            expression.forEach {
                if (isOperation(it)) {
                    if ((params.isEmpty())) {
                        params.add(it)
                    } else {
                        val cmd = params.pop()
                        if (!isOperation(cmd)) {
                            params.clear()
                        } else {
                            val result = executeSingle(cmd as String, *params.map { it as Number }.toTypedArray())
                            params.clear()
                            params.add(it)
                            params.add(result)
                        }
                    }
                } else if (!params.isEmpty()) params.add(it)
            }

            val cmd = params.pop()
            return executeSingle(cmd as String, *params.map { it as Number }.toTypedArray())
        } catch (e: Exception) {
            throw WrongExpression()
        }
    }

    /**
     * Executes expression from string
     * example : "2+12 / 12 + (25-1) / 10"
     *
     *
     * @throws FunctionNotFound if requested function is missed
     */
    @Throws(FunctionNotFound::class)
    fun executeExpression(expression: String): Number {
        return 1
    }

    class FunctionNotFound : Exception("Function not found")
    class WrongExpression : Exception("Your expression is wrong")
}


fun main(args: Array<String>) {
    val calc = Calculator()

//   println(calc.executeSingle(Calculator.OP_ADD, 2, 3))
//   println(calc.executeSingle(Calculator.OP_SUB, 12, 3,2))
//   println(calc.executeSingle(Calculator.OP_MUL, 2, 3))
//   println(calc.executeSingle(Calculator.OP_DIV, 10, 5))
//
//    calc.registerCustomFunction("TEST",{nums->nums.map{it*it}.reduce{a,b->a+b}})
//    println("test pow 2 =  ${calc.executeSingle("TEST",3,2,5)}")

    println(calc.executeExpression(1, 2, Calculator.OP_DIV, 10, 5, Calculator.OP_ADD, 12))
    println(calc.executeExpression(1, 2, 12))
}


