package ChatApp.domain.metrics;


import ChatApp.domain.User;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "measurement")
public class Measurement{

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(name="group_id")
    private Long groupId;
    @ManyToOne
    @JoinColumn(name="metric_id")
    private Metric metric;
    @Transient
    private Object value;
    @Column(name="value")
    private String valueString;
    @JoinColumn(name="user_id")
    @ManyToOne
    private User user;
    @Column(name="date_time")
    private LocalDateTime dateTime;
    @Column(name = "comment")
    private String comment;
    @Column( name = "flag" )
    private String flag;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    public Metric getMetric() {
        return metric;
    }

    public void setMetric(Metric metric) {
        this.metric = metric;
    }

    public Object getValue() {
        if( value == null ){
            value = metric.getDataType().toObject(valueString);
        }
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
        this.valueString = metric.getDataType().toString( value );
    }

    public String getValueString() {
        return valueString;
    }

    public void setValueString(String valueString) {
        this.valueString = valueString;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }
}
