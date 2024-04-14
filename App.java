import java.util.ArrayList;
import java.util.HashSet;
import java.util.PrimitiveIterator;
import java.util.Set;
import java.util.stream.IntStream;

import javafx.util.Duration;
import javafx.animation.*;
import javafx.application.Application;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Point3D;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
 
public class App extends Application {
 
    //each move corresponds to a particular integer
    static final int R = 1;
    static final int U = 2;
    static final int F = 3;
 
    private static Corner[] corners = new Corner[8];
    private static final PhongMaterial[] colors = {
        new PhongMaterial(Color.WHITE),
        new PhongMaterial(Color.YELLOW),
        new PhongMaterial(Color.RED),
        new PhongMaterial(Color.GREEN),
        new PhongMaterial(Color.BLUE),
        new PhongMaterial(Color.ORANGE)
    };
    private static int colorSelect = 0;
    private static Button[] paints = getPaints();
 
    private static Timeline turnTime;
    private static int[] rightCorners = {4, 6, 7, 5};
    private static int[] frontCorners = {2, 3, 7, 6};
    private static int[] topCorners = {1, 5, 7, 3};

    private static ArrayList<Integer> directions = new ArrayList<>();
    private static IntegerProperty directionIndex = new SimpleIntegerProperty(0);
    private static Label msg;
 
    public static void main(String...args) {
        launch(args);
    }
 
    //sets up all the UI elements including the cube, color pallete, and various buttons
    //sets up eventListeners that trigger changes when the user clicks certain buttons
    @Override
    public void start(Stage stage) throws Exception {
        
        Group solveRoot = new Group();
        Group cubeRoot = new Group();
 
        PhongMaterial black = new PhongMaterial(Color.BLACK);
        Box[] squares = new Box[24];
        for(int i = 0; i < 8; i++){
            Group cornerGroup = new Group();
            Box corner = new Box();
            corner.setWidth(75);
            corner.setHeight(75);
            corner.setDepth(75);
            Box xbox = new Box(1, 65, 65), yBox = new Box(65, 1, 65), zBox = new Box(65, 65, 1);
            if(i > 3){
                corner.setTranslateX(375);
                xbox.setTranslateX(413);
                yBox.setTranslateX(375);
                zBox.setTranslateX(375);
            }
            else{
                corner.setTranslateX(300);
                xbox.setTranslateX(262);
                yBox.setTranslateX(300);
                zBox.setTranslateX(300);
            }
            if(i % 2 == 1){
                corner.setTranslateY(225);
                xbox.setTranslateY(225);
                yBox.setTranslateY(187);
                zBox.setTranslateY(225);
            }
            else{
                corner.setTranslateY(300);
                xbox.setTranslateY(300);
                yBox.setTranslateY(338);
                zBox.setTranslateY(300);
            }
            if(i / 2 % 2 == 1){
                corner.setTranslateZ(-75);
                xbox.setTranslateZ(-75);
                yBox.setTranslateZ(-75);
                zBox.setTranslateZ(-113);
            }
            else{
                corner.setTranslateZ(0);
                xbox.setTranslateZ(0);
                yBox.setTranslateZ(0);
                zBox.setTranslateZ(38);
            }
            xbox.setMaterial(colors[0]);
            corner.setMaterial(black);
            cornerGroup.getChildren().addAll(corner, xbox, yBox, zBox);
           
            squares[i*3] = xbox;
            squares[i*3+1] = yBox;
            squares[i*3+2] = zBox;
 
            corners[i] = new Corner(cornerGroup);
            cubeRoot.getChildren().add(cornerGroup);
        }
        Font size15 = new Font("", 15);

        msg = new Label();
        msg.setWrapText(true);
        msg.setFont(size15);
        msg.setMinHeight(70);

        for(Box sticker : squares) sticker.addEventHandler(MouseEvent.MOUSE_CLICKED, (click) -> sticker.setMaterial(colors[colorSelect]));

        GridPane gp = new GridPane();
        gp.setPrefSize(600, 200);
        gp.setMinSize(0, 0);
        gp.setBackground(Background.fill(Color.WHITE));

        Group titleRoot = new Group();
        Scene scene = new Scene(titleRoot, 800, 500, true);

        Button menu = new Button("Back to Menu");
        menu.setPrefSize(200, 75);
        menu.addEventHandler(MouseEvent.MOUSE_CLICKED, (click) -> scene.setRoot(titleRoot));

        RotateTransition spin = new RotateTransition();
        spin.setNode(cubeRoot);
        spin.setDuration(Duration.millis(4e2));
        spin.setCycleCount(1);
        spin.setInterpolator(Interpolator.LINEAR);
        spin.setAxis(Rotate.Y_AXIS);

        Button rotCW = new Button("Spin Cube");
        rotCW.setPrefSize(200, 75);
        rotCW.addEventHandler(MouseEvent.MOUSE_CLICKED, (click) -> {
            double millis = spin.getCurrentTime().toMillis();
            if(millis == 400 || millis == 0){
                spin.setFromAngle(cubeRoot.getRotate());
                spin.setToAngle(cubeRoot.getRotate()+90);
                spin.play();
            }
        });

        Button next = new Button("Next");
        next.setPrefSize(250, 65);
        next.setFont(size15);
        next.addEventHandler(MouseEvent.MOUSE_CLICKED, (click) -> {
            if(directionIndex.intValue() < directions.size() - 1 && turnIsDone()){
                directionIndex = new SimpleIntegerProperty(directionIndex.get() + 1);
                int currMove = directions.get(directionIndex.intValue());
                msg.setText("Move " + directionIndex.intValue() + "/" + (directions.size()-1) + ": " + Solver.MOVES.get(currMove) + (directionIndex.intValue() == directions.size()-1 ? ". Your cube is solved!" : ""));
                prepareTurn(currMove, 1);
            }
        });
 
        Button prev = new Button("Previous");
        prev.setPrefSize(250, 75);
        prev.setFont(size15);
        prev.addEventHandler(MouseEvent.MOUSE_CLICKED, (click) -> {
            if(directionIndex.intValue() > 0 && turnIsDone()){
                int currMove = directions.get(directionIndex.intValue());
                directionIndex = new SimpleIntegerProperty(directionIndex.get() - 1);
                msg.setText((directionIndex.intValue() > 0 ? "Move " + directionIndex.intValue() + "/" + (directions.size()-1) + ": " : "") + Solver.MOVES.get(directions.get(directionIndex.intValue())));
                prepareTurn(currMove, -1);
            }
        });

        Button reset = new Button("Reset Cube");
        reset.setPrefSize(250, 50);
        reset.setTextFill(Color.RED);
        reset.setFont(size15);

        ScreenUpdate duringSolveUI = (() -> {
            gp.getChildren().clear();
            msg.setTextFill(Color.BLACK);
            String begin = "Your cube can be solved in " + (directions.size() - 1) + " move" + (directions.size() == 2 ? "" : "s") + ". Hold the cube like this.";
            Solver.MOVES.put(-1, begin);
            msg.setText(begin);
            msg.setPrefSize(250, 75);
            gp.add(reset, 0, 0);
            gp.add(msg, 0, 1);
            gp.add(next, 1, 2);
            gp.add(prev, 0, 2);
        });

        Button solve = new Button("Solve");
        solve.setBackground(Background.fill(Color.GREEN));
        solve.setTextFill(Color.WHITE);
        solve.setPrefSize(200, 75);
        solve.addEventHandler(MouseEvent.MOUSE_CLICKED, (click) -> {
            spin.setFromAngle(cubeRoot.getRotate() % 360);
            spin.setToAngle(0);
            spin.play();
            if(isValid(squares)){
                Solver.Move solved = Solver.solveCube(config(squares), solution(squares));
                if(solved == null){
                    msg.setTextFill(Color.RED);
                    msg.setText("Not a solvable puzzle. Try flipping a corner. If it still cannot be solved, flip that same corner in the same direction again. Your cube should then be solvable.");
                }   
                else if(solved.previous == null){
                    msg.setTextFill(Color.BLACK);
                    msg.setText("The Cube is already Solved");
                }
                else{
                    buildDirections(solved);
                    duringSolveUI.update();
                }
            }
            else {
                msg.setTextFill(Color.RED);
                msg.setText("Not a possible configuration. Make sure you entered the colors correctly.");
            }
        });

        ScreenUpdate preSolveUI = (() -> {
            msg.setText("Select a color, then click a square to change it to that color. Hit the solve button once you are ready.");
            msg.setPrefSize(450, 75);
            gp.add(menu, 0, 0);
            gp.add(msg, 0, 1);
            int y = 2;
            for(Button paint : paints) gp.add(paint, 0, y++);
            gp.add(rotCW, 0, 8);
            gp.add(solve, 0, 9);
            gp.setVgap(10);
        });
        preSolveUI.update();

        reset.addEventHandler(MouseEvent.MOUSE_CLICKED, (click) -> {
            for(Corner corn : corners) corn.reset();
            for(Box b : squares) b.setMaterial(colors[0]);
            rightCorners = new int[]{4, 6, 7, 5};
            frontCorners = new int[]{2, 3, 7, 6};
            topCorners = new int[]{1, 5, 7, 3};
            gp.getChildren().clear();
            directions.clear();
            directionIndex = new SimpleIntegerProperty(0);
            preSolveUI.update();
        });
       
        solveRoot.getChildren().add(gp);

        Rotate cubeRotateX = new Rotate();
        cubeRotateX.setAngle(30);
        cubeRotateX.setPivotX(337.5);
        cubeRotateX.setPivotY(262.5);
        cubeRotateX.setPivotZ(-37.5);
        cubeRotateX.setAxis(Rotate.X_AXIS);
        
        Rotate cubeRotateY = new Rotate();
        cubeRotateY.setAngle(30);
        cubeRotateY.setPivotX(337.5);
        cubeRotateY.setPivotY(262.5);
        cubeRotateY.setPivotZ(-37.5);
        cubeRotateY.setAxis(Rotate.Y_AXIS);
 
        cubeRoot.getTransforms().addAll(cubeRotateX, cubeRotateY);
        cubeRoot.setTranslateY(80);
        solveRoot.getChildren().add(cubeRoot);
 
        turnTime = new Timeline(new KeyFrame(Duration.ZERO), new KeyFrame(Duration.seconds(1)));
        turnTime.setCycleCount(1);

        Text title = new Text("Joe's 2x2 Cube Solver");
        title.setFont(new Font("", 50));
        title.setFill(Color.ROYALBLUE);

        Text descrip = new Text("Solves your cube in the least amount of moves possible");
        descrip.setFont(new Font("", 20));
        descrip.setFill(Color.ROYALBLUE);

        Font size30 = new Font("", 30);

        Button start = new Button("Start");
        start.setPrefSize(300, 75);
        start.setFont(size30);
        start.addEventHandler(MouseEvent.MOUSE_CLICKED, (click) -> scene.setRoot(solveRoot));

        Group credRoot = new Group();

        String name = "Joe Fielding";
        Text credits = new Text("Lead Developer - " + name
        + "\nLead Analyst - " + name
        + "\nAlgorithmic Design Manager - " + name 
        + "\nGraphics and Design Manager - "+ name 
        + "\nDebugging Manager - "+ name
        + "\nMathematics Director - " + name);
        credits.setFont(size30);

        Text credTxt = new Text("Credits");
        credTxt.setFont(size30);

        Button back = new Button("Back to Menu");
        back.addEventHandler(MouseEvent.MOUSE_CLICKED, (click) -> scene.setRoot(titleRoot));
        back.setPrefSize(300, 75);
        back.setFont(size30);
        
        BorderPane creditPane = new BorderPane();
        creditPane.setTop(credTxt);
        creditPane.setCenter(credits);
        creditPane.setBottom(back);
        BorderPane.setAlignment(credTxt, Pos.CENTER);
        BorderPane.setAlignment(back, Pos.CENTER);
        creditPane.setPrefSize(800, 500);
        credRoot.getChildren().add(creditPane);

        Button creditButton = new Button("Credits");
        creditButton.setPrefSize(300, 75);
        creditButton.setFont(size30);
        creditButton.addEventHandler(MouseEvent.MOUSE_CLICKED, (click) -> scene.setRoot(credRoot));

        FlowPane options = new FlowPane(descrip, start, creditButton);
        options.setMaxWidth(300);
        options.setAlignment(Pos.CENTER);
        options.setVgap(10);

        Image cubeImage = new Image("Cube.png");
        ImageView imview = new ImageView();
        imview.setImage(cubeImage);

        BorderPane pane = new BorderPane();
        pane.setTop(title);
        pane.setPrefSize(800, 500);
        pane.setCenter(imview);
        pane.setBottom(options);
        BorderPane.setAlignment(title, Pos.CENTER);
        BorderPane.setAlignment(options, Pos.CENTER);
        BorderPane.setAlignment(imview, Pos.CENTER);
        titleRoot.getChildren().addAll(pane);

        stage.setScene(scene);
        stage.getIcons().add(cubeImage);
        stage.setTitle("Joe's 2x2 Cube Solver");
        stage.show();
    }

    static void prepareTurn(int currMove, int sign){
        int degrees = 90 * sign;
        if(currMove % 3 == 1) degrees = 90 * -sign;
        else if(currMove % 3 == 2) degrees = 180;
        int turn = R;
        if(currMove / 3 == 0) turn = U;
        else if(currMove / 3 == 2) turn = F;
        makeTurn(turn, degrees);
    }

    //sets up a color pallete that the user can use to color the cube
    static Button[] getPaints(){
        Button[] paints = new Button[6];
        int index = 0;
        for(PhongMaterial mat : colors){
            Button color = new Button();
            paints[index] = color;
            int colorNum = index++;
            color.setPrefSize(100, 30);
            color.setBackground(Background.fill(mat.getDiffuseColor()));
            color.addEventHandler(MouseEvent.MOUSE_CLICKED, (event) -> {
                if(!msg.getText().contains("Select")){
                    msg.setTextFill(Color.BLACK);
                    msg.setText("Select a color, then click a square to change it to that color. Hit the solve button once you are ready.");
                }
                colorSelect = colorNum;
            });
            color.setBorder(Border.stroke(Color.BLACK));
        }
        return paints;
    }
 
    //compiles the directions after the solution to the puzzle has been found
    private static void buildDirections(Solver.Move solved) {

        while(solved.previous != null){
            directions.add(0, solved.moveID);
            solved = solved.previous;
        }
        directions.add(0, -1);
    }

    //turns the cube on the screen
    //visually shows the moves the user has to make to solve their cube
    private static void makeTurn(int turn, int degrees){
 
        int[] face;
        switch(turn){
            case R: face = rightCorners;
            break;
            case U: face = topCorners;
            break;
            default: face = frontCorners;
        }

        int dir = degrees == 180 ? -2 : Math.abs(degrees) / degrees;
        Rotate[] r = new Rotate[4];
        for(int i = 0; i < 4; i++) r[i] = corners[face[i]].turn(turn, dir);
 
        turnTime.getKeyFrames().setAll(
            new KeyFrame(Duration.ZERO, new javafx.animation.KeyValue(r[0].angleProperty(), r[0].getAngle()),
            new javafx.animation.KeyValue(r[1].angleProperty(), r[1].getAngle()),
            new javafx.animation.KeyValue(r[2].angleProperty(), r[2].getAngle()),
            new javafx.animation.KeyValue(r[3].angleProperty(), r[3].getAngle())),
            new KeyFrame(Duration.seconds(1), new javafx.animation.KeyValue(r[0].angleProperty(), r[0].getAngle()+degrees),
            new javafx.animation.KeyValue(r[1].angleProperty(), r[1].getAngle()+degrees),
            new javafx.animation.KeyValue(r[2].angleProperty(), r[2].getAngle()+degrees),
            new javafx.animation.KeyValue(r[3].angleProperty(), r[3].getAngle()+degrees))
        );
        turnTime.play();
 
        switch(turn){
            case R: rightSwap(dir);
            break;
            case U: topSwap(dir);
            break;
            default: frontSwap(dir);
        }

        int[] corns = face.clone();
        for(int i = 0; i < 4; i++) face[i] = corns[(i - dir + 4) % 4];   
    }
   
    //swaps the appropriate corners when a right turn is made
    static void rightSwap(int dir){
        frontCorners[2] = rightCorners[(2-dir) % 4];
        frontCorners[3] = rightCorners[1-dir];
        topCorners[1] = rightCorners[(3-dir) % 4];
        topCorners[2] = rightCorners[(2-dir) % 4];
    }
 
    //swaps the appropriate corners when a front turn is made
    static void frontSwap(int dir){
        rightCorners[1] = frontCorners[(3-dir) % 4];
        rightCorners[2] = frontCorners[(2-dir) % 4];
        topCorners[2] = frontCorners[(2-dir) % 4];
        topCorners[3] = frontCorners[1-dir];
    }
 
    //swaps the appropriate corners when a top turn is made
    static void topSwap(int dir){
        rightCorners[2] = topCorners[(2-dir) % 4];
        rightCorners[3] = topCorners[1-dir];
        frontCorners[1] = topCorners[(3-dir) % 4];
        frontCorners[2] = topCorners[(2-dir) % 4];
    }

    //checks if the turn animation is done so that it cannot be restart while it is still running
    static boolean turnIsDone(){
        double time = turnTime.getCurrentTime().toSeconds();
        return (time == 1 || time == 0);
    }

    //checks if the cube is a valid configuration based on the colors that the user entered
    //will not attempt to solve if this metyhod returns false, but will tell the user to check the colors
    static boolean isValid(Box[] squares){
        Set<Long> used = new HashSet<>();
        int[] colorCount = new int[6];
        for(int i = 0; i < 24; i+=3){
            int col1 = value((PhongMaterial) squares[i].getMaterial()), col2 = value((PhongMaterial) squares[i+1].getMaterial()),
            col3 = value((PhongMaterial) squares[i+2].getMaterial()), opp1 = opposite(col1), opp2 = opposite(col2);
            long config = col1 * 36 + col2 * 6 + col3;
            if((col1 == col2 || col1 == col3 || col3 == col2) || used.contains(config) || (opp1 == col2 || opp2 == col3 || opp1 == col3)) return false;
            used.add(config);
            colorCount[col1]+=1;
            colorCount[col2]+=1;
            colorCount[col3]+=1;
        }
        for(int colNum : colorCount) if(colNum != 4) return false;
        return true;
    }

    //each cube state has a unique id stored as a long
    //uses a base 6 number system to determine the configuration as there as six solors on the cube
    static long config(Box[] squares) {
        long config = 0L;
        PrimitiveIterator.OfInt orders = IntStream.of(2, 8, 5, 20, 14, 12, 3, 11, 0, 18, 16, 19, 7, 9, 4, 17, 15, 13, 6, 10, 1).iterator();
        for(int i = 3; i < 24; i++) config += value((PhongMaterial) squares[i].getMaterial()) * Solver.sixPow[orders.nextInt()];
        return config;
    }

    //return the value of a particular color (base 6 number system)
    //white = 0, yellow = 1, red = 2, green = 3, blue = 4, orange = 5
    static int value(PhongMaterial mat){
        int val = 0;
        for(PhongMaterial ph : colors){
            if(mat == ph) return val;
            val++;
        }
        return 0;
    }

    //finds the configuration id of the solution to the puzzle based on the colors the user enetered
    //can be determined based on the bottom, left, back corner because this corner never moves since we only make right, top and front moves
    static long solution(Box[] squares){
        long solution = 0L;
        PrimitiveIterator.OfInt it = IntStream.of(2, 3, 20, 6, 7, 17, 18, 14, 15, 16, 8, 9, 10, 11, 4, 5, 19, 0, 1, 12, 13).iterator();
        for(int i = 0; i < 3; i++){
            int val = value((PhongMaterial) squares[i].getMaterial());
            for(int j = i*7; j < (i+1)*7; j++) solution += Solver.sixPow[it.nextInt()] * ((j < i*7+3) ? val : opposite(val));
        }
        return solution;
    }

    //finds the value of the color opposite of the color that corresponds to the given integer value
    //opposite(white) -> yellow, opposite(blue) -> green, opposite(red) -> orange 
    static int opposite(int colrVal){
        switch(colrVal){
            case 0: return 1;
            case 1: return 0;
            case 2: return 5;
            case 3: return 4;
            case 4: return 3;
        }
        return 2;
    }
}

//Corner class representing each of the eight corners on the cube
class Corner{
 
    //1 = x-axis
    //2 = y-axis
    //3 = z-axis
    Group group;
    int X;
    int Z;
    int Y;
 
    Corner(Group group){
        this.group = group;
        this.X = -1;
        this.Y = 2;
        this.Z = 3;
    }
 
    //return a Rotate object that is used to rotate a corner when a move is made
    //has to take into account which way a corner's local coordinate system is oriented.
    Rotate turn(int face, int dir){
       
        Rotate rot = new Rotate();
        rot.setPivotX(337.5);
        rot.setPivotY(262.5);
        rot.setPivotZ(-37.5);
       
        int axis = 0;
        if(face == App.U) axis = this.Y;
        else if(face == App.F) axis = this.Z;
        else axis = this.X;
       
        switch(axis){
            case 1: rot.setAxis(Rotate.X_AXIS);
            break;
            case 2: rot.setAxis(Rotate.Y_AXIS);
            break;
            case 3: rot.setAxis(Rotate.Z_AXIS);
            break;
            case -1: rot.setAxis(new Point3D(-1, 0, 0));
            break;
            case -2: rot.setAxis(new Point3D(0, -1, 0));
            break;
            case -3: rot.setAxis(new Point3D(0, 0, -1));
        }
        swapAxes(face, dir);
        this.group.getTransforms().add(rot);
        return rot;
    }
 
    //updates the X, Y, and Z instance variables after a turn is made as a corner's local coordinate system rotates
    void swapAxes(int face, int dir){
        if(dir != -2){
            if(face == App.F){
                int temp = this.X * -dir;
                this.X = this.Y * dir;
                this.Y = temp;
            }
            else if(face == App.R){
                int temp = this.Z * dir;
                this.Z = this.Y * -dir;
                this.Y = temp;
            }
            else{
                int temp = this.Z * -dir;
                this.Z = this.X * dir;
                this.X = temp;
            }
        }
        else{
            if(face == App.F){
                this.X = -this.X;
                this.Y = -this.Y;
            }
            else if(face == App.R){
                this.Z = -this.Z;
                this.Y = -this.Y;
            }
            else{
                this.X = -this.X;
                this.Z = -this.Z;
            }
        }
    }

    //resets the axes back to their original orientation after
    void reset(){
        this.group.getTransforms().clear();
        this.X = -1;
        this.Y = 2;
        this.Z = 3;
    }
}

//Implemented as an aynonomous inner type for each of the different ways the screen has to be updated after performing certain actions 
interface ScreenUpdate{
    public void update();
}