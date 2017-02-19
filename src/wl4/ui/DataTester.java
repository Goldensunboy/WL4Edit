package wl4.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;

import wl4.GBAPalette;
import wl4.Map16Tile;
import wl4.Tile8x8;
import wl4.WL4Area;
import wl4.WL4Edit;
import wl4.WL4Level;

/**
 * The purpose of this class is to provide various testing methods for data in the ROM
 * 
 * @author Andrew Wilder
 */
public class DataTester {
	
	/**
	 * Create a new tester using 8x8 tiles, map16, or levels
	 * @param o The data
	 * @param o2 Palettes if making an 8x8 tile tester
	 */
	public DataTester(Object o, Object o2) {
		if(o instanceof WL4Level[]) {
			WL4Level[] levels = (WL4Level[]) o;
			new AreaTestFrame(levels).setVisible(true);
		} else if(o instanceof Map16Tile[][]) {
			Map16Tile[][] map16 = (Map16Tile[][]) o;
			new Map16TestFrame(map16).setVisible(true);
		} else if(o instanceof Tile8x8[][] && o2 instanceof GBAPalette[][]) {
			Tile8x8[][] tiles = (Tile8x8[][]) o;
			GBAPalette[][] pals = (GBAPalette[][]) o2;
			new Tile8x8TestFrame(tiles, pals).setVisible(true);
		} else if(o instanceof Tile8x8[][]) {
			System.out.println("Invalid type for first parameter passed to DataTester");
		} else {
			System.out.println("Invalid type for second parameter passed to DataTester");
		}
	}
	
	/**
	 * Construct the DataTester with one object
	 * @param o The data
	 */
	public DataTester(Object o) {
		this(o, null);
	}
	
	/**
	 * Test the levels. Press up/down to change level, left/right to change area.
	 * 
	 * @author Andrew Wilder
	 */
	private static class AreaTestFrame extends JFrame {
		private static final long serialVersionUID = 1L;
		private static final int SCALE = 2;
		private WL4Level[] levels;
		private int levelIdx = 0;
		private int areaIdx = 0;
		private int mask = 0xF;
		private boolean gbaAB = true;
		public AreaTestFrame(WL4Level[] levels) {
			this.levels = levels;
			this.addWindowListener(new WindowListener() {
				@Override
				public void windowClosing(WindowEvent e) {
					System.exit(0);
				}
				@Override
				public void windowClosed(WindowEvent e) {}
				@Override
				public void windowOpened(WindowEvent e) {}
				@Override
				public void windowIconified(WindowEvent e) {}
				@Override
				public void windowDeiconified(WindowEvent e) {}
				@Override
				public void windowActivated(WindowEvent e) {}
				@Override
				public void windowDeactivated(WindowEvent e) {}
			});
			this.addKeyListener(new KeyListener() {
				@Override
				public void keyPressed(KeyEvent e) {
					if(e.getKeyCode() == KeyEvent.VK_UP) {
						if(levelIdx > 0) {
							areaIdx = 0;
							initializePanel(--levelIdx, areaIdx);
						}
					} else if(e.getKeyCode() == KeyEvent.VK_DOWN) {
						if(levelIdx < WL4Edit.LEVEL_COUNT - 1) {
							areaIdx = 0;
							initializePanel(++levelIdx, areaIdx);
						}
					} else if(e.getKeyCode() == KeyEvent.VK_LEFT) {
						if(areaIdx > 0) {
							initializePanel(levelIdx, --areaIdx);
						}
					} else if(e.getKeyCode() == KeyEvent.VK_RIGHT) {
						if(areaIdx < levels[levelIdx].areas.length - 1) {
							initializePanel(levelIdx, ++areaIdx);
						}
					} else if(e.getKeyCode() == KeyEvent.VK_ESCAPE) {
						System.exit(0);
					} else if(e.getKeyCode() == KeyEvent.VK_1) {
						mask ^= 1;
						repaint();
					} else if(e.getKeyCode() == KeyEvent.VK_2) {
						mask ^= 2;
						repaint();
					} else if(e.getKeyCode() == KeyEvent.VK_3) {
						mask ^= 4;
						repaint();
					} else if(e.getKeyCode() == KeyEvent.VK_4) {
						mask ^= 8;
						repaint();
					} else if(e.getKeyCode() == KeyEvent.VK_A) {
						gbaAB = !gbaAB;
						repaint();
					}
				}
				@Override
				public void keyTyped(KeyEvent e) {
				}
				@Override
				public void keyReleased(KeyEvent e) {}
			});
			initializePanel(0, 0);
		}
		private void initializePanel(int lidx, int aidx) {
			this.setTitle(String.format("Area View: Level %02X, Area %02X", lidx, aidx));
			getContentPane().removeAll();
			JPanel panel = new TestPanel(levels[lidx].areas[aidx]);
			panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
			panel.add(Box.createRigidArea(new Dimension(levels[lidx].areas[aidx].width * 16 * SCALE,
					levels[lidx].areas[aidx].height * 16 * SCALE)));
			add(panel);
			pack();
		}
		private class TestPanel extends JPanel {
			private static final long serialVersionUID = 1L;
			WL4Area area;
			public TestPanel(WL4Area area) {
				this.area = area;
			}
			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				g.setColor(Color.BLACK);
				g.fillRect(0, 0, getWidth(), getHeight());
				area.draw(g, SCALE, mask, gbaAB);
				g.setColor(Color.WHITE);
				g.drawString(String.format("Level: (%02X) Area: (%02X) Layers: (%d %d %d %d) Alpha blending: (%s)",
						levelIdx, areaIdx, mask & 1, (mask >> 1) & 1, (mask >> 2) & 1,
						(mask >> 3) & 1, gbaAB ? "GBA" : "WL4Edit"), 5, getHeight() - 5);
			}
		}
	}
	
	/**
	 * Test the map16 layouts. Press up/down to change level.
	 * 
	 * @author Andrew Wilder
	 */
	private static class Map16TestFrame extends JFrame {
		private static final long serialVersionUID = 1L;
		private static final int SCALE = 3;
		private static final int TILE_COUNT_X = 32;
		private static final int TILE_COUNT_Y = 24;
		private Map16Tile[][] map16arr;
		int mapIdx = 0;
		public Map16TestFrame(Map16Tile[][] map16arr) {
			this.map16arr = map16arr;
			this.addWindowListener(new WindowListener() {
				@Override
				public void windowClosing(WindowEvent e) {
					System.exit(0);
				}
				@Override
				public void windowClosed(WindowEvent e) {}
				@Override
				public void windowOpened(WindowEvent e) {}
				@Override
				public void windowIconified(WindowEvent e) {}
				@Override
				public void windowDeiconified(WindowEvent e) {}
				@Override
				public void windowActivated(WindowEvent e) {}
				@Override
				public void windowDeactivated(WindowEvent e) {}
			});
			this.addKeyListener(new KeyListener() {
				@Override
				public void keyPressed(KeyEvent e) {
					if(e.getKeyCode() == KeyEvent.VK_UP) {
						if(mapIdx > 0) {
							initializePanel(--mapIdx);
						}
					} else if(e.getKeyCode() == KeyEvent.VK_DOWN) {
						if(mapIdx < map16arr.length - 1) {
							initializePanel(++mapIdx);
						}
					} else if(e.getKeyCode() == KeyEvent.VK_ESCAPE) {
						System.exit(0);
					}
				}
				@Override
				public void keyTyped(KeyEvent e) {
				}
				@Override
				public void keyReleased(KeyEvent e) {}
			});
			initializePanel(0);
		}
		private void initializePanel(int idx) {
			this.setTitle(String.format("Map16 View: Level %02X", idx));
			getContentPane().removeAll();
			JPanel panel = new TestPanel(map16arr[mapIdx]);
			panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
			panel.add(Box.createRigidArea(new Dimension(TILE_COUNT_X * 16 * SCALE, TILE_COUNT_Y * 16 * SCALE)));
			add(panel);
			pack();
		}
		private class TestPanel extends JPanel {
			private static final long serialVersionUID = 1L;
			Map16Tile[] map16;
			public TestPanel(Map16Tile[] map16) {
				this.map16 = map16;
			}
			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				g.setColor(Color.BLACK);
				g.fillRect(0, 0, getWidth(), getHeight());
				for(int i = 0; i < TILE_COUNT_Y; ++i) {
					for(int j = 0; j < TILE_COUNT_X; ++j) {
						Map16Tile m16 = map16[j + i * TILE_COUNT_X];
						m16.draw(g, j * 16 * SCALE, i * 16 * SCALE, SCALE);
					}
				}
				g.setColor(Color.WHITE);
				g.drawString(String.format("Level: %02X", mapIdx), 5, getHeight() - 5);
			}
		}
	}
	
	/**
	 * Test the 8x8 tiles. Press up/down to change level, left/right to change palette
	 * 
	 * @author Andrew Wilder
	 */
	private static class Tile8x8TestFrame extends JFrame {
		private static final long serialVersionUID = 1L;
		private static final int SCALE = 4;
		private Tile8x8[][] tilesArr;
		private GBAPalette[][] palArr;
		private int tileIdx = 0;
		private int palIdx = 0;
		private static final int TILE_COUNT_X = 32;
		private static final int TILE_COUNT_Y = 48;
		public Tile8x8TestFrame(Tile8x8[][] tilesArr, GBAPalette[][] palArr) {
			this.tilesArr = tilesArr;
			this.palArr = palArr;
			this.addWindowListener(new WindowListener() {
				@Override
				public void windowClosing(WindowEvent e) {
					System.exit(0);
				}
				@Override
				public void windowClosed(WindowEvent e) {}
				@Override
				public void windowOpened(WindowEvent e) {}
				@Override
				public void windowIconified(WindowEvent e) {}
				@Override
				public void windowDeiconified(WindowEvent e) {}
				@Override
				public void windowActivated(WindowEvent e) {}
				@Override
				public void windowDeactivated(WindowEvent e) {}
			});
			this.addKeyListener(new KeyListener() {
				@Override
				public void keyPressed(KeyEvent e) {
					if(e.getKeyCode() == KeyEvent.VK_UP) {
						if(tileIdx > 0) {
							initializePanel(--tileIdx);
						}
					} else if(e.getKeyCode() == KeyEvent.VK_DOWN) {
						if(tileIdx < tilesArr.length - 1) {
							initializePanel(++tileIdx);
						}
					} else if(e.getKeyCode() == KeyEvent.VK_LEFT) {
						if(palIdx > 0) {
							setPalette(--palIdx);
							repaint();
						}
					} else if(e.getKeyCode() == KeyEvent.VK_RIGHT) {
						if(palIdx < 15) {
							setPalette(++palIdx);
							repaint();
						}
					} else if(e.getKeyCode() == KeyEvent.VK_ESCAPE) {
						System.exit(0);
					}
				}
				@Override
				public void keyTyped(KeyEvent e) {
				}
				@Override
				public void keyReleased(KeyEvent e) {}
			});
			initializePanel(0);
		}
		private void initializePanel(int idx) {
			this.setTitle(String.format("8x8 Tile View: Level %02X", idx));
			setPalette(palIdx);
			getContentPane().removeAll();
			JPanel panel = new TestPanel(tilesArr[idx]);
			panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
			panel.add(Box.createRigidArea(new Dimension(TILE_COUNT_X * 8 * SCALE, TILE_COUNT_Y * 8 * SCALE)));
			add(panel);
			pack();
		}
		private void setPalette(int idx) {
			for(int i = 0; i < tilesArr[tileIdx].length; ++i) {
				tilesArr[tileIdx][i].palette = palArr[tileIdx][palIdx];
			}
		}
		private class TestPanel extends JPanel {
			private static final long serialVersionUID = 1L;
			Tile8x8[] tiles;
			public TestPanel(Tile8x8[] tiles) {
				this.tiles = tiles;
			}
			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				g.setColor(Color.BLACK);
				g.fillRect(0, 0, getWidth(), getHeight());
				int idx = 0;
				for(int i = 0; i < TILE_COUNT_Y; ++i) {
					for(int j = 0; j < TILE_COUNT_X; ++j) {
						Tile8x8 tile = tiles[idx++];
						tile.draw(g, j * 8 * SCALE, i * 8 * SCALE, SCALE);
					}
				}
				g.setColor(Color.WHITE);
				g.drawString(String.format("Level: %02X Palette: %X", tileIdx, palIdx), 5, getHeight() - 5);
			}
		}
	}
}
