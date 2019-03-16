package ChatApp.domain.metrics;

public abstract class DataType<T> {

    public String toString(T object){
        return object.toString();
    }

    public abstract T toObject( String string );

    public abstract String getType();
}