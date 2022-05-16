package me.nicolas.stravastats.ihm.chart

import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.event.EventHandler
import javafx.event.EventTarget
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Cursor
import javafx.scene.Node
import javafx.scene.chart.*
import javafx.scene.control.Label
import javafx.scene.input.MouseEvent
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.HBox
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.shape.Line
import javafx.scene.text.Text
import tornadofx.attachTo
import tornadofx.series
import kotlin.math.abs
import kotlin.math.roundToInt


internal fun EventTarget.eddingtonNumberChart(
    activeDaysList: Map<String, Int>,
    op: EddingtonNumberChart.() -> Unit = {}
) = EddingtonNumberChart(activeDaysList).attachTo(this, op)

internal class EddingtonNumberChart(activeByDaysMap: Map<String, Int>) : StackPane() {

    private val eddingtonBars: BarChart<String, Number>
    private val eddingtonBar: BarChart<String, Number>
    private val eddingtonScatter: LineChart<String, Number>
    private val detailsWindow: AnchorPane
    private val titleWindow: VBox

    private val yAxisWidth = 25.0

    private val counts: MutableList<Int>

    private var eddingtonNumber: Int = 0

    init {
        var upperBound = 0

        if (activeByDaysMap.isEmpty()) {
            counts = mutableListOf()
        } else {
            counts = IntArray(activeByDaysMap.maxOf { entry -> entry.value }) { 0 }.toMutableList()
            // counts = number of time we reach a distance
            activeByDaysMap.forEach { entry: Map.Entry<String, Int> ->
                for (day in entry.value downTo 1) {
                    counts[day - 1] += 1
                }
            }

            for (day in counts.size downTo 1) {
                if (counts[day - 1] >= day) {
                    eddingtonNumber = day
                    break
                }
            }
            upperBound = counts[0]
        }

        eddingtonBars = createEddingtonBars(counts, upperBound)
        eddingtonBar = createEddingtonBar(counts, upperBound, eddingtonNumber)
        eddingtonScatter = createEddingtonScatter(counts, upperBound)

        titleWindow = buildTitle("Eddington number : $eddingtonNumber km")
        detailsWindow = AnchorPane()

        bindMouseEvents()

        this.children.addAll(eddingtonBars, eddingtonBar, eddingtonScatter)
        rebuildChart()
    }

    private fun createEddingtonBars(counts: List<Int>, upperBound: Int): BarChart<String, Number> {
        val barChart = BarChart(createXAxis(), createYAxis(upperBound))

        if (counts.isNotEmpty()) {
            val barElements = FXCollections.observableArrayList<XYChart.Data<String, Number>>((counts.indices + 1).map {
                XYChart.Data(it.toString(), counts[it])
            })
            barChart.series("Eddington number : ", barElements)
        }
        setDefaultChartProperties(barChart)
        barChart.isAlternativeColumnFillVisible = false
        barChart.verticalGridLinesVisible = false

        for (node in barChart.lookupAll(".default-color0.chart-bar")) {
            node.style = "-fx-bar-fill: blue;"
        }
        barChart.barGap = -4.0

        return barChart
    }

    private fun createEddingtonBar(counts: List<Int>, upperBound: Int, eddingtonNumber: Int): BarChart<String, Number> {
        val barChart = BarChart(createXAxis(), createYAxis(upperBound))

        if (counts.isNotEmpty()) {
            val barElements = FXCollections.observableArrayList<XYChart.Data<String, Number>>((counts.indices + 1).map {
                if ((it + 1) == eddingtonNumber) {
                    XYChart.Data(it.toString(), counts[it])
                } else {
                    XYChart.Data(it.toString(), 0)
                }
            })
            barChart.series("Eddington number : ", barElements)
        }
        setDefaultChartProperties(barChart)
        barChart.isAlternativeColumnFillVisible = false
        barChart.verticalGridLinesVisible = false

        for (node in barChart.lookupAll(".default-color0.chart-bar")) {
            node.style = "-fx-bar-fill: orange;"
        }
        val contentBackground = barChart.lookup(".chart-content").lookup(".chart-plot-background")
        contentBackground.style = "-fx-background-color: transparent;"

        barChart.barGap = -4.0
        barChart.isAlternativeRowFillVisible = false
        barChart.isAlternativeColumnFillVisible = false
        barChart.isHorizontalGridLinesVisible = false
        barChart.verticalGridLinesVisible = false
        barChart.isMouseTransparent = true

        return barChart
    }

    private fun createEddingtonScatter(counts: List<Int>, upperBound: Int): LineChart<String, Number> {
        val lineChart = object : LineChart<String, Number>(createXAxis(), createYAxis(upperBound)) {
            init {
                // hide axis in constructor, since not public
                chartChildren.remove(xAxis)
                chartChildren.remove(yAxis)
            }
        }
        val scatterElements = FXCollections.observableArrayList<XYChart.Data<String, Number>>((counts.indices + 1).map {
            XYChart.Data(it.toString(), it)
        })
        lineChart.series("Eddington", scatterElements)

        setDefaultChartProperties(lineChart)

        lineChart.isAlternativeRowFillVisible = false
        lineChart.isAlternativeColumnFillVisible = false
        lineChart.isHorizontalGridLinesVisible = false
        lineChart.verticalGridLinesVisible = false
        lineChart.createSymbols = false
        lineChart.isMouseTransparent = true

        val contentBackground = lineChart.lookup(".chart-content").lookup(".chart-plot-background")
        contentBackground.style = "-fx-background-color: transparent;"
        val seriesLine = lineChart.lookup(".chart-series-line")
        seriesLine.style = "-fx-stroke: orange; -fx-stroke-width: 1.5;"

        return lineChart
    }

    private fun createYAxis(upperBound: Int): NumberAxis {
        val axis = NumberAxis(0.0, upperBound.toDouble(), 50.0)
        axis.minWidth = yAxisWidth
        axis.prefWidth = yAxisWidth
        axis.maxWidth = yAxisWidth
        axis.minHeight = yAxisWidth
        axis.prefHeight = yAxisWidth
        axis.maxHeight = yAxisWidth

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
        axis.minWidth = yAxisWidth
        axis.prefWidth = yAxisWidth
        axis.maxWidth = yAxisWidth
        axis.minHeight = yAxisWidth
        axis.prefHeight = yAxisWidth
        axis.maxHeight = yAxisWidth

        return axis
    }

    private fun buildTitle(title: String): VBox {
        val text = Text(title)
        val vBox = VBox(text)
        vBox.alignment = Pos.TOP_CENTER
        vBox.isMouseTransparent = true
        vBox.padding = Insets(20.0)
        vBox.style = "-fx-fill-width: false; -fx-background-color: transparent;"
        vBox.focusTraversableProperty().bind(Platform.accessibilityActiveProperty())
        text.styleClass.add("chart-title")

        return vBox
    }

    private fun setDefaultChartProperties(chart: XYChart<String, Number>) {
        chart.isLegendVisible = false
        chart.animated = false
    }

    private fun rebuildChart() {
        children.clear()
        children.add(resizeXYChart(eddingtonBars))
        children.add(resizeXYChart(eddingtonBar))
        children.add(resizeXYChart(eddingtonScatter).apply { isMouseTransparent = true })
        children.add(detailsWindow)
        children.add(titleWindow)
    }

    private fun resizeXYChart(chart: XYChart<String, Number>): Node {

        val hBox = HBox(chart)
        hBox.alignment = Pos.CENTER_LEFT
        hBox.minHeightProperty().bind(heightProperty())
        hBox.prefHeightProperty().bind(heightProperty())
        hBox.maxHeightProperty().bind(heightProperty())

        hBox.minWidthProperty().bind(widthProperty())
        hBox.prefWidthProperty().bind(widthProperty())
        hBox.maxWidthProperty().bind(widthProperty())

        chart.minWidthProperty().bind(widthProperty())
        chart.prefWidthProperty().bind(widthProperty())
        chart.maxWidthProperty().bind(widthProperty())

        return chart
    }

    private fun bindMouseEvents(strokeWidth: Double = 1.5) {

        val detailsPopup = DetailsPopup()
        children.add(detailsWindow)
        detailsWindow.children.add(detailsPopup)
        detailsWindow.prefHeightProperty().bind(heightProperty())
        detailsWindow.prefWidthProperty().bind(widthProperty())

        detailsWindow.isMouseTransparent = true

        onMouseMoved = null
        isMouseTransparent = false

        val xAxis = eddingtonBars.xAxis
        val yAxis = eddingtonBars.yAxis

        val xLine = Line()
        val yLine = Line()
        yLine.fill = Color.GRAY
        xLine.fill = Color.GRAY
        yLine.strokeWidth = strokeWidth / 2
        xLine.strokeWidth = strokeWidth / 2
        xLine.isVisible = false
        yLine.isVisible = false

        val chartBackground = eddingtonBars.lookup(".chart-plot-background")

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
        chartBackground.onMouseExited = EventHandler {
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

            if (eddingtonBars.xAxis.getValueForDisplay(event.x) != null) {

                detailsPopup.showChartDescription(event.x, event.y)

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
                detailsPopup.isVisible = true
            } else {
                detailsPopup.isVisible = false
            }
        }
    }

    private inner class DetailsPopup : VBox() {

        init {
            style =
                "-fx-border-width: 1px; -fx-padding: 5 5 5 5px; -fx-border-color: gray; -fx-background-color: whitesmoke;"
            isVisible = false
        }

        fun showChartDescription(displayPosition: Double, yValue: Double) {
            children.clear()
            val xValue: String = eddingtonBars.xAxis.getValueForDisplay(displayPosition)
            val baseChartPopupRow = buildPopupRow(xValue, yValue, eddingtonBars)
            if (baseChartPopupRow != null) {
                children.add(baseChartPopupRow)
            }
        }

        private fun buildPopupRow(xValue: String, yValue: Double, lineChart: BarChart<String, Number>): HBox? {
            val seriesName = Label(lineChart.yAxis.label)
            seriesName.textFill = Color.BLUE
            val yValueForChart = getYValueForX(eddingtonBars, xValue) ?: return null
            val yValueLower: Number = normalizeYValue(eddingtonBars, yValue - 10).roundToInt()
            val yValueUpper: Number = normalizeYValue(eddingtonBars, yValue + 10).roundToInt()
            val yValueUnderMouse: Number = eddingtonBars.yAxis.getValueForDisplay(yValue).toDouble().roundToInt()

            // make series name bold when mouse is near given chart's line
            if (isMouseNearLine(
                    yValueForChart,
                    yValueUnderMouse,
                    abs(yValueLower.toDouble() - yValueUpper.toDouble())
                )
            ) {
                seriesName.style = "-fx-font-weight: bold"
            }
            val index = xValue.toInt()
            val label =
                "Your Eddington number is $eddingtonNumber. \nOn ${counts[index] + 1} days you covered at least ${index + 1} km."

            return HBox(10.0, seriesName, Label(label))
        }

        private fun normalizeYValue(lineChart: BarChart<String, Number>, value: Double): Double {
            return lineChart.yAxis.getValueForDisplay(value) as Double
        }

        private fun isMouseNearLine(realYValue: Number, yValueUnderMouse: Number, tolerance: Double): Boolean {
            return abs(yValueUnderMouse.toDouble() - realYValue.toDouble()) < tolerance
        }

        fun getYValueForX(chart: BarChart<String, Number>, xValue: String?): Number? {
            for (data in chart.data[0].data) {
                if (data.xValue == xValue) {
                    return data.yValue as Number
                }
            }
            return null
        }
    }
}