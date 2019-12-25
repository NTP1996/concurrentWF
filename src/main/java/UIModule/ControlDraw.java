package UIModule;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Polyline;
import javafx.scene.text.Font;

/**
 * 控制流节点UI控件类
 * @author kuangzengxiong
 * @date 2019/4/29 17:53
 */
public class ControlDraw extends Pane {
    private static double length=25;
    private Polyline polyline;
    private Label labelText;
    private double x;
    private double y;

    public ControlDraw(double x, double y){
        super();
        this.x=x;
        this.y=y;

        this.setLayoutX(x);
        this.setLayoutY(y-length);
        this.setMaxWidth(length*2);
        this.setMaxHeight(length*2);

        polyline=new Polyline();
        polyline.getPoints().addAll(0+length,0.0,
                0+length*2,0+length,
                0+length,0+length*2,
                0.0,0+length,
                0+length,0.0);
        this.getChildren().add(polyline);


        labelText=new Label();
        labelText.setMaxWidth(length*2);
        labelText.setMinWidth(length*2);
        labelText.setFont(new Font("Arial", 40));
        labelText.setAlignment(Pos.CENTER);
        labelText.setLayoutY(3);
        this.getChildren().add(labelText);


    }

    public void setText(String text){
        labelText.setText(text);
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



    public double[] getTop(){
        return new double[]{x+length,y-length};
    }
    public double[] getRight(){
        return new double[]{x+length*2,y};
    }
    public double[] getBottom(){
        return new double[]{x+length,y+length};
    }
    public double[] getLeft(){
        return new double[]{x,y};
    }

}
