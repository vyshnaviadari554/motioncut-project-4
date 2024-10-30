import java.awt.*;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.event.*;
import java.util.ArrayList;

 class Brick {
    private int x, y;
    private int width = 50, height = 20;
    private boolean isDestroyed = false;

    public Brick(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    public void draw(Graphics g) {
        if (!isDestroyed) {
            g.setColor(Color.GREEN);
            g.fillRect(x, y, width, height);
        }
    }
}




 class Ball {
    int x, y;
    int speedX, speedY;
    int diameter = 20;

    public Ball(int x, int y, int speedX, int speedY) {
        this.x = x;
        this.y = y;
        this.speedX = speedX;
        this.speedY = speedY;
    }

    public void update() {
        x += speedX;
        y += speedY;
        if (x < 0 || x > 600 - diameter) speedX = -speedX;
        if (y < 0) speedY = -speedY;
    }

    public void reverseY() {
        speedY = -speedY;
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, diameter, diameter);
    }

    public void draw(Graphics g) {
        g.setColor(Color.RED);
        g.fillOval(x, y, diameter, diameter);
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void setSpeed(int speedX, int speedY) {
        this.speedX = speedX;
        this.speedY = speedY;
    }
}



 class Paddle {
    private int x, y;
    private int width = 100, height = 15;
    private int direction = 0;
    private int speed = 5;

    public Paddle(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    public void update() {
        x += direction * speed;
        if (x < 0) x = 0;
        if (x > 600 - width) x = 600 - width;
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    public void draw(Graphics g) {
        g.setColor(Color.BLUE);
        g.fillRect(x, y, width, height);
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }
}





class GamePanel extends JPanel implements Runnable, KeyListener {
    private Thread gameThread;
    private boolean isRunning;
    private boolean gameWon = false;
    private Paddle paddle;
    private Ball ball;
    private ArrayList<Brick> bricks;
    private int score = 0;
    private int lives = 3;
    private int level = 1;

    public GamePanel() {
        setPreferredSize(new Dimension(600, 500));
        setFocusable(true);
        addKeyListener(this);
        initializeGame();
    }

    private void initializeGame() {
        paddle = new Paddle(250, 450);
        ball = new Ball(290, 430, -2, -3);  // Initial speed
        bricks = new ArrayList<>();
        loadLevel(level);
    }

    private void loadLevel(int level) {
        bricks.clear();  // Clear previous level's bricks
        int brickWidth = 50;
        int brickHeight = 20;
        int rows = 6 + level;  // Increase rows with level
        int cols = 8;  // Keep columns consistent

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                bricks.add(new Brick(j * (brickWidth + 5) + 30, i * (brickHeight + 5) + 30));
            }
        }

        // Increase ball speed slightly each level
        int speedX = -2 - level;
        int speedY = -3 - level;
        ball.setSpeed(speedX, speedY);
    }

    public void startGame() {
        isRunning = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {
        while (isRunning) {
            update();
            repaint();
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void update() {
        if (gameWon || lives <= 0) {
            isRunning = false;
            return;
        }

        paddle.update();
        ball.update();
        checkCollisions();

        // Check if all bricks are destroyed to proceed to the next level
        if (bricks.isEmpty()) {
            level++;
            loadLevel(level);
            resetBallAndPaddle();
            gameWon = false;  // Reset for the new level
        }
    }

    private void checkCollisions() {
        if (ball.getBounds().intersects(paddle.getBounds())) {
            ball.reverseY();
        }

        for (int i = 0; i < bricks.size(); i++) {
            Brick brick = bricks.get(i);
            if (ball.getBounds().intersects(brick.getBounds())) {
                bricks.remove(brick);
                ball.reverseY();
                score += 10;
                break;
            }
        }

        if (ball.y > getHeight()) {
            lives--;
            resetBallAndPaddle();
            if (lives <= 0) {
                isRunning = false;
            }
        }
    }

    private void resetBallAndPaddle() {
        paddle.setPosition(250, 450);
        ball.setPosition(290, 430);
        int speedX = -2 - level;
        int speedY = -3 - level;
        ball.setSpeed(speedX, speedY);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        paddle.draw(g);
        ball.draw(g);
        for (Brick brick : bricks) {
            brick.draw(g);
        }

        g.drawString("Score: " + score, 10, 10);
        g.drawString("Lives: " + lives, 10, 30);
        g.drawString("Level: " + level, 10, 50);

        // Display game over or winning message
        if (!isRunning) {
            g.setColor(Color.BLACK);
            g.setFont(new Font("Arial", Font.BOLD, 30));
            String message = gameWon ? "You Win! Final Score: " + score : "Game Over! Final Score: " + score;
            g.drawString(message, getWidth() / 2 - 150, getHeight() / 2);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            paddle.setDirection(-1);
        } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            paddle.setDirection(1);
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        paddle.setDirection(0);
    }

    @Override
    public void keyTyped(KeyEvent e) { }
}

public class MainGame {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Brick Buster");
        GamePanel gamePanel = new GamePanel();
        
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.add(gamePanel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        gamePanel.startGame();
    }
}
