package me.nicolas.stravastats.ihm

import javafx.geometry.Insets
import javafx.scene.layout.BorderPane
import javafx.scene.paint.Color
import javafx.scene.text.Font
import tornadofx.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.*

internal class StravaAPIAuthenticationView : View("Strava API authentication") {

    override val root = BorderPane()

    init {
        val (clientId, clientSecret) = readStravaAuthentication()

        with(root) {
            padding = Insets(10.0, 50.0, 50.0, 50.0)
            top {
                hbox {
                    padding = Insets(20.0, 20.0, 20.0, 30.0)
                    text("Strava API authentication") {
                        font = Font.font("Verdana", 30.0)
                    }
                }
            }
            center {
                form {
                    paddingAll = 20.0
                    label("Client ID").gridpaneConstraints {
                        columnRowIndex(0, 0)
                    }
                    val clientIdTextField = textfield(clientId).gridpaneConstraints {
                        columnRowIndex(1, 0)
                        fillWidth = true
                    }
                    label("Client Secret").gridpaneConstraints {
                        columnRowIndex(0, 1)
                    }
                    val clientSecretPasswordField =
                        textfield(clientSecret).gridpaneConstraints {
                            columnRowIndex(1, 1)
                            fillWidth = true
                        }
                    val messageLabel = label().gridpaneConstraints {
                        columnRowIndex(1, 3)
                    }
                    button("Launch") {
                        gridpaneConstraints {
                            columnRowIndex(1, 2)
                        }
                        setOnAction {
                            val clientId = clientIdTextField.text.toString()
                            val clientSecret = clientSecretPasswordField.text.toString()

                            if (clientId != "") {
                                storeStravaAuthentication(clientId, clientSecret)

                                this@StravaAPIAuthenticationView.replaceWith(
                                    replacement = SplashScreenView(clientId, clientSecret.ifBlank { null }),
                                    sizeToScene = true,
                                    centerOnScreen = true
                                )
                            } else {
                                messageLabel.text = "Client Id is mandatory."
                                messageLabel.textFill = Color.RED
                            }
                        }
                    }
                }
            }
        }
    }

    private fun storeStravaAuthentication(clientId: String, clientSecret: String) {
        val file = File(".strava")
        if (!file.exists()) {
            file.createNewFile()
        }
        val prop = Properties()
        FileInputStream(file).use {
            prop.load(it)
            prop.setProperty("clientId", clientId)
            prop.setProperty("clientSecret", clientSecret)
            val out: OutputStream = FileOutputStream(file)
            prop.store(out, "Strava authentication")
        }
    }

    private fun readStravaAuthentication(): Pair<String?, String?> {
        val file = File(".strava")
        val properties = Properties()

        if (file.exists()) {
            FileInputStream(file).use { properties.load(it) }
        }
        return Pair(properties["clientId"]?.toString(), properties["clientSecret"]?.toString())
    }
}

