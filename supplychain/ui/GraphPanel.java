//package com.supplychain.ui;
//
//import com.supplychain.model.*;
//import com.supplychain.logic.*;
//
//import java.util.List;
//import javax.swing.*;
//import javax.swing.border.*;
//import java.awt.*;
//import java.awt.event.*;
//import java.awt.geom.*;
//import java.awt.RenderingHints;
//import java.util.*;
//
//public class GraphPanel extends JPanel {
//
//    private Graph graph;
//    private Map<Node, Point> nodePositions;
//
//    private Set<Node> affectedNodes   = new HashSet<>();
//    private Set<Node> bottleneckNodes = new HashSet<>();
//    private Set<Node> criticalNodes   = new HashSet<>();
//
//    private Node selectedNode = null;
//    private Node hoveredNode  = null;
//    private Point mousePoint  = null;
//
//    // ── Palette ──────────────────────────────────────────────────────────────
//    private static final Color BG           = new Color(0xF1EFE8);
//    private static final Color EDGE_COLOR   = new Color(0xB4B2A9);
//    private static final Color EDGE_LABEL   = new Color(0x888780);
//    private static final Color NODE_TEXT    = Color.WHITE;
//    private static final Color NODE_BORDER  = new Color(0xFFFFFF, true);
//    private static final Color SUBTEXT      = new Color(0xD3D1C7);
//
//    private static final Color C_SUPPLIER   = new Color(0x1D9E75);
//    private static final Color C_FACTORY    = new Color(0x185FA5);
//    private static final Color C_WAREHOUSE  = new Color(0x534AB7);
//    private static final Color C_RETAILER   = new Color(0xBA7517);
//
//    private static final Color C_AFFECTED   = new Color(0xE24B4A);
//    private static final Color C_BOTTLENECK = new Color(0xEF9F27);
//    private static final Color C_CRITICAL   = new Color(0x2C2C2A);
//
//    private static final Color LEGEND_BG    = new Color(0xFFFFFF);
//    private static final Color LEGEND_BORDER= new Color(0xD3D1C7);
//
//    private static final int   NODE_RADIUS  = 28;
//    private static final Font  FONT_NODE    = new Font("Segoe UI", Font.BOLD,   11);
//    private static final Font  FONT_TYPE    = new Font("Segoe UI", Font.PLAIN,  10);
//    private static final Font  FONT_EDGE    = new Font("Segoe UI", Font.PLAIN,  11);
//    private static final Font  FONT_LEGEND  = new Font("Segoe UI", Font.PLAIN,  12);
//    private static final Font  FONT_LEG_HDR = new Font("Segoe UI", Font.BOLD,   11);
//
//    // ── Constructor ──────────────────────────────────────────────────────────
//    public GraphPanel(Graph graph) {
//        this.graph = graph;
//        this.nodePositions = new HashMap<>();
//        generatePositions();
//        setBackground(BG);
//
//        addMouseListener(new MouseAdapter() {
//
//            @Override
//            public void mousePressed(MouseEvent e) {
//                selectedNode = getClickedNode(e.getPoint());
//            }
//
//            @Override
//            public void mouseReleased(MouseEvent e) {
//                selectedNode = null;
//            }
//
//            @Override
//            public void mouseClicked(MouseEvent e) {
//                Node clickedNode = getClickedNode(e.getPoint());
//
//                if (clickedNode != null) {
//                    showFailureDialog(clickedNode);
//                } else {
//                    handleEdgeClick(e.getPoint());
//                }
//            }
//
//            @Override
//            public void mouseExited(MouseEvent e) {
//                hoveredNode = null;
//                mousePoint  = null;
//                repaint();
//            }
//        });
//
//        addMouseMotionListener(new MouseMotionAdapter() {
//            @Override
//            public void mouseDragged(MouseEvent e) {
//                if (selectedNode != null) {
//                    nodePositions.put(selectedNode, e.getPoint());
//                    repaint();
//                }
//            }
//
//            @Override
//            public void mouseMoved(MouseEvent e) {
//                Node n = getClickedNode(e.getPoint());
//                hoveredNode = n;
//                mousePoint  = e.getPoint();
//                setCursor(n != null
//                        ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
//                        : Cursor.getDefaultCursor());
//                repaint();
//            }
//        });
//    }
//
//    // ── Failure dialog (replaces raw JOptionPane) ─────────────────────────
//    private void showFailureDialog(Node clickedNode) {
//        JDialog dialog = new JDialog(
//                (Frame) SwingUtilities.getWindowAncestor(this),
//                "Simulate Node Failure", true);
//        dialog.setUndecorated(false);
//        dialog.setSize(340, 180);
//        dialog.setLocationRelativeTo(this);
//        dialog.setLayout(new BorderLayout(0, 0));
//
//        JPanel body = new JPanel(new BorderLayout(0, 12));
//        body.setBackground(Color.WHITE);
//        body.setBorder(new EmptyBorder(24, 28, 20, 28));
//
//        JLabel title = new JLabel("Simulate failure of " + clickedNode.getName() + "?");
//        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
//        title.setForeground(new Color(0x2C2C2A));
//
//        JLabel sub = new JLabel("Affected, bottleneck and critical nodes will be highlighted.");
//        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
//        sub.setForeground(new Color(0x888780));
//
//        JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 4));
//        textPanel.setBackground(Color.WHITE);
//        textPanel.add(title);
//        textPanel.add(sub);
//
//        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
//        btnPanel.setBackground(Color.WHITE);
//
//        JButton cancelBtn = styledButton("Cancel", new Color(0xF1EFE8), new Color(0x5F5E5A));
//        JButton simBtn    = styledButton("Simulate failure", new Color(0xE24B4A), Color.WHITE);
//
//        final int[] choice = {JOptionPane.NO_OPTION};
//
//        cancelBtn.addActionListener(ev -> dialog.dispose());
//        simBtn.addActionListener(ev -> {
//            choice[0] = JOptionPane.YES_OPTION;
//            dialog.dispose();
//        });
//
//        btnPanel.add(cancelBtn);
//        btnPanel.add(simBtn);
//
//        body.add(textPanel, BorderLayout.CENTER);
//        body.add(btnPanel,  BorderLayout.SOUTH);
//        dialog.add(body);
//        dialog.setVisible(true);
//
//        if (choice[0] == JOptionPane.YES_OPTION) {
//            DisruptionSimulator simulator = new DisruptionSimulator(graph);
//            BottleneckAnalyzer  analyzer  = new BottleneckAnalyzer(graph);
//
//            List<Node> affected    = simulator.simulateFailure(clickedNode);
//            List<Node> bottlenecks = analyzer.findBottlenecks();
//            List<Node> critical    = analyzer.findSinglePointsOfFailure();
//
//            showSimulation(
//                    new HashSet<>(affected),
//                    new HashSet<>(bottlenecks),
//                    new HashSet<>(critical));
//        }
//    }
//
//    // ── Button helper ─────────────────────────────────────────────────────
//    private JButton styledButton(String text, Color bg, Color fg) {
//        JButton btn = new JButton(text);
//        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
//        btn.setBackground(bg);
//        btn.setForeground(fg);
//        btn.setFocusPainted(false);
//        btn.setBorder(BorderFactory.createCompoundBorder(
//                new LineBorder(bg.darker(), 1, true),
//                new EmptyBorder(7, 18, 7, 18)));
//        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
//        return btn;
//    }
//
//    // ── Positions ─────────────────────────────────────────────────────────
//    private void generatePositions() {
//        int x = 120, y = 120;
//        for (Node node : graph.getAllNodes()) {
//            nodePositions.put(node, new Point(x, y));
//            x += 180;
//            if (x > 700) { x = 120; y += 180; }
//        }
//    }
//
//    // ── Paint ─────────────────────────────────────────────────────────────
//    @Override
//    protected void paintComponent(Graphics g) {
//        super.paintComponent(g);
//        Graphics2D g2 = (Graphics2D) g;
//        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,    RenderingHints.VALUE_ANTIALIAS_ON);
//        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
//
//        drawEdges(g2);
//        drawNodes(g2);
//        drawLegend(g2);
//        drawTooltip(g2);
//    }
//
//    // ── Draw edges ────────────────────────────────────────────────────────
//    private void drawEdges(Graphics2D g2) {
//        g2.setFont(FONT_EDGE);
//        for (Node node : graph.getAllNodes()) {
//            Point p1 = nodePositions.get(node);
//            for (Edge edge : graph.getNeighbors(node)) {
//                Point p2 = nodePositions.get(edge.getDestination());
//                if (p1 == null || p2 == null) continue;
//
//                g2.setColor(EDGE_COLOR);
//                g2.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
//                drawArrow(g2, p1, p2);
//
//                int midX = (p1.x + p2.x) / 2;
//                int midY = (p1.y + p2.y) / 2;
//
//                String label = "C:" + edge.getCost();
//                FontMetrics fm = g2.getFontMetrics();
//                int lw = fm.stringWidth(label);
//
//                g2.setColor(new Color(255, 255, 255, 210));
//                g2.fillRoundRect(midX - lw / 2 - 4, midY - 9, lw + 8, 16, 6, 6);
//
//                g2.setColor(EDGE_LABEL);
//                g2.drawString(label, midX - lw / 2, midY + 3);
//            }
//        }
//        g2.setStroke(new BasicStroke(1f));
//    }
//
//    // ── Draw nodes ────────────────────────────────────────────────────────
//    private void drawNodes(Graphics2D g2) {
//        for (Node node : graph.getAllNodes()) {
//            Point p = nodePositions.get(node);
//            if (p == null) continue;
//
//            Color fill = getColor(node);
//
//            // shadow / glow ring for critical nodes
//            if (criticalNodes.contains(node)) {
//                g2.setColor(new Color(0xEF9F27));
//                g2.setStroke(new BasicStroke(3f));
//                g2.drawOval(p.x - NODE_RADIUS - 4, p.y - NODE_RADIUS - 4,
//                        (NODE_RADIUS + 4) * 2, (NODE_RADIUS + 4) * 2);
//            }
//
//            // filled circle
//            g2.setColor(fill);
//            g2.fillOval(p.x - NODE_RADIUS, p.y - NODE_RADIUS,
//                    NODE_RADIUS * 2, NODE_RADIUS * 2);
//
//            // inner border (white 40% alpha)
//            g2.setColor(new Color(255, 255, 255, 100));
//            g2.setStroke(new BasicStroke(1.5f));
//            g2.drawOval(p.x - NODE_RADIUS, p.y - NODE_RADIUS,
//                    NODE_RADIUS * 2, NODE_RADIUS * 2);
//
//            g2.setStroke(new BasicStroke(1f));
//
//            // node name — split long names
//            g2.setFont(FONT_NODE);
//            g2.setColor(NODE_TEXT);
//            FontMetrics fm = g2.getFontMetrics();
//            String[] words = node.getName().split(" ");
//            if (words.length == 1) {
//                int sw = fm.stringWidth(node.getName());
//                g2.drawString(node.getName(), p.x - sw / 2, p.y + fm.getAscent() / 2 - 1);
//            } else {
//                String line1 = words[0];
//                String line2 = String.join(" ", Arrays.copyOfRange(words, 1, words.length));
//                int sw1 = fm.stringWidth(line1), sw2 = fm.stringWidth(line2);
//                g2.drawString(line1, p.x - sw1 / 2, p.y - 3);
//                g2.drawString(line2, p.x - sw2 / 2, p.y + fm.getHeight() - 3);
//            }
//
//            // type label below node
//            g2.setFont(FONT_TYPE);
//            g2.setColor(new Color(0x5F5E5A));
//            FontMetrics fmT = g2.getFontMetrics();
//            int tw = fmT.stringWidth(node.getType());
//            g2.drawString(node.getType(), p.x - tw / 2, p.y + NODE_RADIUS + 14);
//        }
//    }
//
//    // ── Node color ────────────────────────────────────────────────────────
//    private Color getColor(Node node) {
//
//        // 🔥 Highest priority → FAILURE IMPACT
//        if (affectedNodes.contains(node)) {
//            return C_AFFECTED; // RED
//        }
//
//        // Then structural analysis
//        if (criticalNodes.contains(node)) {
//            return C_CRITICAL; // BLACK
//        }
//
//        if (bottleneckNodes.contains(node)) {
//            return C_BOTTLENECK; // ORANGE
//        }
//
//        return getColor(node.getType());
//    }
//
//    private Color getColor(String type) {
//        switch (type) {
//            case "Supplier":  return C_SUPPLIER;
//            case "Factory":   return C_FACTORY;
//            case "Warehouse": return C_WAREHOUSE;
//            case "Retailer":  return C_RETAILER;
//            default:          return new Color(0x888780);
//        }
//    }
//
//    // ── Arrow ─────────────────────────────────────────────────────────────
//    private void drawArrow(Graphics2D g2, Point p1, Point p2) {
//        int offset = NODE_RADIUS;
//        double angle = Math.atan2(p2.y - p1.y, p2.x - p1.x);
//
//        int x1 = (int) (p1.x + offset * Math.cos(angle));
//        int y1 = (int) (p1.y + offset * Math.sin(angle));
//        int x2 = (int) (p2.x - offset * Math.cos(angle));
//        int y2 = (int) (p2.y - offset * Math.sin(angle));
//
//        g2.drawLine(x1, y1, x2, y2);
//
//        double phi = Math.toRadians(22);
//        int    barb = 12;
//        double theta = Math.atan2(y2 - y1, x2 - x1);
//
//        g2.drawLine(x2, y2,
//                (int)(x2 - barb * Math.cos(theta + phi)),
//                (int)(y2 - barb * Math.sin(theta + phi)));
//        g2.drawLine(x2, y2,
//                (int)(x2 - barb * Math.cos(theta - phi)),
//                (int)(y2 - barb * Math.sin(theta - phi)));
//    }
//
//    // ── Click node ────────────────────────────────────────────────────────
//    private Node getClickedNode(Point p) {
//        for (Node node : nodePositions.keySet()) {
//            if (p.distance(nodePositions.get(node)) <= NODE_RADIUS) return node;
//        }
//        return null;
//    }
//
//    // ── Click edge ────────────────────────────────────────────────────────
//    private void handleEdgeClick(Point click) {
//        for (Node node : graph.getAllNodes()) {
//            Point p1 = nodePositions.get(node);
//            for (Edge edge : graph.getNeighbors(node)) {
//                Point p2 = nodePositions.get(edge.getDestination());
//                if (isPointNearLine(click, p1, p2)) {
//                    showEdgeDialog(edge);
//                    return;
//                }
//            }
//        }
//    }
//
//    private void showEdgeDialog(Edge edge) {
//        JDialog d = new JDialog(
//                (Frame) SwingUtilities.getWindowAncestor(this),
//                "Edge details", true);
//        d.setSize(280, 180);
//        d.setLocationRelativeTo(this);
//
//        JPanel body = new JPanel(new GridLayout(3, 2, 8, 8));
//        body.setBackground(Color.WHITE);
//        body.setBorder(new EmptyBorder(20, 24, 20, 24));
//
//        Font lbl = new Font("Segoe UI", Font.PLAIN, 12);
//        Font val = new Font("Segoe UI", Font.BOLD,  12);
//        Color clbl = new Color(0x888780);
//        Color cval = new Color(0x2C2C2A);
//
//        addRow(body, "Cost",     String.valueOf(edge.getCost()),     lbl, val, clbl, cval);
//        addRow(body, "Time",     String.valueOf(edge.getTime()),     lbl, val, clbl, cval);
//        addRow(body, "Capacity", String.valueOf(edge.getCapacity()), lbl, val, clbl, cval);
//
//        d.add(body);
//        d.setVisible(true);
//    }
//
//    private void addRow(JPanel p, String label, String value,
//                        Font lf, Font vf, Color lc, Color vc) {
//        JLabel l = new JLabel(label); l.setFont(lf); l.setForeground(lc);
//        JLabel v = new JLabel(value); v.setFont(vf); v.setForeground(vc);
//        p.add(l); p.add(v);
//    }
//
//    // ── Line detection ────────────────────────────────────────────────────
//    private boolean isPointNearLine(Point p, Point p1, Point p2) {
//        return Line2D.ptSegDist(p1.x, p1.y, p2.x, p2.y, p.x, p.y) < 10;
//    }
//
//    // ── Hover tooltip (drawn on canvas) ───────────────────────────────────
//    private void drawTooltip(Graphics2D g2) {
//        if (hoveredNode == null || mousePoint == null) return;
//
//        Node n = hoveredNode;
//        String status = "Normal";
//        if (criticalNodes.contains(n))   status = "Critical";
//        else if (bottleneckNodes.contains(n)) status = "Bottleneck";
//        else if (affectedNodes.contains(n))   status = "Affected";
//
//        String[] labels = { "Type", "Capacity", "Health", "Status" };
//        String[] values = { n.getType(), String.valueOf(n.getCapacity()),
//                n.getHealth() + "%", status };
//
//        Font fLabel = new Font("Segoe UI", Font.PLAIN, 11);
//        Font fValue = new Font("Segoe UI", Font.BOLD,  11);
//        Font fTitle = new Font("Segoe UI", Font.BOLD,  12);
//
//        int pad = 12, rowH = 20, titleH = 22;
//        int rows = labels.length;
//        int boxW = 180;
//        int boxH = pad + titleH + rows * rowH + pad;
//
//        int tx = mousePoint.x + 16;
//        int ty = mousePoint.y - boxH / 2;
//        if (tx + boxW > getWidth()  - 8) tx = mousePoint.x - boxW - 16;
//        if (ty < 8)                       ty = 8;
//        if (ty + boxH > getHeight() - 8)  ty = getHeight() - boxH - 8;
//
//        // card background
//        g2.setColor(new Color(255, 255, 255, 245));
//        g2.fillRoundRect(tx, ty, boxW, boxH, 12, 12);
//        g2.setColor(new Color(0xD3D1C7));
//        g2.setStroke(new BasicStroke(0.5f));
//        g2.drawRoundRect(tx, ty, boxW, boxH, 12, 12);
//
//        // colour accent bar on left edge
//        g2.setColor(getColor(n.getType()));
//        g2.fillRoundRect(tx, ty, 4, boxH, 4, 4);
//        g2.fillRect(tx + 2, ty, 2, boxH); // square right side of bar
//
//        int cx = tx + pad + 6;
//        int cy = ty + pad + 13;
//
//        // node name title
//        g2.setFont(fTitle);
//        g2.setColor(new Color(0x2C2C2A));
//        g2.drawString(n.getName(), cx, cy);
//        cy += titleH;
//
//        // rows
//        for (int i = 0; i < labels.length; i++) {
//            g2.setFont(fLabel);
//            g2.setColor(new Color(0x888780));
//            g2.drawString(labels[i], cx, cy);
//
//            g2.setFont(fValue);
//            g2.setColor(new Color(0x2C2C2A));
//            FontMetrics fm = g2.getFontMetrics();
//            int vw = fm.stringWidth(values[i]);
//            g2.drawString(values[i], tx + boxW - pad - vw, cy);
//            cy += rowH;
//        }
//
//        g2.setStroke(new BasicStroke(1f));
//    }
//
//    // ── Legend ────────────────────────────────────────────────────────────
//    private void drawLegend(Graphics2D g2) {
//        int x = 16, y = 16;
//        int pw = 170, ph = 9 * 22 + 28;
//
//        g2.setColor(LEGEND_BG);
//        g2.fillRoundRect(x, y, pw, ph, 12, 12);
//        g2.setColor(LEGEND_BORDER);
//        g2.setStroke(new BasicStroke(0.5f));
//        g2.drawRoundRect(x, y, pw, ph, 12, 12);
//
//        int cx = x + 14, cy = y + 18;
//
//        g2.setFont(FONT_LEG_HDR);
//        g2.setColor(new Color(0x888780));
//        g2.drawString("LEGEND", cx, cy);
//        cy += 18;
//
//        drawLegendItem(g2, "Supplier",       C_SUPPLIER,   cx, cy); cy += 22;
//        drawLegendItem(g2, "Factory",        C_FACTORY,    cx, cy); cy += 22;
//        drawLegendItem(g2, "Warehouse",      C_WAREHOUSE,  cx, cy); cy += 22;
//        drawLegendItem(g2, "Retailer",       C_RETAILER,   cx, cy); cy += 22;
//
//        // divider
//        g2.setColor(LEGEND_BORDER);
//        g2.drawLine(cx, cy - 4, cx + pw - 28, cy - 4);
//        cy += 4;
//
//        drawLegendItem(g2, "Affected node",   C_AFFECTED,   cx, cy); cy += 22;
//        drawLegendItem(g2, "Bottleneck node", C_BOTTLENECK, cx, cy); cy += 22;
//        drawLegendItem(g2, "Critical node",   C_CRITICAL,   cx, cy);
//
//        g2.setStroke(new BasicStroke(1f));
//    }
//
//    private void drawLegendItem(Graphics2D g2, String label, Color color, int x, int y) {
//        g2.setColor(color);
//        g2.fillOval(x, y - 9, 13, 13);
//        g2.setColor(new Color(255, 255, 255, 80));
//        g2.drawOval(x, y - 9, 13, 13);
//        g2.setColor(new Color(0x2C2C2A));
//        g2.setFont(FONT_LEGEND);
//        g2.drawString(label, x + 19, y + 1);
//    }
//
//    // ── Simulation ────────────────────────────────────────────────────────
//    public void showSimulation(Set<Node> affected,
//                               Set<Node> bottlenecks,
//                               Set<Node> critical) {
//        this.affectedNodes   = affected;
//        this.bottleneckNodes = bottlenecks;
//        this.criticalNodes   = critical;
//        repaint();
//    }
//}
//



package com.supplychain.ui;

import com.supplychain.analytics.AnalyticsEngine;
import com.supplychain.model.*;
import com.supplychain.logic.*;

import java.util.List;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.RenderingHints;
import java.util.*;

public class GraphPanel extends JPanel {

    private Graph graph;
    private Map<Node, Point> nodePositions;

    private Set<Node> affectedNodes   = new HashSet<>();
    private Set<Node> bottleneckNodes = new HashSet<>();
    private Set<Node> criticalNodes   = new HashSet<>();

    private Node selectedNode = null;
    private Node hoveredNode  = null;
    private Point mousePoint  = null;

    // ── Column order ──────────────────────────────────────────────────────
    private static final List<String> COLUMN_ORDER =
            Arrays.asList("Supplier", "Factory", "Warehouse", "Retailer");

    // ── Column x positions (will be computed dynamically) ─────────────────
    private final Map<String, Integer> columnX = new LinkedHashMap<>();

    // ── Palette ───────────────────────────────────────────────────────────
    private static final Color BG            = new Color(0xF1EFE8);
    private static final Color EDGE_COLOR    = new Color(0xB4B2A9);
    private static final Color EDGE_LABEL    = new Color(0x888780);
    private static final Color NODE_TEXT     = Color.WHITE;
    private static final Color COL_HEADER_BG = new Color(0xFFFFFF, true);

    private static final Color C_SUPPLIER    = new Color(0x1D9E75);
    private static final Color C_FACTORY     = new Color(0x185FA5);
    private static final Color C_WAREHOUSE   = new Color(0x534AB7);
    private static final Color C_RETAILER    = new Color(0xBA7517);

    private static final Color C_AFFECTED    = new Color(0xE24B4A);
    private static final Color C_BOTTLENECK  = new Color(0xEF9F27);
    private static final Color C_CRITICAL    = new Color(0x2C2C2A);

    private static final Color LEGEND_BG     = new Color(0xFFFFFF);
    private static final Color LEGEND_BORDER = new Color(0xD3D1C7);

    private static final int  NODE_RADIUS    = 28;
    private static final int  COL_TOP_PAD    = 60;   // space for column header
    private static final int  NODE_V_GAP     = 100;  // vertical gap between nodes
    private static final int  COL_H_GAP      = 210;  // horizontal gap between columns
    private static final int  LEFT_PAD       = 100;  // left margin

    private static final Font FONT_NODE      = new Font("Segoe UI", Font.BOLD,   11);
    private static final Font FONT_TYPE      = new Font("Segoe UI", Font.PLAIN,  10);
    private static final Font FONT_EDGE      = new Font("Segoe UI", Font.PLAIN,  11);
    private static final Font FONT_LEGEND    = new Font("Segoe UI", Font.PLAIN,  12);
    private static final Font FONT_LEG_HDR   = new Font("Segoe UI", Font.BOLD,   11);
    private static final Font FONT_COL_HDR   = new Font("Segoe UI", Font.BOLD,   13);

    // ── Constructor ───────────────────────────────────────────────────────
    public GraphPanel(Graph graph) {
        this.graph = graph;
        this.nodePositions = new LinkedHashMap<>();
        generateColumnPositions();
        setBackground(BG);

        addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                selectedNode = getClickedNode(e.getPoint());
            }
            @Override public void mouseReleased(MouseEvent e) {
                selectedNode = null;
            }
            @Override public void mouseClicked(MouseEvent e) {
                Node cn = getClickedNode(e.getPoint());
                if (cn != null) showFailureDialog(cn);
                else            handleEdgeClick(e.getPoint());
            }
            @Override public void mouseExited(MouseEvent e) {
                hoveredNode = null; mousePoint = null; repaint();
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseDragged(MouseEvent e) {
                if (selectedNode != null) {
                    nodePositions.put(selectedNode, e.getPoint());
                    repaint();
                }
            }
            @Override public void mouseMoved(MouseEvent e) {
                Node n = getClickedNode(e.getPoint());
                hoveredNode = n;
                mousePoint  = e.getPoint();
                setCursor(n != null
                        ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                        : Cursor.getDefaultCursor());
                repaint();
            }
        });
    }

    // ── Column-based position generator ───────────────────────────────────
    private void generateColumnPositions() {
        // Group nodes by type
        Map<String, List<Node>> byType = new LinkedHashMap<>();
        for (String t : COLUMN_ORDER) byType.put(t, new ArrayList<>());

        for (Node node : graph.getAllNodes()) {
            String t = node.getType();
            byType.computeIfAbsent(t, k -> new ArrayList<>()).add(node);
        }

        // Assign column x values
        int col = 0;
        for (String type : COLUMN_ORDER) {
            if (!byType.getOrDefault(type, Collections.emptyList()).isEmpty()) {
                columnX.put(type, LEFT_PAD + col * COL_H_GAP);
                col++;
            }
        }

        // Place nodes within their column, centred vertically
        for (String type : COLUMN_ORDER) {
            List<Node> nodes = byType.getOrDefault(type, Collections.emptyList());
            if (nodes.isEmpty()) continue;
            int x = columnX.get(type);
            int startY = COL_TOP_PAD + 50;
            for (int i = 0; i < nodes.size(); i++) {
                nodePositions.put(nodes.get(i), new Point(x, startY + i * NODE_V_GAP));
            }
        }
    }

    // ── Paint ─────────────────────────────────────────────────────────────
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,     RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        drawColumnHeaders(g2);
        drawColumnDividers(g2);
        drawEdges(g2);
        drawNodes(g2);
        drawLegend(g2);
        drawTooltip(g2);
    }

    // ── Column headers ────────────────────────────────────────────────────
    private void drawColumnHeaders(Graphics2D g2) {
        Color[] headerColors = {C_SUPPLIER, C_FACTORY, C_WAREHOUSE, C_RETAILER};
        int idx = 0;
        for (String type : COLUMN_ORDER) {
            if (!columnX.containsKey(type)) { idx++; continue; }
            int x = columnX.get(type);
            Color accent = headerColors[idx % headerColors.length];

            // pill background
            g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 22));
            g2.fillRoundRect(x - 50, 10, 100, 32, 20, 20);
            g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 80));
            g2.setStroke(new BasicStroke(0.8f));
            g2.drawRoundRect(x - 50, 10, 100, 32, 20, 20);

            // label
            g2.setFont(FONT_COL_HDR);
            g2.setColor(accent);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(type, x - fm.stringWidth(type) / 2, 32);
            idx++;
        }
        g2.setStroke(new BasicStroke(1f));
    }

    // ── Subtle vertical dividers between columns ───────────────────────────
    private void drawColumnDividers(Graphics2D g2) {
        List<String> present = new ArrayList<>();
        for (String t : COLUMN_ORDER) { if (columnX.containsKey(t)) present.add(t); }

        g2.setColor(new Color(0xD3D1C7));
        g2.setStroke(new BasicStroke(0.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                1f, new float[]{6, 6}, 0));

        for (int i = 0; i < present.size() - 1; i++) {
            int x1 = columnX.get(present.get(i));
            int x2 = columnX.get(present.get(i + 1));
            int midX = (x1 + x2) / 2;
            g2.drawLine(midX, 50, midX, getHeight() - 20);
        }
        g2.setStroke(new BasicStroke(1f));
    }

    // ── Draw edges ────────────────────────────────────────────────────────
    private void drawEdges(Graphics2D g2) {
        g2.setFont(FONT_EDGE);
        for (Node node : graph.getAllNodes()) {
            Point p1 = nodePositions.get(node);
            for (Edge edge : graph.getNeighbors(node)) {
                Point p2 = nodePositions.get(edge.getDestination());
                if (p1 == null || p2 == null) continue;

                g2.setColor(EDGE_COLOR);
                g2.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                drawArrow(g2, p1, p2);

                int midX = (p1.x + p2.x) / 2;
                int midY = (p1.y + p2.y) / 2;
                String label = "C:" + edge.getCost();
                FontMetrics fm = g2.getFontMetrics();
                int lw = fm.stringWidth(label);

                g2.setColor(new Color(255, 255, 255, 210));
                g2.fillRoundRect(midX - lw / 2 - 4, midY - 9, lw + 8, 16, 6, 6);
                g2.setColor(EDGE_LABEL);
                g2.drawString(label, midX - lw / 2, midY + 3);
            }
        }
        g2.setStroke(new BasicStroke(1f));
    }

    // ── Draw nodes ────────────────────────────────────────────────────────
    private void drawNodes(Graphics2D g2) {
        for (Node node : graph.getAllNodes()) {
            Point p = nodePositions.get(node);
            if (p == null) continue;

            Color fill = getNodeColor(node);

            // glow ring for critical
            if (criticalNodes.contains(node)) {
                g2.setColor(new Color(0xEF9F27));
                g2.setStroke(new BasicStroke(3f));
                g2.drawOval(p.x - NODE_RADIUS - 4, p.y - NODE_RADIUS - 4,
                        (NODE_RADIUS + 4) * 2, (NODE_RADIUS + 4) * 2);
            }

            // drop shadow
            g2.setColor(new Color(0, 0, 0, 18));
            g2.fillOval(p.x - NODE_RADIUS + 3, p.y - NODE_RADIUS + 4,
                    NODE_RADIUS * 2, NODE_RADIUS * 2);

            // filled circle
            g2.setColor(fill);
            g2.fillOval(p.x - NODE_RADIUS, p.y - NODE_RADIUS,
                    NODE_RADIUS * 2, NODE_RADIUS * 2);

            // inner white ring
            g2.setColor(new Color(255, 255, 255, 90));
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawOval(p.x - NODE_RADIUS, p.y - NODE_RADIUS,
                    NODE_RADIUS * 2, NODE_RADIUS * 2);
            g2.setStroke(new BasicStroke(1f));

            // node name (split if long)
            g2.setFont(FONT_NODE);
            g2.setColor(NODE_TEXT);
            FontMetrics fm = g2.getFontMetrics();
            String[] words = node.getName().split(" ");
            if (words.length == 1) {
                int sw = fm.stringWidth(node.getName());
                g2.drawString(node.getName(), p.x - sw / 2, p.y + fm.getAscent() / 2 - 1);
            } else {
                String line1 = words[0];
                String line2 = String.join(" ", Arrays.copyOfRange(words, 1, words.length));
                g2.drawString(line1, p.x - fm.stringWidth(line1) / 2, p.y - 3);
                g2.drawString(line2, p.x - fm.stringWidth(line2) / 2, p.y + fm.getHeight() - 3);
            }
        }
    }

    // ── Node color logic ──────────────────────────────────────────────────
    private Color getNodeColor(Node node) {
        if (affectedNodes.contains(node))   return C_AFFECTED;
        if (criticalNodes.contains(node))   return C_CRITICAL;
        if (bottleneckNodes.contains(node)) return C_BOTTLENECK;
        return getTypeColor(node.getType());
    }

    private Color getTypeColor(String type) {
        switch (type) {
            case "Supplier":  return C_SUPPLIER;
            case "Factory":   return C_FACTORY;
            case "Warehouse": return C_WAREHOUSE;
            case "Retailer":  return C_RETAILER;
            default:          return new Color(0x888780);
        }
    }

    // ── Arrow ─────────────────────────────────────────────────────────────
    private void drawArrow(Graphics2D g2, Point p1, Point p2) {
        int offset = NODE_RADIUS;
        double angle = Math.atan2(p2.y - p1.y, p2.x - p1.x);

        int x1 = (int)(p1.x + offset * Math.cos(angle));
        int y1 = (int)(p1.y + offset * Math.sin(angle));
        int x2 = (int)(p2.x - offset * Math.cos(angle));
        int y2 = (int)(p2.y - offset * Math.sin(angle));

        g2.drawLine(x1, y1, x2, y2);

        double phi = Math.toRadians(22);
        int barb = 12;
        double theta = Math.atan2(y2 - y1, x2 - x1);
        g2.drawLine(x2, y2, (int)(x2 - barb * Math.cos(theta + phi)), (int)(y2 - barb * Math.sin(theta + phi)));
        g2.drawLine(x2, y2, (int)(x2 - barb * Math.cos(theta - phi)), (int)(y2 - barb * Math.sin(theta - phi)));
    }

    // ── Click node ────────────────────────────────────────────────────────
    private Node getClickedNode(Point p) {
        for (Node node : nodePositions.keySet()) {
            if (p.distance(nodePositions.get(node)) <= NODE_RADIUS) return node;
        }
        return null;
    }

    // ── Click edge ────────────────────────────────────────────────────────
    private void handleEdgeClick(Point click) {
        for (Node node : graph.getAllNodes()) {
            Point p1 = nodePositions.get(node);
            for (Edge edge : graph.getNeighbors(node)) {
                Point p2 = nodePositions.get(edge.getDestination());
                if (p1 != null && p2 != null && isPointNearLine(click, p1, p2)) {
                    showEdgeDialog(edge);
                    return;
                }
            }
        }
    }

    private boolean isPointNearLine(Point p, Point p1, Point p2) {
        return Line2D.ptSegDist(p1.x, p1.y, p2.x, p2.y, p.x, p.y) < 10;
    }

    // ── Edge info dialog ──────────────────────────────────────────────────
    private void showEdgeDialog(Edge edge) {
        JDialog d = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Edge Details", true);
        d.setUndecorated(true);
        d.setSize(300, 200);
        d.setLocationRelativeTo(this);

        JPanel body = new JPanel(new BorderLayout(0, 0)) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(C_FACTORY);
                g2.fillRoundRect(0, 0, getWidth(), 5, 4, 4);
                g2.setColor(new Color(0xD3D1C7));
                g2.setStroke(new BasicStroke(0.5f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 16, 16);
            }
        };
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(20, 24, 18, 24));

        JLabel title = new JLabel("Edge Details");
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setForeground(new Color(0x1E293B));
        body.add(title, BorderLayout.NORTH);

        JPanel rows = new JPanel(new GridLayout(3, 2, 8, 10));
        rows.setOpaque(false);
        rows.setBorder(new EmptyBorder(14, 0, 14, 0));
        addRow(rows, "Cost",     String.valueOf(edge.getCost()));
        addRow(rows, "Time",     String.valueOf(edge.getTime()));
        addRow(rows, "Capacity", String.valueOf(edge.getCapacity()));
        body.add(rows, BorderLayout.CENTER);

        JButton close = dialogBtn("Close", new Color(0xF1EFE8), new Color(0x5F5E5A));
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnRow.setOpaque(false);
        btnRow.add(close);
        close.addActionListener(e -> d.dispose());
        body.add(btnRow, BorderLayout.SOUTH);

        d.setContentPane(body);
        d.setVisible(true);
    }

    private void addRow(JPanel p, String label, String value) {
        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        l.setForeground(new Color(0x888780));
        JLabel v = new JLabel(value);
        v.setFont(new Font("Segoe UI", Font.BOLD, 12));
        v.setForeground(new Color(0x1E293B));
        p.add(l); p.add(v);
    }

    // ── Failure dialog ────────────────────────────────────────────────────
    private void showFailureDialog(Node clickedNode) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Simulate Node Failure", true);
        dialog.setUndecorated(true);
        dialog.setSize(380, 200);
        dialog.setLocationRelativeTo(this);

        JPanel body = new JPanel(new BorderLayout(0, 14)) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(C_AFFECTED);
                g2.fillRoundRect(0, 0, getWidth(), 5, 4, 4);
                g2.setColor(new Color(0xD3D1C7));
                g2.setStroke(new BasicStroke(0.5f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 16, 16);
            }
        };
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(22, 26, 20, 26));

        JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 4));
        textPanel.setOpaque(false);
        JLabel title = new JLabel("Simulate failure of " + clickedNode.getName() + "?");
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setForeground(new Color(0x1E293B));
        JLabel sub = new JLabel("Affected, bottleneck and critical nodes will be highlighted.");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sub.setForeground(new Color(0x888780));
        textPanel.add(title); textPanel.add(sub);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setOpaque(false);
        JButton cancelBtn = dialogBtn("Cancel",           new Color(0xF1EFE8), new Color(0x5F5E5A));
        JButton simBtn    = dialogBtn("Simulate failure", C_AFFECTED,          Color.WHITE);
        final int[] choice = {JOptionPane.NO_OPTION};
        cancelBtn.addActionListener(ev -> dialog.dispose());
        simBtn.addActionListener(ev -> { choice[0] = JOptionPane.YES_OPTION; dialog.dispose(); });
        btnPanel.add(cancelBtn); btnPanel.add(simBtn);

        body.add(textPanel, BorderLayout.CENTER);
        body.add(btnPanel,  BorderLayout.SOUTH);
        dialog.setContentPane(body);
        dialog.setVisible(true);

        if (choice[0] == JOptionPane.YES_OPTION) {

            DisruptionSimulator simulator = new DisruptionSimulator(graph);
            BottleneckAnalyzer analyzer = new BottleneckAnalyzer(graph);

            // STEP 1: failure propagation
            List<Node> affected = simulator.simulateFailure(clickedNode);
            AnalyticsEngine analytics = new AnalyticsEngine(graph);

// 🔥 TRIGGER LOSS CALCULATION HERE
            double loss = analytics.calculateLossPerHour(new HashSet<>(affected));

            System.out.println("Loss per hour: " + loss);

            // STEP 2: load impact (🔥 ADD THIS)
            Map<Node, String> impact = simulator.calculateImpact(clickedNode);

            // STEP 3: bottlenecks
            List<Node> bottlenecks = new ArrayList<>(analyzer.findBottlenecks());

            // 🔥 ADD: mark nodes with load increase as bottlenecks
            for(Node n : impact.keySet()){
                if(impact.get(n).contains("LOAD")){
                    bottlenecks.add(n);
                }
            }

            // STEP 4: critical nodes
            List<Node> critical = analyzer.findSinglePointsOfFailure();

            // STEP 5: show simulation
            showSimulation(
                    new HashSet<>(affected),
                    new HashSet<>(bottlenecks),
                    new HashSet<>(critical)
            );
        }
    }

    private JButton dialogBtn(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? bg.darker() : bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(fg);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 18, 8, 18));
        return btn;
    }

    // ── Hover tooltip ─────────────────────────────────────────────────────
    private void drawTooltip(Graphics2D g2) {
        if (hoveredNode == null || mousePoint == null) return;

        Node n = hoveredNode;
        String status = "Normal";
        if (criticalNodes.contains(n))   status = "Critical";
        else if (bottleneckNodes.contains(n)) status = "Bottleneck";
        else if (affectedNodes.contains(n))   status = "Affected";

        String[] labels = {"Type","Capacity","Health","Status"};
        String[] values = {n.getType(), String.valueOf(n.getCapacity()), n.getHealth()+"%", status};

        int pad=12, rowH=20, titleH=22;
        int boxW=185, boxH=pad+titleH+labels.length*rowH+pad;

        int tx = mousePoint.x + 18;
        int ty = mousePoint.y - boxH/2;
        if (tx+boxW > getWidth()-8)  tx = mousePoint.x - boxW - 18;
        if (ty < 8)                   ty = 8;
        if (ty+boxH > getHeight()-8)  ty = getHeight()-boxH-8;

        // shadow
        g2.setColor(new Color(0,0,0,18));
        g2.fillRoundRect(tx+3, ty+4, boxW, boxH, 12, 12);

        // card
        g2.setColor(new Color(255,255,255,248));
        g2.fillRoundRect(tx, ty, boxW, boxH, 12, 12);
        g2.setColor(new Color(0xD3D1C7));
        g2.setStroke(new BasicStroke(0.5f));
        g2.drawRoundRect(tx, ty, boxW, boxH, 12, 12);

        // accent bar
        Color accent = getTypeColor(n.getType());
        g2.setColor(accent);
        g2.fillRoundRect(tx, ty, 4, boxH, 4, 4);
        g2.fillRect(tx+2, ty, 2, boxH);

        int cx=tx+pad+6, cy=ty+pad+13;

        g2.setFont(new Font("Segoe UI",Font.BOLD,12));
        g2.setColor(new Color(0x1E293B));
        g2.drawString(n.getName(), cx, cy);
        cy+=titleH;

        for (int i=0; i<labels.length; i++) {
            g2.setFont(new Font("Segoe UI",Font.PLAIN,11));
            g2.setColor(new Color(0x888780));
            g2.drawString(labels[i], cx, cy);
            g2.setFont(new Font("Segoe UI",Font.BOLD,11));
            g2.setColor(new Color(0x1E293B));
            FontMetrics fm=g2.getFontMetrics();
            g2.drawString(values[i], tx+boxW-pad-fm.stringWidth(values[i]), cy);
            cy+=rowH;
        }
        g2.setStroke(new BasicStroke(1f));
    }

    // ── Legend ────────────────────────────────────────────────────────────
    private void drawLegend(Graphics2D g2) {
        // Place legend AFTER the last column (to the right)
        int lastColX = LEFT_PAD;
        for (int x : columnX.values()) lastColX = Math.max(lastColX, x);
        int legendX = lastColX + 80;
        int legendY = 10;

        int pw = 175, ph = 9 * 22 + 28;

        // shadow
        g2.setColor(new Color(0,0,0,12));
        g2.fillRoundRect(legendX+3, legendY+3, pw, ph, 12, 12);

        g2.setColor(LEGEND_BG);
        g2.fillRoundRect(legendX, legendY, pw, ph, 12, 12);
        g2.setColor(LEGEND_BORDER);
        g2.setStroke(new BasicStroke(0.5f));
        g2.drawRoundRect(legendX, legendY, pw, ph, 12, 12);

        int cx=legendX+14, cy=legendY+18;

        g2.setFont(FONT_LEG_HDR);
        g2.setColor(new Color(0x888780));
        g2.drawString("LEGEND", cx, cy);
        cy+=18;

        drawLegendItem(g2, "Supplier",       C_SUPPLIER,   cx, cy); cy+=22;
        drawLegendItem(g2, "Factory",        C_FACTORY,    cx, cy); cy+=22;
        drawLegendItem(g2, "Warehouse",      C_WAREHOUSE,  cx, cy); cy+=22;
        drawLegendItem(g2, "Retailer",       C_RETAILER,   cx, cy); cy+=22;

        // divider
        g2.setColor(LEGEND_BORDER);
        g2.drawLine(cx, cy-4, cx+pw-28, cy-4);
        cy+=4;

        drawLegendItem(g2, "Affected",   C_AFFECTED,   cx, cy); cy+=22;
        drawLegendItem(g2, "Bottleneck", C_BOTTLENECK, cx, cy); cy+=22;
        drawLegendItem(g2, "Critical",   C_CRITICAL,   cx, cy);

        g2.setStroke(new BasicStroke(1f));
    }

    private void drawLegendItem(Graphics2D g2, String label, Color color, int x, int y) {
        g2.setColor(color);
        g2.fillOval(x, y-9, 13, 13);
        g2.setColor(new Color(255,255,255,80));
        g2.drawOval(x, y-9, 13, 13);
        g2.setColor(new Color(0x2C2C2A));
        g2.setFont(FONT_LEGEND);
        g2.drawString(label, x+19, y+1);
    }

    // ── Simulation ────────────────────────────────────────────────────────
    public void showSimulation(Set<Node> affected,
                               Set<Node> bottlenecks,
                               Set<Node> critical) {
        this.affectedNodes   = affected;
        this.bottleneckNodes = bottlenecks;
        this.criticalNodes   = critical;
        repaint();
    }
    public Map<Node, String> calculateImpact(Node failedNode) {

        Map<Node, String> impact = new HashMap<>();

        // Step 1: find nodes that were sending to failed node (incoming edges)
        for(Node node : graph.getAllNodes()) {

            for(Edge edge : graph.getNeighbors(node)) {

                if(edge.getDestination().equals(failedNode)) {

                    int lostCapacity = edge.getCapacity();

                    // Step 2: try to find alternative path
                    Node alt = findAlternative(node, failedNode);

                    if(alt != null) {

                        impact.put(alt,
                                "LOAD INCREASE +" + lostCapacity);

                        impact.put(node,
                                "REDIRECTED to " + alt.getName());

                    } else {

                        impact.put(node,
                                "NO ROUTE → LOSS");

                    }
                }
            }
        }

        return impact;
    }

    private Node findAlternative(Node source, Node failed) {

        for(Edge edge : graph.getNeighbors(source)) {

            Node next = edge.getDestination();

            if(!next.equals(failed)) {
                return next;
            }
        }

        return null;
    }
}