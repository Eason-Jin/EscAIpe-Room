package nz.ac.auckland.se206.gpt;

import nz.ac.auckland.se206.GameState;

/** Utility class for generating GPT prompt engineering strings. */
public class GptPromptEngineering {

  /**
   * Generates a GPT prompt engineering string for a riddle with the given word.
   *
   * @param wordToGuess the word to be guessed in the riddle
   * @return the generated prompt engineering string
   */
  public static String getRiddleWithGivenWord(String wordToGuess) {
    return "Write a rhyming riddle of 4 lines with answer '"
        + wordToGuess // the lowercase enum value of current object
        + "''. Start with only the riddle and let the user guess the answer and ask for hints, but"
        + " never give the answer. if user guesses answer correctly say 'Correct' and mention the"
        + " object is in the bedroom, else if user says 'correct' say 'Incorrect', else if"
        + " incorrect say 'Incorrect' and encourage user creatively. Only if user asks for a hint,"
        + " which include but not limited to asking for what to do now or what to do next, you must"
        + " start with 'Hint:'.";
  }

  /**
   * Get the prompt for the chat view AI.
   *
   * @return the chat prompt
   */
  public static String startChat() {
    return "You are a helpful AI game master guiding a player who has to escape a space station."
        + " Start by saying hello. Only if the player asks for a hint, which include but not"
        + " limited to asking for what to do now or what to do next, start with 'Hint: You"
        + " need to "
        + getHint() // insert hint based on current next task to complete
        + "'. Respond in a friendly sophisticated manner straight to the point";
  }

  /**
   * Helper method to get the hint for the next task the player has to complete.
   *
   * @return a string with the required hint
   */
  private static String getHint() {
    switch (GameState.nextTask) {
      case RIDDLE:
        return "switch to RIDDLE mode and solve";
      case BEDROOM:
        return "go to the bedroom";
      case PAPER:
        return "find the paper under the object";
      case WIRES:
        return "go to the airlock and connect the broken wires";
      case CONTROL_CONSOLE:
        return "find the control console and enter the code";
      case ESCAPE:
        return "escape using the space shuttle";
      case SUCCESS:
        // no need for a hint because player has solved all challenges
        return "relax, you have already escaped";
      case FAIL:
        // player has to restart to be able to win
        return "restart the game, you failed";
      default:
        return "";
    }
  }
}
