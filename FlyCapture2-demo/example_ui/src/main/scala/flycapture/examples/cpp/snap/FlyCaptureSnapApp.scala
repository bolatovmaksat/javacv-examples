/*
 * Copyright (c) 2014 Jarek Sacha. All Rights Reserved.
 *
 * Author's e-mail: jpsacha at gmail.com
 */

package flycapture.examples.cpp.snap

import java.io.IOException

import grizzled.slf4j.Logger
import org.apache.log4j.Level
import org.controlsfx.dialog.Dialogs

import scala.reflect.runtime.universe.typeOf
import scalafx.Includes._
import scalafx.application.JFXApp.PrimaryStage
import scalafx.application.{JFXApp, Platform}
import scalafx.concurrent.Task
import scalafx.scene.Scene
import scalafxml.core.{DependenciesByType, FXMLView}

/**
 * `FlyCaptureSnapApp` starts the FlyCaptureSnap application.
 *
 * First it will initialize FX, logging, and uncaught exception handling.
 * Then it will open the main application window `SnapView` and initialize its model.
 *
 * When application is closing, it will also attempt to close the SnapView model.
 *
 * @author Jarek Sacha 
 */
object FlyCaptureSnapApp extends JFXApp {
  val title = "Fly Capture Snap"

  // Initialize logging before anything else, so logging in constructors is functional.
  private val logger = Logger(this.getClass)
  initializeLogging(Level.INFO)

  setupUncaughtExceptionHandling(logger, title)

  val snapModel = new SnapModel()
  try {
    // Load main view
    val resourcePath = "SnapView.fxml"
    val resource = getClass.getResource(resourcePath)
    if (resource == null) throw new IOException("Cannot load resource: '" + resourcePath + "'")

    val root = FXMLView(resource, new DependenciesByType(Map(typeOf[SnapModel] -> snapModel)))

    // Create UI
    stage = new PrimaryStage() {
      title = "FlyCapture Snap Example"
      scene = new Scene(root)
    }

    snapModel.parent = stage

    // Initialize camera connections
    // Use worker thread for non-UI operations
    // TODO handle excaption in Task
    new Thread(Task {snapModel.initialize()}).start()
  } catch {
    case t: Throwable =>
      logger.error("Unexpected error. Application will terminate.", t)
      Dialogs.
        create().
        owner(null).
        title(title).
        masthead("Unexpected error. Application will terminate.").
        showException(t)

      Platform.exit()
  }

  override def stopApp() = {
    snapModel.shutDown()
    super.stopApp()
  }
}
