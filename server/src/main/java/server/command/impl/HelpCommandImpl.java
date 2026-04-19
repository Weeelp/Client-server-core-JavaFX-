package  server.command.impl;
import common.Response;
import server.command.Command;

public class HelpCommandImpl implements Command {

    @Override
    public Response execute (String[] args, Object data, String login){
        return new Response("200", "Success",
               "Справка по командам:\n" +
               "help - показать справку\n" +
               "exit - выход из программы\n" +
               "info - информация о коллекции тип, дата инициализации, количество элементов и т.д.)\n" +
               "show - вывести в стандартный поток вывода все элементы коллекции в строковом представлении\n" +
               "add {element} - добавить новый элемент в коллекцию\n" +
               "update - обновить файл\n" +
               "update_by_id {id} - обновить значение элемента коллекции, id которого равен заданному\n" +
               "remove_by_id {id} - удалить элемент из коллекции по его id\n" +
               "clear - очистить коллекцию\n" +
               "save - сохранить коллекцию в файл\n" +
               "execute_script {file_name} - считать и исполнить скрипт из указанного файла. В скрипте содержатся команды в таком же виде, в котором их вводит пользователь в интерактивном режиме.\n" +
               "remove_last - удалить последний элемент из коллекции\n" +
               "add_if_max {element} - добавить новый элемент в коллекцию, если его значение oscarsCount превышает значение oscarsCount наибольшего элемента этой коллекции\n" +
               "remove_greater {element} - удалить из коллекции все элементы, превышающие заданный\n" +
               "max_by_oscars_count - вывести любой объект из коллекции, значение поля oscarsCount которого является максимальным\n" +
               "print_ascending - вывести элементы коллекции в порядке возрастания\n" +
               "print_descending - вывести элементы коллекции в порядке убывания");
    }
} 