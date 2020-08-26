package processor;

import exception.DukeException;
import exception.FileCorruptedException;
import task.DeadlineTask;
import task.EventTask;
import task.Task;
import task.ToDoTask;
import task.TaskList;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.PatternSyntaxException;

/**
 * Converts data from file to TaskList class.
 *
 * <p>The 'FileToTaskListConverter' supports operators, supported include: </p>
 *
 * <p> (i) converting file to TaskList object </p>
 *
 * <p> (ii) saving TaskList data into file provided </p>
 */
public abstract class FileToTaskListConverter {

    /**
     * Converts file to TaskList object.
     *
     * @param data the file loaded to be converted.
     * @return a TaskList containing all tasks inscribed in file
     * @throws FileCorruptedException If file is not formmatted in the correct order. ie
     *  not in <type>//<done status>//<task description>//<date if applicable>//<time if applicable>
     */
    public static TaskList convert(File data) throws FileCorruptedException {
        List<String> dataList = loadStringData(data);
        List<Task> list = new ArrayList<>();

        for (int i = 0; i < dataList.size(); i++) {
            list.add(getTaskFromData(dataList.get(i)));
        }

        return new TaskList(list);
    }

    /**
     * Saves TaskList to file provided.
     * Data is stored in <type>//<done status>//<task description>//<date if applicable>//<time if applicable>
     * @param list the TaskList to be saved.
     * @param file the file to be saved in.
     * @return save status of TaskList. True if successful, false otherwise.
     */
    public static boolean saveToFile(TaskList list, File file) {
        try {
            FileWriter fw = new FileWriter(file);
            StringBuilder sb = new StringBuilder();
            for(int i = 0; i < list.size(); i++) {
                Task task = list.getTask(i);
                String type = task.getType();

                sb.append(type + "//");
                sb.append(task.isDone() ? "✓" : "✘");
                sb.append("//");
                sb.append(task.getDescription() + "//");

                if (type.equals("D") || type.equals("E")) {
                    sb.append(task.getDateInput() + "//");
                    sb.append(task.getTimeInput());
                }
                sb.append("\n");
            }
            fw.write(sb.toString());
            fw.close();
            return true;
        } catch (IOException e) {
            System.out.println("Unable to save");
            return false;
        }
    }

    private static List<String> loadStringData(File data) {
        try {
            List<String> list = new ArrayList<>();
            Scanner sc = new Scanner(data);
            while (sc.hasNext()) {
                list.add(sc.nextLine());
            }
            sc.close();
            return list;
        } catch (FileNotFoundException e) {
            File directory = data.getParentFile();

            try {
                if (directory.exists() && directory.isDirectory()) {
                    data.createNewFile();
                } else {
                    directory.mkdirs();
                    data.createNewFile();
                }
            } catch (IOException ioe) {
                System.out.println("Unable to open/create file");
            }
            return new ArrayList<>();
        }
    }

    private static Task getTaskFromData(String info) throws FileCorruptedException {
        try {
            String[] words = info.split("//");
            char type = words[0].charAt(0);
            boolean isDone = words[1].equals("✓");
            String description = words[2];

            switch (type) {
            case 'T':
                return new ToDoTask(description, isDone);
            case 'D':
                return new DeadlineTask(description, words[3] + " " + words[4], isDone);
            case 'E':
                return new EventTask(description, words[3] + " " + words[4], isDone);
            default:
                return new Task("No Task !!!!");
            }
        } catch (DukeException de) {
            throw new FileCorruptedException();
        } catch (IndexOutOfBoundsException ioobe) {
            throw new FileCorruptedException();
        } catch (PatternSyntaxException pse) {
            throw new FileCorruptedException();
        }
    }
}
