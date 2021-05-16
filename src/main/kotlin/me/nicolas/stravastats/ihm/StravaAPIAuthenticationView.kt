package me.nicolas.stravastats.ihm

import javafx.geometry.Insets
import javafx.scene.paint.Color
import javafx.scene.text.Font
import tornadofx.*

internal class StravaAPIAuthenticationView : View("Strava API authentication") {

    override val root = borderpane {
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
                val clientIdTextField = textfield("41902").gridpaneConstraints {
                    columnRowIndex(1, 0)
                    fillWidth = true
                }
                label("Client Secret").gridpaneConstraints {
                    columnRowIndex(0, 1)
                }
                val clientSecretPasswordField = textfield("c71038c8bd9f48fdf4b66d25deb9964e66a5301e").gridpaneConstraints {
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

