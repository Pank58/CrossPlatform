import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;

public class CosIntegralApp extends JFrame {

    private JTextField lowerField;
    private JTextField upperField;
    private JTextField stepField;
    private JTable table;
    private DefaultTableModel tableModel;

    private ArrayList<Integration.RecIntegral> integralList;

    public CosIntegralApp() {
        setTitle("Вычисление интеграла с проверкой исключений");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 800);
        setLocationRelativeTo(null);

        integralList = new ArrayList<>();

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new FlowLayout());

        lowerField = new JTextField(5);
        upperField = new JTextField(5);
        stepField = new JTextField(5);

        inputPanel.add(new JLabel("Нижняя граница:"));
        inputPanel.add(lowerField);
        inputPanel.add(new JLabel("Верхняя граница:"));
        inputPanel.add(upperField);
        inputPanel.add(new JLabel("Шаг:"));
        inputPanel.add(stepField);

        tableModel = new DefaultTableModel(new Object[]{"Нижняя граница", "Верхняя граница", "Шаг", "Результат"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column < 3;
            }
        };
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());

        JButton addButton = new JButton("Добавить");
        JButton deleteButton = new JButton("Удалить");
        JButton computeButton = new JButton("Вычислить");
        JButton clearButton = new JButton("Очистить");
        JButton fillButton = new JButton("Заполнить");

        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(computeButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(fillButton);

        addButton.addActionListener(e -> addRow());
        deleteButton.addActionListener(e -> deleteRow());
        computeButton.addActionListener(e -> computeIntegral());
        clearButton.addActionListener(e -> clearTable());
        fillButton.addActionListener(e -> fillTable());

        setLayout(new BorderLayout());
        add(inputPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    // --- Метод для проверки строки на недопустимые символы ---
    private void checkString(String text) throws NumberFormatException {
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            // Разрешаем цифры, точку и минус (в начале числа)
            // Если символ не цифра, не точка и не минус -> выбрасываем ошибку
            if (!Character.isDigit(c) && c != '.' && c != '-') {
                throw new NumberFormatException("Недопустимый символ: '" + c + "'\nВведите только цифры и точки.");
            }
        }
    }

    private void addRow() {
        try {
            String lowerStr = lowerField.getText();
            String upperStr = upperField.getText();
            String stepStr = stepField.getText();

            if (lowerStr.isEmpty() || upperStr.isEmpty() || stepStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Пожалуйста, заполните все поля.");
                return;
            }

            // 1. Сначала проверяем на наличие букв и запрещенных знаков
            checkString(lowerStr);
            checkString(upperStr);
            checkString(stepStr);

            // 2. Если проверка checkString прошла, пробуем парсить
            // (здесь может возникнуть ошибка, если например ввести две точки "5..5")
            double lower = Double.parseDouble(lowerStr);
            double upper = Double.parseDouble(upperStr);
            double step = Double.parseDouble(stepStr);

            Integration.RecIntegral record = new Integration.RecIntegral(lower, upper, step);
            integralList.add(record);
            tableModel.addRow(new Object[]{lower, upper, step, ""});

        } catch (NumberFormatException ex) {
            // Если ошибка пришла из checkString, там уже есть нужное сообщение.
            // Если ошибка пришла из Double.parseDouble (например, две точки "5..5"), 
            // сообщение будет системное "For input string...", заменим его на понятное.
            String message = ex.getMessage();
            if (message.startsWith("For input string")) {
                message = "Ошибка формата числа (например, лишняя точка или минус не на месте).";
            }
            JOptionPane.showMessageDialog(this, message);
            
        } catch (Integration.RecIntegralException ex) {
            JOptionPane.showMessageDialog(this, "Ошибка данных: " + ex.getMessage(), "Предупреждение", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void deleteRow() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            // Исправленная логика удаления (как обсуждали в прошлом шаге)
            try {
                double a = Double.parseDouble(tableModel.getValueAt(selectedRow, 0).toString());
                double b = Double.parseDouble(tableModel.getValueAt(selectedRow, 1).toString());
                double h = Double.parseDouble(tableModel.getValueAt(selectedRow, 2).toString());
                
                for (int i = 0; i < integralList.size(); i++) {
                    Integration.RecIntegral rec = integralList.get(i);
                    if (Math.abs(rec.getLowerBound() - a) < 1e-9 &&
                        Math.abs(rec.getUpperBound() - b) < 1e-9 &&
                        Math.abs(rec.getStep() - h) < 1e-9) {
                        integralList.remove(i);
                        break;
                    }
                }
            } catch (Exception ignored) {}

            tableModel.removeRow(selectedRow);
        } else {
            JOptionPane.showMessageDialog(this, "Выберите строку для удаления.");
        }
    }

    private void clearTable() {
        tableModel.setRowCount(0);
    }

    private void fillTable() {
        tableModel.setRowCount(0);
        for (Integration.RecIntegral rec : integralList) {
            Object resultVal = (rec.getResult() == 0.0) ? "" : rec.getResult();
            tableModel.addRow(new Object[]{
                rec.getLowerBound(),
                rec.getUpperBound(),
                rec.getStep(),
                resultVal
            });
        }
    }

    private void computeIntegral() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Выберите строку для вычисления.");
            return;
        }

        try {
            double a = Double.parseDouble(tableModel.getValueAt(selectedRow, 0).toString());
            double b = Double.parseDouble(tableModel.getValueAt(selectedRow, 1).toString());
            double h = Double.parseDouble(tableModel.getValueAt(selectedRow, 2).toString());

            if (h <= 0 || b <= a) {
                JOptionPane.showMessageDialog(this, "Некорректные границы или шаг.");
                return;
            }

            Integration.RecIntegral currentRec = null;
            // Поиск объекта по значениям
            for (Integration.RecIntegral rec : integralList) {
                if (Math.abs(rec.getLowerBound() - a) < 1e-9 &&
                    Math.abs(rec.getUpperBound() - b) < 1e-9 &&
                    Math.abs(rec.getStep() - h) < 1e-9) {
                    currentRec = rec;
                    break;
                }
            }

            if (currentRec == null) {
                try {
                    currentRec = new Integration.RecIntegral(a, b, h);
                    integralList.add(currentRec);
                } catch (Integration.RecIntegralException e) {
                    JOptionPane.showMessageDialog(this, "Ошибка при пересоздании записи: " + e.getMessage());
                    return;
                }
            }

            double result = currentRec.integr(a, b, h);
            currentRec.setResult(result);
            tableModel.setValueAt(result, selectedRow, 3);

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Некорректный ввод чисел в таблице.");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            CosIntegralApp app = new CosIntegralApp();
            app.setVisible(true);
        });
    }
}