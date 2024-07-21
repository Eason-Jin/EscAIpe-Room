package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.GameState;
import nz.ac.auckland.se206.GameState.NextTask;
import nz.ac.auckland.se206.gpt.ChatMessage;
import nz.ac.auckland.se206.gpt.GptPromptEngineering;
import nz.ac.auckland.se206.gpt.openai.ApiProxyException;
import nz.ac.auckland.se206.gpt.openai.ChatCompletionResult.Choice;
import nz.ac.auckland.se206.managers.SceneManager.SceneUi;

/** Controller class for the riddle. */
public class RiddleController extends AiController {

  /**
   * Initializes the AI view, loading the riddle.
   *
   * @throws ApiProxyException if there is an error communicating with the API proxy
   */
  @FXML
  private void initialize() throws ApiProxyException {
    super.initialize(
        GptPromptEngineering.getRiddleWithGivenWord(
            GameState.riddleObject.toString().toLowerCase()));
    System.out.println(GameState.riddleObject);
  }

  /**
   * Navigates to chat view when the relevant button is clicked by the player.
   *
   * @param event the action event triggered by the go back button
   * @throws IOException if there is an I/O error
   */
  @FXML
  private void onClickChat(ActionEvent event) throws IOException {
    App.setRoot(SceneUi.CHAT);
  }

  @Override
  protected Task<Void> createGptTask(ChatMessage msg) {
    // create task to run GPT in separate thread
    Task<Void> task =
        new Task<Void>() {
          @Override
          protected Void call() throws Exception {
            lastMsg = runGpt(msg);
            if (lastMsg.getRole().equals("assistant")
                && lastMsg.getContent().startsWith("Correct")) {
              // the player guessed the correct answer to the riddle
              GameState.isRiddleSolved.set(true);
              GameState.nextTask =
                  (GameState.currentScene == SceneUi.BEDROOM) ? NextTask.PAPER : NextTask.BEDROOM;
              disableInput();
            }
            return null;
          }
        };
    return task;
  }

  @Override
  protected void updateChatTextArea(String pastEntry, Choice result) {
    if (!GameState.isRiddleSolved.get()) {
      enableInputText();
    }
    super.updateChatTextArea(pastEntry, result);
  }
}
