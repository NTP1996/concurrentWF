package util;

import javafx.beans.property.SimpleStringProperty;

/**
 * @author kuangzengxiong
 * @date 2019/4/30 14:50
 */
public class TaskDataRelation {
    private final SimpleStringProperty taskName;
    private final SimpleStringProperty inputDataName;
    private final SimpleStringProperty outputDataName;

    public TaskDataRelation(String taskName, String inputName, String outputName){
        this.taskName=new SimpleStringProperty(taskName);
        this.inputDataName=new SimpleStringProperty(inputName);
        this.outputDataName=new SimpleStringProperty(outputName);
    }

    public String getTaskName() {
        return taskName.get();
    }

    public SimpleStringProperty taskNameProperty() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName.set(taskName);
    }

    public String getInputDataName() {
        return inputDataName.get();
    }

    public SimpleStringProperty inputDataNameProperty() {
        return inputDataName;
    }

    public void setInputDataName(String inputDataName) {
        this.inputDataName.set(inputDataName);
    }

    public String getOutputDataName() {
        return outputDataName.get();
    }

    public SimpleStringProperty outputDataNameProperty() {
        return outputDataName;
    }

    public void setOutputDataName(String outputDataName) {
        this.outputDataName.set(outputDataName);
    }
}
