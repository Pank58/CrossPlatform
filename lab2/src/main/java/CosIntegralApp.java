import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;

// 1. Класс для хранения данных одной записи (RecIntegral)
class RecIntegral {
    private double lowerBound;
    private double upperBound;
    private double step;
    private double result;

    public RecIntegral(double lowerBound, double upperBound, double step) {
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.step = step;
        this.result = 0.0;
    }

    public double getLowerBound() { return lowerBound; }
    public void setLowerBound(double lowerBound) { this.lowerBound = lowerBound; }

    public double getUpperBound() { return upperBound; }
    public void setUpperBound(double upperBound) { this.upperBound = upperBound; }

    public double getStep() { return step; }
    public void setStep(double step) { this.step = step; }

    public double getResult() { return result; }
    public void setResult(double result) { this.result = result; }

    // Метод для сравнения объектов (чтобы найти нужный в списке)
    public boolean equalsValues(double l, double u, double s) {
        // Сравниваем с очень маленькой погрешностью, так как это double
        double epsilon = 0.0000001;
        return Math.abs(this.lowerBound - l) < epsilon &&
               Math.abs(this.upperBound - u) < epsilon &&
               Math.abs(this.step - s) < epsilon;
    }
}

public class CosIntegralApp extends JFrame {

    private JTextField lowerField;
    private JTextField upperField;
    private JTextField stepField;
    private JTable table;
    private DefaultTableModel tableModel;
    
    // 2. Коллекция для хранения записей (ArrayList)
    private ArrayList<RecIntegral> integralList;

    public CosIntegralApp() {
        setTitle("Вычисление интеграла cos(x) с коллекциями");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 800);
        setLocationRelativeTo(null);

        // Инициализация коллекции
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
                // Разрешаем редактирование ячеек ввода (0, 1, 2), но не результата (3)
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

    private void addRow() {
        try {
            String lowerStr = lowerField.getText();
            String upperStr = upperField.getText();
            String stepStr = stepField.getText();

            if (lowerStr.isEmpty() || upperStr.isEmpty() || stepStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Пожалуйста, заполните все поля.");
                return;
            }

            double lower = Double.parseDouble(lowerStr);
            double upper = Double.parseDouble(upperStr);
            double step = Double.parseDouble(stepStr);

            // Создаем объект и добавляем в коллекцию
            RecIntegral record = new RecIntegral(lower, upper, step);
            integralList.add(record);

            // Добавляем строку в визуальную таблицу
            tableModel.addRow(new Object[]{lower, upper, step, ""});
            
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Некорректный ввод чисел.");
        }
    }

    // --- ИСПРАВЛЕННЫЙ МЕТОД УДАЛЕНИЯ ---
    private void deleteRow() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            try {
                // 1. Берем данные из визуальной таблицы
                double a = Double.parseDouble(tableModel.getValueAt(selectedRow, 0).toString());
                double b = Double.parseDouble(tableModel.getValueAt(selectedRow, 1).toString());
                double h = Double.parseDouble(tableModel.getValueAt(selectedRow, 2).toString());

                // 2. Ищем объект в списке по значениям, а НЕ по индексу
                for (int i = 0; i < integralList.size(); i++) {
                    if (integralList.get(i).equalsValues(a, b, h)) {
                        integralList.remove(i); // Удаляем найденный объект
                        break; // Выходим после первого удаления
                    }
                }
            } catch (Exception e) {
                // Игнорируем ошибки парсинга при удалении, если вдруг в таблице мусор
            }

            // 3. Удаляем строку из таблицы
            tableModel.removeRow(selectedRow);
        } else {
            JOptionPane.showMessageDialog(this, "Выберите строку для удаления.");
        }
    }

    // Метод очистки визуальной таблицы (данные в ArrayList остаются)
    private void clearTable() {
        tableModel.setRowCount(0);
    }

    // Метод заполнения таблицы данными из ArrayList
    private void fillTable() {
        // Сначала очищаем таблицу, чтобы не дублировать данные
        tableModel.setRowCount(0);
        
        for (RecIntegral rec : integralList) {
            Object[] row = new Object[]{
                rec.getLowerBound(),
                rec.getUpperBound(),
                rec.getStep(),
                rec.getResult() == 0.0 ? "" : rec.getResult() 
            };
            tableModel.addRow(row);
        }
    }

    // --- ИСПРАВЛЕННЫЙ МЕТОД ВЫЧИСЛЕНИЯ ---
    private void computeIntegral() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Выберите строку для вычисления.");
            return;
        }

        try {
            // Получаем данные из ячеек таблицы
            double a = Double.parseDouble(tableModel.getValueAt(selectedRow, 0).toString());
            double b = Double.parseDouble(tableModel.getValueAt(selectedRow, 1).toString());
            double h = Double.parseDouble(tableModel.getValueAt(selectedRow, 2).toString());

            if (h <= 0 || b <= a) {
                JOptionPane.showMessageDialog(this, "Некорректные границы или шаг.");
                return;
            }

            // Ищем соответствующий объект в списке
            RecIntegral currentRec = null;
            for (RecIntegral rec : integralList) {
                if (rec.equalsValues(a, b, h)) {
                    currentRec = rec;
                    break;
                }
            }

            // Если объект не найден (например, вы изменили числа в ячейке таблицы вручную),
            // то создаем новый и добавляем его в список, чтобы сохранить синхронизацию.
            if (currentRec == null) {
                currentRec = new RecIntegral(a, b, h);
                integralList.add(currentRec);
            }

            // Вычисление
            double sum = 0;
            double x;
            for (x = a; x < b; x += h) {
                sum += Math.cos(x) * h;
            }
            sum += (x > b) ? (Math.cos(b) * (b - (x - h))) : (0);

            // Сохраняем результат в объект и в таблицу
            currentRec.setResult(sum);
            tableModel.setValueAt(sum, selectedRow, 3);

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