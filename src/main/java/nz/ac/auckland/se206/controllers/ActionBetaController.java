package nz.ac.auckland.se206.controllers;

import java.net.URISyntaxException;
import javafx.animation.TranslateTransition;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import nz.ac.auckland.se206.managers.SceneManager.SceneUi;

/** Controller class for the Act B view. */
public class ActionBetaController extends Controller {

  @FXML private Rectangle fade;
  @FXML private Button skipButton;
  @FXML private ImageView background;

  private int duration;
  private int count;
  private Thread waitThread;

  /**
   * Initializes the Act B view. Moves the background then transitions to the success view.
   *
   * @throws URISyntaxException if the sound file cannot be found
   */
  @FXML
  private void initialize() throws URISyntaxException {
    // set the duration and count for the animation
    duration = 3000;
    count = 10;
    fadeIn(fade);
    playSound("radio-noise", false);

    // move the background image
    moveImageByF(background, 0, 10);

    Task<Void> wait = getWaitTask(duration);
    waitThread = new Thread(wait);
    waitThread.start();
    wait.setOnSucceeded(
        e -> {
          fadeOut(fade);
          waitSecondMoveToScene(1, SceneUi.SUCCESS);
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

  /**
   * Moves the image by a given x and y value.
   *
   * @param image the image to be moved
   * @param x the amount to move the image horizontally
   * @param y the amount to move the image vertically
   */
  @FXML
  private void moveImageByF(ImageView image, float x, float y) {
    // create a new transition to move the image
    TranslateTransition move = new TranslateTransition(Duration.millis(duration / count), image);
    move.setByX(x);
    move.setByY(y);
    move.setCycleCount(count);
    move.setAutoReverse(true);
    move.play();
  }
}
