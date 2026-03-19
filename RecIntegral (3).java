import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class RecIntegral implements Externalizable {
    private static int counter = 0;
    private int id;
    private double lowerBound;
    private double upperBound;
    private double step;
    private double result;

    // Конструктор без аргументов для Externalizable
    public RecIntegral() {
        this.id = counter++;
    }

    public RecIntegral(double lowerBound, double upperBound, double step) throws InvalidRangeException {
        this.id = counter++;

        validate(lowerBound);
        validate(upperBound);
        validate(step);

        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.step = step;

        validateBounds();
        this.result = 0.0;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(id);
        out.writeDouble(lowerBound);
        out.writeDouble(upperBound);
        out.writeDouble(step);
        out.writeDouble(result);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        int readId = in.readInt();
        // Восстанавливаем счётчик, если нужно
        if (readId >= counter) {
            counter = readId + 1;
        }
        this.id = readId;
        this.lowerBound = in.readDouble();
        this.upperBound = in.readDouble();
        this.step = in.readDouble();
        this.result = in.readDouble();
    }

    public static void setCounter(int val) {
        counter = val;
    }

    private void validate(double value) throws InvalidRangeException {
        if (value < 0.000001 || value > 1000000) {
            throw new InvalidRangeException("Значение вне диапазона (0.000001 - 1000000): " + value);
        }
    }

    public void validateBounds() throws InvalidRangeException {
        if (upperBound <= lowerBound) {
            throw new InvalidRangeException("Ошибка: верхняя граница должна быть больше нижней.");
        }
        if (step > (upperBound - lowerBound)) {
            throw new InvalidRangeException("Ошибка: шаг не может быть больше длины диапазона.");
        }
    }
    
    
    
    public void setId(int id) {
    this.id = id;
}

    public int getId() { return id; }

    public double getLowerBound() { return lowerBound; }
    public void setLowerBound(double lowerBound) throws InvalidRangeException {
        validate(lowerBound);
        this.lowerBound = lowerBound;
    }

    public double getUpperBound() { return upperBound; }
    public void setUpperBound(double upperBound) throws InvalidRangeException {
        validate(upperBound);
        this.upperBound = upperBound;
    }

    public double getStep() { return step; }
    public void setStep(double step) throws InvalidRangeException {
        validate(step);
        this.step = step;
    }

    public double getResult() { return result; }
    public void setResult(double result) { this.result = result; }

    public boolean equalsById(int otherId) {
        return this.id == otherId;
    }

    }
