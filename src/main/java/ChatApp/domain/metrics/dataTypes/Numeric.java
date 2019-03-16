package ChatApp.domain.metrics.dataTypes;

import ChatApp.domain.metrics.DataType;

public class Numeric extends DataType<Double> {
    @Override
    public Double toObject(String string) {
        return Double.valueOf( string );
    }

    @Override
    public String getType() {
        return "NUMERIC";
    }
}
