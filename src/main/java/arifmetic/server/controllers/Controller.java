package arifmetic.server.controllers;

import arifmetic.server.ClientThread;
import arifmetic.server.Operations;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.regex.Pattern;

public class Controller extends Thread {

    private Thread server;
    private boolean isInterrupt = true;
    private int port;

    @FXML
    ToggleButton serverButton;

    @FXML
    TextField addressField;

    @FXML
    TextField portField;

    public void initialize() {

        server = new Thread(this);

        portField.textProperty().addListener(new ChangeListener<String>() {
            // проверка ввода для поля порта
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {

                Pattern notNumber = Pattern.compile("\\D");

                if (notNumber.matcher(newValue).find()) {
                    portField.setText(oldValue);
                }

                if (newValue.length() > 5) {
                    portField.setText(oldValue);
                }
            }
        });

    }

    // срабатывает по нажатию кнопки "Инициализировать"
    @FXML
    public void init() {
        try {
            // отключаем возможность использования сервера
            serverButton.setDisable(true);
            server.interrupt();
            // скачиваем конфигурацию и парсим
            getConfig();
            // можно включать сервер
            serverButton.setDisable(false);
        } catch (Exception e) {
            System.out.println("Возникла ошибка во время чтения файла конфигурации");
        }
    }

    public void getConfig() throws Exception {
        String port = portField.getText();
        String address = addressField.getText();
        String configAddress = address + ":" + port + "/config.xml";

        // инициализация xml документа для парсинга
        Document config = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(configAddress);

        // список всех методов из конфигурации
        NodeList methods = config.getElementsByTagName("cmd");

        // парсинг каждого метода
        for (int i = 0; i < methods.getLength(); i++) {
            NamedNodeMap attributes = methods.item(i).getAttributes();
            Short key = Short.parseShort(attributes.getNamedItem("value").getNodeValue(), 16);
            String name = attributes.getNamedItem("method").getNodeValue();
            // добавляем в словарь операций
            Operations.addMethod(key, name);
        }

        // порт для сервера
        this.port = Integer.parseInt(config.getDocumentElement().getAttribute("port"));


    }

    @FXML
    public void listenClients() {
        if (serverButton.isSelected()) {
            // начинаем прослушивание клиентов
            isInterrupt = false;
            server.start();
            serverButton.setText("Сервер работает");
        } else {
            // останавливаем работу сервера
            isInterrupt = true;
            serverButton.setText("Сервер не работает");
        }
    }

    public void run() {

        try {
            ServerSocket serverSocket = new ServerSocket(port);
            while (true) {
                if (isInterrupt) {
                    break;
                }
                // ждем подключение клиента
                Socket client = serverSocket.accept();

                // запускаем цикл обработки клиента
                Thread thread = new Thread(new ClientThread(client));
                thread.start();

            }
        } catch (IOException e) {
            System.out.println("Возникла ошибка во время работы сервера");
        }
    }
}
