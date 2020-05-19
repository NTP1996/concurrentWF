package UIModule;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Polyline;

/**任务节点UI控件类
 * @author kuangzengxiong
 * @date 2019/4/29 19:22
 */
public class TaskDraw extends Pane {
    private static double length=25;
    private Polyline polyline;
    private Label labelText;
    private double x;
    private double y;

    public TaskDraw(double x,double y){
        super();
        this.x=x;
        this.y=y;

        this.setLayoutX(x);
        this.setLayoutY(y-length);
        this.setMaxWidth(length*3);
        this.setMaxHeight(length*2);


        polyline=new Polyline();
        polyline.getPoints().addAll(0.0,0.0,
                length*3,0.0,
                length*3, length*2,
                0.0,length*2,
                0.0,0.0);

        this.getChildren().add(polyline);

        labelText=new Label();
        labelText.setLayoutY(15);
        labelText.setMaxWidth(length*3);
        labelText.setMinWidth(length*3);
        labelText.setAlignment(Pos.CENTER);
        labelText.setStyle("-fx-font-weight: bold");
        this.getChildren().add(labelText);
    }

    public static double getLength() {
        return length;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public void setText(String text){
        labelText.setText(text);
    }

    public double[] getLeft(){
        return new double[]{x,y};
    }

    public double[] getRight(){
        return new double[]{x+length*3,y};
    }

//    public double[] getLTCoord(){
//        return new double[]{x,y};
//    }
//
//    public double[] getRTCoord(){
//        return new double[]{x+length*3,y};
//    }
//
//    public double[] getRBCoord(){
//        return new double[]{x+length*3,y+length*2};
//    }
//
//    public double[] getLBCoord(){
//        return new double[]{x,y+length*2};
//    }


}
