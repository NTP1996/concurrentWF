package UIModule;

import javafx.scene.layout.Pane;
import javafx.scene.shape.Ellipse;

/**开始、结束节点UI控制类
 * @author kuangzengxiong
 * @date 2019/4/30 20:17
 */
public class CircleDraw extends Pane {
    private double length=30;
    private Ellipse ellipse;
    private double x;
    private double y;

    public CircleDraw(double x, double y){
//        this.setBackground(new Background(new BackgroundFill(Color.BLUE,null,null)));
        this.x=x;
        this.y=y;
        this.setLayoutX(x);
        this.setLayoutY(y-length/2);
        this.setMaxWidth(length);
        this.setMaxHeight(length);

        ellipse=new Ellipse();
        ellipse.setCenterX(length/2);
        ellipse.setCenterY(length/2);
        ellipse.setRadiusX(length/2);
        ellipse.setRadiusY(length/2);

        this.getChildren().add(ellipse);
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

    public double[] getLeft(){
        return new double[]{x,y};
    }

    public double[] getRight(){
        return new double[]{x+length,y};
    }
}

