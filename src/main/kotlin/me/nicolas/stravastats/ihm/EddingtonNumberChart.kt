package me.nicolas.stravastats.ihm

import javafx.collections.FXCollections
import javafx.event.EventHandler
import javafx.event.EventTarget
import javafx.scene.Cursor
import javafx.scene.Node
import javafx.scene.chart.*
import javafx.scene.control.Label
import javafx.scene.input.MouseEvent
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.shape.Line
import tornadofx.attachTo
import tornadofx.series
import kotlin.math.abs
import kotlin.math.roundToInt


internal fun EventTarget.eddingtonNumberChart(
    activeDaysList: Map<String, Int>,
    op: EddingtonNumberChart.() -> Unit = {}
) = EddingtonNumberChart(activeDaysList).attachTo(this, op)

internal class EddingtonNumberChart(activeByDaysMap: Map<String, Int>) : StackPane() {

    private val eddingtonBar: BarChart<String, Number>

    private val eddingtonScatter: LineChart<String, Number>

    private val detailsWindow: AnchorPane


    init {
        // counts = number of time we reach a distance
        val counts: MutableList<Int> = // init to 0
            activeByDaysMap
                .maxOf { entry -> entry.value }
                .let { List(it) { 0 }.toMutableList() }

        activeByDaysMap.forEach { entry: Map.Entry<String, Int> ->
            for (day in entry.value downTo 1) {
                counts[day - 1] += 1
            }
        }

        var eddingtonNumber = 0
        for (day in counts.size downTo 1) {
            if (counts[day - 1] >= day) {
                eddingtonNumber = day
                break
            }
        }

        eddingtonBar = createEddingtonBar(counts, eddingtonNumber)
        eddingtonScatter = createEddingtonScatter(counts)

        detailsWindow = AnchorPane()
        bindMouseEvents(eddingtonBar, 1.5)

        this.children.addAll(eddingtonBar)
    }

    private fun createEddingtonBar(counts: List<Int>, eddingtonNumber: Int): BarChart<String, Number> {
        val eddingtonBar = BarChart(createXAxis(), createYAxis(counts.maxOf { it }))
        val barElements = FXCollections.observableArrayList<XYChart.Data<String, Number>>((counts.indices + 1).map {
            XYChart.Data(it.toString(), counts[it])
        })
        eddingtonBar.title = "Eddington number : $eddingtonNumber"
        eddingtonBar.series("Eddington number", barElements)
        setDefaultChartProperties(eddingtonBar)

        eddingtonBar.isAlternativeColumnFillVisible = false
        eddingtonBar.verticalGridLinesVisible = false

        //eddingtonBar.isHorizontalGridLinesVisible = true

        for (node in eddingtonBar.lookupAll(".default-color0.chart-bar")) {
            node.style = "-fx-bar-fill: blue;"
        }
        eddingtonBar.barGap = -4.0

        return eddingtonBar
    }

    private fun createEddingtonScatter(counts: List<Int>): LineChart<String, Number> {

        val eddingtonScatter = object : LineChart<String, Number>(createXAxis(), createYAxis(counts.maxOf { it })) {
            init {
                // hide axis in constructor, since not public
                chartChildren.remove(xAxis)
                chartChildren.remove(yAxis)
            }
        }
        val scatterElements = FXCollections.observableArrayList<XYChart.Data<String, Number>>((counts.indices + 1).map {
            XYChart.Data(it.toString(), it)
        })
        eddingtonScatter.series("", scatterElements)
        setDefaultChartProperties(eddingtonScatter)
        configureEddingtonScatter(eddingtonScatter)

        return eddingtonScatter
    }

    private fun configureEddingtonScatter(chart: LineChart<String, Number>) {
        chart.isAlternativeRowFillVisible = false
        chart.isAlternativeColumnFillVisible = false
        chart.isHorizontalGridLinesVisible = false
        chart.verticalGridLinesVisible = false
        chart.createSymbols = false
        chart.isMouseTransparent = true

        val contentBackground = chart.lookup(".chart-content").lookup(".chart-plot-background")
        contentBackground.style = "-fx-background-color: transparent;"
        val seriesLine = chart.lookup(".chart-series-line")
        seriesLine.style = "-fx-stroke: orange; -fx-stroke-width: 1.5;"
    }

    private fun createYAxis(upperBound: Int): NumberAxis {
        val axis = NumberAxis(0.0, upperBound.toDouble(), 50.0)
        axis.minWidth = 35.0
        axis.prefWidth = 35.0
        axis.maxWidth = 35.0
        axis.minHeight = 35.0
        axis.prefHeight = 35.0
        axis.maxHeight = 35.0

        axis.minorTickCount = 10
        axis.tickLabelFormatter = object : NumberAxis.DefaultFormatter(axis) {
            override fun toString(number: Number): String {
                return String.format("%d", number.toInt())
            }
        }
        return axis
    }

    private fun createXAxis(): CategoryAxis {
        val axis = CategoryAxis()
        axis.minWidth = 35.0
        axis.prefWidth = 35.0
        axis.maxWidth = 35.0
        axis.minHeight = 35.0
        axis.prefHeight = 35.0
        axis.maxHeight = 35.0

        return axis
    }

    private fun setDefaultChartProperties(chart: XYChart<String, Number>) {
        chart.isLegendVisible = false
        chart.animated = false
    }


    private fun bindMouseEvents(baseChart: BarChart<String, Number>, strokeWidth: Double) {

        val detailsPopup = DetailsPopup()
        children.add(detailsWindow)
        detailsWindow.children.add(detailsPopup)
        detailsWindow.prefHeightProperty().bind(heightProperty())
        detailsWindow.prefWidthProperty().bind(widthProperty())
        detailsWindow.isMouseTransparent = true

        onMouseMoved = null
        isMouseTransparent = false

        val xAxis = baseChart.xAxis
        val yAxis = baseChart.yAxis

        val xLine = Line()
        val yLine = Line()
        yLine.fill = Color.GRAY
        xLine.fill = Color.GRAY
        yLine.strokeWidth = strokeWidth / 2
        xLine.strokeWidth = strokeWidth / 2
        xLine.isVisible = false
        yLine.isVisible = false

        val chartBackground = baseChart.lookup(".chart-plot-background")
        for (node in chartBackground.parent.childrenUnmodifiable) {
            if (node !== chartBackground && node !== xAxis && node !== yAxis) {
                node.isMouseTransparent = true
            }
        }
        chartBackground.cursor = Cursor.CROSSHAIR
        chartBackground.onMouseEntered = EventHandler { event: MouseEvent ->
            chartBackground.onMouseMoved.handle(event)
            detailsPopup.isVisible = true
            xLine.isVisible = true
            yLine.isVisible = true
            detailsWindow.children.addAll(xLine, yLine)
        }
        chartBackground.onMouseExited = EventHandler { event: MouseEvent ->
            detailsPopup.isVisible = false
            xLine.isVisible = false
            yLine.isVisible = false
            detailsWindow.children.removeAll(xLine, yLine)
        }
        chartBackground.onMouseMoved = EventHandler { event: MouseEvent ->
            val x = event.x + chartBackground.layoutX
            val y = event.y + chartBackground.layoutY
            xLine.startX = 10.0
            xLine.endX = detailsWindow.width - 10
            xLine.startY = y + 5
            xLine.endY = y + 5
            yLine.startX = x + 5
            yLine.endX = x + 5
            yLine.startY = 10.0
            yLine.endY = detailsWindow.height - 10
            detailsPopup.showChartDescription(event)

            if (y + detailsPopup.height + 10 < height) {
                AnchorPane.setTopAnchor(detailsPopup, y + 10)
            } else {
                AnchorPane.setTopAnchor(detailsPopup, y - 10 - detailsPopup.height)
            }

            if (x + detailsPopup.width + 10 < width) {
                AnchorPane.setLeftAnchor(detailsPopup, x + 10)
            } else {
                AnchorPane.setLeftAnchor(detailsPopup, x - 10 - detailsPopup.width)
            }
        }
    }

    private inner class DetailsPopup : VBox() {

        init {
            style =
                "-fx-border-width: 1px; -fx-padding: 5 5 5 5px; -fx-border-color: gray; -fx-background-color: whitesmoke;"
            isVisible = false
        }

        fun showChartDescription(event: MouseEvent) {
            children.clear()
            val xValue: String? = eddingtonBar.xAxis.getValueForDisplay(event.x)
            val baseChartPopupRow = buildPopupRow(event, xValue, eddingtonBar)
            if (baseChartPopupRow != null) {
                children.add(baseChartPopupRow)
            }
        }

        private fun buildPopupRow(event: MouseEvent, xValue: String?, lineChart: BarChart<String, Number>): HBox? {
            val seriesName = Label(lineChart.yAxis.label)
            seriesName.textFill = Color.BLUE
            val yValueForChart = getYValueForX(eddingtonBar, xValue) ?: return null
            val yValueLower: Number = normalizeYValue(eddingtonBar, event.y - 10).roundToInt()
            val yValueUpper: Number = normalizeYValue(eddingtonBar, event.y + 10).roundToInt()
            val yValueUnderMouse: Number = eddingtonBar.yAxis.getValueForDisplay(event.y).toDouble().roundToInt()

            // make series name bold when mouse is near given chart's line
            if (isMouseNearLine(
                    yValueForChart,
                    yValueUnderMouse,
                    abs(yValueLower.toDouble() - yValueUpper.toDouble())
                )
            ) {
                seriesName.style = "-fx-font-weight: bold"
            }
            return HBox(10.0, seriesName, Label("[$yValueForChart]"))
        }

        private fun normalizeYValue(lineChart: BarChart<String, Number>, value: Double): Double {
            return lineChart.yAxis.getValueForDisplay(value) as Double
        }

        private fun isMouseNearLine(realYValue: Number, yValueUnderMouse: Number, tolerance: Double): Boolean {
            return abs(yValueUnderMouse.toDouble() - realYValue.toDouble()) < tolerance
        }

        fun getYValueForX(chart: BarChart<String, Number>, xValue: String?): Number? {
            val dataList =
                chart.data[0].data //(chart.data[0] as Series<String, Number>).data as List<XYChart.Data<String, Number>>
            for (data in dataList) {
                if (data.xValue == xValue) {
                    return data.yValue as Number
                }
            }
            return null
        }
    }

}