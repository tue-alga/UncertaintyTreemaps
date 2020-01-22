package com.eduardovernier;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;

public class TreemapPanel extends JPanel implements ActionListener, KeyListener, MouseMotionListener, MouseListener {

    private float progress = 0.0f;
    private TreemapManager treemapManager;
    private Point mousePosition;
    private Container hoveredContainer;

    public TreemapPanel(TreemapManager treemapManager) {
        this.treemapManager = treemapManager;
        // Timer used for screen refreshing -- 30ms
        Timer timer = new Timer(30, this);
        timer.start();
        addMouseMotionListener(this);
        addMouseListener(this);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);

        Graphics2D g = (Graphics2D) graphics;

        paintTreemapEntities(treemapManager.getRootPackage(), g);
        paintTreemapBorders(treemapManager.getRootPackage(), g, 0);
        paintHoveredItem(g);

        // Improves graphics on Linux
        Toolkit.getDefaultToolkit().sync();
    }

    private void paintHoveredItem(Graphics2D graphics) {

        if (this.hoveredContainer != null) {
            double x = (1.0 - progress) * this.hoveredContainer.oldRectangle.x + progress * this.hoveredContainer.rectangle.x;
            double y = (1.0 - progress) * this.hoveredContainer.oldRectangle.y + progress * this.hoveredContainer.rectangle.y;
            double width = (1.0 - progress) * this.hoveredContainer.oldRectangle.width + progress * this.hoveredContainer.rectangle.width;
            double height = (1.0 - progress) * this.hoveredContainer.oldRectangle.height + progress * this.hoveredContainer.rectangle.height;

            Rectangle2D.Double rectangle = new Rectangle2D.Double(x, y, width, height);
            graphics.setColor(Color.RED);
            graphics.setStroke(new BasicStroke(1, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER));
            graphics.draw(rectangle);

            graphics.setColor(Color.BLACK);
            graphics.drawString(this.hoveredContainer.id, (float) mousePosition.getX(), (float) mousePosition.getY());
        }
    }

    private void paintTreemapEntities(Package pack, Graphics2D g) {

        if (pack.treemap.root != null) {
            paintTreemapItems(pack.treemap.root, g);
        }

        for (Package childPackage : pack.packageList) {
            paintTreemapEntities(childPackage, g);
        }
    }

    private void paintTreemapItems(Container container, Graphics2D graphics) {
        // For the animation
        if (container.oldRectangle == null) {
            container.oldRectangle = new Rectangle(container.rectangle.x + container.rectangle.width / 2,
                    container.rectangle.y + container.rectangle.height / 2, 1, 1);
        }

        if (container.getCentralWeight() > 0 && !(container.id.endsWith("_") || container.id.equals(""))) {
            double x = (1.0 - progress) * container.oldRectangle.x + progress * container.rectangle.x;
            double y = (1.0 - progress) * container.oldRectangle.y + progress * container.rectangle.y;
            double width = (1.0 - progress) * container.oldRectangle.width + progress * container.rectangle.width;
            double height = (1.0 - progress) * container.oldRectangle.height + progress * container.rectangle.height;

            Rectangle2D.Double rectangle = new Rectangle2D.Double(x, y, width, height);
            if (container.kind == Kind.PARENT) {
                graphics.setColor(new Color(200, 200, 200, 255));
            } else if (container.kind == Kind.LEAF) {
                if (this.mousePosition != null && rectangle.contains(this.mousePosition)) {
                    this.hoveredContainer = container;
                }
                graphics.setColor(Color.WHITE);
            }
            graphics.fill(rectangle);

            graphics.setColor(new Color(200, 200, 200, 255));
            graphics.setStroke(new BasicStroke(1, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER));
            graphics.draw(rectangle);
        }


        if (container.central != null) {
            paintTreemapItems(container.central, graphics);
        }

        if (container.right != null) {
            paintTreemapItems(container.right, graphics);
        }

        if (container.bottom != null) {
            paintTreemapItems(container.bottom, graphics);
        }
    }

    private void paintTreemapBorders(Package pack, Graphics2D graphics, int level) {

        if (pack.treemap.oldCanvas == null) {
            pack.treemap.oldCanvas = new Rectangle(pack.treemap.canvas.x + pack.treemap.canvas.width / 2,
                    pack.treemap.canvas.y + pack.treemap.canvas.height / 2, 1, 1);
        }

        if (pack.getWeight() > 0 && pack.treemap.canvas != null) {
            graphics.setColor(new Color(0, 0, 0, 255));
            level = (level > 3) ? 3 : level;
            graphics.setStroke(new BasicStroke(4 - level, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER));
            double x = (1.0 - progress) * pack.treemap.oldCanvas.x + progress * pack.treemap.canvas.x;
            double y = (1.0 - progress) * pack.treemap.oldCanvas.y + progress * pack.treemap.canvas.y;
            double width = (1.0 - progress) * pack.treemap.oldCanvas.width + progress * pack.treemap.canvas.width;
            double height = (1.0 - progress) * pack.treemap.oldCanvas.height + progress * pack.treemap.canvas.height;
            graphics.draw(new Rectangle2D.Double(x, y, width, height));
        }

        for (Package childPackage : pack.packageList) {
            paintTreemapBorders(childPackage, graphics, level + 1);
        }
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        if (progress < 0.98) {
            progress += 0.02;
            repaint();
        }
    }

    @Override
    public void keyTyped(KeyEvent keyEvent) {
    }

    @Override
    public void keyPressed(KeyEvent keyEvent) {
    }

    @Override
    public void keyReleased(KeyEvent keyEvent) {
        if (keyEvent.getKeyCode() == KeyEvent.VK_X) {
            treemapManager.advanceOneRevision();
            progress = 0;
        } else if (keyEvent.getKeyCode() == KeyEvent.VK_Z) {
            treemapManager.returnOneRevision();
            progress = 0;
        }
        Main.frame.setTitle("Insertion Treemap - Revision " + treemapManager.getRevision());
    }

    @Override
    public void mouseDragged(MouseEvent mouseEvent) {

    }

    @Override
    public void mouseMoved(MouseEvent mouseEvent) {
        mousePosition = mouseEvent.getPoint();
        repaint();
    }

    @Override
    public void mouseClicked(MouseEvent mouseEvent) {

    }

    @Override
    public void mousePressed(MouseEvent mouseEvent) {

    }

    @Override
    public void mouseReleased(MouseEvent mouseEvent) {

    }

    @Override
    public void mouseEntered(MouseEvent mouseEvent) {

    }

    @Override
    public void mouseExited(MouseEvent mouseEvent) {
        this.hoveredContainer = null;
        this.mousePosition = new Point(-1, -1);
        repaint();
    }
}
