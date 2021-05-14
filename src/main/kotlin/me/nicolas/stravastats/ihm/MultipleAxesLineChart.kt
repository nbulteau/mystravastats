package me.nicolas.stravastats.ihm

import javafx.beans.Observable
import javafx.collections.FXCollections
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.geometry.Side
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
import kotlin.math.abs
import kotlin.math.roundToInt

internal class MultipleAxesLineChart(
    private val baseChart: LineChart<String, Number>,
    lineColor: Color = Color.RED,
    private val strokeWidth: Double = 0.3
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
            Color.LEMONCHIFFON,
            Color.MEDIUMTURQUOISE,
            Color.SIENNA,
            Color.SKYBLUE
        )
    }

    private var colorIndex = 0
    private val backgroundCharts = FXCollections.observableArrayList<LineChart<String, Number>>()
    private val chartColorMap: MutableMap<LineChart<String, Number>, Color> = mutableMapOf()
    private val yAxisWidth = 60.0
    private val detailsWindow: AnchorPane
    private val yAxisSeparation = 0.0

    init {
        chartColorMap[baseChart] = lineColor
        styleBaseChart(baseChart)
        styleChartLine(baseChart, lineColor)
        setFixedAxisWidth(baseChart)

        alignment = Pos.CENTER_LEFT
        backgroundCharts.addListener { _: Observable? -> rebuildChart() }
        detailsWindow = AnchorPane()
        bindMouseEvents(baseChart, this.strokeWidth)
        rebuildChart()
    }

    private fun bindMouseEvents(baseChart: LineChart<String, Number>, strokeWidth: Double) {

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
            //xLine.isVisible = true
            //yLine.isVisible = true
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

    private fun styleBaseChart(baseChart: LineChart<String, Number>) {
        baseChart.createSymbols = false
        baseChart.isLegendVisible = false
        //baseChart.xAxis.isAutoRanging = false
        baseChart.xAxis.animated = false
        baseChart.yAxis.animated = false
    }

    private fun setFixedAxisWidth(chart: LineChart<String, Number>) {
        chart.yAxis.prefWidth = yAxisWidth
        chart.yAxis.maxWidth = yAxisWidth
    }

    private fun rebuildChart() {
        children.clear()
        children.add(resizeBaseChart(baseChart))
        for (lineChart in backgroundCharts) {
            children.add(resizeBackgroundChart(lineChart))
        }
        children.add(detailsWindow)
    }

    private fun resizeBaseChart(lineChart: LineChart<String, Number>): Node {

        val hBox = HBox(lineChart)
        hBox.alignment = Pos.CENTER_LEFT
        hBox.prefHeightProperty().bind(heightProperty())
        hBox.prefWidthProperty().bind(widthProperty())

        lineChart.minWidthProperty()
            .bind(widthProperty()) //.subtract((yAxisWidth + yAxisSeparation) * backgroundCharts.size))
        lineChart.prefWidthProperty()
            .bind(widthProperty())//.subtract((yAxisWidth + yAxisSeparation) * backgroundCharts.size))
        lineChart.maxWidthProperty()
            .bind(widthProperty())//.subtract((yAxisWidth + yAxisSeparation) * backgroundCharts.size))

        return lineChart
    }

    private fun resizeBackgroundChart(lineChart: LineChart<String, Number>): Node {

        val hBox = HBox(lineChart)
        hBox.alignment = Pos.CENTER_LEFT
        hBox.prefHeightProperty().bind(heightProperty())
        hBox.prefWidthProperty().bind(widthProperty())
        hBox.isMouseTransparent = true

        lineChart.minWidthProperty()
            .bind(widthProperty())//.subtract((yAxisWidth + yAxisSeparation) * backgroundCharts.size))
        lineChart.prefWidthProperty()
            .bind(widthProperty())//.subtract((yAxisWidth + yAxisSeparation) * backgroundCharts.size))
        lineChart.maxWidthProperty()
            .bind(widthProperty())//.subtract((yAxisWidth + yAxisSeparation) * backgroundCharts.size))


//        lineChart.translateXProperty().bind(baseChart.yAxis.widthProperty())
//        lineChart.yAxis.translateX = (yAxisWidth + yAxisSeparation) * backgroundCharts.indexOf(lineChart)

        return hBox
    }

    fun addSeries(series: Series<String, Number>, lineColor: Color = COLORS[this.colorIndex++]) {
        val xAxis = CategoryAxis()
        val yAxis = NumberAxis()

        // style x-axis
        xAxis.isVisible = false
        xAxis.opacity = 0.0 // somehow the upper setVisible does not work
        xAxis.borderProperty().bind((baseChart.xAxis as CategoryAxis).borderProperty())

        // style y-axis
        yAxis.isVisible = false
        yAxis.side = Side.RIGHT
        yAxis.label = series.name

        // create chart
        val lineChart = object : LineChart<String, Number>(xAxis, yAxis) {
            init { // hide xAxis in constructor, since not public
                chartChildren.remove(yAxis)
            }
        }
        lineChart.animated = false
        lineChart.isLegendVisible = false
        lineChart.data.add(series)
        styleBackgroundChart(lineChart, lineColor)
        setFixedAxisWidth(lineChart)
        chartColorMap[lineChart] = lineColor
        backgroundCharts.add(lineChart)
    }

    private fun styleBackgroundChart(lineChart: LineChart<String, Number>, lineColor: Color) {
        styleChartLine(lineChart, lineColor)
        val contentBackground = lineChart.lookup(".chart-content").lookup(".chart-plot-background")
        contentBackground.style = "-fx-background-color: transparent;"
        lineChart.isVerticalZeroLineVisible = false
        lineChart.isHorizontalZeroLineVisible = false
        lineChart.verticalGridLinesVisible = false
        lineChart.isHorizontalGridLinesVisible = false
        lineChart.createSymbols = false
    }

    private fun toRGBCode(color: Color?): String {
        return String.format(
            "#%02X%02X%02X",
            (color!!.red * 255).toInt(),
            (color.green * 255).toInt(),
            (color.blue * 255).toInt()
        )
    }

    private fun styleChartLine(chart: LineChart<String, Number>, lineColor: Color) {
        chart.yAxis.lookup(".axis-label").style = "-fx-text-fill: " + toRGBCode(lineColor) + "; -fx-font-weight: bold;"
        val seriesLine = chart.lookup(".chart-series-line")
        seriesLine.style = "-fx-stroke: " + toRGBCode(lineColor) + "; -fx-stroke-width: " + strokeWidth + ";"
    }

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

        fun showChartDescription(event: MouseEvent) {
            children.clear()
            val xValue: String? = baseChart.xAxis.getValueForDisplay(event.x)
            val baseChartPopupRow = buildPopupRow(event, xValue, baseChart)
            if (baseChartPopupRow != null) {
                children.add(baseChartPopupRow)
            }
            for (lineChart in backgroundCharts) {
                val popupRow = buildPopupRow(event, xValue, lineChart) ?: continue
                children.add(popupRow)
            }
        }

        private fun buildPopupRow(event: MouseEvent, xValue: String?, lineChart: LineChart<String, Number>): HBox? {
            val seriesName = Label(lineChart.yAxis.label)
            seriesName.textFill = chartColorMap[lineChart]
            val yValueForChart = getYValueForX(lineChart, xValue) ?: return null
            val yValueLower: Number = normalizeYValue(lineChart, event.y - 10).roundToInt()
            val yValueUpper: Number = normalizeYValue(lineChart, event.y + 10).roundToInt()
            val yValueUnderMouse: Number = lineChart.yAxis.getValueForDisplay(event.y).toDouble().roundToInt()

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

        private fun normalizeYValue(lineChart: LineChart<String, Number>, value: Double): Double {
            return lineChart.yAxis.getValueForDisplay(value) as Double
        }

        private fun isMouseNearLine(realYValue: Number, yValueUnderMouse: Number, tolerance: Double): Boolean {
            return abs(yValueUnderMouse.toDouble() - realYValue.toDouble()) < tolerance
        }

        fun getYValueForX(chart: LineChart<String, Number>, xValue: String?): Number? {
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