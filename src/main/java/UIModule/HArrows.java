package UIModule;

import javafx.scene.layout.Pane;
import javafx.scene.shape.Polyline;

/**横射线UI控件类
 * @author kuangzengxiong
 * @date 2019/4/30 20:45
 */
public class HArrows extends Pane {
    private double x;
    private double y;
    private Polyline polyline;

    private double length;
    private double height=6;

    public HArrows(double x, double y,double length){
//        this.setBackground(new Background(new BackgroundFill(Color.BLUE,null,null)));

        this.x=x;
        this.y=y;
        this.length=length;

        if(length<0){
            this.setLayoutX(x+length);
            this.setLayoutY(y-height/2);
            polyline=new Polyline();
            polyline.setStrokeWidth(2);
            polyline.getPoints().addAll(-length,height/2,0.0,height/2);
            this.getChildren().add(polyline);

            polyline=new Polyline();
            polyline.setStrokeWidth(2);
            polyline.getPoints().addAll(5.0,0.0,0.0,height/2,5.0,height);
            this.getChildren().add(polyline);
        }else{
            this.setLayoutX(x);
            this.setLayoutY(y-height/2);
            polyline=new Polyline();
            polyline.setStrokeWidth(2);
            polyline.getPoints().addAll(0.0,height/2,length,height/2);
            this.getChildren().add(polyline);

            polyline=new Polyline();
            polyline.setStrokeWidth(2);
            polyline.getPoints().addAll(length-5,0.0,length,height/2,length-5,height);
            this.getChildren().add(polyline);
        }


        this.setMaxWidth(length>0?length:-length);
        this.setMaxHeight(height);



    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }



    public double getLength(){
        return length>0?length:-length;
    }

    public double[] getStart(){
        return new double[]{x,y};
    }
    public double[] getEnd(){
        return new double[]{x+length,y};
    }
}
