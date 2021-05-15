package me.nicolas.stravastats.ihm

import javafx.application.Platform
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Cursor
import javafx.scene.Node
import javafx.scene.chart.CategoryAxis
import javafx.scene.chart.LineChart
import javafx.scene.chart.NumberAxis
import javafx.scene.chart.XYChart.Series
import javafx.scene.control.CheckBox
import javafx.scene.control.Label
import javafx.scene.input.MouseEvent
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.HBox
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.shape.Line
import javafx.scene.text.Text
import kotlin.math.abs
import kotlin.math.roundToInt

internal class MultipleLineChart(
    title: String,
    seriesList: List<Series<String, Number>>,
    private val strokeWidth: Double = 1.5
) : StackPane() {

    companion object {
        val COLORS = listOf(
            Color.BLUE,
            Color.GREEN,
            Color.CORAL,
            Color.YELLOW,
            Color.ORANGE,
            Color.INDIGO,
            Color.DEEPPINK,
            Color.DARKBLUE,
            Color.TOMATO,
            Color.SIENNA,
            Color.CYAN,
            Color.CRIMSON
        )
    }

    private val baseChart: LineChart<String, Number>
    private val backgroundCharts = mutableListOf<LineChart<String, Number>>()
    private val chartColorMap = mutableMapOf<LineChart<String, Number>, Color>()

    private val yAxisWidth = 35.0
    private val detailsWindow: AnchorPane
    private val titleWindow: VBox


    init {
        assert(seriesList.isNotEmpty()) {"Need at least one Series"}

        // find upper bound of all series
        var upperBound = 0
        seriesList.forEach { series ->
            upperBound = maxOf(upperBound, series.data.maxOf { data -> data.yValue.toInt() })
        }

        baseChart = buildBaseChart(seriesList.first(), upperBound)

        seriesList.filterIndexed { index, _ -> index != 0 }.forEach { series ->
            backgroundCharts.add(buildBackgroundCharts(series, upperBound))
        }

        titleWindow = buildTitle(title)
        detailsWindow = AnchorPane()

        bindMouseEvents(baseChart, this.strokeWidth)
        rebuildChart()
    }

    private fun buildBaseChart(series: Series<String, Number>, upperBound: Int): LineChart<String, Number> {
        val xAxis = createXAxis()
        val yAxis = createYAxis(upperBound)

        // create chart
        val lineChart = LineChart(xAxis, yAxis)
        lineChart.data.add(series)

        val lineColor: Color = COLORS[chartColorMap.size]
        chartColorMap[lineChart] = lineColor

        styleChartLine(lineChart, lineColor)
        setFixedAxisWidth(lineChart)

        lineChart.isAlternativeColumnFillVisible = false
        lineChart.verticalGridLinesVisible = false
        lineChart.isLegendVisible = false

        return lineChart
    }

    private fun buildBackgroundCharts(series: Series<String, Number>, upperBound: Int): LineChart<String, Number> {
        val xAxis = createXAxis()
        val yAxis = createYAxis(upperBound)

        val lineChart = object : LineChart<String, Number>(xAxis, yAxis) {
            init { // hide xAxis in constructor, since not public
                chartChildren.remove(yAxis)
                chartChildren.remove(xAxis)
            }
        }
        lineChart.data.add(series)

        val lineColor: Color = COLORS[chartColorMap.size]
        chartColorMap[lineChart] = lineColor

        styleChartLine(lineChart, lineColor)
        setFixedAxisWidth(lineChart)
        styleBackgroundChart(lineChart)

        return lineChart
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

    private fun bindMouseEvents(lineChart: LineChart<String, Number>, strokeWidth: Double) {

        children.add(detailsWindow)

        val detailsPopup = DetailsPopup()
        detailsWindow.children.add(detailsPopup)
        detailsWindow.prefHeightProperty().bind(heightProperty())
        detailsWindow.prefWidthProperty().bind(widthProperty())
        detailsWindow.isMouseTransparent = true

        onMouseMoved = null
        isMouseTransparent = false

        val xAxis = lineChart.xAxis
        val yAxis = lineChart.yAxis

        val xLine = Line()
        val yLine = Line()
        yLine.fill = Color.GRAY
        xLine.fill = Color.GRAY
        yLine.strokeWidth = strokeWidth / 2
        xLine.strokeWidth = strokeWidth / 2
        xLine.isVisible = false
        yLine.isVisible = false

        val chartBackground = lineChart.lookup(".chart-plot-background")
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

            if (lineChart.xAxis.getValueForDisplay(event.x) != null) {

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

    private fun setFixedAxisWidth(chart: LineChart<String, Number>) {
        chart.yAxis.prefWidth = yAxisWidth
        chart.yAxis.maxWidth = yAxisWidth
    }

    private fun rebuildChart() {
        children.clear()
        alignment = Pos.CENTER_LEFT
        children.add(resizeChart(baseChart))
        for (lineChart in backgroundCharts) {
            val hBox = resizeChart(lineChart)
            hBox.isMouseTransparent = true
            children.add(hBox)
        }
        children.add(detailsWindow)
        children.add(titleWindow)
    }

    private fun resizeChart(lineChart: LineChart<String, Number>): Node {

        val hBox = HBox(lineChart)
        hBox.alignment = Pos.CENTER_LEFT
        hBox.minHeightProperty().bind(heightProperty())
        hBox.prefHeightProperty().bind(heightProperty())
        hBox.maxHeightProperty().bind(heightProperty())

        hBox.minWidthProperty().bind(widthProperty())
        hBox.prefWidthProperty().bind(widthProperty())
        hBox.maxWidthProperty().bind(widthProperty())

        lineChart.minWidthProperty().bind(widthProperty())
        lineChart.prefWidthProperty().bind(widthProperty())
        lineChart.maxWidthProperty().bind(widthProperty())

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

    private fun styleChartLine(lineChart: LineChart<String, Number>, lineColor: Color) {
        lineChart.createSymbols = false
        lineChart.isLegendVisible = false
        lineChart.animated = false
        lineChart.xAxis.animated = false
        lineChart.yAxis.animated = false

        lineChart.lookup(".chart-series-line").style =
            "-fx-stroke: " + toRGBCode(lineColor) + "; -fx-stroke-width: " + strokeWidth + ";"
    }

    private fun styleBackgroundChart(lineChart: LineChart<String, Number>) {
        chartColorMap[lineChart]?.let { color -> styleChartLine(lineChart, color) }


        lineChart.lookup(".chart-content").lookup(".chart-plot-background").style =
            "-fx-background-color: transparent;"

        lineChart.isVerticalZeroLineVisible = false
        lineChart.isHorizontalZeroLineVisible = false
        lineChart.verticalGridLinesVisible = false
        lineChart.isHorizontalGridLinesVisible = false
        lineChart.isAlternativeColumnFillVisible = false
    }

    private fun toRGBCode(color: Color?) = "#%02X%02X%02X".format(
        (color!!.red * 255).toInt(),
        (color.green * 255).toInt(),
        (color.blue * 255).toInt()
    )

    val legend: Node
        get() {
            val hBox = HBox()
            val baseChartCheckBox = CheckBox(baseChart.yAxis.label)
            baseChartCheckBox.isSelected = true
            baseChartCheckBox.style =
                "-fx-text-fill: " + toRGBCode(chartColorMap[baseChart]) + "; -fx-font-weight: bold;"
            baseChartCheckBox.isDisable = true
            baseChartCheckBox.styleClass.add("readonly-checkbox")
            baseChartCheckBox.onAction = EventHandler {
                baseChartCheckBox.isSelected = true
            }

            hBox.children.add(baseChartCheckBox)
            for (lineChart in backgroundCharts) {
                val checkBox = CheckBox(lineChart.yAxis.label)
                checkBox.style = "-fx-text-fill: " + toRGBCode(chartColorMap[lineChart]) + "; -fx-font-weight: bold"
                checkBox.isSelected = true
                checkBox.onAction = EventHandler {
                    if (backgroundCharts.contains(lineChart)) {
                        backgroundCharts.remove(lineChart)
                    } else {
                        backgroundCharts.add(lineChart)
                    }
                }
                hBox.children.add(checkBox)
            }
            hBox.alignment = Pos.CENTER
            hBox.spacing = 20.0
            hBox.style = "-fx-padding: 0 10 20 10"
            return hBox
        }

    private inner class DetailsPopup : VBox() {

        init {
            style =
                "-fx-border-width: 1px; -fx-padding: 5 5 5 5px; -fx-border-color: gray; -fx-background-color: whitesmoke;"
            isVisible = false
        }

        fun showChartDescription(displayPosition: Double, yValue: Double) {
            children.clear()
            val xValue: String = baseChart.xAxis.getValueForDisplay(displayPosition)
            val baseChartPopupRow = buildPopupRow(xValue, yValue, baseChart)
            if (baseChartPopupRow != null) {
                children.add(baseChartPopupRow)
                for (lineChart in backgroundCharts) {
                    val popupRow = buildPopupRow(xValue, yValue, lineChart) ?: continue
                    children.add(popupRow)
                }
            }
        }

        private fun buildPopupRow(xValue: String, yValue: Double, lineChart: LineChart<String, Number>): HBox? {
            val seriesName = Label(lineChart.data[0].name)
            seriesName.textFill = chartColorMap[lineChart]

            val yValueForChart = getYValueForX(lineChart, xValue) ?: return null
            val yValueLower: Number = normalizeYValue(lineChart, yValue - 10).roundToInt()
            val yValueUpper: Number = normalizeYValue(lineChart, yValue + 10).roundToInt()
            val yValueUnderMouse: Number = lineChart.yAxis.getValueForDisplay(yValue).toDouble().roundToInt()

            // make series name bold when mouse is near given chart's line
            if (isMouseNearLine(
                    yValueForChart,
                    yValueUnderMouse,
                    abs(yValueLower.toDouble() - yValueUpper.toDouble())
                )
            ) {
                seriesName.style = "-fx-font-weight: bold"
            }
            return HBox(10.0, seriesName, Label("${yValueForChart.toInt()} km"))
        }

        private fun normalizeYValue(lineChart: LineChart<String, Number>, value: Double): Double {
            return lineChart.yAxis.getValueForDisplay(value) as Double
        }

        private fun isMouseNearLine(realYValue: Number, yValueUnderMouse: Number, tolerance: Double): Boolean {
            return abs(yValueUnderMouse.toDouble() - realYValue.toDouble()) < tolerance
        }

        fun getYValueForX(chart: LineChart<String, Number>, xValue: String?): Number? {
            for (data in chart.data[0].data) {
                if (data.xValue == xValue) {
                    return data.yValue as Number
                }
            }
            return null
        }
    }
}