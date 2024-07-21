package nz.ac.auckland.se206.managers;

import java.io.IOException;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.util.Duration;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.GameState;
import nz.ac.auckland.se206.GameState.NextTask;
import nz.ac.auckland.se206.managers.SceneManager.SceneUi;

/** Manages the time remaining based on time limit set by player. */
public class TimeManager {

  /** The current time remaining in the game. */
  public static IntegerProperty timeRemaining = new SimpleIntegerProperty();

  private static Timeline timeline;

  /** Resets the time remaining and starts the timeline. */
  public static void resetAndStart() {
    if (timeline != null) {
      timeline.stop();
    }

    // timeline that decreases time remaining every second throughout the game
    timeRemaining.set(GameState.timeLimit);
    timeline =
        new Timeline(
            new KeyFrame(
                Duration.seconds(1),
                event -> {
                  timeRemaining.set(timeRemaining.get() - 1);
                  if (timeRemaining.get() < 0) {
                    // player has run out of time
                    timeline.stop();
                    timeRemaining.set(0);
                    GameState.nextTask = NextTask.FAIL;
                    try {
                      App.setRoot(SceneUi.FAILED);
                    } catch (IOException e) {
                      e.printStackTrace();
                    }
                  }
                }));
    timeline.setCycleCount(Timeline.INDEFINITE);
    timeline.play();
  }

  /**
   * Stops the timeline so {@code timeRemaining} will not be decremented further. Useful for
   * calculating time taken to complete the game.
   */
  public static void stopTime() {
    if (timeline != null) {
      timeline.stop();
    }
  }
}
