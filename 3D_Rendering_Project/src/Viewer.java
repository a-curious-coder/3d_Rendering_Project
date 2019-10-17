import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;
import java.util.ArrayList;
public class Viewer {

    public static void main(String[] args) {
        JFrame frame = new JFrame();
        Container pane = frame.getContentPane();
        pane.setLayout(new BorderLayout());

        // Slider to control horizontal rotation
        JSlider headingSlider = new JSlider(SwingConstants.HORIZONTAL, -90, 90, 0);
        pane.add(headingSlider, BorderLayout.SOUTH);

        // Slider to control vertical rotation
        JSlider pitchSlider = new JSlider (SwingConstants.VERTICAL, -90, 90, 0);
        pane.add(pitchSlider, BorderLayout.EAST);

        // Panel to display render results
        JPanel renderPanel = new JPanel() {
            public void paintComponent(Graphics g)  {
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(Color.BLACK);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // Rendering happens here.

                ArrayList<Triangle> tris = new ArrayList<>();
                tris.add(new Triangle(new Vertex(100, 100, 100),
                        new Vertex(-100, -100, 100),
                        new Vertex(-100, 100, -100),
                        Color.WHITE));
                tris.add(new Triangle(new Vertex(100, 100, 100),
                        new Vertex(-100, -100, 100),
                        new Vertex(100, -100, -100),
                        Color.RED));
                tris.add(new Triangle(new Vertex(-100, 100, -100),
                        new Vertex(100, -100, -100),
                        new Vertex(100, 100, 100),
                        Color.GREEN));
                tris.add(new Triangle(new Vertex(-100, 100, -100),
                        new Vertex(100, -100, -100),
                        new Vertex(-100, -100, 100),
                        Color.BLUE));

                double heading = Math.toRadians(headingSlider.getValue()); // Unsure
                Matrix3 headingTransform = new Matrix3(new double[]    { // transforms /rotates using matrices.
                        Math.cos(heading), 0, -Math.sin(heading),
                        0, 1, 0,
                        Math.sin(heading), 0, Math.cos(heading)
                });// Move horizontal.

                double pitch = Math.toRadians(pitchSlider.getValue()); // Gets value of pitchSlider - stores in pitch variable
                Matrix3 pitchTransform = new Matrix3(new double[]   {
                        1, 0, 0,
                        0, Math.cos(pitch), Math.sin(pitch), // Sin * pitch with math library.
                        0, -Math.sin(pitch), Math.cos(pitch)
                }); // Vertical Movement - Matrix that determines the rotation vertically
                Matrix3 transform = headingTransform.multiply(pitchTransform);

                g2.translate(getWidth() / 2, getHeight() / 2);
                g2.setColor(Color.WHITE);
                for (Triangle t : tris) {
                    Vertex v1 = transform.transform(t.v1);
                    Vertex v2 = transform.transform(t.v2);
                    Vertex v3 = transform.transform(t.v3);
                    Path2D path = new Path2D.Double();
                    path.moveTo(v1.x, v1.y);
                    path.lineTo(v2.x, v2.y);
                    path.lineTo(v3.x, v3.y);
                    path.closePath();
                    g2.draw(path);
                }
            }
        };

        headingSlider.addChangeListener(e -> renderPanel.repaint());
        pitchSlider.addChangeListener(e -> renderPanel.repaint());
        pane.add(renderPanel, BorderLayout.CENTER);
        frame.setSize(400, 400);
        frame.setVisible(true);
    }

}

class Vertex {
    double x, y, z;
    Vertex(double x, double y, double z)    {
        this.x = x; // X coord means movement to left-right directions
        this.y = y; // Y coord means movement up or down the screen
        this.z = z; // Z with be perpendicular to the screen - Positive z will be "towards to observer"
    }
}

class Triangle  {
    Vertex v1, v2, v3;
    Color color;
    Triangle(Vertex v1, Vertex v2, Vertex v3, Color color)  {
        this.v1 = v1;
        this.v2 = v2;
        this.v3 = v3;
        this.color = color;
    }
}

class Matrix3   {  // Handles Matrix-Matrix and Vector-Matrix multiplication
    double[] values;
    Matrix3(double[] values)    {
        this.values = values;
    }
    Matrix3 multiply(Matrix3 other) {
        double[] result = new double[9];
        for(int row = 0; row < 3; row++)    {
            for(int col = 0; col < 3; col++)    {
                for(int i = 0; i < 3; i++)  {
                    result[row * 3 + col] +=
                            this.values[row * 3 + i] * other.values[i * 3 + col];
                }
            }
        }
        return new Matrix3(result);
    }

    Vertex transform(Vertex in) {
        return new Vertex(
                in.x * values[0] + in.y * values[3] + in.z * values[6],
                in.x * values[1] + in.y * values[4] + in.z * values[7],
                in.x * values[2] + in.y * values[5] + in.z * values[8]
        );
    }
}