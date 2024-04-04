package org.example;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.restassured.response.Response;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static io.restassured.RestAssured.*;
import static io.restassured.RestAssured.baseURI;

public class Main {
    //prod
//    static String baseURIStage = "https://omscloud.ismet.kz/";
//    static String omsId = "f5d083d2-a053-415a-9644-5d9f2d57ecc1";
//    static String clientToken = "b960edc4-ae85-4de2-9a6a-c40ce6aff594";
//    static String baseURIMPT = "https://elk.prod.markirovka.ismet.kz/api/v3/true-api/";

    //stage
    static String baseURIStage = "https://suzcloud.stage.ismet.kz/";
    static String omsId = "d9cdba15-0536-4c28-a328-a42fe99d931a";
    static String clientToken = "ed310465-2759-4b1d-be06-1f027b59e245";
    static String baseURIMPT = "https://stage.ismet.kz/api/v3/true-api/";


    static ArrayList<String> orders = new ArrayList<>();

    static ArrayList<String> ki = new ArrayList<>();
    static ArrayList<String> one = new ArrayList<>();
    static ArrayList<String> two = new ArrayList<>();

    static JTextArea resp = new JTextArea(10, 30);
    static JTextArea saveInfo = new JTextArea(10, 30);
    static JTextArea setOrder = new JTextArea(10, 30);


    static String encodedString = null;
    static byte[] ecp;
    static String finalXml = null;
    static String token;
    static ArrayList<String> forBufferStatus = null;

    public static void main(String[] args) {

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException |
                 UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        // Немного улучшим стиль кнопок
        UIManager.put("Button.font", new Font("Arial", Font.PLAIN, 14));
        UIManager.put("Button.background", Color.LIGHT_GRAY);
        UIManager.put("Button.foreground", Color.BLACK);

        // Немного улучшим стиль Label
        UIManager.put("Label.font", new Font("Arial", Font.PLAIN, 14));
        UIManager.put("Label.background", Color.LIGHT_GRAY);
        UIManager.put("Label.foreground", Color.BLACK);

        // Немного улучшим стиль Panel
        UIManager.put("TabbedPane.font", new Font("Arial", Font.PLAIN, 14));
        UIManager.put("TabbedPane.background", Color.lightGray);
        UIManager.put("TabbedPane.foreground", Color.BLACK);

        // Немного улучшим стиль TextArea
        UIManager.put("TextArea.font", new Font("Arial", Font.PLAIN, 14));
        UIManager.put("TextArea.background", Color.WHITE);
        UIManager.put("TextArea.foreground", Color.BLACK);


        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("ApplicationByNK");
            frame.setSize(900, 600);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            JTabbedPane tabbedPane = new JTabbedPane();
            // Заказ КМ
            JPanel tab1 = new JPanel();
            tab1.setLayout(null);

            JLabel product = new JLabel("Товар:");
            product.setBounds(10, 10, 150, 15);

            JTextArea gtin = new JTextArea(10, 30);
            gtin.setLineWrap(true);
            gtin.setWrapStyleWord(true);
            JScrollPane scrollPane = new JScrollPane(gtin);
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            scrollPane.setBounds(10, 40, 150, 250);

            JTextArea gtinSize = new JTextArea(10, 30);
            gtinSize.setLineWrap(true);
            gtinSize.setWrapStyleWord(true);
            gtinSize.setBounds(100, 10, 30, 18);
            tab1.add(gtinSize);

            JTextArea quantitySize = new JTextArea(10, 30);
            quantitySize.setLineWrap(true);
            quantitySize.setWrapStyleWord(true);
            quantitySize.setBounds(280, 10, 30, 18);
            tab1.add(quantitySize);

            JLabel qt = new JLabel("Количество:");
            qt.setBounds(180, 10, 100, 15);

            JTextArea quantity = new JTextArea(10, 30);
            quantity.setLineWrap(true);
            quantity.setWrapStyleWord(true);
            JScrollPane scrollPane1 = new JScrollPane(quantity);
            scrollPane1.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            scrollPane1.setBounds(180, 40, 100, 250);

            //Кнопка "Создать заказ"
            JButton sendOrder = new JButton("Создать заказ");
            sendOrder.setBounds(10, 300, 150, 30);

            //Окно для ответа
            resp.setLineWrap(true);
            resp.setWrapStyleWord(true);
            JScrollPane scrollPane2 = new JScrollPane(resp);
            scrollPane2.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            scrollPane2.setBounds(10, 350, 300, 80);


            //Читает данные по факту
            gtin.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    updateSize();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    updateSize();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    updateSize();
                }

                private void updateSize() {
                    String[] lines = gtin.getText().split("\n");
                    gtinSize.setText(String.valueOf(lines.length));
                }
            });
            //Читает данные по факту
            quantity.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    updateSize();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    updateSize();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    updateSize();
                }

                private void updateSize() {
                    String[] lines = quantity.getText().split("\n");
                    quantitySize.setText(String.valueOf(lines.length));
                }
            });


            //Выполнение Кнопка "Создать заказ"
            sendOrder.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {

                    String gtinValue = gtin.getText();
                    ArrayList<String> gv = Scanning.scan(gtinValue);

                    forBufferStatus = Scanning.scan(gtinValue);

                    String quantityValue = quantity.getText();
                    ArrayList<String> qv = Scanning.scan(quantityValue);

                    // Создаем объект model
                    JsonObject model = new JsonObject();
                    model.addProperty("contactPerson", "admin");
                    model.addProperty("releaseMethodType", "IMPORT");
                    model.addProperty("createMethodType", "SELF_MADE");
                    model.addProperty("country", "CN");

                    // Создаем productsArray
                    JsonArray productsArray = new JsonArray();
                    for (int i = 0; i < gv.size(); i++) {
                        JsonObject productObject = new JsonObject();
                        productObject.addProperty("gtin", gv.get(i));
                        productObject.addProperty("quantity", qv.get(i));
                        productObject.addProperty("serialNumberType", "OPERATOR");
                        productObject.addProperty("templateId", 1);
                        //собираем наши объекты products в объект productObject
                        productsArray.add(productObject);

                        if (productsArray.size() == 10 || i == gv.size() - 1) {
                            // В этом месте массив productsArray добавляется в объект model
                            model.add("products", productsArray);
                            Gson gson = new Gson();
                            String json = gson.toJson(model);
                            testPost(json);
                            // Очищаем productsArray для следующего блока
                            productsArray = new JsonArray();
                        }
                    }
//                    gtins.clear();
//                    quantities.clear();
                }
            });


            //Окно для проверки заказа
            JTextArea orderId = new JTextArea(10, 30);
            orderId.setLineWrap(true);
            orderId.setWrapStyleWord(true);
            JScrollPane scrollPane3 = new JScrollPane(orderId);
            scrollPane3.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            scrollPane3.setBounds(350, 40, 300, 80);

            //Кнопка "Проверить заказ"
            JButton infoOrder = new JButton("Проверить заказ");
            infoOrder.setBounds(350, 130, 150, 30);

            //Окно для ответа по проверке заказа
            JTextArea TextInfoOrder = new JTextArea(10, 30);
            TextInfoOrder.setLineWrap(true);
            TextInfoOrder.setWrapStyleWord(true);
            TextInfoOrder.setCaretColor(Color.red);
            JScrollPane scrollPane4 = new JScrollPane(TextInfoOrder);
            scrollPane4.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            scrollPane4.setBounds(350, 180, 500, 80);

            //Выполнение Кнопка "Проверить заказ"
            infoOrder.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String textOrderId = orderId.getText();
                    String bufferStatus = null;
                    baseURI = baseURIStage;
                    basePath = "/api/v2/shoes/buffer/status";
                    Response response =
                            given().log().all()
                                    .queryParam("omsId", omsId)
                                    .queryParam("orderId", textOrderId)
                                    .queryParam("gtin", forBufferStatus.get(0))
                                    .headers("clientToken", clientToken)
                                    .when().get()
                                    .then()
                                    .extract().response();
                    String place = response.asPrettyString();
                    System.out.println(place);

                    JsonObject jsonObject = new JsonParser().parse(place).getAsJsonObject();
                    bufferStatus = jsonObject.get("bufferStatus").getAsString();
                    if (bufferStatus.equals("PENDING")) {
                        TextInfoOrder.setText("ЗАКАЗ ФОРМИРУЕТСЯ");
                    } else if (bufferStatus.equals("ACTIVE")) {
                        TextInfoOrder.setText("МОЖЕТЕ ПОЛУЧАТЬ КОДЫ!");
                    } else if (response.statusCode() == 400) {
                        TextInfoOrder.setText(place);
                    }
                }
            });

            tab1.add(new JLabel(""));
            tab1.add(product);
            tab1.add(qt);
            tab1.add(scrollPane);
            tab1.add(scrollPane1);
            tab1.add(sendOrder);
            tab1.add(scrollPane2);
            tab1.add(infoOrder);
            tab1.add(scrollPane3);
            tab1.add(scrollPane4);
            tabbedPane.addTab("Заказ КМ", tab1);

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

            //Получение КМ
            JPanel tab2 = new JPanel();
            tab2.setLayout(null);

//            JLabel product1 = new JLabel("Товар:");
//            product1.setBounds(10, 10, 150, 15);
//
//            JTextArea getGtin = new JTextArea(10, 30);
//            getGtin.setLineWrap(true);
//            getGtin.setWrapStyleWord(true);
//            JScrollPane scrollPane5 = new JScrollPane(getGtin);
//            scrollPane5.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
//            scrollPane5.setBounds(10, 40, 150, 250);
//
//            JLabel qt1 = new JLabel("Количество:");
//            qt1.setBounds(180, 10, 100, 15);
//
//            JTextArea getQuantity = new JTextArea(10, 30);
//            getQuantity.setLineWrap(true);
//            getQuantity.setWrapStyleWord(true);
//            JScrollPane scrollPane6 = new JScrollPane(getQuantity);
//            scrollPane6.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
//            scrollPane6.setBounds(180, 40, 100, 250);

            //Путь сохранения
//            JTextArea waySaveCodes = new JTextArea(10, 30);
//            waySaveCodes.setLineWrap(true);
//            waySaveCodes.setWrapStyleWord(true);
//            JScrollPane scrollPane8 = new JScrollPane(waySaveCodes);
//            scrollPane8.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
//            scrollPane8.setBounds(10, 40, 300, 30);

            //Выбрать папку для сохранения КМ
            JLabel labelSave = new JLabel("Укажите путь для сохранения:");
            labelSave.setBounds(10, 10, 300, 15);

            JButton chooseFolderButton3 = new JButton("Выбрать папку");
            chooseFolderButton3.setBounds(10, 30, 300, 30);
            JTextField waySaveCodes = new JTextField(30);
            waySaveCodes.setBounds(10, 60, 300, 30);
            waySaveCodes.setEditable(false);


            // Добавление слушателя для кнопки "Выбрать папку"
            chooseFolderButton3.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JFileChooser fileChooser = new JFileChooser();
                    fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

                    int result = fileChooser.showOpenDialog(null);

                    if (result == JFileChooser.APPROVE_OPTION) {
                        File selectedFileOrFolder = fileChooser.getSelectedFile();
                        waySaveCodes.setText(selectedFileOrFolder.getAbsolutePath());
                    }
                }
            });

            JLabel labelOrder = new JLabel("Укажите номер заказа:");
            labelOrder.setBounds(10, 100, 200, 15);

            //окно для номера заказа

            setOrder.setLineWrap(true);
            setOrder.setWrapStyleWord(true);
            JScrollPane scrollPane9 = new JScrollPane(setOrder);
            scrollPane9.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            scrollPane9.setBounds(10, 120, 300, 150);

            //Кнопка "Получить коды"
            JButton getKM = new JButton("Получить коды");
            getKM.setBounds(10, 280, 150, 30);


            //Окно для ответа
            saveInfo.setLineWrap(true);
            saveInfo.setWrapStyleWord(true);
            JScrollPane scrollPane7 = new JScrollPane(saveInfo);
            scrollPane7.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            scrollPane7.setBounds(400, 40, 400, 400);


            //Действия кнопки getKM
            getKM.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {

                    // Создаем новый поток для выполнения запроса к API
                    Thread apiThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                String gtinValue = gtin.getText();
                                ArrayList<String> gs = Scanning.scan(gtinValue);

                                String quantityValue = quantity.getText();
                                ArrayList<String> qv = Scanning.scan(quantityValue);

                                String idss = setOrder.getText();
                                ArrayList<String> idS = Scanning.scan(idss);

                                for (int k = 0; k < gs.size(); k++) {

                                    String orderId = idS.get(k);
                                    for (int i = k * 10; i < (k * 10 + 10); i++) {
                                        baseURI = baseURIStage;
                                        basePath = "/api/v2/shoes/codes";

                                        Response response = given().log().all()
                                                .headers("clientToken", clientToken)
                                                .queryParam("omsId", omsId)
                                                .queryParam("orderId", orderId)
                                                .queryParam("gtin", gs.get(i))
                                                .queryParam("quantity", qv.get(i))
                                                .when().get();

                                        String place = response.asString();
                                        if (response.statusCode() != 200) {
                                            saveInfo.append("Тут вы вносите неверные данные: Товар: " + gs.get(i) + " Количество: " + qv.get(i) + "\n");

                                            idS.clear();
                                            gs.clear();
                                            qv.clear();
                                        } else {
                                            ki.add(gs.get(i) + " Количество: " + qv.get(i));
                                            saveInfo.append("Получено: Товар: " + ki.get(ki.size() - 1) + "\n");
                                        }

                                        Gson gson = new Gson();
                                        Resp resp = gson.fromJson(place, Resp.class);

                                        String text = "";
                                        for (int j = 0; j < resp.getCodes().size(); j++) {
                                            text += resp.getCodes().get(j) + "\n";
                                        }
                                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy.HH.mm.ss.SSS");
                                        Date date = new Date();
                                        String formattedDate = dateFormat.format(date);

                                        FileWriter fileWriter = null;
                                        try {
                                            fileWriter = new FileWriter(waySaveCodes.getText() + "\\" + gs.get(i) + formattedDate + ".txt");
                                            fileWriter.write(text);
                                            fileWriter.flush();
                                        } catch (IOException ex) {
                                            ex.printStackTrace();
                                        } finally {
                                            if (fileWriter != null) {
                                                try {
                                                    fileWriter.close();
                                                } catch (IOException ex) {
                                                    ex.printStackTrace();
                                                }
                                            }
                                        }
                                    }
                                }

                                idS.clear();
                                gs.clear();
                                qv.clear();
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    });

                    // Запускаем поток
                    apiThread.start();
                }
            });
//            tab2.add(product1);
//            tab2.add(qt1);
//            tab2.add(scrollPane5);
//            tab2.add(scrollPane6);
            tab2.add(getKM);
            tab2.add(scrollPane7);
            //tab2.add(scrollPane8);
            tab2.add(chooseFolderButton3);
            tab2.add(waySaveCodes);
            tab2.add(labelSave);
            tab2.add(scrollPane9);
            tab2.add(labelOrder);
            //tab2.add(loadingWheel);
            tabbedPane.addTab("Получение КМ", tab2);

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

            //Объедение КМ
            JPanel tab3 = new JPanel();
            tab3.setLayout(null);

            JLabel labelSaveCodes = new JLabel("Укажите путь к папке где находятся КМ:");
            labelSaveCodes.setBounds(200, 40, 500, 15);

            //Путь к КМ
            JButton chooseFolderButton = new JButton("Выбрать папку");
            chooseFolderButton.setBounds(200, 80, 500, 30);
            JTextField WayToFolder = new JTextField(30);
            WayToFolder.setBounds(200, 120, 500, 30);
            WayToFolder.setEditable(false);

            JLabel labelSaveCodes1 = new JLabel("Укажите путь для сохранения:");
            labelSaveCodes1.setBounds(200, 170, 500, 15);

            //Путь сохранения КМ
            JButton chooseFolderButton1 = new JButton("Выбрать папку");
            chooseFolderButton1.setBounds(200, 210, 500, 30);
            JTextField WayToSaveFolder = new JTextField(30);
            WayToSaveFolder.setBounds(200, 250, 500, 30);
            WayToSaveFolder.setEditable(false);

            //Кнопка "Объеденить файлы"
            JButton combine = new JButton("Объеденить файлы");
            combine.setBounds(200, 340, 500, 30);
            //Путь сохранения итог
            JTextArea waySaveCodes3 = new JTextArea(10, 30);
            waySaveCodes3.setLineWrap(true);
            waySaveCodes3.setWrapStyleWord(true);
            JScrollPane scrollPane12 = new JScrollPane(waySaveCodes3);
            scrollPane12.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            scrollPane12.setBounds(200, 380, 600, 50);


            // Добавление слушателя для кнопки выбора папки
            chooseFolderButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JFileChooser fileChooser = new JFileChooser();
                    fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

                    int result = fileChooser.showOpenDialog(null);

                    if (result == JFileChooser.APPROVE_OPTION) {
                        File selectedFileOrFolder = fileChooser.getSelectedFile();
                        WayToFolder.setText(selectedFileOrFolder.getAbsolutePath());
                    }
                }
            });

            // Добавление слушателя для кнопки выбора папки
            chooseFolderButton1.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JFileChooser fileChooser = new JFileChooser();
                    fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

                    int result = fileChooser.showOpenDialog(null);

                    if (result == JFileChooser.APPROVE_OPTION) {
                        File selectedFileOrFolder = fileChooser.getSelectedFile();
                        WayToSaveFolder.setText(selectedFileOrFolder.getAbsolutePath());
                    }
                }
            });

            //Действие кнопки combine
            combine.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {

                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy.HH.mm.ss.SSS");
                    Date date = new Date();
                    String formattedDate = dateFormat.format(date);

                    ArrayList<String> name = new ArrayList<>();

                    String path = WayToFolder.getText();
                    File file = new File(path);
                    if (file.exists() && file.isDirectory()) {
                        File[] files = file.listFiles();

                        if (files != null) {
                            for (File file1 : files) {
                                String fileName = file1.getName();
                                //System.out.println(fileName);
                                name.add(fileName);
                            }
                        }
                    }
                    FileWriter fileWriter = null;
                    try {
                        fileWriter = new FileWriter(WayToSaveFolder.getText() + "\\Все КМ в файле " + formattedDate + ".txt");
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                    for (int i = 0; i < name.size(); i++) {

                        try (BufferedReader reader = new BufferedReader(new FileReader(path + "\\" + name.get(i)))) {
                            String line;

                            while ((line = reader.readLine()) != null) {
                                System.out.println(line);
                                fileWriter.write(line + "\n");
                            }
                            fileWriter.flush();
                        } catch (IOException ex) {
                            //throw new RuntimeException(ex);
                        }
                    }
                    try {
                        fileWriter.close();
                    } catch (IOException ex) {
                        //throw new RuntimeException(ex);
                        System.err.println("Хуйня");
                    }
                    name.clear();
                    waySaveCodes3.setText("Файлы успешно объедены в: " + WayToSaveFolder.getText() + "\\Все КМ в файле " + formattedDate + ".txt");
                }
            });

            tab3.add(labelSaveCodes);
            //tab3.add(scrollPane10);
            tab3.add(labelSaveCodes1);
            //tab3.add(scrollPane11);
            tab3.add(chooseFolderButton1);
            tab3.add(scrollPane12);
            tab3.add(combine);
            tab3.add(chooseFolderButton);
            tab3.add(WayToFolder);
            //tab3.add(wayToSaveFolder);
            tab3.add(WayToSaveFolder);
            tabbedPane.addTab("Объедение КМ", tab3);

            ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            //Уведомление о ввозе (третьи страны
            JPanel tab4 = new JPanel();
            tab4.setLayout(null);

            JLabel regNumber = new JLabel("Регистрационный номер:");
            regNumber.setBounds(10, 10, 200, 15);
            JTextArea regNumberShow = new JTextArea(1, 30);
            regNumberShow.setLineWrap(true);
            regNumberShow.setWrapStyleWord(true);
            regNumberShow.setBounds(10, 30, 200, 20);
            //

            JLabel regDate = new JLabel("Дата регистрации: 2023-12-12");
            regDate.setBounds(250, 10, 500, 15);
            JTextArea regDateShow = new JTextArea(1, 30);
            regDateShow.setLineWrap(true);
            regDateShow.setWrapStyleWord(true);
            regDateShow.setBounds(250, 30, 200, 20);
            //
            JLabel decisionCode = new JLabel("Код решения:");
            decisionCode.setBounds(10, 60, 500, 15);
            JTextArea decisionCodeShow = new JTextArea(1, 30);
            decisionCodeShow.setLineWrap(true);
            decisionCodeShow.setWrapStyleWord(true);
            decisionCodeShow.setBounds(10, 80, 200, 20);
            //
            JLabel decisionDateTime = new JLabel("Дата решения решения: 2023-12-12");
            decisionDateTime.setBounds(250, 60, 600, 15);
            JTextArea decisionDateTimeShow = new JTextArea(1, 30);
            decisionDateTimeShow.setLineWrap(true);
            decisionDateTimeShow.setWrapStyleWord(true);
            decisionDateTimeShow.setBounds(250, 80, 250, 20);
            //
            JLabel customCode = new JLabel("Код таможенного органа:");
            customCode.setBounds(500, 10, 500, 15);
            JTextArea customCodeShow = new JTextArea(1, 30);
            customCodeShow.setLineWrap(true);
            customCodeShow.setWrapStyleWord(true);
            customCodeShow.setBounds(500, 30, 200, 20);

            //Путь к КМ
            JButton chooseFileKm = new JButton("Выбрать КМ");
            chooseFileKm.setBounds(10, 120, 250, 20);
            JTextField chooseFileKmWay = new JTextField(30);
            chooseFileKmWay.setBounds(10, 150, 400, 20);
            chooseFileKmWay.setEditable(false);
            // Добавление слушателя для кнопки выбора папки
            chooseFileKm.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JFileChooser fileChooser = new JFileChooser();
                    fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

                    int result = fileChooser.showOpenDialog(null);

                    if (result == JFileChooser.APPROVE_OPTION) {
                        File selectedFileOrFolder = fileChooser.getSelectedFile();
                        chooseFileKmWay.setText(selectedFileOrFolder.getAbsolutePath());
                    }
                }
            });

            //Путь к Выбрать ЭЦП
            JButton chooseFileECP = new JButton("Выбрать ЭЦП");
            chooseFileECP.setBounds(500, 120, 250, 20);
            JTextField chooseFileECPWay = new JTextField(30);
            chooseFileECPWay.setBounds(500, 150, 350, 20);
            chooseFileECPWay.setEditable(false);
            // Добавление слушателя для кнопки выбора папки Выбрать ЭЦП
            chooseFileECP.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JFileChooser fileChooser = new JFileChooser();
                    fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

                    int result = fileChooser.showOpenDialog(null);

                    if (result == JFileChooser.APPROVE_OPTION) {
                        File selectedFileOrFolder = fileChooser.getSelectedFile();
                        chooseFileECPWay.setText(selectedFileOrFolder.getAbsolutePath());
                    }
                }
            });
            //Пароль
            JLabel passwordLabel = new JLabel("Пароль:");
            passwordLabel.setBounds(500, 180, 100, 30);
            JTextArea password = new JTextArea(1, 30);
            password.setLineWrap(true);
            password.setWrapStyleWord(true);
            password.setBounds(580, 180, 100, 30);


            //Кнопка "Создать документ"
            JButton generateXML = new JButton("Создать документ");
            generateXML.setBounds(10, 180, 200, 30);
            //Окно готовности XML
            JTextArea finalXML = new JTextArea(1, 30);
            finalXML.setLineWrap(true);
            finalXML.setWrapStyleWord(true);
            finalXML.setBounds(10, 225, 500, 30);


            generateXML.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    //Получение по 1 методу
                    baseURI = baseURIMPT;
                    basePath = "auth/key";
                    Response response1 = (Response) given().log().all()
                            .when().get()
                            .then()
                            .statusCode(200)
                            .extract().response();
                    String key = response1.asString();
                    //System.out.println(place1);

                    //Парсинг json uuid и data
                    JsonObject jsonObject1 = new JsonParser().parse(key).getAsJsonObject();
                    String uuid = jsonObject1.get("uuid").getAsString();
                    String data = jsonObject1.get("data").getAsString();

                    //Необходимо подписать data=encodedString и после отправить в метод auth/simpleSignIn
                    ECPHelper ecpHelper = new ECPHelper();
                    File file = new File(chooseFileECPWay.getText());
                    FileInputStream fl = null;
                    try {
                        fl = new FileInputStream(file);
                    } catch (FileNotFoundException ex) {
                        throw new RuntimeException(ex);
                    }
                    ecp = new byte[(int) file.length()];
                    try {
                        fl.read(ecp);
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                    try {
                        fl.close();
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                    String s = ecpHelper.createPKCS7(data, ecp, password.getText(), true);
                    //System.out.println(s);

                    //Получение по 2 методу (token)
                    baseURI = baseURIMPT;
                    basePath = "auth/simpleSignIn";
                    Response response2 = (Response) given().log().all()
                            .header("Content-Type", "application/json")
                            .body("{" + "\"uuid\"" + ":" + "\"" + uuid + "\"" + "," + "\"data\"" + ":" + "\"" + s + "\"" + "}")
                            .when().post()
                            .then()
                            .statusCode(200)
                            .extract().response();
                    String simpleSignIn = response2.asPrettyString();
                    //System.out.println(place2);
                    JsonObject jsonObject2 = new JsonParser().parse(simpleSignIn).getAsJsonObject();
                    token = jsonObject2.get("token").getAsString();

                    String gtin = null;
                    String text = null;
                    String ki = null;

                    ArrayList<String> km = new ArrayList<>();
                    ArrayList<String> item = new ArrayList<>();

                    // Генерация нового GUID
                    UUID uuid1 = UUID.randomUUID();
                    String generatedGuid = uuid1.toString();
                    // Получение текущей даты и времени
                    LocalDateTime currentDateTime = LocalDateTime.now();
                    // Вычитание 6 часов
                    LocalDateTime modifiedDateTime = currentDateTime.minusHours(6);
                    // Преобразование в строку в нужном формате
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
                    String formattedDateTime = modifiedDateTime.format(formatter);

                    File file1 = new File(chooseFileKmWay.getText());
                    Scanner scanner = null;
                    try {
                        scanner = new Scanner(file1);
                    } catch (FileNotFoundException ex) {
                        throw new RuntimeException(ex);
                    }
                    String firstXML = null;
                    String secondXML = null;
                    while (scanner.hasNextLine()) {
                        String line = scanner.nextLine();
                        String[] ss = line.split(" ");
                        km.add(ss[0]);
                        StringBuilder stringBuilder = new StringBuilder();
                        for (String a : ss) {
                            stringBuilder.append(a);
                            stringBuilder.append(" ");
                            text = stringBuilder.toString();

                            // Задаем текст для кодирования
                            gtin = text.substring(2, 16);
                            ki = text.substring(0, 31);
                            System.out.println(ki);

                        }
                        try {
                            baseURI = baseURIMPT;
                            basePath = "products/listV2";
                            Response response =
                                    given().log().all()
                                            .queryParam("cis", ki)
                                            .headers("Content-Type", "application/json")
                                            .header("Authorization", "Bearer" + token)
                                            .when().get()
                                            .then()
                                            .statusCode(200)
                                            .extract().response();
                            String place = response.asPrettyString();
                            //System.out.println(place);

                            JsonObject jsonObject = new JsonParser().parse(place).getAsJsonObject();
                            JsonArray resultsArray = jsonObject.getAsJsonArray("results");
                            String cis = null;
                            String tnVedCode10 = null;
                            String gtin1 = null;

                            ki = ki.replace("<", "&lt;");
                            ki = ki.replace(">", "&gt;");
                            ki = ki.replace("\"", "&quot;");
                            ki = ki.replace("'", "&apos;");
                            ki = ki.replace("&", "&amp;");
                            if (resultsArray.size() > 0) {
                                JsonObject firstResult = resultsArray.get(0).getAsJsonObject();

                                //cis = firstResult.get("cis").getAsString();
                                gtin1 = firstResult.get("gtin").getAsString();
                                tnVedCode10 = firstResult.get("tnVedCode10").getAsString();

                            }

                            firstXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                    "<file xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" fileId=\"IMPORT_" + generatedGuid + "\" version=\"1.0\" sendingDateTime=\"" + formattedDateTime + "\" xsi:noNamespaceSchemaLocation=\"xsd_notific_import.xsd\">\n" +
                                    "\t<document>\n" +
                                    "\t\t<mainInfo>\n" +
                                    "\t\t\t<importerInfo importerName=\"name\" importerCode=\"960202350172\"/>\n" +
                                    "\t\t\t<exportCountry>CN</exportCountry>\n" +
                                    "\t\t\t<customDocument docType=\"DECLARATION\" regNumber=\"" + regNumberShow.getText() + "\" regDate=\"" + regDateShow.getText() + "\"/>\n" +
                                    "\t\t\t<customDecisionInfo decisionCode=\"10\" decisionDateTime=\"" + decisionDateTimeShow.getText() + "T00:00:00Z\" customCode=\"" + customCodeShow.getText() + "\"/>\n" +
                                    "\t\t\t<certificationInfo docType=\"Декларация о соответствии\" docNumber=\"ЕАЭС KG417/031.Д.0021456\" docDate=\"2022-09-23\"/>\n" +
                                    "\t\t</mainInfo>\n" +
                                    "\t\t<productItems>";
                            String xmlString = String.format(
                                    "<item iCodeFromDeclaration=\"%s\">\n" +
                                            "    <product tnved=\"%s\" gtin=\"%s\" originCountry=\"CN\" itemNumber=\"1\">\n" +
                                            "    </product>\n" +
                                            "</item>\n", ki, tnVedCode10, gtin1);
                            item.add(xmlString);

                            secondXML = "\t\t</productItems>\n" +
                                    "\t</document>\n" +
                                    "</file>";

                        } catch (Exception ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                    // Создаем StringBuilder для построения конечного XML-документа
                    StringBuilder xmlBuilder = new StringBuilder();
                    // Добавляем первую часть XML-документа
                    xmlBuilder.append(firstXML);
                    // Добавляем все сгенерированные XML-строки item
                    for (String itemXml : item) {
                        xmlBuilder.append(itemXml);
                    }
                    // Добавляем вторую часть XML-документа
                    xmlBuilder.append(secondXML);

                    // Получаем конечную XML-строку
                    finalXml = xmlBuilder.toString();
                    // Теперь finalXml содержит ваш полный XML-документ с добавленными элементами item
                    //System.out.println(finalXml);
                    finalXML.setText("Готово! Можете отправить документ");
                }
            });

            //Кнопка "Создать документ"
            JButton sendDoc = new JButton("Отправить документ");
            sendDoc.setBounds(10, 260, 200, 30);
            //Окно после нажатия Кнопка "Создать документ"
            JTextArea idDoc = new JTextArea(1, 30);
            idDoc.setLineWrap(true);
            idDoc.setWrapStyleWord(true);
            idDoc.setBounds(10, 300, 500, 60);

            sendDoc.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {

                    ECPHelper ecpHelper = new ECPHelper();
                    encodedString = Base64.getEncoder().encodeToString(finalXml.getBytes());

                    String signature = ecpHelper.createPKCS7(finalXml, ecp, password.getText(), false);//false при подписании документов
                    //System.out.println(signature);

                    baseURI = baseURIMPT;
                    basePath = "documents/transit/import/third_countries";
                    Response response =
                            given().log().all()
                                    .header("Content-Type", "application/json")
                                    .header("Authorization", "Bearer " + token)
                                    .body("{\"document\":" + "\"" + encodedString + "\"" + "," + "\"signature\":" + "\"" + signature + "\"" + "}")
                                    .when().post()
                                    .then()
                                    .statusCode(200)
                                    .extract().response();
                    String place = response.asPrettyString();
                    System.out.println(place);
                    idDoc.setText(place);
                }
            });


            tab4.add(regNumber);
            tab4.add(regNumberShow);
            tab4.add(regDate);
            tab4.add(regDateShow);
            tab4.add(decisionCode);
            tab4.add(decisionCodeShow);
            tab4.add(decisionDateTime);
            tab4.add(decisionDateTimeShow);
            tab4.add(customCode);
            tab4.add(customCodeShow);
            tab4.add(generateXML);
            tab4.add(chooseFileKm);
            tab4.add(chooseFileKmWay);
            tab4.add(finalXML);
            tab4.add(sendDoc);
            tab4.add(idDoc);
            tab4.add(chooseFileECPWay);
            tab4.add(chooseFileECP);
            tab4.add(passwordLabel);
            tab4.add(password);
            tabbedPane.addTab("Уведомление о ввозе (третьи страны)", tab4);

            ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            //Вывод из оборота
            JPanel tab5 = new JPanel();
            tab5.add(new

                    JLabel(""));
            tabbedPane.addTab("Вывод из оборота", tab5);

            // Добавление JTabbedPane в центр JFrame
            frame.add(tabbedPane, BorderLayout.CENTER);
            frame.setVisible(true);
        });

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static void testPost(String json) {
        baseURI = baseURIStage;
        basePath = "/api/v2/shoes/orders";
        Response response =
                given().log().all()
                        .queryParam("omsId", omsId)
                        .headers("clientToken", clientToken)
                        .headers("Content-Type", "application/json")
                        .body(json)
                        .when().post()
                        .then()
                        .extract().response();
        String place = response.asPrettyString();
        //System.out.println(place);

        JsonObject jsonObject5 = new JsonParser().parse(place).getAsJsonObject();
        String orderId;
        String error = "Обратитесь в техническую поддержку: mark@ismet.kz";

        if (jsonObject5.has("orderId")) {
            orderId = jsonObject5.get("orderId").getAsString();
            orders.add(orderId);

            StringBuilder ordersText = new StringBuilder();
            for (String order : orders) {
                ordersText.append(order).append("\n");
            }
            String str = ordersText.toString();
            resp.setText("Заказ создан: " + "\n" + str + "\n");
            setOrder.setText(str);
            //очищаем чтоб можно было не стерая заказать повторно
            one.clear();
            two.clear();
        } else {
            resp.setText("Заказ не создан: " + error + " .\"\n\" Причина:\"\n\" " + place);
        }
    }

}