package UIModule;

import javafx.scene.layout.Pane;
import javafx.scene.shape.Polyline;

/**横直线UI控件类
 * @author kuangzengxiong
 * @date 2019/4/30 22:01
 */
public class HLine extends Pane {
    private double x;
    private double y;
    private double length;
    private Polyline polyline;
    private double height=6;

    public HLine(double x, double y, double length){
//        this.setBackground(new Background(new BackgroundFill(Color.BLUE,null,null)));
        this.x=x;
        this.y=y;
        this.length=length;

        if(length<0){
            this.setLayoutX(x+length);
            this.setLayoutY(y-height/2);
        }else{
            this.setLayoutX(x);
            this.setLayoutY(y-height/2);
        }

        polyline=new Polyline();
        polyline.setStrokeWidth(2);
        polyline.getPoints().addAll(0.0,height/2,length>0?length:-length,height/2);
        this.getChildren().add(polyline);
        this.setMaxWidth(length>0?length:-length);
        this.setMaxHeight(height);


    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getLength() {
        return length;
    }

    public double[] getStart(){
        return new double[]{x,y};
    }

    public double[] getEnd(){
        return new double[]{x+length,y};
    }
}
