package me.nicolas.stravastats.ihm

import javafx.collections.FXCollections
import javafx.event.EventHandler
import javafx.event.EventTarget
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

    private val yAxisWidth = 25.0

    private val counts: MutableList<Int> = activeByDaysMap
        .maxOf { entry -> entry.value }
        .let { List(it) { 0 }.toMutableList() }

    init {
        // counts = number of time we reach a distance
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
        eddingtonScatter = createEddingtonScatter(counts, eddingtonNumber)
        detailsWindow = AnchorPane()

        bindMouseEvents()

        this.children.addAll(eddingtonBar, eddingtonScatter)
        rebuildChart()
    }

    private fun createEddingtonBar(counts: List<Int>, eddingtonNumber: Int): BarChart<String, Number> {
        val eddingtonBar = BarChart(createXAxis(), createYAxis(counts.maxOf { it }))
        //eddingtonBar.title = "Eddington number : $eddingtonNumber"

        val barElements = FXCollections.observableArrayList<XYChart.Data<String, Number>>((counts.indices + 1).map {
            XYChart.Data(it.toString(), counts[it])
        })
        eddingtonBar.series("Eddington number", barElements)

        setDefaultChartProperties(eddingtonBar)
        eddingtonBar.isAlternativeColumnFillVisible = false
        eddingtonBar.verticalGridLinesVisible = false

        for (node in eddingtonBar.lookupAll(".default-color0.chart-bar")) {
            node.style = "-fx-bar-fill: blue;"
        }
        eddingtonBar.barGap = -4.0

        return eddingtonBar
    }

    private fun createEddingtonScatter(counts: List<Int>, eddingtonNumber: Int): LineChart<String, Number> {

        val eddingtonScatter = object : LineChart<String, Number>(createXAxis(), createYAxis(counts.maxOf { it })) {
            init {
                // hide axis in constructor, since not public
                chartChildren.remove(xAxis)
                chartChildren.remove(yAxis)
            }
        }
        //eddingtonScatter.title = "Eddington number : $eddingtonNumber"

        val scatterElements = FXCollections.observableArrayList<XYChart.Data<String, Number>>((counts.indices + 1).map {
            XYChart.Data(it.toString(), it)
        })
        eddingtonScatter.series("Eddington", scatterElements)

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

    private fun setDefaultChartProperties(chart: XYChart<String, Number>) {
        chart.isLegendVisible = false
        chart.animated = false
    }

    private fun rebuildChart() {
        children.clear()
        children.add(resizeEddigtonBarChart())
        children.add(resizeEddingtonScatter())
        children.add(detailsWindow)
    }

    private fun resizeEddigtonBarChart(): Node {

        val hBox = HBox(eddingtonBar)
        hBox.alignment = Pos.CENTER_LEFT
        hBox.minHeightProperty().bind(heightProperty())
        hBox.prefHeightProperty().bind(heightProperty())
        hBox.maxHeightProperty().bind(heightProperty())

        hBox.minWidthProperty().bind(widthProperty())
        hBox.prefWidthProperty().bind(widthProperty())
        hBox.maxWidthProperty().bind(widthProperty())

        eddingtonBar.minWidthProperty().bind(widthProperty())//.subtract(yAxisWidth + yAxisSeparation))
        eddingtonBar.prefWidthProperty().bind(widthProperty())//.subtract(yAxisWidth + yAxisSeparation))
        eddingtonBar.maxWidthProperty().bind(widthProperty())//.subtract(yAxisWidth + yAxisSeparation))

        return eddingtonBar
    }

    private fun resizeEddingtonScatter(): Node {

        val hBox = HBox(eddingtonScatter)
        hBox.alignment = Pos.CENTER_LEFT
        hBox.minHeightProperty().bind(heightProperty())
        hBox.prefHeightProperty().bind(heightProperty())
        hBox.maxHeightProperty().bind(heightProperty())

        hBox.minWidthProperty().bind(widthProperty())
        hBox.prefWidthProperty().bind(widthProperty())
        hBox.maxWidthProperty().bind(widthProperty())

        hBox.isMouseTransparent = true

        eddingtonScatter.minWidthProperty().bind(widthProperty())//.subtract(yAxisWidth + yAxisSeparation))
        eddingtonScatter.prefWidthProperty().bind(widthProperty())//.subtract(yAxisWidth + yAxisSeparation))
        eddingtonScatter.maxWidthProperty().bind(widthProperty())//.subtract(yAxisWidth + yAxisSeparation))

        //eddingtonScatter.translateXProperty().bind(eddingtonBar.yAxis.widthProperty())
        //eddingtonScatter.yAxis.translateX = (yAxisWidth)

        return hBox
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

        val xAxis = eddingtonBar.xAxis
        val yAxis = eddingtonBar.yAxis

        val xLine = Line()
        val yLine = Line()
        yLine.fill = Color.GRAY
        xLine.fill = Color.GRAY
        yLine.strokeWidth = strokeWidth / 2
        xLine.strokeWidth = strokeWidth / 2
        xLine.isVisible = false
        yLine.isVisible = false

        val chartBackground = eddingtonBar.lookup(".chart-plot-background")

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

            if (eddingtonBar.xAxis.getValueForDisplay(event.x) != null) {

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
            val xValue: String = eddingtonBar.xAxis.getValueForDisplay(displayPosition)
            val baseChartPopupRow = buildPopupRow(xValue, yValue, eddingtonBar)
            if (baseChartPopupRow != null) {
                children.add(baseChartPopupRow)
            }
        }

        private fun buildPopupRow(xValue: String, yValue: Double, lineChart: BarChart<String, Number>): HBox? {
            val seriesName = Label(lineChart.yAxis.label)
            seriesName.textFill = Color.BLUE
            val yValueForChart = getYValueForX(eddingtonBar, xValue) ?: return null
            val yValueLower: Number = normalizeYValue(eddingtonBar, yValue - 10).roundToInt()
            val yValueUpper: Number = normalizeYValue(eddingtonBar, yValue + 10).roundToInt()
            val yValueUnderMouse: Number = eddingtonBar.yAxis.getValueForDisplay(yValue).toDouble().roundToInt()

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
            val label = "On ${counts[index] + 1} days you covered at least ${index + 1} km."

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