package UIModule;

import javafx.scene.layout.Pane;
import javafx.scene.shape.Polyline;

/**
 * 纵射线UI控件类
 * @author kuangzengxiong
 * @date 2019/5/1 12:50
 */
public class VArrows extends Pane {
    private double x;
    private double y;
    private Polyline polyline;

    private double length=6;
    private double height;

    public VArrows(double x, double y,double height){
//        this.setBackground(new Background(new BackgroundFill(Color.BLUE,null,null)));
        this.x=x;
        this.y=y;
        this.height=height;

        if(height<0){
            this.setLayoutX(x-length/2);
            this.setLayoutY(y+height);
            polyline=new Polyline();
            polyline.setStrokeWidth(2);
            polyline.getPoints().addAll(length/2,-height,length/2,0.0);
            this.getChildren().add(polyline);

            polyline=new Polyline();
            polyline.setStrokeWidth(2);
            polyline.getPoints().addAll(0.0,5.0,length/2,0.0,length,5.0);
            this.getChildren().add(polyline);
        }else {
            this.setLayoutX(x-length/2);
            this.setLayoutY(y);
            polyline=new Polyline();
            polyline.setStrokeWidth(2);
            polyline.getPoints().addAll(length/2,0.0,length/2,height);
            this.getChildren().add(polyline);

            polyline=new Polyline();
            polyline.setStrokeWidth(2);
            polyline.getPoints().addAll(0.0,height-5,length/2,height,length,height-5);
            this.getChildren().add(polyline);
        }


        this.setMaxWidth(length);
        this.setMaxHeight(height>0?height:-height);


    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getHLength(){
        return height>0?height:-height;
    }

    public double[] getStart(){
        return new double[]{x,y};
    }

    public double[] getEnd(){
        return new double[]{x,y+height};
    }
}
