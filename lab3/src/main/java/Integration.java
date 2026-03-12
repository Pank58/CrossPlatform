import java.io.Serializable;

// Файл 1: Класс для хранения данных, логики и пользовательского исключения
public class Integration {

    public static class RecIntegralException extends Exception {
        public RecIntegralException(String message) {
            super(message);
        }
    }
    
    // Добавили 'implements Serializable' для работы сохранения в файл
    public static class RecIntegral implements Serializable {
        
        private static final long serialVersionUID = 1L;

        private double lowerBound;
        private double upperBound;
        private double step;
        private double result;

        public RecIntegral(double lowerBound, double upperBound, double step) throws RecIntegralException {
            // 1. Сначала проверяем диапазон каждого числа
            validate(lowerBound);
            validate(upperBound);
            validate(step);
            
            // 2. Проверяем логику интервала
            if (lowerBound >= upperBound) { // Исправлено: нельзя, чтобы нижняя была >= верхней
                 throw new RecIntegralException("Нижняя граница должна быть меньше верхней");
            }
            
            // 3. Проверяем шаг
            if (step > (upperBound - lowerBound)) {
                 throw new RecIntegralException("Шаг не может быть больше длины интервала");
            }

            this.lowerBound = lowerBound;
            this.upperBound = upperBound;
            this.step = step;
            this.result = 0.0;
        }

        // Вспомогательный метод валидации диапазона
        private void validate(double value) throws RecIntegralException {
            if (value < 0.000001 || value > 1000000) {
                throw new RecIntegralException("Значение " + value + " выходит за диапазон (0.000001 - 1000000)");
            }
        }

        // --- Геттеры и Сеттеры с ВАЛИДАЦИЕЙ ---

        public double getLowerBound() { return lowerBound; }
        
        // Теперь сеттер тоже проверяет данные!
        public void setLowerBound(double lowerBound) throws RecIntegralException { 
            validate(lowerBound);
            if (lowerBound >= this.upperBound) {
                throw new RecIntegralException("Нижняя граница должна быть меньше верхней");
            }
            this.lowerBound = lowerBound; 
        }

        public double getUpperBound() { return upperBound; }
        
        public void setUpperBound(double upperBound) throws RecIntegralException { 
            validate(upperBound);
            if (this.lowerBound >= upperBound) {
                throw new RecIntegralException("Верхняя граница должна быть больше нижней");
            }
            this.upperBound = upperBound; 
        }

        public double getStep() { return step; }
        
        public void setStep(double step) throws RecIntegralException { 
            validate(step);
            if (step > (this.upperBound - this.lowerBound)) {
                throw new RecIntegralException("Шаг не может быть больше длины интервала");
            }
            this.step = step; 
        }

        public double getResult() { return result; }
        public void setResult(double result) { this.result = result; }

        // Метод вычисления интеграла
        public double integr(double a, double b, double h) {
            double sum = 0;
            double x;
            for (x = a; x < b; x += h) {
                sum += Math.cos(x) * h;
            }
            // Добавка хвоста
            if (x > b) { // Упрощенная проверка
                 sum += Math.cos(b) * (b - (x - h));
            }
            return sum;
        }

        // Метод для сравнения (НУЖЕН ДЛЯ КОРРЕКТНОГО УДАЛЕНИЯ в GUI)
        public boolean equalsValues(double l, double u, double s) {
            double epsilon = 0.0000001;
            return Math.abs(this.lowerBound - l) < epsilon &&
                   Math.abs(this.upperBound - u) < epsilon &&
                   Math.abs(this.step - s) < epsilon;
        }
        
        @Override
        public String toString() {
            return lowerBound + " " + upperBound + " " + step + " " + result;
        }
    }
}