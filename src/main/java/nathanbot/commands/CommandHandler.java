/**
 * This code has been refined with the assistance of Copilot to comply with the Java Style Guide.
 * Documentation comments have been generated by Copilot.
 * For further information, please refer to the AI.md.
 */
package nathanbot.commands;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import nathanbot.tasks.Deadline;
import nathanbot.tasks.Event;
import nathanbot.tasks.Task;
import nathanbot.tasks.TaskList;
import nathanbot.tasks.ToDo;

/**
 * Handles various commands for managing tasks.
 */
public abstract class CommandHandler {
    private static final String LINE = "____________________________________________________________\n";
    private static final String EXIT = "Bye. Hope to see you again soon!\n";
    private static final String GREET = """
                        Hello! I'm NathanBot
                        What can I do for you?
                       """;

    /**
     * Handles the greet command by printing a greeting message.
     *
     * @return A greeting message.
     */
    public static String handleGreet() {
        return GREET;
    }

    /**
     * Handles the exit command by printing a farewell message.
     *
     * @return A farewell message.
     */
    public static String handleExit() {
        return EXIT;
    }

    /**
     * Handles the display list command by printing the task list.
     *
     * @param taskList The task list to display.
     * @return A string representation of the task list.
     */
    public static String handleDisplayList(TaskList taskList) {
        return taskList.toString();
    }

    /**
     * Handles the mark command, marking a task as done or undone.
     *
     * @param input The user input containing the command and task number.
     * @param command The command string (e.g., "mark" or "unmark").
     * @param taskList The list of tasks.
     * @param isDone True if the task should be marked as done, false otherwise.
     * @return A message indicating the result of the mark command.
     */
    public static String handleMarkCommand(String input, String command, TaskList taskList, boolean isDone) {
        // Logic implemented by me; syntax and formatting recommended by Copilot.
        assert input.startsWith(command) : "Input should start with the command";

        try {
            int index = Integer.parseInt(input.substring(command.length()).trim());
            if (isDone) {
                taskList.markAsDone(index - 1);
                return "Nice! I've marked this task as done:\n  " + taskList.getTask(index - 1) + "\n";
            } else {
                taskList.markAsUndone(index - 1);
                return "OK, I've marked this task as not done yet:\n  " + taskList.getTask(index - 1) + "\n";
            }
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            return "Invalid task number. To see the list of tasks, use: list\n";
        }
    }

    /**
     * Handles the tag command, tagging a task.
     *
     * @param input The user input containing the command and task number.
     * @param taskList The list of tasks.
     * @return A message indicating the result of the mark command.
     */
    public static String handleTagCommand(String input, TaskList taskList) {
        // Logic implemented by me; syntax and formatting recommended by Copilot.
        assert input.startsWith(CommandType.TAG.getCommand()) : "Input should start with the command";

        try {
            String[] parts = input.substring(CommandType.TAG.getCommand().length()).trim().split(" ");
            int index = Integer.parseInt(parts[0]);
            String tag = parts[1];

            if (index < 0 || index >= taskList.listLength()) {
                return "Invalid task number. To see the list of tasks, use: list\n";
            }

            taskList.tagTask(index, tag);

            return "Task " + index + " tagged with " + tag + "\n";
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            return "Invalid task number. To see the list of tasks, use: list\n";
        }
    }

    /**
     * Handles the delete command by removing a task from the task list.
     *
     * @param input The user input containing the command and task number.
     * @param taskList The list of tasks.
     * @return A message indicating the result of the delete command.
     */
    public static String handleDeleteCommand(String input, TaskList taskList) {
        // Logic implemented by me; syntax and formatting recommended by Copilot.
        assert input.startsWith(CommandType.DELETE.getCommand()) : "Input should start with the command";

        try {
            int index = Integer.parseInt(input.substring(CommandType.DELETE.getCommand().length()).trim()) - 1;
            Task task = taskList.getTask(index);
            taskList.deleteTask(index);
            return "Noted. I've removed this task:\n"
                + task + "\nNow you have " + taskList.listLength() + " tasks in the list.\n";
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            return "Invalid task number. To see the list of tasks, use: list\n";
        }
    }

    /**
     * Handles the todo command by adding a todo task to the task list.
     *
     * @param input The user input containing the command.
     * @param taskList The list of tasks.
     * @return A message indicating the result of the todo command.
     */
    public static String handleTodoCommand(String input, TaskList taskList) {
        // Logic implemented by me; syntax and formatting recommended by Copilot.
        assert input.startsWith(CommandType.TODO.getCommand()) : "Input should start with the command";

        input = input.substring(CommandType.TODO.getCommand().length()).trim();
        if (input.isEmpty()) {
            return "The description of a todo cannot be empty. Use: todo <description>\n";
        }
        ToDo task = new ToDo(input);
        taskList.addTask(task);
        assert taskList.containsTask(task) : "Task should be added to the task list";

        return printAddTaskLine(task, taskList);
    }

    /**
     * Handles the deadline command by adding a deadline task to the task list.
     * It has to be in this format: deadline &lt;description&gt; /by &lt;date&gt
     *
     * @param input The user input containing the command.
     * @param taskList The list of tasks.
     * @return A message indicating the result of the deadline command.
     */
    public static String handleDeadlineCommand(String input, TaskList taskList) {
        // Logic implemented by me; syntax and formatting recommended by Copilot.
        assert input.startsWith(CommandType.DEADLINE.getCommand()) : "Input should start with the command";

        input = input.substring(CommandType.DEADLINE.getCommand().length()).trim();

        String[] parts = input.split(" /by ");
        if (parts.length < 2) {
            return "Invalid deadline format. "
                + "Use: deadline <description> /by <date>\n";
        }

        String description = parts[0].trim();
        String by = parts[1].trim();
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HHmm");
            LocalDateTime deadline = LocalDateTime.parse(by, formatter);
            Deadline task = new Deadline(description, deadline);

            taskList.addTask(task);
            assert taskList.containsTask(task) : "Task should be added to the task list";

            return printAddTaskLine(task, taskList);
        } catch (DateTimeParseException e) {
            return "Invalid date format. Please use dd/MM/yyyy HHmm.\n";
        }
    }

    /**
     * Handles the event command by adding an event task to the task list.
     * It has to be in this format: event &lt;description&gt; /from &lt;start time&gt; /to &lt;end time&gt;.
     *
     * @param input The user input containing the command.
     * @param taskList The list of tasks.
     * @return A message indicating the result of the event command.
     */
    public static String handleEventCommand(String input, TaskList taskList) {
        // Logic implemented by me; syntax and formatting recommended by Copilot.
        assert input.startsWith(CommandType.EVENT.getCommand()) : "Input should start with the command";

        input = input.substring(CommandType.EVENT.getCommand().length()).trim();

        String[] parts = input.split(" /from | /to ");
        if (parts.length < 3) {
            return "Invalid event format. Use: event <description> /from <start time> /to <end time>\n";
        }

        String description = parts[0].trim();
        String from = parts[1].trim();
        String to = parts[2].trim();
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HHmm");
            LocalDateTime fromDateTime = LocalDateTime.parse(from, formatter);
            LocalDateTime toDateTime = LocalDateTime.parse(to, formatter);
            Event task = new Event(description, fromDateTime, toDateTime);

            taskList.addTask(task);
            assert taskList.containsTask(task) : "Task should be added to the task list";

            return printAddTaskLine(task, taskList);
        } catch (DateTimeParseException e) {
            return "Invalid date format. Please use dd/MM/yyyy HHmm.\n";
        }
    }

    /**
     * Handles the find command by finding a task based on the search string.
     *
     * @param input The user input containing the command.
     * @param taskList The list of tasks.
     * @return A message indicating the result of the find command.
     */
    public static String handleFindCommand(String input, TaskList taskList) {
        // Logic implemented by me; syntax and formatting recommended by Copilot.
        assert input.startsWith(CommandType.FIND.getCommand()) : "Input should start with the command";

        String searchString = input.substring(CommandType.FIND.getCommand().length()).trim();
        TaskList tasksFound = taskList.find(searchString);
        if (tasksFound.isEmpty()) {
            return "No tasks found containing: " + searchString + "\n";
        } else {
            return "Here are the matching tasks in your list:\n" + tasksFound;
        }
    }

    /**
     * Handles unknown commands.
     *
     * @return A message indicating an unknown command.
     */
    public static String handleUnknownCommand() {
        return "Unknown Command, womp womp.\n";
    }

    /**
     * Prints the message after adding a task to the task list.
     *
     * @param task The task that was added.
     * @param taskList The list of tasks.
     * @return A message indicating the task was added.
     */
    private static String printAddTaskLine(Task task, TaskList taskList) {
        return "Got it. I've added this task: \n"
            + task + "\nNow you have " + taskList.listLength() + " tasks in the list.\n";
    }
}
