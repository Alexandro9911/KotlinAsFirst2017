@file:Suppress("UNUSED_PARAMETER")
package lesson6.task1

import lesson1.task1.sqr

/**
 * Точка на плоскости
 */
data class Point(val x: Double, val y: Double) {
    /**
     * Пример
     *
     * Рассчитать (по известной формуле) расстояние между двумя точками
     */
    fun distance(other: Point): Double = Math.sqrt(sqr(x - other.x) + sqr(y - other.y))
}

/**
 * Треугольник, заданный тремя точками (a, b, c, см. constructor ниже).
 * Эти три точки хранятся в множестве points, их порядок не имеет значения.
 */
class Triangle private constructor(private val points: Set<Point>) {

    private val pointList = points.toList()

    val a: Point get() = pointList[0]

    val b: Point get() = pointList[1]

    val c: Point get() = pointList[2]

    constructor(a: Point, b: Point, c: Point): this(linkedSetOf(a, b, c))
    /**
     * Пример: полупериметр
     */
    fun halfPerimeter() = (a.distance(b) + b.distance(c) + c.distance(a)) / 2.0

    /**
     * Пример: площадь
     */
    fun area(): Double {
        val p = halfPerimeter()
        return Math.sqrt(p * (p - a.distance(b)) * (p - b.distance(c)) * (p - c.distance(a)))
    }

    /**
     * Пример: треугольник содержит точку
     */
    fun contains(p: Point): Boolean {
        val abp = Triangle(a, b, p)
        val bcp = Triangle(b, c, p)
        val cap = Triangle(c, a, p)
        return abp.area() + bcp.area() + cap.area() <= area()
    }

    override fun equals(other: Any?) = other is Triangle && points == other.points

    override fun hashCode() = points.hashCode()

    override fun toString() = "Triangle(a = $a, b = $b, c = $c)"
}

/**
 * Окружность с заданным центром и радиусом
 */
data class Circle(val center: Point, val radius: Double) {
    /**
     * Простая
     *
     * Рассчитать расстояние между двумя окружностями.
     * Расстояние между непересекающимися окружностями рассчитывается как
     * расстояние между их центрами минус сумма их радиусов.
     * Расстояние между пересекающимися окружностями считать равным 0.0.
     */
    fun distance(other: Circle): Double {
        if ((center.distance(other.center) - (radius + other.radius)) > 0) return center.distance(other.center) -
                (radius + other.radius)
        return 0.0
    }


    /**
     * Тривиальная
     *
     * Вернуть true, если и только если окружность содержит данную точку НА себе или ВНУТРИ себя
     */
    fun contains(p: Point): Boolean = Math.sqrt(sqr(p.x - center.x) + sqr(p.y - center.y)) <= radius
}

/**
 * Отрезок между двумя точками
 */
data class Segment(val begin: Point, val end: Point) {
    override fun equals(other: Any?) =
            other is Segment && (begin == other.begin && end == other.end || end == other.begin && begin == other.end)

    override fun hashCode() =
            begin.hashCode() + end.hashCode()
}

/**
 * Средняя
 *
 * Дано множество точек. Вернуть отрезок, соединяющий две наиболее удалённые из них.
 * Если в множестве менее двух точек, бросить IllegalArgumentException
 */
fun diameter(vararg points: Point): Segment {
    if (points.size < 2) throw IllegalArgumentException()
    var dmax = points[0].distance(points[0])
    var seg = Segment(points[0], points[0])
    for (i in 0..points.size - 2) {
        for (e in i + 1..points.size - 1) {
            if (points[i].distance(points[e]) > dmax) {
                dmax = points[i].distance(points[e])
                seg = Segment(points[i], points[e])
            }
        }
    }
    return seg
}


/**
 * Простая
 *
 * Построить окружность по её диаметру, заданному двумя точками
 * Центр её должен находиться посередине между точками, а радиус составлять половину расстояния между ними
 */
fun circleByDiameter(diameter: Segment): Circle {
    val center = Point((diameter.begin.x + diameter.end.x) / 2, (diameter.begin.y + diameter.end.y) / 2)
    val radius = center.distance(diameter.begin)
    return Circle(center, radius)
}
/**
 * Прямая, заданная точкой point и углом наклона angle (в радианах) по отношению к оси X.
 * Уравнение прямой: (y - point.y) * cos(angle) = (x - point.x) * sin(angle)
 * или: y * cos(angle) = x * sin(angle) + b, где b = point.y * cos(angle) - point.x * sin(angle).
 * Угол наклона обязан находиться в диапазоне от 0 (включительно) до PI (исключительно).
 */
class Line private constructor(val b: Double, val angle: Double) {
    init {
        assert(angle >= 0 && angle < Math.PI) { "Incorrect line angle: $angle" }
    }

    constructor(point: Point, angle: Double): this(point.y * Math.cos(angle) - point.x * Math.sin(angle), angle)

    /**
     * Средняя
     *
     * Найти точку пересечения с другой линией.
     * Для этого необходимо составить и решить систему из двух уравнений (каждое для своей прямой)
     */
    fun crossPoint(other: Line): Point {
        if (this.angle == Math.PI / 2) {
            val x = -this.b
            val y = (-this.b) * Math.tan(other.angle) + other.b / Math.cos(other.angle)
            return Point(x, y)
        }
        if (other.angle == Math.PI / 2) {
            val x = -other.b
            val y = (-other.b) * Math.tan(this.angle) + this.b / Math.cos(this.angle)
            return Point(x, y)
        }
        val x = -(this.b / Math.cos(this.angle) - other.b / Math.cos(other.angle)) /
                (Math.tan(this.angle) - Math.tan(other.angle))
        val y = (-(this.b / Math.cos(this.angle) - other.b / Math.cos(other.angle)) /
                (Math.tan(this.angle) - Math.tan(other.angle))) *
                Math.tan(this.angle) + this.b / Math.cos(this.angle)
        return Point(x, y)
    }

    override fun equals(other: Any?) = other is Line && angle == other.angle && b == other.b

    override fun hashCode(): Int {
        var result = b.hashCode()
        result = 31 * result + angle.hashCode()
        return result
    }

    override fun toString() = "Line(${Math.cos(angle)} * y = ${Math.sin(angle)} * x + $b)"
}

/**
 * Средняя
 *
 * Построить прямую по отрезку
 */
fun lineBySegment(s: Segment): Line {
    var a = Math.atan2((s.end.y - s.begin.y), (s.end.x - s.begin.x))
    if (a < 0) a += Math.PI
    if (a >= Math.PI) a -= Math.PI
    return Line(s.begin, a)

}

/**
 * Средняя
 *
 * Построить прямую по двум точкам
 */
fun lineByPoints(a: Point, b: Point): Line = lineBySegment(Segment(a, b))

/**
 * Сложная
 *
 * Построить серединный перпендикуляр по отрезку или по двум точкам
 */
fun bisectorByPoints(a: Point, b: Point): Line {
    val midSeg = Point((a.x + b.x) / 2, (a.y + b.y) / 2)
    var atg = lineByPoints(a, b).angle
    if (lineByPoints(a, b).angle <= Math.PI / 2) atg += Math.PI / 2
    else atg = lineByPoints(a, b).angle - Math.PI / 2
    if (atg == Math.PI) atg = 0.0
    return Line(midSeg, atg)
}

/**
 * Средняя
 *
 * Задан список из n окружностей на плоскости. Найти пару наименее удалённых из них.
 * Если в списке менее двух окружностей, бросить IllegalArgumentException
 */
fun findNearestCirclePair(vararg circles: Circle): Pair<Circle, Circle> = TODO()

/**
 * Сложная
 *
 * Дано три различные точки. Построить окружность, проходящую через них
 * (все три точки должны лежать НА, а не ВНУТРИ, окружности).
 * Описание алгоритмов см. в Интернете
 * (построить окружность по трём точкам, или
 * построить окружность, описанную вокруг треугольника - эквивалентная задача).
 */
fun circleByThreePoints(a: Point, b: Point, c: Point): Circle =
        Circle(bisectorByPoints(a, b).crossPoint(bisectorByPoints(b, c)),
                bisectorByPoints(a, b).crossPoint(bisectorByPoints(b, c)).distance(a))

/**
 * Очень сложная
 *
 * Дано множество точек на плоскости. Найти круг минимального радиуса,
 * содержащий все эти точки. Если множество пустое, бросить IllegalArgumentException.
 * Если множество содержит одну точку, вернуть круг нулевого радиуса с центром в данной точке.
 *
 * Примечание: в зависимости от ситуации, такая окружность может либо проходить через какие-либо
 * три точки данного множества, либо иметь своим диаметром отрезок,
 * соединяющий две самые удалённые точки в данном множестве.
 */
fun minContainingCircle(vararg points: Point): Circle {
    var answ = Circle(Point(0.0, 0.0), 1000000000000.0)
    var answD = Circle(Point(0.0, 0.0), 1000000000000.0)
    if (points.isEmpty()) throw IllegalArgumentException()
    if (points.size == 1) return Circle(points[0], 0.0)
    if (points.size == 2)  return circleByDiameter(Segment(points[0], points[1]))
    for (m in points) {
        val allWithOutM = points.filter { it != m }
        for (n in allWithOutM) {
            var dmax = points[0].distance(points[0])
            var seg = Segment(points[0], points[0])
            for (i in 0..points.size - 2) {
                for (e in i + 1..points.size - 1) {
                    if (points[i].distance(points[e]) > dmax) {
                        dmax = points[i].distance(points[e])
                        seg = Segment(points[i], points[e])
                    }
                }
            }
            val diametr = seg
            val center = Point((diametr.begin.x + diametr.end.x) / 2, (diametr.begin.y + diametr.end.y) / 2)
            val rad = center.distance(diametr.begin)
            if (Circle(center, rad).radius < answD.radius && points.all { Circle(center, rad).contains(it) })
                answD = Circle(center, rad)
        }
    }
    for (a in points) {
        val allWithOutA = points.filter { it != a }
        for (b in allWithOutA) {
            val allWithOutAandB = points.filter { it != a && it != b }
            for (c in allWithOutAandB) {
                if (circleByThreePoints(a, b, c).radius < answ.radius &&
                        points.all { circleByThreePoints(a, b, c).contains(it) })
                    answ = circleByThreePoints(a, b, c)
            }
        }
    }
    if (answ.radius < answD.radius) return answ
    else return answD
}

