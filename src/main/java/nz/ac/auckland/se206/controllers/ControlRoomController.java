package nz.ac.auckland.se206.controllers;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import javafx.animation.FadeTransition;
import javafx.animation.RotateTransition;
import javafx.animation.TranslateTransition;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.image.ImageView;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.GameState;
import nz.ac.auckland.se206.GameState.NextTask;
import nz.ac.auckland.se206.custom.GameMaster;
import nz.ac.auckland.se206.managers.SceneManager.SceneUi;
import nz.ac.auckland.se206.managers.TimeManager;

/** Controller class for the control room view. */
public class ControlRoomController extends Controller {

  /** Names of the possible sound effects for the control room. */
  private enum SoundEffect {
    TINK,
    DOOSH;
  }

  @FXML private ImageView imageViewPowerOff;
  @FXML private ImageView imageViewStart1;
  @FXML private ImageView imageViewStart2;
  @FXML private ImageView imageViewStart3;
  @FXML private ImageView imageViewStart4;
  @FXML private ImageView imageViewStart5;
  @FXML private ImageView imageViewStart6;
  @FXML private ImageView imageViewStart7;
  @FXML private ImageView imageViewStart8;
  @FXML private ImageView imageViewPowerOn;
  @FXML private ImageView imageViewGameMaster;
  @FXML private ImageView imageViewMedium;
  @FXML private ImageView imageViewHard;
  @FXML private ImageView imageView4Minutes;
  @FXML private ImageView imageView6Minutes;
  @FXML private ImageView arrow;
  @FXML private Rectangle rectanglePowerButton;
  @FXML private Rectangle rectangleToBedroom;
  @FXML private Rectangle rectangleToAirlock;
  @FXML private Rectangle rectangleGameMaster;
  @FXML private Rectangle rectangleDark;
  @FXML private Rectangle rectanglePowerVisible;
  @FXML private Label labelToAirlock;
  @FXML private Label labelToBedroom;
  @FXML private Label labelText;
  @FXML private RadioButton radioButtonEasy;
  @FXML private RadioButton radioButtonMedium;
  @FXML private RadioButton radioButtonHard;
  @FXML private RadioButton radioButton2Minutes;
  @FXML private RadioButton radioButton4Minutes;
  @FXML private RadioButton radioButton6Minutes;
  @FXML private GameMaster gameMaster;
  @FXML private ImageView cdPlayer;

  private MediaPlayer mediaPlayer;
  private Map<SoundEffect, String> soundEffects;
  private Thread moveArrowThread;
  private int counter;

  /**
   * Initializes the control room. The sound effects will be loaded. This view is only initialised
   * when the game starts/restarts, so all GameState variables are reset here.
   */
  @FXML
  private void initialize() {
    GameState.resetGame();
    createNewTextToSpeech();
    settings.disableRestart();
    cdPlayer.setDisable(true);
    cdPlayer.setOpacity(0);
    counter = 0;

    // load sound effects
    soundEffects = new HashMap<>(2);
    soundEffects.put(
        SoundEffect.TINK, new File("src/main/resources/sounds/tink.mp3").toURI().toString());
    soundEffects.put(
        SoundEffect.DOOSH, new File("src/main/resources/sounds/doosh.mp3").toURI().toString());

    // set instructions
    labelText.setText(
        "I need power! Once you've selected your desired difficulty and time limit, locate and"
            + " press the power button!");
    Task<Void> moveArrowTask =
        new Task<Void>() {

          @Override
          protected Void call() throws Exception {
            // bounce arrow up and down indefinitely
            TranslateTransition arrowTransition =
                new TranslateTransition(Duration.seconds(1), arrow);
            arrowTransition.setCycleCount(TranslateTransition.INDEFINITE);
            arrowTransition.setByY(10);
            arrowTransition.setAutoReverse(true);
            arrowTransition.play();
            return null;
          }
        };
    moveArrowThread = new Thread(moveArrowTask, "Move Arrow");
    moveArrowThread.start();
  }

  /** Handles the click event on the power button. */
  @FXML
  private void onPowerOnButtonClicked() {
    rectanglePowerButton.setVisible(false);
    ttsSpeak("Power on");
    moveArrowThread.interrupt();
    powerOn();
  }

  /**
   * Handles the click event on the button to go to the bedroom.
   *
   * @throws IOException if "src/main/resources/fxml/bedroom.fxml" is not found
   */
  @FXML
  private void onToBedroomButtonClicked() throws IOException {
    ttsSpeak("Entering bedroom.");
    if (GameState.nextTask == NextTask.BEDROOM) {
      GameState.nextTask = NextTask.PAPER;
    }
    App.setRoot(SceneUi.BEDROOM);
  }

  /**
   * Handles the click event on the button to go to the airlock.
   *
   * @throws IOException if "src/main/resources/fxml/closedAirlock.fxml" is not found
   */
  @FXML
  private void onToAirlockButtonClicked() throws IOException {
    // speech
    ttsSpeak("Entering airlock.");
    // check if the airlock is open
    if (GameState.isAirlockOpen) {
      App.setRoot(SceneUi.OPEN_AIRLOCK);
    } else {
      App.setRoot(SceneUi.CLOSED_AIRLOCK);
    }
  }

  /** Handles the click event on a time limit radio button. */
  @FXML
  private void onUpdateTimeLimit() {
    // check which time limit has been newly selected
    if (radioButton2Minutes.isSelected()) {
      GameState.timeLimit = 120;
      ttsSpeak("Two minutes.");
    } else if (radioButton4Minutes.isSelected()) {
      GameState.timeLimit = 240;
      ttsSpeak("Four minutes.");
    } else if (radioButton6Minutes.isSelected()) {
      GameState.timeLimit = 360;
      ttsSpeak("Six minutes.");
    }
  }

  /**
   * Sends the player to the chat to talk with the Game Master.
   *
   * @throws IOException if "src/main/resources/fxml/chat.fxml" is not found
   */
  @FXML
  private void onToChatClicked() throws IOException {
    App.setRoot(SceneUi.CHAT);
  }

  /** Helper method to start the power on sequence in a separate thread. */
  private void powerOn() {
    Task<Void> taskPowerOn =
        new Task<Void>() {

          @Override
          protected Void call() throws Exception {
            // remove radio buttons and instructions
            fadeOut(radioButtonEasy);
            fadeOut(radioButtonMedium);
            fadeOut(radioButtonHard);
            fadeOut(radioButton2Minutes);
            fadeOut(radioButton4Minutes);
            fadeOut(radioButton6Minutes);
            fadeOut(labelText);
            fadeOut(arrow);

            // light up room
            FadeTransition fadeOut = new FadeTransition(Duration.seconds(3), rectangleDark);
            fadeOut.setFromValue(0.75);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(
                e -> {
                  rectangleDark.setVisible(false);
                  rectanglePowerVisible.setVisible(false);
                });
            fadeOut.play();

            // retract extra signs
            retractSign(imageViewMedium, true);
            retractSign(imageViewHard, false);
            retractSign(imageView4Minutes, true);
            retractSign(imageView6Minutes, false);

            // switch on control room
            playSoundEffect(SoundEffect.TINK);
            Thread.sleep(100);

            imageViewPowerOff.setVisible(false);
            imageViewStart1.setVisible(true);
            playSoundEffect(SoundEffect.TINK);
            Thread.sleep(200);

            imageViewStart1.setVisible(false);
            imageViewStart2.setVisible(true);
            playSoundEffect(SoundEffect.TINK);
            Thread.sleep(300);

            imageViewStart2.setVisible(false);
            imageViewStart3.setVisible(true);
            playSoundEffect(SoundEffect.TINK);
            Thread.sleep(400);

            imageViewStart3.setVisible(false);
            imageViewStart4.setVisible(true);
            playSoundEffect(SoundEffect.TINK);
            Thread.sleep(350);

            imageViewStart4.setVisible(false);
            imageViewStart5.setVisible(true);
            playSoundEffect(SoundEffect.TINK);
            Thread.sleep(600);

            imageViewStart5.setVisible(false);
            imageViewStart6.setVisible(true);
            playSoundEffect(SoundEffect.TINK);
            Thread.sleep(300);

            // switch on Game Master
            RotateTransition rotateGameMaster =
                new RotateTransition(Duration.seconds(2), imageViewGameMaster);
            rotateGameMaster.setByAngle(3_600);
            rotateGameMaster.play();

            imageViewStart6.setVisible(false);
            imageViewStart7.setVisible(true);
            playSoundEffect(SoundEffect.TINK);
            Thread.sleep(700);

            imageViewStart7.setVisible(false);
            imageViewStart8.setVisible(true);
            playSoundEffect(SoundEffect.DOOSH);
            Thread.sleep(600);

            // enable access to other rooms with ceiling light
            labelToAirlock.setVisible(true);
            labelToBedroom.setVisible(true);
            rectangleToBedroom.setVisible(true);
            rectangleToAirlock.setVisible(true);
            imageViewStart8.setVisible(false);
            imageViewPowerOn.setVisible(true);
            playSoundEffect(SoundEffect.DOOSH);

            // start and display countdown
            TimeManager.resetAndStart();
            imageViewGameMaster.setVisible(false);
            gameMaster.setVisible(true);
            settings.enableRestart();

            // Make CD player available
            cdPlayer.setDisable(false);
            return null;
          }
        };

    Thread threadPowerOn = new Thread(taskPowerOn, "Power On");
    threadPowerOn.start();
  }

  /**
   * Helper function to play a specified sound effect.
   *
   * @param soundEffect the desired sound effect to be played
   */
  private void playSoundEffect(SoundEffect soundEffect) {
    if (mediaPlayer != null) {
      mediaPlayer.stop();
      mediaPlayer.dispose();
    }
    if (GameState.isSoundEffectOn.get()) {
      mediaPlayer = new MediaPlayer(new Media(soundEffects.get(soundEffect)));
      mediaPlayer.play();
    }
  }

  /**
   * Helper function to fade out a specified node in 200 ms.
   *
   * @param node the node to be faded out
   */
  private void fadeOut(Node node) {
    FadeTransition fadeTransition = new FadeTransition(Duration.millis(200), node);
    fadeTransition.setCycleCount(1);
    fadeTransition.setFromValue(1);
    fadeTransition.setToValue(0);
    fadeTransition.setOnFinished(event -> node.setVisible(false));
    fadeTransition.play();
  }

  /**
   * Helper function to retract the signs that are no longer needed by creating translate
   * transitions for the nodes.
   *
   * @param node the node to be translated upwards
   * @param middle true if the sign is in the middle, false if the sign is on the bottom
   */
  private void retractSign(Node node, boolean middle) {
    TranslateTransition translateNode = new TranslateTransition(Duration.millis(2500), node);
    translateNode.setCycleCount(1);
    if (middle) {
      translateNode.setByY(-110);
    } else {
      // retract by more because it has further to go
      translateNode.setByY(-220);
    }
    // hide node when retracted
    translateNode.setOnFinished(event -> node.setVisible(false));
    translateNode.play();
  }

  /** Handles the click event on a difficulty radio button. */
  @FXML
  private void onUpdateDifficulty() {
    // check which difficulty has been newly selected
    if (radioButtonEasy.isSelected()) {
      GameState.numHintsRemaining.set(-1);
      ttsSpeak("Easy, unlimited hints.");
    } else if (radioButtonMedium.isSelected()) {
      GameState.numHintsRemaining.set(5);
      ttsSpeak("Medium, five hints.");
    } else if (radioButtonHard.isSelected()) {
      GameState.numHintsRemaining.set(0);
      ttsSpeak("Hard, no hints.");
    }
  }

  /**
   * Handles the click event of the CD player.
   *
   * @throws URISyntaxException if the sound effect cannot be found
   */
  @FXML
  private void onClickCdPlayer() throws URISyntaxException {
    counter++;
    if (counter < 5) {
      playSound("click", false);
    }
    if (counter >= 3) {
      cdPlayer.setOpacity(1);
    }
    if (counter >= 5) {
      // Disable the button
      cdPlayer.setDisable(true);
      // Disable the sounds then play the video
      GameState.isMusicOn.set(false);
      App.stopMusic();
      Task<Void> talkingTask =
          new Task<Void>() {
            @Override
            protected Void call() throws Exception {
              tts.speak("lol, get rick rolled");
              return null;
            }
          };
      Thread talkingThread = new Thread(talkingTask, "Talking");
      talkingThread.start();
      talkingTask.setOnSucceeded(
          e -> openWebpage("https://youtu.be/dQw4w9WgXcQ?si=gc2-4YpnTH6RQXOp"));
    }
  }

  /**
   * Method to open a webpage in the default web browser.
   *
   * @param url the address of the webpage to be opened
   */
  private void openWebpage(String url) {
    if (Desktop.isDesktopSupported()) {
      Desktop desktop = Desktop.getDesktop();
      if (desktop.isSupported(Desktop.Action.BROWSE)) {
        try {
          // open url in user's default browser
          desktop.browse(new URI(url));
        } catch (IOException | URISyntaxException e) {
          e.printStackTrace();
        }
      }
    }
  }
}
