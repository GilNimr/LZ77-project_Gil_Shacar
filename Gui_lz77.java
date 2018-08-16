//package gui;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.JTextField;

public class Gui_lz77 {

	private JFrame frame;
	private JPanel menu_panel;
	private JPanel generate_panel;
	private JPanel other_actions_panel;
	private JFileChooser open_file;
	private JFileChooser save_to;
	private String output_file_path;
	private String input_file_path;
	private int action; // 1-compress 2-decompress 3-compressUpgrade
						// 4-DecompressUpgrade
	private LZ77 lz;
	protected String upgrade_side_file_path; // needs to delete this!
	private JTextField textFieldWindow;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Gui_lz77 window = new Gui_lz77();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public Gui_lz77() {
		lz = new LZ77(3,5);
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.getContentPane().setBackground(new Color(255, 255, 204));
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new CardLayout(0, 0));

		menu_panel = new JPanel();
		menu_panel.setBackground(new Color(255, 255, 204));
		frame.getContentPane().add(menu_panel, "name_132382732624764");
		menu_panel.setLayout(null);

		JButton generateButton = new JButton("Generate txt file");
		generateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				generate_panel.setVisible(true);
				menu_panel.setVisible(false);
			}
		});
		generateButton.setBounds(10, 11, 161, 30);
		menu_panel.add(generateButton);

		JButton compressButton = new JButton("Compress");
		compressButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				action = 1;
				other_actions_panel.setVisible(true);
				menu_panel.setVisible(false);
			}
		});
		compressButton.setBounds(10, 52, 161, 30);
		menu_panel.add(compressButton);

		JButton decompressButton = new JButton("Decompress");
		decompressButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				action = 2;
				other_actions_panel.setVisible(true);
				menu_panel.setVisible(false);
			}
		});
		decompressButton.setBounds(10, 93, 161, 30);
		menu_panel.add(decompressButton);

		JButton compressUpgradeButton = new JButton("Compress upgraded");
		compressUpgradeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				action = 3;
				other_actions_panel.setVisible(true);
				menu_panel.setVisible(false);
			}
		});
		compressUpgradeButton.setBounds(10, 133, 161, 30);
		menu_panel.add(compressUpgradeButton);

		JButton decompressUpgradeButton = new JButton("Decompress upgraded");
		decompressUpgradeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				action = 4;
				other_actions_panel.setVisible(true);
				menu_panel.setVisible(false);
			}
		});
		decompressUpgradeButton.setBounds(10, 180, 161, 30);
		menu_panel.add(decompressUpgradeButton);

		generate_panel = new JPanel();
		generate_panel.setBackground(new Color(255, 255, 204));
		frame.getContentPane().add(generate_panel, "name_132393387881191");
		generate_panel.setLayout(null);

		JLabel directoryGenerateLable = new JLabel("");
		directoryGenerateLable.setBackground(new Color(255, 255, 255));
		directoryGenerateLable.setBounds(180, 10, 200, 23);
		generate_panel.add(directoryGenerateLable);

		JButton chooseButton = new JButton("Choose directory");
		chooseButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				open_file = new JFileChooser();
				open_file.setCurrentDirectory(null);
				open_file.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int returnvalue = open_file.showOpenDialog(null);
				if (returnvalue == JFileChooser.APPROVE_OPTION) {
					output_file_path = open_file.getSelectedFile()
							.getAbsolutePath() + "\\\\generated.txt";
					directoryGenerateLable.setText(open_file.getSelectedFile()
							.getAbsolutePath());

				} else {
					directoryGenerateLable.setText("problem loading directory");
				}
			}
		});
		chooseButton.setBounds(10, 10, 144, 23);
		generate_panel.add(chooseButton);
		
		JLabel lbGeneratingDone = new JLabel("");
		lbGeneratingDone.setBounds(175, 170, 82, 14);
		generate_panel.add(lbGeneratingDone);

		JButton generate2Button = new JButton("Generate text");
		generate2Button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				lbGeneratingDone.setText("generating..");
				lz.Generate_String(output_file_path);
				lbGeneratingDone.setText("done!");
			}
		});
		generate2Button.setBounds(124, 107, 184, 36);
		generate_panel.add(generate2Button);

		JButton btnBack = new JButton("back");
		btnBack.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				generate_panel.setVisible(false);
				menu_panel.setVisible(true);
			}
		});
		btnBack.setBounds(10, 202, 68, 23);
		generate_panel.add(btnBack);

		

		other_actions_panel = new JPanel();
		other_actions_panel.setBackground(new Color(255, 255, 204));
		frame.getContentPane().add(other_actions_panel, "name_132440449940290");
		other_actions_panel.setLayout(null);

		JLabel lbOpenFile = new JLabel("");
		lbOpenFile.setBounds(210, 10, 200, 23);
		other_actions_panel.add(lbOpenFile);

		JLabel lbSaveAt = new JLabel("");
		lbSaveAt.setBounds(210, 44, 200, 23);
		other_actions_panel.add(lbSaveAt);

		JButton btnOpenFile = new JButton("Open file");
		btnOpenFile.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				open_file = new JFileChooser();
				open_file.setCurrentDirectory(null);
				open_file.setFileFilter(new FileNameExtensionFilter(
						"TEXT FILES", "txt", "text"));
				int returnvalue = open_file.showOpenDialog(null);
				if (returnvalue == JFileChooser.APPROVE_OPTION) {
					input_file_path = open_file.getSelectedFile()
							.getAbsolutePath();
					lbOpenFile.setText(open_file.getSelectedFile()
							.getAbsolutePath());
					System.out.println(input_file_path);

				} else {
					lbOpenFile.setText("problem loading directory");
				}
			}
		});
		btnOpenFile.setBounds(10, 10, 149, 23);
		other_actions_panel.add(btnOpenFile);

		JButton btnSaveAt = new JButton("Save at");
		btnSaveAt.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				save_to = new JFileChooser();
				save_to.setCurrentDirectory(null);
				save_to.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int returnvalue = save_to.showOpenDialog(null);
				if (returnvalue == JFileChooser.APPROVE_OPTION) {
					if (action == 1 || action == 3)
						output_file_path = save_to.getSelectedFile()
								.getAbsolutePath() + "\\\\compressed.txt";
					else
						output_file_path = save_to.getSelectedFile()
								.getAbsolutePath() + "\\\\returned.txt";
					lbSaveAt.setText(save_to.getSelectedFile()
							.getAbsolutePath());
					System.out.println(output_file_path);

				} else {
					lbSaveAt.setText("problem loading directory");
				}
			}
		});
		btnSaveAt.setBounds(10, 44, 149, 23);
		other_actions_panel.add(btnSaveAt);

		JLabel lbcomputing_done = new JLabel("");
		lbcomputing_done.setBounds(200, 221, 71, 14);
		other_actions_panel.add(lbcomputing_done);
		
		JLabel lbBitsWindow = new JLabel("Size in bits of sliding window:");
		lbBitsWindow.setBounds(10, 89, 143, 14);
		other_actions_panel.add(lbBitsWindow);
		
		JLabel lbBitsLook = new JLabel("Size in bits of the look a head buffer:");
		lbBitsLook.setBounds(10, 114, 185, 14);
		other_actions_panel.add(lbBitsLook);
		
		JLabel lbLook = new JLabel("5");
		lbLook.setBounds(260, 114, 88, 14);
		other_actions_panel.add(lbLook);
		
		textFieldWindow = new JTextField();
		textFieldWindow.setText("3");
		textFieldWindow.setBounds(262, 86, 86, 20);
		other_actions_panel.add(textFieldWindow);
		textFieldWindow.setColumns(10);

		JButton btnGo = new JButton("Go");
		btnGo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int sliding_window, look_a_head;
				try{
					sliding_window=Integer.parseInt(textFieldWindow.getText());
					if(sliding_window>7||sliding_window<1){
	
						throw new Exception();
						
					}
		            look_a_head=8-sliding_window;
		            lbLook.setText(Integer.toString(look_a_head));
		           lz=new LZ77(sliding_window,look_a_head);
					}
				catch(Exception e1){
					JOptionPane.showMessageDialog(null, "please enter a valid number between 1-7");
					action=5;
				}
				if (action == 1) {
					lbcomputing_done.setText("compressing..");
					lz.Compress(input_file_path, output_file_path);
					lbcomputing_done.setText("done!");
					
				} else if (action == 2) {
					lbcomputing_done.setText("decompressing..");
					lz.Decompress(input_file_path, output_file_path);
					lbcomputing_done.setText("done!");
				} else if (action == 3) {
					lbcomputing_done.setText("compressing..");
					lz.CompressWithUpgrade(input_file_path, output_file_path);
					lbcomputing_done.setText("done!");
				} else if (action == 4) {
					lbcomputing_done.setText("decompressing..");
					lz.Decompress_Upgraded(input_file_path, output_file_path);
					lbcomputing_done.setText("done!");
				}
			}
		});
		btnGo.setBounds(133, 149, 200, 50);
		other_actions_panel.add(btnGo);

		JButton btnBack_1 = new JButton("back");
		btnBack_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				other_actions_panel.setVisible(false);
				lbOpenFile.setText("");
				lbSaveAt.setText("");
				input_file_path=null;
				output_file_path=null;
				menu_panel.setVisible(true);
			}
		});
		btnBack_1.setBounds(10, 217, 71, 23);
		other_actions_panel.add(btnBack_1);
		
		

	}
}
