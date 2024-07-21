package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.input.KeyEvent;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.GameState;
import nz.ac.auckland.se206.custom.Settings;
import nz.ac.auckland.se206.managers.SceneManager.SceneUi;
import nz.ac.auckland.se206.speech.TextToSpeech;

/** Superclass controller for all the controller classes of the separate views. */
public abstract class Controller {

  @FXML protected static MediaPlayer player;
  @FXML protected Settings settings;

  protected TextToSpeech tts;

  /**
   * Helper method to create a new instance of a text-to-speech object if one is not already
   * created.
   */
  protected void createNewTextToSpeech() {
    if (tts == null) {
      tts = new TextToSpeech();
    }
  }

  /**
   * Converts some text to speech and plays it.
   *
   * @param speech the text to be spoken
   */
  protected void ttsSpeak(String speech) {
    if (GameState.isTextToSpeechOn.get()) {
      Task<Void> speakingTask =
          new Task<Void>() {
            @Override
            protected Void call() throws Exception {
              tts.speak(speech);
              return null;
            }
          };

      // create a new thread to speak
      Thread speakingThread = new Thread(speakingTask);
      speakingThread.setDaemon(true);
      speakingThread.start();
    }
  }

  /**
   * Creates a new task that puts the thread to sleep for a certain number of seconds.
   *
   * @param seconds the number of seconds the thread should sleep for
   * @return the task
   */
  protected Task<Void> getWaitTask(int seconds) {
    return new Task<Void>() {
      @Override
      protected Void call() throws Exception {
        Thread.sleep(seconds);
        return null;
      }
    };
  }

  /**
   * Handles the key pressed event.
   *
   * @param event the key event
   */
  @FXML
  protected void onKeyPressed(KeyEvent event) {
    System.out.println("key " + event.getCode() + " pressed");
  }

  /**
   * Handles the key released event.
   *
   * @param event the key event
   */
  @FXML
  protected void onKeyReleased(KeyEvent event) {
    System.out.println("key " + event.getCode() + " released");
  }

  /**
   * Plays a sound specified by the filename. The file should be located in the /resources/sounds/
   * folder and be of type mp3.
   *
   * @param songName the filename of the sound file to be played
   * @throws URISyntaxException if the sound file cannot be found
   */
  @FXML
  protected void playSound(String songName, boolean repeat) throws URISyntaxException {
    Media sound =
        new Media(App.class.getResource("/sounds/" + songName + ".mp3").toURI().toString());
    // stop current sound
    if (player != null) {
      player.stop();
    }
    if (GameState.isSoundEffectOn.get()) {
      player = new MediaPlayer(sound);
      player.play();
      if (repeat) {
        player.setOnEndOfMedia(
            () -> {
              player.seek(Duration.ZERO);
              player.play();
            });
      }
    }
  }

  /**
   * Fades in a rectangle over 1 second using a FadeTransition.
   *
   * @param rectangle the rectangle node to be faded in
   */
  @FXML
  protected void fadeIn(Rectangle rectangle) {
    FadeTransition fade = new FadeTransition(Duration.seconds(1), rectangle);
    // Set the starting opacity
    fade.setFromValue(1);
    // Set the ending opacity
    fade.setToValue(0);
    // Set the cycle count for the fade
    fade.setOnFinished(e -> rectangle.setVisible(false));
    fade.play();
  }

  /**
   * Fades out a rectangle over 1 second using a FadeTransition.
   *
   * @param rectangle the rectangle node to be faded out
   */
  @FXML
  protected void fadeOut(Rectangle rectangle) {
    rectangle.setVisible(true);
    FadeTransition fade = new FadeTransition(Duration.seconds(1), rectangle);
    // Set the starting opacity
    fade.setFromValue(0);
    // Set the ending opacity
    fade.setToValue(1);
    fade.play();
  }

  /**
   * Waits for a given number of seconds for fade in/out, then moves to a given scene.
   *
   * @param seconds the number of seconds to wait before fade
   * @param sceneUi the scene to transition to
   */
  @FXML
  protected void waitSecondMoveToScene(int seconds, SceneUi sceneUi) {
    Task<Void> wait =
        new Task<Void>() {
          @Override
          protected Void call() throws Exception {
            Thread.sleep(seconds * 1000);
            return null;
          }
        };
    // create a new thread to wait
    Thread waitThread = new Thread(wait);
    waitThread.setDaemon(true);
    waitThread.start();
    wait.setOnSucceeded(
        e -> {
          try {
            App.setRoot(sceneUi);
          } catch (IOException e1) {
            e1.printStackTrace();
          }
        });
  }

  /** Handles the click event to open the settings window. */
  @FXML
  private void openSettingWindow() {
    settings.openSettingWindow();
  }

  /**
   * Append text to Text letter by letter.
   *
   * @param subtitle the Text node to display the message in
   * @param msg the message to be displayed
   */
  protected void appendTextTo(Text subtitle, String msg) {
    ExecutorService executorService = Executors.newFixedThreadPool(1);
    StringBuffer stringBuffer = new StringBuffer();

    // display every time new letter appended
    for (char letter : msg.toCharArray()) {
      executorService.execute(
          () -> {
            Platform.runLater(
                () -> {
                  stringBuffer.append(letter);
                  subtitle.setText(stringBuffer.toString());
                });
            try {
              Thread.sleep(10);
            } catch (InterruptedException e) {
              e.printStackTrace();
            }
          });
    }
  }
}
