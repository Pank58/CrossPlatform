
public class Integration {
    
   
    public static class RecIntegral {
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

        
        public double getLowerBound() {
            return lowerBound;
        }

        public void setLowerBound(double lowerBound) {
            this.lowerBound = lowerBound;
        }

        public double getUpperBound() {
            return upperBound;
        }

        public void setUpperBound(double upperBound) {
            this.upperBound = upperBound;
        }

        public double getStep() {
            return step;
        }

        public void setStep(double step) {
            this.step = step;
        }

        public double getResult() {
            return result;
        }

        public void setResult(double result) {
            this.result = result;
        }

        // Метод вычисления интеграла cos(x)
        public double integr(double a, double b, double h) {
            double sum = 0;
            double x;
            // Метод прямоугольников (или трапеций, в зависимости от предыдущей реализации)
            for (x = a; x < b; x += h) {
                sum += Math.cos(x) * h;
            }
            
            // Учет остатка ("хвоста" интервала)
            if (x > b) {
                 sum += Math.cos(b) * (b - (x - h));
            }
            
            return sum;
        }
    }
}