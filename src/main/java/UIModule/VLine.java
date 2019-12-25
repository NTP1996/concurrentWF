package UIModule;

import javafx.scene.layout.Pane;
import javafx.scene.shape.Polyline;

/**
 * 纵直线UI控件类
 * @author kuangzengxiong
 * @date 2019/4/30 22:28
 */
public class VLine extends Pane {
    private double x;
    private double y;
    private double length=6;
    private Polyline polyline;
    private double height;

    public VLine(double x, double y, double height){
//        this.setBackground(new Background(new BackgroundFill(Color.BLUE,null,null)));
        this.x=x;
        this.y=y;
        if(height<0){
            this.height=height;
            this.setLayoutX(x-length/2);
            this.setLayoutY(this.y+this.height);
        }else{
            this.height=height;
            this.setLayoutX(x-length/2);
            this.setLayoutY(y);
        }
        polyline=new Polyline();
        polyline.setStrokeWidth(2);
        polyline.getPoints().addAll(length/2,this.height>0?this.height:-this.height,length/2,0.0);
        this.getChildren().add(polyline);

        this.setMaxWidth(length);
        this.setMaxHeight(this.height>0?this.height:-this.height);


    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getHLength() {
        return height>0?height:-height;
    }

    public double[] getStart(){
        return new double[]{x,y};
    }

    public double[] getEnd(){
        return new double[]{x,y+height};
    }
}
