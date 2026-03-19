import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;


public class CosIntegralApp extends JFrame {

    private JTextField lowerField;
    private JTextField upperField;
    private JTextField stepField;
    private final JTable table;
    private DefaultTableModel tableModel;
    private ArrayList<RecIntegral> integralList;
    private final JFileChooser fileChooser;


    private boolean isUpdating = false;

    public CosIntegralApp() {
        setTitle("Вычисление интеграла cos(x)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 800);
        setLocationRelativeTo(null);

        integralList = new ArrayList<>();
        fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File("."));


        JPanel inputPanel = new JPanel(new FlowLayout());
        lowerField = new JTextField(5);
        upperField = new JTextField(5);
        stepField = new JTextField(5);

        inputPanel.add(new JLabel("Нижняя граница:"));
        inputPanel.add(lowerField);
        inputPanel.add(new JLabel("Верхняя граница:"));
        inputPanel.add(upperField);
        inputPanel.add(new JLabel("Шаг:"));
        inputPanel.add(stepField);

        tableModel = new DefaultTableModel(new Object[]{
                "ID", "Нижняя граница", "Верхняя граница", "Шаг", "Результат"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column >= 1 && column <= 3;
            }
        };

        table = new JTable(tableModel);
        table.getColumn("ID").setMinWidth(0);
        table.getColumn("ID").setMaxWidth(0);

        JScrollPane scrollPane = new JScrollPane(table);

        JPanel originalButtonPanel = new JPanel(new FlowLayout());
JButton addButton = new JButton("Добавить");
JButton deleteButton = new JButton("Удалить");
JButton computeButton = new JButton("Вычислить");
JButton clearButton = new JButton("Очистить"); // Только визуально
JButton fillButton = new JButton("Заполнить"); // Восстановить из памяти

originalButtonPanel.add(addButton);
originalButtonPanel.add(deleteButton);
originalButtonPanel.add(computeButton);
originalButtonPanel.add(clearButton);
originalButtonPanel.add(fillButton);

addButton.addActionListener(e -> addRow());
deleteButton.addActionListener(e -> deleteRow());
computeButton.addActionListener(e -> computeIntegral());
clearButton.addActionListener(e -> clearTable());
fillButton.addActionListener(e -> fillTable());

// Панель для кнопок работы с файлами (текст, бинарник)
JPanel fileButtonPanel = new JPanel(new FlowLayout());
JButton saveTextButton = new JButton("Сохр. текст");
JButton loadTextButton = new JButton("Загр. текст");
JButton saveBinButton = new JButton("Сохр. bin");
JButton loadBinButton = new JButton("Загр. bin");

fileButtonPanel.add(saveTextButton);
fileButtonPanel.add(loadTextButton);
fileButtonPanel.add(saveBinButton);
fileButtonPanel.add(loadBinButton);

// Панель для JSON‑кнопок
JPanel jsonButtonPanel = new JPanel(new FlowLayout());
JButton saveJSONButton = new JButton("Сохр. JSON");
JButton loadJSONButton = new JButton("Загр. JSON");

jsonButtonPanel.add(saveJSONButton);
jsonButtonPanel.add(loadJSONButton);

// Назначаем обработчики событий
saveTextButton.addActionListener(e -> saveToText());
loadTextButton.addActionListener(e -> loadFromText());
saveBinButton.addActionListener(e -> saveToBinary());
loadBinButton.addActionListener(e -> loadFromBinary());
saveJSONButton.addActionListener(e -> saveToJSON());
loadJSONButton.addActionListener(e -> loadFromJSON());

// Создаём общую панель для всех кнопок файлов и JSON
JPanel southPanel = new JPanel();
southPanel.setLayout(new BoxLayout(southPanel, BoxLayout.Y_AXIS));
southPanel.add(originalButtonPanel);
southPanel.add(Box.createRigidArea(new Dimension(0, 5))); // Отступ между группами
southPanel.add(fileButtonPanel);
southPanel.add(Box.createRigidArea(new Dimension(0, 5))); // Отступ
southPanel.add(jsonButtonPanel);

// Устанавливаем компоновку основного окна и добавляем компоненты
setLayout(new BorderLayout());
add(inputPanel, BorderLayout.NORTH);
add(scrollPane, BorderLayout.CENTER);
add(southPanel, BorderLayout.SOUTH);
       

        tableModel.addTableModelListener(e -> {
            if (isUpdating) return;

            if (e.getType() == TableModelEvent.UPDATE) {
                int row = e.getFirstRow();
                int col = e.getColumn();

                if (col >= 1 && col <= 3) {
                    RecIntegral rec = null;
                    double oldVal = 0;
                    double oldLower = 0, oldUpper = 0, oldStep = 0;

                    try {
                        int id = Integer.parseInt(tableModel.getValueAt(row, 0).toString());
                        rec = integralList.stream()
                                .filter(r -> r.equalsById(id))
                                .findFirst()
                                .orElse(null);

                        if (rec == null) return;

                        oldLower = rec.getLowerBound();
                        oldUpper = rec.getUpperBound();
                        oldStep = rec.getStep();

                        switch (col) {
                            case 1 -> oldVal = oldLower;
                            case 2 -> oldVal = oldUpper;
                            case 3 -> oldVal = oldStep;
                        }

                        String inputString = tableModel.getValueAt(row, col).toString();
                        double newVal = Double.parseDouble(inputString);

                        switch (col) {
                            case 1 -> rec.setLowerBound(newVal);
                            case 2 -> rec.setUpperBound(newVal);
                            case 3 -> rec.setStep(newVal);
                        }

                        rec.validateBounds();

                        rec.setResult(0.0);
                        isUpdating = true;
                        tableModel.setValueAt("", row, 4);
                        isUpdating = false;

                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(this, "Введено не число: " + ex.getMessage());
                        revertToOldValue(row, col, oldVal);
                    } catch (InvalidRangeException ex) {
                        JOptionPane.showMessageDialog(this, ex.getMessage());
                        // Откат объекта
                        if (rec != null) {
                            try {
                                rec.setLowerBound(oldLower);
                                rec.setUpperBound(oldUpper);
                                rec.setStep(oldStep);
                            } catch (Exception ignored) {}
                        }
                        revertToOldValue(row, col, oldVal);
                    }
                }
            }
        });
    }

    private void revertToOldValue(int row, int col, double oldValue) {
        SwingUtilities.invokeLater(() -> {
            isUpdating = true;
            tableModel.setValueAt(oldValue, row, col);
            isUpdating = false;
        });
    }

    private void addRow() {
        try {
            double lower = Double.parseDouble(lowerField.getText());
            double upper = Double.parseDouble(upperField.getText());
            double step = Double.parseDouble(stepField.getText());

            RecIntegral record = new RecIntegral(lower, upper, step);
            integralList.add(record);
            tableModel.addRow(new Object[]{record.getId(), lower, upper, step, ""});

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Некорректный символ: " + ex.getMessage());
        } catch (InvalidRangeException ex) {
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

    private void clearTable() {
        tableModel.setRowCount(0);
    }

    private void fillTable() {
        tableModel.setRowCount(0); // Очищаем дубли, если они есть
        for (RecIntegral rec : integralList) {
            tableModel.addRow(new Object[]{
                    rec.getId(),
                    rec.getLowerBound(),
                    rec.getUpperBound(),
                    rec.getStep(),
                    rec.getResult() == 0.0 ? "" : rec.getResult()
            });
        }
    }

    private void computeIntegral() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Выберите строку.");
            return;
        }
        try {
            int id = Integer.parseInt(tableModel.getValueAt(row, 0).toString());
            RecIntegral rec = integralList.stream().filter(r -> r.equalsById(id)).findFirst().orElse(null);

            if (rec != null) {
                double a = rec.getLowerBound();
                double b = rec.getUpperBound();
                double h = rec.getStep();
                
                double sum = 0, x;
                for (x = a; x < b; x += h) sum += Math.cos(x) * h;
                if (x > b) sum += Math.cos(b) * (b - (x - h));

                rec.setResult(sum);
                tableModel.setValueAt(sum, row, 4);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Ошибка вычисления: " + ex.getMessage());
        }
    }

    private void saveToText() {
        fileChooser.setFileFilter(new FileNameExtensionFilter("Текстовые файлы (*.txt)", "txt"));
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File f = fileChooser.getSelectedFile();
            if (!f.getName().endsWith(".txt")) f = new File(f.getPath() + ".txt");
            try (PrintWriter pw = new PrintWriter(f)) {
                for (RecIntegral rec : integralList) {
                    pw.println(rec.getLowerBound() + ";" + rec.getUpperBound() + ";" + rec.getStep() + ";" + rec.getResult());
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Ошибка сохранения: " + e.getMessage());
            }
        }
    }

    private void loadFromText() {
        fileChooser.setFileFilter(new FileNameExtensionFilter("Текстовые файлы (*.txt)", "txt"));
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (BufferedReader br = new BufferedReader(new FileReader(fileChooser.getSelectedFile()))) {
                String line;
                ArrayList<RecIntegral> newList = new ArrayList<>();
                while ((line = br.readLine()) != null) {
                    String[] parts = line.split(";");
                    if (parts.length == 4) {
                        double l = Double.parseDouble(parts[0]);
                        double u = Double.parseDouble(parts[1]);
                        double s = Double.parseDouble(parts[2]);
                        double r = Double.parseDouble(parts[3]);
                        RecIntegral rec = new RecIntegral(l, u, s);
                        rec.setResult(r);
                        newList.add(rec);
                    }
                }
                integralList = newList;
                fillTable();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Ошибка загрузки: " + e.getMessage());
            }
        }
    }

    private void saveToBinary() {
        fileChooser.setFileFilter(new FileNameExtensionFilter("Бинарные файлы (*.bin)", "bin"));
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File f = fileChooser.getSelectedFile();
            if (!f.getName().endsWith(".bin")) f = new File(f.getPath() + ".bin");
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f))) {
                oos.writeObject(integralList);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Ошибка сериализации: " + e.getMessage());
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void loadFromBinary() {
        fileChooser.setFileFilter(new FileNameExtensionFilter("Бинарные файлы (*.bin)", "bin"));
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fileChooser.getSelectedFile()))) {
                integralList = (ArrayList<RecIntegral>) ois.readObject();
                int maxId = 0;
                for (RecIntegral rec : integralList) {
                    if (rec.getId() > maxId) maxId = rec.getId();
                }
                RecIntegral.setCounter(maxId + 1);
                fillTable();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Ошибка десериализации: " + e.getMessage());
            }
        }
    }
    private void saveToJSON() {
    fileChooser.setFileFilter(new FileNameExtensionFilter("JSON файлы (*.json)", "json"));
    if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
        File f = fileChooser.getSelectedFile();
        if (!f.getName().endsWith(".json")) {
            f = new File(f.getPath() + ".json");
        }
        try (PrintWriter writer = new PrintWriter(f, "UTF-8")) {
            writer.println("[");
            for (int i = 0; i < integralList.size(); i++) {
                RecIntegral rec = integralList.get(i);
                writer.print("  {");
                writer.print("\"id\":" + rec.getId());
                writer.print(",\"lowerBound\":" + rec.getLowerBound());
                writer.print(",\"upperBound\":" + rec.getUpperBound());
                writer.print(",\"step\":" + rec.getStep());
                writer.print(",\"result\":" + rec.getResult());
                writer.print("}");
                if (i < integralList.size() - 1) {
                    writer.println(",");
                } else {
                    writer.println();
                }
            }
            writer.println("]");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Ошибка сохранения в JSON: " + e.getMessage());
        }
    }
}

    
 private void loadFromJSON() {
    fileChooser.setFileFilter(new FileNameExtensionFilter("JSON файлы (*.json)", "json"));
    if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
        File f = fileChooser.getSelectedFile();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"))) {
            StringBuilder jsonContent = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonContent.append(line);
            }

            integralList.clear();

            String content = jsonContent.toString().trim();
            if (content.startsWith("[") && content.endsWith("]")) {
                content = content.substring(1, content.length() - 1);
                String[] records = content.split("\\},\\s*");

                for (String record : records) {
                    record = record.trim();
                    if (!record.isEmpty()) {
                        if (record.endsWith("}")) {
                    record = record.substring(0, record.length() - 1);
        }
        if (record.startsWith("{")) {
            record = record.substring(1);
        }

        Map<String, String> fields = new HashMap<>();
        String[] pairs = record.split(",\\s*");
        for (String pair : pairs) {
            String[] keyValue = pair.split(":", 2);
            if (keyValue.length == 2) {
                String key = keyValue[0].replaceAll("\"|\s", "");
                String value = keyValue[1].trim();
                fields.put(key, value);
            }
        }

        
        RecIntegral rec = new RecIntegral();
        
        rec.setId(Integer.parseInt(fields.get("id")));
        rec.setLowerBound(Double.parseDouble(fields.get("lowerBound")));
        rec.setUpperBound(Double.parseDouble(fields.get("upperBound")));
        rec.setStep(Double.parseDouble(fields.get("step")));
        rec.setResult(Double.parseDouble(fields.get("result")));
        integralList.add(rec);
    }
}
            }
            
            int maxId = 0;
            for (RecIntegral rec : integralList) {
                if (rec.getId() > maxId) {
                    maxId = rec.getId();
                }
            }
            RecIntegral.setCounter(maxId + 1);

            
            fillTable();
            JOptionPane.showMessageDialog(this, "Данные успешно загружены из JSON");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Ошибка чтения JSON-файла: " + e.getMessage());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Ошибка формата данных в JSON-файле: " + e.getMessage());
        } catch (InvalidRangeException e) {
            JOptionPane.showMessageDialog(this, "Ошибка валидации данных: " + e.getMessage());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Неизвестная ошибка при загрузке JSON: " + e.getMessage());
        }
    }
}



    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CosIntegralApp().setVisible(true));
    }
}