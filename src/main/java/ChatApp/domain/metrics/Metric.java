package ChatApp.domain.metrics;

import sun.misc.ClassLoaderUtil;

import javax.persistence.*;

@Entity
@Table( name="metric" )
public class Metric {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column
    private String label;
    @Transient
    private DataType dataType;
    @Column( name = "data_type" )
    private String dataTypeClass;
    @ManyToOne
    @JoinColumn(name="unit_id")
    private Unit unit;
    @Transient
    private Object lowerBound;
    @Column( name="lower_bound" )
    private String lowerBoundString;
    @Transient
    private Object upperBound;
    @Column( name="upper_bound" )
    private String upperBoundString;
    @Column(name="operator")
    @Enumerated(EnumType.STRING)
    private Operator operator;
    @Column(name="validation")
    private String validation;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    public DataType getDataType() {
        if( dataType == null ){
            try {
                Class<?> dataClass = ClassLoader.getSystemClassLoader().loadClass( dataTypeClass );
                dataType = (DataType) dataClass.newInstance();
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return dataType;
    }

    public void setDataType(DataType dataType) {
        this.dataType = dataType;
        this.dataTypeClass = dataType.getClass().getName();
    }

    public Object getLowerBound() {
        if( lowerBound == null ){
            lowerBound = dataType.toObject( lowerBoundString );
        }
        return lowerBound;
    }

    public void setLowerBound(Object lowerBound) {
        this.lowerBound = lowerBound;
        this.lowerBoundString = dataType.toString( lowerBound );
    }

    public Object getUpperBound() {
        if( upperBound == null ){
            upperBound = dataType.toObject(upperBoundString);
        }
        return upperBound;
    }

    public void setUpperBound(Object upperBound) {
        this.upperBound = upperBound;
        this.upperBoundString = dataType.toString( upperBound );
    }

    public String getLowerBoundString() {
        return lowerBoundString;
    }

    public void setLowerBoundString(String lowerBoundString) {
        this.lowerBoundString = lowerBoundString;
    }

    public String getUpperBoundString() {
        return upperBoundString;
    }

    public void setUpperBoundString(String upperBoundString) {
        this.upperBoundString = upperBoundString;
    }

    public Operator getOperator() {
        return operator;
    }

    public void setOperator(Operator operator) {
        this.operator = operator;
    }

    public String getValidation() {
        return validation;
    }

    public void setValidation(String validation) {
        this.validation = validation;
    }

    public String getDataTypeClass() {
        return dataTypeClass;
    }

    public void setDataTypeClass(String dataTypeClass) {
        this.dataTypeClass = dataTypeClass;
    }
}
