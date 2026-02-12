import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;

public class CosIntegralApp extends JFrame {

    private JTextField lowerField;
    private JTextField upperField;
    private JTextField stepField;
    private JTable table;
    private DefaultTableModel tableModel;

    public CosIntegralApp() {
        setTitle("Вычисление интеграла cos(x)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 800);
        setLocationRelativeTo(null);

        
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

        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(computeButton);

       
        addButton.addActionListener(e -> addRow());
        deleteButton.addActionListener(e -> deleteRow());
        computeButton.addActionListener(e -> computeIntegral());

        
        setLayout(new BorderLayout());
        add(inputPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    
    private void addRow() {
        String lower = lowerField.getText();
        String upper = upperField.getText();
        String step = stepField.getText();

        if (lower.isEmpty() || upper.isEmpty() || step.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Пожалуйста, заполните все поля.");
            return;
        }

        tableModel.addRow(new Object[]{lower, upper, step, ""});
    }

    
    private void deleteRow() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            tableModel.removeRow(selectedRow);
        } else {
            JOptionPane.showMessageDialog(this, "Выберите строку для удаления.");
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

            double sum = 0;
            double x;
            for (x = a; x < b; x += h) {
                sum += Math.cos(x) * h;
            }
            if(x>b){
                sum+= Math.cos(b)*(b-(x-h));
            }
            tableModel.setValueAt(sum, selectedRow, 3);

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Некорректный ввод чисел.");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            CosIntegralApp app = new CosIntegralApp();
            app.setVisible(true);
        });
    }
}
