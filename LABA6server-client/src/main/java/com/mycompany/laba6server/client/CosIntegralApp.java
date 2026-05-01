package com.mycompany.laba6server.client;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class CosIntegralApp extends JFrame {

    private JTextField lowerField;
    private JTextField upperField;
    private JTextField stepField;
    private final JTable table;
    private DefaultTableModel tableModel;
    private ArrayList<RecIntegral> integralList;
    private final JFileChooser fileChooser;

    private JRadioButton singleThreadRadio;
    private JRadioButton multiThreadRadio;

    private boolean isUpdating = false;

    public CosIntegralApp() {
        setTitle("Вычисление интеграла cos(x)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 850);
        setLocationRelativeTo(null);

        integralList = new ArrayList<>();
        fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File("."));

        JPanel inputPanel = new JPanel(new GridLayout(3, 1));
        JPanel fieldsPanel = new JPanel(new FlowLayout());

        lowerField = new JTextField(5);
        upperField = new JTextField(5);
        stepField = new JTextField(5);

        fieldsPanel.add(new JLabel("Нижняя граница:"));
        fieldsPanel.add(lowerField);
        fieldsPanel.add(new JLabel("Верхняя граница:"));
        fieldsPanel.add(upperField);
        fieldsPanel.add(new JLabel("Шаг:"));
        fieldsPanel.add(stepField);

        JPanel radioPanel = new JPanel(new FlowLayout());
        singleThreadRadio = new JRadioButton("Однопоточно", true);
        multiThreadRadio = new JRadioButton("Через сервер (UDP)");

        ButtonGroup group = new ButtonGroup();
        group.add(singleThreadRadio);
        group.add(multiThreadRadio);

        radioPanel.add(new JLabel("Режим:"));
        radioPanel.add(singleThreadRadio);
        radioPanel.add(multiThreadRadio);

        inputPanel.add(fieldsPanel);
        inputPanel.add(radioPanel);

        tableModel = new DefaultTableModel(new Object[]{
                "ID", "Нижняя граница", "Верхняя граница", "Шаг", "Результат"
        }, 0) {
            public boolean isCellEditable(int row, int col) {
                return col >= 1 && col <= 3;
            }
        };

        table = new JTable(tableModel);
        table.getColumn("ID").setMinWidth(0);
        table.getColumn("ID").setMaxWidth(0);

        JScrollPane scrollPane = new JScrollPane(table);

        JPanel btnPanel1 = new JPanel(new FlowLayout());
        JButton addButton = new JButton("Добавить");
        JButton deleteButton = new JButton("Удалить");
        JButton computeButton = new JButton("Вычислить");
        JButton clearButton = new JButton("Очистить");
        JButton fillButton = new JButton("Заполнить");

        btnPanel1.add(addButton);
        btnPanel1.add(deleteButton);
        btnPanel1.add(computeButton);
        btnPanel1.add(clearButton);
        btnPanel1.add(fillButton);

        JPanel btnPanel2 = new JPanel(new FlowLayout());
        JButton saveTxt = new JButton("Сохр. текст");
        JButton loadTxt = new JButton("Загр. текст");
        JButton saveBin = new JButton("Сохр. bin");
        JButton loadBin = new JButton("Загр. bin");

        btnPanel2.add(saveTxt);
        btnPanel2.add(loadTxt);
        btnPanel2.add(saveBin);
        btnPanel2.add(loadBin);

        JPanel south = new JPanel(new GridLayout(2,1));
        south.add(btnPanel1);
        south.add(btnPanel2);

        add(inputPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(south, BorderLayout.SOUTH);

        addButton.addActionListener(e -> addRow());
        deleteButton.addActionListener(e -> deleteRow());
        computeButton.addActionListener(e -> computeIntegral());
        clearButton.addActionListener(e -> tableModel.setRowCount(0));
        fillButton.addActionListener(e -> fillTable());

        saveTxt.addActionListener(e -> saveToText());
        loadTxt.addActionListener(e -> loadFromText());
        saveBin.addActionListener(e -> saveToBinary());
        loadBin.addActionListener(e -> loadFromBinary());

        tableModel.addTableModelListener(this::handleEdit);
    }

    private void handleEdit(TableModelEvent e) {
        if (isUpdating) return;
    }

    private void addRow() {
        try {
            double a = Double.parseDouble(lowerField.getText());
            double b = Double.parseDouble(upperField.getText());
            double h = Double.parseDouble(stepField.getText());

            RecIntegral rec = new RecIntegral(a, b, h);
            integralList.add(rec);
            tableModel.addRow(new Object[]{rec.getId(), a, b, h, ""});
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    private void deleteRow() {
        int row = table.getSelectedRow();
        if (row != -1) {
            int id = Integer.parseInt(tableModel.getValueAt(row, 0).toString());
            integralList.removeIf(r -> r.equalsById(id));
            tableModel.removeRow(row);
        }
    }

    private void fillTable() {
        tableModel.setRowCount(0);
        for (RecIntegral r : integralList) {
            tableModel.addRow(new Object[]{
                    r.getId(),
                    r.getLowerBound(),
                    r.getUpperBound(),
                    r.getStep(),
                    r.getResult() == 0 ? "" : r.getResult()
            });
        }
    }

    private void computeIntegral() {
        int row = table.getSelectedRow();
        if (row == -1) return;

        RecIntegral rec = integralList.get(row);

        double a = rec.getLowerBound();
        double b = rec.getUpperBound();
        double h = rec.getStep();

        if (singleThreadRadio.isSelected()) {
            double sum = 0;
            for (double x = a; x < b; x += h)
                sum += Math.cos(x) * h;

            rec.setResult(sum);
            tableModel.setValueAt(sum, row, 4);
        } else {
            new Thread(() -> {
                try {
                    double result = computeViaServer(a, b, h);
                    SwingUtilities.invokeLater(() -> {
                        rec.setResult(result);
                        tableModel.setValueAt(result, row, 4);
                    });
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, e.getMessage());
                }
            }).start();
        }
    }

    private double computeViaServer(double a, double b, double h) throws Exception {
        DatagramSocket socket = new DatagramSocket();

        String msg = "TASK;" + a + ";" + b + ";" + h;

        DatagramPacket packet = new DatagramPacket(
                msg.getBytes(),
                msg.length(),
                InetAddress.getByName("localhost"),
                5000
        );

        socket.send(packet);

        byte[] buffer = new byte[1024];
        DatagramPacket resp = new DatagramPacket(buffer, buffer.length);
        socket.receive(resp);

        socket.close();

        String res = new String(resp.getData(), 0, resp.getLength());
        return Double.parseDouble(res.split(";")[1]);
    }

    private void saveToText() {
        fileChooser.setFileFilter(new FileNameExtensionFilter("*.txt", "txt"));
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (PrintWriter pw = new PrintWriter(fileChooser.getSelectedFile())) {
                for (RecIntegral r : integralList) {
                    pw.println(r.getLowerBound() + ";" + r.getUpperBound() + ";" + r.getStep() + ";" + r.getResult());
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, e.getMessage());
            }
        }
    }

    private void loadFromText() {
        fileChooser.setFileFilter(new FileNameExtensionFilter("*.txt", "txt"));
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (BufferedReader br = new BufferedReader(new FileReader(fileChooser.getSelectedFile()))) {
                integralList.clear();
                String line;
                while ((line = br.readLine()) != null) {
                    String[] p = line.split(";");
                    RecIntegral r = new RecIntegral(
                            Double.parseDouble(p[0]),
                            Double.parseDouble(p[1]),
                            Double.parseDouble(p[2])
                    );
                    r.setResult(Double.parseDouble(p[3]));
                    integralList.add(r);
                }
                fillTable();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, e.getMessage());
            }
        }
    }

    private void saveToBinary() {
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fileChooser.getSelectedFile()))) {
                oos.writeObject(integralList);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, e.getMessage());
            }
        }
    }

    private void loadFromBinary() {
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fileChooser.getSelectedFile()))) {
                integralList = (ArrayList<RecIntegral>) ois.readObject();
                fillTable();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CosIntegralApp().setVisible(true));
    }
}