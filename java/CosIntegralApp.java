import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
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
        setTitle("Вычисление интеграла cos(x) (Многопоточность)");
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
        singleThreadRadio = new JRadioButton("Однопоточно (Main)", true);
        multiThreadRadio = new JRadioButton("Многопоточно (3 threads)");
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
        JButton clearButton = new JButton("Очистить");
        JButton fillButton = new JButton("Заполнить");

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

        JPanel fileButtonPanel = new JPanel(new FlowLayout());
        JButton saveTextButton = new JButton("Сохр. текст");
        JButton loadTextButton = new JButton("Загр. текст");
        JButton saveBinButton = new JButton("Сохр. bin");
        JButton loadBinButton = new JButton("Загр. bin");

        fileButtonPanel.add(saveTextButton);
        fileButtonPanel.add(loadTextButton);
        fileButtonPanel.add(saveBinButton);
        fileButtonPanel.add(loadBinButton);

        saveTextButton.addActionListener(e -> saveToText());
        loadTextButton.addActionListener(e -> loadFromText());
        saveBinButton.addActionListener(e -> saveToBinary());
        loadBinButton.addActionListener(e -> loadFromBinary());

        JPanel southPanel = new JPanel(new GridLayout(2, 1));
        southPanel.add(originalButtonPanel);
        southPanel.add(fileButtonPanel);

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
                        rec = integralList.stream().filter(r -> r.equalsById(id)).findFirst().orElse(null);
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
    public static class ComputeThread extends Thread {
        private final double start;
        private final double end;
        private final double step;
        private double partialSum = 0;

        public ComputeThread(double start, double end, double step) {
            this.start = start;
            this.end = end;
            this.step = step;
        }

        @Override
        public void run() {
            double x;
            for (x = start; x < end; x += step) {
                partialSum += Math.cos(x) * step;
            }
            if (x > end && (x - step) < end) {
                 partialSum += Math.cos(end) * (end - (x - step));
            }
        }

        public double getPartialSum() {
            return partialSum;
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

            if (rec == null) return;

            double a = rec.getLowerBound();
            double b = rec.getUpperBound();
            double h = rec.getStep();

            if (singleThreadRadio.isSelected()) {
                long startTime = System.nanoTime();

                double sum = 0, x;
                for (x = a; x < b; x += h) sum += Math.cos(x) * h;
                if (x > b) sum += Math.cos(b) * (b - (x - h));

                long endTime = System.nanoTime();
                double timeMs = (endTime - startTime) / 1_000_000.0;

                rec.setResult(sum);
                tableModel.setValueAt(sum, row, 4);
                JOptionPane.showMessageDialog(this, "Вычисление завершено (1 поток).\nВремя: " + timeMs + " мс");

            } else {
                
                new Thread(() -> {
                    long startTime = System.nanoTime();

                    double range = b - a;
                    double part = range / 3.0;

                    ComputeThread t1 = new ComputeThread(a, a + part, h);
                    ComputeThread t2 = new ComputeThread(a + part, a + 2 * part, h);
                    ComputeThread t3 = new ComputeThread(a + 2 * part, b, h);

                    t1.start();
                    t2.start();
                    t3.start();

                    try {
                        t1.join();
                        t2.join();
                        t3.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    double totalSum = t1.getPartialSum() + t2.getPartialSum() + t3.getPartialSum();

                    long endTime = System.nanoTime();
                    double timeMs = (endTime - startTime) / 1_000_000.0;

                    SwingUtilities.invokeLater(() -> {
                        rec.setResult(totalSum);
                        tableModel.setValueAt(totalSum, row, 4);
                        JOptionPane.showMessageDialog(this, "Вычисление завершено (3 потока).\nВремя: " + timeMs + " мс");
                    });

                }).start();
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Ошибка: " + ex.getMessage());
        }
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
        tableModel.setRowCount(0);
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
                        RecIntegral rec = new RecIntegral(
                                Double.parseDouble(parts[0]),
                                Double.parseDouble(parts[1]),
                                Double.parseDouble(parts[2]));
                        rec.setResult(Double.parseDouble(parts[3]));
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CosIntegralApp().setVisible(true));
    }
}