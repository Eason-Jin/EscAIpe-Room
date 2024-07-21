package nz.ac.auckland.se206.controllers;

import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import nz.ac.auckland.se206.managers.SceneManager.SceneUi;

/** Controller class for the Act A view. */
public class ActionAlphaController extends Controller {

  @FXML private Rectangle fade;
  @FXML private Button skipButton;
  @FXML private ImageView shuttle;

  private int duration;
  private Thread waitThread;

  /** Initializes the Act A view. Moves the shuttle then transitions to Act B. */
  @FXML
  private void initialize() {
    duration = 3000;
    fadeIn(fade);
    TranslateTransition moveLeft = new TranslateTransition(Duration.millis(duration), shuttle);
    moveLeft.setByX(-300f);
    moveLeft.play();

    ScaleTransition downScale = new ScaleTransition(Duration.millis(duration), shuttle);
    downScale.setByX(-1.0f);
    downScale.setByY(-1.0f);
    downScale.play();

    // create a new task to wait for the duration
    Task<Void> wait = getWaitTask(duration);
    waitThread = new Thread(wait);
    waitThread.start();
    wait.setOnSucceeded(
        e -> {
          fadeOut(fade);
          waitSecondMoveToScene(1, SceneUi.ACT_B);
        });
  }

  /**
   * Handles the skip pressed event.
   *
   * @param event the mouse event
   */
  @FXML
  private void onSkip(MouseEvent event) {
    skipButton.setDisable(true);
    fadeOut(fade);
    waitThread.interrupt();
    waitSecondMoveToScene(1, SceneUi.SUCCESS);
  }
}
