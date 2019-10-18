import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import java.awt.geom.*;
import java.awt.image.BufferedImage;

public class Viewer {

    public static void main(String[] args) {
        JFrame frame = new JFrame();

        Container pane = frame.getContentPane();
        pane.setLayout(new BorderLayout());

        // Slider to control horizontal rotation
        JSlider headingSlider = new JSlider(SwingConstants.HORIZONTAL, -180, 180, 0);
        pane.add(headingSlider, BorderLayout.SOUTH);

        // Slider to control vertical rotation
        JSlider pitchSlider = new JSlider (SwingConstants.VERTICAL, -90, 90, 0);
        pane.add(pitchSlider, BorderLayout.EAST);

        // Panel to display render results
        JPanel renderPanel = new JPanel() {
            public void paintComponent(Graphics g)  {
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(Color.BLACK); // set color of g2 to black
                g2.fillRect(0, 0, getWidth(), getHeight()); // Fill rectangle zone from 0,0 to width/height

                // Rendering happens here.

                List<Triangle> tris = new ArrayList<>(); // Arraylist to hold each triangle with their attributes
                tris.add(new Triangle( // First triangle
                        new Vertex(100, 100, 100),
                        new Vertex(-100, -100, 100),
                        new Vertex(-100, 100, -100),
                        Color.WHITE));
                tris.add(new Triangle( // Second triangle... etc.
                        new Vertex(100, 100, 100),
                        new Vertex(-100, -100, 100),
                        new Vertex(100, -100, -100),
                        Color.RED));
                tris.add(new Triangle(
                        new Vertex(-100, 100, -100),
                        new Vertex(100, -100, -100),
                        new Vertex(100, 100, 100),
                        Color.GREEN));
                tris.add(new Triangle(
                        new Vertex(-100, 100, -100), // Vertex is a point to another point. - These are the x, y and z coords.
                        new Vertex(100, -100, -100), // Vertex starts at 0 then this one will start from the last set points.
                        new Vertex(-100, -100, 100),
                        Color.BLUE));
                //Builds a tetrahedron - making use of class Triangle to produce these.
                for (int i = 0; i < 4; i++) {
                    tris = inflate(tris);
                }

                double heading = Math.toRadians(headingSlider.getValue()); // Get value of headingSlider - To be paired with rotation angle of shape
                Matrix3 headingTransform = new Matrix3(new double[]    { // transforms / rotates using correct transformation matrix.
                        Math.cos(heading), 0, -Math.sin(heading),
                        0, 1, 0,
                        Math.sin(heading), 0, Math.cos(heading)
                });// Moves the tetrahedron horizontally (right n left)

                double pitch = Math.toRadians(pitchSlider.getValue()); // Gets value of pitchSlider - stores in pitch variable
                Matrix3 pitchTransform = new Matrix3(new double[]   {
                        1, 0, 0,
                        0, Math.cos(pitch), Math.sin(pitch), // Sin * pitch with math library.
                        0, -Math.sin(pitch), Math.cos(pitch)
                }); // Vertical Movement - Matrix that determines the rotation vertically

                Matrix3 transform = headingTransform.multiply(pitchTransform);

                g2.translate(getWidth() / 2, getHeight() / 2);
                g2.setColor(Color.WHITE);

                // BufferedImage will represent the tetrahedrom, colours determined by ARGB - A being transparency of colour.
                BufferedImage img = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
                double[] zBuffer = new double[img.getWidth() * img.getHeight()];

                // initialize array with extremely far away depths
                for (int q = 0; q < zBuffer.length; q++) {
                    zBuffer[q] = Double.NEGATIVE_INFINITY;
                }

                for (Triangle t : tris) {
                    Vertex v1 = transform.transform(t.v1);
                    v1.x += getWidth() / 2;
                    v1.y += getHeight() / 2;
                    Vertex v2 = transform.transform(t.v2);
                    v2.x += getWidth() / 2;
                    v2.y += getHeight() / 2;
                    Vertex v3 = transform.transform(t.v3);
                    v3.x += getWidth() / 2;
                    v3.y += getHeight() / 2;

                    Vertex ab = new Vertex(v2.x - v1.x, v2.y - v1.y, v2.z - v1.z); // Crossproduct using determinants- two vertexes
                    Vertex ac = new Vertex(v3.x - v1.x, v3.y - v1.y, v3.z - v1.z); // Second crossproduct
                    Vertex norm = new Vertex(
                            ab.y * ac.z - ab.z * ac.y,
                            ab.z * ac.x - ab.x * ac.z,
                            ab.x * ac.y - ab.y * ac.x
                    );

                    double normalLength =
                            Math.sqrt(norm.x * norm.x + norm.y * norm.y + norm.z * norm.z);
                    norm.x /= normalLength;
                    norm.y /= normalLength;
                    norm.z /= normalLength;

                    double angleCos = Math.abs(norm.z);


                    // compute rectangular bounds for triangle
                    int minX = (int) Math.max(0, Math.ceil(Math.min(v1.x, Math.min(v2.x, v3.x)))); // ceil rounds number to closest int greater than result.
                    int maxX = (int) Math.min(img.getWidth() - 1, Math.floor(Math.max(v1.x, Math.max(v2.x, v3.x)))); // floor rounds the number to closest number less than result.

                    int minY = (int) Math.max(0, Math.ceil(Math.min(v1.y, Math.min(v2.y, v3.y))));
                    int maxY = (int) Math.min(img.getHeight() - 1, Math.floor(Math.max(v1.y, Math.max(v2.y, v3.y))));

                    double triangleArea = (v1.y - v3.y) * (v2.x - v3.x) + (v2.y - v3.y) * (v3.x - v1.x); // One triangle's area?

                    for (int y = minY; y <= maxY; y++) { // Not fully sure what's happening here.
                        for (int x = minX; x <= maxX; x++) {

                            double b1 = ((y - v3.y) * (v2.x - v3.x) + (v2.y - v3.y) * (v3.x - x)) / triangleArea;
                            double b2 = ((y - v1.y) * (v3.x - v1.x) + (v3.y - v1.y) * (v1.x - x)) / triangleArea;
                            double b3 = ((y - v2.y) * (v1.x - v2.x) + (v1.y - v2.y) * (v2.x - x)) / triangleArea;

                            if (b1 >= 0 && b1 <= 1 && b2 >= 0 && b2 <= 1 && b3 >= 0 && b3 <= 1) {
                                double depth = b1 * v1.z + b2 * v2.z + b3 * v3.z; //
                                int zIndex = y * img.getWidth() + x;
                                if (zBuffer[zIndex] < depth) {
                                    img.setRGB(x, y, getShade(t.color, angleCos).getRGB());
                                    zBuffer[zIndex] = depth;
                                }
                            }
                        }
                    }


                }

                g2.drawImage(img, -400, -400, null); // Draws Tetrahedron - refers to buffer, pos 0,0 - Had to put to -400, -400 to center the shape... idk why
            }

        };

        pane.add(renderPanel, BorderLayout.CENTER);

        headingSlider.addChangeListener(e -> renderPanel.repaint());
        pitchSlider.addChangeListener(e -> renderPanel.repaint());


        //Frame settings - What holds the shape.
        frame.setSize(800, 800); // Size of window
        frame.setTitle("3D Render Project"); // Title of window
        frame.setLocationRelativeTo(null); // Positions window center
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Closes program when X is clicked
        frame.setVisible(true); // Window is visible.
    }

    public static Color getShade(Color color, double shade) { // Sorts out the colour of each side of the shape.
        double redLinear = Math.pow(color.getRed(), 2.4) * shade; // More accurate value to shade
        double greenLinear = Math.pow(color.getGreen(), 2.4) * shade;
        double blueLinear = Math.pow(color.getBlue(), 2.4) * shade;

        int red = (int) Math.pow(redLinear, 1/2.4);
        int green = (int) Math.pow(greenLinear, 1/2.4);
        int blue = (int) Math.pow(blueLinear, 1/2.4);

        return new Color(red, green, blue);
    }

    /**
     * Repeatedly divides each triangle into four smallest triangles - "inflates" the tetrahedron
     * @param tris - Holds triangle objects
     * @return
     */
    public static List<Triangle> inflate(List<Triangle> tris) { // This function allows me to create a sphere shape.
        List<Triangle> result = new ArrayList<>();
        for (Triangle t : tris) {
            Vertex m1 = new Vertex((t.v1.x + t.v2.x)/2, (t.v1.y + t.v2.y)/2, (t.v1.z + t.v2.z)/2);
            Vertex m2 = new Vertex((t.v2.x + t.v3.x)/2, (t.v2.y + t.v3.y)/2, (t.v2.z + t.v3.z)/2);
            Vertex m3 = new Vertex((t.v1.x + t.v3.x)/2, (t.v1.y + t.v3.y)/2, (t.v1.z + t.v3.z)/2);
            result.add(new Triangle(t.v1, m1, m3, t.color));
            result.add(new Triangle(t.v2, m1, m2, t.color));
            result.add(new Triangle(t.v3, m2, m3, t.color));
            result.add(new Triangle(m1, m2, m3, t.color));
        }

        for (Triangle t : result) {
            for (Vertex v : new Vertex[] { t.v1, t.v2, t.v3 }) {
                double l = Math.sqrt(v.x * v.x + v.y * v.y + v.z * v.z) / Math.sqrt(30000);
                v.x /= l;
                v.y /= l;
                v.z /= l;
            }
        }
        return result;
    }
}




class Vertex { // Vertex class - Allows program to use it's constructor
    double x, y, z;
    Vertex(double x, double y, double z)    {
        this.x = x; // X coord means movement to left-right directions
        this.y = y; // Y coord means movement up or down the screen
        this.z = z; // Z with be perpendicular to the screen - Positive z will be "towards to observer"
    }
}

class Triangle  { // Allows program to create Triangle objects using this class's constructor
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
        ); // Puts all values into a matrix.
    }
}



