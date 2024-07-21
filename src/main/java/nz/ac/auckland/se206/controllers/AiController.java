package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.util.Duration;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.GameState;
import nz.ac.auckland.se206.custom.GameMaster;
import nz.ac.auckland.se206.gpt.ChatMessage;
import nz.ac.auckland.se206.gpt.openai.ApiProxyException;
import nz.ac.auckland.se206.gpt.openai.ChatCompletionRequest;
import nz.ac.auckland.se206.gpt.openai.ChatCompletionResult;
import nz.ac.auckland.se206.gpt.openai.ChatCompletionResult.Choice;

/** Superclass controller for the separate AI view controllers. */
public abstract class AiController extends Controller {

  @FXML private TextArea chatTextArea;
  @FXML private Button sendButton;
  @FXML private Label labelNumHints;
  @FXML private GameMaster gameMaster;
  @FXML protected TextField inputText;

  private ChatMessage initialMsg;
  private ChatMessage msg; // This is the msg passes into GPT
  private Task<Void> gptTask;
  private Thread gptThread;
  private String pastEntry;
  private String openText; // The opening text, riddle or chat
  private List<String> noMoreHintsText;
  protected ChatCompletionRequest chatCompletionRequest;
  protected ChatMessage lastMsg;

  /** Spins the game master when GPT is loading. */
  private void spinGameMasterOnLoading() {
    Task<Void> spinGameMasterTask =
        new Task<Void>() {

          @Override
          protected Void call() throws Exception {
            RotateTransition rotateGameMaster =
                new RotateTransition(Duration.seconds(0.25), gameMaster);
            rotateGameMaster.setByAngle(360);
            rotateGameMaster.setInterpolator(Interpolator.LINEAR);
            Thread.sleep(500);
            while (chatTextArea.getText().contains("Thinking...")) {
              // keep rotating if GPT is loading
              rotateGameMaster.play();
            }
            return null;
          }
        };

    // run task in thread
    Thread spinGameMasterThread = new Thread(spinGameMasterTask, "Spin Game Master");
    spinGameMasterThread.setDaemon(true);
    spinGameMasterThread.start();
  }

  /**
   * Appends a chat message to the chat text area letter by letter.
   *
   * @param msg the chat message to append
   */
  private void appendChatMessage(String msg) {
    // Method suggested by ChatGPT
    ExecutorService executorService = Executors.newFixedThreadPool(1);
    for (char letter : msg.toCharArray()) {
      executorService.execute(
          () -> {
            Platform.runLater(
                () -> {
                  chatTextArea.appendText(String.valueOf(letter));
                });
            try {
              Thread.sleep(10);
            } catch (InterruptedException e) {
              e.printStackTrace();
            }
          });
    }
  }

  /**
   * Runs the GPT model with a given chat message.
   *
   * @param msg the chat message to process
   * @return the response chat message
   * @throws ApiProxyException if there is an error communicating with the API proxy
   */
  protected ChatMessage runGpt(ChatMessage msg) throws ApiProxyException {
    chatCompletionRequest.addMessage(msg);
    try {
      // get new message from GPT and add it to TextArea
      ChatCompletionResult chatCompletionResult = chatCompletionRequest.execute();
      Choice result = chatCompletionResult.getChoices().iterator().next();
      chatCompletionRequest.addMessage(result.getChatMessage());
      Platform.runLater(() -> updateChatTextArea(pastEntry, result));
      return result.getChatMessage();
    } catch (ApiProxyException e) {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * Adds the new message to the chat text area by first checking whether the player has asked for a
   * hint. The player will not be given the hint if they don't have any hints remaining.
   *
   * @param pastEntry the previous text in the chat area
   * @param result the GPT result containing the new text
   */
  protected void updateChatTextArea(String pastEntry, Choice result) {
    chatTextArea.clear();

    if (pastEntry == null) {
      appendChatMessage(result.getChatMessage().getContent());
    } else {
      // check for hint
      if (result.getChatMessage().getContent().startsWith("Hint")) {
        // check for limit
        if (GameState.numHintsRemaining.get() == 0) {
          chatTextArea.setText(openText + "\n\n" + pastEntry + "\n\n");
          appendChatMessage(
              noMoreHintsText.get(GameState.getRandomNumber(0, noMoreHintsText.size())));
        } else {
          GameState.numHintsRemaining.set(GameState.numHintsRemaining.get() - 1);
          chatTextArea.setText(openText + "\n\n" + pastEntry + "\n\n"); // remove "Hint: "
          appendChatMessage(result.getChatMessage().getContent().substring(6));
        }
      } else {
        chatTextArea.setText(openText + "\n\n" + pastEntry + "\n\n");
        appendChatMessage(result.getChatMessage().getContent());
      }
    }
  }

  /** Sends a message to the GPT model. */
  @FXML
  private void onSendMessage() {
    spinGameMasterOnLoading();

    String message = inputText.getText();
    if (message.trim().isEmpty()) {
      // don't send to GPT if player has not entered any text
      return;
    }

    // show loading and disable player from typing anything else
    inputText.clear();
    msg = new ChatMessage("user", message);
    pastEntry = msg.getContent();
    chatTextArea.setText(openText + "\n\n" + pastEntry + "\n\n" + "Thinking...");
    disableInput();

    // send to GPT
    gptTask = createGptTask(msg);
    gptThread = new Thread(gptTask, "Send Message to GPT");
    gptThread.setDaemon(true);
    gptThread.start();
  }

  /**
   * Navigates back to the previous view.
   *
   * @param event the action event triggered by the go back button
   * @throws IOException if the current scene is not found
   */
  @FXML
  private void onGoBack(ActionEvent event) throws IOException {
    App.setRoot(GameState.currentScene);
  }

  /**
   * Creates a task to run the GPT model and checks whether the player solved the riddle if in the
   * riddle view.
   *
   * @return the task created
   */
  protected abstract Task<Void> createGptTask(ChatMessage msg);

  /**
   * Initialises the GPT chat by and prompts the AI using the text in the {@code messageContent}
   * string. It will also set up a listener on the number of hints the player has remaining to
   * update the text in the AI views if it changes.
   *
   * @param messageContent the starting message for the GPT response
   * @throws ApiProxyException if there is an error communicating with the API proxy
   */
  protected void initialize(String messageContent) throws ApiProxyException {

    updateNumHintsText();
    initialiseNoMoreHintsText();
    gameMaster.setClickable(false);

    // initialize chat
    chatTextArea.setText("Thinking...");
    disableInput();
    spinGameMasterOnLoading();

    // ask GPT for initial message
    chatCompletionRequest =
        new ChatCompletionRequest().setN(1).setTemperature(0.2).setTopP(0.5).setMaxTokens(80);
    initialMsg = new ChatMessage("system", messageContent);

    // run GPT in a separate thread
    Task<Void> taskGpt =
        new Task<Void>() {

          @Override
          protected Void call() throws Exception {
            openText = runGpt(initialMsg).getContent();
            return null;
          }
        };
    // run task in thread
    Thread threadGpt = new Thread(taskGpt, "Initial GPT");
    threadGpt.setDaemon(true);
    threadGpt.start();

    // ensure number of hints gets updated
    GameState.numHintsRemaining.addListener(
        (observable, oldValue, newValue) -> Platform.runLater(() -> updateNumHintsText()));
  }

  /** Helper method to set the text within the hints label. */
  private void updateNumHintsText() {
    labelNumHints.setText(
        ((GameState.numHintsRemaining.get() < 0) ? "∞" : GameState.numHintsRemaining.get())
            + " hint"
            + ((GameState.numHintsRemaining.get() == 1) ? "" : "s")
            + " remaining");
  }

  /** Helper method to enable the text field for typing and send button for running GPT. */
  protected void enableInputText() {
    inputText.setDisable(false);
    inputText.requestFocus();
    sendButton.setDisable(false);
  }

  /** Helper method to disable the text field and send button for when GPT is running. */
  protected void disableInput() {
    inputText.setDisable(true);
    sendButton.setDisable(true);
  }

  /** Helper method to add a few options for GPT to display when player has 0 hints remaining. */
  private void initialiseNoMoreHintsText() {
    // create array
    noMoreHintsText = new ArrayList<>();

    // add first five messages
    noMoreHintsText.add(
        "Your communication with mission control has been temporarily cut off. You're on your own"
            + " now!");
    noMoreHintsText.add(
        "You've already had your share of cosmic guidance. It's time to navigate this mission"
            + " solo.");
    noMoreHintsText.add(
        "The space-time continuum doesn't allow for more hints at this point. You're on the edge of"
            + " discovery.");
    noMoreHintsText.add(
        "In the vastness of space, you've reached the limit of hint transmissions. It's time to"
            + " rely on your astronaut instincts.");
    noMoreHintsText.add(
        "Your helmet HUD is showing that you've exhausted the hint reserves. Time to solve this"
            + " like a true space explorer.");

    // add second five messages
    noMoreHintsText.add(
        "Mission protocol dictates that you've had enough hints. It's time to boldly go where no"
            + " one has gone before... without hints.");
    noMoreHintsText.add(
        "Space is unforgiving, and so is our hint policy. You're on your final frontier.");
    noMoreHintsText.add(
        "Houston, or in this case, mission control, says 'no more hints.' It's time to trust your"
            + " training.");
    noMoreHintsText.add(
        "The stars above are watching, and they say it's time to shine on your own. No more hints"
            + " for you.");
    noMoreHintsText.add(
        "The escape room AI is calculating that you've had your fair share of assistance. Fly solo"
            + " from here on out.");

    // add third five messages
    noMoreHintsText.add(
        "Your communicator's battery is running low, and so is our hint dispenser. Make your moves"
            + " count.");
    noMoreHintsText.add(
        "Space is a silent vacuum, and your hint requests are too. No more cosmic guidance for"
            + " now.");
    noMoreHintsText.add(
        "The cosmic council has ruled: no more hints for this journey. You must explore the unknown"
            + " on your own.");
    noMoreHintsText.add(
        "You've entered the 'no-hint zone' of the escape room galaxy. Time to rely solely on your"
            + " problem-solving skills.");
    noMoreHintsText.add(
        "The AI's hint circuits are overheating. You're on your own from this point on.");

    // add fourth five messages
    noMoreHintsText.add(
        "Your interstellar hint subscription has expired. It's time to show your mettle as an"
            + " astronaut.");
    noMoreHintsText.add(
        "The escape room's hint-generating reactor is offline. You're in for a true challenge"
            + " now.");
    noMoreHintsText.add(
        "Space is vast, and so is your potential. No more hints to assist you on this cosmic"
            + " journey.");
    noMoreHintsText.add(
        "Your hint requests have been intercepted by space pirates. It's time to fend for"
            + " yourself!");
    noMoreHintsText.add(
        "The countdown to escape is ticking, and so is the clock for hints. You've reached your"
            + " limit, astronaut. Proceed wisely.");
  }
}
